//*********************************************** ??
//меняет индикатор статуса документа на "изменён" ??
//*********************************************** ??
function recChanged(id,tid)
{
var mn="m"+id+"_"+tid;
var m=document.getElementById(mn);
m.innerHTML="<font color=red>*</font>";
var s=document.getElementById("status");
s.innerHTML="<font color=red>*изменен!*</font>";
}

function lockIt(cop,nodid)
{
openWindow('c=budget/lock&cop='+cop+'&NODID='+nodid,'_blank', 400, 100);
}

//Возвращает документ из фрейма.

function getIFrameDocument(aID){ 
 var rv = null; 
//alert('getIFrameDocument');
 // if contentDocument exists, W3C compliant (Mozilla) 
 if (document.getElementById(aID).contentDocument){ 
  // window.alert('mozilla');
   rv = document.getElementById(aID).contentDocument; 

 } else { 
   // IE 
   rv = document.frames[aID].document; 
 } 

 return rv; 
  } 


//*********************************************** ??
//highlight trigger changed (изменен триггер 		??
//подсветки по дате изменения) frm.hl_trigger		??
//*********************************************** ??

function hl_trigger_changed()
{
		
  if(theForm.hl_trigger.checked)
	parent.hl_trig="on";
  else
	parent.hl_trig="";
  theForm.submit();

}

//*********************************************** ??
//перегруженная функция, из cal2.js работает 		??
//по выходу из календарика. 						??
//если галка установлена - субмитим форму         ??
//*********************************************** ??

function calendarSelected(dd)
{	
parent.hl_date=theForm.HL.value;
if (theForm.hl_trigger.checked==true){
	theForm.submit();
	}
}

//*********************************************** ??
//правильно округляет float с точностью до        ??
//знака после запятой								??
//num: float number								??
//decplaces: decimal places after comma(dot)		??
//*********************************************** ??

function formatNumber (num, decplaces) {
    num = parseFloat(num);
    if (!isNaN(num)) {
         var str = "" + Math.round (eval(num) * Math.pow(10,decplaces));
        if (str.indexOf("e") != -1) {
            return "Out of Range";
        }
        while (str.length <= decplaces) {
            str = "0" + str;
        }
        var decpoint = str.length - decplaces;
        return str.substring(0,decpoint) + "." + str.substring(decpoint,str.length);
    } else {
        return "NaN";
    }
}

//*********************************************** ??
//функция сохранения.								??
//ищет звёздочку в спанах. 						??
//если находит -- вытягивает из имени спана 		??	
//параметры и посылает их в save.cfg, субмитит 	??
//его и снимает звёздочку со спана.				??
//если же не находит ни находит ни единой *       ??
//ставит индикатор состояния документа в значение ??
//в значение "сохранён"							??
//*********************************************** ??
function saveIt()
{
var pat='^c\\d+_\\d+$';
for(i=0; i<=theForm.elements.length-1;i++)
{ if (testName(theForm.elements[i].name,pat))
  { var s= theForm.elements[i].name;
    var paramStr=s.substring(1,s.length);
    var mn="m"+paramStr;
    var m=document.getElementById(mn);
    var z=m.innerHTML;
	z=z.replace(/(^\s+)|(\s+$)/g, ""); //типа трим
    saved=true;
    if (z!="")
    { var saved=false;
      var paramArr=paramStr.split("_");
	  
	  //var t1 = document.frames['save'];
	  //alert(t1);
	  //var iFrame = document.frames["save"];
	  //var iFrameDoc = iFrame.contentDocument || iFrame.contentWindow.document;
	  //alert(iFrameDoc);
 //var to = document.frames.save.document.saveForm;
 var to = getIFrameDocument('save').saveForm;
     //Устенко 24.08.10	var to = getIFrameDocument("save").saveForm;
      to.nsb_id.value=paramArr[0]; //nsb_id
      to.tree_id.value=paramArr[1];
      to.summa.value=theForm.elements[i].value;
      m.innerHTML="";
      to.submit();
      break;
    }
  }

}
if (saved==true)
{	var s=document.getElementById("status");
	s.innerHTML="<font color=blue>*сохранён*</font>";
 var inf1 = getIFrameDocument('info').theForm;
 //alert(inf1);
//Устенко 24.08.10  var inf1 = getIFrameDocument("info").theForm;
  if (typeof inf1 == "object")
    inf1.submit();
}

}

//*********************************************** ??
//проверка вводса склавиатуры. отсеивает 			??
//нецифровой ввод, короме "." и "-"				??
//*********************************************** ??

function check(field, event)
{
var keyCode = event.charCode ? event.charCode : event.keyCode;
//alert(typeof event.charCode);
//if (field.value=='0') field.value="0.";

if (keyCode == 8||keyCode == 9||keyCode == 37||keyCode == 39||keyCode == 46) return true; // BS, TAB, <-, ->, Del
if ((keyCode >= 48 && keyCode <= 57)) return true; // цифры
if ((keyCode == 45) && (field.value.search(/\-/)==-1)) return true; // -
if (keyCode == 46 && field.value.search(/\./) == -1 && field.value.length > 0) return true; // .

// alert(keyCode);  //
  return false;
}

function setTabCookie(yr){
	if(getCookie('curr_budget_table')==null){
		setCookie('curr_budget_table', 'BUDGET_'+yr);
	}
		setCookie('curr_budget_table', frm.ctable.value);
//		alert(frm.ctable.value);
}
function openWindow4Print(param,name,w,h)
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
  "toolbar=no,location=no,directories=no,status=no,menubar=yes,scrollbars=yes," +
  "resizable=yes,copyhistory=yes,width="+w+",height="+h);
}

//*********************************************** ??
//считаем правые                                  ??
//суммы по строкам при загрузке страницы			??
//*********************************************** ??
function update_right_sums(){
var p="^c\\d+$";
var z=0;
for (z=0; z<=nodeIDs.length-2;z++){
	sumRow(nodeIDs[z]);
}
}

//*********************************************** ??
//запускаем процесс суммирования 					??
//*********************************************** ??
function sumit(ID,TID)
{
var nn="c"+ID+"_"+TID;
  try
  { var n=document.getElementById(nn);
    if ((n.value.indexOf('.')!=-1)&&(n.value.length-n.value.indexOf('.')>2))
    { n.value=formatNumber (n.value,1);
      alert('Внимание!\n\r Разрешен ввод одной цифры после точки.\n\rЧисло округлено до '+n.value);
    }
    if (n.value=='')
    { n.value=0;
    }
//    alert (n.value);
    sumit_2(ID,TID);
  }
  catch (e) { //alert (e);
  }
}
//*********************************************** ??
//вычисляет сумму по строке						  ??
//*********************************************** ??
function sumRow(ID)
{
var sum=0.;
for(i=0; i<=colIDs.length-2;i++)
{ try{
  elem=document.getElementById("c"+ID+"_"+colIDs[i]);
  if (elem.value.length > 0)
    sum=sum+eval(elem.value);
    }
    catch (e)
    { //alert ("c"+ID+"_"+colIDs[i]);
    }
}

var m=document.getElementById("c" + ID);
m.value=formatNumber(sum,1);

}

//*********************************************** ??
//возвращает индекс элемента в массиве по 		??
//значению.										??
//*********************************************** ??

function indexOf(arr, val)
{ // alert (val)
  for(var k=0; k<arr.length-1; k++)
  { if (arr[k] == val) return k;
  }
  return 0;
}
//*********************************************** ??
//рекурсивная функция суммирования элементов 		??
//столбца											??
//Вход: ID,TID элемента, породившего суммирование ??
//Выход из рекурсии по PID=0						??
//*********************************************** ??

function sumit_2(ID,TID)
{
//var PID=parentIDs[ID-1];
  var PID=parentIDs[indexOf(nodeIDs, ID)];
// alert ("c"+ID+"_"+TID + "; PID:" + PID);
  SENDER=document.getElementById("c"+ID+"_"+TID);

  SENDER.value=eval(SENDER.value);
  sumRow(ID);

  var sum=0.;
  for(i=nodeIDs.length-1; i>=0; i--)
  { if (parentIDs[i] == PID)
    { elem = document.getElementById("c"+nodeIDs[i]+"_"+TID);
    if (elem.value.length > 0)
      sum=sum+eval(elem.value);
   }
  }

  var PPID=parentIDs[PID-1];

  if (PID!='0')
  { document.getElementById("c"+PID+"_"+TID).value=formatNumber(sum,1);
    sumit_2(PID,TID);
  }

}

//*********************************************** ??
//проверяет соответствие имени и маски.   boolean	??
//*********************************************** ??

function testName(iname,ipattern)
{ 
var re = new RegExp(ipattern);
var res = re.test (iname);
return res;
}
