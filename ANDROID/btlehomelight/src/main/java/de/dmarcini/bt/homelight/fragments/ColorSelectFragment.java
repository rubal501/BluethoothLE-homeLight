/******************************************************************************
 * *
 * project: ANDROID                                                      *
 * module: btlehomelight                                                 *
 * class: ColorSelectFragment                                            *
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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import java.util.List;
import java.util.Locale;

import de.dmarcini.bt.homelight.BuildConfig;
import de.dmarcini.bt.homelight.R;
import de.dmarcini.bt.homelight.interrfaces.IMainAppServices;
import de.dmarcini.bt.homelight.utils.BluetoothModulConfig;
import de.dmarcini.bt.homelight.utils.ProjectConst;
import de.dmarcini.bt.homelight.views.ColorPicker;


/**
 * Created by dmarc on 22.08.2015.
 */
public class ColorSelectFragment extends AppFragment implements ColorPicker.OnColorChangedListener, ColorPicker.OnColorSelectedListener, View.OnClickListener
{
  private static final String TAG = ColorSelectFragment.class.getSimpleName();
  private int          currColor;
  private ColorPicker  picker;
  private ToggleButton calToggleButton;

  public ColorSelectFragment()
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
      Log.e(TAG, "Konstructor: " + ex.getLocalizedMessage());
    }
  }

  /**
   * Returns a new instance of this fragment for the given section
   * number.
   */
  public static ColorSelectFragment newInstance(int sectionNumber, BluetoothModulConfig btConfig)
  {
    ColorSelectFragment fragment = new ColorSelectFragment();
    fragment.setBlutethoothConfig(btConfig);
    Bundle args = new Bundle();
    args.putInt(ProjectConst.ARG_SECTION_NUMBER, sectionNumber);
    fragment.setArguments(args);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "ColorSelectFragment.newInstance(%04d)", sectionNumber));
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
    int resId;
    //
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreateView...");
    }
    //
    if( getActivity() instanceof IMainAppServices )
    {
      mainService = ( IMainAppServices ) getActivity();
    }
    else
    {
      Log.e(TAG, "Application is not type of AppServices");
      mainService = null;
    }

    if( getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "Orientation => LANDSCAPE...");
      }
      resId = R.layout.fragment_colors_wheel_land;
    }
    else
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "Orientation => PORTRAIT...");
      }
      resId = R.layout.fragment_colors_wheel_port;
    }
    View rootView = inflater.inflate(resId, container, false);
    picker = ( ColorPicker ) rootView.findViewById(R.id.colorPicker);
    calToggleButton = ( ToggleButton ) rootView.findViewById(R.id.RGBWToggleButton);
    //
    // Farbe setzen (Voreinstellung)
    //
    picker.setColor(0xFFFFFF);
    calToggleButton.setChecked(true);
    //
    // Change Listener setzen
    //
    picker.setOnColorChangedListener(this);
    picker.setOnColorSelectedListener(this);
    calToggleButton.setOnClickListener(this);
    //
    // "alte" Farbe nicht setzen/anzeigen
    //
    setColorPropertysFromConfig();
    setHasOptionsMenu(true);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreateView...OK");
    }
    return (rootView);
  }

  private void changeLayoutOrientation(int orientation)
  {
    int          resId;
    LinearLayout rootView;
    LinearLayout tempView;

    if( orientation == Configuration.ORIENTATION_LANDSCAPE )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "changeLayoutOrientation => LANDSCAPE...");
      }
      resId = R.layout.fragment_colors_wheel_land;
    }
    else
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "changeLayoutOrientation => PORTRAIT...");
      }
      resId = R.layout.fragment_colors_wheel_port;
    }
    //
    rootView = ( LinearLayout ) getActivity().findViewById(R.id.colorWeelLayout);
    //
    // remote Views...
    //
    rootView.removeAllViews();
    // Orientierung ändern
    rootView.setOrientation(orientation);
    // neue Resource laden (wegen der Dimensionen, ist dort leicher definierbar
    tempView = ( LinearLayout ) getActivity().getLayoutInflater().inflate(resId, ( ViewGroup ) rootView.getParent(), false);
    picker = ( ColorPicker ) tempView.findViewById(R.id.colorPicker);
    calToggleButton = ( ToggleButton ) tempView.findViewById(R.id.RGBWToggleButton);
    //
    // Vies in das Layout einfügen
    //
    tempView.removeAllViews();
    tempView.invalidate();
    rootView.addView(picker);
    rootView.addView(calToggleButton);
    //
    // Farbe setzen (Voreinstellung)
    //
    picker.setColor(0xFFFFFF);
    calToggleButton.setChecked(true);
    //
    // Change Listener setzen
    //
    picker.setOnColorChangedListener(this);
    picker.setOnColorSelectedListener(this);
    calToggleButton.setOnClickListener(this);
    //
    // "alte" Farbe nicht setzen/anzeigen
    //
    setColorPropertysFromConfig();
    setHasOptionsMenu(true);

    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "changeLayoutOrientation...OK");
    }
  }

  private void setColorPropertysFromConfig()
  {
    if( btConfig != null && btConfig.isConnected() )
    {
      picker.setEnabled(true);
    }
    picker.setEnabled(false);
  }


  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreateOptionsMenu...");
    }
    inflater.inflate(R.menu.menu_home_light_main, menu);
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
  public void onColorChanged(int color)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "color changed to %08X", color));
    }
    currColor = color;
    // Werte verschicken
    rgbw[ 0 ] = ( short ) ((color >> 16) & 0xff);
    rgbw[ 1 ] = ( short ) ((color >> 8) & 0xff);
    rgbw[ 2 ] = ( short ) (color & 0xff);
    rgbw[ 3 ] = 0;
    //
    if( timeToSend < System.currentTimeMillis() && mainService != null )
    {
      //
      // Mal wieder zum Contoller senden!
      //
      if( calToggleButton.isChecked() )
      {
        mainService.setModulRGB4Calibrate(rgbw);
      }
      else
      {
        mainService.setModulRawRGBW(rgbw);
      }
      //
      // Neue Deadline setzen
      //
      timeToSend = System.currentTimeMillis() + ProjectConst.TIMEDIFF_TO_SEND;
    }
  }


  @Override
  public void onBTConnected()
  {
    //
    // GUI für verbundenes Gerät einrichten
    //
    setColorPropertysFromConfig();
  }

  @Override
  public void onBTDisconnected()
  {
    //
    // GUI für getrenntes Gerät einrichten
    //
    setColorPropertysFromConfig();
  }

  @Override
  public void onBTServicesRecived(List<BluetoothGattService> gattServices)
  {

  }

  @Override
  public void onBTDataAvaiable(String[] param)
  {
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
            if( BuildConfig.DEBUG )
            {
              Log.v(TAG, "RGBW from module recived!");
            }
            final String[] pm = param;
            //
            // Das läßt sich nur von diesem Thread aus machen, daher dieses
            //
            picker.post(new Runnable()
            {
              public void run()
              {
                setColorWheel(pm);
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
      Log.v(TAG, "Page COLORSELECT (Weehl) was selected");
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
      rgbw[0] = pm[0];
      rgbw[1] = pm[1];
      rgbw[2] = pm[2];
      rgbw[3] = pm[3];
      setColorWheel();
      //mainService.askModulForRGBW();
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
      if( BuildConfig.DEBUG )
      {
        Log.w(TAG, "new orientation is UNKNOWN");
      }
    }
    changeLayoutOrientation(newConfig.orientation);
    // die Farbe auch wieder einstellen!
    picker.setColor(currColor);
    (( IMainAppServices ) getActivity()).switchToFragment(ProjectConst.PAGE_COLOR_CIRCLE);
  }


  private void setColorWheel(String[] param)
  {
    //
    if( param.length < ProjectConst.C_ASKRGB_LEN )
    {
      Log.w(TAG, "setColorWheel() -> param array to short! IGNORED!");
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
        Log.w(TAG, "setColorWheel: <" + param[ i ] + "> is not an valid number! Set to 0!");
        rgbw[ i - 1 ] = 0;
      }
    }
    //
    // hier sollten die Parameter gesetzt sein
    //
    Log.d(TAG, "set color wheel...");
    setColorWheel();
  }

  /**
   * Die SeekBars nach dem RGBW Array setzen
   */
  private void setColorWheel()
  {
    currColor = ((rgbw[ 0 ] << 16) | (rgbw[ 1 ] << 8) | (rgbw[ 2 ]));
    picker.setColor(currColor);
  }


  @Override
  public void onColorSelected(int color)
  {

    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "color selected to %08X", color));
    }
    currColor = color;
    // Werte verschicken
    rgbw[ 0 ] = ( short ) ((color >> 16) & 0xff);
    rgbw[ 1 ] = ( short ) ((color >> 8) & 0xff);
    rgbw[ 2 ] = ( short ) (color & 0xff);
    rgbw[ 3 ] = 0;
    //
    if( mainService != null )
    {
      //
      // Mal wieder zum Contoller senden!
      //
      if( calToggleButton.isChecked() )
      {
        mainService.setModulRGB4Calibrate(rgbw);
      }
      else
      {
        mainService.setModulRawRGBW(rgbw);
      }
      //
      // Neue Deadline setzen
      //
      timeToSend = System.currentTimeMillis() + ProjectConst.TIMEDIFF_TO_SEND;
    }
  }

  @Override
  public void onClick(View clickedView)
  {
    if( clickedView instanceof ToggleButton )
    {
      if( clickedView.equals(calToggleButton) )
      {
        if( (( ToggleButton ) clickedView).isChecked() )
        {
          if( BuildConfig.DEBUG )
          {
            Log.i(TAG, "Button CHECKED");
          }
        }
        else
        {
          if( BuildConfig.DEBUG )
          {
            Log.i(TAG, "Button UNCHECKED");
          }
        }
        onColorSelected(picker.getColor());
      }
    }
  }
}
