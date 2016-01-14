/******************************************************************************
 * *
 * project: ANDROID                                                      *
 * module: btlehomelight                                                 *
 * class: SystemPrefActivity                                             *
 * date: 2016-01-10                                                      *
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

package de.dmarcini.bt.homelight;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * Eine Activity zum Einstellen der Systemparameter
 */
public class SystemPrefActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
  private static final String TAG = SystemPrefActivity.class.getSimpleName();
  private boolean hasChanged;


  @Override
  @SuppressWarnings( "deprecation" )
  protected void onCreate(Bundle args)
  {
    super.onCreate(args);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreate...");
    }
    // Load the preferences from an XML resource
    addPreferencesFromResource(R.xml.system_prop);
    hasChanged = false;
  }

  @Override
  public void onPause()
  {
    Intent intent;
    super.onPause();
    Log.v(TAG, "onPause()...");
  }


  @Override
  @SuppressWarnings( "deprecation" )
  public void onResume()
  {
    super.onResume();
    Log.v(TAG, "onResume()...");
    //
    // setze Listener, der überwacht, wenn Preferenzen geändert wurden
    //
    getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
  }

  /**
   * Called when a shared preference is changed, added, or removed. This
   * may be called even if a preference is set to its existing value.
   * <p/>
   * <p>This callback will be run on your main thread.
   *
   * @param sharedPreferences The {@link SharedPreferences} that received
   *                          the change.
   * @param key               The key of the preference that was changed, added, or
   */
  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
  {
    SharedPreferences sPref;

    Log.v(TAG, "preference <" + key + "> changed!");
    hasChanged = true;
    //    sPref = PreferenceManager.getDefaultSharedPreferences(this);
    //    SharedPreferences.Editor editor = pref.edit();
    //    editor.putString(ProjectConst.KEY_LAST_BT_DEVICE, btConfig.getDeviceAddress());
    //    editor.putString(ProjectConst.KEY_LAST_BT_NAME, btConfig.getDeviceName());
    //    editor.apply();

  }
}
