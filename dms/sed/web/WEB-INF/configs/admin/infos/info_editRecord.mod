[comments]
descr=А: Ручное редактирование записи справочника.

input=info_id - ID выбранного справочника, record_id - ID записи в справочнике
output=HTML форма редактирования записи справочника, 
parents=admin/infos/info_viewData.cfg
childs=
test_URL=?c=admin/infos/info_editRecord&info_id=10&record_id=2000
[end]

[parameters]
request_name=A:редактир. данных справочника
service=jinr.sed.ServiceEditInfoData
tableCfg=table_no
KeepLog=false
ClearLog=true
SYS_FIELDS=IS_DELETED, IS_MANUAL, DATE_FORMAT(changed,'#dateTimeFormat#') as MODIFIED, VIEW1, VIEW2, VIEW3, SORT
SYS_FIELDS_UPDATE=IS_DELETED, IS_MANUAL, CHANGED, VIEW1, VIEW2, VIEW3, SORT 
SYS_FIELDS_TYPES=boolean,boolean,sysdate,int,int,int,int
[end]


[report header]
$GET_DATA [getInfo] ??
+++++++ временно - открыто всем. Потом - проверить R и RW права пользователя +++ ??
$SET_PARAMETERS RWACC=Y; RACC=Y;
$INCLUDE [set defaults]  ??cop
$INCLUDE [OK report header]  ??RACC&!cop=save
[end]

[set defaults]
$SET_PARAMETERS VIEW1=0; ??!VIEW1
$SET_PARAMETERS VIEW2=0; ??!VIEW2
$SET_PARAMETERS VIEW3=0; ??!VIEW3
$SET_PARAMETERS IS_DELETED=0; ??!IS_DELETED
$SET_PARAMETERS IS_MANUAL=0; ??!IS_MANUAL
[end]

[OK report header]
--------------------------------------------- ??
------------ возвращаемые результаты --------- ??
--------------------------------------------- ??
$GET_DATA [add record] ??cop=add
<div id="result">
<small>#c#</small> ??debug=on
<form name="popupForm" method="POST" enctype="multipart/form-data" target="wf">
<input type=hidden name="c" value="#c#">
<input type=hidden name="info_id" value="#info_id#">

<input type=hidden name="cop" value="">
<input type=hidden
name=record_id value='#record_id#'>
<center>
++++++++++++++++++ Шапка таблицы  +++++++++++++++++++++ ??
Справочник: #INFO_NAME# (#TABLE_NAME#)
<table border=0 class="tlist" cellpadding=0 cellspacing=0>
<tr><td>Поле:</td><td>Значение:</td></tr>
$SET_PARAMETERS RO=readonly;
[end]

[add record]
select 
ifnull(max(id), 0) + 1 ??!IS_EXTERNAL=1
ifnull(min(id), 0) - 1 ??IS_EXTERNAL=1
as "record_id" from #TABLE_NAME# 
;
insert into #TABLE_NAME# (id) values (#record_id#)
;
[end]

============== Вывод одного поля для редактирования ======= ??
[item]
<tr><td class=label>#NAME#
(#COMMENT#) ??COMMENT
: </td>
$INCLUDE [input] ??TYPE=int|SIZE<64
$INCLUDE [text] ??TYPE=varchar&SIZE>63
$INCLUDE [date field] ??TYPE=date
</tr>
[end]

[input]
<td><input size=#SIZE# class="#RO#" #RO# name=#FIELD_DB_NAME# value="^#FIELD_DB_NAME#"></td>
$SET_PARAMETERS RO=;
[end]

[text]
<td><textarea cols=60 name=#FIELD_DB_NAME# rows=2>^#FIELD_DB_NAME#</textarea>
[end]


[date field]
<td>
$INCLUDE dat/doc_fields.dat[date field w]
</td>
[end]



[report footer]
</form>
$INCLUDE [view footer]  ??!cop=save
$INCLUDE [save footer]  ??cop=save
[end]

[view footer]
++++++++++++ Конец формы редактирования записи ++++++++++ ??
<tr><td class=label>Показывать в представлении:</td><td>
<input type=checkbox name="VIEW1" value="1"
checked ??VIEW1=1
> 1
<input type=checkbox name="VIEW2" value="1"
checked ??VIEW2=1
> 2
<input type=checkbox name="VIEW3" value="1"
checked ??VIEW3=1
> 3
&nbsp;
сортировка: <input size=3 name=SORT value="#SORT#">
</td></tr>

<tr><td></td><td>
<input type=checkbox name="IS_MANUAL" value="1"
checked ??IS_MANUAL=1
>Только ручное обновление<br>
<input type=checkbox name="IS_DELETED" value="1"
checked ??IS_DELETED=1
>Помечена на удаление

</td></tr>
<tr><td colspan=2 class="right small">Запись обновлена: #MODIFIED#</td></tr>
</table>
<input type="button" class="butt1" style="width:120;" value="Закрыть" onClick="HideDialog();">  ??RACC

<input type="button" class="butt1" style="width:120;" value="Сохранить" onClick="document.popupForm.cop.value='save'; document.popupForm.submit();">  ??RWACC
<input type="button" class="butt1" style="width:120;" value="Сохранить" onClick="doSubmit('save', '#c#');">  ??RWACC_ZZZ

+++++++++ Скрипт отображения pop-up окна ++++ ??
<script>
window.parent.ShowDialog(true); 
window.parent.showMsg("##dialog_title", "Редактирование данных справочника #INFO_NAME#");
window.parent.centerDialog();  
</script>
[end]


+++++++++ Скрипт обновления таблицы данных в вызывавшей странице ++++ ??
+++++++++ (Pop-up окно уже закрыто по кнопке на предыд. шаге) +++ ??
[save footer]
$GET_DATA [update views] ??

<script>
+++++ Сабмит родительской формы. +++ ??
window.parent.doSubmit('', 'admin/infos/info_viewData');  ??!ERROR
</script>
[end]



***************************** Шаблоны SQL запросов ***************************

[preSQLs]
select i.ID as "INFO_ID"
, i.name as INFO_NAME, i.IS_EXTERNAL
, i.IS_TREE, i.IS_ACTIVE
, i.TABLE_NAME
, data_updated 
from i_infos i
where i.Id=#info_id#
;
select concat(field_db_name, ',') as FIELDS
 , concat(name, ',') as FIELDS_NAMES ??
 , concat(type, ',') as FIELDS_TYPES
from i_fields
where info_id=#info_id#
order by nr
;
select count(field_db_name) as NUM_FIELDS, count(field_db_name)+ 1 as NUM_COLUMNS
from i_fields where info_id=#info_id#
;
[end]


[SQL]
select NR, NAME, TYPE, SIZE, COMMENT, FIELD_DB_NAME
from i_fields i
where info_id=#info_id#
order by nr
[end]


[update views]
[end]