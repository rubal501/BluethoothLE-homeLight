/******************************************************************************
 *                                                                            *
 *      project: ANDROID                                                      *
 *      module: btlehomelight                                                 *
 *      class: PredefColorFragment                                            *
 *      date: 2016-01-05                                                      *
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

package de.dmarcini.bt.homelight.fragments;

import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Locale;

import de.dmarcini.bt.homelight.BuildConfig;
import de.dmarcini.bt.homelight.R;
import de.dmarcini.bt.homelight.utils.BluetoothModulConfig;
import de.dmarcini.bt.homelight.utils.ProjectConst;


/**
 * Created by dmarc on 22.08.2015.
 */
public class PredefColorFragment extends AppFragment
{
  private static final String TAG = PredefColorFragment.class.getSimpleName();

  public PredefColorFragment()
  {
  }

  /**
   * Returns a new instance of this fragment for the given section
   * number.
   */
  public static PredefColorFragment newInstance(int sectionNumber, BluetoothModulConfig btConfig)
  {
    PredefColorFragment fragment = new PredefColorFragment();
    fragment.setBlutethoothConfig(btConfig);
    Bundle args = new Bundle();
    args.putInt(ProjectConst.ARG_SECTION_NUMBER, sectionNumber);
    fragment.setArguments(args);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "%s.newInstance(%04d)", TAG, sectionNumber));
    }
    return fragment;
  }

  private void setBlutethoothConfig(BluetoothModulConfig btConfig)
  {
    this.btConfig = btConfig;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View rootView = inflater.inflate(R.layout.fragment_dummy, container, false);
    setHasOptionsMenu(true);
    return (rootView);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreateOptionsMenu...");
    }
    //inflater.inflate(R.menu.menu_home_light_main, menu);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu)
  {
    super.onPrepareOptionsMenu(menu);
    // noch was vorbereiten?
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onPrepareOptionsMenu...");
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    if( BuildConfig.DEBUG )
    {
      Log.e(TAG, "onOptionsItemSelected...");
    }
    return super.onOptionsItemSelected(item);
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
  public void onBTDataAvaiable(String[] data)
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
    Log.d(TAG, "Page DUMMY was selected");
  }

}
