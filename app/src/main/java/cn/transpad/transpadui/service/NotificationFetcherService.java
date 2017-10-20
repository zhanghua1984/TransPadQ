package cn.transpad.transpadui.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.Intent;
import android.os.Message;
import android.view.accessibility.AccessibilityEvent;

import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.util.L;
import de.greenrobot.event.EventBus;

/**
 * Created by Kongxiaojun on 2015/4/10.
 */
public class NotificationFetcherService extends AccessibilityService {
    private static final String TAG = "NotificationFetcherService";

    public static final int MSG_WHAT_RECEIVER_NOTIFICATION = 10099;

    public static final String NOTIFICATION_ACCESSIBILITY_SHAREREFECES = "notification_accessibility_sharerefeces";

    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!(event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)) {
            return;
        }
        Notification localNotification = (Notification) event.getParcelableData();
        if (localNotification != null) {
            L.v(TAG, "receive notification : " + localNotification.tickerText);
            if (localNotification.flags == (localNotification.flags | Notification.FLAG_AUTO_CANCEL)) {
                //可以删除的通知,进行显示
                L.v(TAG, "receive notification is FLAG_AUTO_CANCEL : " + localNotification.tickerText);
                Message message = new Message();
                message.what = MSG_WHAT_RECEIVER_NOTIFICATION;
                message.obj = localNotification;
                EventBus.getDefault().post(message);
            }
        }

    }

    @Override
    protected void onServiceConnected() {
        // Define it in both xml file and here,  for compatibility with pre-ICS devices
        SharedPreferenceModule.getInstance().setBoolean(NOTIFICATION_ACCESSIBILITY_SHAREREFECES,true);
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED |
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
    }

    @Override
    public void onInterrupt() {
        L.v(TAG, "onInterrupt");
//        SharedPreferenceModule.getInstance().setBoolean(NOTIFICATION_ACCESSIBILITY_SHAREREFECES,false);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        L.v(TAG, "onUnbind");
        SharedPreferenceModule.getInstance().setBoolean(NOTIFICATION_ACCESSIBILITY_SHAREREFECES,false);
        return super.onUnbind(intent);
    }
}
