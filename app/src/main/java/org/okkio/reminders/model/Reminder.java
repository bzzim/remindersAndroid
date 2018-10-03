package org.okkio.reminders.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class Reminder implements Serializable {
    private UUID mUUID;
    private String mTitle;
    private Date mReminderDate;
    private boolean mIsDone;
    private boolean mIsDeleted;
    private static final String FIELD_ID = "uuid";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_REMINDER_DATE = "reminder_date";
    private static final String FIELD_IS_DONE = "is_done";

    public Reminder() {
        mUUID = UUID.randomUUID();
        mTitle = "";
        mReminderDate = null;
        mIsDone = false;
    }

    public Reminder(JSONObject jsonObject) throws JSONException {
        mUUID = UUID.fromString(jsonObject.getString(FIELD_ID));
        mTitle = jsonObject.getString(FIELD_TITLE);
        mIsDone = jsonObject.getBoolean(FIELD_IS_DONE);
        if (jsonObject.has(FIELD_REMINDER_DATE)) {
            mReminderDate = new Date(jsonObject.getLong(FIELD_REMINDER_DATE));
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(FIELD_TITLE, mTitle);
        jsonObject.put(FIELD_IS_DONE, mIsDone);
        if (mReminderDate != null) {
            jsonObject.put(FIELD_REMINDER_DATE, mReminderDate.getTime());
        }
        jsonObject.put(FIELD_ID, mUUID.toString());

        return jsonObject;
    }

    public UUID getIdentifier() {
        return mUUID;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mToDoText) {
        this.mTitle = mToDoText;
    }

    public Date getReminderDate() {
        return mReminderDate;
    }

    public void setReminderDate(Date mToDoDate) {
        this.mReminderDate = mToDoDate;
    }

    public boolean isDone() {
        return mIsDone;
    }

    public void setDone(boolean done) {
        mIsDone = done;
    }

    public boolean isDeleted() {
        return mIsDeleted;
    }

    public void setDeleted(boolean deleted) {
        mIsDeleted = deleted;
    }
}
