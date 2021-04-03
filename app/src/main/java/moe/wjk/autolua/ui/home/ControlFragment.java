package moe.wjk.autolua.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import moe.wjk.autolua.MyAccessibilityService;
import moe.wjk.autolua.R;
import moe.wjk.autolua.utils.Utils;

public class ControlFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button exec_code = view.findViewById(R.id.control_exec_button);
        exec_code.setOnClickListener(v -> {
            Intent reportIntent = new Intent(getContext(), MyAccessibilityService.class);
            reportIntent.setAction(MyAccessibilityService.ACTION_REPORT_QUICK);
            getContext().startService(reportIntent);
        });

        Button update_log = view.findViewById(R.id.control_update_log);
        update_log.setOnClickListener(v -> {
            updateErrorLog(view.findViewById(R.id.frag_control_textview));
        });

        Button exec_noti = view.findViewById(R.id.control_exec_notification_button);
        exec_noti.setOnClickListener(v -> {
            Intent reportIntent = new Intent(getContext(), MyAccessibilityService.class);
            reportIntent.setAction(MyAccessibilityService.ACTION_REPORT_NOTIFICATION);
            getContext().startService(reportIntent);
        });

        updateErrorLog(view.findViewById(R.id.frag_control_textview));

    }

    private void updateErrorLog(TextView tv) {
        tv.setText(Utils.readFile(getContext(), Utils.ERR_LOG_FILENAME));
    }

}