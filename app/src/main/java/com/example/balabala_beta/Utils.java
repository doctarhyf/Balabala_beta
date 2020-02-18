package com.example.balabala_beta;

import android.content.Context;

import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {
    public static float FOLLOW_ME_ZOOM_LEVEL = 18;

    public static String GR_GS(Context contextCompat, int str_id) {

        return contextCompat.getString(str_id);
    }



    public static String FormatMillisToDateTime(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.

        if(dateFormat == null) dateFormat = "dd/MM/yyyy hh:mm:ss.SSS";

        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
