package com.example.worldsticher360;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;

public class ImagePreviewActivity extends AppCompatActivity{

    private ImageView imageViewPreview;
    private Button homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        imageViewPreview = findViewById(R.id.image_preview);

        //Home Button initialization
        homeButton = (Button) findViewById(R.id.imgPrevHomeButton);

        // Get the image path from the intent
        // Load and display the image
        Uri imageUri = getIntent().getParcelableExtra("imageUri");

        // Load and display the image
        if (imageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imageViewPreview.setImageBitmap(bitmap);
                Log.d("ImagePreviewActivity", "Image loaded successfully");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("ImagePreviewActivity", "Error loading image: " + e.getMessage());
                // Handle the exception
            }
        } else {
            Log.e("ImagePreviewActivity", "Image URI is null");
        }
    }
}