sys/ar/set_all_docs_permits.mod


[comments]
descr=S: Установка разрешений для всех документов
input=
output=запись в таблицу p_permits
childs=sys/ar/set_doc_permits.cfg
test_URL=?c=sys/ar/set_all_docs_permits&min=0&num=1000
author=Куняев
[end]

[description]
    Цикл по всем документам, имеющим атрибуты
    установка разрешений через вызов sys/ar/set_doc_permits.cfg
[end]


[parameters]
service=dubna.walt.service.TableServiceSpecial
tableCfg=table_no
LOG=ON
[end]


[report header]
    $SET_PARAMETERS num=1000 ??min&!num
    <b>Установка разрешений всех документов от #min# до #min#+#num#</b><br>
    $GET_DATA [clear docs permits] 
[end]

[item]
    #doc_id#, 
    $CALL_SERVICE c=sys/ar/set_doc_permits;
    $SET_PARAMETERS user_id=;
[end]

[report footer]
[end]



***************************** Шаблоны SQL запросов ***************************
[clear doc permits] ****** Удаление разрешений всех юзеров на все документы, данных "по правилам"
    delete from p_permits 
    where not rule_nr is null /* только разрешения "по правилам" */
    and doc_id between #min# and #min#+#num#  ??min
[end]


[SQL]  ****** выбираем все документы с атрибутами
    select dh.id as "doc_id"
    from d_list dh 
    where id in(select doc_id from p_doc_attr)
    and dh.id between #min# and #min#+#num#  ??min
    order by dh.id
[end]


join d_types dt on dt.id=dh.type_id
dh.is_deleted=0
and dt.group_id<70
and dh.id<200  ??
[end]

