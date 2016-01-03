/******************************************************************************
 * *
 * project: ANDROID                                                      *
 * module: btlehomelight                                                 *
 * class: CmdQueueThread                                                 *
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

package de.dmarcini.bt.homelight.utils;

import android.util.Log;

import java.util.Vector;

import de.dmarcini.bt.homelight.HomeLightMainActivity;

/**
 * Thread zum auslesen des Ringpuffers und zum Benachrichtigen der Anwendung über DAten
 */
public class CmdQueueThread implements Runnable
{
  private final    String  TAG       = CmdQueueThread.class.getSimpleName();
  private volatile boolean isRunning = true;
  private HomeLightMainActivity.CommandReciver cReciver;
  private Vector<String>                       cmdBuffer;

  /**
   * Standartkonstruktor als PRIVAT => nicht benutzbar
   */
  private CmdQueueThread()
  {

  }

  /**
   * Konstruktor für den Reader-Thread
   *
   * @param cmdBuffer Das extraierte Kommando
   */
  public CmdQueueThread(Vector<String> cmdBuffer, HomeLightMainActivity.CommandReciver cReciver)
  {
    this.cmdBuffer = cmdBuffer;
    this.cReciver = cReciver;
  }


  @Override
  public void run()
  {

    Log.d(TAG, "BEGIN CmdQueueThread");
    isRunning = true;
    if( cmdBuffer == null )
    {
      Log.e(TAG, "Command Queue not avaivible!");
      return;
    }
    //
    // Solange das Programm läuft
    //
    while( isRunning )
    {
      if( !cmdBuffer.isEmpty() )
      {
        cReciver.reciveCommand(cmdBuffer.remove(0));
      }
      else
      {
        try
        {
          synchronized( cmdBuffer )
          {
            //
            // wenn nicht aufgeweckt, dann warte eine Weile
            //
            cmdBuffer.wait(80);
          }
        }
        catch( InterruptedException ex )
        {
          //TODO: Was sinnvolles machen
        }

      }
    }
    Log.d(TAG, "END CmdQueueThread");
  }

  public void doStop()
  {
    isRunning = false;
    synchronized( cmdBuffer )
    {
      cmdBuffer.notifyAll();
    }
  }
}
