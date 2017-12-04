function showPopUp(divId, show)
{ var d = document.getElementById(divId);
  d.style.display= (show)? "block" : "none";
}

function setDivPosition(fID, baseID, offX, offY)
{ var pos = fGetXY(document.getElementById(baseID));
  var f = document.getElementById(fID);
  f.style.left = pos[0]+offX;
  f.style.top = pos[1]+offY;
}

function fGetXY(a) 
{	var p = new Array (0,0);
	while(a) {
		tn=a.tagName.toUpperCase();
//		alert (tn);
		p[0]+=a.offsetLeft-(tn=="DIV"&&a.scrollLeft?a.scrollLeft:0);
		p[1]+=a.offsetTop-(tn=="DIV"&&a.scrollTop?a.scrollTop:0);
		if (tn=="BODY") break;
		a=a.offsetParent;
	}
	return p;
}


function formatNumber (num, decplaces) 
{ num = parseFloat(num);
  if (!isNaN(num)) 
  { var str = "" + Math.round (eval(num) * Math.pow(10,decplaces));
    if (str.indexOf("e") != -1) 
    { return "Out of Range";
    }
    while (str.length <= decplaces) 
    { str = "0" + str;
    }
    var decpoint = str.length - decplaces;
    return str.substring(0,decpoint) + "." + str.substring(decpoint,str.length);
  } 
  else 
  { return "-";
  }
}

function setLongCookie(name,value,hours)
{ var exp=new Date();
  exp.setHours(exp.getHours()+hours);
  document.cookie=name+"="+escape(value)+"; path=/; expires="+exp.toGMTString()+";"
}

function setCookie(name, value)
{if (getCookie(name) != value)
	 document.cookie = name + "=" + escape(value) + "; path=/;"
}

function getCookie(name)
{ var search = name+"="
  if (document.cookie.length > 0)
  { offset = document.cookie.indexOf( search)
    if (offset != -1)
    { offset += search.length
      end = document.cookie.indexOf(";", offset)
      if (end == -1) end = document.cookie.length
      return unescape( document.cookie.substring( offset, end))
    }
  }
  return null;
}

function selectOptionByVal(dd,val)
{ // alert (val);
	try {var opt=dd.options;
	if (val != "None")
	{ var valU=val;
    if (typeof valU == "string") valU=valU.toUpperCase();
		for (i=0; i < opt.length; i++)
		if ((opt[i].value.toUpperCase() == valU) ||
					(opt[i].text.toUpperCase() == valU))
			{ opt[i].selected=true;
				return;
			}
	}} catch(e) { }
}

function getSelectedVal(dd)
{ return dd.options[dd.selectedIndex].value;
}

function getSelectedText(dd)
{ return dd.options[dd.selectedIndex].text;
}

function openWindow(param,name,w,h)
{ var tm = (new Date()).getTime();
  var s=window.location.href;

  if (param.indexOf("http") != 0 && param.indexOf("/") != 0)
  { var i = s.indexOf("?");
    if (i > 0) s = s.substring(0,s.indexOf("?") + 1);
    else s = s + "?";
  }
  else
    s = "";
//  alert (s);
  s = s + param + "&tm=" + tm;
  var win= window.open( s, name,
  "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes," +
  "resizable=yes,copyhistory=yes,width="+w+",height="+h);
}

function refrSelf()
{ window.focus();
	document.theForm.submit();
}

function textCounter(f,c,m)
{ var a = 0;
  var s = f.value;
	for (var i = 0; i < s.length; i++)
	{ if (s.substring(i, i + 1) == '\n')
			a += 2;
	}
  if (m - s.length - a < 0) 
    f.value = s.substring(0, s.length-1);
	c.value = m - s.length - a;
}

function setWindow(mt, maxWidth)
{ try
{ var h = mt.offsetHeight+80;
if (h > maxWidth) h = maxWidth;
var w = mt.offsetWidth+50;
window.resizeTo(w,h);
window.moveTo((1024-w)/3,(780-h)/3);
// mt.width="100%";
} catch (e) {}
}

//========================== DATE PROCESSING ===============================
var datError="";
var months = ["Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"];
dMonth = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
var maxYear;

function checkDate(fieldName, fieldLabel, mandatory, formName)
{
//======= Get the source value =======
//		alert (fieldName);
  try 
  { var f = null;
		if (formName)
			f = eval("document." + formName + "." + fieldName);
		else
			f = eval("document.theForm." + fieldName);
    var s = f.value;
//		alert (s);
  }
  catch (e) // {alert(e); return true; }
	{return true;}
//======= Set the MIN-MAX values and the error messages =======
  var errMsg = "'" + fieldLabel + "'";   
  var dMin;
  var y0 = (new Date()).getYear();  // the current year (yyyy)
  if (y0 < 2000) y0 += 1900;
  maxYear = y0 + 1;

  dMin = new Date(y0-1,0,1);
  dMax = new Date(y0+1,12,31);
  
  if (fieldName == "data_rozhd_new")
  { errMsg = "Дата рождения";
		dMin = new Date(y0-99,0,1);
    dMax = new Date(y0-14,12,31);
  }
  else
  { dMin = new Date(y0-99,0,1);
    dMax = new Date(y0+1,12,31);
  }
  if (typeof fieldLabel == "string")
    errMsg = fieldLabel;

  if (typeof mandatory == "undefined")
  	mandatory = false;
  if (s.length == 0)
  {	if (mandatory) 
  	{ alert (errMsg + " - необходимо заполнить ");
  	  return false;
  	}
  	else
      return true;
  }	

//======= Parse the date value =======
//  alert ("checkDate:" + s);
  s = parseDate(s);
  
//  alert (s + ":datError=" + datError);
  if (s.length != 10 || datError.length > 0)
  { alert (errMsg + " - ошибка в формате: " + datError + ": " + s);
    return false;
  }
  
  var d = null;
  try
  { var a = s.split(".");
    d = new Date(parseInt(a[2], 10),parseInt(a[1], 10),parseInt(a[0], 10));
  }
  catch (e)
  { alert (errMsg + " - ошибка в формате. " + s);
    return false;
  }
  if (isNaN(d))
  { alert (errMsg + " - ошибка в формате. " + s);
    return false;
  }

//======= Check MIN-MAX value =======
  if ( d < dMin || d > dMax)
  { alert (errMsg + " - ошибка в значении. " + s);
    return false;
  }
//===============

//  f.value = toString_(d);
  f.value = s;
  return true;
}


function parseDate(s)
{ 
  if (s.length < 5) return "";
  var vDD = "";
  var vMM = "";
  var vY4 = "";	

  var a = s.split(".");
  if (typeof a != "object" || a.length != 3)
    a = s.split("\/");
  if (typeof a != "object" || a.length != 3)
    a = s.split("-");
  if (typeof a != "object" || a.length != 3)
    return "";

  vDD = (a[0].length < 2) ? "0" + a[0] : a[0];
	vMM = (a[1].length < 2) ? "0" + a[1] : a[1];
	vY4 = a[2];
  if (vY4.length < 4)
  { var i = parseInt(vY4, 10);
    if (i> maxYear - 2000) i = 1900 + i
    else i = 2000 + i;
    vY4 = i.toString();
  }
  if (isValidDate(vDD + "." + vMM + "." + vY4))
    return vDD + "." + vMM + "." + vY4;
    
  return "";
}

function isValidDate(s)
{ datError="";

//  alert ("'"+s + "':");
//  var re = new RegExp("(?:\\d{2})(?:\\.)(?:\\d{2})(?:\\.)(?:\\d{4})");
//  alert ("'"+s + "':" +re);
  var re = new RegExp("^\\d{2}\\.\\d{2}\\.\\d{4}$");
//  alert ("'"+s + "':" +re+":"+ re.test (s));
  if(!re.test (s)) { return false; }

  try
  { var a = s.split(".");
    var d = parseInt(a[0], 10);
    var m = parseInt(a[1], 10);
    var y = parseInt(a[2], 10);
    
//    alert (s + " : " + a[0]  + ";" + a[1] + ";" + a[2] + " : " + d + ";" + m + ";" + y);
    if (isNaN(d)||isNaN(m)||isNaN(y)) {datError="ошибка в формате"; return false; }
    if (y < 1900 || y > 2020) {datError="неверный год"; return false;}
    if (m < 1 || m > 12) {datError="неверный месяц";  return false;}
    if (y % 4 == 0) dMonth[1] = 29;
    else dMonth[1] = 28;
    if (d < 1 || d > dMonth[m-1])  {datError="неверный день"; return false; }
  }
  catch (e) { alert (e); datError="ошибка в формате"; return false; }
//  alert ("isValidDate: " + s + "; datError=" + datError);
  return true;
}

function checkFloat(fieldName, min, max, errMsg)
{
  //======= Get the source value =======
  var f = eval("document.theForm." + fieldName);
  var s = trim(f.value);
  f.value = s;
  if (s.length == 0) return true;
 //======= Check the value =======
  s=s.replace(',','.');
//  alert (s);
  var v = parseFloat(s);
//  alert (fieldName+":" + s + ":" + v + " : " + (/[^0-9\.-]/).test(s));
  if (isNaN(v)||(/[^0-9\.-]/).test(s))
  { alert ("Неверный формат поля '" + errMsg + "'");
    return false;
  }
  if (min && v < min)
  { alert ("Ошибка ввода поля '" + errMsg
    + "'\n\r (должно быть число не менее " + min + ")");
    return false;
  }
  if (max && v > max)
  { alert ("Ошибка ввода поля '" + errMsg
    + "'\n\r (должно быть число не более " + max + ")");
    return false;
  }
  f.value = v;
  return true;
}

function trim(val)
{ return val.replace(/^\s+/g, "").replace(/\s+$/g, "");
}

function checkText(val, minLength, errMsg)
{ var v = " " + val;
  if (trim(v).length < minLength)
  { if (errMsg.length > 0)  alert ("Введите " + errMsg); 
    return false;
  }
  return true;
}

function checkTextField(input, minLength, errMsg)
{ 
  var v = "" + input.value;
  v = v.replace(/'/g, "`");
  input.value = v;
  v = v.replace(/\s/g, "");
  if (trim(v).length < minLength)
  { alert ("Введите " + errMsg); return false;}
  return true;
}

function checkPathField(input, minLength, tailSlash, errMsg)
{ 
  var v = "" + trim(input.value);
  if (v.length < minLength)
  { alert ("Please fill field " + errMsg); return false;}
//  v = v.replace(/'/g, "`");
  v = v.replace(/\\/g, "/");
	if (tailSlash)
	{ if (v.substring(v.length-1,v.length) != "/")
			v = v + "/";
	}
	else
	{ if (v.substring(v.length-1,v.length) == "/")
			v = v.substring(0, v.length-1);
	}
  input.value = v;
//  v = v.replace(/\s/g, "");
  return true;
}

function handleErr(msg,url,l)
{  try
	{	txt="JavaScript ошибка на странице.\n\n"
			+"Error: " + escape(msg) + "\n"
			+"Строка: " + l + "\n\n"
//			+"URL: " + url + "\n" 
			+"Нажмите OK для продолжения.\n\n";
		alert(unescape(txt));
	}
	catch (e) { return true; }
return true;
}

