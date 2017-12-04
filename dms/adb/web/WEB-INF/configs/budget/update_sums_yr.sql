create or replace
PROCEDURE update_sums (ID1 int, TREE_ID1 int, yr varchar)
AS
-------------------------------------------------------------
BEGIN
DECLARE
  TYPE ref_cursor IS REF CURSOR;
  
-------------------------------------------------------------
cur ref_cursor;
summa_ varchar2(9);
sql_stmt varchar2(1000);
dat_ varchar2(10);
pid_ number;
existz_ number;
razdel_ varchar2(1);
budget_table varchar2(20);
nsb_table varchar2(20);
tree_table varchar2(20);
tree_all_table varchar2(20);
budget_codes_table varchar2(20);
BEGIN

budget_table:='budget_'||yr;
nsb_table:='nsb_'||yr;
tree_table:='tree_'||yr;
tree_all_table:='tree_all_'||yr;
budget_codes_table:='budget_codes_'||yr;

--берем парента от ID1 из NSB 
sql_stmt:='select pid from '||nsb_table||' where id='||ID1;
-- dbms_output.put_line (sql_stmt);
	OPEN cur FOR sql_stmt;
	fetch cur into  pid_;
	close cur;

-- если не корневой...
IF (pid_ > 0) THEN
sql_stmt:='select RAZD_NR from '||tree_all_table||' where id='||TREE_ID1;
-- dbms_output.put_line (sql_stmt);
	OPEN cur FOR sql_stmt;
	fetch cur into razdel_;
	close cur;
 
sql_stmt:='select to_char(sum(b.summa),''999990.0'') as SUMMA  
	from '||budget_table||' b, '||nsb_table||' n, '||tree_all_table||' t'
	||' where n.pid in(select pid from '||nsb_table||' where id='||ID1||')'
	||' and b.nsb_id=n.id and b.tree_id='||TREE_ID1||' and t.id='||TREE_ID1
	||' and t.razd_nr='||razdel_||' and n.r1 like ''%'||razdel_||'%''';
-- dbms_output.put_line ('=======');
-- dbms_output.put_line (sql_stmt);
	OPEN cur FOR sql_stmt;
	fetch cur into summa_;
	close cur;

sql_stmt:='select to_char(max(b.dat),''dd.mm.yyyy'') as DAT '
||' from '||budget_table||' b, '||nsb_table||' n, '||tree_all_table||' t '
||' where n.pid in(select pid from '||nsb_table||' where id='||ID1||') '
||' and b.nsb_id=n.id '
||' and b.tree_id='||TREE_ID1
||' and t.id='||TREE_ID1
||' and t.razd_nr='||razdel_
||' and n.r1 like ''%'||razdel_||'%''';
-- dbms_output.put_line (sql_stmt);
	OPEN cur FOR sql_stmt;
	fetch cur into dat_;
	close cur;


sql_stmt:='update '|| budget_table
||' set SUMMA='||summa_||', MODIFIER=0, DAT=to_date('''||dat_||''',''dd.mm.yyyy'')'
||' where NSB_ID='||pid_||' and tree_id='||TREE_ID1;
-- dbms_output.put_line (sql_stmt);
EXECUTE IMMEDIATE sql_stmt;

IF SQL%NOTFOUND THEN
	sql_stmt:='insert into '|| budget_table ||' (NSB_ID,TREE_ID,SUMMA, MODIFIER, Dat)'
	  ||' values ('||pid_||','||TREE_ID1||','||summa_||', 0, to_date('''||dat_||''',''dd.mm.yyyy'')'||')';
-- dbms_output.put_line ('=======');
-- dbms_output.put_line (sql_stmt);
	  EXECUTE IMMEDIATE sql_stmt;
END IF;
commit;
update_sums(pid_,TREE_ID1,yr);
END IF;
END;
END update_sums;
/	

SET SERVEROUTPUT ON;
exec update_sums (42, 236,'06');