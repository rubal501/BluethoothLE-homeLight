/*
* CommandParser.cpp
*
* Created: 08.12.2015 21:14:00
* Author: dmarc
*/

#include "CommandParser.hpp"


// default constructor
CommandParser::CommandParser()
{
} //CommandParser

// default destructor
CommandParser::~CommandParser()
{
} //~CommandParser


/************************************************************************/
/* Checke das Kommando, gib das Kommando als byte zurück                */
/************************************************************************/
byte CommandParser::parseCommand( String& kdoString, byte* kdo )
{
  char *ptr;
  char buffer[kdoString.length()+2];
  char delimiter[] PROGMEM = ":";
  byte i=0;

  //
  // Kommando isolieren
  //
  // initialisieren und ersten Abschnitt erstellen
  kdoString.toCharArray(buffer, kdoString.length()+1, 0 );
  // splitte das Kommando in seien Abschnitte
  ptr = strtok( buffer, delimiter);

  while(ptr != NULL)
  {
    // String zu zahl konvertieren
    byte zahl = strtoul(ptr, NULL, 16);
    // und den Parameter in das Byte-Array
    kdo[i++] = zahl;
    // solange das in das Array passt
    if( i > 9 )
    {
      break;
    }
    // naechsten Abschnitt erstellen
    ptr = strtok(NULL, delimiter);
  }
  kdoString = "";
  return( kdo[0] );
}





