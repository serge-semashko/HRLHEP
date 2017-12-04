sys/audit/start_nonstarted_steps.mod

[comments]
Ошибки маршрутизации: 

input=
output=HTML таблица http-запросов, 
parents=
childs=sys/viewRequest
[end]


[parameters]
service=dubna.walt.service.TableServiceSpecial
title=***ЛОГ-СЭД
request_name=Лог запросов
LOG=ON
tableCfg=table_no
[end]


[report header]
$SET_PARAMETERS srn=1; rpp=9999; RESULT=; USER_ID=0; user_FIO=Monitor; prev_doc_id=;


[end]


[item]
$INCLUDE [process doc] ??!doc_id=#prev_doc_id#
    $SET_PARAMETERS DID=; doc_id=; file_id=; file_name=; file_comment=; prev_doc_id=#doc_id#;
[end]

[process doc]
    док.#doc_id#, wf.#wf_id#, step=#step#
    #DOC_TYPE#
    №#DOC_NR# от #DOC_DAT# ??DOC_NR
    : #title#

    $CALL_SERVICE c=wf/start_wf_step.ajm

    $SET_PARAMETERS RESULT=#RESULT# #DOC_TYPE# #doc_id#, wf.#wf_id#, step=#step#  #ERROR#<br>
[end]

[report footer]
    $SET_PARAMETERS RESULT=OK #ERROR#; ??!RESULT
    RESULT=#RESULT#
    $CALL_SERVICE c=wf/process_events;  ??RESULT=OK
    $GET_DATA [schedule next run]
[end]



[schedule next run]
    update schedule set active=1, nextCall=DATE_ADD(now(),INTERVAL 5 MINUTE) where id=#MONITOR_TASK_ID#  ??MONITOR_TASK_ID&RESULT=OK
    ;
    insert into schedule (module, param, nextCall, comment, lastCall, lastResult) values ('sys/audit/start_nonstarted_steps','', DATE_ADD(now(),INTERVAL 3 MINUTE), 'Старт шагов', null, '-')   ??!MONITOR_TASK_ID|!RESULT=OK
[end]

    replace into schedule (module, param, nextCall, comment, lastCall, lastResult) ??


****************************************************************************
****************************************************************************
****************************************************************************


[SQL]
    select e.wf_id
        , e.step 
        , e.doc_id
        , t.short_name as "DOC_TYPE", d.title, d.number as "DOC_NR"

    from wf_not_started_err e 
        left join d_list d on d.id=e.doc_id
        left join d_types t on t.id=d.type_id 
[end]
