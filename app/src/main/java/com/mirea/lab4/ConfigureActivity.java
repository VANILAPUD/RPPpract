package com.mirea.lab4;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;


public class ConfigureActivity extends Activity {

    public final static String WIDGET_PREF = "WIDGET_PREF";
    private int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    private long startDate;
    private long endDate;

    private Intent resultValue;
    private SharedPreferences sharedPreferences;
    private Calendar calendar = Calendar.getInstance();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.my_widget_configure);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        setResult(RESULT_CANCELED, resultValue);
        sharedPreferences = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);

        setDate();
    }

    public void setDate() {
        DatePickerDialog dialog = new DatePickerDialog(ConfigureActivity.this, d,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    finish();
                }
            }
        });
    }

    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setInitialDates();

            sharedPreferences.edit()
                             .putLong("LONG1" + widgetID, startDate)
                             .apply();
            sharedPreferences.edit()
                             .putLong("LONG2" + widgetID, endDate)
                             .apply();

            MyWidget.updateAppWidget(ConfigureActivity.this,
                                    AppWidgetManager.getInstance(ConfigureActivity.this), widgetID);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    private void setInitialDates() {
        Calendar currentMillis = Calendar.getInstance();
        long millis1 = currentMillis.getTimeInMillis();
        long millis2 = calendar.getTimeInMillis();

        startDate = millis1;
        endDate = millis2;
    }
}

