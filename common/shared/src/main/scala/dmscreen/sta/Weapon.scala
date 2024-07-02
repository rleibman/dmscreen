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

  case Close, Medium, Long

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
  area:         Int,
  calibration:  Int,
  charge:       Int,
  dampening:    Int,
  deadly:       Int,
  depleting:    Int,
  devastating:  Int,
  hidden:       Int,
  highYield:    Int,
  intense:      Int,
  jamming:      Int,
  knockdown:    Int,
  nonLethal:    Int,
  persistentX:  Int,
  piercing:     Int,
  slowing:      Int,
  versatile:    Int,
  vicious:      Int,
  debilitating: Int,
  accurate:     Int,
  areaOrSpread: Int,
  spread:       Int
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
