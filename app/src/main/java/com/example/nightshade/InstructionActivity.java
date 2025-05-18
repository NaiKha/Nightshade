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

public class InstructionActivity extends AppCompatActivity {

    private TextView countdownText;
    private Button startButton;
    private MediaPlayer countdownPlayer;


    private Vibrator vibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        countdownText = findViewById(R.id.countdownText);
        startButton = findViewById(R.id.startButton);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        countdownPlayer = MediaPlayer.create(this, R.raw.countdown);

        ImageView back = (ImageView) findViewById(R.id.backbalance);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InstructionActivity.this, Movement.class));
            }
        });


        startButton.setOnClickListener(v -> {
            startButton.setEnabled(false); // Prevent repeat clicks
            countdownText.setVisibility(View.VISIBLE);
            startCountdown();
        });
    }


    private void startCountdown() {
        if (countdownPlayer != null) {
            countdownPlayer.start(); 
        }

        new CountDownTimer(3000, 1000) {
            int count = 3;

            public void onTick(long millisUntilFinished) {
                countdownText.setText(String.valueOf(count));
                vibrate(); // Optional
                count--;
            }

            public void onFinish() {
                countdownText.setText("Go!");
                startActivity(new Intent(InstructionActivity.this, Balance.class));
                finish();
            }
        }.start();
    }

    private void vibrate() {
                if (vibrator != null && vibrator.hasVibrator()) {
                    VibrationEffect effect = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        effect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(effect);
                    }
                }
            }


    public void onFinish() {
        countdownText.setText("Go!");
        startActivity(new Intent(InstructionActivity.this, Balance.class));
        finish();
    }
}
