CREATE TABLE `dmscreenUser`
(
    `id`             int(11)    NOT NULL AUTO_INCREMENT,
    `hashedPassword` text NULL,
    `name`           text      NOT NULL,
    `created`        timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `lastUpdated`    timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `email`          varchar(255)       DEFAULT NULL,
    `deleted`        tinyint(4) NOT NULL DEFAULT '0',
    `deletedDate`    timestamp NULL     DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table campaign
(
    `id`   int(11)    NOT NULL AUTO_INCREMENT,
    dm     int(11) not null,
    `name` text NOT NULL,
    info   json not null,
    gameSystem enum('dnd5e', 'pathfinder2e', 'starTrekAdventures') not null,
    key    campaign_dm (dm),
    constraint campaign_dm foreign key (dm) references `dmscreenUser` (id),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table playerCharacter
(
    `id`   int(11)    NOT NULL AUTO_INCREMENT,
    `name` text NOT NULL,
    info   json not null,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table playerCharacterCampaign
(
    playerCharacterId int(11) not null,
    campaignId        int(11) not null,
    key               player_character_campaign_player_character (playerCharacterId),
    constraint player_character_campaign_player_character foreign key (playerCharacterId) references `playerCharacter` (id),
    key               player_character_campaign_campaign (campaignId),
    constraint player_character_campaign_campaign foreign key (campaignId) references `campaign` (id),
    PRIMARY KEY (playerCharacterId, campaignId)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table nonPlayerCharacter
(
    `id`   int(11)    NOT NULL AUTO_INCREMENT,
    `name` text NOT NULL,
    info   json not null,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table nonPlayerCharacterCampaign
(
    nonPlayerCharacterId int(11) not null,
    campaignId           int(11) not null,
    key                  non_player_character_campaign_non_player_character (nonPlayerCharacterId),
    constraint non_player_character_campaign_non_player_character foreign key (nonPlayerCharacterId) references `nonPlayerCharacter` (id),
    key                  non_player_character_campaign_campaign (campaignId),
    constraint non_player_character_campaign_campaign foreign key (campaignId) references `campaign` (id),
    PRIMARY KEY (nonPlayerCharacterId, campaignId)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table monster
(
    `id`   int(11)    NOT NULL AUTO_INCREMENT,
    `name` text NOT NULL,
    info   json not null,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table source
(
    `id`   int(11)    NOT NULL AUTO_INCREMENT,
    `name` text NOT NULL,
    info   json not null,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table race
(
    `id`   int(11)    NOT NULL AUTO_INCREMENT,
    `name` text NOT NULL,
    info   json not null,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table characterClass
(
    `id`   int(11)    NOT NULL AUTO_INCREMENT,
    `name` text NOT NULL,
    info   json not null,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table background
(
    `id`   int(11)    NOT NULL AUTO_INCREMENT,
    `name` text NOT NULL,
    info   json not null,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table subclass
(
    `id`    int(11)    NOT NULL AUTO_INCREMENT,
    classId int(11) not null,
    `name`  text NOT NULL,
    info    json not null,
    PRIMARY KEY (`id`),
    key     subclass_character_class (classId),
    constraint subclass_character_class foreign key (classId) references `characterClass` (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
