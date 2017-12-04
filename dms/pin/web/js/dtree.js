/*--------------------------------------------------|
| dTree 2.05 | www.destroydrop.com/javascript/tree/ |
|---------------------------------------------------|
| Copyright (c) 2002-2003 Geir Landrц               |
|                                                   |
| This script can be used freely as long as all     |
| copyright messages are intact.                    |
|                                                   |
| Updated: 17.04.2003                               |
|--------------------------------------------------*/

// Node object
function Node(id, pid, name, url, title, target, icon, iconOpen, open) 
{	this.id = id;
	this.pid = pid;
	this.name = name;
	this.url = url;
	this.title = title;
	this.target = target;
	this.icon = icon;
	this.iconOpen = iconOpen;
	this._io = open || false;
	this._is = false;
	this._ls = false;
	this._hc = false;
	this._ai = 0;
	this._p;
};

// Tree object
function dTree(objName) 
{	this.config = 
	{	target				: null,
		folderLinks			: true,
		useSelection		: true,
		useCookies			: false,
		useLines			: false,
		useIcons			: false,
		useStatusText		: false,
		closeSameLevel		: false,
		inOrder				: false
	}
	this.icon = 
	{	// root				: imgRoot + 'base.gif',
		root				: imgRoot + 'empty_14.gif',
		folder				: imgRoot + 'folder.gif',
		folderOpen			: imgRoot + 'folderopen.gif',
		node				: imgRoot + 'page.gif',
		bullet				: imgRoot + 'bullet_b1.gif',
		empty				: imgRoot + 'empty_14.gif',
		line				: imgRoot + 'line.gif',
		join				: '',
		joinBottom			: imgRoot + 'joinbottom.gif',
		plus				: imgRoot + 'folder.gif',
		plusBottom			: imgRoot + 'plusbottom.gif',
		minus				: imgRoot + 'folder.gif',
		minusBottom			: imgRoot + 'folder.gif',
		nlPlus				: imgRoot + 'bullet_r2.gif',
		nlMinus				: imgRoot + 'bullet_rd.gif'
//		nlPlus				: imgRoot + 'plus_14r.gif',
//		nlMinus				: imgRoot + 'minus_14r.gif'

/*		root				: imgRoot + 'empty.gif',
		folder				: imgRoot + 'folder.gif',
		folderOpen			: imgRoot + 'folderopen.gif',
		node				: imgRoot + 'page.gif',
		empty				: imgRoot + 'empty.gif',
		line				: imgRoot + 'line.gif',
		join				: imgRoot + 'join.gif',
		joinBottom			: imgRoot + 'joinbottom.gif',
		plus				: imgRoot + 'plus.gif',
		plusBottom			: imgRoot + 'plusbottom.gif',
		minus				: imgRoot + 'minus.gif',
		minusBottom			: imgRoot + 'minusbottom.gif',
		nlPlus				: imgRoot + 'nolines_plus.gif',
		nlMinus				: imgRoot + 'nolines_minus.gif'
*/		
	};
	this.obj = objName;
	this.aNodes = [];
	this.aIndent = [];
	this.root = new Node(-1);
	this.selectedNode = null;
	this.selectedFound = false;
	this.completed = false;
	this.indl = 0;
	this.actNode = -1;
	this.frameWidth=208;
	this.rootFolderId='0';
};

// Adds a new node to the node array
dTree.prototype.add = function(id, pid, name, url, title, target, icon, iconOpen, open) 
{	this.aNodes[this.aNodes.length] = new Node(id, pid, name, url, title, target, icon, iconOpen, open);
};

// Open/close all nodes
dTree.prototype.openAll = function() 
{	this.oAll(true);
};

dTree.prototype.closeAll = function() 
{	this.oAll(false);
};

// Outputs the tree to the page
dTree.prototype.toString = function() 
{	var str = '<div class="dtree">\n';
	if (document.getElementById) 
	{	if (this.config.useCookies) 
			this.selectedNode = this.getSelected();
		str += this.addNode(this.root);
	} else str += 'Browser not supported.';
	str += '</div>';
	if (!this.selectedFound) this.selectedNode = null;
	this.completed = true;
	return str;
};

// Creates the tree structure
dTree.prototype.addNode = function(pNode) 
{
	var str = '';
	var n=0;
	if (this.config.inOrder) n = pNode._ai;
	for (n; n<this.aNodes.length; n++) 
	{	if (this.aNodes[n].pid == pNode.id) 
		{	var cn = this.aNodes[n];
			cn._p = pNode;
			cn._ai = n;
			this.setCS(cn);
			if (!cn.target && this.config.target) cn.target = this.config.target;
			if (cn._hc && !cn._io && this.config.useCookies) cn._io = this.isOpen(cn.id);
			if (!this.config.folderLinks && cn._hc) cn.url = null;
			if (this.config.useSelection && cn.id == this.selectedNode && !this.selectedFound) 
			{	cn._is = true;
				this.selectedNode = n;
				this.selectedFound = true;
			}
//			if (pNode != this.root)
				str += this.node(cn, n);
			if (cn._ls) break;
		}
	}
	return str;
};

dTree.prototype.select = function(id, lev) 
{ document.getElementById('s_' + this.obj + id).className = "item_a";
  this.selectedNode = id;
}

dTree.prototype.unselect = function(id, lev) 
{ var s = 's_' + this.obj + id;
	if (id == this.actNode)
		document.getElementById(s).className = "item_s";
	else
		document.getElementById(s).className = "item";
}

dTree.prototype.getTreeId = function(id) 
{ for (var i=0; i<this.aNodes.length; i++)
	{ if (this.aNodes[i].id == id)
	      return i;	      
  }
  return 0;
}


dTree.prototype.itemClick = function (url, target, id_)
{ 	
//  var u = unescape(url);
	var id=id_;
	var cn = this.aNodes[id_];
	if (typeof cn != "object")
	{ for (var i=0; i<this.aNodes.length; i++)
	  { if (this.aNodes[i].id == id_)
	    { cn = this.aNodes[i];
	      id = i;	      
	      break;
	    }
	  }
	  if (i >= this.aNodes.length ) return;
	}

  var u = unescape(cn.url);
//  alert (u);
	var s = 's_' + this.obj;
	if (this.actNode > 0)
		document.getElementById(s + this.actNode).className = "item";
	document.getElementById(s + id).className = "item_s";
	this.actNode = id;
	if (u.indexOf("javascript:") == 0)
	{	s = u.substr(11);
		eval(s);
	}
	else 
		try
		{ parent.frames(target).location.href=u;
		}
		catch (e)
		{ // alert (u + ":" + target);
		  var win = top.window.open( u, target,
	  		"toolbar=yes,location=yes,directories=no,status=no,menubar=no,scrollbars=yes," +
  			"resizable=yes,copyhistory=yes,width=500,height=500");
		}	
}

// Creates the node icon, url and text
dTree.prototype.node = function(node, nodeId) 
{	var str = '' 
	if (node.id == 1001)
		str = '<div class="dTreeNode0">\n';
	else if (node.pid == this.rootFolderId)
		str = '<div class="dTreeNode1">\n';
	else if (node._hc) 
		str = '<div class="dTreeNode2">\n' 
	else
		str = '<div>\n' 
	str += this.indent(node, nodeId);

	if (this.config.useIcons) 
	{	if (!node.icon) node.icon = (this.root.id == node.pid) ? this.icon.root : ((node._hc) ? this.icon.folder : this.icon.node);
		if (!node.iconOpen) node.iconOpen = (node._hc) ? this.icon.folderOpen : this.icon.node;
		if (this.root.id == node.pid) 
		{	node.icon = this.icon.root;
			node.iconOpen = this.icon.root;
		}
		str += '<img id="i' + this.obj + nodeId + '" src="' + ((node._io) ? node.iconOpen : node.icon) + '" alt="" />';
	}

	
	if (node.url) 
	{	// var l = this.indl;
	    str += '<span id="s_' + this.obj  + nodeId + '" style="width:' + this.indl + ';" class=item ' ;
		str += 'onmouseover="'+ this.obj + '.select(' + nodeId + ',1)" onmouseout="' + this.obj + '.unselect(' + nodeId + ',1)" ';
//		str += 'onclick="'+ this.obj + '.itemClick(\'' + escape(node.url) +'\',\'' + node.target + '\','+ nodeId +');" ';
		str += 'onclick="'+ this.obj + '.itemClick(\'' + nodeId +'\',\'' + node.target + '\','+ nodeId +');" ';
		
//		if (node.title) str += ' title="' + node.title + '"';
//		if (node.target) str += ' target="' + node.target + '"';
//		if (this.config.useStatusText) 
//		  str += ' onmouseover="window.status=\'' 
//		      + node.name + '\';return true;" onmouseout="window.status=\'\';return true;" ';
//		if (this.config.useSelection && ((node._hc && this.config.folderLinks) || !node._hc))
//			str += ' onclick="javascript: ' + this.obj + '.s(' + nodeId + ');"';
		str += '>';
	}
//	else if ((!this.config.folderLinks || !node.url) && node._hc && node.pid != this.root.id)
//		str += '<a href="javascript: ' + this.obj + '.o(' + nodeId + ');" class="node">';
		
	str += '&nbsp;' + node.name;
	if (node.url) 
  	str += '\n</span>\n';
//	if (node.url || ((!this.config.folderLinks || !node.url) && node._hc)) str += '</a>';

	str += '</div>\n';
	if (node._hc) 
	{	str += '<div id="d' + this.obj + nodeId 
			+ '" class="clip" style="display:' 
			+ ((this.root.id == node.pid || node._io) ? 'block' : 'none') + ';">';
		if (node.id == this.rootFolderId) str = "";
		str += this.addNode(node);
		if (node.id != this.rootFolderId) str += '</div>\n\n';
	}
	this.aIndent.pop();
	return str;
};


// Adds the empty and line icons
dTree.prototype.indent = function(node, nodeId) 
{	var str = '';
	this.indl = this.frameWidth-10;

  var plusIcon =  this.icon.nlPlus;
  var minusIcon =  this.icon.nlMinus;
  var bulletIcon = this.icon.bullet;
	if (!this.config.useIcons) 
	{	if (node.icon) { plusIcon=node.icon; bulletIcon = plusIcon;}
		if (node.iconOpen) minusIcon = node.iconOpen;
	}
    
	if (this.root.id != node.pid) 
	{	for (var n=0; n<this.aIndent.length; n++)
		{	str += '<img src="' 
				+ ( (this.aIndent[n] == 1 && this.config.useLines) ? this.icon.line : this.icon.empty ) 
				+ '" width=8 height=16>';
			this.indl -= 8;
		}
		(node._ls) ? this.aIndent.push(0) : this.aIndent.push(1);

//		if (node._hc && node.id != 1001)  - структура
		if (node._hc) 
		{	str += '<a href="javascript: ' 
				+ this.obj + '.o(' + nodeId 
				+ ');"><img id="j' 
				+ this.obj + nodeId + '" src="';
			if (this.config.useLines) 
				str += ( (node._io) ? ((node._ls && this.config.useLines) ? this.icon.minusBottom : minusIcon) : ((node._ls && this.config.useLines) ? this.icon.plusBottom : plusIcon ) );
			else 
				str += (node._io) ? minusIcon : plusIcon;
        
			str += '" width=14 height=12 ></a>'; // size of the plus/minus icons
			this.indl -= 14;  // width of the plus/minus icons
		} 
		else 
		{
			str += '<img src="' 
				+ ( (this.config.useLines) ? ((node._ls) ? this.icon.joinBottom : this.icon.join ) : bulletIcon) 
//				+ ( (this.config.useLines) ? ((node._ls) ? this.icon.joinBottom : this.icon.join ) : this.icon.empty) 
				+ '" width=8 height=12>';
			this.indl -= 8;
		}
	}
	return str;
};

// Checks if a node has any children and if it is the last sibling
dTree.prototype.setCS = function(node) 
{	var lastId;
	for (var n=0; n<this.aNodes.length; n++) 
	{	if (this.aNodes[n].pid == node.id) node._hc = true;
		if (this.aNodes[n].pid == node.pid) lastId = this.aNodes[n].id;
	}
	if (lastId==node.id) node._ls = true;
};

// Returns the selected node
dTree.prototype.getSelected = function() 
{	var sn = this.getCookie('cs' + this.obj);
	return (sn) ? sn : null;
};

// Highlights the selected node
dTree.prototype.s = function(id) 
{	if (!this.config.useSelection) return;
	var cn = this.aNodes[id];
	if (typeof cn != "object")
	{ id = this.getTreeId(id);
    cn = this.aNodes[id];
	}
	if (typeof cn != "object") return;
	if (cn._hc && !this.config.folderLinks) return;
	if (this.selectedNode != id) 
	{	eNew = document.getElementById("s_" + this.obj + id);
		eNew.className = "item_s";
		if (this.selectedNode) 
		{	eOld = document.getElementById("s_" + this.obj + this.selectedNode);
			eOld.className = "item";
		}
		if (this.actNode && this.actNode > 0) 
		{	eOld = document.getElementById("s_" + this.obj + this.actNode);
			eOld.className = "item";
		}
		this.selectedNode = id;
    this.actNode = id;
//    alert (this.selectedNode);
		if (this.config.useCookies) this.setCookie('cs' + this.obj, cn.id);
	}
};

// Toggle Open or close
dTree.prototype.o = function(id) 
{	
	var cn = this.aNodes[id];
	this.nodeStatus(!cn._io, id, cn._ls);
	cn._io = !cn._io;
	if (this.config.closeSameLevel) this.closeLevel(cn);
	if (this.config.useCookies) this.updateCookie();
};

// Open or close all nodes
dTree.prototype.oAll = function(status) 
{	for (var n=0; n<this.aNodes.length; n++) 
	{	if (this.aNodes[n]._hc && this.aNodes[n].pid != this.root.id) 
		{	this.nodeStatus(status, n, this.aNodes[n]._ls)
			this.aNodes[n]._io = status;
		}
	}
	if (this.config.useCookies) this.updateCookie();
};

dTree.prototype.getNode = function(nId) 
{	var cn=this.aNodes[nId];
	if (typeof cn != "object")
	{ id = this.getTreeId(nId);
    cn = this.aNodes[id];
	}
  return cn;
};

// Opens the tree to a specific node
dTree.prototype.openTo = function(nId, bSelect) 
{
	var cn=this.aNodes[nId];
	if (typeof cn != "object")
	{ id = this.getTreeId(nId);
  	if (id == 0) return false;
    cn = this.aNodes[id];
	}
  
	if (cn.pid==this.root.id || !cn._p) return true;
	cn._io = true;
	cn._is = bSelect;
	if (this.completed && cn._hc) this.nodeStatus(true, cn._ai, cn._ls);
	if (this.completed && bSelect) this.s(cn._ai);
	else if (bSelect) this._sn=cn._ai;
	this.openTo(cn._p._ai, false);
  return true;
};

// Closes all nodes on the same level as certain node
dTree.prototype.closeLevel = function(node) {
	for (var n=0; n<this.aNodes.length; n++) {
		if (this.aNodes[n].pid == node.pid && this.aNodes[n].id != node.id && this.aNodes[n]._hc) {
			this.nodeStatus(false, n, this.aNodes[n]._ls);
			this.aNodes[n]._io = false;
			this.closeAllChildren(this.aNodes[n]);
		}
	}
}

// Closes all children of a node
dTree.prototype.closeAllChildren = function(node) {
	for (var n=0; n<this.aNodes.length; n++) {
		if (this.aNodes[n].pid == node.id && this.aNodes[n]._hc) {
			if (this.aNodes[n]._io) this.nodeStatus(false, n, this.aNodes[n]._ls);
			this.aNodes[n]._io = false;
			this.closeAllChildren(this.aNodes[n]);		
		}
	}
}

// Change the status of a node(open or closed)
dTree.prototype.nodeStatus = function(status, id, bottom) 
{	
  var plusIcon =  this.icon.nlPlus;
  var minusIcon = this.icon.nlMinus;
	var cn = this.aNodes[id];
	if (typeof cn != "object")
	{ id = this.getTreeId(id);
    cn = this.aNodes[id];
	}
  if (typeof cn != "object") return;
  eDiv	= document.getElementById('d' + this.obj + id);
	eJoin	= document.getElementById('j' + this.obj + id);
  
	if (cn.icon) plusIcon=cn.icon;
	if (cn.iconOpen) minusIcon = cn.iconOpen;

	if (this.config.useIcons) 
	{	eIcon	= document.getElementById('i' + this.obj + id);
		eIcon.src = (status) ? this.aNodes[id].iconOpen : this.aNodes[id].icon;
	}
	try {
	eJoin.src = (this.config.useLines)?
		((status)?((bottom)?this.icon.minusBottom:minusIcon):((bottom)?this.icon.plusBottom:plusIcon)):
		((status)?minusIcon:plusIcon);
  }
  catch (e) { alert ("eJoin.src:" + e);}
	eDiv.style.display = (status) ? 'block': 'none';
};

/**************************** SERVICE Functions ********************************************/
// [Cookie] Clears a cookie
dTree.prototype.clearCookie = function() {
	var now = new Date();
	var yesterday = new Date(now.getTime() - 1000 * 60 * 60 * 24);
	this.setCookie('co'+this.obj, 'cookieValue', yesterday);
	this.setCookie('cs'+this.obj, 'cookieValue', yesterday);
};

// [Cookie] Sets value in a cookie
dTree.prototype.setCookie = function(cookieName, cookieValue, expires, path, domain, secure) {
	document.cookie =
		escape(cookieName) + '=' + escape(cookieValue)
		+ (expires ? '; expires=' + expires.toGMTString() : '')
		+ (path ? '; path=' + path : '')
		+ (domain ? '; domain=' + domain : '')
		+ (secure ? '; secure' : '');
};

// [Cookie] Gets a value from a cookie
dTree.prototype.getCookie = function(cookieName) {
	var cookieValue = '';
	var posName = document.cookie.indexOf(escape(cookieName) + '=');
	if (posName != -1) {
		var posValue = posName + (escape(cookieName) + '=').length;
		var endPos = document.cookie.indexOf(';', posValue);
		if (endPos != -1) cookieValue = unescape(document.cookie.substring(posValue, endPos));
		else cookieValue = unescape(document.cookie.substring(posValue));
	}
	return (cookieValue);
};

// [Cookie] Returns ids of open nodes as a string
dTree.prototype.updateCookie = function() {
	var str = '';
	for (var n=0; n<this.aNodes.length; n++) {
		if (this.aNodes[n]._io && this.aNodes[n].pid != this.root.id) {
			if (str) str += '.';
			str += this.aNodes[n].id;
		}
	}
	this.setCookie('co' + this.obj, str);
};

// [Cookie] Checks if a node id is in a cookie
dTree.prototype.isOpen = function(id) {
	var aOpen = this.getCookie('co' + this.obj).split('.');
	for (var n=0; n<aOpen.length; n++)
		if (aOpen[n] == id) return true;
	return false;
};

// If Push and pop is not implemented by the browser
if (!Array.prototype.push) {
	Array.prototype.push = function array_push() {
		for(var i=0;i<arguments.length;i++)
			this[this.length]=arguments[i];
		return this.length;
	}
};
if (!Array.prototype.pop) {
	Array.prototype.pop = function array_pop() {
		lastElement = this[this.length-1];
		this.length = Math.max(this.length-1,0);
		return lastElement;
	}
};