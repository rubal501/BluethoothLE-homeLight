package de.dmarcini.bt.btlehomelight.interfaces;

import android.bluetooth.BluetoothDevice;

/**
 * Created by dmarc on 28.02.2016.
 */
public interface IBtCommand
{
  /**
   * Suche nach BTLE Geräten (wenn uuidArr != null nach allen)
   * @param uuidArr
   * @return konnte der Vorgang gestartet werden?
   */
  boolean discoverDevices( String[] uuidArr );

  /**
   * Stoppe die Erkundung der BTLE Geräte falls gerade in Arbeit
   */
  void stopDiscoverDevices();

  /**
   * Berbinde zu einem BTLE Modul
   *
   * @param addr Adresse des Modules
   */
  void connectTo( String addr );

  /**
   * Trenne die Verbindung mit einem BTLE Modul
   */
  void disconnect();

  /**
   * Frage, ob eine Verbindung zu einem BT Device besteht
   *
   * @return Verbuunden ?
   */
  int getModulOnlineStatus();

  /**
   * Frage welches Modul verbunden ist
   *
   * @return Moduladresse oder NULL
   */
  BluetoothDevice getConnectedModul();

  /**
   * Frage (noch einmal) nach dem Modultyp
   */
  void askModulForType();

  /**
   * Fragt das Modul nach seinem Namen
   */
  void askModulForName();

  /**
   * gib Modulneman zurück, wenn im Service schon ermittelt
   *
   * @return Modulname
   */
  String getConnectedModulName();

  /**
   * Frage das Modul nach der aktuellen RGBW Einstellung (Roh)
   */
  void askModulForRGBW();

  /**
   * Schaltet das Modul dunkel oder hell
   */
  void setModulPause();

  /**
   * Setze Farben als RGB
   *
   * @param rgbw RGB Werte
   */
  void setModulRawRGBW(short[] rgbw);

  /**
   * Setze Farben als RGB, Modul kalibriert nach RGBW
   *
   * @param rgbw RGB Werte, White wird ignoriert
   */
  void setModulRGB4Calibrate(short[] rgbw);

  /**
   * Setze den neuen Modulnamen
   *
   * @param newName der Neue Name
   */
  void setModuleName( String newName );
}
