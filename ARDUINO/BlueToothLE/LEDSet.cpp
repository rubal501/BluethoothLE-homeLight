/* 
* LEDSet.cpp
*
* Created: 30.12.2015 19:36:34
* Author: dmarc
*/


#include "Arduino.h"
#include "CommandParser.hpp"
#include "LEDSet.hpp"

// default constructor
LEDSet::LEDSet()
{
} //LEDSet

// default destructor
LEDSet::~LEDSet()
{
} //~LEDSet

void LEDSet::init( EEPROMConfig& cfg )
{
  //
  // Mode OUTPUT setzen
  //
  pinMode(PWM_RED, OUTPUT);
  pinMode(PWM_GREEN, OUTPUT);
  pinMode(PWM_BLUE, OUTPUT);
  pinMode(PWM_WHITE, OUTPUT);
  //
  // Anfangshelligkeit setzen
  //
  LEDSet::setBrightnessFromConfig( cfg );
}

void LEDSet::setBrightnessOff()
{
  analogWrite( PWM_RED, 0 );
  analogWrite( PWM_GREEN, 0 );
  analogWrite( PWM_BLUE, 0 );
  analogWrite( PWM_WHITE, 0 );
}

void LEDSet::setBrightnessFromConfig( EEPROMConfig& cfg )
{
  analogWrite( PWM_RED, cfg.getCalRed() );
  analogWrite( PWM_GREEN, cfg.getCalGreen() );
  analogWrite( PWM_BLUE, cfg.getCalBlue() );
  analogWrite( PWM_WHITE, cfg.getCalWhite() );
}


void LEDSet::setBrightness( EEPROMConfig& cfg, const byte *vals )
{
  byte Ri,Gi,Bi,Wi,kdo;
  byte Ro,Go,Bo,Wo;
  
  // Das Kommando erfahren
  kdo = *vals++;
  // Farben lesen
  Ri = *vals++;
  Gi = *vals++;
  Bi = *vals++;
  Wi = *vals;
  // Rohdaten speichern
  cfg.setRed( Ri );
  cfg.setGreen( Gi );
  cfg.setBlue( Bi );
  cfg.setWhite( Wi );
  //
  // selber in RGBW umsetzen?
  //
  if( kdo == C_SETCALRGB )
  {
    //
    // jetzt das Minimum
    //
    Wo = LEDSet::cMin(Ri, Gi, Bi);
    // und die kalibrierten Werte
    Ro = LEDSet::getColorVal(Ri, Wo);
    Go = LEDSet::getColorVal(Gi, Wo);
    Bo = LEDSet::getColorVal(Bi, Wo);
    // speichern
    cfg.setCalRed(Ro);
    cfg.setCalGreen(Go);
    cfg.setCalBlue(Bo);
    cfg.setCalWhite(Wo);
    // anzeigen
    analogWrite(PWM_RED, (int)Ro );
    analogWrite(PWM_GREEN, (int)Go );
    analogWrite(PWM_BLUE, (int)Bo );
    analogWrite(PWM_WHITE, (int)Wo );
  }
  else
  {
    // speichern
    cfg.setCalRed(Ri);
    cfg.setCalGreen(Gi);
    cfg.setCalBlue(Bi);
    cfg.setCalWhite(Wi);
    // anzeigen
    analogWrite(PWM_RED, (int)Ri );
    analogWrite(PWM_GREEN, (int)Gi );
    analogWrite(PWM_BLUE, (int)Bi );
    analogWrite(PWM_WHITE, (int)Wi );
  }
}

byte LEDSet::cMin( byte a, byte b, byte c )
{
  byte minAB;
  (a < b) ? minAB = a : minAB = b;
  if(c < minAB )
  {
    return( c );
  }
  return( minAB );
}

byte LEDSet::getColorVal( byte col, byte white )
{
  if( col > white )
  {
    return( col - white);
  }
  return( 0 );
}


