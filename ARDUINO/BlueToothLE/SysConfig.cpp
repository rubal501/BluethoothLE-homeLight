/* 
* SysConfig.cpp
*
* Created: 23.12.2015 17:13:50
* Author: dmarc
*/

#include <Arduino.h>
#include "SoftwareSerial.hpp"
#include "config.hpp"
#include "EEPROMConfig.hpp"
#include "Communication.hpp"
#include "SysConfig.hpp"


// default constructor
SysConfig::SysConfig()
{
} //SysConfig

// default destructor
SysConfig::~SysConfig()
{
} //~SysConfig

void SysConfig::SystemPreInit( EEPROMConfig& theConfig )
{
  //
  // EEPROM-Pin setzen
  //
  pinMode( EEPROM_PIN, OUTPUT );
  //
  // Die Konfiguration laden (Aus EEPROM)
  //
  theConfig.loadConfig();
  //
  // PWM Ports vorbereiten, 488 Herz PWM
  //
  SysConfig::setPwmFrequency(PWM_RED, 64);
  SysConfig::setPwmFrequency(PWM_GREEN, 64);
  SysConfig::setPwmFrequency(PWM_BLUE, 64);
  SysConfig::setPwmFrequency(PWM_WHITE, 64);
}

void SysConfig::SystemInit( SoftwareSerial& mySerial, Communication& myComm, EEPROMConfig& theConfig )
{
  long currSpeed;
  //
  // Ports f�r Software Serial Serial setzen
  //
  pinMode( TXPIN, OUTPUT );
  pinMode( RXPIN, INPUT );
  // Interruptsteuerung des Empfanges AN
  mySerial.listen();
  //
  // Port f�r ONLINE Check des BT Moduls
  //
  pinMode( ONLINE_PIN, INPUT ); 
  digitalWrite(ONLINE_PIN, HIGH ); 
  //
  // der RESET-PIN (HIGH ist alles in ordnung)
  //
  pinMode( RESET_PIN, OUTPUT );
  digitalWrite( RESET_PIN, HIGH );
  delay(300);
#ifdef DEBUG
  Serial.print("ROT: ");
  Serial.println( theConfig.getCalRed(), HEX );
  Serial.print("GRUEN: ");
  Serial.println( theConfig.getCalGreen(), HEX );
  Serial.print("BLAU: ");
  Serial.println( theConfig.getCalBlue(), HEX );
  Serial.print("WEISS: ");
  Serial.println( theConfig.getCalWhite(), HEX );
#endif
  //
  // Verbindungsgeschwindigkeit checken,
  // langsdam rantasten...
  // gibt es 0 zur�ck, PANIK, 
  // TODO: noch was machen
  currSpeed = myComm.findCommSpeed( mySerial );
  //*
  if( currSpeed != 19200 )
  {
    Serial.println( "Setze neue Geschwindigkeit auf 19200..." );
    myComm.sendCommand( mySerial, "AT+BAUD1" );
    mySerial.begin( 19200L );
    delay(250);
    myComm.sendCommand( mySerial, "AT" );
    Serial.println( "Setze neue Geschwindigkeit auf 19200...OK" );
  }
  //
  // RESET, saubere Anfangskonfig
  //
  myComm.sendCommand( mySerial, "AT+RESET" );
  delay( 1500 );
  //
  // Statusport-LED ohne blinken
  //
  myComm.sendCommand( mySerial, "AT+PIO11" );
  //
  // Modulname setzen
  //
  setModuleName( mySerial, myComm, theConfig, theConfig.getModuleName() );
  //
  // Version erfragen
  //
#ifdef DEBUG
  myComm.sendCommand( mySerial, "AT+VERS?" );
  myComm.sendCommand( mySerial, "AT+BAUD?" );
  myComm.sendCommand( mySerial, "AT+NAME?" );
#endif
  //
  // AT Mode festlegen
  //
  myComm.sendCommand( mySerial, "AT+MODE0" ); 
}

//
// Setze im Modul den Modulnamen
//
void SysConfig::setModuleName( SoftwareSerial& mySerial, Communication& myComm, EEPROMConfig& theConfig, String name )
{
  String mName;
  //
  // Neuer Name oder Name aus der Configuration?
  //
  if( name == theConfig.getModuleName() )
  {
    // Neuer Name
    theConfig.setModuleName( name );
  }
  mName = "AT+NAME" + name;
  //
  // Modulname setzen
  //
  myComm.sendCommand( mySerial, mName );
}

/**
 * Divides a given PWM pin frequency by a divisor.
 * (Arduino Playground)
 * 
 * The resulting frequency is equal to the base frequency divided by
 * the given divisor:
 *   - Base frequencies:
 *      o The base frequency for pins 3, 9, 10, and 11 is 31250 Hz.
 *      o The base frequency for pins 5 and 6 is 62500 Hz.
 *   - Divisors:
 *      o The divisors available on pins 5, 6, 9 and 10 are: 1, 8, 64,
 *        256, and 1024.
 *      o The divisors available on pins 3 and 11 are: 1, 8, 32, 64,
 *        128, 256, and 1024.
 * 
 * PWM frequencies are tied together in pairs of pins. If one in a
 * pair is changed, the other is also changed to match:
 *   - Pins 5 and 6 are paired on timer0
 *   - Pins 9 and 10 are paired on timer1
 *   - Pins 3 and 11 are paired on timer2
 * 
 * Note that this function will have side effects on anything else
 * that uses timers:
 *   - Changes on pins 3, 5, 6, or 11 may cause the delay() and
 *     millis() functions to stop working. Other timing-related
 *     functions may also be affected.
 *   - Changes on pins 9 or 10 will cause the Servo library to function
 *     incorrectly.
 * 
 * Thanks to macegr of the Arduino forums for his documentation of the
 * PWM frequency divisors. His post can be viewed at:
 *   http://www.arduino.cc/cgi-bin/yabb2/YaBB.pl?num=1235060559/0#4
 */
void SysConfig::setPwmFrequency(int pin, int divisor) 
{
  byte mode;
  if(pin == 5 || pin == 6 || pin == 9 || pin == 10) 
  {
    switch(divisor) 
    {
      case 1: mode = 0x01; break;
      case 8: mode = 0x02; break;
      case 64: mode = 0x03; break;
      case 256: mode = 0x04; break;
      case 1024: mode = 0x05; break;
      default: return;
    }
    if(pin == 5 || pin == 6) 
    {
      TCCR0B = (TCCR0B & 0b11111000) | mode;
    } 
    else 
    {
      TCCR1B = (TCCR1B & 0b11111000) | mode;
    }
  } 
  else if(pin == 3 || pin == 11) 
  {
    switch(divisor) 
    {
      case 1: mode = 0x01; break;
      case 8: mode = 0x02; break;
      case 32: mode = 0x03; break;
      case 64: mode = 0x04; break;
      case 128: mode = 0x05; break;
      case 256: mode = 0x06; break;
      case 1024: mode = 0x7; break;
      default: return;
    }
    TCCR2B = (TCCR2B & 0b11111000) | mode;
  }
}



