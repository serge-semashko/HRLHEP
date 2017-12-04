[comments]
descr=A: Редактирование свойств шага шаблона workflow для типа документов. 

input=step_id - ID шага шаблона workflow (из таблицы wf_templates) (для режимов "edit" и "del");
wf_template_id - ID шаблона workflow (из таблицы wf_templates_list) (для режима "add");
prev_step - номер предыдущего шага в workflow (для режима "add");

mode - режим работы: "add" - создание нового шага шаблона workflow,
"edit" - редактирование свойств шага шаблона workflow с ID = step_id;
"del" - удаление шага шаблона workflow с ID = step_id;

cop - код операции: "create" - создание нового шага шаблона workflow, 
"update" - обновление свойств шага шаблона workflow с ID = step_id;
"delete" - удаление шаблона workflow с ID = step_id;

output=HTML форма редактирования свойств шага шаблона workflow, 
parents=admin/wf/show_wf_template.cfg, admin/wf/wf_template_editor.cfg
childs=
test_URL=?c=admin/wf/edit_wf_template_step&mode=edit&step_id=1
author=Яковлев, Куняев
[end]

[description]
Редактор свойств шага шаблона workflow для типа документов.<br>
<br>
Режимы работы (mode): <br>
<ul><li>"add" - вызывается в этом режиме для создания нового шага шаблона workflow<br>
В качестве входных параметров в этом режиме требует<br>
 - type_id - wf_template_id - ID шаблона workflow (из таблицы wf_templates_list)<br>
 - prev_step - номер предыдущего шага в workflow</li>
<li>"edit" - вызывается в этом режиме для редактирования свойств существующего шага шаблона<br>
В качестве входного параметра в этом режиме требует - step_id - ID шага шаблона workflow <br>
(из таблицы wf_templates)</li>
<li>"del" - вызывается в этом режиме для удаления существующего шага шаблона<br>
В качестве входного параметра в этом режиме требует - step_id - ID шага шаблона workflow <br>
(из таблицы wf_templates)</li>
</ul>
[end]


[parameters]
request_name=A: Редактирование свойств шага шаблона workflow для типа документов
tableCfg=table_no ??
KeepLog=false
ClearLog=true

divider=<tr><td colspan=2 class="bg_white" style="height:10px; border-top:solid 1px gray;"></td></tr>
[end]

[report]
++++ временно - открыто всем. Потом - проверить R и RW права пользователя +++ ??
$SET_PARAMETERS RWACC=Y; RACC=Y;
$INCLUDE [report_]  ??RACC
[end]


[report_]
$INCLUDE [do save]  ??cop=create|cop=update|cop=delete
$INCLUDE [form]  ??!cop=create&!cop=update&!cop=delete

[end]

[form]
<body>
<div id="result">
#c# ??debug=on
<center>
<form name="popupForm"> ??
<form name="popupForm" method="POST" enctype="multipart/form-data" target="wf">

$GET_DATA [getWfTemplateStepInfoSQLs]  ??mode=edit|mode=del
$GET_DATA [getWfTemplateInfoSQLs]

<input type=hidden name="step_id" value="#ID#">
<input type=hidden name="wf_template_id" value="#wf_template_id#">
<input type=hidden id="prev_step" name="prev_step" value="#prev_step#">
<input type=hidden id="step_num" name="step_num" value="#step#">

<input type=hidden id="step_create_add_parallel" name="step_create_add_parallel" value="#step_create_add_parallel#">

<input type=hidden name="c" value="#c#">
<input type=hidden name="cop" value="">

<table border=0 bgcolor=white cellpadding=4 style="border:solid 1px gray;">
<tr><td class=right>Шаблон workflow:</td><td> <b> #wf_name# </b> &nbsp; 
</td></tr>


$INCLUDE [step_ID_add]  ??mode=add
$INCLUDE [step_ID_edit_del]  ??mode=edit|mode=del

</table>

<table border=0 bgcolor=white cellpadding=4 style="border:solid 1px gray;">
<tr><td class=label right>Роль:</td><td> 
$INCLUDE [ROLE div] ??mode=add|mode=edit
#ROLE_NAME# ??mode=del
</td></tr>

<tr><td class=label right>Тип цели:</td><td> 
$INCLUDE [TARGET_TYPE div] ??mode=add|mode=edit
#TARGET_TYPE# ??mode=del
</td></tr>

<tr><td class=label right>Пользователь:</td><td> 
$INCLUDE [USER div] ??mode=add|mode=edit
#USER_IOF# ??mode=del
</td></tr>

<tr><td class=label>Тип шага:</td><td>
$INCLUDE [step_type add_edit] ??mode=add|mode=edit
$INCLUDE [step_type del] ??mode=del
</td></tr>

<tr><td class=label>Критерий завершения шага:</td><td>
$INCLUDE [criteria add_edit] ??mode=add|mode=edit
$INCLUDE [criteria del] ??mode=del
</td></tr>

<tr><td class=label>Cпецифические критерии включения шага <br> в зависимости от данных документа:</td><td> 
$INCLUDE [custom_criteria add_edit] ??mode=add|mode=edit
#custom_criteria# ??mode=del
</td></tr>

<tr><td class=label>Действие с номером документа:</td><td>
$INCLUDE [set_number add_edit] ??mode=add|mode=edit
$INCLUDE [set_number del] ??mode=del
</td></tr>

<tr><td class=label>Послать док в шлюз по завершении шага:</td><td>
$INCLUDE [post_doc add_edit] ??mode=add|mode=edit
$INCLUDE [post_doc del] ??mode=del
</td></tr>
</table>
<br>

<h3><font color="red">Удалить шаг ?</font></h3><br><br>  ??mode=del

<input type="button" class="butt1" style="width:100;" value="Отмена" onClick="HideDialog();">

$INCLUDE [save_button_add] ??mode=add
$INCLUDE [save_button_edit] ??mode=edit
$INCLUDE [delete_button] ??mode=del

</form>
</div>

+++++++++ Скрипт возврата результатов в вызывавшую страницу ++++ ??
<script>
window.parent.ShowDialog(true); 
window.parent.showMsg("##dialog_title", "Создать новый шаг шаблона workflow"); ??mode=add
window.parent.showMsg("##dialog_title", "Редактирование свойств шага шаблона workflow"); ??mode=edit
window.parent.showMsg("##dialog_title", "Удаление шага шаблона workflow"); ??mode=del
window.parent.centerDialog();  
</script>

</body></html>
[end]


[step_ID_add] ******* Вызывается в режиме создания шага
$INCLUDE [step_number_scripts]

<tr>

$INCLUDE [prev_step_zero]  ??prev_step=0
$INCLUDE [prev_step_not_zero]  ??prev_step>0


<td> 
Порядковый номер шага:
<input size=5 id="f_step" name="f_step" 
readonly="readonly">
</td></tr>
[end]


[prev_step_zero] ******* Вызывается, если предыдущий номер шага = 0
$INCLUDE [set_Create_New_Step]
[end]

[prev_step_not_zero] ******* Вызывается, если предыдущий номер шага > 0
$INCLUDE [set_Create_New_Step]
<td>

<input type=radio name=f_step_mode value="-1" 
checked ??f_step_mode=-1|!f_step_mode
onClick="setCreateNewStep();">Вставить новый шаг
<br>

<input type=radio name=f_step_mode value="1" 
checked ??f_step_mode=1
onClick="setAddParallelStep();">Добавить параллельный шаг

</td>

[end]


[step_ID_edit_del] ******* Вызывается в режиме редактирования/удаления шага
<tr><td class=right>Порядковый номер шага:</td><td> 
<b>#step#</b>
 &nbsp; 
ID: 
<b>#ID#</b> &nbsp; 
</td></tr>

[end]



[step_number_scripts]
<script type="text/javascript">
function setCreateNewStep() {
    var next_step = +($('#prev_step').val())+1;

    $('#f_step').val(next_step);
    $('#step_create_add_parallel').val(0);
}

function setAddParallelStep() {
    var next_step = +($('#prev_step').val())+1;
    var curr_step = ($('#prev_step').val());

    $('#f_step').val(curr_step);
    $('#step_create_add_parallel').val(1);
}

</script>
[end]


[set_Create_New_Step]
<script type="text/javascript">
    var next_step = +($('#prev_step').val())+1;

    $('#f_step').val(next_step);
    $('#step_create_add_parallel').val(0);
</script>
[end]


[ROLE div]
$SET_PARAMETERS LX=350;
$SET_PARAMETERS LY=20;
$GET_DATA [get info type]

<input type=hidden name="ROLE_for_wf_id" id="ROLE_for_wf_id" size=5 value="#ROLE_ID#">
<input type=hidden name="ROLE_for_wf" id="ROLE_for_wf_txt" size=50 value="">
<div class="info_input pt bg_white" id="ROLE_for_wf" info_id="1" info_view="11" info_type="3" searchFor='none' 
style="display: inline-block; width:#LX#px; height:#LY#px; border:solid 1px gray;">#ROLE_NAME#
выбрать ??!ROLE_NAME
</div>
[end]

[TARGET_TYPE div]
$SET_PARAMETERS LX=200;
$SET_PARAMETERS LY=20;
$GET_DATA [get info type]

<input type=hidden name="TARGET_TYPE_for_wf_id" id="TARGET_TYPE_for_wf_id" size=5 value="#TARGET_TYPE_ID#">
<input type=hidden name="TARGET_TYPE_for_wf" id="TARGET_TYPE_for_wf_txt" size=50 value="">
<div class="info_input pt bg_white" id="TARGET_TYPE_for_wf" info_id="2" info_view="11" info_type="3" searchFor='none' 
style="display: inline-block; width:#LX#px; height:#LY#px; border:solid 1px gray;">#TARGET_TYPE#
выбрать ??!TARGET_TYPE
</div>
[end]

[get info type]
select view, MULTI, editable  from infos_views where id=11
[end]


[USER div]
<div id = "USER_for_wf_list">
$INCLUDE [selected_USER]   ??SELECTED_USER_ID
</div>
<input type=hidden id="USER_for_wf_id" name="USER_for_wf_id" size=3 value="#SELECTED_USER_ID#">
<input type=hidden id="USER_for_wf_text" name="USER_for_wf_text" size=20 value="">
<span class="info_input" id="USER_for_wf" info_id="5" searchFor='none' info_view="1">выбрать</span> 
[end]

[selected_USER]
<ul class="p0"><li class="nobull">#USER_IOF#
<a class="delcross" title="Удалить" onclick="delSelectedInfoItemFromChoice(this)" delfromtext="USER_for_wf_text" delval="val_id" delfrom="USER_for_wf_id">✖</a>
</li></ul>
[end]


[step_type add_edit]
<input type=radio name=f_step_type value="#~wf_step_in_progress#" 
checked  ??step_type=#~wf_step_in_progress#|!step_type
>Согласование
<br>
<input type=radio name=f_step_type value="#~wf_step_signed#" 
checked  ??step_type=#~wf_step_signed#
>Финальная подпись (Утверждение)
<br>
<input type=radio name=f_step_type value="#~wf_step_process#" 
checked  ??step_type=#~wf_step_process#
>Завершение обработки документа
<br>
<input type=radio name=f_step_type value="#~wf_step_information#" 
checked  ??step_type=#~wf_step_information#
>Информирование
<br>
<input type=radio name=f_step_type value="#~wf_step_preparation#" 
checked  ??step_type=#~wf_step_preparation#
>Подготовка документа
[end]

<br>
<input type=radio name=f_step_type value="#~wf_step_1C#" 
checked  ??step_type=#~wf_step_1C#
>Занесение в 1С

[step_type del]
(согласование) ??step_type=#~wf_step_in_progress#
(утверждение) ??step_type=#~wf_step_signed#
(завершение) ??step_type=#~wf_step_process#
(информирование) ??step_type=#~wf_step_information#
(подготовка) ??step_type=#~wf_step_preparation#
[end]


[criteria add_edit]
<input type=radio name=f_criteria value="100" 
checked  ??criteria=100|!criteria
>Согласуют все перечисленные в списке
<br>
<input type=radio name=f_criteria value="1" 
checked  ??criteria=1
>Согласует только один (любой) из списка
[end]

[criteria del]
Согласуют все перечисленные в списке  ??criteria=100
Согласует только один (любой) из списка  ??criteria=1
[end]


[custom_criteria add_edit]
<input size=40 name="f_custom_criteria" 
value="" ??mode=add
value="#custom_criteria#" ??mode=edit
> &nbsp; 
[end]


[set_number add_edit]
<input type=radio name=f_set_number value="0" 
checked  ??set_number=0|!set_number
>Ничего
<br>
<input type=radio name=f_set_number value="1" 
checked  ??set_number=1
>Установить при старте шага
<br>
<input type=radio name=f_set_number value="2" 
checked  ??set_number=2
>Показать кнопку "Зарегистрировать"
<br>
<input type=radio name=f_set_number value="3" 
checked  ??set_number=3
>Ввести номер вручную
[end]

[set_number del]
Ничего  ??set_number=0
Установить при старте шага  ??set_number=1
Показать кнопку "Зарегистрировать"  ??set_number=2
Ввести номер вручную  ??set_number=3
[end]


[post_doc add_edit]
<input type=radio name=f_post_doc value="0" 
checked  ??post_doc=0|!post_doc
>Нет
<input type=radio name=f_post_doc value="1" 
checked  ??post_doc=1
>Да
[end]

[post_doc del]
Нет  ??post_doc=0
Да  ??post_doc=1
[end]


[save_button_add]
<input type="button" class="butt1" style="width:100;" value="Сохранить" 
onClick="document.popupForm.cop.value='create'; document.popupForm.submit(); HideDialog();">
[end]

[save_button_edit]
<input type="button" class="butt1" style="width:100;" value="Сохранить" 
onClick="document.popupForm.cop.value='update'; document.popupForm.submit(); HideDialog();">
[end]

[delete_button]
<input type="button" class="butt1" style="width:100;" value="Удалить" 
onClick="document.popupForm.cop.value='delete'; document.popupForm.submit(); HideDialog();">
[end]


[do save]
$GET_DATA [create wf_template_step] ??cop=create
$GET_DATA [update property] ??cop=update
$GET_DATA [delete wf_template_step] ??cop=delete

<script>
+++++ Сабмит родительской формы. +++ ??
window.parent.AjaxCall('wf_template_step_table_#wf_template_id#', 'c=admin/wf/wf_template_step_editor&wf_template_id=#wf_template_id#');  ??!ERROR&cop=create
window.parent.AjaxCall('wf_template_step_table_#wf_template_id#', 'c=admin/wf/wf_template_step_editor&wf_template_id=#wf_template_id#'); ??!ERROR&cop=update
window.parent.AjaxCall('wf_template_step_table_#wf_template_id#', 'c=admin/wf/wf_template_step_editor&wf_template_id=#wf_template_id#'); ??!ERROR&cop=delete

alert(" Ошибка при создании нового шага шаблона workflow ! Проверьте целостность базы !!"); ??ERROR&cop=create
alert(" Ошибка при изменении свойств шага шаблона workflow !!"); ??ERROR&cop=update
alert(" Ошибка при удалении шага шаблона workflow !!"); ??ERROR&cop=delete
</script>
[end]


***************************** Шаблон SQL запроса ***************************

[getWfTemplateStepInfoSQLs]
select wt.ID
, wt.wf_template_id
, wt.step
, wt.step_type
, wt.criteria
, wt.custom_criteria
, wt.set_number
, wt.post_doc
, wt.role_id as "ROLE_ID", r.name as "ROLE_NAME"
, wt.role_target_type_id as "TARGET_TYPE_ID", tt.type as "TARGET_TYPE"
, wt.user_id as "SELECTED_USER_ID", iof(u.F, u.I, u.O) as "USER_IOF"
,i.name as "INFO_NAME", i.id as "INFO_ID"
from wf_templates wt
left join a_roles r on r.id=wt.role_id
left join a_target_types tt on tt.id=wt.role_target_type_id
left join #table_users_full# u on u.id=wt.user_id
left join i_infos i on i.id = tt.info_id
where wt.id = #step_id#
;
[end]

[getWfTemplateInfoSQLs]
select wftpls.wf_name
from wf_templates_list wftpls
where wftpls.id = #wf_template_id#
;
[end]


[create wf_template_step] ******* Создает новый шаг шаблона
START TRANSACTION
;

$INCLUDE [push the step]   ??step_create_add_parallel=0

insert wf_templates (wf_template_id, step, user_id, role_id, role_target_type_id
, step_type, criteria, custom_criteria, set_number, post_doc, modified, modifier_id)
values ('#wf_template_id#', '#f_step#'

, '#USER_for_wf_id#' ??USER_for_wf_id
, NULL ??!USER_for_wf_id

, '#ROLE_for_wf_id#' ??ROLE_for_wf_id
, NULL ??!ROLE_for_wf_id

, '#TARGET_TYPE_for_wf_id#' ??TARGET_TYPE_for_wf_id
, NULL ??!TARGET_TYPE_for_wf_id

, '#f_step_type#', '#f_criteria#', '#f_custom_criteria#'
, '#f_set_number#', '#f_post_doc#', now(), #USER_ID#)
;

COMMIT;   ??!ERROR
ROLLBACK; ??ERROR

[end]

[push the step] ******* "Раздвигает" шаги - увеличивает порядок всех последующих шагов на 1
update wf_templates set
step = step + 1
where wf_templates.wf_template_id = #wf_template_id#
and step >= '#f_step#'
;
[end]

[update property] ******* Обновляет свойства шага
update wf_templates set

 user_id = '#USER_for_wf_id#' ??USER_for_wf_id
 user_id = NULL ??!USER_for_wf_id

, role_id = '#ROLE_for_wf_id#' ??ROLE_for_wf_id
, role_id = NULL ??!ROLE_for_wf_id

, role_target_type_id = '#TARGET_TYPE_for_wf_id#' ??TARGET_TYPE_for_wf_id
, role_target_type_id = NULL ??!TARGET_TYPE_for_wf_id

, step_type = '#f_step_type#'
, criteria = '#f_criteria#'
, custom_criteria = '#f_custom_criteria#'
, set_number = '#f_set_number#'
, post_doc = '#f_post_doc#'
, modified = now()
, modifier_id = #USER_ID#
where wf_templates.Id = #step_id#
;
[end]


[delete wf_template_step] ******* Удаляет шаг шага
START TRANSACTION
;
select COUNT(wf_templates.ID) as "CUR_STEP_COUNT"
from wf_templates
where wf_templates.wf_template_id = #wf_template_id#
and wf_templates.step = #step_num#
and wf_templates.id <> #step_id#
;
delete from wf_templates
where wf_templates.id = #step_id#
;

update wf_templates set
wf_templates.step = wf_templates.step - 1
where wf_templates.wf_template_id = #wf_template_id#
and wf_templates.step > '#step_num#'
and '#CUR_STEP_COUNT#' = 0
;

COMMIT;   ??!ERROR
ROLLBACK; ??ERROR

[end]

