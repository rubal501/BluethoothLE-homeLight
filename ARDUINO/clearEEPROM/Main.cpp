#include <Arduino.h>
#include <EEPROM.h>


void setup();
void loop();


void setup()
{
  unsigned long val,len;
  Serial.begin(115200);
  while (!Serial)
  {
    ; // wait for serial port to connect. Needed for native USB port only
  }
  Serial.println("warte...");
  delay(4000);
  Serial.println("Leere den Flash...");
  len = EEPROM.length();
  for (unsigned long i = 0 ; i < len ; i++) 
  {
    val = (100L*i) / len;
    Serial.print("Fortschritt: ");
    Serial.print( val, DEC );
    Serial.println(".");
    EEPROM.write(i, 0);
  }
  Serial.println("Leere den Flash...OK!");

}

void loop()
{
  delay(1000);
	/*
	  Endlosschleife
	*/

  
}
