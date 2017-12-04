files/sys/sendConvertRequest.mod

[comments]
descr=S: Посылка запроса в сервис растеризации на конвертирование файла
input=file_id - id исходного файла в таблице doc_files, [verbose] - вывод информации
parents=files/sys/convertDocFiles.cfg
testURL=?c=files/sys/sendConvertRequest&file_id=541&verbose=3
author=Куняев
[end]

[parameters]
service=jinr.sed.viewer.ServiceSendConvertRequest
LOG=ON
[end]



$SET_PARAMETERS serviceUrl=http://159.93.41.37:8080/converter/convert?id=#file_id#&width=900&type=1574&title=f_#file_id#.#file_ext#&size=#file_size#&usePDF=false; ??USER_ID=95
$SET_PARAMETERS serviceUrl=http://159.93.153.102:8083/converter/convert?id=#file_id#&width=900&type=1574&title=f_#file_id#.#file_ext#&size=#file_size#&usePDF=false;  ??!USER_ID=95
$SET_PARAMETERS serviceUrl=http://192.168.33.215:8083/converter/convert?id=#file_id#&width=900&type=1574&title=f_#file_id#.#file_ext#&size=#file_size#&usePDF=false; ??ServerName=ak0211

[report header]
    $SET_PARAMETERS ERROR=;
    $GET_DATA [get file param]
    <b>#file_name#</b> (#size#, id:#file_id#)  ??verbose>0
    doc: <b>#doc_id#</b>  ??verbose>2
    $INCLUDE [send request]  ??!NUM_MARKUPS>0
    $INCLUDE [register error] ??NUM_MARKUPS>0
[end]

[send request]  ***** Посылка запроса на конвертирование. Вызывается только если нет заметок к файлу.
    Удаление старых страниц... ??verbose>2
    $CALL_SERVICE c=files/sys/deleteFilePages;
    $SET_PARAMETERS host=http://159.93.153.102:8083  ??
    $SET_PARAMETERS host=http://192.168.33.215:8083  
    $SET_PARAMETERS host=http://192.168.33.215:8083  ??!ds=y&ZZZ
     ??ServerName=ak0211&!p=y
    $SET_PARAMETERS host=http://159.93.41.37:8080 ??USER_ID=95

    id=#file_id#&width=900&type=1574&title=f_#file_id#.#file_ext#&size=#file_size#&usePDF=false&file_name=#file_name#; ??
        &file_name=#file_name# ??-cyrillic

    $SET_PARAMETERS serviceUrl=#host#/converter/convert?id=#file_id#&width=900&type=1574&title=f_#file_id#.#file_ext#&size=#file_size#&usePDF=false; 
    $SET_PARAMETERS url=#ServerPath##ServletPath#?c=files/download_file&id=#file_id#;
    $SET_PARAMETERS urlOut=#ServerPath##ServletPath#?c=files/sys/getPage; 

    $GET_DATA [register request]
    Посылка запроса... ??verbose>1
    <br>Запрос: #serviceUrl#&url=#url#&urlOut=#urlOut#;<br> ??verbose>2
    $LOG3 <b>Convert request:</b> serviceUrl=#serviceUrl#; <br>url=#url#;<br>urlOut=#urlOut#;<br>
[end]


[register error]
    $SET_PARAMETERS ERROR=Ошибка подготовки файла. К файлу есть заметки: #NUM_MARKUPS# !;
    $LOG_ERROR #ERROR#
    <b>#ERROR#</b>  ??verbose>1
    $GET_DATA [register error SQL]
[end]

 
[report footer]
    $GET_DATA [register responce]
    Ответ:  ??verbose>1
    #RESPONCE# (#TIME#мс.)<br> ??verbose>0
    $LOG1 <b>Convert responce: #RESPONCE#;</b>  TIME=#TIME#;<hr>
[end]


[get file param]
    select doc_id, file_name, file_ext, file_size from doc_files where id=#file_id#
    ;
    select count(*) as NUM_MARKUPS from doc_data_markups where file_id=#file_id#
[end]

[register request]
    replace into doc_file_convert(file_id, doc_id, sent)
    values(#file_id#, #doc_id#, now())
[end]


[register responce]
    select count(*) as num_pages from doc_file_pages where file_id=#file_id#
    ;
    update doc_file_convert set finished=now(), responce='#RESPONCE#'
        ,time=#TIME#  ??TIME
        ,num_pages=#num_pages# ??num_pages
    where file_id=#file_id#
[end]


[register error SQL]
    insert into doc_file_errors (doc_id, file_id, comments, dat)
    values (#doc_id#, #file_id#, '#ERROR#', now())
[end]