package de.dmarcini.bt.btlehomelight.fragments;

import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import de.dmarcini.bt.btlehomelight.BuildConfig;
import de.dmarcini.bt.btlehomelight.ProjectConst;
import de.dmarcini.bt.btlehomelight.R;
import de.dmarcini.bt.btlehomelight.dialogs.EditModuleNameDialogFragment;
import de.dmarcini.bt.btlehomelight.interfaces.IBtCommand;
import de.dmarcini.bt.btlehomelight.utils.BTLEListAdapter;
import de.dmarcini.bt.btlehomelight.utils.BlueThoothMessage;
import de.dmarcini.bt.btlehomelight.utils.HM10GattAttributes;

/**
 * Created by dmarc on 28.02.2016.
 */
public class BTConnectFragment extends LightRootFragment implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener
{
  private static final String                     TAG             = BTConnectFragment.class.getSimpleName();
  private static final ArrayList<BluetoothDevice> foundDevices    = new ArrayList<>();
  private BTLEListAdapter mBTLEDeviceListAdapter;
  private TextView        discoverHeadLine;
  private ProgressBar     scanProgress;
  private Button          scanButton;
  private ListView        discoverList;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onCreate...");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    View rootView;

    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onCreateView...");
    }
    //
    // wenn kein Container vorhanden ist, dann gibts auch keinen View
    //
    if( container == null )
    {
      Log.e(TAG, "onCreateView: container is NULL ...");
      return (null);
    }
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "make connect fragment...");
    }
    rootView = inflater.inflate(R.layout.fragment_connect_and_discover, container, false);
    //
    // Adressen der GUI Objekte bestimmen
    //
    discoverHeadLine = ( TextView ) rootView.findViewById(R.id.discoverHeadLine);
    scanProgress = ( ProgressBar ) rootView.findViewById(R.id.scanProgress);
    scanButton = ( Button ) rootView.findViewById(R.id.scanButton);
    discoverList = ( ListView ) rootView.findViewById(R.id.discoverList);
    mBTLEDeviceListAdapter = new BTLEListAdapter(getActivity());
    discoverList.setAdapter(mBTLEDeviceListAdapter);
    discoverList.setOnItemClickListener(this);
    discoverList.setOnItemLongClickListener(this);
    scanButton.setOnClickListener(this);
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, "onCreateView...OK");
    }
    return (rootView);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState)
  {
    super.onActivityCreated(savedInstanceState);
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onActivityCreated: ...");
    }
    runningActivity = ( IBtCommand ) getActivity();
  }

  @Override
  public void onResume()
  {
    super.onResume();
    //
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onResume: ...");
    }
    if( runningActivity != null )
    {
      BluetoothDevice dev = runningActivity.getConnectedModul();
      if( dev != null )
      {
        mBTLEDeviceListAdapter.addDevice(dev);
        mBTLEDeviceListAdapter.setConnectedDevice(dev);
        mBTLEDeviceListAdapter.notifyDataSetChanged();
        prepareHeader();
      }
    }
  }

  /**
   * Behandle alle ankommenden Nachrichten
   * <p/>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  @Override
  public void handleMessages(BlueThoothMessage msg)
  {
    switch( msg.getMsgType() )
    {
      case ProjectConst.MESSAGE_NONE:
      case ProjectConst.MESSAGE_TICK:
        break;

      case ProjectConst.MESSAGE_DISCONNECTED:
        msgDisconnected(msg);
        break;

      case ProjectConst.MESSAGE_CONNECTING:
        msgConnecting(msg);
        break;

      case ProjectConst.MESSAGE_CONNECTED:
        msgConnected(msg);
        break;

      case ProjectConst.MESSAGE_BTLE_DEVICE_DISCOVERING:
        msgDiscovering(msg);
        break;

      case ProjectConst.MESSAGE_BTLE_DEVICE_DISCOVERED:
        msgBtLeDeviceDiscovered(msg);
        break;

      case ProjectConst.MESSAGE_BTLE_DEVICE_END_DISCOVERING:
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "recive MESSAGE_BTLE_DEVICE_END_DISCOVERING");
        }
        break;

      case ProjectConst.MESSAGE_GATT_SERVICES_DISCOVERED:
        break;

      case ProjectConst.MESSAGE_BTLE_DATA:
        msgDataRecived( msg );
        break;

      default:
        if( BuildConfig.DEBUG )
        {
          Log.v(TAG, "recive unhandled Message...:" + msg.getMsgType());
        }
        break;
    }
  }

  /**
   * Wenn ein Gerät gefunden wurde und hinzugefügt wird
   *
   * @param msg
   */
  private void msgBtLeDeviceDiscovered(BlueThoothMessage msg)
  {
    //
    // Gerät noch nicht in der Liste
    //
    BluetoothDevice dev = msg.getDevice();
    if( dev != null )
    {
      mBTLEDeviceListAdapter.addDevice(dev);
      //
      // ist das Modul verbunden?
      //
      if( runningActivity.getConnectedModul() != null && runningActivity.getConnectedModul().equals(dev.getAddress()) )
      {
        mBTLEDeviceListAdapter.setConnectedDevice(dev);
      }
      mBTLEDeviceListAdapter.notifyDataSetChanged();
    }
  }

  public void msgDiscovering(BlueThoothMessage msg)
  {
    prepareHeader();
  }

  /**
   * Behandle ankommende Nachricht über den Versuch eine Verbindung aufzubauen
   * <p/>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  @Override
  public void msgConnecting(BlueThoothMessage msg)
  {
    prepareHeader();
  }

  /**
   * Behandle Nachricht über den erfolgreichen Aufbau einer Verbindung zum BT Gerät
   * <p/>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  @Override
  public void msgConnected(BlueThoothMessage msg)
  {
    //
    // Versuche das Verbundene Gerät in der Liste zu finden und zu markieren
    //
    if( mBTLEDeviceListAdapter != null && msg != null )
    {
      for( int i = 0; i < mBTLEDeviceListAdapter.getCount(); i++ )
      {
        BluetoothDevice btDev = mBTLEDeviceListAdapter.getDevice(i);
        if( btDev.getAddress().equals(msg.getDevice().getAddress()) )
        {
          Log.i(TAG, "connected marked");
          mBTLEDeviceListAdapter.setConnectedDevice(btDev);
          discoverList.setAdapter(mBTLEDeviceListAdapter);
          break;
        }
      }
    }
    prepareHeader();
    // TODO: Nach dem Verbinden nach RGBW fragen... private Handler mHandler         = new Handler();
  }

  /**
   * Behandle Nachricht über den Verlust der BT-Verbindung
   * <p/>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  @Override
  public void msgDisconnected(BlueThoothMessage msg)
  {
    //
    // Kein Gerät als verbunden kennzeichnen
    //
    if( mBTLEDeviceListAdapter != null )
    {
      mBTLEDeviceListAdapter.setConnectedDevice(null);
      discoverList.setAdapter(mBTLEDeviceListAdapter);
    }
    prepareHeader();
  }

  /**
   * Daten angekommen...
   *
   * @param msg
   */
  @Override
  public void msgDataRecived( BlueThoothMessage msg )
  {
    if( msg.getData() != null )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, "data recived! <" + msg.getData() + ">");
      }
    }
    else
    {
      Log.w(TAG, "NO DATA!");
    }
  }

  /**
   * Reaktion aufgerufener Dialoge POSITIV
   *
   * @param dialog der Dialog, welcher aufrief
   */
  @Override
  public void onDialogPositiveClick(DialogFragment dialog)
  {

  }

  /**
   * Reaktion aufgerufener Dialoge NEGATIV
   *
   * @param dialog der Dialog, welcher aufrief
   */
  @Override
  public void onDialogNegativeClick(DialogFragment dialog)
  {

  }

  /**
   * Behandle TICK-Nachricht vom Service
   * <p/>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  @Override
  public void msgRecivedTick(BlueThoothMessage msg)
  {

  }

  /**
   * Behandle die Nachricht vom Service, dass der Verbindungsversuch erfolglos war
   * <p/>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  @Override
  public void msgConnectError(BlueThoothMessage msg)
  {
    // TODO: Fehlermeldung aus der message anzeigen

  }

  /**
   * Behandle die _Nachricht, dass es einen Timeout beim schreiben zum BT-Gerät gab
   * <p/>
   * Stand: 16.11.2013
   *
   * @param msg
   */
  @Override
  public void msgReciveWriteTmeout(BlueThoothMessage msg)
  {

  }

  /**
   * Called when a view has been clicked.
   *
   * @param clickedView The view that was clicked.
   */
  @Override
  public void onClick(View clickedView)
  {
    String filters[] = {HM10GattAttributes.HM_RXTX_UUID.toString()  }; // Suche nur nach Modulen mit dieser Kennung
    //
    // Checke mal, ob das was für mich ist
    //
    if( clickedView.getId() == R.id.scanButton )
    {
      // Feedback geben
      clickedView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
      //
      // Abhängig vom Status
      //
      switch( runningActivity.getModulOnlineStatus() )
      {
        case ProjectConst.STATUS_CONNECT_ERROR:
        case ProjectConst.STATUS_DISCONNECTED:
          foundDevices.clear();
          runningActivity.discoverDevices(filters);
          prepareHeader();
          break;

        case ProjectConst.STATUS_CONNECTING:
        case ProjectConst.STATUS_CONNECTED:
          foundDevices.clear();
          runningActivity.disconnect();
          //runningActivity.discoverDevices(filters);
          prepareHeader();
          break;

        case ProjectConst.STATUS_DISCOVERING:
          runningActivity.stopDiscoverDevices();
          prepareHeader();
          break;

        default:
          foundDevices.clear();
          Log.e(TAG, "unknown connection status, connect programmer!");
          prepareHeader();
          //TODO: Usernachricht
      }
    }
  }

  private void prepareHeader()
  {
    switch( runningActivity.getModulOnlineStatus() )
    {
      case ProjectConst.STATUS_CONNECTING:
        scanProgress.setVisibility(View.VISIBLE);
        break;

      case ProjectConst.STATUS_CONNECT_ERROR:
      case ProjectConst.STATUS_DISCONNECTED:
        // getrennt
        discoverHeadLine.setText(getResources().getString(R.string.bt_connect_headline_devices));
        scanButton.setText(getResources().getString(R.string.bt_connect_headline_search));
        scanProgress.setVisibility(View.INVISIBLE);
        break;

      case ProjectConst.STATUS_CONNECTED:
        // Verbunden
        discoverHeadLine.setText(getResources().getString(R.string.bt_connect_headline_devices));
        scanButton.setText(getResources().getString(R.string.bt_connect_disconnect));
        scanProgress.setVisibility(View.INVISIBLE);
        break;

      case ProjectConst.STATUS_DISCOVERING:
        discoverHeadLine.setText(getResources().getString(R.string.bt_connect_headline_search));
        scanButton.setText(getResources().getString(R.string.bt_connect_discovering_stop));
        scanProgress.setVisibility(View.VISIBLE);
        break;

      default:
        scanButton.setText(getResources().getString(R.string.bt_connect_discovering_scan));
        scanProgress.setVisibility(View.INVISIBLE);
    }
  }

  /**
   * Callback method to be invoked when an item in this AdapterView has
   * been clicked.
   * <p/>
   * Implementers can call getItemAtPosition(position) if they need
   * to access the data associated with the selected item.
   *
   * @param parent      The AdapterView where the click happened.
   * @param clickedView The view within the AdapterView that was clicked (this
   *                    will be a view provided by the adapter)
   * @param position    The position of the view in the adapter.
   * @param id          The row id of the item that was clicked.
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View clickedView, int position, long id)
  {
    // Feedback geben
    clickedView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    //
    final BluetoothDevice device = mBTLEDeviceListAdapter.getDevice(position);
    if( device == null )
    {
      Log.e(TAG, "not device found an clicked position!");
      return;
    }
    if( BuildConfig.DEBUG )
    {
      Log.v(TAG, String.format(Locale.ENGLISH, "connect to device %s...", device.getAddress()));
    }
    //
    // wenn er noch am scannen ist, erst mal abschalten
    //
    switch( runningActivity.getModulOnlineStatus() )
    {
      case ProjectConst.STATUS_DISCOVERING:
        runningActivity.stopDiscoverDevices();
        break;
      case ProjectConst.STATUS_CONNECTING:
      case ProjectConst.STATUS_CONNECTED:
        runningActivity.disconnect();
        break;
    }
    Log.d(TAG, String.format(Locale.ENGLISH, "try BTLE connect to device <%s> <%s>...", device.getName(), device.getAddress()));
    runningActivity.connectTo(device.getAddress());
  }

  /**
   * Callback method to be invoked when an item in this view has been
   * clicked and held.
   * <p/>
   * Implementers can call getItemAtPosition(position) if they need to access
   * the data associated with the selected item.
   *
   * @param parent   The AbsListView where the click happened
   * @param clickedView     The view within the AbsListView that was clicked
   * @param position The position of the view in the list
   * @param id       The row id of the item that was clicked
   * @return true if the callback consumed the long click, false otherwise
   * //TODO: Aktionen zum Gerät ausführen
   */
  @Override
  public boolean onItemLongClick(AdapterView<?> parent, View clickedView, int position, long id)
  {
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
    if( runningActivity.getConnectedModul() != null  && runningActivity.getConnectedModul().getAddress().equals(device.getAddress()) )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, String.format(Locale.ENGLISH, "propertys for device %s...", device.getAddress()));
      }
      EditModuleNameDialogFragment frag = new EditModuleNameDialogFragment();
      Bundle args = new Bundle();
      args.putString(EditModuleNameDialogFragment.MODULNAME, device.getName());
      frag.setArguments(args);
      frag.show(getActivity().getFragmentManager(), "changeModuleName");
    }
    return (true);
  }
}
