gateway/receive

[parameters]
request_name=S:Прием  объекта
service=jinr.sed.gateway.ServiceReceiveObject 
LOG=ON
[end]


[LOG REQUEST]
$LOG3 <hr>receive<br>
$LOG3 ClientID=#ClientID#; Command=#Command#; ObjectType=#ObjectType#; ObjectID=#ObjectID#; GlobalID=#GlobalID#; ClientObjectID=#ClientObjectID#;<br>
$LOG9 Object=#Object#;<br> 
[end]


[process request]
$LOG3 <hr>----- receive[process request] <br>---------- Command=#Command#;<br>

$SET_PARAMETERS ERR_CODE=1; ERROR=Неверный тип объекта ObjectType=#ObjectType#; 
$INCLUDE [process receive]  ??Command=PutObject|Command=DeleteObject|Command=UnDeleteObject
$INCLUDE [process register] ??Command=RegisterReceiverObjectID

$SET_PARAMETERS ERR_CODE=3; ??ERROR&!ERR_CODE|ERR_CODE=0
[end]


[process register]
$CALL_SERVICE c=gateway/register_dogovor;    ??ObjectType=38 
$CALL_SERVICE c=gateway/register_60_visitor;    ??ObjectType=60 
[end]

[process receive]  
$LOG2 <hr>gateway/receive[process receive] ObjectType=#ObjectType#<br> 
$CALL_SERVICE c=gateway/receive_1_kontragent; ??ObjectType=1
$CALL_SERVICE c=gateway/receive_3_podrazd;    ??ObjectType=3
$CALL_SERVICE c=gateway/receive_7_sotrudnik;  ??ObjectType=7
$CALL_SERVICE c=gateway/receive_8_fizlitso;   ??ObjectType=8
$CALL_SERVICE c=gateway/receive_4_5_WBS_WU;   ??ObjectType=5|ObjectType=4
$CALL_SERVICE c=gateway/receive_6_dogpodr;    ??ObjectType=6
$CALL_SERVICE c=gateway/receive_23_rate;      ??ObjectType=23
$CALL_SERVICE c=gateway/receive_61_visitor;   ??ObjectType=61
$CALL_SERVICE c=gateway/receive_70_bc;        ??ObjectType=70
[end]



[report]
$INCLUDE gateway/receive[OK]       ??!ERROR
$INCLUDE gateway/receive[ERROR]    ??ERROR
[end]


[OK]
$SET_PARAMETERS Result=Запись создана;    ??!Result&!REC_EXISTS&Command=PutObject
$SET_PARAMETERS Result=Запись обновлена;  ??!Result&REC_EXISTS&Command=PutObject
$SET_PARAMETERS Result=Запись удалена;    ??!Result&REC_EXISTS&Command=DeleteObject
$SET_PARAMETERS Result=Запись не найдена; ??!Result&!REC_EXISTS&Command=DeleteObject
{"ResultCode":"0", "Result":"#Result#", "ClientObjectID":"#REC_ID#"}
[end]

[ERROR]
{
"ResultCode":"#ERR_CODE#",
"Result":"#ERROR#",
"ClientObjectID":"#REC_ID#"
}
[end]
