package de.dmarcini.bt.btlehomelight.utils;

import android.bluetooth.BluetoothDevice;

import de.dmarcini.bt.btlehomelight.ProjectConst;

/**
 * Created by dmarc on 24.02.2016.
 */
public class BlueThoothMessage
{
  private int msgType    = ProjectConst.MESSAGE_NONE;
  private int resourceId = 0;
  private String          data;
  private long            timeStamp;
  private BluetoothDevice device;

  /**
   * nicht benutzbar!
   */
  private BlueThoothMessage()
  {
  }

  /**
   * Message mit nur TYP als Parameter
   *
   * @param msgType Typ der Nachricht (siehe ProjectConst)
   */
  public BlueThoothMessage(int msgType)
  {
    this.msgType = msgType;
    this.data = null;
    this.timeStamp = System.currentTimeMillis();
    this.device = null;
  }

  /**
   * Message mit TYP und ResourcenID als Parameter
   *
   * @param msgType Typ der Nachricht
   * @param resId String Resourcen-Id
   */
  public BlueThoothMessage(int msgType, int resId )
  {
    this.msgType = msgType;
    this.resourceId = resId;
    this.data = null;
    this.timeStamp = System.currentTimeMillis();
    this.device = null;
  }

  /**
   * Message mit Typ, Strig ResourceId und Timestamp
   *
   * @param msgType Typ der Nachricht
   * @param resId String Resourcen-Id
   * @param timeStamp Zeitstempel
   */
  public BlueThoothMessage(int msgType, int resId, long timeStamp )
  {
    this.msgType = msgType;
    this.resourceId = resId;
    this.data = null;
    this.timeStamp = timeStamp;
    this.device = null;
  }

  /**
   * Message mit Typ und Daten als Parameter
   *
   * @param msgType Typ der Nachricht (siehe ProjectConst)
   * @param data    empfangene Daten
   */
  public BlueThoothMessage(int msgType, String data)
  {
    this.msgType = msgType;
    this.data = data;
    this.timeStamp = System.currentTimeMillis();
    this.device = null;
  }

  /**
   * Message mit Ty, Daten und Zeitstempel
   *
   * @param msgType   Typ der Nachricht (siehe ProjectConst)
   * @param data      empfangene Daten
   * @param timeStamp Zeitstempel der Nachricht explizit
   */
  public BlueThoothMessage(int msgType, String data, long timeStamp)
  {
    this.msgType = msgType;
    this.data = data;
    this.timeStamp = timeStamp;
    this.device = null;
  }

  /**
   * Message mit Typ, BTLE-Characteristic und Timestamp
   *
   * @param msgType   Typ der Nachricht (siehe ProjectConst)
   * @param device    Das Bluethoothgerät
   * @param timeStamp Zeitstempel der Nachricht explizit
   */
  public BlueThoothMessage(int msgType, BluetoothDevice device, long timeStamp)
  {
    this.msgType = msgType;
    this.data = null;
    this.timeStamp = timeStamp;
    this.device = device;
  }

  /**
   * Message mit Typ, BTLE-Characteristic und Timestamp
   *
   * @param msgType Typ der Nachricht (siehe ProjectConst)
   * @param device  Das Bluethoothgerät
   */
  public BlueThoothMessage(int msgType, BluetoothDevice device)
  {
    this.msgType = msgType;
    this.data = null;
    this.timeStamp = System.currentTimeMillis();
    this.device = device;
  }

  public int getResourceId()
  {
    return resourceId;
  }

  public BluetoothDevice getDevice()
  {
    return device;
  }

  public int getMsgType()
  {
    return msgType;
  }

  public String getData()
  {
    return data;
  }

  public long getTimeStamp()
  {
    return timeStamp;
  }
}
