// MS - Microsofs Internet Explorer
// MZ - Mozilla
// по идее все что тута есть работает на обоих браузерах

var IE=document.all?1:0;

// ищет объект (MS&MZ copatible)
function getObject(id) {
	return IE?document.all[id]:document.getElementById(id);
}

function openWin(url, width, height) {
	if(!width) width=500;
    if(!height) height=400;
    Win=this.open(url, '', 'toolbar=no,scrollbars=yes,status=yes,height='+height+',width='+width);
    Win.focus();
}

function setLongCookie(name,value,hours)
{
  var exp=new Date();
  exp.setHours(exp.getHours()+hours);
  document.cookie=name+"="+escape(value)+"; path=/; expires="+exp.toGMTString()+";"
}

function setCookie(name, value)
{
//  alert ("Set cookie "+name+"="+value);
  if (getCookie(name) != value)
	  document.cookie = name + "=" + escape(value) + "; path=/;"
}

function getCookie(name)
{
  var search = name+"="
//alert ("Cookies:"+document.cookie);
  if (document.cookie.length > 0)  // if there are any cookies
  {
    offset = document.cookie.indexOf( search)
    if (offset != -1)	      // if cookie exists
    {
      offset += search.length
      end = document.cookie.indexOf(";", offset)
      if (end == -1) end = document.cookie.length
      return unescape( document.cookie.substring( offset, end))
    }
  }
  return null;
}

var zakladkiTop = Array(
	// Array(url, title)
	Array("?c=search", "Поиск"),
	Array("?c=view", "Просмотр/Редактирование"),
	Array("?c=add", "Добавление")
);

function drawZakladki(z, active, add) {
	document.write("<table "+add+" align=center bgcolor=white border=0 cellpadding=3 cellspacing=0 width=800>\n");
	document.write("<tr>\n");
	w = Math.round(100/z.length);
	for(i=0; i<z.length; i++) {
		cl = (i==0?(active!=i?'left':'leftActive'):
					(i==(z.length-1)?(active!=i?'right':'rightActive'):
						(active!=i?'mid':'midActive')
					)
				);
		document.write("<td width="+w+"% class="+cl+"><a href='adb"+z[i][0]+"'>"+z[i][1]+"</a></td>\n");
	}
	document.write("</tr><tr><td colspan="+(z.length)+">\n");
}
