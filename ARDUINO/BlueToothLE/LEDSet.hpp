/* 
* LEDSet.h
*
* Created: 30.12.2015 19:36:34
* Author: dmarc
*/

#include "EEPROMConfig.hpp"
#include "config.hpp"


#ifndef __LEDSET_H__
#define __LEDSET_H__


class LEDSet
{
//variables
public:
protected:
private:

//functions
public:
	LEDSet();
	~LEDSet();
  //
  // initialisiere die Helligkeite der LED aus der Konfiguration
  //
  static void init( EEPROMConfig& cfg );
  //
  // Schalte _Helligkeit auf null
  //
  static void setBrightnessOff();
  //
  // Helligkeit aus der Konfiguration setzen
  //
  static void setBrightnessFromConfig( EEPROMConfig& cfg );
  //
  // Helligkeit aus RGB Array
  // 0 => Kommando, 1,2,3,4 => R G B W
  //
  static void setBrightness( EEPROMConfig& cfg,  const byte *vals );
protected:
private:
	LEDSet( const LEDSet &c );
	LEDSet& operator=( const LEDSet &c );
  //
  // Das Minimum der Farben finden
  //
  static inline byte cMin( byte a, byte b, byte c );
  //
  // Farbe nach Korrektur (aber minimal Wert 0) finden
  //
  static inline byte getColorVal( byte col, byte white );
}; //LEDSet

#endif //__LEDSET_H__
