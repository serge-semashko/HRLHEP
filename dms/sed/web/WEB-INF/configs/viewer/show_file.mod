viewer/show_file.cfg

[comments]
descr=U: Постраничный просмотр растеризованного файла по его id
input=id - id файла из таблицы doc_files
output=Отдельное окно простмотра файла 
parents=files/file_list.cfg
children=viewer/show_file_body.cfg,viewer/common_blocks.dat
testURL=?c=viewer/show_file&id=662
author=Куняев
[end]

[description]
Головной модуль вьювера файла.
<ul>
<li>Определяет разрешение экрана и записывает куку smallScreen для ширины < 1300</li>
<li>Загружает скрипты и основные css (viewer/common_blocks.dat[head])</li>
<li>Вызывает viewer/show_file_body - отображение соержимого</li>
</ul>
[end]


[parameters]
request_name=U:Просмотр файла
[end]


[report]
$GET_DATA [get file param]
$SET_PARAMETERS file_id=#id#;
$CALL_SERVICE c=viewer/check_AR;  ??doc_id&file_name&!ERROR
$INCLUDE viewer/common_blocks.dat[head]  *** Блок <head>...</head>

<body>
<div id="content"><center><br><br><br><br>
    <img src="#imgPath#loading.gif">   ??doc_id&file_name&!ERROR
    $SET_PARAMETERS ERROR=Ошибка - файл не найден.<br>#ERROR#;  ??!doc_id|!file_name
    #ERROR#  ??ERROR
</center></div>

<script type="text/javascript">
    if(window.screen.width < 1300) 
      setCookie('smallScreen', 'true');
    else
      setCookie('smallScreen', '');
--- AJAX-вызов модуля вывода контента ---   ??
    AjaxCall("content", "c=viewer/show_file_body&file_id=#id#"); ??doc_id&file_name&!ERROR
</script>
</body></html>

$GET_DATA [register download] ??!USER_ID=#uploader_id#&active_page_id>0&!USER_ID=2309
[end]
  
================================================================================

[register download]
update doc_files set downloaded=downloaded+1 where id = #file_id#
[end]



[get file param]
SELECT f.doc_id, f.file_name
from doc_files f
where f.id = #id#
[end]
