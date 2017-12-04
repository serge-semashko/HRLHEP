admin/doctypes/template_files_dnld.ajm

[comments]
descr=A: Выгрузка файла шаблона по id
author=Устенко
input=id - id файла из таблицы doc_templates_files
output=файл или сообщение об ошибке
parents=admin/doctypes/template_files_list.cfg
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
$GET_DATA [get typeid]
$SET_PARAMETERS UNAUTHORIZED_ACCESS=;

[end]

[get typeid]
SELECT type_id from doc_templates_files where Id = #id#
;
[end]

[SQL]
select * from doc_templates_files where Id=#id#
[end]