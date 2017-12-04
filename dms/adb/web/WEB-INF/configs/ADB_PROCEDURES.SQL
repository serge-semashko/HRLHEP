SET SERVEROUTPUT ON;
-----------------------------------------------------------------------------
SET_RESPS ПОВТОРИТЬ - были ошибки!!!

create OR REPLACE 
PROCEDURE SET_RESPS AS
--
-- Заполнить табличку i_bc_resp - ответственные за бюдж. коды
-- одна запись (список отв.) на каждый бюдж.код
-- табличку надо обновлять каждый раз, когда меняется ответственный.
-- Процедура вызывается из info/bc/editCode.cfg
--
  curr_bc varchar2(4) :='-1';
  curr_resps varchar2(512) :='';
  curr_tab_n varchar2(512) :='';
BEGIN
-- очистить таблицу
delete from i_bc_resp;
-- селект всех ответственных
DECLARE CURSOR resp_list IS
  select r.bc, p.tab_n, p.f from i_persons p, i_resp r
  where r.tab_n=p.tab_n
  order by r.bc, p.f;
BEGIN
-- цикл - склеиваем всех отв. за один ВС в одну запись
FOR r IN resp_list LOOP
  IF curr_bc = r.bc THEN  -- BC тот же - склеиваем
    curr_resps :=  curr_resps ||', '|| initcap(r.f);
    curr_tab_n :=  curr_tab_n ||', '|| r.tab_n;
  ELSE  -- ВС изменился - заносим накопленное и сбрасываем
      IF curr_bc <> '-1' THEN  -- если было что-то накоплено
       insert into i_bc_resp (bc, resp, tab_n)
       values (curr_bc, curr_resps, curr_tab_n);
      END IF;
      curr_bc := r.bc;
      curr_resps := initcap(r.f);
      curr_tab_n := r.tab_n;
  END IF;
END LOOP;
 insert into i_bc_resp (bc, resp, tab_n)  -- последняя накопленная запись
 values (curr_bc, curr_resps, curr_tab_n);
commit;
end;
end SET_RESPS;
/

--------------------------------------------------------------
create or replace
PROCEDURE TEST(tname_src varchar2, tname varchar2) AS
BEGIN
DECLARE 
  TYPE cv_typ IS REF CURSOR;
  src cv_typ;
  res1 varchar2 (20);
  res2 varchar2 (20);
  sql_stmt varchar2(200);
BEGIN
  sql_stmt := 'insert into '||tname||' values (:1, :2)';
OPEN src FOR 'select A, B from '||tname_src;
LOOP
  fetch src into res1, res2;
  exit when src%NOTFOUND;
  EXECUTE IMMEDIATE sql_stmt USING res1,res2;
END LOOP;
END;
commit;
END TEST;
/

exec test('a','aa');

=============================================
create or replace
FUNCTION get_is_sum (ID_ int, Razd varchar, yr varchar)
return number is
BEGIN
DECLARE 
  TYPE ref_cursor IS REF CURSOR;
  cur ref_cursor;
  zzz number(10);
BEGIN
--  OPEN cur FOR 'select nvl(max(id),-777) from nsb_'||yr
  OPEN cur FOR 'select decode(count(*),0,0,1) from nsb_'||yr
    ||' where pid='||ID_ ||' and R1 like ''%'||Razd||'%''';
fetch cur into zzz;
close cur;
return zzz;
END;
END get_is_sum;
/

--------------------------------------------------------------

create or replace
PROCEDURE ROLLUP_BUDGET(start_tree_id int, yr varchar) AS
--
-- скручиваем бюджет вверх по дереву
-- начиная с узла "start_tree_id" - начальный узел,
-- для которого сумируются дети
BEGIN
DECLARE 
  TYPE ref_cursor IS REF CURSOR;
  cur ref_cursor;
  pid_	number;
  budget_table varchar2 (20);
  tree_all_table varchar2 (20);
  sql_stmt varchar2(1000);

  nsb_id number;
  pid number;
  s_total varchar2(10);
  last varchar2(20);
BEGIN
  budget_table := 'budget_'||yr;
  tree_all_table := 'tree_all_'||yr;
--
-- select суммарные данные из таблицы BUDGET
--
  sql_stmt := 'select b.nsb_id, t.pid, to_char(sum(b.summa),''999990.0''), to_char(max(b.dat),''DD.MM.YYYY HH24:MI'') from '
    ||budget_table||' b, '||tree_all_table||' t'
    ||' where b.tree_id=t.id and t.STATUS=''A'' and t.pid='||start_tree_id
    ||' group by nsb_id,t.pid';
    dbms_output.put_line (sql_stmt);
  OPEN cur FOR sql_stmt;
--
-- цикл по полученным данным
--
LOOP
  fetch cur into nsb_id, pid, s_total, last;
  exit when cur%NOTFOUND;
-- попытка update таблицу "budget"
	sql_stmt := 'update '||budget_table 
    ||' set summa = '||s_total||', MODIFIER=0, DAT=to_date('''
    ||last||''',''DD.MM.YYYY HH24:MI'')'
    ||' where nsb_id='||nsb_id||' and tree_id='||pid; 
--    dbms_output.put_line ('========');
--    dbms_output.put_line (sql_stmt);
  EXECUTE IMMEDIATE sql_stmt;
-- Insert новую запись, если нужная запись не найдена
	IF SQL%NOTFOUND and pid>0 THEN
		sql_stmt := 'insert into '||budget_table
      ||' (NSB_ID, TREE_ID, SUMMA, MODIFIER, DAT) values ('
      ||nsb_id||', '||pid||', '||s_total||', 0, to_date('''
      ||last||''',''DD.MM.YYYY HH24:MI''))';
--    dbms_output.put_line ('========');
--    dbms_output.put_line (sql_stmt);
    EXECUTE IMMEDIATE sql_stmt;
	END IF;
END LOOP;
commit;
--
-- рекурсивный вызов, пока не доберемся до верхушки дерева
--
  sql_stmt := 'select pid from '||tree_all_table||' where id='||start_tree_id;
  OPEN cur FOR sql_stmt;
  fetch cur into pid;
  IF (pid <> 0) THEN
    ROLLUP_BUDGET(pid, yr);
  END IF;
END;
commit;
--
-- напоследок удалим нулевые значения
-- delete from budget where abs(summa) < 0.01; (пока убрали)
END ROLLUP_BUDGET;
/

SET SERVEROUTPUT ON;
exec ROLLUP_BUDGET(141,'06');
 


--------------------------------------------------------------
create or replace
PROCEDURE ROLLUP_WHOLE_BUDGET(yr varchar) AS
BEGIN
DECLARE 
  TYPE ref_cursor IS REF CURSOR;
  cur ref_cursor;
  p number;
BEGIN
  OPEN cur for 'select id from tree_'||yr||' where id in(select pid from budgetcodes_'||yr||')';
LOOP
  fetch cur into p;
  exit when cur%NOTFOUND;
  ROLLUP_BUDGET(p,yr);
END LOOP;
end;
END ROLLUP_WHOLE_BUDGET;
/

exec ROLLUP_WHOLE_BUDGET('06');

--------------------------------------------------------------
set serveroutput on

--------------------------------------------------------------

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
dat_ varchar2(16);
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
 fetch cur into  pid_; close cur;

-- если не корневой...
IF (pid_ > 0) THEN
sql_stmt:='select RAZD_NR from '||tree_all_table||' where id='||TREE_ID1;
-- dbms_output.put_line (sql_stmt);
 OPEN cur FOR sql_stmt;
 fetch cur into razdel_; close cur;

sql_stmt:='select to_char(sum(b.summa),''999990.0'')'
  ||', to_char(max(b.dat),''DD.MM.YYYY HH:MI'')'
 ||' from '||budget_table||' b, '||nsb_table||' n, '||tree_all_table||' t'
 ||' where n.pid in (select pid from '||nsb_table||' where id='||ID1||')'
 ||' and b.nsb_id=n.id and b.tree_id=t.id and t.id='||TREE_ID1
 ||' and t.razd_nr='||razdel_||' and n.r1 like ''%'||razdel_||'%''';
-- dbms_output.put_line ('=======');
-- dbms_output.put_line (substr(sql_stmt,1,254));
-- dbms_output.put_line (substr(sql_stmt,255));
 OPEN cur FOR sql_stmt;
 fetch cur into summa_, dat_; close cur;

IF summa_ is null then 
    summa_ := 0;
    dat_ :=to_char(SYSDATE,'dd.mm.yyyy HH24:MI');
end if;

sql_stmt:='update '|| budget_table
||' set SUMMA='||summa_||', MODIFIER=-1, DAT=to_date('''||dat_||''',''dd.mm.yyyy HH24:MI'')'
||' where NSB_ID='||pid_||' and tree_id='||TREE_ID1;
-- dbms_output.put_line (sql_stmt);
EXECUTE IMMEDIATE sql_stmt;

IF SQL%NOTFOUND THEN
 sql_stmt:='insert into '|| budget_table ||' (NSB_ID,TREE_ID,SUMMA, MODIFIER, Dat)'
   ||' values ('||pid_||','||TREE_ID1||','||summa_||', -1, to_date('''||dat_||''',''dd.mm.yyyy HH24:MI'')'||')';
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
exec update_sums (42, 141,'06');
-- ========================================================
--------------------------------------------------------------

create or replace
PROCEDURE UPDATE_ALL_SUMS(nsb_id varchar, yr varchar) AS
BEGIN
DECLARE 
  TYPE ref_cursor IS REF CURSOR;
  cur ref_cursor;
  p number;
BEGIN
  OPEN cur for 'select id from budgetcodes_'||yr;
LOOP
  fetch cur into p;
  exit when cur%NOTFOUND;
--  dbms_output.put_line ('== tree_id=' || p);
  update_sums (nsb_id, p, yr);
END LOOP;
end;
END UPDATE_ALL_SUMS;
/

exec UPDATE_ALL_SUMS('1', '06');
exec UPDATE_ALL_SUMS('41', '06');

-------------------------------------------------------------


create or replace
PROCEDURE ZZZ_GET_PAYED AS
--
-- Данные регистрации документов:
-- получить данные об оплате документов за последние 50 дней
-- на основе банковских выписок
-- по номеру первичного документа в выписке
-- 
BEGIN
--
-- select новые документы, не отмеченные, как оплаченные
-- из таблицы MV_VYPISKA 
--
DECLARE CURSOR c_vyp IS
  SELECT v.DAT_OPL, v.DOCNO, to_number(v.DOC_REG_NR) as DOC_NR
  FROM mv_vypiska@asu_db v
  WHERE DOC_REG_NR is not null
  and DAT_OPL between sysdate-50 and SYSDATE
  and v.IDDOCDEF=12070
  AND v.is_prixod=0
  and v.DOC_REG_NR not in 
  ( select doc_id from docs 
    where REG_DATE between sysdate-100 and SYSDATE
    and not dat_plat_1 is null
  )
;
--
-- select новые ВАЛЮТНЫЕ документы, не отмеченные, как оплаченные
-- из таблицы MV_VYPISKA 
--
CURSOR c_vyp_val IS
  SELECT v.DAT_OPL, v.DOCNO, to_number(v.DOC_REG_NR) as DOC_NR
  FROM mv_vypiska@asu_db v
  WHERE DOC_REG_NR is not null
  and DAT_OPL between sysdate-50 and SYSDATE
  and v.IDDOCDEF=16417
  AND v.is_prixod=0
  and v.DOC_REG_NR not in 
  ( select doc_id from docs 
    where 
     REG_DATE between sysdate-100 and SYSDATE and 
     not dat_plat_1 is null
  )
;

BEGIN
--
-- цикл по результатам select-a
-- проставляем номер платежки и дату
--
FOR p IN c_vyp LOOP
-- update the table "docs"
	update docs set 
		plat_1=p.DOCNO, dat_plat_1=p.DAT_OPL, dat_opl1=p.DAT_OPL
		, payed=p.DAT_OPL
    , is_locked='Y'
	where doc_id=p.DOC_NR;
END LOOP;
 commit;
--
-- цикл по результатам select-a валюты
-- проставляем дату оплаты
--
FOR p IN c_vyp_val LOOP
-- update the table "docs"
	update docs set 
--		plat_1=p.DOCNO, dat_plat_1=p.DAT_OPL, dat_opl1=p.DAT_OPL,
		 payed=p.DAT_OPL
    , is_locked='Y'
	where doc_id=p.DOC_NR;
END LOOP;
end;
 commit;
end GET_PAYED;
/
------------------------------------------------------------------------

create or replace
PROCEDURE ZZZ_UPDATE_PAYED AS
 summa_rub$  number := 0 ;
 payed$ date := null;
BEGIN
--
-- copy the original docs into the DOCS_ARCH table
--
insert into docs_arch select * from docs where doc_id in
 (select doc_id from docs_payed) and doc_id not in (select doc_id from docs_arch);
--
-- select the new data from the DOCS_PAYED table
--
DECLARE CURSOR c_docs_payed IS
  select doc_id, bc, DOC_CODE, DOC_DATE, CHIEF_SGN, CHIEF_CODE, CHIEF_DATE, APPROVAL
   , APPROVAL_C, APPROVAL_DATE, RECEIVER, RECEIVER_C, INN_RECEIVER, INFO
   , SUMMA_CURR, CURR_CODE, RATE
   , PLAT_1, DAT_PLAT_1, DAT_OPL1, PLAT_2, DAT_PLAT_2, DAT_OPL2, PAYED
  from docs_payed;
BEGIN
--
-- loop through the new docs
--
FOR p IN c_docs_payed LOOP
 -- get the field "summa_rub"
 IF p.curr_code = '810' THEN
  summa_rub$ := p.summa_curr;
 ELSE
  summa_rub$ := p.summa_curr * p.rate;
 END IF;
 -- get the field "payed"
 IF NOT p.dat_plat_1 is null THEN
  IF NOT p.dat_opl1 is null THEN
   payed$ := p.dat_opl1;
  ELSE
   payed$ := p.dat_plat_1;
  END IF;
-- ELSE
--  payed$ := trunc(sysdate);
 END IF;
 -- update the table "docs"
 update docs set
  doc_code = p.doc_code, doc_date=p.doc_date
  , chief_sgn=initcap(p.chief_sgn), chief_code=p.chief_code, chief_date=p.chief_date
  , approval=initcap(p.approval), approval_c=p.approval_c, approval_date=p.approval_date
  , receiver=p.receiver, receiver_c=p.receiver_c, inn_receiver=p.inn_receiver
--  , info=p.info
  , plat_1=p.plat_1, plat_2=p.plat_2
--   , dat_plat_1=p.dat_plat_1, dat_opl1=p.dat_opl1
--   , dat_plat_2=p.dat_plat_2, dat_opl2=p.dat_opl2, payed=payed$
--  , summa_rub = summa_rub$, curr_code = p.curr_code, summa_curr = p.summa_curr
 where doc_id=p.doc_id;
 -- Insert a new record, if document not found
 IF SQL%NOTFOUND THEN
  insert into docs (doc_id, reg_date, doc_status, bc, doc_code, doc_date, info
  , chief_sgn, chief_code, chief_date
  , approval, approval_c, approval_date, receiver, receiver_c, inn_receiver
  , plat_1, plat_2
--  , dat_plat_1, dat_opl1, dat_plat_2, dat_opl2, payed
  , summa_rub, curr_code, summa_curr)
  values (p.doc_id, sysdate, 3, p.bc, p.doc_code, p.doc_date, p.info
  , p.chief_sgn, p.chief_code, p.chief_date
  , p.approval, p.approval_c, p.approval_date, p.receiver, p.receiver_c, p.inn_receiver
  , p.plat_1, p.plat_2
--  , p.dat_plat_1, p.dat_opl1, p.dat_plat_2, p.dat_opl2, p.dat_plat_1
  , summa_rub$, p.curr_code, p.summa_curr);
 END IF;
END LOOP;
end;
 delete from docs_payed;
 commit;
end UPDATE_PAYED;
/
------------------------------------------------------------------------

create or replace
PROCEDURE ZZZ_SET_CHILDREN_PAYED AS

BEGIN
-- Данные регистрации документов:
-- выбираем неоплаченных детей,
-- у которых оплаченные родители
-- селектим номера и даты платежек
--
DECLARE CURSOR c_vyp IS
  select D.DOC_ID, dp.PLAT_1, dp.DAT_PLAT_1, dp.DAT_OPL1
   ,dp.PLAT_2, dp.DAT_PLAT_2, dp.DAT_OPL2
from docs dp, docs d
where d.pid=dp.doc_id
  and d.dat_plat_1 is null
  and dp.dat_plat_1 is not null
  and dp.doc_id>11000
;
BEGIN
--
-- проставляем номер платежки и дату детям
--
FOR p IN c_vyp LOOP
  update docs 
   set plat_1=p.plat_1, dat_plat_1=p.dat_plat_1, dat_opl1=p.dat_opl1
     , plat_2=p.plat_2, dat_plat_2=p.dat_plat_2, dat_opl2=p.dat_opl2
     , payed=p.dat_plat_1
  where doc_id=p.doc_id;
END LOOP;
end;
 commit;
end SET_CHILDREN_PAYED;
/

exec SET_CHILDREN_PAYED;

------------------------------------------------------------------------

create OR REPLACE 
procedure ZZZ_CHACCS(sid integer, schtype varchar2, iscurr integer, schs varchar2) as
--
-- используется в Cash-Flow
-- уточнить, как
--
s varchar2(1000);
schs1 varchar2(1000);
BEGIN
schs1:=schs||',';
delete from cf_accs where pid=sid and trim(sch_type)=trim(schtype) and is_curr=iscurr;
IF schs1!=',' THEN
s:='';
FOR i IN 1..LENGTH(schs1) LOOP
  IF substr(schs1,i,1)!=',' THEN
    s:=s||substr(schs1,i,1);
      ELSE
        insert into cf_accs (pid,is_curr,sch,sch_type) values (sid,iscurr,s,schtype);
 s:='';
  END IF;
END LOOP;
END IF;
commit;
end chaccs;

