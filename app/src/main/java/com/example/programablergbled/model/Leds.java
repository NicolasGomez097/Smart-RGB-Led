package com.example.programablergbled.model;


import androidx.annotation.Size;

import com.example.programablergbled.Utils.Parser;

import java.util.ArrayList;

public class Leds {

    private byte LEDS_INDEX_SEPARATOR = (byte)0;

    private HSVInteger leds[];
    private int ledsNum;

    public Leds(int ledsNum){
        this.ledsNum = ledsNum;
        leds = new HSVInteger[ledsNum];
    }

    public boolean setColor(int numLed,@Size(3) byte hsv[]){
        if(hsv.length != 3)
            return false;
        if(numLed >= ledsNum)
            return false;
        leds[numLed] = new HSVInteger(hsv[0],hsv[1],hsv[2]);
        return true;
    }

    public byte[] turnOff(){
        ArrayList<Byte> aux = new ArrayList<>();
        byte[] out;
        aux.add((byte)'P');
        aux.add(Integer.valueOf(0).byteValue());
        aux.add(LEDS_INDEX_SEPARATOR);
        aux.add(Integer.valueOf(ledsNum-1).byteValue());
        aux.add(Integer.valueOf(1).byteValue());
        aux.add(Integer.valueOf(0).byteValue());
        aux.add(Integer.valueOf(0).byteValue());
        out = Parser.arrayListToByteArray(aux);
        return out;
    }

    public byte[] getBytes(){
        ArrayList<Byte> aux = new ArrayList<>();
        byte[] out;
        Integer colorCount = 0;
        HSVInteger lastColor = null;

        for(int i=0; i < ledsNum; i++){
            if(leds[i] == null && colorCount == 0)
                continue;

            if(colorCount == 0){
                lastColor = leds[i];
                colorCount++;
                continue;
            }

            if(lastColor.equal(leds[i])){
                colorCount++;
                continue;
            }else{
                if(colorCount == 1)
                    addLedsToArray(aux,lastColor,i-colorCount);
                else
                    addLedsToArray(aux,lastColor,i-colorCount,i-1);

                if(leds[i] == null)
                    colorCount = 0;
                else
                    colorCount = 1;

                lastColor = leds[i];
            }
        }
        if(colorCount != 0){
            addLedsToArray(aux,lastColor,ledsNum,colorCount);
        }

        if(aux.size() == 0)
            return null;
        aux.add(0,(byte)'P');
        out = Parser.arrayListToByteArray(aux);
        return out;
    }

    private void addLedsToArray(ArrayList<Byte> list, HSVInteger color, Integer startIndex, Integer endIndex){
        list.add(Integer.valueOf(startIndex).byteValue());
        if(endIndex > 1){
            list.add(LEDS_INDEX_SEPARATOR);
            list.add(Integer.valueOf(endIndex).byteValue());
        }
        byte hsv[] = color.getHSV();
        list.add(hsv[1]);
        list.add(hsv[0]);
        list.add(hsv[2]);
    }

    private void addLedsToArray(ArrayList<Byte> list, HSVInteger color, Integer startIndex){
        list.add(Integer.valueOf(startIndex).byteValue());
        byte hsv[] = color.getHSV();
        list.add(hsv[1]);
        list.add(hsv[0]);
        list.add(hsv[2]);
    }



}
