doc_final_forward.cfg
[comments]
descr=U: Пересылка документа на этапе утверждения

input=doc_id - ID документа, cop = ~doc_action_delegate или ~doc_action_forward (см. [sys_const].alias like '~doc_action_%')

output=Pop-up окно действия
parents=docs/view_doc.cfg
childs=docs/doc_user_action_process.cfg
test_URL=?c=docs/doc_final_forward&doc_id=1
[end]

[parameters]
request_name=U:пересылка документа на утверждение
KeepLog=false
ClearLog=true
[end]

[report]
$CALL_SERVICE c=sys/getARUD;   ??!AR_S=Y
$INCLUDE [OK report]  ??AR_S=Y
$CALL_SERVICE c=sys/log_doc_access; doc_id=#doc_id#; rejected=1;   ??!AR_S=Y
[end]


[OK report]
$SET_PARAMETERS result_code=#~doc_action_delegate#; ??cop=forward&!result_code
$SET_PARAMETERS result_code=#~doc_action_sign#; WF_ID=#WF_ID_INACT#; CURR_STEP=0; ??cop=#~doc_action_startWF#
$SET_PARAMETERS result_code=#cop#; ??!result_code

$SET_PARAMETERS WF_ID=#WF_ID_INACT#; ??!WF_ID&WF_ID_INACT_ZZZ
$GET_DATA [get active wf step id]  ??!cop=#~doc_action_terminate#
$CALL_SERVICE c=wf/check_next_step; ??cop=#~doc_action_startWF#|cop=#~doc_action_sign#

$CALL_SERVICE c=docs/doc_user_action_process.cfg ??doIt=Y
$INCLUDE [form] ??!doIt=Y|ERROR|INPUT_ERROR
[end]


[form]
<div id="result">  ??!ajax

$CALL_SERVICE c=svs/get_user_info; requested_user_id=#NEXT_USER_ID#; ??NEXT_USER_ID
$SET_PARAMETERS next_FIO=#u_FIO#; next_IOF=#u_IOF#; next_posts=#u_posts#; next_roles=#u_roles#;  ??NEXT_USER_ID
, u_email, u_phone, , u_login,  ??

#INPUT_ERROR# ??
<small>#c#</small> ??debug=on
result_code=#result_code#; ??debug=on

=================== Форма ввода данных для действия с документом ==============??
<form name="popupForm" method="POST" enctype="multipart/form-data" target="wf" autocomplete="off">
<input type=hidden name="c" value="docs/doc_final_forward">
<input type=hidden name="cop" value="#cop#">
<input type=hidden name="doIt" value="Y">
<input type=hidden name=doc_id value='#doc_id#'>

<center><table border=0 cellpadding=5 cellspacing=0>
<tr><td colspan=2 class="big center">
Уважаемый ??!user_sex=Ж
Уважаемая ??user_sex=Ж
#user_I# #user_O#,</td></tr>

<tr><td colspan=2 class=big>для <b>ПЕРЕСЫЛКИ</b> документа </td></tr>

$INCLUDE [select forward user]
$INCLUDE [comment] 
$INCLUDE dat/common.dat[pw2]
$INCLUDE [msg box] 
 
<tr><td colspan=2 class=center>
<input type="button" class="butt1" style="width:120;" value="Отмена" onClick="HideDialog();">&nbsp; &nbsp;
<input type="button" class="butt1" style="width:120;" value="Переслать" onClick="document.popupForm.submit();"> 
</td></tr>
</table>
$INCLUDE [messages]
$INCLUDE dat/common.dat[restore forward users] ??extra_users_id&!ajax
<script type="text/javascript">
var setMessage=function(){
$('##msg').html($('##delegate_msg').html());  ??!result_code=#~doc_action_forward#
$('##msg').html($('##forward_msg').html());  ??result_code=#~doc_action_forward#
}
</script>
</div>   ??!ajax

$INCLUDE [ajax script]   ??ajax
$INCLUDE [script]   ??!ajax
[end]



[ajax script]
<script type="text/javascript">
showMsg("##popupCont"); ??
showMsg("##dialog_title", "Пересылка документа");
jAlert("#cop# / #doIt#;"); ??
ShowDialog(true);
centerDialog(); 
setMessage();
</script>
[end]

[script]
<script type="text/javascript">
window.parent.showMsg("##popupCont");
window.parent.showMsg("##dialog_title", "Пересылка документа");
window.parent.getResult("popupCont", document.getElementById("result"));
window.parent.ShowDialog(true);
window.parent.centerDialog(); 
window.parent.setMessage();
</script>
[end]



[comment]
<tr><td class="label big 
error ??INPUT_ERROR&!COMMENT_OK=Y&!cop=#~doc_action_startWF#&!cop=#~doc_action_sign#&!cop=#~doc_action_terminate#
"> (*) введите Ваш комментарий:</td><td><textarea name=user_comment rows=4 cols=60>#user_comment#</textarea></td></tr>
[end]


[select forward user]
=============  Элементы формы для выбора пользователя, кому переслать документ ======= ??
<tr><td class="label big
error ??INPUT_ERROR&!FORWARD_USER_OK
">(*) Укажите, кому Вы хотите<br>переслать документ:</td><td>
$INCLUDE dat/common.dat[extra users selector] param: multi=0; 

<br>
$INCLUDE [forward type] ??AR_S=Y
<input type=hidden name="result_code" value='#~doc_action_delegate#'>  ??!AR_S=Y&AR_A=Y
</td></tr>
[end]


[forward type]
<input type=radio name="result_code" value='#~doc_action_forward#' onClick="$('##msg').html($('##forward_msg').html())"
checked ??result_code=#~doc_action_forward#|!result_code
>Послать на согласование с возвратом мне на утверждение<br>

<input type=radio name="result_code" value='#~doc_action_delegate#' onClick="$('##msg').html($('##delegate_msg').html())"
checked ??result_code=#~doc_action_delegate#
>Делегировать право утверждения<br><br>
[end]


[msg box]
<tr><td colspan=2 class="center bg_white gray_border" nowrap>
После нажатия Вами кнопки "Переслать"<div id="msg"></div>

<div style="margin:10px;" class=
"small" ??!INPUT_ERROR
"error" ??INPUT_ERROR
>
<b>#INPUT_ERROR#</b><br> ??INPUT_ERROR
<i>поля, отмеченные (*), обязательны для заполнения!</i>
</div>
Ошибка выполнения операции. ??ERROR
<br><small>#ERROR#</small> ??ERROR
</td></tr>
[end]


[messages]
<div id="delegate_msg" style="display:none;"> документ будет переслан на утверждение выбранному Вами пользователю.<br>
<b>К Вам на утверждение он не вернется.</b></div> 

<div id="forward_msg" style="display:none;"> документ будет переслан на рассмотрение выбранному Вами пользователю.<br>
<b>Он вернется к Вам на утверждение в случае его согласования.</b></div>  
[end]

==============================================================================
***************************** Шаблоны SQL запросов ***************************
==============================================================================


[get active wf step id]
    select /* ID шага, на котором ждем утверждения */ 
        id as "step_id" 
        , wf.role_id as "CURR_STEP_ROLE_ID"
        , wf.role_target_type_id as "CURR_STEP_TARGET_TYPE_ID"
        , wf.role_target_id  as "CURR_STEP_TARGET_ID"
        step_type as "CURRENT_STEP_TYPE" ??
    from wf 
    where wf_id=#WF_ID# 
        and step=#CURR_STEP# 
        and user_id=#USER_ID#
        and not started is null 
        and result_code is null
[end]

and user_id=#NEXT_USER_ID# ??NEXT_USER_ID
and user_id=#USER_ID# ??!NEXT_USER_ID

[get next user ID]
select /* берем ID юзера, выбранного по workflow на шаге (разрешено только для админов!) */
user_id as "NEXT_USER_ID"
from wf 
where wf_id=#WF_ID# and step=#CURR_STEP# 
and not started is null and result_code is null
limit 0,1
;
[end]
