package cern.kpi.viewdb;

//import dubna.walt.util.*;
//import java.sql.*;

public class HackerHunter extends dubna.walt.service.Service
{

public void beforeStart() throws Exception
{ 
  System.out.println("!!!!!!!!!!!!!!!!!!!! HackerHunter !!!!!!!!!!!!!!!!!!!!");
  cfgTuner.deleteParameter("debug");
}
public void start() throws Exception
{ cfgTuner.addParameter("AUTH_ERROR", "NO ACCESS");
  cfgTuner.getCustomSection("headers.dat", "Header_HTML", out);
//  out.println("Na xui!");
}

}