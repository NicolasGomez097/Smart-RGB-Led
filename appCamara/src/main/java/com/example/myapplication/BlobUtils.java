package com.example.myapplication;

import java.util.ArrayList;

public class BlobUtils {

    private static int COLOR_THRESHOLD = 50;
    private static int DISTANCE_THRESHOLD = 15;


    public static ArrayList<Blob> getBlobs(int[] pixels, int width, int height){
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
                    blobColor = Blob.BLOB_RED;
                }

                if(rgb[0] < COLOR_THRESHOLD &&
                        rgb[1] > 255 - COLOR_THRESHOLD &&
                        rgb[2] < COLOR_THRESHOLD){
                    aux = greenBlobs;
                    blobColor = Blob.BLOB_GREEN;
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
