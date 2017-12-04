/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jinr.sed;

import dubna.walt.util.IOUtil;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.io.*;


/**
 * Создание *.docx - файлов для конкретного документа из шаблона документов
 * Автор: Яковлев А.В.
  */

public class ServiceCreateDocFiles extends dubna.walt.service.Service {

//	boolean debugMode = false;
//	boolean sendFiles = true;
        
        private int nr;
	private int numFields = 0;
	private int numCols = 0;
	private String[] fields_types = null;
	private String[] form_fields_types = null;
        private String[] form_fields_headers = null;
	private String[] fields = null;
	private String[] fields_names = null;
	private String doc_sql = "";
        
//        private String logFileName = "C:\\public\\sector\\SED\\Documents\\log_1.txt";
        
        Hashtable<String, String> mapDocData = new Hashtable();
        Hashtable<String, Vector<String>> mapDocDataVec = new Hashtable();
        Hashtable<String, Hashtable> mapDocDataProperty = new Hashtable();
                
    	public void start () throws Exception
               
	{
          if(cfgTuner.getIntParameter("NUM_TEMPLATES") == 0) return;
          FeelHashtable();
          OutputHashtable(); // (использовалось для отладки)
          CreateDocFiles();
 
	}
        
        public void FeelHashtable() throws Exception
// Отвечает за наполнение хэш-таблицы               
        {
            int info_numFields = 0;
            String[] info_fields_types = null;
            String[] info_fields = null;
            String[] info_fields_names = null;
            String info_sql = "";
            
            VectorDocDataHashtable mapVectorDocData = new VectorDocDataHashtable();
            
//    получаем список полей справочника, их названия и типы из описания документа
// (результат выполнения запросов в секции [preSQLs] .cfg - файла)
			fields = cfgTuner.getParameter("FIELDS").split(",");
                        fields_names = cfgTuner.getParameter( "FIELDS_NAMES" ).split( "," );
                        fields_types = cfgTuner.getParameter( "FIELDS_TYPES" ).split( "," );
			form_fields_types = cfgTuner.getParameter("FORM_FIELDS_TYPES").split(",");
                        form_fields_headers = cfgTuner.getParameter("FORM_FIELDS_HEADERS").split(",");
			numFields = fields.length;

// отображение формы
// конструируем SQL для выборки значений полей документа
			doc_sql = "select ";
			for (int i = 0; i < numFields; i++) {
                                doc_sql += fields[i] + ", ";
					if (ServiceEditDocData.typesNeedIdField.contains(form_fields_types[i])) {
						doc_sql += fields[i] + "_id, ";
					}
                                        
			}
			doc_sql += cfgTuner.getParameter("SYS_FIELDS")
					+ " from " + cfgTuner.getParameter("TABLE_NAME")
					+ " where id=" + cfgTuner.getParameter("DOC_DATA_RECORD_ID");
			IOUtil.writeLogLn(2, "+++ Get doc data record SQL: " + doc_sql, rm);
// выполняем запрос к базе
			getPreData(doc_sql);
                        
                        mapDocData.put("DocNumber", cfgTuner.getParameter("DOC_NUMBER"));
                        mapDocData.put("DocDate", cfgTuner.getParameter("DOC_DATE"));
                        mapDocData.put("DocTitle", cfgTuner.getParameter("DOC_TITLE"));
                        
                        mapVectorDocData.Add("DocNumber", cfgTuner.getParameter("DOC_NUMBER"));
                        mapVectorDocData.Add("DocDate", cfgTuner.getParameter("DOC_DATE"));
                        mapVectorDocData.Add("DocTitle", cfgTuner.getParameter("DOC_TITLE"));
                        
                        String data_sql =  "select number AS PARENT_DOC_NUMBER"
                                + ", title AS PARENT_DOC_TITLE"
                                + ", DATE_FORMAT(doc_date,'" + cfgTuner.getParameter("dateFormat") + "') AS PARENT_DOC_DATE"
                                + " from d_list"
                                + " where id in ( select pid from d_list where id = " + cfgTuner.getParameter("DOC_ID") + ")";
                        
                        getPreData(data_sql);
                        
                        mapDocData.put("ParentDocNumber", cfgTuner.getParameter("PARENT_DOC_NUMBER"));
                        mapDocData.put("ParentDocDate", cfgTuner.getParameter("PARENT_DOC_DATE"));
                        mapDocData.put("ParentDocTitle", cfgTuner.getParameter("PARENT_DOC_TITLE"));
                        
                        mapVectorDocData.Add("ParentDocNumber", cfgTuner.getParameter("PARENT_DOC_NUMBER"));
                        mapVectorDocData.Add("ParentDocDate", cfgTuner.getParameter("PARENT_DOC_DATE"));
                        mapVectorDocData.Add("ParentDocTitle", cfgTuner.getParameter("PARENT_DOC_TITLE"));

			for (int i=0; i<fields.length; i++) {
                                String temp_data_sql = "";
                                        
                                if (form_fields_types[i].equals("1")| form_fields_types[i].equals("2") ) {
                                    
                                    String fieldValue = cfgTuner.getParameter(fields[i]);
                                    if ((fieldValue.length() != 0) && (form_fields_headers[i].equals("1")))  {
                                        fieldValue = fields_names[i] + ": " + fieldValue;
                                    }
                                    
                                    mapDocData.put(fields_names[i], fieldValue);
                                    mapVectorDocData.Add(fields_names[i], fieldValue);
                                    
                                }
                                
                                if (form_fields_types[i].equals("5")) { //дата
                                    String fieldValue = cfgTuner.getParameter(fields[i]);
                                    
                                    if (fieldValue.length() == 0) {
                                        mapDocData.put(fields_names[i], "____");
                                        mapVectorDocData.Add(fields_names[i], "____");
                                        }
                                    else {
                                        temp_data_sql = "select DATE_FORMAT('" + cfgTuner.getParameter(fields[i]) + "','" + cfgTuner.getParameter("dateFormat") + "') AS DATE_FORMAT_VALUE";
                                        getPreData(temp_data_sql);
                                        mapDocData.put(fields_names[i], cfgTuner.getParameter("DATE_FORMAT_VALUE"));
                                        mapVectorDocData.Add(fields_names[i], cfgTuner.getParameter("DATE_FORMAT_VALUE"));
                                        }
                                }

                                if (form_fields_types[i].equals("19")) { //диапазон дат
                                    temp_data_sql = "select " + fields[i] + "_end AS END_DATE"
                                            + " from " + cfgTuner.getParameter("TABLE_NAME")
                                            + " where id=" + cfgTuner.getParameter("DOC_DATA_RECORD_ID");
                                    getPreData(temp_data_sql);

                                    String fieldValue = cfgTuner.getParameter(fields[i]);
                                    
                                    if (fieldValue.length() == 0) {
                                        mapDocData.put(fields_names[i] + "::с", "____");
                                        mapVectorDocData.Add(fields_names[i] + "::с", "____");
                                        }
                                    else {
                                        temp_data_sql = "select DATE_FORMAT('" + cfgTuner.getParameter(fields[i]) + "','" + cfgTuner.getParameter("dateFormat") + "') AS DATE_FORMAT_VALUE";
                                        getPreData(temp_data_sql);
                                        mapDocData.put(fields_names[i] + "::с", cfgTuner.getParameter("DATE_FORMAT_VALUE"));
                                        mapVectorDocData.Add(fields_names[i] + "::с", cfgTuner.getParameter("DATE_FORMAT_VALUE"));
                                        }
                                    
                                    String fieldEndDateValue = cfgTuner.getParameter("END_DATE");
                                    if (fieldEndDateValue.length() == 0) {
                                        mapDocData.put(fields_names[i] + "::по", "____");
                                        mapVectorDocData.Add(fields_names[i] + "::по", "____");
                                        }
                                    else {
                                        temp_data_sql = "select DATE_FORMAT('" + cfgTuner.getParameter("END_DATE")+ "','" + cfgTuner.getParameter("dateFormat") + "') AS END_DATE_FORMAT_VALUE";
                                        getPreData(temp_data_sql);
                                        mapDocData.put(fields_names[i] + "::по", cfgTuner.getParameter("END_DATE_FORMAT_VALUE"));
                                        mapVectorDocData.Add(fields_names[i] + "::по", cfgTuner.getParameter("END_DATE_FORMAT_VALUE"));
                                        }
                                }
                                
                                if (form_fields_types[i].equals("7")) { //денежная сумма + валюта
                                    temp_data_sql = "select " + fields[i] + "_curr AS CURR_VALUE "
                                            + " from " + cfgTuner.getParameter("TABLE_NAME")
                                            + " where id=" + cfgTuner.getParameter("DOC_DATA_RECORD_ID");
                                    getPreData(temp_data_sql);
                                    
                                    mapDocData.put(fields_names[i] + "::валюта", cfgTuner.getParameter("CURR_VALUE"));
                                    mapVectorDocData.Add(fields_names[i] + "::валюта", cfgTuner.getParameter("CURR_VALUE"));
                                    
                                    String fieldValue = cfgTuner.getParameter(fields[i]);
                                    if (fieldValue.length() == 0) {
                                        mapDocData.put(fields_names[i] + "::сумма", "___");
                                        mapDocData.put(fields_names[i] + "::сумма прописью", "___");
                                        mapVectorDocData.Add(fields_names[i] + "::сумма", "___");
                                        mapVectorDocData.Add(fields_names[i] + "::сумма прописью", "___");
                                        }
                                    else {
                                        temp_data_sql = "select replace(replace(format(" 
                                                + cfgTuner.getParameter(fields[i]) + ", 2),',',' '),'.',',') AS FORMAT_SUM";
                                        getPreData(temp_data_sql);
                                                
                                        mapDocData.put(fields_names[i] + "::сумма", cfgTuner.getParameter("FORMAT_SUM"));
                                        mapDocData.put(fields_names[i] + "::сумма прописью", 
                                            SumByWord("RU", cfgTuner.getParameter("CURR_VALUE"), 
                                            Double.parseDouble(cfgTuner.getParameter(fields[i]))));
                                        
                                        mapVectorDocData.Add(fields_names[i] + "::сумма", cfgTuner.getParameter("FORMAT_SUM"));
                                        mapVectorDocData.Add(fields_names[i] + "::сумма прописью", 
                                            SumByWord("RU", cfgTuner.getParameter("CURR_VALUE"), 
                                            Double.parseDouble(cfgTuner.getParameter(fields[i]))));
                                        }
                                }
                                    
                                if (form_fields_types[i].equals("3") | form_fields_types[i].equals("4")) { // справочники
                                    mapDocData.put(fields_names[i], cfgTuner.getParameter(fields[i]));
                                    mapVectorDocData.Add(fields_names[i], cfgTuner.getParameter(fields[i]));
                                    
                                    // !! получение и парсинг значения поля FIELD + _id !! 
                                    
                                    temp_data_sql = "select " + fields[i] + "_id AS ID_VALUE "
                                            + " from " + cfgTuner.getParameter("TABLE_NAME")
                                            + " where id=" + cfgTuner.getParameter("DOC_DATA_RECORD_ID");
                                    getPreData(temp_data_sql);
                                    
                                    // !!! сейчас не обрабатывается случай мультивыбора !!! 
                                    // !!! рассматривается только первое значение ID_VALUE !!! 
                                    String ID_value[] = cfgTuner.getParameter( "ID_VALUE" ).split( "," );
                                    // !!! если нет ни одного значения, ID_VALUE присваиваем 0 !!! 
                                    if (ID_value[0].equals("")) {
                                        IOUtil.writeLogLn( "+++ ID NULL " + "'"+ID_value[0]+"'", rm );
                                        ID_value[0] = "0";
                                    }
                                    
                                    mapDocData.put(fields_names[i] + " ID", ID_value[0]);
                                    mapVectorDocData.Add(fields_names[i] + " ID", ID_value[0]);
                                    
                                    String temp_info_data_sql = "";
                                    temp_info_data_sql = "select dtf.info_id AS INFO_ID from d_fields dtf "
                                            + "left join d_list dh on (dtf.type_id = dh.type_id)"
                                            + " where dh.id=" + cfgTuner.getParameter("DOCUMENT_ID")
                                            + " and dtf.is_active=1 and dtf.field_db_name = '" + fields[i] + "'"; 
                                    
                                    getPreData(temp_info_data_sql);
//                                    String info_ID_value = cfgTuner.getParameter("INFO_ID");
                                    
                                    getData("getInfosInfo");
//    получаем список полей справочника, их названия и типы из описания справочника
                                    info_fields = cfgTuner.getParameter( "INFO_FIELDS" ).split( "," );
                                    info_fields_names = cfgTuner.getParameter( "INFO_FIELDS_NAMES" ).split( "," );
                                    info_fields_types = cfgTuner.getParameter( "INFO_FIELDS_TYPES" ).split( "," );
                                    info_numFields = cfgTuner.getIntParameter("INFO_NUM_FIELDS");
                                    
// конструируем SQL для выборки значений полей записи справочника
                                    
                                    info_sql = "select ";  
                                    for( int j = 0; j < info_numFields; j++ ) 
					info_sql += info_fields[j] +  ", ";
                                    info_sql += "is_deleted, is_manual"
					+ " from " + cfgTuner.getParameter("INFO_TABLE_NAME") 
                                        + " where id=" + ID_value[0];

                                    
                                    IOUtil.writeLogLn( "+++ Get info record data SQL: " + info_sql, rm );
// выполняем запрос к базе
                                    getPreData(info_sql);
                                    
                                    for (int j=0; j<info_fields.length; j++) {
                                        
                                        if (info_fields_types[j].equals("date")) { //дата
                                            String info_date_format_sql = "";
                                            info_date_format_sql = "select DATE_FORMAT('" + cfgTuner.getParameter(info_fields[j]) + "','" + cfgTuner.getParameter("dateFormat") + "') AS INFO_DATE_FORMAT_VALUE";
                                            getPreData(info_date_format_sql);
                                            mapDocData.put(fields_names[i] + "::" + info_fields_names[j], cfgTuner.getParameter("INFO_DATE_FORMAT_VALUE"));
                                            mapVectorDocData.Add(fields_names[i] + "::" + info_fields_names[j], cfgTuner.getParameter("INFO_DATE_FORMAT_VALUE"));
                                            }
                                        else {
                                            mapDocData.put(fields_names[i] + "::" + info_fields_names[j], cfgTuner.getParameter(info_fields[j]));
                                            mapVectorDocData.Add(fields_names[i] + "::" + info_fields_names[j], cfgTuner.getParameter(info_fields[j]));
                                            }
                                        }
                                }
                                
                                
                                if (form_fields_types[i].equals("10")) { //денежная сумма в рублях
                                    String fieldValue = cfgTuner.getParameter(fields[i]);
                                    
                                    if (fieldValue.length() == 0) {
                                        mapDocData.put(fields_names[i] + "::сумма", "___");
                                        mapDocData.put(fields_names[i] + "::сумма прописью", "___");
                                        
                                        mapVectorDocData.Add(fields_names[i] + "::сумма", "___");
                                        mapVectorDocData.Add(fields_names[i] + "::сумма прописью", "___");
                                        }
                                    else {
                                        temp_data_sql = "select replace(replace(format(" 
                                                + cfgTuner.getParameter(fields[i]) + ", 2),',',' '),'.',',') AS FORMAT_SUM";
                                        getPreData(temp_data_sql);
                                                
                                        mapDocData.put(fields_names[i] + "::сумма", cfgTuner.getParameter("FORMAT_SUM"));
                                        mapDocData.put(fields_names[i] + "::сумма прописью", 
                                            SumByWord("RU", "руб.", 
                                            Double.parseDouble(cfgTuner.getParameter(fields[i]))));
                                        
                                        mapVectorDocData.Add(fields_names[i] + "::сумма", cfgTuner.getParameter("FORMAT_SUM"));
                                        mapVectorDocData.Add(fields_names[i] + "::сумма прописью", 
                                            SumByWord("RU", "руб.", 
                                            Double.parseDouble(cfgTuner.getParameter(fields[i]))));
                                        }
                                }
                                
                                if (form_fields_types[i].equals("11")) { //сумма + процент + валюта
                                    temp_data_sql = "select " + fields[i] + "_curr AS CURR_VALUE, "
                                            + fields[i] + "_percent AS PERCENT, "
                                            + fields[i] + "_total_sum AS TOTAL_SUM "
                                            + " from " + cfgTuner.getParameter("TABLE_NAME")
                                            + " where id=" + cfgTuner.getParameter("DOC_DATA_RECORD_ID");
                                    getPreData(temp_data_sql);
                                    
                                    mapDocData.put(fields_names[i] + "::валюта", cfgTuner.getParameter("CURR_VALUE"));
                                    mapVectorDocData.Add(fields_names[i] + "::валюта", cfgTuner.getParameter("CURR_VALUE"));
                                    
                                    String fieldValue = cfgTuner.getParameter(fields[i]);
                                    
                                    if (fieldValue.length() == 0) {
                                        mapDocData.put(fields_names[i] + "::выплата сумма", "___");
                                        mapDocData.put(fields_names[i] + "::выплата прописью", "___");
                                        
                                        mapVectorDocData.Add(fields_names[i] + "::выплата сумма", "___");
                                        mapVectorDocData.Add(fields_names[i] + "::выплата прописью", "___");
                                        }
                                    else {
                                        temp_data_sql = "select replace(replace(format(" 
                                                + cfgTuner.getParameter(fields[i]) + ", 2),',',' '),'.',',') AS FORMAT_SUM";
                                        getPreData(temp_data_sql);
                                                
                                        mapDocData.put(fields_names[i] + "::выплата сумма", cfgTuner.getParameter("FORMAT_SUM"));
                                        mapDocData.put(fields_names[i] + "::выплата прописью", 
                                            SumByWord("RU", cfgTuner.getParameter("CURR_VALUE"), 
                                            Double.parseDouble(cfgTuner.getParameter(fields[i]))));
                                        
                                        mapVectorDocData.Add(fields_names[i] + "::выплата сумма", cfgTuner.getParameter("FORMAT_SUM"));
                                        mapVectorDocData.Add(fields_names[i] + "::выплата прописью", 
                                            SumByWord("RU", cfgTuner.getParameter("CURR_VALUE"), 
                                            Double.parseDouble(cfgTuner.getParameter(fields[i]))));
                                        }
                                    
                                    String fieldPercentValue = cfgTuner.getParameter("PERCENT");
                                    if (fieldPercentValue.length() == 0) {
                                        mapDocData.put(fields_names[i] + "::процент", "");
                                        mapVectorDocData.Add(fields_names[i] + "::процент", "");
                                        }
                                    else {
                                        mapDocData.put(fields_names[i] + "::процент", cfgTuner.getParameter("PERCENT"));
                                        mapVectorDocData.Add(fields_names[i] + "::процент", cfgTuner.getParameter("PERCENT"));
                                        }
                                    
                                    String fieldTotalValue = cfgTuner.getParameter("TOTAL_SUM");
                                    if (fieldTotalValue.length() == 0) {
                                        mapDocData.put(fields_names[i] + "::общая сумма", "___");
                                        mapDocData.put(fields_names[i] + "::общая сумма прописью", "___");
                                        
                                        mapVectorDocData.Add(fields_names[i] + "::общая сумма", "___");
                                        mapVectorDocData.Add(fields_names[i] + "::общая сумма прописью", "___");
                                        }
                                    else {
                                        temp_data_sql = "select replace(replace(format(" 
                                                + cfgTuner.getParameter("TOTAL_SUM") + ", 2),',',' '),'.',',') AS FORMAT_TOTAL_SUM";
                                        getPreData(temp_data_sql);
                                                
                                        mapDocData.put(fields_names[i] + "::общая сумма", cfgTuner.getParameter("FORMAT_TOTAL_SUM"));
                                        mapDocData.put(fields_names[i] + "::общая сумма прописью", 
                                            SumByWord("RU", cfgTuner.getParameter("CURR_VALUE"), 
                                            Double.parseDouble(cfgTuner.getParameter("TOTAL_SUM"))));
                                        
                                        mapVectorDocData.Add(fields_names[i] + "::общая сумма", cfgTuner.getParameter("FORMAT_TOTAL_SUM"));
                                        mapVectorDocData.Add(fields_names[i] + "::общая сумма прописью", 
                                            SumByWord("RU", cfgTuner.getParameter("CURR_VALUE"), 
                                            Double.parseDouble(cfgTuner.getParameter("TOTAL_SUM"))));
                                        }
                                }
                                
                                if (form_fields_types[i].equals("12")) { //сумма + процент в рублях
                                    temp_data_sql = "select " + fields[i] + ", "
                                            + fields[i] + "_percent AS PERCENT, "
                                            + fields[i] + "_total_sum AS TOTAL_SUM "
                                            + " from " + cfgTuner.getParameter("TABLE_NAME")
                                            + " where id=" + cfgTuner.getParameter("DOC_DATA_RECORD_ID");
                                    getPreData(temp_data_sql);
                                    
                                    String fieldValue = cfgTuner.getParameter(fields[i]);
                                    if (fieldValue.length() == 0) {
                                        mapDocData.put(fields_names[i] + "::выплата сумма", "___");
                                        mapDocData.put(fields_names[i] + "::выплата прописью", "___");
                                        
                                        mapVectorDocData.Add(fields_names[i] + "::выплата сумма", "___");
                                        mapVectorDocData.Add(fields_names[i] + "::выплата прописью", "___");
                                        }
                                    else {
                                        temp_data_sql = "select replace(replace(format(" 
                                                + cfgTuner.getParameter(fields[i]) + ", 2),',',' '),'.',',') AS FORMAT_SUM";
                                        getPreData(temp_data_sql);
                                                
                                        mapDocData.put(fields_names[i] + "::выплата сумма", cfgTuner.getParameter("FORMAT_SUM"));
                                        mapDocData.put(fields_names[i] + "::выплата прописью", 
                                            SumByWord("RU", "руб.", 
                                            Double.parseDouble(cfgTuner.getParameter(fields[i]))));
                                        
                                        mapVectorDocData.Add(fields_names[i] + "::выплата сумма", cfgTuner.getParameter("FORMAT_SUM"));
                                        mapVectorDocData.Add(fields_names[i] + "::выплата прописью", 
                                            SumByWord("RU", "руб.", 
                                            Double.parseDouble(cfgTuner.getParameter(fields[i]))));
                                        }
                                    
                                    String fieldPercentValue = cfgTuner.getParameter("PERCENT");
                                    if (fieldPercentValue.length() == 0) {
                                        mapDocData.put(fields_names[i] + "::процент", "");
                                        mapVectorDocData.Add(fields_names[i] + "::процент", "");
                                        }
                                    else {
                                        mapDocData.put(fields_names[i] + "::процент", cfgTuner.getParameter("PERCENT"));
                                        mapVectorDocData.Add(fields_names[i] + "::процент", cfgTuner.getParameter("PERCENT"));
                                        }
                                    
                                    String fieldTotalValue = cfgTuner.getParameter("TOTAL_SUM");
                                    if (fieldTotalValue.length() == 0) {
                                        mapDocData.put(fields_names[i] + "::общая сумма", "___");
                                        mapDocData.put(fields_names[i] + "::общая сумма прописью", "___");
                                        
                                        mapVectorDocData.Add(fields_names[i] + "::общая сумма", "___");
                                        mapVectorDocData.Add(fields_names[i] + "::общая сумма прописью", "___");
                                        }
                                    else {
                                        
                                        temp_data_sql = "select replace(replace(format(" 
                                                + cfgTuner.getParameter("TOTAL_SUM") + ", 2),',',' '),'.',',') AS FORMAT_TOTAL_SUM";
                                        getPreData(temp_data_sql);
                                                
                                        mapDocData.put(fields_names[i] + "::общая сумма", cfgTuner.getParameter("FORMAT_TOTAL_SUM"));
                                        mapDocData.put(fields_names[i] + "::общая сумма прописью", 
                                            SumByWord("RU", "руб.", 
                                            Double.parseDouble(cfgTuner.getParameter("TOTAL_SUM"))));
                                        
                                        mapVectorDocData.Add(fields_names[i] + "::общая сумма", cfgTuner.getParameter("FORMAT_TOTAL_SUM"));
                                        mapVectorDocData.Add(fields_names[i] + "::общая сумма прописью", 
                                            SumByWord("RU", "руб.", 
                                            Double.parseDouble(cfgTuner.getParameter("TOTAL_SUM"))));
                                        }
                                }
                                
//  Далее идут спецполя                                
                                if (form_fields_types[i].equals("1003")) { // физическое лицо
                                    mapDocData.put(fields_names[i], cfgTuner.getParameter(fields[i]));
                                    mapVectorDocData.Add(fields_names[i], cfgTuner.getParameter(fields[i]));
                                    
                                    temp_data_sql = "select '" + fields[i] + "' AS F_DB_N";
                                    getPreData(temp_data_sql);
                                    mapDocData.put(fields_names[i], cfgTuner.getParameter("F_DB_N"));
                                    mapVectorDocData.Add(fields_names[i], cfgTuner.getParameter("F_DB_N"));
                                    
                                    getData("getFizlitsaDataInfo");
                                    
                                    
                                    mapDocData.put(fields_names[i] + "::ФИО", cfgTuner.getParameter("F_F") 
                                            + " " + cfgTuner.getParameter("F_I") + " " + cfgTuner.getParameter("F_O"));
                                    mapVectorDocData.Add(fields_names[i] + "::ФИО", cfgTuner.getParameter("F_F") 
                                            + " " + cfgTuner.getParameter("F_I") + " " + cfgTuner.getParameter("F_O"));
                                    
                                    mapDocData.put(fields_names[i] + "::Фамилия", cfgTuner.getParameter("F_F"));
                                    mapVectorDocData.Add(fields_names[i] + "::Фамилия", cfgTuner.getParameter("F_F"));
                                    
                                    mapDocData.put(fields_names[i] + "::Имя", cfgTuner.getParameter("F_I"));
                                    mapVectorDocData.Add(fields_names[i] + "::Имя", cfgTuner.getParameter("F_I"));
                                    
                                    mapDocData.put(fields_names[i] + "::Отчество", cfgTuner.getParameter("F_O"));
                                    mapVectorDocData.Add(fields_names[i] + "::Отчество", cfgTuner.getParameter("F_O"));
                                    
                                    String fieldF_drValue = cfgTuner.getParameter("F_dr");
                                    if (fieldF_drValue.length() == 0) {
                                        mapDocData.put(fields_names[i] + "::Дата рождения", "");
                                        mapVectorDocData.Add(fields_names[i] + "::Дата рождения", "");
                                        }
                                    else {
                                        temp_data_sql = "select DATE_FORMAT('" 
                                                + cfgTuner.getParameter("F_dr") + "','" 
                                                + cfgTuner.getParameter("dateFormat")
                                                + "') AS DATE_FORMAT_VALUE";
                                        getPreData(temp_data_sql);
                                        mapDocData.put(fields_names[i] 
                                                + "::Дата рождения", cfgTuner.getParameter("DATE_FORMAT_VALUE"));
                                        mapVectorDocData.Add(fields_names[i] 
                                                + "::Дата рождения", cfgTuner.getParameter("DATE_FORMAT_VALUE"));
                                        }
                                    
                                    mapDocData.put(fields_names[i] + "::ИНН", cfgTuner.getParameter("F_inn"));
                                    mapVectorDocData.Add(fields_names[i] + "::ИНН", cfgTuner.getParameter("F_inn"));
                                    
                                    mapDocData.put(fields_names[i] + "::ПФР", cfgTuner.getParameter("F_pfr"));
                                    mapVectorDocData.Add(fields_names[i] + "::ПФР", cfgTuner.getParameter("F_pfr"));
                                    
                                    mapDocData.put(fields_names[i] + "::Страна", cfgTuner.getParameter("F_strana"));
                                    mapDocData.put(fields_names[i] + "::Гражданство", cfgTuner.getParameter("F_strana"));
                                    mapVectorDocData.Add(fields_names[i] + "::Страна", cfgTuner.getParameter("F_strana"));
                                    mapVectorDocData.Add(fields_names[i] + "::Гражданство", cfgTuner.getParameter("F_strana"));

                                    mapDocData.put(fields_names[i] + "::Пол", cfgTuner.getParameter("F_sex"));
                                    mapVectorDocData.Add(fields_names[i] + "::Пол", cfgTuner.getParameter("F_sex"));
                                    
                                    mapDocData.put(fields_names[i] + "::Вид документа", cfgTuner.getParameter("F_vid_doc"));
                                    mapVectorDocData.Add(fields_names[i] + "::Вид документа", cfgTuner.getParameter("F_vid_doc"));
                                    
                                    mapDocData.put(fields_names[i] + "::Серия", cfgTuner.getParameter("F_seria"));
                                    mapVectorDocData.Add(fields_names[i] + "::Серия", cfgTuner.getParameter("F_seria"));
                                    
                                    mapDocData.put(fields_names[i] + "::Номер", cfgTuner.getParameter("F_nomer"));
                                    mapVectorDocData.Add(fields_names[i] + "::Номер", cfgTuner.getParameter("F_nomer"));
                                    
                                    mapDocData.put(fields_names[i] + "::Кем выдан", cfgTuner.getParameter("F_vidan"));
                                    mapVectorDocData.Add(fields_names[i] + "::Кем выдан", cfgTuner.getParameter("F_vidan"));
                                    
                                    mapDocData.put(fields_names[i] + "::Код подразд.", cfgTuner.getParameter("F_kod_podrazd"));
                                    mapVectorDocData.Add(fields_names[i] + "::Код подразд.", cfgTuner.getParameter("F_kod_podrazd"));
                                    
                                    String fieldF_dat_vidValue = cfgTuner.getParameter("F_dat_vid");
                                    if (fieldF_dat_vidValue.length() == 0) {
                                        mapDocData.put(fields_names[i] + "::Дата выдачи", "");
                                        mapVectorDocData.Add(fields_names[i] + "::Дата выдачи", "");
                                        }
                                    else {
                                        temp_data_sql = "select DATE_FORMAT('" 
                                                + cfgTuner.getParameter("F_dat_vid") + "','" 
                                                + cfgTuner.getParameter("dateFormat") 
                                                + "') AS DATE_FORMAT_VALUE";
                                        getPreData(temp_data_sql);
                                        mapDocData.put(fields_names[i] 
                                                + "::Дата выдачи", cfgTuner.getParameter("DATE_FORMAT_VALUE"));
                                        mapVectorDocData.Add(fields_names[i] 
                                                + "::Дата выдачи", cfgTuner.getParameter("DATE_FORMAT_VALUE"));
                                        }
                                    
                                    String fieldF_dat_regValue = cfgTuner.getParameter("F_dat_reg");
                                    if (fieldF_dat_regValue.length() == 0) {
                                        mapDocData.put(fields_names[i] + "::Дата регистрации", "");
                                        mapVectorDocData.Add(fields_names[i] + "::Дата регистрации", "");
                                        }
                                    else {
                                        temp_data_sql = "select DATE_FORMAT('" 
                                                + cfgTuner.getParameter("F_dat_reg") + "','" 
                                                + cfgTuner.getParameter("dateFormat") 
                                                + "') AS DATE_FORMAT_VALUE";
                                        getPreData(temp_data_sql);
                                        mapDocData.put(fields_names[i] 
                                                + "::Дата регистрации", cfgTuner.getParameter("DATE_FORMAT_VALUE"));
                                        mapVectorDocData.Add(fields_names[i] 
                                                + "::Дата регистрации", cfgTuner.getParameter("DATE_FORMAT_VALUE"));
                                        }
                                    
                                    mapDocData.put(fields_names[i] + "::Адрес прописки", cfgTuner.getParameter("F_address"));
                                    mapVectorDocData.Add(fields_names[i] + "::Адрес прописки", cfgTuner.getParameter("F_address"));
                                    
                                    mapDocData.put(fields_names[i] + "::Телефон", cfgTuner.getParameter("F_phon"));
                                    mapVectorDocData.Add(fields_names[i] + "::Телефон", cfgTuner.getParameter("F_phon"));

/*                                    
                                    temp_data_sql = "select DATE_FORMAT('" + cfgTuner.getParameter(fields[i]) + "','" + cfgTuner.getParameter("dateFormat") + "') AS DATE_FORMAT_VALUE";
                                    getPreData(temp_data_sql);
                                    mapDocData.put(fields_names[i], cfgTuner.getParameter("DATE_FORMAT_VALUE"));
*/                                    
                                }

                                if (form_fields_types[i].equals("1004")) { // источники финансирования

                                    temp_data_sql = "select '" + fields[i] + "' AS F_DB_N";
                                    getPreData(temp_data_sql);
                                    getData("getSourceFundingDataInfo");
                                    
                                    mapDocData.put(fields_names[i], cfgTuner.getParameter("RESULT"));
                                    mapVectorDocData.Add(fields_names[i], cfgTuner.getParameter("RESULT"));
                                }
                                
                                if (form_fields_types[i].equals("1008")) { //Аванс для Договора Подряда
                                    String fieldValue = cfgTuner.getParameter(fields[i]);
                                    if (fieldValue.length() == 0) {
                                        mapDocData.put(fields_names[i] + "::сумма", "___");
                                        mapDocData.put(fields_names[i] + "::сумма прописью", "___");
                                        
                                        mapVectorDocData.Add(fields_names[i] + "::сумма", "___");
                                        mapVectorDocData.Add(fields_names[i] + "::сумма прописью", "___");
                                        }
                                    else {
                                        temp_data_sql = "select replace(replace(format(" 
                                                + cfgTuner.getParameter(fields[i]) + ", 2),',',' '),'.',',') AS FORMAT_SUM";
                                        getPreData(temp_data_sql);
                                                
                                        mapDocData.put(fields_names[i] + "::сумма", cfgTuner.getParameter("FORMAT_SUM"));
                                        mapDocData.put(fields_names[i] + "::сумма прописью", 
                                            SumByWord("RU", "руб.", 
                                            Double.parseDouble(cfgTuner.getParameter(fields[i]))));
                                        
                                        mapVectorDocData.Add(fields_names[i] + "::сумма", cfgTuner.getParameter("FORMAT_SUM"));
                                        mapVectorDocData.Add(fields_names[i] + "::сумма прописью", 
                                            SumByWord("RU", "руб.", 
                                            Double.parseDouble(cfgTuner.getParameter(fields[i]))));
                                        }
                                    
                                    temp_data_sql = "select " + fields[i] + "_up_to AS UP_TO_DATE"
                                            + " from " + cfgTuner.getParameter("TABLE_NAME")
                                            + " where id=" + cfgTuner.getParameter("DOC_DATA_RECORD_ID");
                                    getPreData(temp_data_sql);
                                                                        
                                    String fieldEndDateValue = cfgTuner.getParameter("UP_TO_DATE");
                                    if (fieldEndDateValue.length() == 0) {
                                        mapDocData.put(fields_names[i] + "::в срок до", "____");
                                        mapVectorDocData.Add(fields_names[i] + "::в срок до", "____");
                                        }
                                    else {
                                        temp_data_sql = "select DATE_FORMAT('" + cfgTuner.getParameter("UP_TO_DATE")+ "','" + cfgTuner.getParameter("dateFormat") + "') AS UP_TO_DATE_FORMAT_VALUE";
                                        getPreData(temp_data_sql);
                                        mapDocData.put(fields_names[i] + "::в срок до", cfgTuner.getParameter("UP_TO_DATE_FORMAT_VALUE"));
                                        mapVectorDocData.Add(fields_names[i] + "::в срок до", cfgTuner.getParameter("UP_TO_DATE_FORMAT_VALUE"));
                                        }
                                }

                                if (form_fields_types[i].equals("1012")) { // Список рассылки
                                    
                                    String[] membersIDs = null;
                                    int countMembers = 0;
                                    
                                    temp_data_sql = "select '" + fields[i] + "' AS F_1012_DB_N";
                                    getPreData(temp_data_sql);
                                    
                                    getData("getMemberIdsDataInfo");
                                    
                                    String stLenOfMemberID = cfgTuner.getParameter("Len_of_MEMBER_ID");
                                    int intLenOfMemberID = Integer.parseInt(stLenOfMemberID);
                                    membersIDs = cfgTuner.getParameter("MEMBER_ID").split( "," );
                                    countMembers = membersIDs.length;
                                    
                                    Vector<String> vecType1012_name = new Vector<String>();
                                    Vector<String> vecType1012_members_FIO = new Vector<String>();
                                    Vector<String> vecType1012_members_post = new Vector<String>();
                                    if (intLenOfMemberID>0) {
                                        vecType1012_name.add(fields_names[i]);
                                        for (int j=0; j<membersIDs.length; j++) {
                                            temp_data_sql = "select '" + membersIDs[j] + "' AS ZK_MEMBER_ID";
                                            getPreData(temp_data_sql);
                                            
                                            getData("getZKMemberDataInfo");
                                            
                                            vecType1012_members_FIO.add(cfgTuner.getParameter("MEMBER_FIO"));
                                            String fieldMemberPostValue = cfgTuner.getParameter("MEMBER_POST");
                                            String endSmblMemberPostValue = fieldMemberPostValue.substring((fieldMemberPostValue.length() - 1));
                                            if (endSmblMemberPostValue.equals(",")) {
                                                fieldMemberPostValue = fieldMemberPostValue.substring(0,(fieldMemberPostValue.length() - 1));
                                                }

                                            vecType1012_members_post.add(fieldMemberPostValue);

                                            if (fieldMemberPostValue.length()>42) {
                                                vecType1012_members_FIO.add("    ");
                                                }

                                            }
                                        
                                        }
                                    else {
                                        vecType1012_name.add("");
                                        vecType1012_members_FIO.add("");
                                        vecType1012_members_post.add("");
                                        }
                                    
                                    Hashtable properties_ZK_members = new Hashtable();
                                    properties_ZK_members.put(DocxReplace.PROPERTY_FONT_SIZE, new Integer(12));
//                                    properties_ZK_members.put(DocxReplace.PROPERTY_BOLD, new Boolean(true));
                                    
                                    
                                                                        
                                    mapVectorDocData.Add(fields_names[i] + "::Название", vecType1012_name);
                                    mapVectorDocData.Add(fields_names[i] + "::ФИО", vecType1012_members_FIO);
                                    mapVectorDocData.Add(fields_names[i] + "::Должность", vecType1012_members_post);
                                    
                                    mapDocDataProperty.put(fields_names[i] + "::Название", properties_ZK_members);
                                    mapDocDataProperty.put(fields_names[i] + "::ФИО", properties_ZK_members);
                                    mapDocDataProperty.put(fields_names[i] + "::Должность", properties_ZK_members);

/*                                    
                                    for (int j=0; j<members_ids.length; j++) {
                                        
                                        vec_type_1012_members_id.add(members_ids[j]);
                                        }
                                    
                                    mapDocDataVec.put(fields_names[i] + "::ФИО", vec_type_1012_members_id);
                                    
*/                                    
                                }

                                if (form_fields_types[i].equals("1017")) { // Закупочные комиссии
                                    
                                    mapDocData.put(fields_names[i], cfgTuner.getParameter(fields[i]));
                                    mapVectorDocData.Add(fields_names[i], cfgTuner.getParameter(fields[i]));

                                    
                                    temp_data_sql = "select '" + fields[i] + "' AS F_DB_N";
                                    getPreData(temp_data_sql);
                                    mapDocData.put(fields_names[i], cfgTuner.getParameter("F_DB_N"));
                                    mapVectorDocData.Add(fields_names[i], cfgTuner.getParameter("F_DB_N"));
                                    
                                    getData("getZKDataInfo");
                                    
                                    mapDocData.put(fields_names[i] + "::Название в заголовке", cfgTuner.getParameter("F_header"));
                                    mapVectorDocData.Add(fields_names[i] + "::Название в заголовке", cfgTuner.getParameter("F_header"));
                                    
                                    mapDocData.put(fields_names[i] + "::Действует на основании", cfgTuner.getParameter("F_titulature"));
                                    mapVectorDocData.Add(fields_names[i] + "::Действует на основании", cfgTuner.getParameter("F_titulature"));
                                    
                                    mapDocData.put(fields_names[i] + "::Присутствуют", cfgTuner.getParameter("F_present"));
                                    mapVectorDocData.Add(fields_names[i] + "::Присутствуют", cfgTuner.getParameter("F_present"));
                                    
                                }                                                                

                                if (form_fields_types[i].equals("1014")) { // Список участников конкурса закупочной комиссии
                                    String[] tenderParticipantsIds = null;
                                    
                                    temp_data_sql = "select '" + fields[i] + "' AS F_DB_N";
                                    getPreData(temp_data_sql);
                                    getData("getTenderParticipantsIDDataInfo");
                                    
                                    tenderParticipantsIds = cfgTuner.getParameter("TP_DOC_IDS").split( "," );
                                    
                                    Vector<String> vecType1014 = new Vector<String>();
                                    
                                    for (int j=0; j<tenderParticipantsIds.length; j++) {
                                            
                                        if (tenderParticipantsIds[j].length() > 0){
                                            temp_data_sql = "select '" + tenderParticipantsIds[j] + "' AS TENDER_PARTICIPANT_DOC_ID";
                                            getPreData(temp_data_sql);
                                            getData("getTenderParticipantDataInfo");
                                            
                                            vecType1014.add((j + 1) + ". " + cfgTuner.getParameter("TP_PROVIDER"));
                                            vecType1014.add(" ");

                                            vecType1014.add("Стоимость оборудования - " + cfgTuner.getParameter("TP_COST"));
                                            vecType1014.add("Предлагаемое оборудование - " + cfgTuner.getParameter("TP_PRODUCT"));
                                            vecType1014.add("Ставка НДС - " + cfgTuner.getParameter("TP_VAT"));
                                            vecType1014.add("Срок поставки - " + cfgTuner.getParameter("TP_DELIVERY_TIME"));
                                            vecType1014.add("Гарантия - " + cfgTuner.getParameter("TP_GUARANTEE"));
                                            vecType1014.add("Условия оплаты - " + cfgTuner.getParameter("TP_TERMS_OF_PAYMENT"));
                                            
                                            vecType1014.add(" ");
                                            vecType1014.add(" ");
                                            }
                                        
                                        }

                                    Hashtable properties_TP = new Hashtable();
                                    properties_TP.put(DocxReplace.PROPERTY_FONT_SIZE, new Integer(12));
                                    mapVectorDocData.Add(fields_names[i], vecType1014);
                                    mapDocDataProperty.put(fields_names[i], properties_TP);
                                    
                                }                                

                                if (form_fields_types[i].equals("1018")) { // Протокол голосования закупочной комиссии
                                    
                                    String[] ZK_VP_membersFIOs = null;
                                    String[] ZK_VP_membersIDs = null;
                                    String[] ZK_VP_membersVotes = null;
                                    
                                    temp_data_sql = "select '" + fields[i] + "' AS ZK_VP_F_DB_N";
                                    getPreData(temp_data_sql);
                                    
                                    getData("getVPDataInfo");
                                    
                                    String valuesFor = "";
                                    String valuesAgainst = "";
                                    String valuesAbstand = "";
                                    
                                    ZK_VP_membersFIOs = cfgTuner.getParameter("ZK_VP_FIO").split( "<BR/>" );
                                    ZK_VP_membersIDs = cfgTuner.getParameter("ZK_VP_members_id").split( "," );
                                    ZK_VP_membersVotes = cfgTuner.getParameter("ZK_VP_votes").split( "," );
                                    
                                    if ((ZK_VP_membersFIOs.length == ZK_VP_membersIDs.length) & 
                                            (ZK_VP_membersIDs.length == ZK_VP_membersVotes.length)) {

                                        for (int j=0; j<ZK_VP_membersIDs.length; j++) {
                                            
                                            if (ZK_VP_membersIDs[j].length() > 0){
                                                temp_data_sql = "select '" + ZK_VP_membersIDs[j] + "' AS ZK_VP_MEMBER_ID";
                                                getPreData(temp_data_sql);
                                                getData("getVPMemberInfo");
                                                
                                                if (ZK_VP_membersVotes[j].equals("1")) {
//                                                    valuesFor += ZK_VP_membersFIOs[j] + ", ";
                                                    valuesFor += cfgTuner.getParameter("ZK_VP_MEMBER_FIO") + ", ";
                                                    }
                                                
                                                if (ZK_VP_membersVotes[j].equals("2")) {
//                                                    valuesAgainst += ZK_VP_membersFIOs[j] + ", ";
                                                    valuesAgainst += cfgTuner.getParameter("ZK_VP_MEMBER_FIO") + ", ";
                                                    }
                                                
                                                if (ZK_VP_membersVotes[j].equals("3")) {
//                                                    valuesAbstand += ZK_VP_membersFIOs[j] + ", ";
                                                    valuesAbstand += cfgTuner.getParameter("ZK_VP_MEMBER_FIO") + ", ";
                                                    }
                                                }
                                            }
                                        }

                                    if (valuesAgainst.equals("")) {
                                        valuesAgainst += "НЕТ";
                                    }
                                    
                                    mapVectorDocData.Add(fields_names[i] + "::За", valuesFor);
                                    mapVectorDocData.Add(fields_names[i] + "::Против", valuesAgainst);
                                    mapVectorDocData.Add(fields_names[i] + "::Воздержались", valuesAbstand);
                                }


                                
                                if (form_fields_types[i].equals("1019")) { // Состав закупочной комиссии
                                    
                                    String[] ZK_membersFIOs = null;
                                    String[] ZK_membersIDs = null;
                                    String[] ZK_membersRanks = null;
                                    String[] ZK_membersPresence = null;
                                    
                                    temp_data_sql = "select '" + fields[i] + "' AS ZK_members_F_DB_N";
                                    getPreData(temp_data_sql);
                                    
                                    getData("getCommissionMembersDataInfo");

                                    String valuesFor = "";
                                    String valuesAgainst = "";
                                    String valuesAbstand = "";
                                    
                                    ZK_membersFIOs = cfgTuner.getParameter("com_members_FIO").split( "<BR/>" );
                                    ZK_membersIDs = cfgTuner.getParameter("com_members_id").split( "," );
                                    ZK_membersRanks = cfgTuner.getParameter("com_members_rank").split( "," );
                                    ZK_membersPresence = cfgTuner.getParameter("com_members_presence").split( "," );
                                    
                                    Vector<String> vecType1019_chef_name = new Vector<String>();
                                    Vector<String> vecType1019_chef_present_name = new Vector<String>();
                                    Vector<String> vecType1019_chef_FIO = new Vector<String>();
                                    Vector<String> vecType1019_chef_present_FIO = new Vector<String>();
                                    Vector<String> vecType1019_chef_post = new Vector<String>();
                                    
                                    Vector<String> vecType1019_vice_chef_name = new Vector<String>();
                                    Vector<String> vecType1019_vice_chef_present_name = new Vector<String>();
                                    Vector<String> vecType1019_vice_chef_FIO = new Vector<String>();
                                    Vector<String> vecType1019_vice_chef_present_FIO = new Vector<String>();
                                    Vector<String> vecType1019_vice_chef_post = new Vector<String>();
                                    
                                    Vector<String> vecType1019_members_name = new Vector<String>();
                                    Vector<String> vecType1019_members_present_name = new Vector<String>();
                                    Vector<String> vecType1019_members_FIO = new Vector<String>();
                                    Vector<String> vecType1019_members_present_FIO = new Vector<String>();
                                    Vector<String> vecType1019_members_post = new Vector<String>();
                                    
                                    Vector<String> vecType1019_secretary_name = new Vector<String>();
                                    Vector<String> vecType1019_secretary_present_name = new Vector<String>();
                                    Vector<String> vecType1019_secretary_FIO = new Vector<String>();
                                    Vector<String> vecType1019_secretary_present_FIO = new Vector<String>();
                                    Vector<String> vecType1019_secretary_post = new Vector<String>();
                                    
                                    int chefCount = 0;
                                    int chefPresentCount = 0;
                                    int vice_chefCount = 0;
                                    int vice_chefPresentCount = 0;
                                    int membersCount = 0;
                                    int membersPresentCount = 0;
                                    int secretaryCount = 0;
                                    int secretaryPresentCount = 0;

                                    if ((ZK_membersFIOs.length == ZK_membersIDs.length) & 
                                            (ZK_membersIDs.length == ZK_membersRanks.length) &
                                            (ZK_membersIDs.length == ZK_membersPresence.length)) {

                                        for (int j=0; j<ZK_membersIDs.length; j++) {
                                            
                                            if (ZK_membersIDs[j].length() > 0){
                                                temp_data_sql = "select '" + ZK_membersIDs[j] + "' AS ZK_MEMBER_ID";
                                                getPreData(temp_data_sql);
                                                getData("getZKMemberDataInfo");

                                                if (ZK_membersRanks[j].equals("1")) {
                                                    chefCount ++;
                                                    
                                                    vecType1019_chef_FIO.add(cfgTuner.getParameter("MEMBER_FIO"));
                                                    String fieldChefPostValue = cfgTuner.getParameter("MEMBER_POST");
                                                    String endSmblChefPostValue = fieldChefPostValue.substring((fieldChefPostValue.length() - 1));
                                                    if (endSmblChefPostValue.equals(",")) {
                                                        fieldChefPostValue = fieldChefPostValue.substring(0,(fieldChefPostValue.length() - 1));
                                                        }
                                                    
                                                    vecType1019_chef_post.add(fieldChefPostValue);
                                                    if (fieldChefPostValue.length()>42) {
                                                        vecType1019_chef_FIO.add("    ");
                                                        }
                                                    if (fieldChefPostValue.length()>85) {
                                                        vecType1019_chef_FIO.add("    ");
                                                        }
                                                    
                                                    if (ZK_membersPresence[j].equals("0")) {
                                                        vecType1019_chef_FIO.add("    ");
                                                        vecType1019_chef_post.add("(отсутствует)");
                                                        }
                                                    else {
                                                        vecType1019_chef_present_FIO.add(cfgTuner.getParameter("MEMBER_FIO"));
                                                        vecType1019_chef_present_FIO.add("  ");
                                                        chefPresentCount ++;
                                                       
                                                        }
                                                    
                                                    }

                                                if (ZK_membersRanks[j].equals("2")) {
                                                    vice_chefCount ++;
                                                    
                                                    vecType1019_vice_chef_FIO.add(cfgTuner.getParameter("MEMBER_FIO"));
                                                    String fieldViceChefPostValue = cfgTuner.getParameter("MEMBER_POST");
                                                    String endSmblViceChefPostValue = fieldViceChefPostValue.substring((fieldViceChefPostValue.length() - 1));
                                                    if (endSmblViceChefPostValue.equals(",")) {
                                                        fieldViceChefPostValue = fieldViceChefPostValue.substring(0,(fieldViceChefPostValue.length() - 1));
                                                        }
                                                    
                                                    vecType1019_vice_chef_post.add(fieldViceChefPostValue);
                                                    if (fieldViceChefPostValue.length()>42) {
                                                        vecType1019_vice_chef_FIO.add("    ");
                                                        }
                                                    if (fieldViceChefPostValue.length()>85) {
                                                        vecType1019_vice_chef_FIO.add("    ");
                                                        }
                                                    
                                                    if (ZK_membersPresence[j].equals("0")) {
                                                        vecType1019_vice_chef_FIO.add("    ");
                                                        vecType1019_vice_chef_post.add("(отсутствует)");
                                                        }
                                                    else {
                                                        vecType1019_vice_chef_present_FIO.add(cfgTuner.getParameter("MEMBER_FIO"));
                                                        vecType1019_vice_chef_present_FIO.add("  ");
                                                        vice_chefPresentCount ++;
                                                       
                                                        }
                                                    
                                                    }
                                                    

                                                if (ZK_membersRanks[j].equals("3")) {
                                                    membersCount ++;
                                                    
                                                    vecType1019_members_FIO.add(cfgTuner.getParameter("MEMBER_FIO"));
                                                    String fieldMemberPostValue = cfgTuner.getParameter("MEMBER_POST");
                                                    String endSmblMemberPostValue = fieldMemberPostValue.substring((fieldMemberPostValue.length() - 1));
                                                    if (endSmblMemberPostValue.equals(",")) {
                                                        fieldMemberPostValue = fieldMemberPostValue.substring(0,(fieldMemberPostValue.length() - 1));
                                                        }
                                                    
                                                    vecType1019_members_post.add(fieldMemberPostValue);
                                                    if (fieldMemberPostValue.length()>42) {
                                                        vecType1019_members_FIO.add("    ");
                                                        }
                                                    if (fieldMemberPostValue.length()>85) {
                                                        vecType1019_members_FIO.add("    ");
                                                        }
                                                    
                                                    if (ZK_membersPresence[j].equals("0")) {
                                                        vecType1019_members_FIO.add("    ");
                                                        vecType1019_members_post.add("(отсутствует)");
                                                        }
                                                    else {
                                                        vecType1019_members_present_FIO.add(cfgTuner.getParameter("MEMBER_FIO"));
                                                        vecType1019_members_present_FIO.add("  ");
                                                        membersPresentCount ++;
                                                       
                                                        }
                                                    }

                                                if (ZK_membersRanks[j].equals("4")) {
                                                    secretaryCount ++;
                                                    
                                                    vecType1019_secretary_FIO.add(cfgTuner.getParameter("MEMBER_FIO"));
                                                    String fieldSecretaryPostValue = cfgTuner.getParameter("MEMBER_POST");
                                                    String endSmblSecretaryPostValue = fieldSecretaryPostValue.substring((fieldSecretaryPostValue.length() - 1));
                                                    if (endSmblSecretaryPostValue.equals(",")) {
                                                        fieldSecretaryPostValue = fieldSecretaryPostValue.substring(0,(fieldSecretaryPostValue.length() - 1));
                                                        }
                                                    
                                                    vecType1019_secretary_post.add(fieldSecretaryPostValue);
                                                    if (fieldSecretaryPostValue.length()>42) {
                                                        vecType1019_secretary_FIO.add("    ");
                                                        }
                                                    if (fieldSecretaryPostValue.length()>85) {
                                                        vecType1019_secretary_FIO.add("    ");
                                                        }
                                                    
                                                    if (ZK_membersPresence[j].equals("0")) {
                                                        vecType1019_secretary_FIO.add("    ");
                                                        vecType1019_secretary_post.add("(отсутствует)");
                                                        }
                                                    else {
                                                        vecType1019_secretary_present_FIO.add(cfgTuner.getParameter("MEMBER_FIO"));
                                                        vecType1019_secretary_present_FIO.add("  ");
                                                        secretaryPresentCount ++;
                                                       
                                                        }
                                                    
                                                    }
                                                
                                                
                                                }
                                            }
                                        }
                                    
                                    if (chefCount > 0) {
                                        vecType1019_chef_name.add("Председатель");
                                        }
                                    else {
                                        vecType1019_chef_name.add("");
                                        vecType1019_chef_FIO.add("");
                                        vecType1019_chef_post.add("");
                                        }
                                    
                                    if (chefPresentCount > 0) {
                                        vecType1019_chef_present_name.add("Председатель");
                                        }
                                    else {
                                        vecType1019_chef_present_name.add("");
                                        vecType1019_chef_present_FIO.add("");
                                        }
                                    
                                    mapVectorDocData.Add(fields_names[i] + "::Пред_Название", vecType1019_chef_name);
                                    mapVectorDocData.Add(fields_names[i] + "::Пред_реал_Название", vecType1019_chef_present_name);
                                    mapVectorDocData.Add(fields_names[i] + "::Пред_ФИО", vecType1019_chef_FIO);
                                    mapVectorDocData.Add(fields_names[i] + "::Пред_реал_ФИО", vecType1019_chef_present_FIO);
                                    mapVectorDocData.Add(fields_names[i] + "::Пред_Должность", vecType1019_chef_post);
                                    
                                    if (vice_chefCount > 0) {
                                        vecType1019_vice_chef_name.add("Зам.председателя");
                                        }
                                    else {
                                        vecType1019_vice_chef_name.add("");
                                        vecType1019_vice_chef_FIO.add("");
                                        vecType1019_vice_chef_post.add("");
                                        }
                                    
                                    if (vice_chefPresentCount > 0) {
                                        vecType1019_vice_chef_present_name.add("Зам.председателя");
                                        }
                                    else {
                                        vecType1019_vice_chef_present_name.add("");
                                        vecType1019_vice_chef_present_FIO.add("");
                                        }
                                    
                                    mapVectorDocData.Add(fields_names[i] + "::Зам_Название", vecType1019_vice_chef_name);
                                    mapVectorDocData.Add(fields_names[i] + "::Зам_реал_Название", vecType1019_vice_chef_present_name);
                                    mapVectorDocData.Add(fields_names[i] + "::Зам_ФИО", vecType1019_vice_chef_FIO);
                                    mapVectorDocData.Add(fields_names[i] + "::Зам_реал_ФИО", vecType1019_vice_chef_present_FIO);
                                    mapVectorDocData.Add(fields_names[i] + "::Зам_Должность", vecType1019_vice_chef_post);
                                    
                                    if (membersCount > 0) {
                                        vecType1019_members_name.add("Члены комиссии");
                                        }
                                    else {
                                        vecType1019_members_name.add("");
                                        vecType1019_members_FIO.add("");
                                        vecType1019_members_post.add("");
                                        }
                                    
                                    if (membersPresentCount > 0) {
                                        vecType1019_members_present_name.add("Члены комиссии");
                                        }
                                    else {
                                        vecType1019_members_present_name.add("");
                                        vecType1019_members_present_FIO.add("");
                                        }
                                    
                                    mapVectorDocData.Add(fields_names[i] + "::Члены_Название", vecType1019_members_name);
                                    mapVectorDocData.Add(fields_names[i] + "::Члены_реал_Название", vecType1019_members_present_name);
                                    mapVectorDocData.Add(fields_names[i] + "::Члены_ФИО", vecType1019_members_FIO);
                                    mapVectorDocData.Add(fields_names[i] + "::Члены_реал_ФИО", vecType1019_members_present_FIO);
                                    mapVectorDocData.Add(fields_names[i] + "::Члены_Должность", vecType1019_members_post);
                                    
                                    if (secretaryCount > 0) {
                                        vecType1019_secretary_name.add("Секретарь комиссии");
                                        }
                                    else {
                                        vecType1019_secretary_name.add("");
                                        vecType1019_secretary_FIO.add("");
                                        vecType1019_secretary_post.add("");
                                        }
                                    
                                    if (secretaryPresentCount > 0) {
                                        vecType1019_secretary_present_name.add("Секретарь комиссии");
                                        }
                                    else {
                                        vecType1019_secretary_present_name.add("");
                                        vecType1019_secretary_present_FIO.add("");
                                        }
                                    
                                    mapVectorDocData.Add(fields_names[i] + "::Секр_Название", vecType1019_secretary_name);
                                    mapVectorDocData.Add(fields_names[i] + "::Секр_реал_Название", vecType1019_secretary_present_name);
                                    mapVectorDocData.Add(fields_names[i] + "::Секр_ФИО", vecType1019_secretary_FIO);
                                    mapVectorDocData.Add(fields_names[i] + "::Секр_реал_ФИО", vecType1019_secretary_present_FIO);
                                    mapVectorDocData.Add(fields_names[i] + "::Секр_Должность", vecType1019_secretary_post);
                                    
                                    Hashtable properties_ZK_members = new Hashtable();
                                    properties_ZK_members.put(DocxReplace.PROPERTY_FONT_SIZE, new Integer(12));
                                    
                                    mapDocDataProperty.put(fields_names[i] + "::Пред_Название", properties_ZK_members);
                                    mapDocDataProperty.put(fields_names[i] + "::Пред_ФИО", properties_ZK_members);
                                    mapDocDataProperty.put(fields_names[i] + "::Пред_реал_ФИО", properties_ZK_members);
                                    mapDocDataProperty.put(fields_names[i] + "::Пред_Должность", properties_ZK_members);
                                    
                                    mapDocDataProperty.put(fields_names[i] + "::Зам_Название", properties_ZK_members);
                                    mapDocDataProperty.put(fields_names[i] + "::Зам_ФИО", properties_ZK_members);
                                    mapDocDataProperty.put(fields_names[i] + "::Зам_реал_ФИО", properties_ZK_members);
                                    mapDocDataProperty.put(fields_names[i] + "::Зам_Должность", properties_ZK_members);
                                    
                                    mapDocDataProperty.put(fields_names[i] + "::Члены_Название", properties_ZK_members);
                                    mapDocDataProperty.put(fields_names[i] + "::Члены_ФИО", properties_ZK_members);
                                    mapDocDataProperty.put(fields_names[i] + "::Члены_реал_ФИО", properties_ZK_members);
                                    mapDocDataProperty.put(fields_names[i] + "::Члены_Должность", properties_ZK_members);
                                    
                                    mapDocDataProperty.put(fields_names[i] + "::Секр_Название", properties_ZK_members);
                                    mapDocDataProperty.put(fields_names[i] + "::Секр_ФИО", properties_ZK_members);
                                    mapDocDataProperty.put(fields_names[i] + "::Секр_реал_ФИО", properties_ZK_members);
                                    mapDocDataProperty.put(fields_names[i] + "::Секр_Должность", properties_ZK_members);
                                    
                                }



                                
                                
                                
			}
        }

        public String SumByWord(String Language, String Currency, double Sum)
// Используя модуль MonetaryAmount формирует денежную сумму прописью
        {
            String res = "";
            MonetaryAmount mnw = new MonetaryAmount();
            
            try 
                {
                    if (Currency.equals("руб.")) 
                    {
                        if (Language.equals("RU")) 
                        {
                            res = mnw.sumToString(MonetaryAmount.monLanguage.Ru, 
                                    MonetaryAmount.monCurrency.RUB,
                                    MonetaryAmount.monCentFlag.Dig, Sum);
                        }
                        if (Language.equals("EN")) 
                        {
                            res = mnw.sumToString(MonetaryAmount.monLanguage.En, 
                                    MonetaryAmount.monCurrency.RUB,
                                    MonetaryAmount.monCentFlag.Dig, Sum);
                        }
                    }
                    if (Currency.equals("USD")) 
                    {
                        if (Language.equals("RU")) 
                        {
                            res = mnw.sumToString(MonetaryAmount.monLanguage.Ru, 
                                    MonetaryAmount.monCurrency.USD,
                                    MonetaryAmount.monCentFlag.Dig, Sum);
                        }
                        if (Language.equals("EN")) 
                        {
                            res = mnw.sumToString(MonetaryAmount.monLanguage.En, 
                                    MonetaryAmount.monCurrency.USD,
                                    MonetaryAmount.monCentFlag.Dig, Sum);
                        }
                    }
                    if (Currency.equals("EUR")) 
                    {
                        if (Language.equals("RU")) 
                        {
                            res = mnw.sumToString(MonetaryAmount.monLanguage.Ru, 
                                    MonetaryAmount.monCurrency.EUR,
                                    MonetaryAmount.monCentFlag.Dig, Sum);
                        }
                        if (Language.equals("EN")) 
                        {
                            res = mnw.sumToString(MonetaryAmount.monLanguage.En, 
                                    MonetaryAmount.monCurrency.EUR,
                                    MonetaryAmount.monCentFlag.Dig, Sum);
                        }
                    }
                } 
            catch (Exception e) 
                {
                    throw new RuntimeException(e);
                }
            
            return res;
        }

        public String SumByDigits(String Language, String Currency, double Sum)
// Используя модуль MonetaryAmount формирует денежную сумму прописью
        {
            String res = "";
            String temp_data_sql = "";
            
            try 
                {
                    if (Currency.equals("руб.")) 
                    {
                        if (Language.equals("RU")) 
                        {
                            temp_data_sql = "select replace(replace(format(" 
                                    + Sum + ", 2),',',' '),'.',',') AS FORMAT_SUM";
                            getPreData(temp_data_sql);
                            
                            res = cfgTuner.getParameter("FORMAT_SUM") + " руб.";

                        }
                        if (Language.equals("EN")) 
                        {
                        }
                    }
                    if (Currency.equals("USD")) 
                    {
                        if (Language.equals("RU")) 
                        {
                        }
                        if (Language.equals("EN")) 
                        {
                        }
                    } 
                    if (Currency.equals("EUR")) 
                    {
                        if (Language.equals("RU")) 
                        {
                        }
                        if (Language.equals("EN")) 
                        {
                        }
                    }
                } 
            catch (Exception e) 
                {
                    throw new RuntimeException(e);
                }
            
            return res;
        }

        
        public void CreateDocFiles() throws Exception
// Ищет все шаблоны печатных форм, соответствующие данному типу документа и подает 
// их на вход в модуль Е.Александрова на подстановку значений для конкретного 
// документа, в случае успеха обновляет данные в таблице doc_out_files
        
        {
            String[] tmpl_files_names = null;
            String[] tmpl_files_exts = null;
            String[] tmpl_files_content_types = null;
            String[] tmpl_fs_files_names = null;
            
            
            getData("getTemplateFilesInfo");
            String fsStoragePath = cfgTuner.getParameter("file_storage_path");
            
            tmpl_files_names = cfgTuner.getParameter("TMPL_FILES_NAMES").split(",");
            tmpl_files_exts = cfgTuner.getParameter( "TMPL_FILES_EXTS" ).split( "," );
            tmpl_files_content_types = cfgTuner.getParameter( "TMPL_FILES_CONTENT_TYPES" ).split( "," );
            tmpl_fs_files_names = cfgTuner.getParameter("TMPL_FS_FILES_NAMES").split(",");
            
//            getData("delete_doc_out_files"); // удаляем прежние версии 
            cfgTuner.getCustomSection("delete_doc_out_files"); // удаляем прежние версии 
            
            int countTemplateFiles = 0;
            try 
                {
                    countTemplateFiles = Integer.parseInt(cfgTuner.getParameter("cnt"));
                    
                    if (countTemplateFiles>0) {

                        for (int i=0; i<tmpl_files_names.length; i++) {
                            
                            String fsFullTemplatePathName = fsStoragePath + tmpl_fs_files_names[i];
                            String fsFullOutputPath = fsStoragePath + "docs_out"  + "/"
                                    + cfgTuner.getParameter("doc_id") + "/";
                            String DBFullOutputPath = "docs_out"  + "/"
                                    + cfgTuner.getParameter("doc_id") + "/";
                            String fsFullDocFilePathName = fsFullOutputPath 
                                    + tmpl_fs_files_names[i].substring(tmpl_fs_files_names[i].lastIndexOf("/")+1);
                            DBFullOutputPath+=tmpl_fs_files_names[i].substring(tmpl_fs_files_names[i].lastIndexOf("/")+1);
                            try
                                {
                                    File dir = new File(fsFullOutputPath);
                                    if (!dir.exists()) {
                                        dir.mkdirs();
                                    }

                                    DocxReplace DocxFileMaker = new DocxReplace();
//                                    DocxFileMaker.replace(fsFullTemplatePathName, fsFullDocFilePathName, mapDocData);
                                    DocxFileMaker.replaceMap(fsFullTemplatePathName, fsFullDocFilePathName, mapDocDataVec, mapDocDataProperty);
                        
                                    File docFile = new File(fsFullDocFilePathName);
                                    long lenDocFile = docFile.length();
                        
                                    getData("getDocOutFilesNorderInfo");

                                    String insert_into_doc_out_files_sql = "";
                                    insert_into_doc_out_files_sql = "insert into doc_out_files"
                                        + " (doc_id, norder, file_name, file_ext, file_content_type,"
                                        + " file_size, upload_date, fs_file_name)"
                                        + " values(" + cfgTuner.getParameter("doc_id") + ", "
                                        + cfgTuner.getParameter("DOC_OUT_NORDER") + ", "
                                        + "'" + tmpl_files_names[i] + "', " 
                                        + "'" + tmpl_files_exts[i] + "', " 
                                        + "'" + tmpl_files_content_types[i] + "', " 
                                        + lenDocFile + ", "
                                        + " now(), " 
                                        + "'" + DBFullOutputPath + "')";
                        
                                    IOUtil.writeLogLn( "+++ Insert into doc_out_files SQL: " 
                                            + insert_into_doc_out_files_sql, rm );
// выполняем запрос к базе                        
                                    getPreData(insert_into_doc_out_files_sql);
                                
                                }
                            catch (Exception e) {
                                    e.printStackTrace(System.out);
                                    IOUtil.writeLogLn("DocOutFile Exception: " + e.toString(), rm);
                                }
                
                            }
                    }
                } 
            catch (NumberFormatException e) {
                e.printStackTrace(System.out);
                IOUtil.writeLogLn("DocOutFile Exception: " + e.toString(), rm);

                } 
            // if cnt
            
            
                
        }
        
/* */   
        public void OutputHashtable()
// Вывод всех данных из хэш-таблицы в текствый файл (использовалось для отладки)
        {
            Enumeration mapDocData_names;
            String mapDocData_key;
            
            mapDocData_names = mapDocData.keys();
            IOUtil.writeLogLn("<b>+++++ ServiceCreateDocFiles - Параметры:</b>", rm);
            while(mapDocData_names.hasMoreElements()) {
                mapDocData_key = (String) mapDocData_names.nextElement();
                IOUtil.writeLogLn(mapDocData_key + "='" + mapDocData.get(mapDocData_key) + "'", rm);
                }
            
        }
/**/
        


/**
 * Служебный класс для заполнения хэш-таблицы mapDocDataVec
 * Перегружает метод Add в зависимости от входного параметра (строка или вектор)
 * Автор: Яковлев А.В.
  */

private class VectorDocDataHashtable {

        void Add(String mapDocDataName, String stDocDataValue)
// Второй входной параметр stDocDataValue - строка
        {
            Vector<String> VectorDocDataName = new Vector<String>();
            VectorDocDataName.add(stDocDataValue);
            mapDocDataVec.put(mapDocDataName, VectorDocDataName);
        }
        
        void Add(String mapDocDataName, Vector<String> vcDocDataValue)
// Второй входной параметр vcDocDataValue - вектор
        {
            mapDocDataVec.put(mapDocDataName, vcDocDataValue);
        }
        
/**/    
    
    
    
}
}
