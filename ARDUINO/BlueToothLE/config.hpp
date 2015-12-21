/*
 * config.h
 *
 * Created: 11.12.2015 17:35:58
 *  Author: dmarc
 */ 


#ifndef CONFIG_H_
#define CONFIG_H_


//
// PWM Ausgänge für LED Ansteuerung                                                             
//
#define PWM_RED             5
#define PWM_GREEN           6
#define PWM_BLUE            10
#define PWM_WHITE           11
//
// Software UART Ports
//
#define RXPIN               8
#define TXPIN               2
#define ONLINE_PIN          12
#define RESET_PIN           7
 
//
// Übertragungsgeschwindigkeiten
//
#define BAUDRATE_USB        115200
#define DESTBAUDRATE_VAL    38400
#define DESTBAUDRATE_NUM    2




#endif /* CONFIG_H_ */