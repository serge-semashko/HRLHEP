[comments]
descr=A: Редактирование свойств шаблона workflow для типа документов. 

input=wf_template_id - ID шаблона workflow (из таблицы wf_templates_list) (для режима "edit");
type_id - ID типа документа (из таблицы d_types) (для режима "add");

mode - режим работы: "add" - создание нового шаблона workflow,
"edit" - редактирование свойств шаблона workflow с ID = wf_template_id;

cop - код операции: "create" - создание нового шаблона workflow, 
"update" - обновление свойств шаблона workflow с ID = wf_template_id;

output=HTML форма редактирования свойств шаблона workflow, 
parents=admin/wf/show_wf_template.cfg, admin/wf/admin/wf/wf_template_editor.cfg
childs=
test_URL=?c=admin/wf/edit_wf_template&mode=edit&wf_template_id=1
author=Яковлев, Куняев
[end]

[description]
Редактор свойств шаблона workflow для типа документов.<br>
<br>
Режимы работы (mode): <br>
<ul><li>"add" - вызывается в этом режиме для создания нового шаблона workflow<br>
В качестве входного параметра в это режиме требует - type_id - ID типа документа (из таблицы d_types)<br>
для которого будет создан новый шаблон.</li>
<li>"edit" - вызывается в этом режиме для редактирования свойств существующего шаблона workflow<br>
В качестве входного параметра в это режиме требует - wf_template_id - ID шаблона workflow <br>
(из таблицы wf_templates_list)</li>
</ul>
[end]


[parameters]
request_name=A: Редактирование свойств шаблона workflow для типа документов
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
$INCLUDE [do save]  ??cop=create|cop=update
$INCLUDE [form]  ??!cop=create&!cop=update
[end]

[form]
<body>
<div id="result">
#c# ??debug=on
<center>
<form name="popupForm"> ??
<form name="popupForm" method="POST" enctype="multipart/form-data" target="wf">

$GET_DATA [getWfTemplateInfoSQLs]  ??mode=edit

<input type=hidden name="wf_template_id" value="#ID#">
<input type=hidden name="type_id" value="#type_id#">
<input type=hidden name="c" value="#c#">
<input type=hidden name="cop" value="">

<table border=0 bgcolor=white cellpadding=4 style="border:solid 1px gray;">

<tr><td class=label right>ID:</td><td> <input size=5 name="f_wf_template_id" value="#ID#" readonly="readonly"> &nbsp; 
</td></tr>
<tr><td class=label>Название:</td><td> <input size=30 name="f_wf_name" 
value="Новый шаблон" ??mode=add
value="#wf_name#" ??mode=edit
> &nbsp; </td></tr>

<tr><td class=label>Коды подразделений:</td><td> <input size=30 name="f_div_ids" 
value="" ??mode=add
value="#div_ids#" ??mode=edit
> &nbsp; </td></tr>

<tr><td class=label>Описание:</td><td> <input size=40 name="f_wf_description" 
value="" ??mode=add
value="#wf_description#" ??mode=edit
> &nbsp; </td></tr>

<tr><td class=label>Активность:</td><td>
<input type=radio name=f_is_active value="1" 
checked  ??is_active=1|!is_active
>Рабочий
<input type=radio name=f_is_active value="0" 
checked  ??is_active=0
>Не рабочий
</td></tr>

</table>
<br>

<input type="button" class="butt1" style="width:100;" value="Закрыть" onClick="HideDialog();">

<input type="button" class="butt1" style="width:100;" value="Сохранить" 
onClick="document.popupForm.cop.value='create'; document.popupForm.submit(); HideDialog();"> ??mode=add
onClick="document.popupForm.cop.value='update'; document.popupForm.submit(); HideDialog();"> ??mode=edit

</form>
</div>

+++++++++ Скрипт возврата результатов в вызывавшую страницу ++++ ??
<script>
window.parent.ShowDialog(true); 
window.parent.showMsg("##dialog_title", "Создать новый шаблон workflow"); ??mode=add
window.parent.showMsg("##dialog_title", "Редактирование свойств шаблона workflow"); ??mode=edit
window.parent.centerDialog();  
</script>

</body></html>
[end]


[do save]
$GET_DATA [create wf_template] ??cop=create
$GET_DATA [update property] ??cop=update


<script>
+++++ Сабмит родительской формы. +++ ??
window.parent.AjaxCall('c_doctypes', 'c=admin/doctypes/doctype_panel&type_id=#type_id#');  ??!ERROR&cop=create
window.parent.AjaxCall("wf_template_#wf_template_id#","c=admin/wf/show_wf_template&wf_template_id=#wf_template_id#"); ??!ERROR&cop=update

alert(" Ошибка при создании нового шаблона workflow ! Проверьте целостность базы !!"); ??ERROR&cop=create
alert(" Ошибка при изменении свойств шаблона workflow !!"); ??ERROR&cop=update
</script>
[end]


***************************** Шаблон SQL запроса ***************************
[getWfTemplateInfoSQLs] ******* Запрашивает свойства шаблона
select wftpls.ID
, wftpls.doc_type_id
, wftpls.wf_name
, wftpls.div_ids
, wftpls.wf_description
, wftpls.is_active 
from wf_templates_list wftpls
where wftpls.id = #wf_template_id#
;
[end]

[create wf_template] ******* Создает новый шаблон
insert wf_templates_list (doc_type_id, wf_name, div_ids, wf_description
, is_active, modified, modifier_id) 
values ('#type_id#', '#f_wf_name#', '#f_div_ids#', '#f_wf_description#'
, '#f_is_active#', now(), #USER_ID#)
;
[end]


[update property] ******* Обновляет свойства шаблона
update wf_templates_list set
  wf_name = '#f_wf_name#'
, div_ids = '#f_div_ids#'
, wf_description = '#f_wf_description#'
, is_active = '#f_is_active#'
, modified = now()
, modifier_id = #USER_ID#

where wf_templates_list.Id = #wf_template_id#
;
[end]
