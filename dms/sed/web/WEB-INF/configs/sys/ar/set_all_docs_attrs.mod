sys/ar/set_all_docs_attrs.cfg

[comments]
descr=S: Установка атрибутов всех документов
input=
output=запись в таблицу p_doc_attr
childs=sys/ar/set_doc_attrs.cfg
test_URL=?c=sys/ar/set_all_docs_attrs
author=Куняев
[end]

[description]
Цикл по всем документам, кроме служебных (group_id > 70)
установка атрибутов через вызов sys/ar/set_doc_attrs.cfg
[end]


[parameters]
service=dubna.walt.service.TableServiceSpecial
tableCfg=table_no
LOG=ON
[end]


[report header]
Установка атрибутов всех документов<br>
$GET_DATA [clear attrs]
[end]

[item]
#doc_id#, 
$CALL_SERVICE c=sys/ar/set_doc_attrs;
[end]

[report footer]
[end]



***************************** Шаблоны SQL запросов ***************************
[clear attrs]
truncate table p_doc_attr;
[end]

[SQL]
select dh.id as "doc_id"
from d_list dh join d_types dt on dt.id=dh.type_id
where dt.group_id<70 and dh.is_deleted=0
[end]

