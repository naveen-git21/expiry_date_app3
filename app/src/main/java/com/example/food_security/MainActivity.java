package com.example.food_security;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final String CHANNEL_ID = "your_channel_id";
    public static final int NOTIFICATION_ID = 1;
    private static final String SHARED_PREFS = "shared_prefs";
    private static final String PRODUCTS_KEY = "products_key";

    private TextView textView;
    private Button scanButton;
    private TableLayout stk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanButton = findViewById(R.id.scanButton);
        stk = findViewById(R.id.table_main);

        loadData();

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

        // Initial table setup
        TableRow tbrow0 = new TableRow(this);

        TextView tv0 = new TextView(this);
        tv0.setText("NAME");
        tv0.setTextColor(Color.BLACK);
        tv0.setPadding(16, 16, 16, 16);
        tbrow0.addView(tv0);
        TextView tv1 = new TextView(this);
        tv1.setText("TYPE");
        tv1.setPadding(16, 16, 16, 16);
        tv1.setTextColor(Color.BLACK);
        tbrow0.addView(tv1);
        TextView tv2 = new TextView(this);
        tv2.setText("EXPDATE");
        tv2.setTextColor(Color.BLACK);
        tv2.setPadding(16, 16, 16, 16);
        tbrow0.addView(tv2);
        TextView tv3 = new TextView(this);
        tv3.setText("Check");
        tv3.setTextColor(Color.BLACK);
        tv3.setPadding(16, 16, 16, 16);
        tbrow0.addView(tv3);
        stk.addView(tbrow0);

        createNotificationChannel();
        updateTable();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            Log.d("hemanth", result.toString());

            String reqdata = result.toString().split(":")[2];
            Log.d("ANITHA2", reqdata.substring(0, reqdata.length() - 9).trim());

            if( !Checkstring(reqdata.substring(0, reqdata.length() - 9).trim()) )
            {
                Toast.makeText(getApplicationContext(), "INVALID QR CODE", Toast.LENGTH_SHORT).show();
                return;
            }

            reqdata = reqdata.trim().substring(0,reqdata.length()-7);

            Log.d("KUMAR" , reqdata);

            data obj = new data();
            String[] strarray = reqdata.substring(0, reqdata.length() - 9).trim().split(",");
            obj.name = strarray[0].trim();
            obj.expdate = strarray[2].trim();
            obj.snaktype = strarray[1].trim();
            obj.image= "https://" +strarray[3].trim().split("@")[0].trim();
            obj.nutition = strarray[3].trim().split("@")[1].trim();
            Log.d("KUMAR3" , obj.nutition);

            MEOW.products.add(obj);





            Log.d("IMAGE" , obj.image);

            String notdate = onemonthdate(obj.expdate);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            try {
                Date curr = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    curr = sdf.parse(LocalDate.now().toString());
                }
                Date exp = sdf.parse(obj.expdate);
                Date notexp = sdf.parse(notdate);

                if (curr != null && exp.before(curr)) {
                    Toast.makeText(getApplicationContext(), "EXPIRED PRODUCT CANNOT BE ADDED", Toast.LENGTH_SHORT).show();
                } else if (curr != null && curr.equals(exp)) {
                    Toast.makeText(getApplicationContext(), "PRODUCT EXPIRES TODAY", Toast.LENGTH_SHORT).show();
                } else {
                    updateTable();
                    scheduleNotification(notexp, obj.name);
                    Toast.makeText(getApplicationContext(), "PRODUCT ADDED", Toast.LENGTH_SHORT).show();
                }
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "DATE PARSING ERROR", Toast.LENGTH_SHORT).show();
            }

            for (int i = 0; i < MEOW.products.size(); i++) {
                Log.d("ANITHA", MEOW.products.get(i).name + " " + MEOW.products.get(i).snaktype + " " + MEOW.products.get(i).expdate);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    void updateTable() {
        // Clear all rows except the header
        int childCount = stk.getChildCount();
        if (childCount > 1) {
            stk.removeViews(1, childCount - 1);
        }

        // Add new rows with product data
        for (int i = 0; i < MEOW.products.size(); i++) {
            TableRow tbrow = new TableRow(this);

            Button checkButtondata = new Button(this);
            checkButtondata.setText(MEOW.products.get(i).name);
            int j = i;
            checkButtondata.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MEOW.currentimage = MEOW.products.get(j).image;
                    MEOW.currentnutri = MEOW.products.get(j).nutition;
                    navigateToSecondActivity();
                }
            });
            tbrow.addView(checkButtondata);

            TextView tvType = new TextView(this);
            tvType.setText(MEOW.products.get(i).snaktype + "    \t");
            tvType.setTextColor(Color.BLACK);
            tbrow.addView(tvType);

            TextView tvExpDate = new TextView(this);
            tvExpDate.setText(MEOW.products.get(i).expdate + "    \t");
            tvExpDate.setTextColor(Color.BLACK);
            tbrow.addView(tvExpDate);

            // Add button to each row
            Button checkButton = new Button(this);
            checkButton.setText("Remove");
            int index = i;
            checkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MEOW.products.remove(index);
                    updateTable();
                    saveData();
                }
            });
            tbrow.addView(checkButton);

            stk.addView(tbrow);
        }

        saveData();
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(MEOW.products);
        editor.putString(PRODUCTS_KEY, json);
        editor.apply();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(PRODUCTS_KEY, null);
        Type type = new TypeToken<ArrayList<data>>() {}.getType();
        MEOW.products = gson.fromJson(json, type);
        if (MEOW.products == null) {
            MEOW.products = new ArrayList<>();
        }
    }

    public static class data {
        public String name;
        public String expdate;
        public String snaktype;
        public String image;
        public String nutition;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void scheduleNotification(Date date, String productName) {
        checkAndRequestExactAlarmPermission();

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("productName", productName);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Set the alarm to start at the specified date
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // Check if the date is in the past
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            Log.e("scheduleNotification", "The specified date is in the past. Notification will not be scheduled.");
            return;
        }

        // Use setExactAndAllowWhileIdle for more accurate and battery-efficient scheduling
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        try {
            showNotification(productName + " " + "Notification scheduled for: " + calendar.getTime().toString());
        } catch (Exception e) {
            Log.d("MEOW", "Error creating notification", e);
        }


    }

    private String onemonthdate(String date) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate parsedDate = LocalDate.parse(date);
            LocalDate oneMonthBefore = parsedDate.minus(1, ChronoUnit.MONTHS);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return oneMonthBefore.format(formatter);
        }
        return date;
    }

    private void checkAndRequestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Exact Alarm permission is necessary for scheduling notifications", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Call this method when you want to show the notification
    private void showNotification(String name) {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.appicon)
                .setContentTitle("My notification")
                .setContentText(name)   // can be changed
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that fires when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Request missing notification permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            return;
        }

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static boolean Checkstring(String input) {
//
//        // Regular expression for the desired format
//        String regex = "^\"([^,]+),([^,]+),(\\d{4}-\\d{2}-\\d{2}),([^@]+)@Producer\\$ ([^;]+);Origin\\$ ([^;]+);Certifications\\$ ([^;]+);Ingredients\\$ ([^;]+);Nutrition Facts \\(per \\d+g serving\\)\\$;Energy\\$ (\\d+ kcal);Protein\\$ (\\d+g);Fat\\$ (\\d+g);Carbohydrates\\$ (\\d+g);Dietary Fiber\\$ (\\d+g);Sugar\\$ (\\d+g);Sodium\\$ (\\d+mg)\"\\s+\"([^,]+),([^,]+),(\\d{4}-\\d{2}-\\d{2}),([^@]+)@Producer\\$ ([^;]+);Origin\\$ ([^;]+);Certifications\\$ ([^;]+);Ingredients\\$\n";
//        // Check if the string matches the regex
//        boolean matches = input.matches(regex);

        if( input.substring(input.length()-5).equals("#meow") )
        {
            return true;
        }


        return false;  // change
    }

    void navigateToSecondActivity(){
        try {
            Intent intent = new Intent(MainActivity.this,MainActivity2.class);
            startActivity(intent);
        }
        catch (Exception e)
        {
            Log.d("ACTIVITYERROR", "ErrOR ON ACTIVITY ");

        }
    }

}
