package moe.wjk.autolua.ui.home.code;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import moe.wjk.autolua.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CodeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CodeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CodeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CodeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CodeFragment newInstance() {
        CodeFragment fragment = new CodeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_code, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button button_save = view.findViewById(R.id.button_save);
        button_save.setOnClickListener(v -> {
            ((CodeWebView)view.findViewById(R.id.code_web_view)).saveCurrentCode();
        });
    }
}