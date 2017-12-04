create or replace
FUNCTION get_labs (ID int, SEPARATOR varchar2)
return varchar2
is
BEGIN
DECLARE
  TYPE ref_cursor IS REF CURSOR;
  cur ref_cursor;
  r varchar2(512);
  result varchar2(1024);
BEGIN
  --OPEN cur FOR 'select t.des as DES, t.id as ID from tree t where t.id!=0  connect by prior t.pid=t.id start with t.id='||ID||'  order by rownum desc';
  --OPEN cur FOR 'select t.des as DES, t.id as ID from tree t connect by prior t.pid=t.id start with t.id='||ID||'  order by rownum desc';
OPEN cur FOR 'select l.div from labs l, doc_lab dl where dl.lab_code=l.code and dl.doc_id='||ID;
LOOP
fetch cur into r;
	exit when cur%NOTFOUND;
	result:=result||r||SEPARATOR;
    -- dbms_output.put_line ('r:'||r);
	-- dbms_output.put_line ('result:'||result);
END LOOP;

close cur;
return result; 
END;
END get_labs;
/
SET SERVEROUTPUT ON;
select get_labs(1,'<br>') as LABS from dual;
-- exec get_path(49,'-->');