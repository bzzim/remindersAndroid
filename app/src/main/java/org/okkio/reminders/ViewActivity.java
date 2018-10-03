package org.okkio.reminders;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.okkio.reminders.model.Reminder;
import org.okkio.reminders.service.NotificationService;
import org.okkio.reminders.util.AppHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class ViewActivity extends AppCompatActivity {
    private AppHelper mAppHelper;
    private ArrayList<Reminder> mReminderArrayList;
    private Reminder mReminder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        mAppHelper = new AppHelper(this);
        mReminderArrayList = mAppHelper.loadReminderList();

        Intent i = getIntent();
        UUID id = (UUID) i.getSerializableExtra(NotificationService.EXTRA_REMINDER_UUID);
        mReminder = null;
        for (Reminder item : mReminderArrayList) {
            if (item.getIdentifier().equals(id)) {
                mReminder = item;
                break;
            }
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView titleView = findViewById(R.id.title);
        Button doneButtonView = findViewById(R.id.done_button);

        titleView.setText(mReminder.getTitle());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        doneButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReminder.setDone(true);
                mAppHelper.saveReminderList(mReminderArrayList);
                finish();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reminder_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            mReminderArrayList.remove(mReminder);
            mAppHelper.saveReminderList(mReminderArrayList);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
