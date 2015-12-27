package de.dmarcini.test.colorpicker;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.util.Locale;


public class MainActivity extends Activity implements ColorPicker.OnColorChangedListener, ColorPicker.OnColorSelectedListener
{
  private static final String TAG = MainActivity.class.getSimpleName();
  private ColorPicker colorPicker;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_picker);

    colorPicker = ( ColorPicker ) findViewById(R.id.colorPicker);
    colorPicker.setOnColorChangedListener( this );
    colorPicker.setOnColorSelectedListener(this);
/*
    button = ( Button ) findViewById(R.id.button);
    button.setOnClickListener(new OnClickListener()
    {

      @Override
      public void onClick(View v)
      {

        int    color     = colorPicker.getColor();
        String rgbString = "R: " + Color.red(color) + " B: " + Color.blue(color) + " G: " + Color.green(color);

        Toast.makeText(MainActivity.this, rgbString, Toast.LENGTH_SHORT).show();

      }
    });
*/
  }

  @Override
  public void onColorChanged(int color)
  {
    Log.i(TAG, String.format(Locale.ENGLISH, "color changed to %08X", color ));
  }

  @Override
  public void onColorSelected(int color)
  {
    Log.i(TAG, String.format(Locale.ENGLISH, "color SELECTED to %08X", color ));
  }
}
