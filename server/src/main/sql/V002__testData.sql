insert into dmscreenUser (`id`,
                          `name`,
                          `email`)
values (1, 'administrator', 'roberto+dmscreen@leibman.net');

insert into dmscreenUser (`id`,
                          `name`,
                          `email`,
                          `deleted`,
                          `hashedPassword`)
values (2, 'testuser', 'validuser@example.com', 0, SHA2('correctpassword', 512));


insert into campaign (`id`,
                      dmUserId,
                      `name`,
                      info,
                      version,
                      gameSystem)
values (1, 1, 'test', '{"notes": "test Notes"}', '0.0.1', 'dnd5e');
