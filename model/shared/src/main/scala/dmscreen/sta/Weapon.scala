/*
 * Copyright (c) 2024 Roberto Leibman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dmscreen.sta

enum WeaponRange {

  case Close, Medium, Long, Melee

}

enum EnergyType {

  case AntiProton, Disruptor, ElectroMagneticIonic, FreeElectron, Graviton,
    PhasedPolaron, Phaser, PhasePulse, Proton, Tetryon, Other

}

enum TorpedoLoadType {

  case Chroniton, Gravimetric, Neutronic, Nuclear, Photon, Photonic, Plasma, Polaron,
    Positron, Quantum, Spatial, Tetryonic, Transphasic, Tricobolt

}

enum CaptureType {

  case Tractor, Grappler

}

case class WeaponQualities(
  area:         Boolean,
  intense:      Boolean,
  knockdown:    Boolean,
  accurate:     Boolean,
  charge:       Boolean,
  cumbersome:   Boolean,
  deadly:       Boolean,
  debilitating: Boolean,
  grenade:      Boolean,
  inaccurate:   Boolean,
  nonlethal:    Boolean,
  hidden:       Int,
  piercing:     Int,
  vicious:      Int,
  calibration:  Boolean = false,
  dampening:    Boolean = false,
  depleting:    Boolean = false,
  devastating:  Boolean = false,
  highYield:    Boolean = false,
  jamming:      Boolean = false,
  nonLethal:    Boolean = false,
  persistentX:  Boolean = false,
  slowing:      Boolean = false,
  versatile:    Boolean = false,
  spread:       Boolean = false
)

sealed trait Weapon {

  def name:      String
  def qualities: WeaponQualities

}

case class MeleeWeapon(
  override val name:      String,
  override val qualities: WeaponQualities
) extends Weapon

case class EnergyWeapon(
  override val name:      String,
  range:                  WeaponRange,
  energyType:             EnergyType,
  override val qualities: WeaponQualities
) extends Weapon

case class TorpedoWeapon(
  override val name:      String,
  range:                  WeaponRange,
  loadType:               TorpedoLoadType,
  override val qualities: WeaponQualities
) extends Weapon

case class MineWeapon(
  override val name:      String,
  range:                  WeaponRange,
  override val qualities: WeaponQualities
) extends Weapon

case class CaptureWeapon(
  override val name:      String,
  range:                  WeaponRange,
  captureType:            CaptureType,
  override val qualities: WeaponQualities
) extends Weapon

case class CharacterWeapon(
  override val name:      String,
  description:            String = "",
  damage:                 Int = 1,
  range:                  WeaponRange = WeaponRange.Melee,
  hands:                  Int = 1,
  override val qualities: WeaponQualities
) extends Weapon
