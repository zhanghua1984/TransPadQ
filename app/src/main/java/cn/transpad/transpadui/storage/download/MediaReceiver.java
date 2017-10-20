package cn.transpad.transpadui.storage.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import de.greenrobot.event.EventBus;

/**
 * 监听sdcard变化
 *
 * @author wangyang
 * @since 2014-9-25
 */
public class MediaReceiver extends BroadcastReceiver {
    private static final String TAG = MediaReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        L.v(TAG, "onReceive", "start action=" + action);
        if (Intent.ACTION_MEDIA_REMOVED.equals(action)
                || Intent.ACTION_MEDIA_UNMOUNTED.equals(action)
                || Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action)
                || Intent.ACTION_MEDIA_EJECT.equals(action)
                || Intent.ACTION_MEDIA_SHARED.equals(action)
                || Intent.ACTION_MEDIA_UNMOUNTABLE.equals(action)) {
            // SD卡挂载失败
            L.e(TAG, "onReceive", "storage mounted fail");
//			// 清理之前的通知栏
//			List<Download> downloadList = FileDownloadDataBaseAdapter
//					.getInstance().getDownloadList(Download.DOWNLOAD_STATE_ALL);
//			if (downloadList != null) {
//				NotificationManager notificationManager = (NotificationManager) context
//						.getSystemService(Context.NOTIFICATION_SERVICE);
//				for (Download download : downloadList) {
//					notificationManager.cancel(download
//							.getDownloadNotificationId());
//				}
//			}

            Message message = new Message();
            message.what = StorageModule.MSG_STORAGE_MOUNTED_FAIL;
            EventBus.getDefault().post(message);

        } else {

            L.v(TAG, "onReceive", "storage mounted success");
            Message message = new Message();
            message.what = StorageModule.MSG_STORAGE_MOUNTED_SUCCESS;
            EventBus.getDefault().post(message);
        }

    }

}
