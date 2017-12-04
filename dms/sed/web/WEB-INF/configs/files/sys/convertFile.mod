files/sys/convertFile.cfg

[comments]
descr=S: Посылка файла в сервис растеризации
author=Куняев
input=file_id = id файла в таблице doc_files
parents=docs/doc_files_list.cfg
[end]

[parameters]
LOG=ON
[end]


$SET_PARAMETERS ip=137;  ??!ip

[report]
$SET_PARAMETERS ServerPath=http://159.93.33.203 ??
$SET_PARAMETERS ServerPath=http://159.93.40.211:8084  ??ServerPath=localhost
$GET_DATA files/sys/sendConvertRequest[get file param]
$SET_PARAMETERS host=http://192.168.33.215:8083  
$SET_PARAMETERS host=http://192.168.33.215:8083  ??!ds=y&ZZZ
 ??ServerName=ak0211&!p=y
$SET_PARAMETERS host=http://159.93.41.37:8080 ??USER_ID=95

id=#file_id#&width=900&type=1574&title=f_#file_id#.#file_ext#&size=#file_size#&usePDF=false&file_name=#file_name#; ??
    &file_name=#file_name# ??-cyrillic

$SET_PARAMETERS serviceUrl=#host#/converter/convert?id=#file_id#&width=900&type=1574&title=f_#file_id#.#file_ext#&size=#file_size#&usePDF=false; 
$SET_PARAMETERS url=#ServerPath##ServletPath#?c=files/download_file&id=#file_id#;
$SET_PARAMETERS urlOut=#ServerPath##ServletPath#?c=files/sys/getPage; 

Посылка запроса...
<br>Запрос: #serviceUrl#&url=#url#&urlOut=#urlOut#;<br> 
$LOG3 <b>Convert request:</b> serviceUrl=#serviceUrl#; <br>url=#url#;<br>urlOut=#urlOut#;<br>

<script type="text/javascript">

var request = "#serviceUrl#&url=" + encodeURIComponent("#url#") + "&urlOut=" + encodeURIComponent("#urlOut#");  

console.log("Шлем запрос: \n\r" + request);
alert ("Шлем запрос: \n\r" + request); ??
+ "\n\n url:" + getFileUrl + "\n\r urlOut: " + urlOut);  ??

window.open(request, "convert#file_id#");
frames["wf"].window.location.href= request; ??

</script>
[end]