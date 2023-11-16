package com.example.worldsticher360;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {
    private EditText nameEditText;
    private Switch themeSwitch;

    // SharedPreferences file name
    private static final String SHARED_PREFS_NAME = "MyPrefs";

    // SharedPreferences keys
    private static final String KEY_NAME = "name";
    private static final String KEY_THEME = "theme";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        nameEditText = findViewById(R.id.settingsNameEditText);
        themeSwitch = findViewById(R.id.settingsThemeSwitch);

        // Load saved preferences
        loadPreferences();

        Button backButton = findViewById(R.id.settingsBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save preferences before finishing the activity
                savePreferences();
                finish();
            }
        });

        themeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save theme preference when switch state changes
                savePreferences();
            }
        });
    }

    // Save preferences
    private void savePreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save name
        editor.putString(KEY_NAME, nameEditText.getText().toString());

        // Save theme mode
        editor.putBoolean(KEY_THEME, themeSwitch.isChecked());

        // Apply changes
        editor.apply();
    }

    // Load preferences
    private void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);

        // Load name
        String name = sharedPreferences.getString(KEY_NAME, "");
        nameEditText.setText(name);

        // Load theme mode
        boolean isDarkTheme = sharedPreferences.getBoolean(KEY_THEME, false);
        themeSwitch.setChecked(isDarkTheme);
    }


}