package com.example.food_security;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NotificationScheduler {

    public static void scheduleNotifications(Context context, String productName, String expDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar expCalendar = Calendar.getInstance();
            expCalendar.setTime(sdf.parse(expDate));

            // Schedule notification for the day of expiration
            scheduleNotification(context, expCalendar, productName, "Product " + productName + " has expired!");

            // Schedule notification for two weeks before expiration
            expCalendar.add(Calendar.DAY_OF_YEAR, -14);
            scheduleNotification(context, expCalendar, productName, "Product " + productName + " is expiring soon!");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static void scheduleNotification(Context context, Calendar calendar, String productName, String message) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("message", message);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}
