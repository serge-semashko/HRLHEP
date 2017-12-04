package jinr.dms;

import dubna.walt.util.IOUtil;
import dubna.walt.util.Tuner;

/**
 * Просмотр и редактирование данных документа
 */
public class ServiceEditDocData extends dubna.walt.service.TableServiceSpecial {

	private int numFields = 0;
	private String[] fields = null;
	private String[] fields_types = null;
	private String[] form_fields_types = null;
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

//    получаем список полей документа, их названия и типы из описания типа документа
// (результат выполнения запросов в секции [preSQLs] .cfg - файла)
			fields = cfgTuner.getParameter("FIELDS").split(",");
			fields_types = cfgTuner.getParameter("FIELDS_TYPES").split(",");
			form_fields_types = cfgTuner.getParameter("FORM_FIELDS_TYPES").split(",");
			numFields = fields.length;

			if (cfgTuner.enabledOption("cop=save")) {
				doSave(); // сохранение записи по кнопке "Сохранить"
			}
			if (!cfgTuner.enabledOption("cop") // отображение формы, если действия нет
			  || cfgTuner.enabledOption("ERROR")) { // или была ошибка выполнения
// конструируем SQL для выборки значений полей документа
				sql = "select ";
				for (int i = 0; i < numFields; i++) {
					sql += fields[i] + ", ";
					if (form_fields_types[i].equals("4")) {
						sql += fields[i] + "_id, ";
					}
				}

				sql += cfgTuner.getParameter("SYS_FIELDS")
						+ " from " + cfgTuner.getParameter("TABLE_NAME")
						+ " where id=" + cfgTuner.getParameter("DOC_DATA_RECORD_ID");
				IOUtil.writeLogLn("=================", rm);
				IOUtil.writeLogLn("+++ Get info record data SQL: " + sql, rm);
				IOUtil.writeLogLn("=================", rm);
// выполняем запрос к базе
				getPreData(sql);
// начало формы редактирования				
				cfgTuner.outCustomSection("start form", out);
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
	 * Сохранение полей документа в БД
	 *
	 * @throws Exception
	 */
	private void doSave() throws Exception {
// Конструируем начало SQL запроса - список всех полей записи
		String fds = "";
		for (int i = 0; i < numFields; i++) {
			fds += fields[i] + ", ";
			if (form_fields_types[i].equals("4")) { // справочник - сохраняем ID 
				fds += fields[i] + "_id, ";
			} else if (form_fields_types[i].equals("7")) {  // сумма - сохраняем валюту
				fds += fields[i] + "_curr, ";
			}
		}
		IOUtil.writeLogLn("+++ FIELDS: '" + fds + "'", rm);

		sql = "replace into " + cfgTuner.getParameter("TABLE_NAME") + " (id, " // SQL обновления полей документа
				+ fds
				+ cfgTuner.getParameter("SYS_FIELDS_UPDATE")
				+ ") values (" + cfgTuner.getParameter("DOC_DATA_RECORD_ID");;

// добавляем к запросу часть " values (...) " с данными из формы
		for (int i = 0; i < numFields; i++) {
			sql += ", " + makeParamValue(fields[i], fields_types[i], cfgTuner);
			if (form_fields_types[i].equals("4")) {  // справочник - сохраняем ID 
				sql += ", " + makeParamValue(fields[i] + "_id", "int", cfgTuner);
			} else if (form_fields_types[i].equals("7")) {  // сумма - сохраняем валюту
				sql += ", " + makeParamValue(fields[i] + "_curr", "varchar", cfgTuner);
			}
		}

// добавляем к запросу часть " values (...) " со скрытыми полями
		String[] sysFields = cfgTuner.getParameter("SYS_FIELDS_UPDATE").split(",");
		String[] sysFieldsTypes = cfgTuner.getParameter("SYS_FIELDS_UPDATE_TYPES").split(",");
		for (int i = 0; i < sysFields.length; i++) {
			sql += ", " + makeParamValue(sysFields[i], sysFieldsTypes[i], cfgTuner);
		}
		sql += ")";

		IOUtil.writeLogLn("+++ UPDATE RECORD SQL: '" + sql + "'", rm);
//	Наконец выполняем запрос
		getPreData(sql);

	}

	/**
	 *
	 * Подготовка значения параметра к вставке в текст SQL запроса в зависимости
	 * от типа поля Вероятно, метод пригодится во многих местах, поэтому сраза
	 * сделан STATIC. В перспективе - доработать и вытащить в какой-нибудь класс
	 * типа "utils"
	 *
	 * @param paramName - имя параметра в Tuner, в котором лежит нужное значение
	 * @param paramType - тип значения (int, boolean, varchar, date, datetime
	 * или sysdate
	 * @param cfgTuner - Tuner, с которым работаем
	 * @return значение параметра, подготовленное к вставке в SQL запрос. int -
	 * как есть, varchar - в кавычках, date и datetime - преобразование из
	 * строки в дату по стандартному формату, sysdate => now()
	 */
	public static String makeParamValue(String paramName, String paramType, Tuner cfgTuner) {
		String paramValue = cfgTuner.getParameter(paramName.trim());
		if (paramValue.length() == 0) {
			if (paramType.equals("int") || paramType.equals("boolean")
					|| paramType.equals("date") || paramType.equals("datetime")) {
				return "null";
			}
		}
		if (paramType.equals("int")) {
			return paramValue;
		} else if (paramType.equals("float")) {
			return paramValue;
		} else if (paramType.equals("boolean")) {
			return (paramValue.equals("on") || paramValue.equals("1")) ? "1" : "0";
		} else if (paramType.equals("varchar")) {
			return "'" + paramValue + "'";
		} else if (paramType.equals("dir")) {
			return "'" + paramValue + "'";
		} else if (paramType.equals("date")) {
			return "STR_TO_DATE('" + paramValue + "','" + cfgTuner.getParameter("dateFormat") + "')";
		} else if (paramType.equals("datetime")) {
			return "STR_TO_DATE('" + paramValue + "','" + cfgTuner.getParameter("dateTimeFormat") + "')";
		} else if (paramType.equals("sysdate")) {
			return "now()";
		} else {
			System.out.println("!!! UNKNOWN DATA FORMAT: " + paramType);
			return paramValue;
		}
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
