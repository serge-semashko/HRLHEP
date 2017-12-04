package jinr.sed;

import dubna.walt.util.IOUtil;
import dubna.walt.util.Tuner;
import jinr.sed.tools.CommonTools;

/**
 * Просмотр и редактирование одной записи справочника
 */
public class ServiceEditInfoData extends dubna.walt.service.TableServiceSpecial {

    private int nr;
    private int numFields = 0;
//	private final int numCols = 0;
    private String[] fields_types = null;
    private String[] fields = null;
//	private String[] fields_names = null;
    private String sql = "";

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

//    получаем список полей справочника, их названия и типы из описания справочника
// (резущьтат выполнения запросов в секции [preSQLs] .cfg - файла)
            fields = cfgTuner.getParameter("FIELDS").split(",");
//		  fields_names = cfgTuner.getParameter( "FIELDS_NAMES" ).split( "," );
            fields_types = cfgTuner.getParameter("FIELDS_TYPES").split(",");
            numFields = cfgTuner.getIntParameter("NUM_FIELDS");

            if (cfgTuner.enabledOption("cop=save")) {
                doSave(); // сохранение записи по кнопке "Сохранить"
            } else {  // иначе - отображение формы
// конструируем SQL для выборки значений полей записи справочника
                sql = "select ";
                for (int i = 0; i < numFields; i++) {
                    sql += fields[i] + ", ";
                }
                sql += cfgTuner.getParameter("SYS_FIELDS")
                        + " from " + cfgTuner.getParameter("TABLE_NAME")
                        + " where id=" + cfgTuner.getParameter("record_id");
                IOUtil.writeLogLn("+++ Get info record data SQL: " + sql, rm);
// выполняем запрос к базе
                getPreData(sql);

// выводим форму с полями справочника
                makeTable();
            }

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
     * Сохранение в БД обновленной записи справочника
     *
     * @throws Exception
     */
    private void doSave() throws Exception {
// Конструируем начало SQL запроса - список всех полей записи
        sql = "replace into " + cfgTuner.getParameter("TABLE_NAME") + " (" // SQL для обновления значений полей записи справочника
                + cfgTuner.getParameter("FIELDS")
                + cfgTuner.getParameter("SYS_FIELDS_UPDATE") + ") values (";

// добавляем к запросу часть " values (...) " с данными из формы
        String s = "";
        for (int i = 0; i < numFields; i++) {
            s += ", " + CommonTools.makeParamValue(fields[i], fields_types[i], cfgTuner);
        }
        sql += s.substring(1);

// добавляем к запросу часть " values (...) " с скрытыми полями
        String[] sysFields = cfgTuner.getParameter("SYS_FIELDS_UPDATE").split(",");
        String[] sysFieldsTypes = cfgTuner.getParameter("SYS_FIELDS_TYPES").split(",");
        s = "";
        for (int i = 0; i < sysFields.length; i++) {
            s += ", " + CommonTools.makeParamValue(sysFields[i], sysFieldsTypes[i], cfgTuner);
        }
        sql += s + ")";

        IOUtil.writeLogLn("+++ UPDATE RECORD SQL: '" + sql + "'", rm);
//	Наконец выполняем запрос
        getPreData(sql);

    }

 
    /**
     * нужно только для суперкласса - здесь это все не используется.
     *
     * @throws Exception
     */
    private void initSuper() throws Exception {
        makeTableTuner();
        initFormatParams();
        makeTotalsForCols = "";
        makeSubtotals = false;
        unicodeHeaders = false;
        initTableTagsObjects();
    }

}
