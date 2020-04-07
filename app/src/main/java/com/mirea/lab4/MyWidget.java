package com.mirea.lab4;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class MyWidget extends AppWidgetProvider {

    private static String CHANNEL_ID = "Channel";
    final String UPDATE_ALL_WIDGETS = "update_all_widgets";

    private static String TAG = MyWidget.class.getSimpleName();
    private static long startDate;
    private static long endDate;
    private static long leftDays = 0;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(ConfigureActivity.WIDGET_PREF, Context.MODE_PRIVATE);

        startDate = sharedPreferences.getLong("LONG1" + appWidgetId, 0);

        if (startDate != 0) {
            endDate = sharedPreferences.getLong("LONG2" + appWidgetId, 0);
            leftDays = getDifference(startDate, endDate);
        } else {
            setLeftDays(context);
        }

        Log.e(TAG, "DAYS: " + leftDays);

        String widgetText = context.getString(R.string.appwidget_text);
        widgetText += " " + leftDays;

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        SharedPreferences.Editor editor = context.getSharedPreferences(
                ConfigureActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
        editor.remove("LONG1" + appWidgetId);
        editor.apply();

        Intent configIntent = new Intent(context, ConfigureActivity.class);
        configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        setLeftDays(context);


        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 9);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        Intent intent = new Intent(context, MyWidget.class);
        intent.setAction(UPDATE_ALL_WIDGETS);

        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, c.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pIntent);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        Intent intent = new Intent(context, MyWidget.class);
        intent.setAction(UPDATE_ALL_WIDGETS);

        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equalsIgnoreCase(UPDATE_ALL_WIDGETS)) {
            ComponentName thisAppWidget = new ComponentName(
                    context.getPackageName(), getClass().getName());

            AppWidgetManager appWidgetManager = AppWidgetManager
                    .getInstance(context);

            int[] ids = appWidgetManager.getAppWidgetIds(thisAppWidget);
            for (int appWidgetID : ids) {
                updateAppWidget(context, appWidgetManager, appWidgetID);
            }
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        SharedPreferences.Editor editor = context.getSharedPreferences(
                ConfigureActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();

        for (int widgetID : appWidgetIds) {
            editor.remove("LONG1" + widgetID);
            editor.remove("LONG2" + widgetID);
        }
        editor.apply();
    }

    static long getDifference(long millis1, long millis2) {
        long diff = millis2 - millis1;
        return diff / (24 * 60 * 60 * 1000);
    }

    static void setLeftDays(Context context) {
        Calendar currentDate = Calendar.getInstance();
        long currentMillis = currentDate.getTimeInMillis();
        long countDays = getDifference(currentMillis, endDate);

        if (countDays != leftDays) {
            leftDays = countDays;
        }

        if (isTimeCome() && countDays == 0) {
            showNotification(context);
        }
    }

    static boolean isTimeCome() {
        String strTargetTime = "09:00:00";
        String strEndBorderTime = "10:00:00";

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

        Calendar cal = Calendar.getInstance();
        Date time = cal.getTime();

        String strCurrentTime = dateFormat.format(time);

        try {
            Date targetTime = sdf.parse(strTargetTime);
            Date currentTime = sdf.parse(strCurrentTime);
            Date endBorderTime = sdf.parse(strEndBorderTime);

            if (targetTime.equals(currentTime) || currentTime.before(endBorderTime) && currentTime.after(targetTime)) {
                return true;
            }
        } catch (ParseException ex) {
            Log.e(TAG, " " + ex.getMessage());
        }

        return false;
    }

    static void showNotification(Context context) {

        CharSequence name = "Напоминание";
        String description = "Событие наступило!";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.watch)
                .setContentTitle(name)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(1, builder.build());
    }
}

