/*
 *   project: BlueThoothLE
 *   programm: Home Light control (Bluethooth LE with HM-10)
 *   purpose:  control home lights via BT (color and brightness)
 *   Copyright (C) 2015  Dirk Marciniak
 *   file: DirectControlFragment.java
 *   last modified: 19.12.15 17:17
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

import android.app.Activity;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import de.dmarcini.bt.homelight.R;
import de.dmarcini.bt.homelight.interrfaces.IBtEventHandler;
import de.dmarcini.bt.homelight.interrfaces.IMainAppServices;
import de.dmarcini.bt.homelight.utils.BluetoothConfig;
import de.dmarcini.bt.homelight.utils.ProjectConst;


/**
 * Created by dmarc on 22.08.2015.
 */
public class DirectControlFragment extends Fragment implements IBtEventHandler
{
  private static final String TAG = DirectControlFragment.class.getSimpleName();
  private BluetoothConfig btConfig;
  private IMainAppServices mainService;
  private final short[] rgbw = {0,0,0,0};

  public DirectControlFragment()
  {
    Bundle args;
    int pos;

    try
    {
      args = getArguments();
      pos = args.getInt(ProjectConst.ARG_SECTION_NUMBER, 0);
      Log.v(TAG, String.format(Locale.ENGLISH, "Konstructor: id is %04d", pos));
    }
    catch( NullPointerException ex )
    {
      Log.e(TAG, "Konstructor: " + ex.getLocalizedMessage());
    }
  }

  /**
   * Returns a new instance of this fragment for the given section
   * number.
   */
  public static DirectControlFragment newInstance(int sectionNumber, BluetoothConfig btConfig)
  {
    DirectControlFragment fragment = new DirectControlFragment();
    fragment.setBlutethoothConfig(btConfig);
    Bundle args = new Bundle();
    args.putInt(ProjectConst.ARG_SECTION_NUMBER, sectionNumber);
    fragment.setArguments(args);
    Log.v(TAG, String.format(Locale.ENGLISH, "DirectControlFragment.newInstance(%04d)", sectionNumber));
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
    View rootView = inflater.inflate(R.layout.fragment_direct_control, container, false);
    setHasOptionsMenu(true);
    if( getActivity() instanceof IMainAppServices )
    {
      mainService = (IMainAppServices)getActivity();
    }
    else
    {
      mainService = null;
    }
    Log.v(TAG, "onCreateView...OK");
    return (rootView);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
  {
    Log.v(TAG, "onCreateOptionsMenu...");
    //inflater.inflate(R.menu.menu_home_light_main, menu);
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
  public void onPause()
  {
    super.onPause();
    Log.v(TAG, "onPause()");

  }

  @Override
  public void onResume()
  {
    super.onResume();
    Log.v(TAG, "onResume()");
  }

  @Override
  //public void onAttach(Activity activity)
  public void onAttach(Context ctx)
  {
    super.onAttach(ctx);
    Log.v(TAG, "onAttach()");
  }

  @Override
  public void onBTConnected()
  {
    Log.v(TAG, "BT Device connected!");
  }

  @Override
  public void onBTDisconnected()
  {
    Log.v(TAG, "BT Device disconnected!");
  }

  @Override
  public void onBTServicesRecived(List<BluetoothGattService> gattServices)
  {
    Log.v(TAG, "BT Device services recived");
  }

  @Override
  public void onBTDataAvaiable(String data)
  {
    Log.v(TAG, "BT Device data recived");
    if( Pattern.matches(ProjectConst.KOMANDPATTERN, data))
    {
      //
      // Kommando empfangen
      //
      //data = data.substring(1);
      //data = data.substring(0, data.indexOf(0x03));
      //data.replaceAll(ProjectConst.REPLACEPATTERN, "-");
      Log.v(TAG, String.format(Locale.ENGLISH, "command recived: <%s>", data));
    }
  }

  @Override
  public void onServiceConnected()
  {
    Log.v(TAG, "BT Service connected");
  }

  @Override
  public void onServiceDisconnected()
  {
    Log.v(TAG, "BT Service disconnected");
  }

  @Override
  public void onPageSelected()
  {
    Log.v(TAG,"Page DIRECTCONTROL was selected");
    if( btConfig.isConnected() && btConfig.getCharacteristicTX() != null && btConfig.getCharacteristicRX() != null )
    {
      //
      // Alles ist so wie es soll
      // mach eine Abfrage vom Modul und dann geht es weiter
      //
      Log.v(TAG, "BT Device is connected and ready....");
      mainService.askModulForRGBW();
    }
    else if( btConfig.isConnected() )
    {
      Log.v(TAG, "BT Device is connected....");
      if( btConfig.getModuleType() == null )
      {
        // Frage das Modul nach dem Typ, wenn noch nicht geschehen
        // sollte nach dem connect passieren
        mainService.askModulForType();
      }
    }
  }


}
