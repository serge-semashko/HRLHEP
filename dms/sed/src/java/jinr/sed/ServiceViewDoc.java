package jinr.sed;

import dubna.walt.util.IOUtil;

/**
 * Просмотр документа
 */
public class ServiceViewDoc extends dubna.walt.service.TableServiceSpecial {

    protected int numFields = 0;
    protected String[] form_fields_types = null;
    protected String[] fields = null;
    protected String sql = "";

    /**
     * Основной метод сервиса - определяет порядок обработки запроса.
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        try {
            initSuper(); // чтобы родительский класс инициализировал необходимые переменные
            cfgTuner.outCustomSection("report header", out); // вывод начала формы
            if (cfgTuner.enabledOption("DOC_DATA_RECORD_ID")) {
                makeSelectSQL(true);
// выполняем запрос к базе
                try {
                    getPreData(sql);
                }
                catch(java.sql.SQLException ex) {
                    if (cfgTuner.enabledOption("ADMIN_REQUEST=Y")) {
                       IOUtil.writeLogLn("XXXXXXXX Exception: " + ex.toString(), rm);
                       ex.printStackTrace(System.out);
                       cfgTuner.addParameter("DB_ERROR", ex.toString());
                    }
                    else {throw ex;}
                }
            }

// выводим форму с полями документа
            makeTable();

        } catch (Exception e) {
            e.printStackTrace(System.out);
            IOUtil.writeLogLn("XXXXXXXX Exception: " + e.toString(), rm);
            cfgTuner.addParameter("ERROR", e.toString());
        } finally {
// выводим завершение формы 
            cfgTuner.outCustomSection("report footer", out);
            out.flush();
        }
    }
    
    /**
     * Конструирует SQL запрос на выборку полей документа
     * 
     */
    protected void makeSelectSQL(boolean doFormat) {
//    получаем список полей документа, их названия и типы из описания документа
// (результат выполнения запросов в секции [preSQLs] .cfg - файла)
                fields = cfgTuner.getParameter("FIELDS").split(",");
//		  fields_names = cfgTuner.getParameter( "FIELDS_NAMES" ).split( "," );
		String[] fields_types = cfgTuner.getParameter( "FIELDS_TYPES" ).split( "," );
                form_fields_types = cfgTuner.getParameter("FORM_FIELDS_TYPES").split(",");
                numFields = fields.length;

// конструируем SQL для выборки значений полей документа
                sql = "select ";
                for (int i = 0; i < numFields; i++) {
                    if(fields_types[i].equals("float0") && doFormat)                        
                        sql += "replace(FORMAT(" + fields[i] + ",2),',',' ') as \"" + fields[i] + "\", ";
                    else
                        sql += fields[i] + ", ";
                    cfgTuner.addParameter(fields[i], "");
                    if (ServiceEditDocData.typesNeedIdField.contains(form_fields_types[i])) {
                        sql += fields[i] + "_id, ";
                        cfgTuner.addParameter(fields[i] + "_id", "");
                    }
                }
                sql += cfgTuner.getParameter("SYS_FIELDS")
                        + " from " + cfgTuner.getParameter("TABLE_NAME")
                        + " where id=" + cfgTuner.getParameter("DOC_DATA_RECORD_ID");
                IOUtil.writeLogLn(2, "+++ Get doc data record SQL: " + sql.replaceAll(", ", "<br>, "), rm);
        
    }

    /**
     * нужно только для суперкласса - здесь это все не используется.
     *
     * @throws Exception
     */
    protected void initSuper() throws Exception {
        makeTableTuner();
        initFormatParams();
        makeTotalsForCols = "";
        makeSubtotals = false;
        unicodeHeaders = false;
        initTableTagsObjects();
    }

}
