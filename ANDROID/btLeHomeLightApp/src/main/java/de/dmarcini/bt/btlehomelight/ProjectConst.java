/******************************************************************************
 * *
 * project: ANDROID                                                      *
 * module: btlehomelight                                                 *
 * class: ProjectConst                                                   *
 * date: 2016-01-15                                                      *
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

package de.dmarcini.bt.btlehomelight;

import java.util.Locale;

/**
 * Created by dmarc on 22.08.2015.
 */
public class ProjectConst
{
  //
  // Stops scanning after 10 seconds.
  //
  public static final long   SCAN_PERIOD                         = 4000;
  //
  // Welche Seite soll bei connect gerufen werden?
  //
  public static final int    DEFAULT_CONNECT_PAGE                = R.id.navColorCircle;
  //
  // Stati für Verbindung
  //
  public static final int    STATUS_DISCONNECTED                 = 0;
  public static final int    STATUS_CONNECTING                   = 1;
  public static final int    STATUS_CONNECTED                    = 2;
  public static final int    STATUS_DISCOVERING                  = 3;
  public static final int    STATUS_CONNECT_ERROR                = -1;
  //
  // Message-ID für BT Service Messages
  //
  public static final int    MESSAGE_NONE                        = 1;
  public static final int    MESSAGE_TICK                        = 2;
  public static final int    MESSAGE_DISCONNECTED                = 3;
  public static final int    MESSAGE_CONNECTING                  = 4;
  public static final int    MESSAGE_CONNECTED                   = 5;
  public static final int    MESSAGE_CONNECT_ERROR               = 6;
  public static final int    MESSAGE_BTLE_DEVICE_DISCOVERING     = 7;
  public static final int    MESSAGE_BTLE_DEVICE_DISCOVERED      = 8;
  public static final int    MESSAGE_BTLE_DEVICE_END_DISCOVERING = 9;
  public static final int    MESSAGE_GATT_SERVICES_DISCOVERED    = 10;
  public static final int    MESSAGE_BTLE_DATA                   = 11;
  //
  // Intend Merkmal (Ordnungsnummer)
  //
  public static final int    REQUEST_ENABLE_BT                   = 1;
  public static final int    REQUEST_SYS_PREFS                   = 2;
  //
  // Intend result
  //
  public static final String RESULT_DEV_NAME                     = "deviceName";
  //
  // Der Modultyp mit dem ich mich verbinden will
  //
  public static final String MY_MODULTYPE                        = "DM_RGBW";
  //
  // Steuerzeichen für Übertragungsprotokoll
  //
  public static final String STX                                 = new String(new byte[]{0x02});
  public static final String ETX                                 = new String(new byte[]{0x03});
  public static final byte   BSTX                                = 0x02;
  public static final byte   BETX                                = 0x03;
  //
  // Kommandos für Arduino
  // ACHTUNG: Version mit Scetch vergleichen!
  //
  public static final byte   C_UNKNOWN                           = -1;
  public static final byte   C_ASKTYP                            = 0x00; // Modultyp erfragen
  public static final byte   C_ASKNAME                           = 0x01; // Modul name erfragen
  public static final byte   C_ASKRGBW                           = 0x02; // RGBW aktuell erfragen
  public static final byte   C_SETCOLOR                          = 0x03; // Farbe DIREKT RGBW setzen
  public static final byte   C_SETCALRGB                         = 0x04; // Farbe als RGB senden, Modul kalibriert zu RGBW
  public static final byte   C_SETNAME                           = 0x05; // Name des Moduls setzen (und speichern)
  public static final byte   C_ONOFF                             = -2;   // eine "Pause" Funktion
  //
  // Suchpattern für Kommando
  //
  public static final String KOMANDPATTERN                       = "\\d{2}(\\:.*)?";
  public static final String PDUPATTERN                          = STX + KOMANDPATTERN + ETX;
  public static final String MODULTYPPATTERN                     = String.format(Locale.ENGLISH, "%s%02X\\:.*%s", STX, C_ASKTYP, ETX);
  public static final String MY_MODULTYPPATTERN                  = String.format(Locale.ENGLISH, "%s%02X\\:%s%s", STX, C_ASKTYP, MY_MODULTYPE, ETX);

  //
  // Benamsung
  //
  public static String getMsgName(int msgId)
  {
    if( BuildConfig.DEBUG )
    {
      switch( msgId )
      {
        case MESSAGE_NONE:
          return ("MESSAGE_NONE");
        case MESSAGE_TICK:
          return ("MESSAGE_TICK");
        case MESSAGE_DISCONNECTED:
          return ("MESSAGE_DISCONNECTED");
        case MESSAGE_CONNECTING:
          return ("MESSAGE_CONNECTING");
        case MESSAGE_CONNECTED:
          return ("MESSAGE_CONNECTED");
        case MESSAGE_CONNECT_ERROR:
          return ("MESSAGE_CONNECT_ERROR");
        case MESSAGE_BTLE_DEVICE_DISCOVERING:
          return ("MESSAGE_BTLE_DEVICE_DISCOVERING");
        case MESSAGE_BTLE_DEVICE_DISCOVERED:
          return ("MESSAGE_BTLE_DEVICE_DISCOVERED");
        case MESSAGE_BTLE_DEVICE_END_DISCOVERING:
          return ("MESSAGE_BTLE_DEVICE_END_DISCOVERING");
        case MESSAGE_GATT_SERVICES_DISCOVERED:
          return ("MESSAGE_GATT_SERVICES_DISCOVERED");
        case MESSAGE_BTLE_DATA:
          return ("MESSAGE_BTLE_DATA");
        default:
          return ("unknown message");
      }
    }
    else
    {
      return ("unknown message");
    }
  }
}
