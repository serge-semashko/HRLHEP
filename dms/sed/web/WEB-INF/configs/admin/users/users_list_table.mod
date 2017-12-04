[comments]
descr=А: Модуль вывода списка пользователей (вывод самой таблицы)

input=f_nam: фильтр по фамилии или логину; f_role: фильтр по ID роли
output=HTML таблица пользователей
parents=admin/users/users_list
childs=admin/users/user_roles
testURL=?c=admin/users/users_list_table
author=Куняев
[end]


[parameters]
service=dubna.walt.service.TableServiceSpecial
request_name=A:Таблица юзеров
KeepLog=true
tableCfg=table_no
$INCLUDE dat/common.dat[rowLinks] 
[end]


[report header]
$INCLUDE [OK report header]  ??AR_ADMIN=1
[end]


[OK report header]
+++++++ сортировка таблицы по умолчанию ++++ ??
$SET_PARAMETERS execute=document.theForm.c.value='admin/users/users_list_table'; 
+++++++ начальная строка и кол-во строк на странице по умолчанию ++++ ??
$SET_PARAMETERS srn=1;  ??!srn|srn<1
$SET_PARAMETERS rpp=20; ??!rpp
$GET_DATA [get JINR div filter] ??f_div_id_ZZZ
<div id="result_table"> 
2: srt=#srt#; ??
#c# / srn=#srn#; rpp=#rpp#;  ??debug=on

$GET_DATA [check srt] ??srt
$LOG_ERROR NON_FATAL_ERROR=#NON_FATAL_ERROR# ??NON_FATAL_ERROR
$SET_PARAMETERS srt=; NON_FATAL_ERROR=; ??NON_FATAL_ERROR

<input type=hidden name="srn" value="#srn#"> 
<input type=hidden name="srt" value="#srt#">
<input type=hidden name="desc" value="#desc#">

++++++++++++++++++ Шапка таблицы пользователей +++++++++++++++++++++ ??
<table class="tlist tgreen" cellspacing=0>
<tr>
<th class="srh" sr="u.ID">id</th> ??AR_DEV=1
<th class="srh" sr="u.OLD_ID">стар.id</th> ??AR_DEV=1
<th class="srh" sr="u.F">ФИО</th>
<th class="srh" sr="u.roles">Роли</th>
<th>Права доступа</th>
<th class="srh" sr="u.DIV_CODE">Лаб.</th> 
<th>Должности</th>
<th class="srh" sr="u.LOGIN">Логин</th>
<th class="srh" sr="u.email">email, тел.</th>
<th class="srh" sr="u.changed">Изменено</th>
</tr>
[end]


[item]
++++++++++++++++++ Строка таблицы - 1 пользователь +++++++++++++++++++++ ??
<tr class="pt
oddRow ??oddRow=1&!is_deleted=1
bg_red  ??is_deleted=1
" onClick="AjaxCall('popupCont','c=admin/users/user_edit&user_person_id=#ID#')"> ??ID
" onClick="AjaxCall('popupCont','c=admin/users/user_edit&old_id=#OLD_ID#')"> ??!ID
<td class=small>#ID#</td> ??AR_DEV=1
<td class=small>#OLD_ID#</td> ??AR_DEV=1

<td class="nowrap">#USER_FIO#</td>
<td>#roles#
- ??!ID
$SET_PARAMETERS srn_sav=#srn#; srn=1; ??
$CALL_SERVICE c=admin/users/user_roles ??ID_ZZZ
$SET_PARAMETERS srn=#srn_sav#; ??
</td>
<td>
$CALL_SERVICE c=sys/ar/view_user_rules_RO; user_person_id=#ID#; 
</td>
<td>#DIV# 
<small>(#DIV_CODE#)/#view3#</small>  ??USER_ID=2309
</td> 
<td class="ellipsis small">#POSTS#</td>
<td class=small>#LOGIN#</td>
<td class=small>#email#<br>#phone#</td>
<td class=small><small>#MODIFIER#, #MODIFIED#</small></td> 
</tr>
[end]


[report footer]
<tr><td colspan=10 class="pager last">
$INCLUDE dat/common.dat[rpp] param: execute=document.theForm.c.value='admin/users/users_list_table'; ??!NumTableRows=0
<input type=hidden name="rpp" value="#rpp#"> ??NumTableRows=0
</td></tr>
</table>
</div> 

+++++++++ Скрипт возврата результатов в вызывавшую страницу ++++ ??
<script>
$INCLUDE [ajax script]  ??ajax
$INCLUDE [noajax script]  ??!ajax
</script>
[end]

[ajax script]
setModule("admin/users/users_list_table"); 
--------- Отображение сортировки в заголовке таблицы ----- ??
showSrt("#srt#","sup"); ??!desc
showSrt("#srt#","sdown"); ??desc
[end]

[noajax script]
window.parent.getResult("content_table", document.getElementById("result_table")); 
window.parent.setModule("admin/users/users_list_table"); ??
--------- Отображение сортировки в заголовке таблицы ----- ??
window.parent.showSrt("#srt#","sup"); ??!desc
window.parent.showSrt("#srt#","sdown"); ??desc
[end]
***************************** Шаблон SQL запроса ***************************

[check srt]
try: select #srt# from #table_users_full# u where 1=0
[end]


[SQL]
select u.ID, u.OLD_ID
, concat( u.F, ' ', ifnull(u.I,''), ' ', ifnull(u.O,'')) as USER_FIO
, u.is_deleted
, u.POSTS, u.DIV_CODE, d.short_name as "DIV", d.view3
, initcap(ifnull(u.roles,'')) as roles ??
, initcap(u.roles) as roles 
, u.roles ??
, u.LOGIN, u.email, u.phone 
, DATE_FORMAT(u.changed,'#dateTimeFormat#') as MODIFIED 
, concat(uu.F, ' ', left(IFNULL(uu.I,''),1), '.', left(IFNULL(uu.O,''),1),'.') as MODIFIER  
, u.modifier_id 
from 
#table_users_full# 
#table_users_full#_v  ??
u
left join #table_users_full# uu on uu.id=u.modifier_id 
left join info_11 i on i.person_id=u.PERSON_ID  ??
left join info_10 d on d.id=u.DIV_CODE

where 1=1
(u.ID is null or u.ID>0) ??
and (u.F like '#f_nam#%' or u.login like '#f_nam#%')  ??f_nam
and u.id in (select user_id from a_user_role where role_id=#f_role# and active=1) ??f_role&!f_role=Y&!f_role=N
and u.id in (select user_id from a_user_role where active=1 and not user_id is null) ??f_role=Y
and not u.id in (select user_id from a_user_role where active=1 and not user_id is null) ??f_role=N
and (u.div_code=#f_div_id# or u.id in (select user_id from a_user_role where target_type_id=1 and target_code in(#f_div_id#) and active=1) )  ??f_div_id
and u.id in(select user_id from p_user_rule)  ??f_rules
and u.is_deleted=0  ??!USER_ID=2309
order by 
#srt# #desc#, ??srt
 u.F 
[end]

and (u.posts like '%#F_LAB#%' or u.id in (select user_id from a_user_role where target_type_id=1 and target_code in(#f_div_id#) and active=1) )  ??F_LAB

[ZZZget JINR div filter]
select concat(': ', short_name, ' /') as F_LAB from info_10 where id in(#f_div_id#) ??f_div_id
[end]

