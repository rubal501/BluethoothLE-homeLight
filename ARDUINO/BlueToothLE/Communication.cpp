/* 
* Communication.cpp
*
* Created: 13.12.2015 18:34:24
* Author: dmarc
*/


#include "Communication.hpp"
#include "CommandParser.hpp"

// default constructor
Communication::Communication()
{
  wasSTXrec = false;
} //Communication

// default destructor
Communication::~Communication()
{
} //~Communication

//#############################################################################
// Kommando senden und das Echo ausgeben
// f�r Debugging der BT Schnittstelle notwendig,kann sp�ter entfallen
//#############################################################################
String Communication::sendCommand( HardwareSerial& comm, SoftwareSerial *dComm, String cmd, bool echo )
{
  unsigned long waitfor;
  String retval;
  
  #ifdef DEBUG
  if( dComm != NULL)
  {
    dComm->print("Sende <");
    dComm->print( cmd );
    dComm->println(">...");
  }
  #endif
  delay(25);
  comm.print(cmd);
  comm.flush();
  delay(35);
  #ifdef DEBUG
  if( dComm != NULL )
  {
    dComm->println("Sende CMD...OK");
  }
  #endif  
  waitfor = millis() + 500L;
  if( echo )
  {
    retval = "";
  }
  while( comm.available() || waitfor > millis() )
  {
    if( comm.available() )
    {
      if( echo )
      {
        int ch = comm.read();
        retval += char(ch);
        #ifdef DEBUG
        if( dComm != NULL )
        {
          dComm->write( ch );
        }
        #endif        
      }
      else
      {
        #ifdef DEBUG
        if( dComm != NULL )
        {
          dComm->write(comm.read());
        }
        #else
        comm.read();
        #endif
      }
    }
    else
    {
      delay(20);
    }
  }
  #ifdef DEBUG
  if( dComm != NULL )
  {
    dComm->println("");
  }
  #endif
  if( echo )
  {
    return( retval );
  }
  return( "-" );
}

//#############################################################################
// Finde die Kommunikationsgeschwindigkeit des Moduls heraus
//#############################################################################
long Communication::findCommSpeed( HardwareSerial& comm, SoftwareSerial *dComm )
{
  String testString;
  //
  // TESTE BASIC: 9600 Baud
  //
  comm.flush();
  comm.begin(9600L);
  #ifdef DEBUG
  if( dComm != NULL )
  {
    dComm->println("Teste 9600 baud...");
  }
  #endif
  testString = sendCommand( comm, dComm, "AT", true );
  #ifdef DEBUG
  if( dComm != NULL )
  {
    dComm->print("Ergebnis: <");
    dComm->print( testString );
    dComm->println(">");
  }
  #endif
  if( testString == "OK" )
  {
    // gefundene Geschwindigkeit.
    #ifdef DEBUG
    if( dComm != NULL )
    {
      dComm->println("9600 baud gefunden...");
    }
    #endif
    return( 9600L );
  }
  //
  // Teste auf 19200 Baud
  //
  comm.begin(19200L);
  #ifdef DEBUG
  if( dComm != NULL )
  {
    dComm->println("Teste 19200 baud...");
  }
  #endif
  testString = sendCommand( comm, dComm, "AT", true );
  #ifdef DEBUG
  if( dComm != NULL )
  {
    dComm->print("Ergebnis: <");
    dComm->print( testString );
    dComm->println(">");
  }
  #endif
  if( testString == "OK" )
  {
    // gefundene Geschwindigkeit.
    #ifdef DEBUG
    if( dComm != NULL )
    {
      dComm->println("19200 baud gefunden...");
    }
    #endif
    return( 19200L );
  }
  
  //
  // Teste auf 38400 Baud
  //
  comm.begin(38400L);
  #ifdef DEBUG
  if( dComm != NULL )
  {
    dComm->println("Teste 38400 baud...");
  }
  #endif
  testString = sendCommand( comm, dComm, "AT", true );
  #ifdef DEBUG
  if( dComm != NULL )
  {
    dComm->print("Ergebnis: <");
    dComm->print( testString );
    dComm->println(">");
  }
  #endif
  if( testString == "OK" )
  {
    // gefundene Geschwindigkeit.
    #ifdef DEBUG
    if( dComm != NULL )
    {
      dComm->println("38400 baud gefunden...");
    }
    #endif
    return( 38400L );
  }

  //
  // Test auf 57600 Baud
  //
  comm.begin(57600L);
  #ifdef DEBUG
  if( dComm != NULL )
  {
    dComm->println("Teste 58600 baud...");
  }
  #endif
  testString = sendCommand( comm, dComm, "AT", true );
  #ifdef DEBUG
  if( dComm != NULL )
  {
    dComm->print("Ergebnis: <");
    dComm->print( testString );
    dComm->println(">");
  }
  #endif
  if( testString == "OK" )
  {
    // gefundene Geschwindigkeit.
    #ifdef DEBUG
    if( dComm != NULL )
    {
      dComm->println("57600 baud gefunden...");
    }
    #endif
    return( 57600L );
  }
  
  return( 0L );
}

//#############################################################################
// Lese Zeichen aus der Empfangsschnittstelle...
// return 1 => Zeilenende (ETX)gefunden
// return 0 => Nix unternehmen
//#############################################################################
byte Communication::readMessageIfAvavible( HardwareSerial& comm, SoftwareSerial *dComm, String& btKdoStr )
{  
  int inChar;
   
  //
  // Solange hier aten rein purzeln
  // 
  while( comm.available() )
  {
    // Das _Zeichen lesen
    inChar = comm.read() & 0x00ff;
    #ifdef DEBUG
    if( dComm != NULL )
    {
      dComm->write(inChar);
    }
    #endif
    // wenn die Zeichenkette zu lang ist:
    if( btKdoStr.length() > 24 )
    {
      // k�rze auf 0, und alles auf Anfang
      btKdoStr = "";
      wasSTXrec = false;
    }
    else
    {
      // Ok, es ist noch Platz hier
      //
      // Der Anfang einer Sequenz vom MASTER?
      //
      if( inChar == Communication::STX )
      {
        // Kommando ist am Anfang!
        btKdoStr = "";
        wasSTXrec = true;
        continue;
      }
      //
      // Das Ende einer Sequenz vom Master?
      //
      if( inChar == Communication::ETX )
      {
        // War das Startzeichen anwesend?
        if( wasSTXrec )
        {
          // Ja, das Kommando sollte g�ltig sein
          return( 1 );
        }
        else
        {
          // Ende ohne Start ist falsch....
          // Alles auf Anfang
          btKdoStr = "";
          continue;
        }
      }
      else
      {
        // Das Zeichen in den String
        // evtl nur wenn STX da war?
        btKdoStr += (char)inChar;
      }
    }  
  }
  //
  // immer return 0 ausser es gibt STX und ETX
  //
  return( 0 );
}

//#############################################################################
// Ist das Modul verbunden? (Test �ber das Statuspin ONLINE_PIN)
//#############################################################################
boolean Communication::isModulConnected(void)
{
  unsigned long saveTime = millis() + 750L;
  
  while( saveTime > millis() )
  {
    if( digitalRead( ONLINE_PIN ) )
    {
      return( true );
    }
    delay(30);
  }
  return( false );
}

//#############################################################################
// Sende das Zeug an den Meister
//#############################################################################
void Communication::sendToMaster( HardwareSerial& comm, String& toSend )
{
  comm.write( Communication::STX );
  comm.print( toSend );
  comm.write( Communication::ETX );
  comm.flush();   
}

//#############################################################################
// Wandle Nibbles in Hex
//#############################################################################
char Communication::nibbleToHex(byte ch)
{
  return("0123456789ABCDEF"[0x0F & (unsigned char)ch]);
}

//#############################################################################
//  Sende aktuelle Einstellungen des Moduls RGBW
//#############################################################################
void Communication::sendRGBW( HardwareSerial& comm, byte* rgbw )
{
  String cmd;
  
  cmd += nibbleToHex( C_ASKRGBW >> 4 );
  cmd += nibbleToHex( C_ASKRGBW );
  for( int i=0; i<4; i++ )
  {
    cmd += ':';
    cmd += nibbleToHex( *rgbw >> 4 );
    cmd += nibbleToHex( *rgbw );
    rgbw++;
  }
  sendToMaster( comm, cmd );
}


//#############################################################################
// Sende den Modulnamen 
//#############################################################################
void Communication::sendModuleName( HardwareSerial& comm, String& mName )
{
  String cmd;
  cmd += nibbleToHex( C_ASKNAME >> 4);
  cmd += nibbleToHex(C_ASKNAME );
  cmd += ":";
  cmd += mName;
  sendToMaster( comm, cmd );
}  

//#############################################################################
// Sende den Modulnamen
//#############################################################################
void Communication::sendModuleType( HardwareSerial& comm, const String& moduleType )
{
  String cmd;
  cmd += nibbleToHex( C_ASKTYP >> 4);
  cmd += nibbleToHex(C_ASKTYP );
  cmd += ":";
  cmd += moduleType ;
  sendToMaster( comm, cmd );
}
