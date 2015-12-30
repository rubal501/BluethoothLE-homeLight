/*
 *   project: BlueThoothLE
 *   programm: Home Light control (Bluethooth LE with HM-10)
 *   purpose:  control home lights via BT (color and brightness)
 *   Copyright (C) 2015  Dirk Marciniak
 *   file: IMainAppServices.java
 *   last modified: 20.12.15 18:54
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/
 *
 */

package de.dmarcini.bt.homelight.interrfaces;

/**
 * Die Main Activity soll dieses Interface implementieren, dann kann man standartisiert auf Services zugreifen
 */
public interface IMainAppServices
{
  /**
   * Funktion fordert die App auf, das Fragment mit der übergebenen Nummer zu aktivieren
   *
   * @param position Nummer des Fragments (Positionsnummer aus den Konstanten)
   */
  void switchToFragment(int position);

  /**
   * Frage (noch einmal) nach dem Modultyp
   */
  void askModulForType( );

  /**
   * Fragt das Modul nach seinem Namen
   */
  void askModulForName();

  /**
   * Frage das Modul nach der aktuellen RGBW Einstellung (Roh)
   */
  public void askModulForRawRGBW();

  /**
   * Frage das Modul nach der aktuellen RGBW Einstellung (Kalibriert)
   */
  public void askModulForCalibratedRGBW();

  /**
   * Setze im Modul die neuen Einstellungen für LED (ROH/direkt)
   *
   * @param rgbw Array für RGBW
   */
  void setModulRawRGBW(short[] rgbw);

  /**
   * Setze Modul RGBW unkalibriert (wird nur gespeichert als Basis für kalibrierte Daten )
   *
   * @param rgbw
   */
  void setModulRAWRGBWStore(short[] rgbw);

  /**
   * Setze Modul RGBW kalibriert (wird angezeigt)
   * @param rgbw
   */
  void setModulCalibredRGBW( short[] rgbw );

  /**
   * Schalte die LED AUS/AN
   */
  void setModulOnOff();
}
