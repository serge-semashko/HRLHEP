
sys/ar/list_doc_permits.mod

[comments]
descr=U: Список разрешений документа
input=doc_id
test_URL=?c=sys/ar/list_doc_permits.mod&doc_id=7415
author=Куняев
[end]

[parameters]
service=dubna.walt.service.TableServiceSpecial
tableCfg=table_no
LOG=ON
[end]

[report header]
$INCLUDE dat/common.dat[head] 
    <style>
    table.tlist tr.bold td{font-weight:bold;}
    table.tlist tr.gray td, table.tlist tr.gray td a{color:##808080;}
    table.doc tr td {padding:5px;}
    table.doc tr td {padding:7px 7px 10px 5px;}
    table.doc tr td.label {padding:10px 7px 10px 0;} ??
    </style>
    </head> 
<body>
<br><b>Разрешения на документ #doc_id#:</b>
<table>
[end]

[item]
$SET_PARAMETERS user_person_id=#user_id#; HAS_RULES=1;
<tr>
    <td>#FIO# <small>(#user_id#)</small>
<div style="font-size:8pt; text-align:right; border:solid 1px red;">
$CALL_SERVICE  c=sys/ar/view_user_rules_RO;
</div></td>
<td>#rule_nr#</td>
<td>#attr_id#</td>
<td>#shortName#</td>

<td>#value#</td>
<td>#CREATED#</td><td>#UPDATED#</td>
<td>#SENT#</td>

</tr>
[end]

[report footer]
</table>
<br>
[end]



[SQL]
  select p.user_id, u.FIO
    , p.rule_nr
    , DATE_FORMAT(p.created,'#dateTimeFormat#:%s') as CREATED, DATE_FORMAT(p.updated,'#dateTimeFormat#:%s') as UPDATED  
    , DATE_FORMAT(s.dat,'#dateTimeFormat#') as SENT  ??
, s.cnt as SENT
    ,r.attr_id, r.value
    ,c.shortName
  from p_permits p 
    left join #table_users_full# u on u.Id = p.user_id
    left join p_user_rule r on r.rule_nr=p.rule_nr and r.user_id=p.user_id and exclude=0
    left join p_attrib a on a.id=r.attr_id
    left join p_category c on c.id=a.cat_id
    left join d_sent_FYI s on s.doc_id=#doc_id# and s.user_id=p.user_id ??
    left join d_sent_FYI_count s on s.doc_id=#doc_id# and s.user_id=p.user_id
  where p.doc_id=#doc_id#
order by  
    u.FIO, ??
    r.attr_id, p.updated desc
[end]
