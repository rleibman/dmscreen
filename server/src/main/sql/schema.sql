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
    `id`       int(11)                                              NOT NULL AUTO_INCREMENT,
    dmUserId   int(11)                                              not null,
    `name`     text                                                 NOT NULL,
    `version`  text                                                 NOT NULL,
    info       json                                                 not null,
    gameSystem enum ('dnd5e', 'pathfinder2e', 'starTrekAdventures') not null,
    key campaign_dm (dmUserId),
    constraint campaign_dm foreign key (dmUserId) references `dmscreenUser` (id),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table scene
(
    `id`       int(11) NOT NULL AUTO_INCREMENT,
    campaignId int(11) not null,
    `name`     text    NOT NULL,
    info       json    not null,
    `version`  text    NOT NULL,
    PRIMARY KEY (`id`),
    key scene_character_campaign (campaignId),
    constraint scene_character_campaign foreign key (campaignId) references `campaign` (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


create table playerCharacter
(
    `id`         int(11) NOT NULL AUTO_INCREMENT,
    campaignId   int(11) not null,
    `name`       text    NOT NULL,
    `playerName` text    NULL, -- TODO change to foreign key to user
    info         json    not null,
    `version`    text    NOT NULL,
    PRIMARY KEY (`id`),
    key player_character_campaign (campaignId),
    constraint player_character_campaign foreign key (campaignId) references `campaign` (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table encounter
(
    `id`       int(11) NOT NULL AUTO_INCREMENT,
    campaignId int(11) not null,
    sceneId    int(11) null,
    `name`     text    NOT NULL,
    `status`   text    NOT NULL,
    `order`    int(11) null,
    info       json    not null,
    `version`  text    NOT NULL,
    PRIMARY KEY (`id`),
    key encounter_campaign (campaignId),
    constraint encounter_campaign foreign key (campaignId) references `campaign` (id),
    key encounter_scene (sceneId),
    constraint encounter_scene foreign key (sceneId) references `scene` (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


create table nonPlayerCharacter
(
    `id`       int(11) NOT NULL AUTO_INCREMENT,
    campaignId int(11) not null,
    `name`     text    NOT NULL,
    info       json    not null,
    `version`  text    NOT NULL,
    PRIMARY KEY (`id`),
    key non_player_character_campaign (campaignId),
    constraint non_player_character_campaign foreign key (campaignId) references `campaign` (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


create table monster
(
    `id`          int(11) NOT NULL AUTO_INCREMENT,
    `name`        text    NOT NULL,
    `monsterType` text    NOT NULL,
    `biome`       text    NULL,
    `alignment`   text    NULL,
    `cr`          double  NOT NULL,
    `xp`          bigint  NOT NULL,
    `armorClass`  int(11) NOT NULL,
    `hitPoints`   int(11) NOT NULL,
    `size`        text    NOT NULL,
    `info`        json    not null,
    `version`     text    NOT NULL,
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
    key subclass_character_class (classId),
    constraint subclass_character_class foreign key (classId) references `characterClass` (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

