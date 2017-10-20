package cn.transpad.transpadui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

import cn.transpad.transpadui.util.L;
import de.greenrobot.event.EventBus;

/**
 * Created by Kongxiaojun on 2015/4/20.
 */
public class TPApplicationReceiver extends BroadcastReceiver {

    private static final String TAG = "TPApplicationReceiver";

    public static final int MSG_WHAT_APPLICATION_INSTALL = 4501;
    public static final int MSG_WHAT_APPLICATION_UNINSTALL = 4502;

    @Override
    public void onReceive(Context context, Intent intent) {
        L.v(TAG,"onReceive");
        int what = -1;
        String packageName = null;
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)){
            what = MSG_WHAT_APPLICATION_UNINSTALL;
            packageName = intent.getDataString();
            L.v(TAG,"ACTION_PACKAGE_REMOVED " + packageName);
        }else if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)){
            what = MSG_WHAT_APPLICATION_INSTALL;
            packageName = intent.getDataString();
            L.v(TAG,"ACTION_PACKAGE_ADDED " + packageName);
        }
        if (what > 0){
            if (packageName.startsWith("package:")){
                packageName = packageName.replaceAll("package:","");
            }
            Message msg = new Message();
            msg.what = what;
            msg.obj = packageName;
            EventBus.getDefault().post(msg);
        }
    }
}
