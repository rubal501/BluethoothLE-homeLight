package de.dmarcini.bt.btlehomelight.fragments;

import android.app.Fragment;

import de.dmarcini.bt.btlehomelight.ProjectConst;
import de.dmarcini.bt.btlehomelight.interfaces.IBtCommand;
import de.dmarcini.bt.btlehomelight.interfaces.IBtServiceListener;

/**
 * Abstrakte Klasse, dient als polimorphes Objekt für Nandhabung in der MainActivity
 */
public abstract class LightRootFragment extends Fragment implements IBtServiceListener
{
  protected long timeToSend;
  protected IBtCommand runningActivity;

  /**
   * Lese die Farbparameter aus dem Kommando und speichere diese in meinem Array
   *
   * @param param Array mit Kommandowerten
   */
  protected short[] fillValuesInArray(String[] param)
  {
    short[] rgbw = new short[ 4 ];
    //
    // der erste Parameter ist das Kommando, den ignoriere ich mal
    //
    for( int i = 1; i < ProjectConst.C_ASKRGB_LEN; i++ )
    {
      try
      {
        rgbw[ i - 1 ] = Short.parseShort(param[ i ], 16);
      }
      catch( NumberFormatException ex )
      {
        rgbw[ i - 1 ] = 0;
      }
    }
    return (rgbw);
  }

  /**
   * Sende die Farben an das Modul
   *
   * @param color Die Farbe
   * @param isRaw soll RGBW(raw) oder RGB zur Kalibrierung auf dem MOdul gesendet werden
   */
  protected void sendColor(int color, boolean isRaw)
  {
    short[] rgbw = new short[ 4 ];
    //
    rgbw[ 0 ] = ( short ) ((color >> 16) & 0xff);
    rgbw[ 1 ] = ( short ) ((color >> 8) & 0xff);
    rgbw[ 2 ] = ( short ) (color & 0xff);
    rgbw[ 3 ] = 0;

    if( timeToSend < System.currentTimeMillis() )
    {
      //
      // Mal wieder zum Contoller senden!
      //
      if( isRaw )
      {
        runningActivity.setModulRawRGBW(rgbw);
      }
      else
      {
        runningActivity.setModulRGB4Calibrate(rgbw);
      }
      //
      // Neue Deadline setzen
      //
      timeToSend = System.currentTimeMillis() + ProjectConst.TIMEDIFF_TO_SEND;
    }
  }

}
