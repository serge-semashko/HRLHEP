sys/ar/set_doc_permits.cfg


[comments]
descr=U: Установка прав пользователей на документ
input=doc_id, [single_user_id] - если не задан, то по все юзерам
output=запись в таблицу p_permits
parents=sys/ar/set_all_docs_permits.cfg, sys/ar/set_user_permits.cfg, docs/edit_doc.cfg
childs=sys/ar/set_user_rule_docs.cfg
test_URL=?c=sys/ar/set_doc_permits&doc_id=813
author=Куняев&Семашко
[end]


[description]
Цикл по всем юзерам, установка прав на документ.
1. Удаляет все разрешения всех юзеров на документ p_permits, кроме постоянных прав
Добавляет запись в p_permits, если RULE_OK=Y 

[end]


[parameters]
service=dubna.walt.service.TableServiceSpecial
tableCfg=table_no
LOG=ON
[end]


[report header]
    $SET_PARAMETERS doc_attrs=; srn=1; rpp=9999; HAS_ACCESS=;
    $GET_DATA [clear doc permits] ??!single_user_id
    $GET_DATA [get doc attr list]
[end]


[item]
    $SET_PARAMETERS HAS_ACCESS=; RULE_OK=;
    $GET_DATA [check user access] 
    $LOG2 <br>HAS_ACCESS=#HAS_ACCESS#; check rules user_id=#user_id#...
    $INCLUDE [item_]  ??!HAS_ACCESS
    $SET_PARAMETERS RULES=;  ??!single_user_id
[end]


[item_]
    $GET_DATA sys/ar/set_user_permits.cfg[get user rules] ??!RULES
    $EXECUTE_LOOP rule_nr; #RULES#; [process rule] ??RULES
[end]


[process rule] ****** проверка, проходит ли док по правилу rule_nr
    $LOG2 check rule #rule_nr#...
    $CALL_SERVICE c=sys/ar/check_doc_rule;  ??!HAS_ACCESS
    $GET_DATA [add user permission] ??RULE_OK=Y&!HAS_ACCESS
    $SET_PARAMETERS HAS_ACCESS=Y; ??RULE_OK=Y
    $SET_PARAMETERS RULE_OK=;
[end]


[report footer]
    $LOG2 #user_id#: <b>HAS_ACCESS=#HAS_ACCESS#; #F# (#roles#)</b><hr>  ??HAS_ACCESS
    $LOG2 #user_id#: HAS_ACCESS=#HAS_ACCESS#; #F# (#roles#)<hr>  ??!HAS_ACCESS
[end]


***************************** Шаблоны SQL запросов ***************************

[clear doc permits] ****** Удаление разрешений юзера на документ, данных "по правилам"
    delete from p_permits 
    where doc_id=#doc_id# 
    and user_id=#user_id# ??user_id
    and not rule_nr is null
[end]

[check user access]
    select 'Y' as "HAS_ACCESS" from p_permits where doc_id=#doc_id# and user_id=#user_id#
[end]

[get doc attr list] ******* список ID атрибутов документа
    select group_concat(attr_id) as doc_attrs
    from p_doc_attr 
    where doc_id=#doc_id#
[end]


[SQL]  ******* ID юзеров
    select #single_user_id# as "user_id" ??single_user_id
    $INCLUDE [SQL all users] ??!single_user_id
[end]


[SQL all users]  ******* ID юзеров, имеющих правила кроме полных прав и прав по рассылке, у которых присутствует атрибут документа или в правиле есть exclude
    select distinct r.user_id
    , u.F, u.roles
    from p_user_rule r 
    left join a_users_jinr u on u.id=r.user_id
    where not r.rule_nr=0 
    ------- исключаем юзеров, имеющих права по рассылке ---- ??
    and r.user_id not in (select user_id from p_permits where doc_id=#doc_id#)
    and r.user_id in (select user_id from p_user_rule where attr_id in(#doc_attrs#) or exclude=1)
    order by r.user_id
[end]



[add user permission]  ****** добавление разрешения юзеру
    insert into p_permits (user_id, doc_id, rule_nr) values (#user_id#, #doc_id#, #rule_nr#)
[end]



