files/download_file.cfg

[comments]
descr=U: Выгрузка файла по id
input=id - id файла из таблицы doc_files
output=файл или сообщение об ошибке
parents=files/file_list.cfg
author=Куняев
[end]

[parameters]
request_name=U:Скачивание файла
service=jinr.sed.ServiceFile
[end]

[report header]   ***** выполняется сервисом вначале обработки запроса
$LOG3 <hr>========================= files/download_file.cfg: service=#service#; =================<br> 
$GET_DATA [get file param]
$SET_PARAMETERS s1=Y; ??ClientIP=159.93.153.102|ClientIP=159.93.40.211|ClientIP=192.168.33.215|ClientIP=10.0.2.2
$SET_PARAMETERS s2=Y; ??ClientIP=159.93.41.37|ClientIP=159.93.41.137|ClientIP=159.93.39.20|ClientIP=159.93.40.2 
$SET_PARAMETERS service=Y; ??s1|s2
$SET_PARAMETERS AR_R=;
$SET_PARAMETERS AR_R=Y; ??service=Y
$CALL_SERVICE c=sys/getARUD; doc_id=#doc_id#; ??!AR_R=Y
$LOG3 ................ AR_R=#AR_R#; .................<br> 

$SET_PARAMETERS ERROR=Файл не зарегистрирован! ??!FILE_PATH
$SET_PARAMETERS ERROR=Нет прав для загрузки файла!; ??!AR_R=Y
[end]

[report footer]  ***** выполняется сервисом, если !ERROR
$GET_DATA [register download] ??!USER_ID=#uploader_id#&!service=Y
[end]


[ERR_MSG]   ***** выводится сервисом, если ERROR
<script>
alert("#ERROR#");
</script>
ClientIP=#ClientIP#
[end]


[get file param]
select doc_id, file_name as "FILE_NAME", file_content_type as "CONTENT_TYPE"
 , concat('#file_storage_path#', fs_file_name) as "FILE_PATH"
 , uploader_id
  , file_size as "SIZE"  ??
, file_name, file_ext, field_id, upload_date, downloaded ??
 from doc_files where id=#id#
[end]



[register download]
update doc_files set downloaded=downloaded+1 where id = #id#
[end]

