package jinr.sed.gateway;

import dubna.walt.util.IOUtil;
import dubna.walt.util.gateway.Utils;
import jinr.sed.ServiceEditDocData;
import jinr.sed.ServiceViewDoc;

/**
 *
 * @author serg
 */
public class ServiceObject2Json extends ServiceViewDoc {

    /**
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        
        String collection_json = cfgTuner.getParameter("collection_json");

        try {
            initSuper(); // чтобы родительский класс инициализировал необходимые переменные
            cfgTuner.getCustomSection("report header"); // подготовка данных

            if (cfgTuner.enabledOption("DOC_DATA_RECORD_ID")) {
// готовим и выполняем запрос к базе
                makeSelectSQL(false);
                getPreData(sql);
            }

// Готовим фиксированное содержимое документа
            String json = "{" + Utils.getEncodedJSON("object header", rm, false);
// Добавляем динамическое содержимое документа
            for (int i = 0; i < numFields; i++) {
                IOUtil.writeLogLn(fields[i] + "=" + cfgTuner.getParameter(fields[i]).replaceAll("\"", "`"), rm);
                if (ServiceEditDocData.typesNeedIdField.contains(form_fields_types[i])) {
//                    encodedData += URLEncoder.encode(",\"" + fields[i] + "\":\"", "utf-8") 
//                         + URLEncoder.encode(cfgTuner.getParameter(fields[i] + "_id") + "\"", "utf-8");
                    json += ",\"" + fields[i] + "_id\":\"" + cfgTuner.getParameter(fields[i] + "_id") + "\" ";
                }
//                encodedData += URLEncoder.encode(",\"" + fields[i]+ "\":\"", "utf-8") 
//                            + URLEncoder.encode(cfgTuner.getParameter(fields[i]) + "\"", "utf-8");
//                json += ",\"" + fields[i] + "\":\"" + cfgTuner.getParameter(fields[i]).replaceAll("\"", "`") + "\"\n";
//                json += ",\"" + fields[i] + "\":\"" + cfgTuner.getParameter(fields[i]).replaceAll("\"", "\\\"") + "\"\n";
                json += ",\"" + fields[i] + "\":\"" + cfgTuner.getParameter(fields[i]).replaceAll("\"", "*") + "\"\n";
            }

//            cfgTuner.addParameter("encodedObjectData", prev_data + encodedData + URLEncoder.encode("}", "utf-8") ); 
            cfgTuner.addParameter("object_json", json+ "}");
            if(collection_json.length() > 0) collection_json +=", ";
            collection_json += json+ "}";
            cfgTuner.addParameter("collection_json", collection_json);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            IOUtil.writeLogLn("XXXXXXXX Exception: " + e.toString(), rm);
            cfgTuner.addParameter("ERROR", e.toString());
        } finally {
// выводим секцию для отладки 
            cfgTuner.outCustomSection("report footer", out);
            out.flush();
        }
    }


    /**
     * Конструирует SQL запрос на выборку полей документа
     *
     */
    @Override
    protected void makeSelectSQL(boolean doFormat) {
//    получаем список полей документа, их названия и типы из описания документа
// (результат выполнения запросов в секции [preSQLs] .cfg - файла)
        fields = cfgTuner.getParameter("FIELDS").split(",");
//		  fields_names = cfgTuner.getParameter( "FIELDS_NAMES" ).split( "," );
        String[] fields_types = cfgTuner.getParameter("FIELDS_TYPES").split(",");
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
