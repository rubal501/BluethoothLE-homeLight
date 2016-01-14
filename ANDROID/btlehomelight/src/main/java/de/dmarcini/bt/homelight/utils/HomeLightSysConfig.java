/******************************************************************************
 *                                                                            *
 *      project: ANDROID                                                      *
 *      module: btlehomelight                                                 *
 *      class: HomeLightSysConfig                                             *
 *      date: 2016-01-14                                                      *
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

import android.content.SharedPreferences;
import android.content.res.Resources;

import de.dmarcini.bt.homelight.R;

/**
 * Objekt zur Sicherung der App-Konfiguration, statisch, global
 */
public final class HomeLightSysConfig
{
  private static boolean showDirectControl = true;
  private static boolean jumpToDefaultPageOnConnect = true;
  private static boolean isAppDebugging = false;

  /**
   * lese die Programmeinstellungen aus den übergebenen Einstellungen aus
   *
   * @param res Resourcen der App
   * @param pref die ausgewählten shared prefrences
   */
   public static void readSysPrefs( Resources res, SharedPreferences pref )
   {
     showDirectControl = pref.getBoolean(res.getString(R.string.pref_sys_showpage_direct), true);
     jumpToDefaultPageOnConnect = pref.getBoolean( res.getString(R.string.pref_sys_jump_to_page_on_connect), true );
     isAppDebugging = pref.getBoolean( res.getString(R.string.pref_sys_debugging_stat), true );
   }
}
