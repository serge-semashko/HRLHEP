package cern.kpi.viewdb;

public class RecompileService extends UserConnectionService
{

public void beforeStart() throws Exception
{ 
  super.beforeStart();
  
  if ( dbUtil != null)
  { //===== create procedure "recompile_all_objects"
    String[] sa = cfgTuner.getCustomSection("create proc");
    String sqlProc = "";
    for (int i=0; i < sa.length; i++)
      sqlProc += sa[i] + "\n";     
    executeProcedure(sqlProc, dbUtil, rm);

    //===== execute procedure "recompile_all_objects" and drop it
    sa = cfgTuner.getCustomSection("exec proc");
    for (int i=0; i < sa.length; i++)
      executeProcedure(sa[i], dbUtil, rm);    
  }
}

}