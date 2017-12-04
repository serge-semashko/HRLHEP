calc_zajavka_usd.cfg

[comments]
descr=S: Пересчет сумм заявок ОИЯИ в USD, EUR и руб. (временно, для перехода)
testURL=?c=tmp/calc_zajavka_usd
author=Куняев
[end]

[parameters]
service=dubna.walt.service.TableServiceSpecial
tableCfg=table_no
KeepLog=true
[end]

[preSQLs]
[end]

[report header]
<table border=1>
<tr>
<td>ID</td>
<td>DOC_DATE</td>
<td>MODIFIED</td>
<td>PRICE VAL</td>
    <td>summa_rub</td>
    <td>summa_usd</td>
    <td>summa_eur</td>
<td>PRICE_USD</td>
<td>usd_rate</td>
<td>PRICE_EUR</td>
<td>eur_rate</td>
<td>PRICE_RUB</td>
<td>rate_date</td>
</tr>
[end]


[item]
$INCLUDE [set currencies] 
<tr class_="pt">
    <td>#ID#</td>
    <td>#DOC_DATE#</td>
    <td>#MODIFIED#</td>
    <td>#PRICE# #VAL#</td>
    <td>#summa_rub#</td>
    <td>#summa_usd#</td>
    <td>#summa_eur#</td>

    <td>#PRICE_USD#</td>
    <td>#usd_rate#</td>
    <td>#PRICE_EUR#</td>
    <td>#eur_rate#</td>
    <td>#PRICE_RUB#</td>
    <td>#rate_date#</td>
</tr>
[end]

[report footer]
</table>
[end]

[set currencies]
$LOG MODIFIED=#MODIFIED#;
$SET_PARAMETERS PRICE=0; ??!PRICE
$GET_DATA [get currency rates]

$GET_DATA [save rub] ??VAL=руб.
$GET_DATA [save usd] ??VAL=USD
$GET_DATA [save eur] ??VAL=EUR

$GET_DATA [check rate]
[end]

[get currency rates]
SELECT /* doc_id=#ID# */ 
    rd.rate as usd_rate, re.rate as eur_rate
 ,DATE_FORMAT(rd.date,'#dateFormat#') as rate_date
FROM i_curr_rate rd
inner join i_curr_rate re on re.date = rd.date and re.curr='EUR'
WHERE rd.curr='USD' and rd.date<= STR_TO_DATE('#MODIFIED#', '#dateFormat#')
ORDER BY rd.date desc
limit 1
[end]

[save rub]
update d_data_1 
set summa_rub=#PRICE# 
, summa_usd = #PRICE# / #usd_rate#
, summa_eur = #PRICE# / #eur_rate#
, r_usd=#usd_rate#, r_eur=#eur_rate#, r_date=STR_TO_DATE('#rate_date#', '#dateFormat#')
where doc_id=#ID#
[end]

[save usd]
update d_data_1
set summa_usd=#PRICE# 
, summa_rub = #PRICE# * #usd_rate# ??usd_rate
, summa_eur = (#PRICE# * #usd_rate#) / #eur_rate# ??usd_rate&eur_rate
, r_usd=#usd_rate#, r_eur=#eur_rate#, r_date=STR_TO_DATE('#rate_date#', '#dateFormat#')
where doc_id=#ID#
[end]

[save eur]
update d_data_1 
set 
  summa_eur=#PRICE# 
, summa_rub = #PRICE# * #eur_rate#  ??eur_rate
, summa_usd = (#PRICE# * #eur_rate#) / #usd_rate# ??usd_rate&eur_rate
, r_usd=#usd_rate#, r_eur=#eur_rate#, r_date=STR_TO_DATE('#rate_date#', '#dateFormat#')
where doc_id=#ID#
[end]


[check rate]
select
  summa_eur as PRICE_EUR
, summa_rub as PRICE_RUB
, summa_usd as PRICE_USD
from d_data_1
where doc_id=#ID#
[end]

[SQL]
select
dh.ID
, DATE_FORMAT(dh.doc_date,'#dateFormat#') as DOC_DATE 
, DATE_FORMAT(dh.modified,'#dateFormat#') as MODIFIED

, dat.summa as "PRICE"
, dat.summa_curr as "VAL"
, dat.summa_rub, dat.summa_usd, dat.summa_eur
from d_list dh
left join d_types dtp on dtp.Id = dh.type_id ??
left join d_data_1 dat on dat.doc_id=dh.id

where dh.TYPE_ID = 1
and not dat.summa is null and dat.summa>0

and not dat.summa_curr is null and not dat.summa_curr=''
and abs(dat.r_usd - 60) > 40 ??
and dh.id=159 ??

order by dh.modified desc

[end]


dh.is_deleted=0 