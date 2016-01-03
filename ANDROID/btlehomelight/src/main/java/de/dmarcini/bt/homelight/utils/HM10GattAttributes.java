/******************************************************************************
 *                                                                            *
 *      project: ANDROID                                                      *
 *      module: btlehomelight                                                 *
 *      class: HM10GattAttributes                                             *
 *      date: 2016-01-03                                                      *
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

package de.dmarcini.bt.homelight.utils;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class HM10GattAttributes
{
  public static  String                  CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
  public static  String                  HM_10_CONF                   = "0000ffe0-0000-1000-8000-00805f9b34fb";
  public static  String                  HM_RX_TX                     = "0000ffe1-0000-1000-8000-00805f9b34fb";
  private static HashMap<String, String> attributes                   = new HashMap();

  static
  {
    // Sample Services.
    attributes.put("0000ffe0-0000-1000-8000-00805f9b34fb", "HM 10 Serial");
    attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Device Information Service");
    // Sample Characteristics.
    attributes.put(HM_RX_TX, "RX/TX data");
    attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
  }

  public static String lookup(String uuid, String defaultName)
  {
    String name = attributes.get(uuid);
    return name == null ? defaultName : name;
  }
}
