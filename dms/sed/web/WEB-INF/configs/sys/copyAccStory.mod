copyAccStory.cfg

[parameters]
title=*ЛОГ=>архив
debug=off

[end]

[report]
$INCLUDE dat/common.dat[head]
<style type="text/css">
$INCLUDE free/main_css_noDB.cfg[report]
</style>


$GET_DATA [getArchTables]
$GET_DATA [move to achive] ??cop=Y&!TABLE_EXISTS

<body bgcolor=F4FFFA style="padding:20px;">
<table border=0 cellpadding=0 cellspacing=0 width=98%>
<tr><td width=90%><h3>СЭД - лог запросов - сброс в архив</h3></td>
<td align=right nowrap=true>
</td><td align=right nowrap=true>
<a class=info href="/adb/adb">
Главная</a>
</td></tr></table>
<form name="theForm" method="POST" enctype="multipart/form-data">
<input type=hidden name="c" value="#c#">
<input type=hidden name="cop" value="Y">

<table border=0 cellpadding=8 style="border:solid 1px gray; margin:10px 0 0 200px; background-color:white;">
<tr><td style="text-align:right;">Записей в a_req_log: </td><td>#TOT_NUM_RECS#</td></tr>
<tr><td style="text-align:right;">Таблицы:</td><td>#TNAMES#</select></td></tr>
<tr><td colspan=2 align=center>
<input type="submit" value=" Сбросить в архив #ARCH_TABLE#"
disabled ??TABLE_EXISTS
></td></tr>
</table>
#ERROR#
</body>
</html>
[end]




****************************************************************************
****************************************************************************
****************************************************************************

[getArchTables]
select concat(table_name,'<br>') as TNAMES
from information_schema.tables
where table_schema='dms'
and table_name like 'a_req_log%'
order by table_schema
;
select count(*) as TOT_NUM_RECS from a_req_log
;
select concat('a_req_log_', DATE_FORMAT(now(),'%Y%m%d')) as ARCH_TABLE from dual
;
select table_name as TABLE_EXISTS
from information_schema.tables
where table_schema='dms' and table_name='#ARCH_TABLE#'
;
[end]

[move to achive]
create table #ARCH_TABLE# as select * from a_req_log
;
truncate table a_req_log ??!ERROR
;
CREATE INDEX IDX_#ARCH_TABLE#_DAT ON #ARCH_TABLE# (DAT)
;
CREATE INDEX IDX_#ARCH_TABLE#_USER ON #ARCH_TABLE# (USER_ID)
;
[end]
