viewer/page_image.mod

[comments]
descr=U: Выгрузка растеризованной страницы файла по ее id как src для <img...> с проверкой права доступа
input=page_id - id страницы из таблицы doc_file_pages
output=содержимое файла или сообщение об ошибке
parents=files/file_list.cfg
testURL=?c=viewer/page_image&page_id=1
author=Куняев
[end]

[parameters]
service=jinr.sed.ServiceFile
LOG=ON
[end]

[report header]  ***** выполняется сервисом вначале обработки запроса
$LOG3 <hr>========================= files/download_file.cfg: service=#service#; =================<br> 
$GET_DATA [get file param]
$CALL_SERVICE c=sys/getARUD; doc_id=#doc_id#; ??
$SET_PARAMETERS AR_R=Y;
$LOG3 ................ AR_R=#AR_R#; .................<br> 

$SET_PARAMETERS ERROR=Страница не зарегистрирована! ??!FILE_PATH
$SET_PARAMETERS ERROR=Нет прав для просмотра файла!; ??!AR_R=Y
[end]


[report footer]  ***** выполняется сервисом, если !ERROR - не используется
$GET_DATA [register download] ??!USER_ID=#uploader_id#&ZZZ
[end]


[ERR_MSG]   ***** выводится сервисом, если ERROR
<script>
alert("#ERROR#");
</script>
[end]


[get file param]
SELECT concat('#file_storage_path#', p.fs_file_name) as "FILE_PATH", p.fs_file_name as "FILE_NAME" 
  , p.file_content_type as "CONTENT_TYPE"
  , f.id, f.doc_id
 from doc_file_pages p 
  join doc_files f on f.id=p.file_id
where p.id = #page_id#
[end]

select doc_id
  , file_content_type as "CONTENT_TYPE"
  , file_size as "SIZE"
 , concat('#file_storage_path#', fs_file_name) as "FILE_PATH"
 , uploader_id
, file_name, file_ext, field_id, upload_date, downloaded ??
 from doc_files where id=#id#
[end]



[register download]
update doc_files set downloaded=downloaded+1 where id = #id#
[end]

