/*
 *   project: BlueThoothLE
 *   programm: Home Light control (Bluethooth LE with HM-10)
 *   purpose:  control home lights via BT (color and brightness)
 *   Copyright (C) 2015  Dirk Marciniak
 *   file: ProjectConst.java
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

package de.dmarcini.bt.homelight.utils;

import java.util.UUID;

/**
 * Created by dmarc on 22.08.2015.
 */
public class ProjectConst
{
  //
  // zuerst definiere die Seitennummern
  //
  public static final int    PAGE_COUNT            = 3;
  public static final int    PAGE_DISCOVERING      = 0;
  public static final int    PAGE_DIRECT_CONTROL   = 1;
  public static final int    PAGE_COLOR_CIRCLE     = 2;
  //
  // dahin wechseln, wenn Verbindung aufgebaut ist
  //
  public static final int    DEFAULT_CONNECT_PAGE  = 1;
  //
  // UUID für die Modulattribute
  //
  public final static UUID   UUID_HM_RX_TX         = UUID.fromString(HM10GattAttributes.HM_RX_TX);
  //
  // Stops scanning after 10 seconds.
  //
  public static final long   SCAN_PERIOD           = 4000;
  //
  // Intend Merkmal (Ordnungsnummer)
  //
  public static final int    REQUEST_ENABLE_BT     = 1;
  //
  // Für die Fragmente definierter ARG Name
  //
  public static final String ARG_SECTION_NUMBER    = "section_number";
  //
  // Kennzeichnug für Argumente
  //
  public static final String EXTRAS_DEVICE_NAME    = "DEVICE_NAME";
  public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
  public static final String LIST_NAME             = "NAME";
  public static final String LIST_UUID             = "UUID";
  //
  // Der Modultyp mit dem ich mich verbinden will
  //
  public static final String MY_MODULTYPE          = "DM_RGBW";
  //
  // Steuerzeichen für Übertragungsprotokoll
  //
  public static final String STX                   = new String(new byte[]{0x02});
  public static final String ETX                   = new String(new byte[]{0x03});
  public static final byte   BSTX                  = 0x02;
  public static final byte   BETX                  = 0x03;
  //
  // Kommandos für Arduino
  // ACHTUNG: Version mit Scetch vergleichen!
  //
  public static final byte   C_UNKNOWN             = -1;
  public static final byte   C_ASKTYP              = 0x00;
  public static final byte   C_ASKNAME             = 0x01;
  public static final byte   C_ASKRGB              = 0x02;
  public static final byte   C_SETCOLOR            = 0x03;
  public static final byte   C_ONOFF               = 0x04;
  //
  // Länge der Kommandoketten
  //
  public static final int    C_ASKRGB_LEN          = 5;
  //
  // Zeit bis zum Senden neuer RGBW Werte, wenn der User kontinuierlich schiebt
  //
  public static final long   TIMEDIFF_TO_SEND      = 100L;
  //
  // Suchpattern für Kommando
  //
  public static final String KOMANDPATTERN         = "\\d{2}(\\:.*)?";
}
