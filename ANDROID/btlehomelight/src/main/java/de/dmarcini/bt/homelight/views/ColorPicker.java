/*
 * //@formatter:off
 *
 *     ANDROID
 *     btlehomelight
 *     ColorPicker
 *     2016-01-02
 *     Copyright (C) 2016  Dirk Marciniak
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/
 * /
 * //@formatter:on
 */

package de.dmarcini.bt.homelight.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorPicker extends View
{
  private static final String  TAG                   = ColorPicker.class.getSimpleName();
  /**
   * Display Parameter in Prozent
   */
  private final        int     paramOuterPadding     = 2; //** outer padding of the whole color picker view
  private final        int     paramInnerPadding     = 5; //** distance between value slider wheel and inner color wheel
  private final        int     paramValueSliderWidth = 16; //** width of the value slider
  private final        int     paramArrowPointerSize = 4; //** size of the arrow pointer; set to 0 to hide the pointer
  private final        float[] oldColorHSV           = new float[]{0f, 0f, 1f};
  private Paint                   colorWheelPaint;
  private Paint                   valueSliderPaint;
  private Paint                   colorViewPaint;
  private Paint                   colorPointerPaint;
  private RectF                   colorPointerCoords;
  private Paint                   valuePointerPaint;
  private Paint                   valuePointerArrowPaint;
  private RectF                   outerWheelRect;
  private RectF                   innerWheelRect;
  private Path                    colorViewPath;
  private Path                    valueSliderPath;
  private Path                    arrowPointerPath;
  private Bitmap                  colorWheelBitmap;
  private int                     valueSliderWidth;
  private int                     innerPadding;
  private int                     outerPadding;
  private int                     arrowPointerSize;
  private int                     outerWheelRadius;
  private int                     innerWheelRadius;
  private int                     colorWheelRadius;
  private Matrix                  gradientRotationMatrix;
  private OnColorChangedListener  onColorChangedListener;
  private OnColorSelectedListener onColorSelectedListener;
  /** Currently selected color */
  private float[] currColorHSV = new float[]{0f, 0f, 1f};

  /**
   * Konstruktor
   *
   * @param context  App-Kontext
   * @param attrs    Attibute
   * @param defStyle der style
   */
  public ColorPicker(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);
    init();
  }

  /**
   * Konstruktor
   *
   * @param context App Kontext
   * @param attrs   Attibute
   */
  public ColorPicker(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    init();
  }

  /**
   * Konstruktor
   *
   * @param context der App-Kontext
   */
  public ColorPicker(Context context)
  {
    super(context);
    init();
  }

  /**
   * Initialisiere das Objekt
   */
  private void init()
  {
    // Der Farbzeiger
    colorPointerPaint = new Paint();
    colorPointerPaint.setStyle(Style.STROKE);
    colorPointerPaint.setStrokeWidth(2f);
    colorPointerPaint.setARGB(128, 0, 0, 0);
    // der Stärke/Helligkeits-Zeiger
    valuePointerPaint = new Paint();
    valuePointerPaint.setStyle(Style.STROKE);
    valuePointerPaint.setStrokeWidth(2f);
    // Der Pfeil für die Stärke/Helligkeit
    valuePointerArrowPaint = new Paint();
    //Das Farbrad
    colorWheelPaint = new Paint();
    colorWheelPaint.setAntiAlias(true);
    colorWheelPaint.setDither(true);
    // der "slider" für die Helligkeit
    valueSliderPaint = new Paint();
    valueSliderPaint.setAntiAlias(true);
    valueSliderPaint.setDither(true);
    // Die echt-Farbanzeige
    colorViewPaint = new Paint();
    colorViewPaint.setAntiAlias(true);
    // Die anzeigelinien?
    colorViewPath = new Path();
    valueSliderPath = new Path();
    arrowPointerPath = new Path();
    // Begrenzugen der Anzeigen
    outerWheelRect = new RectF();
    innerWheelRect = new RectF();
    // Die Koordinaten des Farbanzeigers
    colorPointerCoords = new RectF();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
  {
    // Das Objekt in den vorgegebenen Rahmen einpassen
    int widthSize  = MeasureSpec.getSize(widthMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);
    // Immer Quadratisch machen, der kleinere Wert zählt
    int size = Math.min(widthSize, heightSize);
    setMeasuredDimension(size, size);
  }

  @SuppressLint( "DrawAllocation" )
  @Override
  protected void onDraw(Canvas canvas)
  {
    //
    // Lege schonmal die Mitte des Views fest
    //
    int centerX = getWidth() / 2;
    int centerY = getHeight() / 2;
    //
    // Das Farbrad als erstes zeichen
    //
    canvas.drawBitmap(colorWheelBitmap, centerX - colorWheelRadius, centerY - colorWheelRadius, null);
    //
    // Die Farbansicht zeichnen
    //
    colorViewPaint.setColor(Color.HSVToColor(currColorHSV));
    canvas.drawPath(colorViewPath, colorViewPaint);
    //
    // den Helligkeitsregler zeichnen
    //
    float[]       hsv           = new float[]{currColorHSV[ 0 ], currColorHSV[ 1 ], 1f};
    SweepGradient sweepGradient = new SweepGradient(centerX, centerY, new int[]{Color.BLACK, Color.HSVToColor(hsv), Color.WHITE}, null);
    sweepGradient.setLocalMatrix(gradientRotationMatrix);
    valueSliderPaint.setShader(sweepGradient);
    canvas.drawPath(valueSliderPath, valueSliderPaint);
    //
    // den Farbzeiger zeichnen
    //
    float hueAngle      = ( float ) Math.toRadians(currColorHSV[ 0 ]);
    int   colorPointX   = ( int ) (-Math.cos(hueAngle) * currColorHSV[ 1 ] * colorWheelRadius) + centerX;
    int   colorPointY   = ( int ) (-Math.sin(hueAngle) * currColorHSV[ 1 ] * colorWheelRadius) + centerY;
    float pointerRadius = 0.075f * colorWheelRadius;
    int   pointerX      = ( int ) (colorPointX - pointerRadius / 2);
    int   pointerY      = ( int ) (colorPointY - pointerRadius / 2);
    colorPointerCoords.set(pointerX, pointerY, pointerX + pointerRadius, pointerY + pointerRadius);
    canvas.drawOval(colorPointerCoords, colorPointerPaint);
    //
    // den Helligkeitszeiger zeichnen
    //
    valuePointerPaint.setColor(Color.HSVToColor(new float[]{0f, 0f, 1f - currColorHSV[ 2 ]}));
    double valueAngle  = (currColorHSV[ 2 ] - 0.5f) * Math.PI;
    float  valueAngleX = ( float ) Math.cos(valueAngle);
    float  valueAngleY = ( float ) Math.sin(valueAngle);
    canvas.drawLine(valueAngleX * innerWheelRadius + centerX, valueAngleY * innerWheelRadius + centerY, valueAngleX * outerWheelRadius + centerX, valueAngleY * outerWheelRadius + centerY, valuePointerPaint);
    //
    // und den Zeiger für den Helligkeitszeiger, wenn er eine darstellbsare Größe hat
    //
    if( arrowPointerSize > 0 )
    {
      drawPointerArrow(canvas);
    }
  }

  /**
   * Zeichne den Pfeil für den Helligekeitsregler
   *
   * @param canvas
   */
  private void drawPointerArrow(Canvas canvas)
  {
    int centerX = getWidth() / 2;
    int centerY = getHeight() / 2;
    //
    // Winkel für de Pfeil errechnen
    //
    double tipAngle   = (currColorHSV[ 2 ] - 0.5f) * Math.PI;
    double leftAngle  = tipAngle + Math.PI / 96;
    double rightAngle = tipAngle - Math.PI / 96;
    //
    // Koordinaten der Eckpunkte errechnen
    //
    double tipAngleX   = Math.cos(tipAngle) * outerWheelRadius;
    double tipAngleY   = Math.sin(tipAngle) * outerWheelRadius;
    double leftAngleX  = Math.cos(leftAngle) * (outerWheelRadius + arrowPointerSize);
    double leftAngleY  = Math.sin(leftAngle) * (outerWheelRadius + arrowPointerSize);
    double rightAngleX = Math.cos(rightAngle) * (outerWheelRadius + arrowPointerSize);
    double rightAngleY = Math.sin(rightAngle) * (outerWheelRadius + arrowPointerSize);
    //
    // Das Teil zeichnen (Pfad bestimmem)
    //
    arrowPointerPath.reset();
    arrowPointerPath.moveTo(( float ) tipAngleX + centerX, ( float ) tipAngleY + centerY);
    arrowPointerPath.lineTo(( float ) leftAngleX + centerX, ( float ) leftAngleY + centerY);
    arrowPointerPath.lineTo(( float ) rightAngleX + centerX, ( float ) rightAngleY + centerY);
    arrowPointerPath.lineTo(( float ) tipAngleX + centerX, ( float ) tipAngleY + centerY);
    // Farbe und Stil festlegen
    valuePointerArrowPaint.setColor(Color.HSVToColor(currColorHSV));
    valuePointerArrowPaint.setStyle(Style.FILL);
    canvas.drawPath(arrowPointerPath, valuePointerArrowPaint);
    valuePointerArrowPaint.setStyle(Style.STROKE);
    valuePointerArrowPaint.setStrokeJoin(Join.ROUND);
    valuePointerArrowPaint.setColor(Color.BLACK);
    // aus die Leinwand malen
    canvas.drawPath(arrowPointerPath, valuePointerArrowPaint);
  }

  @Override
  protected void onSizeChanged(int width, int height, int oldw, int oldh)
  {
    int centerX = width / 2;
    int centerY = height / 2;
    //
    // Größe neu berechnen
    innerPadding = paramInnerPadding * width / 100;
    outerPadding = paramOuterPadding * width / 100;
    arrowPointerSize = paramArrowPointerSize * width / 100;
    valueSliderWidth = paramValueSliderWidth * width / 100;
    //
    outerWheelRadius = width / 2 - outerPadding - arrowPointerSize;
    innerWheelRadius = outerWheelRadius - valueSliderWidth;
    colorWheelRadius = innerWheelRadius - innerPadding;
    //
    outerWheelRect.set(centerX - outerWheelRadius, centerY - outerWheelRadius, centerX + outerWheelRadius, centerY + outerWheelRadius);
    innerWheelRect.set(centerX - innerWheelRadius, centerY - innerWheelRadius, centerX + innerWheelRadius, centerY + innerWheelRadius);
    //
    colorWheelBitmap = createColorWheelBitmap(colorWheelRadius * 2, colorWheelRadius * 2);
    //
    gradientRotationMatrix = new Matrix();
    gradientRotationMatrix.preRotate(270, width / 2, height / 2);
    //
    colorViewPath.reset();
    colorViewPath.arcTo(outerWheelRect, 270, -180);
    colorViewPath.arcTo(innerWheelRect, 90, 180);
    //
    valueSliderPath.reset();
    valueSliderPath.arcTo(outerWheelRect, 270, 180);
    valueSliderPath.arcTo(innerWheelRect, 90, -180);
  }

  /**
   * Erzeuge die Bitmap (die Farbfläche) für das Farbrad
   *
   * @param width  Breite
   * @param height Höhe
   * @return Die erzeugte Bitmap
   */
  private Bitmap createColorWheelBitmap(int width, int height)
  {
    Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
    //
    int   colorCount     = 12;
    int   colorAngleStep = 360 / 12;
    int   colors[]       = new int[ colorCount + 1 ];
    float hsv[]          = new float[]{0f, 1f, 1f};
    for( int i = 0; i < colors.length; i++ )
    {
      hsv[ 0 ] = (i * colorAngleStep + 180) % 360;
      colors[ i ] = Color.HSVToColor(hsv);
    }
    colors[ colorCount ] = colors[ 0 ];

    SweepGradient  sweepGradient  = new SweepGradient(width / 2, height / 2, colors, null);
    RadialGradient radialGradient = new RadialGradient(width / 2, height / 2, colorWheelRadius, 0xFFFFFFFF, 0x00FFFFFF, TileMode.CLAMP);
    ComposeShader  composeShader  = new ComposeShader(sweepGradient, radialGradient, PorterDuff.Mode.SRC_OVER);

    colorWheelPaint.setShader(composeShader);

    Canvas canvas = new Canvas(bitmap);
    canvas.drawCircle(width / 2, height / 2, colorWheelRadius, colorWheelPaint);

    return bitmap;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    int action = event.getAction();
    switch( action )
    {
      // bei Berührung(start) und Bewegung
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_MOVE:
        // Wo fand das statt?
        int x = ( int ) event.getX();
        int y = ( int ) event.getY();
        int cx = x - getWidth() / 2;
        int cy = y - getHeight() / 2;
        // Berechne den Vector Mitte -> Akteller Punkt
        double d = Math.sqrt(cx * cx + cy * cy);
        //
        // Ist das im Farbkreis?
        //
        if( d <= colorWheelRadius )
        {

          currColorHSV[ 0 ] = ( float ) (Math.toDegrees(Math.atan2(cy, cx)) + 180f);
          currColorHSV[ 1 ] = Math.max(0f, Math.min(1f, ( float ) (d / colorWheelRadius)));
          invalidate();
          //
          // Hat sich nach der Aktion die Farbe geändert?
          //
          if( colorChanged() )
          {
            setOldColor();
            if( onColorChangedListener != null )
            {
              onColorChangedListener.onColorChanged(getColor());
            }
          }
        }
        //
        // nicht im Farbkreis, ist das dann im Helligkeitsregler?
        //
        else if( x >= getWidth() / 2 && d >= innerWheelRadius )
        {
          currColorHSV[ 2 ] = ( float ) Math.max(0, Math.min(1, Math.atan2(cy, cx) / Math.PI + 0.5f));
          invalidate();
          //
          // Hat sich nach der Aktion die Farbe geändert?
          //
          if( colorChanged() )
          {
            setOldColor();
            if( onColorChangedListener != null )
            {
              onColorChangedListener.onColorChanged(getColor());
            }
          }
        }
        return true;

      // Wenn das Objekt gelöst wird
      case MotionEvent.ACTION_UP:
        //
        // Hat sich nach der Aktion die Farbe geändert?
        //
        if( colorChanged() )
        {
          setOldColor();
          if( onColorSelectedListener != null )
          {
            onColorSelectedListener.onColorSelected(getColor());
          }
        }
        return true;
    }
    return super.onTouchEvent(event);
  }

  /**
   * Hat sich die Farbe verändert?
   *
   * @return
   */
  private boolean colorChanged()
  {
    return (currColorHSV[ 0 ] != oldColorHSV[ 0 ]) || (currColorHSV[ 1 ] != oldColorHSV[ 1 ]) || (currColorHSV[ 2 ] != oldColorHSV[ 2 ]);
    }

  /**
   * setze die alte Farbe auf die neue....
   */
  private void setOldColor()
  {
    oldColorHSV[ 0 ] = currColorHSV[ 0 ];
    oldColorHSV[ 1 ] = currColorHSV[ 1 ];
    oldColorHSV[ 2 ] = currColorHSV[ 2 ];
  }

  /**
   * Gib die Farbe des Farbwählers zurück
   *
   * @return Die Farbe als ARGB
   */
  public int getColor()
  {
    return Color.HSVToColor(currColorHSV);
  }

  /**
   * Setze die Farbe (RGB) des Farbwählers
   *
   * @param color Die Farbe als ARGB
   */
  public void setColor(int color)
  {
    Color.colorToHSV(color, currColorHSV);
    setOldColor();
  }

  /**
   * Sichere die Einstellung
   *
   * @return Die Einstelllungen
   */
  @Override
  protected Parcelable onSaveInstanceState()
  {
    Bundle state = new Bundle();
    state.putFloatArray("color", currColorHSV);
    state.putParcelable("super", super.onSaveInstanceState());
    return state;
  }

  /**
   * stelle die Einstellugnen wieder her...
   *
   * @param state Die Einstellungen
   */
  @Override
  protected void onRestoreInstanceState(Parcelable state)
  {
    if( state instanceof Bundle )
    {
      Bundle bundle = ( Bundle ) state;
      currColorHSV = bundle.getFloatArray("color");
      super.onRestoreInstanceState(bundle.getParcelable("super"));
    }
    else
    {
      super.onRestoreInstanceState(state);
    }
  }

  /**
   * gib den Callback für Farbänderung zurück
   *
   * @return {@code OnColorSelectedListener}
   */
  public OnColorSelectedListener getOnColorSelectedListener()
  {
    return this.onColorSelectedListener;
  }

  /**
   * Setze einen Callbsack für ausgewählte Farbe (Ende der Wahl)
   *
   * @param listener {@code OnColorSelectedListener}
   */
  public void setOnColorSelectedListener(OnColorSelectedListener listener)
  {
    this.onColorSelectedListener = listener;
  }

  public OnColorChangedListener getOnColorChangedListener()
  {
    return this.onColorChangedListener;
  }

  /**
   * Setze einen Callback für Farbänderungen (live)
   *
   * @param listener {@code OnColorChangedListener}
   */
  public void setOnColorChangedListener(OnColorChangedListener listener)
  {
    this.onColorChangedListener = listener;
  }

  /**
   * Rechne RGB Werte in RGBW (mit Erhalt der Helligkeit) um
   *
   * @return RGBW-Wert, nicht veränderbar
   */
  public short[] getColorRGBW()
  {
    short[] rgbw = new short[ 4 ];
    //double maxVal;
    double minVal;
    double Ri, Gi, Bi, Ro, Go, Bo, Wo;
    int    color;

    color = Color.HSVToColor(currColorHSV);
    //
    // SIMPEL Methode
    //

    //
    // Berechnug in double
    //
    Ri = ( double ) Color.red(color);
    Gi = ( double ) Color.green(color);
    Bi = ( double ) Color.blue(color);
    //
    // Bestimme maximal und Minimal Wert der Farben
    //
    //maxVal = Math.max(Ri, Math.max(Gi, Bi));
    minVal = Math.min(Ri, Math.min(Gi, Bi));
    //
    // Weisswert
    //
    //double startVal = (255 - Math.round(100 * ((maxVal - minVal) / maxVal)));
    //double averageVal = ((Color.red(color) + Color.green(color) + Color.blue(color)) / 3);
    // Variante 1
    //Wo = Math.floor( startVal / 255 * averageVal);
    // Variante 2
    Wo = Math.floor(minVal);
    Ro = Ri >= Wo ? Math.floor(Ri - Wo) : 0.0;
    Go = Gi >= Wo ? Math.floor(Gi - Wo) : 0.0;
    Bo = Bi >= Wo ? Math.floor(Bi - Wo) : 0.0;
    //
    // Werte als Short (wenn > 0  in Array sichern)
    //
    rgbw[ 0 ] = ( short ) (Math.round(Ro) & 0xff);
    rgbw[ 1 ] = ( short ) (Math.round(Go) & 0xff);
    rgbw[ 2 ] = ( short ) (Math.round(Bo) & 0xff);
    rgbw[ 3 ] = ( short ) (Math.round(Wo) & 0xff);
    return (rgbw);
  }

  /**
   * Das Interface für Callbacks bei der Änderugn der Farbe
   *
   * @author lars
   */
  public interface OnColorChangedListener
  {
    void onColorChanged(int color);
  }

  /**
   * Das Interface für Callbacks bei der Festlegung der Farbe
   */
  public interface OnColorSelectedListener
  {
    void onColorSelected(int color);
  }

}
