package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class AndroidCameraApi extends AppCompatActivity {
    private static final String TAG = "AndroidCameraApi";
    private TextureView textureView;
    private ImageView preview;
    private SurfaceTexture texture2;
    private TextView fpsCounter;
    private FrameLayout blobContainer;
    /*private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }*/
    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;


    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    private YUVtoRGB yuvToRgbConverter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_xmain);
        textureView =  findViewById(R.id.texture);
        textureView.setSurfaceTextureListener(textureListener);
        preview = findViewById(R.id.preview);
        //preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        fpsCounter = findViewById(R.id.fpsCounter);
        blobContainer = findViewById(R.id.blobContainer);

        yuvToRgbConverter = new YUVtoRGB(getApplicationContext());
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void createCameraPreview() {
        try {
            final SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            imageReader = ImageReader.newInstance(imageDimension.getWidth(),
                    imageDimension.getHeight(), ImageFormat.YUV_420_888,3);

            List surfaces = new ArrayList();

            Surface surface = imageReader.getSurface();
            surfaces.add(surface);
            captureRequestBuilder.addTarget(surface);

            /*Surface surface2 = new Surface(texture);
            surfaces.add(surface2);
            captureRequestBuilder.addTarget(surface2);*/

            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    final long time = System.currentTimeMillis();
                    Image image = reader.acquireLatestImage();
                    if(image == null){
                        System.out.println("Imagen Nula");
                        return;
                    }
                    final Bitmap b = yuvToRgbConverter.getBitmap(image);
                    image.close();
                    if (b == null)
                        return;

                    Bitmap aux = b.copy(Bitmap.Config.ARGB_8888,true);

                    /*runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            preview.setImageBitmap(b);


                        }
                    });*/

                    int pixels[] = new int[b.getWidth()*b.getHeight()];
                    aux.getPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());

                    ArrayList<Blob> list = BlobUtils.getBlobs(pixels,b.getWidth(),b.getHeight());
                    showBlobls(list,new Size(b.getWidth(),b.getHeight()));
                    //System.out.println("Blobs size: "+list.size());
                    fpsCounter.setText(""+((System.currentTimeMillis()-time)*60*60)/1000);
                }
            },mBackgroundHandler);


            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
            captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,50);

            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(AndroidCameraApi.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            Size sizes[] = map.getOutputSizes(SurfaceTexture.class);
            for(Size s: sizes){
                if(s.getWidth() == 1280 || s.getHeight() == 720)
                    imageDimension = s;
            }
            if(imageDimension == null)
                return;
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AndroidCameraApi.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    protected void updatePreview() {
        if(null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        //captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(AndroidCameraApi.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void showBlobls(ArrayList<Blob> blobs,Size image){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                blobContainer.removeAllViews();
            }
        });

        for(final Blob blob: blobs){
            if(blob.getSize() < 100)
                continue;
            Vector2D<Integer> center = blob.getCenter();

            final float resolution_factor_x = 1.0f*blobContainer.getWidth()/image.getWidth();
            final float resolution_factor_y = 1.0f*blobContainer.getHeight()/image.getHeight();


            center.x = (int)(resolution_factor_x*center.x);
            center.y = (int)(resolution_factor_y*center.y);

            final View v = new View(getApplicationContext());
            v.setX(center.x-blob.getWidth()/2);
            v.setY(center.y-blob.getHeight()/2);
            Drawable d = getDrawable(R.drawable.border);
            if(blob.description.equals(Blob.BLOB_RED))
                d.setTint(0xffff0000);
            else
                d.setTint(0xff00ff00);
            v.setBackground(d);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    blobContainer.addView(v,blob.getWidth(), blob.getHeight());
                }
            });

        }
    }

}