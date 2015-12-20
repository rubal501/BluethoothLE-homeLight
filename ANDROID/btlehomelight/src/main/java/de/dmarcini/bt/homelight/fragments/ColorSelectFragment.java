/*
 *   project: BlueThoothLE
 *   programm: Home Light control (Bluethooth LE with HM-10)
 *   purpose:  control home lights via BT (color and brightness)
 *   Copyright (C) 2015  Dirk Marciniak
 *   file: ColorSelectFragment.java
 *   last modified: 19.12.15 20:01
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

package de.dmarcini.bt.homelight.fragments;

import android.bluetooth.BluetoothGattService;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;

import java.util.List;
import java.util.Locale;

import de.dmarcini.bt.homelight.interrfaces.IBtEventHandler;
import de.dmarcini.bt.homelight.R;
import de.dmarcini.bt.homelight.utils.BluetoothConfig;
import de.dmarcini.bt.homelight.utils.ProjectConst;


/**
 * Created by dmarc on 22.08.2015.
 */
public class ColorSelectFragment extends Fragment implements IBtEventHandler, ColorPicker.OnColorChangedListener
{
  private static final String TAG = ColorSelectFragment.class.getSimpleName();
  private ColorPicker   picker;
  private SVBar         svBar;
  private BluetoothConfig btConfig;

  public ColorSelectFragment()
  {
  }

  /**
   * Returns a new instance of this fragment for the given section
   * number.
   */
  public static ColorSelectFragment newInstance(int sectionNumber, BluetoothConfig btConfig)
  {
    ColorSelectFragment fragment = new ColorSelectFragment();
    fragment.setBlutethoothConfig(btConfig);
    Bundle args = new Bundle();
    args.putInt(ProjectConst.ARG_SECTION_NUMBER, sectionNumber);
    fragment.setArguments(args);
    Log.v(TAG, String.format(Locale.ENGLISH, "ColorSelectFragment.newInstance(%04d)", sectionNumber));
    return fragment;
  }

  private void setBlutethoothConfig(BluetoothConfig btConfig)
  {
    this.btConfig = btConfig;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    Log.v(TAG, "onCreateView...");
    //getResources().getConfiguration().orientation
    View rootView = inflater.inflate(R.layout.fragment_colors_wheel, container, false);
    picker = ( ColorPicker ) rootView.findViewById(R.id.picker);
    svBar = ( SVBar ) rootView.findViewById(R.id.svbar);
    picker.addSVBar(svBar);
    //To get the color
    //picker.getColor();
    picker.setOldCenterColor(picker.getColor());
    //To set the old selected color u can do it like this
    picker.setOldCenterColor(picker.getColor());
    // adds listener to the colorpicker which is implemented
    //in the activity
    picker.setOnColorChangedListener(this);
    //to turn of showing the old color
    picker.setShowOldCenterColor(false);

    setHasOptionsMenu(true);
    Log.v(TAG, "onCreateView...OK");
    return (rootView);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
  {
    Log.v(TAG, "onCreateOptionsMenu...");
    inflater.inflate(R.menu.menu_home_light_main, menu);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu)
  {
    super.onPrepareOptionsMenu(menu);
    // noch was vorbereiten?
    Log.v(TAG, "onPrepareOptionsMenu...");
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    Log.e(TAG, "onOptionsItemSelected...");
    switch( item.getItemId() )
    {
      case R.id.menu_scan:
        //mLeDeviceListAdapter.clear();
        //scanLeDevice(true);
        break;
      case R.id.menu_stop:
        //scanLeDevice(false);
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onColorChanged(int color)
  {
    Log.i(TAG, String.format(Locale.ENGLISH, "color changed to %08X", color));
  }

  @Override
  public void onBTConnected()
  {

  }

  @Override
  public void onBTDisconnected()
  {

  }

  @Override
  public void onBTServicesRecived(List<BluetoothGattService> gattServices)
  {

  }

  @Override
  public void onBTDataAvaiable(String data)
  {

  }

  @Override
  public void onServiceConnected()
  {

  }

  @Override
  public void onServiceDisconnected()
  {

  }

  @Override
  public void onPageSelected()
  {
    Log.v(TAG,"Page COLORSELECT (Weehl) was selected");
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig)
  {
    super.onConfigurationChanged(newConfig);

    // Checks the orientation of the screen
    if( newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE )
    {
      Log.v(TAG, "onConfigurationChanged: landscape...");
      Toast.makeText(getActivity(), "landscape", Toast.LENGTH_SHORT).show();
    }
    else if( newConfig.orientation == Configuration.ORIENTATION_PORTRAIT )
    {
      Log.v(TAG, "onConfigurationChanged: portrait...");
      Toast.makeText(getActivity(), "portrait", Toast.LENGTH_SHORT).show();
    }
  }

}
