function mustBeNumeric(field)
{

if (field.value=='0') field.value="0.";

if ((window.event.keyCode >= 48 &&
window.event.keyCode <= 57) ||
(window.event.keyCode == 46)||(window.event.keyCode == 45))
{
if ((window.event.keyCode == 45)&&(field.value.search(/\-/)==-1)){
return true;
}
if (window.event.keyCode != 46) {
return true;
}
else
{
if (field.value.search(/\./) == -1 &&
field.value.length > 0)
return true;
else
return false;
}
}
else
{
return false;
}

}


function mustBeIntOnChange(el) {
str=el.value;
reg=/\D/g ;
el.value=str.replace(reg, "");
}


function mustBeInt(field)
{
//alert(ss)
//alert(window.event.keyCode);
if (field.value=='0') field.value="0.";

if ((window.event.keyCode >= 48 &&
window.event.keyCode <= 57) ||
(window.event.keyCode == 46)||(window.event.keyCode == 45))
{
if ((window.event.keyCode == 45)&&(field.value.search(/\-/)==-1)){
return true;
}
if (window.event.keyCode != 46) {
return true;
}
else
{
//if (field.value.search(/\./) == -1 &&
//field.value.length > 0)
//return true;
//else
return false;
}
}
else
{
return false;
}

}