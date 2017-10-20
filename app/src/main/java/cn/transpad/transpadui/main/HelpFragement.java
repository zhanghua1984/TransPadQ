package cn.transpad.transpadui.main;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.util.L;

/**
 * 帮助的Fragment
 * Created by Kongxiaojun on 2015/4/7.
 */
public class HelpFragement extends BaseFragment {
    private static final String TAG = "HelpFragement";

    @InjectView(R.id.wvHelp)
    WebView wvHelp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.v(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        L.v(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        L.v(TAG, "onCreateView");
        if (mView == null) {
            mView = inflater.inflate(R.layout.activity_help, null);
            ButterKnife.inject(this, mView);
            initData();
        }
        return mView;
    }

    private void initData() {
        String url = getString(R.string.help_url);
        wvHelp.loadUrl(url);
        wvHelp.getSettings().setJavaScriptEnabled(true);
        wvHelp.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                wvHelp.loadUrl(url);
                return false;
            }
        });
    }
}
