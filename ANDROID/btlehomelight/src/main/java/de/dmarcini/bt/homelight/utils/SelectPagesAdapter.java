/******************************************************************************
 *                                                                            *
 *      project: ANDROID                                                      *
 *      module: btlehomelight                                                 *
 *      class: SelectPagesAdapter                                             *
 *      date: 2016-01-03                                                      *
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

package de.dmarcini.bt.homelight.utils;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.Locale;

import de.dmarcini.bt.homelight.BuildConfig;
import de.dmarcini.bt.homelight.R;
import de.dmarcini.bt.homelight.fragments.ColorSelectFragment;
import de.dmarcini.bt.homelight.fragments.DirectControlFragment;
import de.dmarcini.bt.homelight.fragments.DiscoveringFragment;


/**
 * Created by dmarc on 22.08.2015.
 */
public class SelectPagesAdapter extends FragmentPagerAdapter
{
  private static final String  TAG = SelectPagesAdapter.class.getSimpleName();
  private final Fragment fragmentsArray[] = new Fragment[ ProjectConst.PAGE_COUNT ];
  private              Context ctx = null;
  private BluetoothConfig btConfig;

  public SelectPagesAdapter(FragmentManager fm, Context ctx, BluetoothConfig btConfig)
  {
    super(fm);
    this.ctx = ctx;
    this.btConfig = btConfig;
  }

  @Override
  public Fragment getItem(int position)
  {
    //
    // zunächst schauen wir mal, ob das Fragment schon erzeugt wurde
    //
    try
    {
      if( fragmentsArray[ position ] != null )
      {
        //
        // ich habe das Fragment schon
        //
        if( BuildConfig.DEBUG )Log.v(TAG, "getItem() => recycle Fragment Object");
        return (fragmentsArray[ position ]);
      }
    }
    catch( IndexOutOfBoundsException ex )
    {
      Log.e(TAG, "getItem() => index to high! IGNORE");
    }
    //
    // je nach Position gibt es dann eine Instanz eines Fragmentes
    //
    if( BuildConfig.DEBUG )Log.v(TAG, "getItem() => create Fragment Object");
    try
    {
      switch( position )
      {
        case ProjectConst.PAGE_DISCOVERING:
          fragmentsArray[ position ] = DiscoveringFragment.newInstance(position, btConfig);
          return (fragmentsArray[ position ]);
        //return (DiscoveringFragment.newInstance(position++, btConfig));

        case ProjectConst.PAGE_DIRECT_CONTROL:
          fragmentsArray[ position ] = DirectControlFragment.newInstance(position, btConfig);
          return (fragmentsArray[ position ]);
        //return (DirectControlFragment.newInstance(position++, btConfig));

        case ProjectConst.PAGE_COLOR_CIRCLE:
          fragmentsArray[ position ] = ColorSelectFragment.newInstance(position, btConfig);
          return (fragmentsArray[ position ]);
        //return (ColorSelectFragment.newInstance(position++, btConfig));

        default:
          Log.e(TAG, "getItem() => position to high! return NULL");
          return (null);
      }
    }
    catch( IndexOutOfBoundsException ex )
    {
      Log.e(TAG, "getItem() => index to high! IGNORE");
      return (null);
    }
  }

  @Override
  public int getCount()
  {
    //
    // Zeige exakt so viele Seiten, wie ich will
    //
    return (ProjectConst.PAGE_COUNT);
  }

  @Override
  public CharSequence getPageTitle(int position)
  {
    Locale l = Locale.getDefault();
    switch( position )
    {
      case 0:
        return ctx.getString(R.string.discovering_headline).toUpperCase(l);
      case 1:
      default:
        return ctx.getString(R.string.discovering_headline).toUpperCase(l);
    }
  }
}
