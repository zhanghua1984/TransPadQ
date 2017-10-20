package cn.transpad.transpadui.storage.download;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.storage.StorageConfig;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;

/**
 * 提供报文处理方法
 *
 * @author Administrator
 */
public class OfflineCachePacketDownloadManager {
    private static final String TAG = "OfflineCachePacketDownloadManager";
    public static final String FILE_PATH_EXTENTION = ".fem";
    private static Context mContext = null;
    private static final OfflineCachePacketDownloadManager mInstance = new OfflineCachePacketDownloadManager();

    public static OfflineCachePacketDownloadManager getInstance() {
        return mInstance;
    }

    public static void init(Context context) {
        mContext = context;
    }


    public String parseXYZPlayUrl(OfflineCache offlineCache) {

        String pageUrl = "&comefrom="
                + offlineCache.getCacheReportPage();
        StringBuffer output = new StringBuffer(pageUrl);
        if (!pageUrl.endsWith("&") && !pageUrl.endsWith("?")) {
            if (pageUrl.contains("?")) {
                output.append("&");
            } else {
                output.append("?");
            }
        }
        final String xyzPlayUrl = TPUtil.handleUrl(pageUrl == null ? ""
                : pageUrl);
        v(TAG, "parseXYZPlayUrl", "start file=" + offlineCache.toString());
        return xyzPlayUrl;
    }

    /**
     * 根据参数获取分片存储路径(root+视频来源名称+ccid_cid+fragmentIndex.fem)
     *
     * @param offlineCache
     * @param fragmentIndex
     * @return String
     */
    public String getOfflineCacheFragmentStoragePath(OfflineCache offlineCache,
                                                     int fragmentIndex) {
        String filePath = offlineCache.getCacheStoragePath();
        StringBuilder storagePath = new StringBuilder();
        storagePath.append(filePath);
        storagePath.append(File.separator);
        storagePath.append(fragmentIndex + 1);
        storagePath.append(FILE_PATH_EXTENTION);
        File file = new File(storagePath.toString());

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                if (e != null) {
                    e.printStackTrace();
                }

            }
        }
        return storagePath.toString();
    }

    /**
     * 根据参数获取视频存储路径(root+视频来源名称+ccid_cid)
     *
     * @param offlineCachePath
     * @param offlineCache
     * @return String
     */
    public String getOfflineCacheFileStoragePath(String offlineCachePath,
                                                 OfflineCache offlineCache) {
        StringBuilder storagePath = new StringBuilder();
        storagePath.append(offlineCachePath);
        storagePath.append(File.separator);
        File file = new File(storagePath.toString());
        if (!file.exists()) {
            file.mkdirs();
        }
        return storagePath.toString();
    }

    /*
    * MD5加密
    */
    public String getMD5(String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException caught!");
            System.exit(-1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] byteArray = messageDigest.digest();
        StringBuffer md5StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        //16位加密，从第9位到25位
        return md5StrBuff.substring(8, 24).toString().toUpperCase();
    }

    static void v(String tag, String type, String msg) {
        if (StorageConfig.CACHE_MESSAGE_HANDLER_LOG) {
            L.v(tag, type, msg);
        }
    }

    static void e(String tag, String type, String msg) {
        if (StorageConfig.CACHE_MESSAGE_HANDLER_LOG) {
            L.e(tag, type, msg);
        }
    }
}
