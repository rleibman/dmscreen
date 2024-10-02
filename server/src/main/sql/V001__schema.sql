CREATE TABLE `dmscreenUser`
(
    `id`             int(11)    NOT NULL AUTO_INCREMENT,
    `hashedPassword` text       NULL,
    `name`           text       NOT NULL,
    `created`        timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `lastUpdated`    timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `email`          varchar(255)        DEFAULT NULL,
    `deleted`        tinyint(4) NOT NULL DEFAULT '0',
    `deletedDate`    timestamp  NULL     DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table campaign
(
    `id`           int(11)                                              NOT NULL AUTO_INCREMENT,
    dmUserId       int(11)                                              not null,
    `name`         text                                                 NOT NULL,
    `version`      text                                                 NOT NULL,
    `deleted`      tinyint(4)                                           NOT NULL DEFAULT '0',
    info           json                                                 not null,
    gameSystem     enum ('dnd5e', 'pathfinder2e', 'starTrekAdventures') not null,
    campaignStatus enum ('active', 'archived')                          not null,
    key campaign_dm (dmUserId),
    constraint campaign_dm foreign key (dmUserId) references `dmscreenUser` (id) on delete cascade,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table campaignLog
(
    campaignId int(11)    not null,
    message    text       not null,
    timestamp  timestamp  not null,
    key campaign_log_campaign (campaignId),
    constraint campaign_log_campaign foreign key (campaignId) references `campaign` (id) on delete cascade
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table scene
(
    `id`       int(11)    NOT NULL AUTO_INCREMENT,
    campaignId int(11)    not null,
    `orderCol` int(11)    not null,
    `name`     text       NOT NULL,
    info       json       not null,
    `version`  text       NOT NULL,
    `deleted`  tinyint(4) NOT NULL DEFAULT '0',
    `isActive` boolean    not null,
    PRIMARY KEY (`id`),
    key dnd5e_scene_character_campaign (campaignId),
    constraint dnd5e_scene_character_campaign foreign key (campaignId) references `campaign` (id) on delete cascade
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table playerCharacter
(
    `id`         int(11)    NOT NULL AUTO_INCREMENT,
    campaignId   int(11)    not null,
    `name`       text       NOT NULL,
    `playerName` text       NULL,     -- Enhancement change to foreign key to user
    source       text       not null, -- Really json, but we need to be able to perform stuff on it
    info         json       not null,
    `version`    text       NOT NULL,
    `deleted`    tinyint(4) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    key dnd5e_player_character_campaign (campaignId),
    constraint dnd5e_player_character_campaign foreign key (campaignId) references `campaign` (id) on delete cascade
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table encounter
(
    `id`       int(11)    NOT NULL AUTO_INCREMENT,
    campaignId int(11)    not null,
    sceneId    int(11)    null,
    `name`     text       NOT NULL,
    `status`   text       NOT NULL,
    `orderCol` int(11)    null,
    info       json       not null,
    `version`  text       NOT NULL,
    `deleted`  tinyint(4) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    key dnd5e_encounter_campaign (campaignId),
    constraint dnd5e_encounter_campaign foreign key (campaignId) references `campaign` (id) on delete cascade,
    key dnd5e_encounter_scene (sceneId),
    constraint dnd5e_encounter_scene foreign key (sceneId) references `scene` (id) on delete cascade
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


create table nonPlayerCharacter
(
    `id`       int(11)    NOT NULL AUTO_INCREMENT,
    campaignId int(11)    not null,
    `name`     text       NOT NULL,
    info       json       not null,
    `version`  text       NOT NULL,
    `deleted`  tinyint(4) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    key dnd5e_non_player_character_campaign (campaignId),
    constraint dnd5e_non_player_character_campaign foreign key (campaignId) references `campaign` (id) on delete cascade
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


create table monster
(
    `id`              int(11)    NOT NULL AUTO_INCREMENT,
    `sourceId`        text       NOT NULL,
    `name`            text       NOT NULL,
    `monsterType`     text       NOT NULL,
    `biome`           text       NULL,
    `alignment`       text       NULL,
    `cr`              double     NOT NULL,
    `xp`              bigint     NOT NULL,
    `armorClass`      int(11)    NOT NULL,
    `hitPoints`       int(11)    NOT NULL,
    `size`            text       NOT NULL,
    `initiativeBonus` int(11)    NOT NULL,
    `info`            json       not null,
    `version`         text       NOT NULL,
    `deleted`         tinyint(4) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table source
(
    `id`      int(11) NOT NULL AUTO_INCREMENT,
    `name`    text    NOT NULL,
    info      json    not null,
    `version` text    NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table race
(
    `id`      int(11) NOT NULL AUTO_INCREMENT,
    `name`    text    NOT NULL,
    info      json    not null,
    `version` text    NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table characterClass
(
    `id`      int(11) NOT NULL AUTO_INCREMENT,
    `name`    text    NOT NULL,
    info      json    not null,
    `version` text    NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table background
(
    `id`      int(11) NOT NULL AUTO_INCREMENT,
    `name`    text    NOT NULL,
    info      json    not null,
    `version` text    NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table subclass
(
    `id`      int(11) NOT NULL AUTO_INCREMENT,
    classId   int(11) not null,
    `name`    text    NOT NULL,
    info      json    not null,
    `version` text    NOT NULL,
    PRIMARY KEY (`id`),
    key dnd5e_subclass_character_class (classId),
    constraint dnd5e_subclass_character_class foreign key (classId) references `characterClass` (id) on delete cascade
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

