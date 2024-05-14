CREATE TABLE `user`
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
    `name` text NOT NULL,
    info   json not null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table player_character
(
    `id`   int(11)    NOT NULL AUTO_INCREMENT,
    `name` text NOT NULL,
    info   json not null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table player_character_campaign
(
    player_character_id int(11) not null,
    campaign_id         int(11) not null,
    key                 player_character_campaign_player_character (player_character_id),
    constraint player_character_campaign_player_character foreign key (player_character_id) references `player_character` (id),
    key                 player_character_campaign_campaign (campaign_id),
    constraint player_character_campaign_campaign foreign key (campaign_id) references `campaign` (id),
    PRIMARY KEY (player_character_id, campaign_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table non_player_character
(
    `id`   int(11)    NOT NULL AUTO_INCREMENT,
    `name` text NOT NULL,
    info   json not null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table non_player_character_campaign
(
    non_player_character_id int(11) not null,
    campaign_id             int(11) not null,
    key                     non_player_character_campaign_non_player_character (non_player_character_id),
    constraint non_player_character_campaign_non_player_character foreign key (non_player_character_id) references `non_player_character` (id),
    key                     non_player_character_campaign_campaign (campaign_id),
    constraint non_player_character_campaign_campaign foreign key (campaign_id) references `campaign` (id),
    PRIMARY KEY (non_player_character_id, campaign_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table monster
(
    `id`   int(11)    NOT NULL AUTO_INCREMENT,
    `name` text NOT NULL,
    info   json not null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table source
(
    `id`   int(11)    NOT NULL AUTO_INCREMENT,
    `name` text NOT NULL,
    info   json not null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table race
(
    `id`   int(11)    NOT NULL AUTO_INCREMENT,
    `name` text NOT NULL,
    info   json not null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table character_class
(
    `id`   int(11)    NOT NULL AUTO_INCREMENT,
    `name` text NOT NULL,
    info   json not null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table background
(
    `id`   int(11)    NOT NULL AUTO_INCREMENT,
    `name` text NOT NULL,
    info   json not null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

create table subclass
(
    `id`     int(11)    NOT NULL AUTO_INCREMENT,
    class_id int(11) not null foreign key references `character_class` (id),
    `name`   text NOT NULL,
    info     json not null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

