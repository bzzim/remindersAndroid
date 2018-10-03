package org.okkio.reminders.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.format.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.okkio.reminders.R;
import org.okkio.reminders.model.Reminder;
import org.okkio.reminders.service.NotificationService;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.text.format.Time;

public class AppHelper {
    public static final String SHARED_PREF_DATA_SET_CHANGED = "org.okkio.reminders.dataSetChanged";
    public static final String CHANGE_OCCURED = "org.okkio.reminders.changeOccured";
    private static final String FILE_NAME = "data.json";

    private Context mContext;
    private AlarmManager mAlarmManager;

    public AppHelper(Context context) {
        mContext = context;
        mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    public static String formatDate(String formatString, Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatString, Locale.ENGLISH);
        return simpleDateFormat.format(date);
    }

    //region Date
    public String getRelativeTimeSpan(long time) {
        long now = System.currentTimeMillis();
        long duration = Math.abs(now - time);
        Resources r = mContext.getResources();
        if (duration < DateUtils.DAY_IN_MILLIS) {
            return getRelativeDay(r, time, now);
        }
        return DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT).format(time);
    }

    private String getRelativeDay(Resources r, long day, long today) {
        String dateString = DateFormat.getTimeInstance(DateFormat.SHORT).format(day);

        Time startTime = new Time();
        startTime.set(day);
        int startDay = Time.getJulianDay(day, startTime.gmtoff);

        Time currentTime = new Time();
        currentTime.set(today);
        int currentDay = Time.getJulianDay(today, currentTime.gmtoff);

        int days = Math.abs(currentDay - startDay);
        boolean past = (today > day);

        if (days == 1) {
            return past ?
                    String.format(r.getString(R.string.yesterday_in), dateString) :
                    String.format(r.getString(R.string.tomorrow_in), dateString);
        } else if (days == 0) {
            return String.format(r.getString(R.string.today_in), dateString);
        }

        int resId = past ? R.plurals.num_days_ago : R.plurals.in_num_days;

        String format = r.getQuantityString(resId, days);
        return String.format(format, days);
    }
    //endregion

    //region Files
    public boolean saveReminderList(ArrayList<Reminder> items) {
        FileOutputStream fileOutputStream;
        OutputStreamWriter outputStreamWriter;
        JSONArray jsonArray = new JSONArray();
        try {
            fileOutputStream = mContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            for (Reminder item : items) {
                JSONObject jsonObject = item.toJSON();
                jsonArray.put(jsonObject);
            }
            outputStreamWriter.write(jsonArray.toString());
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public ArrayList<Reminder> loadReminderList() {
        ArrayList<Reminder> reminderList = new ArrayList<>();
        BufferedReader bufferedReader;
        FileInputStream fileInputStream;

        try {
            fileInputStream = mContext.openFileInput(FILE_NAME);
            StringBuilder builder = new StringBuilder();
            String line;
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            JSONArray jsonArray = (JSONArray) new JSONTokener(builder.toString()).nextValue();
            for (int i = 0; i < jsonArray.length(); i++) {
                Reminder item = new Reminder(jsonArray.getJSONObject(i));
                reminderList.add(item);
            }

            bufferedReader.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            //do nothing about it
            //file won't exist first time app is run
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return reminderList;
        }

        return reminderList;
    }
    //endregion

    //region Alarms
    public void toggleReminderAlarm(Reminder reminder) {
        long now = new Date().getTime();
        Intent i = new Intent(mContext, NotificationService.class);
        // in any case, first delete the alarm
        deleteAlarm(i, reminder.getIdentifier().hashCode());
        if (!reminder.isDone() && reminder.getReminderDate() != null && reminder.getReminderDate().getTime() > now) {
            deleteAlarm(i, reminder.getIdentifier().hashCode());
            i.putExtra(NotificationService.EXTRA_REMINDER_TITLE, reminder.getTitle());
            i.putExtra(NotificationService.EXTRA_REMINDER_UUID, reminder.getIdentifier());
            createAlarm(i, reminder.getIdentifier().hashCode(), reminder.getReminderDate().getTime());
        }
    }

    public void createAlarm(Intent i, int requestCode, long timeInMillis) {
        PendingIntent pi = PendingIntent.getService(mContext, requestCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
    }

    public void deleteAlarm(Intent i, int requestCode) {
        if (isPendingIntentExist(i, requestCode)) {
            PendingIntent pi = PendingIntent.getService(mContext, requestCode, i, PendingIntent.FLAG_NO_CREATE);
            pi.cancel();
            mAlarmManager.cancel(pi);
        }
    }

    private boolean isPendingIntentExist(Intent i, int requestCode) {
        PendingIntent pi = PendingIntent.getService(mContext, requestCode, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }
    //endregion
}
