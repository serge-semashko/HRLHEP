<%@ page contentType="text/html;charset=utf-8"%>
<%@page import="java.util.*, myiss.*, java.net.*, java.text.*"%>
<jsp:useBean id="JspData" class="java.util.HashMap" scope="request"/>
<div class = "queryresulttable">
<%

int uId = Tools.parseInt(JspData.get("UID"),-1);
int debugger_user_id = myiss.mainServlet.debuggerUserId;
boolean debugger = (uId>0&&debugger_user_id>0&&uId==debugger_user_id);

if(JspData.get("QUERY_RESULT")!=null&&((List<HashMap>)JspData.get("QUERY_RESULT")).size()>0){
List<HashMap> queryResult = (List<HashMap>)JspData.get("QUERY_RESULT");


HashMap links = (HashMap)JspData.get("LINKS");
HashMap linkParams = (HashMap)JspData.get("LINK_PARAMS");
ArrayList<String> tabHeaders = (ArrayList<String>)JspData.get("TABLE_HEADERS");
boolean showAllCols = true;

if(tabHeaders!=null && tabHeaders.size()>0){
    for (int ii = 0 ; ii < tabHeaders.size(); ii++){
        if (request.getParameter("r"+ii)!=null){
            showAllCols = false;
        }
    }
}

String queryTitle = Tools.getStringValue(JspData.get("QUERY_TITLE"),"");
String baseUrl = Tools.getStringValue(JspData.get("SERVLET_BASE_URL"),"");
String queryString = Tools.getStringValue(JspData.get("SERVLET_QUERY_STRING"),"");

int Page = Tools.parseInt(JspData.get("Page"),1);
int PageCount = Tools.parseInt(JspData.get("PageCount"),1);
%>
<!--h1><%=queryTitle%></h1-->
<div id="excbutt">
<a href = "?<%=queryString%>&downloadXLS=y" target = "_blank"><img  title="Загрузить в Excel" class="tiptip" src="img/icons/excel.jpg" alt="Загрузить в Excel"/></a>
<%if(JspData.get("HAS_GRAPHIC")!=null){%>
<a class="pirobox" rel="single" href="?<%=queryString%>&downloadImg=y.png" title = "<a  href='?<%=queryString%>&downloadImg=y.png' id='full_image' target = '_blank'>В новом окне</a>"><img title="График" class='tiptip' alt='График' src = "img/icons/chart.png"/></a>
<%}%>
</div>

<%if (PageCount>1){%>
<br/>

Страницы:
<ul class="paginator">
<%for (int i = 1 ; i <= PageCount; i++){
%>
<li>
<%if(Page!=i){%>
<a href = "#" onclick="setPage(<%=i%>)"><%=i%></a>
<%}else{%>
<span class="currpagenum"><strong><%=i%></strong></span>
<%}%>
</li>
<%
}%>
</ul>
<%
}
%>

<div id="scroll-pane">
<!--div id="scroll-pane"-->
<table  cellpadding="0" cellspacing="0" id = "qtable">
<thead>
<tr id = "tab-header-row">
<%


for (int i = 0 ; i < tabHeaders.size(); i++){
    boolean showCol = (request.getParameter("r"+i)!=null || showAllCols)? true : false;
    if(showCol){
    %>
        <th><%=tabHeaders.get(i)%></th>
    <%
    }
}
%>
</tr>
</thead>
<tbody>
<%
for (int i = 0 ; i<queryResult.size(); i++){
String trclass=((i+1)%2==0)?"even":"odd";
HashMap resRow = (HashMap)queryResult.get(i);
%><tr class="<%=trclass%>" onmouseover="highlightRow(this);" onmouseout="shadeRow(this);" onclick="markRow(this);" ondblclick="showRow(this);">
<%

for (int ii = 0 ; ii < tabHeaders.size(); ii++){
    boolean showCol = (request.getParameter("r"+ii)!=null || showAllCols)? true : false;
    String key = tabHeaders.get(ii);
    String claass="";
    if(resRow.get(key)!=null)
        claass=resRow.get(key).getClass().getName();
        else
        claass="null";
    claass=claass.substring(claass.lastIndexOf(".")+1);
    String value = Tools.getStringValue(resRow.get(key),"","dd.MM.yyyy");
    if("Long".equals(claass)||"Double".equals(claass)||"Float".equals(claass)){
    DecimalFormat longFormatter = new DecimalFormat("###,##0.00");
    value = longFormatter.format(resRow.get(key));
    }
    if(showCol == false) continue;
    if(links.get(key)==null){
    %>
        <td class="<%=claass%>"><%=value%></td>
    <%
        }else{
        String tname = Tools.getStringValue(((HashMap)links.get(key)).get("TName"),"");
        String qname = Tools.getStringValue(((HashMap)links.get(key)).get("QName"),"");
        String keyVal = Tools.getStringValue(resRow.get(key),"");
        String linkHref = "";
        if(tname.length()>0 && qname.length()>0){
        if (linkParams.get(key)!=null){
            List<String> hrefParamList = (List<String>)linkParams.get(key);
            for (String paramName : hrefParamList){
                String paramValue = Tools.getStringValue(resRow.get(paramName),Tools.getStringValue(request.getParameter(paramName),"","dd.MM.yyyy"),"dd.MM.yyyy");
                linkHref+="&"+paramName+"="+paramValue;
            }
        }
//key=URLEncoder.encode(key);
%>
<td class="<%=claass%>">

<a href = "?Action=query&Page=1&tname=<%=tname%>&qname=<%=qname%><%=linkHref%>"><%=value%></a>
<%--a href = "?Action=query&Page=1&tname=<%=tname%>&qname=<%=qname%>&<%=key%>=<%=keyVal%>"><%=value%></a--%>
</td>
<%
}else{
%>
<td>
<%
if(
    value.toLowerCase().startsWith("http")
    ||value.toLowerCase().startsWith("ftp")
){
%>

<a target="_blank" href = "<%=value%>" title="Перейти по адресу:
<%=value%>"><img alt="<%=value%>" src = "img/hyperlink.png"/></a>
<%
}else{
%>
    <%=value%>
<%
}
%>
<%--a href = "?Action=query&Page=1&tname=<%=tname%>&qname=<%=qname%>&<%=key%>=<%=keyVal%>"><%=value%></a--%>
</td>
<%

}
}
}
%>
</tr>
<%
}
if(JspData.get("TOTALS")!=null && Page==PageCount){
    List<HashMap> totals = (List<HashMap>)JspData.get("TOTALS");
    if(totals.size()>0){
        for (int k = 0; k < totals.size(); k++ ){
        HashMap row = totals.get(k);
        Set set = row.keySet();     
        %>
        <tr>
            <%         
            Iterator iter = set.iterator();
                
                while (iter.hasNext())
                {
                    Object o = iter.next();
                    String key = o.toString();
                    String claass="";
                    if(row.get(key)!=null)
                    claass = row.get(key).getClass().getName();
                    else
                    claass="null";
                    claass=claass.substring(claass.lastIndexOf(".")+1);
                    String value ="";
                    if("Long".equals(claass)||"Double".equals(claass)||"Float".equals(claass)){
                        DecimalFormat longFormatter = new DecimalFormat("###,##0.00");
                        value = longFormatter.format(row.get(key));
                    }else{
                        value = Tools.getStringValue(row.get(key),"","mm.DD.yyyy");
                    }
                    
                    
            //String key = rowkeys.get(m);
             
            %>
            <td class="totals <%=claass%>"><%=value%></td>
            <%
            
            }%>
        </tr>
        <%
        }
    
    }




}
%>
</tbody>
</table>

</div>

<!--input type="hidden" name = "Page" value = "<%=Page%>"/-->

<%if (PageCount>1){%>
<div id = "pageselect">
Страница:
<select name = "Page" onchange="setPage(this.value)">

<%for (int i = 1 ; i <= PageCount; i++){
%>
<li>
<%if(Page==i){%>
<option value = "<%=i%>" selected ="selected"><%=i%></option>
<%}else{%>
<option value = "<%=i%>"><%=i%></option>
<%}%>

<%
}%>
</select>
</div>
<%
}
%>
<%
if(request.getSession().getAttribute("MAIN_SQL")!=null){
    //List<String> exceptions = (ArrayList<String>)JspData.get("SERVLET_EXCEPTION");
    //for (String exceptionMessage:exceptions){
    if(debugger){
    %>
    <p style="width:400px;"><small><%=request.getSession().getAttribute("MAIN_SQL")%></small></p>
    <%
    }
    //}
  }

}else{
  if(Tools.parseInt(JspData.get("IS_QUERY"),-1)==1 && request.getParameter("_gqr_")==null){
  
%>

<h1>Нет данных</h1>
<%}else{
        
  }
}
if(debugger){
%>
<p>

<% 

List<String> exs=(List<String>)request.getSession().getAttribute("EXCEPTION_LIST");
for (String s:exs){
%>
<%=s%><br/>
<%
}%>
</p>
<%}%>
</div>