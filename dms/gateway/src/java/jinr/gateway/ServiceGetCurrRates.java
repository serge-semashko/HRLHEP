package jinr.gateway;

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
            IOUtil.writeLog(1, "<hr> Загрузка курсов валют из uri=" + uri + "... ", rm);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(uri);
            // Выполнять нормализацию не обязательно, но рекомендуется
            doc.getDocumentElement().normalize();
            IOUtil.writeLogLn(2, "Получен XML. Корневой элемент: " + doc.getDocumentElement().getNodeName(), rm);

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
                    IOUtil.writeLogLn(2, "***** <b>КУРС " + charCode + "(" + NumCode + ") =" + rate + " за " + nominal + "</b>", rm);
                    cfgTuner.outCustomSection("item", out);
                }
            }
}

        } catch (Exception e) {
            e.printStackTrace();
            IOUtil.writeLogLn(0, "ERROR:" + e.toString() + "<hr>", rm);
            cfgTuner.addParameter("ERROR", e.toString());
        } finally {
            cfgTuner.outCustomSection("report footer", out);
            if(out != null)
                out.flush();
        }
    }
}
