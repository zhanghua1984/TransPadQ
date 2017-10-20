package cn.transpad.transpadui.storage.download;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.storage.OfflineCacheDataBaseAdapter;
import cn.transpad.transpadui.storage.OfflineCacheModule;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import de.greenrobot.event.EventBus;

/**
 * 离线缓存下载管理
 *
 * @author wangyang
 * @since 2014年6月13日
 */
public class OfflineCacheDownloadManager {
    private static final String TAG = "OfflineCacheDownloadManager";
    /**
     * 最大下载数量
     */
    public static final int MAX_DOWNLOAD_NUM = 2;
    private OfflineCacheDownloadQueue<String, OfflineCacheDownloadRunnable> mDownloadRunnableActiveQueue = new OfflineCacheDownloadQueue<String, OfflineCacheDownloadRunnable>();
    private OfflineCacheDownloadQueue<String, OfflineCacheDownloadRunnable> mDownloadRunnableUnactiveUserQueue = new OfflineCacheDownloadQueue<String, OfflineCacheDownloadRunnable>();
    private OfflineCacheDownloadQueue<String, OfflineCacheDownloadRunnable> mDownloadRunnableUnactiveAutoQueue = new OfflineCacheDownloadQueue<String, OfflineCacheDownloadRunnable>();
    private OfflineCacheDownloadQueue<String, OfflineCacheDownloadRunnable> mDownloadRunnableUpgradeActiveQueue = new OfflineCacheDownloadQueue<String, OfflineCacheDownloadRunnable>();
    private static final OfflineCacheDownloadManager mInstance = new OfflineCacheDownloadManager();

    public static OfflineCacheDownloadManager getInstance() {
        return mInstance;
    }

    private OfflineCacheDownloadManager() {

    }

    public static void init() {
        L.v(TAG, "init", "start");
    }

    /**
     * 获取当前正在下载的集合
     *
     * @return ArrayList<DownloadRunnable> 当前正在下载的集合
     */
    public ArrayList<OfflineCacheDownloadRunnable> getDownloadRunnableList() {
        ArrayList<OfflineCacheDownloadRunnable> downloadRunnableList = new ArrayList<OfflineCacheDownloadRunnable>();
        downloadRunnableList
                .addAll(mDownloadRunnableUnactiveUserQueue.values());
        return downloadRunnableList;
    }

    /**
     * 初始化下载队列
     */
    public void initQueue() {
        L.v(TAG, "initQueue", "start");
        List<OfflineCache> offlineCacheNotFinishList = OfflineCacheDataBaseAdapter
                .getInstance().getOfflineCacheFileNotFinishList();
        if (offlineCacheNotFinishList != null
                && offlineCacheNotFinishList.size() > 0) {

            for (OfflineCache offlineCache : offlineCacheNotFinishList) {

                // 获取配置文件中的下载信息
                // key=cid+_+dfnt
                String key = getKey(offlineCache);

                switch (offlineCache.getCacheDownloadState()) {
                    case OfflineCache.CACHE_STATE_WAITING:
                    case OfflineCache.CACHE_STATE_DOWNLOADING:
                        // 加入队列
                        create(offlineCache);
                        break;
                    case OfflineCache.CACHE_STATE_PAUSE:
                    case OfflineCache.CACHE_STATE_ERROR:
                        // 创建下载任务,开始下载
                        OfflineCacheDownloadRunnable downloadRunnable = new OfflineCacheDownloadRunnable();
                        downloadRunnable.setOfflineCache(offlineCache);
                        downloadRunnable.setHandler(mHandler);
                        downloadRunnable.setOperateType(OfflineCache.OPERATE_STOP);
                        switch (offlineCache.getCacheDownloadType()) {
                            case OfflineCache.CACHE_FROM_PAGE_UPGRADE:

                                mDownloadRunnableUpgradeActiveQueue.put(key,
                                        downloadRunnable);
                                break;
                            default:

                                mDownloadRunnableUnactiveUserQueue.put(key,
                                        downloadRunnable);
                                break;
                        }
                        break;
                    default:
                        L.e(TAG,
                                "initQueue",
                                "downloadState="
                                        + offlineCache.getCacheDownloadState()
                                        + " illegal");
                        break;
                }
            }

            // 开始下载
            startAuto();

        } else {

            L.v(TAG, "initQueue", "offlineCacheNotFinishList=null");

        }

    }

    /**
     * 创建任务
     *
     * @param offlineCache 视频信息
     * @return void
     */
    public void create(OfflineCache offlineCache) {

        L.v(TAG, "create", "start name=" + offlineCache.getCacheName()
                + " downloadType="
                + offlineCache.getCacheDownloadType());

        // key=cid+_+dfnt
        String key = getKey(offlineCache);

        // 创建下载任务,开始下载
        OfflineCacheDownloadRunnable downloadRunnable = new OfflineCacheDownloadRunnable();
        downloadRunnable.setOfflineCache(offlineCache);
        downloadRunnable.setHandler(mHandler);


        // 判断缓存来自哪个界面
        switch (offlineCache.getCacheDownloadType()) {
            case OfflineCache.CACHE_FROM_PAGE_UPGRADE:
                // 升级
                downloadRunnable
                        .setOperateType(OfflineCache.OPERATE_RUNNING);
                ThreadPoolManager.getInstance().executeAppFileRunnable(
                        downloadRunnable);
                mDownloadRunnableUpgradeActiveQueue.put(key, downloadRunnable);
                break;
            default:
                // 其他界面,开始下载,变为下载中状态
                // 判断是否超过规定的下载数量
                if (mDownloadRunnableActiveQueue.size() < MAX_DOWNLOAD_NUM) {
                    // 应用推荐
                    downloadRunnable
                            .setOperateType(OfflineCache.OPERATE_RUNNING);
                    // 放到活跃队列里面
                    ThreadPoolManager.getInstance().executeOfflineCacheRunnable(
                            downloadRunnable);
                    mDownloadRunnableActiveQueue.put(key, downloadRunnable);
                } else {

                    L.v(TAG,
                            "create",
                            "add unActiveAutoQueue key=" + key + " name="
                                    + offlineCache.getCacheName()
                                    + " downloadState=CACHE_STATE_WAITING"
                                    + " downloadType="
                                    + offlineCache.getCacheDownloadType());

                    // 放到非活跃队列里面
                    mDownloadRunnableUnactiveAutoQueue.put(key, downloadRunnable);
                }
                break;
        }
        L.v(TAG,
                "create",
                "add activeQueue key=" + key + " name="
                        + offlineCache.getCacheName()
                        + " downloadState=CACHE_STATE_DOWNLOADING"
                        + " downloadType="
                        + offlineCache.getCacheDownloadType());

        // 请求真实文件
        File file = new File(
                offlineCache.getCacheStoragePath());

        if (!file.exists()) {

            L.v(TAG, "limitSpeedDownloadFragment", "create new file path="
                    + offlineCache.getCacheStoragePath());

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 下载任务
     *
     * @param offlineCacheList 下载对象
     * @return int 操作结果<br>
     * 1 添加成功<br>
     * -1 任务已存在 <br>
     * -2添加异常
     */
    public void addList(List<OfflineCache> offlineCacheList) {
        L.v(TAG, "addList", "start size=" + offlineCacheList.size());

        for (OfflineCache offlineCache : offlineCacheList) {
            add(offlineCache);
        }
    }

    /**
     * 添加单个视频
     *
     * @param offlineCache 视频信息
     * @return void
     */
    public void add(OfflineCache offlineCache) {

        L.v(TAG, "add", "start name=" + offlineCache.getCacheName()
        );

        // key=cid+_+dfnt
        String key = getKey(offlineCache);

        // 是否重复
        boolean isRepeat = mDownloadRunnableActiveQueue.containsKey(key)
                || mDownloadRunnableUnactiveUserQueue.containsKey(key)
                || mDownloadRunnableUnactiveAutoQueue.containsKey(key);

        // 判断任务是否重复
        if (isRepeat) {

            OfflineCacheDownloadRunnable downloadRunnable = mDownloadRunnableActiveQueue
                    .get(key);

            if (downloadRunnable == null) {

                downloadRunnable = mDownloadRunnableUnactiveUserQueue.get(key);

                if (downloadRunnable == null) {

                    downloadRunnable = mDownloadRunnableUnactiveAutoQueue
                            .get(key);

                }
            }

            if (downloadRunnable != null) {

                switch (downloadRunnable.getRunnableState()) {
                    case OfflineCacheDownloadRunnable.STATE_RUNNING:
                        // 重复
                        L.w(TAG, "add", "name=" + offlineCache.getCacheName()
                                + " already repeat");
                        break;
                    case OfflineCacheDownloadRunnable.STATE_STOP_SEND:
                    case OfflineCacheDownloadRunnable.STATE_STOP_UNSEND:
                    case OfflineCacheDownloadRunnable.STATE_FINISH:
                        L.w(TAG, "add", "name=" + offlineCache.getCacheName()
                                + " delete already stop task");
                        // 删除已停止的任务
                        mDownloadRunnableActiveQueue.remove(key);
                        mDownloadRunnableUnactiveUserQueue.remove(key);
                        mDownloadRunnableUnactiveAutoQueue.remove(key);

                        // 创建任务
                        create(offlineCache);

                        break;
                    default:
                        break;
                }

            }

        } else {

            // 创建任务
            create(offlineCache);

        }

    }

    /**
     * 自动开始任务
     *
     * @return void
     */
    public void startAuto() {

        L.v(TAG, "startAuto", "start");

        // 判断活跃队列是否超过规定的下载数量(在规定数量范围内下载)
        while (mDownloadRunnableActiveQueue.size() < MAX_DOWNLOAD_NUM) {

            L.v(TAG, "startAuto",
                    "remove start mDownloadRunnableUnactiveAutoQueue.size="
                            + mDownloadRunnableUnactiveAutoQueue.size());
            // 根据键获取非活跃自动下载队列的任务
            OfflineCacheDownloadRunnable downloadRunnableUnactiveAutoFirst = mDownloadRunnableUnactiveAutoQueue
                    .removeFirst();

            L.v(TAG, "startAuto",
                    "remove end mDownloadRunnableUnactiveAutoQueue.size="
                            + mDownloadRunnableUnactiveAutoQueue.size());

            // 非空判断
            if (downloadRunnableUnactiveAutoFirst != null) {

                OfflineCache offlineCache = downloadRunnableUnactiveAutoFirst
                        .getOfflineCache();
                switch (offlineCache.getCacheDownloadState()) {
                    case OfflineCache.CACHE_STATE_NOT_DOWNLOAD:
                    case OfflineCache.CACHE_STATE_DOWNLOADING:
                    case OfflineCache.CACHE_STATE_WAITING:

                        L.v(TAG, "startAuto",
                                "auto start name=" + offlineCache.getCacheName()
                        );

                        downloadRunnableUnactiveAutoFirst
                                .setOperateType(OfflineCache.OPERATE_RUNNING);

                        // 放到活跃队列里面
                        ThreadPoolManager.getInstance().executeOfflineCacheRunnable(
                                downloadRunnableUnactiveAutoFirst);
                        // key=cid+_+dfnt
                        String key = getKey(offlineCache);

                        L.v(TAG,
                                "startAuto",
                                "add activeQueue key=" + key + " name="
                                        + offlineCache.getCacheName()
                                        + " downloadState="
                                        + offlineCache.getCacheDownloadState());

                        mDownloadRunnableActiveQueue.put(key,
                                downloadRunnableUnactiveAutoFirst);
                        break;
                    default:
                        L.v(TAG,
                                "startAuto",
                                "jump auto start name="
                                        + offlineCache.getCacheName()
                        );
                        break;
                }

            } else {

                // 任务不存在或已经下载完,此处忽略
                L.v(TAG, "startAuto", "downloadRunnableUnactiveAutoFirst=null");
                break;
            }

        }
    }

    /**
     * 开始任务
     *
     * @param offlineCache 下载对象
     * @return void
     */
    public void start(OfflineCache offlineCache) {

        L.v(TAG, "start", "start name=" + offlineCache.getCacheName()
                + " downloadState="
                + offlineCache.getCacheDownloadState() + " percentNum="
                + offlineCache.getCachePercentNumString());

        // key=cid+_+dfnt
        String key = getKey(offlineCache);

        OfflineCacheDownloadRunnable downloadRunnableUnactiveFirst = null;

        switch (offlineCache.getCacheDownloadState()) {
            case OfflineCache.CACHE_STATE_PAUSE:
            case OfflineCache.CACHE_STATE_PAUSE_USER:
            case OfflineCache.CACHE_STATE_ERROR:
                L.v(TAG, "start",
                        "remove start mDownloadRunnableUnactiveUserQueue.size="
                                + mDownloadRunnableUnactiveUserQueue.size());

                // 根据键获取非活跃的任务
                downloadRunnableUnactiveFirst = mDownloadRunnableUnactiveUserQueue
                        .remove(key);

                L.v(TAG, "start",
                        "remove end mDownloadRunnableUnactiveUserQueue.size="
                                + mDownloadRunnableUnactiveUserQueue.size());

                if (downloadRunnableUnactiveFirst == null) {

                    // 下载出错,手动点击重试
                    L.w(TAG,
                            "start",
                            "key="
                                    + key
                                    + " downloadRunnableUnactiveUserFirst=null downloadState="
                                    + offlineCache.getCacheDownloadState()
                                    + " userContent="
                                    + mDownloadRunnableUnactiveUserQueue.toString());

                }
                break;
            case OfflineCache.CACHE_STATE_WAITING:
                L.v(TAG, "start",
                        "remove start mDownloadRunnableUnactiveAutoQueue.size="
                                + mDownloadRunnableUnactiveAutoQueue.size());

                // 根据键获取活跃的任务
                downloadRunnableUnactiveFirst = mDownloadRunnableUnactiveAutoQueue
                        .remove(key);

                L.v(TAG, "start",
                        "remove end mDownloadRunnableUnactiveAutoQueue.size="
                                + mDownloadRunnableUnactiveAutoQueue.size());

                if (downloadRunnableUnactiveFirst == null) {
                    // 下载出错,手动点击重试
                    L.w(TAG,
                            "start",
                            "key="
                                    + key
                                    + " downloadRunnableUnactiveAutoFirst=null  downloadState="
                                    + offlineCache.getCacheDownloadState()
                                    + " autoContent="
                                    + mDownloadRunnableUnactiveAutoQueue.toString());

                }
                break;
        }

        // 非空判断
        if (downloadRunnableUnactiveFirst != null) {

            // 判断活跃队列是否超过规定的下载数量
            if (mDownloadRunnableActiveQueue.size() < MAX_DOWNLOAD_NUM) {

                L.v(TAG, "start", "size<MAX_DOWNLOAD_NUM");

                downloadRunnableUnactiveFirst
                        .setOperateType(OfflineCache.OPERATE_RUNNING);

                // 放到活跃队列里面
                ThreadPoolManager.getInstance().executeOfflineCacheRunnable(
                        downloadRunnableUnactiveFirst);

                mDownloadRunnableActiveQueue.put(key,
                        downloadRunnableUnactiveFirst);

                L.v(TAG,
                        "start",
                        "add activeQueue key=" + key + " name="
                                + offlineCache.getCacheName()
                );
            } else {

                L.v(TAG, "start", "size>=MAX_DOWNLOAD_NUM");

                // 判断活跃队列是否有任务
                if (mDownloadRunnableActiveQueue.size() > 0) {

                    // 标记开始
                    downloadRunnableUnactiveFirst
                            .setOperateType(OfflineCache.OPERATE_STOP_BATCH_WAIT_STATE);

                    // 将非活跃任务添加到活跃队列的末尾
                    mDownloadRunnableUnactiveAutoQueue.put(key,
                            downloadRunnableUnactiveFirst);

                    L.v(TAG, "start",
                            "add activeQueue key=" + key + " name="
                                    + offlineCache.getCacheName()
                    );
                }
            }

        } else {

            // 任务不存在,重新创建任务
            add(offlineCache);
        }

    }

    /**
     * 开始所有任务
     */
    public void startAll() {
        L.v(TAG, "startAll", "start mDownloadRunnableActiveQueue.size="
                + mDownloadRunnableActiveQueue.size());
        // 把活跃队列里面的所有任务都开始下载
        Iterator<Entry<String, OfflineCacheDownloadRunnable>> activeQueueIterator = mDownloadRunnableActiveQueue
                .entrySet().iterator();
        while (activeQueueIterator.hasNext()) {
            Entry<String, OfflineCacheDownloadRunnable> entry = (Entry<String, OfflineCacheDownloadRunnable>) activeQueueIterator
                    .next();
            OfflineCacheDownloadRunnable downloadOfflineCacheActiveRunnable = entry
                    .getValue();
            switch (downloadOfflineCacheActiveRunnable.getRunnableState()) {
                case OfflineCacheDownloadRunnable.STATE_RUNNING:
                    // 已经启动的任务做忽略处理
                    L.w(TAG, "startAll",
                            "downloadOfflineCacheActiveRunnable already STATE_RUNNING");
                    break;
                default:
                    downloadOfflineCacheActiveRunnable
                            .setOperateType(OfflineCache.OPERATE_RUNNING);

                    ThreadPoolManager.getInstance().executeOfflineCacheRunnable(
                            downloadOfflineCacheActiveRunnable);
                    L.v(TAG, "startAll", "mDownloadRunnableActiveQueue.size="
                            + mDownloadRunnableActiveQueue.size());
                    break;
            }
        }

        while (true) {
            // 判断活跃队列是否达到上限
            if (mDownloadRunnableActiveQueue.size() < MAX_DOWNLOAD_NUM) {

                L.v(TAG, "startAll", "start fill mDownloadRunnableActiveQueue");

                // 把非活跃队列里面的所有任务都开始下载
                OfflineCacheDownloadRunnable downloadOfflineCacheUnactiveAutoRunnable = mDownloadRunnableUnactiveAutoQueue
                        .removeFirst();

                if (downloadOfflineCacheUnactiveAutoRunnable != null) {

                    switch (downloadOfflineCacheUnactiveAutoRunnable
                            .getRunnableState()) {
                        case OfflineCacheDownloadRunnable.STATE_RUNNING:
                            // 已经启动的任务做忽略处理
                            L.w(TAG, "startAll",
                                    "downloadOfflineCacheUnactiveAutoRunnable already STATE_RUNNING");
                            break;
                        default:
                            downloadOfflineCacheUnactiveAutoRunnable
                                    .setOperateType(OfflineCache.OPERATE_RUNNING);

                            ThreadPoolManager.getInstance().executeOfflineCacheRunnable(
                                    downloadOfflineCacheUnactiveAutoRunnable);
                            L.v(TAG,
                                    "startAll",
                                    "operateType=OPERATE_RUNNING mDownloadRunnableUnactiveAutoQueue.size="
                                            + mDownloadRunnableUnactiveAutoQueue
                                            .size());

                            break;
                    }

                    OfflineCache offlineCache = downloadOfflineCacheUnactiveAutoRunnable.getOfflineCache();
                    String key = getKey(offlineCache);
                    mDownloadRunnableActiveQueue.put(key, downloadOfflineCacheUnactiveAutoRunnable);

                } else {
                    L.v(TAG, "startAll",
                            "downloadOfflineCacheUnactiveAutoRunnable=null");
                    break;

                }

            } else {
                L.v(TAG, "startAll", "already MAX_DOWNLOAD_NUM");
                break;
            }

        }

        while (true) {
            OfflineCacheDownloadRunnable downloadOfflineCacheUpgradeRunnable = mDownloadRunnableUpgradeActiveQueue
                    .removeFirst();

            if (downloadOfflineCacheUpgradeRunnable != null) {
                switch (downloadOfflineCacheUpgradeRunnable
                        .getRunnableState()) {
                    case OfflineCacheDownloadRunnable.STATE_RUNNING:
                        // 已经启动的任务做忽略处理
                        L.w(TAG, "startAll",
                                "downloadOfflineCacheUpgradeRunnable already STATE_RUNNING");
                        break;
                    default:
                        downloadOfflineCacheUpgradeRunnable
                                .setOperateType(OfflineCache.OPERATE_RUNNING);

                        ThreadPoolManager.getInstance().executeAppFileRunnable(
                                downloadOfflineCacheUpgradeRunnable);
                        L.v(TAG,
                                "startAll",
                                "operateType=OPERATE_RUNNING mDownloadRunnableUpgradeActiveQueue.size="
                                        + mDownloadRunnableUpgradeActiveQueue
                                        .size());

                        break;
                }
            } else {
                break;
            }
        }

        L.v(TAG, "startAll", "end");
    }

    /**
     * 暂停任务
     *
     * @param offlineCache 下载对象
     * @return void
     */
    public void pause(OfflineCache offlineCache) {

        L.v(TAG, "pause", "start name=" + offlineCache.getCacheName()
        );

        L.v(TAG, "pause", "remove start mDownloadRunnableActiveQueue.size="
                + mDownloadRunnableActiveQueue.size());

        // key=cid+_+dfnt
        String key = getKey(offlineCache);
        switch (offlineCache.getCacheDownloadState()) {
            case OfflineCache.CACHE_STATE_DOWNLOADING:

                // 根据键获取活跃的任务
                OfflineCacheDownloadRunnable downloadRunnableActiveFirst = mDownloadRunnableActiveQueue
                        .remove(key);

                L.v(TAG, "pause", "remove end mDownloadRunnableActiveQueue.size="
                        + mDownloadRunnableActiveQueue.size());

                // 非空判断
                if (downloadRunnableActiveFirst != null) {

                    L.v(TAG, "pause", "set operateType=OPERATE_STOP");

                    // 停止当前活跃任务
                    downloadRunnableActiveFirst
                            .setOperateType(OfflineCache.OPERATE_STOP);

                    L.v(TAG,
                            "pause",
                            "add unactiveUserQueue key=" + key + " name="
                                    + offlineCache.getCacheName()
                    );
                    // 把活跃任务添加到非活跃队列队尾
                    mDownloadRunnableUnactiveUserQueue.put(key,
                            downloadRunnableActiveFirst);

                    // 自动开始下一个任务
                    startAuto();

                } else {

                    // 任务不存在或已经下载完或已经全部暂停
                    L.e(TAG, "pause", "key=" + key
                            + " downloadRunnableActiveFirst=null activeContent="
                            + mDownloadRunnableActiveQueue.toString());
                }
                break;
            case OfflineCache.CACHE_STATE_WAITING:
                L.v(TAG, "pause",
                        "remove start mDownloadRunnableUnactiveAutoQueue.size="
                                + mDownloadRunnableUnactiveAutoQueue.size());
                // 根据键获取活跃的任务
                OfflineCacheDownloadRunnable downloadRunnableUnactiveFirst = mDownloadRunnableUnactiveAutoQueue
                        .remove(key);

                if (downloadRunnableUnactiveFirst != null) {
                    downloadRunnableUnactiveFirst
                            .setOperateType(OfflineCache.OPERATE_STOP);

                    mDownloadRunnableUnactiveUserQueue.put(key, downloadRunnableUnactiveFirst);

                } else {
                    // 下载出错,手动点击重试
                    L.w(TAG,
                            "pause",
                            "key="
                                    + key
                                    + " downloadRunnableUnactiveAutoFirst=null  downloadState="
                                    + offlineCache.getCacheDownloadState()
                                    + " autoContent="
                                    + mDownloadRunnableUnactiveAutoQueue.toString());
                }
                L.v(TAG, "pause",
                        "remove end mDownloadRunnableUnactiveAutoQueue.size="
                                + mDownloadRunnableUnactiveAutoQueue.size());
                break;
        }
    }

    /**
     * 暂停所有任务
     *
     * @param operateState 操作状态
     * @return void
     */
    public synchronized void pauseAll(int operateState) {
        L.v(TAG, "pauseAll", "start operateState=" + operateState
                + " mDownloadRunnableActiveQueue.size="
                + mDownloadRunnableActiveQueue.size());

        switch (operateState) {
            case OfflineCache.OPERATE_STOP_BATCH_PAUSE_STATE: {

                L.v(TAG,
                        "pauseAll",
                        "operateState=OPERATE_STOP_BATCH_PAUSE_STATE activeIterator unactiveAutoIterator");

                // 把活跃队列里面的所有任务都停止下载,不删除
                Iterator<Entry<String, OfflineCacheDownloadRunnable>> activeIterator = mDownloadRunnableActiveQueue
                        .entrySet().iterator();

                pauseList(operateState, activeIterator);

                // 把自动队列里面的所有任务都停止下载,不删除
                Iterator<Entry<String, OfflineCacheDownloadRunnable>> unactiveAutoIterator = mDownloadRunnableUnactiveAutoQueue
                        .entrySet().iterator();

                pauseList(operateState, unactiveAutoIterator);
                break;
            }
            default: {

                L.v(TAG, "pauseAll", "operateState=" + operateState
                        + " activeIterator");

                // 把活跃队列里面的所有任务都停止下载,不删除
                Iterator<Entry<String, OfflineCacheDownloadRunnable>> activeIterator = mDownloadRunnableActiveQueue
                        .entrySet().iterator();

                pauseList(operateState, activeIterator);

                break;
            }
        }

    }

    /**
     * 暂停多个任务
     *
     * @param operateState 操作状态
     * @param iterator     任务列表
     * @return void
     */
    public void pauseList(int operateState,
                          Iterator<Entry<String, OfflineCacheDownloadRunnable>> iterator) {

        List<OfflineCache> offlineCacheList = new ArrayList<OfflineCache>();
        while (iterator.hasNext()) {
            Entry<String, OfflineCacheDownloadRunnable> entry = (Entry<String, OfflineCacheDownloadRunnable>) iterator
                    .next();
            OfflineCacheDownloadRunnable downloadOfflineCacheActiveRunnable = entry
                    .getValue();
            switch (downloadOfflineCacheActiveRunnable.getRunnableState()) {
                case OfflineCacheDownloadRunnable.STATE_RUNNING:
                case OfflineCacheDownloadRunnable.STATE_STOP_SEND:
                case OfflineCacheDownloadRunnable.STATE_STOP_UNSEND: {

                    downloadOfflineCacheActiveRunnable.setOperateType(operateState);

                    OfflineCache offlineCache = downloadOfflineCacheActiveRunnable
                            .getOfflineCache();

                    offlineCacheList.add(offlineCache);

                    L.v(TAG,
                            "pauseList",
                            "mRunnableState="
                                    + downloadOfflineCacheActiveRunnable
                                    .getRunnableState() + " name="
                                    + offlineCache.getCacheName()
                                    + " mDownloadRunnableActiveQueue.size="
                                    + mDownloadRunnableActiveQueue.size());

                    // 更新数据库
                    OfflineCacheDataBaseAdapter.getInstance()
                            .updateOfflineCacheFileList(offlineCacheList);

                    break;
                }
                case OfflineCacheDownloadRunnable.STATE_FINISH:

                    // 已经完成的任务,移除活跃队列
                    OfflineCache offlineCache = downloadOfflineCacheActiveRunnable
                            .getOfflineCache();

                    // key=cid+_+dfnt
                    String key = getKey(offlineCache);

                    OfflineCacheDownloadRunnable offlineCacheActiveRunnable = mDownloadRunnableActiveQueue
                            .remove(key);

                    if (offlineCacheActiveRunnable == null) {
                        L.v(TAG, "pauseList",
                                "mRunnableState=STATE_FINISH remove key=" + key
                                        + " offlineCacheActiveRunnable=null name="
                                        + offlineCache.getCacheName()
                        );
                    } else {

                        L.v(TAG, "pauseList",
                                "mRunnableState=STATE_FINISH remove key=" + key
                                        + "name=" + offlineCache.getCacheName()
                        );
                    }
                    break;
                default:
                    L.e(TAG, "pauseList", "offlineCacheActiveRunnable default case");
                    break;
            }

        }
    }

    /**
     * 处理下载成功的任务
     *
     * @param offlineCache 视频信息
     * @return void
     */
    public void handleFinish(OfflineCache offlineCache) {

        //判断是否安装应用
        if (offlineCache.getCacheIsInstall()) {

            StorageModule.getInstance().installApp(offlineCache.getCacheStoragePath());

        }

        // key=cid+_+dfnt
        String key = getKey(offlineCache);

        OfflineCacheDownloadRunnable offlineCacheActiveRunnable = mDownloadRunnableActiveQueue
                .remove(key);

        if (offlineCacheActiveRunnable == null) {
            L.v(TAG,
                    "handleFinish",
                    "key=" + key + " offlineCacheActiveRunnable=null name="
                            + offlineCache.getCacheName()
            );
        } else {

            L.v(TAG, "handleFinish", "name=" + offlineCache.getCacheName()
            );
        }

        // 自动下载下一个任务
        startAuto();
    }

    /**
     * 处理下载错误任务
     *
     * @param offlineCache 视频信息
     * @return void
     */
    public void handleError(OfflineCache offlineCache) {

        // key=cid+_+dfnt
        String key = getKey(offlineCache);

        OfflineCacheDownloadRunnable offlineCacheActiveRunnable = mDownloadRunnableActiveQueue
                .remove(key);

        if (offlineCacheActiveRunnable != null) {

            // 把出错的添加到非活跃队列
            mDownloadRunnableUnactiveUserQueue.put(key,
                    offlineCacheActiveRunnable);
        } else {
            L.v(TAG,
                    "handleError",
                    "key=" + key + " offlineCacheActiveRunnable=null name="
                            + offlineCache.getCacheName()
            );
        }

        // 自动下载下一个任务
        startAuto();
    }

    /**
     * 删除任务
     *
     * @param offlineCacheList 下载对象
     * @return void
     */
    public void delete(List<OfflineCache> offlineCacheList) {
        L.v(TAG, "delete",
                "start offlineCacheList.size=" + offlineCacheList.size());
        for (OfflineCache offlineCache : offlineCacheList) {

            String key = getKey(offlineCache);
            // 停止下载,把任务从活跃队列和非活跃队列中删除
            OfflineCacheDownloadRunnable downloadOfflineCacheActiveRunnable;

            switch (offlineCache.getCacheDownloadType()) {
                case OfflineCache.CACHE_FROM_PAGE_UPGRADE:
                    downloadOfflineCacheActiveRunnable = mDownloadRunnableUpgradeActiveQueue.remove(key);
                    break;
                default:
                    downloadOfflineCacheActiveRunnable = mDownloadRunnableActiveQueue
                            .remove(key);
                    break;
            }

            if (downloadOfflineCacheActiveRunnable != null) {
                L.v(TAG, "delete",
                        "activeQueue name=" + offlineCache.getCacheName()
                );
                switch (downloadOfflineCacheActiveRunnable.getRunnableState()) {
                    case OfflineCacheDownloadRunnable.STATE_RUNNING:
                        // 运行中删除
                        downloadOfflineCacheActiveRunnable
                                .setOperateType(OfflineCache.OPERATE_DELETE);
                        break;
                    case OfflineCacheDownloadRunnable.STATE_STOP_SEND:
                    case OfflineCacheDownloadRunnable.STATE_STOP_UNSEND:
                    case OfflineCacheDownloadRunnable.STATE_FINISH:
                        // 删除已停止的任务
                        OfflineCacheModule.getInstance().deleteOfflineCacheFile(
                                offlineCache);
                        break;
                    default:
                        break;
                }

            } else {
                OfflineCacheDownloadRunnable downloadOfflineCacheUnactiveUserRunnable = mDownloadRunnableUnactiveUserQueue
                        .remove(key);
                if (downloadOfflineCacheUnactiveUserRunnable != null) {
                    L.v(TAG,
                            "delete",
                            "unActiveUserQueue name="
                                    + offlineCache.getCacheName()
                    );
                    switch (downloadOfflineCacheUnactiveUserRunnable
                            .getRunnableState()) {
                        case OfflineCacheDownloadRunnable.STATE_RUNNING:
                            // 运行中删除
                            downloadOfflineCacheUnactiveUserRunnable
                                    .setOperateType(OfflineCache.OPERATE_DELETE);
                            break;
                        case OfflineCacheDownloadRunnable.STATE_STOP_SEND:
                        case OfflineCacheDownloadRunnable.STATE_STOP_UNSEND:
                        case OfflineCacheDownloadRunnable.STATE_FINISH:
                            // 删除已停止的任务
                            OfflineCacheModule.getInstance()
                                    .deleteOfflineCacheFile(offlineCache);
                            break;
                        default:
                            break;
                    }
                } else {
                    OfflineCacheDownloadRunnable downloadOfflineCacheUnactiveAutoRunnable = mDownloadRunnableUnactiveAutoQueue
                            .remove(key);
                    if (downloadOfflineCacheUnactiveAutoRunnable != null) {

                        L.v(TAG,
                                "delete",
                                "unActiveAutoQueue name="
                                        + offlineCache.getCacheName()
                        );

                        switch (downloadOfflineCacheUnactiveAutoRunnable
                                .getRunnableState()) {
                            case OfflineCacheDownloadRunnable.STATE_RUNNING:
                                // 运行中删除
                                downloadOfflineCacheUnactiveAutoRunnable
                                        .setOperateType(OfflineCache.OPERATE_DELETE);
                                break;
                            case OfflineCacheDownloadRunnable.STATE_STOP_SEND:
                            case OfflineCacheDownloadRunnable.STATE_STOP_UNSEND:
                            case OfflineCacheDownloadRunnable.STATE_FINISH:
                                // 删除已停止的任务
                                OfflineCacheModule.getInstance()
                                        .deleteOfflineCacheFile(offlineCache);
                                break;
                            default:
                                break;
                        }

                    } else {

                        L.v(TAG, "delete",
                                "finish name=" + offlineCache.getCacheName()
                        );

                        OfflineCacheModule.getInstance()
                                .deleteOfflineCacheFile(offlineCache);
                    }
                }
            }

        }

        // 删除成功后,尝试开启等待中的任务开始下载
        startAuto();

        // 删除相关文件和数据
        deleteData(offlineCacheList);
    }

    /**
     * 根据缓存对象删除缓存文件(此方法内部使用,外部不需要调用)
     *
     * @return void
     */
    private void deleteData(List<OfflineCache> offlineCacheList) {
        L.v(TAG, "deleteData", "start");
        long startTime = System.currentTimeMillis();
        L.v(TAG, "deleteData", "delete database offlineCacheFile");
        // 删除文件层级信息
        OfflineCacheDataBaseAdapter.getInstance()
                .deleteOfflineCacheList(offlineCacheList);

        L.v(TAG, "deleteData", "delete database offlineCacheFolder");
        // 删除文件夹层级数据
//        OfflineCacheFolderDataBaseAdapter.getInstance()
//                .deleteOfflineCacheFolder();

        long endTime = System.currentTimeMillis();
        L.v(TAG, "deleteData", "end intervalTime=" + (endTime - startTime));
    }

    /**
     * 根据视频信息创建任务key
     *
     * @param offlineCache 视频信息
     * @return String 任务key
     */
    public String getKey(OfflineCache offlineCache) {
        // key=md5(url)
        return offlineCache.getCacheID() + "";
    }

    private static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            if (bundle != null) {
                OfflineCache offlineCache = bundle
                        .getParcelable(OfflineCache.OFFLINE_CACHE);
                if (offlineCache != null) {

                    switch (offlineCache.getCacheDownloadState()) {
                        case OfflineCache.CACHE_STATE_ERROR:
                            L.v(TAG, "handleMessage",
                                    "name=" + offlineCache.getCacheName()
                                            + " downloadState=CACHE_STATE_ERROR");
                            // 处理错误
                            OfflineCacheDownloadManager.getInstance().handleError(
                                    offlineCache);

                            break;
                        case OfflineCache.CACHE_STATE_FINISH:
                            L.v(TAG, "handleMessage",
                                    "name=" + offlineCache.getCacheName()
                                            + " downloadState=CACHE_STATE_FINISH");

                            Message finishMsg = new Message();
                            finishMsg.what = StorageModule.MSG_DOWNLOAD_CACHE_SUCCESS;
                            Bundle finishBundle = new Bundle();
                            finishBundle.putParcelable(OfflineCache.OFFLINE_CACHE,
                                    offlineCache);
                            finishMsg.setData(finishBundle);
                            EventBus.getDefault().post(finishMsg);
                            // 完成一个任务,然后自动开启一个任务
                            OfflineCacheDownloadManager.getInstance().handleFinish(
                                    offlineCache);

                            break;
                        default:
                            break;
                    }
//                    L.v(TAG, "handleMessage",
//                            "name=" + offlineCache.getCacheName()
//                                    + " downloadState=" + offlineCache.getCacheDownloadState());
                    OfflineCacheModule.getInstance().mergeOfflineCache(offlineCache);

                }

            }

        }
    };

}
