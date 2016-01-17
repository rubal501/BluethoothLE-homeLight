/******************************************************************************
 * *
 * project: ANDROID                                                      *
 * module: btlehomelight                                                 *
 * class: SelectPagesAdapter                                             *
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

package de.dmarcini.bt.homelight.utils;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.Locale;

import de.dmarcini.bt.homelight.BuildConfig;
import de.dmarcini.bt.homelight.ProjectConst;
import de.dmarcini.bt.homelight.R;
import de.dmarcini.bt.homelight.fragments.BrightnessOnlyFragment;
import de.dmarcini.bt.homelight.fragments.ColorSelectFragment;
import de.dmarcini.bt.homelight.fragments.DirectControlFragment;
import de.dmarcini.bt.homelight.fragments.DiscoveringFragment;
import de.dmarcini.bt.homelight.fragments.PredefColorFragment;


/**
 * Ein intelligenten Adapter zur Verwaltung der Seiten
 */
public class SelectPagesAdapter extends FragmentStatePagerAdapter
{
  private static final String                TAG                 = SelectPagesAdapter.class.getSimpleName();
  private final        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
  private              Context               ctx                 = null;
  private              boolean[]             enabledPages        = new boolean[ ProjectConst.PAGE_COUNT ];
  private              int                   pagesCount          = ProjectConst.PAGE_COUNT;
  private int[]                pagesAvailvible;
  private BluetoothModulConfig btConfig;

  /**
   * Default Konstruktor
   *
   * @param fragmentManager
   */
  public SelectPagesAdapter(FragmentManager fragmentManager)
  {
    super(fragmentManager);
    //
    // Alle Seiten erst einmal erlauben
    //
    pagesAvailvible = new int[ ProjectConst.PAGE_COUNT ];
    for( int i = 0; i < ProjectConst.PAGE_COUNT; i++ )
    {
      // Erlaube Seite
      enabledPages[ i ] = true;
      pagesAvailvible[ i ] = i;
    }
  }

  /**
   * Konstruktor mit meinen Parametern
   *
   * @param fm       Fragment Manager
   * @param ctx      Context
   * @param btConfig die Bluethooth Konfigurationsdaten
   */
  public SelectPagesAdapter(FragmentManager fm, Context ctx, BluetoothModulConfig btConfig)
  {
    super(fm);
    this.ctx = ctx;
    this.btConfig = btConfig;
    //
    // Alle Seiten erst einmal erlauben
    //
    pagesAvailvible = new int[ ProjectConst.PAGE_COUNT ];
    for( int i = 0; i < ProjectConst.PAGE_COUNT; i++ )
    {
      // Erlaube Seite
      enabledPages[ i ] = true;
      pagesAvailvible[ i ] = i;
    }
  }

  /**
   * Konstruktor mit meinen Parametern
   *
   * @param fm       Fragment Manager
   * @param ctx      Context
   * @param btConfig die Bluethooth Konfigurationsdaten
   */
  public SelectPagesAdapter(FragmentManager fm, Context ctx, BluetoothModulConfig btConfig, boolean[] en)
  {
    super(fm);
    int pageIndex;
    this.ctx = ctx;
    this.btConfig = btConfig;
    this.enabledPages = en;
    //
    // zuest die aktiven Seiten zählen
    //
    pagesCount = 0;
    for( int i = 0; i < ProjectConst.PAGE_COUNT; i++ )
    {
      if( enabledPages[ i ] )
      {
        pagesCount++;
      }
    }
    //
    // jetzt die aktiven Seiten einrichten
    //
    pageIndex = 0;
    pagesAvailvible = new int[ pagesCount ];
    for( int i = 0; i < ProjectConst.PAGE_COUNT; i++ )
    {
      if( enabledPages[ i ] )
      {
        pagesAvailvible[ pageIndex++ ] = i;
      }
    }
    // FERTIG
  }


  @Override
  public Object instantiateItem(ViewGroup container, int position)
  {
    Fragment fragment = ( Fragment ) super.instantiateItem(container, position);
    registeredFragments.put(position, fragment);
    return fragment;
  }

  // Unregister when the item is inactive
  @Override
  public void destroyItem(ViewGroup container, int position, Object object)
  {
    registeredFragments.remove(position);
    super.destroyItem(container, position, object);
  }

  // Returns the fragment for the position (if instantiated)
  public Fragment getRegisteredFragment(int position)
  {
    Fragment retFrag;
    int      pageType = -1;
    //
    // erst mal der Position einen PageTyp zuweisen
    //
    pageType = pagesAvailvible[ position ];
    retFrag = registeredFragments.get(position);
    //
    // kein Fragment registriert: NULL zurück
    //
    if( retFrag == null )
    {
      return (null);
    }
    //
    // wenn der richtige Typ des Fragments da ist, zurückgeben
    //
    switch( pageType )
    {
      case ProjectConst.PAGE_DISCOVERING:
        if( retFrag instanceof DiscoveringFragment )
        {
          return (retFrag);
        }
        break;

      case ProjectConst.PAGE_DIRECT_CONTROL:
        if( retFrag instanceof DirectControlFragment )
        {
          return (retFrag);
        }
        break;

      case ProjectConst.PAGE_COLOR_WHEEL:
        if( retFrag instanceof ColorSelectFragment )
        {
          return (retFrag);
        }
        break;

      case ProjectConst.PAGE_BRIGHTNESS_ONLY:
        if( retFrag instanceof BrightnessOnlyFragment )
        {
          return (retFrag);
        }
        break;

      case ProjectConst.PAGE_PREDEF_COLORS:
        if( retFrag instanceof PredefColorFragment )
        {
          return (retFrag);
        }
        break;
    }
    return (null);
  }

  @Override
  public Fragment getItem(int position)
  {
    Fragment retFrag;
    int      pageType = -1;
    //
    // je nach Position gibt es dann eine Instanz eines Fragmentes
    //
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "getItem() => create Fragment Object");
    }
    //
    // erst mal der Position einen PageTyp zuweisen
    //
    pageType = pagesAvailvible[ position ];

    //
    // erst mal schauen, ob da was vorhanden ist
    //
    retFrag = registeredFragments.get(position);
    //
    // ist es der richtige Typ? Wenn ncht, neu erzeugen
    //
    switch( pageType )
    {
      case ProjectConst.PAGE_DISCOVERING:
        if( (retFrag == null) || (retFrag instanceof DiscoveringFragment) )
        {
          retFrag = DiscoveringFragment.newInstance(position, btConfig);
          registeredFragments.put(position, retFrag);
        }
        return (retFrag);

      case ProjectConst.PAGE_DIRECT_CONTROL:
        if( (retFrag == null) || (retFrag instanceof DirectControlFragment) )
        {
          retFrag = DirectControlFragment.newInstance(position, btConfig);
          registeredFragments.put(position, retFrag);
        }
        return (retFrag);

      case ProjectConst.PAGE_COLOR_WHEEL:
        if( (retFrag == null) || (retFrag instanceof ColorSelectFragment) )
        {
          retFrag = ColorSelectFragment.newInstance(position, btConfig);
          registeredFragments.put(position, retFrag);
        }
        return (retFrag);

      case ProjectConst.PAGE_BRIGHTNESS_ONLY:
        if( (retFrag == null) || (retFrag instanceof BrightnessOnlyFragment) )
        {
          retFrag = BrightnessOnlyFragment.newInstance(position, btConfig);
          registeredFragments.put(position, retFrag);
        }
        return (retFrag);

      case ProjectConst.PAGE_PREDEF_COLORS:
        if( (retFrag == null) || (retFrag instanceof PredefColorFragment) )
        {
          retFrag = PredefColorFragment.newInstance(position, btConfig);
          registeredFragments.put(position, retFrag);
        }
        return (retFrag);

      default:
        Log.e(TAG, "getItem() => position to high! return NULL");
        return (null);
    }
  }

  @Override
  public int getCount()
  {
    //
    // Zeige exakt so viele Seiten, wie ich will
    //
    return (pagesCount);
  }

  @Override
  public CharSequence getPageTitle(int position)
  {
    //
    // Falls ein TOP-Indikator existiert, wird hier der Titel geliefert
    //
    Locale l = Locale.getDefault();
    return ctx.getString(R.string.app_name).toUpperCase(l);
  }

  /*
   * We can access the selected page within the ViewPager at any time with the getCurrentItem method which returns the current page:
   *
   * vpPager.getCurrentItem(); // --> 2
   *
   * The current page can also be changed programmatically with the
   *
   * vpPager.setCurrentItem(2)
   */
}

