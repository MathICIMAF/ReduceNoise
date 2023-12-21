package com.amg.reducenoise;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.appcompat.app.AppCompatActivity;

/* loaded from: classes.dex */
public class SplashActivity extends AppCompatActivity {
    private static final long COUNTER_TIME = 5;
    private static final String LOG_TAG = "SplashActivity";
    private long secondsRemaining;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        createTimer(COUNTER_TIME);
    }

    private void createTimer(long seconds) {
        new CountDownTimer(seconds * 1000, 1000L) { // from class: com.amg.compressaudio.SplashActivity.1
            @Override // android.os.CountDownTimer
            public void onTick(long millisUntilFinished) {
                SplashActivity.this.secondsRemaining = (millisUntilFinished / 1000) + 1;
            }

            @Override // android.os.CountDownTimer
            public void onFinish() {
                SplashActivity.this.secondsRemaining = 0L;
                SplashActivity.this.startMainActivity();
            }
        }.start();
    }

    public void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
