package com.example.worldsticher360;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private ImageView click_image_id;
    private String currentPhotoPath;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private static final int pic_id = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        click_image_id = findViewById(R.id.click_image_id);
        Button btnOpenCamera = findViewById(R.id.btnOpenCamera);

        btnOpenCamera.setOnClickListener(v -> {
            Intent i = new Intent(this, CameraActivity.class);
            startActivity(i);
        });

    }
}



