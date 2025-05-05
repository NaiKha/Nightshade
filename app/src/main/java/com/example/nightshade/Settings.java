package com.example.nightshade;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        EditText focusTime = (EditText) findViewById(R.id.editTextNumber);
        Button saveButton =  (Button) findViewById(R.id.save_button);
        SharedPreferences sharedPreferences = getSharedPreferences("FocusTimePref", MODE_PRIVATE);
        String savedFocusTime = sharedPreferences.getString("focus_time", "25");
        focusTime.setText(savedFocusTime);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String focusTimeStr = focusTime.getText().toString();

                SharedPreferences sharedPreferences = getSharedPreferences("FocusTimePref", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("focus_time", focusTimeStr);
                editor.apply();



                Toast.makeText(Settings.this, "Your preferences are now saved!", Toast.LENGTH_SHORT).show();
            }
        });
    }



}