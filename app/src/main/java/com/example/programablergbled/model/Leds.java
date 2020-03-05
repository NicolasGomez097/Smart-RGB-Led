package com.example.programablergbled.model;


import com.example.programablergbled.Utils.Parser;

import java.util.ArrayList;

public class Leds {

    private byte LEDS_SEPARATOR = (byte)255;
    private byte LEDS_SUM_SEPARATOR = (byte)254;

    private Color leds[];
    private int ledsNum;

    public Leds(int ledsNum){
        this.ledsNum = ledsNum;
        leds = new Color[ledsNum];
    }

    public boolean setColor(int numLed, int rgb[]){
        if(rgb.length != 3)
            return false;
        leds[numLed] = new Color(rgb[0],rgb[1],rgb[2]);
        return true;
    }

    public byte[] turnOff(){
        ArrayList<Byte> aux = new ArrayList<>();
        byte[] out;
        aux.add((byte)'P');
        aux.add(Integer.valueOf(0).byteValue());
        aux.add(LEDS_SUM_SEPARATOR);
        aux.add(Integer.valueOf(ledsNum-1).byteValue());
        aux.add(Integer.valueOf(0).byteValue());
        aux.add(Integer.valueOf(0).byteValue());
        aux.add(Integer.valueOf(0).byteValue());
        out = Parser.arrayListToByteArray(aux);
        return out;
    }

    public byte[] getBytes(){
        ArrayList<Byte> aux = new ArrayList<>();
        byte[] out;
        Integer sameColors = 0;
        Color lastColor = null;

        for(int i=0; i < ledsNum; i++){
            if(leds[i] == null && sameColors == 0)
                continue;

            if(sameColors == 0){
                lastColor = leds[i];
            }

            if(lastColor.equal(leds[i])){
                sameColors++;
                continue;
            }else{
                addLedsToArray(aux,lastColor,i,sameColors);

                if(leds[i] == null)
                    sameColors = 0;
                else
                    sameColors = 1;
                lastColor = leds[i];
            }
        }
        if(sameColors != 0){
            addLedsToArray(aux,lastColor,ledsNum,sameColors);
        }

        if(aux.get(aux.size()-1) == (byte)255)
            aux.remove(aux.size()-1);

        if(aux.size() == 0)
            return null;
        aux.add(0,(byte)'P');
        out = Parser.arrayListToByteArray(aux);
        return out;
    }

    private void addLedsToArray(ArrayList<Byte> list,Color color, Integer pos,Integer count){
        list.add(Integer.valueOf(pos-count).byteValue());
        if(count > 1){
            list.add(LEDS_SUM_SEPARATOR);
            list.add(Integer.valueOf(count-1).byteValue());
        }
        byte rgb[] = color.getRGB();
        list.add(rgb[0]);
        list.add(rgb[1]);
        list.add(rgb[2]);
        list.add(LEDS_SEPARATOR);
    }



}
