/* 
* EEPROMConfig.h
*
* Created: 13.12.2015 20:08:36
* Author: dmarc
*/


#ifndef __EEPROMCONFIG_H__
#define __EEPROMCONFIG_H__

#include <Arduino.h>
#include <EEPROM.h>
//#include <avr/pgmspace.h>
#include "config.hpp"


struct StoreStruct 
{
  //
  // Hier werden die Starteinstellungen des 
  // Programmes gesichert
  //
  char version[6];
  char moduleName[10];
  //
  // Die Helligkeitswerte
  // der LED's
  //
  byte red,green,blue,white;
  //
  // die angezeigten Helligkeitswerte, ggf kalibriert
  //
  byte c_red,c_green,c_blue,c_white;
};

class EEPROMConfig
{
//variables
public:
protected:
private:
  boolean wasChanged = false;
  StoreStruct storedConfig;
   
//functions
public:
	EEPROMConfig();
	~EEPROMConfig();
  void loadConfig(void);
  void saveConfig(void);
  //
  // Getter für Konfig
  //
  byte getRed(void);
  byte getGreen(void);
  byte getBlue(void);
  byte getWhite(void);
  byte getCalRed(void);
  byte getCalGreen(void);
  byte getCalBlue(void);
  byte getCalWhite(void);
  String getModuleName(void);
  //
  // Setter für Konfig
  //
  void setRed(byte red);
  void setGreen(byte green);
  void setBlue(byte blue);
  void setWhite(byte white);
  void setCalRed(byte red);
  void setCalGreen(byte green);
  void setCalBlue(byte blue);
  void setCalWhite(byte white);
  void setModuleName(String& mName);
  
protected:
private:
	EEPROMConfig( const EEPROMConfig &c );
	EEPROMConfig& operator=( const EEPROMConfig &c );
  int checkVersion(void);
  void initConfig(void);
  
}; //EEPROMConfig

#endif //__EEPROMCONFIG_H__
