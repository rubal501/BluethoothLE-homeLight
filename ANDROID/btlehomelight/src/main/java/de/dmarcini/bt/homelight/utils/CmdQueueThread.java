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

    Log.i(TAG, "BEGIN CmdQueueThread");
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
        String msg = cmdBuffer.remove(0);
        Log.i(TAG, "CMD deliver: " + msg);
        cReciver.reciveCommand(msg /*cmdBuffer.remove(0)*/);
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
    Log.i(TAG, "END CmdQueueThread");
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
