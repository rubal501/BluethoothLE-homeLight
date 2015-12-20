/*
 *   project: BlueThoothLE
 *   programm: Home Light control (Bluethooth LE with HM-10)
 *   purpose:  control home lights via BT (color and brightness)
 *   Copyright (C) 2015  Dirk Marciniak
 *   file: MainActivity.java
 *   last modified: 19.12.15 18:00
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/
 *
 */

package de.dmarcini.test.colorpicker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ColorPicker.OnColorChangedListener
{
  private static final String TAG = MainActivity.class.getSimpleName();
  private ColorPicker   picker;
  private SVBar         svBar;
//  private OpacityBar    opacityBar;
//  private SaturationBar saturationBar;
//  private ValueBar      valueBar;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_cp);
    picker = ( ColorPicker ) findViewById(R.id.picker);
    svBar = ( SVBar ) findViewById(R.id.svbar);
//    opacityBar = ( OpacityBar ) findViewById(R.id.opacitybar);
//    saturationBar = ( SaturationBar ) findViewById(R.id.saturationbar);
//    valueBar = ( ValueBar ) findViewById(R.id.valuebar);

    picker.addSVBar(svBar);
//    picker.addOpacityBar(opacityBar);
//    picker.addSaturationBar(saturationBar);
//    picker.addValueBar(valueBar);

    //To get the color
    picker.getColor();

    //To set the old selected color u can do it like this
    picker.setOldCenterColor(picker.getColor());
    // adds listener to the colorpicker which is implemented
    //in the activity
    picker.setOnColorChangedListener(this);

    //to turn of showing the old color
    picker.setShowOldCenterColor(false);

    //adding onChangeListeners to bars
    /*
    opacityBar.setOnOpacityChangeListener(new OnOpacityChangeListener …)
    valueBar.setOnValueChangeListener(new NumberPicker.OnValueChangeListener …)
    saturationBar.setOnSaturationChangeListener(new OnSaturationChangeListener …)
    */
  }


  @Override
  public void onColorChanged(int color)
  {
    Log.i(TAG, String.format(Locale.ENGLISH, "color changed to %02X", color ));
  }
}




