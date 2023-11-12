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
            Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(camera_intent, pic_id);
        });

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        handleImage();
                    } else {
                        Toast.makeText(this, "Image capture failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getFilesDir(); // Use getFilesDir() for internal storage
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == pic_id) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            click_image_id.setImageBitmap(photo);
        }

        if (data.resolveActivity(getPackageManager()) != null) {
            Toast.makeText(this, "Saving", Toast.LENGTH_SHORT).show();
            try {
                File photoFile = createImageFile();
                Uri photoURI = FileProvider.getUriForFile(
                        this,
                        "com.example.android.file-provider",
                        photoFile
                );
                data.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureLauncher.launch(data);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }
        } else {

            Toast.makeText(this, "Error cmage file", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImage() {
        File imgFile = new File(currentPhotoPath);
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            saveImageToGallery();
            click_image_id.setImageBitmap(myBitmap);
        }
    }

    private void saveImageToGallery() {
        MediaScannerConnection.scanFile(
                this,
                new String[]{currentPhotoPath},
                null,
                (path, uri) -> {
                    // Media scan completed, you can perform any additional actions if needed.
                    Toast.makeText(MainActivity.this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
                }
        );
    }
}



