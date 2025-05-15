package com.example.nightshade;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class Stretch extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor linearAcceleration;

    private ImageView stretchImage;
    private TextView readyText;

    private boolean armsUp = false;
    private boolean isReady = false;
    private long lastEventTime = 0;
    private long lastTimestamp = 0L;
    private int counter = 0;
    private KonfettiView konfettiView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stretch);

        stretchImage = findViewById(R.id.stretchImage);
        stretchImage.setImageResource(R.drawable.stretch1);

        readyText = findViewById(R.id.readyText);
        readyText.setVisibility(View.VISIBLE);

        konfettiView = findViewById(R.id.konfettiView);


        ImageView back = findViewById(R.id.back);
        back.setOnClickListener(v -> didFinish());

        // Vi vill inte att en av aktiveringsskakningarna ska räknas...
        new Handler().postDelayed(() -> {
            isReady = true;
            readyText.setText(R.string.stretcha);
        }, 1000);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        linearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, linearAcceleration, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isReady || event.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION) {
            lastTimestamp = event.timestamp;
            return;
        }

        long nowNs = event.timestamp;
        if (lastTimestamp != 0L) {
            float deltaTime = (nowNs - lastTimestamp) / 1_000_000_000f;
            float y = event.values[1];
            long now = System.currentTimeMillis();
            if (Math.abs(y * deltaTime) > 0.250f && (now - lastEventTime) > 2000) {
                toggleStretch();
                lastEventTime = now;
                resetTime();
            }
        }
        lastTimestamp = nowNs;

        if (counter > 4) {
            triggerCelebration();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void toggleStretch() {
        stretchImage.setImageResource(armsUp ? R.drawable.stretch1 : R.drawable.stretch2);
        armsUp = !armsUp;
        giveFeedback();
        counter += 1; 
    }

    private void giveFeedback() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    private void triggerCelebration() {
        EmitterConfig emitterConfig = new Emitter(500, TimeUnit.MILLISECONDS).max(500);
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
        View view  = LayoutInflater.from(Stretch.this).inflate(R.layout.movement_dialog, dialogLayout);

        AlertDialog.Builder builder = new AlertDialog.Builder(Stretch.this);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();


        if (alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        sensorManager.unregisterListener(this);
        alertDialog.show();
        new Handler().postDelayed(() -> {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
                startActivity(new Intent(Stretch.this, Movement.class));
            }
        }, 5000);

    }

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

    private void resetTime() {
        lastTimestamp = 0L;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        didFinish();
    }

    private void didFinish() {
        Intent intent = new Intent(this, Movement.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}