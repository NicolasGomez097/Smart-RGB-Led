#pragma version(1)
#pragma rs java_package_name(com.example.myapplication)
#pragma rs_fp_relaxed

rs_allocation input;
rs_allocation output;
uint32_t width;
uint32_t height;

uint32_t RS_KERNEL getUVIndex(uint32_t i);

uchar RS_KERNEL yuv_to_rgb(uchar in, uint32_t x){
    uint32_t uvIndex = getUVIndex(x);

    uchar Y = rsGetElementAt_uchar(input, x);
    uchar U = rsGetElementAt_uchar(input, uvIndex);
    uchar V = rsGetElementAt_uchar(input, uvIndex+1);

    uchar4 rgba = rsYuvToRGBA_uchar4(Y, U, V);

    x = x*4;

    rsSetElementAt_uchar(output,rgba.r, x);
    rsSetElementAt_uchar(output,rgba.g, x+1);
    rsSetElementAt_uchar(output,rgba.b, x+2);
    rsSetElementAt_uchar(output,255, x+3);

    //rsSetElementAt_uchar(output,Y, x*4);
    //rsSetElementAt_uchar(output,Y, x*4+1);
    //rsSetElementAt_uchar(output,Y, x*4+2);
    return in;
}

float RS_KERNEL map(float value,float vmin,float vmax,float newMin,float newMax){
    float a1;
    float b1;
    float a2;
    float b2;
    float valueAux;
    float res;
    a1 = 100.0/(vmax-vmin);
    a2 = 100.0/(newMax-newMin);
    b1 = -a1*vmin;
    b2 = -a2*newMin;
    valueAux = (a1*value+b1);
    res = (valueAux-b2)/a2;
    return res;
}

uint32_t RS_KERNEL getUVIndex(uint32_t i){
    uint32_t index = width*height;
    index += ((i-width*((i/width)/2+(i/width)%2))/2)*2;
    //index += ((i-(i/2+(i/width)%2))/2)*2;
    return index;
}