[comments]
descr=S: Login iframe для DMS
input=uname, pw (при входе), q_cwl - если уже залогинен
output=форма логина или подтверждение и запись куки или кнопка "Выход"
parents=
childs=
testURL=?c=wlogin_dms
author=Куняев
[end]

[parameters]
service=jinr.cwl.ServiceWLogin
LOG=ON
[end]


[not identified]
$INCLUDE [report]
[end]

background-color: whitesmoke; ??q|!logged=YES&!cop=u
background-color: white; ??!q&logged=YES|cop=u
background: url(#imgPath#top_bg1.gif) repeat-x; ??ZZZ&!q&logged=YES|cop=u

[report]
$SET_PARAMETERS debug=off
  ??xxxdb=PPO
$SET_PARAMETERS longLogin=y ??login=nalog

<HTML><HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=windows-1251">
<meta name="CACHE-CONTROL" CONTENT="NO-CACHE">
<meta http-equiv="pragma" content="no-cache">
<style>
body { background-color: white; font-family: Verdana,Arial,Helvetica,sans-serif; font-size: 11pt; margin:0;}

td.big {font-size: 11pt;}

input.butt1 {background-color: ##e0e0e0; color:##000080; margin-left:6px; border:outset 1px ##aaaaaa; height:22px; cursor:pointer; border-radius: 8px; font-size:9pt;}
.small {font-size:9pt;}
a.small {font-size:9pt;}
.msg1 {font-size:11pt;}
</style>
</HEAD>

<BODY>
$INCLUDE [logout report] ??cop=u
$INCLUDE [login form]   ??!logged&!q&!cop=u
$INCLUDE [confirm report] ??q&!cop=u
$INCLUDE [logged report]  ??!q&logged=YES&!cop=u
$INCLUDE [preSQLs] ??
</body></html>
[end]

[login form]
============================= ФОРМА ЛОГИНА ========================= ??
$SET_PARAMETERS login=#uname# ??uname_ZZZ

<CENTER>
$INCLUDE wlogin_dms.cfg[form top]

<TABLE BORDER=0 CELLSPACING=1 cellpadding=1>
<TR><td align=right class=big width=1%>логин:</td><td width=1%><input name="uname" size=15 value='#uname#'></td><td></td></tr>

<tr><td class=big align=right>пароль:</td><td><input type=password name="upass" size=15></td>
<td align=left><input type=submit class=butt1 style="width:80px;" value="  Вход  ">
</tr>

<TR><td colspan=3 align=center>
$INCLUDE wlogin_dms.cfg[err msg]  ??uname&upass&!logged=YES
</td></tr></table>
</FORM>

$INCLUDE wlogin_dms.cfg[login form script]
[end]


[form top]
 <FORM NAME="loginForm" METHOD="POST" enctype="multipart/form-data" onSubmit="return doIt();">
 <INPUT TYPE="hidden" NAME="c" VALUE="#c#">
 <INPUT TYPE="hidden" NAME="cop" VALUE="">
 <INPUT TYPE="hidden" NAME="back_host" VALUE="#back_host#">
 <INPUT TYPE="hidden" NAME="back_param" VALUE="#back_param#">
 <INPUT TYPE="hidden" NAME="back_url" VALUE="#back_url#">
<input type=hidden name="tm" value="#tm#">
[end]


[confirm report]
===================== CONFIRM ==================== ??
$GET_DATA wlogin_dms.cfg[update wu]
<form name=loginForm  METHOD="POST"  target=_top
action="#back_host#" ??back_host
action="#back_url#"  ??back_url&!back_host
 enctype="multipart/form-data" 
>
<input type=hidden name="sess_id" value="#q#"> ??
<CENTER>
Пользователь <i>#FIO# (#login#)</i><br> успешно зарегистрирован.  ??!GET_DATA_ERROR&is_active=1
Аккаунт <i>#FIO# (#login#)</i><br> заблокирован.  ??!GET_DATA_ERROR&is_active=0
ПРОИЗОШЛА СИСТЕМНАЯ ОШИБКА! #is_active#; <br><br>Обратитесь в службу поддержки ??GET_DATA_ERROR|!is_active
</center>
$SET_PARAMETERS_SESSION FIO=#FIO#;

<script>
var frm = document.loginForm;
alert("loginCookieName=#loginCookieName#"); ??
document.cookie = "#loginCookieName#=" + escape("#q#") + "; domain=.jinr.ru; path=/;";  ??!longLogin&is_active=1

var exp=new Date(); exp.setHours(exp.getHours()+1000);   ??longLogin
document.cookie = "#loginCookieName#=" + escape("#q#") + "; domain=.jinr.ru; path=/; expires="+exp.toGMTString()+";";  ??longLogin&is_active=1
var t = #tm# / 1000; 
document.cookie = "cwldid=" + t + "; domain=.jinr.ru; path=/; expires=Sun, 04 Jan 2026 03:14:07 GMT;";  ??!q_cwldid

$INCLUDE wlogin_dms.cfg[redir script]  ??is_active=1&back_host|back_url
</script>
</form></center>
[end]

#uname#_


[logged report]
===================== LOGGED ==================== ??
<div style="text-align:right;">
 <FORM NAME="loginForm" METHOD="POST" enctype="multipart/form-data">
 <INPUT TYPE="hidden" NAME="c" VALUE="wlogin_dms">
 <INPUT TYPE="hidden" NAME="cop" VALUE="">
 <INPUT TYPE="hidden" NAME="back_host" VALUE="">
 <INPUT TYPE="hidden" NAME="back_param" VALUE="">
 <INPUT TYPE="hidden" NAME="back_url" VALUE="#back_url#">

<input type=button class=butt1 style="width:80px; margin:0;" value="Выход" onClick="unlog();">
<br>&nbsp;&nbsp;<a class=small href="javascript:cpass()">сменить пароль >></a> ??


</form>
</div>

$INCLUDE wlogin_dms.cfg[logged script]
[end]



[logout report]
===================== LOGOUT ==================== ??
<form name=loginForm  METHOD="POST" target=_top
action="#back_host#" ??back_host
action="#back_url#"  ??back_url&!back_host
 enctype="multipart/form-data" 
>
<input type=hidden name="sess_id" value="#q#">
<CENTER>
ПРОИЗОШЛА СИСТЕМНАЯ ОШИБКА! <br>Обратитесь в службу поддержки ??GET_DATA_ERROR
Выход из системы...  ??!GET_DATA_ERROR
</center>

<script>
var frm = document.loginForm;
document.cookie = "#loginCookieName#=; domain=.jinr.ru; path=/; expires: -1;"; 

$INCLUDE wlogin_dms.cfg[redir script]  ??back_host|back_url
</script>
</form></center>
[end]


+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
+++++++++++++++++++++++++++++++++ СКРИПТЫ +++++++++++++++++++++++++++++++++
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

[login form script]
<script>
var frm = document.loginForm;
var v ="#queryString#";
v = v.replace(/&c=wlogin3/,"");
alert ("#queryString#\n\r" + v);  ??
v = v.replace(/¦/g,"&");
document.write(v); ??
var i = v.indexOf("?");
if (i < 0) i = v.length;

frm.back_param.value=escape(v.substring(i+1)); ??!back_param

var j = v.indexOf("back_url=");
if (j >=0)
{
  v = v.substring(j+9,i);
  alert (v); ??
  document.write("<br>host: '" + v + "'<br>" ); ??
  frm.back_host.value=v; ??!back_host
}

function doIt()
{ if (frm.uname.value=="" || frm.upass.value=="")
  { alert ("Введите имя пользователя и пароль");
    return false;
  }
}

</script>
[end]


[redir script]
============== редирект на стр. приложения после входа или выхода =============??
var v =unescape("#back_param#");
v = v.replace(/&c=#c#/,"");
var a ="";
a ="#back_host#?" + v ??back_host
a ="#back_url#?"  + v ??back_url&!back_host
var p = v.split("&");
var s = "";
for (i=0; i<p.length; i++)
{ // document.write("<br>" + p[i]);
  var par = p[i].split("=");
  if (par.length == 2)
  {
  document.write("<input name='" + par[0] + "' value='"+ par[1] + "'"
  	+ " type=hidden"  + ">");
  document.write("'" + par[0] + "'='"+ par[1] + "'<br>");   ??
    s = "&";
  }
}

a += s + "sess=#ses#";

document.write("</center><a href='" + a + "' target=_top>.</a>");

function redir()
{ 
  try {
    top.window.location.replace(a);
  } catch (e) {
    alert("submit");  ??
    document.loginForm.submit(); 
  }
}

setTimeout("redir()",100) 
[end]


[logged script]
========================= скрипт залогиненого ================ ??
<script>
var frm = document.loginForm;
var v ="#queryString#";
v = v.replace(/¦/g,"&");
v = v.replace(/&c=#c#/,"");
document.write(v); ??
var i = v.indexOf("?");
if (i < 0) i = v.length;
frm.back_param.value=escape(v.substring(i+1)); ??!back_param

var j = v.indexOf("back_url=");
v = v.substring(j+9,i);
document.write("<br>host: '" + v + "'<br>" );  ??
frm.back_host.value=v; ??!back_host

function cpass()
{  var win= window.open( "#ServletPath#?c=cp", "cp",
  "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes," +
  "resizable=yes,copyhistory=yes,width=600,height=400");
}

function unlog()
{ 
	document.loginForm.cop.value="u";
  document.loginForm.submit();
}
document.cookie = "#loginCookieName#=; domain=.jinr.ru; path=/; expires: -1;"; ??cop=u
</script>
[end]



[err msg]
$SET_PARAMETERS errMsg=Неверное имя пользователя или пароль ??!ERROR
$SET_PARAMETERS errMsg=С этого IP-адреса вход не разрешен ??ERROR=WRONG IP
<IMG VALIGN="TOP" SRC="#imgPath#alert.gif" BORDER=0 width=16 heigth=16>
#errMsg#
[end]

===============================================================================
===============================================================================
===============================================================================

update up set p=md5(concat(pw,'~',right(pw,2)))

[preSQLs]
$INCLUDE wlogin_dms.cfg[get user] ??uname&upass
$INCLUDE wlogin_dms.cfg[registerUserSQL] ??USER_ID&uname&upass
$INCLUDE wlogin_dms.cfg[logoutSQL] ??cop=u
[end]

[get user]
select u.person_id as USER_ID, u.ID as OLD_USER_ID
, concat(left(p.I,1),'.', left(p.o,1),'.',p.F) as FIO
, p.FIO ??
, u.IP_MASK ??
, 'YES' as logged
, lower('#uname#') as "login"
, md5('#tm#') as "ses"
, u.is_active
from users u join up on up.id=u.person_id
join dms.info_11 p on p.person_id=u.person_id
persons p on p.person_id=u.person_id ??
where lower(u.login)=lower('#uname#') 
and up.pw='#upass#' ??
and up.p=md5(concat('#upass#','~',right('#upass#',2)))
limit 1;
[end]


[registerUserSQL]
insert into sessions (user_id, login_time, IP, agent, referer, did) 
 values (#USER_ID#, now() ,'#ClientIP#', '#h_user-agent#', '#back_url#',
'#q_cwldid#' ??q_cwldid
cast(#tm#/1000 as char) ??!q_cwldid
)
;
select LAST_INSERT_ID() as NEW_SESS_ID
;
update users
 set sess_ID=#NEW_SESS_ID#, last_login=now(), IP='#ClientIP#', agent='#h_user-agent#', num_logs=ifnull(num_logs,0)+1
where person_id=#USER_ID#
;
[end]


[update wu]
update up set sh='#q#' where id=#USER_ID#;
[end]

[logoutSQL]
try: update up set sh='' where id=#USER_ID#
[end]


update wu set last_hit=sysdate, NUM_HITS = nvl(NUM_HITS,0) + 1
  where id='#USER_ID#'
;
update sessions set last_hit=sysdate, NUM_HITS = nvl(NUM_HITS,0) + 1
  where sess_id=(select SESSID from wl.wu where id='#USER_ID#');


