[comments]
descr=U: Выгрузка файла по id
author=Устенко
input=id - id файла из таблицы doc_out_files
output=файл или сообщение об ошибке
parents=docs/out_files_list.cfg
[end]

[comments]
author=Устенко
[end]

[parameters]
service=jinr.sed.ServiceDownloadFile
--------------------------------------------------------------------------------??
-----------перечень табличных полей для сервиса, чтобы понять, в каком поле что ??
--------------------------------------------------------------------------------??
NAME_FIELD=file_name
CT_FIELD=file_content_type
PATH_FIELD=fs_file_name
[end]
--------------------------------------------------------------------------------??
------текст сообщения об ошибке, если файл не найден----------------------------??
--------------------------------------------------------------------------------??
[file not found]
Файл не найден!
[end]

[before all]
$GET_DATA [get docid]
$SET_PARAMETERS UNAUTHORIZED_ACCESS=Y
$CALL_SERVICE c=sys/getARUD; doc_id=#doc_id#;  ??doc_id
$SET_PARAMETERS UNAUTHORIZED_ACCESS=; ??AR_R=Y
[end]

[get docid]
SELECT doc_id from doc_out_files where Id = #id#
[end]

[SQL]
select * from doc_out_files where Id=#id# ??id
[end]
