<%@ page contentType="text/html;charset=utf-8"%>
<%@page import="java.util.*, myiss.*, java.net.*"%>
<jsp:useBean id="JspData" class="java.util.HashMap" scope="request"/>
<% 
boolean updateIsFinished = (JspData.get("UPDATE_FINISHED")!=null) ? true : false ; 
%>
<SCRIPT type="text/javascript" language = "javascript">
function checkIfUpdateFinished(isFinished){
    if(isFinished) alert("Данные пересчитаны.");
}
function updAsk(){
res =confirm ('Вы действительно хотите пересчитать данные? Процесс может занять несколько минут.')
if(res) showLoader();
return res;
}
checkIfUpdateFinished(<%=updateIsFinished%>);
</SCRIPT>

<%
List<HashMap> updates = (List<HashMap>)JspData.get("UPDATES");
if (updates!=null && updates.size()>0){ %>
<form name = "upd_form" id = "upd_form" action="" method="POST" accept-charset="utf-8">
<input type="submit" name = "update_data" value ="Обновить" onclick="return updAsk();"/>
</form>
<%}
%>