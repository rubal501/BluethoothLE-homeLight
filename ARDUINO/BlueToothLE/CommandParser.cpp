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
  // splitte das Kommando in seine Abschnitte
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
  return( kdo[0] );
}

/************************************************************************/
/* Gib aus dem Stgring den Modulnamen zurück                            */
/************************************************************************/
String CommandParser::getModuleName(String& btInputString )
{
  char *ptr;
  char buffer[btInputString.length()+2];
  char delimiter[] PROGMEM = ":";
  //
  // initialisieren und ersten Abschnitt erstellen
  //
  btInputString.toCharArray(buffer, btInputString.length()+1, 0 );
  // splitte in seine Abschnitte
  ptr = strtok( buffer, delimiter);
  // der Erste Abschnitt ist das Kommando, wenig interessant hier
  if(ptr != NULL)
  {
    // Der zweite Abschnitt sollte der _Name sein
    ptr = strtok(NULL, delimiter);
    if( ptr != NULL )
    {
      // jetzt habe ich den Namen
      return( String(ptr) );
    }
  }
  return( String() );    
}



