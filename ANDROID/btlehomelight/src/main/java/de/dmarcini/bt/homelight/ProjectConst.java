/******************************************************************************
 *                                                                            *
 *      project: ANDROID                                                      *
 *      module: btlehomelight                                                 *
 *      class: ProjectConst                                                   *
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

package de.dmarcini.bt.homelight;

import java.util.UUID;

import de.dmarcini.bt.homelight.utils.HM10GattAttributes;

/**
 * Created by dmarc on 22.08.2015.
 */
public class ProjectConst
{
  //
  // Preferenzen Name FARBEN
  //
  public static final String COLOR_PREFS          = "BTLE_COLOR_PREFS";
  //
  // Einstellungen fuer vordefinierte Farben in den Präferenzen
  //
  public static final String KEY_PREDEF_COLOR_01  = "predefColor01";
  public static final String KEY_PREDEF_COLOR_02  = "predefColor02";
  public static final String KEY_PREDEF_COLOR_03  = "predefColor03";
  public static final String KEY_PREDEF_COLOR_04  = "predefColor04";
  public static final String KEY_PREDEF_COLOR_05  = "predefColor05";
  public static final String KEY_PREDEF_COLOR_06  = "predefColor06";
  public static final String KEY_LAST_BT_DEVICE   = "lastBTDevice";
  public static final String KEY_LAST_BT_NAME     = "lastBTName";
  //
  // zuerst definiere die Seitennummern
  //
  public static final int    PAGE_DISCOVERING     = 0;
  public static final int    PAGE_DIRECT_CONTROL  = 1;
  public static final int    PAGE_COLOR_WHEEL = 2;
  public static final int    PAGE_BRIGHTNESS_ONLY = 3;
  public static final int    PAGE_PREDEF_COLORS   = 4;
  public static final int    PAGE_COUNT           = 5;
  //
  // dahin wechseln, wenn Verbindung aufgebaut ist
  //
  public static final int    DEFAULT_CONNECT_PAGE = 2;
  //
  // UUID für die Modulattribute
  //
  public final static UUID   UUID_HM_RX_TX        = UUID.fromString(HM10GattAttributes.HM_RX_TX);
  //
  // Stops scanning after 10 seconds.
  //
  public static final long   SCAN_PERIOD          = 4000;
  //
  // Intend Merkmal (Ordnungsnummer)
  //
  public static final int    REQUEST_ENABLE_BT    = 1;
  public static final int    REQUEST_SYS_PREFS    = 2;
  //
  // Intend result
  //
  public static final String RESULT_DEV_NAME      = "deviceName";
  //
  // Für die Fragmente Argumentbezeichnungen
  //
  public static final String ARG_SECTION_NUMBER   = "section_number";
  public static final String ARG_COLOR_LIST       = "color_list";
  public static final String ARG_PREVIEW_COLOR    = "preview_color";
  public static final String ARG_PREDEF_NUMBER    = "predef_number";
  public static final String ARG_MODULE_NAME      = "module_name";
  //
  // Der Modultyp mit dem ich mich verbinden will
  //
  public static final String MY_MODULTYPE         = "DM_RGBW";
  //
  // Steuerzeichen für Übertragungsprotokoll
  //
  public static final String STX                  = new String(new byte[]{0x02});
  public static final String ETX                  = new String(new byte[]{0x03});
  public static final byte   BSTX                 = 0x02;
  public static final byte   BETX                 = 0x03;
  //
  // Kommandos für Arduino
  // ACHTUNG: Version mit Scetch vergleichen!
  //
  public static final byte   C_UNKNOWN            = -1;
  public static final byte   C_ASKTYP             = 0x00; // Modultyp erfragen
  public static final byte   C_ASKNAME            = 0x01; // Modul name erfragen
  public static final byte   C_ASKRGBW            = 0x02; // RGBW aktuell erfragen
  public static final byte   C_SETCOLOR           = 0x03; // Farbe DIREKT RGBW setzen
  public static final byte   C_SETCALRGB          = 0x04; // Farbe als RGB senden, Modul kalibriert zu RGBW
  public static final byte   C_SETNAME            = 0x05; // Name des Moduls setzen (und speichern)
  public static final byte   C_ONOFF              = -2;
  //
  // Länge der Kommandoketten für Farbe
  //
  public static final int    C_ASKRGB_LEN         = 5;
  //
  // Zeit bis zum Senden neuer RGBW Werte, wenn der User kontinuierlich schiebt
  //
  public static final long   TIMEDIFF_TO_SEND     = 100L;
  //
  // Suchpattern für Kommando
  //
  public static final String KOMANDPATTERN        = "\\d{2}(\\:.*)?";
}
