package jinr.sed;

import dubna.walt.util.IOUtil;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author serg
 */
public class ServiceGetCurrRates extends dubna.walt.service.Service {

    private String currCodes;

    /**
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        try {
            currCodes = cfgTuner.getParameter("currCodes");
            cfgTuner.outCustomSection("report header", out);
if(cfgTuner.enabledOption("DATA_MISSING=Y")) {
            // Строим объектную модель исходного XML 
            String uri = cfgTuner.getParameter("uri");;
            IOUtil.writeLog("<hr> Загрузка курсов валют из uri=" + uri + "... ", rm);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(uri);
            // Выполнять нормализацию не обязательно, но рекомендуется
            doc.getDocumentElement().normalize();
            IOUtil.writeLogLn("Получен XML. Корневой элемент: " + doc.getDocumentElement().getNodeName(), rm);

            // Получаем все узлы с именем "Valute"
            NodeList nodeList = doc.getElementsByTagName("Valute");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                // Проходим по элементам и отбираем USD и EUR
                Element element = (Element) node;
                String NumCode = element.getElementsByTagName("NumCode").item(0).getTextContent().trim();
                if (currCodes.contains("," + NumCode + ",")) {
                    String charCode = element.getElementsByTagName("CharCode").item(0).getTextContent();
                    String nominal = element.getElementsByTagName("Nominal").item(0).getTextContent();
                    String rate = element.getElementsByTagName("Value").item(0).getTextContent().replace(",", ".");
                    cfgTuner.addParameter("code", NumCode);
                    cfgTuner.addParameter("charCode", charCode);
                    cfgTuner.addParameter("nominal", nominal);
                    cfgTuner.addParameter("rate", rate);
                    IOUtil.writeLogLn("***** <b>КУРС " + charCode + "(" + NumCode + ") =" + rate + " за " + nominal + "</b>", rm);
                    getData("put record");
                }
            }
}

        } catch (Exception e) {
            e.printStackTrace();
            IOUtil.writeLogLn("ERROR:" + e.toString() + "<hr>", rm);
            cfgTuner.addParameter("ERROR", e.toString());
        } finally {
            cfgTuner.outCustomSection("report footer", out);
            out.flush();
        }
    }
}

/*
http://j4web.ru/java-xml/chtenie-xml-fajla-v-java-sredstvami-dom-xml.html


Пример итерирования узлов XML документа
Второй пример программы выполняет следующие действия: 
загружает XML файл staff.xml в память, 
перебирает все узлы этого документа 
и печатает имя и значение этого узла, а если есть — все атрибуты узла.

package ru.j4web.examples.java.xml;
 
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
 
public class ReadXMLFileDOMExample2 {
 
    private static final String FILENAME = "staff.xml";
 
    public static void main(String[] args) {
        try {
            // Строим объектную модель исходного XML файла
            final File xmlFile = new File(System.getProperty("user.dir")
                    + File.separator + FILENAME);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(xmlFile);
 
            // Выполнять нормализацию не обязательно, но рекомендуется
            doc.getDocumentElement().normalize();
 
            if (doc.hasChildNodes()) {
                printNodes(doc.getChildNodes());
            }
        } catch (ParserConfigurationException | SAXException
                | IOException ex) {
            Logger.getLogger(ReadXMLFileDOMExample2.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
 
    private static void printNodes(NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (Node.ELEMENT_NODE == node.getNodeType()) {
 
                // Печатаем имя ноды и значение
                System.out.println();
                System.out.println("Имя ноды: " + node.getNodeName());
                System.out.println("Значение ноды: "
                        + node.getTextContent());
 
                if (node.hasAttributes()) {
                    // Есть атрибуты: печатаем и их
                    NamedNodeMap attributes = node.getAttributes();
                    for (int j = 0; j < attributes.getLength(); j++) {
                        Node attribute = attributes.item(j);
                        System.out.println("Имя атрибута: "
                                + attribute.getNodeName());
                        System.out.println("Значение атрибута: "
                                + attribute.getNodeValue());
                    }
                }
                 
                if (node.hasChildNodes()) {
                    // Есть дочерние ноды: печатаем их
                    printNodes(node.getChildNodes());
                }
            }
        }
    }
}


public void ReadXMLFileDOM() {
 
        try {
 
            // Строим объектную модель исходного XML файла
//            final File xmlFile = new File("/home/serg/DAT/xml.xml");
            String uri = cfgTuner.getParameter("uri");;
            System.out.println("uri=" + uri);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
//            Document doc = db.parse(xmlFile);
            Document doc = db.parse(uri);
 
            // Выполнять нормализацию не обязательно, но рекомендуется
            doc.getDocumentElement().normalize();
 
            System.out.println("Корневой элемент: " + doc.getDocumentElement().getNodeName());
            System.out.println("============================");
 
            // Получаем все узлы с именем "Valute"
            NodeList nodeList = doc.getElementsByTagName("Valute");
 
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                // Выводим информацию по каждому из найденных элементов
                Element element = (Element) node;
                String NumCode = element.getElementsByTagName("NumCode").item(0).getTextContent().trim();
//                System.out.println("NumCode= '" + NumCode + "'");
                if(currCodes.contains("," + NumCode + ",")) {
                System.out.println();
                cfgTuner.addParameter("code", NumCode);
                cfgTuner.addParameter("rate", element.getElementsByTagName("Value").item(0).getTextContent().replace(",",".") );
                getData("put record");
                System.out.println("Текущий элемент: " + node.getNodeName());
                if (Node.ELEMENT_NODE == node.getNodeType()) {
                    System.out.println("ID: "
                            + element.getAttribute("ID"));
                    System.out.println("NumCode: " + element
                            .getElementsByTagName("NumCode").item(0)
                            .getTextContent());
                    System.out.println("CharCode: " + element
                            .getElementsByTagName("CharCode").item(0)
                            .getTextContent());
                    System.out.println("Nominal: " + element
                            .getElementsByTagName("Nominal").item(0)
                            .getTextContent());
                    System.out.println("Value: " + element
                            .getElementsByTagName("Value").item(0)
                            .getTextContent());
                }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }  
    


 */
