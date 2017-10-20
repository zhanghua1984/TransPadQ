package cn.transpad.transpadui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.http.LoginRst;
import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.ApplicationUtil;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.SystemUtil;
import cn.transpad.transpadui.util.UpdateUtil;
import de.greenrobot.event.EventBus;

/**
 * Created by user on 2015/5/11.
 */
public class ForceUpdate extends Activity {

    public static final String TAG = ForceUpdate.class.getSimpleName();
    private LoginRst loginRst;
    private String name;
    private String url;
    long id;
    File file;


    @InjectView(R.id.settings_update_force_dialog_message)
    TextView updateMessage;
    @InjectView(R.id.settings_update_force_dialog_progressbar)
    ProgressBar progressBar;
    @InjectView(R.id.settings_update_force_dialog_ok)
    Button download;
    @InjectView(R.id.settings_update_force_dialog_cancel)
    Button cancel;
//    @InjectView(R.id.update_background)
//    RelativeLayout relativeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_force_dialog);

        ButterKnife.inject(this);
        EventBus.getDefault().register(this);
        Intent intent = getIntent();
        loginRst = (LoginRst) intent.getSerializableExtra("loginRst");
        name = loginRst.softupdate.name;
        url = loginRst.softupdate.updateurl;
//        name = intent.getStringExtra("name");
//        url = intent.getStringExtra("url");
        if (this.getResources().getConfiguration().locale.getCountry().equals("CN")) {
            updateMessage.setText(loginRst.softupdate.updatedesc);
        } else {
            updateMessage.setText(R.string.update_description);//英文描述语
        }
        id = url.hashCode();
        L.v(TAG, "onCreate", "offline id= " + id);
//        relativeLayout.setClickable(false);
//        点击边缘消失问题
        setFinishOnTouchOutside(false);
        String filePath = SystemUtil.getInstance().getOfflineCachePath();
        L.v(TAG, "onCreate", "filepath= " + filePath);
        file = new File(filePath, name + ".apk");


    }


    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart(this.getClass().getSimpleName());
        L.v(TAG, "onResume", "dialog onResume");
        OfflineCache localOfflineCache = StorageModule.getInstance().getOfflineCacheById(id);
        if (file.exists()) {
            L.v(TAG, "onResume", "file exist " + file.getPath() + download.isEnabled() + localOfflineCache);
            if (localOfflineCache != null) {
                int state = localOfflineCache.getCacheDownloadState();
                switch (state) {
                    case OfflineCache.CACHE_STATE_DOWNLOADING:
//                        download.setVisibility(View.GONE);
//                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case OfflineCache.CACHE_STATE_FINISH:
                        download.setText(R.string.setting_update_install);
                        cancel.setVisibility(View.GONE);
                        download.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            } else {
                file.delete();
            }
        } else {
            L.v(TAG, "onResume", "file not exist" + download.isEnabled() + localOfflineCache);
            download.setText(R.string.setting_update_ok);
//            download.setEnabled(true);
            download.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
//            progressBar.setProgress(0);
            file.delete();
            StorageModule.getInstance().deleteUpgradeOfflineCache();
            if (localOfflineCache != null) {
                StorageModule.getInstance().deleteCache(localOfflineCache);
            }
        }
        ApplicationUtil.nonWiFiToast();
    }

    @OnClick(R.id.settings_update_force_dialog_ok)
    public void updateOk() {
        OfflineCache localOfflineCache = StorageModule.getInstance().getOfflineCacheById(id);
        if (file.exists()) {
            L.v(TAG, "updateOk", "file.getpath= " + file.getPath() + localOfflineCache);
            if (localOfflineCache != null) {
                int state = localOfflineCache.getCacheDownloadState();
                switch (state) {
                    case OfflineCache.CACHE_STATE_FINISH:
                        StorageModule.getInstance().installApp(file.getPath());
                        return;
                    case OfflineCache.CACHE_STATE_DOWNLOADING:
                        L.v(TAG, "updateOk", "downloading");
                        StorageModule.getInstance().startCache(localOfflineCache);
                        break;
//                    case OfflineCache.CACHE_STATE_PAUSE:
//                        StorageModule.getInstance().startCache(localOfflineCache);
//                        break;
                    default:
                        L.v(TAG, "updateOk", "localoffline" + localOfflineCache);
                        file.delete();
                        StorageModule.getInstance().deleteUpgradeOfflineCache();
                        Toast.makeText(this, R.string.setting_update_force_fail, Toast.LENGTH_SHORT).show();
                        break;
                }
            } else {

            }
        } else {
            OfflineCache offlineCache = new OfflineCache();
            offlineCache.setCacheDetailUrl(url);
            offlineCache.setCacheName(name);
            offlineCache.setCachePackageName(getPackageName());
            offlineCache.setCacheID(id);
            offlineCache.setCacheDownloadType(OfflineCache.CACHE_FROM_PAGE_UPGRADE);
            download.setText(R.string.setting_update_ok);
            StorageModule.getInstance().addCache(offlineCache);
            download.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            Calendar calendar = Calendar.getInstance();
            SharedPreferenceModule.getInstance().setLong("updateTime", calendar.getTimeInMillis());
        }
        download.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.settings_update_force_dialog_cancel)
    public void updateCancel() {
        L.v(TAG, "cancel", "click cancel");
        file.delete();
        StorageModule.getInstance().deleteUpgradeOfflineCache();
        setResult(RESULT_OK);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }


    @Override
    public void onBackPressed() {
//        返回按钮失效
//        super.onBackPressed();
        L.v(TAG, "onBackPressed");
        OfflineCache localOfflineCache = StorageModule.getInstance().getOfflineCacheById(id);
        if (file.exists()) {
            if (localOfflineCache != null) {
                int state = localOfflineCache.getCacheDownloadState();
                switch (state) {
                    case OfflineCache.CACHE_STATE_FINISH:
                        break;
                    case OfflineCache.CACHE_STATE_DOWNLOADING:
                        L.v(TAG, "onBackPressed", "downloading");
                        file.delete();
                        StorageModule.getInstance().deleteUpgradeOfflineCache();
                        break;
                    default:
                        L.v(TAG, "onBackPressed", "default");
                        file.delete();
                        StorageModule.getInstance().deleteUpgradeOfflineCache();
                        break;
                }
            } else {
            }
        } else {
        }
        setResult(RESULT_OK);
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(Message message) {
        OfflineCache offlineCache;
        boolean isForceOffline;
        switch (message.what) {
            case StorageModule.MSG_FILE_CACHE_UPDATE_PROGRESS_SUCCESS:
                offlineCache = message.getData().getParcelable(OfflineCache.OFFLINE_CACHE);
                if (offlineCache != null && offlineCache.getCachePackageName() != null) {
                    isForceOffline = offlineCache.getCachePackageName().equals(getPackageName());
                    if (isForceOffline) {
                        if (!file.exists()) {
                            StorageModule.getInstance().deleteCache(offlineCache);
                        } else {
                            progressBar.setProgress((int) offlineCache.getCachePercentNum());
                        }
                    }
                }
                break;
            case StorageModule.MSG_DOWNLOAD_CACHE_SUCCESS:
                offlineCache = message.getData().getParcelable(OfflineCache.OFFLINE_CACHE);
                if (offlineCache != null && offlineCache.getCachePackageName() != null) {
                    isForceOffline = offlineCache.getCachePackageName().equals(getPackageName());
                    if (isForceOffline) {
                        download.setText(R.string.setting_update_install);
                        StorageModule.getInstance().installApp(offlineCache.getCacheStoragePath());
                    }
                }
                break;
            case StorageModule.MSG_DOWNLOAD_ERROR:
                break;
            case StorageModule.MSG_NO_NETWORK_TYPE:
                Toast.makeText(this, R.string.settings_version_fail_noNetwork, Toast.LENGTH_SHORT).show();
                break;
            case StorageModule.MSG_WIFI_NETWORK_TYPE:
            case StorageModule.MSG_2G_NETWORK_TYPE:
            case StorageModule.MSG_3G_NETWORK_TYPE:
            case StorageModule.MSG_4G_NETWORK_TYPE:
                UpdateUtil.startDownloadUpdateFile(loginRst);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd(this.getClass().getSimpleName());
    }
}
