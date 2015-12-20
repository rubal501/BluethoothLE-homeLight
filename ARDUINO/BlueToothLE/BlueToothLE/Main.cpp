/*
 Receives from the hardware serial, sends to software serial.
 Receives from software serial, sends to hardware serial.
 The circuit:
 * RX is digital pin 10 (connect to TX of other device)
 * TX is digital pin 11 (connect to RX of other device)
 Note:
 */
#include <Arduino.h>
#include "SoftwareSerial.hpp"
#include "config.hpp"
#include "main.hpp"
#include "CommandParser.hpp"
#include "Communication.hpp"
#include "EEPROMConfig.hpp"
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

//#############################################################################
// SETUP
//#############################################################################
void setup() 
{
  long currSpeed;
  //
  // Serielle USB Verbindung öffnen
  //
  Serial.begin(BAUDRATE_USB);
  while (!Serial)
  {
    ; // wait for serial port to connect. Needed for native USB port only
  }
  // Interruptsteuerung des Empfanges AN
  mySerial.listen();
#ifdef DEBUG
  //delay(200);
  Serial.println("BT Testprogramm!");
  Serial.println( "Lese Konfiguration...");
#endif
  theConfig.loadConfig();
  //
  // PWM Ports vorbereiten
  //
  analogWrite( PWM_RED, theConfig.getRed() );
  analogWrite( PWM_GREEN, theConfig.getGreen() );
  analogWrite( PWM_BLUE, theConfig.getBlue() );
  analogWrite( PWM_WHITE, theConfig.getWhite() );
  //
  // Ports für Software Serial Serial setzen
  //
  pinMode( TXPIN, OUTPUT );
  pinMode( RXPIN, INPUT );
  //
  // Port für ONLINE Check des BT Moduls
  //
  pinMode( ONLINE_PIN, INPUT ); 
  digitalWrite(ONLINE_PIN, HIGH ); 
  //
  // der RESET-PIN (HIGH ist alles in ordnung)
  //
  pinMode( RESET_PIN, OUTPUT );
  digitalWrite( RESET_PIN, HIGH );
  /*
#ifdef DEBUG
Serial.println( "BT Modul resetten..." );
#endif
  delay(200);
  digitalWrite( RESET_PIN, LOW );
  delay(100);
  digitalWrite( RESET_PIN, HIGH );
  delay(2000);
#ifdef DEBUG
Serial.println( "BT Modul resetten...OK" );
#endif
    */
#ifdef DEBUG
  delay(300);
  Serial.print("ROT: ");
  Serial.println( theConfig.getRed(), HEX );
  Serial.print("GRUEN: ");
  Serial.println( theConfig.getGreen(), HEX );
  Serial.print("BLAU: ");
  Serial.println( theConfig.getBlue(), HEX );
  Serial.print("WEISS: ");
  Serial.println( theConfig.getWhite(), HEX );
#endif
  //
  // Verbindungsgeschwindigkeit checken,
  // langsdam rantasten...
  // gibt es 0 zurück, PANIK, 
  // TODO: noch was machen
  currSpeed = myComm->findCommSpeed( mySerial );
  //*
  if( currSpeed != 19200 )
  {
    Serial.println( "Setze neue Geschwindigkeit auf 19200..." );
    myComm->sendCommand( mySerial, "AT+BAUD1" );
    mySerial.begin( 19200L );
    delay(250);
    myComm->sendCommand( mySerial, "AT" );
    Serial.println( "Setze neue Geschwindigkeit auf 19200...OK" );
  }
  //*/
  //myComm->findCommSpeed( mySerial, DESTBAUDRATE_VAL );
  //
  // RESET
  //
  myComm->sendCommand( mySerial, "AT+RESET" );
  delay( 1500 );
  //
  // Statusport-LED ohne blinken
  //
  myComm->sendCommand( mySerial, "AT+PIO11" );
  //
  // Modulname setzen
  //
  String mName = "AT+NAME" + theConfig.getModuleName();
  myComm->sendCommand( mySerial, mName );
  //
  // Version erfragen
  //
  myComm->sendCommand( mySerial, "AT+VERS?" );
#ifdef DEBUG  
  myComm->sendCommand( mySerial, "AT+BAUD?" );
  myComm->sendCommand( mySerial, "AT+NAME?" );
#endif
  //
  // AT Mode festlegen
  //
  myComm->sendCommand( mySerial, "AT+MODE0" );
#ifdef DEBUG  
  Serial.println("");
  Serial.println("BEREIT!");
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
      case C_ASKRGB:
        kdo[0] = theConfig.getRed();
        kdo[1] = theConfig.getGreen();
        kdo[2] = theConfig.getBlue();
        kdo[3] = theConfig.getWhite();
        myComm->sendRGBW( mySerial, kdo );
        #ifdef DEBUG
        Serial.println("Sende RGBW an Master..." );
        #endif
        break;
      
      // Gib die Farbe an die LED aus 0x03
      case C_SETCOLOR:
        analogWrite(PWM_RED, kdo[1]);
        analogWrite(PWM_GREEN, kdo[2]);
        analogWrite(PWM_BLUE, kdo[3]);
        analogWrite(PWM_WHITE, kdo[4]);
        theConfig.setRed( kdo[1] );
        theConfig.setGreen( kdo[2] );
        theConfig.setBlue( kdo[3] );
        theConfig.setWhite( kdo[4] );
#ifdef DEBUG
        Serial.println("SET COLOR emfpangen..." );
#endif        
        // in frühestens 2 Sekunden sichern
        saveTime = millis() + SAVEDELAY;
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
  //
  // TODO: von Zeit zu Zeit update des EEPROM
  //
}

