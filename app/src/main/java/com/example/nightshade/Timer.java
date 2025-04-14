    package com.example.nightshade;

    import android.content.Intent;
    import android.os.Bundle;
    import android.os.CountDownTimer;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.os.VibrationEffect;
    import android.os.Vibrator;

    import androidx.activity.EdgeToEdge;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;

    import java.text.DecimalFormat;
    import java.text.NumberFormat;

    import android.media.MediaPlayer;


    public class Timer extends AppCompatActivity {
        private boolean started = false;
        private CountDownTimer countDownTimer;
        private Button start_pause;
        private long totalInMilliSecs = 0;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_timer);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            start_pause = (Button)findViewById(R.id.start_btn);
            Button reset_btn = (Button)findViewById(R.id.reset_btn);
            EditText hours = (EditText)findViewById((R.id.card1));
            EditText mins = (EditText)findViewById((R.id.card2));
            EditText secs = (EditText)findViewById((R.id.card3));


            start_pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    started = !started;
                    if (started){
                        start_pause.setText("Pause");

                        int h = Integer.parseInt(hours.getText().toString());
                        int m = Integer.parseInt(mins.getText().toString());
                        int s = Integer.parseInt(secs.getText().toString());

                        totalInMilliSecs = calculateTimeLeft(h,m,s);
                        enableEditTexts(hours, mins, secs, false);


                        if (totalInMilliSecs!=0){
                            startTimer(hours, mins, secs);
                        }


                    } else{
                        start_pause.setText("Start");
                        enableEditTexts(hours, mins, secs, true);
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }

                    }
                }
            });


            reset_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    hours.setText("00");
                    mins.setText("15");
                    secs.setText("00");
                    int h = Integer.parseInt(hours.getText().toString());
                    int m = Integer.parseInt(mins.getText().toString());
                    int s = Integer.parseInt(secs.getText().toString());
                    totalInMilliSecs = calculateTimeLeft(h,m,s);
                    started = false;
                    start_pause.setText("Start");
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }

                }
        });
        }

    private int calculateTimeLeft(int h, int m, int s){
            return (h * 3600000) + (m * 60000) + (s * 1000);
    }
    private void enableEditTexts(EditText hours, EditText mins, EditText secs, boolean bol){
        hours.setEnabled(bol);
        mins.setEnabled(bol);
        secs.setEnabled(bol);
        if (!bol){
            hours.setCursorVisible(!bol);
            mins.setCursorVisible(!bol);
            secs.setCursorVisible(!bol);
        }

    }



        private void startTimer(EditText hours, EditText mins, EditText secs) {
            countDownTimer = new CountDownTimer(totalInMilliSecs, 1000){

                @Override
                public void onTick(long millisUntilFinished) {
                    totalInMilliSecs = millisUntilFinished;
                    NumberFormat f = new DecimalFormat("00");
                    long hour = (millisUntilFinished / 3600000) % 24;
                    long min = (millisUntilFinished / 60000) % 60;
                    long sec = (millisUntilFinished / 1000) % 60;
                    hours.setText(f.format(hour));
                    mins.setText(f.format(min));
                    secs.setText(f.format(sec));

                }
                @Override
                public void onFinish() {
                    hours.setText("00");
                    mins.setText("15");
                    secs.setText("00");
                    started =false;
                    start_pause.setText("Start");
                    totalInMilliSecs = 0;

                    Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    if (v != null) {
                        v.vibrate(1000);
                    }
                    MediaPlayer mediaPlayer = MediaPlayer.create(Timer.this, R.raw.timer);
                    mediaPlayer.start();

                    mediaPlayer.setOnCompletionListener(mp -> {
                        mp.release();
                        Intent intent = new Intent(Timer.this, Movement.class);
                        startActivity(intent);
                    });

                }

            }.start();
        }
    }