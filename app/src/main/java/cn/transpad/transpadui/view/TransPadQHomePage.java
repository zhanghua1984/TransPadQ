package cn.transpad.transpadui.view;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Message;
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
 * Created by wangshaochun on 2015/9/18.
 */
public class TransPadQHomePage extends LinearLayout implements View.OnClickListener{
    public static final String TAG = "TransPadQHomePage";
    @InjectView(R.id.app_image1)
    ImageView appImage1;
    @InjectView(R.id.app_image2)
    ImageView appImage2;
    @InjectView(R.id.app_image3)
    ImageView appImage3;
    @InjectView(R.id.app_image4)
    ImageView appImage4;
    @InjectView(R.id.function1)
    RelativeLayout function1;
    @InjectView(R.id.function2)
    RelativeLayout function2;
    @InjectView(R.id.function3)
    RelativeLayout function3;
    @InjectView(R.id.function4)
    RelativeLayout function4;
    @InjectView(R.id.function5)
    RelativeLayout function5;
    @InjectView(R.id.new_message_circle)
    ImageView redDot;
    @InjectView(R.id.iv_link)
    ImageView iv_link;
//    @InjectView(R.id.tv_link)
//    TextView tv_link;
    @InjectView(R.id.tv_name1)
    TextView tv_name1;
    @InjectView(R.id.tv_name2)
    TextView tv_name2;
    @InjectView(R.id.tv_name3)
    TextView tv_name3;
    @InjectView(R.id.function_image1)
    ImageView function_image1;
    @InjectView(R.id.function_image2)
    ImageView function_image2;
    @InjectView(R.id.function_image3)
    ImageView function_image3;
    @InjectView(R.id.function_image4)
    ImageView function_image4;
    @InjectView(R.id.function_text1)
    TextView function_text1;
    @InjectView(R.id.function_text2)
    TextView function_text2;
    @InjectView(R.id.function_text3)
    TextView function_text3;
    @InjectView(R.id.function_text4)
    TextView function_text4;


    private SharedPreferences sp;
    private SharedPreferences.Editor editor = null;
    private HotspotRst hotspotRst;
    private Handler mHandler;
    private static final int MSG_WHAT_UPDATE_HOTSPOT = 1;
    private static final int MSG_WHAT_DISMESS_LOADING_DIALOG = 2;
    DisplayImageOptions options;
    private MainPageDownloadDialog mainPageDownloadDialog;

    public TransPadQHomePage(Context context) {
        super(context);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.transpad_q_homepage, this);
        options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).build();
        sp = getContext().getSharedPreferences("config", 0);
        EventBus.getDefault().register(this);
        ButterKnife.inject(this);
        initHandler();
        initData();

    }
    /**
     * 初始化数据
     */
    public void initData() {

        Message message = new Message();
        message.what = HomeActivity.MSG_WHAT_SHOW_LOADING_DIALOG;
        EventBus.getDefault().post(message);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HotspotRst hotspotRst = TPUtil.readCachedServerData(HotspotRst.class, getContext(), "tpq_home_hotspot");
                if (hotspotRst != null) {
                    TransPadQHomePage.this.hotspotRst = hotspotRst;
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
                    L.v(TAG,"initData"+hotspotRst.toString());
                    TransPadQHomePage.this.hotspotRst = hotspotRst;
                    updateHomeHotspot();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                L.v(TAG,"failure","Body="+error.getBody());
                L.v(TAG,"failure","Message="+error.getMessage());
                mHandler.sendEmptyMessageDelayed(MSG_WHAT_DISMESS_LOADING_DIALOG, 1500);
                Message message = new Message();
                message.what = HomeActivity.MSG_WHAT_HOME_DATA_REQUEST_ERROR;
                EventBus.getDefault().post(message);
            }
        });
        appImage1.setOnClickListener(this);
        appImage2.setOnClickListener(this);
        appImage3.setOnClickListener(this);
        appImage4.setOnClickListener(this);
        function1.setOnClickListener(this);
        function2.setOnClickListener(this);
        function3.setOnClickListener(this);
        function4.setOnClickListener(this);
        function5.setOnClickListener(this);
        updateRedDot();
        if (TransPadService.isConnected()) {
            iv_link.setImageResource(R.drawable.close_screen_bg);
        } else {
            iv_link.setImageResource(R.drawable.tpq_home_page_link);
        }
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
//                tv_link.setText(R.string.close_link);
                break;
            case TransPadService.TRANSPAD_STATE_DISCONNECTED:
                L.v(TAG, "onEventMainThread", "TRANSPAD_STATE_DISCONNECTED");
                //tvWifiTitle.setVisibility(View.GONE);
                iv_link.setImageResource(R.drawable.tpq_home_page_link);
//                tv_link.setText(R.string.link);
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
                    tv_name1.setText(hotspotRst.posters.posterList.get(0).name);
                }
                if (hotspotRst.posters.posterList.size() > 1) {
                    ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(hotspotRst.host, hotspotRst.shost, hotspotRst.posters.posterList.get(1).pic), appImage2, options);
                    tv_name2.setText(hotspotRst.posters.posterList.get(1).name);
                }
                if (hotspotRst.posters.posterList.size() > 2) {
                    ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(hotspotRst.host, hotspotRst.shost, hotspotRst.posters.posterList.get(2).pic), appImage3, options);
                    tv_name3.setText(hotspotRst.posters.posterList.get(2).msg);
                }
                if (hotspotRst.posters.posterList.size() > 3) {
                    ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(hotspotRst.host, hotspotRst.shost, hotspotRst.posters.posterList.get(3).pic), appImage4, options);
//                    function_text1.setText(hotspotRst.posters.posterList.get(3).name);
                }
                if (hotspotRst.posters.posterList.size() > 4) {
                    ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(hotspotRst.host, hotspotRst.shost, hotspotRst.posters.posterList.get(4).pic),function_image1, options);
                    function_text1.setText(hotspotRst.posters.posterList.get(4).name);
                }
                if (hotspotRst.posters.posterList.size() > 5) {
                    ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(hotspotRst.host, hotspotRst.shost, hotspotRst.posters.posterList.get(5).pic), function_image2, options);
                    function_text2.setText(hotspotRst.posters.posterList.get(5).name);
                }
                if (hotspotRst.posters.posterList.size() > 6) {
                    ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(hotspotRst.host, hotspotRst.shost, hotspotRst.posters.posterList.get(6).pic), function_image3, options);
                    function_text3.setText(hotspotRst.posters.posterList.get(6).name);
                }
                if (hotspotRst.posters.posterList.size() > 7) {
                    ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(hotspotRst.host, hotspotRst.shost, hotspotRst.posters.posterList.get(7).pic), function_image4, options);
                    function_text4.setText(hotspotRst.posters.posterList.get(7).name);
                }
            }
        }
    }
    public boolean isInit() {
        return hotspotRst != null;
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
        switch (v.getId()){
            case R.id.app_image1:
                onClickApp(0);
                break;
            case R.id.app_image2:
                onClickApp(1);
                break;
            case R.id.app_image3:
                onClickApp(2);
                break;
            case R.id.app_image4:
                onClickApp(3);
                break;
            case R.id.function1:
                onClickApp(4);
                break;
            case R.id.function2:
                onClickApp(5);
                break;
            case R.id.function3:
                onClickApp(6);
                break;
            case R.id.function4:
                onClickApp(7);
                break;
            case R.id.function5:
                Fragment fragment = new MultimediaFragment();
                HomeActivity.switchFragment(fragment);
                break;
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
        if (poster.utp.equals("4")) {//发广播，跳转
            editor = sp.edit();
            editor.putString("function_url", poster.url);
            editor.commit();
            Message message = new Message();
            message.what = HomeActivity.MSG_HOME_PAGE_BUTTON_MESSAGE;
            EventBus.getDefault().post(message);
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
                    case OfflineCache.CACHE_STATE_WAITING:
                        Toast.makeText(getContext(), R.string.app_already_downlaod, Toast.LENGTH_SHORT).show();
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
}
