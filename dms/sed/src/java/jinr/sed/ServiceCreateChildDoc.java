package jinr.sed;

import dubna.walt.util.IOUtil;

/**
 *
 * @author serg
 */
public class ServiceCreateChildDoc extends ServiceViewDoc {
    
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
                makeSelectSQL(false);
// выполняем запрос к базе
                getPreData(sql);
            }

// выводим форму с полями документа
//            makeTable();

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
    
}
