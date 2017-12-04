SET SERVEROUTPUT ON;

CREATE OR REPLACE 
PROCEDURE ADB.set_nica_wu_dates(WU_ID int, VERSION int, DATE_START char, DATE_FINISH char, USER_ID int)
AS
BEGIN
DECLARE
sql_stmt varchar2(512);
BEGIN

-- update NICA_WU
sql_stmt:='update nica_wu set DATE_START=to_date('''||DATE_START||''',''dd.mm.yyyy'')'
||' , DATE_FINISH=to_date('''||DATE_FINISH||''',''dd.mm.yyyy'')'
||' where ID='||WU_ID;
-- dbms_output.put_line ('.....');
-- dbms_output.put_line (sql_stmt);
EXECUTE IMMEDIATE sql_stmt;

-- update NICA_VER_DATA
sql_stmt:='update nica_ver_data set DATE_START=to_date('''||DATE_START||''',''dd.mm.yyyy'')'
||' , DATE_FINISH=to_date('''||DATE_FINISH||''',''dd.mm.yyyy'')'
||' , MODIFIED=SYSDATE, MODIFIER_ID='||USER_ID
||' where W_ID='||WU_ID||' and VERSION='||VERSION;
-- dbms_output.put_line ('.....');
-- dbms_output.put_line (sql_stmt);
EXECUTE IMMEDIATE sql_stmt;

-- update parents in NICA_VER_DATA
sql_stmt:='update nica_ver_data vd set vd.DATE_FINISH=(select max(w.DATE_FINISH) from nica_ver_data w where w.pid=vd.w_id and w.version='||VERSION
||') , MODIFIED=SYSDATE, MODIFIER_ID='||USER_ID
||' where w_id in (select id from nica_wbs_wu where active>0 and typ=''WBS'' connect by prior pid=id start with id='||WU_ID 
||') and version='||VERSION;
EXECUTE IMMEDIATE sql_stmt;
EXECUTE IMMEDIATE sql_stmt;

-- update parents in NICA_WBS
sql_stmt:='update nica_wbs w set w.DATE_FINISH=(select vd.DATE_FINISH from nica_ver_data vd where vd.w_id=w.id and vd.version='||VERSION
||') where w.id in (select id from nica_wbs_wu where active>0 and typ=''WBS'' connect by prior pid=id start with id='||WU_ID 
||')';
EXECUTE IMMEDIATE sql_stmt;

commit;
END;
END set_nica_wu_dates;
/


exec set_nica_wu_dates (21937, 8, '02.03.2015', '16.05.2015', 459);



create or replace
FUNCTION get_nica_current_version
return number is
BEGIN
DECLARE 
  TYPE ref_cursor IS REF CURSOR;
  cur ref_cursor;
  zzz number(10);
BEGIN
  OPEN cur FOR 'select max(id) from nica_versions where RW=1';
fetch cur into zzz;
close cur;
return zzz;
END;
END get_nica_current_version;
/


=============== EV - PLAN =====================


CREATE OR REPLACE FUNCTION getPlannedEV(ver int, unit_id int, dat date) RETURN NUMBER IS
 ev number;
-- calculate planned EV in $ for WU unit (unit_id) for date (dat) for given plan version (ver)
-- interpolates data between the points nearest to the given date
BEGIN
	select case 
		when dat < DAT_0  then 0 
		when dat > DAT_4 then EV_4
		when dat between DAT_0 and DAT_1 then EV_1*(dat-DAT_0) / (DAT_1-DAT_0)
		when dat between DAT_1 and DAT_2 then EV_1 + (EV_2 - EV_1)*(dat-DAT_1) / (DAT_2-DAT_1)
		when dat between DAT_2 and DAT_3 then EV_2 + (EV_3 - EV_2)*(dat-DAT_2) / (DAT_3-DAT_2)
		when dat between DAT_3 and DAT_4 then EV_3 + (EV_4 - EV_3)*(dat-DAT_3) / (DAT_4-DAT_3)
	end into ev
	from
	nica_ev_plan
	where w_id=unit_id and version=ver and extract(year from dat_0) = extract(year from dat)
 ;
 RETURN nvl(ev, 0);
END getPlannedEV;
/


CREATE OR REPLACE 
FUNCTION getWBSPlannedEV(ver int, wbs_ID int, dat date) RETURN NUMBER IS
 ev number;
-- calculate planned EV in $ for WBS unit (wbs_ID) at given date (dat) for the given plan version (ver)
-- summarizes data for underlying WU units
BEGIN
select sum(nvl(getPlannedEV(ver, pev.w_ID, dat),0)) into ev
	from nica_ev_plan pev where pev.w_id in
	(select w.id from nica_wbs_wu w	 where w.pid=wbs_ID and active=1) ;
--dbms_output.put_line (ver || ':' || wbs_ID|| ' / ' || to_char(dat,'DD.MM.YYYY') ||' / ' || ev);
 RETURN nvl(ev, 0);
END getWBSPlannedEV;
/



CREATE OR REPLACE 
PROCEDURE setWBSPlannedEV(ver int, wbs_ID int, yr int) as
-- set planned EV in % and $ for WBS unit (wbs_ID) in 5 point inside a year (yr) for version (ver) of the plan
-- date points are: start of WBS or beginning of the year, 1/4, 1/2, 3/4, end of WBS or end of the year
-- summarizes PEV in $ of underlying WU for these points, sets PEV in $, calculates % for these points
 D0 date;
 D1 date;
 D2 date;
 D3 date;
 D4 date;
 evv_1 number;
 evv_2 number;
 evv_3 number;
 evv_4 number;
 parent_id int;

BEGIN
-- get the start point (start of the WU or beginning of the year - what is later
 select max(date_start) into D0 from (select date_start from nica_wbs_wu w where w.id=wbs_ID
   union select to_date( '01.01.'||trim(to_char( yr,'0000')),'DD.MM.YYYY') as date_start from dual);
-- get the end point (end of the WU or end of the year - what is elier
 select min(date_finish) into D4 from (select date_finish from nica_wbs_wu w where w.id=wbs_ID
   union select to_date('31.12.'||trim(to_char( yr,'0000')),'DD.MM.YYYY') as date_finish from dual);
-- calculate intermediate dates
 D1 := (D0 + (D4 - D0) / 4);
 D2 := D0 + (D4 - D0) / 2;
 D3 := D0 + 3 * (D4 - D0) / 4;
-- clear previous data
delete from nica_ev_plan where w_id=wbs_ID;
-- insert dates
insert into nica_ev_plan (w_id, version, scenarii, dat_0, ps_0, ev_0, dat_1, dat_2, dat_3, dat_4, ps_4, modifier, modified)
values (wbs_ID, ver, 4, D0, 0, 0, D1, D2, D3, D4, 100, 0, SYSDATE);
-- obtain and insert PEV in $
select getWBSPlannedEV(ver, wbs_ID, D1) into evv_1 from dual;
-- dbms_output.put_line (to_char(D1,'DD.MM.YYYY') ||' / ' || evv_1);
select getWBSPlannedEV(ver, wbs_ID, D2) into evv_2 from dual;
select getWBSPlannedEV(ver, wbs_ID, D3) into evv_3 from dual;
select getWBSPlannedEV(ver, wbs_ID, D4) into evv_4 from dual;
update nica_ev_plan p set ev_1 = evv_1, ev_2 = evv_2, ev_3 = evv_3, ev_4 = evv_4
where w_id=wbs_ID and version=ver and DAT_1=D1;
-- calculate PEV in %
update nica_ev_plan set ps_1=100*ev_1/ev_4, ps_2=100*ev_2/ev_4, ps_3=100*ev_3/ev_4
where w_id=wbs_ID and DAT_1=D1 and not ev_4 is null and ev_4>0;
commit;
-- Recursive call for direct parent
select pid into parent_id from nica_wbs where id=wbs_ID;
      IF parent_id > -1 THEN  -- until do not react the root of the tree
				setWBSPlannedEV(ver, parent_id, yr); 
      END IF;
END setWBSPlannedEV;
/



create OR REPLACE 
PROCEDURE setAllWBSPlannedEV(ver int, yr int) as
-- calculate planned EV for ALL WBS units starting from the lowest level
-- LOOP: selects all WBS having active and reporting WU for given year
-- and calls setWBSPlannedEV for all of them
BEGIN
DECLARE 
  TYPE cur_typ IS REF CURSOR;
  cur cur_typ;
  next_wbs_id int;
BEGIN
-- delete old data for WBS
delete from nica_ev_plan where w_id<1000 and w_id>-1;
-- select low-level WBS having active and reporting WUs for the given year
OPEN cur FOR 'select w.pid from nica_wbs_wu_' || trim(to_char(ver,'990')) 
	|| ' w where w.TYP=''WU'' and w.active=1 and w.REPORTING=1 and extract(year from w.DATE_START)=' || yr;
LOOP
  fetch cur into next_wbs_id;
  exit when cur%NOTFOUND;
	setWBSPlannedEV(ver, next_wbs_id, yr);
END LOOP;
END;
end setAllWBSPlannedEV;
/
