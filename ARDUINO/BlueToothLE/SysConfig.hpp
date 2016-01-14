/* 
* SysConfig.h
*
* Created: 23.12.2015 17:13:50
* Author: dmarc
*/

#include <Arduino.h>
#include "SoftwareSerial.hpp"


#ifndef __SYSCONFIG_H__
#define __SYSCONFIG_H__


class SysConfig
{
//variables
public:
protected:
private:

//functions
public:
	SysConfig();
	~SysConfig();
  //
  // die Sachen, die schnell gehen sollen...
  //
  static void SystemPreInit( EEPROMConfig& cfg  );
//
  // Die gesamte Systeminitialisierung ausgelagert aus Main.cpp
  //
  static void SystemInit( SoftwareSerial& comm, Communication& myComm, EEPROMConfig& cfg  );
  //
  // Setzte den Modulnamen
  //
  static void setModuleName( SoftwareSerial& mySerial, Communication& myComm, EEPROMConfig& theConfig, String name );
  //
  // setze die PWM Frequenzen nach meinen Bedürfnissen
  //
  static void setPwmFrequency(int pin, int divisor);
  //
  // Das Modul durch POWER OFF PAUSE ON zurücksetzen zur Übernahme
  //
  static void restartBTModul( SoftwareSerial& mySerial, Communication& myComm ); 
protected:
private:
	SysConfig( const SysConfig &c );
	SysConfig& operator=( const SysConfig &c );

}; //SysConfig

#endif //__SYSCONFIG_H__
