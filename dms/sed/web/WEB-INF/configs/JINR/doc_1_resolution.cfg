JINR/doc_1_resolution

[comments]
descr=U: форма ввода резолюции директора по документу
input=doc_id - ID документа
output=Pop-up окно действия
parents=JINR/doc_1_settings.cfg
childs=
[end]

[parameters]
request_name=U:резолюция директора
LOG=ON
[end]

[report]
$CALL_SERVICE c=sys/getARUD
$SET_PARAMETERS AR_S=Y; ??USER_ID=2309|USER_ID=2645
$INCLUDE [OK report]  ??AR_S=Y
$CALL_SERVICE c=sys/log_doc_access; access_type=4; rejected=1;   ??!AR_S=Y
[end]


[OK report]
$SET_PARAMETERS result_code=#~doc_action_signed#; ??

$INCLUDE [process doc resolution]   ??doIt=Y
$GET_DATA JINR/doc_1_show_resolution.cfg[get resolution]    ??!doIt=Y
$INCLUDE [form] ??!doIt=Y|ERROR|INPUT_ERROR
[end]


[form]
<div id="result">  ??!ajax

=================== Форма ввода данных для действия с документом ==============??
<form name="popupForm" method="POST" enctype="multipart/form-data" target="wf" autocomplete="off">
<input type=hidden name="c" value="JINR/doc_1_resolution">
<input type=hidden name="cop" value="#~doc_action_signed#">
<input type=hidden name="doIt" value="Y">
<input type=hidden name=doc_id value='#doc_id#'>

<center><table border=0 cellpadding=5 cellspacing=0>
<tr><td colspan=2 class="big center">
Уважаемый ??!user_sex=Ж
Уважаемая ??user_sex=Ж
#user_I# #user_O#,</td></tr>

<tr><td class="label big
error ??!resolution&INPUT_ERROR
">(*) Выберите Вашу резолюцию<br> по этой заявке:</td><td>
<input type=radio name=resolution value="1"
checked ??resolution=1
>продолжить оформление с подписанием договора директором<br>
<input type=radio name=resolution value="2"
checked ??resolution=2
>продолжить оформление с подписанием договора вице-директором<br>
<input type=radio name=resolution value="3"
checked ??resolution=3
>вернуть на доработку<br>
<input type=radio name=resolution value="4"
checked ??resolution=4
>другое<br>
</td></tr>

$INCLUDE docs/custom_settings.cfg[set custom parameters] ??
$INCLUDE #CUSTOM_DECISION#  ??CUSTOM_DECISION_ZZZ

<tr><td class="label big"> можно ввести комментарий:</td>
<td><textarea name=resolution_text rows=2 cols=60>#resolution_text#</textarea></td></tr>

$INCLUDE dat/common.dat[pw2]
$INCLUDE [msg box] ??INPUT_ERROR
 
<tr><td colspan=2 class=center>
<input type="button" class="butt1" style="width:120;" value="Отмена" onClick="HideDialog();">&nbsp; &nbsp;
<input type="button" class="butt1" style="width:120;" value="Сохранить" onClick="document.popupForm.submit();"> 
</td></tr>
</table>
$INCLUDE [messages]
</div>   ??!ajax

$INCLUDE [ajax script]   ??ajax
$INCLUDE [script]   ??!ajax
[end]


[ajax script]
<script type="text/javascript">
showMsg("##dialog_title", "Резолюция по заявке");
ShowDialog(true);
centerDialog(); 
</script>
[end]

[script]
<script type="text/javascript">
window.parent.showMsg("##popupCont");
window.parent.showMsg("##dialog_title", "Резолюция по заявке");
window.parent.getResult("popupCont", document.getElementById("result"));
window.parent.ShowDialog(true);
window.parent.centerDialog(); 
</script>
[end]



[msg box]
<tr><td colspan=2 class="center gray_border" nowrap>
Ошибка выполнения операции: ??ERROR|INPUT_ERROR
<div style="margin:10px;" class=
"small" ??!INPUT_ERROR
"error" ??INPUT_ERROR
>

<b>#INPUT_ERROR#</b> ??INPUT_ERROR
<i>поля, отмеченные (*), обязательны для заполнения!</i> ??
</div>

<br><small>#ERROR#</small> ??ERROR_ZZZ
</td></tr>
[end]

=================== РЕЗОЛЮЦИЯ - обработка ===========

[process doc resolution]   ******* Сохранение резолюции
$GET_DATA docs/doc_user_action_process.cfg[check user PW] ??authPW
$SET_PARAMETERS INPUT_ERROR=Неверный пароль; ??!PW_OK
$SET_PARAMETERS INPUT_ERROR=Введите пароль для подписи; ERROR=Введите пароль для подписи;  ??!authPW
$SET_PARAMETERS INPUT_ERROR=Выберите резолюцию; ERROR=Выберите резолюцию;  ??!resolution
$GET_DATA [save resolution]  ??!ERROR&!INPUT_ERROR
<script type="text/javascript">
window.parent.HideDialog();   ??!ERROR&!INPUT_ERROR
window.parent.AjaxCall("resolution_workdiv","c=JINR/doc_1_show_resolution&doc_id=#doc_id#", true);   ??!ERROR&!INPUT_ERROR
</script>
[end]


[save resolution]
update #DOC_DATA_TABLE# set 
resolution=#resolution#,  resolution_text='#resolution_text#'  
,resolution_date=now(),  resolution_user_id=#USER_ID#  
where doc_id=#doc_id# and version=#DOC_VERSION# 
[end]

