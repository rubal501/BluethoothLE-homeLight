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

void SysConfig::SystemPreInit( EEPROMConfig& theConfig, SoftwareSerial* dComm  )
{
  #ifdef DEBUG
  //
  // Ports für Software Serial Serial setzen
  //
  pinMode( TXPIN, OUTPUT );
  pinMode( RXPIN, INPUT );
  dComm->begin(BAUDRATE_DEBUG);
  // Interruptsteuerung des Empfanges AN
  dComm->listen();
  dComm->println("SystemPreInit: OK");
  #endif
  //
  // EEPROM-Pin setzen
  //
  pinMode( EEPROM_PIN, OUTPUT );
  //
  // Die Konfiguration laden (Aus EEPROM)
  //
  theConfig.loadConfig(dComm);
  //
  // PWM Ports vorbereiten, 488 Herz PWM
  //
  SysConfig::setPwmFrequency(PWM_RED, 64);
  SysConfig::setPwmFrequency(PWM_GREEN, 64);
  SysConfig::setPwmFrequency(PWM_BLUE, 64);
  SysConfig::setPwmFrequency(PWM_WHITE, 64);
}

void SysConfig::SystemInit( HardwareSerial& comm, SoftwareSerial* dComm, Communication& myComm, EEPROMConfig& theConfig   )
{
  long currSpeed;
  //
  // Port für ONLINE Check des BT Moduls
  //
  pinMode( ONLINE_PIN, INPUT ); 
  //
  // der RESET-PIN (HIGH ist alles in Ordnung)
  //
  pinMode( RESET_PIN, OUTPUT );
  digitalWrite( RESET_PIN, HIGH );
  delay(300);
  #ifdef DEBUG
  if( dComm != NULL )
  {
    dComm->println("SystemInit...");
    dComm->print("ROT: ");
    dComm->println( theConfig.getCalRed(), HEX );
    dComm->print("GRUEN: ");
    dComm->println( theConfig.getCalGreen(), HEX );
    dComm->print("BLAU: ");
    dComm->println( theConfig.getCalBlue(), HEX );
    dComm->print("WEISS: ");
    dComm->println( theConfig.getCalWhite(), HEX );
    dComm->flush();
  }
  #endif
  //
  // Verbindungsgeschwindigkeit checken,
  // langsdam rantasten...
  // gibt es 0 zurück, PANIK, 
  // TODO: noch was machen
  currSpeed = myComm.findCommSpeed( comm, dComm );
  //*
  if( currSpeed != BAUDRATE_BT )
  {
    #ifdef DEBUG   
    if( dComm != NULL )
    {
      dComm->print( "Setze neue Geschwindigkeit auf " );
      dComm->println( BAUDRATE_BT );
      dComm->flush();
    } 
    #endif
    myComm.sendCommand( comm, dComm, "AT+BAUD1" );
    comm.begin( BAUDRATE_BT );
    delay(250);
    myComm.sendCommand( comm, dComm, "AT" );
    #ifdef DEBUG
    if( dComm != NULL )
    {
      dComm->print( "Setze neue Geschwindigkeit auf " );
      dComm->print( BAUDRATE_BT );
      dComm->println( " ...OK");
      dComm->flush();
    }
    #endif
  }
  #ifdef DEBUG
  if( dComm != NULL )
  {
    dComm->print( "Geschwindigkeit fuer BT Modul: " );
    dComm->print( BAUDRATE_BT );
    dComm->println( " baud");
    dComm->flush();
  }
  #endif
  //
  // Statusport-LED ohne blinken
  //
  myComm.sendCommand( comm, dComm, "AT+PIO11" );
  //
  // Modulname setzen
  //
  setModuleName( comm, dComm, myComm, theConfig, theConfig.getModuleName() );
  //
  // Version erfragen
  //
  #ifdef DEBUG
  myComm.sendCommand( comm, dComm, "AT+VERS?" );
  myComm.sendCommand( comm, dComm, "AT+BAUD?" );
  myComm.sendCommand( comm, dComm, "AT+NAME?" );
  #endif
  //
  // AT Mode festlegen
  //
  myComm.sendCommand( comm, dComm, "AT+MODE0" ); 
  #ifdef DEBUG
  if( dComm != NULL )
  {
    dComm->println("SystemInit...OK");
  }
  #endif  
}

//
// Setze im Modul den Modulnamen
//
void SysConfig::setModuleName( HardwareSerial& comm, SoftwareSerial* dComm, Communication& myComm, EEPROMConfig& theConfig, String name )
{
  String mName;
  //
  // Neuer Name
  //
  theConfig.setModuleName( name );
  mName = "AT+NAME" + name;
  //
  // Modulname setzen
  //
  myComm.sendCommand( comm, dComm, mName );
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

/************************************************************************/
/* Das Modul wird ausgeschaltet, 500 ms gewartet und dann wieder        */
/* eingeschaltet. Die Funktion bockiert etwas über 6000 ms, die BT      */
/* Verbindung wird (natürlich) getrennt                                 */
/************************************************************************/
void SysConfig::restartBTModul( HardwareSerial comm, SoftwareSerial* dComm, Communication& myComm )
{
  #ifdef DEBUG
  if( dComm != NULL )
  {
    dComm->println("BT Modul resetten...");
    dComm->flush();
  }
  #endif
  //
  // RESET, saubere Anfangskonfig
  //
  myComm.sendCommand( comm, dComm, "AT+RESET" );
  delay( 1500 );
  //
  // Statusport-LED ohne blinken
  //
  myComm.sendCommand( comm, dComm,"AT+PIO11" );
  //
  // Völlig herzlos rebooten
  //
  //asm volatile ("  jmp 0");
}

