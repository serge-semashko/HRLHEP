r_RF_SVOD_XL.cfg
nica/costbook/r_RF_XL.cfg

[parameters]
service=jinr.adb.ServiceMimeData 
of=bin  
contentType=application/vnd.ms-excel  
saveAsFile=NICA_SVOD.xls
mimeType=application/vnd.ms-excel 
[end]

[report]
<html><head>
<META http-equiv=Content-Type content="text/html; charset=windows-1251">
$SET_PARAMETERS XL=Y;
$CALL_SERVICE c=nica/costbook/r_RF_Svod
</body></html>
[end]


