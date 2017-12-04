/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jinr.sed;

import dubna.walt.util.IOUtil;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author serg
 */
public class ServiceViewDocForm extends dubna.walt.service.TableServiceSpecial {

    protected int numFields = 0;
    protected String[] fields = null;
    protected String[] fields_types = null;
    protected String[] form_fields_types = null;
    protected String sql = "";
 
    public static final Set<String> typesNeedIdField = new HashSet<String>() {
        {
            add("3");
            add("4");
            add("6");
            add("1001");
            add("1003");
            add("1009");
            add("1011");
            add("1012");
            add("1017");
        }
    };

    public static final Set<String> typesIsCollection = new HashSet<String>() {
        {
            add("1004");
            add("1014");
            add("1016");
        }
    };

    /**
     * 
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        try {
            initSuper(); // чтобы родительский класс инициализировал необходимые переменные
            getDocInfo();          
            cfgTuner.outCustomSection("report header", out); // вывод начала формы

            
            if (cfgTuner.enabledExpression("!cop|ERROR|INPUT_ERROR")) { // отображение формы, если действия нет или есть ошибка 
                if (!cfgTuner.enabledExpression("ERROR|INPUT_ERROR")) {  // если не было ошибки - берем из БД, иначе - сохраняем данные формы
// конструируем SQL для выборки значений полей документа
                    sql = "select ";
                    for (int i = 0; i < numFields; i++) {
                        sql += fields[i] + ", ";
                        if (typesNeedIdField.contains(form_fields_types[i])) {
                            sql += fields[i] + "_id, ";
                        }
                    }

                    sql += cfgTuner.getParameter("SYS_FIELDS")
                            + " from " + cfgTuner.getParameter("TABLE_NAME")
                            + " where id=" + cfgTuner.getParameter("DOC_DATA_RECORD_ID");
                    IOUtil.writeLogLn("=================", rm);
                    IOUtil.writeLogLn("++++++++++ Get doc data SQL: " + sql, rm);
                    IOUtil.writeLogLn("=================", rm);
// выполняем запрос к базе
                    getPreData(sql);
                }
// начало формы редактирования				
//                cfgTuner.outCustomSection("start form", out);
// выводим саму форму с полями документа
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
     *  получаем список полей документа, их названия и типы из описания типа документа
     * (результат выполнения запросов в секции [preSQLs] .cfg - файла)
     * @throws Exception 
     */
    protected void getDocInfo() throws Exception{
        if(!cfgTuner.enabledOption("FIELDS"))
            cfgTuner.getCustomSection("getDocInfo"); // выполнение запросов в БД о структуре документа

            fields = cfgTuner.getParameter("FIELDS").split(",");
            fields_types = cfgTuner.getParameter("FIELDS_TYPES").split(",");
            form_fields_types = cfgTuner.getParameter("FORM_FIELDS_TYPES").split(",");
            numFields = fields.length;
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
