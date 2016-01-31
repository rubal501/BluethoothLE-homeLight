/******************************************************************************
 *                                                                            *
 *      project: ANDROID                                                      *
 *      module: btlehomelight                                                 *
 *      class: DirectControlFragment                                          *
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
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ValueBar;

import java.util.List;
import java.util.Locale;

import de.dmarcini.bt.homelight.BuildConfig;
import de.dmarcini.bt.homelight.ProjectConst;
import de.dmarcini.bt.homelight.R;
import de.dmarcini.bt.homelight.interrfaces.IFragmentInterface;
import de.dmarcini.bt.homelight.interrfaces.IMainAppServices;
import de.dmarcini.bt.homelight.utils.BluetoothModulConfig;


/**
 * Direkte Helligkeitssteuerung aller Farben, eigentlich nur zum Testen
 */
public class DirectControlFragment extends AppFragment implements IFragmentInterface, View.OnTouchListener
{
  private static String TAG = DirectControlFragment.class.getSimpleName();
  private TextView deviceAddress;
  private TextView connectionState;
  private TextView isSerial;
  private ValueBar seekRed;
  private ValueBar seekGreen;
  private ValueBar seekBlue;
  private ValueBar seekWhite;

  public DirectControlFragment()
  {
    super();
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
    int resId;
    //
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreateView...");
    }
    setHasOptionsMenu(true);
    //
    // die richtige Orientierung erfragen
    //
    if( getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "Orientation => LANDSCAPE...");
      }
      resId = R.layout.fragment_direct_control_land;
    }
    else
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "Orientation => PORTRAIT...");
      }
      resId = R.layout.fragment_direct_control_port;
    }
    View rootView = inflater.inflate(resId, container, false);
    //
    // Zeiger und Callbacks vorbereiten
    //
    prepareViews(rootView);
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

  /**
   * lade ein neues Layout, wenn sich die Orientation geändert hat
   *
   * @param orientation Ausrichtung
   */
  private void changeLayoutOrientation(int orientation)
  {
    int          resId;
    LinearLayout rootView;
    LinearLayout tempLayout;
    LinearLayout headerLayout;
    LinearLayout sliderLayout;

    if( orientation == Configuration.ORIENTATION_LANDSCAPE )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "changeLayoutOrientation => LANDSCAPE...");
      }
      resId = R.layout.fragment_direct_control_land;
    }
    else
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "changeLayoutOrientation => PORTRAIT...");
      }
      resId = R.layout.fragment_direct_control_port;
    }
    //
    // das rootview suchen
    //
    rootView = ( LinearLayout ) getActivity().findViewById(R.id.directControlRootLayout);
    //
    // remove Views...
    //
    rootView.removeAllViews();
    // Orientierung ändern
    rootView.setOrientation(orientation);
    // neue Resource laden (wegen der Dimensionen, ist dort leicher definierbar
    tempLayout = ( LinearLayout ) getActivity().getLayoutInflater().inflate(resId, ( ViewGroup ) rootView.getParent(), false);
    headerLayout = ( LinearLayout ) tempLayout.findViewById(R.id.headerLayout);
    sliderLayout = ( LinearLayout ) tempLayout.findViewById(R.id.sliderLayout);
    //
    // Vies in das Layout einfügen
    //
    tempLayout.removeAllViews();
    tempLayout.invalidate();
    rootView.addView(headerLayout);
    rootView.addView(sliderLayout);
    //
    // Alle Zeiger und Callbacks vorbereiten
    //
    prepareViews(rootView);
    setSeekBars();
    rootView.invalidate();
    //
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "changeLayoutOrientation...OK");
    }
  }

  /**
   * Die Zeiger (Referenzen) und Callbacks vorbereiten, wenn neues View erzeugt wird
   *
   * @param rootView das Wurzelview
   */
  private void prepareViews(View rootView)
  {
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
    // Farbe des Pointers setzen
    //
    seekRed.setBarPointerHaloPaintColor(getResources().getColor(R.color.valuebar_holo_pointer_color));
    seekGreen.setBarPointerHaloPaintColor(getResources().getColor(R.color.valuebar_holo_pointer_color));
    seekBlue.setBarPointerHaloPaintColor(getResources().getColor(R.color.valuebar_holo_pointer_color));
    seekWhite.setBarPointerHaloPaintColor(getResources().getColor(R.color.valuebar_holo_pointer_color));
    //
    // onTouchListener (um mitzugekommen, wann das schieben zuende ist)
    //
    seekRed.setOnTouchListener(this);
    seekGreen.setOnTouchListener(this);
    seekBlue.setOnTouchListener(this);
    seekWhite.setOnTouchListener(this);
    //
    // On Change Listener setzen
    //
    seekRed.setOnValueChangedListener(new ValueBar.OnValueChangedListener()
    {
      @Override
      public void onValueChanged(int value)
      {
        rgbw[ 0 ] = ( short ) ((value >> 16) & 0xff);
        Log.i(TAG, String.format(Locale.ENGLISH, "value RED changed to %02X...", rgbw[ 0 ]));
        onProgressChanged();
      }
    });

    seekGreen.setOnValueChangedListener(new ValueBar.OnValueChangedListener()
    {
      @Override
      public void onValueChanged(int value)
      {
        rgbw[ 1 ] = ( short ) ((value >> 8) & 0xff);
        Log.i(TAG, String.format(Locale.ENGLISH, "value GREEN changed to %02X...", rgbw[ 1 ]));
        onProgressChanged();
      }
    });
    seekBlue.setOnValueChangedListener(new ValueBar.OnValueChangedListener()
    {
      @Override
      public void onValueChanged(int value)
      {
        rgbw[ 2 ] = ( short ) (value & 0xff);
        Log.i(TAG, String.format(Locale.ENGLISH, "value BLUE changed to %02X...", rgbw[ 2 ]));
        onProgressChanged();
      }
    });
    seekWhite.setOnValueChangedListener(new ValueBar.OnValueChangedListener()
    {
      @Override
      public void onValueChanged(int value)
      {
        rgbw[ 3 ] = ( short ) (value & 0xff);
        Log.i(TAG, String.format(Locale.ENGLISH, "value WHITE changed to %02X...", rgbw[ 3 ]));
        onProgressChanged();
      }
    });
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreateOptionsMenu...");
    }
    //MenuInflater inflater = getActivity().getMenuInflater();
    //inflater.inflate(R.menu.menu_direct_control_fragment, menu);
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
      case R.id.menu_preferences:
        // TODO: Preferences Activity aufrufen
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
    setGUIContent();
  }

  /**
   * Inhalte in den Textfelder etc setzen
   */
  private void setGUIContent()
  {
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
      isSerial.setText(getActivity().getResources().getString(R.string.direct_control_is_serial));
    }
    else
    {
      isSerial.setText(getActivity().getResources().getString(R.string.direct_control_no_serial));
    }

    if( btConfig.isConnected() )
    {
      connectionState.setText(getActivity().getResources().getString(R.string.direct_control_connected));
    }
    else
    {
      connectionState.setText(getActivity().getResources().getString(R.string.direct_control_disconnected));
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
    rgbw[ 0 ] = rgbw[ 1 ] = rgbw[ 2 ] = rgbw[ 3 ] = 0;
    setSeekBars();
    deviceAddress.setText("--:--:--:--:--:--");
    connectionState.setText(getActivity().getResources().getString(R.string.direct_control_disconnected));
    isSerial.setText(getActivity().getResources().getString(R.string.direct_control_no_serial));
  }

  @Override
  public void onPageSelected()
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "Page DIRECTCONTROL was selected");
    }
    if( mainServiceRef == null )
    {
      Log.e(TAG, "can't set Callback handler to APP");
      return;
    }
    mainServiceRef.setHandler( this );
    //
    //Wenn Modul verbunden ist, setzte die SeekBars
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
      final short[] pm = mainServiceRef.getModulRGBW();
      rgbw[ 0 ] = pm[ 0 ];
      rgbw[ 1 ] = pm[ 1 ];
      rgbw[ 2 ] = pm[ 2 ];
      rgbw[ 3 ] = pm[ 3 ];
      setSeekBars();
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

  /**
   * Wenn sich da was geändert hat :-)
   */
  private void onProgressChanged()
  {
    if( timeToSend < System.currentTimeMillis() && mainServiceRef != null )
    {
      //
      // Mal wieder zum Contoller senden!
      //
      mainServiceRef.setModulRawRGBW(rgbw);
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
    changeLayoutOrientation(newConfig.orientation);
    setGUIContent();
    setSeekBars();
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
        mainServiceRef.setModulRawRGBW(rgbw);
        //
        // Neue Deadline setzen
        //
        timeToSend = System.currentTimeMillis() + ProjectConst.TIMEDIFF_TO_SEND;
        break;
    }
    return false;
  }
}
