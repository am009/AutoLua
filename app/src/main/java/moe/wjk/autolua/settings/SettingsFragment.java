package moe.wjk.autolua.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import moe.wjk.autolua.MyAccessibilityService;
import moe.wjk.autolua.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Preference enable_action_bar = findPreference("enable_action_bar");
        enable_action_bar.setOnPreferenceChangeListener((pref, newValue) -> {
            boolean nv = (boolean) newValue;
            String action;
            if (nv) {
                action = MyAccessibilityService.ACTION_SHOW_ACTION_BAR;
            } else {
                action = MyAccessibilityService.ACTION_HIDE_ACTION_BAR;
            }

            Intent reportIntent = new Intent(getContext(), MyAccessibilityService.class);
            reportIntent.setAction(action);
            getContext().startService(reportIntent);

            return true;
        });
    }

    // 照顾TimePreference
    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // Try if the preference is one of our custom Preferences
        DialogFragment dialogFragment = null;
        if (preference instanceof TimePreference) {
            // Create a new instance of TimePreferenceDialogFragment with the key of the related
            // Preference
            dialogFragment = TimePreferenceDialogFragmentCompat
                    .newInstance(preference.getKey());
        }

        if (preference instanceof DeleteLogDialogPreference) {
            dialogFragment = new DeleteLogDialogFragmentCompat().newInstance(preference.getKey());
        }

        // If it was one of our cutom Preferences, show its dialog
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getParentFragmentManager(),
                    "android.support.v7.preference" +
                            ".PreferenceFragment.DIALOG");
        }
        // Could not be handled here. Try with the super method.
        else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

}