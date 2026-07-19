package com.beacat.calendar.ladycal

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.view.View

/**
 * Activity that handles rating.
 */

class RateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val i = intent
        if (i != null) {
            setTheme(i.getIntExtra(getString(R.string.KEY_THEME), R.style.AppTheme))
        }
        setContentView(R.layout.rate_layout_base)

        // hide the actionbar
        supportActionBar!!.hide()
    }

    fun happyClick(view: View) {
        setContentView(R.layout.rate_layout_happy)
        setRateCounting()
    }

    fun rateClick(view: View) {
        // you can also use BuildConfig.APPLICATION_ID
        val appId = packageName
        val rateIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appId))
        var marketFound = false

        // find all applications able to handle our rateIntent
        val otherApps = packageManager.queryIntentActivities(rateIntent, 0)
        for (otherApp in otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName == "com.android.vending") {

                val otherAppActivity = otherApp.activityInfo
                val componentName = ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                )
                // make sure it does NOT open in the stack of your activity
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                // task reparenting if needed
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

                // if the Google Play was already open in a search result
                //  this make sure it still go to the app page you requested
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                // this make sure only the Google Play app is allowed to
                // intercept the intent
                rateIntent.component = componentName
                startActivity(rateIntent)
                marketFound = true
                break
            }
        }

        // if GP not present on device, open web browser
        if (!marketFound) {
            val webIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + appId))
            startActivity(webIntent)
        }
    }

    fun closeClick(view: View) {
        this.finish()
    }

    fun feedbackClick(view: View) {
        // send an email to the developer
        val data = "Device info:" +
                "Model: " + Build.MODEL + "\n" +
                "Manufacturer: " + Build.MANUFACTURER + "\n" +
                "Brand: " + Build.BRAND + "\n" +
                "Android: " + Build.VERSION.RELEASE + "(skd " + Build.VERSION.SDK_INT + ")\n" +
                "App version code: " + BuildConfig.VERSION_CODE + "\n" +
                "App version name: " + BuildConfig.VERSION_NAME + "\n\n"
        val uriText =
                "mailto:agnesebussone+appsupport@gmail.com" +
                        "?subject=" + Uri.encode("LadyCal feedback") +
                        "&body=" + Uri.encode(data)

        val uri = Uri.parse(uriText)

        val sendIntent = Intent(Intent.ACTION_SENDTO)
        sendIntent.data = uri
        startActivity(Intent.createChooser(sendIntent, "Send email with"))
    }

    fun sadClick(view: View) {
        setContentView(R.layout.rate_layout_sad)
        setRateCounting()
    }

    // If the user chose to go on with this, don't ask for rate anymore
    private fun setRateCounting() {
        val sp = getDefaultSharedPreferences(applicationContext)
        val editor = sp.edit()
        editor.putInt(getString(R.string.KEY_ASK_FOR_RATE), resources.getInteger(R.integer.askForRate_null))
        editor.apply()
    }
}
