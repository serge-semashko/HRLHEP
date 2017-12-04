package jinr.dms;

import dubna.walt.util.IOUtil;
import dubna.walt.util.Tuner;

/**
 * Просмотр и редактирование одной записи справочника ServiceEditDocsData
 */
public class ServiceViewDoc extends dubna.walt.service.TableServiceSpecial {

	private int nr;
	private int numFields = 0;
	private int numCols = 0;
//	private String[] fields_types = null;
	private String[] form_fields_types = null;
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

//    получаем список полей справочника, их названия и типы из описания документа
// (результат выполнения запросов в секции [preSQLs] .cfg - файла)
			fields = cfgTuner.getParameter("FIELDS").split(",");
//		  fields_names = cfgTuner.getParameter( "FIELDS_NAMES" ).split( "," );
//		  fields_types = cfgTuner.getParameter( "FIELDS_TYPES" ).split( "," );
			form_fields_types = cfgTuner.getParameter("FORM_FIELDS_TYPES").split(",");
			numFields = fields.length;

// отображение формы
// конструируем SQL для выборки значений полей документа
			sql = "select ";
			for (int i = 0; i < numFields; i++) {
				sql += fields[i] + ", ";
			}
			sql += cfgTuner.getParameter("SYS_FIELDS")
					+ " from " + cfgTuner.getParameter("TABLE_NAME")
					+ " where id=" + cfgTuner.getParameter("DOC_DATA_RECORD_ID");
			IOUtil.writeLogLn("+++ Get doc data record SQL: " + sql, rm);
// выполняем запрос к базе
			getPreData(sql);

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
