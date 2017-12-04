[comments]
descr=А: Просмотр данных справочника. 

input=info_id - ID выбранного справочника
output=HTML форма данных справочника, 
parents=admin/infos/info_data.cfg
childs=admin/infos/info_editRecord 
test_URL=?c=admin/infos/info_viewData&info_id=10
author=Куняев
[end]

[parameters]
request_name=A:просмотр данных справочника
service=jinr.sed.ServiceViewInfoData
KeepLog=false
ClearLog=true
[end]


[report header]
$SET_PARAMETERS RWACC=Y
$SET_PARAMETERS SYS_FIELDS=IS_DELETED, IS_MANUAL, DATE_FORMAT(changed,'#dateTimeFormat#') as MODIFIED, SORT
$SET_PARAMETERS CRITERIA=where is_manual=1; 	??f_rec_status=MANUAL
$SET_PARAMETERS CRITERIA=where is_deleted=1; 	??f_rec_status=DELETED
$SET_PARAMETERS CRITERIA=where is_manual=0; 	??f_rec_status=AUTO
$SET_PARAMETERS CRITERIA=#CRITERIA# and view#f_rec_view#=1 ??f_rec_view&CRITERIA
$SET_PARAMETERS CRITERIA=where view#f_rec_view#=1 ??f_rec_view&!CRITERIA
$SET_PARAMETERS srt=sort; desc=; ??!srt
$SET_PARAMETERS ORDER_BY=#srt# #desc#,;

<div id="result">
<style>table.tlist td, table.tlist th{font-size:8pt;}</style>
<small>#c#</small> ??debug=on
<center>

============ Начало таблицы =============== ??
<table border=0 class="tlist tborder" cellpadding=0 cellspacing=0>
<tr><th colspan=#NUM_COLUMNS# 
style="border-bottom:solid 1px gray;" ??
>
<div class="big" style="float:left;"> 
Просмотр данных справочника "#INFO_NAME#"
</div>
<div style="float:left; margin-left:100px;"> 
<input type="button" class="butt1" style="width:160;" value="Добавить запись" 
onClick="doSubmit('add', 'admin/infos/info_editRecord ');" ??
onClick="AjaxCall('popupCont','c=admin/infos/info_editRecord&cop=add&info_id=#info_id#');"
>  ??RWACC
</div>
<div style="clear:both;"></div>
</th></tr>

============ FILTERS =============== ??
<tr><td colspan=#NUM_COLUMNS#>
<table border=0 cellpadding=0 cellspacing=0>
<tr><td><input type=radio name=f_rec_status value="" onclick="showNext(0);"
checked ??!f_rec_status
>  Все записи
</td><td class="DELETED">
<input type=radio name=f_rec_status value="DELETED" onclick="showNext(0);"
checked ??f_rec_status=DELETED
>  Только удаленные
</td><td class="MANUAL">
<input type=radio name=f_rec_status value="MANUAL" onclick="showNext(0);"
checked ??f_rec_status=MANUAL
>  Только "ручные"
</td><td class="AUTO">
<input type=radio name=f_rec_status value="AUTO" onclick="showNext(0);"
checked ??f_rec_status=AUTO
>  Только автоимпортируемые (не ручные)
&nbsp; Поиск:
<input name="searchFor" id="searchFor" class="xp" size=20 value="#searchFor#" onChange="document.theForm.START_REC.value=1;">
<img src="#imgPath#search.gif" style="vertical-align: bottom;" onclick="showNext(0);">

$SET_PARAMETERS START_REC=1; ??!START_REC
$SET_PARAMETERS isrn=0; ??!isrn&ZZZ
$SET_PARAMETERS irpp=50;
  ??!irpp
<input type=hidden name=isrn value="#isrn#"> ??
<input type=hidden name=irpp value="#irpp#"> 
<input type=hidden name=srt value="#srt#"> 
<input type=hidden name=desc value="#desc#"> 

</td></tr><tr><td colspan=4>
Только записи в представлении справочника:
<input type=radio name=f_rec_view value=1 onclick="showNext(0);"
checked ??f_rec_view=1
> 1
<input type=radio name=f_rec_view value=2 onclick="showNext(0);"
checked ??f_rec_view=2
> 2
<input type=radio name=f_rec_view value=3 onclick="showNext(0);"
checked ??f_rec_view=3
> 3
<input type=radio name=f_rec_view value="" onclick="showNext(0);"
checked ??!f_rec_view
> все записи

</td></tr></table></td></tr>


============ Заголовки столбцов =============== ??
<tr>#TableColsHeaders#<th class="srh" sr="changed" >Обновлено</th><th class="srh" sr="sort">сортировка</th></tr>
[end]


============ Строка таблицы =============== ??
[item]
<tr onClick="ShowDialog(true); AjaxCall('popupCont','c=admin/infos/info_editRecord&record_id=#record_id#&info_id=#info_id#');"
class="pt
DELETED ??IS_DELETED=1
MANUAL ??IS_MANUAL=1&!IS_DELETED=1
">#record#
<td nowrap>#MODIFIED#</td>
<td>#SORT#</td>
</tr>
[end]

[prevLink]
<span class="pt" onClick="showNext(-1);"> << </span>
[end]

[nextLink]
<span class="pt" onClick="showNext(1);"> >> </span>
[end]

[report footer]
</table>
$INCLUDE [prevLink] ??HAS_PREV=Y
строки 
<input type=hidden name=START_REC value="#START_REC#">   ??!TOT_NUM_RECS>0
с <input size=4 class="xp center" name=START_REC value="#START_REC#">   ??TOT_NUM_RECS>0
#START_REC# ??
по #END_REC# из #TOT_NUM_RECS#  ??TOT_NUM_RECS>0
#TOT_NUM_RECS#: <b>"#searchFor#" НЕ НАЙДЕНО!</b>  ??!TOT_NUM_RECS>0
$INCLUDE [nextLink] ??HAS_NEXT=Y
</center>
<b>ОШИБКА:</b> #ERROR# ??ERROR
</div>

+++++++++ Скрипт возврата результатов в вызывавшую страницу ++++ ??
<script>
window.parent.getResult("test_result", document.getElementById("result")); 
window.parent.setModule("admin/infos/info_viewData");
--------- Отображение сортировки в заголовке таблицы ----- ??
window.parent.$("th[sr='#srt#']").removeClass("MANUAL"); 

window.parent.showSrt("#srt#","sup"); ??!desc
window.parent.showSrt("#srt#","sdown"); ??desc

</script>

[end]


==============================================================
==============================================================
==============================================================


[preSQLs]
$INCLUDE admin/infos/info_data.cfg[getInfoSQL]
;
select concat(field_db_name, ',') as FIELDS
 , concat(name, ',') as FIELDS_NAMES 
 , concat(type, ',') as FIELDS_TYPES
 , concat(is_manual, ',') as FIELDS_MANUAL
from i_fields
where info_id=#info_id#
order by nr
;
select count(field_db_name) as NUM_FIELDS, count(field_db_name)+ 2 as NUM_COLUMNS
from i_fields where info_id=#info_id#
;
[end]
