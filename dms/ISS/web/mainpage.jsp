<%@ page contentType="text/html;charset=utf-8"%>
<%@page import="java.util.*, myiss.*, java.net.*"%>
<jsp:useBean id="JspData" class="java.util.HashMap" scope="request"/>

<%
String verNum = "1.5";
JspData.put("iss-version",verNum);
String ver = "?ver="+verNum;%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Информационно-справочная система</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link type="text/css" href="css/site.css<%=ver%>" rel="Stylesheet" />
<!--link type="text/css" href="css/smoothness/jquery-ui.css" rel="Stylesheet" /-->
<!--link type="text/css" href="css/ingrid.css" rel="Stylesheet" /-->
<link rel="Stylesheet" type="text/css" href="css/chosen.css<%=ver%>" />
<link rel="Stylesheet" type="text/css" href="css/ui.totop.css<%=ver%>" />

<link type="text/css" href="css/jquery.jscrollpane.css<%=ver%>" rel="stylesheet" media="all" />
<link type="text/css" href="css/jquery.jscrollpane.lozenge.css<%=ver%>"  rel="stylesheet" media="all" />
<link rel="stylesheet" type="text/css" href="css/colresizable.css<%=ver%>" />  
<link rel="stylesheet" type="text/css" href="css/tipTip.css<%=ver%>" />  
<link href="css_pirobox/style_5/style.css<%=ver%>" rel="stylesheet" type="text/css" />
<link rel="stylesheet" href="fancybox/jquery.fancybox-1.3.4.css<%=ver%>" type="text/css" media="screen" />

<script type="text/javascript" src="jquery.js<%=ver%>"></script>
<script type="text/javascript" src="js/jquery.mousewheel.js<%=ver%>"></script>
<script type="text/javascript" src="js/jquery.jscrollpane.min.js<%=ver%>"></script>
<script type="text/javascript" src="js/chosen.jquery.min.js<%=ver%>"></script>
<script type="text/javascript" src="fancybox/jquery.fancybox-1.3.4.pack.js<%=ver%>"></script>
<script type="text/javascript" src="js/jquery-ui-1.8.16.custom.min.js<%=ver%>"></script>
<script type="text/javascript" src="js/pirobox_extended_feb_2011.js<%=ver%>"></script>
<script type="text/javascript" src="js/jquery.tipTip.minified.js<%=ver%>"></script>
<script type="text/javascript" src="js.js<%=ver%>"></script>
<script type="text/javascript" src="js/jquery.cookie.js<%=ver%>"></script>

<!--script type="text/javascript" src="js/easing.js"></script>
<script type="text/javascript" src="js/jquery.ui.totop.min.js"></script-->
<!--script  src="js/colResizable-1.2.min.js"></script-->

</head>
<%
String severeException = Tools.getStringValue(JspData.get("SevereException"),"");
int uId = Tools.parseInt(JspData.get("UID"),-1);
String baseUrl = Tools.getStringValue(JspData.get("SERVLET_BASE_URL"),"");
String queryString = Tools.getStringValue(JspData.get("SERVLET_QUERY_STRING"),"");
int actionId = Tools.parseInt(JspData.get("ACTION_ID"),-1);
%>
<body>
<div id="mainwrapper">
<div id = "loader"><img id = "loaderimg" src="img/loader2.gif"/></div>



<div class="header">
<jsp:include page="header.jsp"/>
</div>
<%
if (severeException.length()>0){%>
<div id="severe_exception">
Похоже, что-то поломалось:<br/>
<%=severeException%>
</div>
<%}%>

<% if (uId>0){%>
<div class="middle" id = "mid_conteiner">
<jsp:include page="left_menu.jsp"/>
<%
List<HashMap> querylist = (JspData.get("QUERY_LIST")!=null)?(List<HashMap>)JspData.get("QUERY_LIST"): new ArrayList<HashMap>();
HashMap navipath = (JspData.get("NAVI_PATH")!=null)? (HashMap)JspData.get("NAVI_PATH"): null;
%>
<div id="content">
<div class="c-wrapper">


<%if(queryString.length()==0){%>
<style type="text/css">
    #USER_MESSAGE{
        width: 100%;
        height: 15em;
        margin-bottom: 1em;
    }
    #USER_MESSAGE_SAVE{
        text-align: right;
    }
</style>
<form method="POST" accept-charset="utf-8">
<%----%>
<span id = "announcement-logged" style="font-size:14px;"><strong>Уважаемый пользователь!</strong><br/>
В настоящее время производится анализ актуальности информационных систем ОИЯИ.
В связи с этим убедительно просим Вас сообщить, какие отчеты ИСС Вам необходимы.
Эти данные нам необходимо получить не позднее <i>20.11.2017</i>.<br/>
<textarea name="USER_MESSAGE" id="USER_MESSAGE" placeholder="Введите, какие данные ИСС Вам необходимы"></textarea>
<div id="USER_MESSAGE_SAVE">
<button type="submit" >Отправить</button>
</div>
</span>
</form>
<%----%>

    <div id = "maininfo">

<ul>
 <li>ИСС обеспечивает авторизованный доступ к информации любого профиля посредством настраиваемого Web-интерфейса.</li>

<li>Web-интерфейс на основе таблиц БД предоставляет возможность задавать следующие элементы системы: меню и подменю группы пользователей, SQL-текст запроса, общесистемные и локальные параметры, связи между запросами и объектами.</li>

<li>Пользователю предоставляется возможность в запросе выбирать значения параметров, порядок сортировки и определять список выходных реквизитов (с перерасчетом всей формы при использовании групповых операций).</li>

<li>В ИСС реализованы функции оперативного и статистического учета работы пользователей</li>
</ul>
</div>
<%}%>
<%if(navipath!=null && navipath.size()>0){
String menuName = Tools.getStringValue(navipath.get("NAME"),"");
String smenuName = Tools.getStringValue(navipath.get("SUB_NAME"),"");
String squeryTitle = Tools.getStringValue(navipath.get("QUERY_TITLE"),"");
%>
    <ul class="navigation">
    <%if(menuName.length()>0){%>
        <li><%=menuName%></li>
    <%}%>
    <%if(smenuName.length()>0){%>
        <li>&rarr;&nbsp;<%=smenuName%></li>
    <%}%>
    <%if(squeryTitle.length()>0){%>
        <li>&rarr;&nbsp;<%=squeryTitle%></li>
    <%}%>
    </ul>
    <jsp:include page="updates.jsp"/>
<%}
if(querylist.size()>0){
%>
<ul class="querylist">
<%

    String tname = Tools.getStringValue(JspData.get("TNAME"),"");
    for (HashMap qhm: querylist){
    String Name_QUERY =  Tools.getStringValue(qhm.get("Name_QUERY"),"");
    String QUERY = Tools.getStringValue(qhm.get("QUERY"),"");
    String qhref = "";
    qhref +="?_gqr_=1&Action=query&tname="+tname+"&qname="+Name_QUERY+"&Page=1";
    %>
    <li><a href = "<%=qhref%>"><%=QUERY%></a></li>
    
    <%
}
%>
</ul>
<%
}




if(actionId == CommonData.ACTION_QUERY){
%>
<form name ="theform" id ="theform" method="get" action="?<%=queryString%>" accept-charset="utf-8">
<jsp:include page="q_filters.jsp"/>
<jsp:include page="q_table.jsp"/>
</form>
<%}%>






</div> <!--c-wrapper-->
</div> <!--content-->
<div class="clear">
</div>
</div>
<!--/form-->
<%}else{

%>

<div class="middle" id = "mid_conteiner">


<div id = "issdescription">

<p>
<%--
<span id = "announcement"><strong>Уважаемые пользователи,</strong>
для устранения проблем, связанных с предупреждением системы безопасности,
которые возникли 04.06.2014 во многих web-сервисах ОИЯИ,
установите к себе на компьютер сертификат ОИЯИ. <a href = "http://noc.jinr.ru/inform/inf_cert.shtml" target="_blank">Здесь</a> подробно описано, как это сделать. 
</span>
--%>

ИСС обеспечивает авторизованный доступ к оперативным данным административно-хозяйственной деятельности института. Права пользователя устанавливаются при регистрации. 
Для получения пользовательского пароля Вам необходимо заполнить <a target = "_blank" title="Документ Microsoft Word, 36КБ" href="ASU_REG.doc">бланк регистрации</a>, который Вы можете скопировать себе на ПК в формате MS Word, получить разрешающие визы и отправить его администратору Базы Данных. 
<!--Для незарегистрированных пользователей имя : guest, пароль : guest.--> Для работы с ИСС рекомендуется использовать <a href="https://www.mozilla.org/ru/firefox/new/">Firefox 12.0+</a>, <a href="http://www.opera.com/ru">Opera 11+</a>, <a href="https://www.google.ru/intl/ru/chrome/browser/">Google Chrome</a>, <a href="http://windows.microsoft.com/ru-ru/internet-explorer/download-ie">Microsoft Internet Explorer</a> версии 9.0 и выше.
<br/>
<%--
<span id = "oldversion">
<a href="http://adm2.jinr.ru/query/" target="_blank"><img src = "img/oldversion.png"/></a>
<span><a href="http://adm2.jinr.ru/query/" target="_blank">Старая версия ИСС</a></span>
</span>

</p>
--%>
</div>
</div>
<%}%>
<div class="push"></div>
</div>
<div class="footer"><p>Все вопросы и замечания направлять: <a href = "mailto:vborisov@jinr.ru">vborisov@jinr.ru</a>, 
<a href = "mailto:adminasu@jinr.ru">adminasu@jinr.ru</a>
</p>
<%
Date date = new Date();
int year= date.getYear()+1900;
boolean isProduction = (request.getServerName().indexOf("iss.jinr.ru")>-1);
isProduction =false;
     baseUrl = (isProduction
                    ? "https://"
                    : request.getScheme() + "://") +   // "http" + "://
             request.getServerName() +       // "iss.jinr.ru"
             (isProduction
                    ?  ""
                    : ":" + request.getServerPort()) +       // ":8080"
             ((isProduction)
                    ? request.getRequestURI().replaceAll("/iss/","")
                    : request.getRequestURI())                                  // "/iss/index.jsp"
             ;

               baseUrl = baseUrl.replaceAll("mainpage.jsp","");
%>
<p>ИСС <a  class="iss-version" href = "<%=baseUrl%>/1.html">версия: <%=verNum%></a> &copy; <%=year%>г. </p>
</div>
</body>
</html>