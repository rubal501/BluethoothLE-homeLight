package de.dmarcini.bt.btlehomelight.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Locale;

import de.dmarcini.bt.btlehomelight.BuildConfig;
import de.dmarcini.bt.btlehomelight.ProjectConst;
import de.dmarcini.bt.btlehomelight.R;
import de.dmarcini.bt.btlehomelight.dialogs.ColorPrefSaveDialog;
import de.dmarcini.bt.btlehomelight.interfaces.IBtCommand;
import de.dmarcini.bt.btlehomelight.utils.BlueThoothMessage;
import de.dmarcini.bt.btlehomelight.views.ColorPicker;

/**
 * Created by dmarc on 18.02.2016.
 */
public class ColorCircleFragment extends LightRootFragment implements ColorPicker.OnColorChangedListener, ColorPicker.OnColorSelectedListener, View.OnClickListener
{
  private static final String TAG = ColorCircleFragment.class.getSimpleName();
  private int          currColor;
  private ColorPicker  picker;
  private ToggleButton calToggleButton;
  private Button       colorWheelSaveColorButton;
  private boolean      isRGBW;

  /**
   * Nach dem Erzeugen des Objektes noch Einstellungen....
   */
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
      Log.d(TAG, "make color wheel fragment...");
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
    rootView = inflater.inflate(resId, container, false);
    isRGBW = true;
    prepareRootView(rootView);
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
   * Zeiger (Referenzen) und Callbacks vorbereiten
   *
   * @param rootView das root view
   */
  private void prepareRootView(View rootView)
  {
    picker = ( ColorPicker ) rootView.findViewById(R.id.colorPicker);
    calToggleButton = ( ToggleButton ) rootView.findViewById(R.id.RGBWToggleButton);
    colorWheelSaveColorButton = ( Button ) rootView.findViewById(R.id.colorWheelSaveColorButton);
    //
    // Farbe setzen (Voreinstellung)
    //
    picker.setColor(0xFFFFFF);
    calToggleButton.setChecked(isRGBW);
    //
    // Change Listener setzen
    //
    picker.setOnColorChangedListener(this);
    picker.setOnColorSelectedListener(this);
    calToggleButton.setOnClickListener(this);
    colorWheelSaveColorButton.setOnClickListener(this);
    //
    // "alte" Farbe nicht setzen/anzeigen
    //
    setPickerPropertysFromConfig();
    setHasOptionsMenu(true);
  }

  private void setPickerPropertysFromConfig()
  {
//    if( btConfig != null && btConfig.isConnected() )
//    {
//      picker.setEnabled(true);
//    }
    picker.setEnabled(false);
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
    LinearLayout tempView;
    LinearLayout buttonsLayout;
    LinearLayout colorPickerLayout;

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
    colorPickerLayout = ( LinearLayout ) tempView.findViewById(R.id.colorPickerLayout);
    buttonsLayout = ( LinearLayout ) tempView.findViewById(R.id.buttonsLayout);
    //
    // Views in das Layout einfügen
    //
    tempView.removeAllViews();
    tempView.invalidate();
    rootView.addView(colorPickerLayout);
    rootView.addView(buttonsLayout);
    prepareRootView(rootView);
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
    picker.setColor(currColor);
  }


  @Override
  public void onColorChanged(int color)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "color changed to %08X!", color));
    }
    currColor = color;
    sendColor(color, !calToggleButton.isChecked());
  }

  @Override
  public void onColorSelected(int color)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "color selected to %08X!", color));
    }
    currColor = color;
    sendColor(color, !calToggleButton.isChecked());
  }


  /**
   * Called when a view has been clicked.
   *
   * @param clickedView The view that was clicked.
   */
  @Override
  public void onClick(View clickedView)
  {
    SharedPreferences pref;
    // Feedback geben
    clickedView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    //
    if( clickedView.getId() == R.id.RGBWToggleButton )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "toggle RGB <-> RGBW");
      }
    }
    else if( clickedView.getId() == R.id.colorWheelSaveColorButton )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "saveColorButton");
      }
      if( clickedView.equals(colorWheelSaveColorButton) )
      {
        // TODO: aktuelle Farbe als Predef setzen (Dialog öffnen)
        if( BuildConfig.DEBUG )
        {
          Log.i(TAG, "Button save color clicked");
        }
        pref = getActivity().getSharedPreferences(ProjectConst.COLOR_PREFS, Context.MODE_PRIVATE);
        ArrayList<Integer> cl = new ArrayList<>();
        cl.add(pref.getInt(ProjectConst.KEY_PREDEF_COLOR_01, 0xffa0a0a0));
        cl.add(pref.getInt(ProjectConst.KEY_PREDEF_COLOR_02, 0xffa0a0a0));
        cl.add(pref.getInt(ProjectConst.KEY_PREDEF_COLOR_03, 0xffa0a0a0));
        cl.add(pref.getInt(ProjectConst.KEY_PREDEF_COLOR_04, 0xffa0a0a0));
        cl.add(pref.getInt(ProjectConst.KEY_PREDEF_COLOR_05, 0xffa0a0a0));
        cl.add(pref.getInt(ProjectConst.KEY_PREDEF_COLOR_06, 0xffa0a0a0));
        Bundle arg = new Bundle();
        arg.putIntegerArrayList(PredefColorsFragment.ARG_PREFED_COLORLIST, cl);
        arg.putInt(PredefColorsFragment.ARG_CURRENT_COLOR, currColor);
        ColorPrefSaveDialog colDi = new ColorPrefSaveDialog();
        colDi.setArguments(arg);
        colDi.show(getActivity().getFragmentManager(), "savePredefColor");
      }
    }
    else
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "unknown view clicked");
      }
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
   * Die SeekBars nach dem RGBW Array setzen
   */
  private void setColorWheel(final short[] rgbw)
  {
    Log.e(TAG, "setColorWheel...");
    currColor = ((rgbw[ 0 ] << 16) | (rgbw[ 1 ] << 8) | (rgbw[ 2 ]));
    picker.setColor(currColor);
  }

  /**
   * Behandle ankommende Daten
   *
   * @param msg Nachricht mit eingeschlossenen Daten
   */
  @Override
  public void msgDataRecived(BlueThoothMessage msg)
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
          picker.post(new Runnable()
          {
            public void run()
            {
              setColorWheel(rgbw);
            }
          });
          return;

        //
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
    if( dialog instanceof ColorPrefSaveDialog )
    {
      SharedPreferences pref = getActivity().getSharedPreferences(ProjectConst.COLOR_PREFS, Context.MODE_PRIVATE);
      String key;
      int color;
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "dialog has selected color...");
      }
      switch( (( ColorPrefSaveDialog ) dialog).getSelectedColorItem() )
      {
        case 0:
          key = ProjectConst.KEY_PREDEF_COLOR_01;
          break;
        case 1:
          key = ProjectConst.KEY_PREDEF_COLOR_02;
          break;
        case 2:
          key = ProjectConst.KEY_PREDEF_COLOR_03;
          break;
        case 3:
          key = ProjectConst.KEY_PREDEF_COLOR_04;
          break;
        case 4:
          key = ProjectConst.KEY_PREDEF_COLOR_05;
          break;
        case 5:
        default:
          key = ProjectConst.KEY_PREDEF_COLOR_06;
      }
      SharedPreferences.Editor editor = pref.edit();
      color = (( ColorPrefSaveDialog ) dialog).getSettedColor();
      if( calToggleButton.isChecked() )
      {
        // RGBW
        color |= 0xff000000;
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, String.format(Locale.ENGLISH, "dialog has selected color RGBW %08X...", color));
        }
      }
      else
      {
        // RGB
        color &= 0xfeffffff;
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, String.format(Locale.ENGLISH, "dialog has selected color RGB %08X...", color));
        }
      }
      editor.putInt(key, color);
      if( editor.commit() )
      {
        if( BuildConfig.DEBUG )
        {
          Log.d(TAG, "onPositiveDialogFragment: wrote preferences to storage.");
        }
      }
      else
      {
        Log.e(TAG, "onPositiveDialogFragment: CAN'T wrote preferences to storage.");
      }
      dialog.dismiss();
    }
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
}
