/*
 * //@formatter:off
 *
 *     ANDROID
 *     btlehomelight
 *     AppFragment
 *     2016-01-02
 *     Copyright (C) 2016  Dirk Marciniak
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/
 * /
 * //@formatter:on
 */

package de.dmarcini.bt.homelight.fragments;

import android.support.v4.app.Fragment;

/**
 * Created by dmarc on 24.12.2015.
 */
public class AppFragment extends Fragment
{
  private boolean shouldNewCreated = false;

  public boolean isShouldNewCreated()
  {
    return shouldNewCreated;
  }

  public void setShouldNewCreated(boolean shouldNewCreated)
  {
    this.shouldNewCreated = shouldNewCreated;
  }


}
