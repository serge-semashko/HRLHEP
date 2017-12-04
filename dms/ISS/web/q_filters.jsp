<%@ page contentType="text/html;charset=utf-8"%>
<%@page import="java.util.*, myiss.*, java.net.*"%>
<jsp:useBean id="JspData" class="java.util.HashMap" scope="request"/>
<%
HashMap rParams = new HashMap();

if(JspData.get("RPARAMS")!=null) rParams=(HashMap)JspData.get("RPARAMS");
String rparamAction = "";
rparamAction = Tools.getStringValue(rParams.get("Action"),"","dd.MM.yyyy");
String rparamTname = "";
rparamTname = Tools.getStringValue(rParams.get("tname"),"","dd.MM.yyyy");
String rparamQname = "";
rparamQname = Tools.getStringValue(rParams.get("qname"),"","dd.MM.yyyy");
ArrayList<String> colNames = (ArrayList<String>)JspData.get("COLUMN_NAMES");
%>
<input type="hidden" name = "Action" value = "<%=rparamAction%>"/>
<input type="hidden" name = "tname" value = "<%=rparamTname%>"/>
<input type="hidden" name = "qname" value = "<%=rparamQname%>"/>
<% 



List<HashMap> filters = new ArrayList<HashMap>();
HashMap filter_data = new HashMap();
if (JspData.get("FILTERS")!=null) filters = (List<HashMap>)JspData.get("FILTERS");
if (JspData.get("FILTER_DATA")!=null) filter_data = (HashMap)JspData.get("FILTER_DATA");

%>



<div id = "filt_cont">
<table id="options">
<tr>
<td id = "bk_filters" colspan=2>

    <table id = "q_filters">
        <tr>
            <th colspan="2">Параметры
            </th>
        </tr>
<%
    for (int i = 0; i < filters.size(); i++)  {
    %>
        <tr>
    <%
        HashMap hm = filters.get(i);
        String fNameParam= Tools.getStringValue(hm.get("NameParam"),"");
        String fType= Tools.getStringValue(hm.get("Type"),"");
        %>
            <td class="name">
            <%=fNameParam%>
            </td>
        
        <!--td>
            <%=fType%>
        </td-->
        <%
        if(fNameParam.length()>0 && filter_data.get(fNameParam)!=null){
            List<HashMap> fdata = (List<HashMap>)filter_data.get(fNameParam);
            String selectedCode = Tools.getStringValue(rParams.get(fNameParam),"","dd.MM.yyyy");
            %>
            <td class="param">
            <%
            if((fdata.size()>3)){
            %>
            
            <SELECT name = "<%=fNameParam%>" id = "<%=fNameParam%>" class="chosen">
            <option value="*">Все</option>
            <%
            
            for(HashMap option : fdata){
                String optionName = Tools.getStringValue(option.get("NAME"),"","dd.MM.yyyy");
                String optionCode = Tools.getStringValue(option.get("CODE"),"","dd.MM.yyyy");
                if(optionCode.equals("")&&optionName.equals("")) continue;
                %>
                <option value="<%=optionCode.replaceAll("(?si)\"","&quot;")%>"><%=optionName%></option>
                <%
            }
            %>
            </select>
            <%}else{
            %>
            <label><input name = "<%=fNameParam%>" type="radio" checked="checked" value = "*">Все</label>
            <%
              for(HashMap option : fdata){
                String optionName = Tools.getStringValue(option.get("NAME"),"","dd.MM.yyyy");
                String optionCode = Tools.getStringValue(option.get("CODE"),"","dd.MM.yyyy");%>
            <label><input name = "<%=fNameParam%>" type="radio" value = "<%=optionCode%>"/><%=optionName%></label>
            <%}
            }%>
            <SCRIPT type="text/javascript">
            //$("#<%=fNameParam%>").val("<%=selectedCode.replaceAll("(?si)\"","&quot;")%>");
            quickSelect("<%=fNameParam%>",'<%=selectedCode.replaceAll("(?si)\"","&quot;")%>');
            </SCRIPT>
            </td>
            <%
        }else{%>
            <td>
            <%
            String fValueParam = Tools.getStringValue(rParams.get(fNameParam),"*","dd.MM.yyyy");
            %>
            <input type = "text" defaultValue="*" placeholder="*" name = "<%=fNameParam%>" value = "<%=fValueParam%>"/>
            </td>
        <%}
    %>
    </tr>
    <%
    }

%>
</table>
</td>
</tr>
<tr>
<td id="bk_cols" colspan=2>
<%
if(colNames!=null && colNames.size()>0){
    boolean checkAll = true;
    for (int i = 0; i<colNames.size(); i++){
        if (request.getParameter("r"+i)!=null)
        checkAll = false;
    }
%>



<table id= "q_cols">
<%int nrows = 3;%>
<tr>
<th colspan="<%=nrows%>">
Отображать столбцы
</th>
</tr>
<tr>

<%
    for (int i = 0; i<colNames.size(); i++){
        String label = colNames.get(i);
        String checked = (request.getParameter("r"+i)!=null||checkAll)?"checked=\"checked\"":"";
        int cellColspan = (i==colNames.size()-1)?(nrows-(i%nrows)):1;
%>
<td colspan="<%=cellColspan%>" >
  <input type="checkbox" name = "r<%=i%>" id = "r<%=i%>" <%=checked%>/> 
  <label for="r<%=i%>"><%=label%></label>
</td>
     <%   
        if((i+1)%nrows==0 && i!=colNames.size()-1){
        %>
        </tr><tr>
        <%
        } 
    }
%>
</tr>
</table>



<%--
<table id= "q_cols">
<%int nrows = 3;%>
<tr>
<th colspan="<%=(colNames.size()/nrows)+(colNames.size()%nrows>0?1:0)%>">
Отображать столбцы
</th>
</tr>
<tr>
<td>
<%
    for (int i = 0; i<colNames.size(); i++){
        String label = colNames.get(i);
        String checked = (request.getParameter("r"+i)!=null||checkAll)?"checked=\"checked\"":"";
%>
     
   <label><input type="checkbox" name = "r<%=i%>" <%=checked%>/><%=label%></label><br/>
     <%   
        if((i+1)%nrows==0 && i!=colNames.size()-1){
        %>
        </td><td>
        <%} 

    }
%>
</td>
</tr>
</table>
--%>
<%
}
%>
</td>
</tr>


<tr>
<td id="bk_sort">
 
    <table id = "q_sort">
        <tr>
            <th>
                Сортировка 
            </th>
        </tr>
        <tr>
            <td>
                <%
                String AZchecked = (JspData.get("TABLE_SORT")==null)?"checked=\"checked\"":"";
                String ZAchecked = (JspData.get("TABLE_SORT")!=null)?"checked=\"checked\"":"";
                String table_sortby = Tools.getStringValue(JspData.get("TABLE_SORTBY"),"");
                %>
                <%
                if(colNames!=null && colNames.size()>0){
                %>
                <!--label for="table_sortby">Столбец</label-->
                <span class="label4chzn">Столбец</span>
                    <SELECT name = "table_sortby" id = "table_sortby" class="chosen-nosearch">
                        <option value ="">по умолчанию</option>
                        <%for (String colName:colNames){%>
                        <option value = "<%=colName%>"><%=colName%></option>
                        <%}%>
                    </SELECT>
                    <SCRIPT type="text/javascript">
                        quickSelect("table_sortby","<%=table_sortby%>");
                    </SCRIPT>
                <%
                }
                %>
                <input type="radio" name = "table_sort" value="" <%=AZchecked%> id="ts_a"/> <label for="ts_a">А-я</label> <input type="radio" id="ts_d" name = "table_sort" value="DESC" <%=ZAchecked%>/> <label for="ts_d">Я-а</label>

            </td>
        </tr>
    </table>

</td>
<td id="bk_amount">
<%
String itemsOnPage = Tools.getStringValue(JspData.get("itemsOnPage"),"10","");
%>
    <table id="q_amount">
        <tr>

            <th >
                Показывать
            </th>
        </tr>
        <tr>
            <td>

            <span class="label4chzn">Cтрок на странице</span>
                <select name = "itemsOnPage" id = "itemsOnPage" class="chosen-nosearch">
                    <option value="10">10&nbsp;&nbsp;&nbsp;</option>
                    <option value="20">20&nbsp;&nbsp;&nbsp;</option>
                    <option value="50">50&nbsp;&nbsp;&nbsp;</option>
                    <option value="100">100&nbsp;&nbsp;&nbsp;</option>
                    <option value="<%=Integer.MAX_VALUE%>">Все</option>
                </select>
               
                <SCRIPT type="text/javascript">
                        quickSelect("itemsOnPage","<%=itemsOnPage%>");
                </SCRIPT>
            </td>
        </tr>
    </table>
</td>
</tr>
</table>








<div id = "execquery">
<button type="submit" id="execute"><span id = "exe"><img src="img/exe.gif"><div>Выполнить</div></span><span id = "busy" style="display:none;"><img src="img/busy.gif"><div>Подождите</div></span></button>
</div>
</div>