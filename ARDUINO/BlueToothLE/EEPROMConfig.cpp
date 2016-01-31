/* 
* EEPROMConfig.cpp
*
* Created: 13.12.2015 20:08:35
* Author: dmarc
*/

#include <Arduino.h>
#include <EEPROM.h>
#include "config.hpp"
#include "EEPROMConfig.hpp"

static String verString             = "BTLE02";
const static unsigned int verLength  = 6;
const static unsigned int CONFIG_START  = 32;

// default constructor
EEPROMConfig::EEPROMConfig()
{
} //EEPROMConfig

// default destructor
EEPROMConfig::~EEPROMConfig()
{
} //~EEPROMConfig

//
// Lade Konfiguration aus dem EEPROM
//
void EEPROMConfig::loadConfig( SoftwareSerial *dComm ) 
{
  //
  // Wenn die Version stimmt, lade Config aus dem EEPROM
  //
  if (checkVersion() == 1 )
  {  
    #ifdef DEBUG
    if( dComm !=  NULL )
    {
      dComm->println( "Version ok, lese aus EEPROM..." );
    }
    #endif
    for (unsigned int t=0; t<sizeof(storedConfig); t++)
    {
      *((char*)&storedConfig + t) = EEPROM.read(CONFIG_START + t);
    }
    wasChanged = false;    
    #ifdef DEBUG
    if( dComm !=  NULL )
    {
      dComm->println( "Version ok, lese aus EEPROM...OK" );
    }
    #endif
  }
  else
  {
    //
    // neue Version erzeugen
    //
    #ifdef DEBUG
    if( dComm !=  NULL )
    {
      dComm->println( "Version nicht ok, erzeuge KONFIG..." );
    }
    #endif
    initConfig();
    saveConfig(dComm);
    wasChanged = false;
  }
}

//
// Teste, ob es die richtige Version ist
//
int EEPROMConfig::checkVersion()
{ 
  for( unsigned int i = 0; i < verString.length() - 1; i++ )
  {
    if( EEPROM.read(CONFIG_START + i) != verString[i] )
    {
      return( 0 );
    }
  }
  return( 1 );
}

//
// Konfiguration zum ersten mal konfigurieren
//
void EEPROMConfig::initConfig(void)
{
  unsigned int len = verString.length();
  char moduleName[10] = {'B','T','L','E', 'x', 'x', 'x', 0, 0, 0 };
  
  wasChanged = true;
  //
  // den Versionsstring erzeugen und ablegen
  //
  for( unsigned int i = 0; (i < len && i < verLength) ; i++ )
  {
    storedConfig.version[i] = verString[i];
  }
  //
  // den Modulnamen erzeugen und ablegen
  //
  for( unsigned int i = 0; i < 10; i++ )
  {
    storedConfig.moduleName[i] = moduleName[i];
  }
  //
  // die restlichen Werte voreinstellen
  //
  storedConfig.red=24;  
  storedConfig.green=24;
  storedConfig.blue=24;
  storedConfig.white=128;
}  

//
// Die Konfiguration byteweise in EEPROM sichern
//
void EEPROMConfig::saveConfig( SoftwareSerial *dComm ) 
{
  #ifdef DEBUG
  if( dComm != NULL )
  {
    dComm->println( "Sichere KONFIG..." );
  }
  #endif
  for (unsigned int t=0; t<sizeof(storedConfig); t++)
  {
    EEPROM.update(CONFIG_START + t, *((char*)&storedConfig + t));
  }
  #ifdef DEBUG
  if( dComm != NULL )
  {
    dComm->println( "Sichere KONFIG...OK" );
  }
  #endif
}

//
// gespeicherter Wert für rot 
//
byte EEPROMConfig::getRed(void)
{
  return( storedConfig.red);
}

//
// gespeicherter Wert für rot
//
byte EEPROMConfig::getCalRed(void)
{
  return( storedConfig.c_red);
}

//
// gespeicherter Wert für grün
//
byte EEPROMConfig::getGreen(void)
{
  return( storedConfig.green);
}

//
// gespeicherter Wert für grün
//
byte EEPROMConfig::getCalGreen(void)
{
  return( storedConfig.c_green);
}

//
// gespeicherter Wert für blau
//
byte EEPROMConfig::getBlue(void)
{
  return( storedConfig.blue);
}

//
// gespeicherter Wert für blau
//
byte EEPROMConfig::getCalBlue(void)
{
  return( storedConfig.c_blue);
}

//
// gespeicherter Wert für weiss
//
byte EEPROMConfig::getWhite(void)
{
  return( storedConfig.white);
}

//
// gespeicherter Wert für weiss
//
byte EEPROMConfig::getCalWhite(void)
{
  return( storedConfig.c_white);
}

//
// gib den Modulnamen heraus!
//
String EEPROMConfig::getModuleName(void)
{
  return( String(storedConfig.moduleName) );  
}

//#############################################################################

//
// setzte rotwert in die Konfiguration
//
void EEPROMConfig::setRed(byte red)
{
  wasChanged = true;
  storedConfig.red = red;    
}

//
// setzte rotwert in die Konfiguration
//
void EEPROMConfig::setCalRed(byte red)
{
  wasChanged = true;
  storedConfig.c_red = red;
}

//
// setzte Grünwert in die Konfiguration
//
void EEPROMConfig::setGreen(byte green)
{
  wasChanged = true;
  storedConfig.green = green;
}

//
// setzte Grünwert in die Konfiguration
//
void EEPROMConfig::setCalGreen(byte green)
{
  wasChanged = true;
  storedConfig.c_green = green;
}

//
// setzte Blauwert in die Konfiguration
//
void EEPROMConfig::setBlue(byte blue)
{
  wasChanged = true;
  storedConfig.blue = blue;
}

//
// setzte Blauwert in die Konfiguration
//
void EEPROMConfig::setCalBlue(byte blue)
{
  wasChanged = true;
  storedConfig.c_blue = blue;
}

//
// setze weiiswert in die Konfiguration
//
void EEPROMConfig::setWhite(byte white)
{
  wasChanged = true;
  storedConfig.white = white;
}

//
// setze weiiswert in die Konfiguration
//
void EEPROMConfig::setCalWhite(byte white)
{
  wasChanged = true;
  storedConfig.c_white = white;
}

//
// setze den Modulnamen in die Konfiguration
//
void EEPROMConfig::setModuleName(String& mName)
{
  wasChanged = true;
  
  for( unsigned int i = 0; i < 10; i++ )
  {
    if( i < mName.length() )
    {
      storedConfig.moduleName[i] = mName[i];
    }
    else
    {
      storedConfig.moduleName[i] = 0;
    }
  }
}




