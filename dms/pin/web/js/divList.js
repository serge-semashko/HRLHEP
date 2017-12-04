var frm=null;
var f_menu = null; 
var f_popup = null; 
var searchDiv=null; 

function hideOtdelHeader()
{ try
	{ document.getElementById("otdelHeader").style.display="none";
	} catch (e) {;}
}

function getResults(srcDiv, targetDivId)
{ document.getElementById(targetDivId).innerHTML = srcDiv.innerHTML;
  if (frm.cop.value == "del") frm.cop.value = "";
//  frm.getPath.value="";
}


function showSearch()
{
	var searchDiv=document.getElementById("searchBlock"); 
	var searchAnc=document.getElementById("aSearch");
	if (searchDiv.style.display=="block")
	{	searchDiv.style.display="none";
    return false;
  }
	else
	{
		searchDiv.style.display="block";
	}
  return true;
}

function closeSearch()
{  searchDiv.style.display="none";
}


function doIt()
{ return doSearch();
}

function openFolder(id)
{ f_menu.reloadMenu(id, true);
}

function goToFolder(id)
{  openFolder(id);
}


function gotoObject(id)
{ // alert (id);
	try
	{ frm.f_searchArea[0].checked=true;
	}
  catch (e) {}
	try
	{ resetForm();
	}
  catch (e) {}
	openFolder(id);
}


function refreshCont()
{ doSearch();
}


function setObjects()
{ frm=document.theForm; 
	try{	f_menu = top.frames["TREE"].window; } catch (e) {}
	try{	f_popup = top.frames["popup"].window;} catch (e) {}
	try{	searchDiv=document.getElementById("searchBlock"); } catch (e) {}
}

