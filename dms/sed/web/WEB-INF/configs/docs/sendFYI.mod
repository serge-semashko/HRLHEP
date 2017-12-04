[comments]
descr=U: форма подготовки посылки FYI.

input=doc_id - ID документа

output=Pop-up окно с формой
parents=docs/view_doc.cfg
childs=docs/doc_user_action_process.cfg
test_URL=?c=docs/sendFYI&doc_id=1
[end]

[parameters]
request_name=U:посылка документа
LOG=OFF
[end]

[report]
$CALL_SERVICE c=sys/getARUD;   ??!AR_R=Y&!AR_A=Y
$INCLUDE [OK report]  ??AR_A=Y|AR_R=Y
[end]


[OK report]
$SET_PARAMETERS result_code=#~doc_action_inform#; ??!result_code

$SET_PARAMETERS step_id=0;   

$CALL_SERVICE c=docs/sendFYI_process.cfg ??doIt=Y
$INCLUDE [form] ??!doIt=Y|ERROR|INPUT_ERROR
[end]

[form]
<div id="result">  ??!ajax
#INPUT_ERROR# ??
<small>#c#</small> ??debug=on
result_code=#result_code#; ??debug=on

=================== Форма ввода данных для действия с документом ==============??
<form name="popupForm" method="POST" enctype="multipart/form-data" target="wf">
<input type=hidden name="c" value="docs/sendFYI">
<input type=hidden name="doIt" value="Y">
<input type=hidden name=doc_id value='#doc_id#'>

<center><table border=0 cellpadding=5 cellspacing=0>
<tr><td colspan=2 class="big center">
Уважаемый ??!user_sex=Ж
Уважаемая ??user_sex=Ж
#user_I# #user_O#,</td></tr>

<tr><td colspan=2 class="big
error ??ERROR&!FORWARD_USERS_OK=Y
">Выберите пользователей, которым Вы хотите отправить документ:
</td></tr>

=============  Выбор пользователя, кому послать документ FYI ======= ??
<tr><td></td><td>
<div id="extra_users_list" class="big"></div>
<span class="info_input big" id="extra_users" info_id="5" searchFor='none' info_view="5">выбрать...</span>
<input type=hidden id="extra_users_id" name="extra_users_id" size=25 value=""> 
<input type=hidden id="extra_users_text" name="extra_users_text" size=25 value="">
</td></tr>

<tr><td></td><td class="big"><input type=radio name="result_code" value='#~doc_action_inform#' onClick="$('##msg').html($('##FYI_msg').html())"
checked ??result_code=#~doc_action_inform#
>для ознакомления
<br><input type=radio name="result_code" value='#~doc_action_prepare#' onClick="$('##msg').html($('##Prepare_msg').html())" ??AR_W=Y
checked ??result_code=#~doc_action_prepare#&AR_W=Y
>для подготовки документа  ??AR_W=Y
</td></tr>

=============  Комментарий ======= ??
<tr><td class="label big"> можете ввести Ваш комментарий:
<br><input type=checkbox checked name="comment_opened" onClick="if(this.checked)$('##conf').html('Комментарий будет виден всем пользователям'); else $('##conf').html('Комментарий будет доступен только получателям.');">показывать всем пользователям
</td><td><textarea name=user_comment rows=4 cols=60>#user_comment#</textarea>
</td></tr>

=============  Подсказка ======= ??
<tr><td colspan=2 nowrap>
<fieldset class="bg_white gray_border"> ??
<fieldset class="info">
<legend><i class="fa fa-info-circle" aria-hidden="true"></i></legend>
<center>
После нажатия Вами кнопки  "Отправить"
<div id="msg"> документ будет отправлен <b>для ознакомления</b> выбранным Вами пользователям.
<div id="conf">Комментарий будет виден всем пользователям</div>
</div>    
<div style="margin:10px;" class=
"small" ??!INPUT_ERROR
"error" ??INPUT_ERROR
>
<b>#INPUT_ERROR#</b><br> ??INPUT_ERROR
</div>
Ошибка выполнения операции. ??ERROR
<br><small>#ERROR#</small> ??ERROR
</center></fieldset>
</td></tr>

<tr><td colspan=2 class=center>
<br>
<input type="button" class="butt1" style="width:120;" value="Отмена" onClick="HideDialog();">&nbsp; &nbsp;
<input type="button" class="butt1" style="width:120;" value="Отправить" onClick="document.popupForm.submit();"> 
</td></tr>
</table>
$INCLUDE [messages]
</div>   ??!ajax

$INCLUDE [ajax script]   ??ajax
$INCLUDE [script]   ??!ajax
[end]

[ajax script]
<script type="text/javascript">
showMsg("##popupCont"); ??
showMsg("##dialog_title", "Посылка документа для ознакомления");
ShowDialog(true);
centerDialog(); 
</script>
[end]

[script]
<script type="text/javascript">
window.parent.showMsg("##popupCont");
window.parent.showMsg("##dialog_title", "Посылка документа для ознакомления");
window.parent.getResult("popupCont", document.getElementById("result"));
window.parent.ShowDialog(true);
window.parent.centerDialog(); 
</script>
[end]


[messages]
<div id="FYI_msg" style="display:none;"> документ будет послан <b>для ознакомления</b> выбранному Вами пользователю.</div> 

<div id="Prepare_msg" style="display:none;"> документ будет послан для подготовки выбранному Вами пользователю<br>
и он <b>сможет вносить в документ изменения.</b>
</div> 
[end]
