package moe.wjk.autolua.ui.doc;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import moe.wjk.autolua.R;

/**
 * A Fragment containing WebView to display tutorials.
 * Navigate here from {@link DocListFragment}
 * Use the {@link DocWebViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DocWebViewFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_DOC_URL = "doc_url";

    private String doc_url;

    public DocWebViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param doc_url Document url to exported html file. e.g. file:///android_asset/???.html
     * @return A new instance of fragment DocWebViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DocWebViewFragment newInstance(String doc_url) {
        DocWebViewFragment fragment = new DocWebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DOC_URL, doc_url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            doc_url = getArguments().getString(ARG_DOC_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_doc_web_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        WebView wv = view.findViewById(R.id.doc_web_view);
        wv.getSettings().setUseWideViewPort(true);
        wv.getSettings().setLoadWithOverviewMode(true);
        //
        wv.loadUrl(doc_url);

    }
}