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
  static void SystemPreInit( EEPROMConfig& cfg, SoftwareSerial* dComm );
//
  // Die gesamte Systeminitialisierung ausgelagert aus Main.cpp
  //
  static void SystemInit( HardwareSerial& comm, SoftwareSerial* dComm, Communication& myComm, EEPROMConfig& cfg  );
  //
  // Setzte den Modulnamen
  //
  static void setModuleName( HardwareSerial& comm, SoftwareSerial* dComm, Communication& myComm, EEPROMConfig& theConfig, String name );
  //
  // setze die PWM Frequenzen nach meinen Bedürfnissen
  //
  static void setPwmFrequency(int pin, int divisor);
  //
  // Das Modul durch POWER OFF PAUSE ON zurücksetzen zur Übernahme
  //
  static void restartBTModul( HardwareSerial comm, SoftwareSerial* dComm, Communication& myComm ); 
protected:
private:
	SysConfig( const SysConfig &c );
	SysConfig& operator=( const SysConfig &c );

}; //SysConfig

#endif //__SYSCONFIG_H__
