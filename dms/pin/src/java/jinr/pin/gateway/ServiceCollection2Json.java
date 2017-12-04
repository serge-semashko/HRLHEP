package jinr.pin.gateway;

/**
 *
 * @author serg
 */
public class ServiceCollection2Json extends dubna.walt.service.Service{
    
//String collectionJson;
       /**
     * the standard entry point of the service
     *
     * @exception java.lang.Exception
     * @see dubna.walt.service.Service
     */
    @Override
    public void start() throws Exception {
        cfgTuner.getCustomSection("process collection");
//        String collection_json = "[" + cfgTuner.getParameter("collection_json") + "]";
//        cfgTuner.addParameter("collection_json",  collection_json ); 
////        String encoded_data = URLEncoder.encode(collection_json, "utf-8");
//        cfgTuner.addParameter("encodedCollectionData", URLEncoder.encode(collection_json, "utf-8") ); 
//

        /* Output the [report] section (for debug only) */
        cfgTuner.outCustomSection("report", out);
    }

    

}
