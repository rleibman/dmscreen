create table DND5eSceneNPC
(
    sceneId    int(11) not null,
    npcId      int(11) not null,
    key dnd5e_scene_npc_scene (sceneId),
    key dnd5e_scene_npc_npc (npcId),
    constraint dnd5e_scene_npc_scene foreign key (sceneId) references DND5eScene (id) on delete cascade,
    constraint dnd5e_scene_npc_npc foreign key (npcId) references DND5eNonPlayerCharacter (id) on delete cascade,
    PRIMARY KEY (sceneId, npcId)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
