
#include <Arduino.h>
#include "SoftwareSerial.hpp"
#include "config.hpp"
#include "main.hpp"
#include "CommandParser.hpp"
#include "Communication.hpp"
#include "EEPROMConfig.hpp"
#include "SysConfig.hpp"
#include "LEDSet.hpp"

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
EEPROMConfig theConfig;

const int baudRateVal = DESTBAUDRATE_VAL;
String btInputString="";
unsigned long saveTime = 0L;
unsigned long eepromShutoffTime = 0L;
unsigned long onlineTestTime = 0L;
const unsigned long SAVEDELAY = 2500L;
const unsigned long BEAMDELAY = 250L;
const unsigned long ONLINETEST_DELAY = 300L;
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
  eepromShutoffTime = millis() + BEAMDELAY;
  digitalWrite(ONLINE_PIN, HIGH );
  SysConfig::SystemPreInit( theConfig );
  // LED Helligkeiten aus Config einstellen
  LEDSet::init( theConfig );    
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
      
       // Gib die Farbe an die LED aus 0x04
      // Nativ, ohne Kalibrierung
      case C_SETCOLOR:
      // oder kalibriere RGB selber  0x05
      case C_SETCALRGB:
        isLightsOFF = false;
        LEDSet::setBrightness( theConfig, kdo );
        #ifdef DEBUG
        Serial.println("SET COLOR emfpangen..." );
        #endif        
        // in frühestens 2 Sekunden sichern
        saveTime = millis() + SAVEDELAY;
        break;

      // Setzte den Modulnamen
      case C_SETNAME:
        #ifdef DEBUG
        Serial.println("Modulname setzen..." );
        #endif
        paramString = CommandParser::getModuleName(btInputString);
        if( paramString.length() > 2 )
        {
          SysConfig::setModuleName( mySerial, *myComm, theConfig, paramString );
          #ifdef DEBUG
          Serial.println("Modulname setzen: " + paramString );
          #endif
          theConfig.saveConfig();
          // Das Modul neu starten....
          SysConfig::restartBTModul( mySerial, *myComm );
          // in frühestens 2 Sekunden sichern
          saveTime = millis() + SAVEDELAY;
        }
        break;
      
      // der AN/AUS Schalter 
      case C_ONOFF:
        if( isLightsOFF )
        {
          #ifdef DEBUG
          Serial.println("LEDs ON..." );
          #endif
          // Die Dinger an machen
          LEDSet::setBrightnessFromConfig( theConfig );
        }
        else
        {
          #ifdef DEBUG
          Serial.println("LEDs OFF..." );
          #endif
          LEDSet::setBrightnessOff();
        }
        isLightsOFF = !isLightsOFF;
        break;
        
      default:
        //nix verstehen meister!
        Serial.println("NIX VERSTEHEN..." );
        break;
    } 
    btInputString = "";
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
      eepromShutoffTime = millis() + BEAMDELAY;
      saveTime = 0L;
    }
  }
  //
  // Die LED zur Anzeige des EEPROM schreibens AUS
  //
  if( eepromShutoffTime > 0L )
  {
    // EEPROM Leuchte aus
    if( eepromShutoffTime < millis() )
    {
      digitalWrite(ONLINE_PIN, LOW );
      eepromShutoffTime = 0L;
    }    
  }
  //
  // ab und an schauen, ob das Modul den Onlinestatus
  // verändert hat
  //
  if( onlineTestTime < millis() )
  {
    if( digitalRead(ONLINE_PIN) == HIGH )
    {
      //
      // Modul ist ONLINE
      //
      if( !isOnline )
      {
        // Status verändert
        Serial.println("Modul ging ONLINE");
        isOnline = true;
      }
    }
    else
    {
      //
      // Modul ist OFFLINE
      //
      if( isOnline )
      {
        // Status verändert
        Serial.println("Modul ging OFFLINE");
        isOnline = false;
      }
    }
    onlineTestTime = millis() + ONLINETEST_DELAY;    
  }
  //
  if(Serial.available()) 
  {
    mySerial.write(Serial.read());
  }

}

