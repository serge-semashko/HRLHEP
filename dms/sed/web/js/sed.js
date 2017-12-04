var frm;
var dialogOn = false;
var infoOn = false;
var atab="t_myDocs";
var module="tab_myDocs";
var timeout_id=false;
function t_monitor() {AjaxCall("c_monitor", "c=admin/tab_monitor"); }
function t_myDocs()  {AjaxCall("c_myDocs", "c=tab_myDocs"); } // Клик на табе "Мои документы"
function t_settings() {AjaxCall("c_settings", "c=tab_settings");}
var setTabsClicks=function(tabDivId, act)
{
var a = 0;
if (act) a = act;
$( "#" + tabDivId ).tabs({
active: a,
beforeActivate: function( ev, ui )
{
var old_cont = atab.replace("t_", "c_");
console.log(old_cont + " / " + atab + " => " + ui.newTab[0]["id"]);
if(tabDivId !== "info_tabs")
$(".cont").html(""); //очистить все табы, если работаем не с под-табами (справочник)
try { document.theForm.srt.value=""; } catch (e) {;}
atab=ui.newTab[0]["id"];
eval(atab + "();");
}
});
}
var clickTab=function(id)
{
$("#" + id + " a").click();
}
var setLang=function(lang){
frm.target="";
frm.lang_.value=lang;
frm.c.value='free/main_js_noDB';
frm.submit();
}
var formChanged=false;
var checkChanged=function()
{
if(formChanged)
{	if (confirm('При переходе изменения данных будут потеряны!\n\r\n\rНажмите "OK" для подтверждения перехода,\n\r"Отмена" для сохранения данных'))
formChanged = false;
else
return false;
}
return true;
}
var setChanged=function(){	formChanged=true;}
var setFilter = function(ev) {
var filter = $(this).attr("f");
var val =  $(this).attr("val");
if(filter && val) {
eval("document.theForm." + filter + ".value='" + val + "'");
document.theForm.submit();
}
return false;
}
/*
* Проверка корректности ввода данных пользователем.
* На данный момент проверяет только наличие каких-либо данных в полях с атрибутом mand
* Вызывается из doSubmit() с cop=
*/
var checkInput=function(sel)
{
var msg="";
sel.each( function(ind)
{
var des_id=$(this).attr("name") + "_descr";
if($(this)[0].type == "radio")
{
if(!$('input[name=' + $(this)[0].name + ']:checked').val())
{
if(msg.indexOf($(this).attr("mand"))<0)
msg += ', "' + $(this).attr("mand") + '"';
}
}	else if ($(this).val()=='' && $(this).attr("mand"))
{		msg += ', "' + $(this).attr("mand") + '"';
$("#" + des_id).addClass("MANDATORY");
}
else
$("#" + des_id).removeClass("MANDATORY");
}
);
if (msg)
{	alert("Заполните обязательные поля");
return false;
}
return true;
}
/*мультивыбор из плоских справочников: удаление*/
var delSelectedInfoItemFromChoice = function(el){
where = $(el).attr("delfrom");  //id элемента с id-шниками
what = $(el).attr("delval");   //значение id-шника, который удаляем
wheretext = $(el).attr("delfromtext"); //хидден с текстами в том же порядке, что и id-шники. (если надо)
if(where.length>0 && what.length>0){
val = $('#'+where).val();
tval = $('#'+wheretext).val();
aval = val.split(',');
atval = tval.split(',');
i = aval.indexOf(what);
aval.splice(i,1);
atval.splice(i,1);
$('#'+where).val(aval.join());
$('#'+wheretext).val(atval.join());
$(el).parent().remove();
}
}
var setStandardEvents = function()
{	$('.srh, .filter, .spr_item, .pt, .info_input, .info_input, .fe').unbind();
$('.srh, td.filter, td.spr_item, .pt, .info_input').mouseout(sel);
$('.srh, td.filter, td.spr_item, .pt, .info_input').mouseover(sel);
$('.srh').click(setSrt);
$('.filter').click(setFilter);
$(".resize").resizable();
$('.info_input').click(showSprav);  // события на тестовых полях для справочников
$(".hasDatepick").datepick({yearRange:	'c-2:c+7', showSpeed: 'fast'});
}
var sel = function(ev) {
$("#firstRow").removeClass("sel");
if(ev.type == "mouseover") $(this).addClass("sel");
else if(ev.type == "mouseout")	$(this).removeClass("sel");
return true;
}
var sel_b = function(ev) {
if(ev.type == "mouseover") $(ev.target).addClass("ba");
else if(ev.type == "mouseout") $(ev.target).removeClass("ba");
return false;
}
var setSrt = function(ev) {
var s_new = $(this).attr("sr");
var s_old = frm.srt.value;
console.log(s_new + " / " + s_old);
if (s_new == s_old)
{ if(frm.desc.value == 'desc') frm.desc.value = '';
else frm.desc.value = 'desc';
}
else
frm.srt.value = s_new;
document.theForm.submit();
return false;
}
var showSrt = function(srt, cl) {$("th[sr='" + srt + "']").addClass(cl); }
function goToRow(nr)
{
document.theForm.srn.value=nr;
//  console.log (frm.srn.value);
document.theForm.submit();
}
var dialogMinWidth='';
ShowDialog = function(modal, minWidth)
{
oldCmd = "."; // сбрасываем запомненную предыдущую команду
$('#overlay').height($(document).height()).show();
$("#dialog").show();
if(minWidth) {
dialogMinWidth = minWidth;
if($("#dialog").width() < minWidth)
$("#dialog").width(minWidth);
}
else
dialogMinWidth = '';
if (modal)
$("#overlay").unbind("click");
else
$("#overlay").click(function (e) {HideDialog(); });
$( "#dialog" ).draggable({
handle: '.dialog_title'});
dialogOn = true;
centerDialog();
}
var HideDialog=function()
{
$("#overlay").hide();
$("#dialog").fadeOut(300);
$("#popupCont").html("");
dialogOn = false;
hideToolTip();
}
var showMsg = function(selector, msg)
{
var m= (msg)? msg: $("#loadingMsg").html();
$(selector).each(function() {$(this).html(m); });
}
centerDialog = function(){
if (!dialogOn) return;
var h = document.body.offsetHeight-200;
if (h<200) h=200;
if (h>500) h=500;
if(dialogMinWidth)
if($("#dialog").width() < dialogMinWidth)
$("#dialog").width(dialogMinWidth);
//	t=$(window).scrollTop() + 10;
var top = window.document.body.scrollTop + 50 + (h - $('.dialog').outerHeight())/2 ;
if (top<10) top=10;
$('.dialog').css({
position:'absolute',
left: ($("body").innerWidth() - $('.dialog').outerWidth())/2,
top: top
});
}
$(window).resize(function(){
centerDialog();
});
/*
*	Вызов окна справочника по событию.
*	Из атрибута info_type вызывающего элемента определяется, выводить ли справочник
*  в pop-up окно (тип 1 - плоский, 2 - дерево) или в drop-down окно (тип 3)
*/
var showSprav = function(ev) {
var info_type = $(ev.target).attr("info_type");  //тип справочника, кот.надо вызвать
console.log(info_type);
var is_multi = $(ev.target).attr("multi");
if(info_type=="3") {
if (is_multi=="1"){
showSpravMS(ev);
}else{
showSpravDD(ev);
}
}else{
showSpravPlain(ev);
}
}
/*
*
* @param {type} ev
* @returns {Boolean}
*/
var showSpravMS = function(ev) {
console.log("ms");
var requesterId = $(ev.target).attr("id");  //input field name
var obj = $('#' + requesterId);
var info_id = $(ev.target).attr("info_id");  //id справочника, кот.надо вызвать
var info_view = $(ev.target).attr("info_view");  //представление справочника
console.log(ev);
$(ev.target).after('<div id = "cntntr">xx</div>');
var request = $.ajax({
url: "/sed/dubna",
type: "POST",
data: { "c" : "svs/info_show_ms"
, "info_id" : info_id, view: info_view // справочник, его представление
, "requesterId" : requesterId  // элемент который вызвал справочник (куда возвращать выбор)
},
dataType: "html"
});
request.done(function( msg ) {
$( "#cntntr" ).html( msg );
setStandardEvents();
});
request.fail(function( jqXHR, textStatus ) {
alert( "Ошибка: " + textStatus );
});
ev.stopPropagation();
return false;
}
/*
*	Вызов pop-up окна справочника по событию.
*	Параметры берутся из атрибутов вызывающего элемента
*/
var showSpravPlain = function(ev) {
var requesterId = $(ev.target).attr("id");  //input field name
var obj = $('#' + requesterId);
//		console.log("showSprav: ID=" + requesterId);
var info_id = $(ev.target).attr("info_id");  //id справочника, кот.надо вызвать
var info_view = $(ev.target).attr("info_view");  //представление справочника
var multi = $(ev.target).attr("multi");  //мультивыбор
var searchFor = $(ev.target).attr("searchFor");  //Строка поиска из атрибута инпута
//		if(typeof searchFor == "undefined") searchFor=obj.val(); //строка поиска из значения инпута
if(searchFor == "") searchFor=obj.html(); //строка поиска из HTML элемента
var top = window.document.body.scrollTop + 100 ;
var left = obj.offset().left - 50;
$('#d_sprav_window').css({'top': top, 'left': left}); //ставим координаты pop-up окна
//	  if (timeout_id) clearTimeout(timeout_id);  //таймаут открытия окна справочника. Вроде, и не нужен пока. Пока оставлено на всякий случай.
//		timeout_id = window.setTimeout(doShowSprav, 200);
doShowSprav();
document.theForm.c.value="svs/info_show";
document.theForm.request_param.value="info_id=" + info_id + "&view=" + info_view
+ "&requesterId=" + requesterId + "&searchFor=" + searchFor + "&irpp=10"
+ "&multi="+multi;
document.theForm.submit();
document.theForm.request_param.value="";
return false;
}
/*
*	Собственно открытие окна справочника .
*	Параметры берутся из атрибутов вызывающего элемента
*/
var doShowSprav = function()
{
$('#d_spravCont').html("ЗАГРУЗКА");
//		$('#d_spravCont').html($('#loadingCont').html());
$('#sprav_overlay').height($(document).height()).show();
$('#d_sprav_window').show();
$( "#d_sprav_window" ).draggable({
handle: '.dialog_title'});
infoOn = true;
centerInfo();
}
/*
*	Убрать окно справочника.
*/
var hideSprav = function()
{
$('#d_spravCont').html("");
$('#d_sprav_window').hide();
$('#sprav_overlay').hide();
infoOn = false;
document.theForm.request_param.value="";
hideToolTip();
}
/*
* Поставить заголовок справочника.
* Дергается конкретным справочником при отображении контента
*/
var setInfoName = function(name){
$('#sprav_title').html('Справочник "' + name + '"');
}
/*
* Возврат значения, выбранного из справочника
*/
var pasteInfoResult = function(requesterId, id, text) {
pasteText(requesterId + "_id", id);
pasteText(requesterId, text);
//hideSprav();
$('#dd_info').hide(); //убираем drop-down окно
$('#d_tooltip').hide(); //убираем tooltip окно
}
/*
* Вставка текста в элемент.
* Для input пытаемся вставить текст в document.theForm.id.value,
* если не получилось - то в input.val() по его ID,
* для других элементов - в html элемента по его ID,
*/
var pasteText = function(id, txt){
try {
eval("document.theForm." + id + ".value='" + txt + "';");
} catch(e){
if ($('#' + id).is("input"))
$('#' + id).val(txt); //пытаемся вставить текст в значение инпута
else
$('#' + id).html(txt); //вставляем текст в элемент по ID
}
}
/*
* Центрирование окна справочника на экране
*/
var centerInfo = function(){
if (!infoOn) return;
var h = document.body.clientHeight-200;
if (h<200) h=200;
if (h>1200) h=1200;
var top = window.document.body.scrollTop + 50 + (h - $('#d_sprav_window').outerHeight())/2 ;
// console.log("top=" + top + ". h=" + h + ". d_h=" + $('#d_sprav_window').outerHeight());
if (top<10) top=10;
var left =  ($("body").innerWidth() - $('#d_sprav_window').outerWidth()) / 2;
$('#d_sprav_window').css({
position:'absolute',
left: left,
});
}
/*
*	Вызов окна drop-down справочника по событию.
*	Параметры берутся из атрибутов вызывающего элемента
*/
var showSpravDD = function(ev) {
var requesterId = $(ev.target).attr("id");  //input field name
var obj = $('#' + requesterId);
var info_id = $(ev.target).attr("info_id");  //id справочника, кот.надо вызвать
var info_view = $(ev.target).attr("info_view");  //представление справочника
var top = obj.offset().top + obj.height() + 1; //Y-координата вызывающего элемента
if (top<10) top=10;
var left = obj.offset().left;
console.log("showSpravDD: " + requesterId + "; width:" + obj.width());
$('#dd_info').css({'top': top, 'left': left, 'width': obj.width() + 24 }); //ставим координаты pop-up окна
$('#dd_info').show(200); //показываем Tooltip окно
var request = $.ajax({
url: "/sed/dubna",
type: "POST",
data: { "c" : "svs/info_show_dd"
, "info_id" : info_id, view: info_view // справочник, его представление
, "requesterId" : requesterId  // элемент который вызвал справочник (куда возвращать выбор)
},
dataType: "html"
});
request.done(function( msg ) {
$( "#dd_info" ).html( msg );
setStandardEvents();
});
request.fail(function( jqXHR, textStatus ) {
alert( "Ошибка: " + textStatus );
});
ev.stopPropagation();
return false;
}
/*
* Загрузить в окно tooltip-a произвольный контент и показать tooltop с задержкой
*/
var showToolTip = function(ev, obj) {
try{
var left = obj.offset().left; // X - от источника
var top = obj.offset().top + obj.height() + 5; //Y - от источника
var tt_cfg = obj.attr("tt_cfg");  //модуль вывода содержания тултипа
var tt_id = obj.attr("tt_id");  //id содержания тултипа
var tt_width = obj.attr("tt_width");  //макс.ширина окна тултипа
if(!tt_width) tt_width=obj.width() - 30; //если ширина не указана - берем от источника
var shiftX = obj.attr("shiftX");  //сдвиг окна тултипа по Х
if(shiftX) left = Number(left) + Number(shiftX);
$('#d_tooltip').css({'top': top, 'left': left, 'max-width': tt_width}); //ставим координаты и ширину Tooltip окна
toolTipParam = "c=" + tt_cfg + "&tt_id=" + tt_id;
if (timeout_id) clearTimeout(timeout_id);  //таймаут открытия окна tooltip-a.
timeout_id = window.setTimeout(doShowTooltip, 500);
} catch (e) {alert(e);}
}
/*
* Загрузить в окно tooltip-a контент из указанного справочника и показать tooltop с задержкой
*/
var showInfoToolTip = function(ev, obj) {
try{
var left = obj.offset().left + 30; // X - от источника
var top = obj.offset().top + obj.height() + 5; //Y - от источника
var width = obj.width() - 30; //макс.ширина - от источника
$('#d_tooltip').css({'top': top, 'left': left, 'max-width': width}); //ставим координаты и ширину Tooltip окна
var info_id = obj.attr("info_id");  //id справочника
var view = obj.attr("view");  				//представление справочника
var recordId = obj.attr("recordId");  //id записи, кот нужно отобразить
toolTipParam = "c=svs/showInfoTooltip&info_id=" + info_id + "&view=" + view + "&recordId=" + recordId;
if (timeout_id) clearTimeout(timeout_id);  //таймаут открытия окна tooltip-a.
timeout_id = window.setTimeout(doShowTooltip, 500);
} catch (e) {alert(e);}
}
var toolTipParam = "";
/*
* Показать окно tooltip-a
*/
var doShowTooltip=function() {
if (timeout_id) clearTimeout(timeout_id);  //на всякий случай сбрасываем таймаут открытия окна tooltip-a.
if(toolTipParam) {
$('#d_tooltip').html("ЗАГРУЗКА...");
AjaxCall("d_tooltip", toolTipParam, true); //загружаем tooltip контент
$('#d_tooltip').show(); //показываем Tooltip окно
}
}
/*
* Убрать окно tooltip-a
*/
var hideToolTip = function(){
if (timeout_id) clearTimeout(timeout_id);  //сбрасываем таймаут открытия окна tooltip-a.
$('#d_tooltip').hide(); //убираем Tooltip окно
}
var showPageTop = function(show){
var top = -92; //На сколько сдвинуть окно вверх
var t = 100;
if(show) { top = 0; t = 500; }
$( "#tabs" ).animate({ top: top }, t, function() { });
}
var showLoading=function(targetDiv)
{
$("#" + targetDiv).html($("#loadingMsg").html() );
}
var toggleDiv = function(target_div, delay) {
if ($('#' + target_div).css('display') == 'none' ) {
$('#' + target_div).show(delay);
return true;
}
else {
$('#' + target_div).hide(delay);
return false;
}
}
var getObjectById=function(elementId) { return document.getElementById(elementId);}
IsNumeric = function(input)
{ return (input - 0) == input && input.length > 0;
}
var mouseX, mouseY;
$(document).mousemove(function(e) {
mouseX = e.pageX;
mouseY = e.pageY;
});
var setElement = function(div_id, txt)
{
if(div_id && txt)
$("#"+div_id).html(txt);
}
var replaceAll = function(src, what, replacement) {
return src.replace(new RegExp(what,'g'),replacement);
}
var fixSQL_TEXT = function(toDB){
if(toDB)
document.theForm.SQL_TEXT.value=replaceAll(document.theForm.SQL_TEXT.value, "'", "''");
else
document.theForm.SQL_TEXT.value=replaceAll(document.theForm.SQL_TEXT.value, "''", "'");
}
/*
* Submit document.theForm в заданный фрейм (если он указан)
* Ставит в форме значения "cop", "c" (если они указаны)
* Убирает диалоговое окно (если !keepDialog == true)
* если cop="u" | "save", то перед сабмитом вызывается checkInput() для проверки корректности данных формы
*/
var doSubmit=function(cop, c, keepDialog, frame)
{
console.log("doSubmit: c=" + c + "; frame=" + frame + ";");
formChanged=false;
frm=document.theForm;
if(frame) frm.target=frame;
if(cop && (cop=='u' || cop=='save') && !checkInput($("[mand]"))) return; //проверка ввода
if(c) {
var c_Old = frm.c.value; //сохраняем старый "c"
frm.c.value=c;
}
frm.cop.value=cop;
frm.submit();
if (!keepDialog) HideDialog();
if(c)	frm.c.value = c_Old;  //возвращаем c
frm.target="wf";
}
var setModule=function(m)
{
if(m) {
module=m;
document.theForm.c.value=module;
}
}
/*
* AJAX-загрузка контента в target_div.
* query - запрос в URL-формате (param1=val1&param2=val2)
* Запрос выполняется только если doIt=TRUE.
* Если doIt не задано или не boolean, то запрос выполняется, если элемент target_div не скрыт.
*/
var AjaxCall=function(target_div, query, doIt) {
var vis = false;
if(typeof (doIt) == "boolean") vis = doIt;
else vis = $('#' + target_div).css('display') != 'none';
if ( vis ) {
if(target_div !== "d_tooltip"
&& target_div !== "field_property_panel"
&& target_div !== "doctype_form_panel"
&& target_div !== "toolbar_panel"
)
showLoading(target_div);
var data = {};
var params = query.split("&");
params.forEach(function(item) {
var p = item.split("=");
data[p[0]] = p[1];
});
data["ajax"] = "Y";
var request = $.ajax({ url: "/sed/dubna", type: "POST", data:data, dataType: "html" });
request.done(function( msg ) { if(target_div) $( "#" + target_div ).html( msg ); setStandardEvents(); });
request.fail(function( jqXHR, textStatus ) { alert( "Ошибка: " + textStatus ); });
}
return vis;
}
/*
* Непосредственная загрузка URL в iframe
*/
function loadFrame(param, frame, host)
{
alert(" loadFrame !!!!!");
}
/*
* Возврат результатов из iframe (из модуля) в основной документ браузера
*
*/
var getResult = function(div_ids, res, visibleOnly)
{
if(div_ids && res) {
var divs = div_ids.split(",");
var i;
for (i=0; i<divs.length; i++) {
if(visibleOnly) {
if( $("#"+divs[i]).is(':visible') ) {
$("#"+divs[i]).html(res.innerHTML);
}
else
$("#"+divs[i]).html("");
}
else {
$("#"+divs[i]).html(res.innerHTML);
}
}
if(res.innerHTML)
{ setStandardEvents();
}
if (div_ids==="popupCont") centerDialog();
}
}
