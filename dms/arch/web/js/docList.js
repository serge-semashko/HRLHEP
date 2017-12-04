function refresh()
{   document.theForm.submit();
}

function countSelected()
{   v=0;
    for (i=0;i<document.theForm.CHECKED_LIST.length;i++)
    { if (document.theForm.CHECKED_LIST[i].checked==true)
            v++;
    }
	return v;
}

function showSearchAllPanel()
{   var panel = document.getElementById('search_panel_all');
    var a = document.getElementById('sh_search_all');
    if (panel.style.display=='block')
    { panel.style.display='none';
        a.innerHTML="<b><i>Поиск >></i></b>";
    }
    else
    {   panel.style.display='block';
        a.innerHTML="<b><i>Поиск &lt;&lt;&nbsp;</i></b>";
    }
}

function showSearchPanel()
{
    var f = document.theForm.showSearch;
    var panel = document.getElementById('search_panel');
    var a = document.getElementById('sh_search');
    if (f.value == "Y")
    { f.value="N";
        panel.style.display='none';
        a.innerHTML="<b><i>Расширенный поиск >></i></b>";
    }
    else
    { f.value="Y";
        panel.style.display='block';
        a.innerHTML="<b><i>&nbsp;&nbsp;&nbsp;Простой поиск &lt;&lt;&nbsp;</i></b>";
    }
    setParam();
}
    
function setParam()
{	try{
    document.theForm.target="svsFrame";
    document.theForm.c.value="doc/setParam";
    document.theForm.submit();
    document.theForm.target="";
    document.theForm.c.value="main";
  } catch(e) {}
}



function checkCriteriaExists()
{ var s = gf("f_regnum") + gf("f_DAT_1") + gf("f_DAT_2")
     + gf("f_num") + gf("f_tom") + gf("f_comments")
      + gf("f_resolution") + gf("f_ispolnitel") + gf("f_result")
        + gf("f_link")+ gf("f_content") + gf("f_correspondent");
    s = s.replace(/\s/g, "");
    if (s.length < 1)
    { alert ("Введите критерий поиска"); return false;
    }
    return true;
}

function gf(field)
{ try {return eval("document.theForm." + field + ".value"); }
    catch (e) { return ""; }
}

function parentSet()
{ if (document.theForm.PID.value!=0)
    { if(confirm('Подтвердите перемещение\nвыбраных документов ('+countSelected()+') в раздел\n'+document.theForm.PARENT_NAME.value+'?'))
        {   document.theForm.cop.value="move";
            document.theForm.submit();
        }
    }
    else
    {   alert('Нельзя переносить документы в корневой раздел.');
    }
}

//tooltip
//
 
function bodyClickHandler()  
{ hideTooltip();
}
 
function hideTooltip()
{ showPopUp("d_tooltip", false);
}
 
function showTooltip(baseId, text)
{ var xOffset = -50;
 var yOffset = 3;
 if (text.length > 70) 
 { xOffset = -70;
  yOffset = 3;
 }

 var d = document.getElementById("d_tooltip");
 setDivPosition("d_tooltip", baseId, xOffset, yOffset);

 showPopUp("d_tooltip", true, "<table class=t_tooltip cellpadding=0; cellspacing=0><tr><td class=td_tooltip >" + text 
 + "</td></tr></table>");
}

 
function showPopUp(divId, show, cont)
{ var d = document.getElementById(divId);
  d.style.display= (show)? "block" : "none";
 if (cont) d.innerHTML = cont;
}
 
function getDivPosition(divID, baseID)
{ var baseDiv = null;
 if (baseID) baseDiv = document.getElementById(baseID);
 var basePos = fGetXY();
 var pos = fGetXY(document.getElementById(divID));
 pos[0] = eval(pos[0] + "-" + basePos[0]);
 pos[1] = eval(pos[1] + "-" + basePos[1]);
 return pos;
}
 
function setDivPosition(divID, baseID, offX, offY)
{ var baseDiv = null;
 if (baseID) baseDiv = document.getElementById(baseID);
 var pos = fGetXY(document.getElementById(baseID));
  var f = document.getElementById(divID);
  f.style.left = pos[0]+offX;
  f.style.top = pos[1]+offY;
}
 
function fGetXY(a) 
{ var p = new Array (0,0);
 if (a != null && typeof a == "object")
 { while(a) 
  { tn=a.tagName.toUpperCase();
 //  alert (tn);
   p[0]+=a.offsetLeft-(tn=="DIV"&&a.scrollLeft?a.scrollLeft:0);
   p[1]+=a.offsetTop-(tn=="DIV"&&a.scrollTop?a.scrollTop:0);
   if (tn=="BODY") break;
   a=a.offsetParent;
  }
 }
 return p;
}

//end of tooltip functions

function doDelete()
{	if (countSelected()>0)
		deleteSelected();
	else 
		alert("Нет отмеченных документов!");
}

function checkall(t){
    for (i=1;i<document.theForm.CHECKED_LIST.length;i++){
        document.theForm.CHECKED_LIST[i].checked=t.checked;
    } 
    for(i=0;i<=document.theForm.CHECKALL.length;i++){
        document.theForm.CHECKALL[i].checked=t.checked;
    }
}

function doEdit(id)
{ 
try{
	document.theForm.c.value="doc/docDet";
    if (id){
        document.theForm.ID.value=id;
       }
    else{
        document.theForm.cop.value="add";
}
    document.theForm.submit();
 }
 	catch (e){
 	}
}


function show_named_panel(nm,a_nm,open_text,close_text){

    z=document.getElementById(nm);
    y=document.getElementById(a_nm);
    if (z.style.display=='inline')
    {
        z.style.display='none';
        y.innerText=open_text;
    }else
    {
        z.style.display='inline';
        y.innerText=close_text; 
    }

}

function deleteSelected(){
if(confirm('Вы настаиваете на УДАЛЕНИИ\nвыбраных документов ('+countSelected()+')?')){

    document.theForm.cop.value="delete";
    document.theForm.submit();
    }
}
