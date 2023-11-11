package com.example.worldsticher360;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.bytedeco.javacv.AndroidFrameConverter;

public class MainActivity extends AppCompatActivity {
    private AndroidFrameConverter converterToBitmap = new AndroidFrameConverter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}