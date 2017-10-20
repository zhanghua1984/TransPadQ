package cn.transpad.transpadui.main;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.http.LoginRst;
import cn.transpad.transpadui.http.Reporter;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.http.SoftRst;
import cn.transpad.transpadui.player.AudioPlayer;
import cn.transpad.transpadui.receiver.SuicideReceiver;
import cn.transpad.transpadui.service.NotificationFetcherService;
import cn.transpad.transpadui.service.TransPadService;
import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.BandUtil;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;
import cn.transpad.transpadui.util.UpdateUtil;
import cn.transpad.transpadui.view.SuggestUpdateDialog;
import cn.transpad.transpadui.view.WifiDialog;
import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HomeActivity extends Activity {

    private static final String TAG = "HomeActivity";

    public static final int MSG_WHAT_CHANGE_FRAGMENT = 3001;

    public static final int MSG_WHAT_ADD_APP = 3002;

    public static final int MSG_WHAT_GO_BACK = 3003;

    public static final int MSG_WHAT_BACKAGE_GROUND_CHANGED = 3004;

    public static final int MSG_WHAT_PAGE_DELETED = 3005;

    public static final int MSG_WHAT_ADD_NOITEMVIEW = 3006;

    public static final int MSG_WHAT_HOME_DATA_REQUEST_ERROR = 3007;

    public static final int MSG_WHAT_GO_HOME_PAGE1 = 1;

    public static final int MSG_WHAT_SHOW_AD = 2;//显示购买小宝的广告

    public static final String OPEN_ACTION = "action";

    public static final String OPEN_MY_APP_INTENT_NAME = "open_myapp";

    public static final String OPEN_HOME_INTENT_NAME = "open_home";

    public static final String WALLPAGER_FILENAME = "wallpager.png";

    public static final int MSG_WHAT_ACTIVITY_RESUME = 3008;

    public static final int MSG_WHAT_SHOW_LOADING_DIALOG = 3009;

    public static final int MSG_WHAT_DISMESS_LOADING_DIALOG = 3010;

    public static final int MSG_HOME_PAGE_BUTTON_MESSAGE = 3011;

    SuggestUpdateDialog suggestUpdateDialog;
    public static final String SUGGEST_UPDATE = "0"; // 推荐升级
    public static final String FORCE_UPDATE = "1"; // 强制升级
    public static final String ALREADY_NEWLEST = "2"; // 已是最新版本
    public static final String FIRST_LAUNCH = "first_launch"; // 第一次启动
    boolean isActivityRunning = false;
    public static final int FORCE_UPDATE_ACTIVITY = 1;
    public boolean NeedForceUpdate = false;

    @InjectView(R.id.main_fragment_layout)
    LinearLayout fragemntLayout;
    @InjectView(R.id.wallpager)
    ImageView wallpager;
    @InjectView(R.id.guidepage)
    ImageView guidePage;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.v(TAG, "onCreate", "onCreate");
        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);
        EventBus.getDefault().register(this);
        updateWallpager();
        if (fragemntLayout != null && savedInstanceState == null) {
            showMainFragment();
        }

        initHandler();

        // 开启离线缓存service
        StorageModule.getInstance().startCacheService();

        TPUtil.isNetOkWithToast();

        initUpdate();

        showFirstTimeGuidePage();

        //初始化工具库
        BandUtil.init(this);

        //应用互斥
        Intent intentReceiver = new Intent(SuicideReceiver.ACTION);
        intentReceiver.putExtra(SuicideReceiver.PACKAGE_NAME, getPackageName());
        sendBroadcast(intentReceiver);


        int deviceAdShowTimes = SharedPreferenceModule.getInstance().getInt("device_ad_show_times",0);

        if (getPackageName().equals("com.fone.player") && deviceAdShowTimes < 5) {
            mHandler.sendEmptyMessageDelayed(MSG_WHAT_SHOW_AD,5000);
            deviceAdShowTimes++;
            SharedPreferenceModule.getInstance().setInt("device_ad_show_times",deviceAdShowTimes);
        }
    }

    private void showFirstTimeGuidePage() {
        boolean isFirstLaunch = SharedPreferenceModule.getInstance().getBoolean(FIRST_LAUNCH, true);
        if (isFirstLaunch) {
            guidePage.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.guidepage)
    void dismissGuidepage() {
        guidePage.setVisibility(View.GONE);
        SharedPreferenceModule.getInstance().setBoolean(FIRST_LAUNCH, false);
    }

    @OnClick(R.id.btnWifi)
    void wifiList() {
        WifiDialog wd = new WifiDialog(this);
        wd.show();
    }

    private void initUpdate() {
        suggestUpdateDialog = new SuggestUpdateDialog(this, R.style.myDialog);
        Request.getInstance().login(0, new Callback<LoginRst>() {
            @Override
            public void success(final LoginRst loginRst, Response response) {
                if (loginRst != null && loginRst.softupdate != null) {

                    L.v(TAG, "success", "loginrst" + loginRst.softupdate.updateflag);
                    switch (loginRst.softupdate.updateflag) {
                        case SUGGEST_UPDATE:
                            UpdateUtil.deleteOldDownloadingUpdateFile(loginRst);
                            UpdateUtil.checkSuggestUpdate(loginRst);
                            showSuggestUpdateDialog(loginRst);
//                            -------------------------------------
//                            NeedForceUpdate = true;
//                            if (UpdateUtil.checkForceUpdateFileAvailable(loginRst)) {
//                                finish();
//                                return;
//                            }
//                            showForceUpdateActivity(loginRst);
                            break;
                        case FORCE_UPDATE:
                            NeedForceUpdate = true;
                            if (UpdateUtil.checkForceUpdateFileAvailable(loginRst)) {
                                finish();
                                return;
                            }
                            showForceUpdateActivity(loginRst);
                            break;
                        case ALREADY_NEWLEST:
//                            不提示
//                            Toast.makeText(MainActivity.this, R.string.version_dialog_new, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    public void showForceUpdateActivity(LoginRst loginRst) {
        Intent intent = new Intent();
        intent.setClass(this, ForceUpdate.class);
        intent.putExtra("loginRst", loginRst);
//        intent.putExtra("name", loginRst.softupdate.name);
//        intent.putExtra("url", loginRst.softupdate.updateurl);
//        intent.putExtra("dec", loginRst.softupdate.updatedesc);
        startActivityForResult(intent, FORCE_UPDATE_ACTIVITY);
    }

    public void showSuggestUpdateDialog(final LoginRst loginRst) {
        Reporter.logEvent(Reporter.EventId.UPGRADE_POPUP);
        suggestUpdateDialog.setUpdateMessage(loginRst);
        suggestUpdateDialog.setClickListener(new SuggestUpdateDialog.ClickListener() {
            @Override
            public void onOk() {
                Reporter.logEvent(Reporter.EventId.UPGRADE_CLICK_OK);
                UpdateUtil.startDownloadUpdateFile(loginRst);
            }

            @Override
            public void onCancel() {
                Reporter.logEvent(Reporter.EventId.UPGRADE_CLICK_CANCEL);
                UpdateUtil.deleteUpdateFile(loginRst);
                suggestUpdateDialog.dismiss();
            }
        });
        if (isActivityRunning) {
            suggestUpdateDialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FORCE_UPDATE_ACTIVITY && resultCode == RESULT_OK) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        L.v(TAG, "onResume", "onResume");
        Message message = new Message();
        message.what = MSG_WHAT_ACTIVITY_RESUME;
        EventBus.getDefault().post(message);
        TransPadService.getInstance().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
//        if (suggestUpdateDialog != null && suggestUpdateDialog.isShowing()) {
//            suggestUpdateDialog.dismiss();
//        }
    }

    /**
     * 根据连接类型显示不同的设备页
     */
    private void showMainFragment() {
        Fragment fragment = new TpqHomeFragement();
        if (fragment != null) {
            fragment.setArguments(getIntent().getExtras());
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            Fragment f = getFragmentManager().findFragmentById(R.id.main_fragment_layout);
            if (f != null) {
                transaction.remove(f);
            }
            getFragmentManager().popBackStack();
            transaction.add(R.id.main_fragment_layout, fragment);
            transaction.commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityRunning = true;
    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_WHAT_GO_HOME_PAGE1://到首页的第一屏
                        L.v(TAG, "MSG_WHAT_GO_HOME_PAGE1");
                        Fragment curFragment = getFragmentManager().findFragmentById(R.id.main_fragment_layout);
                        if (curFragment != null) {
                            if (curFragment instanceof TpqHomeFragement) {
                                TpqHomeFragement fragment = (TpqHomeFragement) curFragment;
                                fragment.scrollToRestoredPage();
                            }
                        }
                        break;
                    case MSG_WHAT_SHOW_AD://显示购买小宝广告
                        final Dialog dialog = new Dialog(HomeActivity.this, R.style.dialog_base);
                        View contentView = LayoutInflater.from(HomeActivity.this).inflate(R.layout.buydevicead, null);
                        ImageView close = (ImageView) contentView.findViewById(R.id.iv_adclose);
                        ImageView deviceAd = (ImageView) contentView.findViewById(R.id.iv_devicead);
                        close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.cancel();
                            }
                        });
                        deviceAd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent();
                                intent.setData(Uri.parse("https://tpcast.taobao.com"));
                                intent.setAction(Intent.ACTION_VIEW);
                                startActivity(intent);
                                dialog.cancel();
                            }
                        });
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setContentView(contentView);
                        dialog.show();
                        break;
                }
            }
        };
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 更新壁纸
     */
    private void updateWallpager() {
        L.v(TAG, "updateWallpager");
        File file = new File(getCacheDir(), WALLPAGER_FILENAME);
        if (file.exists()) {
            L.v(TAG, "updateWallpager file.exists()");
            wallpager.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
        }
    }

    public void onEventMainThread(Message message) {
        switch (message.what) {
            case SuicideReceiver.EXIT_APP:
                L.v(TAG, "onEventMainThread", "EXIT_APP");
                finish();
                System.exit(0);
                break;
            case TransPadService.TRANSPAD_STATE_CONNECTED:
                //保持屏幕常亮
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                L.v(TAG, "onEventMainThread", "TRANSPAD_STATE_CONNECTED");
                //显示弹框
                // BandUtil.showConnectAfterDialog();
                if (BandUtil.is24GHzWifiNet()) {
                    TPUtil.showToast(R.string.msg_connect_tip);
                }

                break;
            case TransPadService.TRANSPAD_STATE_DISCONNECTED:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                break;
            case NotificationFetcherService.MSG_WHAT_RECEIVER_NOTIFICATION:
                Notification localNotification = (Notification) message.obj;
                Intent intent = new Intent(this, NotificationViewActivity.class);
                intent.putExtra("notification", localNotification);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case MSG_WHAT_CHANGE_FRAGMENT://改变fragment
                Fragment fragment = (Fragment) message.obj;
                if (fragment != null) {
                    changeFragment(fragment);
                }
                break;
            case MSG_WHAT_GO_BACK://返回
                onBackPressed();
                break;
            case StorageModule.MSG_DOWNLOAD_CACHE_SUCCESS://应用下载完成
                OfflineCache offlineCache = message.getData().getParcelable(OfflineCache.OFFLINE_CACHE);
                offlineCache = message.getData().getParcelable(OfflineCache.OFFLINE_CACHE);
                //安装应用
                StorageModule.getInstance().installApp(offlineCache.getCacheStoragePath());
                break;
            case MSG_WHAT_BACKAGE_GROUND_CHANGED://壁纸换了
                updateWallpager();
                break;
            case MSG_HOME_PAGE_BUTTON_MESSAGE://接收跳转栏目页的消息
                SharedPreferences sp = getSharedPreferences("config", 0);
                String function_url = sp.getString("function_url", "null");
                //得到软件页的对象
                SoftRst softRst = TransPadApplication.getTransPadApplication().getSoftRst();
                //得到栏目页的的数组
                List<SoftRst.Col> list = softRst.cols;
                if (list.size() > 0) {
                    //遍历查找对应的clid
                    for (int i = 0; i < list.size(); i++) {
                        if (function_url.contains(TPUtil.getCLIdByUrl(list.get(i).url))) {
                            int index = i;
                            Fragment curFragment = getFragmentManager().findFragmentById(R.id.main_fragment_layout);
                            if (curFragment != null) {
                                if (curFragment instanceof TpqHomeFragement) {
                                    TpqHomeFragement fragment2 = (TpqHomeFragement) curFragment;
                                    //跳转到指定页面
                                    fragment2.setCurrentPage(2 + index);
                                }
                            }
                        }
                    }
                }
                break;
        }
    }

    public void changeFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_layout, fragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * 切换Fragment
     *
     * @param fragment
     */
    public static void switchFragment(Fragment fragment) {
        Message message = new Message();
        message.what = HomeActivity.MSG_WHAT_CHANGE_FRAGMENT;
        message.obj = fragment;
        EventBus.getDefault().post(message);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NeedForceUpdate) {
            initUpdate();
        }
        String action = intent.getStringExtra(OPEN_ACTION);
        L.v(TAG, "onNewIntent action = " + action);
        if (!TextUtils.isEmpty(action)) {
            if (action.equals(OPEN_HOME_INTENT_NAME)) {
                goHomeFragment();
                mHandler.sendEmptyMessageDelayed(MSG_WHAT_GO_HOME_PAGE1, 200);
            } else if (action.equals(OPEN_MY_APP_INTENT_NAME)) {//打开我的应用
                Fragment curFragment = getFragmentManager().findFragmentById(R.id.main_fragment_layout);
                if (curFragment != null && !(curFragment instanceof ApplicationFragment)) {
                    Fragment fragment = new ApplicationFragment();
                    changeFragment(fragment);
                }
            }
        }

    }

    /**
     * 回退到HomeFragment
     */
    private void goHomeFragment() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            for (int i = 0; i < getFragmentManager().getBackStackEntryCount(); i++) {
                getFragmentManager().popBackStack();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityRunning = false;
        L.v(TAG, "onStop", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Reporter.ordinaryStart();
        L.v(TAG, "onDestroy", "onDestroy");
        if (suggestUpdateDialog != null && suggestUpdateDialog.isShowing()) {
            suggestUpdateDialog.dismiss();
        }
        EventBus.getDefault().unregister(this);
        AudioPlayer.getInstance().stopAduioService();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    private long lastPauseBackTime = 0;

    @Override
    public synchronized void onBackPressed() {

        if (System.currentTimeMillis() - lastPauseBackTime > 1000) {
            lastPauseBackTime = System.currentTimeMillis();
            Fragment curFragment = getFragmentManager().findFragmentById(R.id.main_fragment_layout);
            if (curFragment != null && curFragment instanceof TpqHomeFragement) {
                if (((TpqHomeFragement) curFragment).getCurrentPage() != ((TpqHomeFragement) curFragment).getRestorePage()) {
                    ((TpqHomeFragement) curFragment).scrollToRestoredPage();
                    return;
                }
            }
            super.onBackPressed();
        }
    }

}
