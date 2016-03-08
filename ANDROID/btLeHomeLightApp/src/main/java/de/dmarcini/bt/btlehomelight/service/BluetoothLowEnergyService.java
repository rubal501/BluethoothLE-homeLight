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

import android.annotation.SuppressLint;
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
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Vector;

import de.dmarcini.bt.btlehomelight.BuildConfig;
import de.dmarcini.bt.btlehomelight.ProjectConst;
import de.dmarcini.bt.btlehomelight.R;
import de.dmarcini.bt.btlehomelight.interfaces.IBtCommand;
import de.dmarcini.bt.btlehomelight.utils.BTReaderThread;
import de.dmarcini.bt.btlehomelight.utils.BlueThoothMessage;
import de.dmarcini.bt.btlehomelight.utils.CircularByteBuffer;
import de.dmarcini.bt.btlehomelight.utils.HM10GattAttributes;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLowEnergyService extends Service
{
  private final static String             TAG                      = BluetoothLowEnergyService.class.getSimpleName();
  //private final static String  servicePrefix    = BluetoothLowEnergyService.class.getName();
  private final        IBinder            mBinder                  = new LocalBinder();
  private final        CircularByteBuffer ringBuffer               = new CircularByteBuffer(1024);
  private final        Vector<String>     cmdBuffer                = new Vector<>();
  private final        CRunnable          cmdQueueReader           = new CRunnable();
  private              int                mConnectionState         = ProjectConst.STATUS_DISCONNECTED;
  private              boolean            isCorrectConnectedModule = false;
  private              Handler            btEventHandler           = null;
  private              Handler            mHandler                 = new Handler();
  private BTReaderThread              readerThread;
  private BluetoothManager            mBluetoothManager;
  private BluetoothAdapter            mBluetoothAdapter;
  private String                      mBluetoothDeviceAddress;
  private BluetoothGatt               mBluetoothGatt;
  private BluetoothGattCharacteristic characteristicTX;
  private BluetoothGattCharacteristic characteristicRX;
  //
  // implementiert Methjoden für GATT (Gereric Attribute) Ereignisse, welche die APP
  // bearbeiten soll.
  // Beispielsweise Verbindungsänderungen und Service descovering
  //
  private final BluetoothGattCallback           btLeGattCallback   = new BluetoothGattCallback()
  {
    private final String TAGCG = BluetoothGattCallback.class.getSimpleName();

    //
    // Verbindungsstatus ändert sich
    //
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
    {
      //
      // Verbindung zu GATT Server?
      //
      if( newState == BluetoothProfile.STATE_CONNECTED )
      {
        //
        // erschoben zu dataToRingBuffer, wenn ein Korrektes Modul gemeldet wird
        // setConnectionState( ProjectConst.STATUS_CONNECTED );
        // dafür nur Status setzen, ohne Benachrichtigung
        //
        setConnectionState(ProjectConst.STATUS_TEST_MODULE);
        if( BuildConfig.DEBUG )
        {
          Log.i(TAGCG, "Connected to GATT server.Check services...");
        }
        //
        // versuche Services zu finden, wenn gefunden Modultyp abfragen (bei den Services)
        //
        if( BuildConfig.DEBUG )
        {
          Log.i(TAGCG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
        }
        else
        {
          mBluetoothGatt.discoverServices();
        }
      }
      //
      // Verbindung beendet?
      //
      else if( newState == BluetoothProfile.STATE_DISCONNECTED )
      {
        setConnectionState(ProjectConst.STATUS_DISCONNECTED);
        //
        // Falls der Thread noch läuft, erst mal stoppen
        //
        if( readerThread != null )
        {
          readerThread.doStop();
          readerThread = null;
        }
        cmdBuffer.clear();
        if( BuildConfig.DEBUG )
        {
          Log.i(TAGCG, "Disconnected from GATT server.");
        }
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
      if( BuildConfig.DEBUG )
      {
        Log.i(TAGCG, "onServicesDiscovered received: status " + status);
      }
      if( status == BluetoothGatt.GATT_SUCCESS )
      {
        if( btEventHandler != null )
        {
          BlueThoothMessage msg = new BlueThoothMessage(ProjectConst.MESSAGE_GATT_SERVICES_DISCOVERED);
          btEventHandler.obtainMessage(ProjectConst.MESSAGE_GATT_SERVICES_DISCOVERED, msg).sendToTarget();
        }
        //
        // Suche beendet! Finde RXTX Service
        //
        List<BluetoothGattService> gattServices = gatt.getServices();
        if( gattServices == null )
        {
          //
          // Keine Services gefunden => FEHLER
          //
          setConnectionState(ProjectConst.STATUS_CONNECT_ERROR, R.string.service_err_not_services);
          return;
        }
        //
        // jetzt nach serialer Verbindung suchen
        //
        characteristicTX = characteristicRX = null;
        for( BluetoothGattService gattService : gattServices )
        {
          if( BuildConfig.DEBUG )
          {
            Log.d(TAGCG, "service   : " + gattService.getUuid().toString() + "...");
          }
          //
          // ist dieser Service ein Serial RXTX?
          //
          if( gattService.getUuid().toString().compareTo(HM10GattAttributes.HM_RXTX_UUID.toString()) == 0 )
          {
            //
            // Alles ist in Ordnung
            //
            if( BuildConfig.DEBUG )
            {
              Log.v(TAGCG, "found serial RX TX service! All OK!");
            }
            characteristicTX = gattService.getCharacteristic(HM10GattAttributes.HM_10_CONF_UUID);
            characteristicRX = gattService.getCharacteristic(HM10GattAttributes.HM_10_CONF_UUID);
            //
            // Falls der Thread schon läuft, erst mal stoppen
            //
            if( readerThread != null )
            {
              readerThread.doStop();
              readerThread = null;
            }
            cmdBuffer.clear();
            readerThread = new BTReaderThread(ringBuffer, cmdBuffer);
            Thread rThread = new Thread(readerThread, "reader_thread");
            rThread.start();
            //
            // wenn die Kommunikation sichergestellt ist, frage nach dem Modul,
            // wenn es das Richtige ist, CONNECT Meldung senden
            //
            askModulForType();
            return;
          }
          else
          {
            if( BuildConfig.DEBUG )
            {
              Log.v(TAGCG, "found service: " + HM10GattAttributes.lookup(gattService.getUuid().toString(), "unknown"));
            }
          }
        }
        //
        // Kontrolle, ob es Services gab, ansonsten intervenieren
        //
        if( characteristicTX == null || characteristicRX == null )
        {
          characteristicTX = characteristicRX = null;
          Log.e(TAGCG, "it was not characteristic there!");
          setConnectionState(ProjectConst.STATUS_CONNECT_ERROR, R.string.service_err_not_characteristics);
          return;
        }
      }
      else
      {
        //
        // Da ist noch was gefunden worden
        //
        if( BuildConfig.DEBUG )
        {
          Log.i(TAGCG, "onServicesDiscovered received: " + status);
        }
      }
    }

    //
// Es wurden Daten empfangen, bitte abholen!
//
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
    {
      //
      // Alles fertig gelesen?
      //
      if( status == BluetoothGatt.GATT_SUCCESS )
      {
        dataToRingBuffer(characteristic);
      }
    }

    /**
     * Bearbeite die von der characteristic empfangenen Daten
     *
     * @param characteristic Der Datenkanal aus dem die Daten kommen
     * TODO: Hier erst mal direkt, wobei ich davon ausgehe, dass die datensätze zusammenhängend empfangen werden. Später Ringpuffer implementieren und Daten daraus versenden
     */

    private void dataToRingBuffer(BluetoothGattCharacteristic characteristic)
    {
      byte   data[];
      String recMsg;
      //
      // lese die Daten aus dem BT-Kanal (Characteristic)
      //
      data = characteristic.getValue();
      //
      // Sind Daten vorhanden?
      //
      if( data != null && data.length > 0 )
      {
        //
        // verbundenes BT Gerät hat Daten empfangen
        // Datenblöcke werden bis 20 Byte am Stück übertragen (BT 4.0)
        // Daten in den Ringpuffer, leider kommen die Daten nicht immer am Stück
        //
        try
        {
          //
          // Daten in den Ringpufer schicken, um die Verarbeitung
          // kümmert sich der ReaderThread
          //
          ringBuffer.getOutputStream().write(data);
          // Thread bescheid geben, da war doch was...
          synchronized( ringBuffer )
          {
            //
            // beende die Wartezeit des ReaderThreads, es sind ja Daten gekommen!
            //
            ringBuffer.notifyAll();
          }
        }
        catch( IOException ex )
        {
          Log.e(TAG, "IOException while read from BT decice..." + ex.getLocalizedMessage());
        }
      }
    }

    //
// eine "characteristic" hat sich geändert
//
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
    {
      dataToRingBuffer(characteristic);
    }
  };
  /**
   * Callback Methode beim Scannen von BTLE Geräten
   */
  @SuppressLint( "NewApi" )
  private       ScanCallback                    scanCallback       = new ScanCallback()
  {

    @Override
    public void onBatchScanResults(List<ScanResult> results)
    {
      Iterator<ScanResult> it = results.iterator();
      while( it.hasNext() )
      {
        ScanResult result = it.next();
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "found BTLE Device: " + result.getDevice().getAddress());
        }
        if( btEventHandler != null )
        {
          BlueThoothMessage msg = new BlueThoothMessage(ProjectConst.MESSAGE_BTLE_DEVICE_DISCOVERED, result.getDevice());
          btEventHandler.obtainMessage(ProjectConst.MESSAGE_BTLE_DEVICE_DISCOVERED, msg).sendToTarget();
        }
      }
    }

    @Override
    public void onScanFailed(int errorCode)
    {
      Log.e(TAG, "BTLE scan ERROR, Code: " + errorCode);
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result)
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "found BTLE Device (new API): " + result.getDevice().getAddress());
      }
      if( btEventHandler != null )
      {
        BlueThoothMessage msg = new BlueThoothMessage(ProjectConst.MESSAGE_BTLE_DEVICE_DISCOVERED, result.getDevice());
        btEventHandler.obtainMessage(ProjectConst.MESSAGE_BTLE_DEVICE_DISCOVERED, msg).sendToTarget();
      }
      else
      {
        Log.w(TAG, "can't send message to app! No Message Handler");
      }
    }
  };
  /**
   * Callback für Androis < Lollipop
   */
  private       BluetoothAdapter.LeScanCallback oldApiScanCallback = new BluetoothAdapter.LeScanCallback()
  {

    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord)
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "found BTLE Device (old API): " + device.getAddress());
      }
      if( btEventHandler != null )
      {
        BlueThoothMessage msg = new BlueThoothMessage(ProjectConst.MESSAGE_BTLE_DEVICE_DISCOVERED, device);
        btEventHandler.obtainMessage(ProjectConst.MESSAGE_BTLE_DEVICE_DISCOVERED, msg).sendToTarget();
      }
    }
  };

  /**
   * Den Binder zurückgeben, wenn der Service an die App gebunden wurde
   *
   * @param intent
   * @return der Binder
   */
  @Override
  public IBinder onBind(Intent intent)
  {
//    if( BuildConfig.DEBUG )
//    {
//      Log.v(TAG, "start recived commands queue thread");
//    }
//    Thread th = new Thread(cmdQueueReader);
//    th.setName("readBTDataThread");
//    th.start();
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
//    if( cmdQueueReader != null )
//    {
//      cmdQueueReader.stopThread();
//      {
//        if( BuildConfig.DEBUG )
//        {
//          Log.v(TAG, "stop recived commands queue thread");
//        }
//      }
//    }
    //
    // Wenn ein Gerät nicht mehr genutzt wird, soll BluetoothGatt.close() aufgerufen werden
    // um Resourcen wieder freizugeben. Hier erledigt das die Funktion close()
    //
    close();
    btEventHandler = null;
    return super.onUnbind(intent);
  }

  /**
   * Initialisiert eine Referenz zum Bluethoothadapter
   *
   * @return true wenn erfolgreich
   */
  @Override
  public void onCreate()
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
    }
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "start recived commands queue thread");
    }
    Thread th = new Thread(cmdQueueReader);
    th.setName("readBTDataThread");
    th.start();
  }

  /**
   * Verbinde zum GATT Server auf dem entfernten BTLE Gerät
   *
   * @param address Die Adresse des fernen Gerätes
   * @return True wenn die Verbindung erfolgreich war
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
    // ist da noch was beim Discover?
    //
    if( mConnectionState == ProjectConst.STATUS_DISCOVERING )
    {
      stopDiscoverDevices();
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
        setConnectionState(ProjectConst.STATUS_CONNECTING);
        return true;
      }
      else
      {
//
// Neu verbinden schlug fehl, versuche GATT zu empfangen
//
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mBluetoothGatt = device.connectGatt(this, false, btLeGattCallback);
        mBluetoothDeviceAddress = address;
        return false;
      }
    }
//
// Kein neuverbinden, also Verbindung zum entfernten Gerät aufbauen
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
    mBluetoothGatt = device.connectGatt(this, false, btLeGattCallback);
    Log.i(TAG, "Trying to create a new connection.");
    mBluetoothDeviceAddress = address;
    setConnectionState(ProjectConst.STATUS_CONNECTING);
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
   * @param enabled        aktiv/deaktiv
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
    if( HM10GattAttributes.HM_RXTX_UUID.equals(characteristic.getUuid()) )
    {
      BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(HM10GattAttributes.UUID_CLIENT_CHARACTERISTIC_CONFIG));
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
   * Finde BTLE Geräte, wenn nicht gerade eine Verbindung besteht, Möglichkeiet Filter zu setzen
   * Beispiel hier: HM10GattAttributes.HM_10_CONF == "0000ffe0-0000-1000-8000-00805f9b34fb"
   *
   * @param uuidArr
   * @return
   */
  @SuppressLint( "NewApi" )
  public boolean discoverDevices(String[] uuidArr)
  {
    if( mConnectionState != ProjectConst.STATUS_DISCONNECTED || mBluetoothAdapter == null )
    {
      Log.w(TAG, "discovering not possible, device not disconnected or not BT Adapter here!");
      return (false);
    }
    // Stops scanning after a pre-defined scan period.
    mHandler.postDelayed(new Runnable()
    {
      @Override
      public void run()
      {
        stopDiscoverDevices();
      }
    }, ProjectConst.SCAN_PERIOD);
    //
    // gibt es keinen Filter, alles suchen
    //
    if( uuidArr == null || uuidArr.length == 0 )
    {
      if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP )
      {
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "start BTLE discovering with API > LOLLIPOP without filters");
        }
        mBluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
      }
      else
      {
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "start BTLE discovering with old API < LOLLIPOP without filters");
        }
        mBluetoothAdapter.startLeScan(oldApiScanCallback);
      }
    }
    else
    {
      if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP )
      {
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "start BTLE discovering with API > LOLLIPOP with filters");
        }
        //
        // erzeuge den Suchfilter
        //
        ArrayList<ScanFilter> filterList = new ArrayList();
        for( int i = 0; i < uuidArr.length; i++ )
        {
          // Empfangsstärke wäre noch möglich, .setRssiRange(-75, 0).
          filterList.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(uuidArr[ i ])).build());
        }
        ScanSettings.Builder builder = new ScanSettings.Builder();
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
        {
          builder.setMatchMode(ScanSettings.SCAN_MODE_BALANCED);
        }
        ScanSettings scSettings = builder.build();
        mBluetoothAdapter.getBluetoothLeScanner().startScan(filterList, scSettings, scanCallback);
      }
      else
      {
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "start BTLE discovering with old API < LOLLIPOP without filters");
        }
        mBluetoothAdapter.startLeScan(oldApiScanCallback);
      }
    }
    setConnectionState(ProjectConst.STATUS_DISCOVERING);
    return (true);
  }

  /**
   * Stoppe den Scannervorgang!
   *
   * @return
   */
  public void stopDiscoverDevices()
  {
    if( mConnectionState == ProjectConst.STATUS_DISCOVERING && mBluetoothAdapter != null )
    {
      if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP )
      {
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "stop BTLE discovering with API > LOLLIPOP");
        }
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
      }
      else
      {
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "stop BTLE discovering with old API < LOLLIPOP");
        }
        mBluetoothAdapter.stopLeScan(oldApiScanCallback);
      }
      if( btEventHandler != null )
      {
        BlueThoothMessage msg1 = new BlueThoothMessage(ProjectConst.MESSAGE_BTLE_DEVICE_END_DISCOVERING);
        btEventHandler.obtainMessage(ProjectConst.MESSAGE_BTLE_DEVICE_END_DISCOVERING, msg1).sendToTarget();
      }
      setConnectionState(ProjectConst.STATUS_DISCONNECTED);
    }
  }

  /**
   * Verbindungsstatus ohne Fehlermeldung ändern
   *
   * @param connectionState Der neue Verbindungsstatus
   */
  private void setConnectionState(int connectionState)
  {
    setConnectionState(connectionState, 0);
  }

  /**
   * Verbindungsstatus mit Fehlermeldung setzten
   *
   * @param connectionState Neuer Verbindungsstatus
   * @param errResourceId   Resource-Id der Fehlermeldung in Strings
   */
  private void setConnectionState(int connectionState, int errResourceId)
  {
    //
    // sind da noch aktionen auszuführen?
    //
    switch( connectionState )
    {
      case ProjectConst.STATUS_DISCONNECTED:
        if( btEventHandler != null )
        {
          if( mConnectionState == ProjectConst.STATUS_DISCOVERING )
          {
            // Beendetes discovering
            BlueThoothMessage msg1 = new BlueThoothMessage(ProjectConst.MESSAGE_BTLE_DEVICE_END_DISCOVERING);
            btEventHandler.obtainMessage(ProjectConst.MESSAGE_BTLE_DEVICE_END_DISCOVERING, msg1).sendToTarget();
          }
          BlueThoothMessage msg2 = new BlueThoothMessage(ProjectConst.MESSAGE_DISCONNECTED);
          btEventHandler.obtainMessage(mConnectionState, msg2).sendToTarget();
        }
        isCorrectConnectedModule = false;
        characteristicTX = characteristicRX = null;
        break;

      case ProjectConst.STATUS_CONNECTING:
        if( btEventHandler != null )
        {
          BlueThoothMessage msg = new BlueThoothMessage(ProjectConst.MESSAGE_CONNECTING);
          btEventHandler.obtainMessage(ProjectConst.MESSAGE_CONNECTING, msg).sendToTarget();
        }
        isCorrectConnectedModule = false;
        break;

      case ProjectConst.STATUS_CONNECTED:
        if( btEventHandler != null )
        {
          BlueThoothMessage msg = new BlueThoothMessage(ProjectConst.MESSAGE_CONNECTED, mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT).get(0));
          btEventHandler.obtainMessage(ProjectConst.MESSAGE_CONNECTED, msg).sendToTarget();
        }
        break;

      case ProjectConst.STATUS_DISCOVERING:
        if( btEventHandler != null )
        {
          BlueThoothMessage msg = new BlueThoothMessage(ProjectConst.MESSAGE_BTLE_DEVICE_DISCOVERING);
          btEventHandler.obtainMessage(ProjectConst.MESSAGE_BTLE_DEVICE_DISCOVERING, msg).sendToTarget();
        }
        isCorrectConnectedModule = false;
        break;

      case ProjectConst.STATUS_TEST_MODULE:
        isCorrectConnectedModule = false;
        break;

      case ProjectConst.STATUS_CONNECT_ERROR:
      default:
        if( btEventHandler != null )
        {
          BlueThoothMessage msg = new BlueThoothMessage(ProjectConst.MESSAGE_CONNECT_ERROR, errResourceId);
          btEventHandler.obtainMessage(ProjectConst.MESSAGE_CONNECT_ERROR, msg).sendToTarget();
          BlueThoothMessage msg2 = new BlueThoothMessage(ProjectConst.MESSAGE_DISCONNECTED);
          btEventHandler.obtainMessage(mConnectionState, msg2).sendToTarget();
        }
        isCorrectConnectedModule = false;
        characteristicTX = characteristicRX = null;
    }
    mConnectionState = connectionState;
  }

  /**
   * Sende an das Modul die Frage nach seinem Typ
   */
  private void askModulForType()
  {
    String kommandoString;
    //
    // Kommando zusammenbauen
    //
    kommandoString = String.format(Locale.ENGLISH, "%s%02X%s", ProjectConst.STX, ProjectConst.C_ASKTYP, ProjectConst.ETX);
    Log.d(TAG, "send ask for type =" + kommandoString);
    sendKdoToModule(kommandoString);
  }

  /**
   * Frage das Modul nach seinem Namen
   */
  private void askModulForName()
  {
    String kommandoString;
    //
    // Kommando zusammenbauen
    //
    kommandoString = String.format(Locale.ENGLISH, "%s%02X%s", ProjectConst.STX, ProjectConst.C_ASKNAME, ProjectConst.ETX);
    Log.d(TAG, "send ask for name =" + kommandoString);
    sendKdoToModule(kommandoString);
  }

  /**
   * Frage das Modul nach aktellem RGBW
   */
  private void askModulForRGBW()
  {
    String kommandoString;
    //
    // Kommando zusammenbauen
    //
    kommandoString = String.format(Locale.ENGLISH, "%s%02X%s", ProjectConst.STX, ProjectConst.C_ASKRGBW, ProjectConst.ETX);
    Log.d(TAG, "send ask for RGBW (raw) =" + kommandoString);
    sendKdoToModule(kommandoString);
  }

  public void setModulRawRGBW(short[] rgbw)
  {
    String kommandoString;
    //
    // Kommando zusammenbauen
    //
    kommandoString = String.format(Locale.ENGLISH, "%s%02X:%02X:%02X:%02X:%02X%s", ProjectConst.STX, ProjectConst.C_SETCOLOR, rgbw[ 0 ], rgbw[ 1 ], rgbw[ 2 ], rgbw[ 3 ], ProjectConst.ETX);
    Log.d(TAG, "send set RGBW =" + kommandoString);
    sendKdoToModule(kommandoString);
  }

  /**
   * Setze Farben als RGB, Modul kalibriert nach RGBW
   *
   * @param rgbw RGB Werte, White wird ignoriert
   */
  public void setModulRGB4Calibrate(short[] rgbw)
  {
    String kommandoString;
    //
    // Kommando zusammenbauen
    //
    kommandoString = String.format(Locale.ENGLISH, "%s%02X:%02X:%02X:%02X:%02X%s", ProjectConst.STX, ProjectConst.C_SETCALRGB, rgbw[ 0 ], rgbw[ 1 ], rgbw[ 2 ], 0, ProjectConst.ETX);
    Log.d(TAG, "send set RGBW =" + kommandoString);
    sendKdoToModule(kommandoString);
  }

  /**
   * schalte den Pausenmodus um
   */
  private void setModulPause()
  {
    String kommandoString;
    //
    // Kommando zusammenbauen
    //
    kommandoString = String.format(Locale.ENGLISH, "%s%02X%s", ProjectConst.STX, ProjectConst.C_ONOFF, ProjectConst.ETX);
    Log.d(TAG, "send light on/off =" + kommandoString);
    sendKdoToModule(kommandoString);
  }

  /**
   * Sende den Kommandostring (incl ETX und STX) zum Modul, wenn Verbunden
   *
   * @param kdo String mit ETC und STX
   */
  private boolean sendKdoToModule(final String kdo)
  {
    if( (mConnectionState == ProjectConst.STATUS_CONNECTED || mConnectionState == ProjectConst.STATUS_TEST_MODULE) && characteristicRX != null && characteristicTX != null && mBluetoothGatt != null )
    {
      byte[] tx = kdo.getBytes();
      characteristicTX.setValue(tx);
      mBluetoothGatt.writeCharacteristic(characteristicTX);
      setCharacteristicNotification(characteristicRX, true);
      if( BuildConfig.DEBUG )
      {
        Log.d(TAG, "send OK");
      }
      return (true);
    }
    else
    {
      Log.w(TAG, "send NOT OK, not connected?");
      return (false);
    }
  }

  /**
   * Private Klasse zum Auslesen der MessageQueue und Weiterleiten an die App
   */
  private class CRunnable implements Runnable
  {
    private static final String RTAG = "cmdQueueReader";
    private boolean isRunning;

    @Override
    public void run()
    {
      isRunning = true;
      while( isRunning )
      {
        if( cmdBuffer.isEmpty() )
        {
          synchronized( cmdBuffer )
          {
            try
            {
              cmdBuffer.wait(100);
            }
            catch( InterruptedException ex )
            {
              Log.e(RTAG, ex.getLocalizedMessage());
              //TODO Meldung machen?
            }
          }
        }
        else
        {
          String recMsg = cmdBuffer.remove(0);
          //
          // Sind die Daten als Komando korrekt formatiert?
          //
          if( recMsg.matches(ProjectConst.KOMANDPATTERN) )
          {
            //
            // Es gibt ein gültiges Kommando / Nachricht
            //
            if( BuildConfig.DEBUG )
            {
              Log.v(RTAG, "readed data ist valid PDU, recived: " + recMsg);
            }
            //
            // Ist es eine Nachricht über den Typ des Modules ?
            //
            if( recMsg.matches(ProjectConst.MODULTYPPATTERN) )
            {
              //
              // Jetzt wichtig: Ist es ein zugelassener Modultyp oder nicht?
              //
              if( recMsg.matches(ProjectConst.MY_MODULTYPPATTERN) )
              {
                isCorrectConnectedModule = true;
                Log.i(RTAG, "connected modul is an correct type");
                setConnectionState(ProjectConst.STATUS_CONNECTED);
              }
              else
              {
                Log.e(RTAG, "connected modul is an incorrect type!");
                setConnectionState(ProjectConst.STATUS_CONNECT_ERROR, R.string.service_err_incorrect_module_type);
              }
            }
            else
            {
              //
              // Keine Modultypnachricht
              // An Activity senden, wenn Handler gesetzt ist
              //
              if( btEventHandler != null && isCorrectConnectedModule )
              {
                BlueThoothMessage msg = new BlueThoothMessage(ProjectConst.MESSAGE_BTLE_DATA, recMsg);
                btEventHandler.obtainMessage(ProjectConst.MESSAGE_BTLE_DATA, msg).sendToTarget();
              }
            }
          }
          else
          {
            //
            // Daten sind ungültig
            //
            if( BuildConfig.DEBUG )
            {
              Log.v(RTAG, "readet data ist NOT valid PDU, recived: " + recMsg);
            }
            setConnectionState(ProjectConst.STATUS_CONNECT_ERROR, R.string.service_err_data_corrupt);
          }
        }
      }
    }

    public void stopThread()
    {
      isRunning = false;
      Log.v(RTAG, "CRunable stopping");
      synchronized( cmdBuffer )
      {
        cmdBuffer.notifyAll();
      }
    }
  }

  /**
   * Der Binder über welchen die APP den service erreicht
   */
  public class LocalBinder extends Binder implements IBtCommand
  {
    /**
     * Gib den Service an die App zurück
     *
     * @return Referenz auf den Service
     */
    public BluetoothLowEnergyService getService()
    {
      return BluetoothLowEnergyService.this;
    }

    /**
     * registriere einen Callback
     *
     * @param mHandler der Handler
     */
    public void registerServiceHandler(Handler mHandler)
    {
      Log.i(TAG, "Client register");
      btEventHandler = mHandler;
      if( mConnectionState == ProjectConst.STATUS_DISCONNECTED )
      {
        BlueThoothMessage msg = new BlueThoothMessage(ProjectConst.MESSAGE_DISCONNECTED);
        btEventHandler.obtainMessage(ProjectConst.MESSAGE_DISCONNECTED, msg).sendToTarget();
      }
      else if( mConnectionState == ProjectConst.STATUS_CONNECTING )
      {
        BlueThoothMessage msg = new BlueThoothMessage(ProjectConst.MESSAGE_CONNECTING);
        btEventHandler.obtainMessage(ProjectConst.MESSAGE_CONNECTING, msg).sendToTarget();
      }
      else if( mConnectionState == ProjectConst.STATUS_CONNECTED )
      {
        BlueThoothMessage msg = new BlueThoothMessage(ProjectConst.MESSAGE_CONNECTED, mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT).get(0));
        btEventHandler.obtainMessage(ProjectConst.MESSAGE_CONNECTED, msg).sendToTarget();
      }
      else
      {
        BlueThoothMessage msg = new BlueThoothMessage(ProjectConst.MESSAGE_DISCONNECTED);
        btEventHandler.obtainMessage(ProjectConst.MESSAGE_DISCONNECTED, msg).sendToTarget();
      }
    }

    /**
     * lösche den Handler
     */
    public void unregisterServiceHandler()
    {
      Log.i(TAG, "Client register");
      btEventHandler = null;
    }

    /**
     * Suche nach BTLE Geräten (wenn uuidArr != null nach allen)
     *
     * @param uuidArr
     */
    @Override
    public boolean discoverDevices(String[] uuidArr)
    {
      return (BluetoothLowEnergyService.this.discoverDevices(uuidArr));
    }

    /**
     * Stoppe die Erkundung!
     */
    @Override
    public void stopDiscoverDevices()
    {
      BluetoothLowEnergyService.this.stopDiscoverDevices();
    }

    /**
     * Berbinde zu einem BTLE Modul
     *
     * @param addr Adresse des Modules
     */
    @Override
    public void connectTo(String addr)
    {
      BluetoothLowEnergyService.this.connect(addr);
    }

    /**
     * Trenne die Verbindung mit einem BTLE Modul
     */
    @Override
    public void disconnect()
    {
      BluetoothLowEnergyService.this.disconnect();
    }

    @Override
    public int askModulOnlineStatus()
    {
/*
      if( btEventHandler != null )
      {
        btEventHandler.obtainMessage(mConnectionState, null).sendToTarget();
      }
*/
      return (mConnectionState);
    }

    /**
     * Frage welches Modul verbunden ist
     *
     * @return Moduladresse oder NULL
     */
    @Override
    public BluetoothDevice askConnectedModul()
    {
      if( mConnectionState == ProjectConst.STATUS_CONNECTED )
      {

        if( !mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT).isEmpty() )
        {
          if( !mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT).get(0).getAddress().isEmpty() )
          {
            return (mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT).get(0));
          }
        }
      }
      return null;
    }

    /**
     * Frage (noch einmal) nach dem Modultyp
     */
    @Override
    public void askModulForType()
    {
      BluetoothLowEnergyService.this.askModulForType();
    }

    /**
     * Fragt das Modul nach seinem Namen
     */
    @Override
    public void askModulForName()
    {
      BluetoothLowEnergyService.this.askModulForName();
    }

    /**
     * Frage das Modul nach der aktuellen RGBW Einstellung (Roh)
     */
    @Override
    public void askModulForRGBW()
    {
      BluetoothLowEnergyService.this.askModulForRGBW();
    }

    /**
     * Schaltet das Modul dunkel oder hell
     */
    @Override
    public void setModulPause()
    {
      BluetoothLowEnergyService.this.setModulPause();
    }

    /**
     * Setze Farben als RGB
     *
     * @param rgbw RGB Werte
     */
    @Override
    public void setModulRawRGBW(short[] rgbw)
    {
      BluetoothLowEnergyService.this.setModulRawRGBW(rgbw);
    }

    /**
     * Setze Farben als RGB, Modul kalibriert nach RGBW
     *
     * @param rgbw RGB Werte, White wird ignoriert
     */
    @Override
    public void setModulRGB4Calibrate(short[] rgbw)
    {
      BluetoothLowEnergyService.this.setModulRGB4Calibrate(rgbw);
    }
  }
}
