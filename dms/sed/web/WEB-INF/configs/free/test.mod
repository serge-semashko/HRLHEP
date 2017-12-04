[report]
$GET_DATA [test SQL]
#RESULT#
DB DMS: #connString#: #ERROR#  ??ERROR
[end]

<br>

$SET_PARAMETERS ERROR=; RESULT=;
$USE_DB ADB 
$GET_DATA [test ADB]
DB ADB: #RESULT#
#connStringADB#: #ERROR#  ??ERROR
<br>


$SET_PARAMETERS ERROR=; RESULT=;
$USE_DB WL 
$GET_DATA [test WL]
DB WL: #RESULT#
#connStringWL#: #ERROR#  ??ERROR
<br>



$SET_PARAMETERS ERROR=; RESULT=;
$USE_DB ASU 
$GET_DATA [test ASU]
DB ASU: #RESULT#
#connStringASU#: #ERROR#  ??ERROR
<br>


[test SQL]
select 'OK' as "RESULT" 
from d_form_fields_types
where id=1
[end]

[test ADB]
select 'OK' as "RESULT" 
from nica_wbs where id=0
[end]


[test WL]
select 'OK' as "RESULT" 
from wu
where id=1
[end]

