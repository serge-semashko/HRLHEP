docs/print_doc.cfg


[report]
    $INCLUDE dat/common.dat[check login]
    $LOG1 <b>============== print_doc: doc_id=#doc_id#; USER_ID=#USER_ID#; ==================</b><br>
    $CALL_SERVICE c=sys/getARUD; ??!AR_R=Y&USER_ID
    $INCLUDE [OK report]  ??AR_R=Y
    $CALL_SERVICE c=sys/log_doc_access; doc_id=#doc_id#; rejected=1;   ??!AR_R=Y
[end]


[OK report]
    $SET_PARAMETERS DOC_DATA_RECORD_ID=;
    $GET_DATA docs/view_doc.cfg[getDocInfo]
    $INCLUDE docs/custom_settings.cfg[set custom parameters]
    $CALL_SERVICE c=#print_module#;  ??print_module
    $CALL_SERVICE c=docs/view_doc; doc_id=#doc_id#; mode=print;  ??!print_module
[end]
