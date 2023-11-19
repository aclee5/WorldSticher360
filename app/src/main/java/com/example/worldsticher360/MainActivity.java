package com.example.worldsticher360;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private RecyclerView recyclerView;
    private ImageGridAdapter adapter;

    private Button deleteDbBtn;
    // SharedPreferences keys
    private static final String KEY_NAME = "name";
    private static final String KEY_THEME = "theme";
    private TextView welcomeTextView;
    private ImageButton settingsBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //welcomeMessageTextView
        ImageButton btnOpenCamera = findViewById(R.id.btnOpenCamera);
        settingsBtn = findViewById(R.id.settingsBtn);
        welcomeTextView = findViewById(R.id.welcomeMessageTextView);
        deleteDbBtn = findViewById(R.id.btnDeleteDb);
        btnOpenCamera.setOnClickListener(v -> {
            Intent i = new Intent(this, CameraActivity.class);
            startActivity(i);
        });
        settingsBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        });
        deleteDbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDatabase();
            }
        });
        displayPreferences();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        MyDatabase myDatabase = new MyDatabase(this);
        Cursor cursor = myDatabase.getPhotoData();

        Toast.makeText(this,"Number of rows in cursor: " + cursor.getCount(), Toast.LENGTH_SHORT).show();
        // Set up the adapter
        adapter = new ImageGridAdapter(this, cursor);
        recyclerView.setAdapter(adapter);
    }

    // Display saved preferences
    private void displayPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);

        // Display name
        String name = sharedPreferences.getString(KEY_NAME, "");
        welcomeTextView.setText("Welcome " + name);

    }

    private void deleteDatabase() {
        // Replace "YourDatabaseName" with your actual database name
        String databaseName = Constants.DATABASE_NAME;
        boolean isDeleted = this.deleteDatabase(databaseName);

        if (isDeleted) {
            Toast.makeText(this, "Database deleted successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to delete database", Toast.LENGTH_SHORT).show();
        }
    }
}



