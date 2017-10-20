package cn.transpad.transpadui.main;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fone.player.L;
import com.umeng.analytics.MobclickAgent;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.service.TransPadService;
import cn.transpad.transpadui.util.ScreenUtil;
import cn.transpad.transpadui.util.TPUtil;
import de.greenrobot.event.EventBus;

/**
 * TP浏览器
 *
 * @author kongxiaojun
 * @since 2015-4-14
 */
public class TPBrowserActivity extends Activity {

    public static final String URL = "browser_url";

    public static final String LANDSCAPE = "landscape";

    public static final String DEFAULT_URL = "http://www.100tv.com/webNavigation/webNav_n.html";

    private static final String TAG = "TPBrowserActivity";


    @InjectView(R.id.tp_webview)
    WebView webView;

    @InjectView(R.id.loading_progress)
    ProgressBar loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        ButterKnife.inject(this);
        initWebview();
        handleIntent(getIntent());
        EventBus.getDefault().register(this);
    }

    private void handleIntent(Intent intent) {
        String url = intent.getStringExtra(URL);
        if (TextUtils.isEmpty(url)) {
            //取DATA中的URL
            Uri uriPath = intent.getData();
            try {
                if (null != uriPath) {
                    String scheme = uriPath.getScheme();
                    if (null != scheme) {
                        url = URLDecoder.decode(uriPath.toString(), "utf-8");
                    } else {
                        url = URLDecoder.decode(uriPath.getPath(), "utf-8");
                    }
                    if (!TextUtils.isEmpty(url) && url.startsWith("transpad://")) {
                        url = url.replace("transpad://", "http://");
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (TextUtils.isEmpty(url)) {
            url = DEFAULT_URL;
        }
        boolean land = intent.getBooleanExtra(LANDSCAPE, true);
        setRequestedOrientation(land ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        loadUrl(url);
    }

    public void initWebview() {
        //WebView.enablePlatformNotifications();
        setWebView();
        // setting.setLoadWithOverviewMode(true);
        WebViewClient mWebClient = new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                L.v(TAG, "shouldOverrideUrlLoading beg url:", url);
                // toolbarLayout.setVisibility(View.VISIBLE);
                if (!TPUtil.isNetOk()) {
//                    Toast.makeText(TPBrowserActivity.this, TPBrowserActivity.this.getResources().getText(R.string.network_error).toString(), Toast.LENGTH_SHORT).show();
                    L.v(TAG, "shouldOverrideUrlLoading", "no net");
                } else {
                    if (url != null) {
                        loadUrl(url);
                    }
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (loadingProgress != null) {
                    loadingProgress.setVisibility(View.VISIBLE);
                }
                if (view != null) {
                    view.getSettings().setBlockNetworkImage(true);
                }
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                L.v(TAG, "onPageFinished end url:", url);
                loadingProgress.setVisibility(View.GONE);
                if (webView != null) {
                    webView.getSettings().setBlockNetworkImage(false);
                }
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                L.v(TAG, "onReceivedError description:", description);
                L.v(TAG, "onReceivedError errorCode:", "" + errorCode);
                L.v(TAG, "onReceivedError end url:", failingUrl);
                loadingProgress.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        };

        webView.setWebViewClient(mWebClient);
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                return true;
            }

            @Override
            public boolean onJsTimeout() {
                L.v(TAG, "onJsTimeout", "onJsTimeout");
                return super.onJsTimeout();
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // if(newProgress > 70 && (webView.getVisibility() ==
                // View.INVISIBLE) ){
                if (newProgress > 90) {
                    // webView.setVisibility(View.VISIBLE);
                    // loading.setVisibility(View.INVISIBLE);
                }
                // loadingText.setText(newProgress + "%");
            }

        });

        webView.setDownloadListener(new MyWebViewDownLoadListener());
    }

    public void loadUrl(String url) {
        L.v(TAG, "loadURL URL: ", url);
        if (url != null) {
            if (url.toLowerCase().startsWith("http")) {
                webView.stopLoading();
                webView.invalidate();
                setWebView();
                webView.loadUrl(url);
//            }else {
//                try{
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.setData(Uri.parse(url));
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    mContext.startActivity(intent);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
            }
        } else {
            Toast.makeText(this, TPBrowserActivity.this.getText(R.string.webview_url_is_null), Toast.LENGTH_SHORT).show();
        }
        L.v(TAG, "loadUrl", url);
    }

    @OnClick(R.id.back)
    public void goBack() {
        L.v(TAG, "webViewGoBack", "click");
        if (!webView.canGoBack()) {
            finish();
        } else {
            webView.goBack();
        }
    }

    @OnClick(R.id.forward)
    public void goForward() {
        L.v(TAG, "webViewGoForward", "click");
        if (webView.canGoForward()) {
            webView.goForward();
        }
    }

    @OnClick(R.id.refresh)
    public void reLoad() {
        L.v(TAG, "webViewRefush", "click");
        webView.reload();
    }

    @OnClick(R.id.exit_browser)
    public void exit() {
        finish();
    }

    private void setWebView() {
        if (ScreenUtil.getScreenWidthPix(this) <= 480) {
            webView.setInitialScale(50);
        } else {
            webView.setInitialScale(75);
        }
        WebSettings setting = webView.getSettings();
        setting.setUserAgentString(TPUtil.getUA());
        setting.setAllowFileAccess(true);
        setting.setJavaScriptEnabled(true);
        setting.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        setting.setSupportZoom(true);
        setting.setBuiltInZoomControls(false);
        setting.setUseWideViewPort(true);
        setting.setUseWideViewPort(true);
        setting.setLoadWithOverviewMode(true);
        int screenDensity = getResources().getDisplayMetrics().densityDpi;
        WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.MEDIUM;
        switch (screenDensity) {
            case DisplayMetrics.DENSITY_LOW:
                zoomDensity = WebSettings.ZoomDensity.CLOSE;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                zoomDensity = WebSettings.ZoomDensity.MEDIUM;
                break;
            case DisplayMetrics.DENSITY_HIGH:
                zoomDensity = WebSettings.ZoomDensity.FAR;
                break;
        }
        setting.setDefaultZoom(zoomDensity);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.clearCache(true);
            webView.clearView();
            webView.destroy();
            webView = null;
        }
        CookieManager.getInstance().removeAllCookie();

        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onEventMainThread(Message message) {
        switch (message.what) {
            case TransPadService.TRANSPAD_STATE_DISCONNECTED:
                finish();
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private class MyWebViewDownLoadListener implements DownloadListener {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                    long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart(this.getClass().getSimpleName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd(this.getClass().getSimpleName());
    }

}
