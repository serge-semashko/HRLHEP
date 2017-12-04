// ====================== TeX convertor wrapper (for edit.cfg) ===============================
 var d_preview = document.getElementById("d_preview");
 var dd_preview = document.getElementById("dd_preview");


function showPreview(fieldName, srcFieldName)
{	
	var win= window.open( "", "PreviewWindow",
  "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes," +
  "resizable=yes,copyhistory=no,width=600,height=500");
	win.document.open();
	win.document.write('<html><body style="border:solid 1px black; margin:0; background-color:#406080;">');
	win.document.write('<table border=0 width=100% cellpadding=5 cellspacing=0><tr><th colspan=3 style="text-align:center; background-color:#406080; color:white;">Просмотр результата конвертирования ТеХ=>HTML</th></tr>');
	win.document.write('<tr><td></td></tr><tr><td></td><td bgcolor=white>');
	win.focus();
	var s = eval("document.theForm." + fieldName + ".value");
	if (s.length < 2)
		s = tex2html(eval("document.theForm." + srcFieldName + ".value"));	win.document.write(s);
	win.document.write('</td><td></td></tr><tr><td colspan=3 align=center><input type=button value="Закрыть" onClick="javascript:window.close()"></td></tr></table></body><html>');	
	win.document.close();
}

function convertTeX(srcFieldName, destFieldName)
{ re = /\r\n/g;
	var src = eval("document.theForm." + srcFieldName); 
	var dest = eval("document.theForm." + destFieldName);
	reLt = /</g;
	reGt = />/g;
	re2Sp = /  /g;
	src.value = src.value.replace(reLt, ' < ').replace(reGt, ' > ').replace(re2Sp, ' ').replace(re2Sp, ' ').replace(re2Sp, ' ');
	dest.value=tex2html(src.value);
	if (dest.value.length < 2) 
	{ dest.value = "-"; 
	}
//	alert (destFieldName + ": '" + dest.value + "'");
}

/* Основная функция - 
* правка после конвертирования LaLeX в HTML ( ltxParse(src) )  
*  - выбрасываем все <td> и <tr> вне таблиц 
*  - удаляем перевод строки, анкоры, дублируем # 
*/
function tex2html(src)
{ 
	var t = ltxParse(src);
//	alert ("'" + t + "'"); 
  var res = "";
  var s = "";
  var tab = "";
  var reTD=/<t[rd].*>/g;
  var reTDE= new RegExp("</t[rd]>","g");
  var reTE= new RegExp("</table>","g");
	var reA = /<a[^<]*>/g;
	var reAE = new RegExp("</a>","g");
  var j = 1;
  for (i = 0; j>0; )
  { i = t.indexOf("<table"); //проверяем - есть ли таблица
    if (i >= 0) // если есть
	{ j = t.indexOf("</table", i); // - ищем закрытие
	  k = t.indexOf("<table", i+8);  // и проверяем - есть ли вложенная таблица
	  while (k>0 && j > k)		//пропускаем все вложенные таблицы (надо проверить, работает ли при уровне вложенности > 2)
		{ j = t.indexOf("</table", j+8);
 	   k = t.indexOf("<table", k+8);
	 }
	  if (j > 0)
  	  tab = t.substring(i, j+8); // берем таблицы, как есть
	}
    else
		{ i = t.length; // берем весь текст
	  tab = "";
	  j = -1;
	}
	s = t.substring(0,i);
		res += s.replace(reTD," ").replace(reTDE," ").replace(reTE," ") + tab; // выбрасываем все <td <tr </td> </tr>
	if (j > 0)
	 t = t.substring(j+9);
  }
//  return res;
//	t = compressHTML(res.replace(/\r\n/g," ").replace(/#/g,"####").replace(reA,"").replace(reAE,"")); 
//	alert ("'" + t + "'"); 
  return compressHTML(res.replace(/\r\n/g," ").replace(/#/g,"####").replace(reA,"").replace(reAE,""));
}

function compressHTML(src)
{ var res = src.replace(/\n\s+([^\s])/g, '\n$1');
  res = res.replace(/>\r?\n/g, '>');
  return res.replace(/\r?\n</g, '<');
}
