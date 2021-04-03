package moe.wjk.autolua.settings;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import moe.wjk.autolua.R;

public class DeleteLogDialogPreference extends DialogPreference {

    public DeleteLogDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public DeleteLogDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DeleteLogDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DeleteLogDialogPreference(Context context) {
        super(context);
        init();
    }

    public void init() {
        setDialogTitle(R.string.dialog_clean_log_title);
        setDialogMessage(R.string.dialog_clean_log_message);
    }


}
