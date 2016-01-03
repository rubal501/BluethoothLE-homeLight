/******************************************************************************
 *                                                                            *
 *      project: ANDROID                                                      *
 *      module: btlehomelight                                                 *
 *      class: BTReaderThread                                                 *
 *      date: 2016-01-03                                                      *
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

package de.dmarcini.bt.homelight.utils;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Vector;

import de.dmarcini.bt.homelight.BuildConfig;
import de.dmarcini.bt.homelight.exceptions.BufferOverflowException;

/**
 * Thread zum auslesen des Ringpuffers und zum Benachrichtigen der Anwendung über DAten
 */
public class BTReaderThread implements Runnable
{
  public final Object syncObj = new Object();
  private final String TAG = BTReaderThread.class.getSimpleName();
  private CircularByteBuffer ringBuffer;
  //private HomeLightMainActivity.CommandReciver cReciver;
  private volatile boolean isRunning = true;
  private Vector<String> cmdBuffer;

  /**
   * Standartkonstruktor als PRIVAT => nicht benutzbar
   */
  private BTReaderThread()
  {

  }

  /**
   * Konstruktor für den Reader-Thread
   *
   * @param ringBuffer Der Puffer für empfangene Daten
   * @param cmdBuffer  Das extraierte Kommando
   */
  public BTReaderThread(CircularByteBuffer ringBuffer, Vector<String> cmdBuffer)
  {
    this.ringBuffer = ringBuffer;
    this.cmdBuffer = cmdBuffer;
  }


  @Override
  public void run()
  {
    String      readMessage = "";
    int         start, end;
    InputStream iStream;
    //
    // den Inputstream solange lesen, wie die Verbindung besteht
    //
    Log.d(TAG, "BEGIN BTreaderThread");
    ringBuffer.clear();
    iStream = ringBuffer.getInputStream();
    isRunning = true;
    //
    // Solange das Programm läuft
    //
    while( isRunning )
    {
      if( ringBuffer.getAvailable() > 0 )
      {
        start = ringBuffer.indexOf(ProjectConst.BSTX);
        end = ringBuffer.indexOf(ProjectConst.BETX);
        //
        // solange ENDE gefunden wurde
        // oder nicht CANCEL gerufen
        //
        while( (end > -1) && isRunning && (ringBuffer != null) )
        {
          try
          {
            if( start > -1 )
            {
              // Nicht am Anfang == überlese den Anfang
              // end ist größer als 0 (siehe while)
              if( start > 0 )
              {
                iStream.skip(start - 1);
                if( BuildConfig.DEBUG )Log.v(TAG, String.format(Locale.GERMAN, "before STX deleted: <%02d> bytes", start));
                start = ringBuffer.indexOf(ProjectConst.BSTX);
                end = ringBuffer.indexOf(ProjectConst.BETX);
                continue;
              }
              // ist ETX zuerst (es fehlt etwas vorne) dann bis STX überlesen
              // end ist größer als 0 (siehe while)
              // an dieser Stelle sollte start == 0 sein
              if( start > end )
              {
                iStream.skip(start);
                if( BuildConfig.DEBUG )Log.v(TAG, String.format(Locale.GERMAN, "past ETX deleted: <%02d> bytes", start));
                start = ringBuffer.indexOf(ProjectConst.BSTX);
                end = ringBuffer.indexOf(ProjectConst.BETX);
                continue;
              }
              // jetzt sollte als erstes STX da stehen...
              // end ist größer als 0 (siehe while)
              // an dieser Stelle sollte start == 0 sein
              if( end > start )
              {
                byte[] buf = new byte[ end - 1 ];
                // STX überlesen
                iStream.skip(1L);
                // KDO lesen
                iStream.read(buf);
                // ETX überlesen
                iStream.skip(1L);
                readMessage = new String(buf);
                Log.d(TAG, "message recived: <" + readMessage + ">, len <" + readMessage.length() + ">...");
                //
                // Kommando gefunden => Sende an die App / das Fragment
                //
                cmdBuffer.add(readMessage);
                synchronized( cmdBuffer )
                {
                  cmdBuffer.notifyAll();
                }
                start = ringBuffer.indexOf(ProjectConst.BSTX);
                end = ringBuffer.indexOf(ProjectConst.BETX);
              }
            }
            else
            {
              // hier ist start == -1 also nicht gefunden...
              // end ist aber > -1, also ENDE ohne ANFANG
              // alles aus dem Puffer entfernen bis einschliesslich END
              iStream.skip(end);
              Log.w(TAG, String.format(Locale.GERMAN, "deleted before and inclusice ETX <%02d> bytes...", end));
              start = ringBuffer.indexOf(ProjectConst.BSTX);
              end = ringBuffer.indexOf(ProjectConst.BETX);
            }
          }
          catch( NullPointerException ex )
          {
            if( ringBuffer != null )
            {
              Log.e(TAG, "Null Pointer Exception");
            }
            isRunning = false;
            break;
          }
          catch( BufferOverflowException ex )
          {
            Log.e(TAG, "BUFFER OVERFLOW in ringbuffer!");
            isRunning = false;
            break;
          }
          catch( IOException ex )
          {
            Log.e(TAG, "IOEXCEPTION: " + ex.getLocalizedMessage());
            isRunning = false;
            break;
          }
        }
        //
        // Puffer ist wieder leer
        //
        try
        {
          synchronized( ringBuffer )
          {
            //
            // wenn nicht aufgeweckt, dann warte eine Weile
            //
            ringBuffer.wait(80);
          }
        }
        catch( InterruptedException ex )
        {
          //TODO: Was sinnvolles machen
        }
      }
    }
    Log.d(TAG, "END BTreaderThread");
  }

  public void doStop()
  {
    isRunning = false;
    ringBuffer.clear();
    //
    // hier riskiere ich eine nullpointer exception
    // die fange ich oben ein.....
    //
    ringBuffer = null;
  }
}
