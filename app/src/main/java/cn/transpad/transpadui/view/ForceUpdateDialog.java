package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import de.greenrobot.event.EventBus;

/**
 * Created by ctccuser on 2015/4/6.
 */
public class ForceUpdateDialog extends Dialog {
    public static final String TAG = ForceUpdateDialog.class.getSimpleName();
    String updateDescription;
    String updateUrl;
    boolean flag = false;

    private ClickListener listener;

    @InjectView(R.id.settings_update_force_dialog_message)
    TextView updateMessage;
    @InjectView(R.id.settings_update_force_dialog_progressbar)
    ProgressBar progressBar;
    @InjectView(R.id.settings_update_force_dialog_ok)
    Button button;
//    @InjectView(R.id.settings_update_force_dialog_install)
//    Button installButton;

    public ForceUpdateDialog(Context context) {
        super(context);
    }

    public ForceUpdateDialog(Context context, int theme) {
        super(context, theme);
    }

    public void setUpdateMessage(String updateDescription, String updateUrl) {
        this.updateDescription = updateDescription;
        this.updateUrl = updateUrl;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.v(TAG, "onCreate");

        setContentView(R.layout.update_force_dialog);
        ButterKnife.inject(this);
        updateMessage.setText(updateDescription);

//        String filePath = SystemUtil.getInstance().getOfflineCachePath();
//        File file = new File(filePath, name + ".apk");
        if (isExist()) {
//            StorageModule.getInstance().installApp(file.getPath());
            button.setText(R.string.setting_update_install);

        } else {
            button.setText(R.string.setting_update_ok);

        }


    }

    public boolean isExist() {
        return flag;
    }

    public void setIsExist(boolean flag) {
        this.flag = flag;
//        if (flag) {
//            button.setText(R.string.setting_update_force_install);
//        } else {
//            button.setText(R.string.setting_update_force_ok);
//        }
    }

    @OnClick(R.id.settings_update_force_dialog_ok)
    public void updateOk() {
        if (listener != null) {
            listener.onOk();
        }
    }

//    @OnClick(R.id.settings_update_force_dialog_install)
//    public void install() {
//        if (listener != null) {
//            listener.onInstall();
//        }
//    }


    @Override
    public void onBackPressed() {
//        返回按钮失效
//        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        L.v(TAG, "onStart");
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        L.v(TAG, "onStop");
        EventBus.getDefault().unregister(this);

    }


    public void onEventMainThread(Message message) {
        OfflineCache offlineCache = message.getData().getParcelable(OfflineCache.OFFLINE_CACHE);
        switch (message.what) {
            case StorageModule.MSG_FILE_CACHE_UPDATE_PROGRESS_SUCCESS:
                L.v(TAG, "onEventMainThread");
                progressBar.setProgress((int) offlineCache.getCachePercentNum());
                button.setEnabled(false);
                break;
            case StorageModule.MSG_DOWNLOAD_CACHE_SUCCESS:
                StorageModule.getInstance().installApp(offlineCache.getCacheStoragePath());
//                button.setText(R.string.setting_update_force_install);
                button.setEnabled(true);
                break;
        }
    }

    public void setClickListener(ClickListener listener) {
        this.listener = listener;
    }


    public interface ClickListener {

        void onOk();

//        void onInstall();
    }
}
