package de.dmarcini.bt.btlehomelight.fragments;

import android.app.DialogFragment;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ValueBar;

import java.util.Locale;

import de.dmarcini.bt.btlehomelight.BuildConfig;
import de.dmarcini.bt.btlehomelight.ProjectConst;
import de.dmarcini.bt.btlehomelight.R;
import de.dmarcini.bt.btlehomelight.interfaces.IBtCommand;
import de.dmarcini.bt.btlehomelight.utils.BlueThoothMessage;

/**
 * Fragment nicht fertig gestellte Seiten
 */
public class EqualizerFragment extends LightRootFragment implements View.OnTouchListener
{
  private static final String  TAG  = EqualizerFragment.class.getSimpleName();
  private              short[] rgbw = new short[ 4 ];
  private TextView connectedDevice;
  private TextView connectionState;
  private ValueBar seekRed;
  private ValueBar seekGreen;
  private ValueBar seekBlue;
  private ValueBar seekWhite;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onCreate...");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View rootView;
    int  resId;
    //
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onCreateView...");
    }
    //
    // wenn kein Container vorhanden ist, dann gibts auch keinen View
    //
    if( container == null )
    {
      Log.e(TAG, "onCreateView: container is NULL ...");
      return (null);
    }
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "make equalizer fragment...");
    }
    //
    // die richtige Orientierung erfragen
    //
    if( getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "Orientation => LANDSCAPE...");
      }
      resId = R.layout.fragment_equalizer_land;
    }
    else
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "Orientation => PORTRAIT...");
      }
      resId = R.layout.fragment_equalizer_port;
    }
    rootView = inflater.inflate(resId, container, false);
    prepareRootView(rootView);
    //
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreateView...OK");
    }
    return (rootView);
  }

  /**
   * Zeiger (Referenzen) und Callbacks vorbereiten
   *
   * @param rootView das root view
   */
  private void prepareRootView(View rootView)
  {
    //
    // Adressen der GUI Objekte bestimmen
    //
    connectedDevice = ( TextView ) rootView.findViewById(R.id.connectedDevice);
    connectionState = ( TextView ) rootView.findViewById(R.id.connectionState);
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

  /**
   * Wenn sich da was geändert hat :-)
   */
  private void onProgressChanged()
  {
    if( timeToSend < System.currentTimeMillis() )
    {
      //
      // Mal wieder zum Contoller senden!
      //
      runningActivity.setModulRawRGBW(rgbw);
      //
      // Neue Deadline setzen
      //
      timeToSend = System.currentTimeMillis() + ProjectConst.TIMEDIFF_TO_SEND;
    }
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
  public void onActivityCreated(Bundle savedInstanceState)
  {
    super.onActivityCreated(savedInstanceState);
    runningActivity = ( IBtCommand ) getActivity();
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onActivityCreated: ...");
    }
  }

  /**
   * Behandle alle ankommenden Nachrichten
   * <p/>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  @Override
  public void handleMessages(BlueThoothMessage msg)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "message recived!");
    }
    switch( msg.getMsgType() )
    {
      case ProjectConst.MESSAGE_NONE:
      case ProjectConst.MESSAGE_TICK:
      case ProjectConst.MESSAGE_DISCONNECTED:
      case ProjectConst.MESSAGE_CONNECTING:
      case ProjectConst.MESSAGE_CONNECTED:
      case ProjectConst.MESSAGE_CONNECT_ERROR:
      case ProjectConst.MESSAGE_BTLE_DEVICE_DISCOVERING:
      case ProjectConst.MESSAGE_BTLE_DEVICE_DISCOVERED:
      case ProjectConst.MESSAGE_BTLE_DEVICE_END_DISCOVERING:
      case ProjectConst.MESSAGE_GATT_SERVICES_DISCOVERED:
        break;

      case ProjectConst.MESSAGE_BTLE_DATA:
        msgDataRecived(msg);
        break;

      default:
        Log.e(TAG, "unhandled message recived: " + msg.getMsgType());
    }
  }

  /**
   * Behandle ankommende Nachricht über den Versuch eine Verbindung aufzubauen
   * <p/>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  @Override
  public void msgConnecting(BlueThoothMessage msg)
  {

  }

  /**
   * Behandle Nachricht über den erfolgreichen Aufbau einer Verbindung zum BT Gerät
   * <p/>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  @Override
  public void msgConnected(BlueThoothMessage msg)
  {

  }

  /**
   * Behandle Nachricht über den Verlust der BT-Verbindung
   * <p/>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  @Override
  public void msgDisconnected(BlueThoothMessage msg)
  {

  }

  /**
   * Behandle TICK-Nachricht vom Service
   * <p/>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  @Override
  public void msgRecivedTick(BlueThoothMessage msg)
  {

  }

  /**
   * Behandle die Nachricht vom Service, dass der Verbindungsversuch erfolglos war
   * <p/>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  @Override
  public void msgConnectError(BlueThoothMessage msg)
  {

  }

  /**
   * Behandle die _Nachricht, dass es einen Timeout beim schreiben zum BT-Gerät gab
   * <p/>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  @Override
  public void msgReciveWriteTmeout(BlueThoothMessage msg)
  {

  }

  /**
   * BEhandle ankommende Daten
   *
   * @param msg Nachricht mit eingeschlossenen Daten
   */
  @Override
  public void msgDataRecived(BlueThoothMessage msg)
  {
    if( msg.getData() == null || msg.getData().isEmpty() )
    {
      Log.w(TAG, "not data in message!");
      return;
    }
    //
    // Jetzt guck mal nach den Daten
    //
    String[] param;
    int      cmdNum;
    //
    // Kommando empfangen
    //
    param = msg.getData().split(":");
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
        default:
        case ProjectConst.C_UNKNOWN:
          Log.e(TAG, "unknown command recived! Ignored.");
          return;
        //
        // Frage nach RGBW
        //
        case ProjectConst.C_ASKRGBW:
          //
          // Weitergeben an die Fragmente
          //
          if( BuildConfig.DEBUG )
          {
            Log.v(TAG, "RGBW from module <" + msg.getData() + ">");
          }
          if( param.length != ProjectConst.C_ASKRGB_LEN )
          {
            return;
          }
          rgbw = fillValuesInArray(param);
          seekRed.post(new Runnable()
          {
            public void run()
            {
              setSeekBars();
            }
          });
          break;
      }
    }
  }

  /**
   * Reaktion aufgerufener Dialoge POSITIV
   *
   * @param dialog der Dialog, welcher aufrief
   */
  @Override
  public void onDialogPositiveClick(DialogFragment dialog)
  {

  }

  /**
   * Reaktion aufgerufener Dialoge NEGATIV
   *
   * @param dialog der Dialog, welcher aufrief
   */
  @Override
  public void onDialogNegativeClick(DialogFragment dialog)
  {

  }

  /**
   * Wenn sich die Ausrichtung geändert hat
   *
   * @param orientation die Ausrichtung des gerätes
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
      resId = R.layout.fragment_equalizer_land;
    }
    else
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "changeLayoutOrientation => PORTRAIT...");
      }
      resId = R.layout.fragment_equalizer_port;
    }
    //
    rootView = ( LinearLayout ) getActivity().findViewById(R.id.directControlRootLayout);
    //
    // remove Views...
    //
    rootView.removeAllViews();
    // Orientierung ändern
    rootView.setOrientation(orientation);
    // neue Resource laden (wegen der Dimensionen, ist dort leicher definierbar)
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
    prepareRootView(rootView);
    setSeekBars();
    rootView.invalidate();
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "changeLayoutOrientation...OK");
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
    //TODO picker.setColor(currColor);
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
        runningActivity.setModulRawRGBW(rgbw);
        //
        // Neue Deadline setzen
        //
        timeToSend = System.currentTimeMillis() + ProjectConst.TIMEDIFF_TO_SEND;
        break;
    }
    return false;
  }

  @Override
  public void onResume()
  {
    super.onResume();
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onResume: ...");
    }
    if( runningActivity.getModulOnlineStatus() == ProjectConst.STATUS_CONNECTED )
    {
      connectionState.setText(R.string.equalizer_fragment_connected);
      connectedDevice.setText(runningActivity.getConnectedModulName());
    }
    else
    {
      connectionState.setText(R.string.equalizer_fragment_disconnected);
      connectedDevice.setText(R.string.equalizer_fragment_no_data);
    }
    runningActivity.askModulForRGBW();
  }
}
