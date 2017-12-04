package jinr.sed;

import java.sql.ResultSet;
import dubna.walt.util.DBUtil;
import dubna.walt.util.IOUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.util.Vector;

/**
 * Сервис для загрузки справочников из внешней базы данных
 *
 */
public class ServiceLoadExtData extends dubna.walt.service.Service {

    private PreparedStatement checkStmt = null;
    private PreparedStatement updateStmt = null;
    private PreparedStatement okStmt = null;
    private Vector<String> vData = null;

//    private int nr;
    private int numSrcCols = 0;
    private String[] dest_fields_types = null;
    private String[] dest_fields = null;
    private String[] dest_fields_names = null;
    private int numDestCols = 0;

    /**
     * Основной метод - определяет ход работы.
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        ResultSet r;
        DBUtil srcDBUtil = null;
        vData = new Vector(numSrcCols); // сюда будем складывать новые данные для insert или update
        try {
            cfgTuner.outCustomSection("report header", out);
            //		в [report header] DB переключается на внешнюю DB для импорта справочника
            srcDBUtil = dbUtil;
//            String request = cfgTuner.getParameter("REQUEST");
            //		получаем ResultSet с внешними данными для справочника
            r = srcDBUtil.getResults(cfgTuner.getParameter("REQUEST"));
        } catch (Exception e) {
            showErr(e, "Could not get external data...");
            r = null;
        }

        try {
            if (r != null) {
//					String[] headers = DBUtil.getColNames(r);
                ResultSetMetaData metaData = r.getMetaData();
                numSrcCols = metaData.getColumnCount();

                dest_fields = cfgTuner.getParameter("DEST_FIELDS").split(",");
                dest_fields_names = cfgTuner.getParameter("DEST_FIELDS_NAMES").split(",");
                dest_fields_types = cfgTuner.getParameter("DEST_FIELDS_TYPES").split(",");
                numDestCols = dest_fields.length;
                useDb(""); // снова используем обычный коннект к нашей базе

//			   System.out.println( "+++ numSrcCols=" + numSrcCols + "; numDestCols=" + numDestCols );
                String s = "";
                String h_dest = "";
                for (int i = 0; i < numDestCols; i++) {
                    s += "?, "; // заготовка для updateStmt - нужное кол-во "?" по кол-ву SRC-полей
                    h_dest += "<th style='padding: 1px;'>" + dest_fields_names[i] + "</th>";
                }
//                String h_src = "";
//                for (int i = 0; i < numSrcCols; i++) {
//                    h_src += "<td>" + dest_fields_names[i] + "</td>";
//                }

                cfgTuner.addParameter("h_dest", h_dest); // названия полей источника (для заголовка таблицы)
//                cfgTuner.addParameter("h_src", h_src); // названия полей приемника (нашей таблицы)
                cfgTuner.outCustomSection("start load", out); // Выводим заголовок таблицы 

// Готовим prepareStatement-ы для сверки и обновления нашей таблицы
                Connection conn = dbUtil.getConnection();
                conn.setAutoCommit(true);

                String sql = "select " + cfgTuner.getParameter("DEST_FIELDS") + " IS_MANUAL from " + cfgTuner.getParameter("TABLE_NAME") + " where id=?";
                IOUtil.writeLogLn("+++ CHECK RECORD SQL: '" + sql + "'", rm);
                checkStmt = conn.prepareStatement(sql);

                String destImportedFields = "";
                for (int i = 0; i < numSrcCols; i++) {
                    destImportedFields += dest_fields[i] + ", ";
                }
                sql = "replace into " + cfgTuner.getParameter("TABLE_NAME") + " (" + destImportedFields + " changed, is_deleted) values (" + s + " now(), 0)";
                IOUtil.writeLogLn("+++ UPDATE STATEMENT: '" + sql + "'", rm);
                updateStmt = conn.prepareStatement(sql);

                IOUtil.writeLogLn("+++ OK STATEMENT: '" + "update " + cfgTuner.getParameter("TABLE_NAME") + " set is_deleted=0 where is_manual=0 and id=?" + "'", rm);
                okStmt = conn.prepareStatement("update " + cfgTuner.getParameter("TABLE_NAME") + " set is_deleted=0 where is_manual=0 and id=?");

                while (r.next()) // цикл по полученным записям
                    processRecord(r);

                srcDBUtil.closeResultSet(r);  // закрываем внешнийй коннект,
                srcDBUtil.close();  // который больше не нужен

                showExtraRecords("DELETED");
                showExtraRecords("MANUAL");
//			 showManualRecords();
//				showDeletedRecords();  // показываем исчезнувшие записи, которые пометили на удаление
            }
        } catch (Exception e) {
            showErr(e, " Import external data error");
        } finally {
            cfgTuner.outCustomSection("report footer", out);
            out.flush();
        }
    }

    /**
     *
     * @param r
     */
    protected void processRecord(ResultSet r) {
        vData.clear(); // сбрасываем вектор новых данных
        cfgTuner.addParameter("newData", ""); // и параметры для их вывода
        cfgTuner.addParameter("oldData", "");
        cfgTuner.addParameter("IMPORT_ERROR", "");
        String val;
        try {
            if (!checkRecord(r)) // проверка изменений в данных
            {
                for (int i = 2; i <= numSrcCols; i++) // запихиваем данные в prepareStatement updateStmt для обновления данных
                {
                    val = vData.elementAt(i - 1);
//                    IOUtil.writeLogLn(i + ": '" + val + "'", rm);
                    if (dest_fields_types[i - 1].equals("int")) {
                        try {
                            if(val.length() > 0)
                                updateStmt.setInt(i, Integer.parseInt(val)); //;
                            else
                                updateStmt.setNull(i, java.sql.Types.INTEGER );
                        } catch (Exception e) {
                            updateStmt.setNull(i, java.sql.Types.INTEGER);
                            showErr(e, " Integer field value error: " + dest_fields_names[i] + "='" + vData.elementAt(i - 1) + "'");
                        }
                    } else if (dest_fields_types[i - 1].equals("varchar")) {
                        updateStmt.setString(i, val);
                    }
                }
//                if(!cfgTuner.enabledExpression("IMPORT_ERROR"))
                    updateStmt.executeUpdate();  // обновляем (или добавляем) запись
            } else {
                if(!cfgTuner.enabledExpression("IMPORT_ERROR")) {
                    okStmt.setInt(1, (Integer.parseInt(vData.elementAt(0))));
                    IOUtil.writeLogLn(" +++ execute: '" + okStmt.toString() + "'", rm); 
                    okStmt.executeUpdate(); // помечаем запись, как актуальную
                }
            }
        } catch (Exception ex) {
            showErr(ex, "Process Record Error");
        }
        try {
            cfgTuner.outCustomSection("item", out);  // отображаем запись
        } catch (Exception ex) {
        }
        cfgTuner.addParameter("IMPORT_ERROR", "");

}

     
    /**
     * Проверка записи на изменение. Выполняется запрос в базу СЭД для выборки
     * записи с текущим ID записи. Если запись с таким ID имеется - проверяется
     * равенство всех полей записи. Если где-то встретились разные значения -
     * запись помечается на обновление (в cfgTuner выставляется параметр
     * RECORD=UPDATED)
     *
     * Если записи с таким ID нет - запись помечается на добавление
     * (выставляется параметр RECORD=NEW).
     *
     * Новые значения полей записи складываются в Vector <String> vData
     *
     * @param r данные источника
     * @return true если запись не изменилась
     * @throws Exception
     */
    private boolean checkRecord(ResultSet r) throws Exception {
        boolean isEqual = true;
        boolean idError = false;
        String fieldNew;
        String fieldOld;
        String oldData;
        String newData;
        vData.addElement(r.getString(1).trim()); //ID записи
        int id;
        try { 
            id = r.getInt(1); 
            newData = "<td>" + id + "</td>";
        } catch (Exception e) {
            idError = true;
            newData = "<td>" + vData.elementAt(0) + "</td>";
            id=0;
        }

        updateStmt.setInt(1, id);
        checkStmt.setInt(1, id);
        ResultSet checkResultSet = checkStmt.executeQuery();
        cfgTuner.addParameter("newData", cfgTuner.getParameter("newData") + "<td>" + vData.elementAt(0) + "</td>");
        if (checkResultSet != null && checkResultSet.next()) // record exists
        {
            oldData = "<td>" + id + "</td>";
            for (int i = 2; i <= numSrcCols; i++) {  // сравнение всех полей старой и новой записи
//               Разрешенные типы полей пока тлько int и varchar
                if (dest_fields_types[i - 1].equals("varchar")
                        || dest_fields_types[i - 1].equals("int")) {
//                  Выборка нового значения поля
                    fieldNew = r.getString(i);
                    if (fieldNew == null) {
                        fieldNew = "";
                    }
                    fieldNew = fieldNew.trim();
                    if (dest_fields_types[i - 1].equals("int")) {
                        if(fieldNew.length() > 0)
                            fieldNew = Integer.toString(Integer.parseInt(fieldNew));
                    }
                    vData.addElement(fieldNew);

//                  Выборка старого значения поля
                    fieldOld = checkResultSet.getString(i);
                    if (fieldOld == null) {
                        fieldOld = "";
                    }
                    fieldOld = fieldOld.trim();

//                  Сравниваем значения
                    if (fieldNew.equals(fieldOld)) {
                        oldData += "<td>" + fieldOld + "</td>";
                        newData += "<td>" + fieldNew + "</td>";
                    } else {
                        isEqual = false;
                        oldData += "<td><b>" + fieldOld + "</b></td>";
                        newData += "<td><b>" + fieldNew + "</b></td>";
                    }
                } else {
                    System.out.println(" !!!!! UNKNOWN TYPE: " + dest_fields_types[i - 1]);
                    IOUtil.writeLogLn("!!!!! +++ ERROR: UNKNOWN FIELD TYPE: " + dest_fields_types[i - 1], rm);
                    cfgTuner.addParameter("IMPORT_ERROR", "<td colspan=" + numSrcCols + "><b> Неизместный тип поля:</b> " + dest_fields_types[i - 1]+ "</td>");

                }
            }

            int is_manual = checkResultSet.getInt(numDestCols + 1);
            if (is_manual == 1) {
                isEqual = true;
            }

            if (isEqual) {
                cfgTuner.addParameter("RECORD", "OLD");
            } else {
                cfgTuner.addParameter("RECORD", "UPDATED");
            }
        } else // в источнике обнаружена новая запись
        {
            cfgTuner.addParameter("RECORD", "NEW");
            oldData = "<td>-</td>";
            for (int i = 2; i <= numSrcCols; i++) {
                if (dest_fields_types[i - 1].equals("varchar")
                        || dest_fields_types[i - 1].equals("int")) {
                    fieldNew = r.getString(i);
                    if (fieldNew == null) {
                        fieldNew = "";
                    }
                    fieldNew = fieldNew.trim();
                    vData.addElement(fieldNew);
                    newData += "<td>" + fieldNew + "</td>";
                } else {
                    showErr(null, "!!!!! +++ ERROR: UNKNOWN FIELD TYPE: " + dest_fields_types[i - 1]);
                }
//                oldData += "<td>-</td>"; 
            }
            isEqual = false;
        }
//	  oldData += "<td></td>";
        if(idError){
            if(vData.elementAt(0).length() == 0)
                showErr(null, " ERROR: record ID is empty! ");            
            else
                showErr(null, " ERROR: record ID='" + vData.elementAt(0) + "'");            
        }
        
        cfgTuner.addParameter("oldData", oldData);
        cfgTuner.addParameter("newData", newData);
        return isEqual;
    }

    /**
     * Выводит клиенту "ручные" записи и записи, помеченные на удаление
     *
     * @param extraType - тип записей ("DELETED" или "MANUAL")
     * @throws Exception
     */
    private void showExtraRecords(String extraType) throws Exception {
        cfgTuner.addParameter("RECORD", extraType);
        cfgTuner.addParameter("newData", "");
        Connection conn = dbUtil.getConnection();
        String sql = "select " + cfgTuner.getParameter("DEST_FIELDS") + "is_manual from " + cfgTuner.getParameter("TABLE_NAME");
        if (extraType.equals("DELETED")) {
            sql += " where is_deleted=1 and is_manual=0";
        } else if (extraType.equals("MANUAL")) {
            sql += " where is_deleted=0 and is_manual=1";
        }
        IOUtil.writeLogLn("+++ " + extraType + " SQL: '" + sql + "'", rm);
        PreparedStatement delStmt = conn.prepareStatement(sql);
        ResultSet r = delStmt.executeQuery();
        while (r.next()) {
            String oldData = "";
            for (int i = 1; i <= numDestCols; i++) {
                switch (dest_fields_types[i - 1]) {
                    case "int":
                        oldData += "<td>" + r.getInt(i) + "</td>";
                        break;
                //        if(i<numSrcCols)
                //          newData += "<td>-</td>";
                    case "varchar":
                        oldData += "<td>" + r.getString(i) + "</td>";
                        break;
                    default:
                        System.out.println(" !!!!! UNKNOWN TYPE: " + dest_fields_types[i - 1]);
                        break;
                }
            }
            cfgTuner.addParameter("oldData", oldData);
            cfgTuner.outCustomSection("item", out);
        }
    }

       /**
     * Обработка ошибки - вывод сообщения в output, Log и в System.out
     * 
     * @param e
     * @param msg 
     */
private void showErr(Exception e, String msg) {
    
        String s = "";
            if(e != null) {
                e.printStackTrace(System.out);
                s = e.toString();
            }
        try {
            for (int i=0; i<vData.size(); i++) 
                    s += dest_fields_names[i] + "='" + vData.elementAt(i) + "', ";
            System.out.println(msg + ": " + s);
            cfgTuner.addParameter("IMPORT_ERROR", "<b>" + msg + "</b> " + s);
            cfgTuner.outCustomSection("err_msg", out);  // отображаем строку ошибки
            IOUtil.writeLogLn("<b>XXXXXXXX Exception: " + msg + "</b> " + s, rm);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
//        cfgTuner.addParameter("IMPORT_ERROR", "");
        cfgTuner.addParameter("ERROR", msg + ":" + s);
}
    
}

/**
 * Не используется
 *
 * @throws Exception
 *
 * private void showDeletedRecords() throws Exception { cfgTuner.addParameter(
 * "RECORD", "DELETED" ); cfgTuner.addParameter( "newData", "" ); Connection
 * conn = dbUtil.getConnection(); String sql = "select " +
 * cfgTuner.getParameter( "DEST_FIELDS" ) + "is_manual from " +
 * cfgTuner.getParameter( "TABLE_NAME" ) + " where is_deleted=1 and
 * is_manual=0"; IOUtil.writeLogLn( "+++ DELETED SQL: '" + sql + "'", rm );
 * PreparedStatement delStmt = conn.prepareStatement( sql ); ResultSet r =
 * delStmt.executeQuery(); while( r.next() ) { String oldData = ""; for( int i =
 * 1; i <= numDestCols; i++ ) { if( dest_fields_types[i - 1].equals( "int" ) )
 * oldData += "<td>" + r.getInt( i ) + "
 * </td>"; else if( dest_fields_types[i - 1].equals( "varchar" ) ) oldData +=
 * "<td>" + r.getString( i ) + "</td>"; else System.out.println( " !!!!! UNKNOWN
 * TYPE: " + dest_fields_types[i - 1] ); //	if(i<numSrcCols) // newData +=
 * "<td>-</td>"; } System.out.println( "+++ DELETED: oldData=" + oldData );
 * cfgTuner.addParameter( "oldData", oldData ); cfgTuner.outCustomSection(
 * "item", out ); } } /*
 */
    /**
     * НЕ ИСПОЛЬЗУЕТСЯ private void showManualRecords() throws Exception {
     * cfgTuner.addParameter( "RECORD", "MANUAL" ); cfgTuner.addParameter(
     * "newData", "" ); Connection conn = dbUtil.getConnection(); String sql =
     * "select " + cfgTuner.getParameter( "DEST_FIELDS" ) + "is_manual from " +
     * cfgTuner.getParameter( "TABLE_NAME" ) + " where is_manual=1"; //
     * is_deleted=0 and IOUtil.writeLogLn( "+++ MANUAL SQL: '" + sql + "'", rm
     * ); PreparedStatement manualStmt = conn.prepareStatement( sql ); ResultSet
     * r = manualStmt.executeQuery(); while( r.next() ) { String oldData = "";
     * for( int i = 1; i <= numDestCols; i++ ) { if( dest_fields_types[i -
     * 1].equals( "int" ) ) oldData += "<td>" + r.getInt( i ) + "
     * </td>"; else if( dest_fields_types[i - 1].equals( "varchar" ) ) oldData
     * += "<td>" + r.getString( i ) + "</td>"; else System.out.println( " !!!!!
     * UNKNOWN TYPE: " + dest_fields_types[i - 1] ); // if(i<numSrcCols) //
     * newData += "<td>-</td>"; } System.out.println( "+++ MANUAL: oldData=" +
     * oldData ); cfgTuner.addParameter( "oldData", oldData );
     * cfgTuner.outCustomSection( "item", out ); } } /*
     */
