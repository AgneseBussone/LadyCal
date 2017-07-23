package com.beacat.calendar.ladycal;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

/**
 * Broadcast receiver for showing the notification and handle the reboot of the system
 */

public class Reminder extends BroadcastReceiver {

    public static final String NOTIFICATION_ID = "com.beacat.calendar.ladycal.notificationId";
    public static final String NOTIFICATION = "com.beacat.calendar.ladycal.notification";
    public static final String NOTIFICATION_ACTION = "com.beacat.calendar.ladycal.action.showNotification";
    public static final int NOTIFICATION_CODE_FRIENDLY = 1;
    public static final int NOTIFICATION_CODE_START = 2;
    public static final int NOTIFICATION_CODE_END = 3;


    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean friendlyRem = sharedPref.getBoolean(context.getString(R.string.KEY_FRIENDLY_REM), false);
        boolean periodRem = sharedPref.getBoolean(context.getString(R.string.KEY_PERIOD_REM), false);

        String action = intent.getAction();

        if(action != null) {
            if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                // Called in case the device is rebooted to reschedule the friendly and period reminders.
                if (friendlyRem) {
                    long friendlyDate = sharedPref.getLong(context.getString(R.string.KEY_FRIENDLY_REM_DATE), 0);
                    if (friendlyDate != 0 && friendlyDate >= Calendar.getInstance().getTimeInMillis()) {
                        scheduleNotification(getNotification(NOTIFICATION_CODE_FRIENDLY, context),
                                friendlyDate, NOTIFICATION_CODE_FRIENDLY, context);
                    }
                }
                if (periodRem) {
                    long periodDate = sharedPref.getLong(context.getString(R.string.KEY_PERIOD_REM_DATE), 0);
                    if (periodDate != 0 && periodDate >= Calendar.getInstance().getTimeInMillis()) {
                        scheduleNotification(getNotification(NOTIFICATION_CODE_START, context),
                                periodDate, NOTIFICATION_CODE_START, context);
                    }
                }
            }
            else if(action.equals(NOTIFICATION_ACTION)) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notification = intent.getParcelableExtra(NOTIFICATION);
                int id = intent.getIntExtra(NOTIFICATION_ID, 0);
                // show the notification if the preferences still allow that (in case the user enabled and then disabled the notifications)
                switch(id){
                    case NOTIFICATION_CODE_FRIENDLY:
                        if(friendlyRem)
                            notificationManager.notify(id, notification);
                        break;
                    case NOTIFICATION_CODE_START:
                    case NOTIFICATION_CODE_END:
                        if(periodRem)
                            notificationManager.notify(id, notification);
                        break;
                }
            }
        }
    }

    protected static void scheduleNotification(Notification notification, long dueDate, int type, Context context) {
        Intent notificationIntent = new Intent(context, Reminder.class);
        notificationIntent.putExtra(Reminder.NOTIFICATION_ID, type);
        notificationIntent.putExtra(Reminder.NOTIFICATION, notification);
        notificationIntent.setAction(Reminder.NOTIFICATION_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, type, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, dueDate, pendingIntent);
    }

    protected static Notification getNotification(int type, Context context) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_notification2);
        builder.setColor(context.getResources().getColor(UtilityClass.getThemeColor(context, R.attr.colorPrimary)));
        builder.setAutoCancel(true);
        Intent i = new Intent(context, SplashActivity.class); // intent for opening the app when tap
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);
        builder.setContentTitle("LadyCal");
        switch(type) {
            case NOTIFICATION_CODE_FRIENDLY:
                builder.setContentText("Personal event upcoming within 3 days");
                break;
            case NOTIFICATION_CODE_START:
                builder.setContentText("Is it started? Confirm it");
                break;
            case NOTIFICATION_CODE_END:
                builder.setContentText("Last day? If so, no need to confirm");
            }
        return builder.build();
    }
}
