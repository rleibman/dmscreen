CREATE TABLE `token`
(
    tok          varchar(255) not null,
    userId       int(11)      not null,
    tokenPurpose text         not null,
    expireTime   timestamp    not null,
    PRIMARY KEY (`tok`),
    constraint `token_user_id` foreign key (userId) references `dmscreenUser` (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

alter table dmscreenUser add column `active` tinyint(4) NOT NULL DEFAULT '0';
