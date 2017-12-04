var x999NS4compatible = (document.layers);
var x999IE4compatible = (document.all);
var x999dayabbs = [ 'Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat' ];
var x999daynames = [ 'Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday' ];
var x999monthnames = [ 'January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December' ];
var x999monthabbs = [ 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec' ];
var x999weekdays = [ 'Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa' ];
var x999months = [ 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec' ];
var x999years  = new Array(10);
for (i = 0; i < x999years.length; i++) x999years[i] = 2000 + i;
var x999today = new Date();
var x999currentday = x999today.getDate();
var x999currentmonth = x999today.getMonth();
var x999currentyear = x999today.getFullYear();
var x999left = 100;
var x999top = 80;
var x999width = 230;
var x999height = 255;
var x999cell_width = 20;
var x999cell_height = 20;
var x999bg_color = '#cccccc';
var x999offsety = 32;
var x999fontface = "verdana";
var x999weekcolor = "black";
var x999weeksize= "2";
var x999lowyear = 2000;
var x999highyear = 2009;
var x999weekfont = '<font face="' + x999fontface + '" size="' + x999weeksize + '" color="' + x999weekcolor + '">';
var x999font = '<font face="' + x999fontface + '" size="1">';
var x999style = "style = 'font-family:" + x999fontface + ";color:#000000;font-size:7pt;font-weight:bold;'";
var x999headerstyle = "style = 'font-family:" + x999fontface + ";color:#000000;font-size:1;font-weight:bold;'";
var x999daystyle = "style = 'font-family:" + x999fontface + ";color:#000000;font-size:7pt;font-weight:normal;'";
var x999tableborder = '1';

function x999cal(is_popup, win, handler, left, top, width, height,
  cell_width, cell_height, bg_color, sel_month, sel_year, sel_hour, sel_minute, sel_ampm)
{
  this.is_popup = is_popup;
  this.parent = null;
  this.window = null;
  if (this.is_popup)
    this.parent = win;
  else
    this.window = win;
  this.handler = handler;
  this.left = left;
  this.top = top;
  this.width = width || x999width;
  this.height = height || x999height;
  this.cell_width = cell_width || x999cell_width;
  this.cell_height = cell_height || x999cell_height;
  this.bg_color = bg_color || x999bg_color;
  this.sel_month = sel_month;
  this.sel_year = sel_year;
  this.sel_hour = sel_hour;
  if (this.sel_hour == null) this.sel_hour = 1;
  this.sel_minute = sel_minute;
  if (this.sel_minute == null) this.sel_minute = 0;
  this.sel_ampm = sel_ampm;
  if (this.sel_ampm == null) this.sel_ampm = 'AM';
  if (this.sel_month == null) this.sel_month = x999currentmonth;
  if (this.sel_year == null) this.sel_year = x999currentyear;
  this.days = new Array(42);
  this.cal = null;
  this.day_table = null;

  if (x999NS4compatible)
  {
    x999cal.prototype.create_cal = x999cal_create_cal_NS4;
    x999cal.prototype.show_days = x999cal_show_days_NS4;
  }
  else
  {
    x999cal.prototype.create_cal = x999cal_create_cal_IE4;
    x999cal.prototype.show_days = x999cal_show_days_IE4;
  }
  x999cal.prototype.popup_cal = x999cal_popup_cal;
  x999cal.prototype.prev_year = x999cal_prev_year;
  x999cal.prototype.next_year = x999cal_next_year;
  x999cal.prototype.new_year = x999cal_new_year;
  x999cal.prototype.prev_month = x999cal_prev_month;
  x999cal.prototype.next_month = x999cal_next_month;
  x999cal.prototype.clicked_day = x999cal_clicked_day;
  x999cal.prototype.close = x999cal_close;
  if (x999cal.prototype.id == null) x999cal.prototype.id = 0;
  x999cal.prototype.id++;
  this.id = x999cal.prototype.id;
  this.cal_id = 'cal' + this.id;
  this.day_table_id = 'day_table' + this.id;
  this.select_form_id = 'select_form' + this.id;
}

function x999cal_create_cal_NS4()
{
  if (this.is_popup)
  {
    var x = this.parent.screenX + (this.parent.outerWidth - this.parent.innerWidth) + (this.left || x999left);
    var y = this.parent.screenY + (this.parent.outerHeight - this.parent.innerHeight) + (this.top || x999top);
    this.window = this.parent.open("", this.cal_id, 'screenX=' + x + ',screenY=' + y + ',innerWidth=' + this.width + ',innerHeight=' + this.height + ',scrollbars=no,resizeable=no');
    this.window.document.open();  //Need to open and close document in Navigator
    this.window.document.writeln('<title>QuickCal</title>');
    this.window.document.close();
  }

  this.cal = new Layer(this.width, this.window);
  this.cal.name = this.cal_id;
  if (this.is_popup)
  {
    this.cal.left = 0;
    this.cal.top = 0;
  }
  else
  {
    this.cal.left = this.left || x999left;
    this.cal.top = this.top || x999top;
  }
  this.cal.zIndex = 1;
  this.cal.clip.width = this.width;
  this.cal.clip.height = this.height;
  this.cal.bgColor = this.bg_color;
  this.cal.visibility = 'show';
  var doc = this.cal.document;
  var x999today = new Date();
  var vYear = x999today.getFullYear();
  
  doc.picker = this;
  doc.open();
  doc.writeln("<center>");
  doc.writeln("<table align='center' border='0'>");
  doc.writeln("  <tr>");
  doc.writeln("    <td align='center'>");
  doc.writeln("      <form name='" + this.select_form_id + "'>" + x999font);
  doc.writeln("        <input type='button' value='&lt;&lt;' onClick='document.picker.prev_year()' " + x999style + ">");
  doc.writeln("        <input type='button' value='&nbsp;&lt;&nbsp;' onClick='document.picker.prev_month()' " + x999style + ">");
  doc.writeln("    <select name='month_sel' onChange='document.picker.new_year()'  " + x999style + " >");
  for (i = 0; i < x999months.length; i++)
    doc.writeln("        <option value='" + i + "'>" + x999months[i]);
  doc.writeln("        </select></font>");
  doc.writeln("        " + x999font + " <input type = 'text' value = '" + vYear + "' size = '4' maxlength = '4' name = 'year_sel' " + x999headerstyle + " onBlur='document.picker.new_year()'>\n");
  doc.writeln("        <input type='button' value='&nbsp;&gt;&nbsp;' onClick='document.picker.next_month()' " + x999style + ">");
  doc.writeln("        <input type='button' value='&gt;&gt;' onClick='document.picker.next_year()' " + x999style + ">");
  doc.writeln("      </form>");
  
  doc.writeln("    </td>");
  doc.writeln("  </tr>");
  doc.writeln("</table>");
  doc.writeln("</center>");
  doc.close();
  this.day_table = new Layer(this.width, this.cal);
  this.day_table.name = this.day_table_id;
  this.day_table.left = 0;
  this.day_table.top = x999offsety;
  this.day_table.zIndex = 2;
  this.day_table.visibility = 'inherit';
}

function x999cal_show_days_NS4()
{
  var month_select = this.cal.document.forms[this.select_form_id]['month_sel'];
  var year_select = this.cal.document.forms[this.select_form_id]['year_sel'];
  month_select.selectedIndex = this.sel_month;
  //year_select.selectedIndex = this.sel_year - x999years[0];
  year_select.value = this.sel_year;


  var days_in_month = get_days_in_month(this.sel_month, this.sel_year);
  var day_of_week_first = (new Date(this.sel_year, this.sel_month, 1)).getDay() - 0;
  if (day_of_week_first< 1) day_of_week_first += 7;
  for (i = 0; i < this.days.length; i++) this.days[i] = 0;
  for (i = 0; i < days_in_month; i++)
    this.days[i + day_of_week_first] = i + 1;

  var doc = this.day_table.document;
  doc.picker = this;
  doc.open();
  doc.writeln("<center>");
  doc.writeln("<table align='center' border='" + x999tableborder + "'>");
  doc.writeln("<form>");
  doc.writeln("  <tr>");
    doc.writeln("  <td>&nbsp;</td>");
  for (j = 0; j < x999weekdays.length; j++)
  {
    doc.writeln("    <td align='center' valign='middle' width='" + this.cell_width + "' >" + x999weekfont + x999weekdays[j] + "</td>");
  }
  doc.writeln("  </tr>");
  for (i = 0; i < 6; i++)
  {
    doc.writeln("  <tr>");
    doc.writeln("  <td>");
    var month_select = this.cal.document.forms[this.select_form_id]['month_sel'];
    if (i*7 < days_in_month + day_of_week_first) { doc.writeln( x999font + getWeek(this.cal.document.forms[this.select_form_id]['year_sel'].value, month_select.options[month_select.selectedIndex].value,this.days[i * 7]) + "</font>") } else { doc.writeln("&nbsp;");}    doc.writeln("  </td>");
    for(j = 0; j < 7; j++)
    {
      var val = this.days[i * 7 + j];
      if (val > 0 && val < 10) val = " " + val + " ";
      if (this.days[i * 7 + j])
        doc.writeln("    <td align='center' valign='middle' height='" + this.cell_height + "'>" + x999font + "<input type='button' value='" + val + "' onClick='document.picker.clicked_day(" + this.days[i * 7 + j] + ")'></td>");
      else
        doc.writeln("    <td>&nbsp;</td>");
    }
    doc.writeln("  </tr>");
  }
  doc.writeln("</form>");
  doc.writeln("</table>");
  doc.writeln("</center>");
  doc.close();
}

function x999cal_create_cal_IE4()
{
  if (this.is_popup)
  {
    var x = this.parent.screenLeft + (this.left || x999left);
    var y = this.parent.screenTop + (this.top || x999top);
    this.window = this.parent.open("", this.cal_id, 'left=' + x + ',top=' + y + ',width=' + this.width + ',height=' + this.height + ',scrollbars=no,resizeable=no');
    this.window.document.open();
    this.window.document.writeln('<title>QuickCal</title>');
    this.window.document.close();
  }

  this.window.document.body.insertAdjacentHTML("beforeEnd", "<div id='" + this.cal_id + "' style='position:absolute'></div>");
  this.cal = this.window.document.all[this.cal_id];
  if (this.is_popup)
  {
    this.cal.style.pixelLeft = 0;
    this.cal.style.pixelTop = 0;
  }
  else
  {
    this.cal.style.pixelLeft = this.left || x999left;
    this.cal.style.pixelTop = this.top || x999top;
  }
  this.cal.style.zIndex = 1;
  this.cal.style.width = this.width;
  this.cal.style.height = this.height;
  this.cal.style.backgroundColor = this.bg_color;
  this.cal.style.visibility = 'visible';
  this.cal.picker = this;
  var x999today = new Date();
  var vYear = x999today.getFullYear();
  var str =
  "<center>\n" +
  "<table align='center'><tr><td><table align='center' border='0'>\n" +
  "      <form name='" + this.select_form_id + "'>\n" +
  "  <tr>\n" +
  "    <td align='center'>\n" +
  "        <input type='button' " + x999style + "  value='&lt;&lt;' onClick='document.all." + this.cal_id + ".picker.prev_year()'>\n" +
  "        <input type='button' " + x999style + "  value='&lt;&nbsp;' onClick='document.all." + this.cal_id + ".picker.prev_month()'>\n" +
  "        <select name='month_sel' " + x999style + "  onChange='document.all." + this.cal_id + ".picker.new_year()'>\n";
  for (i = 0; i < x999months.length; i++)
   str += "        <option value='" + i + "'>" + x999months[i] + "\n";
  str += "        </select>\n" +
  "        <input type = 'text' value = '" + vYear + "' size = '4' maxlength = '4' name = 'year_sel' " + x999style + " onBlur='document.all." + this.cal_id + ".picker.new_year()'>\n" +
  "        <input type='button' " + x999style + "  value='&gt;&nbsp;' onClick='document.all." + this.cal_id + ".picker.next_month()'>\n" +
  "        <input type='button' " + x999style + "  value='&gt;&gt;' onClick='document.all." + this.cal_id + ".picker.next_year()'>\n" +
  "     </td>\n" +
  "  </tr>\n";  
  str += "      </form>\n" +
  "</table>\n" +
  "</center>\n" +
  "<div id='" + this.day_table_id + "' style='position:absolute'></div>\n";
  //window.alert(str);
  this.cal.innerHTML = str;
  this.day_table = this.window.document.all[this.day_table_id];
  this.day_table.style.pixelLeft = 4;
  this.day_table.style.pixelTop = x999offsety;
  this.day_table.style.zIndex = 2;
  this.day_table.style.visibility = 'inherit';
  this.day_table.picker = this;
}

function y2k(number) { return (number < 1000) ? number + 1900 : number; }

function getWeek(year,month,day) {
    var when = new Date(year,month,day);
    var newYear = new Date(year,0,1);
    var offset = 7 + 1 - newYear.getDay();
    if (offset == 8) offset = 1;
    var daynum = ((Date.UTC(y2k(year),when.getMonth(),when.getDate(),0,0,0) - Date.UTC(y2k(year),0,1,0,0,0)) /1000/60/60/24) + 1;
    var weeknum = Math.floor((daynum-offset+7)/7);
    if (month == 0 && weeknum > 7) weeknum = 0;
    if (offset > 4) weeknum = weeknum + 1;
    if (weeknum == 0) {
        year--;
        var prevNewYear = new Date(year,0,1);
        var prevOffset = 7 + 1 - prevNewYear.getDay();
        if (prevOffset == 2 || prevOffset == 8) weeknum = 53; else weeknum = 52;
    }
    return weeknum;
}

function x999cal_show_days_IE4()
{
  var month_select = this.window.document.forms[this.select_form_id]['month_sel'];
  var year_select = this.window.document.forms[this.select_form_id]['year_sel'];
  month_select.selectedIndex = this.sel_month;
 // year_select.selectedIndex = this.sel_year - x999years[0];
  year_select.value = this.sel_year;

  var days_in_month = (new Date(this.sel_year, this.sel_month+1, 0)).getDate();
  var day_of_week_first = (new Date(this.sel_year, this.sel_month, 1)).getDay() - 0;
  if (day_of_week_first< 0 ) day_of_week_first += 7;
  for (i = 0; i < this.days.length; i++) this.days[i] = 0;
  for (i = 0; i < days_in_month; i++)
    this.days[i + day_of_week_first] = i + 1;

  var str =
  "<center>\n" +
  "&nbsp;&nbsp;<table width='227' cellpadding=0 cellspacing=0><tr><td align=center><table align='center' border='" + x999tableborder + "'>\n" +
  "<form>\n" +
  "  <tr>\n";
    str += "  <td>&nbsp;</td>";
  for (j = 0; j < x999weekdays.length; j++)
  {
    str += "    <td align='center' valign='middle' width='" + this.cell_width + "'>" + x999weekfont + x999weekdays[j] + "</td>\n";
  }
  str += "  </tr>\n";
  for (i = 0; i < 6; i++)
  {
    str += "  <tr>\n";
    str += "  <td>";
    var month_select = this.cal.document.forms[this.select_form_id]['month_sel'];
    if (i*7 < days_in_month + day_of_week_first) { str += x999font + getWeek(this.cal.document.forms[this.select_form_id]['year_sel'].value, month_select.options[month_select.selectedIndex].value,this.days[i * 7]) + "</font" } else { str+="&nbsp;"}    str += "</td>";
    for(j = 0; j < 7; j++)
    {
      var val = this.days[i * 7 + j];
      if (val > 0 && val < 10) val = " " + val + " ";
      if (this.days[i * 7 + j])
        str += "    <td align='center' valign='middle' height='" + this.cell_height + "'>" + x999font + "<input type='button' " + x999daystyle + " value='" + val + "' onClick='document.all." + this.day_table_id + ".picker.clicked_day(" + this.days[i * 7 + j] + ")'></td>\n";
      else
        str += "    <td>&nbsp;</td>\n";
    }
    str += "  </tr>\n";
  }
  str +=
  "</form>\n" +
  "</table></td></tr></table>\n" +
  "</center>\n";
  this.day_table.innerHTML = str;
}

function x999cal_popup_cal()
{
  if (this.is_popup)
  {
    if (this.cal && this.window && !this.window.closed)
      this.window.close();
    else
    {
      this.create_cal();
      this.show_days();
    }
  }
  else
  {
    if (!this.cal)
    {
      if (document.forms['Reservation'].elements[this.date_fld].value != '' ) {
         var myArray = document.forms['Reservation'].elements[this.date_fld].value.toString().split("/");
         input = 'Sun, ' + myArray[1] + ' ' + x999monthabbs[((parseInt(myArray[0].charAt(0)) == 0) ? (parseInt(myArray[0].charAt(1)) -1) : (parseInt(myArray[0]) -1))] + ' 12:00:00 ' + myArray[2];
         var o2 = new Date(Date.parse(input));
         if (!isNaN(o2)) {
            this.sel_month = o2.getMonth();
            this.sel_year = o2.getYear(); if (this.sel_year < 1000) this.sel_year += 1900;
         }
      }
      this.create_cal();
      this.show_days();
    }
    else
    {
      if (x999NS4compatible)
      {
        if (this.cal.visibility == 'show')
          this.cal.visibility = 'hide';
        else
          this.cal.visibility = 'show';
      }
      else
      {
        if (this.cal.style.visibility == 'visible')
          this.cal.style.visibility = 'hidden';
        else
          this.cal.style.visibility = 'visible';
      }
      if (document.forms['Reservation'].elements[this.date_fld].value != '' ) {
         var myArray = document.forms['Reservation'].elements[this.date_fld].value.toString().split("/");
         input = 'Sun, ' + ((parseInt(myArray[1].charAt(0)) == 0) ? (parseInt(myArray[1].charAt(1))) : (parseInt(myArray[1]))) + ' ' + x999monthabbs[((parseInt(myArray[0].charAt(0)) == 0) ? (parseInt(myArray[0].charAt(1)) -1) : (parseInt(myArray[0]) -1))] + ' 12:00:00 ' + myArray[2];
         var o2 = new Date(Date.parse(input)); 
         if (!isNaN(o2)) { 
            this.sel_month = o2.getMonth(); 
            this.sel_year = o2.getYear();if (this.sel_year < 1000) this.sel_year += 1900; 
         } 
      }
    this.show_days();
    }
  }
}

//function x999cal_new_month()
//{
//  var month_select = this.cal.document.forms[this.select_form_id]['month_sel'];
//  this.sel_month = new Number(month_select.options[month_select.selectedIndex].value);
//  var year_select = this.cal.document.forms[this.select_form_id]['year_sel'];
//  this.sel_year = new Number(year_select.options[year_select.selectedIndex].value);
//  this.show_days();
//}

function x999cal_new_year()
{
 var month_select = this.cal.document.forms[this.select_form_id]['month_sel'];
 this.sel_month = new Number(month_select.options[month_select.selectedIndex].value);
 var year_select = this.cal.document.forms[this.select_form_id]['year_sel'];
 this.sel_year = new Number(year_select.value);
 if ( (isNaN(parseInt(this.sel_year))) || (this.sel_year < x999lowyear) || (this.sel_year > x999highyear) )
 {
 window.alert('This is not a valid year.');
 var x999today = new Date;
 var yr = x999today.getFullYear();
 this.sel_year = yr;
 }
this.show_days();
}

function x999cal_prev_month()
{
  this.sel_month--;
  if (this.sel_month < 0)
  {
    this.sel_month = 11;
    this.sel_year--;
  }
  this.show_days();
}


function x999cal_prev_year()
{
 this.sel_year--;
 this.show_days();
}

function x999cal_next_year()
{
 this.sel_year++;
 this.show_days();
}


function x999cal_next_month()
{
  this.sel_month++;
  if (this.sel_month > 11)
  {
    this.sel_month = 0;
    this.sel_year++;
  }
  this.show_days();
}

function x999cal_close()
{
  if (this.is_popup)
    this.window.close();
  else
    if (x999NS4compatible)
      this.cal.visibility = 'hide';
    else
      this.cal.style.visibility = 'hidden';
}
function x999cal_clicked_day(day)
{
  if (this.is_popup) {
    this.window.close();
  } else 
    if (x999NS4compatible) {
    this.cal.visibility = 'hide';
    } else {
      this.cal.style.visibility = 'hidden';  
  }  if (day != 0) { 
    var sel_date = new Date(this.sel_year, this.sel_month, day);
    if (this.handler) this.handler(this, sel_date); }
  } 
function twodigit(num)
{
  var numStr = num + '';
  if (numStr.length == 1) {
     numStr = "0" + numStr; }
  return numStr;
}
function righttwodigits(num)
{
  var numStr = num + '';
  if (numStr.length == 4) {
     numStr = numStr.substring(2,4);}
  return numStr;
}
function get_days_in_month(mon, yr)
{
  var month = new Number(mon);
  var year = new Number(yr);
  var mdays = [ [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31],
                [31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31] ];
  var isleap =
     (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) ? 1 : 0;
  return mdays[isleap][month];
}


function my_date_selected(cal, date)
{
x999time = "";
  document.forms['Reservation'].elements[cal.date_fld].value =
    '' + ''  + twodigit(date.getMonth() + 1) + '/'  + twodigit(date.getDate()) + '/'  + date.getFullYear();

  document.forms['Reservation'].elements[cal.date_fld].focus();
}



var my_cal1 = new x999cal(false, self, my_date_selected, 100, 100);
my_cal1.date_fld = 'startdate';
var my_cal2 = new x999cal(false, self, my_date_selected, 100, 100);
my_cal2.date_fld = 'fnishdate';
var my_cal3 = new x999cal(false, self, my_date_selected, 100, 100);
my_cal3.date_fld = 'finishdate';
var my_cal4 = new x999cal(false, self, my_date_selected, 100, 100);
my_cal4.date_fld = 'TicketDate';
