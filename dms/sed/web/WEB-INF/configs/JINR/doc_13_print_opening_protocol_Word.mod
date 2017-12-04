JINR/doc_13_print_opening_protocol_Word.mod


[parameters]
request_name=U:Статистика XL
service=jinr.sed.ServiceMimeData 
LOG=ON
contentType=application/msword ??of=word_ZZZ
mimeType=application/msword  ??of=word_ZZZ
inline=true  ??для загрузки в браузер
file_name=SED_protokol_#NUMBER#.doc 
[end]


[report]
    <HTML><HEAD><TITLE>#fileName#</TITLE>
    <META http-equiv=Content-Type content="text/html; charset=utf-8">
    <style>
        td {vertical-align:top;}
    </style>
    </head><body>
        $SET_PARAMETERS srn=1; rpp=9999;
        $CALL_SERVICE c=JINR/doc_13_print_opening_protocol;
    </body></html>
[end]

[preSQLs]
select PID from d_list where id=#doc_id#
select NUMBER from d_list where id=#PID#  ??PID
[end]