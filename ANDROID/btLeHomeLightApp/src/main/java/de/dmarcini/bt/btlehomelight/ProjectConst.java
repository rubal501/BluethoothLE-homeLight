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

package de.dmarcini.bt.btlehomelight;

import java.util.UUID;

import de.dmarcini.bt.btlehomelight.utils.HM10GattAttributes;


/**
 * Created by dmarc on 22.08.2015.
 */
public class ProjectConst
{
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
  //
  // Suchpattern für Kommando
  //
  public static final String KOMANDPATTERN        = "\\d{2}(\\:.*)?";
}
