package cn.transpad.transpadui.storage.download;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Message;
import android.text.format.Formatter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.transpad.transpadui.constant.FoneConstant;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.storage.StorageConfig;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.SystemUtil;
import cn.transpad.transpadui.util.TPUtil;
import de.greenrobot.event.EventBus;

/**
 * CacheDownload 流程 DownloadViewControl(UI) <-----> (管理层)CacheDownloadManager
 * <-----> (消息传递层 aidl)DownloadMSGProtocol <----->
 * (service层)CacheDownloadService <----->CacheMessageHandler <-------->
 * fone_service
 *
 * @author Administrator
 */
public class DownloadManager {

    private static final String TAG = DownloadManager.class.getSimpleName();

    private static IDownloadService mICacheDownloadServiceAIDL = null;
    private static Context mContext;
    private ServiceConnection mServiceConnection = null;
    // 延迟操作队列
    private LinkedList<DelayOperate> mDelayOperateList = new LinkedList<DelayOperate>();
    private long mFreeSpaceSize = 0;
    private long mTotalSpaceSize = 0;

    public static void init(Context context) {
        mContext = context;
    }

    private static final DownloadManager mCacheDownloadManager = new DownloadManager();
    ;

    private DownloadManager() {
        EventBus.getDefault().register(this);
    }

    public static DownloadManager getInstance() {

        return mCacheDownloadManager;
    }

    /**
     * 添加缓存文件
     *
     * @param offlineCacheFileList 缓存文件列表
     * @return void
     */
    public void addCacheList(ArrayList<OfflineCache> offlineCacheFileList) {

        // 检查网络
        if (!checkNetwork()) {
            return;
        }

        // 判断存储空间
        if (checkSingleStorageSpace()) {
            return;
        }

//        if (mICacheDownloadServiceAIDL == null) {
//            w(TAG, "addCacheList", "mICacheDownloadServiceAIDL="
//                    + mICacheDownloadServiceAIDL);
//            // 添加一个延迟操作(等待service启动后执行)
//            DelayOperate delayOperate = new DelayOperate();
//            delayOperate.mOfflineCacheList = offlineCacheFileList;
//            mDelayOperateList.add(delayOperate);
//            // 开启service
//            startCacheService();
//            return;
//        }

        v(TAG, "addCacheList", "start size=" + offlineCacheFileList.size());
        Intent intent = new Intent(mContext, CacheDownloadService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(OfflineCache.OFFLINE_CACHE_LIST, offlineCacheFileList);
        bundle.putInt(CacheDownloadService.CACHE_METHOD, CacheDownloadService.ADD_CACHE_LIST);
        intent.putExtras(bundle);
        mContext.startService(intent);
//        try {
//
//            mICacheDownloadServiceAIDL.addCacheList(offlineCacheFileList);
//
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }

        v(TAG, "addCacheList", "end");

    }

    public void pauseCacheAll(int operateState) {

//        try {
//            if (mICacheDownloadServiceAIDL == null) {
//                w(TAG, "pauseCacheAll", "mICacheDownloadServiceAIDL="
//                        + mICacheDownloadServiceAIDL);
//                // 启动服务
//                startCacheService();
//                return;
//            }
//
//            v(TAG, "pauseCacheAll", "start");
//
//            mICacheDownloadServiceAIDL.pauseCacheAll(operateState);
//
//        } catch (RemoteException e) {
//            String message = "";
//            if (e != null) {
//                e.printStackTrace();
//                message = e.getMessage();
//            }
//            e(TAG, "pauseCacheAll", "RemoteException " + message);
//        }
        v(TAG, "pauseCacheAll", "start");
        Intent intent = new Intent(mContext, CacheDownloadService.class);
        Bundle bundle = new Bundle();
        bundle.putInt(OfflineCache.OFFLINE_CACHE_OPERATE, operateState);
        bundle.putInt(CacheDownloadService.CACHE_METHOD, CacheDownloadService.PAUSE_CACHE_ALL);
        intent.putExtras(bundle);
        mContext.startService(intent);
    }

    public void pauseCache(OfflineCache offlineCache) {

//        try {
//            if (mICacheDownloadServiceAIDL == null) {
//                w(TAG, "pauseCache", "mICacheDownloadServiceAIDL="
//                        + mICacheDownloadServiceAIDL);
//                // 启动服务
//                startCacheService();
//                return;
//            }
//
//            mICacheDownloadServiceAIDL.pauseCache(offlineCache);
//
//        } catch (RemoteException e) {
//            String message = "";
//            if (e != null) {
//                e.printStackTrace();
//                message = e.getMessage();
//            }
//            e(TAG, "pauseCache", "RemoteException " + message);
//        }
        v(TAG, "pauseCache", "start");
        Intent intent = new Intent(mContext, CacheDownloadService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(OfflineCache.OFFLINE_CACHE, offlineCache);
        bundle.putInt(CacheDownloadService.CACHE_METHOD, CacheDownloadService.PAUSE_CACHE);
        intent.putExtras(bundle);
        mContext.startService(intent);
    }

    /**
     * 删除下载的缓存(批量删除后,手动调用getCacheAll)
     *
     * @param offlineCacheList 缓存对象列表
     * @return void
     */
    public void deleteCache(ArrayList<OfflineCache> offlineCacheList) {

//        try {
//            if (mICacheDownloadServiceAIDL == null) {
//                w(TAG, "deleteCache", "mICacheDownloadServiceAIDL="
//                        + mICacheDownloadServiceAIDL);
//                // 启动服务
//                startCacheService();
//
//            } else {
//
//                v(TAG, "deleteCache", "start");
//                mICacheDownloadServiceAIDL.deleteCache(offlineCacheList);
//            }
//
//        } catch (RemoteException e) {
//            String message = "";
//            if (e != null) {
//                e.printStackTrace();
//                message = e.getMessage();
//            }
//            e(TAG, "deleteCache", "RemoteException=" + message);
//        }
        v(TAG, "deleteCache", "start");
        Intent intent = new Intent(mContext, CacheDownloadService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(OfflineCache.OFFLINE_CACHE_LIST, offlineCacheList);
        bundle.putInt(CacheDownloadService.CACHE_METHOD, CacheDownloadService.DELETE_CACHE);
        intent.putExtras(bundle);
        mContext.startService(intent);
    }

    public void startCache(OfflineCache offlineCache) {

//        if (mICacheDownloadServiceAIDL == null) {
//            w(TAG, "startCache", "mICacheDownloadServiceAIDL="
//                    + mICacheDownloadServiceAIDL);
//            // 启动服务
//            startCacheService();
//            return;
//        }
//
//        try {
//            v(TAG, "startCache", "start");
//
//            mICacheDownloadServiceAIDL.startCache(offlineCache);
//
//        } catch (RemoteException e) {
//            String message = "";
//            if (e != null) {
//                e.printStackTrace();
//                message = e.getMessage();
//            }
//            e(TAG, "startCache", "RemoteException " + message);
//        }
        v(TAG, "startCache", "start");
        Intent intent = new Intent(mContext, CacheDownloadService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(OfflineCache.OFFLINE_CACHE, offlineCache);
        bundle.putInt(CacheDownloadService.CACHE_METHOD, CacheDownloadService.START_CACHE);
        intent.putExtras(bundle);
        mContext.startService(intent);
    }

    public void startCacheAll() {
        // 检查网络
        if (!checkNetwork()) {
            return;
        }

        // 判断存储空间
        if (checkSingleStorageSpace()) {
            return;
        }
//        if (mICacheDownloadServiceAIDL == null) {
//            w(TAG, "startCacheAll", "mICacheDownloadServiceAIDL="
//                    + mICacheDownloadServiceAIDL);
//            // 启动服务
//            startCacheService();
//            return;
//        }
//        v(TAG, "startCacheAll", "start");
//        try {
//            mICacheDownloadServiceAIDL.startCacheAll();
//        } catch (RemoteException e) {
//            String message = "";
//            if (e != null) {
//                e.printStackTrace();
//                message = e.getMessage();
//            }
//            e(TAG, "startCacheAll", "RemoteException " + message);
//        }
        v(TAG, "startCacheAll", "start");
        Intent intent = new Intent(mContext, CacheDownloadService.class);
        Bundle bundle = new Bundle();
        bundle.putInt(CacheDownloadService.CACHE_METHOD, CacheDownloadService.START_CACHE_ALL);
        intent.putExtras(bundle);
        mContext.startService(intent);
    }

    public void startAuto() {

//        if (mICacheDownloadServiceAIDL == null) {
//            w(TAG, "startAuto", "mICacheDownloadServiceAIDL="
//                    + mICacheDownloadServiceAIDL);
//            // 启动服务
//            startCacheService();
//            return;
//        }
//        v(TAG, "startAuto", "start");
//        try {
//            mICacheDownloadServiceAIDL.startAuto();
//        } catch (RemoteException e) {
//            String message = "";
//            if (e != null) {
//                e.printStackTrace();
//                message = e.getMessage();
//            }
//            e(TAG, "startAuto", "RemoteException " + message);
//        }
        v(TAG, "startCacheAll", "start");
        Intent intent = new Intent(mContext, CacheDownloadService.class);
        Bundle bundle = new Bundle();
        bundle.putInt(CacheDownloadService.CACHE_METHOD, CacheDownloadService.START_AUTO);
        intent.putExtras(bundle);
        mContext.startService(intent);
    }

    /**
     * 检查网络
     */
    public boolean checkNetwork() {
        // 判断网络
        if (DownloadUtil.getNetType() == StorageModule.MSG_NO_NETWORK_TYPE) {
            // 暂时不提示,需要时再解开
            // Message msg = new Message();
            // msg.what = StorageModule.MSG_NO_NETWORK_TYPE;
            // EventBus.getDefault().post(msg);
            return false;
        }
        return true;
    }

    /**
     * 检查存储空间(多次检测)
     *
     * @param currentSize 本次下载的大小
     * @return boolean
     */
    public synchronized void checkManyStorageSpace(long currentSize) {

        if (mFreeSpaceSize == 0) {
            mFreeSpaceSize = DownloadUtil.getStoreFreeSpaceByte();
        } else {

            // 减去当前下载大小
            mFreeSpaceSize = mFreeSpaceSize - currentSize;

        }

        long limitSize = SharedPreferenceModule.getInstance().getLong(
                FoneConstant.TMP_LIMIT_SIZE);

        if (mFreeSpaceSize <= limitSize) {

            long oldLimitSize = SharedPreferenceModule.getInstance().getLong(
                    FoneConstant.TMP_LIMIT_OLD_SIZE);

            Message msg = new Message();
            Bundle bundle = new Bundle();
            // 判断旧的上限是否跟新的上限相同,相同不进行提示,不相同提示
            if (oldLimitSize != limitSize) {
                SharedPreferenceModule.getInstance().setLong(
                        FoneConstant.TMP_LIMIT_OLD_SIZE, limitSize);
                bundle.putBoolean(
                        OfflineCache.OFFLINE_CACHE_IS_SHOW_LIMIT_SPACE, true);

            } else {
                bundle.putBoolean(
                        OfflineCache.OFFLINE_CACHE_IS_SHOW_LIMIT_SPACE, false);
            }
            msg.setData(bundle);
            msg.what = StorageModule.MSG_MIN_SPACE;
            EventBus.getDefault().post(msg);
            e(TAG,
                    "checkManyStorageSpace",
                    "SDCard limit StorageModule.MSG_MIN_SPACE limitSize="
                            + Formatter.formatFileSize(mContext, limitSize)
                            + " mFreeSpaceSize="
                            + Formatter
                            .formatFileSize(mContext, mFreeSpaceSize));

        }

        // 发送实时存储进度信息
        if (mTotalSpaceSize == 0) {
            mTotalSpaceSize = DownloadUtil.getStoreTotalSpaceByte();
        }

        int stroagePrecents = 0;
        if (mTotalSpaceSize == 0) {
            stroagePrecents = 0;
        } else {
            stroagePrecents = (int) (100 * (mTotalSpaceSize - mFreeSpaceSize) / mTotalSpaceSize);
        }

        String spaceLeft = SystemUtil.getInstance().getFreeSpaceFormat(
                mFreeSpaceSize);

        // 调试时使用,勿删
        // L.v(TAG, "checkManyStorageSpace", "currentSize=" + currentSize
        // + " mFreeSpaceSize=" + spaceLeft + " mFreeSpaceSize="
        // + mFreeSpaceSize + " mTotalSpaceSize=" + mTotalSpaceSize
        // + " limitSize=" + limitSize);

//        Message message = new Message();
//        MemoryData memoryData = new MemoryData();
//        memoryData.setSpaceLeft(spaceLeft);
//        memoryData.setStroagePrecents(stroagePrecents);
//
//        message.what = StorageModule.MSG_STORAGE_SPACE_PROGRESS;
//        message.obj = memoryData;
//        EventBus.getDefault().post(message);

    }

    /**
     * 检查存储空间(仅限检测一次)
     *
     * @return boolean
     */
    public synchronized boolean checkSingleStorageSpace() {

        boolean isLimit = false;

        long limitSize = SharedPreferenceModule.getInstance().getLong(
                FoneConstant.TMP_LIMIT_SIZE);

        mFreeSpaceSize = DownloadUtil.getStoreFreeSpaceByte();

        if (mFreeSpaceSize <= limitSize) {
            Message msg = new Message();
            msg.what = StorageModule.MSG_MIN_SPACE;
            Bundle bundle = new Bundle();
            bundle.putBoolean(OfflineCache.OFFLINE_CACHE_IS_SHOW_LIMIT_SPACE,
                    true);
            msg.setData(bundle);
            EventBus.getDefault().post(msg);
            e(TAG, "checkSingleStorageSpace",
                    "SDCard limit StorageModule.MSG_MIN_SPACE  limitSize="
                            + Formatter.formatFileSize(mContext, limitSize));
            isLimit = true;
        } else {
            isLimit = false;
        }

        // 发送实时存储进度信息
        mTotalSpaceSize = DownloadUtil.getStoreTotalSpaceByte();

        int stroagePrecents = 0;
        if (mTotalSpaceSize == 0) {
            stroagePrecents = 0;
        } else {
            stroagePrecents = (int) (100 * (mTotalSpaceSize - mFreeSpaceSize) / mTotalSpaceSize);
        }

        String spaceLeft = SystemUtil.getInstance().getFreeSpaceFormat(
                mFreeSpaceSize);

        L.v(TAG, "checkSingleStorageSpace", "mFreeSpaceSize=" + spaceLeft
                + " mFreeSpaceSize=" + mFreeSpaceSize + " mTotalSpaceSize="
                + mTotalSpaceSize + " limitSize=" + limitSize);

//        Message message = new Message();
//        MemoryData memoryData = new MemoryData();
//        memoryData.setSpaceLeft(spaceLeft);
//        memoryData.setStroagePrecents(stroagePrecents);
//
//        message.what = StorageModule.MSG_STORAGE_SPACE_PROGRESS;
//        message.obj = memoryData;
//        EventBus.getDefault().post(message);

        return isLimit;
    }

    public void onEventMainThread(Message msg) {
        String text = "";
        Bundle bundle = null;
        switch (msg.what) {
            case StorageModule.MSG_NO_NETWORK_TYPE:
                // 暂停所有
                // StorageModule.getInstance().pauseAllCache();
                v(TAG, "onEventMainThread", "MSG_NO_NETWORK_TYPE");
                // 无网
                //FoneUtil.showToast(mContext, R.string.no_network_toast);

                break;
            case StorageModule.MSG_WIFI_NETWORK_TYPE:
                v(TAG, "onEventMainThread", "MSG_WIFI_NETWORK_TYPE");
                // 开启所有
                StorageModule.getInstance().startAllCache();
                // 您当前处于wifi网络环境下
                //FoneUtil.showToast(mContext, R.string.network_wifi_toast);

                break;
            case StorageModule.MSG_2G_NETWORK_TYPE:
            case StorageModule.MSG_3G_NETWORK_TYPE:
            case StorageModule.MSG_4G_NETWORK_TYPE:

                // 获取收藏和缓存界面的"仅wifi缓存按钮"切换状态
                boolean isAutoDownload = SharedPreferenceModule.getInstance()
                        .getBoolean(FoneConstant.AUTO_DOWNLOAD_FLAG_SP, true);

                v(TAG, "onEventMainThread",
                        "MSG_UNWIFI_NETWORK_TYPE isAutoDownload=" + isAutoDownload);

                // 判断是否下载
                if (isAutoDownload) {

                    // 暂停所有(仅wifi开)
                    StorageModule.getInstance().pauseAllCache(
                            OfflineCache.OPERATE_STOP_BATCH_WAIT_STATE);

                } else {

                    // 开启所有(仅wifi关)
                    StorageModule.getInstance().startAllCache();

                }
                // 您当前处于非wifi网络环境下，继续使用可能产生流量
                //FoneUtil.showToast(mContext, R.string.network_unwifi_toast);
                break;
            case StorageModule.MSG_STORAGE_MOUNTED_SUCCESS:

                v(TAG, "onEventMainThread", "MSG_STORAGE_MOUNTED_SUCCESS");

                // 存储器挂载成功
                StorageModule.getInstance().startAllCache();
                break;
            case StorageModule.MSG_STORAGE_MOUNTED_FAIL:

                v(TAG, "onEventMainThread", "MSG_STORAGE_MOUNTED_FAIL");

                // 存储器挂载失败
                //FoneUtil.showToast(mContext, R.string.no_sdcard_mounted_text);

                break;
            case StorageModule.MSG_MIN_SPACE:
                L.v(TAG, "onEventMainThread", "MSG_MIN_SPACE");

                bundle = msg.getData();
                if (bundle != null) {
                    boolean isShowLimitSpace = bundle
                            .getBoolean(OfflineCache.OFFLINE_CACHE_IS_SHOW_LIMIT_SPACE);
                    if (isShowLimitSpace) {

                        text = "存储空间不足，请进行清理";
                        L.v(TAG, "onEventMainThread", "text=" + text);
                        TPUtil.showToast(text);
                    }

                }

                // 暂停所有
                StorageModule.getInstance().pauseAllCache(
                        OfflineCache.OPERATE_STOP_BATCH_PAUSE_STATE);

                break;
            case StorageModule.MSG_MAX_DOWNLOAD:
                text = "最多只能缓存到100集，请稍后添加";
                Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                break;
            case StorageModule.MSG_DOWNLOAD_PREPARE_ONLINE_CACHE_SUCCESS:
                synchronized (TAG) {
                    // 边下边播,已下载15%以上
                    boolean tip = SharedPreferenceModule.getInstance().getBoolean(
                            "cache_online_success");
                    if (!tip) {

                        text = "您缓存的视频可以播放了";
                        Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
                        v(TAG, "onEventMainThread", text);
                        // 保存状态,重复cid不提示
                        SharedPreferenceModule.getInstance().setBoolean(
                                "cache_online_success", true);
                    }

                }
                break;
            default:
                break;
        }
    }

    public void startCacheService() {
        v(TAG, "startCacheService", "start");

        // 打印栈
        //StorageModule.getInstance().writeStackTrace();

//        if (isServiceStarted(mContext, CacheDownloadService.class)) {
//            v(TAG, "startCacheService", "isServiceStarted=true");
//            return;
//        }

        Intent serviceIntent = new Intent();
        serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        serviceIntent.setClass(mContext, CacheDownloadService.class);
        mContext.startService(serviceIntent);
    }

    public void stopCacheService() {
        v(TAG, "stopCacheService", "start");
        Intent serviceIntent = new Intent();
        serviceIntent.setClass(mContext, CacheDownloadService.class);
        mContext.stopService(serviceIntent);
    }

    public boolean isServiceStarted(Context context,
                                    Class<CacheDownloadService> clazz) {
        v(TAG, "isStarted", "isStarted method start");

        if (null == mICacheDownloadServiceAIDL) {
            return false;
        }


        boolean shown = false;
        String className = clazz.getName();
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> serviceList = activityManager
                .getRunningServices(Integer.MAX_VALUE);

        for (int i = 0; !shown && i < serviceList.size(); i++) {
            RunningServiceInfo serviceInfo = serviceList.get(i);
            ComponentName serviceName = serviceInfo.service;

            if (serviceName.getClassName().equals(className)) {
                // v(TAG, "Service " + className + " running ! ");
                shown = true;
            }
        }
        v(TAG, "isServiceStarted", "isRuning=" + String.valueOf(shown));

        return shown;
    }

    public static class DelayOperate {
        public ArrayList<OfflineCache> mOfflineCacheList;
    }

    private void v(String tag, String type, String msg) {
        if (StorageConfig.CACHE_DOWNLOAD_MANAGER_LOG) {
            L.v(tag, type, msg);
        }
    }

    private void w(String tag, String type, String msg) {
        if (StorageConfig.CACHE_DOWNLOAD_MANAGER_LOG) {
            L.w(tag, type, msg);
        }
    }

    private void e(String tag, String type, String msg) {
        if (StorageConfig.CACHE_DOWNLOAD_MANAGER_LOG) {
            L.e(tag, type, msg);
        }
    }
}
