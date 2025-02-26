alter table DND5eMonster modify column `cr` text not null;
update DND5eMonster set `cr` = '1/2' where `cr` = '0.5';
update DND5eMonster set `cr` = '1/4' where `cr` = '0.25';
update DND5eMonster set `cr` = '1/8' where `cr` = '0.125';
