package de.dmarcini.bt.btlehomelight.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.design.widget.NavigationView;
import android.util.Log;

import de.dmarcini.bt.btlehomelight.BuildConfig;
import de.dmarcini.bt.btlehomelight.R;
import de.dmarcini.bt.btlehomelight.utils.HomeLightSysConfig;

/**
 * Created by dmarc on 08.03.2016.
 */
public class SystemPreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
  private static final String TAG = SystemPreferencesFragment.class.getSimpleName();

  //
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreate...");
    }
    // Load the preferences from an XML resource
    addPreferencesFromResource(R.xml.system_prop);
  }

  @Override
  public void onPause()
  {
    super.onPause();
    Log.v(TAG, "onPause()...");
    //
    // lösche Listener, der überwacht, wenn Preferenzen geändert wurden
    //
    getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
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
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "preference <" + key + "> changed!");
    }

    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "sys prefs new reading...");
    }
    HomeLightSysConfig.readSysPrefs(getResources(), sharedPreferences);
    NavigationView navigationView = ( NavigationView ) getActivity().findViewById(R.id.nav_view);
    //
    // Anzeigen oder verstecken der Menüpunkte nach Systemeinstellungen
    //
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "set menu items....");
    }
    navigationView.getMenu().findItem(R.id.navColorCircle).setVisible(HomeLightSysConfig.isShowColorWheel());
    navigationView.getMenu().findItem(R.id.navColorBrightness).setVisible(HomeLightSysConfig.isShowBrightnessOnly());
    navigationView.getMenu().findItem(R.id.navColorEqualizer).setVisible(HomeLightSysConfig.isShowEqualizer());
    navigationView.getMenu().findItem(R.id.navColorPresets).setVisible(HomeLightSysConfig.isShowColorPresets());
    navigationView.invalidate();
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "set menu items....OK");
    }


  }
}
