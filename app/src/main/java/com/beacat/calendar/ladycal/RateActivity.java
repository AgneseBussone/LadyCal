package com.beacat.calendar.ladycal;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import java.util.List;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.beacat.calendar.ladycal.R.string.KEY_THEME;
import static com.beacat.calendar.ladycal.R.style.AppTheme;

/**
 * Activity that handles rating.
 */

public class RateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        if(i != null){
            setTheme(i.getIntExtra(getString(KEY_THEME), AppTheme));
        }
        setContentView(R.layout.rate_layout_base);

        // hide the actionbar
        getSupportActionBar().hide();
    }

    public void happyClick(View view) {
        setContentView(R.layout.rate_layout_happy);
        setRateCounting();
    }

    public void rateClick(View view) {
        // you can also use BuildConfig.APPLICATION_ID
        String appId = getPackageName();
        Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appId));
        boolean marketFound = false;

        // find all applications able to handle our rateIntent
        final List<ResolveInfo> otherApps = getPackageManager().queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp: otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName
                    .equals("com.android.vending")) {

                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                );
                // make sure it does NOT open in the stack of your activity
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // task reparenting if needed
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                // if the Google Play was already open in a search result
                //  this make sure it still go to the app page you requested
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                // this make sure only the Google Play app is allowed to
                // intercept the intent
                rateIntent.setComponent(componentName);
                startActivity(rateIntent);
                marketFound = true;
                break;

            }
        }

        // if GP not present on device, open web browser
        if (!marketFound) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id="+appId));
            startActivity(webIntent);
        }
    }

    public void closeClick(View view) {
        this.finish();
    }

    public void feedbackClick(View view) {
        // send an email to the developer
        String data = "Device info:" +
                "Model: " + Build.MODEL + "\n" +
                "Manufacturer: " + Build.MANUFACTURER+ "\n" +
                "Brand: " + Build.BRAND + "\n" +
                "Android: " + Build.VERSION.RELEASE + "(skd " + Build.VERSION.SDK_INT + ")\n" +
                "App version code: " + BuildConfig.VERSION_CODE + "\n" +
                "App version name: " + BuildConfig.VERSION_NAME + "\n\n";
        String uriText =
                "mailto:agnesebussone+appsupport@gmail.com" +
                        "?subject=" + Uri.encode("LadyCal feedback") +
                        "&body=" + Uri.encode(data);

        Uri uri = Uri.parse(uriText);

        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
        sendIntent.setData(uri);
        startActivity(Intent.createChooser(sendIntent, "Send email with"));
    }

    public void sadClick(View view) {
        setContentView(R.layout.rate_layout_sad);
        setRateCounting();
    }

    // If the user chose to go on with this, don't ask for rate anymore
    private void setRateCounting(){
        SharedPreferences sp = getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(getString(R.string.KEY_ASK_FOR_RATE), getResources().getInteger(R.integer.askForRate_null));
        editor.apply();
    }
}
