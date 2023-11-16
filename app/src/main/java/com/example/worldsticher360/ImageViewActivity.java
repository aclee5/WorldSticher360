package com.example.worldsticher360;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ImageViewActivity extends AppCompatActivity implements View.OnClickListener {
    private Button homeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        homeButton = (Button) findViewById(R.id.imageViewBackButton);
        homeButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == homeButton.getId()){
            Intent homeIntent = new Intent(this, MainActivity.class);
            startActivity(homeIntent);
        }

    }
}