package moe.wjk.autolua.settings;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceDialogFragmentCompat;

import java.io.IOException;

import moe.wjk.autolua.R;
import moe.wjk.autolua.utils.Utils;

public class DeleteLogDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    private static final String TAG = "DeleteLogDialogFragment";

    public static DeleteLogDialogFragmentCompat newInstance(
            String key) {
        final DeleteLogDialogFragmentCompat
                fragment = new DeleteLogDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            try {
                if (!Utils.emptyFile(getContext(), Utils.ERR_LOG_FILENAME)) {
                    Toast.makeText(getContext(), getText(R.string.dialog_clean_log_fail), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), getText(R.string.dialog_clean_log_success), Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Toast.makeText(getContext(), getText(R.string.dialog_clean_log_fail), Toast.LENGTH_LONG).show();
                Log.e(TAG, "onDialogClosed: ", e);
            }

        }
    }
}