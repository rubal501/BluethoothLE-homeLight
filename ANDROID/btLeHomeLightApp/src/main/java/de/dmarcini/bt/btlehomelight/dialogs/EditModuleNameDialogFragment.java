/******************************************************************************
 * *
 * project: ANDROID                                                      *
 * module: btlehomelight                                                 *
 * class: EditModuleNameDialogFragment                                   *
 * date: 2016-01-14                                                      *
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

//@formatter:off
//@formatter:on
package de.dmarcini.bt.btlehomelight.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import de.dmarcini.bt.btlehomelight.BuildConfig;
import de.dmarcini.bt.btlehomelight.R;
import de.dmarcini.bt.btlehomelight.interfaces.INoticeDialogListener;

/**
 * Dialog zum editieren des Modulnamens
 */
public class EditModuleNameDialogFragment extends DialogFragment
{
  public final static  String                MODULNAME  = "module_name";
  private static final String                TAG        = EditModuleNameDialogFragment.class.getSimpleName();
  private              String                moduleName = null;
  private              INoticeDialogListener mListener  = null;
  private View rootView;

  /**
   * Gib den editierten Modulnamen zurück
   *
   * @return der Name als String
   */
  public String getModuleName()
  {
    return moduleName;
  }


  @Override
  public Dialog onCreateDialog(Bundle bundle)
  {
    moduleName = getArguments().getString(MODULNAME, "unknown");
    //
    // Benutze die Builderklasse zum erstellen des Dialogs
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    // Get the layout inflater
    LayoutInflater inflater = getActivity().getLayoutInflater();
    // Inflate and set the layout for the dialog
    // Pass null as the parent view because its going in the dialog layout
    rootView = inflater.inflate(R.layout.fragment_dialog_module_name_edit, null);
    //
    // die Texte einfügen, natürlich
    //
    // das wird ein editierbarer Text!
    EditText ed = ( EditText ) rootView.findViewById(R.id.moduleChangeNameEditTextView);
    ed.setText(moduleName, TextView.BufferType.EDITABLE);
    ed.selectAll();
    //
    // jetzt dem Builder das View übergeben
    //
    builder.setView(rootView);
    // Buttons erzeugen
    builder.setPositiveButton(R.string.module_name_change_ok_button, new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int id)
      {
        EditText ed;
        // Gib in der App bescheid, ich will es so!
        ed = ( EditText ) rootView.findViewById(R.id.moduleChangeNameEditTextView);
        moduleName = ed.getText().toString();
        mListener.onDialogPositiveClick(EditModuleNameDialogFragment.this);
      }
    });
    builder.setNegativeButton(R.string.module_name_change_cancel_button, new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int id)
      {
        // Abbruch!
        mListener.onDialogNegativeClick(EditModuleNameDialogFragment.this);
      }
    });
    // Create the AlertDialog object and return it
    return (builder.create());
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

  // Überschreibe show fürs debugging
  @Override
  public void show(FragmentManager manager, String tag)
  {
    super.show(manager, tag);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "show(manager,tag)...");
    }
  }

}
