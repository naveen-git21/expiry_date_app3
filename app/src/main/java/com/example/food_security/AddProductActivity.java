package com.example.food_security;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddProductActivity extends AppCompatActivity {

    private EditText nameEditText, descriptionEditText, expiryDateEditText;
    private ImageView productImageView;
    private Button saveButton, captureImageButton;
    private Calendar expiryCalendar;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        nameEditText = findViewById(R.id.nameEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        expiryDateEditText = findViewById(R.id.expiryDateEditText);
        productImageView = findViewById(R.id.productImageView);
        saveButton = findViewById(R.id.saveButton);
        captureImageButton = findViewById(R.id.captureImageButton);

        expiryCalendar = Calendar.getInstance();
        databaseHelper = new DatabaseHelper(this);

        expiryDateEditText.setFocusable(false);
        expiryDateEditText.setClickable(true);
        expiryDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddProductActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        expiryCalendar.set(selectedYear, selectedMonth, selectedDay);
                        updateExpiryDateEditText();
                    }
                },
                year,
                month,
                day
        );
        datePickerDialog.show();
    }

    private void updateExpiryDateEditText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        expiryDateEditText.setText(dateFormat.format(expiryCalendar.getTime()));
    }

    private void saveProduct() {
        String name = nameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String expiryDate = expiryDateEditText.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty() || expiryDate.isEmpty()) {
            Toast.makeText(AddProductActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save product to SQLite
        boolean isInserted = databaseHelper.insertProduct(name, description, expiryDate);
        if (isInserted) {
            // Schedule notifications for the product
            NotificationScheduler.scheduleNotifications(this, name, expiryDate);

            Toast.makeText(this, "Product Added Successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddProductActivity.this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Failed to Add Product", Toast.LENGTH_SHORT).show();
        }
    }
}
