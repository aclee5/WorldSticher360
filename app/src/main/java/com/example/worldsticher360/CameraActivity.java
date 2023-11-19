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
import android.os.Handler;
import android.os.Looper;
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
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {
    PreviewView previewView;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float ANGLE_THRESHOLD = 30.0f;
    private int pictureCount = 0;
    // Thresholds for accelerometer values
    private float azimut;
    private double previous_angle=60;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private Button buttonCaptureSave;
    private Button buttonStitch;
    private ImageCapture imageCapture;
    private ArrayList<String> file_paths;
    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    private Sensor orientationSensor;
    private Sensor magnetometer;
    private boolean isCapturing = false;
    private boolean canCapture = false;

    private boolean holding = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        buttonCaptureSave = findViewById(R.id.buttonCaptureSave);
        buttonStitch = findViewById(R.id.imageStitchButton);
        previewView = findViewById(R.id.previewView);

        buttonStitch.setOnClickListener(this);
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
        file_paths = new ArrayList<>();
        pictureCount = 0;
        previous_angle = ANGLE_THRESHOLD;
        canCapture = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        file_paths = new ArrayList<>();
        pictureCount = 0;
        previous_angle = ANGLE_THRESHOLD;
        canCapture = false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        super.onResume();
        file_paths = new ArrayList<>();
        pictureCount = 0;
        previous_angle = ANGLE_THRESHOLD;
        canCapture = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        previous_angle = ANGLE_THRESHOLD;
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
        //absolute value
        double ab_diff;
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
                azimut = orientation[0]; // orientation contains: azimut, pitch, and roll
                ab_diff = Math.abs(previous_angle - ANGLE_THRESHOLD);
                if (Math.abs(Math.toDegrees(orientation[0])) >= ab_diff) {
                    if (!isCapturing) {
                        previous_angle = Math.abs(Math.toDegrees(orientation[0]));
                        capturePhoto();
                        isCapturing = true;
                    }
                } else {
                    isCapturing = false;
                }
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void showHoldMessage()
    {
        if (holding) {
            Toast.makeText(this, "Hold", Toast.LENGTH_SHORT).show();
        }
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
            if(view.getId() == buttonStitch.getId()){
                sensorManager.unregisterListener(CameraActivity.this);
                canCapture = false;
                stitchAndPreview(file_paths);

            }
    }
    private void capturePhoto() {
        long timeStamp = System.currentTimeMillis();
        String fileName = "worldstitcher_" + timeStamp + ".jpg";

        // Specify the folder path where you want to save the photo
        String folderPath = this.getFilesDir() + "/worldstitcher/photos";

        File direct = new File(folderPath);

        if (!direct.exists()) {
            direct.mkdirs();
        }

        File file = new File(direct, fileName);

        Toast.makeText(this, "Taken Photo " + pictureCount, Toast.LENGTH_SHORT).show();
        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        new File(file.getAbsolutePath())
                ).build(),
                executor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Log.d("Camera", "picture count: " + pictureCount);
                        file_paths.add(file.getAbsolutePath());
                        pictureCount++;
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                    }
                });



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
    private void stitchAndPreview(ArrayList<String> paths) {
        long timeStamp = System.currentTimeMillis();
        Bitmap[] images = new Bitmap[paths.size()];

        for (int i = 0; i < paths.size(); i++) {
            File imageFile = new File(paths.get(i));
            images[i] = BitmapUtils.decodeSampledBitmapFromFile(imageFile, 500, 500); // Adjust the sample size as needed
        }

        Bitmap stitchedBitmap = stitchImages(images);
        String image_name = "stitched_image_" + timeStamp +".jpg";

        Uri stitchedImageUri = BitmapUtils.saveBitmapAndGetUri(this, stitchedBitmap, image_name);
        Intent previewIntent = new Intent(CameraActivity.this, ImagePreviewActivity.class);
        previewIntent.putExtra("imageUri", stitchedImageUri);
        previewIntent.putExtra("timestamp", timeStamp);
        file_paths = new ArrayList<>();
        pictureCount = 0;
        previous_angle = 30;
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

