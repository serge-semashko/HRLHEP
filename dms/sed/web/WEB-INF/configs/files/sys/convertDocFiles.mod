files/sys/convertDocFiles.cfg

[comments]
descr=S: Посылка запроса в сервис растеризации на конвертирование файла
input=file_id - id исходного файла в таблице doc_files, [verbose] - вывод информации (1,2,3), [child=y]
parents=files/sys/convertDocFiles.cfg
testURL=?c=files/sys/convertDocFiles&doc_id=813&verbose=3
author=Куняев
[end]


[parameters]
service=dubna.walt.service.TableServiceSpecial
tableCfg=table_no
LOG=ON
[end]


[report header]
$SET_PARAMETERS verbose=2; ??child=y
$GET_DATA [get files count]
<hr>Подготовка файлов документа #doc_id#<br>  ??verbose>0
<div>Нет файлов</div> ??cnt=0
[end]

[item]
#file_name# (#size#, #file_id#)<br>   ??verbose>2
$CALL_SERVICE c=files/sys/sendConvertRequest;
[end]

[report footer]
<hr>    ??verbose>0
[end]



=========================================
[get files count]
select doc_id from doc_files where id=#file_id#  ??file_id
;
select count(Id) as cnt from doc_files where doc_id = #doc_id#
[end]


[SQL]
select id as "file_id", file_name, format_filesize(file_size) as size
from doc_files
where doc_id=#doc_id# and not file_size is null
[end]

  ??!file_id
id=#file_id#  ??file_id
