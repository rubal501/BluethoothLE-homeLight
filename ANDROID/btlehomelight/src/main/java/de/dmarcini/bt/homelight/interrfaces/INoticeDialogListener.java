/******************************************************************************
 *                                                                            *
 *      project: ANDROID                                                      *
 *      module: btlehomelight                                                 *
 *      class: INoticeDialogListener                                          *
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

//@formatter:off
//@formatter:on
package de.dmarcini.bt.homelight.interrfaces;

import android.app.DialogFragment;

/**
 * Die aufrufende App muss das Interface implementieren
 * <p/>
 * Project: SubmatixBTLoggerAndroid Package: de.dmarcini.submatix.android4.utils
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 *         <p/>
 *         Stand: 02.11.2012
 */
public interface INoticeDialogListener
{
  @SuppressWarnings( "javadoc" )
  void onDialogPositiveClick(DialogFragment dialog);

  @SuppressWarnings( "javadoc" )
  void onDialogNegativeClick(DialogFragment dialog);
}
