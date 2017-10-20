package cn.transpad.transpadui.view;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.http.HotspotRst;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.main.ApplicationFragment;
import cn.transpad.transpadui.main.HomeActivity;
import cn.transpad.transpadui.main.MultimediaFragment;
import cn.transpad.transpadui.main.SettingsFragment;
import cn.transpad.transpadui.main.TransPadApplication;
import cn.transpad.transpadui.service.TransPadService;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.ApplicationUtil;
import cn.transpad.transpadui.util.BandUtil;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;
import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Kongxiaojun on 2015/5/11.
 */
public class DongleHomePage extends LinearLayout implements View.OnClickListener {

    private static final String TAG = "DongleHomePage";
    @InjectView(R.id.iv_word)
    ImageView appImage1;
    @InjectView(R.id.iv_calender)
    ImageView appImage2;
    @InjectView(R.id.iv_filemanager)
    ImageView appImage3;
    @InjectView(R.id.iv_email)
    ImageView appImage4;
    @InjectView(R.id.iv_internet)
    ImageView appImage5;

    @InjectView(R.id.tv_word)
    TextView appName1;
    @InjectView(R.id.tv_calender)
    TextView appName2;
    @InjectView(R.id.tv_filemanager)
    TextView appName3;
    @InjectView(R.id.tv_email)
    TextView appName4;
    @InjectView(R.id.tv_internet)
    TextView appName5;

    @InjectView(R.id.rl_word)
    RelativeLayout relativeLayout1;
    @InjectView(R.id.rl_drive)
    RelativeLayout relativeLayout2;
    @InjectView(R.id.rl_ie)
    RelativeLayout relativeLayout3;
    @InjectView(R.id.rl_music)
    RelativeLayout relativeLayout4;
    @InjectView(R.id.rl_myapp)
    RelativeLayout relativeLayout5;
    @InjectView(R.id.new_message_circle)
    ImageView redDot;
    @InjectView(R.id.ll_other)
    LinearLayout layoutLastApp;
    @InjectView(R.id.tvWifiTitle)
    TextView tvWifiTitle;
    @InjectView(R.id.iv_link)
    ImageView iv_link;
    @InjectView(R.id.tv_link)
    TextView tv_link;
    private Handler mHandler;

    private static final int MSG_WHAT_UPDATE_HOTSPOT = 1;
    private static final int MSG_WHAT_DISMESS_LOADING_DIALOG = 2;

    DisplayImageOptions options;

    private HotspotRst hotspotRst;

    private MainPageDownloadDialog mainPageDownloadDialog;

    public DongleHomePage(Context context) {
        super(context);
        init();
    }

    public DongleHomePage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public DongleHomePage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.tv_dongle_page1, this);
        options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).build();
        EventBus.getDefault().register(this);
        ButterKnife.inject(this);
        initHandler();
        initData();
    }

    /**
     * 初始化数据
     */
    public void initData() {

        L.v(TAG, "DongleHomePage init data --- ");
        Message message = new Message();
        message.what = HomeActivity.MSG_WHAT_SHOW_LOADING_DIALOG;
        EventBus.getDefault().post(message);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HotspotRst hotspotRst = TPUtil.readCachedServerData(HotspotRst.class, getContext(), "tpq_home_hotspot");
                if (hotspotRst != null) {
                    DongleHomePage.this.hotspotRst = hotspotRst;
                    mHandler.sendEmptyMessage(MSG_WHAT_UPDATE_HOTSPOT);
                }
            }
        }).start();
        Request.getInstance().hotspot(3, new Callback<HotspotRst>() {
            @Override
            public void success(HotspotRst hotspotRst, Response response) {
                if (hotspotRst != null && hotspotRst.result == 0) {
                    mHandler.sendEmptyMessageDelayed(MSG_WHAT_DISMESS_LOADING_DIALOG, 1500);
                    TPUtil.saveServerData(hotspotRst, "tpq_home_hotspot");
                    DongleHomePage.this.hotspotRst = hotspotRst;
                    updateHomeHotspot();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                mHandler.sendEmptyMessage(MSG_WHAT_DISMESS_LOADING_DIALOG);
                Message message = new Message();
                message.what = HomeActivity.MSG_WHAT_HOME_DATA_REQUEST_ERROR;
                EventBus.getDefault().post(message);
            }
        });

        relativeLayout1.setOnClickListener(this);
        relativeLayout2.setOnClickListener(this);
        relativeLayout3.setOnClickListener(this);
        relativeLayout4.setOnClickListener(this);
        relativeLayout5.setOnClickListener(this);

        updateRedDot();
        if (TransPadService.isConnected()) {
            iv_link.setImageResource(R.drawable.close_screen_bg);
            tv_link.setText(R.string.close_link);
        } else {
            iv_link.setImageResource(R.drawable.tpq_home_page_link);
            tv_link.setText(R.string.link);
        }
    }

    public boolean isInit() {
        return hotspotRst != null;
    }

    public void onEventMainThread(Message message) {
        switch (message.what) {
            case TransPadService.TRANSPAD_STATE_CONNECTED:
                L.v(TAG, "onEventMainThread", "TRANSPAD_STATE_CONNECTED");
//                tvWifiTitle.setVisibility(View.VISIBLE);
//                if (BandUtil.is5GHzWifiP2p()) {
//                    tvWifiTitle.setText("5G极速连接(适合游戏)");
//                } else {
//                    tvWifiTitle.setText("2.4G普通连接(适合视频)");
//                }
                iv_link.setImageResource(R.drawable.close_screen_bg);
                tv_link.setText(R.string.close_link);
                break;
            case TransPadService.TRANSPAD_STATE_DISCONNECTED:
                //tvWifiTitle.setVisibility(View.GONE);
                iv_link.setImageResource(R.drawable.tpq_home_page_link);
                tv_link.setText(R.string.link);
                break;
        }
    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_WHAT_UPDATE_HOTSPOT://更新首页应用推荐
                        updateHomeHotspot();
                        break;
                    case MSG_WHAT_DISMESS_LOADING_DIALOG://隐藏加载dialog
                        Message msg2 = obtainMessage();
                        msg2.what = HomeActivity.MSG_WHAT_DISMESS_LOADING_DIALOG;
                        EventBus.getDefault().post(msg2);
                        break;
                }
            }
        };
    }

    /**
     * 更新红色指示点
     */
    public void updateRedDot() {
        redDot.setVisibility(ApplicationUtil.isTaskUninstalled() ? VISIBLE : INVISIBLE);
    }

    /**
     * 更新hotspot
     */
    private void updateHomeHotspot() {
        if (hotspotRst != null) {
            if (hotspotRst.posters != null) {
                if (hotspotRst.posters.posterList.size() > 0) {
                    ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(hotspotRst.host, hotspotRst.shost, hotspotRst.posters.posterList.get(0).pic), appImage1, options);
                    appName1.setText(hotspotRst.posters.posterList.get(0).name);

                }
                if (hotspotRst.posters.posterList.size() > 1) {
                    ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(hotspotRst.host, hotspotRst.shost, hotspotRst.posters.posterList.get(1).pic), appImage2, options);
                    appName2.setText(hotspotRst.posters.posterList.get(1).name);
                }
                if (hotspotRst.posters.posterList.size() > 2) {
                    ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(hotspotRst.host, hotspotRst.shost, hotspotRst.posters.posterList.get(2).pic), appImage3, options);
                    appName3.setText(hotspotRst.posters.posterList.get(2).name);
                }
                if (hotspotRst.posters.posterList.size() > 3) {
                    ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(hotspotRst.host, hotspotRst.shost, hotspotRst.posters.posterList.get(3).pic), appImage4, options);
                    appName4.setText(hotspotRst.posters.posterList.get(3).name);
                }
                if (TransPadApplication.getTransPadApplication().getShowmedia().equals("1")) {
                    //本页不显示本地媒体，显示海报数据
                    layoutLastApp.setBackgroundResource(R.drawable.color_shade_green);
                    if (hotspotRst.posters.posterList.size() > 4) {
                        ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(hotspotRst.host, hotspotRst.shost, hotspotRst.posters.posterList.get(4).pic), appImage5, options);
                        appName5.setText(hotspotRst.posters.posterList.get(4).name);
                    }
                } else {
                    //本页显示本地媒体
                    layoutLastApp.setBackgroundResource(R.drawable.color_shade_orange2);
                    appImage5.setImageResource(R.drawable.transpad_file);
                    appName5.setText(R.string.home_page2_local_videos);
                }
            }
        }
    }

    private void onClickApp(final int postion) {
        if (hotspotRst == null) {
            Toast.makeText(getContext(), R.string.no_network_toast,
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (hotspotRst.posters == null || hotspotRst.posters.posterList.size() <= postion) {
            return;
        }
        final HotspotRst.Poster poster = hotspotRst.posters.posterList.get(postion);
        if (poster.utp.equals("3")) {
            //打开浏览器
            TPUtil.openBrowser(getContext(), poster.url);
            return;
        }
        PackageInfo info = TPUtil.checkApkExist(getContext(), poster.pkname);
        if (info == null) {
            if (!TPUtil.isNetOkWithToast()) {
                return;
            }
            OfflineCache offlineCache = StorageModule.getInstance().getOfflineCacheById(poster.id);
            if (offlineCache != null) {
                switch (offlineCache.getCacheDownloadState()) {
                    case OfflineCache.CACHE_STATE_FINISH:
                        File file = new File(offlineCache.getCacheStoragePath());
                        if (file.exists()) {
                            TPUtil.installAPK(file, getContext());
                        } else {
                            StorageModule.getInstance().deleteCache(offlineCache);
                            showDialog(hotspotRst.host, poster);
                        }
                        break;
                    case OfflineCache.CACHE_STATE_DOWNLOADING:
                        Toast.makeText(getContext(), R.string.app_already_downlaod, Toast.LENGTH_SHORT).show();
                        break;
                    case OfflineCache.CACHE_STATE_PAUSE:
                        Toast.makeText(getContext(), R.string.app_already_downlaod, Toast.LENGTH_SHORT).show();
                        StorageModule.getInstance().startCache(offlineCache);
                        break;
                }
            } else {
                showDialog(hotspotRst.host, poster);
            }
        } else {
            TPUtil.startAppByPackegName(getContext(), poster.pkname);
        }
    }

    private void showDialog(final String host, final HotspotRst.Poster poster) {
        //未安装
        mainPageDownloadDialog = new MainPageDownloadDialog(getContext(), R.style.myDialog);
        mainPageDownloadDialog.setMessage(String.format(getContext().getString(R.string.home_download_dialog_message), poster.name), poster.url);
        mainPageDownloadDialog.setClickListener(new MainPageDownloadDialog.ClickListener() {
            @Override
            public void onOk() {
                if (TPUtil.isNetOkWithToast()) {
                    OfflineCache offlineCache = new OfflineCache();
                    offlineCache.setCacheName(poster.name);
                    offlineCache.setCacheID(poster.id);
                    offlineCache.setCachePackageName(poster.pkname);
                    String url = TPUtil.getAbsoluteUrl(host, null, poster.url, Request.getInstance().getCipher());
                    offlineCache.setCacheDetailUrl(url);
                    offlineCache.setCacheImageUrl(TPUtil.getAbsoluteUrl(hotspotRst.host, hotspotRst.shost, poster.pic));
                    StorageModule.getInstance().addCache(offlineCache);
                }
                mainPageDownloadDialog.dismiss();
            }

            @Override
            public void onCancel() {
                mainPageDownloadDialog.dismiss();
            }
        });
        mainPageDownloadDialog.show();
    }

    @OnClick(R.id.rl_set)
    public void settings() {
        Fragment fragment = new SettingsFragment();
        HomeActivity.switchFragment(fragment);
    }

    @OnClick(R.id.rl_link)
    public void connectTransPad() {
        //连接设备
        BandUtil.showConnectBeforeDialog();
    }

    @OnClick(R.id.rl_download)
    public void download() {
        Fragment fragment = new ApplicationFragment();
        HomeActivity.switchFragment(fragment);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_word:
                onClickApp(0);
                break;
            case R.id.rl_drive:
                onClickApp(1);
                break;
            case R.id.rl_ie:
                onClickApp(2);
                break;
            case R.id.rl_music:
                onClickApp(3);
                break;
            case R.id.rl_myapp:
                if (TransPadApplication.getTransPadApplication().getShowmedia().equals("1")) {
                    onClickApp(4);
                } else {
                    Fragment fragment = new MultimediaFragment();
                    HomeActivity.switchFragment(fragment);
                }
                break;
        }
    }
}
