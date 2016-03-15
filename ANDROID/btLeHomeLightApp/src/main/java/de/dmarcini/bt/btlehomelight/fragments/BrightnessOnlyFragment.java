package de.dmarcini.bt.btlehomelight.fragments;

import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ValueBar;

import java.util.Locale;

import de.dmarcini.bt.btlehomelight.BuildConfig;
import de.dmarcini.bt.btlehomelight.ProjectConst;
import de.dmarcini.bt.btlehomelight.R;
import de.dmarcini.bt.btlehomelight.interfaces.IBtCommand;
import de.dmarcini.bt.btlehomelight.utils.BlueThoothMessage;

/**
 * Fragment für den Helligkeitsregler
 * TODO: RGBW und RGB unterstützen
 */
public class BrightnessOnlyFragment extends LightRootFragment implements ValueBar.OnValueChangedListener, View.OnTouchListener
{
  private static final String TAG = BrightnessOnlyFragment.class.getSimpleName();
  private ValueBar brightnessSeekBar;
  private float[] mHSVColor = new float[ 3 ];
  private TextView brightnessHeaderTextView;
  private String   brightnessValueString;
  //private int currColor;

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
      Log.d(TAG, "make brightness fragment...");
    }
    rootView = inflater.inflate(R.layout.fragment_brightness_only, container, false);
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

    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreateView...OK");
    }
    return (rootView);
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

  @Override
  public void onResume()
  {
    super.onResume();
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onResume: ...");
    }
    runningActivity.askModulForRGBW();
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
  public void msgDataRecived(final BlueThoothMessage msg)
  {
    final short[] rgbw;
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
          brightnessSeekBar.post(new Runnable()
          {
            public void run()
            {
              int currColor = ((rgbw[ 0 ] << 16) | (rgbw[ 1 ] << 8) | (rgbw[ 2 ]));
              Color.colorToHSV(((rgbw[ 0 ] << 16) | (rgbw[ 1 ] << 8) | (rgbw[ 2 ])), mHSVColor);
              Log.v(TAG, String.format(Locale.ENGLISH, "set bar color to %08X", currColor));
              brightnessSeekBar.setValue(mHSVColor[ 2 ]);
              brightnessSeekBar.setColor(Color.HSVToColor(new float[]{mHSVColor[ 0 ], mHSVColor[ 1 ], 1.0F}));
              brightnessHeaderTextView.setText(String.format(Locale.ENGLISH, brightnessValueString, Math.round(100 * mHSVColor[ 2 ])));
            }
          });
          return;
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

  @Override
  public void onValueChanged(int color)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "color changed to %08X!", color));
    }
    Color.colorToHSV(color, mHSVColor);
    brightnessHeaderTextView.setText(String.format(Locale.ENGLISH, brightnessValueString, Math.round(100 * mHSVColor[ 2 ])));
    sendColor(color, false);
  }

  /**
   * Called when a touch event is dispatched to a view. This allows listeners to
   * get a chance to respond before the target view.
   *
   * @param touchedView The view the touch event has been dispatched to.
   * @param event       The MotionEvent object containing full information about
   *                    the event.
   * @return True if the listener has consumed the event, false otherwise.
   */
  @Override
  public boolean onTouch(View touchedView, MotionEvent event)
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
        int color = Color.HSVToColor(mHSVColor);
        sendColor(color, false);
        //
        // Neue Deadline setzen
        //
        timeToSend = System.currentTimeMillis() + ProjectConst.TIMEDIFF_TO_SEND;
        break;
    }
    return false;
  }
}
