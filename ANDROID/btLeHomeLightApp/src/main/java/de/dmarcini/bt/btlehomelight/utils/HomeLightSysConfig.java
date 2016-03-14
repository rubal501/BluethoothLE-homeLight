/******************************************************************************
 * *
 * project: ANDROID                                                      *
 * module: btlehomelight                                                 *
 * class: HomeLightSysConfig                                             *
 * date: 2016-01-15                                                      *
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

import android.content.SharedPreferences;
import android.content.res.Resources;

import de.dmarcini.bt.btlehomelight.R;

/**
 * Objekt zur Sicherung der App-Konfiguration, statisch, global
 */
public final class HomeLightSysConfig
{
  private static boolean autoReconnect           = false;
  private static boolean showDiscovering         = true;
  private static boolean showDirectControl       = true;
  private static boolean showColorWheel          = true;
  private static boolean showBrightnessOnly      = true;
  private static boolean showPredefColors        = true;
  private static boolean diableBTonEXIT          = false;
  private static boolean isAppDebugging          = false;
  private static String  lastConnectedDeviceAddr = null;
  private static String  lastConnectedDeviceName = null;

  public static boolean isDiableBTonEXIT()
  {
    return diableBTonEXIT;
  }

  public static boolean isAutoReconnect()
  {
    return autoReconnect;
  }

  public static boolean isShowDiscovering()
  {
    return showDiscovering;
  }

  public static boolean isShowColorWheel()
  {
    return showColorWheel;
  }

  public static boolean isShowColorPresets()
  {
    return showPredefColors;
  }

  public static boolean isShowBrightnessOnly()
  {
    return showBrightnessOnly;
  }

  /**
   * Gib den Namen des zuletzt verbundenen Gerätes zurück
   *
   * @return der Name
   */
  public static String getLastConnectedDeviceName()
  {
    return lastConnectedDeviceName;
  }

  /**
   * Gib das zuletzt verbundene Gerät zurück
   *
   * @return Das Gerät
   */
  public static String getLastConnectedDeviceAddr()
  {
    return lastConnectedDeviceAddr;
  }

  /**
   * Setzte im Konfig-Objekt UND in den Preferenzen das zuletzt verbindene Gerät
   *
   * @param res                     System Resourcen
   * @param pref                    System Preferenzen
   * @param lastConnectedDeviceAddr Das neue Gerät
   */
  public static void setLastConnectedDeviceAddr(Resources res, SharedPreferences pref, String lastConnectedDeviceAddr)
  {
    SharedPreferences.Editor ed = pref.edit();
    ed.putString(res.getString(R.string.pref_sys_lastConnectedDeviceAddr), lastConnectedDeviceAddr);
    ed.commit();
    HomeLightSysConfig.lastConnectedDeviceAddr = lastConnectedDeviceAddr;
  }

  /**
   * Soll das Fragment zur Direkten Kontrolle gezeigt werden?
   *
   * @return Ja oder nein
   */
  public static boolean isShowEqualizer()
  {
    return showDirectControl;
  }


  /**
   * Sollen die App im debug-Status sein ?
   *
   * @return ja oder nein
   */
  public static boolean isAppDebugging()
  {
    return isAppDebugging;
  }

  /**
   * lese die Programmeinstellungen aus den übergebenen Einstellungen aus
   *
   * @param res  Resourcen der App
   * @param pref die ausgewählten shared prefrences
   */
  public static void readSysPrefs(Resources res, SharedPreferences pref)
  {
    //
    // Anzeige der Seiten
    //
    showDiscovering = pref.getBoolean(res.getString(R.string.pref_sys_showpage_discovering), true);
    showDirectControl = pref.getBoolean(res.getString(R.string.pref_sys_showpage_direct), true);
    showColorWheel = pref.getBoolean(res.getString(R.string.pref_sys_showpage_colorwheel), true);
    showBrightnessOnly = pref.getBoolean(res.getString(R.string.pref_sys_showpage_brightness), true);
    showPredefColors = pref.getBoolean(res.getString(R.string.pref_sys_showpage_predefcolors), true);
    diableBTonEXIT = pref.getBoolean(res.getString(R.string.pref_sys_disableBTonEXIT), false );
    //
    autoReconnect = pref.getBoolean(res.getString(R.string.pref_sys_autoReconnect), false);
    isAppDebugging = pref.getBoolean(res.getString(R.string.pref_sys_debugging_stat), true);
    lastConnectedDeviceAddr = pref.getString(res.getString(R.string.pref_sys_lastConnectedDeviceAddr), null);
    lastConnectedDeviceName = pref.getString(res.getString(R.string.pref_sys_lastConnectedDeviceName), null);
  }

}
