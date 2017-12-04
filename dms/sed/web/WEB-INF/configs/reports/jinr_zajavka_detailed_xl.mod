reports/jinr_zajavka_detailed_xl.cfg

[parameters]
service=jinr.sed.ServiceMimeData ??cop=XL
LOG=ON
title=НИКА-РФ-Таблица закупок
of=xl ??cop=XL
contentType=application/vnd.ms-excel ??cop=XL
mimeType=application/vnd.ms-excel  ??cop=XL
file_name=SED-NICA-RF.xls
[end]


[report]
<HTML>
$SET_PARAMETERS file_name=SED-NICA-RF_#PLAN_DAT#.xls;
<HEAD><TITLE>#fileName#</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<META http-equiv=Content-Type content="text/html; charset=windows-1251"> ??

<style></style> ??
</head><body>
$SET_PARAMETERS cop=XL;
$CALL_SERVICE c=reports/jinr_zajavka_detailed.cfg;

</body></html>
[end]

