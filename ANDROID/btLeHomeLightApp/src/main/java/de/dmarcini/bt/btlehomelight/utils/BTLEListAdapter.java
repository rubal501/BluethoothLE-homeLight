/******************************************************************************
 * *
 * project: ANDROID                                                      *
 * module: btlehomelight                                                 *
 * class: BTLEListAdapter                                                *
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

package de.dmarcini.bt.btlehomelight.utils;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import de.dmarcini.bt.btlehomelight.BuildConfig;
import de.dmarcini.bt.btlehomelight.R;


// Adapter for holding devices found through scanning.
public class BTLEListAdapter extends BaseAdapter
{
  private static final String          TAG             = BTLEListAdapter.class.getSimpleName();
  private              BluetoothDevice connectedDevice = null;
  private ArrayList<BluetoothDevice> mLeDevices;
  private LayoutInflater             mInflator;

  public BTLEListAdapter(Activity act)
  {
    super();
    mLeDevices = new ArrayList<BluetoothDevice>();
    mLeDevices.clear();
    mInflator = act.getLayoutInflater();
  }


  /**
   * Setze das Gerät mit der Adresse connectedDevice auf "verbunden"
   *
   * @param connectedDevice das verbundene Gerät
   */
  public void setConnectedDevice(BluetoothDevice connectedDevice)
  {
    for( int i = 0; i < getCount(); i++ )
    {
      if( connectedDevice.equals(getDevice(i)) )
      {
        this.connectedDevice = getDevice(i);
        return;
      }
    }
    this.connectedDevice = null;
  }


  public void addDevice(BluetoothDevice device)
  {
    if( !mLeDevices.contains(device) )
    {
      if( BuildConfig.DEBUG )
      {
        Log.v(TAG, String.format(Locale.ENGLISH, "Adapter zufügen: %s...OK", device.getAddress()));
      }
      mLeDevices.add(device);
    }
  }

  public BluetoothDevice getDevice(int position)
  {
    return mLeDevices.get(position);
  }

  public void clear()
  {
    mLeDevices.clear();
    this.connectedDevice = null;
  }

  @Override
  public int getCount()
  {
    return mLeDevices.size();
  }

  @Override
  public Object getItem(int i)
  {
    return mLeDevices.get(i);
  }

  @Override
  public long getItemId(int i)
  {
    return i;
  }

  @Override
  public View getView(int position, View itemView, ViewGroup viewGroup)
  {
    ViewHolder viewHolder;
    // General ListView optimization code.
    if( itemView == null )
    {
      itemView = mInflator.inflate(R.layout.listitem_device, null);
      viewHolder = new ViewHolder();
      viewHolder.deviceAddress = ( TextView ) itemView.findViewById(R.id.device_address);
      viewHolder.deviceName = ( TextView ) itemView.findViewById(R.id.device_name);
      viewHolder.connectedView = ( ImageView ) itemView.findViewById(R.id.connectedView);
      itemView.setTag(viewHolder);
    }
    else
    {
      viewHolder = ( ViewHolder ) itemView.getTag();
    }

    BluetoothDevice device     = mLeDevices.get(position);
    final String    deviceName = device.getName();
    if( deviceName != null && deviceName.length() > 0 )
    {
      viewHolder.deviceName.setText(deviceName);
    }
    else
    {
      viewHolder.deviceName.setText(R.string.list_adapter_unknown_device);
    }
    viewHolder.deviceAddress.setText(device.getAddress());
    //
    // Wenn das Gerät als verbunden gekennzeichnet ist, BLAUES Logo nehmen
    //
    if( device.equals(connectedDevice) )
    {
      viewHolder.connectedView.setImageResource(R.drawable.ic_bluetooth_connected_blue_48dp);
    }
    else
    {
      viewHolder.connectedView.setImageResource(R.drawable.ic_bluetooth_white_48dp);
    }
    return itemView;
  }
}

class ViewHolder
{
  ImageView connectedView;
  TextView  deviceName;
  TextView  deviceAddress;
}
