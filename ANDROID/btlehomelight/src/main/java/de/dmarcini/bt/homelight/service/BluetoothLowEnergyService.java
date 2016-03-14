/******************************************************************************
 * *
 * project: ANDROID                                                      *
 * module: btlehomelight                                                 *
 * class: BluetoothLowEnergyService                                      *
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

package de.dmarcini.bt.homelight.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import de.dmarcini.bt.homelight.BuildConfig;
import de.dmarcini.bt.homelight.ProjectConst;
import de.dmarcini.bt.homelight.utils.HM10GattAttributes;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLowEnergyService extends Service
{
  private final static String  TAG                             = BluetoothLowEnergyService.class.getSimpleName();
  public final static  String  ACTION_GATT_CONNECTED           = "de.dmarcini.bt.homelight.service.ACTION_GATT_CONNECTED";
  public final static  String  ACTION_GATT_DISCONNECTED        = "de.dmarcini.bt.homelight.service.ACTION_GATT_DISCONNECTED";
  public final static  String  ACTION_GATT_SERVICES_DISCOVERED = "de.dmarcini.bt.homelight.service.ACTION_GATT_SERVICES_DISCOVERED";
  public final static  String  ACTION_DATA_AVAILABLE           = "de.dmarcini.bt.homelight.service.ACTION_DATA_AVAILABLE";
  public final static  String  EXTRA_DATA                      = "de.dmarcini.bt.homelight.service.EXTRA_DATA";
  private static final int     STATE_DISCONNECTED              = 0;
  private static final int     STATE_CONNECTING                = 1;
  private static final int     STATE_CONNECTED                 = 2;
  private final        IBinder mBinder                         = new LocalBinder();
  private              int     mConnectionState                = STATE_DISCONNECTED;
  private BluetoothManager mBluetoothManager;
  private BluetoothAdapter mBluetoothAdapter;
  private String           mBluetoothDeviceAddress;
  private BluetoothGatt    mBluetoothGatt;
  // Implements callback methods for GATT events that the app cares about.  For example,
  // connection change and services discovered.
  private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
  {
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
    {
      String intentAction;
      if( newState == BluetoothProfile.STATE_CONNECTED )
      {
        intentAction = ACTION_GATT_CONNECTED;
        mConnectionState = STATE_CONNECTED;
        broadcastUpdate(intentAction);
        if( BuildConfig.DEBUG )
        {
          Log.i(TAG, "Connected to GATT server.");
        }
        // Attempts to discover services after successful connection.
        if( BuildConfig.DEBUG )
        {
          Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
        }

      }
      else if( newState == BluetoothProfile.STATE_DISCONNECTED )
      {
        intentAction = ACTION_GATT_DISCONNECTED;
        mConnectionState = STATE_DISCONNECTED;
        if( BuildConfig.DEBUG )
        {
          Log.i(TAG, "Disconnected from GATT server.");
        }
        broadcastUpdate(intentAction);
      }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status)
    {
      if( status == BluetoothGatt.GATT_SUCCESS )
      {
        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
      }
      else
      {
        if( BuildConfig.DEBUG )
        {
          Log.i(TAG, "onServicesDiscovered received: " + status);
        }
      }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
    {
      if( status == BluetoothGatt.GATT_SUCCESS )
      {
        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
      }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
    {
      broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
    }
  };

  private void broadcastUpdate(final String action)
  {
    final Intent intent = new Intent(action);
    sendBroadcast(intent);
  }

  private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic)
  {
    final Intent intent = new Intent(action);

    // For all other profiles, writes the data formatted in HEX.
    final byte[] data = characteristic.getValue();
    if( BuildConfig.DEBUG )
    {
      Log.i(TAG, "data" + characteristic.getValue());
    }

    if( data != null && data.length > 0 )
    {
      final StringBuilder stringBuilder = new StringBuilder(data.length);
      for( byte byteChar : data )
      {
        stringBuilder.append(String.format("%02X ", byteChar));
      }
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, String.format("%s", new String(data)));
      }
      // getting cut off when longer, need to push on new line, 0A
      intent.putExtra(EXTRA_DATA, String.format("%s", new String(data)));

    }
    sendBroadcast(intent);
  }

  @Override
  public IBinder onBind(Intent intent)
  {
    return mBinder;
  }

  @Override
  public boolean onUnbind(Intent intent)
  {
    // After using a given device, you should make sure that BluetoothGatt.close() is called
    // such that resources are cleaned up properly.  In this particular example, close() is
    // invoked when the UI is disconnected from the Service.
    close();
    return super.onUnbind(intent);
  }

  /**
   * Initializes a reference to the local Bluetooth adapter.
   *
   * @return Return true if the initialization is successful.
   */
  public boolean initialize()
  {
    // For API level 18 and above, get a reference to BluetoothAdapter through
    // BluetoothManager.
    if( mBluetoothManager == null )
    {
      mBluetoothManager = ( BluetoothManager ) getSystemService(Context.BLUETOOTH_SERVICE);
      if( mBluetoothManager == null )
      {
        Log.e(TAG, "Unable to initialize BluetoothManager.");
        return false;
      }
    }

    mBluetoothAdapter = mBluetoothManager.getAdapter();
    if( mBluetoothAdapter == null )
    {
      Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
      return false;
    }

    return true;
  }

  /**
   * Connects to the GATT server hosted on the Bluetooth LE device.
   *
   * @param address The device address of the destination device.
   * @return Return true if the connection is initiated successfully. The connection result
   * is reported asynchronously through the
   * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
   * callback.
   */
  public boolean connect(final String address)
  {
    if( mBluetoothAdapter == null || address == null )
    {
      Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
      return false;
    }

    // Previously connected device.  Try to reconnect.
    if( mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null )
    {
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
      }
      if( mBluetoothGatt.connect() )
      {
        mConnectionState = STATE_CONNECTING;
        return true;
      }
      else
      {
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        mBluetoothDeviceAddress = address;
        return false;
      }
    }

    final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
    if( device == null )
    {
      Log.w(TAG, "Device not found.  Unable to connect.");
      return false;
    }
    // We want to directly connect to the device, so we are setting the autoConnect
    // parameter to false.
    mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
    Log.d(TAG, "Trying to create a new connection.");
    mBluetoothDeviceAddress = address;
    mConnectionState = STATE_CONNECTING;
    return true;
  }

  /**
   * Disconnects an existing connection or cancel a pending connection. The disconnection result
   * is reported asynchronously through the
   * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
   * callback.
   */
  public void disconnect()
  {
    if( mBluetoothAdapter == null || mBluetoothGatt == null )
    {
      Log.w(TAG, "BluetoothAdapter not initialized");
      return;
    }
    mBluetoothGatt.disconnect();
  }

  /**
   * After using a given BLE device, the app must call this method to ensure resources are
   * released properly.
   */
  public void close()
  {
    if( mBluetoothGatt == null )
    {
      return;
    }
    mBluetoothGatt.close();
    mBluetoothGatt = null;
  }

  /**
   * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
   * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
   * callback.
   *
   * @param characteristic The characteristic to read from.
   */
  public void readCharacteristic(BluetoothGattCharacteristic characteristic)
  {
    if( mBluetoothAdapter == null || mBluetoothGatt == null )
    {
      Log.w(TAG, "BluetoothAdapter not initialized");
      return;
    }
    mBluetoothGatt.readCharacteristic(characteristic);
  }

  /**
   * Write to a given char
   *
   * @param characteristic The characteristic to write to
   */
  public void writeCharacteristic(BluetoothGattCharacteristic characteristic)
  {
    if( mBluetoothAdapter == null || mBluetoothGatt == null )
    {
      Log.w(TAG, "BluetoothAdapter not initialized");
      return;
    }

    mBluetoothGatt.writeCharacteristic(characteristic);
  }

  /**
   * Enables or disables notification on a give characteristic.
   *
   * @param characteristic Characteristic to act on.
   * @param enabled        If true, enable notification.  False otherwise.
   */
  public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled)
  {
    if( mBluetoothAdapter == null || mBluetoothGatt == null )
    {
      Log.w(TAG, "BluetoothAdapter not initialized");
      return;
    }
    mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

    // This is specific to Heart Rate Measurement.
    if( ProjectConst.UUID_HM_RX_TX.equals(characteristic.getUuid()) )
    {
      BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(HM10GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
      descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
      mBluetoothGatt.writeDescriptor(descriptor);
    }
  }

  /**
   * Retrieves a list of supported GATT services on the connected device. This should be
   * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
   *
   * @return A {@code List} of supported services.
   */
  public List<BluetoothGattService> getSupportedGattServices()
  {
    if( mBluetoothGatt == null )
    {
      return null;
    }

    return mBluetoothGatt.getServices();
  }

  public class LocalBinder extends Binder
  {
    public BluetoothLowEnergyService getService()
    {
      return BluetoothLowEnergyService.this;
    }
  }
}
