#pragma version(1)
#pragma rs java_package_name(com.example.android.basicrenderscript)
#pragma rs_fp_relaxed

const static float3 gMonoMult = {0.299f, 0.587f, 0.114f};

float2 xy = {0.0f,1.0f};

/*
 * RenderScript kernel that performs saturation manipulation.
 */
uchar4 RS_KERNEL saturation(uchar4 in)
{
    float4 f4 = rsUnpackColor8888(in);
    //float3 result = dot(f4.rgb, gMonoMult);
    float gray = 0.3f*f4.r + 0.59f*f4.g + 0.11f*f4.b;
    if(gray < 0.999999f){
        f4.r = 0;
        f4.g = 0;
        f4.b = 0;
    }else{
        f4.r = gray;
        f4.g = gray;
        f4.b = gray;
    }
    return rsPackColorTo8888(f4);
}

uint8_t RS_KERNEL transformHigh(uchar4 in)
{
    float4 f4 = rsUnpackColor8888(in);
    uint8_t isHigh;

    float gray = 0.3f*f4.r + 0.59f*f4.g + 0.11f*f4.b;
    if(gray < 0.99999f){
        isHigh = 0;
    }else{
        isHigh = 1;
    }
    return isHigh;
}

uint8_t RS_KERNEL promedio(uchar4 in,uint32_t x, uint32_t y)
{
    float4 f4 = rsUnpackColor8888(in);
    uint8_t isHigh;
    if(f4.r > 0.9999f)
        isHigh = 1;
    else
        isHigh = 0;
    return isHigh;
}

int RS_KERNEL rgb_to_int(uchar4 in)
{
    float4 f4 = rsUnpackColor8888(in);
    int r = f4.r*255;
    int g = f4.g*255;
    int b = f4.b*255;
    int color = (r << 16)+ (g << 8) + b;
    return color;
}

