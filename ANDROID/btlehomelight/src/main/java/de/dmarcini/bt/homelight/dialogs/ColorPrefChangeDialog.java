/******************************************************************************
 *                                                                            *
 *      project: ANDROID                                                      *
 *      module: btlehomelight                                                 *
 *      class: ColorPrefChangeDialog                                          *
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

package de.dmarcini.bt.homelight.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Locale;

import de.dmarcini.bt.homelight.BuildConfig;
import de.dmarcini.bt.homelight.R;
import de.dmarcini.bt.homelight.interrfaces.IMainAppServices;
import de.dmarcini.bt.homelight.interrfaces.INoticeDialogListener;
import de.dmarcini.bt.homelight.ProjectConst;
import de.dmarcini.bt.homelight.views.ColorPicker;

/**
 * Created by dmarc on 07.01.2016.
 */
public class ColorPrefChangeDialog extends DialogFragment implements ColorPicker.OnColorChangedListener, ColorPicker.OnColorSelectedListener, View.OnClickListener
{
  private final String  TAG  = ColorPrefChangeDialog.class.getSimpleName();
  private final short[] rgbw = new short[ ProjectConst.C_ASKRGB_LEN - 1 ];
  private IMainAppServices mainService;
  private int              predefColor;
  private int              predefColorNumber;
  private long             timeToSend;
  private boolean          isRGBW;
  private View             rootView;
  private ColorPicker      picker;
  private ToggleButton     calToggleButton;
  // Use this instance of the interface to deliver action events
  private INoticeDialogListener mListener = null;

  public int getPredefColorNumber()
  {
    return predefColorNumber;
  }

  public int getSettedColor()
  {
    return predefColor;
  }

  // Überschreibe onAttach für meine Zwecke mit dem Listener
  @Override
  public void onAttach(Activity activity)
  {
    super.onAttach(activity);
    // Implementiert die Activity den Listener?
    try
    {
      // Instanziere den Listener, wenn möglich, ansonsten wirft das eine exception
      mListener = ( INoticeDialogListener ) activity;
    }
    catch( ClassCastException ex )
    {
      // Die activity implementiert den Listener nicht, werfe eine Exception
      throw new ClassCastException(activity.toString() + " must implement INoticeDialogListener");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    timeToSend = System.currentTimeMillis();
    try
    {
      Bundle arg = getArguments();
      predefColor = arg.getInt(ProjectConst.ARG_PREVIEW_COLOR, 0xff606060);
      predefColorNumber = arg.getInt(ProjectConst.ARG_PREDEF_NUMBER, -1);
      rgbw[ 0 ] = ( short ) ((predefColor >> 16) & 0xff);
      rgbw[ 1 ] = ( short ) ((predefColor >> 8) & 0xff);
      rgbw[ 2 ] = ( short ) (predefColor & 0xff);
      rgbw[ 3 ] = 0;
    }
    catch( NullPointerException ex )
    {
      if( BuildConfig.DEBUG )
      {
        Log.e(TAG, "onCreateDialog: " + ex.getLocalizedMessage());
      }
    }
    //
    // Callback für ColorWheel möglich machen
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
    //
    // Benutze die Builderklasse zum erstellen des Dialogs
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    // Get the layout inflater
    LayoutInflater inflater = getActivity().getLayoutInflater();
    // Inflate and set the layout for the dialog
    // Pass null as the parent view because its going in the dialog layout
    rootView = inflater.inflate(R.layout.fragment_dialog_predef_colors_change, null);
    //
    // Die Referenzen erfragen
    //
    picker = ( ColorPicker ) rootView.findViewById(R.id.colorPicker);
    calToggleButton = ( ToggleButton ) rootView.findViewById(R.id.RGBWToggleButton);
    TextView colorPrefChangeHeadline = ( TextView ) rootView.findViewById(R.id.colorPrefChangeHeadline);
    colorPrefChangeHeadline.setText(String.format(Locale.ENGLISH, getResources().getString(R.string.color_change_dialog_headline), predefColorNumber));
    //
    // ist die Farbe RGBW oder RGB
    // 0xfe000000 bedeutet RGBW mode, 0xff000000 RGB
    //
    if( (predefColor & 0x01000000) > 0 )
    {
      // RGB Mode
      calToggleButton.setChecked(false);
      isRGBW = false;
    }
    else
    {
      // RGBW Mode
      calToggleButton.setChecked(true);
      isRGBW = true;
    }
    calToggleButton.setOnClickListener(this);
    picker.setColor(predefColor | 0xff000000);
    picker.setOnColorChangedListener(this);
    picker.setOnColorSelectedListener(this);
    //
    // jetzt dem Builder das View übergeben
    //
    builder.setView(rootView);
    // Buttons erzeugen
    builder.setPositiveButton(R.string.color_dialog_save_button, new DialogInterface.OnClickListener()
    {
      @SuppressWarnings( "unchecked" )
      @Override
      public void onClick(DialogInterface dialog, int id)
      {
        //
        // erst mal zum Modul senden
        //
        rgbw[ 0 ] = ( short ) ((predefColor >> 16) & 0xff);
        rgbw[ 1 ] = ( short ) ((predefColor >> 8) & 0xff);
        rgbw[ 2 ] = ( short ) (predefColor & 0xff);
        rgbw[ 3 ] = 0;
        onColorChanged(predefColor);
        // was ist ausgewählt?
        if( isRGBW )
        {
          //RGBW Mode
          predefColor &= 0xfeffffff;
        }
        else
        {
          //RGB Mode
          predefColor |= 0xff000000;
        }

        mListener.onDialogPositiveClick(ColorPrefChangeDialog.this);
      }
    });
    builder.setNegativeButton(R.string.color_dialog_cancel_button, new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int id)
      {
        // Abbruch!
        mListener.onDialogNegativeClick(ColorPrefChangeDialog.this);
      }
    });
    // Create the AlertDialog object and return it
    return (builder.create());
  }

  @Override
  public void onColorChanged(int color)
  {
    predefColor = color;
    // Werte verschicken
    rgbw[ 0 ] = ( short ) ((color >> 16) & 0xff);
    rgbw[ 1 ] = ( short ) ((color >> 8) & 0xff);
    rgbw[ 2 ] = ( short ) (color & 0xff);
    rgbw[ 3 ] = 0;
    //
    if( timeToSend < System.currentTimeMillis() && mainService != null )
    {
      onColorSelected(color);
    }
  }

  @Override
  public void onColorSelected(int color)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "color selected to %08X", color));
    }
    predefColor = color;
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

  /**
   * Called when a view has been clicked.
   *
   * @param clickedView The view that was clicked.
   */
  @Override
  public void onClick(View clickedView)
  {
    clickedView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    //
    if( clickedView instanceof ToggleButton )
    {
      if( clickedView.equals(calToggleButton) )
      {
        if( (( ToggleButton ) clickedView).isChecked() )
        {
          isRGBW = true;
          if( BuildConfig.DEBUG )
          {
            Log.i(TAG, "Button CHECKED");
          }
        }
        else
        {
          isRGBW = false;
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
