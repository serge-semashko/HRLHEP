package jinr.sed;

import java.sql.ResultSet;
import dubna.walt.util.IOUtil;
import dubna.walt.util.Tuner;
import java.sql.ResultSetMetaData;
import jinr.sed.tools.CommonTools;

/**
 * Сервис используется для вывода справочника в pop-up окно для выбора записи
 * Выводятся только поля, выбранные для заданного представления справочника
 */
public class ServiceShowInfoData extends ServiceViewInfoData {

    private String[] fields_order = null;
    private String orderByField = null;

    /**
     * Основной метод сервиса - определяет порядок обработки запроса.
     *
     * @throws Exception
     */
    public void start() throws Exception {
        try {
            // получаем список полей справочника, их названия и типы из описания справочника
            // (результат выполнения запросов в секции [preSQLs] .cfg - файла)
            fields = cfgTuner.getParameter("FIELDS").split(",");
            fields_names = cfgTuner.getParameter("FIELDS_NAMES").split(",");
            fields_types = cfgTuner.getParameter("FIELDS_TYPES").split(",");
            fields_order = cfgTuner.getParameter("FIELDS_ORDER").split(",");
            orderByField = cfgTuner.getParameter("orderByField");

            numFields = cfgTuner.getIntParameter("NUM_FIELDS");

            // формируем заголовки столбцов таблицы
            String h = "";
            for (int i = 0; i < numFields; i++) {
                int order = Integer.parseInt(fields_order[i]);
                if (order > 0) //поле отображается 
                {
                    h = h + "<th>" + fields_names[i] + "</th>"; // - делаем заголовок
                    if (orderByField == null || orderByField.isEmpty()) {
                        orderByField = fields[i];
                    }
                }
                cfgTuner.addParameter("TableColsHeaders", h);
            }
            cfgTuner.outCustomSection("report header", out); // Выводим начало таблицы включая заголовки столбцов
            showInfoRecords(); // показываем записи справочника
        } catch (Exception e) {
            e.printStackTrace(System.out);
            IOUtil.writeLogLn("XXXXXXXX Exception: " + e.toString(), rm);
            cfgTuner.addParameter("ERROR", e.toString());
        } finally {
            cfgTuner.outCustomSection("report footer", out);
            out.flush();
        }
    }

    /**
     * Формирование, выполнение запроса в базу и вывод HTML-таблицы записей
     * справочника
     *
     * @throws Exception
     */
    protected void showInfoRecords() throws Exception {
        // конструируем SQL-запрос в базу
        String fields = cfgTuner.getParameter("FIELDS");
        fields = fields.substring(0, fields.length() - 1);
        makeSearchCriteria();
        String[] fields_a = fields.split(",");
        String[] fields_types = cfgTuner.getParameter("FIELDS_TYPES").split(",");
        //	получаем общее кол-во записей, соотв. критерию поиска.	
        String s = "select count(*) as NUM_RECORDS from " + cfgTuner.getParameter("TABLE_NAME") + " " + cfgTuner.getParameter("CRITERIA");
        ResultSet r = dbUtil.getResults(s); // выполняем запрос
        IOUtil.writeLogLn("+++ COUNT RECORDS SQL: '" + s + "'", rm);
        try{
        if (r.next()) {
            int tot_num_recs = r.getInt(1);
            r.close();
            cfgTuner.addParameter("TOT_NUM_RECS", Integer.toString(tot_num_recs));
            if (tot_num_recs > 0) {
                int start_rec = cfgTuner.getIntParameter(null, "START_REC", 1); //начальная запись
                if (start_rec < 1) {
                    start_rec = 1;
                }
                int isrn = start_rec - 1;
                int irpp = cfgTuner.getIntParameter(null, "irpp", 20); // кол-во записей на странице
                //			  cfgTuner.addParameter( "START_REC", Integer.toString( isrn+1 ) );
                int lastRec = isrn + irpp;
                if (lastRec > tot_num_recs) {
                    lastRec = tot_num_recs;
                }
                cfgTuner.addParameter("END_REC", Integer.toString(lastRec));
                
                String f = "";
                for (int i=0; i< fields_a.length; i++) {
                    f += CommonTools.makeSelectField(fields_a[i], fields_types[i], cfgTuner) + ", ";
                }
                //	запрос на выборку данных
                s = "select " + f
//                        + cfgTuner.getParameter("FIELDS") 
                        + cfgTuner.getParameter("SYS_FIELDS")
                        + " from " + cfgTuner.getParameter("TABLE_NAME") + " "
                        + cfgTuner.getParameter("CRITERIA") + " order by " + orderByField
                        + " LIMIT " + isrn + "," + irpp;
                IOUtil.writeLogLn("+++ GET RECORDS SQL: '" + s + "'", rm);
                r = dbUtil.getResults(s); // выполняем запрос
                ResultSetMetaData metaData = r.getMetaData();
                numCols = metaData.getColumnCount();

                int numRecs = 0;
                while (r.next()) // цикл по полученным записям
                {
                    String record = "";
                    String val = "";
                    for (int i = 1; i <= numFields; i++) // цикл по полям записи
                    {
                        val = r.getString(i);
                        if (!fields_order[i - 1].equals("0")) { //поле отображается
                            cfgTuner.addParameter("field_type", fields_types[i - 1]);
                            if(val == null) 
                                cfgTuner.addParameter("val","");
                            else 
                                cfgTuner.addParameter("val", (fields_types[i - 1].equals("varchar") ? markSearchItems(val) : val));
                            record += cfgTuner.getCustomSectionAsString("record") != null ? cfgTuner.getCustomSectionAsString("record") : "";
                            // record += "<td class='right'>" + (fields_types[i - 1].equals( "varchar" )?markSearchItems(val):val) + "</td>"; //формируем HTML
                        }

                        if (i == 1) {
                            cfgTuner.addParameter("returnId", val); // Возвращаем 1-е по порядку поле, как ID
                        } else if (i == 2) {
                            cfgTuner.addParameter("returnValue", val.replaceAll("\"", "``")); // Возвращаем 2-е по порядку поле, как текст с заменой двойных кавычек
                        }
                    }
                    try {
//                    IOUtil.writeLogLn("+++ GET RECORDS ID: field='" + (numFields + 1) + "'", rm);
                        cfgTuner.addParameter("recordId", Integer.toString(r.getInt(numFields + 1))); //в конце идет ID записи в справочнике
                    }
                    catch (Exception ee) {
                        ee.printStackTrace();
                    }
                    cfgTuner.addParameter("record", record); // заносим в Tuner полученную строку
//					for( int i = numFields + 1; i <= numCols; i++ ) // выбираем оставшиеся служебные (не отображаемые) поля
//						cfgTuner.addParameter( metaData.getColumnLabel( i ), r.getString( i ) ); // и пихаем их в Tuner. Пока это не используется. На всякий случай, оставлено.
                    cfgTuner.outCustomSection("item", out); // выводим строку
                    numRecs++;
                }
                r.close();
                if (numRecs == irpp) {
                    cfgTuner.addParameter("HAS_NEXT", "Y");
                }
                if (isrn > 0) {
                    cfgTuner.addParameter("HAS_PREV", "Y");
                }
            }
        }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    protected void addSearchCriteria(String searchSubstring) {
        // 
        if (searchSubstring.length() < 1) {
            return;
        }
        String[] all_fields = cfgTuner.getParameter("ALL_FIELDS").split(",");
        String[] all_fields_types = cfgTuner.getParameter("ALL_FIELDS_TYPES").split(",");
        String searchFor = cfgTuner.getParameter("searchFor");
        Integer i_searchFor = null;
        try {
            i_searchFor = Integer.parseInt(searchSubstring);
        } catch (NumberFormatException nfe) {
            i_searchFor = null;
        }
        String f = "";
        for (int i = 0; i < all_fields.length; i++) {
            if (all_fields_types[i].equals("int")) {
                if (i_searchFor != null) {
                    f += " or " + all_fields[i] + "=" + searchSubstring;
                }
            } else {
                f += " or " + all_fields[i] + " like ('%" + searchSubstring + "%')";
            }
        }
        if (f.length() > 4) {
            String criteria = cfgTuner.getParameter("CRITERIA");
            if (criteria.length() < 1) {
                criteria = " where ";
            } else {
                criteria += " and ";
            }
            criteria += "(" + f.substring(4) + ")";
            cfgTuner.addParameter("CRITERIA", criteria);
        }
    }

}
