package org.okkio.reminders.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import org.okkio.reminders.R;
import org.okkio.reminders.ViewActivity;

import java.util.List;
import java.util.UUID;

public class NotificationService extends IntentService {
    public static final String EXTRA_REMINDER_TITLE = "org.okkio.NotificationServiceTitle";
    public static final String EXTRA_REMINDER_UUID = "org.okkio.NotificationServiceUUID";
    public static final String NOTIFICATION_CHANNEL_ID = "default_channel";
    public static final String NOTIFICATION_CHANNEL_NAME = "Default Channel";
    public static final String NOTIFICATION_CHANNEL_GROUP_ID = "common_group";
    public static final String NOTIFICATION_CHANNEL_GROUP_NAME = "Common Group";

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String reminderTitle = intent.getStringExtra(EXTRA_REMINDER_TITLE);
        UUID reminderUUID = (UUID) intent.getSerializableExtra(EXTRA_REMINDER_UUID);

        Intent i = new Intent(this, ViewActivity.class);
        i.putExtra(NotificationService.EXTRA_REMINDER_UUID, reminderUUID);
        Notification.Builder builder = new Notification.Builder(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(getChannelId(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NOTIFICATION_CHANNEL_GROUP_ID,
                    NOTIFICATION_CHANNEL_GROUP_NAME
            ));
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setColor(getColor(R.color.indigo));
        }
        int defaults = 0;
        defaults |= Notification.DEFAULT_VIBRATE;
        defaults |= Notification.DEFAULT_SOUND;

        builder.setContentTitle(reminderTitle)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setSmallIcon(R.drawable.ic_check_mark_white)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo))
                .setAutoCancel(true)
                .setDefaults(defaults)
                .setContentIntent(PendingIntent.getActivity(this, reminderUUID.hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT));

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(100, builder.build());
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    String getChannelId(String channelId, String name, String groupId, String groupName) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) {
            return null;
        }
        List<NotificationChannel> channels = nm.getNotificationChannels();
        for (NotificationChannel channel : channels) {
            if (channel.getId().equals(channelId)) {
                return channel.getId();
            }
        }

        String group = getNotificationChannelGroupId(groupId, groupName);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel notificationChannel = new NotificationChannel(channelId, name, importance);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);

        notificationChannel.setGroup(group); // set custom group

        notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        nm.createNotificationChannel(notificationChannel);

        return channelId;
    }

    @TargetApi(Build.VERSION_CODES.O)
    String getNotificationChannelGroupId(String groupId, String name) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) {
            return null;
        }
        List<NotificationChannelGroup> groups = nm.getNotificationChannelGroups();
        for (NotificationChannelGroup group : groups) {
            if (group.getId().equals(groupId)) {
                return group.getId();
            }
        }
        nm.createNotificationChannelGroup(new NotificationChannelGroup(groupId, name));
        return groupId;
    }
}
