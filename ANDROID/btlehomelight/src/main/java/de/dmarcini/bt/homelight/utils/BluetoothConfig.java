/*
 * //@formatter:off
 *
 *     ANDROID
 *     btlehomelight
 *     BluetoothConfig
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

package de.dmarcini.bt.homelight.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;

import de.dmarcini.bt.homelight.service.BluetoothLowEnergyService;

/**
 * Objekt zum halten der aktuellen Bluethoot-relevanten Daten
 */
public class BluetoothConfig
{
  private BluetoothAdapter            mBluetoothAdapter;
  private BluetoothLowEnergyService   mBluetoothLeService;
  private String                      mDeviceName;
  private String                      mDeviceAddress;
  private String                      mModuleType;
  private boolean                     mConnected;
  private boolean                     mIsUART;
  private BroadcastReceiver           mGattUpdateReceiver;
  private BluetoothGattCharacteristic characteristicTX;
  private BluetoothGattCharacteristic characteristicRX;

  public BluetoothConfig()
  {
    mConnected = false;
    mIsUART = false;
  }

  public boolean isUART()
  {
    return mIsUART;
  }

  public void setIsUART(boolean mIsUART)
  {
    this.mIsUART = mIsUART;
  }

  public BluetoothGattCharacteristic getCharacteristicTX()
  {
    return characteristicTX;
  }

  public void setCharacteristicTX(BluetoothGattCharacteristic characteristicTX)
  {
    this.characteristicTX = characteristicTX;
  }

  public BluetoothGattCharacteristic getCharacteristicRX()
  {
    return characteristicRX;
  }

  public void setCharacteristicRX(BluetoothGattCharacteristic characteristicRX)
  {
    this.characteristicRX = characteristicRX;
  }

  public BroadcastReceiver getGattUpdateReceiver()
  {
    return mGattUpdateReceiver;
  }

  public void setGattUpdateReceiver(BroadcastReceiver mGattUpdateReceiver)
  {
    this.mGattUpdateReceiver = mGattUpdateReceiver;
  }

  public String getDeviceName()
  {
    return mDeviceName;
  }

  public void setDeviceName(String mDeviceName)
  {
    this.mDeviceName = mDeviceName;
  }

  public String getDeviceAddress()
  {
    return mDeviceAddress;
  }

  public void setDeviceAddress(String mDeviceAddress)
  {
    this.mDeviceAddress = mDeviceAddress;
  }

  public boolean isConnected()
  {
    return mConnected;
  }

  public void setConnected(boolean mConnected)
  {
    this.mConnected = mConnected;
    if( !mConnected )
    {
      characteristicTX = null;
      characteristicRX = null;
      mModuleType = null;
      mIsUART = false;
    }
  }

  public void setBluetoothAdapter(BluetoothAdapter btAdapter)
  {
    mBluetoothAdapter = btAdapter;
    if( btAdapter == null )
    {
      setConnected(false);
    }
  }

  public BluetoothAdapter getBluethoothAdapter()
  {
    return (mBluetoothAdapter);
  }


  public BluetoothLowEnergyService getBluetoothService()
  {
    return mBluetoothLeService;
  }

  public void setBluetoothService(BluetoothLowEnergyService mBluetoothLeService)
  {
    this.mBluetoothLeService = mBluetoothLeService;
  }

  public String getModuleType()
  {
    return mModuleType;
  }

  public void setModuleType(String mModuleType)
  {
    this.mModuleType = mModuleType;
  }


}
