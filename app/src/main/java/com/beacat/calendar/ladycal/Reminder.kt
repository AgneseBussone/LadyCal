package com.beacat.calendar.ladycal

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager

import androidx.core.app.NotificationCompat

import java.util.Calendar

/**
 * Broadcast receiver for showing the notification and handle the reboot of the system
 */

class Reminder : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_ID = "com.beacat.calendar.ladycal.notificationId"
        const val NOTIFICATION = "com.beacat.calendar.ladycal.notification"
        const val NOTIFICATION_ACTION = "com.beacat.calendar.ladycal.action.showNotification"
        const val NOTIFICATION_CODE_FRIENDLY = 1
        const val NOTIFICATION_CODE_START = 2
        const val NOTIFICATION_CODE_END = 3
        const val FRIENDLY_CHANNEL_ID = "friendly"
        const val START_CHANNEL_ID = "start"
        const val END_CHANNEL_ID = "end"

        @JvmStatic
        internal fun scheduleNotification(notification: Notification, dueDate: Long, type: Int, context: Context) {
            val notificationIntent = Intent(context, Reminder::class.java)
            notificationIntent.putExtra(NOTIFICATION_ID, type)
            notificationIntent.putExtra(NOTIFICATION, notification)
            notificationIntent.action = NOTIFICATION_ACTION
            val pendingIntent = PendingIntent.getBroadcast(context, type, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(AlarmManager.RTC, dueDate, pendingIntent)
        }

        @JvmStatic
        internal fun getNotification(type: Int, context: Context): Notification {
            var contentText = ""
            var channel = ""
            when (type) {
                NOTIFICATION_CODE_FRIENDLY -> {
                    contentText = "Personal event upcoming within 3 days"
                    channel = FRIENDLY_CHANNEL_ID
                }
                NOTIFICATION_CODE_START -> {
                    contentText = "Is it started? Confirm it"
                    channel = START_CHANNEL_ID
                }
                NOTIFICATION_CODE_END -> {
                    channel = END_CHANNEL_ID
                    contentText = "Last day? If so, no need to confirm"
                }
            }

            val builder = NotificationCompat.Builder(context, channel)
            builder.setSmallIcon(R.drawable.ic_notification2)
            builder.color = UtilityClass.getThemeColor(context, R.attr.colorPrimary)
            builder.setAutoCancel(true)
            val i = Intent(context, SplashActivity::class.java) // intent for opening the app when tap
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
            builder.setContentIntent(pi)
            builder.setContentTitle("LadyCal")
            builder.setContentText(contentText)
            return builder.build()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val friendlyRem = sharedPref.getBoolean(context.getString(R.string.KEY_FRIENDLY_REM), false)
        val periodRem = sharedPref.getBoolean(context.getString(R.string.KEY_PERIOD_REM), false)

        val action = intent.action

        if (action != null) {
            if (action == "android.intent.action.BOOT_COMPLETED") {
                // Called in case the device is rebooted to reschedule the friendly and period reminders.
                if (friendlyRem) {
                    val friendlyDate = sharedPref.getLong(context.getString(R.string.KEY_FRIENDLY_REM_DATE), 0)
                    if (friendlyDate != 0L && friendlyDate >= Calendar.getInstance().timeInMillis) {
                        scheduleNotification(getNotification(NOTIFICATION_CODE_FRIENDLY, context),
                                friendlyDate, NOTIFICATION_CODE_FRIENDLY, context)
                    }
                }
                if (periodRem) {
                    val periodDate = sharedPref.getLong(context.getString(R.string.KEY_PERIOD_REM_DATE), 0)
                    if (periodDate != 0L && periodDate >= Calendar.getInstance().timeInMillis) {
                        scheduleNotification(getNotification(NOTIFICATION_CODE_START, context),
                                periodDate, NOTIFICATION_CODE_START, context)
                    }
                }
            } else if (action == NOTIFICATION_ACTION) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notification = intent.getParcelableExtra<Notification>(NOTIFICATION)
                val id = intent.getIntExtra(NOTIFICATION_ID, 0)
                // show the notification if the preferences still allow that (in case the user enabled and then disabled the notifications)
                when (id) {
                    NOTIFICATION_CODE_FRIENDLY ->
                        if (friendlyRem)
                            notificationManager.notify(id, notification)
                    NOTIFICATION_CODE_START, NOTIFICATION_CODE_END ->
                        if (periodRem)
                            notificationManager.notify(id, notification)
                }
            }
        }
    }
}
