/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsonsrv;

import org.json.simple.JSONObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;

public class Jsonsrv {

    public static Boolean DeviceIsTesting = true;
    public static int paramPORT = 0;
    public static int paramReceiveDelay = 0;
    public static String paramURL = "-";
//    public static String paramURL = "http://lt-a3.jinr.ru/gateway/a";
//    public static String paramURL = "http://127.0.0.1:81/";
//    public static String paramURL = "http://62.84.109.196:81/";
    public static int paramReqTry = 1;
    public static String paramClientId = "100";
    public static String paramObjectType = "100";
    public static String ConfigFile = "config.json";
    public static JSONObject testObject;
    public static Integer paramSize = 5000;

    public static void main(String[] args) {
        System.out.println("Start V Echo" + Calendar.getInstance().getTime().toString());
        String Str1, Str2, dAnswer = "";
        //Создание команды для устройства
        System.out.println("Started");
//        String ObjectStr = "{"
//                + "\"DOC_TYPE\":\"Договор подряда\""
//                + ",\"DOC_TYPE_ID\":\"6\""
//                + ",\"DOC_NUMBER\":\"37\""
//                + ",\"DOC_DATE\":\"20.04.2016 18:29:30\""
//                + ",\"TITLE\":\"ЛИТ, Абдель Салам Абдель Разик Мохамед Ельмахди Нагат\""
//                + ",\"MODIFIER\":\"Куняев Сергей Васильевич\""
//                + ",\"CREATED\":\"31.03.2016 18:29:30\""
//                + ",\"MODIFIED\":\"24.04.2016 21:21:07\""
//                + ",\"initiator_id\":\"9463\" ,\"initiator\":\"Абдельшакур Эль Саид Мохаммед Абу Эльазм\""
//                + ",\"division_id\":\"600000\" ,\"division\":\"ЛИТ\""
//                + ",\"contractor_id\":\"8678\" ,\"contractor\":\"Абдель Салам Абдель Разик Мохамед Ельмахди Нагат\""
//                + ",\"subject_of_contract\":\"Разработка скриптов для описания компонент библиотеки виртуального конструктора установки для проекта «Virtual Laboratory of spontaneous Fission»\""
//                + ",\"contract_begin_date\":\"01.04.2016\""
//                + ",\"contract_end_date\":\"30.04.2016\""
//                + ",\"avans\":\"1000.00\""
//                + ",\"up_to_advance\":\"30. 04.2016\""
//                + ",\"payment\":\"2000.00\""
//                + ",\"payment_total_sum\":\"2542.00\""
//                + ", \"currency\": \"руб. \""
//                + ",\"source_funding_id\":\"3305\" "
//                + ",\"source_funding\":\"ЛИТ, т.1118 (Кореньков)\""
//                + ",\"jinr_managenent_id\":\"2\" ,\"jinr_managenent\":\"директор Матвеев Виктор Анатольевич\""
//                + "}";
//
//        try {
//
//            JSONParser jParser = new JSONParser();
//            Object obj = jParser.parse(ObjectStr);
//            testObject = (JSONObject) obj;
//            for (int i = 1; i < 0; i++) {
//                Jsonsrv.testObject.put(i, "1234567890");
//            }
//
//        } catch (ParseException ex) {
//            System.out.println("Error process test jsons object:");
//            ex.printStackTrace();
//        }
//                System.out.println("test object length:"+ testObject.toString().length());

        //Запуск потока устройства
        try {

            for (int i = 0; i < args.length; i++) {
                String str1 = args[i].toLowerCase();
//                System.out.println("param[" + String.valueOf(i) + "]=" + str1);
                if (str1.equals("-config")) {
                    ConfigFile = args[++i];
                    System.out.print("param: " + args[i - 1]);
                    System.out.println(" value:" + args[i]);
                }
                if (str1.equals("-port")) {
                    paramPORT = Integer.valueOf(args[++i]);
                    System.out.print("param: " + args[i - 1]);
                    System.out.println(" value:" + args[i]);
                }
                if (str1.equals("-delay")) {
                    paramReceiveDelay = Integer.valueOf(args[++i]);
                    System.out.print("param: " + args[i - 1]);
                    System.out.println(" value:" + args[i]);
                }
                if (str1.equals("-?")) {
                    System.out.println("Usage java -jar ReceiverFromgate [-size <object size in bytes>]");
                    System.out.println("                                 [-port <port number for http server>]");
                    System.out.println("                                 [-delay <receive delay in milisecund>]");
                    System.out.println("                                 [-url <gateway url with port (http://uuu.xxx.com:81)>]");
                    System.out.println("                                 [-count <send request count>]");
                    System.out.println("                                 [-ObjectType <ObjectType>]");
                    System.out.println("                                 [-ClientId <ClientId>]");
                    return;
                }

                if (str1.equals("-clientid")) {
                    paramClientId = args[++i];
                    System.out.print("param: " + args[i - 1]);
                    System.out.println(" value:" + args[i]);
                }
                if (str1.equals("-objecttype")) {
                    paramObjectType = args[++i];
                    System.out.print("param: " + args[i - 1]);
                    System.out.println(" value:" + args[i]);
                }
                if (str1.equals("-count")) {
                    paramReqTry = Integer.valueOf(args[++i]);
                    System.out.print("param: " + args[i - 1]);
                    System.out.println(" value:" + args[i]);
                }
                if (str1.equals("-size")) {
                    paramSize = Integer.valueOf(args[++i]);
                    System.out.print("param: " + args[i - 1]);
                    System.out.println(" value:" + args[i]);
                }

                if (str1.equals("-url")) {
                    paramURL = args[++i];
                    System.out.print("param: " + args[i - 1]);
                    System.out.println(" value:" + args[i]);
                }
            }
        } catch (Exception e) {
            System.out.println("Error process arguments:");
            e.printStackTrace();
        }
        try {
            testObject = new JSONObject();
            for (int i = 1; testObject.toString().length() < paramSize; i++) {
                Jsonsrv.testObject.put(i, "123456789012345678911234567890123456789212345678901234567893123456789412345678951234567896123456789712345678981234567890");
//                Jsonsrv.testObject.put(i, "12345678901234567890");
            }

        } catch (Exception ex) {
            System.out.println("Error process fill test jsons object:");
            ex.printStackTrace();
        }
        System.out.println("test object length:" + testObject.toString().length());

        ProcServer SRV = new ProcServer();

        while (DeviceIsTesting) {
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Jsonsrv.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {

            Thread.currentThread().sleep(1000);
        } catch (Throwable e) {
            System.out.println("Except !!!" + e.getMessage());
        }

        System.out.println(
                "Finished");
    }
}
