/******************************************************************************
 *                                                                            *
 *      project: ANDROID                                                      *
 *      module: btlehomelight                                                 *
 *      class: BrightnessOnlyFragment                                         *
 *      date: 2016-01-10                                                      *
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

import android.app.DialogFragment;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import de.dmarcini.bt.homelight.ProjectConst;


/**
 * Created by dmarc on 22.08.2015.
 */
public class BrightnessOnlyFragment extends AppFragment implements ValueBar.OnValueChangedListener, View.OnTouchListener
{
  private static String TAG = BrightnessOnlyFragment.class.getSimpleName();
  private              int    brightness = 0;
  private ValueBar brightnessSeekBar;
  private TextView brightnessHeaderTextView;
  private String   brightnessValueString;

  public BrightnessOnlyFragment()
  {
    super();
  }

  /**
   * Returns a new instance of this fragment for the given section
   * number.
   */
  public static BrightnessOnlyFragment newInstance(int sectionNumber, BluetoothModulConfig btConfig)
  {
    BrightnessOnlyFragment fragment = new BrightnessOnlyFragment();
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
    brightnessHeaderTextView = ( TextView ) rootView.findViewById(R.id.brightnessHeaderTextView);
    brightnessValueString = getActivity().getString(R.string.brigtness_header_vals);
    brightnessSeekBar = ( ValueBar ) rootView.findViewById(R.id.brightnessValueBar);
    brightnessSeekBar.setOnValueChangedListener(this);
    brightnessSeekBar.setOnTouchListener(this);
    brightnessSeekBar.setColor(0xffffffff);
    brightnessSeekBar.setBarPointerHaloPaintColor(getResources().getColor(R.color.valuebar_holo_pointer_br_color));

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
          if(BuildConfig.DEBUG)Log.v(TAG, "unhandled command recived! Ignored.");
          break;

        //
        // Frage nach RGBW
        //
        case ProjectConst.C_ASKRGBW:
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
            Log.v(TAG, "RGBW from module recived!");
          }
          break;
       }
    }
    else
    {
      Log.e(TAG, "wrong command string recived! Ignored.");
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
    //
    // Setzte Callback für das Fragment bei der App
    //
    if( mainService == null )
    {
      mainService = ( IMainAppServices ) getActivity();
    }
    mainService.setHandler( this );
    //
    //Wenn Modul verbunden ist, setzte die SeekBar
    //
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
      setSeekBar();
    }
  }

  /**
   * Der Dialog hat eine Positive Antwort
   *
   * @param frag Das Fragment( der Dialog )
   */
  @Override
  public void onPositiveDialogFragment(DialogFragment frag)
  {

  }

  /**
   * Der Dialog hat eine Negative Antwort
   *
   * @param frag Das Fragment( der Dialog )
   */
  @Override
  public void onNegativeDialogFragment(DialogFragment frag)
  {

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
    setSeekBar();
  }

  /**
   * Setze die Bar auf die vorhandenen Helligkeitswerte
   */
  private void setSeekBar()
  {
    brightness = ( int ) Math.floor((rgbw[ 0 ] + rgbw[ 1 ] + rgbw[ 2 ] + (3 * rgbw[ 3 ])) / 6);
    for( int i = 0; i < 4; i++ )
    {
      rgbw[ i ] = ( short ) brightness;
    }
    brightnessHeaderTextView.setText(String.format(Locale.ENGLISH, brightnessValueString, brightness));
    brightnessSeekBar.setValue(brightness / 256.0F);
  }


  @Override
  public void onValueChanged(int value)
  {
    //
    // Da die Bar mit weiss initialisiert wurde, sind RGB immer gleich
    // daher nutze ich nur einen Werrt (B)
    //
    brightness = value & 0xff;
    if( BuildConfig.DEBUG )
    {
      Log.i(TAG, String.format(Locale.ENGLISH, "Changed BRIGHTNESS <%03d>", brightness));
    }
    // die RGBW Werte setzen
    rgbw[ 0 ] = rgbw[ 1 ] = rgbw[ 2 ] = rgbw[ 3 ] = ( short ) brightness;
    // die Anzeige korrigieren
    brightnessHeaderTextView.setText(String.format(Locale.ENGLISH, brightnessValueString, brightness));
    //
    // Falls die Zeit reif ist für eine Übertragung zum Modul
    //
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

  /**
   * Called when a touch event is dispatched to a view. This allows listeners to
   * get a chance to respond before the target view.
   *
   * @param v     The view the touch event has been dispatched to.
   * @param event The MotionEvent object containing full information about
   *              the event.
   * @return True if the listener has consumed the event, false otherwise.
   */
  @Override
  public boolean onTouch(View v, MotionEvent event)
  {
    switch( event.getAction() )
    {
      //
      // finger vom Slider genommen -> senden
      //
      case MotionEvent.ACTION_UP:
        //
        // Mal wieder zum Contoller senden!
        //
        mainService.setModulRawRGBW(rgbw);
        //
        // Neue Deadline setzen
        //
        timeToSend = System.currentTimeMillis() + ProjectConst.TIMEDIFF_TO_SEND;
        break;
    }
    return false;
  }
}
