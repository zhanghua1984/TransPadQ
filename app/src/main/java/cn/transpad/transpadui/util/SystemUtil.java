package cn.transpad.transpadui.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.constant.FoneConstant;
import cn.transpad.transpadui.storage.SharedPreferenceModule;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * 系统信息工具<br>
 *
 * @author wangyang
 */
public class SystemUtil {
    private final static String TAG = SystemUtil.class.getSimpleName();

    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String NO_MEDIA = ".nomedia";
    private static final String INDIVIDUAL_DIR_NAME = "images";
    public static final String CACHE_DOWNLOAD = "file";
    public static final String ONLINE_PICTURE = "picture";
    public static final String DOWNLOAD_APK = "apk";
    public static final String ONLINE_CACHE = "cache";

    public static final int CACHE_DOWNLOAD_TYPE = 1;
    public static final int ONLINE_PICTURE_TYPE = 2;
    public static final int DOWNLOAD_APK_TYPE = 3;
    public static final int ONLINE_CACHE_TYPE = 4;
    private static final SystemUtil sSystemUtil = new SystemUtil();
    private static Context sContext;
    private HashMap<String, String> mPathMap = new HashMap<String, String>();
    // 缓存界面使用
    private boolean mIsMounted = false;
    /**
     * 手机机身存储路径
     */
    private static final String INTERNAL_PATH = "internal";
    /**
     * 手机SDcard存储路径
     */
    private static final String EXTERNAL_PATH = "exterNal";

    /**
     * 获取{@link SystemUtil}对象<br>
     * 单例方法<br>
     * 同步
     *
     * @return {@link SystemUtil}对象
     */
    public static SystemUtil getInstance() {
        return sSystemUtil;
    }

    /**
     * 初始化{@link SystemUtil}
     *
     * @param context 当前上下文
     */
    public static void init(Context context) {

        if (context == null) {
            throw new NullPointerException("Context is null");
        }
        sContext = context;

    }

    /**
     * 获取机身可用空间(字节)
     *
     * @return long 机身可用空间字节数
     */
    public long getFreeSpaceByteByPhoneBody() {
        long freeSpace = 0;
        // 得到外部存储的路径
        String externalStorageDirectory = getInternalPhoneBodyPath();
        if (!"".equals(externalStorageDirectory)) {
            // 计算空间
            StatFs stat = new StatFs(externalStorageDirectory);
            long blockSize = stat.getBlockSize();
            long availableBlock = stat.getAvailableBlocks();
            freeSpace = blockSize * availableBlock;
        }

        return freeSpace;
    }

    /**
     * 获取机身空间总的大小(字节)
     *
     * @return long 机身空间总的字节数
     */
    public long getTotalSpaceByteByPhoneBody() {
        long totalSpace = 0;
        // 得到外部存储的路径
        String externalStorageDirectory = getInternalPhoneBodyPath();
        if (!"".equals(externalStorageDirectory)) {
            // 计算空间
            StatFs stat = new StatFs(externalStorageDirectory);
            long blockSize = stat.getBlockSize();
            long totalBlock = stat.getBlockCount();
            totalSpace = blockSize * totalBlock;
        }

        return totalSpace;
    }

    /**
     * 获取格式化字节数为字符串
     *
     * @return String 字符串
     */
    public String getFreeSpaceFormat(long size) {
        return Formatter.formatFileSize(sContext, size);
    }

    /**
     * 获取SDCard可用空间(字节)
     *
     * @return String SDCard可用空间
     */
    public long getFreeSpaceByteBySDCard() {
        // 总大小
        long allStorageFreeSpace = getFreeSpaceByteByAllStorage();
        // 机身大小
        long phoneBodyFreeSpace = getFreeSpaceByteByPhoneBody();

        return allStorageFreeSpace - phoneBodyFreeSpace;
    }

    /**
     * 获取SDCard空间总的大小(字节)
     *
     * @return long SDCard空间总的字节数
     */
    public long getTotalSpaceByteBySDCard() {
        // 总大小
        long allStorageTotalSpace = getTotalSpaceByteByAllStorage();
        // 机身大小
        long phoneBodyTotalSpace = getTotalSpaceByteByPhoneBody();

        return allStorageTotalSpace - phoneBodyTotalSpace;
    }

    /**
     * 存储设备是否挂载(机身和sdcard)
     *
     * @return boolean 是否挂载
     */
    public boolean isStorageMounted() {
        //满足其中一个则认为挂载成功
        return isSDCardMounted() || isPhoneBodyMounted();
    }

    /**
     * 判断SDCard是否挂载
     *
     * @return boolean <br>
     * true 已经挂载 <br>
     * false 未挂载
     */
    public boolean isSDCardMounted() {
        L.v(TAG, "isSDCardMounted", "start");
        // google要求4.4以上版本(包括4.4)sdcard不允许第三方应用操作,除非有root权限
        // 所以大于4.4的直接显示机身空间

        mIsMounted = VERSION.SDK_INT < 19 && !TextUtils.isEmpty(getExternalSDCardPath());

        L.v(TAG, "isSDCardMounted", "mIsMounted=" + mIsMounted);

        return mIsMounted;
    }

    /**
     * 机身内存是否已经挂载
     *
     * @return boolean 是否挂载
     */
    public boolean isPhoneBodyMounted() {

        L.v(TAG, "isPhoneBodyMounted", "start");

        mIsMounted = !TextUtils.isEmpty(getInternalPhoneBodyPath());

        L.v(TAG, "isPhoneBodyMounted", "mIsMounted=" + mIsMounted);

        return mIsMounted;
    }

    /**
     * 获取SDCard和机身可用空间之和(字节)
     *
     * @return long SDCard和机身可用空间之和
     */
    private long getFreeSpaceByteByAllStorage() {

        // 得到已经挂载的存储路径
        HashMap<String, String> mountStorageHashMap = getMountStoragePathByShell();
        // 得到外部存储的路径
        String externalStorageDirectory = Environment
                .getExternalStorageDirectory().getAbsolutePath();
        mountStorageHashMap.put(externalStorageDirectory,
                externalStorageDirectory);

        // 遍历每个目录,并计算空间
        Set<String> keySet = mountStorageHashMap.keySet();
        long freeSpace = 0;
        for (String path : keySet) {
            StatFs stat = new StatFs(path);
            long blockSize = stat.getBlockSize();
            long availableBlock = stat.getAvailableBlocks();
            freeSpace += blockSize * availableBlock;
        }

        return freeSpace;
    }

    /**
     * 获取SDCard和机身总空间之和(字节)
     *
     * @return String SDCard和机身可用空间之和
     */
    private long getTotalSpaceByteByAllStorage() {

        // 得到已经挂载的存储路径
        HashMap<String, String> mountStorageHashMap = getMountStoragePathByShell();
        // 得到外部存储的路径
        String externalStorageDirectory = Environment
                .getExternalStorageDirectory().getAbsolutePath();
        mountStorageHashMap.put(externalStorageDirectory,
                externalStorageDirectory);

        // 遍历每个目录,并计算空间
        Set<String> keySet = mountStorageHashMap.keySet();
        long totalSpace = 0;
        for (String path : keySet) {

            StatFs stat = new StatFs(path);
            long blockSize = stat.getBlockSize();
            long totalBlock = stat.getBlockCount();
            totalSpace += blockSize * totalBlock;
        }

        return totalSpace;
    }

    /**
     * 获取个人缓存目录可用存储空间(图片缓存模块私有)
     *
     * @return 个人缓存目录可用存储空间
     */
    public int getFreeSpaceByIndividualCache() {
        int freeSize = 0;

        if (isStorageMounted()) {
            StatFs statFs = new StatFs(getIndividualCacheDirectory()
                    .getAbsolutePath());

            try {
                long nBlocSize = statFs.getBlockSize();
                long nAvailaBlock = statFs.getAvailableBlocks();
                freeSize = (int) (nBlocSize * nAvailaBlock / 1024 / 1024);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return freeSize;
    }

    /**
     * Returns application cache directory. Cache directory will be created on
     * SD card <i>("/Android/data/[app_package_name]/cache")</i> if card is
     * mounted and app has appropriate permission. Else - Android defines cache
     * directory on device's file system.
     *
     * @return Cache {@link File directory}.<br />
     * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card
     * is unmounted and {@link Context#getCacheDir()
     * Context.getCacheDir()} returns null).
     */
    public File getCacheDirectory() {
        return getCacheDirectory(true);
    }

    /**
     * Returns application cache directory. Cache directory will be created on
     * SD card <i>("/Android/data/[app_package_name]/cache")</i> (if card is
     * mounted and app has appropriate permission) or on device's file system
     * depending incoming parameters.
     *
     * @param preferExternal Whether prefer external location for cache
     * @return Cache {@link File directory}.<br />
     * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card
     * is unmounted and {@link Context#getCacheDir()
     * Context.getCacheDir()} returns null).
     */
    File getCacheDirectory(boolean preferExternal) {
        File appCacheDir = null;
        if (preferExternal
                && MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                && hasExternalStoragePermission(sContext)) {
            appCacheDir = getExternalCacheDir(sContext);
        }
        if (appCacheDir == null) {
            appCacheDir = sContext.getCacheDir();
        }
        if (appCacheDir == null) {
            String cacheDirPath = "/data" + File.separator + "data"
                    + File.separator + sContext.getPackageName() + "/cache/";
            Log.w(TAG,
                    "Can't define system cache directory! '%s' will be used."
                            + cacheDirPath);
            appCacheDir = new File(cacheDirPath);
        }
        return appCacheDir;
    }

    /**
     * Returns individual application cache directory (for only image caching
     * from ImageLoader). Cache directory will be created on SD card
     * <i>("/Android/data/[app_package_name]/cache/uil-images")</i> if card is
     * mounted and app has appropriate permission. Else - Android defines cache
     * directory on device's file system.
     *
     * @return Cache {@link File directory}
     */
    public File getIndividualCacheDirectory() {
        File cacheDir = getCacheDirectory();
        File individualCacheDir = new File(cacheDir, INDIVIDUAL_DIR_NAME);
        if (!individualCacheDir.exists()) {
            if (!individualCacheDir.mkdir()) {
                individualCacheDir = cacheDir;
            }
        }
        return individualCacheDir;
    }

    private File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(
                Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(
                new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                Log.w(TAG, "Unable to create external cache directory");
                return null;
            }
            try {
                boolean result = new File(appCacheDir, NO_MEDIA).createNewFile();
                L.v(TAG, "getExternalCacheDir", "result=" + result);
            } catch (IOException e) {
                Log.i(TAG,
                        "Can't create " + NO_MEDIA + " file in application external cache directory");
            }
        }
        return appCacheDir;
    }

    private boolean hasExternalStoragePermission(Context context) {
        int perm = context
                .checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    public String getAppVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi != null ? pi.versionName : "";
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取缓存目录
     *
     * @param type 1:缓存下载目录 ， 2： 海报， 3：下载apk 4.cache文件夹
     * @return String
     */
    public String getFoneCacheFolder(int type) {
        String status = Environment.getExternalStorageState();
        L.v(TAG, "getFoneCacheFolder", "status:" + status);
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            String path = null;
            switch (type) {
                case CACHE_DOWNLOAD_TYPE: // 离线缓存
                    path = SystemUtil.getInstance().getRootPath() + File.separator
                            + CACHE_DOWNLOAD;
                    break;
                case ONLINE_PICTURE_TYPE:
                    path = SystemUtil.getInstance().getRootPath() + File.separator
                            + ONLINE_PICTURE;
                    break;
                case DOWNLOAD_APK_TYPE:
                    path = SystemUtil.getInstance().getRootPath() + File.separator
                            + DOWNLOAD_APK;
                    break;
                case ONLINE_CACHE_TYPE:
                    path = SystemUtil.getInstance().getRootPath() + File.separator
                            + ONLINE_CACHE;
                    break;
                default:
                    break;
            }
            return path;
        }

        File file = new File(SystemUtil.getInstance().getRootPath());
        if (!file.exists()) {
            boolean result = file.mkdirs();
            L.v(TAG, "getFoneCacheFolder", "file mkdirs result=" + result);
        }

        File childFile = null;
        switch (type) {
            case CACHE_DOWNLOAD_TYPE:// 离线缓存
                // childFile = new File(getCachePath() + File.separator
                // + CACHE_DOWNLOAD);
                childFile = new File(SystemUtil.getInstance().getRootPath()
                        + File.separator + CACHE_DOWNLOAD);
                if (!childFile.exists()) {
                    boolean result = childFile.mkdirs();
                    L.v(TAG, "getFoneCacheFolder", "CACHE_DOWNLOAD_TYPE mkdirs result=" + result);
                }
                break;
            case ONLINE_PICTURE_TYPE:
                childFile = new File(SystemUtil.getInstance().getRootPath()
                        + File.separator + ONLINE_PICTURE);
                if (!childFile.exists()) {
                    boolean result = childFile.mkdirs();
                    L.v(TAG, "getFoneCacheFolder", "ONLINE_PICTURE_TYPE mkdirs result=" + result);
                }
                break;
            case DOWNLOAD_APK_TYPE:
                childFile = new File(SystemUtil.getInstance().getRootPath()
                        + File.separator + DOWNLOAD_APK);
                if (!childFile.exists()) {
                    boolean result = childFile.mkdirs();
                    L.v(TAG, "getFoneCacheFolder", "DOWNLOAD_APK_TYPE mkdirs result=" + result);
                }
                break;
            case ONLINE_CACHE_TYPE:
                childFile = new File(SystemUtil.getInstance().getRootPath()
                        + File.separator + ONLINE_CACHE);
                if (!childFile.exists()) {
                    boolean result = childFile.mkdirs();
                    L.v(TAG, "getFoneCacheFolder", "ONLINE_CACHE_TYPE mkdirs result=" + result);
                }
                break;
            default:
                break;
        }

        if (childFile == null) {
            return null;
        }

        L.v(TAG, "getFoneCacheFolder childFile:", "childFile getPath : "
                + childFile.getPath() + " type : " + type);

        return childFile.getPath();
    }

    /**
     * 删除指定路径文件夹中文件(包括该文件夹中的文件 及子文件夹)
     *
     * @param path 路径
     * @return boolean 是否成功
     */
    public boolean deleteAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            return false;
        }
        String[] tempList = file.list();
        File temp;
        for (String tempPath : tempList) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempPath);
            } else {
                temp = new File(path + File.separator + tempPath);
            }
            if (temp.isFile()) {
                boolean result = temp.delete();
                L.v(TAG, "deleteAllFile", "result=" + result + " path=" + temp.getAbsolutePath());
            }
            if (temp.isDirectory()) {
                deleteAllFile(path + "/" + tempPath);// 先删除文件夹里面的文件
                deleteFolder(path + "/" + tempPath);// 再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 删除指定路径文件夹
     *
     * @param folderPath 文件夹路径
     */
    public void deleteFolder(String folderPath) {
        try {
            deleteAllFile(folderPath); // 删除完里面所有内容
            File myFilePath = new File(folderPath);
            // 删除空文件夹
            boolean result = myFilePath.delete();
            L.v(TAG, "deleteFolder", "result=" + result + " path=" + myFilePath.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否达到上限空间
     *
     * @return boolean true->已经到达上限 false->未到上限
     */
    public boolean isUpperLimitSpace() {
        // 判断是机身还是sdcard
        long freeSpace = mIsMounted ? getFreeSpaceByteBySDCard()
                : getFreeSpaceByteByPhoneBody();

        long limitSize = SharedPreferenceModule.getInstance().getLong(
                FoneConstant.TMP_LIMIT_SIZE);
        L.v(TAG, "isUpperLimitSpace", "freeSpace=" + freeSpace + " limitSize="
                + limitSize);
        // 判断是否超过限制
        return freeSpace <= limitSize;
    }

    /**
     * 获取内部机身存储路径
     *
     * @return 机身存储路径
     */
    public String getInternalPhoneBodyPath() {
        L.v(TAG, "getInternalPhoneBodyPath", "start");
        // 获取存储路径
        ArrayList<String> filePathList = getAllStoragePath();

        // 保存存储路径
        saveToPathMap(filePathList);
        String internalPath = mPathMap.get(INTERNAL_PATH);
        return internalPath == null ? "" : internalPath;
    }

    /**
     * 获取外部Sdcard存储路径
     *
     * @return 外部Sdcard存储路径
     */
    public String getExternalSDCardPath() {

        L.v(TAG, "getExternalSDCardPath", "start");

        // 获取存储路径
        ArrayList<String> filePathList = getAllStoragePath();

        // 保存存储路径
        saveToPathMap(filePathList);

        String externalPath = mPathMap.get(EXTERNAL_PATH);

        return externalPath == null ? "" : externalPath;
    }

    /**
     * 处理特殊机型的存储路径
     *
     * @param waitArrayPath 待处理的存储路径
     */
    public void saveToPathMap(ArrayList<String> waitArrayPath) {
        mPathMap.clear();
        String sysInternalPath = Environment.getExternalStorageDirectory()
                .getPath();
        L.v(TAG, "saveToPathMap", "start waitArrayPath=" + waitArrayPath
                + " externalStorageDirectory=" + sysInternalPath);
        if (waitArrayPath == null || waitArrayPath.size() == 0) {
            mPathMap.put(INTERNAL_PATH, sysInternalPath);
            return;
        }

        // 如果是特殊机型,需要将机身路径和sdcard路径调换
        if (isSpecialPhone(android.os.Build.MODEL)) {
            L.v(TAG, "saveToPathMap", "isSpecialPhone=true");
            for (int i = 0; i < waitArrayPath.size(); i++) {
                String path = waitArrayPath.get(i);
                if (path.equals(sysInternalPath)) {
                    // 外置sd
                    if (new File(path).exists() && new File(path).canWrite()) {
                        // L.v(TAG, "saveToPathMap", "EXTERNAL_PATH=" + path);
                        mPathMap.put(EXTERNAL_PATH, path);
                    } else {
                        L.e(TAG, "saveToPathMap", "not write INTERNAL_PATH="
                                + path);
                    }
                } else {// 外置sd
                    if (new File(path).exists() && new File(path).canWrite()
                            && filterStoragePath(path)) {
                        // L.v(TAG, "saveToPathMap", "INTERNAL_PATH=" + path);
                        mPathMap.put(INTERNAL_PATH, path);
                    } else {
                        L.e(TAG, "saveToPathMap", "not write INTERNAL_PATH="
                                + path);
                    }
                }

            }
        } else {
            L.v(TAG, "saveToPathMap", "isSpecialPhone=false");
            for (int i = 0; i < waitArrayPath.size(); i++) {
                String path = waitArrayPath.get(i);
                if (path.equals(sysInternalPath)) { // 内置sd
                    if (new File(path).exists() && new File(path).canWrite()) {
                        // L.v(TAG, "saveToPathMap", "INTERNAL_PATH=" + path);
                        mPathMap.put(INTERNAL_PATH, path);
                    } else {
                        L.e(TAG, "saveToPathMap", "not write INTERNAL_PATH="
                                + path);
                    }
                } else {// 外置sd
                    if (new File(path).exists() && new File(path).canWrite()
                            && filterStoragePath(path)) {
                        // L.v(TAG, "saveToPathMap", "EXTERNAL=" + path);
                        mPathMap.put(EXTERNAL_PATH, path);
                    } else {
                        L.e(TAG, "saveToPathMap", "not write EXTERNAL_PATH="
                                + path);
                    }
                }
            }
        }
        if (mPathMap.size() == 0) {
            mPathMap.put(INTERNAL_PATH, sysInternalPath);
        }

        L.v(TAG, "saveToPathMap", "mPathMap=" + mPathMap);
    }

    /**
     * 特殊手机：只有外置sd卡，没有手机存储
     *
     * @param phone 手机名称
     * @return boolean 是否是特殊手机
     */
    public boolean isSpecialPhone(String phone) {
        String specialPhoneArray[] = sContext.getResources().getStringArray(
                R.array.cache_phone_facturers);
        L.v(TAG, "isSpecialPhone", "start currentPhone=" + phone
                + " specialPhone=" + Arrays.toString(specialPhoneArray));
        boolean res = false;
        for (String specialPhone : specialPhoneArray) {
            if (specialPhone.toLowerCase(Locale.getDefault()).equals(
                    phone.toLowerCase(Locale.getDefault()))) {
                res = true;
                break;
            }
        }
        return res;
    }

    /**
     * 过滤特殊手机扫到的假的外置sd卡路径
     *
     * @param path 待扫描路径
     * @return boolean 是否扫描到
     */
    public boolean filterStoragePath(String path) {
        boolean res = true;
        if (path.equals("/firmware")) {
            res = false;
        }
        return res;
    }

    /**
     * 获取离线缓存路径(默认存储在sdcard中)
     *
     * @return 离线缓存路径
     */
    public String getOfflineCachePath() {
        String path = getRootPath();
        path += File.separator + CACHE_DOWNLOAD;
        File file = new File(path);
        if (!file.exists()) {
            boolean result = file.mkdirs();
            L.v(TAG, "getOfflineCachePath", "result=" + result + " path=" + path);
        }
        return path;
    }

    /**
     * 获取离线缓存路径(默认存储在sdcard中)
     *
     * @return 离线缓存路径
     */
    public String getRootPath() {

        String path = isSDCardMounted() ? getExternalSDCardPath() : getInternalPhoneBodyPath();

        // 尝试创建路径
        path = createPath(path);

        // google要求4.4以上版本(包括4.4)sdcard不允许第三方应用操作,除非有root权限
        // 所以要判断路径是否创建成功
        if (VERSION.SDK_INT >= 19) {

            path = getInternalPhoneBodyPath();

            path = createPath(path);

        }
        return path;
    }

    /**
     * 获取TransPad根目录(默认存储在sdcard中)
     *
     * @return 离线缓存路径
     */
    public String getTPRootPath() {

        String path = isSDCardMounted() ? getExternalSDCardPath() : getInternalPhoneBodyPath();

        // 尝试创建路径
        path = createTPRootPath(path);

        // google要求4.4以上版本(包括4.4)sdcard不允许第三方应用操作,除非有root权限
        // 所以要判断路径是否创建成功
        if (VERSION.SDK_INT >= 19) {

            path = getInternalPhoneBodyPath();

            path = createTPRootPath(path);

        }
        return path;
    }

    /**
     * 获取离线缓存路径(默认存储在sdcard中)
     *
     * @return 离线缓存路径
     */
    public String get100TVPath() {

        String path = isSDCardMounted() ? getExternalSDCardPath() : getInternalPhoneBodyPath();

        // google要求4.4以上版本(包括4.4)sdcard不允许第三方应用操作,除非有root权限
        // 所以要判断路径是否创建成功
        if (VERSION.SDK_INT >= 19) {

            path = getInternalPhoneBodyPath();

        }

        // 尝试创建路径
        path += File.separator + "100tv";

        return path;
    }

    private String createPath(String path) {
        path += File.separator + "TransPad" + File.separator + "Q";
        File file = new File(path);
        if (!file.exists()) {
            boolean result = file.mkdirs();
            L.v(TAG, "createPath", "result=" + result + " path=" + path);
        }
        return path;
    }

    private String createTPRootPath(String path) {
        path += File.separator + "TransPad";
        File file = new File(path);
        if (!file.exists()) {
            boolean result = file.mkdirs();
            L.v(TAG, "createPath", "result=" + result + " path=" + path);
        }
        return path;
    }

    /**
     * 返回当前设备已经挂载的所有路径
     *
     * @return ArrayList<String> 路径集合
     */
    public ArrayList<String> getAllStoragePath() {

        // 得到已经挂载的存储路径
        HashMap<String, String> mountStorageHashMap = getMountStoragePathByFSTab();
        if (mountStorageHashMap == null || mountStorageHashMap.size() == 0) {
            // 得到已经挂载的存储路径
            mountStorageHashMap = getMountStoragePathByShell();
        }

        // 得到外部存储的路径
        String externalStorageDirectory = Environment
                .getExternalStorageDirectory().getAbsolutePath();
        File file = new File(externalStorageDirectory);
        if (file.exists()) {
            File[] fileArrys = file.listFiles();
            if (fileArrys != null && fileArrys.length > 0) {
                mountStorageHashMap.put(externalStorageDirectory,
                        externalStorageDirectory);
            } else {
                String sdcardFilePath = "/sdcard";
                file = new File(sdcardFilePath);
                if (file.exists()) {
                    mountStorageHashMap.put(sdcardFilePath,
                            sdcardFilePath);
                }
            }
        } else {
            String sdcardFilePath = "/sdcard";
            file = new File(sdcardFilePath);
            if (file.exists()) {
                mountStorageHashMap.put(sdcardFilePath,
                        sdcardFilePath);
            }
        }
        // 遍历每个目录,并计算空间
        Set<String> keySet = mountStorageHashMap.keySet();
        return new ArrayList<String>(keySet);
    }

    /**
     * 根据fstab文件获取已挂载的存储路径
     *
     * @return ArrayList<String> 路径集合
     */
    private HashMap<String, String> getMountStoragePathByFSTab() {
        L.v(TAG, "getMountStoragePathByFSTab", "start");
        HashMap<String, String> mountStorageHashMap = new HashMap<String, String>();
        String fstabFilePath = "/system/etc/vold.fstab";
        if (new File(fstabFilePath).exists()) {
            try {
                FileReader fileReader = new FileReader(fstabFilePath);
                BufferedReader bufferedReader = new BufferedReader(fileReader,
                        8192 * 2);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if ((!line.contains("#")) && (line.length() != 0)) {
                        if (line.contains("mmc")) {

                            // 分隔
                            String[] matchArray = line.split(" ");

                            if (matchArray.length >= 2) {

                                String filePath = matchArray[2];

                                File file = new File(filePath);

                                if (file.exists()) {

                                    // 将不规则的路径转换成规则的路径
                                    String fileCanonicalPath = file
                                            .getCanonicalPath();

                                    // 过滤特殊路径
                                    if (fileCanonicalPath.contains("legacy")
                                            || fileCanonicalPath
                                            .contains("Android/obb")
                                            || fileCanonicalPath
                                            .contains("shell")
                                            || fileCanonicalPath
                                            .contains("data")) {
                                        String externalStorageDirectory = Environment
                                                .getExternalStorageDirectory()
                                                .getAbsolutePath();
                                        File fileTemp = new File(
                                                externalStorageDirectory);
                                        if (fileTemp.exists()) {
                                            fileCanonicalPath = externalStorageDirectory;
                                        }

                                    }

                                    mountStorageHashMap.put(fileCanonicalPath,
                                            fileCanonicalPath);

                                }

                            } else {

                                L.e(TAG,
                                        "getMountStoragePathByFSTab",
                                        "matchArray="
                                                + Arrays.toString(matchArray));
                            }

                        }
                    }
                }

                fileReader.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                L.e(TAG, "getMountStoragePathByFSTab", e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                L.e(TAG, "getMountStoragePathByFSTab", e.getMessage());
            }
        }
        return mountStorageHashMap;
    }

    /**
     * 通过adb shell mount方式获取存储路径(解决机身和sdcard共存时,获取不到sdcard路径问题)
     *
     * @return HashMap&lt;String, String&gt; 存储路径集合
     */
    private HashMap<String, String> getMountStoragePathByShell() {
        L.v(TAG, "getMountStoragePathByShell", "start");
        HashMap<String, String> mountStorageHashMap = new HashMap<String, String>();
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {

                if (line.contains("secure"))
                    continue;
                if (line.contains("asec"))
                    continue;

                // 判断磁盘格式(firmware是网络模块,非存储,过滤)
                if ((line.contains("fat") || line.contains("fuse"))
                        && (!line.contains("firmware") && !line
                        .contains("ext4"))) {
                    String columns[] = line.split(" ");
                    if (columns.length > 1) {
                        String path = columns[1];
                        File file = new File(path);
                        if (file.exists()) {
                            // 将不规则的路径转换成规则的路径
                            String fileCanonicalPath = file.getCanonicalPath();

                            // 过滤特殊路径
                            if (fileCanonicalPath.contains("legacy")
                                    || fileCanonicalPath
                                    .contains("Android/obb")
                                    || fileCanonicalPath.contains("shell")
                                    || fileCanonicalPath.contains("data")) {
                                String externalStorageDirectory = Environment
                                        .getExternalStorageDirectory()
                                        .getAbsolutePath();
                                File fileTemp = new File(
                                        externalStorageDirectory);
                                if (fileTemp.exists()) {
                                    fileCanonicalPath = externalStorageDirectory;
                                }

                            }

                            mountStorageHashMap.put(fileCanonicalPath,
                                    fileCanonicalPath);
                        }

                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mountStorageHashMap;
    }

}
