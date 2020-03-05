package com.example.programablergbled.Utils;

import java.util.ArrayList;

public class Parser {

    public static byte[] arrayListToByteArray(ArrayList<Byte> list){
        int listSize = list.size();
        if(listSize == 0)
            return null;

        byte out[] = new byte[list.size()];
        for(int i=0; i < listSize; i++)
            out[i] = list.get(i).byteValue();
        return out;
    }

    public static String getStringCharProtocol(byte b){
        if(b == (byte)255)
            return "-";

        if(b == (byte)254)
            return "+";

        return Integer.toString(b & 0xff);
    }

    public int parseRange(int value, int min, int max, int newMin, int newMax){
        return newMin+((value-min)*(newMax-newMin))/(max-min);
    }
}
