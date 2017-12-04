admin/monitor/all_docs.mod

[comments]
descr=U: Модуль выбора модуля вывода списка документов по типу документа во вкладке Мониторинг

input=
output=HTML таблица документов
parents=tab_monitor
childs=admin/monitor/all_docs_table
testURL=?c=admin/monitor/all_docs
author=Куняев, Яковлев
[end]


[parameters]
request_name=U:Таблица документов
NumTableCols=7
contentLength=100000 ??
LOG=ON
[end]


[report]
    $INCLUDE dat/common.dat[check login]
    $INCLUDE [report_] ??USER_ID
    $SET_PARAMETERS doc_id=;  
[end]


[report_]
    $SET_PARAMETERS srt=-dh.modified; desc=; ??!srt  
    $SET_PARAMETERS srn=1; rpp=20;  ??!srn

    $GET_DATA admin/monitor/all_docs[getFilters]
    $SET_PARAMETERS monitor_table=admin/monitor/all_docs_table; ??!monitor_table

    $CALL_SERVICE c=#monitor_table#;

    <tr><td colspan=#NumTableCols# class="pager last">
    $INCLUDE dat/common.dat[rpp]  ??!NumTableRows=0
    <input type=hidden name="rpp" value="#rpp#"> ??NumTableRows=0
    </td></tr>
    </table>
    </div>

    $INCLUDE admin/monitor/all_docs[set doc type filter]
[end]


[set doc type filter]
<script type="text/javascript">
    selectOptionByVal(document.theForm.f_doc_type_id,'#f_doc_type_id#'); ??f_doc_type_id
    showSrt("#srt#","sup"); ??!desc
    showSrt("#srt#","sdown"); ??desc
</script>
[end]


[filters table start]
+++++ Фильтры +++++ ??
<div id="filters_div">
<input type=hidden name="srn" value="#srn#"> 
<input type=hidden name="srt" value="#srt#">
<input type=hidden name="desc" value="#desc#">

<table border=0 cellpadding=5 style="margin:20px 0 20px 100px; border:solid 1px white;">

<tr><td class="label">
$INCLUDE dat/common.dat[f_year]
 &nbsp; № документа: </td><td>
 <input size="7" name="f_doc_number" value="#f_doc_number#">

Тип документа: <select name=f_doc_type_id class=norm 
onChange='resetSort(); AjaxCall("all_docs", "c=admin/monitor/all_docs", true, "filters_div");' ??
onChange="submitForm(true);"
><option value="">все</option>#DOC_TYPES_LIST#</select>

$SET_PARAMETERS AR_SYS_ADMIN=1;  ??USER_ID=10473
$GET_DATA admin/monitor/all_docs[det admin div] ??!AR_SYS_ADMIN=1
Подразделение: <b>#DIV#</b><input type=hidden value="#f_div_id#" name="f_div_id">
$INCLUDE admin/monitor/all_docs[all divs filter]  ??AR_SYS_ADMIN=1

</td></tr>

$SET_PARAMETERS f_mon_status=#f_mon_status#;
<tr><td class=label style="white-space:nowrap;" nowrap>
id: <input size="7" name="f_doc_id" value="#f_doc_id#"> &nbsp; ??USER_ID=2309

Статус:</td><td>
<input type=radio name=f_mon_status value="1" 
checked ??f_mon_status=1|!f_mon_status
onClick="submitForm(true);">в процессе согласования
<input type=radio name=f_mon_status value="2" 
checked  ??f_mon_status=2
onClick="submitForm(true);">на этапе завершения
<input type=radio name=f_mon_status value="3" 
checked  ??f_mon_status=3
onClick="submitForm(true);">завершенные
<input type=radio name=f_mon_status value="0" 
checked  ??f_mon_status=0
onClick="submitForm(true);">черновики
<input type=radio name=f_mon_status value="-1" 
checked  ??f_mon_status=-1
onClick="submitForm(true);">удалённые
<input type=radio name=f_mon_status value="-2" 
checked  ??f_mon_status=-2
onClick="submitForm(true);">все
</td></tr>

[end]

[all divs filter]
<input type=hidden value="#f_div#" name="f_div">
<div id="f_div" class="info_input pt big bg_white" style="display: inline-block; width:100px; height:20px; border:solid 1px gray; " info_view="11" info_id="10">
все ??!f_div
#f_div#
</div>
[end]

[filters table end]
++++++++++++++++++ кнопка +++++++++++++++++++++ ??
<tr><td class="label">
Искать текст:</td><td><input size=40 name="f_search" value="#f_search#"> &nbsp; 
<div style="float:right;">
<input type="submit" class="butt1 pt" style="width:100;" value="ОК" 
onClick='AjaxCall("all_docs", "c=admin/monitor/all_docs", true, "filters_div");' ??
>
</div>
</td></tr></table>
<div style="clear:both;"></div>
[end]


[getFilters]
select concat('<option value=', cast(id as char), '>', name, '</option>') as DOC_TYPES_LIST
from d_types where is_active = 1 and group_id<98 and group_id>0
order by sort
;
[end]

[det admin div]
    select target_code as "f_div_id", i.short_name as "DIV"
    from a_user_role r left join info_10 i on i.id=r.target_code
    where user_id=#USER_ID# and role_id=14 and active=1
[end]


[preSQLs]
select monitor_table from d_types where id=#f_doc_type_id#  ??f_doc_type_id
[end]


[criteria]
left join d_types dtp on dtp.Id = dh.type_id
left join #table_users_full# ucr on ucr.id=dh.creator_id 
left join #table_users_full# um on um.id=dh.modifier_id 

where dh.ID>0 
and dh.ID=#f_doc_id# ??f_doc_id
and extract(year from dh.created)=#f_year# ??f_year&!f_year=ALL
and dtp.group_id<99
and dh.NUMBER like '#f_doc_number#%' ??f_doc_number
and dh.status in(#f_mon_status#) ??f_mon_status&!f_mon_status=-1&!f_mon_status=-2
and dh.is_deleted=0 ??!f_mon_status=-1
 ??f_deleted
and dh.is_deleted=1 ??f_mon_status=-1
and dh.IS_CONFIDENTIAL=0

and dh.id in(select doc_id from d_divs where div_id in(#f_div_id#)) ??f_div_id

and dh.NUMBER like '#f_doc_number#%' ??f_doc_number
and dh.TYPE_ID = #f_doc_type_id# ??f_doc_type_id
and dh.creator_id=#f_initiator_id# ??f_initiator_id

and dh.TITLE like '#f_doc_title#%' ??f_doc_titleZZZ
and dh.id in(select doc_id from d_index where context like '%#f_search#%') ??f_search

$INCLUDE admin/monitor/all_docs[access_filter] ??!FULL_READ_ACCESS=Y

[end]

  ??&!AR_SYS_ADMIN=1

and dh.id in(select doc_id from d_divs where div_id in(#A_LAB_CODES#)) ??AR_LAB_ADMIN


[access_filter]
and (
dh.creator_id=#USER_ID# or 
dh.id in(select doc_id from p_permits where user_id=#USER_ID#)  )
[end]

