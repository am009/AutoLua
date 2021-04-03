package moe.wjk.autolua.settings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Calendar;

import moe.wjk.autolua.MyAccessibilityService;

public class EnableSchedulePreference extends SwitchPreferenceCompat implements Preference.OnPreferenceChangeListener {

    public EnableSchedulePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public EnableSchedulePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public EnableSchedulePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EnableSchedulePreference(Context context) {
        super(context);
        init();
    }

    void init() {
        setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean nv = (boolean) newValue;
        if (nv) {
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getContext() /* Activity context */);
            int minutesAfterMidnight = sharedPreferences.getInt("scheduled_time", -1);
            if (minutesAfterMidnight == -1) {
                Toast.makeText(getContext(), "Unable to get time.", Toast.LENGTH_SHORT).show();
                return false;
            }
            // Set the alarm
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int hours = minutesAfterMidnight / 60;
            int minutes = minutesAfterMidnight % 60;
            calendar.set(Calendar.HOUR_OF_DAY, hours);
            calendar.set(Calendar.MINUTE, minutes);

            setAlarm(calendar);
        } else {
            if (cancelAlarm()) {
                Toast.makeText(getContext(), "Canceled.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Cancel failed.", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return true;
    }

    private void setAlarm(Calendar calendar) {
        AlarmManager alarmMgr;
        PendingIntent alarmIntent;
        Context context = getContext();

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MyAccessibilityService.class);
        intent.setAction(MyAccessibilityService.ACTION_REPORT_NOTIFICATION);
        alarmIntent = PendingIntent.getService(context, 0, intent, 0);

        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
//        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
//                1000 * 60 * 20, alarmIntent);
    }

    private boolean cancelAlarm() {
        Context context = getContext();
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MyAccessibilityService.class);
        intent.setAction(MyAccessibilityService.ACTION_REPORT_NOTIFICATION);
        PendingIntent pendingIntent =
                PendingIntent.getService(context, 0, intent,
                        PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            return true;
        } else {
            return false;
        }
    }

}
