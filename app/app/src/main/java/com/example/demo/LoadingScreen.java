package com.example.demo;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import android.widget.ImageView;

public class LoadingScreen extends AppCompatActivity {

    private static final int LOADING_SCREEN_TIME = 10000; // 10 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);

        ImageView gifImageView = findViewById(R.id.gifImageView);
        Glide.with(this)
                .load(R.drawable.four)
                .into(gifImageView);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start the next activity
                Intent intent = new Intent(LoadingScreen.this, FinalActivity.class);
                startActivity(intent);
                finish(); // Finish this activity
            }
        }, LOADING_SCREEN_TIME);
    }
}
