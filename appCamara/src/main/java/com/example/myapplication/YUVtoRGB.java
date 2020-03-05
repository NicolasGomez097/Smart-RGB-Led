package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.Image;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;

import java.nio.ByteBuffer;

public class YUVtoRGB {

    private RenderScript rs;
    private ScriptC_convert convertRGB;
    private Allocation yuvAllocation;
    private Allocation rgbAllocation;
    private Allocation auxAllocation;
    private byte nv21[];
    Image image;

    public YUVtoRGB(Context context){
        rs = RenderScript.create(context);
        convertRGB = new ScriptC_convert(rs);
    }

    public void setImage(Image i){
        this.image = i;
    }

    public boolean isOkToRun(){
        if(yuvAllocation == null)
            return false;
        if(rgbAllocation == null)
            return false;
        if(auxAllocation == null)
            return false;
        if(image == null)
            return false;
        return true;
    }

    public byte[] getRGBA(Image image){
        byte rgba[];

        if(image == null)
            return null;

        this.image = image;
        createAllocations();
        populateAllocation();

        convertRGB.set_input(yuvAllocation);
        convertRGB.set_output(rgbAllocation);
        convertRGB.set_width(image.getWidth());
        convertRGB.set_height(image.getHeight());
        convertRGB.forEach_yuv_to_rgb(auxAllocation,auxAllocation);

        rgba = new byte[rgbAllocation.getBytesSize()];
        rgbAllocation.copyTo(rgba);

        return rgba;
    }

    public Bitmap getBitmap(Image image){
        byte rgba[] = getRGBA(image);
        if(rgba == null)
            return null;

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(),image.getHeight(),Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(rgba));
        bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        return bitmap;
    }



    private void createAllocations(){
        if(nv21 != null && yuvAllocation == null){
            Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
            yuvAllocation = Allocation.createTyped(rs, yuvType.create());
        }

        if(image != null && rgbAllocation == null){
            Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(image.getWidth()*image.getHeight());
            rgbAllocation = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        }
        if(image != null && auxAllocation == null){
            Type.Builder scriptType = new Type.Builder(rs, Element.U8(rs)).setX(image.getWidth()*image.getHeight());
            auxAllocation = Allocation.createTyped(rs, scriptType.create(), Allocation.USAGE_SCRIPT);
        }
    }

    private void populateAllocation(){
        nv21 = YUV_420_888toNV21();
        if(yuvAllocation == null){
            Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
            yuvAllocation = Allocation.createTyped(rs, yuvType.create());
        }
        yuvAllocation.copyFrom(nv21);
    }

    public byte[] YUV_420_888toNV21() {
        byte[] nv21;
        int i;

        if(image == null)
            return null;
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        yBuffer.rewind();
        uBuffer.rewind();
        vBuffer.rewind();


        int ySize = yBuffer.remaining();


        nv21 = new byte[ySize+ySize/2];
        yBuffer.get(nv21,0,ySize);

        i=ySize;
        while (uBuffer.remaining()>1){
            nv21[i] = uBuffer.get();
            nv21[i+1] = vBuffer.get();
            uBuffer.get();
            vBuffer.get();
            i+=2;
        }

        return nv21;
    }


    public static Bitmap YUV_420ToBitmap(Image image){
        byte[] rgba = YUV_420_888toIntRGBA(image);
        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(),image.getHeight(),Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(rgba));
        return bitmap;
    }

    private static byte[] YUV_420_888toIntRGBA(Image image) {
        byte[] rgba;

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();


        byte yuv[] = new byte[3];
        int rgb[];
        int height = image.getHeight();
        int width = image.getWidth();

        rgba = new byte[width*height*4];


        for(int y=0; y < height; y=y+2)
            for(int x=0; x < width; x=x+2){
                int index[] = new int[4];
                index[0] = y*width+x;
                index[1] = index[0]+1;
                index[2] = (y+1)*width+x;
                index[3] = index[2]+1;

                if(uBuffer.remaining()>1){
                    yuv[1]=uBuffer.get();
                    uBuffer.get();
                }

                if(uBuffer.remaining()>1) {
                    yuv[2] = vBuffer.get();
                    vBuffer.get();
                }
                for(int i = 0; i < 4; i++){
                    yuv[0]=yBuffer.get(index[i]);
                    rgb = YUV_RGB(yuv);
                    setRgb(rgba,index[i],rgb);
                }
            }

        return rgba;
    }

    private static int[] YUV_RGB(byte yuv[]){
        int rgb[] = new int[3];
        double yuvAux[] = new double[3];
        yuvAux[0] = mapByteInt(yuv[0]&0xff,0,255,0,1);
        yuvAux[1] = mapByteInt(yuv[1]&0xff,0,255,-0.436 ,0.436);
        yuvAux[2] = mapByteInt(yuv[2]&0xff,0,255,-0.615,0.615);
        /*yuvAux[0] = yuv[0]&0xff;
        yuvAux[1] = yuv[1]&0xff-128;
        yuvAux[2] = yuv[2]&0xff-128;*/

        rgb[0] = (int)Math.round((yuvAux[0]+1.402*yuvAux[2])*255);
        rgb[1] = (int)Math.round((yuvAux[0]-0.34414*yuvAux[1]-0.71414*yuvAux[2])*255);
        rgb[2] = (int)Math.round((yuvAux[0]+1.772*yuvAux[1])*255);

        return rgb;
    }

    private static double mapByteInt(double value,double min,double max,double newMin,double newMax){
        double a1,b1,a2,b2,valueAux;
        double res;
        a1 = 100.0/(max-min);
        a2 = 100.0/(newMax-newMin);
        b1 = -a1*min;
        b2 = -a2*newMin;
        valueAux = (a1*value+b1);
        res = (valueAux-b2)/a2;
        return res;
    }

    private static void setRgb(byte pixels[], int index, int rgb[]){
        if(index >= pixels.length)
            return;
        if(rgb[0] > 255)
            rgb[0] =  255;
        else if(rgb[0] < 0)
            rgb[0] = 0;

        if(rgb[1] > 255)
            rgb[1] = 255;
        else if(rgb[1] < 0)
            rgb[1] = 0;

        if(rgb[2] > 255)
            rgb[2] = 255;
        else if(rgb[2] < 0)
            rgb[2] = 0;

        pixels[index*4] = (byte) rgb[0];
        pixels[index*4+1] = (byte) rgb[1];
        pixels[index*4+2] = (byte) rgb[2];
        pixels[index*4+3] = (byte) 255;
    }

}
