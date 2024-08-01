package com.example.food_security;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class MainActivity2 extends AppCompatActivity {

    ImageView meow;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        tv = findViewById(R.id.textView);

        String req = MEOW.currentnutri.replace("$" , ":").replace("?" , ",").replace(";" , "\n");



        tv.setText(req);

        // Find the ImageView by its ID
        meow = findViewById(R.id.imageView);

        // Load image from URL using Glide
        Glide.with(this)
                .load(MEOW.currentimage)
                .into(meow);
    }




}