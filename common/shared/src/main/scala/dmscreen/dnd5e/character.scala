/*
 * Copyright 2020 Roberto Leibman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dmscreen.dnd5e.dndbeyond

case object Fake
//import java.net.URL
//
//case class DefaultBackdrop(
//                            backdropAvatarUrl: URL,
//                            smallBackdropAvatarUrl: URL,
//                            largeBackdropAvatarUrl: URL,
//                            thumbnailBackdropAvatarUrl: URL
//                          )
//
//case class ThemeColor(
//                       themeColorId: Long,
//                       themeColor: String,
//                       backgroundColor: String,
//                       name: String,
//                       raceId: String,
//                       subRaceId: String,
//                       classId: Long,
//                       tags: List[String],
//                       decorationKey: String
//                     )
//
//case class Decorations(
//                        avatarUrl: URL,
//                        frameAvatarUrl: URL,
//                        backdropAvatarUrl: URL,
//                        smallBackdropAvatarUrl: URL,
//                        largeBackdropAvatarUrl: URL,
//                        thumbnailBackdropAvatarUrl: URL,
//                        defaultBackdrop: DefaultBackdrop,
//                        avatarId: Long,
//                        portraitDecorationKey: String,
//                        frameAvatarDecorationKey: String,
//                        frameAvatarId: String,
//                        backdropAvatarDecorationKey: String,
//                        backdropAvatarId: Long,
//                        smallBackdropAvatarDecorationKey: String,
//                        smallBackdropAvatarId: Long,
//                        largeBackdropAvatarDecorationKey: String,
//                        largeBackdropAvatarId: Long,
//                        thumbnailBackdropAvatarDecorationKey: String,
//                        thumbnailBackdropAvatarId: Long,
//                        themeColor: ThemeColor
//                      )
//
//case class Ability(id: Long, name: String, value: Int, overrideValue: Int, bonusValue: Int)
//
//case class PersonalityTraits(
//                              id: Long,
//                              description: String,
//                              diceRoll: Double
//                            )
//
//case class Sources(
//                    sourceId: Long,
//                    pageNumber: Double,
//                    sourceType: Double
//                  )
//
//case class BackgroundDefinition(
//                                 id: Long,
//                                 entityTypeId: Long,
//                                 definitionKey: String,
//                                 name: String,
//                                 description: String,
//                                 snippet: String,
//                                 shortDescription: String,
//                                 skillProficienciesDescription: String,
//                                 toolProficienciesDescription: String,
//                                 languagesDescription: String,
//                                 equipmentDescription: String,
//                                 featureName: String,
//                                 featureDescription: String,
//                                 avatarUrl: URL,
//                                 largeAvatarUrl: URL,
//                                 suggestedCharacteristicsDescription: String,
//                                 suggestedProficiencies: String,
//                                 suggestedLanguages: String,
//                                 organization: String,
//                                 contractsDescription: String,
//                                 spellsPreDescription: String,
//                                 spellsPostDescription: String,
//                                 personalityTraits: List[PersonalityTraits],
//                                 ideals: List[PersonalityTraits],
//                                 bonds: List[PersonalityTraits],
//                                 flaws: List[PersonalityTraits],
//                                 isHomebrew: Boolean,
//                                 sources: List[Sources],
//                                 featList: String
//                               )
//
//case class CustomBackground(
//                             id: Long,
//                             entityTypeId: Long,
//                             name: String,
//                             description: String,
//                             featuresBackground: String,
//                             characteristicsBackground: String,
//                             featuresBackgroundDefinitionId: String,
//                             characteristicsBackgroundDefinitionId: String,
//                             backgroundType: String
//                           )
//
//case class Background(
//                       hasCustomBackground: Boolean,
//                       backgroundDefinition: BackgroundDefinition,
//                       definitionId: String,
//                       customBackground: CustomBackground
//                     )
//
//case class DisplayConfiguration(
//                                 RACIALTRAIT: Double,
//                                 ABILITYSCORE: Double,
//                                 LANGUAGE: Double,
//                                 CLASSFEATURE: Double
//                               )
//
//case class RacialTraitDefinition(
//                                  id: Long,
//                                  definitionKey: String,
//                                  entityTypeId: Long,
//                                  displayOrder: Double,
//                                  name: String,
//                                  description: String,
//                                  snippet: String,
//                                  hideInBuilder: Boolean,
//                                  hideInSheet: Boolean,
//                                  activation: String,
//                                  sourceId: Long,
//                                  sourcePageNumber: Double,
//                                  //                                  creatureRules: List[CreatureRules],
//                                  featureType: Double,
//                                  sources: List[Sources],
//                                  isCalledOut: Boolean,
//                                  entityType: String,
//                                  entityID: String,
//                                  entityRaceId: Long,
//                                  entityRaceTypeId: Long,
//                                  displayConfiguration: DisplayConfiguration,
//                                  requiredLevel: String
//                                )
//
//case class RacialTraits(
//                         racialTraitDefinition: RacialTraitDefinition
//                       )
//
//case class Normal(
//                   walk: Double,
//                   fly: Double,
//                   burrow: Double,
//                   swim: Double,
//                   climb: Double
//                 )
//
//case class WeightSpeeds(
//                         normal: Normal,
//                         encumbered: String,
//                         heavilyEncumbered: String,
//                         pushDragLift: String,
//                         `override`: String
//                       )
//
//case class Race(
//                 isSubRace: Boolean,
//                 baseRaceName: String,
//                 entityRaceId: Long,
//                 entityRaceTypeId: Long,
//                 definitionKey: String,
//                 fullName: String,
//                 baseRaceId: Long,
//                 baseRaceTypeId: Long,
//                 description: String,
//                 avatarUrl: URL,
//                 largeAvatarUrl: URL,
//                 portraitAvatarUrl: URL,
//                 moreDetailsUrl: URL,
//                 isHomebrew: Boolean,
//                 isLegacy: Boolean,
//                 groupIds: List[Double],
//                 `type`: Double,
//                 supportsSubrace: String,
//                 subRaceShortName: String,
//                 baseName: String,
//                 racialTraits: List[RacialTraits],
//                 weightSpeeds: WeightSpeeds,
//                 //                 featIds: List[FeatIds],
//                 size: String,
//                 sizeId: Long,
//                 sources: List[Sources]
//               )
//
//case class Notes(
//                  allies: String,
//                  personalPossessions: String,
//                  otherHoldings: String,
//                  organizations: String,
//                  enemies: String,
//                  backstory: String,
//                  otherNotes: String
//                )
//
//case class Traits(
//                   personalityTraits: String,
//                   ideals: String,
//                   bonds: String,
//                   flaws: String,
//                   appearance: String
//                 )
//
//case class Preferences(
//                        useHomebrewContent: Boolean,
//                        progressionType: Double,
//                        encumbranceType: Double,
//                        ignoreCoinWeight: Boolean,
//                        hitPointType: Double,
//                        showUnarmedStrike: Boolean,
//                        showScaledSpells: Boolean,
//                        primarySense: Double,
//                        primaryMovement: Double,
//                        privacyType: Double,
//                        sharingType: Double,
//                        abilityScoreDisplayType: Double,
//                        enforceFeatRules: Boolean,
//                        enforceMulticlassRules: Boolean,
//                        enableOptionalClassFeatures: Boolean,
//                        enableOptionalOrigins: Boolean,
//                        enableDarkMode: Boolean,
//                        enableContainerCurrency: Boolean
//                      )
//
//case class Configuration(
//                          startingEquipmentType: Double,
//                          abilityScoreType: Double,
//                          showHelpText: Boolean
//                        )
//
//case class GrantedModifiers(
//                             fixedValue: Double,
//                             id: String,
//                             entityId: String,
//                             entityTypeId: String,
//                             `type`: String,
//                             subType: String,
//                             dice: String,
//                             restriction: String,
//                             statId: String,
//                             requiresAttunement: Boolean,
//                             duration: String,
//                             friendlyTypeName: String,
//                             friendlySubtypeName: String,
//                             isGranted: Boolean,
//                             //                             bonusTypes: List[BonusTypes],
//                             value: Double,
//                             availableToMulticlass: Boolean,
//                             modifierTypeId: Long,
//                             modifierSubTypeId: Long,
//                             componentId: Long,
//                             componentTypeId: Long
//                           )
//
//case class InventoryDefinition(
//                                id: Long,
//                                baseTypeId: Long,
//                                entityTypeId: Long,
//                                definitionKey: String,
//                                canEquip: Boolean,
//                                magic: Boolean,
//                                name: String,
//                                snippet: String,
//                                weight: Double,
//                                weightMultiplier: Double,
//                                capacity: String,
//                                capacityWeight: Double,
//                                `type`: String,
//                                description: String,
//                                canAttune: Boolean,
//                                attunementDescription: String,
//                                rarity: String,
//                                isHomebrew: Boolean,
//                                version: String,
//                                sourceId: String,
//                                sourcePageNumber: String,
//                                stackable: Boolean,
//                                bundleSize: Double,
//                                avatarUrl: URL,
//                                largeAvatarUrl: URL,
//                                filterType: String,
//                                cost: String,
//                                isPack: Boolean,
//                                tags: List[String],
//                                grantedModifiers: List[GrantedModifiers],
//                                subType: String,
//                                isConsumable: Boolean,
//                                //                                weaponBehaviors: List[WeaponBehaviors],
//                                baseItemId: String,
//                                baseArmorName: String,
//                                strengthRequirement: String,
//                                armorClass: String,
//                                stealthCheck: String,
//                                damage: String,
//                                damageType: String,
//                                fixedDamage: String,
//                                properties: String,
//                                attackType: String,
//                                categoryId: String,
//                                range: String,
//                                longRange: String,
//                                isMonkWeapon: Boolean,
//                                levelInfusionGranted: String,
//                                sources: List[Sources],
//                                armorTypeId: String,
//                                gearTypeId: Long,
//                                groupedId: Long,
//                                canBeAddedToInventory: Boolean,
//                                isContainer: Boolean,
//                                isCustomItem: Boolean
//                              )
//
//case class Inventory(
//                      id: Long,
//                      entityTypeId: Long,
//                      inventoryDefinition: InventoryDefinition,
//                      definitionId: Long,
//                      definitionTypeId: Long,
//                      displayAsAttack: String,
//                      quantity: Double,
//                      isAttuned: Boolean,
//                      equipped: Boolean,
//                      equippedEntityTypeId: Long,
//                      equippedEntityId: Long,
//                      chargesUsed: Double,
//                      limitedUse: String,
//                      containerEntityId: Long,
//                      containerEntityTypeId: Long,
//                      containerDefinitionKey: String,
//                      currency: String
//                    )
//
//case class Currencies(
//                       cp: Double,
//                       sp: Double,
//                       gp: Double,
//                       ep: Double,
//                       pp: Double
//                     )
//
//case class ClassFeatures(
//                          id: Long,
//                          name: String,
//                          prerequisite: String,
//                          description: String,
//                          requiredLevel: Double,
//                          displayOrder: Double
//                        )
//
//case class WealthDice(
//                       diceCount: Double,
//                       diceValue: Double,
//                       diceMultiplier: Double,
//                       fixedValue: String,
//                       diceString: String
//                     )
//
//case class LevelSpellSlots(
//                            level0: Double,
//                            level1: Double,
//                            level2: Double,
//                            level3: Double,
//                            level4: Double,
//                            level5: Double,
//                            level6: Double,
//                            level7: Double,
//                            level8: Double
//                          )
//
//case class SpellRules(
//                       multiClassSpellSlotDivisor: Double,
//                       isRitualSpellCaster: Boolean,
//                       levelCantripsKnownMaxes: List[Double],
//                       levelSpellKnownMaxes: List[Double],
//                       levelSpellSlots: List[LevelSpellSlots],
//                       multiClassSpellSlotRounding: Double
//                     )
//
//case class PrerequisiteMappings(
//                                 id: Long,
//                                 entityId: Long,
//                                 entityTypeId: Long,
//                                 `type`: String,
//                                 subType: String,
//                                 value: Double,
//                                 friendlyTypeName: String,
//                                 friendlySubTypeName: String
//                               )
//
//case class Prerequisites(
//                          description: String,
//                          prerequisiteMappings: List[PrerequisiteMappings]
//                        )
//
//case class ClassDefinition(
//                            id: Long,
//                            definitionKey: String,
//                            name: String,
//                            description: String,
//                            equipmentDescription: String,
//                            parentClassId: String,
//                            avatarUrl: URL,
//                            largeAvatarUrl: URL,
//                            portraitAvatarUrl: URL,
//                            moreDetailsUrl: URL,
//                            spellCastingAbilityId: Long,
//                            sources: List[Sources],
//                            classFeatures: List[ClassFeatures],
//                            hitDice: Double,
//                            wealthDice: WealthDice,
//                            canCastSpells: Boolean,
//                            knowsAllSpells: Boolean,
//                            spellPrepareType: String,
//                            spellContainerName: String,
//                            sourcePageNumber: Double,
//                            subclassDefinition: String,
//                            isHomebrew: Boolean,
//                            primaryAbilities: List[Double],
//                            spellRules: SpellRules,
//                            prerequisites: List[Prerequisites]
//                          )
//
//case class SubclassDefinition(
//                               id: Long,
//                               definitionKey: String,
//                               name: String,
//                               description: String,
//                               equipmentDescription: String,
//                               parentClassId: Long,
//                               avatarUrl: URL,
//                               largeAvatarUrl: URL,
//                               portraitAvatarUrl: URL,
//                               moreDetailsUrl: URL,
//                               spellCastingAbilityId: Long,
//                               sources: List[Sources],
//                               subClassFeatures: List[ClassFeatures],
//                               hitDice: Double,
//                               wealthDice: String,
//                               canCastSpells: Boolean,
//                               knowsAllSpells: Boolean,
//                               spellPrepareType: String,
//                               spellContainerName: String,
//                               sourcePageNumber: Double,
//                               subclassDefinition: String,
//                               isHomebrew: Boolean,
//                               primaryAbilities: String,
//                               spellRules: String,
//                               prerequisites: String
//                             )
//
//case class SubclassFeatureDefinition(
//                                      id: Long,
//                                      definitionKey: String,
//                                      entityTypeId: Long,
//                                      displayOrder: Double,
//                                      name: String,
//                                      description: String,
//                                      snippet: String,
//                                      activation: String,
//                                      multiClassDescription: String,
//                                      requiredLevel: Double,
//                                      isSubClassFeature: Boolean,
//                                      limitedUse: List[LimitedUse],
//                                      hideInBuilder: Boolean,
//                                      hideInSheet: Boolean,
//                                      sourceId: Long,
//                                      sourcePageNumber: Double,
//                                      //                                      creatureRules: List[CreatureRules],
//                                      //                                      levelScales: List[LevelScales],
//                                      //                                      infusionRules: List[InfusionRules],
//                                      classId: Long,
//                                      featureType: Double,
//                                      sources: List[Sources],
//                                      entityType: String,
//                                      entityID: String
//                                    )
//
//case class SubclassFeatures(
//                             subclassFeatureDefinition: SubclassFeatureDefinition,
//                             levelScale: String
//                           )
//
//case class Classes(
//                    id: Long,
//                    entityTypeId: Long,
//                    level: Double,
//                    isStartingClass: Boolean,
//                    hitDiceUsed: Double,
//                    definitionId: Long,
//                    subclassDefinitionId: String,
//                    classDefinition: ClassDefinition,
//                    subclassDefinition: SubclassDefinition,
//                    subclassFeatures: List[SubclassFeatures]
//                  )
//
//case class Conditions(
//                       id: Long,
//                       level: String
//                     )
//
//case class DeathSaves(
//                       failCount: Double,
//                       successCount: Double,
//                       isStabilized: Boolean
//                     )
//
//case class SpellSlots(
//                       level: Double,
//                       used: Double,
//                       available: Double
//                     )
//
//case class RaceOptionDefinition(
//                                 id: Long,
//                                 entityTypeId: Long,
//                                 name: String,
//                                 description: String,
//                                 snippet: String,
//                                 activation: String,
//                                 sourceId: Long,
//                                 sourcePageNumber: Double,
//                                 //                                 creatureRules: List[CreatureRules],
//                               )
//
//case class RaceOptions(
//                        componentId: Long,
//                        componentTypeId: Long,
//                        raceOptionDefinition: RaceOptionDefinition
//                      )
//
//case class ClassOptions(
//                         componentId: Long,
//                         componentTypeId: Long,
//                         classOptionDefinition: RaceOptionDefinition
//                       )
//
//case class Options(
//                    raceOptions: List[RaceOptions],
//                    classOptions: List[ClassOptions],
//                    background: String,
//                    item: String,
//                    //                    feat: List[Feat]
//                  )
//
//case class RaceChoices(
//                        componentId: Long,
//                        componentTypeId: Long,
//                        id: String,
//                        parentChoiceId: String,
//                        `type`: Double,
//                        subType: String,
//                        optionValue: Double,
//                        label: String,
//                        isOptional: Boolean,
//                        isInfinite: Boolean,
//                        defaultSubtypes: List[DefaultSubtypes],
//                        displayOrder: String,
//                        options: List[Options],
//                        optionIds: List[Double]
//                      )
//
//case class DefinitionKeyNameMap(
//
//                               )
//
//case class Choices(
//                    raceChoices: List[RaceChoices],
//                    classChoices: List[RaceChoices],
//                    background: List[RaceChoices],
//                    item: String,
//                    feat: List[Feat],
//                    choiceDefinitions: String,
//                    definitionKeyNameMap: DefinitionKeyNameMap
//                  )
//
//case class LimitedUse(
//                       name: String,
//                       statModifierUsesId: String,
//                       resetType: Double,
//                       numberUsed: Double,
//                       minNumberConsumed: Double,
//                       maxNumberConsumed: Double,
//                       maxUses: Double,
//                       operator: Double,
//                       useProficiencyBonus: Boolean,
//                       proficiencyBonusOperator: Double,
//                       resetDice: String
//                     )
//
//case class ActionRange(
//                        range: Double,
//                        longRange: String,
//                        aoeType: Double,
//                        aoeSize: Double,
//                        hasAoeSpecialDescription: Boolean,
//                        minimumRange: String
//                      )
//
//case class Activation(
//                       activationTime: String,
//                       activationType: Double
//                     )
//
//case class RaceActions(
//                        componentId: Long,
//                        componentTypeId: Long,
//                        id: String,
//                        entityTypeId: String,
//                        limitedUse: LimitedUse,
//                        name: String,
//                        description: String,
//                        snippet: String,
//                        abilityModifierStatId: Long,
//                        onMissDescription: String,
//                        saveFailDescription: String,
//                        saveSuccessDescription: String,
//                        saveStatId: Long,
//                        fixedSaveDc: String,
//                        attackTypeRange: String,
//                        actionType: Double,
//                        attackSubtype: String,
//                        dice: WealthDice,
//                        value: String,
//                        damageTypeId: Long,
//                        isMartialArts: Boolean,
//                        isProficient: Boolean,
//                        spellRangeType: String,
//                        displayAsAttack: Boolean,
//                        actionRange: ActionRange,
//                        activation: Activation,
//                        numberOfTargets: String,
//                        fixedToHit: String,
//                        ammunition: String
//                      )
//
//case class Actions(
//                    raceActions: List[RaceActions],
//                    classActions: List[RaceActions],
//                    background: String,
//                    item: String,
//                    feat: List[Feat]
//                  )
//
//case class Modifiers(
//                      raceModifiers: List[GrantedModifiers],
//                      classModifiers: List[GrantedModifiers],
//                      backgroundModifiers: List[GrantedModifiers],
//                      itemModifiers: List[GrantedModifiers],
//                      featModifiers: List[FeatModifiers],
//                      conditionModifiers: List[ConditionModifiers]
//                    )
//
//case class Duration(
//                     durationInterval: Double,
//                     durationUnit: String,
//                     durationType: String
//                   )
//
//case class SpellRange(
//                       origin: String,
//                       rangeValue: Double,
//                       aoeType: String,
//                       aoeValue: String
//                     )
//
//case class AtHigherLevels(
//                           higherLevelDefinitions: List[HigherLevelDefinitions],
//                           additionalAttacks: List[AdditionalAttacks],
//                           additionalTargets: List[AdditionalTargets],
//                           areaOfEffect: List[AreaOfEffect],
//                           duration: List[Duration],
//                           creatures: List[Creatures],
//                           special: List[Special],
//                           points: List[Points],
//                           range: List[Range]
//                         )
//
//case class SpellModifiers(
//                           fixedValue: String,
//                           id: String,
//                           entityId: String,
//                           entityTypeId: String,
//                           `type`: String,
//                           subType: String,
//                           dice: String,
//                           restriction: String,
//                           statId: String,
//                           requiresAttunement: Boolean,
//                           duration: String,
//                           friendlyTypeName: String,
//                           friendlySubtypeName: String,
//                           isGranted: Boolean,
//                           bonusTypes: List[BonusTypes],
//                           value: String,
//                           availableToMulticlass: String,
//                           modifierTypeId: Long,
//                           modifierSubTypeId: Long,
//                           componentId: Long,
//                           componentTypeId: Long,
//                           die: WealthDice,
//                           count: Double,
//                           durationUnit: String,
//                           usePrimaryStat: Boolean,
//                           atHigherLevels: AtHigherLevels
//                         )
//
//case class ClassSpellDefinition(
//                                 id: Long,
//                                 definitionKey: String,
//                                 name: String,
//                                 level: Double,
//                                 school: String,
//                                 duration: Duration,
//                                 activation: Activation,
//                                 spellRange: SpellRange,
//                                 asPartOfWeaponAttack: Boolean,
//                                 description: String,
//                                 snippet: String,
//                                 concentration: Boolean,
//                                 ritual: Boolean,
//                                 rangeArea: String,
//                                 damageEffect: String,
//                                 components: List[Double],
//                                 componentsDescription: String,
//                                 saveDcAbilityId: Long,
//                                 healing: String,
//                                 healingDice: List[HealingDice],
//                                 tempHpDice: List[TempHpDice],
//                                 attackType: String,
//                                 canCastAtHigherLevel: Boolean,
//                                 isHomebrew: Boolean,
//                                 version: String,
//                                 sourceId: String,
//                                 sourcePageNumber: Double,
//                                 requiresSavingThrow: Boolean,
//                                 requiresAttackRoll: Boolean,
//                                 atHigherLevels: AtHigherLevels,
//                                 spellModifiers: List[SpellModifiers],
//                                 conditions: List[Conditions],
//                                 tags: List[String],
//                                 castingTimeDescription: String,
//                                 scaleType: String,
//                                 sources: List[Sources],
//                                 spellGroups: List[SpellGroups]
//                               )
//
//case class Spells(
//                   overrideSaveDc: String,
//                   limitedUse: String,
//                   id: Long,
//                   entityTypeId: Long,
//                   classSpellDefinition: ClassSpellDefinition,
//                   definitionId: Long,
//                   prepared: Boolean,
//                   countsAsKnownSpell: Boolean,
//                   usesSpellSlot: Boolean,
//                   castAtLevel: String,
//                   alwaysPrepared: Boolean,
//                   restriction: String,
//                   spellCastingAbilityId: String,
//                   displayAsAttack: String,
//                   additionalDescription: String,
//                   castOnlyAsRitual: Boolean,
//                   ritualCastingType: String,
//                   spellRange: SpellRange,
//                   activation: Activation,
//                   baseLevelAtWill: Boolean,
//                   atWillLimitedUseLevel: String,
//                   isSignatureSpell: String,
//                   componentId: Long,
//                   componentTypeId: Long,
//                   spellListId: String
//                 )
//
//case class ClassSpells(
//                        entityTypeId: Long,
//                        characterClassId: Long,
//                        spells: List[Spells]
//                      )
//
//case class CustomItems(
//                        id: Long,
//                        name: String,
//                        description: String,
//                        weight: String,
//                        cost: String,
//                        quantity: Double,
//                        notes: String
//                      )
//
//case class Characters(
//                       userId: Long,
//                       username: String,
//                       characterId: Long,
//                       characterName: String,
//                       characterUrl: URL,
//                       avatarUrl: URL,
//                       privacyType: Double,
//                       campaignId: String,
//                       isAssigned: Boolean
//                     )
//
//case class CharacterCampaign(
//                              id: Long,
//                              name: String,
//                              description: String,
//                              link: String,
//                              publicNotes: String,
//                              dmUserId: Long,
//                              dmUsername: String,
//                              characters: List[Characters]
//                            )
//
//case class OptionalClassFeatures(
//                                  classFeatureId: Long,
//                                  affectedClassFeatureId: String,
//                                  classFeatureDefinitionKey: String,
//                                  affectedClassFeatureDefinitionKey: String
//                                )
//
//case class PlayerCharacterInfo(
//                                id: Long,
//                                userId: Long,
//                                username: String,
//                                isAssignedToPlayer: Boolean,
//                                readonlyUrl: URL,
//                                decorations: Decorations,
//                                name: String,
//                                socialName: String,
//                                gender: String,
//                                faith: String,
//                                age: Double,
//                                hair: String,
//                                eyes: String,
//                                skin: String,
//                                height: String,
//                                weight: Double,
//                                inspiration: Boolean,
//                                baseHitPoints: Double,
//                                bonusHitPoints: String,
//                                overrideHitPoints: String,
//                                removedHitPoints: Double,
//                                temporaryHitPoints: Double,
//                                currentXp: Double,
//                                alignmentId: Long,
//                                lifestyleId: Long,
//                                abilities: List[Ability],
//                                background: Background,
//                                race: Race,
//                                raceDefinitionId: String,
//                                raceDefinitionTypeId: String,
//                                notes: Notes,
//                                traits: Traits,
//                                preferences: Preferences,
//                                configuration: Configuration,
//                                lifestyle: String,
//                                inventory: List[Inventory],
//                                currencies: Currencies,
//                                classes: List[Classes],
//                                feats: List[Feats],
//                                features: List[Features],
//                                //                                customDefenseAdjustments: List[CustomDefenseAdjustments],
//                                //                                customSenses: List[CustomSenses],
//                                //                                customSpeeds: List[CustomSpeeds],
//                                //                                customProficiencies: List[CustomProficiencies],
//                                //                                customActions: List[CustomActions],
//                                characterValues: List[CharacterValues],
//                                conditions: List[Conditions],
//                                deathSaves: DeathSaves,
//                                adjustmentXp: String,
//                                spellSlots: List[SpellSlots],
//                                pactMagic: List[SpellSlots],
//                                activeSourceCategories: List[Double],
//                                options: Options,
//                                choices: Choices,
//                                actions: Actions,
//                                modifiers: Modifiers,
//                                classSpells: List[ClassSpells],
//                                customItems: List[CustomItems],
//                                campaign: CharacterCampaign,
//                                creatures: List[Creatures],
//                                optionalOrigins: List[OptionalOrigins],
//                                optionalClassFeatures: List[OptionalClassFeatures],
//                                dateModified: String,
//                                providedFrom: String,
//                                canEdit: Boolean,
//                                status: Double,
//                                statusSlug: String,
//                                campaignSetting: String
//                              )
