var defFrameName = "TREE";
var frameName = "TREE";
var treeFrame = null;

var activeNode = 0;
var excludeNode = new Array();
excludeNode["TREE"] = -1000;
excludeNode["TREE_PARENT"] = -1000;

var indentPixels = 8;

// pre-load all images into cache
var imgPath="/adb/images/";
var fillerImg = new Image(1,1);
fillerImg.src = imgPath+"filler.gif";

var imInactSrc = imgPath+"blue1.gif";
var imInact = new Image(12,12);
imInact.src = imInactSrc;

var imActSrc = imgPath+"blue2.gif";
var imAct = new Image(12,12);
imAct.src = imActSrc;

var imOpnSrc = imgPath+"yellow2.gif";
var imOpn = new Image(12,12);
imOpn.src = imOpnSrc;

var beforeTree = new Array();
//var beforeTree = "<HTML><HEAD><META http-equiv=Content-Type content='text/html; charset=windows-1251'></HEAD><BODY class=tree>" ??
var afterTree = new Array();
//var afterTree = "<hr></html>";

var tr = new Array();
var tr1 = new Array();
var tr2 = new Array();
var opn=[0];

function getFrameName(fName)
{ frameName = fName;
  if (typeof frameName == "undefined" || frameName.length == 0)	
		frameName = defFrameName;
  if (typeof frames[frameName] == "object")
    treeFrame = frames[frameName];
  else
    treeFrame = document.frames["CONT"].frames[frameName];
  if (frameName == defFrameName)
  	tr = tr1;
  else
    tr = tr2; 
//  alert (frameName + ":" + typeof treeFrame);
}

function img_act(imgName, fName)
{ try
	{ getFrameName(fName);
    s = treeFrame.document.images[imgName].src;
  	if (s.indexOf( imOpnSrc ) < 0)
			treeFrame.document.images[imgName].src = imActSrc;
	 } catch (e) {}
}

function img_inact(imgName, fName)
{ try
	{ getFrameName(fName);
//    window.status= "...";
		s = treeFrame.document.images[imgName].src;
		if (s.indexOf( imOpnSrc ) < 0)
			treeFrame.document.images[imgName].src = imInactSrc;
	 } catch (e) {}
}

function getNode(id)
{ for (n=0; n<tr.length; n++)
	{ if (tr[n][0] == id)
		return tr[n];
	}
	return null;
}

function getNodeName(id)
{ var nod = getNode(id);
	return nod[2];
}

function openNode(id, fName)
{	var i = 0;
  opn = new Array(); 
  getFrameName(fName);
/*
  for(j = tr.length-1; j >= 0; j--)
  { opn[i++] = tr[j][0];
	}
/**/  
	var nod = getNode(id)
	while (nod != null)
	{	opn[i++] = nod[0];
			nod = getNode(nod[1])
	}
/**/  
//  alert (opn);
  activeNode = id;
  outTree(fName);
  openCont(id);
}

function openCont(id)
{ /* should be overwritten in "TOP" */
}

function indent_ (ind)
{	return "<IMG SRC='"+imgPath+"filler.gif' class=fi WIDTH=" + (indentPixels*(ind-1)) + ">";
}

function wid1(id, type, lev)
{ if (type == "BC") return "* "; 
  else if (lev <= 3) return "<IMG class=toc" + lev +" SRC='" + imgPath+ "blue1.gif' NAME='im" + id + "'> "; 
  else return "- "; 
}

function wid2(id, type, lev)
{ if (type == "BC") return "<font color=FFFF00><b>* </b></font>"; 
  else if (lev <= 3) return "<IMG class=toc" + lev +" SRC='"+imgPath+"yellow2.gif' NAME='im" + id + "'> "; 
  else return "- "; 
}

function anc(id, name, stat)
{ s = "<A class=toc HREF=\"javascript:top.openNode('" + id
		+ "', '" + frameName +"');\" onMouseOver=\"top.img_act('im" + id
		+ "', '" + frameName +"');\" "
//		+ "'); window.status='Открыть раздел `"+ stat + "`'; return true;\""
		+ " onMouseout=\"top.img_inact('im" + id + "', '" + frameName +"');\">"
		+ name + "</a>";
		return s;
}

var act1 = "<font color=FFFF00><i>";
var act2 ="</i></font>";

function outNode(n)
{ var id = tr[n][0];
  if (id == excludeNode[frameName])  return;

	var lev = nodeLevel(id);
	var pid = tr[n][1];
	var plev = nodeLevel(pid);	
  if (lev < 0 && plev < 0) return; //non-visible node
  
  var s = "";
	var nam = tr[n][2];

	if ( lev == 0)			// root of the tree
	{ /**/ if (activeNode == 0)
		s = anc(id, wid2(id, tr[n][3], 1) + act1 + nam + act2, nam, frameName) + "<br>";
	  else
		s = anc(id, wid1(id,'***',1) +nam , nam, frameName) + "<br>";
		/**/
	}
	else if ( lev > 0)	// opened node
		s = indent_(lev) + anc(id, wid2(id, tr[n][3],lev) + act1 + nam + act2, nam, frameName);
	else 	              // closed visible node
		s = indent_(plev+1)  +  anc(id, wid1(id,tr[n][3], plev+1) + nam, nam, frameName);

  if (n > 0) s = "<br>" + s;
	treeFrame.document.writeln(s);
}

function nodeLevel(id)
{ for (k=0; k<opn.length; k++)
		if (opn[k] == id) return (opn.length-k-1);
	return -1;
}

function outTree(fName)
{ getFrameName(fName);
  treeFrame.document.write(beforeTree[frameName]);

  var n0=0;
//  if (frameName == "TREE_PARENT") n0=1;
  for (n=n0; n<tr.length; n++)
		outNode(n);
		
  treeFrame.document.write(afterTree[frameName]);
	treeFrame.document.close();
}
