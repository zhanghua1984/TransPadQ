package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.http.LoginRst;
import cn.transpad.transpadui.main.HomeActivity;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.ApplicationUtil;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.SystemUtil;
import cn.transpad.transpadui.util.UpdateUtil;
import de.greenrobot.event.EventBus;

/**
 * Created by ctccuser on 2015/4/6.
 */
public class SuggestUpdateDialog extends Dialog {
    private static final String TAG = SuggestUpdateDialog.class.getSimpleName();
    private LoginRst loginRst;
    private String updateDescription;
    private String updateUrl;
    private Context context;
    private String name;
    private ClickListener listener;

    @InjectView(R.id.download_dialog_message)
    TextView updateMessage;
    @InjectView(R.id.settings_update_dialog_progressbar)
    ProgressBar progressBar;
    @InjectView(R.id.download_dialog_ok)
    Button okButton;
    @InjectView(R.id.download_dialog_cancel)
    Button cancel;

    public SuggestUpdateDialog(Context context) {
        super(context);
    }

    public SuggestUpdateDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    public void setUpdateMessage(LoginRst loginRst) {
        if (loginRst != null && loginRst.softupdate != null) {
            this.loginRst = loginRst;
            this.updateDescription = loginRst.softupdate.updatedesc;
            this.name = loginRst.softupdate.name;
            this.updateUrl = loginRst.softupdate.updateurl;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_suggest_dialog);
        ButterKnife.inject(this);
        setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        L.v(TAG, "onStart", "onStart");
        EventBus.getDefault().register(this);
        onResume();
    }

    public void onResume() {
        L.v(TAG, "onResume", "onResume");
        if (context.getResources().getConfiguration().locale.getCountry().equals("CN")) {
            updateMessage.setText(updateDescription);
        } else {
            updateMessage.setText(R.string.update_description);//英文描述语
        }
        String filePath = SystemUtil.getInstance().getOfflineCachePath();
        File file = new File(filePath, name + ".apk");
        OfflineCache localOfflineCache = StorageModule.getInstance().getOfflineCacheById(updateUrl.hashCode());
        if (file.exists()) {
            if (localOfflineCache != null) {
                int state = localOfflineCache.getCacheDownloadState();
                switch (state) {
                    case OfflineCache.CACHE_STATE_FINISH:
//                        progressBar.setProgress(100);
                        progressBar.setVisibility(View.INVISIBLE);
                        okButton.setText(R.string.setting_update_install);
                        okButton.setVisibility(View.VISIBLE);
                        cancel.setVisibility(View.GONE);
                        break;
                    case OfflineCache.CACHE_STATE_DOWNLOADING:
                        progressBar.setVisibility(View.VISIBLE);
                        okButton.setVisibility(View.GONE);
                        cancel.setVisibility(View.VISIBLE);
                        break;
                }
            }
        } else {
            L.v(TAG, "onStart", "file not exist");
            progressBar.setProgress(0);
            progressBar.setVisibility(View.INVISIBLE);
            okButton.setVisibility(View.VISIBLE);
            okButton.setText(R.string.setting_update_ok);
            cancel.setVisibility(View.VISIBLE);
        }
        ApplicationUtil.nonWiFiToast();
    }

    @Override
    protected void onStop() {
        super.onStop();
        L.v(TAG, "onStop", "onStop");
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.download_dialog_ok)
    public void updateOk() {
        if (listener != null) {
            String filePath = SystemUtil.getInstance().getOfflineCachePath();
            File file = new File(filePath, name + ".apk");
            OfflineCache localOfflineCache = StorageModule.getInstance().getOfflineCacheById(updateUrl.hashCode());
            if (file.exists()) {
                if (localOfflineCache != null) {
                    int state = localOfflineCache.getCacheDownloadState();
                    switch (state) {
                        case OfflineCache.CACHE_STATE_FINISH:
                            break;
                        default:
                            okButton.setVisibility(View.GONE);
                            progressBar.setVisibility(View.VISIBLE);
                            break;
                    }
                }
            } else {
                okButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }
            listener.onOk();
            updateMessage.setText(R.string.setting_update_downloading);
        }
    }

    @OnClick(R.id.download_dialog_cancel)
    public void updateCancel() {
        if (listener != null) {
            listener.onCancel();
        }
    }

    public void setClickListener(ClickListener listener) {
        this.listener = listener;
    }

    public interface ClickListener {
        void onOk();

        void onCancel();
    }

    public void onEventMainThread(Message message) {
//        L.v(TAG, "onEventMainThread", message.toString());
        OfflineCache offlineCache;
        boolean isUpdateOffline;
        switch (message.what) {
            case StorageModule.MSG_FILE_CACHE_UPDATE_PROGRESS_SUCCESS:
                offlineCache = message.getData().getParcelable(OfflineCache.OFFLINE_CACHE);
                if (offlineCache != null && offlineCache.getCachePackageName() != null) {
                    isUpdateOffline = offlineCache.getCachePackageName().equals(context.getPackageName());
                    if (isUpdateOffline) {
                        progressBar.setProgress((int) offlineCache.getCachePercentNum());
                    }
                }
                break;
            case StorageModule.MSG_DOWNLOAD_CACHE_SUCCESS:
                offlineCache = message.getData().getParcelable(OfflineCache.OFFLINE_CACHE);
                if (offlineCache != null && offlineCache.getCachePackageName() != null) {
                    isUpdateOffline = offlineCache.getCachePackageName().equals(context.getPackageName());
                    if (isUpdateOffline) {
                        StorageModule.getInstance().installApp(offlineCache.getCacheStoragePath());
                        okButton.setText(R.string.setting_update_install);
                    }
                }
                break;
            case HomeActivity.MSG_WHAT_ACTIVITY_RESUME:
                onResume();
                break;
            case StorageModule.MSG_NO_NETWORK_TYPE:
                Toast.makeText(context, R.string.settings_version_fail_noNetwork, Toast.LENGTH_SHORT).show();
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
}
