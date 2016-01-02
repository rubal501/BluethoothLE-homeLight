/*
* CommandParser.h
*
* Created: 08.12.2015 21:14:00
* Author: dmarc
*
* Die Kommunitaktion erfolg nach dem Uraltmodell
*     binär(0x02)...DATEN...binär(0x03)
* Die Daten sind ASCII codiert übertragen
* Kommando ist "XX:" das Kommando ist eine HEX codierte Zahl, also 255 Kommandos möglich
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
#define C_ASKRGBW       0x02                          // Frage nach RGBW im Modul
#define C_ASKCALRGBW    0x03                          // Frage nach RGBW im Modul, kalibriert
#define C_SETCOLOR      0x04                          // Vom Meister Farbe, unkalibriert, direkte Anzeige mit Speichern
#define C_SETCALRGB     0x05                          // Vom Meister, setze RGB Modul konvertiert zu RGBW
#define C_ONOFF         0xfe                          // AUS, oder wenn alles auf 0 ist EIN


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
