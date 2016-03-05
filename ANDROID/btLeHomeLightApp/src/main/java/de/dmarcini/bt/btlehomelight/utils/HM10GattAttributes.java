/******************************************************************************
 * *
 * project: ANDROID                                                      *
 * module: btlehomelight                                                 *
 * class: HM10GattAttributes                                             *
 * date: 2016-01-03                                                      *
 * *
 * Copyright (C) 2016  Dirk Marciniak                                    *
 * *
 * This program is free software: you can redistribute it and/or modify  *
 * it under the terms of the GNU General Public License as published by  *
 * the Free Software Foundation, either version 3 of the License, or     *
 * (at your option) any later version.                                   *
 * *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU General Public License for more details.                          *
 * *
 * You should have received a copy of the GNU General Public License     *
 * along with this program.  If not, see <http://www.gnu.org/licenses/   *
 * *
 ******************************************************************************/

package de.dmarcini.bt.btlehomelight.utils;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class HM10GattAttributes
{
  public static String UUID_CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
  public static String UUID_MANUFACTURER_NAME            = "00002a29-0000-1000-8000-00805f9b34fb";
  public static String UUID_HM_10_CONF                   = "0000ffe1-0000-1000-8000-00805f9b34fb";
  public static String UUID_HM_RX_TX                     = "0000ffe0-0000-1000-8000-00805f9b34fb";
  public static String UUID_DEVICE_INFO                  = "00001801-0000-1000-8000-00805f9b34fb";
  public static String UUID_GENERIC_ACCESS               = "00001800-0000-1000-8000-00805f9b34fb";
  public static UUID   HM_RXTX_UUID                      = UUID.fromString(UUID_HM_RX_TX);
  public static UUID   HM_10_CONF_UUID                   = UUID.fromString(UUID_HM_10_CONF);
  private static HashMap<String, String> attributes                   = new HashMap();

  static
  {
    attributes.put(UUID_CLIENT_CHARACTERISTIC_CONFIG, "client characteristic config");
    attributes.put(UUID_MANUFACTURER_NAME, "manufacturer name string");
    attributes.put(UUID_HM_10_CONF, "config data");
    attributes.put(UUID_HM_RX_TX, "RX/TX data");
    attributes.put(UUID_DEVICE_INFO, "device information service");
    attributes.put(UUID_GENERIC_ACCESS, "generic access");
  }


  public static String lookup(String uuid, String defaultName)
  {
    String name = attributes.get(uuid);
    return name == null ? defaultName : name;
  }
}
