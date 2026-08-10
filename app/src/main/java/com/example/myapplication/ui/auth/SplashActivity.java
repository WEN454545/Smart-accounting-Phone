package com.example.myapplication.ui.auth;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.data.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private ScalableVideoView splashVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(com.example.myapplication.MyApplication.getThemeResId());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashVideo = findViewById(R.id.splash_video);
        setupVideo();

        // Navigate to next screen on click
        findViewById(R.id.splash_root).setOnClickListener(v -> navigateNext());
    }

    private void setupVideo() {
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.preview);
        splashVideo.setVideoURI(videoUri);
        splashVideo.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            splashVideo.start();
        });
    }

    private void navigateNext() {
        SessionManager sessionManager = new SessionManager(this);
        Intent intent;
        if (sessionManager.isLoggedIn()) {
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}