/******************************************************************************
 *                                                                            *
 *      project: ANDROID                                                      *
 *      module: btlehomelight                                                 *
 *      class: PredefColorFragment                                            *
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
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;
import java.util.Locale;

import de.dmarcini.bt.homelight.BuildConfig;
import de.dmarcini.bt.homelight.ProjectConst;
import de.dmarcini.bt.homelight.R;
import de.dmarcini.bt.homelight.dialogs.ColorPrefChangeDialog;
import de.dmarcini.bt.homelight.interrfaces.IFragmentInterface;
import de.dmarcini.bt.homelight.interrfaces.IMainAppServices;
import de.dmarcini.bt.homelight.utils.BluetoothModulConfig;


/**
 * Created by dmarc on 22.08.2015.
 */
public class PredefColorFragment extends AppFragment implements IFragmentInterface, View.OnClickListener, View.OnLongClickListener
{
  private static String TAG = PredefColorFragment.class.getSimpleName();
  private Button predefRedButton;
  private Button predefGreenButton;
  private Button predefBlueButton;
  private Button predefWhiteButton;
  private Button[] userButtons = new Button[ 6 ];

  public PredefColorFragment()
  {
    super();
  }

  /**
   * Returns a new instance of this fragment for the given section
   * number.
   */
  public static PredefColorFragment newInstance(int sectionNumber, BluetoothModulConfig btConfig)
  {
    PredefColorFragment fragment = new PredefColorFragment();
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
    View rootView = inflater.inflate(R.layout.fragment_predef_colors, container, false);
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
    // Fertich!
    return (rootView);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreateOptionsMenu...");
    }
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
  public void onBTDataAvaiable(String[] data)
  {

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
    Log.d(TAG, "Page PREDEFCOLOR was selected");
    if( mainServiceRef == null )
    {
      Log.e(TAG, "can't set Callback handler to APP");
      return;
    }
    mainServiceRef.setHandler(this);
  }

  /**
   * Der Dialog hat eine Positive Antwort
   *
   * @param frag Das Fragment( der Dialog )
   */
  @Override
  public void onPositiveDialogFragment(DialogFragment frag)
  {
    int prefColorNumber = -1;
    int newColor;

    if( frag instanceof ColorPrefChangeDialog )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "dialog has changed predefined color...");
      }
      prefColorNumber = (( ColorPrefChangeDialog ) frag).getPredefColorNumber();
      newColor = (( ColorPrefChangeDialog ) frag).getSettedColor();
      if( prefColorNumber > -1 || prefColorNumber < 6 )
      {
        userButtons[ prefColorNumber ].setBackgroundColor(newColor);
      }
    }
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
   * Wenn die Farbbuttons geklickt werden
   *
   * @param colorButton Der Farbbutton
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

  /**
   * Button lange gehalten wärend des Clickens
   *
   * @param clickedView Der Knopf, welcher gedrückt war
   * @return true if the callback consumed the long click, false otherwise.
   */
  @Override
  public boolean onLongClick(View clickedView)
  {
    clickedView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    //
    // Ist das ein Button, hat er einen Tag mit Index CCD_BUTTON_TAGID und ist dieser Integer
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
        arg.putInt(ProjectConst.ARG_PREVIEW_COLOR, (( ColorDrawable ) clickedView.getBackground()).getColor());
        arg.putInt(ProjectConst.ARG_PREDEF_NUMBER, val);
        colDi.setArguments(arg);
        colDi.show(getActivity().getFragmentManager(), "changePredefColor");
      }
    }
    return false;
  }

  public void onColorSelected(int color)
  {

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
    if( mainServiceRef != null )
    {
      // 0xfe000000 bedeutet RGBW mode, 0xff000000 RGB
      if( (color & 0x01000000) > 0 )
      {
        // RGB Mode
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, String.format(Locale.ENGLISH, "set selected color RGB %08X...", color));
        }
        mainServiceRef.setModulRawRGBW(rgbw);
      }
      else
      {
        // RGBW Mode
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, String.format(Locale.ENGLISH, "set selected color RGBW %08X...", color));
        }
        mainServiceRef.setModulRGB4Calibrate(rgbw);
      }
    }
  }

}
