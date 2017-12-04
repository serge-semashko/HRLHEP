sys/ar/set_user_permits.mod


[comments]
descr=U: Формирование списка документов, доступных пользователю
input=user_id
output=запись в таблицу p_permits
parents=sys/ar/edit_doc.cfg
childs=sys/ar/set_user_rule_docs.cfg
test_URL=?c=sys/ar/set_user_permits&user_id=10473&doc_id=5764
author=Куняев
[end]


[description]
Цикл по всем документам, проверка всех правил пользователя.
<ol>
    <li>Удаляет все разрешения юзера на документ из  p_permits</li>
    <li>Селектит список правил, которые есть ю юзера</li>
    <li>Если правила есть, то селект ID всех самостоятельных (не вложенных) документов</li>
    <li>Цикл по документам - для каждого документа цикл по правилам: </li>
        <ul>
            <li>сброс RULE_OK</li>
            <li>если еще не было получено разрешение, то вызов sys/ar/add_doc_permit.cfg </li>
        </ul>
    </li>
</ol>
[end]


[parameters]
service=dubna.walt.service.TableServiceSpecial
tableCfg=table_no
LOG=ON
[end]


[report header]
    $SET_PARAMETERS FULL_ACCESS=; RULES=; ATTRS=; HAS_EXCEPTIONS=;
    $GET_DATA [clear user permits] 
    $GET_DATA [check full access]
    $GET_DATA [get user rules] ??!FULL_ACCESS
    $GET_DATA [get user attrs]  ??!HAS_EXCEPTIONS
[end]


[item]
    $CALL_SERVICE c=sys/ar/set_doc_permits; single_user_id=#user_id#;
    $LOG2 <b>doc_id=#doc_id#: HAS_ACCESS=#HAS_ACCESS#</b><br>
[end]


[report footer]
    $SET_PARAMETERS RESULT=OK; ??!ERROR
    $SET_PARAMETERS doc_id=;
[end]
 

***************************** Шаблоны SQL запросов ***************************

[clear user permits] ****** Удаление разрешений юзеру на все документы
    delete from p_permits 
    where user_id=#user_id# 
    and doc_id=#doc_id# ??doc_id
    and not rule_nr is null /* только разрешения "по правилам" */
[end]

[check full access]
    select 'Y' as "FULL_ACCESS"
    from p_user_rule
    where user_id=#user_id# and rule_nr=0
;
    select 'Y' as HAS_EXCEPTIONS
    from p_user_rule where user_id=#user_id# and exclude=1
[end]


[get user rules]   ****** Получение списка номеров правил, которые есть у юзера
    select group_concat(distinct rule_nr) as "RULES"
    from p_user_rule
    where user_id=#user_id#
    order by rule_nr
[end]

[get user attrs]   ****** Получение списка атрибутов, которые есть у юзера и не запрещены
    select group_concat(distinct attr_id) as "ATTRS"
    from p_user_rule
    where user_id=#user_id#
[end]



[SQL]   ******* Выборку документов делаем только если у юзера есть правила и нет полного доступа 
$INCLUDE [SQL_] ??RULES&!FULL_ACCESS
[end]


[SQL_]  ******* Выборка ID всех документов, имеющих атрибуты 
   select dh.id as "doc_id"
    from d_list dh 
    where id in(select distinct doc_id from p_doc_attr
        where attr_id in(#ATTRS#) ??ATTRS
    )
    order by dh.id 
  desc ??
[end]


