/*
 * @file       ProcessJSONRequest.java
 * @author     Сергей Семашко
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsonsrv;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
/** @brief Класс обработки запросов web-приложений. 
  * 
  * Пока работа идёт следующим образом: Команда (в формате JSON)
  * считывается со стандартного ввода и туда же пишется ответ (в формате JSON)
  * на эту команду, сформированный соответствующим методом данного
  * класса.
*/
public class ProcessJSONRequest {
   String PictureDir= "Shoots";
   String OrdersDir = "";
   Object obj;
   ArrayList  OrderList;
   JSONObject jCmd ;
   JSONObject Config;
   JSONObject jAnsw ;
   JSONParser jParser;     
   JSONArray  jArray;     
   String ActiveID;
   int OrderCount;
   JSONArray Orders;
   String  ActiveExperimentID = null;
     public ProcessJSONRequest() throws IOException {
        Config = new JSONObject();
        PictureDir = "." ;  
        jCmd = new JSONObject();
        jAnsw = new JSONObject();
        jParser = new JSONParser();     
//        System.out.println("Load config file");
        try{
            Object obj = jParser.parse(new FileReader(Jsonsrv.ConfigFile));
            Config = (JSONObject) obj;      
//            System.out.println("Config "+Config.toString());
            PictureDir = (String) Config.get("PictureDir");
            OrdersDir = (String) Config.get("OrdersDir");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error config file");
        }   
    
      System.out.println("JSON processing init.");
      
     
   }
   /**
    * @brief Сохраняет список Orders в кодировке UTF-8 
    * 
    * 
   * @bug . Потытка записать и прочитать адекватно текстовый файл в UTF-8 пока не удалась
    * 
    * 
   */
     public void SaveOrders(){
        try {
            String OutSTR = Orders.toJSONString();
            FileWriter file = new FileWriter(OrdersDir+"Orders.json");
            file.write(OutSTR);
            file.flush();
            file.close();

        } catch (IOException e) {
                e.printStackTrace();
        }       
         
     }
     /**
    * @brief Добавляет/обновляет  Order
    * 
    * 
   *
    * 
    * 
   */
     public void UpdateInsertOrder(String OrderDetail){
         
     }

   

   /**
    * @brief Обрабатывает запрос s_cmd в формате 
    * JSON, вызывает соответствующий ему метод и возворащает ответ в
    * формате JSON, сформированный этим методом.
    *
    * @param s_cmd Запрос в формате JSON, приведённый к строке.
    * Допускаются переводы строк.
    *
    * @return Ответ в формате JSON, приведённый к строке. Допускаются
    * переводы строк.
   */
     public String ProcessCommand(String s_cmd){
    
            try {
                jAnsw.clear();
                Object obj = jParser.parse(s_cmd);
                jCmd = (JSONObject) obj;
                String cmd = (String) jCmd.get("Command");
                ActiveID = (String) jCmd.get("OrderID");
                System.out.println(cmd);
                cmd = cmd.toLowerCase();
                if (cmd.equals("getorderlist")) GetOrderList();
                else if (cmd.equals("createorder")) CreateOrder();
                else if (cmd.equals("saveorder")) SaveOrder();
                else if (cmd.equals("deleteorder")) DeleteOrder();
                else if (cmd.equals("getconfigpars")) GetConfigPars();
                else if (cmd.equals("setconfigpars")) SetConfigPars();

/*                switch (cmd) {
                    case "getorderlist":
                        GetOrderList();
                        break;
                    case "createorder" :
                        CreateOrder();
                        break;
                    case "saveorder" :
                        SaveOrder();
                        break;
                    case "deleteorder" :
                        DeleteOrder();
                        break;
                    case "getconfigpars" :
                        GetConfigPars();
                        break;
                    case "setconfigpars" :
                        SetConfigPars();
                        break;
                }
                               /**/
                    

            } catch (ParseException e) {
                e.printStackTrace();
                jAnsw.put("ResultCode", new Integer(-2));
                jAnsw.put("Message", "Illegal format");
                    
            }            
 return (jAnsw.toString());   
}
  /**
   * @brief Получить список  объеков
   *
   *  Обрабатывает запрос в формате:
   *  {"Command":"GetOrderList"}
   *
   * @return Ответ в формате:
   *  {"Resultcode":0,"OrderList":массив структур OrderList}
   *
   * 
   */
  void     GetOrderList() {
    jAnsw.put("ResultCode", new Integer(0));
    jAnsw.put("Message", "ok");
    jAnsw.put("OrderList",Orders);
  }
/**
   * @brief Переименовать названия полей в глобальные из локальных для данного клиента 
   *
   * 
   */
  
      void TranslateToGlobal(JSONObject NewOrder){
         for ( int i = 0; i < NewOrder.size(); i++) {
         }
      }

/**
   * @brief Сохранить информацию о заказе на проведение объекта
   *
   *  Обрабатывает запрос в формате:
   *  {"Command":"SavetOrder","Orderdetail":структура Orderdetail}
   *
   * @return Ответ в формате:
   *  {"Resultcode":0,"Message":"OK"} 
   * 
   */
  void     SaveOrder() {
  }
/**
   * @brief Удалить заказ с соответствующим номером заявки
   *
   *  Обрабатывает запрос в формате:
   *  {"Command":"DeleteOrder","OrderID":ID}
   *
   *  Здесь ID - это целое число номера заказа на проведение
   *  объекта (например, ID=1).
   * 
   * @return Ответ в формате:
   *  {"Resultcode":0,"Message":"OK"} 
   * 
   */
  void     DeleteOrder() {
    String OrderID;
    JSONObject Order = new JSONObject();
    if (ActiveID == null ) {
        jAnsw.put("Message", "Bad OrderID");
        jAnsw.put("ResultCode", new Integer(-3));
        return;
    }
    for (int i = 1; i <Orders.size()-1; i++) {
        Order= (JSONObject) Orders.get(i);
        OrderID = (String) Order.get("OrderID");
        if (OrderID.equals(ActiveID)) {
            Orders.remove(i);
            jAnsw.put("Message", "ok");
            jAnsw.put("ResultCode", new Integer(0));
           return;
        }
    }
    SaveOrders();
    jAnsw.put("Message", "ok");
    
    jAnsw.put("Message", "Order Not Exist");
    jAnsw.put("ResultCode", new Integer(-3));
  }

/**
   * @brief Создать объект 
   *    }
   *  }
   *
   * @return Ответ в формате:
   *  {"Resultcode":0,"Message":"OK","OrderID":1 } Получен ответ создан заказ 1 
   * 
   */
  void     CreateOrder() {
  }

  /**
    * @brief Отдаёт информацию по соответствующему объекту.
    *
    * Обрабатывает запрос в формате:
    * {"Command":"GetOrder","EID":"Идентификатор объекта"}
    *
    * Идентификтор (int) -- номер, присвоенный заявке на объект
    *
    * @return Ответ в формате:
    * Паспорт документа возвращается. Если объект ещё не начат, то
    * возвращается одно. Если уже начался и не кончился, то другое. Если
    * завершён, то третье.
    * 
    */
  void GetOrderInfo()
  {
  }

/**
   * @brief получить структуру ConfigPars 
   *
   *  Обрабатывает запрос в формате:
   *  {"Command":"getConfigPars"}
   * Сруктура ConfigPars
   *    {
   *      "OrdersDir":"папка, где лежит файл Orders и хранятся папки Заказов",    //Папка заказа совпадает по имени с OrderID В папке лежат результаты объекта
   *      "BBBURL":"собственно он сам и есть"
   *    }
   *
   * @return Ответ в формате:
   *  {"Resultcode":0,"Message":"OK","ConfigPars": структура ConfigPars } 
   *
   * 
   */
  void     GetConfigPars() {
    jAnsw.put("ResultCode", new Integer(0));
    jAnsw.put("Message", "ok");
    jAnsw.put("ConfigPars", Config);
    
  }
/**
   * @brief установить  структуру ConfigPars 
   *
   *  Обрабатывает запрос в формате:
   *  {"Command":"SetConfigPars","ConfigPars": структура ConfigPars}
   *
   * @return Ответ в формате:
   *  {"Resultcode":0,"Message":"OK" } 
   *
   * 
   */
  void     SetConfigPars() {
    jAnsw.put("ResultCode", new Integer(0));
    jAnsw.put("Message", "ok");
    Config =(JSONObject)  jCmd.get("ConfigPars");
    try {
            FileWriter file = new FileWriter("Config.json");
            file.write(Config.toJSONString());
            file.flush();
            file.close();

    } catch (IOException e) {
            e.printStackTrace();
    }    
  }

 
   /**
   * @brief Функция обработки входящих команд.
   *
   *  В бесконечном цикле обрабатывает запросы в формате JSON и если в запросе указана
   *  поддерживаемая функция, то запрос пробрасывается в неё
   *  (вызывается эта функция и запрос передаётся ей на обработку)
   *
   */
   
    
}
