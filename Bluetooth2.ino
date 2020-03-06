//#include <SoftwareSerial.h>
#include "FastLED.h"
#include <SoftwareSerial.h>

#define LED_PIN 6
#define NUM_LEDS 10
#define LED_OFFSET 3
#define REAL_LEDS_NUM NUM_LEDS*LED_OFFSET
#define LED_TYPE WS2812B
#define LED_OFF 0x000000
#define INDEX_SEPARATOR 0

SoftwareSerial hc06(2,3);

bool new_pattern = false;
bool isDetecting = false;
bool isLedOn = false;

int reconocimientoCount;

CRGB leds[REAL_LEDS_NUM];

void checkInput();
void readPattern();
void turnOffLeds();
void patronReconocimineto();
void setColorRGB(int index,CRGB color);
void setColorHSV(int index,CHSV color);

void setup(){

  hc06.begin(9600);
  Serial.begin(9600);
  FastLED.addLeds<LED_TYPE,LED_PIN,GRB>(leds,REAL_LEDS_NUM);
}

void loop(){

  checkInput();
  if(new_pattern){
    FastLED.show();
    new_pattern = false;
  }
  if(isDetecting){
    patronReconocimineto();
  }
  /*if(isLedOn == false && leds_on == true){
    FastLED.show();
    isLedOn = true;
  }else if(isLedOn == true && leds_on == false){
    FastLED.show();
    isLedOn = false;
  }*/

  /*for (int i = 0; i < NUM_LEDS; i++) {
    checkInput();
    if(!leds_on)
      break;

    uint8_t hue = map(i, 0, NUM_LEDS, 0, 255);
    leds[i] = CHSV(hue, 255, 255);
    FastLED.show();
    leds[i] = LED_OFF; 
    
    checkInput();
    if(!leds_on)
      break;
    
  }
  FastLED.show(); */
}

void setColorRGB(int index,CRGB color){
  leds[index*LED_OFFSET] = color;
}

void setColorHSV(int index,CHSV color){
  leds[index*LED_OFFSET] = color;
}


void checkInput(){
  char buffer = 'q';
  while(hc06.available()){
    buffer = hc06.read();
    delay(10);
    Serial.println(buffer);
    if(buffer == 'Y'){
      //leds_on = true;
    }else if(buffer == 'N'){
      //leds_on = false;
    }else if(buffer == 'D'){
      if(hc06.available()){
        buffer = hc06.read();
        if(buffer == 'N'){
          reconocimientoCount++;
          isDetecting = true;
        }else if(buffer == 'S'){
          isDetecting = false;
          turnOffLeds();
        }
      }else{
        isDetecting = true;
        reconocimientoCount = 0;
      }
    }else if(buffer == 'P'){
      readPattern();
      hc06.write(buffer);
    }else{
      return;
    }
  }
}

void turnOffLeds(){
  for(int i=0; i < NUM_LEDS; i++){
    setColorRGB(i,LED_OFF);
  }
  FastLED.show();
}

/*  
  El patron que se debe enviar debe comenzar con una 'P' seguido de 
el indice del led que se establecera el color enviado.
  Si se envia el valor '0'despues del indice se debe pasar el indice 
final el cual se establecera el color a todos los led dentro del rango.
  Luego de los indices se deba pasar la saturacion, el HUE, y el valor,
como no se utiliza el '0' para determinar un rango de Leds, el valor de 
la saturacion es de 1 a 255.
  Patrones ejemplo:
P 0 255 0 255 1 255 150 255 = P index saturacion hue value index saturacion hue value
P 0 0 10 255 0 255 11 0 15 255 100 255 = P index separador endIndex saturacion hue value index separador endIndex saturacion hue value

  Si llega un indice mayor a la cantidad de led establecida se terminara el proceso de lectura y se vaciará el buffer.
*/
void readPattern(){
  int index = 0;
  byte buffer = 'q';
  //Se tienene que reiniciar cuando se lee toda la informacion de un led.
  int endIndex = 0;
  int count = 0;
  bool hasMoreIndex = false;
  bool isOneReadComplete = false;
  bool edited = false;
  // fin del bloque.
  int h,s,v;
  while(hc06.available()){
    buffer = hc06.read();    
    Serial.println(buffer);

    if(count == 0){
      index = buffer;
      if(index > NUM_LEDS)
        break;
      count++;
    }else if(buffer == INDEX_SEPARATOR && count == 1){
      hasMoreIndex = true;
    }else if(hasMoreIndex){
      endIndex = buffer;
      if(endIndex >= NUM_LEDS)
        break;
      hasMoreIndex = false;
    }else{
      if(count == 1)
        s = buffer;
      else if(count == 2)
        h = buffer;
      else{
        v = buffer;
        isOneReadComplete = true;
        if(endIndex == 0)
          endIndex = index;
      }
      count++;
    }  

    if(isOneReadComplete){
      for(int i = index; i <= endIndex; i++){
          setColorHSV(i,CHSV(h,s,v));
      }
      
      endIndex = 0;
      count = 0;
      hasMoreIndex = false;
      isOneReadComplete = false;
      edited = true;
    }  
  }
  if(edited)
    new_pattern = true;
}

void patronReconocimineto(){
  int aux;
  int rest;
  for(int i=0; i < NUM_LEDS; i++){
    aux = i;
    for(int j = 0; j < reconocimientoCount; j++){
      aux = aux/2;
    }
    rest = aux%2;
    if(rest == 1)
      setColorRGB(i,CRGB(0,255,0));
    else
      setColorRGB(i,CRGB(255,0,0));
  }
  FastLED.show();
  isDetecting = false;
}