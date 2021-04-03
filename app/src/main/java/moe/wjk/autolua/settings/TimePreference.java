package moe.wjk.autolua.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import androidx.preference.DialogPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import java.util.Locale;

import moe.wjk.autolua.R;

public class TimePreference extends DialogPreference {
    private int mTime = 0;
    private final int mDialogLayoutResId = R.layout.pref_dialog_time;

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        setSummaryProvider(TimePreference.SimpleSummeryProvider.getInstance());
    }

    public TimePreference(Context context) {
        super(context);
    }

    public int getTime() {
        return mTime;
    }

    public void setTime(int time) {
        mTime = time;
        // Save to Shared Preferences
        persistInt(time);
        notifyChanged();
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        // Read the value. Use the default value if it is not possible.
        setTime(getPersistedInt(mTime));
    }

    @Override
    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }

    public static final class SimpleSummeryProvider implements SummaryProvider<TimePreference> {
        private static TimePreference.SimpleSummeryProvider sSimpleSummaryProvider;

        @Override
        public CharSequence provideSummary(TimePreference preference) {
            int minutesAfterMidnight = preference.getTime();
            int hours = minutesAfterMidnight / 60;
            int minutes = minutesAfterMidnight % 60;
            boolean is24hour = DateFormat.is24HourFormat(preference.getContext());

            if (is24hour) {
                String tail;
                if (hours > 12) {
                    tail = "PM";
                    hours -= 12;
                } else {
                    tail = "AM";
                }
                return String.format(Locale.getDefault(), "%02d:%02d ",hours,minutes) + tail;
            } else {
                return String.format(Locale.getDefault(), "%02d:%02d",hours,minutes);
            }
        }

        public static TimePreference.SimpleSummeryProvider getInstance() {
            if (sSimpleSummaryProvider == null) {
                sSimpleSummaryProvider = new TimePreference.SimpleSummeryProvider();
            }
            return sSimpleSummaryProvider;
        }


    }


}
