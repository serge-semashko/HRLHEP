zajavka_statistics_XL.mod

[parameters]
request_name=U:Статистика XL
service=jinr.sed.ServiceMimeData 
LOG=ON
title=SED-Report
contentType=application/vnd.ms-excel ??of=xl
mimeType=application/vnd.ms-excel  ??of=xl
inline=true  ??для загрузки в браузер
file_name=SED-statistics.xls
[end]


[report]
    <HTML><HEAD><TITLE>#fileName#</TITLE>
    <META http-equiv=Content-Type content="text/html; charset=utf-8">
    <style>
        td {vertical-align:top;}
    </style>
    </head><body>
        $SET_PARAMETERS srn=1; rpp=9999;
        $CALL_SERVICE c=JINR/reports/zajavka_statistics;
    </body></html>
[end]

