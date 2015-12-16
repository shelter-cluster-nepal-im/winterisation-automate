--create full winterisation coverage data based on winterisation_db

drop table if exists winterisation;
create table winterisation as select * from codes where priority is true;

--add columns
alter table winterisation add column num_hh_damage float;
alter table winterisation add column pct_damage float;
alter table winterisation add column target_hh float;
alter table winterisation add column in_db boolean;
alter table winterisation add column map_cd float;
alter table winterisation add column census_hh_dmg float;
alter table winterisation add column hh_above_1500 float;
alter table winterisation add column hh_below_1500 float;

--data cleaning
update winterisation set target_hh = null where target_hh = 0;

--set if in db
update winterisation set in_db =
case
when hlcit_code in (select vdc_code from winterisation_db) then TRUE
else FALSE end;

--get hh damage counts
update winterisation set num_hh_damage = d.tot_dmg_cnt
from vdc_damage d
where winterisation.hlcit_code = d.hlcit_code;

-- update hh_above_1500 column 1> hh count above 1500m
update winterisation set hh_above_1500 = s.sumhh from
(
	SELECT "HLCIT_CODE_VDC",SUM("HOUSEHOLD" :: int) as sumhh from "pop_hh_ward"
	where elevation_above_1500m LIKE '1'
	group by 1
)s
where winterisation.hlcit_code = s."HLCIT_CODE_VDC";

-- update hh_above_1500 column 1> hh count below 1500m
update winterisation set hh_below_1500 = s.sumhh from
(
	SELECT "HLCIT_CODE_VDC",SUM("HOUSEHOLD" :: int) as sumhh from "pop_hh_ward"
	where elevation_above_1500m LIKE '0'
	group by 1
)s
where winterisation.hlcit_code = s."HLCIT_CODE_VDC";
--add target counts through group by of winter_db
update winterisation set target_hh = s.sum from
(
	select vdc_code, SUM(target_hh::float) from winterisation_db
	group by 1
)s
where winterisation.hlcit_code = s.vdc_code;

--set pct damage... by default use regular division. if vdc_code is not null and num_hh_damage is null, set to 100 pct
update winterisation set pct_damage =
case when num_hh_damage is null or num_hh_damage = 0 then 1.0
when num_hh_damage <> 0 and target_hh is not null then target_hh/num_hh_damage end;

--map codes:
-- pct damage: if not pct damgae not null
-- neg 1: above 1500m, no coverage... above_1500 true and in_db false
-- neg 2: covered, less than 1500m... target_hh not null and ab_15 false
-- neg 3: presence but unknown coverage amount... in_db TRUE and target_hh is null
-- neg 4: not in db... in_db = FALSE

UPDATE winterisation SET map_cd = CASE
WHEN above_1500 IS TRUE AND in_db IS FALSE THEN -1
WHEN target_hh IS NOT NULL AND above_1500 IS FALSE THEN -2
WHEN in_db IS TRUE AND target_hh IS NULL THEN -3
WHEN in_db IS FALSE THEN -4
WHEN pct_damage IS NOT NULL THEN pct_damage
END;

SELECT CASE
WHEN map_cd = -1 THEN '-1: above 1500m, no coverage (red stripe)'
WHEN map_cd = -2 THEN '-2: covered, less than 1500m (green dots)'
WHEN map_cd = -3 THEN '-3: presence but unknown coverage amount (dark grey)'
WHEN map_cd = -4 THEN '-4: vdc not in consideration (light grey)'
WHEN map_cd >=0 THEN 'properly covered VDC (shades of red)'
ELSE 'THIS IS BAD' END as cd, count(*) from winterisation
group by 1;