package com.example.programablergbled.controller.fragments;

import android.content.pm.PackageManager;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraX;
import androidx.camera.core.CaptureConfig;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.SessionConfig;
import androidx.camera.core.UseCase;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.example.programablergbled.R;

public class FragmentDetectLight2 extends Fragment {
    private View fragment_container;
    private TextureView textureView;
    private ImageView previewContainer;
    private CameraManager cameraManager;
    private int cameraFacing;
    private String cameraId;
    private CameraDevice cameraDevice;

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment_container = inflater.inflate(R.layout.fragment_detect_light2,container,false);

        textureView = fragment_container.findViewById(R.id.cameraPreview);
        previewContainer = fragment_container.findViewById(R.id.imagePreview);

        if(allPermissionsGranted()){
            init(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        return fragment_container;
    }

    private void init(){
        CameraX.unbindAll();

        Size screen = new Size(textureView.getWidth(), textureView.getHeight()); //size of the screen


        PreviewConfig pConfig = new PreviewConfig.Builder().
                setTargetResolution(screen).
                build();
        Preview preview = new Preview(pConfig);
        //to update the surface texture we  have to destroy it first then re-add it
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
             @Override
             public void onUpdated(Preview.PreviewOutput output) {
                 //textureView.setSurfaceTexture(output.getSurfaceTexture());
             }
         });



        ImageAnalysisConfig config =
                new ImageAnalysisConfig.Builder()
                        .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                        .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis(config);

        imageAnalysis.setAnalyzer(new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(ImageProxy image, int rotationDegrees) {

            }
        });




        CameraX.bindToLifecycle((LifecycleOwner) this, preview, imageAnalysis);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                init();
            } else{
                Toast.makeText(getContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }
}

