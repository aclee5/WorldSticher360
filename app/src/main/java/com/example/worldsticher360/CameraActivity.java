package com.example.worldsticher360;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraProvider;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    private CameraProvider cameraProvider;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float ACCELEROMETER_THRESHOLD = 9.0f;
    private static final float ANGLE_THRESHOLD = 60.0f;
    private int pictureCount = 0;
    // Thresholds for accelerometer values
    private float azimut;
    private float[] mGravity;
    private float[] mGeomagnetic;
    float previousOrientation = ANGLE_THRESHOLD;
    private Button buttonCaptureSave, buttonCaptureShow;
    private ImageCapture imageCapture;
    private String [] file_paths;
    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};

    private Sensor orientationSensor;
    private Sensor magnetometer;
    private boolean isCapturing = false;
    private boolean canCapture = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        buttonCaptureSave = findViewById(R.id.buttonCaptureSave);
        previewView = findViewById(R.id.previewView);

        buttonCaptureSave.setOnClickListener(this);

        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        file_paths = new String[3];
        pictureCount = 0;
        previousOrientation = ANGLE_THRESHOLD;
        canCapture = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        file_paths = new String[3];
        pictureCount = 0;
        previousOrientation = ANGLE_THRESHOLD;
        canCapture = false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        super.onResume();
        file_paths = new String[3];
        pictureCount = 0;
        previousOrientation = ANGLE_THRESHOLD;
        canCapture = false;
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                if(Math.abs(Math.toDegrees(orientation[0])) < 30 && Math.abs(Math.toDegrees(orientation[0])) > 0)
                {
                    canCapture = true;

                    Toast.makeText(this, "Can Start", Toast.LENGTH_SHORT).show();
                }
                if(canCapture && !isCapturing && Math.abs(Math.toDegrees(orientation[0])) >= previousOrientation)
                {
                    capturePhoto();
                    isCapturing = true;
                    previousOrientation = (float) (Math.abs(Math.toDegrees(orientation[0])) + 30.0);
                } else {
                    isCapturing = false;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        cameraProvider.unbindAll();

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();


        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

        @Override
    public void onClick(View view) {
            if(view.getId() == buttonCaptureSave.getId()) {
                capturePhoto();
            }
    }
    private void capturePhoto() {
        long timeStamp = System.currentTimeMillis();
        String fileName = "worldstitcher_" + timeStamp + ".jpg";

        // Specify the folder path where you want to save the photo
        String folderPath = Environment.getExternalStorageDirectory() + "/worldstitcher/photos";

        File direct = new File(folderPath);

        if (!direct.exists()) {
            direct.mkdirs();
        }

        File file = new File(direct, fileName);

        if (pictureCount < 3) {
            imageCapture.takePicture(
                    new ImageCapture.OutputFileOptions.Builder(
                            new File(file.getAbsolutePath())
                    ).build(),
                    executor,
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            Log.d("Camera", "picture count: " + pictureCount);
                            file_paths[pictureCount] = file.getAbsolutePath();
                            pictureCount++;
                            if (pictureCount == 3) {
                                stitchAndPreview(file_paths);
                                // Unregister the sensor listener when done capturing three images
                                sensorManager.unregisterListener(CameraActivity.this);
                            }
                        }
                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Toast.makeText(CameraActivity.this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public Bitmap stitchImages(Bitmap[] images) {
        int totalWidth = 0;
        int maxHeight = 0;

        // Calculate total width and maximum height
        for (Bitmap image : images) {
            totalWidth += image.getWidth();
            maxHeight = Math.max(maxHeight, image.getHeight());
        }

        // Create a new bitmap with calculated dimensions
        Bitmap stitchedBitmap = Bitmap.createBitmap(totalWidth, maxHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(stitchedBitmap);

        // Draw each image onto the new bitmap
        int currentX = 0;
        for (Bitmap image : images) {
            canvas.drawBitmap(image, currentX, 0, null);
            currentX += image.getWidth();
        }

        return stitchedBitmap;
    }

    private void stitchAndPreview(String [] paths) {
        long timeStamp = System.currentTimeMillis();
        Bitmap[] images = new Bitmap[3];

        for (int i = 0; i < paths.length; i++) {
            File imageFile = new File(paths[i]);
            images[i] = BitmapUtils.decodeSampledBitmapFromFile(imageFile, 500, 500); // Adjust the sample size as needed
        }

        Bitmap stitchedBitmap = stitchImages(images);
        String image_name = "stitched_image_" + timeStamp +".jpg";

        Uri stitchedImageUri = BitmapUtils.saveBitmapAndGetUri(this, stitchedBitmap, image_name);
        Intent previewIntent = new Intent(CameraActivity.this, ImagePreviewActivity.class);
        previewIntent.putExtra("imageUri", stitchedImageUri);
        previewIntent.putExtra("timestamp", timeStamp);
        file_paths = new String[3];
        pictureCount = 0;
        startActivity(previewIntent);
    }
    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}

