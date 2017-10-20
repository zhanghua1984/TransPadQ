package cn.transpad.transpadui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

import cn.transpad.transpadui.util.L;
import de.greenrobot.event.EventBus;

/**
 * 实现应用互斥
 * Created by user on 2015/12/22.
 */
public class SuicideReceiver extends BroadcastReceiver {
    private static final String TAG = "SuicideReceiver";
    public static final String ACTION = "cn.transpad.transpadui.receiver.SuicideReceiver";
    public static final String PACKAGE_NAME = "package_name";
    public static final int EXIT_APP = 20000;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            String packageName = context.getPackageName();
            String currentPackageName = intent.getStringExtra(PACKAGE_NAME);
            if (!packageName.equals(currentPackageName)) {
                L.v(TAG, "onReceive", "keep currentPackageName=" + currentPackageName + " close packageName=" + packageName);
                Message msg = new Message();
                msg.what = EXIT_APP;
                EventBus.getDefault().post(msg);
            }
        }
    }
}
