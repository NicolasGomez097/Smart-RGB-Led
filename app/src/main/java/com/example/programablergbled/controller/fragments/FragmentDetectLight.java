package com.example.programablergbled.controller.fragments;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.fragment.app.Fragment;

import com.example.android.basicrenderscript.ScriptC_utils;
import com.example.programablergbled.R;
import com.example.programablergbled.model.Blob;
import com.example.programablergbled.model.Vector2D;
import com.otaliastudios.cameraview.CameraLogger;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class FragmentDetectLight extends Fragment {
    private View fragment_container;
    private CameraView camera;
    private Bitmap bitmap;
    private CameraLogger logger;
    private TextView fpsCounter;
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
    private FrameLayout container;
    private ArrayList<View> views;

    private int COLOR_THRESHOLD = 70;
    private int DISTANCE_THRESHOLD = 50;
    private String BLOB_RED = "RED";
    private String BLOB_GREEN = "GREEN";
    private int FACTOR_AJUSTE_Y = 20;
    //private int TAMAÑO_CUADRADO_BUSQUEDA = 5;
    //private int MARGEN_SUPERIOR = 255-VALOR_COLOR_ACEPTABLE;
    private int RES_W = 800;
    private int RES_H = 600;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment_container = inflater.inflate(R.layout.fragment_detect_light,container,false);
        init();
        return fragment_container;
    }

    void init() {
        camera = fragment_container.findViewById(R.id.camara);
        fpsCounter = fragment_container.findViewById(R.id.fpsCount);
        container = fragment_container.findViewById(R.id.container);

        logger = CameraLogger.create("Led RGB");
        CameraLogger.setLogLevel(CameraLogger.LEVEL_INFO);

        rs = RenderScript.create(getContext());
        script = new ScriptC_utils(rs);
        transformScript = ScriptIntrinsicYuvToRGB.create(
                rs, Element.U8_4(rs)
        );

        if(camera == null){
            return;
        }
        camera.setLifecycleOwner(this);
        camera.addFrameProcessor(frame -> {
            time = System.nanoTime();
            if (frame.getDataClass() == byte[].class) {
                if(frame.getFormat() != ImageFormat.NV21)
                    return;

                /* Transformacion de YUV A RGBA con bitmap*/
                byte[] yuv = frame.getData();
                for(int i=0; i< yuv.length;i++)
                    if((yuv[i]&0xff) > 100)
                        yuv[i] = yuv[i];

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

                /* Transformacion de RGB 0-1 a 0-255*/
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

                /* Obtencion de los blobs*/
                ArrayList<Blob> blobs =  getBlobs(array, bitmap.getWidth(), bitmap.getHeight());

                resolution_factor_x = camera.getWidth()*1.0f/bitmap.getHeight();
                resolution_factor_y = camera.getHeight()*1.0f/bitmap.getWidth();

                //Limpiar los blobs del frame anterior
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        container.removeAllViews();
                    }
                });

                for(int i = 0; i < blobs.size() && i < 10;i++){
                    final Blob blob = blobs.get(i);
                    Vector2D<Integer> center = blob.getCenter();
                    int auxInt;
                    auxInt = center.x;
                    center.x = center.y;
                    center.x = bitmap.getHeight()-center.x;
                    center.y = auxInt;

                    center.x = (int)(resolution_factor_x*center.x);
                    center.y = (int)(resolution_factor_y*center.y);

                    final View v = new View(getActivity().getApplicationContext());
                    v.setX(center.x-blob.getWidth()/2);
                    v.setY(center.y-blob.getHeight()/2);
                    Drawable d = getActivity().getDrawable(R.drawable.border);
                    if(blob.description.equals(BLOB_RED))
                        d.setTint(0xffff0000);
                    else
                        d.setTint(0xff00ff00);
                    v.setBackground(d);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            container.addView(v,blob.getHeight(),blob.getWidth());
                        }
                    });

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
        });
    }

    public ArrayList<Blob> getBlobs(int[] pixels, int width, int height){
        ArrayList<Blob> redBlobs = new ArrayList<>();
        ArrayList<Blob> greenBlobs = new ArrayList<>();
        ArrayList<Blob> aux = null;

        int pos;
        int rgb[] = new int[3];
        int color;
        int minDistance;
        int minBlobDistanceIndex;
        int auxDistance;
        String blobColor = "";

        for(int x = 0; x < width; x++)
            for(int y = 0; y < height; y++){
                pos = x + y*width;
                color = pixels[pos];
                rgb[0] = (color & (0xFF<<16))>>16;
                rgb[1] = (color & (0xFF<<8))>>8;
                rgb[2] = color & 0xFF;

                if(rgb[0] > 255 - COLOR_THRESHOLD &&
                        rgb[1] < COLOR_THRESHOLD &&
                        rgb[2] < COLOR_THRESHOLD){
                    aux = redBlobs;
                    blobColor = BLOB_RED;
                }

                if(rgb[0] < COLOR_THRESHOLD &&
                        rgb[1] > 255 - COLOR_THRESHOLD &&
                        rgb[2] < COLOR_THRESHOLD){
                    aux = greenBlobs;
                    blobColor = BLOB_GREEN;
                }

                if(aux == null)
                    continue;


                minDistance = DISTANCE_THRESHOLD;
                minBlobDistanceIndex = -1;

                for(int i = 0; i < aux.size();i++){
                    auxDistance = aux.get(i).getDistance(x,y);
                    if(minDistance>auxDistance){
                        minDistance = auxDistance;
                        minBlobDistanceIndex = i;
                    }
                }

                if(minBlobDistanceIndex != -1)
                    aux.get(minBlobDistanceIndex).addPoint(x,y);
                else{
                    aux.add(new Blob(x,y,blobColor));
                }

                aux = null;
            }
        redBlobs.addAll(greenBlobs);

        return redBlobs;
    }
}
