files/sys/getPage.cfg

[comments]
descr=S: Скачивание готовой растеризованной страницы
input=id - ID файла, который был отправлен на растеризацию, page - номер готовой страницы, count - кол-во страниц, url - URL для скачивания страницы
author=Куняев
[end]


[parameters]
service=jinr.sed.viewer.ServiceUploadPage
LOG=ON
[end]


[report header]
$LOG3 <xmp>queryString=#queryString#</xmp>
$GET_DATA [get page param]
[end]


[report footer]
$SET_PARAMETERS file_size=null;  ??!file_size
$SET_PARAMETERS size=#file_size#; ??!size
$SET_PARAMETERS ERROR=File size error: expected #size#B, got #file_size#B;  ??!file_size=#size#
$SET_PARAMETERS ERROR=File size error: got #file_size#B;  ??!file_size>100

$GET_DATA [register page]
OK      ??!ERROR
$INCLUDE [ERR_MSG]  ??ERROR
[end]


[ERR_MSG]
ERROR: #ERROR#;
$LOG <b>#ERROR#</b></br>
[end]

================================================================================
============================= шаблон SQL-запроса в БД ==========================
================================================================================

[get page param]
select SUBSTRING_INDEX('#queryString#', '&name=', -1) as "name_"
;
select SUBSTRING_INDEX('#name_#', '&', 1) as "name"
;
select SUBSTRING_INDEX('#name#', '.', -1) as "EXT"
;
select f.doc_id
, concat('#file_storage_path#', date_format(d.created,'%Y/%m'), '/',  cast(f.doc_id as char), '/f#id#_page_#page#.#EXT#') as "FILE_PATH" 
, concat(date_format(d.created,'%Y/%m'), '/', cast(f.doc_id as char), '/f#id#_page_#page#.#EXT#') as "FILE_REL_PATH"
, date_format(d.created,'%Y/%m') as "DOC_PATH"
from doc_files f join d_list d on d.id=f.doc_id
where f.id=#id#
[end]


[register page]
replace into doc_file_pages (file_id, page_nr, fs_file_name, file_size, err, uploaded)
values(#id#, #page#, '#FILE_REL_PATH#', #file_size#, '#ERROR#', now())
;
update doc_file_convert set errors='#ERROR#' where file_id=#id#  ??ERROR
;
update doc_files
set num_pages=(select count(id) from doc_file_pages where file_id=#id#)
, errors='#ERROR#' ??ERROR
where id=#id#
[end]

