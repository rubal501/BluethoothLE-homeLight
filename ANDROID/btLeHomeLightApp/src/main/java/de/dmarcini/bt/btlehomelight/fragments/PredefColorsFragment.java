package de.dmarcini.bt.btlehomelight.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Locale;

import de.dmarcini.bt.btlehomelight.BuildConfig;
import de.dmarcini.bt.btlehomelight.ProjectConst;
import de.dmarcini.bt.btlehomelight.R;
import de.dmarcini.bt.btlehomelight.dialogs.ColorPrefChangeDialog;
import de.dmarcini.bt.btlehomelight.interfaces.IBtCommand;
import de.dmarcini.bt.btlehomelight.utils.BlueThoothMessage;

/**
 * Fragment nicht fertig gestellte Seiten
 */
public class PredefColorsFragment extends LightRootFragment implements View.OnClickListener, View.OnLongClickListener
{
  public static final  String ARG_PREFED_COLORLIST = "predef_color_list";
  public static final  String ARG_CURRENT_COLOR    = "current_color";
  public static final  String ARG_PREDEF_NR    = "predef_color_number";
  private static final String TAG                  = PredefColorsFragment.class.getSimpleName();
  private Button predefRedButton;
  private Button predefGreenButton;
  private Button predefBlueButton;
  private Button predefWhiteButton;
  private Button[] userButtons = new Button[ 6 ];

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
    rootView = inflater.inflate(R.layout.fragment_predef_colors, container, false);
    //
    //
    // Finde die Referenzen
    //
    predefRedButton = ( Button ) rootView.findViewById(R.id.predefRedButton);
    predefGreenButton = ( Button ) rootView.findViewById(R.id.predefGreenButton);
    predefBlueButton = ( Button ) rootView.findViewById(R.id.predefBlueButton);
    predefWhiteButton = ( Button ) rootView.findViewById(R.id.predefWhiteButton);
    userButtons[ 0 ] = ( Button ) rootView.findViewById(R.id.predefUserButton01);
    userButtons[ 1 ] = ( Button ) rootView.findViewById(R.id.predefUserButton02);
    userButtons[ 2 ] = ( Button ) rootView.findViewById(R.id.predefUserButton03);
    userButtons[ 3 ] = ( Button ) rootView.findViewById(R.id.predefUserButton04);
    userButtons[ 4 ] = ( Button ) rootView.findViewById(R.id.predefUserButton05);
    userButtons[ 5 ] = ( Button ) rootView.findViewById(R.id.predefUserButton06);
    //
    // Event Handler setzen
    //
    predefRedButton.setOnClickListener(this);
    predefGreenButton.setOnClickListener(this);
    predefBlueButton.setOnClickListener(this);
    predefWhiteButton.setOnClickListener(this);
    for( int i = 0; i < userButtons.length; i++ )
    {
      userButtons[ i ].setOnClickListener(this);
      userButtons[ i ].setOnLongClickListener(this);
      userButtons[ i ].setTag(new Integer(i)); // Gib dem Button seine ID-Nummer für die App mit
    }
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

  }

  /**
   * Reaktion aufgerufener Dialoge POSITIV
   *
   * @param dialog der Dialog, welcher aufrief
   */
  @Override
  public void onDialogPositiveClick(DialogFragment dialog)
  {
    int prefColorNumber = -1;
    int newColor;

    if( dialog instanceof ColorPrefChangeDialog )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "dialog has changed predefined color...");
      }
      prefColorNumber = (( ColorPrefChangeDialog ) dialog).getPredefColorNumber();
      newColor = (( ColorPrefChangeDialog ) dialog).getSettedColor();
      if( prefColorNumber > -1 || prefColorNumber < 6 )
      {
        userButtons[ prefColorNumber ].setBackgroundColor(newColor);
      }
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

  /**
   * Called when a view has been clicked.
   *
   * @param colorButton The view that was clicked.
   */
  @Override
  public void onClick(View colorButton)
  {
    int toSetColor;

    colorButton.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    if( colorButton instanceof Button )
    {
      toSetColor = (( ColorDrawable ) colorButton.getBackground()).getColor();
      onColorSelected(toSetColor);
    }
    Log.v(TAG, "button clicked!");

  }

  @Override
  public void onPause()
  {
    super.onPause();
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onPause...");
    }
    writeColorsToPref();
  }

  @Override
  public void onResume()
  {
    super.onResume();
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onResume...");
    }
    readColorsFromPref();
  }


  /**
   * Called when a view has been clicked and held.
   *
   * @param clickedView The view that was clicked and held.
   * @return true if the callback consumed the long click, false otherwise.
   */
  @Override
  public boolean onLongClick(View clickedView)
  {
    clickedView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    //
    // Ist das ein Button, hat er einen Tag mit Index BUTTON_TAGID und ist dieser Integer
    //
    if( clickedView instanceof Button && clickedView.getTag() != null && clickedView.getTag() instanceof Integer )
    {
      Integer val = ( Integer ) clickedView.getTag();
      // Ist der Wert bestimmt?
      if( val > -1 )
      {
        if( BuildConfig.DEBUG )
        {
          Log.i(TAG, "Button predef color clicked");
        }
        ColorPrefChangeDialog colDi = new ColorPrefChangeDialog();
        Bundle arg = new Bundle();
        arg.putInt(ARG_CURRENT_COLOR, (( ColorDrawable ) clickedView.getBackground()).getColor());
        arg.putInt(ARG_PREDEF_NR, val);
        colDi.setArguments(arg);
        colDi.show(getActivity().getFragmentManager(), "changePredefColor");
      }
    }
    return false;
  }

  private void readColorsFromPref()
  {
    SharedPreferences pref;
    //
    pref = getActivity().getSharedPreferences(ProjectConst.COLOR_PREFS, Context.MODE_PRIVATE);
    //
    // die alphawerte auf ff oder fe setzen,
    // 0xfe bedeutet RGBW mode, 0xff RGB
    //
    userButtons[ 0 ].setBackgroundColor(pref.getInt(ProjectConst.KEY_PREDEF_COLOR_01, 0xffa0a0a0) | 0xfe000000);
    userButtons[ 1 ].setBackgroundColor(pref.getInt(ProjectConst.KEY_PREDEF_COLOR_02, 0xffa0a0a0) | 0xfe000000);
    userButtons[ 2 ].setBackgroundColor(pref.getInt(ProjectConst.KEY_PREDEF_COLOR_03, 0xffa0a0a0) | 0xfe000000);
    userButtons[ 3 ].setBackgroundColor(pref.getInt(ProjectConst.KEY_PREDEF_COLOR_04, 0xffa0a0a0) | 0xfe000000);
    userButtons[ 4 ].setBackgroundColor(pref.getInt(ProjectConst.KEY_PREDEF_COLOR_05, 0xffa0a0a0) | 0xfe000000);
    userButtons[ 5 ].setBackgroundColor(pref.getInt(ProjectConst.KEY_PREDEF_COLOR_06, 0xffa0a0a0) | 0xfe000000);
  }

  private void writeColorsToPref()
  {
    SharedPreferences        pref;
    SharedPreferences.Editor editor;
    //
    pref = getActivity().getSharedPreferences(ProjectConst.COLOR_PREFS, Context.MODE_PRIVATE);
    editor = pref.edit();
    //
    // voreinstellungen sichern
    //
    editor.putInt(ProjectConst.KEY_PREDEF_COLOR_01, (( ColorDrawable ) userButtons[ 0 ].getBackground()).getColor());
    editor.putInt(ProjectConst.KEY_PREDEF_COLOR_02, (( ColorDrawable ) userButtons[ 1 ].getBackground()).getColor());
    editor.putInt(ProjectConst.KEY_PREDEF_COLOR_03, (( ColorDrawable ) userButtons[ 2 ].getBackground()).getColor());
    editor.putInt(ProjectConst.KEY_PREDEF_COLOR_04, (( ColorDrawable ) userButtons[ 3 ].getBackground()).getColor());
    editor.putInt(ProjectConst.KEY_PREDEF_COLOR_05, (( ColorDrawable ) userButtons[ 4 ].getBackground()).getColor());
    editor.putInt(ProjectConst.KEY_PREDEF_COLOR_06, (( ColorDrawable ) userButtons[ 5 ].getBackground()).getColor());
    //
    // und absenden
    //
    if( editor.commit() )
    {
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "writeColorsToPref: wrote preferences to storeage.");
      }
    }
    else
    {
      Log.e(TAG, "writeColorsToPref: CAN'T wrote preferences to storage.");
    }

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

  public void onColorSelected(int color)
  {
    short[] rgbw = new short[ 4 ];
    //
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "color selected to %08X", color));
    }
    // Werte verschicken
    rgbw[ 0 ] = ( short ) ((color >> 16) & 0xff);
    rgbw[ 1 ] = ( short ) ((color >> 8) & 0xff);
    rgbw[ 2 ] = ( short ) (color & 0xff);
    rgbw[ 3 ] = 0;
    //
    if( runningActivity != null )
    {
      // 0xfe000000 bedeutet RGBW mode, 0xff000000 RGB
      if( (color & 0x01000000) > 0 )
      {
        // RGB Mode
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, String.format(Locale.ENGLISH, "set selected color RGB %08X...", color));
        }
        runningActivity.setModulRawRGBW(rgbw);
      }
      else
      {
        // RGBW Mode
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, String.format(Locale.ENGLISH, "set selected color RGBW %08X...", color));
        }
        runningActivity.setModulRGB4Calibrate(rgbw);
      }
    }
  }

}
