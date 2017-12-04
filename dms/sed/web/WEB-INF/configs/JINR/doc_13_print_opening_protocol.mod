JINR/doc_13_print_opening_protocol.mod


[parameters]
request_name=U:Печать протокола вскрытия конвертов
service=jinr.sed.ServiceViewDoc  ??
service=dubna.walt.service.Service
tableCfg=table_no
LOG=ON
t_start=<table class="item"><tr><td class="label">
t_mid=</td><td class="cont">
t_lab=</td><td class="label">
t_end=</td></tr></table>
SYS_FIELDS=DOC_ID
SYS_FIELDS_TYPES=int
[end]

.s3 {font-size:13pt;}
.s2 {font-size:10pt;}
.s1 {font-size:9pt;}
.s0, table.item td.s0 {font-size:8pt;}
.s-1 {font-size:7pt;}


[report]
    $INCLUDE [head]
    $INCLUDE dat/common.dat[check login]
    $LOG1 <b>============== print_doc: doc_id=#doc_id#; USER_ID=#USER_ID#; ==================</b><br>
    $CALL_SERVICE c=sys/getARUD; ??USER_ID

    $SET_PARAMETERS DOC_DATA_RECORD_ID=;
    $GET_DATA docs/view_doc.cfg[getDocInfo]    ??AR_R=Y
    $INCLUDE docs/custom_settings.cfg[set custom parameters]

    $GET_DATA [get more data]
    $GET_DATA [get doc_id list]

    $SET_PARAMETERS s3=21px; s2=19px; s1=17px; s0=15px; s-1=12px; solid=dotted;

    <body>
    <center>
        <img src="https://sed.jinr.ru/sed/images/JINR_Logo.png">
    </center>

    <div class="b_c">Протокол вскрытия конвертов с предложением участников конкурса от #OPEN_PROTOCOL_DATE# г.</div>
    
    <br>

    <div> Конкурентная процедура на #OPEN_PROTOCOL_CONTRACT# по заявке №#Z_NUMBER# от #Z_DATE# г.</div>
    <div> Присутствовали:</div>
    <div> #OPEN_PROTOCOL_PRESENT#</div>
    <div> Процедура вскрытия конвертов с предложениями состоялась #OPEN_PROTOCOL_DATE# г. в ___ Мск.</div>
    <div> Извещение о начале проведения конкурентной процедуры было направлено в следующие компании: #provider_list# </div>
    <div> #OPEN_PROTOCOL#</div>
    <br>
    <div class="ft_sz_10">
    Предоставленные документы:
    </div>

    <table>
        $CALL_SERVICE c=JINR/doc_13_print_opening_protocol_item; field=provider; label=; mode=1;
        $CALL_SERVICE c=JINR/doc_13_print_opening_protocol_item; field=+; label=Заявка на участие; mode=3;
        $CALL_SERVICE c=JINR/doc_13_print_opening_protocol_item; field=const_docs; label=Учредительные документы; mode=2;
        $CALL_SERVICE c=JINR/doc_13_print_opening_protocol_item; field=tax_certificate; label=Свидетельство о постановке на учет в налоговом органе; mode=2;
        $CALL_SERVICE c=JINR/doc_13_print_opening_protocol_item; field=extract; label=Выписка из ЕГРЮЛ; mode=2;
        $CALL_SERVICE c=JINR/doc_13_print_opening_protocol_item; field=accnt_report; label=Бухгалтерский отчет на последнюю дату; mode=2;
        $CALL_SERVICE c=JINR/doc_13_print_opening_protocol_item; field=; label=Ренференц-лист; mode=3;
    </table>
    
    <br>
    <div>
    Результаты оформлены в виде сравнительной таблицы
    </div>
    <div class="r_sz_10_st_i"> 
    Сравнительная таблица предложений участников
    </div>
    
    <table>
        $CALL_SERVICE c=JINR/doc_13_print_opening_protocol_item; field=provider; label=Организация; mode=1; 
        $CALL_SERVICE c=JINR/doc_13_print_opening_protocol_item; field=IF(TP_d_data.technical_task=1, 'да', 'нет'); label=Соответствие конкурсной документации; mode=1;
        $CALL_SERVICE c=JINR/doc_13_print_opening_protocol_item; field=bank_guarantee; label=Банковская гарантия; mode=1;
        $CALL_SERVICE c=JINR/doc_13_print_opening_protocol_item; field=concat(IFNULL(CAST(TP_d_data.cost as CHAR),' '), ' ', TP_d_data.cost_curr); label=Общая стоимость; mode=1;
        $CALL_SERVICE c=JINR/doc_13_print_opening_protocol_item; field=VAT; label=НДС; mode=1;
        $CALL_SERVICE c=JINR/doc_13_print_opening_protocol_item; field=terms_of_payment; label=Условия оплаты; mode=1;
        $CALL_SERVICE c=JINR/doc_13_print_opening_protocol_item; field=delivery_time; label=Срок поставки; mode=1;
        $CALL_SERVICE c=JINR/doc_13_print_opening_protocol_item; field=guarantee; label=Гарантия; mode=1;
    </table>


    <script type="text/javascript" language="javascript"> 
        window.setTimeout(function(){window.print();}, 700); > ??
    </script>

    </body></html>
[end]

[head]
    <!DOCTYPE html><html><head><TITLE>Протокол вскрытия конвертов</TITLE>
    <META http-equiv=Content-Type content="text/html; charset=utf-8">

    <style>
    body {border:dotted 1px ##a0a0a0; font-family:Times; font-size:#s0#; width:510pt; margin:0 10pt 0 30pt; padding:3px;} ??
        body {border:none 1px ##a0a0a0; font-family:Times; font-size:#s0#; margin:0 20pt 0 50pt; padding:3px;}

        .s3 {font-size:#s3#;}
        .s2 {font-size:#s2#;}
        .s1 {font-size:#s1#;}
        .s0, table.item td.s0 {font-size:#s0#;}
        .s-1 {font-size:#s-1#;}
        
        .ft_sz_10 {font-size:10pt;}
        .ft_st_i {font-style:italic;}

        .b {font-weight:bold;}
        .c {text-align:center;}
        .r {text-align:right;}
        .b_c {
            font-weight:bold;
            text-align:center;
        }
        .r_sz_10_st_i {
            text-align:right;
            font-size:10pt;
            font-style:italic;
        }

        .fl {float:left;}
        .fr {float:right;}
        .clr {clear:both;}

        table {
            border-collapse: collapse;
            font-size: 10pt;
        }
        td, th {
            border: 1px solid black;
        }
        
    </style>
    </head>
[end]

[get doc_id list]
    select group_concat(TP_d_data.doc_id) as "doc_id_list"
    from d_data_14 TP_d_data
        left join d_list dh on dh.id = TP_d_data.doc_id
    where dh.pid = #doc_id#
[end]




[get more data]
    select opening_protocol AS "OPEN_PROTOCOL"
        , DATE_FORMAT(opening_protocol_date,'#dateFormat#') AS "OPEN_PROTOCOL_DATE"
        , opening_protocol_contract AS "OPEN_PROTOCOL_CONTRACT"
        , opening_protocol_present AS "OPEN_PROTOCOL_PRESENT"
    from d_data_13
    where doc_id = #doc_id# 
;
    select dh.id
        , dh.number AS "Z_NUMBER"
        , DATE_FORMAT(dh.doc_date,'#dateFormat#') AS "Z_DATE"
    from d_list dh
    where dh.id in (
        select pid from d_list where id = #doc_id#)
;
    select concat(TP_d_data.provider, ', ') as "provider_list"
    from d_data_14 TP_d_data
        left join d_list dh on dh.id = TP_d_data.doc_id
    where dh.pid = #doc_id#
[end]



