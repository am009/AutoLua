package moe.wjk.autolua.ui.home.code;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import moe.wjk.autolua.R;
import moe.wjk.autolua.utils.Utils;

public class CodeWebView extends WebView {
    private String filename = Utils.CODE_FILENAME;
    private String code;
    private static final String TAG = "CodeWebView";

    public CodeWebView(Context context) {
        super(context);
        initView(context);
    }

    public CodeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        handleAttrs(context, attrs);
        initView(context);
    }

    public CodeWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        handleAttrs(context, attrs);
        initView(context);
    }

    private void handleAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CodeWebView,
                0, 0);
        try {
            String name = a.getString(R.styleable.CodeWebView_codeFileName);
            if (name != null && !name.isEmpty()) {
                filename = name;
            }
        } finally {
            a.recycle();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView(Context context) {
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setUseWideViewPort(true);
        this.getSettings().setLoadWithOverviewMode(true);
        setWebContentsDebuggingEnabled(true);

        // 跳转本地assets文件
        loadUrl("file:///android_asset/editor.html");
        // 读取文件到buffer
        code = Utils.readFile(context, filename);
//        Log.d(TAG, "initView: code: " + code);
        if (code != null) {
            // base64 编码，防止双引号等字符需要转义的问题。
            String encoded = Base64.getEncoder().encodeToString((code).getBytes());
            String js = "updateCode(\"" + encoded + "\")";
            setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    evaluateJavascript(js, null);
                }
            });
//            Log.d(TAG, "initView: js: " + js);
        }
    }

    public void saveCurrentCode() {
        // 更新当前代码。
        evaluateJavascript("getCode()", (str) -> {
            str = str.substring(1, str.length()-1);
            byte[] coded = Base64.getDecoder().decode(str.getBytes(StandardCharsets.UTF_8));
//            Log.d(TAG, "saveCurrentCode: " + new String(coded));
            if (Utils.saveFile(getContext(), filename, coded)) {
                Toast.makeText(getContext(), "Saved.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}