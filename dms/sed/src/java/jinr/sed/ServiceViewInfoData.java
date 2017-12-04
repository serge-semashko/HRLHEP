package jinr.sed;

import java.sql.ResultSet;
import dubna.walt.util.IOUtil;
import java.sql.ResultSetMetaData;

/**
 * Сервис используется для вывода всех полей справочника В АДМИНКЕ Выводятся все
 * имеющиеся поля таблицы.
 */
public class ServiceViewInfoData extends dubna.walt.service.Service {

    protected int nr;
    protected int numFields = 0;
    protected int numCols = 0;
    protected String[] fields_types = null;
    protected String[] fields = null;
    protected String[] fields_names = null;
    protected String[] fields_manual = null;

    protected String[] searchSubstrings = null;

    /**
     * Основной метод сервиса - определяет порядок обработки запроса.
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        try {
            //		получаем список полей справочника, их названия и типы из описания справочника
            // (результат выполнения запросов в секции [preSQLs] .cfg - файла)
            fields = cfgTuner.getParameter("FIELDS").split(",");
            fields_names = cfgTuner.getParameter("FIELDS_NAMES").split(",");
            fields_types = cfgTuner.getParameter("FIELDS_TYPES").split(",");
            fields_manual = cfgTuner.getParameter("FIELDS_MANUAL").split(",");
            numFields = cfgTuner.getIntParameter("NUM_FIELDS");

            // формируем заголовки столбцов таблицы
            String h = "";
            String cl;
            for (int i = 0; i < numFields; i++) {
                if (fields_manual[i].equals("1")) {
                    cl = "MANUAL ";
                } else {
                    cl = "";
                }
                h = h + "<th class='" + cl 
//                        + "  style='padding: 2px;' "
                        +" srh' sr='" + fields[i] + "'>" + fields_names[i] + "</th>";
            }
            cfgTuner.addParameter("TableColsHeaders", h);

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
     * Вывод таблицы записей справочника
     *
     * @throws Exception
     */
    protected void showInfoRecords() throws Exception {
        
        // конструируем критерий запроса
        makeSearchCriteria();
        //  получаем общее кол-во записей, соотв. критерию поиска.  
        String s = "select count(*) as NUM_RECORDS from "
                + cfgTuner.getParameter("TABLE_NAME")
                + " " + cfgTuner.getParameter("CRITERIA");
        IOUtil.writeLogLn(3, "+++ COUNT RECORDS SQL: '" + s + "'", rm);
        ResultSet r = dbUtil.getResults(s); // выполняем запрос
        if (r.next()) {
            int tot_num_recs = r.getInt(1);
            r.close();
            IOUtil.writeLogLn(3, "+++ TOT_NUM_RECS=" + Integer.toString(tot_num_recs), rm);
            cfgTuner.addParameter("TOT_NUM_RECS", Integer.toString(tot_num_recs));
            if (tot_num_recs > 0) {

                int start_rec = cfgTuner.getIntParameter(null, "START_REC", 1); //начальная запись
                if (start_rec < 1) {
                    start_rec = 1;
                }
//				int isrn = cfgTuner.getIntParameter( null, "isrn", 0 ); //начальная запись
                int isrn = start_rec - 1;
                int irpp = cfgTuner.getIntParameter(null, "irpp", 20); // кол-во записей на странице
//				cfgTuner.addParameter( "START_REC", Integer.toString( isrn + 1 ) );
                int lastRec = isrn + irpp;
                if (lastRec > tot_num_recs) {
                    lastRec = tot_num_recs;
                }
                cfgTuner.addParameter("END_REC", Integer.toString(lastRec));
                // Составляем запрос в БД
                s = "select " + cfgTuner.getParameter("FIELDS")
                        + cfgTuner.getParameter("SYS_FIELDS")
                        + " from " + cfgTuner.getParameter("TABLE_NAME")
                        + " " + cfgTuner.getParameter("CRITERIA")
                        + " order by "  + cfgTuner.getParameter("ORDER_BY") + " id LIMIT " + isrn + "," + irpp;

                IOUtil.writeLogLn("+++ GET RECORDS SQL: '" + s + "'", rm);
                r = dbUtil.getResults(s); // выполняем запрос
                ResultSetMetaData metaData = r.getMetaData();
                numCols = metaData.getColumnCount(); // кол-во полученных столбцов

                int id;
                int numRecs = 0;
                while (r.next()) // цикл по полученным записям
                {
                    id = r.getInt(1); // первое поле - всегда ID записи 
                    cfgTuner.addParameter("record_id", Integer.toString(id)); // заносим record_id в Tuner
                    String record = "<td class='right'>" + id + "</td>"; // добавляем 1-ю колонку
                    String val = "";
                    for (int i = 2; i <= numFields; i++) // цикл по полям записи
                    {
                        val = r.getString(i);
                        if (fields_types[i - 1].equals("int") || fields_types[i - 1].equals("float")) {
                            if (val == null || val.equals("null")) {
                                record += "<td class='right'></td>";
                            } else {
                                record += "<td class='right'>" + val + "</td>";
                            }
                        } else if (fields_types[i - 1].equals("varchar")) {
                            record += "<td>" + markSearchItems(val) + "</td>";
                        } //							record += "<td>" + r.getString( i ) + "</td>";
                        else if (fields_types[i - 1].equals("date")) {
                            record += "<td>" + val + "</td>";
                        } else if (fields_types[i - 1].equals("datetime")) {
                            record += "<td>" + val + "</td>";
                        } else {
                            System.out.println(" !!!!! UNKNOWN TYPE: " + fields_types[i - 1]);
                        }
                    }
                    cfgTuner.addParameter("record", record); // заносим в Tuner полученную строку
                    for (int i = numFields + 1; i <= numCols; i++) // выбираем служебные (не отображаемые) поля
                    {
                        cfgTuner.addParameter(metaData.getColumnLabel(i), r.getString(i)); // и пихаем их в Tuner
                    }
                    cfgTuner.outCustomSection("item", out); // выводим строку
                    numRecs++;
                }
                r.close();
                if (numRecs == irpp) {
                    cfgTuner.addParameter("HAS_NEXT", "Y");
                }
                if (start_rec > 1) {
                    cfgTuner.addParameter("HAS_PREV", "Y");
                }
            }
        }

    }

    /**
     * Выделение подстроки поиска в результатах запроса
     * 
     * @param val
     * @return
     */
    protected String markSearchItems(String val) {
        if (searchSubstrings == null) {
            return val;
        }
        String s = val;
        for (int k = 0; k < searchSubstrings.length; k++) {
            if (s != null && !s.isEmpty()) {
                String vAL = s.toUpperCase();
                int i = vAL.indexOf(searchSubstrings[k]);
                if (i >= 0) {
                    s = s.substring(0, i) + "<span class='mark'>" + s.substring(i, i + searchSubstrings[k].length()) + "</span>" + s.substring(i + searchSubstrings[k].length());
                }
            }
        }
        //    val = val.toUpperCase().replaceAll(searchSubstrings[k].toUpperCase(), "<span class='mark'>" + searchSubstrings[k].toUpperCase() + "</span>");
        return s;
    }

    /**
     * формируем фильтры (where в SQL), если была задана строка поиска
     *
     */
    protected void makeSearchCriteria() {
        //    System.out.println( "+++ searchFor=" + cfgTuner.getParameter( "searchFor" ) );
        if (cfgTuner.enabledOption("searchFor")) {
            //          String searchFor = cfgTuner.getParameter("searchFor");
            searchSubstrings = cfgTuner.getParameter("searchFor").split("[\\s\\.,]");
            for (int i = 0; i < searchSubstrings.length; i++) {
                searchSubstrings[i] = searchSubstrings[i].toUpperCase();
                addSearchCriteria(searchSubstrings[i]);
            }
        }
    }

    /**
     * Вормирование и добавление в запрос критерия поиска, если была задани строка поиска
     * 
     * @param searchSubstring 
     */
    
    protected void addSearchCriteria(String searchSubstring) {
        // 
        if (searchSubstring.length() < 1) {
            return;
        }
//        String searchFor = cfgTuner.getParameter("searchFor");
        Integer i_searchFor = null;
        try {
            i_searchFor = Integer.parseInt(searchSubstring);
        } catch (NumberFormatException nfe) {
            i_searchFor = null;
        }
        String f = "";
        for (int i = 0; i < fields.length; i++) {
            if (fields_types[i].equals("int")) {
                if (i_searchFor != null) {
                    if(fields[i].equalsIgnoreCase("id") || fields[i].equalsIgnoreCase("pid")) 
                        f += " or replace(format(" + fields[i] + ",0), ',','') like '" + searchSubstring + "%'";
                    else
                        f += " or " + fields[i] + "=" + searchSubstring;
                }
            } else {
                f += " or " + fields[i] + " like ('%" + searchSubstring + "%')";
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
