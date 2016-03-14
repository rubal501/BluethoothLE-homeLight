/******************************************************************************
 * *
 * project: ANDROID                                                      *
 * module: btlehomelight                                                 *
 * class: ColorPrefSaveDialog                                            *
 * date: 2016-01-08                                                      *
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

package de.dmarcini.bt.btlehomelight.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.ArrayList;

import de.dmarcini.bt.btlehomelight.BuildConfig;
import de.dmarcini.bt.btlehomelight.R;
import de.dmarcini.bt.btlehomelight.fragments.PredefColorsFragment;
import de.dmarcini.bt.btlehomelight.interfaces.INoticeDialogListener;
import de.dmarcini.bt.btlehomelight.views.ArrayAdapterWithColors;


/**
 * Created by dmarc on 07.01.2016.
 */
public class ColorPrefSaveDialog extends DialogFragment
{
  private final String TAG = ColorPrefSaveDialog.class.getSimpleName();
  private ArrayList<Integer> colorList;
  private int selectedColorItem = -1;
  private int       previewColor;
  private ImageView colorPreviewImageView;
  private View      rootView;
  // Use this instance of the interface to deliver action events
  private INoticeDialogListener mListener = null;

  public int getSettedColor()
  {
    return previewColor;
  }

  public int getSelectedColorItem()
  {
    return selectedColorItem;
  }

  public void setSelectedColorItem(int selectedColorItem)
  {
    this.selectedColorItem = selectedColorItem;
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
    try
    {
      Bundle arg = getArguments();
      colorList = arg.getIntegerArrayList(PredefColorsFragment.ARG_PREFED_COLORLIST);
      previewColor = arg.getInt(PredefColorsFragment.ARG_CURRENT_COLOR, 0xff606060);
    }
    catch( NullPointerException ex )
    {
      if( BuildConfig.DEBUG )
      {
        Log.e(TAG, "onCreateDialog: " + ex.getLocalizedMessage());
      }
    }
    //
    // Benutze die Builderklasse zum erstellen des Dialogs
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    // Get the layout inflater
    LayoutInflater inflater = getActivity().getLayoutInflater();
    // Inflate and set the layout for the dialog
    // Pass null as the parent view because its going in the dialog layout
    rootView = inflater.inflate(R.layout.fragment_dialog_predef_color, null);
    //
    // die vorhandenen Devices einfügen, natürlich
    //
    Spinner                deviceSpinner = ( Spinner ) rootView.findViewById(R.id.predefColorSpinner);
    ArrayAdapterWithColors ad            = new ArrayAdapterWithColors(getActivity(), 0, getActivity().getResources().getStringArray(R.array.colorSaveStringArray), colorList);
    //ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    deviceSpinner.setAdapter(ad);
    //
    // Vorschaufarbe setzen
    //
    colorPreviewImageView = ( ImageView ) rootView.findViewById(R.id.colorPreviewImageView);
    colorPreviewImageView.setBackgroundColor(previewColor);
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
        // was ist ausgewählt?
        Spinner deviceSpinner = ( Spinner ) rootView.findViewById(R.id.predefColorSpinner);
        selectedColorItem = deviceSpinner.getSelectedItemPosition();
        mListener.onDialogPositiveClick(ColorPrefSaveDialog.this);
      }
    });
    builder.setNegativeButton(R.string.color_dialog_cancel_button, new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int id)
      {
        // Abbruch!
        selectedColorItem = -1;
        mListener.onDialogNegativeClick(ColorPrefSaveDialog.this);
      }
    });
    // Create the AlertDialog object and return it
    return (builder.create());
  }

}
