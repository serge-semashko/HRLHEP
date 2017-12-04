drop procedure get_path;
create or replace
FUNCTION get_path (ID int, SEPARATOR varchar2, a_beg varchar2 , a_end varchar2,id_chr varchar2)
return varchar2
is
BEGIN
DECLARE
  TYPE ref_cursor IS REF CURSOR;
  cur ref_cursor;
  r varchar2(512);
  r1 varchar2(512);
  result varchar2(1024);
	i number;
BEGIN
  --OPEN cur FOR 'select t.des as DES, t.id as ID from tree t where t.id!=0  connect by prior t.pid=t.id start with t.id='||ID||'  order by rownum desc';
  OPEN cur FOR 'select t.des as DES, t.id as ID from tree t connect by prior t.pid=t.id start with t.id='||ID||'  order by rownum desc';
i:=0;
LOOP
r := '';
r1:= '';
fetch cur into r,r1;
exit when cur%NOTFOUND;
IF i>0 THEN
	result:=result||SEPARATOR||REPLACE(a_beg,id_chr,r1)||r||REPLACE(a_end,id_chr,r1);
    -- dbms_output.put_line ('r:'||r);
	-- dbms_output.put_line ('result:'||result);
ELSE
	result:=result||REPLACE(a_beg,id_chr,r1)||r||REPLACE(a_end,id_chr,r1);
END IF;

i:=i+1;
END LOOP;

close cur;
return result;
END;
END get_path;
/
SET SERVEROUTPUT ON;
select get_path(49,'::','<a>','</a>','a') as PATH from dual;
-- exec get_path(49,'-->');