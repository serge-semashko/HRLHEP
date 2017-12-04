doc_1_set_paper_dog.cfg

[report]
$GET_DATA [set paper dog]
<script>
    showDoc(false); 
    AjaxCall('docs_4dogovor', 'c=JINR/reports/zajavka4dogovor', true); 
    
</script>
[end]

[set paper dog]
update d_data_1 set paper_dog=
1  ??paper_doc=true
0  ??!paper_doc=true
where id=#id#
[end]
