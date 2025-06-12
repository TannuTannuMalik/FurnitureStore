package com.example.furniturestore;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Button buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(v -> goToHome());

        // Auto move to Home after delay
        new Handler().postDelayed(this::goToHome, SPLASH_DELAY);
    }

    private void goToHome() {
        Intent intent = new Intent(SplashActivity.this, com.example.furniturestore.MainActivity.class);
        startActivity(intent);
        finish();
    }
}
