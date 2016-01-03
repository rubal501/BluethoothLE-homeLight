/*
 *
 *     ANDROID
 *     btlehomelight
 *     BrightnessOnlyFragment
 *     2016-01-03
 *     Copyright (C) 2016  Dirk Marciniak
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/
 * /
 *
 */

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
import android.widget.SeekBar;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import de.dmarcini.bt.homelight.BuildConfig;
import de.dmarcini.bt.homelight.R;
import de.dmarcini.bt.homelight.interrfaces.IMainAppServices;
import de.dmarcini.bt.homelight.utils.BluetoothConfig;
import de.dmarcini.bt.homelight.utils.ProjectConst;



/**
 * Created by dmarc on 22.08.2015.
 */
public class BrightnessOnlyFragment extends AppFragment implements SeekBar.OnSeekBarChangeListener
{
  private static final String TAG        = BrightnessOnlyFragment.class.getSimpleName();
  private              int    brightness = 0;
  //private VerticalSeekBar brightnessSeekBar;

  public BrightnessOnlyFragment()
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
  public static BrightnessOnlyFragment newInstance(int sectionNumber, BluetoothConfig btConfig)
  {
    BrightnessOnlyFragment fragment = new BrightnessOnlyFragment();
    fragment.setBlutethoothConfig(btConfig);
    Bundle args = new Bundle();
    args.putInt(ProjectConst.ARG_SECTION_NUMBER, sectionNumber);
    fragment.setArguments(args);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "DiscoveringFragment.newInstance(%04d)", sectionNumber));
    }
    return fragment;
  }

  private void setBlutethoothConfig(BluetoothConfig btConfig)
  {
    this.btConfig = btConfig;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View rootView = inflater.inflate(R.layout.fragment_brightness_only, container, false);
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
//    brightnessSeekBar = ( VerticalSeekBar ) rootView.findViewById(R.id.brightnessSeekBar);
//    brightnessSeekBar.setOnSeekBarChangeListener(this);
//    brightnessSeekBar.setMax(256);
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
            if( BuildConfig.DEBUG )
            {
              Log.v(TAG, "Modul type recived! <" + data + ">");
            }
            break;

          //
          // Frage nach dem Modulname / Antwort
          //
          case ProjectConst.C_ASKNAME:
            if( BuildConfig.DEBUG )
            {
              Log.v(TAG, "Modul name recived! <" + data + ">");
            }
            break;

          //
          // Frage nach RGBW
          //
          case ProjectConst.C_ASKRAWRGB:
            final String[] pm = param;
            //
            // Das läßt sich nur von diesem Thread aus machen, daher dieses
            //
            brightnessSeekBar.post(new Runnable()
            {
              public void run()
              {
                setSeekBar(pm);
              }
            });

            if( BuildConfig.DEBUG )
            {
              Log.v(TAG, "RGBW from module recived!  <" + data + ">");
            }
            break;

          //
          // Sende COLOR
          //
          case ProjectConst.C_SETCOLOR:
            if( BuildConfig.DEBUG )
            {
              Log.v(TAG, "SET RGBW to module (should not done)  <" + data + ">");
            }
            break;
        }
      }
      else
      {
        Log.e(TAG, "wrong command string recived! Ignored.");
      }
    }

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
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "Page BRIGHTNESS CONTROL was selected");
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
      mainService.askModulForRawRGBW();
    }
    else if( btConfig.isConnected() )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "BT Device is connected....");
      }
      onServiceConnected();
      if( btConfig.getModuleType() == null )
      {
        // Frage das Modul nach dem Typ, wenn noch nicht geschehen
        // sollte nach dem connect passieren
        mainService.askModulForType();
      }
    }
  }

  private void setSeekBar(String[] param)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "setSeekBar()...");
    }
    if( param.length < ProjectConst.C_ASKRGB_LEN )
    {
      Log.w(TAG, "setSeekBar() -> param array to short! IGNORED!");
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

    brightness = ( int ) Math.floor((rgbw[ 0 ] + rgbw[ 1 ] + rgbw[ 2 ] + (3 * rgbw[ 3 ])) / 6);
    for( int i = 0; i < 4; i++ )
    {
      rgbw[ i ] = ( short ) brightness;
    }
    brightnessSeekBar.setProgress(brightness);
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
  {
    //Log.i(TAG, String.format(Locale.ENGLISH, "Changed BRIGHTNESS <%03d>", progress));

    if( fromUser )
    {
      if( seekBar.equals(brightnessSeekBar) )
      {
        if( BuildConfig.DEBUG )
        {
          Log.i(TAG, String.format(Locale.ENGLISH, "Changed BRIGHTNESS <%03d>", progress));
        }
        rgbw[ 0 ] = rgbw[ 1 ] = rgbw[ 2 ] = rgbw[ 3 ] = ( short ) (progress & 0xff);

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
    mainService.setModulRawRGBW(rgbw);

  }
}
