/*
 * config.h
 *
 * Created: 11.12.2015 17:35:58
 *  Author: dmarc
 */ 


#ifndef CONFIG_H_
#define CONFIG_H_


//
// PWM Ausgänge für LED Ansteuerung gesteuert durch Timer0 und Timer1                                                            
//
#define PWM_RED             5
#define PWM_GREEN           6
#define PWM_BLUE            9
#define PWM_WHITE           10
// RGB invertieren (bei gemeinsamer Anode muss das sein)
#define RGBINVERSE          1 
//
// Software UART Ports
//
#define RXPIN               8
#define TXPIN               2
//
// Test PINS
//
#define ONLINE_PIN          12
#define RESET_PIN           7
//
// PIN zur Signalisierung
//
#define EEPROM_PIN          13
 
//
// Übertragungsgeschwindigkeiten
//
#define BAUDRATE_USB        115200
#define DESTBAUDRATE_VAL    38400
#define DESTBAUDRATE_NUM    2




#endif /* CONFIG_H_ */