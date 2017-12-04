<%@ page contentType="text/html;charset=utf-8"%>
<%@page import="java.util.*, myiss.*, java.net.*"%>
<jsp:useBean id="JspData" class="java.util.HashMap" scope="request"/>
<div id="menutoggle">
    <div id="toggle" title = "скрыть / показать меню" class="tiptip"></div>
    <div id="fs">
        <span id="fs1" title = "компактный размер шрифта" class="fontsize fs1 tiptip"  onclick = "setFS('fs1');"></span>
        <span id="fs2" title = "средний размер шрифта" class="fontsize fs2 tiptip"  onclick="setFS('fs2');"></span>
        <span id="fs3" title = "крупный размер шрифта" class="fontsize fs3 tiptip"  onclick="setFS('fs3');"></span>
    </div>
    <div id ="scrolltop" style="display:none;"><img src="img/icons/totop.png" class="tiptip" title = "наверх" onclick="$(document).scrollTop(0)"/></div>
</div>
<div style="clear:both;"></div>

<%

List<HashMap> menu = (JspData.get("MENU")!=null)?(List<HashMap>)JspData.get("MENU"):new ArrayList<HashMap>();
List<HashMap> submenu = (JspData.get("SUBMENU")!=null)?(List<HashMap>)JspData.get("SUBMENU"): new ArrayList<HashMap>();
String t_name = Tools.getStringValue(JspData.get("TNAME"),"");
if(menu!=null){
%>

<div class="menu menu-closed" id = "menu" >
<ul>
<%for(HashMap  hm : menu){
String href= "";
String name = Tools.getStringValue(hm.get("NAME"),"");
int hasSubs = Tools.parseInt(hm.get("HAS_SUB"),-1);
String menuId = Tools.getStringValue(hm.get("MENU_ID"),"");
String source = Tools.getStringValue(hm.get("SOURCE"),"");
String q_name = Tools.getStringValue(hm.get("Q_NAME"),"");

if(hasSubs == 1){
    href = "?Action=enter&Id=" + menuId;
} else 
{
    if(source.length()>0){
        href+=source;
    } else{
        href+="?Action=menu&name="+q_name;
    }
}
String menuClass = (hasSubs==1 && submenu!=null && submenu.size()>1 && menuId.equals(submenu.get(0).get("Sub_Id"))||q_name.length()>0&&q_name.equals(t_name))?"class=\"current\"":"";

%>
<li <%=menuClass%> ><a href="<%=href%>"><%=name%></a>
</li>
<% if (hasSubs==1 && submenu!=null && submenu.size()>1 && menuId.equals(submenu.get(0).get("Sub_Id"))){%>
<ul>
<%
for (HashMap shm:submenu){

String shref = "";
String sname = Tools.getStringValue(shm.get("Sub_Name"),"-");
String ssource = Tools.getStringValue(shm.get("Source"),"");
String sq_name = Tools.getStringValue(shm.get("Q_Name"),"");
boolean isLink = false;
if (ssource.length()>0){
    shref+=ssource;
    isLink = true;
}
else{  
    shref+="?Action=menu&name="+sq_name;
}

String subMenuClass = (sq_name.length()>0
                        &&t_name.length()>0
                            &&sq_name.equals(t_name))
                            ?"class=\"current\""
                            :(isLink)?"class=\"ext-link\"":"";
%>
<li <% if(isLink){%>title="Внешняя ссылка откроется в новом окне"<%}%> <%=subMenuClass%>><a <%=(shref.startsWith("http"))?"target=\"_blank\"":""%> href="<%=shref%>"><%=sname%></a></li>
<%
}
%>
</ul>
<%
}%>
<%
}%>
</ul>
</div>
<%
}
%>