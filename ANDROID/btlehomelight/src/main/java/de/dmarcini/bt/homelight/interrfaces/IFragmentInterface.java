package de.dmarcini.bt.homelight.interrfaces;

import android.app.DialogFragment;
import android.bluetooth.BluetoothGattService;

import java.util.List;

/**
 * Created by dmarc on 17.01.2016.
 */
public interface IFragmentInterface
{
  /**
   * Das BT Gerät wurde verbunden
   */
  void onBTConnected();

  /**
   * Das BT Gerät wurde getrennt
   */
  void onBTDisconnected();

  /**
   * Das BT Gerät meldet verfügbarte Services
   *
   * @param gattServices
   */
  void onBTServicesRecived(List<BluetoothGattService> gattServices);

  /**
   * Es kommen Daten vom BT Gerät
   *
   * @param data
   */
  void onBTDataAvaiable(String[] data);


  /**
   * Bluethooth Hintergrundservice verbunden!
   */
  void onServiceConnected();

  /**
   * Bluethooth Hintergrundservice getrennt
   */
  void onServiceDisconnected();

  /**
   * Page wurde selektiert
   */
  void onPageSelected();

  /**
   * Der Dialog hat eine Positive Antwort
   *
   * @param frag Das Fragment( der Dialog )
   */
  void onPositiveDialogFragment(DialogFragment frag);

  /**
   * Der Dialog hat eine Negative Antwort
   *
   * @param frag Das Fragment( der Dialog )
   */
  void onNegativeDialogFragment(DialogFragment frag);


}
