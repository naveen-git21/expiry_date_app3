package com.example.food_security;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.database.Cursor;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TableLayout stk;
    private Button scanButton, addProductButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanButton = findViewById(R.id.scanButton);
        addProductButton = findViewById(R.id.addProductButton);
        stk = findViewById(R.id.table_main);
        databaseHelper = new DatabaseHelper(this);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setOrientationLocked(true);
                integrator.setPrompt("Scan a QR Code");
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                integrator.initiateScan();
            }
        });

        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddProductActivity.class);
                startActivity(intent);
            }
        });

        updateTable();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String reqdata = result.getContents();
            // Handle QR scan result here and add to the database.
            // Example:
            // You might want to parse the QR code data and add it to the database.
            Toast.makeText(this, "QR Code Result: " + reqdata, Toast.LENGTH_LONG).show();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateTable() {
        // Clear all rows except the header
        int childCount = stk.getChildCount();
        if (childCount > 1) {
            stk.removeViews(1, childCount - 1);
        }

        // Fetch products from the database
        Cursor cursor = databaseHelper.getAllProducts();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                TableRow tbrow = new TableRow(this);

                // Create Name field
                TextView tvName = new TextView(this);
                tvName.setText(cursor.getString(cursor.getColumnIndex("name")));
                tbrow.addView(tvName);

                // Create Expiration Date field
                TextView tvExpDate = new TextView(this);
                String expDateStr = cursor.getString(cursor.getColumnIndex("expdate"));
                tvExpDate.setText(expDateStr);
                tbrow.addView(tvExpDate);

                // Create Status field
                TextView tvStatus = new TextView(this);
                String status = calculateStatus(expDateStr);  // Calculate status based on the expiration date
                tvStatus.setText(status);
                tbrow.addView(tvStatus);

                // Create Remove button
                Button removeButton = new Button(this);
                removeButton.setText("Remove");
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                removeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Remove product from SQLite database
                        databaseHelper.deleteProduct(id);
                        updateTable();
                    }
                });
                tbrow.addView(removeButton);

                // Add the row to the table layout
                stk.addView(tbrow);

            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    // Method to calculate product status based on expiration date
    private String calculateStatus(String expDateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date expDate = sdf.parse(expDateStr);
            Date currentDate = Calendar.getInstance().getTime();
            long diffInMillis = expDate.getTime() - currentDate.getTime();
            long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
            if (expDate != null && currentDate != null) {
                if (expDate.before(currentDate)) {
                    return "Expired";
                }else if (diffInDays <= 14) {
                    return "Expiring Soon";
                } else {
                    return "Usable";
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Unknown";  // If there's a problem parsing the date
    }

    // Method to create a notification channel
    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "ProductExpirationChannel";
            String description = "Channel for product expiration reminders";
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            android.app.NotificationChannel channel = new android.app.NotificationChannel(Constants.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            android.app.NotificationManager notificationManager = getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}



