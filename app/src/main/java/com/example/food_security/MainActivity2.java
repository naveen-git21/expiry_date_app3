package com.example.food_security;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        TextView nutritionInfo = findViewById(R.id.nutrition_info);

        // Assuming you're trying to show some string for nutrition information
        String req = "Nutritional Info: Calories: 200, Protein: 10g, Fat: 5g";  // Dummy data

        // Display the string in the TextView
        nutritionInfo.setText(req);
    }
}
