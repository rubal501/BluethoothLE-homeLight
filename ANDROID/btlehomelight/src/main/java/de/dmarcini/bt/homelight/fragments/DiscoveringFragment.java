/******************************************************************************
 *                                                                            *
 *      project: ANDROID                                                      *
 *      module: btlehomelight                                                 *
 *      class: DiscoveringFragment                                            *
 *      date: 2016-01-15                                                      *
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

package de.dmarcini.bt.homelight.fragments;

import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.HapticFeedbackConstants;
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import de.dmarcini.bt.homelight.BuildConfig;
import de.dmarcini.bt.homelight.ProjectConst;
import de.dmarcini.bt.homelight.R;
import de.dmarcini.bt.homelight.SystemPrefActivity;
import de.dmarcini.bt.homelight.dialogs.EditModuleNameDialogFragment;
import de.dmarcini.bt.homelight.interrfaces.IFragmentInterface;
import de.dmarcini.bt.homelight.interrfaces.IMainAppServices;
import de.dmarcini.bt.homelight.utils.BTLEListAdapter;
import de.dmarcini.bt.homelight.utils.BluetoothModulConfig;
import de.dmarcini.bt.homelight.utils.HomeLightSysConfig;


/**
 * Das Fragment für die Gerätewahl
 */
public class DiscoveringFragment extends AppFragment implements IFragmentInterface, AdapterView.OnItemClickListener, View.OnClickListener, AdapterView.OnItemLongClickListener
{
  private static final ArrayList<BluetoothDevice> foundDevices = new ArrayList<>();
  private static       String                     TAG          = DiscoveringFragment.class.getSimpleName();
  private ListView        discoverListView;
  private Button          scanButton;
  private ProgressBar     scanProgress;
  private TextView        listHeadline;
  private BTLEListAdapter mBTLEDeviceListAdapter;
  private boolean isAttached;
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
          foundDevices.add(device);
          mBTLEDeviceListAdapter.addDevice(device);
          mBTLEDeviceListAdapter.notifyDataSetChanged();
        }
      });
    }
  };

  public DiscoveringFragment()
  {
    super();
    isAttached = false;
  }

  /**
   * Returns a new instance of this fragment for the given section
   * number.
   */
  public static DiscoveringFragment newInstance(int sectionNumber, BluetoothModulConfig btConfig)
  {
    DiscoveringFragment fragment = new DiscoveringFragment();
    fragment.setBlutethoothConfig(btConfig);
    Bundle args = new Bundle();
    args.putInt(ProjectConst.ARG_SECTION_NUMBER, sectionNumber);
    fragment.setArguments(args);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "%s.newInstance(%04d)", TAG, sectionNumber));
    }
    return fragment;
  }

  private void setBlutethoothConfig(BluetoothModulConfig btConfig)
  {
    this.btConfig = btConfig;
  }

  @Override
  public void onAttach(Context ctx)
  {
    super.onAttach(ctx);
    isAttached = true;
  }

  @Override
  public void onDetach()
  {
    super.onDetach();
    isAttached = false;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    SharedPreferences pref;
    //
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreateView...");
    }
    //
    // Objekte generieren und finden
    //
    View rootView = inflater.inflate(R.layout.fragment_home_discover, container, false);
    discoverListView = ( ListView ) rootView.findViewById(R.id.discoverList);
    listHeadline = ( TextView ) rootView.findViewById(R.id.discoverHeadLine);
    scanButton = ( Button ) rootView.findViewById(R.id.scanButton);
    scanProgress = ( ProgressBar ) rootView.findViewById(R.id.scanProgress);
    //
    mBTLEDeviceListAdapter = new BTLEListAdapter(getActivity());
    discoverListView.setAdapter(mBTLEDeviceListAdapter);
    discoverListView.setOnItemClickListener(this);
    discoverListView.setOnItemLongClickListener(this);
    scanButton.setOnClickListener(this);
    //
    // Letzes verbundenes Device lesen
    //
    HomeLightSysConfig.getLastConnectedDeviceAddr();
    pref = getActivity().getSharedPreferences(ProjectConst.COLOR_PREFS, Context.MODE_PRIVATE);
    if( (HomeLightSysConfig.getLastConnectedDeviceAddr() != null) && (HomeLightSysConfig.getLastConnectedDeviceName() != null) )
    {
      // TODO: Name des Gerätes noch einbringen
      mBTLEDeviceListAdapter.addDevice(btConfig.getBluethoothAdapter().getRemoteDevice(HomeLightSysConfig.getLastConnectedDeviceAddr()));
    }
    setHasOptionsMenu(true);
    //prepareHeader();
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreateView...OK");
    }
    return (rootView);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreateOptionsMenu...");
    }
    inflater.inflate(R.menu.menu_options_discovering, menu);
    prepareHeader();
  }

  /**
   * Setze die Headline entsprechend den Gegebenheiten
   */
  private void prepareHeader()
  {
    if( !isAttached )
    {
      return;
    }
    if( (btConfig != null) && (btConfig.isConnected()) )
    {
      // Verbunden
      listHeadline.setText(getResources().getString(R.string.discovering_headline_devices));
      scanButton.setText(getResources().getString(R.string.discovering_disconnect));
      scanProgress.setVisibility(View.INVISIBLE);
    }
    else
    {
      listHeadline.setText(getResources().getString(R.string.discovering_headline_search));
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
    //
    // Die Liste mit den bekannten Geräten füllen
    //
    if( mBTLEDeviceListAdapter != null )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "fill device list adapter....");
      }
      mBTLEDeviceListAdapter.clear();
      Iterator<BluetoothDevice> it = foundDevices.iterator();
      while( it.hasNext() )
      {
        BluetoothDevice dev = it.next();
        mBTLEDeviceListAdapter.addDevice(dev);
        //
        // die Gretchenrage: ist das Gerät verbunden?
        //
        if( btConfig != null && btConfig.isConnected() && btConfig.getDeviceAddress().equals(dev.getAddress()) )
        {
          mBTLEDeviceListAdapter.setConnectedDevice(dev);
        }
      }
      discoverListView.setAdapter(mBTLEDeviceListAdapter);
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
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onOptionsItemSelected...");
    }
    switch( item.getItemId() )
    {
      case R.id.menu_preferences:
        Log.v(TAG, "onOptionsItemSelected, preferences call...");
        Intent intent = new Intent(getActivity(), SystemPrefActivity.class);
        getActivity().startActivityForResult(intent, ProjectConst.REQUEST_SYS_PREFS);
        break;

      case R.id.menu_finish:
        getActivity().finish();
        break;
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * starte oder beende das Scannen nach BTLE Geräten
   *
   * @param enable starten oder beenden
   */
  private void scanBTLEDevice(final boolean enable)
  {
    if( enable )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "scanBTLEDevice START...");
      }
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
      foundDevices.clear();
      btConfig.getBluethoothAdapter().startLeScan(mLeScanCallback);
      prepareHeader();
    }
    else
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "scanBTLEDevice STOP...");
      }
      mScanning = false;
      btConfig.getBluethoothAdapter().stopLeScan(mLeScanCallback);
      prepareHeader();
    }
    getActivity().invalidateOptionsMenu();
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View clickedView, int position, long id)
  {
    // Feedback geben
    clickedView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    //
    final BluetoothDevice device = mBTLEDeviceListAdapter.getDevice(position);
    if( device == null )
    {
      return;
    }
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "connect to device %s...", device.getAddress()));
    }
    //
    // wenn er noch am scannen ist, erst mal abschalten
    //
    if( mScanning )
    {
      btConfig.getBluethoothAdapter().stopLeScan(mLeScanCallback);
      mScanning = false;
    }
    //
    // Wenn Ref auf activity da ist
    //
    if( mainServiceRef != null )
    {
      if( BuildConfig.DEBUG )
      {
        Log.i(TAG, "activity is instance of IMainAppServices!");
      }
      if( btConfig.isConnected() )
      {
        Log.d(TAG, "BT is connected, so first disconnect device...");
        btConfig.getBluetoothService().disconnect();
      }
      Log.d(TAG, "try BT connect device...");
      btConfig.setDeviceAddress(device.getAddress());
      btConfig.getBluetoothService().connect(device.getAddress());
      //
      // wechseln zur nächsten Seite dann, wenn die Services eingelesen sind
      //
    }
    else
    {
      Log.e(TAG, "callback type IMainAppServices not set!");
    }
  }

  @Override
  public void onPause()
  {
    super.onPause();
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onPause()");
    }

  }

  @Override
  public void onResume()
  {
    super.onResume();
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onResume()");
    }
    prepareHeader();
  }

  @Override
  public void onBTConnected()
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "BT Device connected!");
    }
    prepareHeader();
  }

  @Override
  public void onBTDisconnected()
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "BT Device disconnected!");
    }
    prepareHeader();
  }

  @Override
  public void onBTServicesRecived(List<BluetoothGattService> gattServices)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "BT Device services recived");
    }
    if( btConfig.isConnected() && btConfig.getCharacteristicTX() != null && btConfig.getCharacteristicRX() != null )
    {
      //
      // zur Vorzugsseite wechseln, wenn erwünscht und möglich
      //
      if( HomeLightSysConfig.isJumpToDefaultPageOnConnect() )
      {
        if( HomeLightSysConfig.getDefaultPageOnConnect() != -1 )
        {
          (( IMainAppServices ) getActivity()).switchToFragment(HomeLightSysConfig.getDefaultPageOnConnect());
        }
      }
    }
  }

  @Override
  public void onBTDataAvaiable(String[] data)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "BT Device data recived");
    }
  }

  @Override
  public void onServiceConnected()
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "BT Service connected");
    }
  }

  @Override
  public void onServiceDisconnected()
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "BT Service disconnected");
    }
  }

  @Override
  public void onPageSelected()
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "Page DISCOVERING was selected");
    }
    if( mainServiceRef == null )
    {
      Log.e(TAG, "can't set Callback handler to APP");
      return;
    }
    mainServiceRef.setHandler( this );
    prepareHeader();
  }

  /**
   * Der Dialog hat eine Positive Antwort
   *
   * @param frag Das Fragment( der Dialog )
   */
  @Override
  public void onPositiveDialogFragment(DialogFragment frag)
  {
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "positive answer...");
    }
    if( frag instanceof EditModuleNameDialogFragment )
    {
      // Das ist der Dialog, erfrage den Neuen Namen
      String newModuleName = (( EditModuleNameDialogFragment ) frag).getModuleName();
      if( (btConfig != null) && (newModuleName != btConfig.getModuleName()) )
      {
        Log.i(TAG, "try set new module name...");
        (( IMainAppServices ) getActivity()).setModuleName(newModuleName);
        // Nach einer Wartezeit Verbindung trennen!
        discoverListView.postDelayed(new Runnable()
        {
          public void run()
          {
            btConfig.getBluetoothService().disconnect();
          }
        }, 1200);
      }
    }
  }

  /**
   * Der Dialog hat eine Negative Antwort
   *
   * @param frag Das Fragment( der Dialog )
   */
  @Override
  public void onNegativeDialogFragment(DialogFragment frag)
  {

  }

  @Override
  public void onClick(View clickedView)
  {
    //
    // Checke mal, ob das was für mich ist
    //
    if( clickedView instanceof Button && clickedView.equals(scanButton) )
    {
      // Feedback geben
      clickedView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
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
      if( BuildConfig.DEBUG )
      {
        Log.i(TAG, "new orientation is PORTRAIT");
      }
    }
    else if( newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE )
    {
      if( BuildConfig.DEBUG )
      {
        Log.i(TAG, "new orientation is LANDSCAPE");
      }
    }
    else
    {
      Log.i(TAG, "new orientation is UNKNOWN");
    }
  }

  /**
   * Callback method to be invoked when an item in this view has been
   * clicked and held.
   * <p/>
   * Implementers can call getItemAtPosition(position) if they need to access
   * the data associated with the selected item.
   *
   * @param parent      The AbsListView where the click happened
   * @param clickedView The view within the AbsListView that was clicked
   * @param position    The position of the view in the list
   * @param id          The row id of the item that was clicked
   * @return true if the callback consumed the long click, false otherwise
   */
  @Override
  public boolean onItemLongClick(AdapterView<?> parent, View clickedView, int position, long id)
  {
    Log.v(TAG, String.format(Locale.ENGLISH, "long click on view pos: %d", position));
    // Feedback geben
    clickedView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    //
    final BluetoothDevice device = mBTLEDeviceListAdapter.getDevice(position);
    if( device == null )
    {
      return (true);
    }
    //
    // Jepp, hier ist ein BT Device
    //
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "device <%s>, named <%s> found pn ops %d", device.getAddress(), device.getName(), position));
    }
    //
    // ist das selektierte Gerät auch online?
    //
    if( btConfig != null && btConfig.isConnected() && btConfig.getDeviceAddress().equals(device.getAddress()) )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, String.format(Locale.ENGLISH, "propertys for device %s...", device.getAddress()));
      }
      EditModuleNameDialogFragment frag = new EditModuleNameDialogFragment();
      Bundle args = new Bundle();
      args.putString(ProjectConst.ARG_MODULE_NAME, device.getName());
      frag.setArguments(args);
      frag.show(getActivity().getFragmentManager(), "changeModuleName");
    }
    return (true);
  }
}
