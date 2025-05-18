package com.example.nightshade;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;


import androidx.appcompat.app.AppCompatActivity;

public class Instructions_Stretch extends AppCompatActivity {

    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions_stretch);

        startButton = findViewById(R.id.startButton);

        ImageView back = (ImageView) findViewById(R.id.backbalance);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Instructions_Stretch.this, Movement.class));
            }
        });


        startButton.setOnClickListener(v -> {
            startActivity(new Intent(Instructions_Stretch.this, Stretch.class));
        });
    }
}
