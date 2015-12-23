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
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Date;
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
public class DirectControlFragment extends Fragment implements IBtEventHandler, SeekBar.OnSeekBarChangeListener
{
  private static final String TAG  = DirectControlFragment.class.getSimpleName();
  private final        short[]  rgbw = new short[ProjectConst.C_ASKRGB_LEN-1];
  private long timeToSend;
  private BluetoothConfig  btConfig;
  private IMainAppServices mainService;
  private TextView         deviceAddress;
  private TextView         connectionState;
  private TextView         isSerial;
  private SeekBar          seekRed;
  private SeekBar          seekGreen;
  private SeekBar          seekBlue;
  private SeekBar          seekWhite;

  public DirectControlFragment()
  {
    Bundle args;
    int    pos;

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
    seekRed = ( SeekBar ) rootView.findViewById(R.id.seekRed);
    seekGreen = ( SeekBar ) rootView.findViewById(R.id.seekGreen);
    seekBlue = ( SeekBar ) rootView.findViewById(R.id.seekBlue);
    seekWhite = ( SeekBar ) rootView.findViewById(R.id.seekWhite);
    //
    // On Change Listener setzen
    //
    seekRed.setOnSeekBarChangeListener(this);
    seekGreen.setOnSeekBarChangeListener(this);
    seekBlue.setOnSeekBarChangeListener(this);
    seekWhite.setOnSeekBarChangeListener(this);
    //
    // nicht verbunden einstellen
    //
    onServiceDisconnected();
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
    onServiceConnected();
  }

  @Override
  public void onBTDisconnected()
  {
    Log.v(TAG, "BT Device disconnected!");
    onServiceDisconnected();
  }

  @Override
  public void onBTServicesRecived(List<BluetoothGattService> gattServices)
  {
    Log.v(TAG, "BT Device services recived");
  }

  @Override
  public void onBTDataAvaiable(String data)
  {
    String[] param;
    int      cmdNum;

    if( Pattern.matches(ProjectConst.KOMANDPATTERN, data) )
    {
      //
      // Kommando empfangen
      //
      param = data.split(":");
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
            Log.e(TAG, "unknown command recived! Ignored.");
            break;

          //
          // Frage nach dem Typ / Antwort
          //
          case ProjectConst.C_ASKTYP:
            Log.v(TAG, "Modul type recived! <" + data + ">");
            break;

          //
          // Frage nach dem Modulname / Antwort
          //
          case ProjectConst.C_ASKNAME:
            Log.v(TAG, "Modul name recived! <" + data + ">");
            break;

          //
          // Frage nach RGBW
          //
          case ProjectConst.C_ASKRGB:
            setSeekBars(param);
            Log.v(TAG, "RGBW from module recived! (should not done) <" + data + ">");
            break;

          //
          // Sende COLOR
          //
          case ProjectConst.C_SETCOLOR:
            Log.v(TAG, "SET RGBW to module (should not done)  <" + data + ">");
            break;
        }
      }
      else
      {
        Log.e(TAG, "wrong command string recived! Ignored.");
      }
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
    seekRed.setProgress(rgbw[ 0 ]);
    seekGreen.setProgress(rgbw[ 1 ]);
    seekBlue.setProgress(rgbw[ 2 ]);
    seekWhite.setProgress(rgbw[ 3 ]);
  }

  @Override
  public void onServiceConnected()
  {
    Log.v(TAG, "BT Service connected");
    seekRed.setEnabled(true);
    seekGreen.setEnabled(true);
    seekBlue.setEnabled(true);
    seekWhite.setEnabled(true);
  }

  @Override
  public void onServiceDisconnected()
  {
    Log.v(TAG, "BT Service disconnected");
    seekRed.setEnabled(false);
    seekGreen.setEnabled(false);
    seekBlue.setEnabled(false);
    seekWhite.setEnabled(false);
    seekRed.setProgress(0);
    seekGreen.setProgress(0);
    seekBlue.setProgress(0);
    seekWhite.setProgress(0);
  }

  @Override
  public void onPageSelected()
  {
    Log.v(TAG, "Page DIRECTCONTROL was selected");
    if( btConfig.isConnected() && btConfig.getCharacteristicTX() != null && btConfig.getCharacteristicRX() != null )
    {
      //
      // Alles ist so wie es soll
      // mach eine Abfrage vom Modul und dann geht es weiter
      //
      Log.v(TAG, "BT Device is connected and ready....");
      onServiceConnected();
      mainService.askModulForRGBW();
    }
    else if( btConfig.isConnected() )
    {
      Log.v(TAG, "BT Device is connected....");
      onServiceConnected();
      if( btConfig.getModuleType() == null )
      {
        // Frage das Modul nach dem Typ, wenn noch nicht geschehen
        // sollte nach dem connect passieren
        mainService.askModulForType();
      }
    }
  }


  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
  {
    if( fromUser )
    {
      if( seekBar.equals(seekRed) )
      {
        Log.i(TAG, String.format(Locale.ENGLISH, "Changed RED <%03d>", progress));
        rgbw[0] = (short)(progress & 0xff);
      }
      else if( seekBar.equals(seekGreen) )
      {
        Log.i(TAG, String.format(Locale.ENGLISH, "Changed GREEN <%03d>", progress));
        rgbw[1] = (short)(progress & 0xff);
      }
      else if( seekBar.equals(seekBlue) )
      {
        Log.i(TAG, String.format(Locale.ENGLISH, "Changed BLUE <%03d>", progress));
        rgbw[2] = (short)(progress & 0xff);
      }
      else if( seekBar.equals(seekWhite) )
      {
        Log.i(TAG, String.format(Locale.ENGLISH, "Changed WHITE <%03d>", progress));
        rgbw[3] = (short)(progress & 0xff);
      }
      if( timeToSend < System.currentTimeMillis() && mainService != null )
      {
        //
        // Mal wieder zum Contoller senden!
        //
        mainService.setModulRGBW( rgbw );
        //
        // Neue Deadline setzen
        //
        timeToSend = System.currentTimeMillis() + ProjectConst.TIMEDIFF_TO_SEND;
      }
    }
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar)
  {
    //
    // Wann soll gesendet werden, wenn der user kontinuierlich draggt?
    //
    timeToSend = System.currentTimeMillis() + ProjectConst.TIMEDIFF_TO_SEND;
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar)
  {
    //
    // Mal wieder zum Contoller senden!
    //
    mainService.setModulRGBW( rgbw );
  }
}
