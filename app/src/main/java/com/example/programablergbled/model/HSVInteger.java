package com.example.programablergbled.model;

import androidx.annotation.Size;

public class HSVInteger {
    private byte hsv[];

    /* Aclaraciones
    - Se establece 253 como maximo para usar el 255 como separador de colores de led y 254
    como separador de indices de los leds en el envio de los datos.
    */
    private int MAX_VALUE = 253;

    public HSVInteger(byte h, byte s, byte v){
        this.hsv = new byte[3];
        this.hsv[0] = h;
        this.hsv[1] = s;
        this.hsv[2] = v;
    }

    public HSVInteger(@Size(3) byte hsv[]){
        if(hsv.length != 3)
            return;
        this.hsv = hsv;
    }

    public boolean equal(HSVInteger color){
        if(color == null)
            return false;

        byte hsv2[] = color.getHSV();
        if(hsv[0] != hsv2[0] ||
            hsv[1] != hsv2[1] ||
            hsv[2] != hsv2[2])
            return false;
        return true;
    }

    public byte[] getHSV(){
        return hsv.clone();
    }
}
