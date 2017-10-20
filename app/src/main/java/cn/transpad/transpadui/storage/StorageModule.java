package cn.transpad.transpadui.storage;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import ca.laplanete.mobile.pageddragdropgrid.Item;
import cn.transpad.transpadui.constant.FoneConstant;
import cn.transpad.transpadui.entity.App;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.entity.Page;
import cn.transpad.transpadui.entity.PlayRecord;
import cn.transpad.transpadui.storage.download.DownloadManager;
import cn.transpad.transpadui.storage.download.DownloadUtil;
import cn.transpad.transpadui.storage.download.NotificationHandle;
import cn.transpad.transpadui.util.ExtensionUtil;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.SystemUtil;

/**
 * 文件模块.<br>
 * 与文件相关操作都在此模块.<br>
 * 通过StorageModule.getInstance()得到该类实例,然后操作该类方法.<br>
 * Example:<br>
 * <p/>
 * <pre>
 * StorageModule.getInstance().getVideoList(StorageModule.LIST_TYPE_NORMAL_VIDEO);
 * </pre>
 *
 * @author wangyang
 * @since 2014年4月29日
 */
public class StorageModule {
    private static final String TAG = "StorageModule";
    /**
     * 扫描全盘视频文件开始
     */
    public static final int MSG_ACTION_SCANNER_VIDEO_STARTED = 1;
    /**
     * 扫描全盘视频文件进行中
     */
    public static final int MSG_ACTION_SCANNER_VIDEO_PROCESSING = 2;
    /**
     * 扫描全盘视频文件结束
     */
    public static final int MSG_ACTION_SCANNER_VIDEO_FINISHED = 3;
    /**
     * 扫描全盘视频文件停止
     */
    public static final int MSG_ACTION_SCANNER_VIDEO_STOPPED = 4;
    /**
     * 扫描全盘音频文件开始
     */
    public static final int MSG_ACTION_SCANNER_AUDIO_STARTED = 1;
    /**
     * 扫描全盘音频文件进行中
     */
    public static final int MSG_ACTION_SCANNER_AUDIO_PROCESSING = 2;
    /**
     * 扫描全盘音频文件结束
     */
    public static final int MSG_ACTION_SCANNER_AUDIO_FINISHED = 3;
    /**
     * 扫描全盘音频文件停止
     */
    public static final int MSG_ACTION_SCANNER_AUDIO_STOPPED = 4;
    /**
     * 扫描全盘图片文件开始
     */
    public static final int MSG_ACTION_SCANNER_IMAGE_STARTED = 1;
    /**
     * 扫描全盘图片文件进行中
     */
    public static final int MSG_ACTION_SCANNER_IMAGE_PROCESSING = 2;
    /**
     * 扫描全盘图片文件结束
     */
    public static final int MSG_ACTION_SCANNER_IMAGE_FINISHED = 3;
    /**
     * 扫描全盘图片文件停止
     */
    public static final int MSG_ACTION_SCANNER_IMAGE_STOPPED = 4;
    /**
     * 加密文件全部列表返回成功
     */
    public static final int MSG_ENCRYPT_FILE_LIST_SUCCESS = 102;
    /**
     * 普通视频文件全部列表返回成功
     */
    public static final int MSG_VIDEO_FILE_LIST_SUCCESS = 103;
    /**
     * 普通音频文件全部列表返回成功
     */
    public static final int MSG_AUDIO_FILE_LIST_SUCCESS = 104;
    /**
     * 普通图片文件全部列表返回成功
     */
    public static final int MSG_IMAGE_FILE_LIST_SUCCESS = 105;
    /**
     * 扫描媒体库文件开始
     */
    public static final int MSG_ACTION_MEDIA_SCANNER_STARTED = 1;
    /**
     * 扫描媒体库文件结束
     */
    public static final int MSG_ACTION_MEDIA_SCANNER_FINISHED = 2;
    /**
     * 扫描全盘文件开始
     */
    public static final int MSG_ACTION_SCANNER_STARTED = 3;
    /**
     * 扫描全盘文件进行中
     */
    public static final int MSG_ACTION_SCANNER_PROCESSING = 4;
    /**
     * 扫描全盘文件结束
     */
    public static final int MSG_ACTION_SCANNER_FINISHED = 5;
    /**
     * 扫描全盘文件停止
     */
    public static final int MSG_ACTION_SCANNER_STOPPED = 6;
    /**
     * 普通文件夹全部列表返回成功
     */
    public static final int MSG_NORMAL_FOLDER_LIST_SUCCESS = 101;
    /**
     * 普通文件全部列表返回成功
     */
    public static final int MSG_NORMAL_FILE_LIST_SUCCESS = 103;
    /**
     * 文件夹中文件全部列表返回成功
     */
    public static final int MSG_FOLDER_FILE_LIST_SUCCESS = 104;
    /**
     * 已扫描到的文件列表
     */
    public static final int MSG_SCANNER_FILE_LIST_SUCCESS = 105;
    /**
     * 普通文件全部列表
     */
    public static final int LIST_TYPE_FILE_NORMAL_VIDEO = 201;
    /**
     * 文件夹全部列表
     */
    public static final int LIST_TYPE_FOLDER_NORMAL_VIDEO = 203;
    /**
     * 缓存界面中文件夹列表返回成功
     */
    public static final int MSG_FOLDER_CACHE_LIST_SUCCESS = 301;
    /**
     * 缓存界面中文件列表返回成功
     */
    public static final int MSG_FILE_CACHE_LIST_SUCCESS = 302;
    /**
     * 缓存界面中文件夹列表返回成功
     */
    public static final int MSG_FOLDER_CACHE_UPDATE_PROGRESS_SUCCESS = 305;
    /**
     * 添加文件列表成功
     */
    public static final int MSG_ADD_FILE_LIST_SUCCESS = 106;
    /**
     * 缓存界面中文件列表返回成功
     */
    public static final int MSG_FILE_CACHE_UPDATE_PROGRESS_SUCCESS = 306;
    /**
     * 无网络
     */
    public static final int MSG_NO_NETWORK_TYPE = 401;
    /**
     * wifi网络
     */
    public static final int MSG_WIFI_NETWORK_TYPE = 402;
    /**
     * 非wifi网络
     */
    public static final int MSG_UNWIFI_NETWORK_TYPE = 403;
    /**
     * 3G网络
     */
    public static final int MSG_3G_NETWORK_TYPE = 404;
    /**
     * 2G网络
     */
    public static final int MSG_2G_NETWORK_TYPE = 405;
    /**
     * 移动网络
     */
    public static final int MSG_YIDONG_NETWORK_TYPE = 406;
    /**
     * 联通网络
     */
    public static final int MSG_LIANTONG_NETWORK_TYPE = 407;
    /**
     * 电信网络
     */
    public static final int MSG_DIANXIN_NETWORK_TYPE = 408;
    /**
     * 4G网络
     */
    public static final int MSG_4G_NETWORK_TYPE = 409;
    /**
     * 未知网络
     */
    public static final int MSG_UNKOWN_NETWORK_TYPE = 410;
    /**
     * 电信wap网络
     */
    public static final String WAP_CT = "ctwap";
    /**
     * 移动wap网络
     */
    public static final String WAP_CM = "cmwap";
    /**
     * 联通wap网络
     */
    public static final String WAP_3G = "3gwap";
    /**
     * 联通wap网络
     */
    public static final String WAP_UNI = "uniwap";

    /**
     * 添加任务成功
     */
    public static final int MSG_ADD_CACHE_SUCCESS = 501;
    /**
     * 添加任务失败
     */
    public static final int MSG_ADD_CACHE_FAIL = 502;
    /**
     * 下载任务成功
     */
    public static final int MSG_DOWNLOAD_CACHE_SUCCESS = 503;
    /**
     * 删除下载任务成功
     */
    public static final int MSG_DELETE_CACHE_SUCCESS = 504;
    /**
     * 边下边播,已下载15%以上
     */
    public static final int MSG_DOWNLOAD_PREPARE_ONLINE_CACHE_SUCCESS = 507;
    /**
     * 空间不足
     */
    public static final int MSG_MIN_SPACE = 601;
    /**
     * 达到下载上限
     */
    public static final int MSG_MAX_DOWNLOAD = 602;
    /**
     * 该视频暂时无法缓存
     */
    public static final int MSG_CACHE_ERROR = 603;
    /**
     * 文件下载中消息
     */
    public static final int MSG_DOWNLOAD_PROGRESS = 701;
    /**
     * 文件下载完成消息
     */
    public static final int MSG_DOWNLOAD_SUCCESS = 702;
    /**
     * 文件下载失败消息
     */
    public static final int MSG_DOWNLOAD_ERROR = 703;
    /**
     * 缓存开始全部成功
     */
    public static final int MSG_CACHE_START_ALL_SUCCESS = 704;
    /**
     * 缓存暂停全部成功
     */
    public static final int MSG_CACHE_PAUSE_ALL_SUCCESS = 705;
    /**
     * 本地存储空间进度变化
     */
    public static final int MSG_STORAGE_SPACE_PROGRESS = 706;
    /**
     * 存储器挂载失败
     */
    public static final int MSG_STORAGE_MOUNTED_FAIL = 707;
    /**
     * 存储器挂载成功
     */
    public static final int MSG_STORAGE_MOUNTED_SUCCESS = 708;
    private static final StorageModule mInstance = new StorageModule();
    private static Context sContext;

    /**
     * 获取文件模块的对象.<br>
     * 单例静态方法.
     *
     * @return 文件模块对象
     */
    public static StorageModule getInstance() {
        return mInstance;
    }

    private StorageModule() {

    }

    /**
     * 初始化{@link StorageModule StorageModule}
     *
     * @param context 当前上下文
     */
    public static void init(Context context) {

        if (context == null) {
            throw new NullPointerException(
                    "StorageModule.init(Context context) context is null,please call init() method");
        }
        FoneDatabase.init(context);
        FileLocalModule.init(context);
        SystemUtil.init(context);
        // 初始化通知模块
        NotificationHandle.init(context);
        OfflineCacheModule.init(context);
        ExtensionUtil.init(context);
        sContext = context;

    }

    public void scanningExternalStorage() {
        L.v(TAG, "scanningExternalStorage", "start");
        FileLocalModule.getInstance().scanningExternalStorage();
    }

    public void scanningAllStorage() {
        L.v(TAG, "scanningAllStorage", "start");
        scanningMediaStore();
        scanningExternalStorage();
    }

    public void scanningMediaStore() {
        L.v(TAG, "scanningMediaStore", "start");
        FileLocalModule.getInstance().scanningMediaStore();
    }

    /**
     * 获得目录集合(包括文件和文件夹)
     *
     * @param mediaType 文件类型
     * @return ArrayList<MediaFile> 目录集合
     */
    public ArrayList<MediaFile> getMediaFileList(int mediaType) {
        return FileDataBaseAdapter.getInstance().getMediaFileList(MediaFile.MEDIA_FILE_TYPE, mediaType, 1);
    }

    /**
     * 根据文件路径删除文件 <br>
     * 同步方法
     *
     * @param filePath 文件路径
     * @return int 操作结果<br>
     * 1 成功<br>
     * -1 删除媒体库记录失败<br>
     * -2 删除异常<br>
     * @throws NullPointerException     filePath为null
     * @throws IllegalArgumentException filePath为空字符串
     */
    public int deleteFileByFilePath(String filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException(
                    "StorageModule.deleteFileByFilePath(String filePath) filePath is null");
        }

        if ("".equals(filePath)) {
            throw new IllegalArgumentException(
                    "StorageModule.deleteFileByFilePath(String filePath) filePath is empty");
        }
        return FileLocalModule.getInstance().deleteFileByFilePath(filePath);

    }

    public void cancelScanning() {
        FileLocalModule.getInstance().cancelScanning();
    }

    /**
     * 添加缓存条目<br>
     *
     * @param offlineCache 缓存条目
     * @return int 插入结果<br>
     * 0 成功 <br>
     * 1 插入重复
     * @throws NullPointerException offlineCache为null
     */
    public int addCache(OfflineCache offlineCache) {
        if (offlineCache == null) {
            throw new NullPointerException(
                    "StorageModule.addCache(OfflineCache offlineCache) offlineCache is null");
        }

        boolean isRepeat = OfflineCacheDataBaseAdapter.getInstance().isOfflineCacheById(offlineCache.getCacheID());
        if (isRepeat) {
            L.e(TAG, "addCache", "isRepeat=true");
            return 1;
        }

        L.v(TAG, "addCache", "start");
        ArrayList<OfflineCache> offlineCacheList = new ArrayList<OfflineCache>();
        offlineCacheList.add(offlineCache);
        return addCacheList(offlineCacheList);
    }

    /**
     * 添加缓存条目集合<br>
     *
     * @param offlineCacheList 缓存条目集合
     * @return int 插入结果<br>
     * 1 成功 <br>
     * -1 插入异常
     * @throws NullPointerException offlineCache为null
     */
    public int addCacheList(ArrayList<OfflineCache> offlineCacheList) {
        if (offlineCacheList == null) {
            throw new NullPointerException(
                    "StorageModule.addCacheList(List<OfflineCache> offlineCacheList) offlineCacheList is null");
        }

        L.v(TAG, "addCacheList", "start");

        // 做添加排序
        Collections.sort(offlineCacheList, new OfflineCacheFileComparator());

        OfflineCacheModule.getInstance().addCacheList(offlineCacheList);

        return 1;
    }

    /**
     * 开启后台离线缓存服务
     *
     * @return void
     */
    public void startCacheService() {
        OfflineCacheModule.getInstance().startCacheService();
    }

    /**
     * 停止后台离线缓存服务
     *
     * @return void
     */
    public void stopCacheService() {
        OfflineCacheModule.getInstance().stopCacheService();
    }

    /**
     * 开始一个条目的下载
     *
     * @return void
     */
    public void startCache(OfflineCache offlineCache) {
        L.v(TAG, "startCache", "start");
        OfflineCacheModule.getInstance().startCache(offlineCache);
    }

    /**
     * 开始全部下载 MSG_ACTION_OPERATE_PROGRAM_TYPE
     * <p/>
     * StorageModule.MSG_ACTION_OPERATE_PROGRAM_TYPE 核心层自动判断,记录上一次下载状态<br>
     * StorageModule.MSG_ACTION_OPERATE_USER_TYPE 用户主动请求,开始所有
     *
     * @return void
     */
    public void startAllCache() {
        switch (DownloadUtil.getNetType()) {
            case StorageModule.MSG_WIFI_NETWORK_TYPE: {

                L.v(TAG, "startCacheAllQueue", "MSG_WIFI_NETWORK_TYPE");
                // 开始所有
                OfflineCacheModule.getInstance().startCacheAllQueue();

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

                    L.v(TAG, "startCacheAllQueue",
                            "MSG_UNWIFI_NETWORK_TYPE isAutoDownload="
                                    + isAutoDownload);

                    // 开始所有,仅wifi下载开关关闭(不进行网络类型过滤)
                    OfflineCacheModule.getInstance().startCacheAllQueue();

                } else {

                    L.w(TAG, "startCacheAllQueue",
                            "MSG_UNWIFI_NETWORK_TYPE isAutoDownload="
                                    + isAutoDownload);
                }
                break;
            default:

                break;
        }

    }

    /**
     * 暂停一个条目的下载
     *
     * @return void
     */
    public void pauseCache(OfflineCache offlineCache) {
        L.v(TAG, "pauseCache", "start");
        OfflineCacheModule.getInstance().pauseCache(offlineCache);
    }

    /**
     * 暂停所有条目的下载
     *
     * @param operateState 操作状态
     * @return void
     */
    public void pauseAllCache(int operateState) {

        switch (DownloadUtil.getNetType()) {
            case StorageModule.MSG_WIFI_NETWORK_TYPE: {

                L.v(TAG, "pauseAllCache", "MSG_WIFI_NETWORK_TYPE operateState="
                        + operateState);

                // 暂停所有
                OfflineCacheModule.getInstance().pauseCacheAllQueue(operateState);

                break;
            }
            case StorageModule.MSG_NO_NETWORK_TYPE:
                // 无网
                break;
            case StorageModule.MSG_2G_NETWORK_TYPE:
            case StorageModule.MSG_3G_NETWORK_TYPE:
            case StorageModule.MSG_4G_NETWORK_TYPE:

                // 非wifi网络,状态变为等待
                operateState = OfflineCache.OPERATE_STOP_BATCH_WAIT_STATE;

                L.v(TAG, "pauseAllCache", "MSG_UNWIFI_NETWORK_TYPE operateState="
                        + operateState);

                // 暂停所有
                OfflineCacheModule.getInstance().pauseCacheAllQueue(operateState);

                break;
            default:
                break;
        }

    }

    /**
     * 删除指定条目的下载
     *
     * @return void
     */
    public void deleteCache(OfflineCache offlineCache) {
        ArrayList<OfflineCache> offlineCacheList = new ArrayList<>();
        offlineCacheList.add(offlineCache);
        OfflineCacheModule.getInstance().deleteCacheList(offlineCacheList);
    }

    /**
     * 删除升级类型的缓存任务 <br>
     * 同步方法
     *
     * @return int 操作结果<br>
     * 1 删除成功<br>
     * -1 删除异常<br>
     */
    public int deleteUpgradeOfflineCache() {
        return OfflineCacheDataBaseAdapter.getInstance().deleteOfflineCacheByDownloadType(OfflineCache.CACHE_FROM_PAGE_UPGRADE);
    }

    /**
     * 删除指定条目列表的下载
     *
     * @return void
     */
    public void deleteCacheList(ArrayList<OfflineCache> offlineCacheList) {
        OfflineCacheModule.getInstance().deleteCacheList(offlineCacheList);
    }


    /**
     * 获取缓存对象集合
     *
     * @return List<OfflineCache> 缓存对象
     */
    public ArrayList<OfflineCache> getOfflineCacheList() {
        ArrayList<OfflineCache> offlineCacheList = OfflineCacheDataBaseAdapter.getInstance().getOfflineCacheList();
        List<OfflineCache> offlineCacheDeleteList = new ArrayList<>();
        for (int i = 0; i < offlineCacheList.size(); i++) {
            File file = new File(offlineCacheList.get(i).getCacheStoragePath());
            if (!file.exists()) {
                offlineCacheDeleteList.add(offlineCacheList.remove(i));
            }
        }
        L.v(TAG,"getOfflineCacheList",offlineCacheDeleteList+"");
        OfflineCacheDataBaseAdapter.getInstance().deleteOfflineCacheList(offlineCacheDeleteList);
        return offlineCacheList;
    }

    /**
     * 根据缓存Id获取缓存对象
     *
     * @return OfflineCache 缓存对象
     */
    public OfflineCache getOfflineCacheById(long Id) {
        return OfflineCacheDataBaseAdapter.getInstance().getOfflineCacheById(Id);
    }

    /**
     * 根据缓存Id判断对象是否存在
     *
     * @param Id 缓存Id
     * @return boolean 缓存对象是否存在
     */
    public boolean isOfflineCacheById(long Id) {
        return OfflineCacheDataBaseAdapter.getInstance().isOfflineCacheById(Id);
    }

    /**
     * 获取文件列表的总数量
     *
     * @return int 文件数量
     */
    public int getOfflineCacheFileCount() {
        return OfflineCacheDataBaseAdapter.getInstance().getOfflineCacheFileCount();
    }

    /**
     * 添加桌面
     *
     * @param page 桌面对象
     * @return int 操作结果<br>
     * 1插入成功<br>
     * -1 插入异常
     */
    public int addPage(Page page) {
        return LauncherPageAdapter.getInstance().addPage(page);
    }

    /**
     * 获得桌面集合
     *
     * @return ArrayList<Page> 文件夹集合
     */
    public ArrayList<Page> getPageList() {
        return LauncherPageAdapter.getInstance().getPageList();
    }

    /**
     * 根据pageId删除桌面
     *
     * @param pageId 页面Id
     * @return int 操作状态
     */
    public int deletePage(int pageId) {
        return LauncherPageAdapter.getInstance().deletePage(pageId);
    }

    /**
     * 添加桌面对象
     *
     * @param app 桌面对象
     * @return int 操作结果<br>
     * 1插入成功<br>
     * -1 插入异常
     */
    public int addItem(App app) {
        return LauncherItemAdapter.getInstance().addItem(app);
    }

    /**
     * 更新应用
     *
     * @param app 下载对象
     * @return int 操作结果<br>
     * 1插入成功<br>
     * -1 插入异常
     */
    public int updateApp(App app) {
        return LauncherItemAdapter.getInstance().updateApp(app);
    }

    /**
     * 根据itemId删除桌面对象
     *
     * @param itemId 页面Id
     * @return int 操作状态
     */
    public int deleteItem(int itemId) {
        return LauncherItemAdapter.getInstance().deleteItem(itemId);
    }

    /**
     * 获得页面集合
     *
     * @return ArrayList<App> 文件夹集合
     */
    public ArrayList<App> getAppList() {
        return LauncherItemAdapter.getInstance().getAppList();
    }

    /**
     * 根据pageId获得页面集合
     *
     * @param pageId 页面Id
     * @return ArrayList<App> 文件夹集合
     */
    public List<Item> getAppListByPageId(int pageId) {
        return LauncherItemAdapter.getInstance().getAppListByPageId(pageId);
    }

    /**
     * 获得文件夹集合
     *
     * @return ArrayList<MediaFile> 文件夹集合
     */
    public ArrayList<MediaFile> getFolderList() {
        return FileDataBaseAdapter.getInstance().getMediaFileList(MediaFile.MEDIA_FOLDER_TYPE);
    }

    /**
     * 获得目录集合
     *
     * @return List<MediaFile> 目录集合
     */
    public ArrayList<MediaFile> getUnAddMediaFileList() {
        return FileDataBaseAdapter.getInstance().getUnAddMediaFileList();
    }

    /**
     * 根据媒体类型获取自定义媒体文件列表(包括加密和未加密的)
     *
     * @param mediaFileType 媒体类型
     * @param isVisible     是否显示
     */
    public void getCustomMediaFileList(int mediaFileType, boolean isVisible) {
        ArrayList<MediaFile> mediaFileList = FileDataBaseAdapter.getInstance().getMediaFileList(MediaFile.MEDIA_FILE_TYPE, mediaFileType, isVisible ? 1 : 0);

    }

    /**
     * 根据文件夹路径获得文件集合
     *
     * @param parentName 文件夹名称
     * @param mediaType  媒体类型
     * @return ArrayList<MediaFile> 文件集合
     */
    public ArrayList<MediaFile> getMediaFileListByParentName(String parentName, int mediaType) {
        return FileDataBaseAdapter.getInstance().getCustomMediaFileList(parentName, mediaType);
    }

    /**
     * 更新媒体文件列表到播放列表
     *
     * @param fileList 文件集合
     * @return 是否添加成功
     */
    public boolean updateMediaFileList(List<MediaFile> fileList) {
        return FileDataBaseAdapter.getInstance().updateFileList(fileList);
    }

    public void addFileList(List<MediaFile> fileList) {
        FileDataBaseAdapter.getInstance().addFileList(fileList);
    }

    /**
     * 更新文件列表
     *
     * @param fileList 文件集合
     * @return boolean 是否成功
     */
    public boolean updateFileList(List<MediaFile> fileList) {
        return FileDataBaseAdapter.getInstance().updateFileList(fileList);
    }

    /**
     * 添加播放记录
     *
     * @param playRecord 播放记录对象
     * @return int 插入结果<br>
     * 1 插入成功 <br>
     * -1 插入异常
     */
    public int addPlayRecord(PlayRecord playRecord) {
        return PlayRecordDataBaseAdapter.getInstance().addPlayRecord(playRecord);
    }

    /**
     * 根据类型和连接删除播放记录 <br>
     * 同步方法
     *
     * @param playRecord 播放记录对象
     * @return int 操作结果<br>
     * 1 删除成功<br>
     * -1 删除异常<br>
     */
    public int deletePlayRecord(PlayRecord playRecord) {
        return PlayRecordDataBaseAdapter.getInstance().deletePlayRecord(playRecord);
    }

    /**
     * 根据播放地址获得播放记录
     *
     * @param playUrl 播放地址
     * @return PlayRecord 播放记录
     */
    public PlayRecord getPlayRecordByPlayUrl(String playUrl) {
        return PlayRecordDataBaseAdapter.getInstance().getPlayRecordByPlayUrl(playUrl);
    }

    /**
     * 根据播放cid获得播放记录
     *
     * @param cid 播放cid
     * @return PlayRecord 播放记录
     */
    public PlayRecord getPlayRecordByCid(long cid) {
        return PlayRecordDataBaseAdapter.getInstance().getPlayRecordByCid(cid);
    }


    /**
     * 安装下载后的apk文件
     *
     * @param filePath 文件本地路径
     * @return void
     */
    public void installApp(String filePath) {

        NotificationHandle.getInstance().installApp(filePath);
    }

    /**
     * 检查存储空间(单次检测)
     *
     * @return boolean
     */
    public void checkSingleStorageSpace() {
        new Thread(new Runnable() {

            @Override
            public void run() {

                DownloadManager.getInstance().checkSingleStorageSpace();

            }
        }).start();
    }

    /**
     * 打印栈信息
     *
     * @return void
     */
    public void writeStackTrace() {
        StackTraceElement[] stackElements = new Throwable().getStackTrace();
        if (stackElements != null) {
            for (int i = 0; i < stackElements.length; i++) {
                L.v("", "" + stackElements[i]);
            }
        }
    }

    /**
     * 文件拷贝
     *
     * @throws IOException
     */
    public void copyDataBaseToSdCard() {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            FileInputStream source = new FileInputStream(
                    sContext.getDatabasePath("transpadui.db"));
            String folderPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator + "TransPad";
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File filepath = new File(folderPath + "/transpadui.db");
            FileOutputStream target = new FileOutputStream(filepath);

            // 新建文件输入流并对它进行缓冲
            inBuff = new BufferedInputStream(source);
            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(target);
            // 缓冲数组
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            // 关闭流
            if (inBuff != null)
                try {
                    inBuff.close();
                } catch (IOException e) {
                    e.printStackTrace();

                }
            if (outBuff != null)
                try {
                    outBuff.close();
                } catch (IOException e) {
                    e.printStackTrace();

                }
        }
    }

    /**
     * 写log到存储设备
     *
     * @param tag     标志
     * @param message 信息
     * @return void
     */
    public void writeLogStorage(String level, String tag, String type,
                                String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS",
                Locale.getDefault());
        String time = sdf.format(new java.util.Date());
        String content = time + "  " + level + "   " + tag + "   " + type
                + "   " + message + "\r\n";
        File folder = new File(Environment.getExternalStorageDirectory()
                + File.separator + "100tv");
        if (!folder.exists()) {
            folder.mkdirs();

        }
        File file = new File(folder.getAbsolutePath() + File.separator
                + "download_log.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

        } catch (IOException e) {
            e.printStackTrace();

        }

        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(content.getBytes());
        } catch (FileNotFoundException e) {
            if (e != null) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            if (e != null) {
                e.printStackTrace();
            }

        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    if (e != null) {
                        e.printStackTrace();
                    }

                }
            }
        }

    }
}
