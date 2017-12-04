package jinr.sed;

import dubna.walt.util.IOUtil;
import dubna.walt.util.ResourceManager;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Описание входного параметра и проверка конкретного значения на валидность.
 *
 * @author serg
 */
public class InputParameter {

    public String name;  // имя параметра из запроса
    public String docTypesId; //список типов объектов (0 - все типы)
    public String des;  // текстовое название параметра
    public String fieldType; // тип поля в БД
    public int fieldSize;  // размер поля в БД
    public ResourceManager rm = null;
    private String errMsg = "";

    public InputParameter(String name, String docTypesId, String des, String fieldType, int fieldSize) {
        this.name = name;
        this.docTypesId = "," + docTypesId.replaceAll(" ", "") + ",";
        this.des = des;
        this.fieldType = fieldType;
        this.fieldSize = fieldSize;
        System.out.println("InputParameter: name=" + name + "; docTypesId=" + docTypesId
                + "; des=" + des + "; fieldType=" + fieldType + "; fieldSize=" + fieldSize);
    }

    /**
     *
     * @param value полученное значение параметра
     * @return Проверенное и при необходимости скорректированное значение
     * параметра. null, если параметр не может быть приведен в соответствие
     * описанию.
     */
    public synchronized String getCheckedValue(String value) {
        if (value.length() == 0) {
            return "";
        }
        String val = value;
        switch (fieldType) {
            case "xxx":
                val = "";
                 System.out.println("HACK: " + name + "=" + value);
                break;
            case "int":
                val = checkInt(value);
                break;
            case "float":
                val = checkFloat(value);
                break;
            case "float0":
                val = checkFloat(value);
                break;
            case "date":
                val = checkDate(value);
                break;
            case "varchar":
                val = checkVarchar(value);
                break;
        }
        if(errMsg.length() > 0)
            IOUtil.writeLogLn(0, "<br><b>***** ERROR: InputValidator.getCheckedValue(" + name + "/" + fieldType+ ": " + value + ")=" + val + "; ERR='" + errMsg + "';</b> <br>", rm);
        else
            IOUtil.writeLogLn(9, "***** InputValidator.getCheckedValue(" + name + "/" + fieldType+ ": " + value + ")=" + val + "; ERR='" + errMsg + "'; <br>", rm);
        return val;
    }

    /**
     * Получить сообщение об ошибке в случае, когда значение параметра не может
     * быть приведено в соответствие описанию или при обрезании длинной строки.
     *
     * @param value
     * @return Сообщение об ошибке или пустая строка, если ошибки не обнаружено.
     */
    public synchronized String getErrMsg(String value) {
        errMsg = "";
        getCheckedValue(value);
    IOUtil.writeLogLn("***** InputValidator.getErrMsg(" + name + "/" + fieldType+ ": " + value + ")=" + errMsg + "; <br>", rm);
        return errMsg;
    }
//<tr><td>name</td><td> docTypesId</td><td>des</td><td>fieldType</td><td>fieldSize</td></tr>");"

    public void log(PrintWriter pw) {
        pw.println("<tr><td>" + name + "</td><td>" + docTypesId + "</td><td>" + fieldType + "</td><td>" + fieldSize + "</td><td>" + des + "</td></tr>");
    }

    /**
     * Проверка значения параметра типа int
     *
     * @param value исходное значение входного параметра
     * @param ip зарегистрированное описание данного параметра
     * @return распознанное целое значение, null если целое не может быть
     * определено.
     */
    private String checkInt(String value) {
        try {
            int iv = Integer.parseInt(value);
            return Integer.toString(iv);
        } catch (Exception e) {
            errMsg = "Ошибка - не целое число";
            if (rm == null) {
                e.printStackTrace();
            } else {
                IOUtil.writeLogLn("***** InputValidator.checkInt(" + name + "=" + value + "); Parse Integer ERROR: " + e.toString(), rm);
            }
            return null;
        }
    }

    /**
     * Проверка значения параметра типа float
     *
     * @param value исходное значение входного параметра
     * @param ip зарегистрированное описание данного параметра
     * @return распознанное float значение, null если float не может быть
     * определено.
     */
    private String checkFloat(String value) {
        try {
            double fv = Double.parseDouble(value);
            return Double.toString(fv);
        } catch (Exception e) {
            errMsg = "Ошибка - не число";
            if (rm == null) {
                e.printStackTrace();
            } else {
                IOUtil.writeLogLn("***** InputValidator.checkFloat(" + name + "=" + value + "); Parse Float ERROR: " + e.toString(), rm);
            }
            return null;
        }
    }

    /**
     * Проверка значения параметра типа date
     *
     * @param value исходное значение входного параметра
     * @param ip зарегистрированное описание данного параметра
     * @return распознанное значение date в формате dd.mm.yyyy, null если date
     * не может быть определено.
     */
    private String checkDate(String value) {
        String val = value;
        try {
            val = val.replaceAll("/", ".").replaceAll("-", ".").replaceAll(",", ".");
            val = val.replaceAll("[^0-9\\.]", "");
            if (val.length() > 10) {
                val = val.substring(0, 9);
            }
            String sa[] = val.split("\\.");
//            IOUtil.writeLogLn("***** InputParameter.checkDate(" + value + ") => " + val + "; sa.length=" + sa.length, rm);
            val = sa[0] + "." + sa[1] + "." + sa[2];
            if (val.length() == 10) {
                try {
                    DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                    formatter.setLenient(false);
                    Date date = formatter.parse(val);
//                    System.out.println("***** DATE=" + date);
                    return val;
                } catch (Exception ee) {
                    errMsg = "Ошибка - неверная дата";
                    if (rm == null) {
                        ee.printStackTrace();
                    } else {
                        IOUtil.writeLogLn("***** InputParameter.checkDate(" + name + "=" + value + "); ERROR: " + errMsg + "; " + ee.toString(), rm);
                    }
                    return null;
                }

            } else {
                errMsg = "Ошибка в формате даты"; // + val.length();
                return null;
            }

        } catch (Exception e) {
            errMsg = "Ошибка в формате даты";
            if (rm == null) {
                e.printStackTrace();
            } else {
                IOUtil.writeLogLn("***** InputParameter.checkDate(" + name + "=" + value + "); ERROR: " + errMsg + "; "  + e.toString(), rm);
            }
            return null;
        }
    }

    /**
     * Проверка значения параметра типа varchar
     *
     * @param value исходное значение входного параметра
     * @param ip зарегистрированное описание данного параметра
     * @return value, обрезанное до максимальной длины (если она задана в
     * описании параметра)
     */
    private String checkVarchar(String value) {
        if (fieldSize > 0 && value.length() > fieldSize) {
            errMsg = "Длинная строка обрезана до " + fieldSize + " симв.";
            return value.substring(0, fieldSize);
        } else {
            return value;
        }
    }

}
