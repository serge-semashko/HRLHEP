docs/doc_action_done.ajm

[comments]
descr=U: форма отметки о завершении документа.
input=doc_id - ID документа
output=Pop-up окно с формой
parents=docs/view_doc.cfg
childs=docs/doc_user_action_process.cfg
author=Куняев
[end]

[parameters]
request_name=U:завершение документа
KeepLog=false
ClearLog=true
[end]


[report]
    $CALL_SERVICE c=sys/getARUD;   ??!AR_X=Y
    $CALL_SERVICE c=wf/check_next_step; 
    $INCLUDE [OK report]  ??AR_X=Y
    $CALL_SERVICE c=sys/log_doc_access; access_type=4; rejected=1;   ??!AR_X=Y
[end]


[OK report]
    $SET_PARAMETERS result_code=#~doc_action_processed#; comment_opened=1;
    $GET_DATA docs/doc_user_action.cfg[get active wf step id] 
    $INCLUDE [process doc done]   ??doIt=Y
    $INCLUDE [form] ??!doIt=Y|ERROR|INPUT_ERROR
[end]


[form] =================== Форма ввода данных  ==============
    <form id="popupForm" name="popupForm" autocomplete="off">
        <input type="hidden" name="c" value="#c#">
        <input type="hidden" name="doc_id" value="#doc_id#">
        <center>
        <table border=0 cellpadding=5 cellspacing=0>
            <tr><td colspan=2 class="big center">
            Уважаемый ??!user_sex=Ж
            Уважаемая ??user_sex=Ж
            #user_I# #user_O#,</td></tr>
            <tr><td colspan=2 class=big>для отметки о <b>ЗАВЕРШЕНИИ</b> документа</td></tr>
            <tr><td class="label big">* Введите результат:</td>
                <td><textarea name=user_comment rows=3 cols=50>#user_comment#</textarea></td>
            </tr>
            $INCLUDE dat/common.dat[pw2]
            $INCLUDE [msg box] 

            <tr><td colspan=2 class=center>
            <input type="button" class="butt1" style="width:120;" value="Отмена" onClick="HideDialog();">&nbsp; &nbsp;
            <input type="button" class="butt1" style="width:120;" value="Завершено"
                onClick="AjaxCall('popupCont', 'c=docs/doc_action_done&doc_id=#doc_id#&cop=#~doc_action_processed#&doIt=Y', true, 'popupForm');"> 
            onClick="document.popupForm.submit();">  ??
            </td></tr>
        </table>
    </form>

    $INCLUDE [messages]
    </center>

    <script type="text/javascript">
        showMsg("##dialog_title", "Завершение обработки документа");
        ShowDialog(true);
        centerDialog(); 
    </script>
[end]


[process doc done] ----- Проверка ввода данных пользователем ------
    $SET_PARAMETERS INPUT_ERROR=;
    $GET_DATA docs/doc_user_action_process.cfg[check user input]
    $SET_PARAMETERS INPUT_ERROR=Введите результат; COMMENT_OK=; ??!user_comment
    $SET_PARAMETERS PW_OK=Y; ??playMode=on
    $SET_PARAMETERS INPUT_ERROR=Неверный пароль<br>#INPUT_ERROR#; ??!PW_OK

    $INCLUDE [really process] ??!ERROR&!INPUT_ERROR
    $INCLUDE [confirm msg]  ??!ERROR&!INPUT_ERROR
[end]


[really process] ----- Выполнение команды, если все ОК ------
    $GET_DATA docs/doc_user_action_process.cfg[start transaction] 
    $CALL_SERVICE c=wf/user_action; result_code=#~doc_action_processed#;

    $GET_DATA docs/doc_user_action_process.cfg[commit]
    <script type="text/javascript">jAlert('#ERROR#','ОШИБКА!');</script> ??ERROR&!INPUT_ERROR
[end]


[confirm msg]    ------ Оповещение и возврат в список документов при нормальном завершении операции -------
    $CALL_SERVICE c=wf/process_events;  ??!ERROR

    $GET_DATA [rollback doc status] ??NUM_NEXT_STEPS>0

    <script type="text/javascript">
    AjaxCall("popupCont", "c=wf/start_wf_step&doc_id=#doc_id#&cop=#cop#", true);  ??NUM_NEXT_STEPS>0
    jAlert("cop=#cop#; result_code=#result_code#", "DEBUG"); ??
    jAlert("Обработка документа завершена.","ОК"); 
    HideDialog(); 
    showDoc(false);
    AjaxCall("c_settings", "c=gateway/post_doc&doc_id=#doc_id#", true); ??POST_DOC_AT_STEP=1
    var go2docsTable = function(){
        AjaxCall("c_myDocs", "c=tab_myDocs"); 
    }
    window.setTimeout(go2docsTable, 1000);
    </script> 
[end]


[rollback doc status]  ***** Откат статуса документа и WF, если есть шаги после "завершения"
    update d_list set status=1 where id=#doc_id#;
    update wf_list set is_active=1 where id=#WF_ID#
[end]




[msg box]  ----- информация для тупых юзеров
    <tr><td colspan=2 class="center bg_white gray_border" nowrap>
        После нажатия Вами кнопки  "Завершено" обработка документа в СЭД будет завершена.<br>
        Инициатор документа будет уведомлен об этом.
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

