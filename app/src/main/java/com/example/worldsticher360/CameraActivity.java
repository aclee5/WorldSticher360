package com.example.worldsticher360;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

    // Thresholds for accelerometer values
    private static final float ACCELEROMETER_THRESHOLD = 9.0f;

    private Button buttonCaptureSave, buttonCaptureShow;
    private ImageCapture imageCapture;
    private static final int img_id = 1;
    public static int buttonCaptureSaveInt;

    // new
    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};

    private boolean isCapturing = false;
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
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float acceleration = (float) Math.sqrt(x * x + y * y + z * z);

        if (acceleration > ACCELEROMETER_THRESHOLD && !isCapturing) {
            // Trigger photo capture
            capturePhoto();
            isCapturing = true;
        } else if (acceleration <= ACCELEROMETER_THRESHOLD) {
            isCapturing = false;
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
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
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

        // Capture the photo
        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        new File(file.getAbsolutePath())
                ).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // Your existing code to handle the saved image

                        // Example: Display the saved file path
                        String savedImagePath = file.getAbsolutePath();
                        Toast.makeText(CameraActivity.this, savedImagePath, Toast.LENGTH_SHORT).show();

                        // Start ImagePreviewActivity and pass the image path
                        Uri savedImageUri = Uri.fromFile(file);
                        Intent previewIntent = new Intent(CameraActivity.this, ImagePreviewActivity.class);
                        previewIntent.putExtra("imageUri", savedImageUri);
                        startActivity(previewIntent);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        // Your existing code to handle the error
                        Toast.makeText(CameraActivity.this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null || cursor.getCount() == 0) {
            // fallback to the original path if cursor is null or empty
            if (cursor != null) {
                cursor.close();
            }
            return uri.getPath();
        } else {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
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

