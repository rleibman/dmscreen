alter table dmscreenUser modify column created datetime not null default now();
alter table dmscreenUser modify column lastUpdated datetime null default now();
alter table dmscreenUser modify column deletedDate datetime null default now();
alter table campaignLog modify column timestamp datetime not null default now();
alter table token  modify column expireTime datetime not null default now();
