docs/doc_user_action.mod

[comments]
descr=U: форма подготовки действия с документом (стартовать, отклонить, согласовать, делегировать, переслать с возвратом).

input=doc_id - ID документа, cop - действие с документом (см. [sys_const].alias like '~doc_action_%')

output=Pop-up окно действия или вызов docs/doc_user_action_process.cfg для выполнения действия
parents=docs/view_doc.cfg
childs=docs/doc_user_action_process.cfg
test_URL=?c=docs/doc_user_action&doc_id=1051&cop=4
author=Куняев
[end]

[description]
<b>При первом вызове</b> (!doIt=Y):
<ul>
<li>Проверка, имеет ли юзер право на действие по кнопке из view_doc.cfg (выставляется AR_OK=Y)</li>
<li>Вывод формы ввода данных для выбранного действия:<br>
"Отправить на согласование"   (cop=##~doc_action_startWF##)<br>
"Вернуть инициатору"   (cop=##~doc_action_reject##)<br>
"Вернуть в СМТС"   (cop=##~doc_action_reject_2SMTS##)<br>
"СОГЛАСОВАНО"   (cop=##~doc_action_sign##)<br>
"ПЕРЕСЛАТЬ"   (cop=forward)<br>
"Отозвать"   (cop=##~doc_action_terminate##)<br>
</li>
</ul>

<b>По Submit формы</b> (doIt=Y):
<ul>
<li>Проверка, имеет ли юзер право на действие (выставляется AR_OK=Y)</li>

<li>Подготовка параметров для выполнения действия:<br>
<b>result_code</b> (обычно = ##cop## за исключением cop=forward => ##~doc_action_delegate##) <br>

</li>
<li>Выполнение соответствующего действия по кнопке из формы (вызов docs/doc_user_action_process.cfg)</li>
<li>Если обнаружена ошибка ввода или выполнения - вывод формы с сообщением об ошибках</li>

</ul>
[end]


[parameters]
request_name=U:действие с документом
LOG=ON
[end]

[report]
$INCLUDE [check WF] ??cop=#~doc_action_startWF#
$CALL_SERVICE c=sys/getARUD;   
  ??ZZZ&!AR_T=Y&!AR_A=Y&!AR_S=Y&!AR_E=Y&!AR_R=Y&!AR_X=Y
$SET_PARAMETERS AR_T=Y; AR_T_ADM=Y;  ??AR_A=Y&WF_ID

$SET_PARAMETERS result_code=#~doc_action_delegate#; ??cop=forward&!result_code
$SET_PARAMETERS result_code=#cop#; ??!result_code
$INCLUDE [check acc rights]

$SET_PARAMETERS result_code=#~doc_action_sign#; WF_ID=#WF_ID_INACT#; CURR_STEP=0; ??cop=#~doc_action_startWF#
$LOG1 <b>docs/doc_user_action.cfg:</b> result_code=#result_code#; cop=#cop#; extra_users_id=#extra_users_id#; extra_role_id=#extra_role_id#;<br>

$CALL_SERVICE  c=wf/check_next_criteria.cfg ??WF_ID
$INCLUDE [report_]  ??AR_OK=Y
$LOG +++++ ERROR=#ERROR#;  ??ERROR
[end]

[check WF]  ***** Выполняется при старте WF
$GET_DATA [check wf exists]
$CALL_SERVICE c=sys/getARUD;  ??!WF_EXISTS=Y
$CALL_SERVICE c=docs/set_doc_divs;   ??!WF_EXISTS=Y
$CALL_SERVICE c=wf/create_wf_for_doc.cfg; ??!WF_EXISTS=Y
[end]

[check wf exists]
select w.id as WF_ID_ZZ, 'Y' as WF_EXISTS
from wf_list w where w.doc_id=#doc_id# 
and w.is_active=1 ??
[end]


[check acc rights] ****** проверка, имеет ли юзер право на выбранное действие 
$SET_PARAMETERS AR_OK=Y; ??AR_R=Y&result_code=#~doc_action_inform#
$SET_PARAMETERS AR_OK=Y; ??AR_E=Y&result_code=#~doc_action_startWF#|result_code=#~doc_action_sign#
$SET_PARAMETERS AR_OK=Y; ??AR_S=Y&result_code=#~doc_action_delegate#|result_code=#~doc_action_forward#|result_code=#~doc_action_reject#|result_code=#~doc_action_sign#|cop=#~doc_action_reject_2SMTS#
$SET_PARAMETERS AR_OK=Y; ??AR_X=Y&result_code=#~doc_action_delegate#|result_code=#~doc_action_forward#|result_code=#~doc_action_sign#
$SET_PARAMETERS AR_OK=Y; ??AR_T=Y&result_code=#~doc_action_terminate#
$SET_PARAMETERS AR_OK=Y; ??AR_A=Y&WF_ID&CURR_STEP&result_code=#~doc_action_delegate#|result_code=#~doc_action_terminate#
$LOG +++++ <b>DOC_USER_ACTION:</b> result_code=#result_code#; AR_OK=#AR_OK#; AR_T=#AR_T#; AR_A=#AR_A#; AR_S=#AR_S#; AR_E=#AR_E#; AR_X=#AR_X#; AR_R=#AR_R#; WF_ID=#WF_ID#; CURR_STEP=#CURR_STEP#; +++++++<br>
$SET_PARAMETERS ERROR=Отказано в доступе; ??!AR_OK=Y
$CALL_SERVICE c=sys/log_doc_access; doc_id=#doc_id#; access_type=#result_code#; rejected=1;   ??ERROR
[end]


[report_] ****** OK report или ERR MSG при ошибке доступа
  ***** Проверка данных перед выполнением действия юзера, если шаг завершается ??
$INCLUDE docs/custom_settings.cfg[check doc data]  ??result_code=#~doc_action_sign#

$INCLUDE [OK report]  ??!DATA_ERR_CODE&!DATA_ERROR
$INCLUDE [ERR MSG]    ??DATA_ERR_CODE|DATA_ERROR
[end]

[ERR MSG] ****** Сообщение об ошибке - нет доступа или ошибка в данных
<div class="big"><i class="fa fa-exclamation-triangle clr-red" aria-hidden="true"></i> Ошибка:<br><br>
 <b>#DATA_ERROR#</b>
<br>(#DATA_ERR_CODE#) ??
<br><br><center>
<input type="button" class="butt1" style="width:120;" value="Закрыть" onClick="HideDialog();">
</center></div>

$INCLUDE [ajax script] 
[end]


[OK report] ****** Вывод формы или вызов обработки (doIt=Y)
$GET_DATA [get active wf step id]  ??!cop=#~doc_action_terminate#
$CALL_SERVICE c=wf/check_next_step; ??cop=#~doc_action_startWF#|cop=#~doc_action_sign#
$SET_PARAMETERS step_id=0; ??!cop=#~doc_action_startWF#&!cop=#~doc_action_sign#

$CALL_SERVICE c=docs/doc_user_action_process.cfg ??doIt=Y&step_id|cop=#~doc_action_terminate#
$SET_PARAMETERS  ERROR=Системная ошибка: не определен активный шаг маршрута; doIt=;   ??doIt=Y&!step_id&!cop=#~doc_action_terminate#
$INCLUDE [form] ??!doIt=Y|ERROR|INPUT_ERROR
[end]


[form] ****** Форма ввода данных для выбранного действия с документом 

$CALL_SERVICE c=svs/get_user_info; requested_user_id=#NEXT_USER_ID#; ??NEXT_USER_ID
$SET_PARAMETERS next_FIO=#u_FIO#; next_IOF=#u_IOF#; next_posts=#u_posts#; next_roles=#u_roles#;  ??NEXT_USER_ID

INPUT_ERROR=#INPUT_ERROR#; result_code=#result_code#; ??

<form name="popupForm" id="popupForm" method="POST" enctype="multipart/form-data" target="wf" autocomplete="off">
<input type=hidden name="c" value="docs/doc_user_action">
<input type=hidden name="cop" value="#cop#">
<input type=hidden name="doIt" value="Y">
<input type=hidden name=doc_id value='#doc_id#'>
<input type=hidden name=admin_action value='Y'>  ??NEXT_USER_ID

<center><table border=0 cellpadding=5 cellspacing=0>
<tr><td colspan=2 class="big center">
Уважаемый ??!user_sex=Ж
Уважаемая ??user_sex=Ж
#user_I# #user_O#,</td></tr>

<tr><td colspan=2 class=big>для  
<b>Отправки документа на согласование</b> ??cop=#~doc_action_startWF#
<b>ВОЗВРАТА</b> ??cop=#~doc_action_reject#|cop=#~doc_action_reject_2SMTS#
<b>СОГЛАСОВАНИЯ</b> ??cop=#~doc_action_sign#
<b>ПЕРЕСЫЛКИ</b> ??cop=forward
<b>ОТЗЫВА</b> ??cop=#~doc_action_terminate#
документа  ??!cop=#~doc_action_startWF#
инициатору ??cop=#~doc_action_reject#
в СМТС ??cop=#~doc_action_reject_2SMTS#
<b>на завершение</b> ??DOC_STATUS=#~doc_status_signed#&!cop=#~doc_action_terminate#
</td></tr>

$INCLUDE [select next user] ??UDEFINED_USERS
$INCLUDE [select forward user] ??cop=forward
$INCLUDE [comment] ??!cop=#~doc_action_startWF#&!cop=#~doc_action_terminate#
$INCLUDE [comment] ??cop=#~doc_action_terminate#&AR_T_ADM=Y

$INCLUDE dat/common.dat[pw2]

$INCLUDE [msg box] 
 
<tr><td colspan=2 class=center>
<input type="button" class="butt1" style="width:120;" value="Отмена" onClick="HideDialog();">&nbsp; &nbsp;

<input type_=hidden id="extra_role_id" name="extra_role_id" size=3 value="20"> ??cop=#~doc_action_reject_2SMTS#

<input type="button" class="butt1" style="width:160;" value=
"Отправить"   ??cop=#~doc_action_startWF#

"Вернуть инициатору"   ??cop=#~doc_action_reject#
"Вернуть в СМТС"   ??cop=#~doc_action_reject_2SMTS#

"СОГЛАСОВАНО"   ??cop=#~doc_action_sign#
"ПЕРЕСЛАТЬ"   ??cop=forward
"Отозвать"   ??cop=#~doc_action_terminate#
onClick="document.popupForm.submit();"  ??
onClick="AjaxCall('popupCont', '', true, 'popupForm', true);"
> 
</td></tr>
</table>
$INCLUDE [messages]
$INCLUDE dat/common.dat[restore forward users] ??extra_users_id_ZZZZZ

$INCLUDE [ajax script]   
[end]



[ajax script]  ****** Отображение диалога и установка заголовка окна при ajax-вызове
<script type="text/javascript">
showMsg("##dialog_title", 
"Отправка документа на согласование"   ??cop=#~doc_action_startWF#
"Согласование"   ??cop=#~doc_action_sign#
"Возврат инициатору"   ??cop=#~doc_action_reject#
"Возврат"   ??cop=#~doc_action_reject_2SMTS#
"Пересылка"   ??cop=forward
"Отзыв"     ??cop=#~doc_action_terminate#
+ " документа"  ??!cop=#~doc_action_startWF#
+ " в СМТС"   ??cop=#~doc_action_reject_2SMTS#
);
jAlert("#cop# / #doIt#;"); ??

ShowDialog(true);
centerDialog(); 
textAreaResize();
</script>
[end]


[comment]   ****** поле ввода комментария к действию
<tr><td class="label big 
error ??INPUT_ERROR&!COMMENT_OK=Y&!cop=#~doc_action_startWF#&!cop=#~doc_action_sign#&!cop=#~doc_action_terminate#
">
(*) введите  ??!cop=#~doc_action_startWF#&!cop=#~doc_action_sign#&!cop=#~doc_action_terminate#
(*) введите  ??cop=#~doc_action_terminate#&AR_T_ADM=Y
можете ввести  ??cop=#~doc_action_startWF#|cop=#~doc_action_sign#|cop=#~doc_action_terminate#&!AR_T_ADM=Y
Ваш комментарий:
<br><input type=checkbox checked name="comment_opened" onClick="if(this.checked)$('##conf').html('Комментарий будет виден всем пользователям'); else $('##conf').html('Комментарий будет доступен только получателям.');">показывать комментарий<br>всем пользователям  ??!cop=#~doc_action_sign#&!cop=#~doc_action_terminate#
<input type=hidden value="on" name="comment_opened" >  ??cop=#~doc_action_sign#|cop=#~doc_action_terminate#
  ??

</td><td><textarea class="autoresize" name=user_comment rows=5 cols=80>#user_comment#</textarea></td></tr>
[end]


[select next user] ****** Выбор пользователей, визирующих на след.шаге, если они не были определены
<tr><td colspan=2 class="label big">
Укажите пользователя, который должен визировать документ на следующем шаге:
</td></tr><tr><td colspan=2 class="big" style="padding:0 0 0 30px;"><b>#UDEFINED_USERS#</b></td></tr>

<tr><td class="big label
error ??INPUT_ERROR&!FORWARD_USER_OK
">(*) Выберите пользователя&nbsp;(ей):</td><td>
$INCLUDE dat/common.dat[extra users selector]

<br><input type=hidden name="result_code" value='#~doc_action_sign#'>
<input type=hidden name="SET_NEXT_USER" value='Y'>
</td></tr>
[end]


[select forward user] ******* Элементы формы для выбора пользователя(ей), кому переслать документ
<tr><td class="label big
error ??INPUT_ERROR&!FORWARD_USER_OK
">(*) Укажите, кому Вы хотите<br>переслать документ:</td><td>
$INCLUDE dat/common.dat[extra users selector]

<br>
$INCLUDE [forward type] ??AR_S=Y|AR_X=Y
<input type=hidden name="result_code" value='#~doc_action_delegate#'>  ??!AR_S=Y&!AR_X=Y&AR_A=Y
</td></tr>
[end]



[forward type]  ****** тип пересылки - делегирование или с возвратом
    <input type=radio name="result_code" value='#~doc_action_delegate#' onClick="$('##msg').html($('##delegate_msg').html())"
    checked ??result_code=#~doc_action_delegate#
    >Делегировать право согласования<br>
    <input type=radio name="result_code" value='#~doc_action_forward#' onClick="$('##msg').html($('##forward_msg').html())"
    checked ??result_code=#~doc_action_forward#
    >Переслать с возвратом мне<br><br>
[end]


[msg box]  ****** Подсказка юзеру о его действии
<tr>
    <td colspan=2 class="center bg_white gray_border" nowrap> ??
    <td colspan=2 nowrap>
    <fieldset class="info"><legend><i class="fa fa-info-circle" aria-hidden="true"></i></legend> ??
    #tag_fs_info#<center>
    <center>
    После нажатия Вами кнопки  
    "Отправить"<br>будет начат процесс согласования документа.  ??cop=#~doc_action_startWF#

    "Вернуть инициатору"<br>процесс согласования документа будет приостановлен.<br> Документ будет отправлен инициатору  ??cop=#~doc_action_reject#
    "Вернуть в СМТС"<br>процесс согласования документа будет приостановлен.<br> Документ будет отправлен на пересмотр в СМТС  ??cop=#~doc_action_reject_2SMTS#

    "Согласовано"<br> документ будет отправлен далее на согласование    ??cop=#~doc_action_sign#

    "Переслать"<div id="msg"> документ будет переслан на согласование указанному Вами пользователю.<br><b>К Вам на согласование на этом шаге маршрута он не вернется.</b></div>     ??result_code=#~doc_action_delegate#&AR_S=Y&DOC_STATUS=#~doc_status_in_progress#
    "Переслать"<div id="msg"> документ будет переслан на согласование указанному Вами пользователю.<br><b>На согласование к пользователю #next_FIO# на этом шаге маршрута он не вернется.</b></div>     ??result_code=#~doc_action_delegate#&!AR_S=Y&AR_A=Y&DOC_STATUS=#~doc_status_in_progress#

    "Переслать"<div id="msg"> документ будет передан на завершение обработки указанному Вами пользователю.<br><b>К Вам на завершение он не вернется.</b></div>     ??result_code=#~doc_action_delegate#&AR_X=Y&DOC_STATUS=#~doc_status_signed#
    "Переслать"<div id="msg"> документ будет передан на завершение обработки указанному Вами пользователю.<br><b>На завершение к пользователю #next_FIO# он больше не вернется.</b></div>  ??result_code=#~doc_action_delegate#&!AR_X=Y&AR_A=Y&DOC_STATUS=#~doc_status_signed#


    "Отозвать"<br>процесс согласования документа будет прекращен.<br> Вы сможете внести в документ изменения и повторно отправить его на согласование. ??cop=#~doc_action_terminate#&!AR_A=Y

    "Отозвать" процесс согласования документа будет прекращен.<br> Инициатор документа сможет внести в документ изменения и повторно отправить его на согласование. ??cop=#~doc_action_terminate#&AR_A=Y

    <br>result_code=#result_code#; AR_X=#AR_X#; AR_A=#AR_A#; DOC_STATUS=#DOC_STATUS#; ??
    <div id="conf"></div>

    <div style="margin:10px;" class=
    "small" ??!INPUT_ERROR
    "error" ??INPUT_ERROR
    >
    <b>#INPUT_ERROR#</b><br> ??INPUT_ERROR
    <i>поля, отмеченные (*), обязательны для заполнения!</i>
    </div>
    Ошибка выполнения операции. ??ERROR
    <br><small>#ERROR#</small> ??ERROR
    </center></fieldset></td>
</tr>
[end]


[messages]
    <div id="delegate_msg" style="display:none;"> документ будет переслан на рассмотрение указанному Вами пользователю.<br>
    <b>К Вам на согласование на этом шаге маршрута он не вернется.</b></div> 

    <div id="forward_msg" style="display:none;"> документ будет переслан на рассмотрение указанному Вами пользователю.<br>
    <b>Он вернется к Вам в случае согласования его этим пользователем.</b></div>  
[end]

==============================================================================
***************************** Шаблоны SQL запросов ***************************
==============================================================================


[get active wf step id]
$INCLUDE [get next user ID]     ??!AR_S=Y&AR_A=Y
$INCLUDE [get next user ID]     ??AR_E=Y&AR_W=Y&CURR_STEP=0

select /* ID шага, на котором ждем действия текущего юзера и его роль по этому шагу */ 
id as "step_id" 
, wf.role_id as "CURR_STEP_ROLE_ID", wf.role_target_type_id as "CURR_STEP_TARGET_TYPE_ID", wf.role_target_id  as "CURR_STEP_TARGET_ID"
, wf.criteria as "CURR_STEP_CRITERIA"

from wf 
where wf_id=#WF_ID# and step=#CURR_STEP# 
and not started is null and result_code is null
and user_id=#NEXT_USER_ID# ??NEXT_USER_ID
and user_id=#USER_ID# ??!NEXT_USER_ID
limit 1
[end]

$SET_PARAMETERS AR_E=Y; ??!WF_ID&AR_W=Y

[get next user ID]
select /* берем ID первого юзера, выбранного по workflow на шаге (разрешено только для админов или для старта на шаге 0) */
    user_id as "NEXT_USER_ID"
from wf 
where wf_id=#WF_ID# and step=#CURR_STEP# 
    and not started is null 
    and result_code is null
limit 0,1;
[end]
