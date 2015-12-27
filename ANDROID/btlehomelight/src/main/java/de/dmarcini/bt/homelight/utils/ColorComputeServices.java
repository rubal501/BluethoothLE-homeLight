package de.dmarcini.bt.homelight.utils;

/**
 * Created by dmarc on 24.12.2015.
 */

class cRGBW
{
  public int red;
  public int green;
  public int blue;
  public int white;

  public cRGBW()
  {
    red = 0;
    green = 0;
    blue = 0;
    white = 0;
  }
}


public class ColorComputeServices
{

  /**
   * Berechne aus RGB die korrigierte RGBW Version
   *
   * @param red Rot 0..255
   * @param green Grün 0..255
   * @param blue Blau 0..255
   * @return der Korrigierte RGBW Farbwert
   */
  public static cRGBW convertRGB2RGBW(int red, int green, int blue)
  {
    cRGBW  computedColors;
    double maxVal;
    double minVal;
    double k;
    double r, g, b, w;

    //
    // Farbbeschreibung anlegen
    //
    computedColors = new cRGBW();
    //
    // Berechnug in double
    //
    r = ( double ) red;
    g = ( double ) green;
    b = ( double ) blue;
    //
    // Bestimme maximal und Minimal Wert der Farben
    //
    maxVal = Math.max(r, Math.max(g, b));
    minVal = Math.min(r, Math.min(g, b));
    //
    // White - Output bestimmen
    //
    if( (minVal / maxVal) < 0.5f )
    {
      //
      // in diesem Falle berechne den Wert von Weiss
      //
      w = (minVal * maxVal) / (maxVal - minVal);
    }
    else
    {
      //
      // hier ist das einfach der Maximalwert der Farben
      //
      w = Math.round(maxVal);
    }
    //
    // ab hier könnte es schon klappen, aber die Helligeit stimmt nicht
    // daher jetzt die H_elligkeit berechnen
    // (Es handelt sich um eine Matrix)
    //
    k = (w + maxVal) / minVal;
    //
    // Helligkeit korrigieren und in Array sichern
    //
    computedColors.red = ( int ) Math.round((k * r) - w);
    computedColors.green = ( int ) Math.round((k * g) - w);
    computedColors.blue = ( int ) Math.round((k * b) - w);
    computedColors.white = ( int ) (Math.round(w));
    //
    return (computedColors);
  }


  /*
  These are plain C++ functions for you to convert the known color space RGB to the rather new color
  space RGBW. Just pass your usual red green and blue values in 0..255 to rgbToRgbw
  and you'll get a new set of changed color values with white in 0..255.
  This technique enables you to make full use of a display panel with red, green, blue and white emitters.

  struct colorRgbw {
    unsigned int   red;
    unsigned int   green;
    unsigned int   blue;
    unsigned int   white;
  };

  // The saturation is the colorfulness of a color relative to its own brightness.
  unsigned int saturation(colorRgbw rgbw) {
      // Find the smallest of all three parameters.
      float low = min(rgbw.red, min(rgbw.green, rgbw.blue));
      // Find the highest of all three parameters.
      float high = max(rgbw.red, max(rgbw.green, rgbw.blue));
      // The difference between the last two variables
      // divided by the highest is the saturation.
      return round(100 * ((high - low) / high));
  }

  // Returns the value of white
  unsigned int getWhite(colorRgbw rgbw) {
      return (255 - saturation(rgbw)) / 255 * (rgbw.red + rgbw.green + rgbw.blue) / 3;
  }

  // Use this function for too bright emitters. It corrects the highest possible value.
  unsigned int getWhite(colorRgbw rgbw, int redMax, int greenMax, int blueMax) {
      // Set the maximum value for all colors.
      rgbw.red = (float)rgbw.red / 255.0 * (float)redMax;
      rgbw.green = (float)rgbw.green / 255.0 * (float)greenMax;
      rgbw.blue = (float)rgbw.blue / 255.0 * (float)blueMax;
      return (255 - saturation(rgbw)) / 255 * (rgbw.red + rgbw.green + rgbw.blue) / 3;
      return 0;
  }

  // Example function.
  colorRgbw rgbToRgbw(unsigned int red, unsigned int green, unsigned int blue) {
      unsigned int white = 0;
      colorRgbw rgbw = {red, green, blue, white};
      rgbw.white = getWhite(rgbw);
      return rgbw;
  }

  // Example function with color correction.
  colorRgbw rgbToRgbw(unsigned int red, unsigned int redMax,
                      unsigned int green, unsigned int greenMax,
                      unsigned int blue, unsigned int blueMax) {
      unsigned int white = 0;
      colorRgbw rgbw = {red, green, blue, white};
      rgbw.white = getWhite(rgbw, redMax, greenMax, blueMax);
      return rgbw;
  }

 #############################

 Think of your leds as a huge pixel. Oliver's answer uses Wo = min(Ri,Gi,Bi),
 what is computationaly cheap and just works. This paper explains other
 alternatives to minimize power consumption, what is good for a home automation
 project. I'm also on a home automation project with OpenHAB, Arduino and RGBW
 leds and on the one hand paper proposed alternative would be good, on the
 other hand a huge LUT would just not work and convert all values on arduino
 eighter. I would suggest you to try correcting RGB values using not so
 energy eficient technics as cited in the paper:

  Assuming Ri, Gi, Bi are color inputs, integers from 0 to 255, so Q = 255 and
  that your outputs are Wo, Ro, Go and Bo with values from 0 to 255:

  M = max(Ri,Gi,Bi)
  m = min(Ri,Gi,Bi)

  Wo = if (m/M < 0.5) use ( (m*M) / (M-m) ) else M
  Q = 255
  K = (Wo + M) / m
  Ro = floor( [ ( K * Ri ) - Wo ] / Q )
  Go = floor( [ ( K * Gi ) - Wo ] / Q )
  Bo = floor( [ ( K * Bi ) - Wo ] / Q )
  Exercise it in a spreadsheet before implementing, experiment
  on Wo = m, m^2 and -m^3+m^2+m, correct Wo value with the right
  Qw and you will be surprised that the simple solution without
  correction is not what one would expect and that other answers
  do not vary that much. Check the paper for color distortion
  results, I suppose that if you do not correct RGB values
  you will end up with some washed out colors.
  */

}
