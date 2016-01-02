/*
 * //@formatter:off
 *
 *     ANDROID
 *     btlehomelight
 *     IBtEventHandler
 *     2016-01-02
 *     Copyright (C) 2016  Dirk Marciniak
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/
 * /
 * //@formatter:on
 */

package de.dmarcini.bt.homelight.interrfaces;

import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * Fragmente müssen dieses Interface implementieren, damit die Kommunikation
 * mit dem Bluethooth Device klappt
 */
public interface IBtEventHandler
{
  /**
   * Das BT Gerät wurde verbunden
   */
  void onBTConnected(/*TODO: Gerät mit übergeben?*/);

  /**
   * Das BT Gerät wurde getrennt
   */
  void onBTDisconnected();

  /**
   * Das BT Gerät meldet verfügbarte Services
   *
   * @param gattServices
   */
  void onBTServicesRecived(List<BluetoothGattService> gattServices);

  /**
   * Es kommen Daten vom BT Gerät
   *
   * @param data
   */
  void onBTDataAvaiable(String data);


  /**
   * Bluethooth Hintergrundservice verbunden!
   */
  void onServiceConnected();

  /**
   * Bluethooth Hintergrundservice getrennt
   */
  void onServiceDisconnected();

  /**
   * Page wurde selektiert
   */
  void onPageSelected();
}
