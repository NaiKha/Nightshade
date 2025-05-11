package com.example.nightshade;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.Rotation;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.*;

import nl.dionsegijn.konfetti.xml.KonfettiView;

public class StepCounter extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor stepCounter;

    private int currentSteps = 0;
    private TextView textView;
    private KonfettiView konfettiView;
    private ImageView imageView;
    private ProgressBar progressBar;
    private boolean goalReached = false;
    private Handler handler;
    private Runnable imageSwitchRunnable;

    private boolean WalkingImage1 = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_step_counter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageView back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StepCounter.this, Movement.class));
            }
        });
        konfettiView = findViewById(R.id.konfettiView);
        textView = findViewById(R.id.textView6);
        imageView = findViewById(R.id.imageView6);
        progressBar = findViewById(R.id.progressBar2);

        handler = new Handler();
        imageSwitchRunnable = new Runnable() {
            @Override
            public void run() {
                if (WalkingImage1) {
                    imageView.setImageResource(R.drawable.walk2);
                } else {
                    imageView.setImageResource(R.drawable.walk1);
                }
                WalkingImage1 = !WalkingImage1;
                handler.postDelayed(this, 750);
            }
        };

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION}, 100);
        } else {
            startMotionActivity();
        }
    }

    private void startMotionActivity() {
        handler.post(imageSwitchRunnable); // Start the image switching
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (stepCounter != null) {
            sensorManager.unregisterListener(this);
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI);
            textView.setText("Start walking...");
        }
        else {
            textView.setText("Step Counter is not supported on this device.");
        }
    }
    private void updateProgressBar(int currentSteps) {
        progressBar.setProgress(currentSteps, true);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            currentSteps++;
            textView.setText("Steps taken: " + currentSteps);
            updateProgressBar(currentSteps);
            //Drawable confetti_sloth = ContextCompat.getDrawable(this, R.drawable.konfetti);

            //Shape.DrawableShape sloth_shape = new Shape.DrawableShape(confetti_sloth, true);

            if (!goalReached && currentSteps >= 20) {
                goalReached = true;
                triggerCelebration();
            }

        }

    }

    private void triggerCelebration() {
        EmitterConfig emitterConfig = new Emitter(300, TimeUnit.MILLISECONDS).max(300);
        konfettiView.start(
                new PartyFactory(emitterConfig)
                        .shapes(Shape.Circle.INSTANCE, Shape.Square.INSTANCE)
                        .spread(360)
                        .position(0.0,0.0,1.0,1.0)
                        .sizes(new Size(8,50,10))
                        .timeToLive(2000).fadeOutEnabled(true).build()
        );
        vibrateAndNotify();
        ConstraintLayout dialogLayout = findViewById(R.id.completeConstraintLayout);
        View view  = LayoutInflater.from(StepCounter.this).inflate(R.layout.movement_dialog, dialogLayout);
        Button mainPage = view.findViewById(R.id.goToHome);
        Button redo = view.findViewById(R.id.redo);
        Button next = view.findViewById(R.id.next);
        AlertDialog.Builder builder = new AlertDialog.Builder(StepCounter.this);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        mainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StepCounter.this, MainActivity.class));
            }
        });
        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StepCounter.this, StepCounter.class));
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StepCounter.this, Movement.class));
            }
        });

        if (alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        sensorManager.unregisterListener(this);
        alertDialog.show();
    }
;
    private void vibrateAndNotify() {
        try {
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                v.vibrate(2000);
            }

            MediaPlayer mp = MediaPlayer.create(this, R.raw.achievement);
            if (mp != null) {
                mp.setOnCompletionListener(mediaPlayer -> {
                    try {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                mp.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, Movement.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish(); //removes current activity from stack
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        currentSteps = 0;
        goalReached = false;
        progressBar.setProgress(0);
        textView.setText("Start walking...");
        super.onResume();
        if (sensorManager != null && stepCounter != null) {
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(imageSwitchRunnable); // Stop the image switching when the activity is paused
    }
}
