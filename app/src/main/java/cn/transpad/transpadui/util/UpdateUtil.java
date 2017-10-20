package cn.transpad.transpadui.util;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.http.LoginRst;
import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.storage.StorageModule;

/**
 * Created by user on 2015/5/25.
 */
public class UpdateUtil {
    private static final String TAG = UpdateUtil.class.getSimpleName();
    private static Context sContext = null;
    //    private static final int MAX_TIME_INTERVAL = 0.02;
    private static final double MAX_TIME_INTERVAL = 24.0;

    public static void init(Context context) {
        sContext = context;
    }

    /**
     * 检查推荐升级环境，排除文件与数据库不一致的情况。
     *
     * @param loginRst
     */
    public static void checkSuggestUpdate(LoginRst loginRst) {
        String filePath = SystemUtil.getInstance().getOfflineCachePath();
        File file = new File(filePath, loginRst.softupdate.name + ".apk");
        if (isOldUpdateFile()) {
            file.delete();
            StorageModule.getInstance().deleteUpgradeOfflineCache();
        }
        int id = loginRst.softupdate.updateurl.hashCode();
        OfflineCache localOfflineCache = StorageModule.getInstance().getOfflineCacheById(id);
        if (file.exists()) {
            if (localOfflineCache != null) {
                int state = localOfflineCache.getCacheDownloadState();
                switch (state) {
                    case OfflineCache.CACHE_STATE_FINISH:
                    case OfflineCache.CACHE_STATE_DOWNLOADING:
                        break;
                    default:
                        file.delete();
                        StorageModule.getInstance().deleteUpgradeOfflineCache();
                        break;
                }
            } else {
                file.delete();
            }
        } else {
            StorageModule.getInstance().deleteUpgradeOfflineCache();
        }
    }

    /**
     * 检查升级文件是否过期，有效时间24小时。
     *
     * @return
     */
    public static boolean isOldUpdateFile() {
        Calendar calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();
        long updateTime = SharedPreferenceModule.getInstance().getLong("updateTime", 0);
        double intervalTime = (currentTime - updateTime) / 1000.0 / 60.0 / 60.0;
        L.v(TAG, "isOldUpdateFile", "diffTime" + intervalTime);
        if (intervalTime > MAX_TIME_INTERVAL || intervalTime < 0) {
            return true;
        }
        return false;
    }

    /**
     * 启动下载升级文件，若文件下载完成则跳转安装界面，若之前未下载完成则继续下载。
     * 若文件不存在则添加下载任务，并记录升级时间。
     *
     * @param loginRst
     */
    public static void startDownloadUpdateFile(LoginRst loginRst) {
        String name = loginRst.softupdate.name;
        String url = loginRst.softupdate.updateurl;
        int id = url.hashCode();
        String filePath = SystemUtil.getInstance().getOfflineCachePath();
        File file = new File(filePath, name + ".apk");
        OfflineCache localOfflineCache = StorageModule.getInstance().getOfflineCacheById(id);
        if (file.exists()) {
            if (localOfflineCache != null) {
                int state = localOfflineCache.getCacheDownloadState();
                switch (state) {
                    case OfflineCache.CACHE_STATE_FINISH:
                        StorageModule.getInstance().installApp(file.getPath());
                        return;
                    case OfflineCache.CACHE_STATE_DOWNLOADING:
                        StorageModule.getInstance().startCache(localOfflineCache);
                        return;
                }
            }
        }

//        file.delete();
//        StorageModule.getInstance().deleteUpgradeOfflineCache();
        L.v(TAG, "startDownloadUpdateFile", "startDownloadUpdateFile");
        OfflineCache offlineCache = new OfflineCache();
        offlineCache.setCacheDetailUrl(url);
        offlineCache.setCacheDownloadType(OfflineCache.CACHE_FROM_PAGE_UPGRADE);
        offlineCache.setCachePackageName(sContext.getPackageName());
        offlineCache.setCacheID(id);
        offlineCache.setCacheName(name);
        StorageModule.getInstance().addCache(offlineCache);

        Calendar calendar = Calendar.getInstance();
        SharedPreferenceModule.getInstance().setLong("updateTime", calendar.getTimeInMillis());
    }

    /**
     * 删除升级文件
     *
     * @param loginRst
     */
    public static void deleteUpdateFile(LoginRst loginRst) {
        String filePath = SystemUtil.getInstance().getOfflineCachePath();
        File file = new File(filePath, loginRst.softupdate.name + ".apk");
        int id = loginRst.softupdate.updateurl.hashCode();
        OfflineCache localOfflineCache = StorageModule.getInstance().getOfflineCacheById(id);
        if (localOfflineCache != null) {
            StorageModule.getInstance().deleteCache(localOfflineCache);
            file.delete();
        }
    }

    /**
     * 检查强制升级文件是否可用，排除文件与数据库不一致的情况。
     *
     * @param loginRst
     * @return true：可用 false：不可用
     */
    public static boolean checkForceUpdateFileAvailable(LoginRst loginRst) {
        String filePath = SystemUtil.getInstance().getOfflineCachePath();
        File file = new File(filePath, loginRst.softupdate.name + ".apk");
        if (UpdateUtil.isOldUpdateFile()) {
            file.delete();
            StorageModule.getInstance().deleteUpgradeOfflineCache();
        }
        int id = loginRst.softupdate.updateurl.hashCode();
        OfflineCache localOfflineCache = StorageModule.getInstance().getOfflineCacheById(id);
        if (file.exists()) {
            if (localOfflineCache != null) {
                int state = localOfflineCache.getCacheDownloadState();
                switch (state) {
                    case OfflineCache.CACHE_STATE_FINISH:
                        StorageModule.getInstance().installApp(file.getPath());
//                        finish();
                        return true;
                    case OfflineCache.CACHE_STATE_DOWNLOADING:
                        break;
                    default:
                        L.v(TAG, "checkForceUpdateFileAvailable", "localoffline" + localOfflineCache);
                        file.delete();
                        StorageModule.getInstance().deleteUpgradeOfflineCache();
                        Toast.makeText(sContext, R.string.setting_update_force_fail, Toast.LENGTH_SHORT).show();
                        break;
                }
            } else {
                file.delete();
            }
        }
        return false;
    }

    /**
     * 启动应用删除之前未下载完成的文件。防止在下载过程中清理内存
     *
     * @param loginRst
     */
    public static void deleteOldDownloadingUpdateFile(LoginRst loginRst) {
        L.v(TAG, "deleteOldDownloadingUpdateFile", "start");
        if (loginRst != null && loginRst.softupdate != null) {
            String filePath = SystemUtil.getInstance().getOfflineCachePath();
            File file = new File(filePath, loginRst.softupdate.name + ".apk");
            OfflineCache localOfflineCache = StorageModule.getInstance().getOfflineCacheById(loginRst.softupdate.updateurl.hashCode());
            if (file.exists()) {
                if (localOfflineCache != null) {
                    int state = localOfflineCache.getCacheDownloadState();
                    switch (state) {
                        case OfflineCache.CACHE_STATE_FINISH:
                            break;
                        case OfflineCache.CACHE_STATE_DOWNLOADING:
                            file.delete();
                            StorageModule.getInstance().deleteUpgradeOfflineCache();
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }
}
