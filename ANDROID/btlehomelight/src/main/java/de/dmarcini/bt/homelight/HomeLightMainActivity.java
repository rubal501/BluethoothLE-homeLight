/*
 *   project: BlueThoothLE
 *   programm: Home Light control (Bluethooth LE with HM-10)
 *   purpose:  control home lights via BT (color and brightness)
 *   Copyright (C) 2015  Dirk Marciniak
 *   file: HomeLightMainActivity.java
 *   last modified: 19.12.15 17:17
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/
 *
 */

package de.dmarcini.bt.homelight;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.dmarcini.bt.homelight.interrfaces.IBtEventHandler;
import de.dmarcini.bt.homelight.interrfaces.IMainAppServices;
import de.dmarcini.bt.homelight.service.BluetoothLowEnergyService;
import de.dmarcini.bt.homelight.utils.BTReaderThread;
import de.dmarcini.bt.homelight.utils.BluetoothConfig;
import de.dmarcini.bt.homelight.utils.CircularByteBuffer;
import de.dmarcini.bt.homelight.utils.HM10GattAttributes;
import de.dmarcini.bt.homelight.utils.ProjectConst;
import de.dmarcini.bt.homelight.utils.SelectPagesAdapter;

public class HomeLightMainActivity extends AppCompatActivity implements IMainAppServices, ViewPager.OnPageChangeListener
{
  private static final String             TAG          = HomeLightMainActivity.class.getSimpleName();
  final                IntentFilter       intentFilter = new IntentFilter();
  private final        CircularByteBuffer ringBuffer   = new CircularByteBuffer(1024);
  private              BluetoothConfig    btConfig     = new BluetoothConfig();
  private BTReaderThread     readerThread;
  private SelectPagesAdapter mSectionsPagerAdapter;
  private ViewPager          mViewPager;
  //
  // verwaltung des Lebenszyklus des Servicves
  //
  private final ServiceConnection mServiceConnection  = new ServiceConnection()
  {

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service)
    {
      btConfig.setBluetoothService((( BluetoothLowEnergyService.LocalBinder ) service).getService());
      if( !btConfig.getBluetoothService().initialize() )
      {
        Log.e(TAG, "Unable to initialize Bluetooth");
        finish();
      }
      // Automatically connects to the device upon successful start-up initialization.
      btConfig.getBluetoothService().connect(btConfig.getDeviceAddress());
      IBtEventHandler handler = ( IBtEventHandler ) (( SelectPagesAdapter ) (mViewPager.getAdapter())).getItem(mViewPager.getCurrentItem());
      handler.onServiceConnected();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)
    {
      btConfig.setBluetoothService(null);
      IBtEventHandler handler = ( IBtEventHandler ) (( SelectPagesAdapter ) (mViewPager.getAdapter())).getItem(mViewPager.getCurrentItem());
      handler.onServiceDisconnected();
    }
  };
  //
  //** Handles various events fired by the Service.
  // ACTION_GATT_CONNECTED: connected to a GATT server.
  // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
  // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
  // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
  //                        or notification operations.
  private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()
  {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      final String    action  = intent.getAction();
      IBtEventHandler handler = ( IBtEventHandler ) (( SelectPagesAdapter ) (mViewPager.getAdapter())).getItem(mViewPager.getCurrentItem());
      //
      if( BluetoothLowEnergyService.ACTION_GATT_CONNECTED.equals(action) )
      {
        //
        // BT Gerät wurde verbunden
        //
        btConfig.setConnected(true);
        handler.onBTConnected();
        invalidateOptionsMenu();
      }
      else if( BluetoothLowEnergyService.ACTION_GATT_DISCONNECTED.equals(action) )
      {
        //
        // BT Gerät wurde getrennt
        btConfig.setConnected(false);
        ringBuffer.clear();
        handler.onBTDisconnected();
        invalidateOptionsMenu();
      }
      else if( BluetoothLowEnergyService.ACTION_GATT_SERVICES_DISCOVERED.equals(action) )
      {
        //
        // finde die unterstützten Services und Characteristica, ich suche UART
        //
        reconGattServices(btConfig.getBluetoothService().getSupportedGattServices());
        handler.onBTServicesRecived(btConfig.getBluetoothService().getSupportedGattServices());
      }
      else if( BluetoothLowEnergyService.ACTION_DATA_AVAILABLE.equals(action) )
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
          ringBuffer.getOutputStream().write(intent.getStringExtra(BluetoothLowEnergyService.EXTRA_DATA).getBytes());
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
  };

  /**
   * Ein Interface für eine Funktion, welche das fertige Kommando empfängt
   */
  public interface CommandReciver
  {
    /**
     * Empfange das Kommando vom entfernten Gerät
     *
     * @param cmd Kommandostring ohne STX/ETX
     */
    void reciveCommand(String cmd);
  }

  /**
   * Implementiere den Callback mit dem Interface zum Empfang der Kommandosequenz
   * und Weiterleitung an den Empfänger...
   */
  private final CommandReciver CReciver = new CommandReciver()
  {
    @Override
    public void reciveCommand(String cmd)
    {
      //
      // finde das aktuelle Fragment und sende die Nachricht
      //
      IBtEventHandler handler = ( IBtEventHandler ) (( SelectPagesAdapter ) (mViewPager.getAdapter())).getItem(mViewPager.getCurrentItem());
      handler.onBTDataAvaiable(cmd);
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    Log.v(TAG, "erzeuge Application...");
    setContentView(R.layout.activity_home_light_main);

    //
    // erzeuge INTENT Filter für BT
    //
    intentFilter.addAction(BluetoothLowEnergyService.ACTION_GATT_CONNECTED);
    intentFilter.addAction(BluetoothLowEnergyService.ACTION_GATT_DISCONNECTED);
    intentFilter.addAction(BluetoothLowEnergyService.ACTION_GATT_SERVICES_DISCOVERED);
    intentFilter.addAction(BluetoothLowEnergyService.ACTION_DATA_AVAILABLE);

    //##############################################################################################
    //
    // Ist Bluethooth LE (4.0) unterstützt?
    //
    if( !getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) )
    {
      Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
      finish();
    }
    //
    // initialisiere den Adapter
    //
    final BluetoothManager bluetoothManager = ( BluetoothManager ) getSystemService(Context.BLUETOOTH_SERVICE);
    btConfig.setBluetoothAdapter(bluetoothManager.getAdapter());
    //
    // ISt ein BT Adapter vorhanden?
    //
    if( btConfig.getBluethoothAdapter() == null )
    {
      Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    //
    // Broadcast Reciver für andere Fragmente zugänglich machen
    //
    btConfig.setGattUpdateReceiver(mGattUpdateReceiver);

    //##############################################################################################
    //
    // Erzeuge einen Select-Adapter zur Erzeugung und Rückgabe der angeforderten Fragmente
    //
    mSectionsPagerAdapter = new SelectPagesAdapter(getSupportFragmentManager(), getApplicationContext(), btConfig);
    // Initialisiere den Pager mit dem Adapter
    mViewPager = ( ViewPager ) findViewById(R.id.container);
    mViewPager.setAdapter(mSectionsPagerAdapter);
    mViewPager.addOnPageChangeListener(this);

    //
    // Vorerst nur der Platzhalter für ein Spielerchen später
    //
    FloatingActionButton fab = ( FloatingActionButton ) findViewById(R.id.fabOnOff);
    fab.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        setModulOnOff();
        //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
      }
    });
    //
    // Der Service muss noch gestartet werden
    //
    Intent gattServiceIntent = new Intent(this, BluetoothLowEnergyService.class);
    bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    //
    // der Reader-Thread wird hier in der haupt-Activity gestartet
    //
    readerThread = new BTReaderThread(ringBuffer, CReciver);
    Thread rThread = new Thread(readerThread, "reader_thread");
    rThread.start();
    Log.v(TAG, "erzeuge Application...OK");
  }

  @Override
  protected void onDestroy()
  {
    super.onDestroy();
    unbindService(mServiceConnection);
    btConfig.setBluetoothService(null);
    readerThread.doStop();
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    Log.v(TAG, "onResume()");
    //
    // Stelle sicher, dass der BT Adapter aktiviert wurde
    // erzeuge einen Intend (eine Absicht) und schicke diese an das System
    //
    if( !btConfig.getBluethoothAdapter().isEnabled() )
    {
      //
      // erzeuge die Nachricht ans System, der "Rest" ist dann bei onActivityResult
      //
      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBtIntent, ProjectConst.REQUEST_ENABLE_BT);
    }
    else
    {
      //
      // versuche zu verbinden (wenn das was zu verbinden ist)
      //
      tryReconnectToDevice();
    }
  }

  private void tryReconnectToDevice()
  {
    registerReceiver(mGattUpdateReceiver, intentFilter);
    if( (btConfig.getBluetoothService() != null) && (btConfig.getDeviceAddress() != null) )
    {
      final boolean result = btConfig.getBluetoothService().connect(btConfig.getDeviceAddress());
      Log.d(TAG, "Connect request result=" + result);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    //
    // Das Ergebnis der Anfrage an den Geneigten User zum Einschaklten der BT Schnittstelle
    //
    if( requestCode == ProjectConst.REQUEST_ENABLE_BT )
    {
      switch( resultCode )
      {
        case Activity.RESULT_CANCELED:
          finish();
          return;

        case Activity.RESULT_OK:
          tryReconnectToDevice();
          break;

        default:
          finish();
          return;
      }
    }
    //
    // nix für mich
    //
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    Log.v(TAG, "onPause()");
    try
    {
      unregisterReceiver(mGattUpdateReceiver);
    }
    catch( IllegalArgumentException ex )
    {
      Log.e(TAG, "Error while unregister Reciver: " + ex.getLocalizedMessage());
    }
  }

  /**
   * Iteriere durch die gefundenen Servivces des entfernten Gerätes und finde UART Service
   *
   * @param gattServices
   */
  private void reconGattServices(List<BluetoothGattService> gattServices)
  {
    if( gattServices == null )
    {
      return;
    }
    String                             uuid                 = null;
    String                             unknownServiceString = getResources().getString(R.string.ble_unknown_service);
    ArrayList<HashMap<String, String>> gattServiceData      = new ArrayList<HashMap<String, String>>();


    //
    // durchsuche die verfügbaren Services
    //
    for( BluetoothGattService gattService : gattServices )
    {
      HashMap<String, String> currentServiceData = new HashMap<String, String>();
      uuid = gattService.getUuid().toString();
      currentServiceData.put(ProjectConst.LIST_NAME, HM10GattAttributes.lookup(uuid, unknownServiceString));

      //
      // Gibt es den UART Servive, dann gib Bescheid!
      //
      if( HM10GattAttributes.lookup(uuid, unknownServiceString) == "HM 10 Serial" )
      {
        btConfig.setIsUART(true);
      }
      else
      {
        btConfig.setIsUART(false);
      }
      currentServiceData.put(ProjectConst.LIST_UUID, uuid);
      gattServiceData.add(currentServiceData);

      // get characteristic when UUID matches RX/TX UUID
      btConfig.setCharacteristicTX(gattService.getCharacteristic(ProjectConst.UUID_HM_RX_TX));
      btConfig.setCharacteristicRX(gattService.getCharacteristic(ProjectConst.UUID_HM_RX_TX));
      //
      // wenn die Kommunikation sichergestellt ist, frage nach dem Modul
      //
      askModulForType();
    }

  }

  @Override
  public void switchToFragment(int position)
  {
    //
    // wechsle zur angeforderten Page
    //
    mViewPager.setCurrentItem(position, true);
  }

  public void askModulForType()
  {
    String kommandoString;
    //
    if( btConfig.isConnected() && btConfig.getCharacteristicTX() != null && btConfig.getCharacteristicRX() != null )
    {
      // Kommando zusammenbauen
      kommandoString = String.format(Locale.ENGLISH, "%s%02X%s", ProjectConst.STX, ProjectConst.C_ASKTYP, ProjectConst.ETX);
      Log.d(TAG, "send ask for type =" + kommandoString);
      byte[] tx = kommandoString.getBytes();
      btConfig.getCharacteristicTX().setValue(tx);
      btConfig.getBluetoothService().writeCharacteristic(btConfig.getCharacteristicTX());
      btConfig.getBluetoothService().setCharacteristicNotification(btConfig.getCharacteristicRX(), true);
    }
  }

  @Override
  public void askModulForName()
  {
    String kommandoString;
    //
    if( btConfig.isConnected() && btConfig.getCharacteristicTX() != null && btConfig.getCharacteristicRX() != null )
    {
      // Kommando zusammenbauen
      kommandoString = String.format(Locale.ENGLISH, "%s%02X%s", ProjectConst.STX, ProjectConst.C_ASKNAME, ProjectConst.ETX);
      Log.d(TAG, "send ask for name =" + kommandoString);
      byte[] tx = kommandoString.getBytes();
      btConfig.getCharacteristicTX().setValue(tx);
      btConfig.getBluetoothService().writeCharacteristic(btConfig.getCharacteristicTX());
      btConfig.getBluetoothService().setCharacteristicNotification(btConfig.getCharacteristicRX(), true);
    }
  }

  @Override
  public void askModulForRGBW()
  {
    String kommandoString;
    //
    if( btConfig.isConnected() && btConfig.getCharacteristicTX() != null && btConfig.getCharacteristicRX() != null )
    {
      // Kommando zusammenbauen
      kommandoString = String.format(Locale.ENGLISH, "%s%02X%s", ProjectConst.STX, ProjectConst.C_ASKRGB, ProjectConst.ETX);
      Log.d(TAG, "send ask for rgbw =" + kommandoString);
      byte[] tx = kommandoString.getBytes();
      btConfig.getCharacteristicTX().setValue(tx);
      btConfig.getBluetoothService().writeCharacteristic(btConfig.getCharacteristicTX());
      btConfig.getBluetoothService().setCharacteristicNotification(btConfig.getCharacteristicRX(), true);
    }
  }

  @Override
  public void setModulRGBW(short[] rgbw)
  {
    String kommandoString;
    //
    if( btConfig.isConnected() && btConfig.getCharacteristicTX() != null && btConfig.getCharacteristicRX() != null )
    {
      // Kommando zusammenbauen
      kommandoString = String.format(Locale.ENGLISH, "%s%02X:%02X:%02X:%02X:%02X%s", ProjectConst.STX, ProjectConst.C_SETCOLOR, rgbw[ 0 ], rgbw[ 1 ], rgbw[ 2 ], rgbw[ 3 ], ProjectConst.ETX);
      Log.d(TAG, "send ask for type =" + kommandoString);
      byte[] tx = kommandoString.getBytes();
      btConfig.getCharacteristicTX().setValue(tx);
      btConfig.getBluetoothService().writeCharacteristic(btConfig.getCharacteristicTX());
      btConfig.getBluetoothService().setCharacteristicNotification(btConfig.getCharacteristicRX(), true);
    }
  }

  @Override
  public void setModulOnOff()
  {
    String kommandoString;
    //
    if( btConfig.isConnected() && btConfig.getCharacteristicTX() != null && btConfig.getCharacteristicRX() != null )
    {
      // Kommando zusammenbauen
      kommandoString = String.format(Locale.ENGLISH, "%s%02X%s", ProjectConst.STX, ProjectConst.C_ONOFF, ProjectConst.ETX);
      Log.d(TAG, "send light on/off =" + kommandoString);
      byte[] tx = kommandoString.getBytes();
      btConfig.getCharacteristicTX().setValue(tx);
      btConfig.getBluetoothService().writeCharacteristic(btConfig.getCharacteristicTX());
      btConfig.getBluetoothService().setCharacteristicNotification(btConfig.getCharacteristicRX(), true);
    }
  }

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
  {
    //Log.d(TAG, String.format(Locale.ENGLISH, "page %02d scrolled posOffset: %03.2f, pixels: %03d", position, positionOffset, positionOffsetPixels));
  }

  @Override
  public void onPageSelected(int position)
  {
    Log.d(TAG, String.format(Locale.ENGLISH, "page %02d selected", position));
    //
    // Gib dem Fragment order, dass es selektiert wurde
    //
    IBtEventHandler handler = ( IBtEventHandler ) (( SelectPagesAdapter ) (mViewPager.getAdapter())).getItem(mViewPager.getCurrentItem());
    handler.onPageSelected();
  }

  @Override
  public void onPageScrollStateChanged(int state)
  {
    Log.d(TAG, String.format(Locale.ENGLISH, "page scroll state %02d", state));
  }
}
