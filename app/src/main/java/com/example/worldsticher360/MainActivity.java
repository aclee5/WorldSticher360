package com.example.worldsticher360;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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
    // SharedPreferences file name
    private static final String SHARED_PREFS_NAME = "MyPrefs";

    // SharedPreferences keys
    private static final String KEY_NAME = "name";
    private static final String KEY_THEME = "theme";
    private TextView welcomeTextView;
    private Button settingsBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //welcomeMessageTextView
        Button btnOpenCamera = findViewById(R.id.btnOpenCamera);
        settingsBtn = findViewById(R.id.settingsBtn);
        welcomeTextView = findViewById(R.id.welcomeMessageTextView);
        btnOpenCamera.setOnClickListener(v -> {
            Intent i = new Intent(this, CameraActivity.class);
            startActivity(i);
        });
        settingsBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        });
        displayPreferences();

    }

    // Display saved preferences
    private void displayPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);

        // Display name
        String name = sharedPreferences.getString(KEY_NAME, "");
        welcomeTextView.setText("Welcome " + name);

    }
}



