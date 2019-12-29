package com.example.programablergbled;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Float2;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.basicrenderscript.ScriptC_utils;
import com.google.android.material.math.MathUtils;
import com.otaliastudios.cameraview.CameraLogger;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity{
    private CameraView camera;
    private Bitmap bitmap;
    private CameraLogger logger;
    private TextView fpsCounter;
    private View[] boxs;
    private long time;
    private float currentFps;
    private RenderScript rs;
    private ScriptC_utils script;
    private ScriptIntrinsicYuvToRGB transformScript;
    private int auxX;
    private int auxY;
    private float resolution_factor_x;
    private float resolution_factor_y;
    private Allocation inputAllocation;
    private Allocation outAllocation;
    private Type.Builder xyCordenadas;
    private Matrix matrix;

    private int VALOR_COLOR_ACEPTABLE = 70;
    private int FACTOR_AJUSTE_Y = 20;
    private int TAMAÑO_CUADRADO_BUSQUEDA = 5;
    private int MARGEN_SUPERIOR = 255-VALOR_COLOR_ACEPTABLE;
    private int RES_W = 800;
    private int RES_H = 600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = findViewById(R.id.camara);
        fpsCounter = findViewById(R.id.fpsCount);
        boxs = new View[6];
        boxs[0] = findViewById(R.id.boxR);
        boxs[1] = findViewById(R.id.boxG);
        boxs[2] = findViewById(R.id.boxB);
        boxs[3] = findViewById(R.id.boxY);
        boxs[4] = findViewById(R.id.boxC);
        boxs[5] = findViewById(R.id.boxM);

        logger = CameraLogger.create("Led RGB");
        CameraLogger.setLogLevel(CameraLogger.LEVEL_INFO);

        rs = RenderScript.create(getApplicationContext());
        script = new ScriptC_utils(rs);
        transformScript = ScriptIntrinsicYuvToRGB.create(
                rs, Element.U8_4(rs)
        );


        matrix = new Matrix();
        matrix.setRotate(90);

        if(camera == null){
            return;
        }
        camera.setLifecycleOwner(this);
        camera.addFrameProcessor(new FrameProcessor() {
            @Override
            @WorkerThread
            public void process(@NonNull Frame frame) {
                time = System.nanoTime();
                if (frame.getDataClass() == byte[].class) {
                    if(frame.getFormat() != ImageFormat.NV21)
                        return;

                    /* Transformacion de YUV A RGBA con bitmap*/
                    byte[] yuv = frame.getData();

                    Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(yuv.length);
                    Allocation yuvInput = Allocation.createTyped(rs, yuvType.create());

                    Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(frame.getSize().getWidth()).setY(frame.getSize().getHeight());
                    Allocation rgbOut = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

                    yuvInput.copyFrom(yuv);

                    transformScript.setInput(yuvInput);
                    transformScript.forEach(rgbOut);

                    ByteBuffer buffer = rgbOut.getByteBuffer();

                    bitmap = Bitmap.createBitmap(frame.getSize().getWidth(),frame.getSize().getHeight(),Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);
                    bitmap = Bitmap.createScaledBitmap(bitmap, RES_W, RES_H, false);

                    /*Obtencion de los puntos mas brillantes del frame*/
                    if(inputAllocation == null)
                        inputAllocation = Allocation.createFromBitmap(rs,bitmap);
                    if(xyCordenadas == null)
                        xyCordenadas = new Type.Builder(rs, Element.I32(rs)).setX(bitmap.getWidth()).setY(bitmap.getHeight());
                    if(outAllocation == null)
                        outAllocation = Allocation.createTyped(rs,xyCordenadas.create());
                    inputAllocation.copyFrom(bitmap);

                    script.forEach_rgb_to_int(inputAllocation,outAllocation);

                    int[] array = new int[outAllocation.getBytesSize()];
                    outAllocation.copyTo(array);

                    Float2[] xy =  get10Pixels(array, bitmap.getWidth(), bitmap.getHeight());


                    resolution_factor_x = bitmap.getHeight()*1.0f/camera.getWidth();
                    resolution_factor_y = bitmap.getWidth()*1.0f/camera.getHeight();

                    for(int i = 0; i < boxs.length; i++){
                        if(xy[i].x != -1 && xy[i].y != -1){

                            xy[i].x = xy[i].x/resolution_factor_x;
                            xy[i].y = xy[i].y/resolution_factor_y-FACTOR_AJUSTE_Y;

                            xy[i].x = camera.getWidth()-xy[i].x;

                            auxX = (int)MathUtils.lerp(boxs[i].getX(),xy[i].x,0.6f);
                            auxY = (int)MathUtils.lerp(boxs[i].getY(),xy[i].y,0.6f);
                            boxs[i].setX(auxX);
                            boxs[i].setY(auxY);
                            boxs[i].setVisibility(View.VISIBLE);
                        }else{
                            boxs[i].setVisibility(View.GONE);
                        }
                    }


                    yuvInput.destroy();
                    rgbOut.destroy();
                    /*inputAllocation.destroy();
                    outAllocation.destroy();*/
                }else{
                    System.out.println("No Image, "+frame.getDataClass());
                }

                time = System.nanoTime() - time;
                currentFps = 1000000.f/time*1000;
                fpsCounter.setText(String.format("%.2f",currentFps));
            }
        });
    }

    private float[] getCountPixels(byte[] array,int width, int heigth){
        float[] xy = {0.0f,0.0f};
        int maxCount = 0;
        int cantX = width/TAMAÑO_CUADRADO_BUSQUEDA;
        int cantY = heigth/TAMAÑO_CUADRADO_BUSQUEDA;
        int x,y,i,j,count,auxX,auxY;

        for(i = 0; i < cantX; i++){
            for(j = 0; j  < cantY; j++){
                count = 0;
                for(x = 0; x < TAMAÑO_CUADRADO_BUSQUEDA; x++){
                    for(y = 0; y < TAMAÑO_CUADRADO_BUSQUEDA; y++){
                        auxX = i*TAMAÑO_CUADRADO_BUSQUEDA+x;
                        auxY = j*TAMAÑO_CUADRADO_BUSQUEDA+y;
                        if(array[auxY*width+auxX] == 1)
                            count++;
                    }
                }
                if(count > maxCount){
                    xy[0] = i*TAMAÑO_CUADRADO_BUSQUEDA;
                    xy[1] = j*TAMAÑO_CUADRADO_BUSQUEDA;
                    maxCount = count;
                }
            }
        }

        float aux = xy[0];
        xy[0] = xy[1];
        xy[1] = aux;
        return xy;
    }

    private Float2[] get10Pixels(int[] pixels, int width, int height){
        int color,x,y,i,j,index,auxX,auxY;
        int[] rgb = new int[3];
        int cantX = width/TAMAÑO_CUADRADO_BUSQUEDA;
        int cantY = height/TAMAÑO_CUADRADO_BUSQUEDA;
        String r,g,b;

        Float2[] array = new Float2[10];
        int[] count = new int[10];
        int[] countAux;


        for(i = 0; i < array.length; i++)
            array[i] = new Float2(-1,-1);

        for(i = 0; i < cantX; i++){
            for(j = 0; j  < cantY; j++){
                countAux = new int[10];
                auxX = i*TAMAÑO_CUADRADO_BUSQUEDA;
                auxY = j*TAMAÑO_CUADRADO_BUSQUEDA;
                for(x = 0; x < TAMAÑO_CUADRADO_BUSQUEDA; x++){
                    for(y = 0; y < TAMAÑO_CUADRADO_BUSQUEDA; y++){
                        color = pixels[(auxY+y)*width+auxX+x];

                        rgb[0] = (color & (0xFF<<16))>>16;
                        rgb[1] = (color & (0xFF<<8))>>8;
                        rgb[2] = color & 0xFF;

                        if(rgb[0]>MARGEN_SUPERIOR&&
                            rgb[1] < VALOR_COLOR_ACEPTABLE && rgb[2] < VALOR_COLOR_ACEPTABLE){
                            countAux[0]++;
                            continue;
                        }

                        if(rgb[0] < VALOR_COLOR_ACEPTABLE&&
                                rgb[1] > MARGEN_SUPERIOR && rgb[2] < VALOR_COLOR_ACEPTABLE){
                            countAux[1]++;
                            continue;
                        }

                        if(rgb[0] < VALOR_COLOR_ACEPTABLE&&
                                rgb[1] < VALOR_COLOR_ACEPTABLE && rgb[2] > MARGEN_SUPERIOR){
                            countAux[2]++;
                            continue;
                        }

                        if(rgb[0] > MARGEN_SUPERIOR&&
                                rgb[1] > MARGEN_SUPERIOR && rgb[2] < VALOR_COLOR_ACEPTABLE){
                            countAux[3]++;
                            continue;
                        }

                        if(rgb[0] < VALOR_COLOR_ACEPTABLE&&
                                rgb[1] > MARGEN_SUPERIOR && rgb[2] > MARGEN_SUPERIOR){
                            countAux[4]++;
                            continue;
                        }

                        if(rgb[0] > MARGEN_SUPERIOR &&
                                rgb[1] > MARGEN_SUPERIOR && rgb[2] > MARGEN_SUPERIOR){
                            countAux[5]++;
                            continue;
                        }
                    }
                }
                for(x = 0; x < count.length; x++){
                    if(count[x] < countAux[x]){
                        array[x].x = auxX;
                        array[x].y = auxY;
                        break;
                    }
                }
            }
        }
        float aux;
        for(i = 0; i < array.length; i++){
            aux = array[i].x;
            array[i].x = array[i].y;
            array[i].y = aux;
        }
        return array;
    }
}
