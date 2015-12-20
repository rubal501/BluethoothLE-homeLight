/*
* CommandParser.h
*
* Created: 08.12.2015 21:14:00
* Author: dmarc
*
* Die Kommunitaktion erfolg nach dem Uraltmodell
*     bin�r(0x02)...DATEN...bin�r(0x03)
* Die Daten sind ASCII codiert �bertragen
* Kommando ist "XX:" das Kommando ist eine HEX codierte Zahl, also 255 Kommandos m�glich
* daraus folgt:
* - erstes und letzes Zeichen einer Sequenz sind 1 Byte (0x02 oder 0x03)
* - Das Kommando sind IMMER 2 Zeichen
* Nach dem ":" kommen Parameter, diese sind durch ":" getrennt
*
* Antworen sind genauso codiert
*/
#include <Arduino.h>
#include "config.hpp"

#ifndef __KOMMANDINTERPRETER_H__
#define __KOMMANDINTERPRETER_H__

//
// Kommandos, welche der Parser bearbeitet
//
#define C_UNKNOWN       0xff                          // unbekanntes Kommando
#define C_ASKTYP        0x00                          // Frage nach TYP des Moduls
#define C_ASKNAME       0x01                          // Frage nach dem Modulnamen
#define C_ASKRGB        0x02                          // Frage nach RGBW im Modul
#define C_SETCOLOR      0x03                          // Vom Meister Farbe


class CommandParser
{
//variables
public:
protected:
private:

//functions
public:
	CommandParser();
	~CommandParser();
  static byte parseCommand( String& kdoString, byte* kdo  );      // Parse das Kommando

protected:
private:
	CommandParser( const CommandParser &c );
	CommandParser& operator=( const CommandParser &c );

}; //CommandParser

#endif //__KOMMANDINTERPRETER_H__
