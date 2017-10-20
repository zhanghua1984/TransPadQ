package cn.transpad.transpadui.player.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Method;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.PlayerUtil;
import cn.transpad.transpadui.util.ScreenUtil;
import cn.transpad.transpadui.util.TPUtil;
import cn.transpad.transpadui.view.ColorBallProgressView;

/**
 * H5播放页面
 *
 * @author kongxiaojun
 * @since 2014-5-20
 */
public class WebViewPlayerActivity extends Activity {

	private WebView webView;
	private View mTitleLayout;
	private boolean isMoving = false;
	private final float hiddenTitileMoveDistace = 10.0f;
	private final static String TAG = "WebViewPlayerActivity";
	GestureDetector gDetector;
	private View mView;
	private Button btBack;
	private TextView tvTitle;
	private View hdPlay;
	private String xyzplay;
	private String name;
	private String ourl;
	/** 圆形进度条 */
	private ColorBallProgressView loadingProgress;
	private int btnply;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mView = LayoutInflater.from(WebViewPlayerActivity.this).inflate(R.layout.webview_player, null);
		setContentView(mView);
		initView();
		gDetector = new GestureDetector(new OnGestureListener() {

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return false;
			}

			@Override
			public void onShowPress(MotionEvent e) {
			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				return false;
			}

			@Override
			public void onLongPress(MotionEvent e) {
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				if (e1.getX() < e2.getX()) {
					if (getdistance(e1, e2) && mTitleLayout.getVisibility() == View.VISIBLE) {
						mTitleLayout.setVisibility(View.GONE);
					}
				} else {
					if (getdistance(e1, e2) && mTitleLayout.getVisibility() == View.GONE) {
						mTitleLayout.setVisibility(View.VISIBLE);
					}
				}
				return false;
			}

			private boolean getdistance(MotionEvent e1, MotionEvent e2) {
				isMoving = Math.abs(e1.getY() - e2.getY()) > hiddenTitileMoveDistace;
				return isMoving;
			}

			@Override
			public boolean onDown(MotionEvent e) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		Intent i = getIntent();
		handleIntent(i);

	}

	/**
	 * 初始化view
	 *
	 * @return void
	 * @throws
	 */
	private void initView() {
		mTitleLayout = findViewById(R.id.webview_player_title_layout);
		initWebview(ScreenUtil.getScreenWidthPix(WebViewPlayerActivity.this));
		btBack = (Button) findViewById(R.id.player_back_webview);
		tvTitle = (TextView) findViewById(R.id.player_title_webview);
		hdPlay = findViewById(R.id.hdplay_layout);
		loadingProgress = (ColorBallProgressView) findViewById(R.id.web_loading);
		btBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBack();
			}
		});
		hdPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				Reporter.logEvent(Reporter.EventId.CLICK_H5_PLAY_BTN);
				//把webview销毁掉
				if (webView != null) {
					webView.stopLoading();
				}
				finish();
				PlayerUtil.openVideoPlayer(WebViewPlayerActivity.this,xyzplay);
			}
		});
	}

	private void handleIntent(Intent i) {
		xyzplay = i.getStringExtra("xyzplay");
		ourl = i.getStringExtra("ourl");
		name = i.getStringExtra("name");
		btnply = i.getIntExtra("btnply", 1);
		if (TextUtils.isEmpty(xyzplay)) {
			finish();
		} else {
			try {
				if(TextUtils.isEmpty(name)){
					tvTitle.setText(ourl);
				}else {
					tvTitle.setText(String.format(getString(R.string.web_player_title), name, ourl));
				}
				if (btnply == 0) {
					// 不显示
					hdPlay.setVisibility(View.GONE);
				} else {
					hdPlay.setVisibility(View.VISIBLE);
				}
				loadUrl(ourl);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	public void initWebview(final int screenWidth) {
		L.v(TAG, "initWebview", "screenWidth:" + screenWidth);
		webView = (WebView) findViewById(R.id.webview_player);
		//WebView.enablePlatformNotifications();
		setWebView();
		// setting.setLoadWithOverviewMode(true);
		webView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gDetector.onTouchEvent(event);
				return false;
			}
		});
		WebViewClient mWebClient = new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				L.v(TAG, "shouldOverrideUrlLoading beg url:", url);
				// toolbarLayout.setVisibility(View.VISIBLE);
				if (TPUtil.isNetOkWithToast()) {
					if (url != null) {
						loadUrl(url);
					}
				}
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				loadingProgress.setVisibility(View.VISIBLE);
				webView.getSettings().setBlockNetworkImage(true);
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
            if (url.toLowerCase().startsWith("http")){
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
			Toast.makeText(WebViewPlayerActivity.this, WebViewPlayerActivity.this.getText(R.string.webview_url_is_null), Toast.LENGTH_SHORT).show();
		}
		L.v(TAG, "loadUrl", url);
	}

	public boolean goBack() {
		L.v(TAG, "webViewGoBack", "click");
		if (webView.canGoBack()) {
			L.v(TAG, "webViewGoBack", "canGoBack");
			webView.goBack();
			return true;
		}
		return false;
	}

	public boolean goForward() {
		L.v(TAG, "webViewGoForward", "click");
		if (webView.canGoForward()) {
			webView.goForward();
            return true;
		}
		return false;
	}

	public boolean reLoad() {
		L.v(TAG, "webViewRefush", "click");
		webView.reload();
		return true;
	}

	private void setWebView() {
		if (ScreenUtil.getScreenWidthPix(WebViewPlayerActivity.this) <= 480) {
			webView.setInitialScale(50);
		} else {
			webView.setInitialScale(75);
		}
		WebSettings setting = webView.getSettings();
		setting.setAllowFileAccess(true);
		setting.setJavaScriptEnabled(true);
		setting.setDefaultZoom(WebSettings.ZoomDensity.FAR);
		setting.setSupportZoom(true);
		setting.setBuiltInZoomControls(false);
		setting.setUseWideViewPort(true);
		setting.setUseWideViewPort(true);
		setting.setLoadWithOverviewMode(true);
		int screenDensity = WebViewPlayerActivity.this.getResources().getDisplayMetrics().densityDpi;
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
	public void onStart() {
		super.onStart();
		L.v(TAG, "onStart", "start");

		L.v(TAG, "onStart", "end");
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		MobclickAgent.onPageStart(this.getClass().getSimpleName());
		L.v(TAG, "onResume", "start");
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			try {
				Class<?> clazz = webView.getClass();
				Method m1 = clazz.getDeclaredMethod("onResume");
				m1.invoke(webView);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (null != mView) {
			mView.setKeepScreenOn(true);
		}
		changeDefaultOrientation();
		L.v(TAG, "onResume", "end");
	}

	private void changeDefaultOrientation() {
		if (getScreenOrientation() == Configuration.ORIENTATION_PORTRAIT) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		MobclickAgent.onPageEnd(this.getClass().getSimpleName());
		L.v(TAG, "onPause", "start");
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			try {
				Class<?> clazz = webView.getClass();
				Method m1 = clazz.getDeclaredMethod("onPause");
				m1.invoke(webView);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (null != mView) {
			mView.setKeepScreenOn(false);
		}
		//WebView.disablePlatformNotifications();
		L.v(TAG, "onPause", "end");
	}

	@Override
	public void onStop() {
		super.onStop();
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

		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		L.v(TAG, "onkeyDown", "start:" + keyCode);
		if(keyCode == KeyEvent.KEYCODE_BACK){
			onBack();
			return true;
		}
		return false;
	}

	public void onBack() {
		if (webView != null) {
			webView.stopLoading();
			webView.clearView();
		}
		finish();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	/**
	 * 获取屏幕方向
	 * 
	 * @return
	 * @return int
	 * @throws
	 */
	private int getScreenOrientation() {
		return this.getResources().getConfiguration().orientation;
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

}
