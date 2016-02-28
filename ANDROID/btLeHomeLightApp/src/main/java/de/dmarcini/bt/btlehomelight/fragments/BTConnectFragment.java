package de.dmarcini.bt.btlehomelight.fragments;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

import de.dmarcini.bt.btlehomelight.BuildConfig;
import de.dmarcini.bt.btlehomelight.ProjectConst;
import de.dmarcini.bt.btlehomelight.R;
import de.dmarcini.bt.btlehomelight.interfaces.IBtCommand;
import de.dmarcini.bt.btlehomelight.utils.BTLEListAdapter;
import de.dmarcini.bt.btlehomelight.utils.BlueThoothMessage;

/**
 * Created by dmarc on 28.02.2016.
 */
public class BTConnectFragment extends LightRootFragment implements View.OnClickListener
{
  private static final String                     TAG             = BTConnectFragment.class.getSimpleName();
  private static final ArrayList<BluetoothDevice> foundDevices    = new ArrayList<>();
  private              IBtCommand                 runningActivity = null;
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
      Log.d(TAG, "make brightness fragment...");
    }
    rootView = inflater.inflate(R.layout.fragment_connect_and_discover, container, false);
    //
    // Adressen der GUI Objekte bestimmen
    //
    discoverHeadLine = ( TextView ) rootView.findViewById(R.id.discoverHeadLine);
    scanProgress = ( ProgressBar ) rootView.findViewById(R.id.scanProgress);
    scanButton = ( Button ) rootView.findViewById(R.id.scanButton);
    discoverList = ( ListView ) rootView.findViewById(R.id.discoverList);
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
    runningActivity = ( IBtCommand ) getActivity();
    if( BuildConfig.DEBUG )
    {
      Log.d(TAG, "onActivityCreated: ...");
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

      case ProjectConst.MESSAGE_BTLE_DEVICE_DISCOVERED:
        msgBtLeDeviceDiscovered(msg);
        break;
      case ProjectConst.MESSAGE_GATT_SERVICES_DISCOVERED:
      case ProjectConst.MESSAGE_BTLE_CHARACTERISTIC:
        break;
    }
  }

  private void msgBtLeDeviceDiscovered(BlueThoothMessage msg)
  {
    BluetoothDevice           mDev, cDev;
    Iterator<BluetoothDevice> it = foundDevices.iterator();

    mDev = msg.getDevice();
    //
    // teste ob das Device schon vorhanden ist
    // Kriterium ist die adresse (sollte eindeutig sein)
    //
    while( it.hasNext() )
    {
      cDev = it.next();
      if( cDev.getAddress().equals(mDev.getAddress()) )
      {
        //
        // Das Teil ist schon hier, mache nix weiter!
        //
        return;
      }
    }
    //
    // Gerät noch nicht in der Liste
    //
    mBTLEDeviceListAdapter.addDevice(mDev);
    mBTLEDeviceListAdapter.notifyDataSetChanged();

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
    //
    // Checke mal, ob das was für mich ist
    //
    if( clickedView.getId() == R.id.scanButton )
    {
      // Feedback geben
      clickedView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
      switch( runningActivity.askModulOnlineStatus() )
      {
        case ProjectConst.STATUS_DISCONNECTED:
          foundDevices.clear();
          prepareHeader();
          runningActivity.discoverDevices(null);
          break;

        case ProjectConst.STATUS_CONNECTING:
        case ProjectConst.STATUS_CONNECTED:
          foundDevices.clear();
          prepareHeader();
          runningActivity.disconnect();
          runningActivity.discoverDevices(null);
          break;

        case ProjectConst.STATUS_DISCOVERING:
          prepareHeader();
          runningActivity.stopDiscoverDevices();
          break;

        case ProjectConst.STATUS_CONNECT_ERROR:
        default:
          prepareHeader();
          foundDevices.clear();
          Log.e(TAG, "connection error (not connected with service?");
          //TODO: Usernachricht
      }
    }
  }

  private void prepareHeader()
  {
    switch( runningActivity.askModulOnlineStatus() )
    {
      case ProjectConst.STATUS_CONNECTING:
        scanProgress.setVisibility(View.VISIBLE);
        break;

      case ProjectConst.STATUS_DISCONNECTED:
      case ProjectConst.STATUS_CONNECTED:
        // Verbunden
        discoverHeadLine.setText(getResources().getString(R.string.discovering_headline_devices));
        scanButton.setText(getResources().getString(R.string.discovering_disconnect));
        scanProgress.setVisibility(View.INVISIBLE);
        break;

      case ProjectConst.STATUS_DISCOVERING:
        discoverHeadLine.setText(getResources().getString(R.string.discovering_headline_search));
        scanButton.setText(getResources().getString(R.string.discovering_stop));
        break;

      case ProjectConst.STATUS_CONNECT_ERROR:
      default:
        scanButton.setText(getResources().getString(R.string.discovering_scan));
        scanProgress.setVisibility(View.INVISIBLE);
    }
  }
}
