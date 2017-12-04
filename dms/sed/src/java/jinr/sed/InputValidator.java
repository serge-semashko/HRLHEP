package jinr.sed;

import dubna.walt.util.DBUtil;
import dubna.walt.util.IOUtil;
import dubna.walt.util.ResourceManager;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Проверка и, при необходимости, корректировка значений параметров запроса.
 * Описания параметров берутся из таблицы d_param_registry. При изменении
 * описаний в d_param_registry ТРЕБУЕТСЯ РЕСТАРТ СЕРВЛЕТА. Если соответствующее
 * описание параметра не найдено, его проверка не производится.
 *
 * @author serg
 */
public class InputValidator {

    private List<InputParameter> ipl = null;
    private ResourceManager rm;

    /**
     * Class constructor. Берет из БД описания зарегистрированных входных
     * параметров и сохраняет их в виде списка InputParameter
     *
     * @param rm
     */
    public InputValidator(ResourceManager rm) {
        this.rm = rm;
        obtainParametersList();
    }

    private void obtainParametersList() {
        try {
            DBUtil dbUtil = makeDBUtil();
            String sql = "select name, ifnull(doc_types_id, ''), des, field_type, field_size from a_param_registry";
            IOUtil.writeLogLn(1, "<b>=======  InputValidator.getParametersList() - SQL: </b>" + sql, rm);
            ResultSet rs = dbUtil.getResults(sql);
            ipl = (List<InputParameter>) new ArrayList<InputParameter>();
            System.out.printf("=======  InputValidator.getParametersList()...");
            while (rs.next()) {
                addParamInfo(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5));
            }
            dbUtil.close();
        } catch (Exception e) {
            System.out.printf("=======  InputValidator.getParametersList() - ERROR: " + e.toString());
            e.printStackTrace();
            IOUtil.writeLogLn("=======  InputValidator.getParametersList() - ERROR: " + e.toString(), rm);

        }
    }
    /**
     * Вывод в лог списка зарегистрированных параметров
     * @param pw Writer файла лога
     */
    public void log(PrintWriter pw){
        pw.println("<h4> InputValidator: </h4>");
        pw.println("<table><tr><td>name</td><td> docTypesId</td><td>fieldType</td><td>fieldSize</td><td>des</td></tr>");
       for (InputParameter ip : ipl) {
           ip.log(pw);
        }
        pw.println("</table><hr>");
    }

    /**
     * Поиск описания параметра.
     * Если задан objectTypeId, то приоритет имеет описание,
     * приписанному к этому objectTypeId (если оно найдено).
     *
     * @param name имя параметра
     * @param objectTypeId тип документа или объекта
     * @return найденное описание. null, если не найдено
     */
    public InputParameter getParamInfo(String name, int objectTypeId) {
        if (ipl == null) {
            obtainParametersList();
        }
        if (objectTypeId > 0) {
            String ot = "," + objectTypeId + ",";
            for (InputParameter ip : ipl) {
                if (ip.name.equals(name) && ip.docTypesId.contains(ot)) {
                    return ip;
                }
            }
        }
        for (InputParameter ip : ipl) {
            if (ip.name.equals(name) && ip.docTypesId.equals(",,")) {
                return ip;
            }
        }
        return obtainParamInfo(name, objectTypeId);
    }

    /**
     * Получение из БД (таблица d_fields) специфического для определенного типа объекта
     * описания параметра и добавление его в коллекцию описаний. Если описание
     * не найдено, то добавляется пустое описание.
     *
     * @param name имя параметра из запроса и соответствующего поля БД
     * @param objectTypeId
     * @return
     */
    private InputParameter obtainParamInfo(String name, int objectTypeId) {
        if (objectTypeId == 0) {
            return null;
        }
        InputParameter ip = null;
        try {
            DBUtil dbUtil = makeDBUtil();
            String sql = "select name, type, size from d_fields"
                    + " where type_id=" + Integer.toString(objectTypeId) + " and field_db_name='" + name + "' and is_active=1";
//            IOUtil.writeLogLn("=======  InputValidator.obtainParamInfo() sql='" + sql + "'", rm);
            ResultSet rs = dbUtil.getResults(sql);
            if (rs.next()) {
                ip = addParamInfo(name, Integer.toString(objectTypeId), rs.getString(1), rs.getString(2), rs.getInt(3));
                rs.close();
                dbUtil.close();
            } else {
                addParamInfo(name, Integer.toString(objectTypeId), "", "", 0);
            }
        } catch (Exception e) {
            System.out.printf("=======  InputValidator.obtainParamInfo() - ERROR: " + e.toString());
            e.printStackTrace();
            IOUtil.writeLogLn("=======  InputValidator.obtainParamInfo() - ERROR: " + e.toString(), rm);
            ip = addParamInfo(name, Integer.toString(objectTypeId), "", "", 0);
        }
        return ip;
    }

    /**
     * Создание описания параметра и добавление его в коллекцию.
     *
     * @param name
     * @param docTypesId
     * @param des
     * @param fieldType
     * @param fieldSize
     * @return
     */
    public InputParameter addParamInfo(String name, String docTypesId, String des, String fieldType, int fieldSize) {
        InputParameter ip = new InputParameter(name, docTypesId, des, fieldType, fieldSize);
        ip.rm = rm;
        ipl.add(ip);
        return ip;
    }

    /**
     * Проверяет значение параметра на соответствие его описанию. Для числовых
     * параметров (int, float) выделяет из значения число, отбрасывая остальное.
     * Для текстовых параметров обрезает значение при превышении длины. Для дат
     * проверяет валидность даты. При заданном objectTypeId (>0) приоритет имеет
     * описание параметра для этого objectTypeId. При не заданном objectTypeId
     * (=0) просматриваются только описания, не привязанные к типу.
     *
     * @param name имя входного параметра
     * @param value полученное значение параметра
     * @param objectTypeId тип документа или объекта, в связи с которым параметр
     * пришел от клиента.
     * @return Проверенное и при необходимости скорректированное значение
     * параметра. null, если параметр не может быть приведен в соответствие
     * описанию. В этом случае сообщение об ошибке может быть получено через
     * метод getErrMsg()
     */
    public String getValidatedValue(String name, String value, int objectTypeId) {
        if (value.length() == 0) {
            return "";
        }
        InputParameter ip = getParamInfo(name, objectTypeId);
        if (ip != null) {
            return ip.getCheckedValue(value);
        } else {
            return value;
        }
    }

        /**
     * Получить сообщение об ошибке в случае, когда значение параметра не может
     * быть приведено в соответствие описанию.
     *
     * @param name
     * @param value
     * @param objectTypeId
     * @return Сообщение об ошибке или пустая строка, если ошибки не обнаружено.
     */
    public String getErrMsg(String name, String value, int objectTypeId) {
        if (value.length() == 0) {
            return "";
        }
        InputParameter ip = getParamInfo(name, objectTypeId);
        if (ip != null) {
            return ip.getErrMsg(value);
        } else {
            return "";
        }
    }

    private DBUtil makeDBUtil() {
        DBUtil dbUtil = null;
        String usr = rm.getString("usr", false, rm.getString("usr", true));
        String pw = rm.getString("pw", false, rm.getString("pw", false));
        String connStr = rm.getString("connString");

//        IOUtil.writeLogLn("+++++ InputValidator: connecting... " + connStr + "/" + usr, rm);
        try {
            Class.forName(rm.getString("dbDriver"));        // init the JDBC driver
            Connection conn = DriverManager.getConnection(rm.getString("connString")
                    + rm.getString("database")
                    + rm.getString("connParam"), rm.getString("usr"), rm.getString("pw"));
            conn.setAutoCommit(false);
            dbUtil = new DBUtil(conn, "SED:InputValidator");
            dbUtil.db = DBUtil.DB_MySQL;
            dbUtil.allocate();
            dbUtil.nrConnsToKeep = 0;
        } catch (Exception e) {
            dbUtil = null;
            e.printStackTrace();
//            IOUtil.writeLogLn("=======  InputValidator.makeDBUtil() - ERROR: " + e.toString(), rm);
        }
        return dbUtil;
    }
}
