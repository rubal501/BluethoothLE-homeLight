/******************************************************************************
 * *
 * project: ANDROID                                                      *
 * module: btlehomelight                                                 *
 * class: DirectControlFragment                                          *
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

package de.dmarcini.bt.homelight.fragments;

import android.bluetooth.BluetoothGattService;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ValueBar;

import java.util.List;
import java.util.Locale;

import de.dmarcini.bt.homelight.BuildConfig;
import de.dmarcini.bt.homelight.R;
import de.dmarcini.bt.homelight.interrfaces.IMainAppServices;
import de.dmarcini.bt.homelight.utils.BluetoothModulConfig;
import de.dmarcini.bt.homelight.utils.ProjectConst;


/**
 * Created by dmarc on 22.08.2015.
 */
public class DirectControlFragment extends AppFragment
{
  private static final String TAG = DirectControlFragment.class.getSimpleName();
  private TextView deviceAddress;
  private TextView connectionState;
  private TextView isSerial;
  private ValueBar seekRed;
  private ValueBar seekGreen;
  private ValueBar seekBlue;
  private ValueBar seekWhite;

  public DirectControlFragment()
  {
    Bundle args;
    int    pos;

    try
    {
      args = getArguments();
      if( args != null )
      {
        pos = args.getInt(ProjectConst.ARG_SECTION_NUMBER, 0);
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, String.format(Locale.ENGLISH, "Konstructor: id is %04d", pos));
        }
      }
    }
    catch( NullPointerException ex )
    {
      if( BuildConfig.DEBUG )
      {
        Log.e(TAG, "Konstructor: " + ex.getLocalizedMessage());
      }
    }
  }

  /**
   * Returns a new instance of this fragment for the given section
   * number.
   */
  public static DirectControlFragment newInstance(int sectionNumber, BluetoothModulConfig btConfig)
  {
    DirectControlFragment fragment = new DirectControlFragment();
    fragment.setBlutethoothConfig(btConfig);
    Bundle args = new Bundle();
    args.putInt(ProjectConst.ARG_SECTION_NUMBER, sectionNumber);
    fragment.setArguments(args);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "DirectControlFragment.newInstance(%04d)", sectionNumber));
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
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreateView...");
    }
    View rootView = inflater.inflate(R.layout.fragment_direct_control, container, false);
    setHasOptionsMenu(true);
    if( getActivity() instanceof IMainAppServices )
    {
      mainService = ( IMainAppServices ) getActivity();
    }
    else
    {
      mainService = null;
    }
    //
    // Adressen der GUI Objekte bestimmen
    //
    deviceAddress = ( TextView ) rootView.findViewById(R.id.deviceAddress);
    connectionState = ( TextView ) rootView.findViewById(R.id.connectionState);
    isSerial = ( TextView ) rootView.findViewById(R.id.isSerial);
    seekRed = ( ValueBar ) rootView.findViewById(R.id.redValueBar);
    seekGreen = ( ValueBar ) rootView.findViewById(R.id.greenValueBar);
    seekBlue = ( ValueBar ) rootView.findViewById(R.id.blueValueBar);
    seekWhite = ( ValueBar ) rootView.findViewById(R.id.whiteValueBar);
    //
    // Grundfarben einstellen
    //
    seekRed.setColor(Color.RED);
    seekGreen.setColor(Color.GREEN);
    seekBlue.setColor(Color.BLUE);
    seekWhite.setColor(Color.WHITE);
    //
    // On Change Listener setzen
    //
    seekRed.setOnValueChangedListener(new ValueBar.OnValueChangedListener()
    {
      @Override
      public void onValueChanged(int value)
      {
        rgbw[ 0 ] = ( short ) ((value >> 16) & 0xff);
        Log.i(TAG, String.format(Locale.ENGLISH, "value RED changed to %02X...", rgbw[0]));
        onProgressChanged();
      }
    });

    seekGreen.setOnValueChangedListener(new ValueBar.OnValueChangedListener()
    {
      @Override
      public void onValueChanged(int value)
      {
        rgbw[ 1 ] = ( short ) ((value >> 8) & 0xff);
        Log.i(TAG, String.format(Locale.ENGLISH, "value GREEN changed to %02X...", rgbw[1]));
        onProgressChanged();
      }
    });
    seekBlue.setOnValueChangedListener(new ValueBar.OnValueChangedListener()
    {
      @Override
      public void onValueChanged(int value)
      {
        rgbw[ 2 ] = ( short ) (value & 0xff);
        Log.i(TAG, String.format(Locale.ENGLISH, "value BLUE changed to %02X...", rgbw[2]));
        onProgressChanged();
      }
    });
    seekWhite.setOnValueChangedListener(new ValueBar.OnValueChangedListener()
    {
      @Override
      public void onValueChanged(int value)
      {
        rgbw[ 3 ] = ( short ) (value & 0xff);
        Log.i(TAG, String.format(Locale.ENGLISH, "value WHITE changed to %02X...", rgbw[3]));
        onProgressChanged();
      }
    });
    //
    // nicht verbunden einstellen
    //
    onServiceDisconnected();
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreateView...OK");
    }
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
      Log.v(TAG, "onOptionsItemSelected...");
    }
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
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onPause()");
    }

  }

  @Override
  public void onResume()
  {
    super.onResume();
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onResume()");
    }
  }

  @Override
  public void onBTConnected()
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "BT Device connected!");
    }
    onServiceConnected();
  }

  @Override
  public void onBTDisconnected()
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "BT Device disconnected!");
    }
    onServiceDisconnected();
  }

  @Override
  public void onBTServicesRecived(List<BluetoothGattService> gattServices)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "BT Device services recived");
    }
  }

  @Override
  public void onBTDataAvaiable(String[] param)
  {
    int cmdNum;

    if( param.length > 0 )
    {
      //
      // Hier mal das Kommando finden und umrechnen
      //
      try
      {
        cmdNum = Integer.parseInt(param[ 0 ], 16);
      }
      catch( NumberFormatException ex )
      {
        cmdNum = ProjectConst.C_UNKNOWN;
      }
      //
      // Jetzt Kommando auswerten
      //
      switch( cmdNum )
      {
        //
        // Unbekanntes Kommando
        //
        case ProjectConst.C_UNKNOWN:
        default:
          if( BuildConfig.DEBUG )
          {
            Log.v(TAG, "unhandled command recived! Ignored.");
          }
          break;

        //
        // Frage nach RGBW
        //
        case ProjectConst.C_ASKRGBW:
          final String[] pm = param;
          //
          // Das läßt sich nur von diesem Thread aus machen, daher dieses
          //
          seekRed.post(new Runnable()
          {
            public void run()
            {
              setSeekBars(pm);
            }
          });
          break;

      }
    }
    else
    {
      Log.e(TAG, "wrong command string recived! Ignored.");
    }
  }

  private void setSeekBars(String[] param)
  {
    if( param.length < ProjectConst.C_ASKRGB_LEN )
    {
      Log.w(TAG, "setSeekBars() -> param array to short! IGNORED!");
      return;
    }
    //
    // der erste Parameter ist das Kommando, den ignoriere ich mal
    //
    for( int i = 1; i < ProjectConst.C_ASKRGB_LEN; i++ )
    {
      try
      {
        rgbw[ i - 1 ] = Short.parseShort(param[ i ], 16);
      }
      catch( NumberFormatException ex )
      {
        Log.w(TAG, "setSeekBars: <" + param[ i ] + "> is not an valid number! Set to 0!");
        rgbw[ i - 1 ] = 0;
      }
    }
    //
    // hier sollten die Parameter gesetzt sein
    //
    setSeekBars();
  }

  /**
   * Die SeekBars nach dem RGBW Array setzen
   */
  private void setSeekBars()
  {
    seekRed.setValue(rgbw[ 0 ] / 256.0F);
    seekGreen.setValue(rgbw[ 1 ] / 256.0F);
    seekBlue.setValue(rgbw[ 2 ] / 256.0F);
    seekWhite.setValue(rgbw[ 3 ] / 256.0F);
  }

  @Override
  public void onServiceConnected()
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "BT Service connected");
    }
    seekRed.setEnabled(true);
    seekGreen.setEnabled(true);
    seekBlue.setEnabled(true);
    seekWhite.setEnabled(true);
    if( btConfig == null )
    {
      Log.e(TAG, "not BT config object there!");
      return;
    }
    if( btConfig.getDeviceAddress() == null )
    {
      deviceAddress.setText("--:--:--:--:--:--");
    }
    else
    {
      deviceAddress.setText(btConfig.getDeviceAddress());
    }

    if( btConfig.isUART() )
    {
      isSerial.setText(getActivity().getResources().getString(R.string.is_serial));
    }
    else
    {
      isSerial.setText(getActivity().getResources().getString(R.string.no_serial));
    }

    if( btConfig.isConnected() )
    {
      connectionState.setText(getActivity().getResources().getString(R.string.connected));
    }
    else
    {
      connectionState.setText(getActivity().getResources().getString(R.string.disconnected));
    }
  }

  @Override
  public void onServiceDisconnected()
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "BT Service disconnected");
    }
    seekRed.setEnabled(false);
    seekGreen.setEnabled(false);
    seekBlue.setEnabled(false);
    seekWhite.setEnabled(false);
    seekRed.setValue(0.0F);
    seekGreen.setValue(0.0F);
    seekBlue.setValue(0.0F);
    seekWhite.setValue(0.0F);
    deviceAddress.setText("--:--:--:--:--:--");
    connectionState.setText(getActivity().getResources().getString(R.string.disconnected));
    isSerial.setText(getActivity().getResources().getString(R.string.no_serial));
  }

  @Override
  public void onPageSelected()
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "Page DIRECTCONTROL was selected");
    }
    if( btConfig.isConnected() && btConfig.getCharacteristicTX() != null && btConfig.getCharacteristicRX() != null )
    {
      //
      // Alles ist so wie es soll
      // mach eine Abfrage vom Modul und dann geht es weiter
      //
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "BT Device is connected and ready....");
      }
      onServiceConnected();
      final short[] pm = mainService.getModulRGBW();
      rgbw[ 0 ] = pm[ 0 ];
      rgbw[ 1 ] = pm[ 1 ];
      rgbw[ 2 ] = pm[ 2 ];
      rgbw[ 3 ] = pm[ 3 ];
      setSeekBars();
    }
  }


  private void onProgressChanged()
  {
    if( timeToSend < System.currentTimeMillis() && mainService != null )
    {
      //
      // Mal wieder zum Contoller senden!
      //
      mainService.setModulRawRGBW(rgbw);
      //
      // Neue Deadline setzen
      //
      timeToSend = System.currentTimeMillis() + ProjectConst.TIMEDIFF_TO_SEND;
    }
  }


  @Override
  public void onConfigurationChanged(Configuration newConfig)
  {
    super.onConfigurationChanged(newConfig);
    if( newConfig.orientation == Configuration.ORIENTATION_PORTRAIT )
    {
      if( BuildConfig.DEBUG )
      {
        Log.i(TAG, "new orientation is PORTRAIT");
      }
    }
    else if( newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE )
    {
      if( BuildConfig.DEBUG )
      {
        Log.i(TAG, "new orientation is LANDSCAPE");
      }
    }
    else
    {
      Log.w(TAG, "new orientation is UNKNOWN");
    }
  }
}
