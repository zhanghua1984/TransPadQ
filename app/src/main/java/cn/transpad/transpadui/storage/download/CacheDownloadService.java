package cn.transpad.transpadui.storage.download;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import java.util.ArrayList;

import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.storage.OfflineCacheDataBaseAdapter;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;
import de.greenrobot.event.EventBus;

public class CacheDownloadService extends Service {
    public final static int ADD_CACHE_LIST = 1;
    public final static int PAUSE_CACHE_ALL = 2;
    public final static int PAUSE_CACHE = 3;
    public final static int DELETE_CACHE = 4;
    public final static int START_CACHE = 5;
    public final static int START_CACHE_ALL = 6;
    public final static int START_AUTO = 7;
    public final static String CACHE_METHOD = "cache_method";
    private int method = 0;
    private static final String TAG = CacheDownloadService.class
            .getSimpleName();
    private static DownloadMessageProtocol mDownloadMSGProtocol = null;
    private static Context sContext = null;

    public IBinder onBind(Intent intent) {

        if (mDownloadMSGProtocol != null) {
            return mDownloadMSGProtocol.mServiceStub;
        } else {
            L.e(TAG, "onBind", "mDownloadMSGProtocol=null");
            return null;
        }

    }

    public void onCreate() {
        super.onCreate();

        sContext = this;

        // 申请电源锁
        CPUWakeLock.acquireCpuWakeLock(this);

        v(TAG, "onCreate", "start");

        v(TAG, "onCreate",
                "init DownloadOfflineCacheManager mDownloadMSGProtocol="
                        + mDownloadMSGProtocol);

        OfflineCacheDownloadManager.init();

        // 清理之前的通知栏
        v(TAG, "onCreate", "clear scrap Notification");

//		List<Download> downloadList = FileDownloadDataBaseAdapter.getInstance()
//				.getDownloadList(Download.DOWNLOAD_STATE_ALL);
//		if (downloadList != null) {
//			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//			for (Download download : downloadList) {
//				notificationManager
//						.cancel(download.getDownloadNotificationId());
//			}
//		}

        v(TAG, "onCreate", "initQueue start");

        // 初始化下载
        OfflineCacheDownloadManager.getInstance().initQueue();

    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent != null) {
            method = intent.getIntExtra(CacheDownloadService.CACHE_METHOD, 0);
            ArrayList<OfflineCache> offlineCacheList = null;
            OfflineCache offlineCache = null;
            Message msg = new Message();
            Bundle bundle = new Bundle();
            switch (method) {
                case ADD_CACHE_LIST:
                    L.v(TAG, "onStartCommand", "ADD_CACHE_LIST");
                    offlineCacheList = intent.getParcelableArrayListExtra(OfflineCache.OFFLINE_CACHE_LIST);
                    OfflineCacheDownloadManager.getInstance().addList(offlineCacheList);

                    // 建立第二级目录(新建文件)
                    OfflineCacheDataBaseAdapter.getInstance().addOfflineCacheFileList(
                            offlineCacheList);

                    // 发送成功消息
                    msg.what = StorageModule.MSG_ADD_CACHE_SUCCESS;
                    bundle.putParcelableArrayList(OfflineCache.OFFLINE_CACHE_LIST,
                            offlineCacheList);
                    msg.setData(bundle);
                    EventBus.getDefault().post(msg);
                    break;
                case PAUSE_CACHE_ALL:
                    L.v(TAG, "onStartCommand", "PAUSE_CACHE_ALL");
                    int operate = intent.getIntExtra(OfflineCache.OFFLINE_CACHE_OPERATE, 0);
                    OfflineCacheDownloadManager.getInstance().pauseAll(operate);
                    msg.what = StorageModule.MSG_CACHE_PAUSE_ALL_SUCCESS;
                    EventBus.getDefault().post(msg);
                    break;
                case PAUSE_CACHE:
                    L.v(TAG, "onStartCommand", "PAUSE_CACHE");
                    offlineCache = intent.getParcelableExtra(OfflineCache.OFFLINE_CACHE);
                    OfflineCacheDownloadManager.getInstance().pause(offlineCache);
                    break;
                case DELETE_CACHE:
                    L.v(TAG, "onStartCommand", "DELETE_CACHE");
                    offlineCacheList = intent.getParcelableArrayListExtra(OfflineCache.OFFLINE_CACHE_LIST);
                    OfflineCacheDownloadManager.getInstance().delete(offlineCacheList);
                    break;
                case START_CACHE:
                    L.v(TAG, "onStartCommand", "START_CACHE");
                    offlineCache = intent.getParcelableExtra(OfflineCache.OFFLINE_CACHE);
                    OfflineCacheDownloadManager.getInstance().start(offlineCache);
                    break;
                case START_CACHE_ALL:
                    L.v(TAG, "onStartCommand", "START_CACHE_ALL");
                    OfflineCacheDownloadManager.getInstance().startAll();
                    msg.what = StorageModule.MSG_CACHE_START_ALL_SUCCESS;
                    EventBus.getDefault().post(msg);
                    break;
                case START_AUTO:
                    L.v(TAG, "onStartCommand", "START_AUTO");
                    OfflineCacheDownloadManager.getInstance().startAuto();
                    break;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();

        v(TAG, "onDestroy", "start");

        // 释放电源锁
        CPUWakeLock.releaseCpuWakeLock();

        // 关闭正在下载的通知栏
        // NotificationHandle.getInstance().closeNotification();

        v(TAG, "onDestroy", "exit(0)");
        System.exit(0);

    }

    /**
     * 判断是否有网络
     */
    public static boolean isNetwork() {
        // L.v(TAG, "isNetwork", "sContext=" + sContext);
        return TPUtil.isNetOk();
    }

    public OfflineCacheDownloadManager getDownloadOfflineCacheManager() {
        return OfflineCacheDownloadManager.getInstance();
    }

    public static void v(String TAG, String type, String msg) {
        L.v(TAG, type, msg);
    }

    public static void e(String TAG, String type, String msg) {
        L.e(TAG, type, msg);
    }
}
