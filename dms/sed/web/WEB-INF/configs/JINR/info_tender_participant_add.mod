JINR/info_tender_participant_add.cfg

[comments]
descr=Справочник участников закупочных конкурсов.
input=requesterId - ID элемента для результата, info_id - ID стравочника; view - № представления. По умолчанию - 1 (все поля по порядку); searchFor - строка поиска.
output=Pup-up окно с данными справочника с фильрами по колонкам представления и с выбором записи
parents=JINR/info_tender_participant
childs=JINR/info_tender_participant_kontragent_data
testURL=?c=JINR/info_BC&info_id=1005&view=1
author=Куняев, Яковлев
[end]

[description]
Модуль вывода данных справочника участников закупочных конкурсов. 
Выводит форму поиска и вызывает модуль вывода таблицы (JINR/info_tender_participant_kontragent_data).

[end]

[parameters]
request_name=U:справочник участников закупочных конкурсов
KeepLog=true
ClearLog=false
[end]



[report]
$SET_PARAMETERS irpp=30;
$SET_PARAMETERS searchFor=; ??searchFor=none|searchFor=undefined
$SET_PARAMETERS START_REC=1; ??!START_REC


<style>table.tlist td, table.tlist th{font-size:8pt;}
fieldset.border {border:solid 1px ##a0a0a0; padding:0;}
##d_sprav_window {background-color:##606060;}
##d_spravCont {background-color:white; margin: 0px 3px 3px 3px;}
table.tlist td, table.tlist th {font-size: 10pt;}
</style>

$LOG3 JINR/info_tender_participant.cfg: searchFor=#searchFor#; prev_prj_id=#prev_prj_id#;<br>

$GET_DATA [get BC info]  ??
searchFor  ??

$SET_PARAMETERS ERROR=;




$LOG3 JINR/info_tender_participant.cfg: => f_bc_div_id=#f_bc_div_id#; f_sbj=#f_sbj#; is_prikaz=#is_prikaz#; dir=#dir#;<br>

============ SEARCH FORM =============== ??
<div style="border:solid 1px red;"> ??
<form name="infoSpecialForm" method="POST" enctype="multipart/form-data" 
onSubmit="return false;" 
target="wf2"    
>
<input type=hidden name=c value="JINR/info_tender_participant_kontragent_data">
<input type=hidden name=requesterId value="#requesterId#"> 
<input type=hidden name=info_id value="#info_id#"> 
<input type=hidden name=multi id = "multi" value="#multi#"/> 
<input type=hidden name=view value="#view#"> 
<input type=hidden name=TABLE_NAME value="#TABLE_NAME#"> 

<input type=hidden name=START_REC value="#START_REC#"> 
<input type=hidden name=irpp value="#irpp#"> 
<input type=hidden name=standalone value="yes">    ??standalone=yes


<fieldset class="border">
<table border=0 cellpadding=5 cellspacing=0 style="border:none 1px gray; background-color:##efefef; width:100%;">

<tr><td><center>
<input type="submit" class="butt1 pt" style="width:250;  margin:5px 50px 10px 10px;" value="Добавить нового участника" onClick="
AjaxCall('d_spravCont', 'c=JINR/info_tender_participant_edit_data&cop=new&info_id=1014');" >

</center></td></tr>

</table>
</fieldset>

<fieldset class="border"><legend><b>Выбрать из справочника контрагентов:</b></legend>
<table border=0 cellpadding=5 cellspacing=0 style="border:none 1px gray; background-color:##efefef; width:100%;">


<tr><td class="label">Наименование:&nbsp;</td><td class="nowrap"><input size=50 name="f_k_name"> 
</td>
<td></td></tr>

<tr><td class="label">ИНН:&nbsp;</td><td class="nowrap"><input size=25 name="f_k_INN"> 
</td>
<td>
<input type="submit" class="butt1 pt" style="width:100;  margin:5px 50px 10px 10px;" value="Искать" onClick="doIt();" > 
</td></tr>



</table>
</fieldset>

<tr><td colspan=3> ??
<div id="info_result_data" style="overflow-y:scroll; height:500px; padding:10px; margin:2px; border:solid 1px ##a0a0a0;"></div>
</td></tr> ??
<tr><td colspan=3 style="border-top:solid 1px gray; text-align: center;"> ??
<center>
<input type="button" class="butt1 pt" style="width:280; margin:5px;" value="В справочник участников конкурса" onClick="
AjaxCall('d_spravCont', 'c=JINR/info_tender_participant&info_id=1014&requesterId=provider&multi=0&view=2&TABLE_NAME=i_jinr_tender_participant&irpp=30&START_REC=1');" > 
</center>
</td></tr> ??

</form>

$INCLUDE svs/info_show_plain_script.dat[css]  ??!INFO_CSS_LOADED
$INCLUDE svs/info_show_plain_script.dat[script] 

<script type="text/javascript">
var infoSpecialForm = document.infoSpecialForm;
infoForm.searchFor.focus();  ??


/**
 * Загрузка данных справочника БК ОИЯИ
 * Submit document.infoForm
 */
var doIt=function()
{
console.log("JINR/info_tender_participant.cfg.doIt...");  ??
    document.infoSpecialForm.START_REC.value=1;
    document.infoSpecialForm.submit();
}

/**
 * Перегрузка стандартной функции поиска по плоскому справочнику
 */
var doSearch=function(){
    console.log("JINR/info_tender_participant.cfg.doSearch()");
    document.infoSpecialForm.submit();
}


var showSpecialNext  = function(shift){
	var start_rec = Number(document.infoSpecialForm.START_REC.value);
	var irpp = Number(document.infoSpecialForm.irpp.value);
	
	if(shift > 0)
		start_rec = start_rec + irpp;
	else if(shift < 0)
		start_rec = start_rec - irpp;
	else
		start_rec = 1;
	if(start_rec < 1) start_rec = 1;
	
	document.infoSpecialForm.START_REC.value = start_rec;
	doSearch();
}


doIt();

    $('##d_spravCont').css({
        width: 900
    });
    $( "##d_sprav_window" ).resizable();  ??

</script>
============ Контейнер для данных =============== ??

</center>
<b>ОШИБКА:</b> #ERROR# ??ERROR
[end]

[get BC info]
try: select div_code as f_bc_div_id, div_code as U_LAB_CODE, sbj as f_sbj, dir
, case when prikaz_n is null then 'n' else 'y' end as "is_prikaz"
from i_jinr_bc
where id=#searchFor#
[end]




