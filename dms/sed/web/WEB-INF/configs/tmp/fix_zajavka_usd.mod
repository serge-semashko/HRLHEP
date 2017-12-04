fix_zajavka_usd.cfg


[comments]
descr=S: Перенос сумм заявок ОИЯИ из источников в d_data_1
testURL=?c=tmp/fix_zajavka_usd&doc_id=938
author=Куняев
[end]

[description]
НЕ СДЕЛАНО!
[end]

[parameters]
service=dubna.walt.service.TableServiceSpecial ??
tableCfg=table_no
LOG=ON
[end]

[preSQLs]
[end]

[report]
$GET_DATA [get sources]
#SOURCES#
$INCLUDE [update sums] ??
$GET_DATA [update sums]
<br>#ERROR#
[end]

[get sources]
select group_concat(id) as SOURCES from d_list
where pid=#doc_id# and type_id=7
[end]

[update sums]
update d_data_1 set 
  summa_rub=(select sum(summa_rub) from d_data_7 where doc_id in(#SOURCES#))
, summa_usd=(select sum(summa_usd) from d_data_7 where doc_id in(#SOURCES#))
, summa_eur=(select sum(summa_eur) from d_data_7 where doc_id in(#SOURCES#))
where doc_id=#doc_id#
;
update d_data_1 set r_usd = summa_rub / summa_usd,  r_eur = summa_rub / summa_eur 
where doc_id=#doc_id#
[end]