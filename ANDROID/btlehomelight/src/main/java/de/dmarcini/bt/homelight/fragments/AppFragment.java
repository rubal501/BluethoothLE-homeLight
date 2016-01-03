/******************************************************************************
 * *
 * project: ANDROID                                                      *
 * module: btlehomelight                                                 *
 * class: AppFragment                                                    *
 * date: 2016-01-03                                                      *
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

package de.dmarcini.bt.homelight.fragments;

import android.bluetooth.BluetoothGattService;
import android.support.v4.app.Fragment;

import java.util.List;

import de.dmarcini.bt.homelight.interrfaces.IMainAppServices;
import de.dmarcini.bt.homelight.utils.BluetoothModulConfig;
import de.dmarcini.bt.homelight.utils.ProjectConst;

/**
 * Created by dmarc on 24.12.2015.
 */
public abstract class AppFragment extends Fragment
{
  protected final short[] rgbw = new short[ ProjectConst.C_ASKRGB_LEN - 1 ];
  protected IMainAppServices     mainService;
  protected BluetoothModulConfig btConfig;
  protected long                 timeToSend;

  public AppFragment()
  {
    mainService = ( IMainAppServices ) getActivity();
  }

  /**
   * Das BT Gerät wurde verbunden
   */
  public abstract void onBTConnected();

  /**
   * Das BT Gerät wurde getrennt
   */
  public abstract void onBTDisconnected();

  /**
   * Das BT Gerät meldet verfügbarte Services
   *
   * @param gattServices
   */
  public abstract void onBTServicesRecived(List<BluetoothGattService> gattServices);

  /**
   * Es kommen Daten vom BT Gerät
   *
   * @param data
   */
  public abstract void onBTDataAvaiable(String[] data);


  /**
   * Bluethooth Hintergrundservice verbunden!
   */
  public abstract void onServiceConnected();

  /**
   * Bluethooth Hintergrundservice getrennt
   */
  public abstract void onServiceDisconnected();

  /**
   * Page wurde selektiert
   */
  public abstract void onPageSelected();

}
