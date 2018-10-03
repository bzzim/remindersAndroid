package org.okkio.reminders;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import org.okkio.reminders.model.Reminder;
import org.okkio.reminders.util.AppHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class FormActivity extends AppCompatActivity {
    public static final String EXTRA_REMINDER_ITEM = "org.okkio.reminders.FormActivity.ReminderItem";
    private Reminder mReminder;
    private EditText mTitleView;
    private View mDateDeleteView;
    private View mDateSelectView;
    private TextView mSelectedDateView;
    private Calendar mCalendar;
    private AppHelper mAppHelper;

    public static Intent newIntent(Context packageContext, Reminder reminder) {
        Intent intent = new Intent(packageContext, FormActivity.class);
        intent.putExtra(EXTRA_REMINDER_ITEM, reminder);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        mAppHelper = new AppHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        View dateSelectorView = findViewById(R.id.date_selector);
        Button saveButtonView = findViewById(R.id.save_button);
        mTitleView = findViewById(R.id.title);
        mSelectedDateView = findViewById(R.id.selected_date);
        mDateDeleteView = findViewById(R.id.date_delete);
        mDateSelectView = findViewById(R.id.date_select);

        mReminder = (Reminder) getIntent().getSerializableExtra(EXTRA_REMINDER_ITEM);
        mTitleView.setText(mReminder.getTitle());
        mTitleView.setSelection(mTitleView.getText().length()); // set cursor at the last pos
        if (mReminder.getReminderDate() != null) {
            mSelectedDateView.setText(mAppHelper.getRelativeTimeSpan(mReminder.getReminderDate().getTime()));
            toggleAlarmView(true);
        }

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mCalendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        mCalendar.setTime(new Date());   // assigns calendar to given date
        mCalendar.add(Calendar.HOUR, 1);

        dateSelectorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateAndTimePicker();
            }
        });

        mDateDeleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReminder.setReminderDate(null);
                toggleAlarmView(false);
            }
        });

        saveButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FormActivity.this, MainActivity.class);
                mReminder.setTitle(mTitleView.getText().toString());
                i.putExtra(EXTRA_REMINDER_ITEM, mReminder);
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mReminder.getTitle().length() > 0) {
            getMenuInflater().inflate(R.menu.menu_reminder_form, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            Intent i = new Intent(this, MainActivity.class);
            mReminder.setDeleted(true);
            i.putExtra(EXTRA_REMINDER_ITEM, mReminder);
            setResult(RESULT_OK, i);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //region Calendar
    private void showDateAndTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // hm, in api 19 this called twice
                if (view.isShown()) {
                    mCalendar.set(year, month, dayOfMonth);
                    showTimePicker();
                }
            }
        }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(new Date().getTime() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        boolean is24HourFormat = DateFormat.is24HourFormat(this);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mCalendar.set(Calendar.MINUTE, minute);
                mReminder.setReminderDate(mCalendar.getTime());
                mSelectedDateView.setText(mAppHelper.getRelativeTimeSpan(mCalendar.getTimeInMillis()));
                toggleAlarmView(true);
            }
        }, is24HourFormat ? mCalendar.get(Calendar.HOUR_OF_DAY) : mCalendar.get(Calendar.HOUR), mCalendar.get(Calendar.MINUTE), is24HourFormat);
        timePickerDialog.show();
    }
    //endregion

    private void toggleAlarmView(boolean show) {
        mSelectedDateView.setVisibility(show ? View.VISIBLE : View.GONE);
        mDateSelectView.setVisibility(show ? View.GONE : View.VISIBLE);
        mDateDeleteView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
