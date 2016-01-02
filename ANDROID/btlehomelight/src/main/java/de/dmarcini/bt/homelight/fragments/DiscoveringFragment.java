/*
 * //@formatter:off
 *
 *     ANDROID
 *     btlehomelight
 *     DiscoveringFragment
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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;
import java.util.Locale;

import de.dmarcini.bt.homelight.BuildConfig;
import de.dmarcini.bt.homelight.R;
import de.dmarcini.bt.homelight.interrfaces.IBtEventHandler;
import de.dmarcini.bt.homelight.interrfaces.IMainAppServices;
import de.dmarcini.bt.homelight.utils.BTLEListAdapter;
import de.dmarcini.bt.homelight.utils.BluetoothConfig;
import de.dmarcini.bt.homelight.utils.ProjectConst;


/**
 * Created by dmarc on 22.08.2015.
 */
public class DiscoveringFragment extends AppFragment implements IBtEventHandler, AdapterView.OnItemClickListener, View.OnClickListener
{
  private static final String TAG = DiscoveringFragment.class.getSimpleName();
  private BluetoothConfig btConfig;
  private ListView        discoverListView;
  private Button          scanButton;
  private ProgressBar     scanProgress;
  private BTLEListAdapter mBTLEDeviceListAdapter;
  private Handler mHandler = new Handler();
  private boolean mScanning;
  private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
  {

    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord)
    {
      getActivity().runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          mBTLEDeviceListAdapter.addDevice(device);
          mBTLEDeviceListAdapter.notifyDataSetChanged();
        }
      });
    }
  };

  public DiscoveringFragment()
  {
    Bundle args;
    int    pos;

    try
    {
      args = getArguments();
      if( args != null )
      {
        pos = args.getInt(ProjectConst.ARG_SECTION_NUMBER, 0);
        if( BuildConfig.DEBUG )Log.v(TAG, String.format(Locale.ENGLISH, "Konstructor: id is %04d", pos));
      }
    }
    catch( NullPointerException ex )
    {
      Log.e(TAG, "Konstructor: " + ex.getLocalizedMessage());
    }
  }

  /**
   * Returns a new instance of this fragment for the given section
   * number.
   */
  public static DiscoveringFragment newInstance(int sectionNumber, BluetoothConfig btConfig)
  {
    DiscoveringFragment fragment = new DiscoveringFragment();
    fragment.setBlutethoothConfig(btConfig);
    Bundle args = new Bundle();
    args.putInt(ProjectConst.ARG_SECTION_NUMBER, sectionNumber);
    fragment.setArguments(args);
    if( BuildConfig.DEBUG )Log.v(TAG, String.format(Locale.ENGLISH, "DiscoveringFragment.newInstance(%04d)", sectionNumber));
    return fragment;
  }

  private void setBlutethoothConfig(BluetoothConfig btConfig)
  {
    this.btConfig = btConfig;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    if( BuildConfig.DEBUG )Log.v(TAG, "onCreateView...");
    //
    // Objekte generieren udn finden
    //
    View rootView = inflater.inflate(R.layout.fragment_home_discover, container, false);
    discoverListView = ( ListView ) rootView.findViewById(R.id.discoverList);
    scanButton = ( Button ) rootView.findViewById(R.id.scanButton);
    scanProgress = ( ProgressBar ) rootView.findViewById(R.id.scanProgress);
    //
    mBTLEDeviceListAdapter = new BTLEListAdapter(getActivity());
    discoverListView.setAdapter(mBTLEDeviceListAdapter);
    discoverListView.setOnItemClickListener(this);
    scanButton.setOnClickListener(this);
    setHasOptionsMenu(true);
    prepareHeader();
    if( BuildConfig.DEBUG )Log.v(TAG, "onCreateView...OK");
    return (rootView);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
  {
    if( BuildConfig.DEBUG )Log.v(TAG, "onCreateOptionsMenu...");
    inflater.inflate(R.menu.menu_home_light_main, menu);
    //
    if( !mScanning )
    {
      menu.findItem(R.id.menu_stop).setVisible(false);
      menu.findItem(R.id.menu_scan).setVisible(true);
    }
    else
    {
      menu.findItem(R.id.menu_stop).setVisible(true);
      menu.findItem(R.id.menu_scan).setVisible(false);
    }
    prepareHeader();
  }

  /**
   * Setze die Headline entsprechend den Gegebenheiten
   */
  private void prepareHeader()
  {
    if( (btConfig != null) && (btConfig.isConnected()) )
    {
      scanButton.setText(getResources().getString(R.string.discovering_disconnect));
      if( mBTLEDeviceListAdapter != null )
      {
        mBTLEDeviceListAdapter.clear();
      }
      scanProgress.setVisibility(View.INVISIBLE);
    }
    else
    {
      if( mScanning )
      {
        scanButton.setText(getResources().getString(R.string.discovering_stop));
        scanProgress.setVisibility(View.VISIBLE);
      }
      else
      {
        scanButton.setText(getResources().getString(R.string.discovering_scan));
        scanProgress.setVisibility(View.INVISIBLE);
      }

    }
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu)
  {
    super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch( item.getItemId() )
    {
      case R.id.menu_scan:
        mBTLEDeviceListAdapter.clear();
        scanBTLEDevice(true);
        break;
      case R.id.menu_stop:
        scanBTLEDevice(false);
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  private void scanBTLEDevice(final boolean enable)
  {
    if( enable )
    {
      if( BuildConfig.DEBUG )Log.v(TAG, "scanBTLEDevice START...");
      // Stops scanning after a pre-defined scan period.
      mHandler.postDelayed(new Runnable()
      {
        @Override
        public void run()
        {
          btConfig.getBluethoothAdapter().stopLeScan(mLeScanCallback);
          mScanning = false;
          prepareHeader();
          getActivity().invalidateOptionsMenu();
        }
      }, ProjectConst.SCAN_PERIOD);

      mScanning = true;
      btConfig.getBluethoothAdapter().startLeScan(mLeScanCallback);
      prepareHeader();
    }
    else
    {
      if( BuildConfig.DEBUG )Log.v(TAG, "scanBTLEDevice STOP...");
      mScanning = false;
      btConfig.getBluethoothAdapter().stopLeScan(mLeScanCallback);
      prepareHeader();
    }
    getActivity().invalidateOptionsMenu();
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    final BluetoothDevice device = mBTLEDeviceListAdapter.getDevice(position);
    if( device == null )
    {
      return;
    }
    if( BuildConfig.DEBUG )Log.v(TAG, String.format(Locale.ENGLISH, "connect to device %s...", device.getAddress()));
    //
    // wenn er noch am scannen ist, erst mal abschalten
    //
    if( mScanning )
    {
      btConfig.getBluethoothAdapter().stopLeScan(mLeScanCallback);
      mScanning = false;
    }
    //
    // Wenn die Activity meine Services kann
    //
    if( getActivity() instanceof IMainAppServices )
    {
      if( BuildConfig.DEBUG )Log.i(TAG, "activity is instance of IMainAppServices!");
      if( btConfig.isConnected() )
      {
        Log.d(TAG, "BT is connected, so first disconnect device...");
        btConfig.getBluetoothService().disconnect();
      }
      Log.d(TAG, "try BT connect device...");
      btConfig.setDeviceAddress( device.getAddress());
      btConfig.getBluetoothService().connect(device.getAddress());
      //
      // wechseln zur nächsten Seite dann, wenn die Services eingelesen sind
      //
    }
    else
    {
      Log.e(TAG, "activity is NOT instance of IMainAppServices!");
    }
  }

  @Override
  public void onPause()
  {
    super.onPause();
    if( BuildConfig.DEBUG )Log.v(TAG, "onPause()");

  }

  @Override
  public void onResume()
  {
    super.onResume();
    if( BuildConfig.DEBUG )Log.v(TAG, "onResume()");
  }

  @Override
  public void onBTConnected()
  {
    if( BuildConfig.DEBUG )Log.v(TAG, "BT Device connected!");
    prepareHeader();
  }

  @Override
  public void onBTDisconnected()
  {
    if( BuildConfig.DEBUG )Log.v(TAG, "BT Device disconnected!");
    prepareHeader();
  }

  @Override
  public void onBTServicesRecived(List<BluetoothGattService> gattServices)
  {
    if( BuildConfig.DEBUG )Log.v(TAG, "BT Device services recived");
    if( btConfig.isConnected() && btConfig.getCharacteristicTX() != null && btConfig.getCharacteristicRX() != null )
    {
      //
      // zur Vorzugsseite wechseln
      //
      (( IMainAppServices ) getActivity()).switchToFragment(ProjectConst.DEFAULT_CONNECT_PAGE);
    }
  }

  @Override
  public void onBTDataAvaiable(String data)
  {
    if( BuildConfig.DEBUG )Log.v(TAG, "BT Device data recived");
  }

  @Override
  public void onServiceConnected()
  {
    if( BuildConfig.DEBUG )Log.v(TAG, "BT Service connected");
  }

  @Override
  public void onServiceDisconnected()
  {
    if( BuildConfig.DEBUG )Log.v(TAG, "BT Service disconnected");
  }

  @Override
  public void onPageSelected()
  {
    if( BuildConfig.DEBUG )Log.v(TAG, "Page DISCOVERING was selected");
    prepareHeader();
    // TODO: ist das Ding verbunden, kann er nicht suchen
    // zeige das dem User
  }

  @Override
  public void onClick(View view)
  {
    //
    // Checke mal, ob das was für mich ist
    //
    if( view instanceof Button && view.equals(scanButton) )
    {
      if( btConfig.isConnected() )
      {
        btConfig.getBluetoothService().disconnect();
      }
      else
      {
        scanBTLEDevice(!mScanning);
      }
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig)
  {
    super.onConfigurationChanged(newConfig);
    if( newConfig.orientation == Configuration.ORIENTATION_PORTRAIT )
    {
      if( BuildConfig.DEBUG )Log.i(TAG, "new orientation is PORTRAIT");
    }
    else if( newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE )
    {
      if( BuildConfig.DEBUG )Log.i(TAG, "new orientation is LANDSCAPE");
    }
    else
    {
      Log.i(TAG, "new orientation is UNKNOWN");
    }
  }

}
