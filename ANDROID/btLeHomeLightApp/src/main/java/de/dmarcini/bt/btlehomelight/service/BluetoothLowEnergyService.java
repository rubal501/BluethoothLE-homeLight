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

package de.dmarcini.bt.btlehomelight.service;

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

import de.dmarcini.bt.btleplaceholder.BuildConfig;
import de.dmarcini.bt.btlehomelight.ProjectConst;
import de.dmarcini.bt.btlehomelight.utils.HM10GattAttributes;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLowEnergyService extends Service
{
  private final static String  TAG                             = BluetoothLowEnergyService.class.getSimpleName();
  private final static String servicePrefix = BluetoothLowEnergyService.class.getName();
  public final static  String  ACTION_GATT_CONNECTED           = servicePrefix + ".ACTION_GATT_CONNECTED";
  public final static  String  ACTION_GATT_DISCONNECTED        = servicePrefix + ".ACTION_GATT_DISCONNECTED";
  public final static  String  ACTION_GATT_SERVICES_DISCOVERED = servicePrefix + ".ACTION_GATT_SERVICES_DISCOVERED";
  public final static  String  ACTION_DATA_AVAILABLE           = servicePrefix + ".ACTION_DATA_AVAILABLE";
  public final static  String  EXTRA_DATA                      = servicePrefix + ".EXTRA_DATA";
  private static final int     STATE_DISCONNECTED              = 0;
  private static final int     STATE_CONNECTING                = 1;
  private static final int     STATE_CONNECTED                 = 2;
  private final        IBinder mBinder                         = new LocalBinder();
  private              int     mConnectionState                = STATE_DISCONNECTED;
  private BluetoothManager mBluetoothManager;
  private BluetoothAdapter mBluetoothAdapter;
  private String           mBluetoothDeviceAddress;
  private BluetoothGatt    mBluetoothGatt;
  //
  // implementiert Methjoden für GATT (Gereric Attribute) Ereignisse, welche die APP
  // bearbeiten soll. Beispielsweise Verbindungsänderungen und DService descovering
  //
  private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
  {
    //
    // Verbindungsstatus ändert sich
    //
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
    {
      String intentAction;
      //
      // Verbindung zu GATT Server?
      //
      if( newState == BluetoothProfile.STATE_CONNECTED )
      {
        intentAction = ACTION_GATT_CONNECTED;
        mConnectionState = STATE_CONNECTED;
        broadcastUpdate(intentAction);
        if( BuildConfig.DEBUG )
        {
          Log.i(TAG, "Connected to GATT server.");
        }
        //
        // versuche Services zu finden
        //
        if( BuildConfig.DEBUG )
        {
          Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
        }
      }
      //
      // Verbindung beendet?
      //
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

    //
    // Service gefunden!
    //
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status)
    {
      //
      // Ist die Suche beendet?
      //
      if( status == BluetoothGatt.GATT_SUCCESS )
      {
        // Suche beendet!
        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
      }
      else
      {
        //
        // Da ist noch was gefunden worden
        //
        if( BuildConfig.DEBUG )
        {
          Log.i(TAG, "onServicesDiscovered received: " + status);
        }
      }
    }

    //
    // Es wurde eine "characteristic" gefunden, d.h. eine Servicefunktion
    //
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
    {
      if( status == BluetoothGatt.GATT_SUCCESS )
      {
        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
      }
    }

    //
    // eine "characteristic" hat sich geändert
    //
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
    {
      broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
    }
  };

  /**
   * Sende die Statusänderung an alle
   *
   * @param action
   */
  private void broadcastUpdate(final String action)
  {
    final Intent intent = new Intent(action);
    sendBroadcast(intent);
  }

  /**
   * Sende die Statusänderung an alle, sende Die Serverfunktion mit
   *
   * @param action String mit der Bezeichnung der aktion
   */
  private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic)
  {
    final Intent intent = new Intent(action);

    //
    // schreibe für alle anderen Profile die Daten in HEX
    //
    final byte[] data = characteristic.getValue();
    if( BuildConfig.DEBUG )
    {
      Log.i(TAG, "data: " + characteristic.getValue());
    }
    //
    // Wurden Daten empfangen?
    //
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
      //
      // Daten gekürzt auf (? Bytes), Bei mehr daten 0x0a als Trenner
      // (getting cut off when longer, need to push on new line, 0A)
      //
      intent.putExtra(EXTRA_DATA, String.format("%s", new String(data)));
    }
    //
    // Status vermelden
    //
    sendBroadcast(intent);
  }

  /**
   * Den Binder zurückgeben, wenn der Service an die App gebunden wurde
   *
   * @param intent
   * @return der Binder
   */
  @Override
  public IBinder onBind(Intent intent)
  {
    return mBinder;
  }

  /**
   * Der Service wird von der App getrennt
   *
   * @param intent
   * @return
   */
  @Override
  public boolean onUnbind(Intent intent)
  {
    //
    // Wenn ein Gerät nicht mehr genutzt wird, soll BluetoothGatt.close() aufgerufen werden
    // um Resourcen wieder freizugeben. Hier erledigt das die Funktion close()
    //
    close();
    return super.onUnbind(intent);
  }

  /**
   * Initialisiert eine Referenz zum Bluethoothadapter
   *
   * @return true wenn erfolgreich
   */
  public boolean initialize()
  {
    //
    // Ab API 18 wird eine Referenz via BluethootManager geholt
    //
    if( mBluetoothManager == null )
    {
      mBluetoothManager = ( BluetoothManager ) getSystemService(Context.BLUETOOTH_SERVICE);
      //
      // War ein holen der Referenz erfolgreich?
      //
      if( mBluetoothManager == null )
      {
        Log.e(TAG, "Unable to initialize BluetoothManager.");
        return false;
      }
    }
    //
    // der Manager ist schon gefunden
    //
    mBluetoothAdapter = mBluetoothManager.getAdapter();
    // Gibt es nun auch den Bluethootadapter?
    //
    if( mBluetoothAdapter == null )
    {
      Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
      return false;
    }
    //
    // Es gibt einen Adapter!
    //
    return true;
  }

  /**
   * Verbinde zum GATT Server auf dem entfernten BTLE Gerät
   *
   * @param address Die Adresse des fernen Gerätes
   * @return True wenn die Verbindung erfolgrecih war
   */
  public boolean connect(final String address)
  {
    //
    // Hier ist leider nichts zu verbinden
    //
    if( mBluetoothAdapter == null || address == null )
    {
      Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
      return false;
    }
    //
    // verbinde ein schon verbindenes Gerät neu (versuche reconnect)
    //
    if( mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null )
    {
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
      }
      if( mBluetoothGatt.connect() )
      {
        //
        // Das neuverbinden war erfolgreich!
        //
        mConnectionState = STATE_CONNECTING;
        return true;
      }
      else
      {
        //
        // Neu verbinden schlug fehl, versuche GATT zu empfangen
        //
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        mBluetoothDeviceAddress = address;
        return false;
      }
    }
    //
    // Kein neuverbinden, also VErbindung zum entfernten Gerät aufbauen
    //
    final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
    if( device == null )
    {
      Log.w(TAG, "Device not found.  Unable to connect.");
      return false;
    }
    //
    // Wir wollen direkt zum Gerät verbinden, also wird der autoConnect
    // Parameter auf "false" gesetzt
    //
    mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
    Log.i(TAG, "Trying to create a new connection.");
    mBluetoothDeviceAddress = address;
    mConnectionState = STATE_CONNECTING;
    return true;
  }

  /**
   * Trennt eine existierende Verbindung oder beendet eine Verbindungsaufnahme.
   * Das Ergebnis wird via Callback "onConnectionStateChange" signalisiert
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

  /**
   * Wenn ein BT Gerät genutzt wurde, muss "close()" ausgeführt werden, um Resourcen freizugeben
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
   * Lese über eine "characteristic" Daten aus einem entfernten Gerät,
   * Rückmeldung via "onCharacteristicRead"
   *
   * @param characteristic "caracteristic" welche gelesen werden soll
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
   * Schreibe über eine "characteristic" Daten an ein entfernets Gerät
   *
   * @param characteristic
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
   * Aktiviere oder deaktiviere die Benachrichtigung auf einer "characteristic"
   *
   * @param characteristic die "characteristic" dessen Benachrichtigung aktiviert/deaktiviert werden soll
   * @param enabled aktiv/deaktiv
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
   * Empfängt eine Liste der unterstützten GATT Services auf dem verbundenen Gerät.
   * Die Funktion sollte nach der erfolgreichen Beendigung von {@code BluetoothGatt#discoverServices()}
   * aufgefufen werden
   *
   * @return Liste {@code List} der unterstützten services
   */
  public List<BluetoothGattService> getSupportedGattServices()
  {
    if( mBluetoothGatt == null )
    {
      return null;
    }

    return mBluetoothGatt.getServices();
  }

  /**
   * Der Binder über welchen die APP den service erreicht
   */
  public class LocalBinder extends Binder
  {
    public BluetoothLowEnergyService getService()
    {
      return BluetoothLowEnergyService.this;
    }
  }
}
