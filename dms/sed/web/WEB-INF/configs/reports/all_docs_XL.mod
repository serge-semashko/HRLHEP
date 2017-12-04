all_docs_XL.cfg

[parameters]
service=jinr.sed.ServiceMimeData 
LOG=ON
title=SED-Report
contentType=application/vnd.ms-excel ??of=xl
mimeType=application/vnd.ms-excel  ??of=xl
inline=true  ??для загрузки в браузер
file_name=SED-Report.xls ??
file_name=SED-NICA-RF_#PLAN_DAT#.xls;
[end]


[report]
<HTML>
<HEAD><TITLE>#fileName#</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<META http-equiv=Content-Type content="text/html; charset=windows-1251"> ??

<style>
td {vertical-align:top;}
</style>
</head><body>
$SET_PARAMETERS srn=1; rpp=9999;
$SET_PARAMETERS modul_table=reports/all_docs_table; ??!modul_table
$SET_PARAMETERS modul_table=JINR/reports/zajavka_table; ??f_doc_type_id=1
$SET_PARAMETERS modul_table=JINR/reports/dog_podr_table; ??f_doc_type_id=6
$SET_PARAMETERS modul_table=JINR/reports/dog_post_table; ??f_doc_type_id=8
$CALL_SERVICE c=#modul_table#;
</body></html>
[end]
