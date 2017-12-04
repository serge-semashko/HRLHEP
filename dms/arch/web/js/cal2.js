var calWin = null;

var cNow = new Date();

var vNowDay;
var vNowMonth;
var vNowYear;

var cYear;
var cMonth;
var cDat;

var minYear;
var maxYear;
var calForm=null;
var p_item = null;

var datError="";

var months = ["Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"];
dMonth = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
dMonthV = [31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];


function show_calendar()
{
	p_item = arguments[0];
//alert (typeof p_item + ":" + p_item.value);
	
  cDat=cNow.getDate();
	cMonth = cNow.getMonth();
	cYear = cNow.getFullYear();
  
	if (typeof arguments[1] != "number" || arguments[1] == null)
		minYear = cYear-10;
	else
		minYear = cYear+arguments[1];
	if (typeof arguments[2] != "number" || arguments[2] == null)
		maxYear = cYear+1;
	else
		maxYear = cYear+arguments[2];

  var s = p_item.value;
  if (typeof s == "string")
  {  s = parseDate(s);
//     alert (s.length + ":" + s);
		  if (s.length == 10 )
  		{	p_item.value = s;
    		cDat = parseInt(getDate(s), 10);
	  		cMonth = parseInt(getMonth(s), 10) - 1;
	  		cYear = parseInt(getYear(s), 10);
	  	}
  }
	
  vNowDay = cDat;
  vNowMonth = cMonth;
  vNowYear = cYear;
	
  calWin= window.open( "",  "Calendar","location=no,directories=no," +
//  "toolbar=yes,status=yes,menubar=yes,scrollbars=no,resizable=yes,copyhistory=no,width=250,height=180");
    "toolbar=no,status=no,menubar=no,scrollbars=no,resizable=no,copyhistory=no,width=350,height=250");
  try { calWin.moveTo(mX+30,mY+50); } catch (e) {}
// alert (window.screenX);
  calWin.focus();

  writeCalWindow();
}

function writeCalWindow()
{
calWin.document.open();
wr("<html><head><META http-equiv=Content-Type content='text/html; charset=windows-1251'>");
wr("<title>Календарь</title>");
wr('<link rel="stylesheet" href="/adb/js/cal.css" type="text/css">');
wr('</head><body><form name="calForm">');
wr('<table width=100% height=100% border=0 cellspacing=1 ><tr><td nowrap=true colspan=7 class=cal_t ALIGN=center>');

wr('<A HREF="javascript:pw.pMonth(-1);"><</A>');
wr('<select name=fcal_mon onChange="pw.changeMonth();">' + setMonths()	+ '<\/select>');
wr('<A HREF="javascript:pw.pMonth(+1);">></A>&nbsp;&nbsp;');

wr('<A HREF="javascript:pw.pYr(-1);"><<</A>');
wr('<select name=fcal_year onChange="pw.changeYear();">' + setYears()	 + '<\/select>');
wr('<A HREF="javascript:pw.pYr(+1);">>> </A></TD></TR>');

wr("<TR><TD class=cal_h WIDTH='14%'>Пн</TD><TD class=cal_h WIDTH='14%'>Вт</TD><TD class=cal_h WIDTH='14%'>Ср</TD><TD class=cal_h WIDTH='14%'>Чт</TD><TD class=cal_h WIDTH='14%'>Пт</TD><TD class=cal_h WIDTH='15%'>Сб</TD><TD class=cal_h WIDTH='15%'>Вс</TD></TR>");

wr (cal_data() );

wr('</td></tr></table></form>');
wr("<script>var pw = window.opener; function m() {alert(\"here\");} window.onblur='m();'; <\/script>");
wr('</body></html>');

calWin.document.close();
//alert ( cNow + "\n" + cDat + ":" + cMonth + ":" + cYear );
calForm=calWin.document.calForm;
selectOptionByVal(calForm.fcal_year, cYear);
selectOptionByVal(calForm.fcal_mon, cMonth);
}

function calSelect(dd)
{	var vData;
	var vMonth = 1 + cMonth;
	vMonth = (vMonth.toString().length < 2) ? "0" + vMonth : vMonth;
	var vY4 = new String(cYear);
	var vDD = (dd.toString().length < 2) ? "0" + dd : dd;
//alert (vDD + ":" + p_item.value);
  p_item.value = vDD + "." + vMonth + "." + vY4;
  calWin.close();
  calendarSelected(dd,p_item);
}

function calendarSelected(dd,p_item)
{
}


function pMonth(n)
{ cMonth += n;
  if (cMonth > 11) {cMonth = 0; cYear +=1;}
  if (cMonth < 0) {cMonth = 11; cYear -=1;}
  writeCalWindow();
}

function pYr(n)
{ if (cYear+n > maxYear || cYear+n < minYear) return;
  cYear += n;
  writeCalWindow();
}

function changeYear()
{ cYear = parseInt(getSelectedText(calForm.fcal_year), 10);
  writeCalWindow();
}

function changeMonth()
{ cMonth = parseInt(getSelectedVal(calForm.fcal_mon), 10);
  writeCalWindow();
}

function setYears()	
{ var s = "";	var i;
  for (i=minYear; i <= maxYear; i++)
		s += "<option>" +i ;
	return s;
}

function setMonths()	
{ var s = "";	var i;
  for (i=0; i <= 11; i++)
    s += "<option value='" + i + "'>" + months[i];
  return s;
}

function get_daysofmonth (monthNo, p_year)
{	/* 	Check for leap year ..
	1.Years evenly divisible by four are normally leap years, except for...
	2.Years also evenly divisible by 100 are not leap years, except for...
	3.Years also evenly divisible by 400 are leap years.
	*/
	if ((p_year % 4) == 0)
	{if ((p_year % 100) == 0 && (p_year % 400) != 0)
			return Calendar.dMonth[monthNo];
		return dMonthV[monthNo];
	}
	else
		return dMonth[monthNo];
}

function toDate(sDate)
{ var a = sDate.split(".");
  if (typeof a != "object" || a.length != 3)
    a = s.split("\/");
  if (typeof a != "object" || a.length != 3)
    a = s.split("-");
  if (typeof a != "object" || a.length != 3)
    return null;

  var vDate = new Date();
  vDate.setDate(parseInt(a[0], 10));  
  vDate.setMonth(parseInt(a[1], 10)-1);
  vDate.setFullYear(parseInt(a[2], 10));
//  alert (a[0] + ":" + parseInt(a[0], 10));
  return vDate;
}

function cal_data()
{
	var vDate = new Date();
	vDate.setDate(1);
	vDate.setMonth(cMonth);
	vDate.setFullYear(cYear);
//alert (vDate);
	var vFirstDay=vDate.getDay()-1;
	if (vFirstDay < 0) vFirstDay= vFirstDay + 7;
	var vDay=1;
	var vLastDay=get_daysofmonth(cMonth, cYear);
	var vOnLastDay=0;

//alert (vFirstDay);
	/*
	Get day for the 1st of the requested month/year..
	Place as many blank cells before the 1st day of the month as necessary.
	*/
	var vCode =  "<TR>";
	var i;
	for (i=0; i<vFirstDay; i++) {
		vCode = vCode + "<TD " + calCellStyle(i) + ">&nbsp;</TD>";
	}

	// Write rest of the 1st week
	for (i=vFirstDay; i<7; i++) {
		vCode = vCode + "<TD " + calCellStyle(i) + ">"
					+ "<A HREF='javascript:pw.calSelect(\"" + vDay + "\");' >"
				 + this.format_day(vDay)
			"</A></TD>";
		vDay=vDay + 1;
	}
	vCode = vCode + "</TR>";

	// Write the rest of the weeks
	for (i=2; i<7; i++) {
		vCode = vCode + "<TR>";
		for (j=0; j<7; j++)
		{	var f_click = "pw.calSelect(\"" + vDay + "\");";
      vCode = vCode + "<TD " + calCellStyle(j) + " onClick='" + f_click + "'>"
					+ "<A HREF='javascript:"  + f_click +  "' >"
					+ format_day(vDay) + "</A></TD>";
			vDay +=1;
			if (vDay > vLastDay) {
				vOnLastDay = 1;
				break;
			}
		}

		if (j == 6)
			vCode = vCode + "</TR>";
		if (vOnLastDay == 1)
			break;
	}
	
	// Fill up the rest of last week with proper blanks, so that we get proper square blocks
	for (m=1; m<(7-j); m++)
  { vCode = vCode + "<TD " + calCellStyle(m+j) +	">&nbsp;"
//    +" <FONT COLOR='gray'>" + m + "</FONT>" 
    +"</TD>";
	}
	return vCode;
}

function calCellStyle(n)
{ if (n >=5) return (" class=cal_we");
	return " class=cal_wd";
}

function format_day(vday)
{ if (vday == vNowDay && cMonth == vNowMonth && cYear == vNowYear)
		return ("<FONT COLOR=\"RED\"><B>" + vday + "</B></FONT>");
	else
		return (vday);
}

function parseDate(s)
{ if (s.length < 5) return "";
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

  var re = new RegExp("^\\d{2}\\.\\d{2}\\.\\d{4}$");
//  var re = new RegExp("(?:\\d{2})(?:\\.)(?:\\d{2})(?:\\.)(?:\\d{4})");
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


function getDate(s)
{ var a = s.split("."); return a[0];
}
function getMonth(s)
{ var a = s.split("."); return a[1];
}
function getYear(s)
{ var a = s.split("."); return a[2];
}

function XXXtoDate_(src)
{ var d;
  try
  { var dd = src.substring(0,2);
    var mm = src.substring(3,5) - 1;
    if (dd > 31 || dd < 1
      || mm < 0 || mm > 11
      || dd > daysInMonth(mm))
      return null;
    var yyyy = src.substring(6,8);
    if (yyyy > ((new Date()).getYear() - 1998) ) yy = "19" + yy;
    else  yyyy = "20" + yyyy;
    d = new Date(yyyy,mm,dd);
  }
  catch (e) {
    return null;}

//  alert ("'" + dd + ":" + mm + ":" + yyyy + "'\n\r" + d + "\n\r" + isNaN(d) );
  if (isNaN(d)) return null;
  return d;

}

function toString_(dat)
{
  try
  { var dd = dat.getDate();
    var mm = dat.getMonth() + 1;
    var yyyy = dat.getFullYear();
    var yy =  String(yyyy).substring(2,4);

    var s = "_";
//    if (yy.length < 1) s = ".0" + yy;
//    else s = "." + yy;

    if (mm <= 9) s = ".0" + mm + "." + yyyy;
    else s = "." + mm + "." + yyyy;

    if (dd <= 9) return "0" + dd + s;
    return dd + s;
  }
  catch (e) {return "";}
}




function wr(t)
{calWin.document.write(t + "\n\r");
}