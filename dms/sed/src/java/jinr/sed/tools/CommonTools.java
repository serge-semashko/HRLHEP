package jinr.sed.tools;

import dubna.walt.util.Tuner;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CommonTools {

    /**
     * Возвращает "человеческое" представление объёма данных. Например: 123,4
     * МБайт или: 1,23 Кбайт
     *
     * @param length
     * @return
     */
    public static String getStringValue(Object o, String def) {
        if (o != null) {
            return (String) o;
        }
        return def;
    }

    public static Integer parseInt(Object o, Integer defaultVal) {
        try {
            return Integer.parseInt(o.toString());
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public static java.util.Date parseDate(String sDateTime, String pattern) {
        try {
            Locale loc = new Locale("ru", "RU");
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, loc);
            return dateFormat.parse(sDateTime);
        } catch (Exception e) {
            return null;
        }
    }

    public static float parseFloat(Object o, float defaultVal) {
        try {
            return Float.parseFloat(o.toString());
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public static double parseDouble(Object o, double defaultVal) {
        try {
            return Double.parseDouble(o.toString());
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public static Object isNull(Object o, Object defaultObject) {
        return (o == null) ? defaultObject : o;
    }

    public static String getShortText(String text, int maxLength) {
        if ((text == null) || (text.length() <= maxLength)) {
            return text;
        }
        int pos = maxLength - 1;
        while (pos >= 0 && Character.isLetter(text.charAt(pos))) {
            pos--;
        }
        if (pos <= 0) {
            pos = maxLength - 1;
        }
        return text.substring(0, pos) + " ...";
    }

    public static String listId2WhereElelement(List<Integer> idList, String fieldName) {
        StringBuffer where = new StringBuffer();
        where.append(" (");
        for (int i = 0; i < idList.size(); i++) {
            if (i > 0) {
                where.append(" OR ");
            }
            where.append(fieldName + "=" + idList.get(i));
        }
        where.append(") ");
        return where.toString();
    }

    public static String listId2WhereElelement(int[] idArr, String fieldName) {
        StringBuffer where = new StringBuffer();
        where.append(" (");
        for (int i = 0; i < idArr.length; i++) {
            if (i > 0) {
                where.append(" OR ");
            }
            where.append(fieldName + "=" + idArr[i]);
        }
        where.append(") ");
        return where.toString();
    }

    public static ArrayList<HashMap> filerListOfHashMap(List<HashMap> inpList, String key, ArrayList<Integer> idList) {
        ArrayList<HashMap> outList = new ArrayList<HashMap>();
        for (HashMap hm : inpList) {
            if (!hm.containsKey(key) || !(hm.get(key) instanceof Integer) || !idList.contains(hm.get(key))) {
                continue;
            }
            outList.add(hm);
        }
        return outList;
    }

    public static ArrayList<HashMap> leftOuterJoinLists(ArrayList<HashMap> inpList, List<HashMap> joinList, String key, String joinKey) {
        for (HashMap hm : inpList) {
            for (HashMap joinHm : joinList) {
                if (hm.get(key).equals(joinHm.get(joinKey))) {
                    hm.putAll(joinHm);
                }
            }
        }
        return inpList;
    }

    /**
     *
     * Подготовка значения параметра к вставке в текст SQL запроса в зависимости
     * от типа поля
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
                    || paramType.equals("float") || paramType.equals("float0") || paramType.equals("numeric")
                    || paramType.equals("date") || paramType.equals("datetime")) {
                return "null";
            }
        }
        if (paramType.equals("int")) {
            return paramValue;
        } else if (paramType.equals("float") || paramType.equals("float0")) {
            return paramValue;
        } else if (paramType.equals("boolean")) {
            return (paramValue.equals("on") || paramValue.equals("1")) ? "1" : "0";
        } else if (paramType.equals("varchar")) {
            return "'" + paramValue + "'";
        } else if (paramType.equals("text")) {
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
            return null;
        }
    }

    /**
     *
     * Подготовка значения параметра к вставке в текст SELECT запроса в
     * зависимости от типа поля
     *
     * @param fieldName - имя параметра в Tuner, в котором лежит нужное значение
     * @param fieldType - тип значения (int, float, varchar, text, date,
     * datetime
     * @param cfgTuner - Tuner, с которым работаем
     * @return значение параметра, подготовленное к вставке в SQL запрос. int,
     * varchar - как есть, date и datetime - преобразование из даты в строку по
     * стандартному формату из Tuner
     */
    public static String makeSelectField(String fieldName, String fieldType, Tuner cfgTuner) {
        if (fieldType.equals("int")) {
            return fieldName;
        } else if (fieldType.equals("float")) {
            return fieldType;
        } else if (fieldType.equals("varchar")) {
            return fieldName;
        } else if (fieldType.equals("text")) {
            return fieldName;
        } else if (fieldType.equals("date")) {
            return "DATE_FORMAT(" + fieldName + ",'" + cfgTuner.getParameter("dateFormat") + "') as " + fieldName;
        } else if (fieldType.equals("datetime")) {
            return "DATE_FORMAT(" + fieldName + ",'" + cfgTuner.getParameter("dateTimeFormat") + "') as " + fieldName;
        } else {
            System.out.println("!!! UNKNOWN DATA FORMAT: " + fieldType);
            return fieldName;
        }
    }

}
