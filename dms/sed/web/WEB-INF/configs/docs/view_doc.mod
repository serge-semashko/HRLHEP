[comments]
descr=U: Просмотр документа

input=doc_id - ID документа (из таблицы d_list)
output=HTML форма просмотра документа, 
parents=docs/docs_in_progress_table.cfg
childs=docs/view_doc_no_wf.cfg, docs/view_doc_wf.cfg
test_URL=?c=docs/view_doc&doc_id=985
author=Куняев
[end]

[description]
Общая часть:
<ul>
<li>Получение информации о структуре документа</li>
<li>Вывод шапки документа</li>
<li>Вызов docs/view_doc_no_wf.cfg для документов без WF</li>
<li>Вызов docs/view_doc_wf.cfg для документов с WF</li>
<li>Вызов obj/view_object.cfg для отображения объекта</li>
<li>Вывод завершения</li>
</ul>

[end]


[parameters]
request_name=U:Просмотр документа
LOG=ON
SYS_FIELDS=DOC_ID, IS_ACTIVE, DATE_FORMAT(modified,'#dateTimeFormat#') as DOC_MODIFIED, VERSION
SYS_FIELDS_TYPES=int,int,varchar,int
divider=<tr><td colspan=2 class="divider"></td></tr> 
[end]


[report]
    $INCLUDE dat/common.dat[check login]
    $LOG1 <b>============== view_doc: doc_id=#doc_id#; USER_ID=#USER_ID#; ==================</b><br>
    $CALL_SERVICE c=sys/getARUD; ??!AR_R=Y&USER_ID
    $SET_PARAMETERS AR_E=; AR_W=; ??
    $SET_PARAMETERS WF_ID=#WF_ID_INACT#;  CURR_STEP=0; ??!WF_ID

    $INCLUDE [OK report]  ??AR_R=Y
    $CALL_SERVICE c=sys/log_doc_access; doc_id=#doc_id#; rejected=1;   ??!AR_R=Y
[end]


[OK report]
    $SET_PARAMETERS AR_W=Y; AR_T=Y; ??USER_ID=2309&ZZZ
    $SET_PARAMETERS request=read;
    $INCLUDE dat/common.dat[head] ??mode=print|mode=ext
    <style>
    table.tlist tr.bold td{font-weight:bold;}
    table.tlist tr.gray td, table.tlist tr.gray td a{color:##808080;}
    table.doc tr td {padding:5px;}
    table.doc tr td {padding:7px 7px 10px 5px;}
    table.doc tr td.label {padding:10px 7px 10px 0;} ??
    </style>
    </head> ??mode=print|mode=ext

    $SET_PARAMETERS DOC_DATA_RECORD_ID=;
    $GET_DATA [getDocInfo]

    $INCLUDE docs/custom_settings.cfg[set custom parameters]
    $SET_PARAMETERS AR_T=; AR_E=; AR_S=; AR_X=; ??is_deleted=1
    $GET_DATA docs/set_doc_number.cfg[auto set doc number SQL] ??SET_NUMBER_AT_STEP=1&!NUMBER&AR_S=Y|AR_X=Y

    $INCLUDE [doc title] ??!mode=popup&!doc_mode=object&!mode=print
    $INCLUDE [doc head]  ??!doc_mode=object

    $LOG2 ===== HAS_WF=#HAS_WF#; doc_mode=#doc_mode#; DOC_DATA_RECORD_ID=#DOC_DATA_RECORD_ID#; =============<br>
    $CALL_SERVICE c=docs/view_doc_no_wf;    ??!HAS_WF=1&DOC_DATA_RECORD_ID&!doc_mode=object
    $CALL_SERVICE c=docs/view_doc_wf;    ??HAS_WF=1&!doc_mode=object
    $CALL_SERVICE c=obj/view_object.cfg;     ??doc_mode=object
    $INCLUDE [doc not found]  ??!DOC_DATA_RECORD_ID

    $INCLUDE [doc bottom] ??!doc_mode=object
[end]

[doc not found]
    <br><br><br><br><b>ОШИБКА: Документ не найден!</b><br>
    <input type="button" class="butt1 pt" style="width:120px; margin:40px 0 30px 0;" onClick="showDoc(false); " value='Закрыть'> 
    $SET_PARAMETERS ERROR=Документ #doc_id# не найден!
[end]

[doc title]
++++++++++++++++++ Верхний заголовок - ссылка на главную  +++++++++++++++++++++ ??
    <small>#c#</small> ??debug=on
    <div style="float:right; color:##808080;">#user_IOF#</div>
    <div class="pt" style="width:190px; height:50px;"  onClick="AjaxCall('c_myDocs', 'c=tab_myDocs');">  ??!admin
    <img src="#imgPath#logo_#lang#.png" style="width:180px; height:50px;"><span style="color:##808080;"><u>На главную</u></span></div> ??!admin
    <div style="clear:both;"></div>
[end]


[doc head]
++++++++++++++++++ Шапка документа  +++++++++++++++++++++ ??
    $INCLUDE [test links]  ??USER_ID=2309&!mode=print
    <center>
    <table border=0 cellpadding=0 cellspacing=0 class="doc" style="min-width:900px;">
    <tr><td colspan=2 style="text-align:right;"><i><u>конфиденциально</u></i></td></tr> ??DOC_IS_CONFIDENTIAL=1

    <tr><th class="center bg_white big" 
        style="background-color:##E3FFC2;" ??DOC_TYPE_ID=1
        style="background-color:##C9EBF5;"  ??DOC_TYPE_ID=8
     colspan=2>
    $INCLUDE [mark doc important]  ??!mode=ext

    $INCLUDE [linked docs] 

    <b>#DOC_TYPE# <span id="doc_reg_number">
    №#NUMBER# ??NUMBER
    от #DOC_DATE# ??DOC_DATE
    $INCLUDE [set doc number field]   ??!mode=print&SET_NUMBER_AT_STEP=3&AR_S=Y|AR_X=Y
    </span>
    <span class="bg_red">УДАЛЕН</span> ??is_deleted=1
    </b>
    $INCLUDE [set doc number button]   ??!mode=print&SET_NUMBER_AT_STEP=2&!NUMBER&AR_S=Y|AR_X=Y

    <div style="clear:both;"></div>

    <br><b>#TITLE#</b> ??
    <div class="mw1000" style="font-weight:bold; padding:10px 10px 0 10px;">#TITLE#</div>

    <input type="button" class="butt1 pt" style="margin-left:100px;" value='в шлюз=>' onClick='AjaxCall("c_settings", "c=gateway/post_doc&doc_id=#doc_id#", true);' >  ??USER_ID=2309_ZZZ

    </th></tr>

    $INCLUDE [WARNING MSG]  ??ZZZ&CURR_STEP<2&DOC_TYPE_ID=8|DOC_TYPE_ID=9

    $INCLUDE [WF cancelled] ??WF_CANCELLED=Y
    #divider# 
[end]


[mark doc important]
    <div style="float:left;">
    <input type=checkbox onClick='AjaxCall("c_settings", "c=docs/markImportant&doc_id=#doc_id#&imp=" + this.checked, true);
        if(this.checked) $(".mark").removeClass("clr-gray").addClass("clr-red"); 
        else $(".mark").removeClass("clr-red").addClass("clr-gray");'
    checked ??MARKED
    ><i class="fa fa-flag mark
    clr-gray ??!MARKED
    clr-red  ??MARKED
    " aria-hidden="true"></i>
    </div>
[end]


[linked docs]
    <div id="d_linked_docs" style="float:right; border:none 1px gray;">
    <span class="pt bottom_dotted" onClick="showLinkedDocs();">Связанные документы (#NUM_LINKED#) <img src="#imgPath#show.gif"></span> ??DOC_PID|NUM_LINKED>0

    $SET_PARAMETERS CanClone=Y; ??!mode=print&!mode=ext&user_roles&DOC_STATUS>#~doc_status_in_progress#|USER_ID=2309|AR_SYS_ADMIN=1
    <input type="button" class="butt1 pt" style="margin-left:20px;" value='Создать на основании' onClick="AjaxCall('popupCont', 'c=docs/choose_child_type&doc_id=#doc_id#', true);">    ??CanClone=Y&!is_deleted=1&NUM_CHILD_TYPES>0
    </div>
    $INCLUDE [linked docs div]  ??DOC_PID|NUM_LINKED>0
    NUM_LINKED=#NUM_LINKED#; ??
[end]



[linked docs div]
    <div id="linked_cont" style="position:absolute; padding:-3px 0 0 0; top:80px; right:10px; width:600px; display:none; box-shadow: 5px 5px 5px rgba(68,68,68,0.6); border:solid 1px ##1f697d; background-color:white;"></div>
    <script>
    var showLinkedDocs=function(){
      var cont = $("##linked_cont");
      if(cont.css('display') != 'none') cont.hide(100)
      else {
        var obj = $('##d_linked_docs'); 
        var top = obj.offset().top + obj.height(); //Y-координата вызывающего элемента 
        cont.css({'top': top}); //ставим координаты pop-up окна

        var left = obj.offset().left + obj.width() - 1; //Y-координата вызывающего элемента ??
           left = left - cont.width() - 10; ??
           $('##linked_cont') ??
         , 'right': 0 , 'width': obj.width() + 24 ??
        alert(obj.width() + ":" + top); ??

        cont.show(200); 
        AjaxCall('linked_cont', 'c=docs/show_linked_docs&doc_id=#doc_id#&mode=#mode#', true);
      }
    }
    </script>
[end]



[WF cancelled]
    $GET_DATA docs/view_doc.cfg[get cancel info]
    <tr><th class="center bg_white big" colspan=2>
    Документ <b>отозван</b> пользователем #TERMINATOR#, #WHEN_TERMINATED#<br>
    <small>(#TERMINATOR_ROLE#)</small><br> ??TERMINATOR_ROLE
    комментарий: #TERMINATOR_COMMENT#  ??TERMINATOR_COMMENT
    </th></tr>
[end]


[set doc number button]
    <button class="butt1 pt" id="register_button" onclick = "event.preventDefault(); event.stopPropagation(); 
    if(confirm('Присвоить документу номер и дату?')) {
        AjaxCall('doc_reg_number', 'c=docs/set_doc_number&doc_id=#doc_id#&auto=y', true); $('##register_button').hide(200);
    }">Зарегистрировать</button>
[end]

[set doc number field]
    $CALL_SERVICE c=docs/set_doc_number; doc_id=#doc_id#
[end]

<script>
    AjaxCall('doc_reg_number', 'c=docs/set_doc_number&doc_id=#doc_id#', true);
</script>

<button class="butt1 pt" id="register_button" onclick = "event.preventDefault(); event.stopPropagation();  ??
    AjaxCall('doc_reg_number', 'c=docs/set_doc_number&doc_id=#doc_id#', true);  ??
 $('##register_button').hide(200); }">Зарегистрировать</button> ??
[end]




[doc bottom]

    <input type="button" class="butt1 pt" style="width:80; margin-right:10px;" value='Наверх' onClick="window.scrollTo(0, 0);">  &nbsp; ??!admin&!mode=popup&!mode=print&!mode=ext
    $INCLUDE [print links] ??DOC_DATA_RECORD_ID&!mode=print

    <iframe width=800 height=0 frameborder="0" name="wf" id="wf"></iframe>  ??mode=ext

    <br>
    $INCLUDE [sys]  ??AR_SYS_ADMIN=1&!mode=print
      ??|VU

    $CALL_SERVICE c=sys/log_doc_access; version=#VERSION#; 

!!!!!!!!!!! HARDCODE !!!!!!!!!!! ??
    $INCLUDE dat/doc_summ_nds.dat[script] 

    <script type="text/javascript" language="javascript">
    showDoc(true, "Просмотр документа"); 

    showPageTop(false); ??!mode=popup&!mode=print
    window.scrollTo(0, 0); $('##doc_container').show(100); $( "##doc_container" ).draggable({handle: '##doc_container_title'}); ??mode=popup

    window.setTimeout(function(){window.print();}, 700);  ??mode=print

    AjaxCall('popupCont', 'c=svs/loginMessage');   ??sid&ZZZ
    AjaxCall('doc_content', 'c=docs/edit_doc&doc_id=#doc_id#', true, '', true);    ??doEdit=Y
    </script>

[end]

[print links]
    <center>
    $INCLUDE docs/custom_settings.cfg[print links]
    <a href="#ServletPath#?c=docs/print_doc&doc_id=#doc_id#" target="_blank"> <img src="#imgPath#printer.gif"> Распечатать документ</a>  ??!hideStandartPrintLink=Y
    $SET_PARAMETERS spec_form=y;   ??WF_ID&DOC_TYPE_ID=6|DOC_TYPE_ID=8|DOC_TYPE_ID=11|DOC_TYPE_ID=15
    <a href="#ServletPath#?c=wf/print_wf&doc_id=#doc_id#" target="_blank"><img src="#imgPath#printer.gif">Распечатать лист согласования</a>  ??spec_form
     <a href="#ServletPath#?c=wf/print_wf_old&doc_id=#doc_id#" target="_blank"><img src="#imgPath#printer.gif">Распечатать маршрут документа</a>  ??WF_ID&!spec_form
    </center> 
[end]


[print forms]
    $GET_DATA docs/view_doc.cfg[check print forms]
    $CALL_SERVICE c=docs/doc_out_files;  ??!ERROR&REFRESH_PRINT_FORM=Y&NUM_PRINT_FORMS>0
    #divider#<tr><td class="label">Печатные формы <br> документа:</td><td> ??NUM_PRINT_FORMS>0
    $CALL_SERVICE c=docs/out_files_list ??NUM_PRINT_FORMS>0
    </td></tr> ??NUM_PRINT_FORMS>0
[end]


==============================================================================
***************************** ОТЛАДОЧНОЕ ***************************
==============================================================================


[sys]
<a href="#ServletPath#?c=docs/doc_out_files&doc_id=#doc_id#" target="_blank">FORMS...</a>
doc_id=#doc_id#; R=#AR_R#; S=#AR_S#; W=#AR_W#; E=#AR_E#; X=#AR_X#; T=#AR_T#; A=#AR_A#; status=#DOC_STATUS#, step_type=#CURR_STEP_TYPE#, deleted=#DOC_IS_DELETED#, WF_ID=#WF_ID#; WF cancelled=#WF_CANCELLED#;</small>   ??!cache_cfg=true&USER_ID=2309|USER_ID=3663
&nbsp; <a href="#ServletPath#?c=files/sys/convertDocFiles&doc_id=#doc_id#" target="_blank"> convert files </a>  ??!doc_mode=object&USER_ID=2309
<br>

$INCLUDE [AR test links] ??USER_ID=2309|USER_ID=3663
 ??|VU

[end]


[test links] ******* ссылки для отладки 
<a href="#ServletPath#?c=gateway/post_doc&doc_id=#doc_id#&mode=print" target="_blank">POST>></a> ??gateway_object_id>0&USER_ID=2309|USER_ID=9635
<a href="#ServletPath#?c=docs/print_doc&doc_id=#doc_id#" target="_blank">print</a> ??DOC_TYPE_ID=15&USER_ID=2309|USER_ID=9635
[end]

[AR test links] ******* ссылки для отладки новой системы прав доступа и др.
<b>new AR tests:</b>
<div id="doc_attrs">
$CALL_SERVICE c=sys/ar/set_doc_conditions; show=Y;
</div>

<span class="pt" onClick="AjaxCall('doc_attrs','c=sys/ar/set_doc_attrs&doc_id=#doc_id#&mode=print&setPermit=Y');">Установить атрибуты док.#doc_id#</span> |

<a href="#ServletPath#?c=sys/ar/list_doc_permits&doc_id=#doc_id#" target=_blank>Разрешения на док.#doc_id#</a> |

<a href="#ServletPath#?c=sys/ar/set_doc_permits&doc_id=#doc_id#" target=_blank>Установить права на док.#doc_id#</a> |


<a href="#ServletPath#?c=sys/ar/list_user_docs&user_person_id=#USER_ID#" target=_blank>Все документы юзера #USER_ID#</a> |
<a href="#ServletPath#?c=sys/ar/set_user_permits&user_id=#USER_ID#" target=_blank>Поставить все права юзера #USER_ID#</a> |

<a href="#ServletPath#?c=sys/ar/set_all_docs_attrs" target=_blank>Поставить атрибуты всем документам</a> |
<a href="#ServletPath#?c=sys/ar/&user_person_id=#USER_ID#" target=_blank>Все документы с атрибутами</a> ??

[end]


<a href="#ServletPath#?c=sys/ar/check_doc_rule&user_id=#USER_ID#&doc_id=#doc_id#&rule_nr=1" target=_blank>Права ю.#USER_ID# на док. #doc_id#</a> |

<a href="#ServletPath#?c=sys/ar/set_user_rules&user_person_id=#USER_ID#" target=_blank>u-rules:#USER_ID#</a> 

==============================================================================
***************************** Шаблоны SQL запросов ***************************
==============================================================================
[getDocInfo]
select 
  dtp.NAME as "DOC_TYPE", dtp.ID as "DOC_TYPE_ID", dtp.is_autonumber, dtp.gateway_object_id
 , concat('d_data_', cast(dtp.id as char)) as TABLE_NAME
 , dh.type_version as "DOC_TYPE_VERSION", dh.pid as PARENT_DOC_ID, dh.TITLE, dh.NUMBER, dh.STATUS
 , dh.comment as "INITIATOR_COMMENT"
 , dh.pid as "DOC_PID"

 , u.FIO as CREATOR, u.email as "CRE_MAIL", u.phone as "CRE_PHONE", u.ID as CREATOR_ID
 , uu.FIO as MODIFIER
 , ifnull(DATE_FORMAT(dh.doc_date,'#dateFormat#'),'') as DOC_DATE
 , ifnull(DATE_FORMAT(dh.created,'#dateTimeFormat#'),'') as CREATED
 , ifnull(DATE_FORMAT(dh.modified,'#dateTimeFormat#'),'') as MODIFIED
, dg.has_wf as "HAS_WF", dh.is_deleted, dm.doc_id as "MARKED"
from d_list dh 
left join d_types dtp on dtp.Id = dh.type_id
left join doc_groups dg on dg.Id = dtp.group_id
left join #table_users_full# u on u.Id = dh.creator_id
left join #table_users_full# uu on uu.Id = dh.modifier_id
left join d_marked dm on (dm.doc_id=dh.id and dm.user_id=#USER_ID#)
where dh.Id = #doc_id#
; 

try: select /* LAST doc data record ID */ id as DOC_DATA_RECORD_ID 
from #TABLE_NAME# 
where doc_id = #doc_id# and is_active=1
 and version=#DOC_VERSION# ??DOC VERSIONS NOT IMPLEMENTED
order by modified desc
limit 0,1
;

try: select concat(dtf.field_db_name, ',') as FIELDS
 , concat(dtf.name, ',') as FIELDS_NAMES 
 , concat(dtf.type, ',') as FIELDS_TYPES
 , concat(dtf.form_field_type, ',') as FORM_FIELDS_TYPES
from d_fields dtf
where dtf.type_id = #DOC_TYPE_ID#
    and dtf.is_visible=1 ??
    and dtf.is_active=1 
    and dtf.min_doc_type_version <= #DOC_TYPE_VERSION#  
    and dtf.max_doc_type_version >= #DOC_TYPE_VERSION#  
    and dtf.field_category>1  ??request=write 
order by nr
;
try: select count(dl.id) 
+ 1 ??DOC_PID
as NUM_LINKED from d_list dl 
join d_types dt on dt.id=dl.type_id and dt.group_id<>99 
where dl.pid=#doc_id# 
and dl.is_deleted=0 
;
try: select count(id) as NUM_CHILD_TYPES from d_types_children where type_id=#DOC_TYPE_ID#
[end]


Общий запрос для view_doc_no_wf.cfg, view_doc_wf.cfg, doc_edit.cfg, obj/edit_object.cfg
[SQL_]
select /* doc data fields description */
  dtf.id as "FIELD_ID", dtf.NR, dtf.NAME, dtf.TYPE, dtf.SIZE, dtf.FORM_FIELD_TYPE, dtf.FIELD_DB_NAME, dtf.field_category as FIELD_CATEGORY
, dtf.NULLS
, dtf.IS_ACTIVE, dtf.IS_VISIBLE, dtf.rw_4_roles
, dtf.INFO_ID, dtf.info_view_nr as INFO_VIEW
, case when dtf.ly is null then 'Y' else '' end as "AUTORESIZE" ??
, case when dtf.autoResize=1 then 'Y' else '' end as "AUTORESIZE" 
, ifnull(dtf.lx, 400) as LX, ifnull(dtf.ly, 40) as LY, ROUND((ifnull(dtf.ly, 40) + 5)/15) as ROWS
, dtf.maxWidth
, ft.src_file as "FIELD_SRC_FILE"

, ft.section_r  ??request=read
, ft.section_w   ??!request=read 
    as "FIELD_SECTION"
, ft.section_w  as "FIELD_SECTION_RW"

from d_fields dtf
left join d_form_fields_types ft on (ft.id = dtf.form_field_type)
left join d_list dh on dtf.type_id = dh.type_id
where dh.Id = #doc_id# 
    and dtf.is_visible=1 
    and dtf.is_active=1 ??!ADMIN_REQUEST=Y
    and dtf.min_doc_type_version <= #DOC_TYPE_VERSION#  
    and dtf.max_doc_type_version >= #DOC_TYPE_VERSION#  
    and dtf.field_category>1  ??request=write 
order by nr
[end]


[get added comments]
select concat('<tr><td>&nbsp;&nbsp;</td><td class="small nowrap">'
 , DATE_FORMAT(wf.finished,'#shortDateTimeFormat#')
 , case when wf.user_id=wf.modifier_id then ', <b>' else ', ' end
 , iof(u.F, u.I, u.O)
 , case when wf.user_id=wf.modifier_id then '' else concat('<b> (', iof(um.F, um.I, um.O), ')') end
 , ':</b></td><td class="bg_white">', wf.comment, '</td></tr>') as ADDED_COMMENTS
from wf 
 left join #table_users_full# u on u.id=wf.user_id
 left join #table_users_full# um on um.id=wf.modifier_id
where wf.wf_id=#WF_ID# and not wf.comment is null and not wf.comment='' 
and wf.step_type=#~wf_step_in_progress#
and not wf.step_type=#~wf_step_signed# ??
order by wf.finished
;
[end]


[get FYI comments]
select concat('<tr><td>&nbsp;&nbsp;</td><td class="small nowrap">'
  , DATE_FORMAT(s.dat,'#shortDateTimeFormat#')
  ,', <b>', iof(u.F, u.I, u.O), '=>', iof(uu.F, uu.I, uu.O), ':</b></td><td class="bg_white">',s.comments,'</td></tr>') as FYI_COMMENTS
from d_sent_FYI s 
left join #table_users_full# u on u.id=s.from_id
left join #table_users_full# uu on uu.id=s.user_id
where s.doc_id=#doc_id# and not s.comments is null and not s.comments=''
and (s.from_id=#USER_ID# or s.user_id=#USER_ID#)
order by s.dat
;
[end]


[check sent FYI]
select /* Was sent FYI? */ count(*) as "NUM_SENT_FYI" from d_sent_FYI s where s.doc_id=#doc_id#;
[end]


[check print forms]
select count(id) as NUM_PRINT_FORMS from doc_out_files where doc_id=#doc_id#;
[end]


[get cancel info]
select wfh.user_fio as TERMINATOR, wfh.user_role as TERMINATOR_ROLE
,DATE_FORMAT(wfh.processed, '#dateTimeFormat#') as WHEN_TERMINATED
,wfh.comment as TERMINATOR_COMMENT
from wf_history wfh
left join #table_users_full# u on u.id=wfh.user_id ??
where wfh.doc_id=#doc_id# and wfh.result_code=#~doc_action_terminate#
order by id desc
limit 1
[end]



ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ
НА ДАННЫЙ МОМЕНТ НЕ ИСПОЛЬЗУЕТСЯ
[ZZZ WARNING MSG]
<tr><td colspan=2><div class="big center" style="width:800pt; font-size: 13pt; margin:0 0 10px 100px; background-color:white; border:solid 1px red;">
На данный момент документ "#DOC_TYPE#" выложен в СЭД <b>в ТЕСТОВОМ РЕЖИМЕ!</b><br>Реальная обработка документа пока не производится.<br><hr> ??
Ознакомьтесь с <a href="#ServerPath##ContextPath#/info/doginfo.html" target=_blank><b>типовыми формами</b></a> договоров
и с <a href="#ServerPath##ContextPath#/info/doginfo.html" target=_blank>инструкцией</a> по процедуре согласования договоров в СЭД. ??
</div> </td></tr>
[end]