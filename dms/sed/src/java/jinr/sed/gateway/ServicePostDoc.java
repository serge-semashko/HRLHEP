/**
 * Стандартный сервис посылки документа в шлюз
 */

package jinr.sed.gateway;


import dubna.walt.util.IOUtil;
import jinr.sed.ServiceEditDocData;
import jinr.sed.ServiceViewDoc;
import dubna.walt.util.gateway.Utils;

/**
 * Просмотр документа
 */
public class ServicePostDoc extends ServiceViewDoc {

    String[] fields_types;

    /**
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        timer = System.currentTimeMillis();
        try {
            initSuper(); // чтобы родительский класс инициализировал необходимые переменные
            cfgTuner.outCustomSection("report header", out); // вывод начала формы

            if (cfgTuner.enabledOption("DOC_DATA_RECORD_ID")) {
                makeSelectSQL(false);
// выполняем запрос к базе
                getPreData(sql);
            }

// выводим поля документа для вытаскивания из базы спец.полей
            makeTable();

// Готовим параметры запроса
            String encodedData = Utils.getEncodedSection("post params", rm);

// Готовим сам JSON по документу
            String object = "{" + Utils.getEncodedJSON("object header", rm, false) + makeJSON() + "}";
        IOUtil.writeLogLn(2, "<hr><b>OBJECT: </b><xmp>" + object + "</xmp>", rm);
            encodedData += "&Object=" + Utils.encodeString(object, rm);
            cfgTuner.addParameter("objectJSON", object);  //для отладки

//    IOUtil.writeLogLn("<hr><b>ALL - encodedData:</b><br><xmp>" + encodedData + "</xmp>", rm);
            
            cfgTuner.addParameter("encodedData", encodedData);  //для отладки

            long t1 = System.currentTimeMillis();
            rm.setParam("log", "true");
            IOUtil.writeLogLn(1, "<hr>***** TIMER 1 (prepare data): " + Long.toString(t1 - timer), rm);

// Посылаем запрос и читаем ответ             
            String responce = Utils.postRequest(cfgTuner.getParameter("GatewayHost"), encodedData, rm);
            long t2 = System.currentTimeMillis();
            IOUtil.writeLogLn(1, "***** TIMER 2 (post data): " + Long.toString(t2 - t1), rm);

            IOUtil.writeLogLn(1, "RESPONCE:" + responce, rm);
            cfgTuner.addParameter("responce", responce);
            Utils.parseJson(responce, rm);

        } catch (Exception e) {
            e.printStackTrace(System.out);
            IOUtil.writeLogLn("XXXXXXXX Exception: " + e.toString(), rm);
            cfgTuner.addParameter("ERROR", e.toString());
            cfgTuner.addParameter("Result", e.toString());
        } finally {
// выводим завершение формы 
            cfgTuner.outCustomSection("report footer", out);
            out.flush();
        }
    }

    private String makeJSON() {
        String json = " ";
        IOUtil.writeLogLn(1, "<hr><b>makeJSON:</b>", rm);
        for (int i = 0; i < numFields; i++) {
            if (ServiceEditDocData.typesIsCollection.contains(form_fields_types[i])) {
                try{
//                    cfgTuner.addParameter("table_part_type_id", fields_types[i]);
                    cfgTuner.storeParameters();
                    callService("c=gateway/get_table_part; table_part_type_id=" + fields_types[i]);
                    json += ",\"" + fields[i] + "\":[" + cfgTuner.getParameter("collection_json") + "]\n";
                    cfgTuner.restoreParameters();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    IOUtil.writeLogLn("EXCEPTION:" + e.toString(), rm);
                }
            } else {
                if (ServiceEditDocData.typesNeedIdField.contains(form_fields_types[i])) {
                    json += ",\"" + fields[i] + "_id\":\"" + cfgTuner.getParameter(fields[i] + "_id") + "\" ";
                }
//                json += ",\"" + fields[i] + "\":\"" + cfgTuner.getParameter(fields[i]).replaceAll("\"", "\\\\\"") + "\"\n";
                json += ",\"" + fields[i] + "\":\"" + cfgTuner.getParameter(fields[i]).replaceAll("\"", "\\\\\"").replaceAll("`", "\'") + "\"\n";
            }
        }
        return json;
    }

    /**
     * Конструирует SQL запрос на выборку полей документа
     *
     * @param doFormat - не используется
     */
    @Override
    protected void makeSelectSQL(boolean doFormat) {
//    получаем список полей документа, их названия и типы из описания документа
// (результат выполнения запросов в секции [preSQLs] .cfg - файла)
        fields = cfgTuner.getParameter("FIELDS").split(",");
//		  fields_names = cfgTuner.getParameter( "FIELDS_NAMES" ).split( "," );
        fields_types = cfgTuner.getParameter("FIELDS_TYPES").split(",");
        form_fields_types = cfgTuner.getParameter("FORM_FIELDS_TYPES").split(",");
        numFields = fields.length;

// конструируем SQL для выборки значений полей документа
        sql = "select ";
        for (int i = 0; i < numFields; i++) {
            if (fields_types[i].equals("float0")) {
                sql += "replace(FORMAT(" + fields[i] + ",2),',','') as \"" + fields[i] + "\", ";
            } else {
                sql += fields[i] + ", ";
            }
            cfgTuner.addParameter(fields[i], "");
            if (ServiceEditDocData.typesNeedIdField.contains(form_fields_types[i])) {
                sql += fields[i] + "_id, ";
                cfgTuner.addParameter(fields[i] + "_id", "");
            }
        }
        sql += cfgTuner.getParameter("SYS_FIELDS")
                + " from " + cfgTuner.getParameter("TABLE_NAME")
                + " where id=" + cfgTuner.getParameter("DOC_DATA_RECORD_ID");
        IOUtil.writeLogLn(2, "+++ Get doc data record SQL: " + sql, rm);

    }

}
