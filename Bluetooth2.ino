//#include <SoftwareSerial.h>
#include "FastLED.h"
#include <SoftwareSerial.h>

#define LED_PIN 6
#define NUM_LEDS 10
#define LED_OFFSET 3
#define REAL_LEDS_NUM NUM_LEDS*LED_OFFSET
#define LED_TYPE WS2812B
#define LED_OFF 0x000000

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
void setColor(int index,CRGB color);

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

void setColor(int index,CRGB color){
  leds[index*LED_OFFSET] = color;
}


void checkInput(){
  char buffer = 'q';
  while(hc06.available()){
    buffer = hc06.read();
    delay(10);
    Serial.write(buffer);
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
    setColor(i,LED_OFF);
  }
  FastLED.show();
}

void readPattern(){
  int index = 0;
  byte buffer = 'q';
  //Se tienene que reiniciar cuando se lee toda la informacion de un led.
  int sum = 0;
  int count = 0;
  bool isSum = false;
  bool isOneReadComplete = false;
  // fin del bloque.
  int r,g,b;
  while(hc06.available()){
    buffer = hc06.read();
    if(buffer == 255)
      continue;
    if(buffer == 80){
      hc06.flush();
      break;
    }
    
    Serial.println(buffer);

    if(count == 0){
      index = buffer;
      count++;
    }else if(buffer == 254){
      isSum = true;
    }else if(isSum){
      sum = buffer;
      isSum = false;
    }else{
      if(count == 1)
        r = buffer;
      else if(count == 2)
        g = buffer;
      else{
        b = buffer;
        isOneReadComplete = true;
      }
      count++;
    }  

    if(isOneReadComplete){
      sum = index + sum + 1;
      String variables = "variables: ";
      String msg = variables + "sum: " + sum + ", " + "index: " + index;
      Serial.println(msg);

      
      
      for(int i = index; i < sum; i++){
        String led = "led ";
        String s = led + i +": " + r + ", " + g + ", " + b;
        Serial.println(s);
        setColor(i,CRGB(r,g,b));
      }
      sum = 0;
      count = 0;
      isSum = false;
      isOneReadComplete = false;
    }  
  }
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
      setColor(i,CRGB(0,50,0));
    else
      setColor(i,CRGB(50,0,0));
  }
  FastLED.show();
  isDetecting = false;
}