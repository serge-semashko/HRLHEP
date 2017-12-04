sys/ar/list_user_docs.mod

[comments]
    descr=S: Лист всех документов с атрибутами видимые пользователю
    input   user_person_id ,
    test_URL=?c=sys/ar/list_user_docs&user_person_id=3363
    author=Семашко
[end]

[description]
Цикл по всем документам из p_permits для данного пользователя
[end]


[parameters]
service=dubna.walt.service.TableServiceSpecial
tableCfg=table_no
LOG=ON
[end]


[report header]
    <table border=1 cellspacing=0>
    <tr>
    <td>ID</td>
    <td>Атрибуты</td>
    </tr>
[end]


[item]
    <tr>
    <td>#doc_id#</td>  
    <td>
        $CALL_SERVICE c=sys/ar/set_doc_conditions; show=Y;
    </td>
    </tr>
[end]

[report footer]
    </table>
[end]
[sql]
  select doc_id 
    from p_permits
  where user_id=#user_person_id#
[end]