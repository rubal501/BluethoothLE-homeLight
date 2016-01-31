/* 
* Communication.h
*
* Created: 13.12.2015 18:34:25
* Author: dmarc
*/


#ifndef __COMMUNICATION_H__
#define __COMMUNICATION_H__

#include <Arduino.h>
#include "SoftwareSerial.hpp"
#include "config.hpp"

class Communication
{
//variables
public:
protected:
private:
  boolean wasSTXrec = false;
  static const int STX = 0x02;
  static const int ETX = 0x03;
  static char nibbleToHex(byte ch);  
  void sendToMaster( HardwareSerial& comm, String& toSend );

//functions
public:
	Communication();
	~Communication();
  String sendCommand( HardwareSerial& comm, SoftwareSerial *dComm, String cmd, bool echo = false );
  long findCommSpeed( HardwareSerial& comm, SoftwareSerial *dComm );
  byte readMessageIfAvavible( HardwareSerial& comm, SoftwareSerial *dComm, String& btKdoStr  );
  boolean isModulConnected(void);
  void sendRGBW( HardwareSerial& comm, byte* rgbw );
  void sendModuleName( HardwareSerial& comm, String& mName );
  void sendModuleType( HardwareSerial& comm, const String& moduleType );
protected:
private:
	Communication( const Communication &c );
	Communication& operator=( const Communication &c );

}; //Communication

#endif //__COMMUNICATION_H__
