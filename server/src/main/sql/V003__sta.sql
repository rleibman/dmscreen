rename table background to DND5eBackground;
rename table characterClass to DND5eCharacterClass;
rename table encounter to DND5eEncounter;
rename table monster to DND5eMonster;
rename table nonPlayerCharacter to DND5eNonPlayerCharacter;
rename table playerCharacter to DND5ePlayerCharacter;
rename table race to DND5eRace;
rename table scene to DND5eScene;
rename table `source` to DND5eSource;
rename table subclass to DND5eSubclass;

create table STACharacter
(
    `id`         int(11)    NOT NULL AUTO_INCREMENT,
    campaignId   int(11)    not null,
    `name`       text       NULL,
    `playerName` text       NULL,     -- Enhancement change to foreign key to user
    info         json       not null,
    `version`    text       NOT NULL,
    `deleted`    tinyint(4) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    key sta_character_campaign (campaignId),
    constraint sta_character_campaign foreign key (campaignId) references `campaign` (id) on delete cascade
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table STAStarship
(
    `id`         int(11)    NOT NULL AUTO_INCREMENT,
    campaignId   int(11)    not null,
    `name`       text       NULL,
    info         json       not null,
    `version`    text       NOT NULL,
    `deleted`    tinyint(4) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    key sta_starship_campaign (campaignId),
    constraint sta_starship_campaign foreign key (campaignId) references `campaign` (id) on delete cascade
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table STAScene
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
    key sta_scene_character_campaign (campaignId),
    constraint sta_scene_character_campaign foreign key (campaignId) references `campaign` (id) on delete cascade
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table STANonPlayerCharacter
(
    `id`       int(11)    NOT NULL AUTO_INCREMENT,
    campaignId int(11)    not null,
    `name`     text       NOT NULL,
    info       json       not null,
    `version`  text       NOT NULL,
    `deleted`  tinyint(4) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    key sta_non_player_character_campaign (campaignId),
    constraint sta_non_player_character_campaign foreign key (campaignId) references `campaign` (id) on delete cascade
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


