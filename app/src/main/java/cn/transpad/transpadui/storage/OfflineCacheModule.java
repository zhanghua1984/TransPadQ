package cn.transpad.transpadui.storage;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.transpad.transpadui.constant.FoneConstant;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.storage.download.DownloadConstant;
import cn.transpad.transpadui.storage.download.DownloadManager;
import cn.transpad.transpadui.storage.download.DownloadUtil;
import cn.transpad.transpadui.storage.download.OfflineCacheDownloadManager;
import cn.transpad.transpadui.storage.download.OfflineCachePacketDownloadManager;
import cn.transpad.transpadui.storage.download.StorageManager;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.SystemUtil;
import de.greenrobot.event.EventBus;

public class OfflineCacheModule {

    private static final String TAG = OfflineCacheModule.class.getSimpleName();
    private static final OfflineCacheModule mInstance = new OfflineCacheModule();
    /**
     * 缓存界面存储空间进度信息(内部使用)
     */
    public static final int MSG_INTERNAL_CACHE_SPACE_SIZE_PROGRESS = 1102;
    /**
     * 缓存界面中文件删除返回成功(内部使用)
     */
    public static final int MSG_INTERNAL_FILE_CACHE_DELETE_SUCCESS = 1103;
    /**
     * 缓存界面中文件下载出错(内部使用)
     */
    public static final int MSG_INTERNAL_FILE_CACHE_ERROR = 1104;
    /**
     * 缓存界面(内部使用)
     */
    public static final int MSG_INTERNAL_CACHE = 1105;
    /**
     * 收藏界面(内部使用)
     */
    public static final int MSG_INTERNAL_FAVOURITE = 1106;
    /**
     * 开始全部(内部使用)
     */
    public static final int MSG_INTERNAL_START_ALL = 1107;
    /**
     * 暂停全部(内部使用)
     */
    public static final int MSG_INTERNAL_PAUSE_ALL = 1108;
    // 是否屏蔽开始全部操作
    private boolean mIsShieldStartAllOperate = false;
    // 是否屏蔽暂停全部操作
    private boolean mIsShieldPauseAllOperate = false;
    // 删除操作是否成功回调
    private boolean mIsDeleteSuccess = false;
    // 添加成功回调
    private boolean mIsAddSuccess = false;
    private LinkedList<CacheOperate> mOperateQueueList = new LinkedList<CacheOperate>();
    private EventBus mEventBus = null;

    private OfflineCacheModule() {
        EventBus.getDefault().register(this);
    }

    public static OfflineCacheModule getInstance() {
        return mInstance;
    }

    public static void init(Context context) {
        L.v(TAG, "init", "start");
        StorageManager.init(context);
        DownloadManager.init(context);
        OperateDataBaseTemplate.init(context);
        DownloadUtil.init(context);
    }

    public void setEventBus(EventBus eventBus) {
        mEventBus = eventBus;
    }

    /**
     * 添加缓存条目集合<br>
     *
     * @param waitOfflineCacheFileList 等待添加的离线缓存列表
     * @return int 插入结果<br>
     * 1 成功 <br>
     * -1 插入异常
     * @throws NullPointerException offlineCache为null
     */
    public int addCacheList(final ArrayList<OfflineCache> waitOfflineCacheFileList) {
        L.v(TAG, "addCacheList", "start");
        try {
            // 获取实际可添加的缓存列表
            //List<OfflineCache> actualOfflineCacheFileList = getActualOfflineCacheList(waitOfflineCacheFileList);
            // /Transpad/file目录
            String offlineCacheRoot = SystemUtil.getInstance()
                    .getOfflineCachePath();
            if (waitOfflineCacheFileList != null
                    && waitOfflineCacheFileList.size() > 0) {

                for (OfflineCache offlineCache : waitOfflineCacheFileList) {
                    L.v(TAG, "addCacheList", "name=" + offlineCache.getCacheName());
                    // file里面的目录
                    String storagePath = offlineCacheRoot + File.separator + offlineCache.getCacheName() + ".apk";
                    offlineCache.setCacheStoragePath(storagePath);
                    offlineCache
                            .setCacheDownloadState(OfflineCache.CACHE_STATE_WAITING);

                }
                // 添加下载
                DownloadManager.getInstance().addCacheList(
                        waitOfflineCacheFileList);
            }

        } catch (Exception e) {

        }
        return 1;
    }

    /**
     * 获取实际可添加的离线缓存列表
     *
     * @param offlineCacheFileList 用户选择的离线缓存列表
     * @return List<OfflineCache>
     */
    public List<OfflineCache> getActualOfflineCacheList(
            List<OfflineCache> offlineCacheFileList) {
        boolean isTip = false;
        List<OfflineCache> actOfflineCacheFileList = new ArrayList<OfflineCache>();

        int fileCount = OfflineCacheDataBaseAdapter.getInstance()
                .getOfflineCacheFileCount();
        int waitCacheCount = offlineCacheFileList.size();
        v(TAG, "getActualOfflineCacheList", "fileCount=" + fileCount
                + " waitCacheCount=" + waitCacheCount);
        // 最多添加100个任务(包括已完成的任务),大于100忽略下载
        if (fileCount >= DownloadConstant.MAX_DOWNLOAD_NUM) {

            // 剩余的空位
            L.w(TAG, "getActualOfflineCacheList",
                    "fileCount limit DownloadConstant.MAX_DOWNLOAD_NUM fileCount="
                            + fileCount);
            isTip = true;

        } else {

            if (waitCacheCount > DownloadConstant.MAX_DOWNLOAD_NUM) {
                L.w(TAG, "getActualOfflineCacheList",
                        "waitCacheCount limit DownloadConstant.MAX_DOWNLOAD_NUM waitCacheCount="
                                + waitCacheCount);
                isTip = true;
            }

            int surplusCount = DownloadConstant.MAX_DOWNLOAD_NUM - fileCount;

            // 多退少补
            if (waitCacheCount < surplusCount) {
                surplusCount = waitCacheCount;
            }

            L.v(TAG, "getActualOfflineCacheList", "surplusCount="
                    + surplusCount);

            // /100tv/video目录
            String offlineCacheRoot = SystemUtil.getInstance()
                    .getOfflineCachePath();

            // 全部添加
            for (int i = 0; i < surplusCount; i++) {

                OfflineCache offlineCache = offlineCacheFileList.get(i);

                // video里面的目录
                String offlineCachePath = OfflineCachePacketDownloadManager
                        .getInstance().getOfflineCacheFileStoragePath(
                                offlineCacheRoot, offlineCache);

                offlineCache.setCacheStoragePath(offlineCachePath);
                offlineCache
                        .setCacheDownloadState(OfflineCache.CACHE_STATE_WAITING);

                actOfflineCacheFileList.add(offlineCache);
            }

        }

        if (isTip) {
            L.v(TAG, "getActualOfflineCacheList", "send MSG_MAX_DOWNLOAD");
            Message msg = new Message();
            msg.what = StorageModule.MSG_ACTION_SCANNER_VIDEO_STARTED;
            EventBus.getDefault().post(msg);
        }

        return actOfflineCacheFileList;
    }

    /**
     * 开始一个条目的下载
     *
     * @return void
     */
    public void startCache(final OfflineCache offlineCache) {
        v(TAG, "startCache", "start id=" + offlineCache.getCacheID());
        new Thread(new Runnable() {

            @Override
            public void run() {

                // 判断存储空间
                if (DownloadManager.getInstance().checkSingleStorageSpace()) {
                    // 状态设置为暂停
                    offlineCache
                            .setCacheDownloadState(OfflineCache.CACHE_STATE_PAUSE);
                    mergeOfflineCache(offlineCache);
                } else {

                    DownloadManager.getInstance().startCache(offlineCache);

                }
            }
        }).start();
    }

    /**
     * 开始全部下载
     *
     * @return void
     */
    public void startCacheAll() {
        if (mIsShieldStartAllOperate) {
            L.w(TAG, "startCacheAll", "startCacheAll is running");
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                L.v(TAG, "startCacheAll", "start startCacheAll");
                mIsShieldStartAllOperate = true;
                DownloadManager.getInstance().startCacheAll();
            }
        }).start();

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                // 10秒超时
                mIsShieldStartAllOperate = false;
                mIsShieldPauseAllOperate = false;
            }
        }, 10000);
    }

    /**
     * 添加到开始全部下载队列
     *
     * @return void
     */
    public synchronized void startCacheAllQueue() {

        if (mIsShieldStartAllOperate) {

            L.v(TAG, "startCacheAllQueue", "operate repeat,clear operate queue");
            mOperateQueueList.clear();

        } else {

            CacheOperate cacheOperate = new CacheOperate();
            cacheOperate.mCacheOperateType = MSG_INTERNAL_START_ALL;
            mOperateQueueList.add(cacheOperate);
            L.v(TAG, "startCacheAllQueue", "add operate queue");

            if (!mIsShieldPauseAllOperate && mOperateQueueList.size() == 1) {
                L.v(TAG, "startCacheAllQueue", "running first operate");
                CacheOperate cacheOperateTemp = mOperateQueueList.removeFirst();
                switch (cacheOperateTemp.mCacheOperateType) {
                    case MSG_INTERNAL_START_ALL:
                        startCacheAll();
                        break;
                    default:
                        break;
                }
            } else {
                L.w(TAG, "startCacheAllQueue", "mIsShieldPauseAllOperate="
                        + mIsShieldPauseAllOperate + " mOperateQueueList.size="
                        + mOperateQueueList.size());
            }
        }

    }

    /**
     * 添加到暂停全部下载队列
     *
     * @return void
     */
    public void pauseCacheAllQueue(final int operateState) {

        if (mIsShieldPauseAllOperate) {

            mOperateQueueList.clear();
            L.v(TAG, "pauseCacheAllQueue", "operate repeat,clear operate queue");
        } else {

            CacheOperate cacheOperate = new CacheOperate();
            cacheOperate.mCacheOperateType = MSG_INTERNAL_PAUSE_ALL;
            cacheOperate.mOperateState = operateState;
            mOperateQueueList.add(cacheOperate);
            L.v(TAG, "pauseCacheAllQueue", "add operate queue");
            if (!mIsShieldStartAllOperate && mOperateQueueList.size() == 1) {
                L.v(TAG, "pauseCacheAllQueue", "running first operate");
                CacheOperate cacheOperateTemp = mOperateQueueList.removeFirst();
                switch (cacheOperateTemp.mCacheOperateType) {
                    case MSG_INTERNAL_PAUSE_ALL:
                        pauseCacheAll(cacheOperateTemp.mOperateState);
                        break;
                    default:
                        break;
                }
            }
        }

    }

    /**
     * 操作等待的操作队列
     *
     * @return void
     */
    public void operateCacheQueue() {
        if (mOperateQueueList.size() == 0) {
            L.v(TAG, "operateCacheQueue", "no operate,OperateQueue size=0");
            return;
        }

        CacheOperate cacheOperateTemp = mOperateQueueList.removeFirst();
        if (cacheOperateTemp != null) {
            switch (cacheOperateTemp.mCacheOperateType) {
                case MSG_INTERNAL_START_ALL:
                    L.v(TAG, "operateCacheQueue",
                            "running MSG_INTERNAL_START_ALL operate");
                    startCacheAll();
                    break;
                case MSG_INTERNAL_PAUSE_ALL:
                    L.v(TAG, "operateCacheQueue",
                            "running MSG_INTERNAL_PAUSE_ALL operate");
                    pauseCacheAll(cacheOperateTemp.mOperateState);
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * 暂停一个条目的下载
     *
     * @return void
     */
    public void pauseCache(final OfflineCache offlineCache) {

        v(TAG, "pauseCache", "start cid=" + offlineCache.getCacheID());

        new Thread(new Runnable() {

            @Override
            public void run() {

                DownloadManager.getInstance().pauseCache(offlineCache);

            }
        }).start();

    }

    /**
     * 暂停所有条目的下载
     *
     * @return void
     */
    public void pauseCacheAll(final int operateState) {
        if (mIsShieldPauseAllOperate) {
            L.w(TAG, "pauseCacheAll", "pauseCacheAll is running");
            return;
        }
        new Thread(new Runnable() {

            @Override
            public void run() {
                L.v(TAG, "pauseCacheAll", "start pauseCacheAll");
                mIsShieldPauseAllOperate = true;
                DownloadManager.getInstance().pauseCacheAll(operateState);
            }
        }).start();

        // 10秒超时
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                mIsShieldStartAllOperate = false;
                mIsShieldPauseAllOperate = false;
            }
        }, 10000);
    }

    /**
     * 删除指定条目列表的下载
     *
     * @return void
     */
    public void deleteCacheList(final ArrayList<OfflineCache> offlineCacheList) {
        L.v(TAG, "deleteCacheList", "start offlineCacheList.size="
                + offlineCacheList.size());
        new Thread(new Runnable() {

            @Override
            public void run() {

                DownloadManager.getInstance().deleteCache(offlineCacheList);

            }
        }).start();
    }

    /**
     * 合并进度
     *
     * @param offlineCacheFile 待合并缓存信息
     * @return void
     */
    public void mergeOfflineCache(OfflineCache offlineCacheFile) {
//        L.v(TAG,
//                "mergeOfflineCache",
//                "MSG_FILE_CACHE_LIST_SUCCESS name="
//                        + offlineCacheFile.getCacheName()
//                        + " alreadySize="
//                        + offlineCacheFile.getCacheAlreadySize()
//                        + " totalSize=" + offlineCacheFile.getCacheTotalSize());
        // 第二层(文件)
        Message fileListMessage = new Message();
        fileListMessage.what = StorageModule.MSG_FILE_CACHE_UPDATE_PROGRESS_SUCCESS;
        Bundle fileBundle = new Bundle();
        fileBundle.putParcelable(OfflineCache.OFFLINE_CACHE,
                offlineCacheFile);
        fileListMessage.setData(fileBundle);
        EventBus.getDefault().post(fileListMessage);

        // L.v(TAG,
        // "mergeOfflineCache",
        // "MSG_INTERNAL_FOLDER_CACHE_LIST_SUCCESS name="
        // + offlineCacheFile.getCacheName()
        // + offlineCacheFile.getCacheEpisodeNum());
    }

    /**
     * 添加任务成功
     *
     * @param offlineCacheFileList 成功任务的集合
     * @return void
     */
    public void addCacheSuccess(List<OfflineCache> offlineCacheFileList) {
        L.v(TAG, "addCacheSuccess", "start offlineCacheFileList.size="
                + offlineCacheFileList.size());
        // 建立第二级目录(新建文件)
        OfflineCacheDataBaseAdapter.getInstance().addOfflineCacheFileList(
                offlineCacheFileList);

        mIsAddSuccess = true;

        // 刷新文件列表(更多剧集界面添加)
        if (offlineCacheFileList != null && offlineCacheFileList.size() > 0) {

            OfflineCache offlineCacheFile = offlineCacheFileList.get(0);

            L.v(TAG, "addCacheSuccess",
                    "refresh offlineCacheFileList downloadType="
                            + offlineCacheFile.getCacheDownloadType());

//            StorageModule.getInstance().getCacheFileListByCCID(
//                    offlineCacheFile.getCacheCCID());

        } else {

            L.v(TAG, "addCacheSuccess", "refresh offlineCacheFileList=null");

        }
    }

    /**
     * 缓存进度处理方法
     *
     * @param msg 订阅消息
     * @return void
     */
    public void onEventBackgroundThread(Message msg) {
        Bundle bundle = null;
        switch (msg.what) {
            case OfflineCacheModule.MSG_INTERNAL_CACHE_SPACE_SIZE_PROGRESS:
                // L.v(TAG, "onEventBackgroundThread",
                // "MSG_INTERNAL_CACHE_SPACE_SIZE_PROGRESS");
                bundle = msg.getData();
                if (bundle == null) {
                    return;
                }
                OfflineCache offlineCacheFile = bundle
                        .getParcelable(OfflineCache.OFFLINE_CACHE);
                if (offlineCacheFile != null) {
                    long currentSize = offlineCacheFile.getCacheCurrentSize();
                    // 做存储检测,通知UI
                    DownloadManager.getInstance()
                            .checkManyStorageSpace(currentSize);
                } else {
                    L.w(TAG, "onEventBackgroundThread",
                            "MSG_INTERNAL_CACHE_SPACE_SIZE_PROGRESS offlineCacheFile=null");
                }

                break;
            case OfflineCacheModule.MSG_INTERNAL_FILE_CACHE_DELETE_SUCCESS: {
                L.v(TAG, "onEventBackgroundThread",
                        "MSG_INTERNAL_FILE_CACHE_DELETE_SUCCESS");
                mIsDeleteSuccess = true;

                EventBus.getDefault().removeStickyEvent(msg);

                break;
            }
            case StorageModule.MSG_CACHE_START_ALL_SUCCESS:
                L.v(TAG, "onEventBackgroundThread", "MSG_CACHE_START_ALL_SUCCESS");
                mIsShieldStartAllOperate = false;
                L.v(TAG, "onEventBackgroundThread", "end startCacheAll");
                // 成功一个,就执行下一个等待的操作
                operateCacheQueue();
                break;
            case StorageModule.MSG_CACHE_PAUSE_ALL_SUCCESS:
                L.v(TAG, "onEventBackgroundThread", "MSG_CACHE_PAUSE_ALL_SUCCESS");
                mIsShieldPauseAllOperate = false;
                L.v(TAG, "onEventBackgroundThread", "end pauseCacheAll");
                // 成功一个,就执行下一个等待的操作
                operateCacheQueue();
                break;
            case StorageModule.MSG_DOWNLOAD_CACHE_SUCCESS:
                // 刷新进度
                break;
//            case StorageModule.MSG_ADD_CACHE_SUCCESS: {
//                L.v(TAG, "onEventBackgroundThread", "MSG_ADD_CACHE_SUCCESS");
//                bundle = msg.getData();
//                if (bundle == null) {
//                    return;
//                }
//                List<OfflineCache> offlineCacheAddFileList = bundle
//                        .getParcelableArrayList(OfflineCache.OFFLINE_CACHE_LIST);
//                if (offlineCacheAddFileList != null
//                        && offlineCacheAddFileList.size() > 0) {
//
//                    L.v(TAG, "onEventBackgroundThread",
//                            "MSG_ADD_CACHE_SUCCESS offlineCacheAddFileList.size="
//                                    + offlineCacheAddFileList.size());
//
//                   // addCacheSuccess(offlineCacheAddFileList);
//
//                    //开始下载
//                    //startAuto();
//
//                } else {
//
//                    L.v(TAG, "onEventBackgroundThread",
//                            "MSG_ADD_CACHE_SUCCESS offlineCacheAddFileList=null");
//
//                }
//
//                break;
//            }
            case StorageModule.MSG_ADD_CACHE_FAIL:
                L.v(TAG, "onEventBackgroundThread", "MSG_ADD_CACHE_FAIL timeout");
                bundle = msg.getData();
                if (bundle == null) {
                    return;
                }
                break;
            default:
                break;
        }

    }

    /**
     * 按顺序下载
     */
    private void startAuto() {
        switch (DownloadUtil.getNetType()) {
            case StorageModule.MSG_WIFI_NETWORK_TYPE: {

                L.v(TAG, "startAuto", "MSG_WIFI_NETWORK_TYPE");
                // 开始所有
                DownloadManager.getInstance().startAuto();

                break;
            }
            case StorageModule.MSG_NO_NETWORK_TYPE:
                // 无网
                break;
            case StorageModule.MSG_2G_NETWORK_TYPE:
            case StorageModule.MSG_3G_NETWORK_TYPE:
            case StorageModule.MSG_4G_NETWORK_TYPE:
                boolean isAutoDownload = SharedPreferenceModule.getInstance()
                        .getBoolean(FoneConstant.AUTO_DOWNLOAD_FLAG_SP);

                if (!isAutoDownload) {

                    L.v(TAG, "startAuto",
                            "MSG_UNWIFI_NETWORK_TYPE isAutoDownload="
                                    + isAutoDownload);

                    // 开始所有,仅wifi下载开关关闭(不进行网络类型过滤)
                    DownloadManager.getInstance().startAuto();

                } else {

                    L.w(TAG, "startAuto",
                            "MSG_UNWIFI_NETWORK_TYPE isAutoDownload="
                                    + isAutoDownload);
                }
                break;
            default:

                break;
        }
    }

    /**
     * 删除视频和配置文件
     *
     * @param offlineCache 视频信息
     * @return void
     */
    public synchronized void deleteOfflineCacheFile(OfflineCache offlineCache) {
        String key = OfflineCacheDownloadManager.getInstance().getKey(offlineCache);
        L.v(TAG,
                "deleteOfflineCacheFile",
                "start name=" + offlineCache.getCacheName()
                        + " key=" + key
                        + " filePath=" + offlineCache.getCacheStoragePath());

        // 非剧集
        String storagePath = offlineCache.getCacheStoragePath();
        if (storagePath != null && !storagePath.equals("")) {
            // 删除视频文件夹
            DownloadUtil
                    .deleteDirectory(storagePath);
            // 删除空文件夹
            DownloadUtil.deleteEmptyDirectory(1, storagePath);
        }

        // 通知UI,删除成功
        Message msg = new Message();
        Bundle bundle = new Bundle();
        msg.what = StorageModule.MSG_DELETE_CACHE_SUCCESS;
        bundle.putParcelable(OfflineCache.OFFLINE_CACHE,
                offlineCache);
        msg.setData(bundle);
        EventBus.getDefault().post(msg);
        // 通知内部组件,删除成功
        msg = new Message();
        msg.what = OfflineCacheModule.MSG_INTERNAL_FILE_CACHE_DELETE_SUCCESS;
        EventBus.getDefault().post(msg);
        File file = new File(storagePath);
        L.v(TAG,
                "deleteOfflineCacheFile",
                "start name=" + offlineCache.getCacheName()
                        + " key=" + key
                        + " filePath=" + offlineCache.getCacheStoragePath() + " exists=" + file.exists());
    }

    /**
     * 开启后台离线缓存服务
     *
     * @return void
     */

    public void startCacheService() {

        DownloadManager.getInstance().startCacheService();
    }

    /**
     * 停止后台离线缓存服务
     *
     * @return void
     */
    public void stopCacheService() {
        DownloadManager.getInstance().stopCacheService();
    }

    /**
     * 开始发送数据
     *
     * @return void
     */
    public void startSendOfflineCacheMessage() {
        v(TAG, "startSendOfflineCacheMessage", "start");
        // 文件和文件夹列表都发送
        // sendOfflineCacheList(OfflineCache.CACHE_HIERARCHY_ALL);
    }

    /**
     * 停止发送数据
     *
     * @return void
     */
    public void stopSendOfflineCacheMessage() {
        v(TAG, "stopSendOfflineCacheMessage", "start");
    }

    void v(String tag, String type, String msg) {
        if (StorageConfig.CACHE_MODULE_LOG) {
            L.v(tag, type, msg);
        }
    }

    void e(String tag, String type, String msg) {
        if (StorageConfig.CACHE_MODULE_LOG) {
            L.e(tag, type, msg);
        }
    }

    void writeOfflineCacheLog(String method, OfflineCache offlineCache) {
        L.v(TAG,
                method,
                " name=" + offlineCache.getCacheName()
                        + " errorCode="
                        + offlineCache.getCacheErrorCode() + " downloadState="
                        + offlineCache.getCacheDownloadState() + " cid="
                        + offlineCache.getCacheID() + " alreadySize="
                        + offlineCache.getCacheAlreadySize() + " totalSize="
                        + offlineCache.getCacheTotalSize());
    }

    void writeOfflineCacheListLog(String method,
                                  List<OfflineCache> offlineCacheList) {

        if (offlineCacheList == null) {
            return;
        }

        for (OfflineCache offlineCache : offlineCacheList) {
            L.v(TAG,
                    method,
                    " name=" + offlineCache.getCacheName()
                            + " errorCode=" + offlineCache.getCacheErrorCode()
                            + " downloadState="
                            + offlineCache.getCacheDownloadState() + " id="
                            + offlineCache.getCacheID()
                            + " alreadySize="
                            + offlineCache.getCacheAlreadySize()
                            + " totalSize=" + offlineCache.getCacheTotalSize());
        }
    }

    public static class CacheOperate {
        public int mOperateState;
        public int mCacheOperateType;
    }
}
