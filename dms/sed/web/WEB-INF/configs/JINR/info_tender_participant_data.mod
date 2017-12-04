JINR/info_tender_participant_data.mod

[comments]
descr=Модуль вывода данных справочника участников закупочных конкурсов (непосредственно вывод таблицы)
input=requesterId - ID элемента для результата, info_id - ID справочника; view - № представления. По умолчанию - 1; searchFor - строка поиска.
output=html-таблица заданного представления справочника. По клику возвращает 1-е (код записи или ID) и 2-е (текст) поля представления. 
parents=JINR/info_tender_participant.cfg
childs=
testURL=?c=JINR/info_BC_data&info_id=1005&view=1
author=Куняев
[end]

[description]
Вызывается через Post формы из JINR/info_tender_participant.cfg
[end]

[parameters]
request_name=U:вывод данных справочника участников закупочных конкурсов
service=dubna.walt.service.TableServiceSpecial
tableCfg=table_no
KeepLog=true
ClearLog=false
info_src=false
[end]



[report header]

$GET_DATA [get num records]

$SET_PARAMETERS callback=pasteInfoResult; ??!callback
<div id="result_table">
info_src=#info_src#; ??
<small>c=JINR/info_tender_participant_data</small> ??debug=on
<center>
============ Начало таблицы =============== ??
irpp=#irpp#; isrn=#isrn#; ??
<table border=0 class="tlist dd_info" id = "#requesterId#_info_table" cellpadding=0 cellspacing=0>
<tr>
<th>Id</th><th>Название</th><th>ИНН</th><th>Контактное лицо</th></tr>
[end]


============ Строка таблицы =============== ??
[item]

$SET_PARAMETERS returnValue=#returnValue#  ??
<tr class="pt" onClick="selectInfoItem(this,'#returnValue#'); hideSprav();"
info_id=#info_id# view=3 returnId="#CODE#" recordId="#CODE#"
>
<td class="marked-cell"></td> ??
<td>#CODE#</td>
<td>#NAME#</td>
<td>#INN#</td>
<td>#CONTACT_PERSON#</td>
</tr>
[end]

[prevLink]
<span class="pt" onClick="showNext(-1);"> << </span>
[end]

[nextLink]
<span class="pt" onClick="showNext(1);"> >> </span>
[end]


[report footer]
<tr><td colspan=3 class="divider"></td><td colspan=2 class="divider">
$SET_PARAMETERS HAS_PREV=Y; ??START_REC>1
$SET_PARAMETERS HAS_NEXT=Y; ??END_REC<#TOT_NUM_RECS#
$INCLUDE [prevLink] ??HAS_PREV=Y
строки #START_REC#-#END_REC# из #TOT_NUM_RECS#  ??TOT_NUM_RECS>0
#TOT_NUM_RECS#: <b>Нет данных</b>  ??!TOT_NUM_RECS>0
$INCLUDE [nextLink] ??HAS_NEXT=Y
</center>
<b>ОШИБКА:</b> #ERROR# ??ERROR
</td></tr>
</table>
</div>

<script type="text/javascript">
window.parent.getResult("info_result_data", document.getElementById("result_table"));

</script>
[end]


==============================================================
==============================================================
==============================================================

[preSQLs]
select #START_REC#-1 as "isrn", #START_REC#+#irpp#-1 as END_REC
;
[end]

[get num records]
select count(*) as TOT_NUM_RECS
$INCLUDE [criteria]
[end]

[SQL]
select tp.id as "CODE", tp.name as "NAME"
    , replace(tp.name,'"','``') as "returnValue"
    , tp.inn as "INN", tp.contact_person as "CONTACT_PERSON" 
$INCLUDE [criteria]
order by tp.name
LIMIT #isrn#,#irpp#
[end]

[criteria]
from i_jinr_tender_participant tp
where 1=1
and tp.name like '%#f_name#%'  ??f_name
and tp.inn = '#f_INN#' ??f_INN
    
[end]



[criteria_]
from i_jinr_tender_participant tp
left join info_10 l on l.id=bc.div_code
where 1=1
and bc.id<400 ??
and div_code=#f_bc_div_id# ??f_bc_div_id&!f_bc_div_id=900000
and div_code=990000 ??f_bc_div_id=900000
and resp like '%#f_resp#%' ??f_resp
and sbj=#f_sbj#  ??f_sbj
and bc.id=#f_code#  ??f_code
and des like 'пр.#f_prikaz# от %' ??f_prikaz
and prikaz_n is null  ??is_prikaz=n
and not prikaz_n is null  ??is_prikaz=y
and dir='Внебюджет'  ??is_prikaz=v
and project_id=#f_bc_project_id#  ??f_bc_project_id
[end]

