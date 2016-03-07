//@formatter:off
/*
    programm: SubmatixBTLoggerAndroid
    purpose:  configuration and read logs from SUBMATIX SPX42 divecomputer via Bluethooth    
    Copyright (C) 2012  Dirk Marciniak

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/
*/
//@formatter:on
package de.dmarcini.bt.btlehomelight.interfaces;


import de.dmarcini.bt.btlehomelight.utils.BlueThoothMessage;

/**
 * 
 * Interfacebeschreibung für BluethoothLE Service Listener
 *
 * @author Dirk Marciniak (dirk_marciniak@arcor.de)
 * 
 *         Stand: 10.11.2013
 */
public interface IBtServiceListener
{
  /**
   * 
   * Behandle alle ankommenden Nachrichten
   *
   * Stand: 16.11.2013
   * 
   * @param msg
   */
  void handleMessages(final BlueThoothMessage msg);

  /**
   * 
   * Behandle ankommende Nachricht über den Versuch eine Verbindung aufzubauen
   * 
   * Stand: 16.11.2013
   * 
   * @param msg
   */
  void msgConnecting(final BlueThoothMessage msg);

  /**
   * 
   * Behandle Nachricht über den erfolgreichen Aufbau einer Verbindung zum BT Gerät
   * 
   * Stand: 16.11.2013
   * 
   * @param msg
   */
  void msgConnected(final BlueThoothMessage msg);

  /**
   * 
   * Behandle Nachricht über den Verlust der BT-Verbindung
   * 
   * Stand: 16.11.2013
   * 
   * @param msg
   */
  void msgDisconnected(final BlueThoothMessage msg);

  /**
   * 
   * Behandle TICK-Nachricht vom Service
   * 
   * Stand: 16.11.2013
   * 
   * @param msg
   */
  void msgRecivedTick(final BlueThoothMessage msg);

  /**
   * 
   * Behandle die Nachricht vom Service, dass der Verbindungsversuch erfolglos war
   * 
   * Stand: 16.11.2013
   * 
   * @param msg
   */
  void msgConnectError(final BlueThoothMessage msg);

  /**
   * 
   * Behandle die _Nachricht, dass es einen Timeout beim schreiben zum BT-Gerät gab
   * 
   * Stand: 16.11.2013
   * 
   * @param msg
   */
  void msgReciveWriteTmeout(final BlueThoothMessage msg);

  /**
   * BEhandle ankommende Daten
   *
   * @param msg Nachricht mit eingeschlossenen Daten
   */
  void msgDataRecived( final BlueThoothMessage msg );
}
