
var imgPath="/arch/images/";

/*====== Непонятное (используется ли?) =======*/

function Show()
{
//alert(frm.NEWDOCTYPE.selectionStart);
}

function refreshList(lastPath)
{ 
//alert (lastPath);
setTimeout("listfiles.refresh()",2000); 
}


/*====== Обработка формы (сохранить, удалить и проч.) =======*/
function block(){
    if(getDocState()!=''){
        alert('Перед блокировкой документ должен сохранен!');
        return;
    }
    frm.IS_BLOCKED.value = '1';
    force_to_view();
    console.log("force to view");
}
 function force_to_view()
 { 
     console.log("force to view");
     if((getDocState()!=''))
	 { if (! confirm("Изменения не сохранены и будут потеряны\n\nПерейти в просмотр?"))
			return;
	 }
		document.theForm.to_view.value='force_to_view';
		top.showTree();
		document.theForm.submit();
 }

function save_form()
{ if (!checkInt("REGNUM",0,99999999,"Рег. номер")) return; 
  if (!checkDate("DAT_DOC", "документа", false)) return;
  if (!checkDate("DAT", "регистрации", false)) return;
  try
	{  chYear();
  }
  catch(e){  }
  
	frm.cop.value='save';
	frm.submit();
}

function goDel(){
if (confirm('Внимание!\nТекущий документ будет удален.\nВы настаиваете на удалении?')){
	frm.cop.value='delete';
	frm.submit();}
}

function load(cop){
	if (cop=='delete'){
 		alert('Документ удален.'); 
		frm.c.value='main';
		frm.submit();
		top.hideTree("N");
	}
	if (cop=='moveToArch'){
		alert('Документ перемещен в архив.');
		frm.c.value='doc/view';
		frm.submit();
		top.hideTree("N");
	}

}

function cl_win(autonumber)
{
	if((getDocState()!='')){
		if ( confirm('Изменения не сохранены!\nЗакрыть окно?')){
				if(document.theForm.DAT_CH.value.length<1
				&& (autonumber != "Y")){
					frm.c.value='/doc/doc_killer';
					frm.submit();
					top.hideTree("N");
				}else{
					frm.c.value='main';
					frm.submit();
					top.hideTree("N");
				}
		}
	} else {
		if(document.theForm.DAT_CH.value.length<1
		&& (autonumber != "Y")){
					frm.c.value='/doc/doc_killer';
					frm.submit();
					top.hideTree("N");
		}
		else{
					frm.c.value='main';
					frm.submit();
					top.hideTree("N");
		}
	}
}

function goto_view(){
	if((getDocState()!='')){
			if ( confirm('Перейти в список БЕЗ СОХРАНЕНИЯ изменений?')){
						frm.c.value='main';
						frm.cop.value='';					
						frm.submit();
						top.hideTree("N");
			}
	} 
	else {
						frm.c.value='main';
						frm.cop.value='';
						frm.submit();
						top.hideTree("N");
	}
}

function goto_arch(){
	if((getDocState()!='')){
			if ( confirm('Перенести документ в архив БЕЗ СОХРАНЕНИЯ изменений?')){
						//frm.c.value='';
						frm.cop.value='moveToArch';					
						frm.submit();
					//	top.hideTree("N");
			}
	} 
	else {
					//	frm.c.value='main';
						frm.cop.value='moveToArch';
						frm.submit();
			//			top.hideTree("N");
	}
}


/*====== Обработка РАССЫЛКИ =======*/

function setSent(tsn,tse)
{	frm.sent.value=tsn;
	frm.SENTIDS.value=tse;
}

function getSentIDs()
{	return frm.SENTIDS.value;
}

function noAddr()
{ alert('Выберите адресатов для рассылки!')
}

function sendMailAccept(op)
{   
    if (getDocState()!='')
	{	alert('Перед рассылкой необходимо сохранить документ!');
		return;
	}
        var confirmMsg = "";
        switch(op){
            case 1:
                confirmMsg = "Послать приглашение инициатору сейчас?";
            break;
            case 0:
                confirmMsg = "Разослать приглашения согласующим сейчас?";
            break;
            case 2:
                confirmMsg = "Послать в юр.отдел?";
            break;
            case 4:
                confirmMsg = "Отправить письма согласующим?";
            break;
            case 5:
               // confirmMsg = "Отправить письмо повторно?";
                op=4;
            break;
        }
    if(confirmMsg=="" || confirm(confirmMsg)) {        
        frm.target="fr_accept";
        frm.c.value="doc/sendMailsAccept";
        frm.IS_INITIATOR.value = op;
        frm.submit();
        frm.target="";
        frm.c.value="doc/docDet";
        frm.IS_INITIATOR.value = "";
        frm.IS_LEGAL_DEPT.value = "";
}
    return false;
}

function getAcceptList(id){
var request = $.ajax({
    url: "/arch/arch",
    type: "POST",
    data: { c : "doc/acceptList", DOCID : id, target : "foredit" },
    dataType: "html"    
    });
    request.done(function( msg ) {
        $( "#acceptlistplace" ).html( msg );
    });
    request.fail(function( jqXHR, textStatus ) {
        alert( "Ошибка: " + textStatus );
    });
}



function sendMails()
{ if (getDocState()!='')
	{	alert('Перед рассылкой необходимо сохранить документ!');
		return;
	}
	
	if((frm.SENTIDS.value.length==0))
	{ noAddr();
	}
	else
	{	frm.target="fr_send";
    frm.c.value="doc/sendMails";
    frm.submit();
    frm.target="";
    frm.c.value="doc/docDet";
  }
}


function selectMails(usrid)
{	//alert(top.frames["TREE_PARENT"].window.location.href);
	top.frames["TREE_PARENT"].window.location.href="?c=doc/ab&USRID_="+usrid
	top.showP();
}

/*================== ПРОЧЕЕ ==================*/

function goReset()
{
	frm.reset();
	form_reset();
}
	
function chYear()
{ if ((getCookie('doc_year')!='NULL')&&(frm.DOC_YEAR.value!='NULL'))
	{	setCookie('doc_year',frm.DOC_YEAR.value);
	}
}

function multisel(s){
	s=s.substring(0,s.length-1);
	s1=s.split(",");
		for (i=0;i<document.theForm.LAB_CODE.options.length;i++){
			document.theForm.LAB_CODE.options[i].selected=false;
				for (ii=0;ii<s1.length;ii++){
					if(document.theForm.LAB_CODE.options[i].value==s1[ii]){
						document.theForm.LAB_CODE.options[i].selected=true;
					}
				}
		}
}

function calendarSelected(dd){
docChanged();
}

function flipFlopRows(ta,min_,max_){
	 flipRows=min_;
	 flopRows=max_;
	
	if(typeof(min_)=='undefined'){
	 flipRows=2;
	}
	if(typeof(max_)=='undefined'){
	 flopRows=5;
	}
	tai=ta+'_img';
	
	i=document.getElementById(tai);
	
	c=document.getElementById(ta);
	if (c.rows==flipRows){
		c.rows=flopRows;
		try{
		i.src= imgPath + 'hide.gif';}
		catch(e){}
		
	}else{
		c.rows=flipRows;
		try{
		i.src=imgPath + 'show.gif';}
		catch(e){}
	}
}

function TextRangeSelect(txt2sel,where) 
{	var r=document.all.NEWDOCTYPE.createTextRange();
	r.findText(txt2sel);
	r.select();
}

function show_named_panel(nm,a_nm,open_text,close_text)
{	z=document.getElementById(nm);
	y=document.getElementById(a_nm);
	if (z.style.display=='block')
	{
		z.style.display='none';
		y.innerText=open_text;
	}else
	{
		z.style.display='block';
		y.innerText=close_text;	
	}
}

function show_panel()
{
	z=document.getElementById('addtyp');
	y=document.getElementById('op');
	if (z.style.display=='inline'){
	z.style.display='none';
	y.innerText='р';
	
	}else{
	z.style.display='inline';
	y.innerText='п';
	}
}

function newdoctypeCheck()
{
	n=document.theForm.NEWDOCTYPE;
	d=document.theForm.DOCTYPE;
	if (n.value.length>0)
	{	frm.DOCTYPE.selectedIndex=-1;
		selectOptionByStrPart(frm.DOCTYPE,n.value);
		docChanged();
			//			remain=''+d.options[d.selectedIndex].text.substr(n.value.length);
	//			n.value=n.value+remain;
	//			TextRangeSelect(remain);
	}
}

function add_new_type()
{
	var n=document.theForm.NEWDOCTYPE;
	var d=document.theForm.DOCTYPE;
	//alert('n.value.toUpper::'+n.value.toUpperCase());
	//alert('ss::'+d.selectedIndex);
	if((n.value.length>0)&&(d.selectedIndex!=-1)&&(n.value.toUpperCase()!=d.options[d.selectedIndex].text.toUpperCase())){
	//	alert ('l::'+d.options.length);
		for(i=0;i<d.options.length;i++){
	//		alert('i::'+i);
			if (d.options[i].value=='-1'){
	//			alert('del::'+i);
				d.remove(i);
				break;
			}
		}
			var oOption = document.createElement("OPTION");
			d.options.add(oOption);
			
			oOption.innerText = n.value;
			oOption.value = "-1";
			selectOptionByVal (d,"-1");
					docChanged();
	}
	if((n.value.length>0)&&(d.selectedIndex==-1)){
	///	alert ('l::'+d.options.length);
		for(i=0;i<d.options.length;i++){
	//		alert('i::'+i);
			if (d.options[i].value=='-1'){
	//			alert('del::'+i);
				d.remove(i);
				break;
			}
	}
			var oOption = document.createElement("OPTION");
			d.options.add(oOption);
			oOption.innerText = n.value;
			oOption.value = "-1";
			selectOptionByVal (d,"-1");
		docChanged();
	}	
}

function selectOptionByStrPart(dd,val)
{  
	try 
	{	var opt=dd.options;
		if (val != "None")
		{ var valU=val;
			if (typeof valU == "string") valU=valU.toUpperCase();
			for (i=0; i < opt.length; i++)
			{	var t=opt[i].text.toUpperCase();
				if (t.substring(0,valU.length) == valU)
				{	opt[i].selected=true;
					return;
				}
			}
		}
	}
	catch(e) { 
	}
}


function changeDocState(state){
	s=document.getElementById('docState');
	s.innerHTML=state;
}

function getDocState(){
	s=document.getElementById('docState');
	return s.innerHTML;
}

function docChanged(){
	changeDocState("<font color=red>*изменен*</font>");
//	frm.b_reset.disabled=false;
//	frm.b_delete.disabled=false; 
}

