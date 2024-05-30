insert into dmscreenUser (`id`,
                          `name`,
                          `email`)
values (1, 'test', 'test@localhost');

insert into campaign (`id`,
                      dm,
                      `name`,
                      info,
                      version,
                      gameSystem)
values (1, 1, 'test', '{"notes": "test Notes"}', '0.0.1', 'dnd5e');