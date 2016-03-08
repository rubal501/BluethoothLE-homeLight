package de.dmarcini.bt.btlehomelight.fragments;

import android.app.Fragment;

import de.dmarcini.bt.btlehomelight.ProjectConst;
import de.dmarcini.bt.btlehomelight.interfaces.IBtServiceListener;

/**
 * Abstrakte Klasse, dient als polimorphes Objekt für Nandhabung in der MainActivity
 */
public abstract class LightRootFragment extends Fragment implements IBtServiceListener
{
  protected long timeToSend;

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

}
