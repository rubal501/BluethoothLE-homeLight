package de.dmarcini.bt.btlehomelight.utils;

import android.bluetooth.BluetoothGattCharacteristic;

import de.dmarcini.bt.btlehomelight.ProjectConst;

/**
 * Created by dmarc on 24.02.2016.
 */
public class BlueThoothMessage
{
  private int msgType = ProjectConst.MESSAGE_NONE;
  private String data;
  private long   timeStamp;
  private BluetoothGattCharacteristic characteristic;
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
    this.characteristic = null;
    this.timeStamp = System.currentTimeMillis();
  }

  /**
   * Message mit Typ und Daten als Parameter
   *
   * @param msgType Typ der Nachricht (siehe ProjectConst)
   * @param data empfangene Daten
   */
  public BlueThoothMessage(int msgType, String data)
  {
    this.msgType = msgType;
    this.data = data;
    this.characteristic = null;
    this.timeStamp = System.currentTimeMillis();
  }

  /**
   * Message mit Typ und BTLE Characteristic
   *
   * @param msgType Typ der Nachricht (siehe ProjectConst)
   * @param characteristic eine BT Characteristic
   */
  public BlueThoothMessage(int msgType, BluetoothGattCharacteristic characteristic )
  {
    this.msgType = msgType;
    this.data = null;
    this.characteristic = characteristic;
    this.timeStamp = System.currentTimeMillis();
  }

  /**
   * Message mit Ty, Daten und Zeitstempel
   *
   * @param msgType Typ der Nachricht (siehe ProjectConst)
   * @param data empfangene Daten
   * @param timeStamp Zeitstempel der Nachricht explizit
   */
  public BlueThoothMessage(int msgType, String data, long timeStamp)
  {
    this.msgType = msgType;
    this.data = data;
    this.characteristic = null;
    this.timeStamp = timeStamp;
  }

  /**
   * Message mit Typ, BTLE-Characteristic und Timestamp
   * @param msgType Typ der Nachricht (siehe ProjectConst)
   * @param characteristic eine BT Characteristic
   * @param timeStamp Zeitstempel der Nachricht explizit
   */
  public BlueThoothMessage(int msgType, BluetoothGattCharacteristic characteristic, long timeStamp )
  {
    this.msgType = msgType;
    this.data = null;
    this.characteristic = characteristic;
    this.timeStamp = timeStamp;
  }

  public BluetoothGattCharacteristic getCharacteristic()
  {
    return characteristic;
  }

  ;

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
