<%@ page contentType="text/html;charset=utf-8"%>
<%@page import="java.util.*, myiss.*, java.net.*"%>
<jsp:useBean id="JspData" class="java.util.HashMap" scope="request"/>

<%


if(JspData.get("QUERY_RESULT")!=null&&((List<HashMap>)JspData.get("QUERY_RESULT")).size()>0){
List<HashMap> queryResult = (List<HashMap>)JspData.get("QUERY_RESULT");


HashMap links = (HashMap)JspData.get("LINKS");
HashMap linkParams = (HashMap)JspData.get("LINK_PARAMS");
ArrayList<String> tabHeaders = (ArrayList<String>)JspData.get("TABLE_HEADERS");
String queryTitle = Tools.getStringValue(JspData.get("QUERY_TITLE"),"");
String baseUrl = Tools.getStringValue(JspData.get("SERVLET_BASE_URL"),"");
String queryString = Tools.getStringValue(JspData.get("SERVLET_QUERY_STRING"),"");

int Page = Tools.parseInt(JspData.get("Page"),1);
int PageCount = Tools.parseInt(JspData.get("PageCount"),1);
%>

<h1><%=queryTitle%></h1>

<a href = "?<%=queryString%>&downloadXLS=y" target = "_blank">Открыть в Excel</a>
<%if(JspData.get("HAS_GRAPHIC")!=null){%>
<a  id="single_image" href ="?<%=queryString%>&downloadImg=y.png">График</a>
<%}%>
<%if (PageCount>1){%>
<br/>
Страницы:
<ul class="paginator">
<%for (int i = 1 ; i <= PageCount; i++){
%>
<li>
<%if(Page!=i){%>
<a href = "#" onclick="setPage(<%=i%>)">
<%=i%>
</a>
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

<table id="table1">
<thead>
<tr>
<%
boolean showAllCols = true;
if(tabHeaders!=null && tabHeaders.size()>0){
    for (int ii = 0 ; ii < tabHeaders.size(); ii++){
    
        if (request.getParameter("r"+ii)!=null)
        showAllCols = false;
    }
}

for (int i = 0 ; i < tabHeaders.size(); i++){
    boolean showCol = (request.getParameter("r"+i)!=null || showAllCols)? true : false;
    if(showCol){
    %>
        <th style="background-color:#666666"><%=tabHeaders.get(i)%></th>
    <%
    }
}
%>
</tr>
</thead>
<tbody>
<%
for (int i = 0 ; i<queryResult.size(); i++){
HashMap resRow = (HashMap)queryResult.get(i);
%><tr>
<%

for (int ii = 0 ; ii < tabHeaders.size(); ii++){
    boolean showCol = (request.getParameter("r"+ii)!=null || showAllCols)? true : false;
    String key = tabHeaders.get(ii);
    String value = Tools.getStringValue(resRow.get(key),"","dd.MM.yyyy");
    if(showCol == false) continue;
    if(links.get(key)==null){
    %>
        <td><%=value%></td>
    <%
        }else{
        String tname = Tools.getStringValue(((HashMap)links.get(key)).get("TName"),"");
        String qname = Tools.getStringValue(((HashMap)links.get(key)).get("QName"),"");
        String keyVal = Tools.getStringValue(resRow.get(key),"");
        String linkHref = "";
        if (linkParams.get(key)!=null){
            List<String> hrefParamList = (List<String>)linkParams.get(key);
            for (String paramName : hrefParamList){
                linkHref+="&"+paramName+"="+Tools.getStringValue(resRow.get(paramName),"","dd.MM.yyyy");
            }
        }
//key=URLEncoder.encode(key);
%>
<td>

<a href = "?Action=query&Page=1&tname=<%=tname%>&qname=<%=qname%><%=linkHref%>"><%=value%></a>

<%--a href = "?Action=query&Page=1&tname=<%=tname%>&qname=<%=qname%>&<%=key%>=<%=keyVal%>"><%=value%></a--%>
</td>
<%
}
}
%>
</tr>
<%
}
if(JspData.get("TOTALS")!=null){
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
                String value = Tools.getStringValue(row.get(key),"","mm.DD.yyyy");
                
           
            //String key = rowkeys.get(m);
             
            %>
            <td><%=value%></td>
            <%
            
            }%>
        </tr>
        <%
        }
    
    }




}
%></tbody></table>


<input type="hidden" name = "Page" value = "<%=Page%>"/>

<%if (PageCount>1){%>

Страницы:
<ul class="paginator">
<%for (int i = 1 ; i <= PageCount; i++){
%>
<li>
<%if(Page!=i){%>
<a href = "#" onclick="setPage(<%=i%>)">
<%=i%>
</a>
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
<%
}else{
  if(JspData.get("SERVLET_EXCEPTION")!=null){
    List<String> exceptions = (ArrayList<String>)JspData.get("SERVLET_EXCEPTION");
    for (String exceptionMessage:exceptions){
    %>
    <small><p><%=exceptionMessage%>
    </p></small>

    <%
    }
  }
%>
<h1>Нет данных</h1>
<%
}
%>