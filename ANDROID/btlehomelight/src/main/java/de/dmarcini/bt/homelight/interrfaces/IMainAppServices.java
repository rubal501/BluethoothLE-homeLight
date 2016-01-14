/******************************************************************************
 *                                                                            *
 *      project: ANDROID                                                      *
 *      module: btlehomelight                                                 *
 *      class: IMainAppServices                                               *
 *      date: 2016-01-15                                                      *
 *                                                                            *
 *      Copyright (C) 2016  Dirk Marciniak                                    *
 *                                                                            *
 *      This program is free software: you can redistribute it and/or modify  *
 *      it under the terms of the GNU General Public License as published by  *
 *      the Free Software Foundation, either version 3 of the License, or     *
 *      (at your option) any later version.                                   *
 *                                                                            *
 *      This program is distributed in the hope that it will be useful,       *
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *      GNU General Public License for more details.                          *
 *                                                                            *
 *      You should have received a copy of the GNU General Public License     *
 *      along with this program.  If not, see <http://www.gnu.org/licenses/   *
 *                                                                            *
 ******************************************************************************/

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
  void askModulForType();

  /**
   * Fragt das Modul nach seinem Namen
   */
  void askModulForName();

  /**
   * Frage das Modul nach der aktuellen RGBW Einstellung (Roh)
   */
  void askModulForRGBW();

  /**
   * Setze im Modul die neuen Einstellungen für LED (ROH/direkt)
   *
   * @param rgbw Array für RGBW
   */
  void setModulRawRGBW(short[] rgbw);

  /**
   * Setze Farben als RGB, Modul kalibriert nach RGBW
   *
   * @param rgbw RGB Werte, White wird ignoriert
   */
  void setModulRGB4Calibrate(short[] rgbw);

  /**
   * Schalte die LED AUS/AN
   */
  void setModulOnOff();

  /**
   * Gib, wenn vorhanden, Farben vom Modul zurück
   *
   * @return Array mit RGBW Werten des Modules
   */
  short[] getModulRGBW();

  void setModuleName(String newName);
}
