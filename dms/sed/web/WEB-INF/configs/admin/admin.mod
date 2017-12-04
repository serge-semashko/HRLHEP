[comments]
descr=А: Корневой модуль. Загружает скрипты, CSS, форму, выводит закладки режима "Панель Администратора",

parents=main.cfg
childs=dat/common.dat, main.cfg
[end]

[parameters]
debug=off
[end]


[report]
$CALL_SERVICE c=sys/get_curr_user;
$PRINT ====== admin.cfg: USER_ID=#USER_ID#; AR_SYS_ADMIN=#AR_SYS_ADMIN#;
$INCLUDE admin/admin.cfg[admin tabs] ??AR_SYS_ADMIN=1
$CALL_SERVICE c=sys/log_doc_access; doc_id=0;  version=0; access_type=10; rejected=1;   ??!AR_SYS_ADMIN=1
$CALL_SERVICE c=main ??!AR_SYS_ADMIN=1
[end]


[admin tabs]
$SET_PARAMETERS title=СЭД-Админ; request_name=Панель администратора;
$INCLUDE main.cfg[top]

<script type="text/javascript">
/*========================= Admin TABs ====================================*/ ??
/*=========================================================================*/ ??

var t_audit = function() {AjaxCall("c_audit", "c=sys/audit/tab_audit",true,"",true);};
var t_doctypes = function() {AjaxCall("c_doctypes", "c=admin/tab_doctypes");} ;
var t_infos = function(){AjaxCall("c_infos", "c=admin/tab_infos"); }; // Клик на табе "А:Справочники" 
var t_admin_settings = function() {AjaxCall("c_admin_settings", "c=admin/tab_admin_settings");};

var t_info_Data=function() {doSubmit("", "admin/infos/info_data"); }; // Клик на под-табе "А:Справочник / Данные"
var t_info_Settings=function() {doSubmit("", "admin/infos/info_edit"); };   // Клик на под-табе "А:Справочник / Настройки"
</script>

============================= ЗАКЛАДКИ ================================= ??
#c# ??debug=on
<div id="tabs" style="margin: -20px 20px 0 20px; display:none; ">
 <ul id="ltabs">
    <li id="t_audit" class="tabRed"><a href="##dt_audit"><span>Ошибки</span></a></li> 
    <li id="t_doctypes" class="tabBlue"><a href="##dt_doctypes"><span>#^doc_types#</span></a></li>
    <li id="t_infos" class="tabGreen"><a href="##dt_infos"><span>#^adm_infos#</span></a></li> 
    <li id="t_admin_settings" class="tabRed"><a href="##dt_admin_settings"><span>#^settings#</span></a></li>
            ??USER_ID=4918|USER_ID=2309|USER_ID=3663
	</ul>
	<div style="clear:both;"></div>

====================== Контейнеры содержимого закладок ============================ ??
    <div class=ui-corner-all id="dt_monitor"><div id="c_monitor" class="cont topRed">таб мониторинг</div></div> ??
    <div id="dt_audit"><div id="c_audit" class="cont topRed" >...</div></div>
    <div id="dt_doctypes"><div id="c_doctypes" class="cont topBlue">c_doctypes</div></div>
    <div id="dt_infos"><div id="c_infos" class="cont topGreen">c_infos</div></div> 
    <div id="dt_admin_settings"><div id="c_admin_settings" class="cont topRed">c_admin_settings</div></div>
</div>
$INCLUDE main.mod[bottom]
[end]

[preSQLs]
$INCLUDE main.mod[preSQLs]
[end]