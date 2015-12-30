
#include <Arduino.h>
#include "SoftwareSerial.hpp"
#include "config.hpp"
#include "main.hpp"
#include "CommandParser.hpp"
#include "Communication.hpp"
#include "EEPROMConfig.hpp"
#include "SysConfig.hpp"

//
// der Modultyp
//
const static String moduleType = "DM_RGBW";

//
// Software Serial Objekt erzeigen und initiieren
//
SoftwareSerial mySerial( RXPIN, TXPIN, false); // RX, TX
//
// das Kommunikationsobjekt (USB und BT Modul)
//
Communication *myComm = new Communication();
//
// die gespeicherten Vorgaben
//
EEPROMConfig theConfig;// = new EEPROMConfig();

const int baudRateVal = DESTBAUDRATE_VAL;
String btInputString="";
unsigned long saveTime = 0L;
const unsigned long SAVEDELAY = 2000L;
boolean isOnline = false;
boolean isLightsOFF = false;

//#############################################################################
// SETUP
//#############################################################################
void setup() 
{
  //
  // Die Sachen, die schnell gehen sollen zuerst...
  //
  SysConfig::SystemPreInit( theConfig );
  //
  // Serielle USB Verbindung öffnen
  //
  Serial.begin(BAUDRATE_USB);
  while (!Serial)
  {
    ; // wait for serial port to connect. Needed for native USB port only
  }
  #ifdef DEBUG
  Serial.println("BT Testprogramm!");
  Serial.println( "Lese Konfiguration...");
  #else
  Serial.println("MODULSTART");
  #endif
  //
  // Konfiguriere das in einer ausgelagerten Sequenz
  //
  SysConfig::SystemInit( mySerial, *myComm, theConfig );
  //
  #ifdef DEBUG  
  Serial.println("");
  #endif
  Serial.println("BEREIT!");
  #ifdef DEBUG
  Serial.println("==========================");
  #endif
}

//#############################################################################
// MAIN
//#############################################################################
void loop() 
{ 
  byte cmd = C_UNKNOWN;
  byte kdo[25];
  String paramString;
  // 
  // Und das hier immer wieder
  //
   
  //
  // gab es ein Zeilenende/Kommandoende Zeichen in der Eingabe?
  //
  if( myComm->readMessageIfAvavible(mySerial, btInputString) )
  {
    // Zeilenende, das war ein Kommando?
    cmd = CommandParser::parseCommand( btInputString, kdo );
    btInputString = "";
    switch( cmd )
    { 
      // Frage nach dem Modultyp 0x00 
      case C_ASKTYP:
        myComm->sendModuleType(mySerial, moduleType);
        #ifdef DEBUG
        Serial.println("Sende Module Typ an Master..." );
        Serial.println( moduleType );
        #endif
        break;
      
      // Frage nach dem Modulnamen 0x01
      case C_ASKNAME:
        paramString = theConfig.getModuleName();
        myComm->sendModuleName( mySerial, paramString );
        #ifdef DEBUG
        Serial.println("Sende Module Name an Master..." );
        #endif        
        break;
        
      // Frage nach RGBW im Modul 0x02
      // die unkalibrierten Werte an die App
      case C_ASKRGBW:
        kdo[0] = theConfig.getRed();
        kdo[1] = theConfig.getGreen();
        kdo[2] = theConfig.getBlue();
        kdo[3] = theConfig.getWhite();
        myComm->sendRGBW( mySerial, kdo );
        #ifdef DEBUG
        Serial.println("Sende RGBW (raw) an Master..." );
        #endif
        break;
      
      // Frage nach RGBW im Modul 0x03
      case C_ASKCALRGBW:
        kdo[0] = theConfig.getCalRed();
        kdo[1] = theConfig.getCalGreen();
        kdo[2] = theConfig.getCalBlue();
        kdo[3] = theConfig.getCalWhite();
        myComm->sendRGBW( mySerial, kdo );
        #ifdef DEBUG
        Serial.println("Sende RGBW (cal) an Master..." );
        #endif
        break;
      
      // Gib die Farbe an die LED aus 0x04
      // Nativ, ohne Kalibrierung
      case C_SETCOLOR:
        isLightsOFF = false;
        theConfig.setRed( kdo[1] );
        theConfig.setGreen( kdo[2] );
        theConfig.setBlue( kdo[3] );
        theConfig.setWhite( kdo[4] );
        // überschreibe die Kalibrierten Werte auch
        theConfig.setCalRed( kdo[1] );
        theConfig.setCalGreen( kdo[2] );
        theConfig.setCalBlue( kdo[3] );
        theConfig.setCalWhite( kdo[4] );
        analogWrite(PWM_RED, kdo[1]);
        analogWrite(PWM_GREEN, kdo[2]);
        analogWrite(PWM_BLUE, kdo[3]);
        analogWrite(PWM_WHITE, kdo[4]);
        #ifdef DEBUG
        Serial.println("SET COLOR (raw) emfpangen..." );
        #endif        
        // in frühestens 2 Sekunden sichern
        saveTime = millis() + SAVEDELAY;
        break;

      // Speichere die Farbe im EEPROM 0x05
      // die unkalibrierten Werte für die App, als Basis für die Grundeinstellung
      // bei ASKRGBW
      case C_SETCOLSAVE:
        theConfig.setRed( kdo[1] );
        theConfig.setGreen( kdo[2] );
        theConfig.setBlue( kdo[3] );
        theConfig.setWhite( kdo[4] );
        #ifdef DEBUG
        Serial.println("SET COLOR SAVE emfpangen..." );
        #endif
        // in frühestens 2 Sekunden sichern
        saveTime = millis() + SAVEDELAY;
        break;

      // Zeige die Farbe Kalibriert direkt an
      // direakt nach dem Kommando C_SETCOLSAVE von der App
      case C_SETCALCOLOR:
        isLightsOFF = false;
        theConfig.setCalRed( kdo[1] );
        theConfig.setCalGreen( kdo[2] );
        theConfig.setCalBlue( kdo[3] );
        theConfig.setCalWhite( kdo[4] );
        analogWrite(PWM_RED, kdo[1]);
        analogWrite(PWM_GREEN, kdo[2]);
        analogWrite(PWM_BLUE, kdo[3]);
        analogWrite(PWM_WHITE, kdo[4]);
        #ifdef DEBUG
        Serial.println("SET COLOR (cal) emfpangen..." );
        #endif
        // in frühestens 2 Sekunden sichern
        saveTime = millis() + SAVEDELAY;
        break;

      // der AN/AUS Schalter 
      case C_ONOFF:
        if( isLightsOFF )
        {
          #ifdef DEBUG
          Serial.println("LEDs ON..." );
          #endif
          // Die Dinger an machen
          analogWrite( PWM_RED, theConfig.getCalRed() );
          analogWrite( PWM_GREEN, theConfig.getCalGreen() );
          analogWrite( PWM_BLUE, theConfig.getCalBlue() );
          analogWrite( PWM_WHITE, theConfig.getCalWhite() );
        }
        else
        {
          #ifdef DEBUG
          Serial.println("LEDs OFF..." );
          #endif
          analogWrite( PWM_RED, 0 );
          analogWrite( PWM_GREEN, 0 );
          analogWrite( PWM_BLUE, 0 );
          analogWrite( PWM_WHITE, 0 );
        }
        isLightsOFF = !isLightsOFF;
        break;
        
      default:
        //nix verstehen meister!
        Serial.println("NIX VERSTEHEN..." );
        break;
    }
    
  }
  
  //
  // wenn was gesichert werden soll, dann mach das alle 2 Sekunden
  //
  if( saveTime > 0L )
  {
    // da soll was gesichert werden!
    if( saveTime < millis() )
    {
      theConfig.saveConfig();
      saveTime = 0L;
    }
  }
  
  if(Serial.available()) 
  {
    mySerial.write(Serial.read());
  }

}

