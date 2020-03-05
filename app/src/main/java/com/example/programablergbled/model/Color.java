package com.example.programablergbled.model;

public class Color {
    private byte rgb[];

    /* Aclaraciones
    - Se establece 253 como maximo para usar el 255 como separador de colores de led y 254
    como separador de indices de los leds en el envio de los datos.
    */
    private int MAX_VALUE = 253;

    public Color(int red, int green, int blue){
        rgb = new byte[3];
        this.rgb[0] = evaluateLimits(red);
        this.rgb[1] = evaluateLimits(green);
        this.rgb[2] = evaluateLimits(blue);
    }

    public Color(android.graphics.Color color){
        this.rgb[0] = evaluateLimits(color.red());
        this.rgb[1] = evaluateLimits(color.green());
        this.rgb[2] = evaluateLimits(color.blue());
    }

    private byte evaluateLimits(int color){
        if(color > MAX_VALUE)
            return (byte) MAX_VALUE;
        if(color < 0)
            return 0;
        return (byte)color;
    }

    private byte evaluateLimits(float color){
        int intColor = Math.round(color);
        if(intColor > MAX_VALUE)
            return (byte) MAX_VALUE;
        if(intColor < 0)
            return 0;
        return (byte) intColor;
    }

    public String getHex(){
        String hex = "0x";
        hex += Integer.toHexString(rgb[0]);
        hex += Integer.toHexString(rgb[1]);
        hex += Integer.toHexString(rgb[2]);
        return hex;
    }

    public boolean equal(Color color){
        if(color == null)
            return false;

        byte rgb2[] = color.getRGB();
        if(rgb[0] != rgb2[0])
            return false;
        if(rgb[1] != rgb2[1])
            return false;
        if(rgb[2] != rgb2[2])
            return false;
        return true;
    }

    public byte[] getRGB(){
        return rgb.clone();
    }
}
