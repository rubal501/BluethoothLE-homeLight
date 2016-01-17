/******************************************************************************
 *                                                                            *
 *      project: ANDROID                                                      *
 *      module: btlehomelight                                                 *
 *      class: AppFragment                                                    *
 *      date: 2016-01-08                                                      *
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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.Locale;

import de.dmarcini.bt.homelight.BuildConfig;
import de.dmarcini.bt.homelight.ProjectConst;
import de.dmarcini.bt.homelight.interrfaces.IMainAppServices;
import de.dmarcini.bt.homelight.utils.BluetoothModulConfig;

/**
 * die Schablone für Fragmente der App
 */
public class AppFragment extends Fragment
{
  private static String TAG = AppFragment.class.getSimpleName();
  protected final short[] rgbw = new short[ ProjectConst.C_ASKRGB_LEN - 1 ];
  protected IMainAppServices mainServiceRef;
  protected BluetoothModulConfig btConfig;
  protected long                 timeToSend;

  @Override
  public void onCreate(Bundle args)
  {
    int pos;
    //
    super.onCreate(args);
    //
    try
    {
      if( args != null )
      {
        pos = args.getInt(ProjectConst.ARG_SECTION_NUMBER, 0);
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, String.format(Locale.ENGLISH, "onCreate: id is %04d", pos));
        }
      }
    }
    catch( NullPointerException ex )
    {
      if( BuildConfig.DEBUG )
      {
        Log.e(TAG, "onCreate: " + ex.getLocalizedMessage());
      }
    }
  }


}
