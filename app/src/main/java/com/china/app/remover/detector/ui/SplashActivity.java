package com.china.app.remover.detector.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.widget.TextView;

import com.china.app.remover.detector.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TextView tvMadewith=findViewById(R.id.tvMadeWith);
        tvMadewith.setText("Made with "+Html.fromHtml("<font color='red'>"+"\u2665"+"</font>"+" in India"));
        new Handler().postDelayed(new Runnable() {


            @Override
            public void run() {
                // This method will be executed once the timer is over
                Intent i = new Intent(SplashActivity.this, HomePageActivity.class);
                startActivity(i);
                finish();
            }
        }, 2000);
    }
}
