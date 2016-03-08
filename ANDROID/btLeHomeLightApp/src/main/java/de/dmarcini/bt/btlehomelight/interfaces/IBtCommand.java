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
   * @return VErbuunden ?
   */
  int askModulOnlineStatus();

  /**
   * Frage welches Modul verbunden ist
   *
   * @return Moduladresse oder NULL
   */
  BluetoothDevice askConnectedModul();

  /**
   * Frage (noch einmal) nach dem Modultyp
   */
  void askModulForType();

  /**
   * Fragt das Modul nach seinem Namen
   */
  void askModulForName();

  /**
   * Frage das Modul nach der aktuellen RGBW Einstellung (Roh)
   */
  void askModulForRGBW();

  /**
   * Schaltet das Modul dunkel oder hell
   */
  void setModulPause();


}
