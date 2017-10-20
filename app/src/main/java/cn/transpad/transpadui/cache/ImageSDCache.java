package cn.transpad.transpadui.cache;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.SystemUtil;


class ImageSDCache {

    private final static String TAG = ImageSDCache.class.getSimpleName();
    private final static int FREE_SD_SPACE_NEEDED_TO_CACHE = 50; // Mb
    private final static int MAX_CACHE_SIZE_NEEDED = 30; // Mb
    private final static String WHOLESALE_CONV = ".jpg";
    private BitmapUtil mBitmapTool = new BitmapUtil();
    private static final ImageSDCache mInstance = new ImageSDCache();

    static ImageSDCache getInstance() {
        return mInstance;
    }

    /**
     * 根据图片url返回图片本地路径
     *
     * @param url       图片网络路径
     * @param cachePath 本地缓存父路径<br>
     *                  SystemUtil.getIndividualCacheDirectory()
     * @return String 图片本地路径
     */
    String getImagePathByUrl(String url, String cachePath) {

        String filePath = "";
        if (isImageSDCachedByPath(url, cachePath)) {
            filePath = url != null && url.startsWith("http://") ? (cachePath
                    + File.separator + convertUrlToFileName(url)) : "";
        } else {
            L.w(TAG, "getImagePathByUrl", "Pictures don't cached into SDCard.");
        }

        return filePath;
    }

    /**
     * 根据图片路径返回Bitmap
     *
     * @param url 图片路径
     * @return bitmap
     */
    Bitmap getBitmapBySDCardPath(String url, int screenWidth, int screenHeight) {

        Bitmap bitmap = null;
        File file = new File(url);
        if (file.exists()) {
            bitmap = mBitmapTool.createImage(url, screenWidth, screenHeight);
        } else {
            L.w(TAG, "getBitmapByCachePath",
                    "Pictures don't cached into SDCard.");
        }

        return bitmap;
    }

    /**
     * 根据图片缓存路径返回Bitmap
     *
     * @param url       图片缓存路径
     * @param cachePath 本地缓存父路径</br>PathCommonDefines.PHOTOCACHE_FOLDER 程序缓存图片路径;</br>
     *                  PathCommonDefines.MY_FAVOURITE_FOLDER 我的收藏图片路径
     * @return bitmap
     */
    Bitmap getBitmapByCachePath(String url, String cachePath, int screenWidth,
                                int screenHeight) {

        Bitmap bitmap = null;
        if (isImageSDCachedByPath(url, cachePath)) {
            String filePath = cachePath + File.separator
                    + convertUrlToFileName(url);
            bitmap = mBitmapTool.createImage(filePath, screenWidth,
                    screenHeight);
        } else {
            L.w(TAG, "getBitmapByCachePath",
                    "Pictures don't cached into SDCard.");
        }

        return bitmap;
    }

    /**
     * 判断图片是否存在
     *
     * @param url       图片链接
     * @param cachePath 本地缓存父路径</br>PathCommonDefines.PHOTOCACHE_FOLDER 程序缓存图片路径;</br>
     *                  PathCommonDefines.MY_FAVOURITE_FOLDER 我的收藏图片路径
     * @return 是否存在
     */
    boolean isImageSDCachedByPath(String url, String cachePath) {
        if (url == null || url.trim().length() <= 0) {
            L.e(TAG, "NullPointException", "img url is null");
            return false;
        }

        String fileName = convertUrlToFileName(url);
        // Logger.i(TAG, "isImageSDCachedByPath+url:" + url);
        // Logger.d(TAG, "isImageSDCachedByPath+fileName:" + fileName);
        // Create Path
        File file = new File(cachePath + File.separator);
        if (!file.exists()) {
            file.mkdirs();
        }
        if (fileName != null) {
            String filePath = cachePath + File.separator + fileName;
            File f = new File(filePath);

            if (f.exists()) {
                L.i(TAG, "", "File sdCard cached_myimg exits:" + url);
                return true;
            }
        }
        return false;
    }

    /**
     * 保存Bitmap到指定的目录
     *
     * @param bitmap    保存的bitmap
     * @param url       图片网络路径
     * @param cachePath 本地缓存父路径</br>PathCommonDefines.PHOTOCACHE_FOLDER 程序缓存图片路径;</br>
     *                  PathCommonDefines.MY_FAVOURITE_FOLDER 我的收藏图片路径
     * @param isJpg     是否是JPG
     * @return 是否成功
     */
    boolean saveBitmapToSDCard(Bitmap bitmap, String url, String cachePath,
                               boolean isJpg) {

        boolean result = false;

        if (bitmap == null) {
            L.w(TAG, "NullPointException", " trying to save null bitmap");
            return false;
        }

        if (FREE_SD_SPACE_NEEDED_TO_CACHE > SystemUtil.getInstance()
                .getFreeSpaceByIndividualCache()) {
            L.w(TAG, "SDCardException", "Low free space onsd, do not cache");
            return false;
        }

        if (url == null || (url != null && url.equals(""))) {
            return false;
        }

        File makeDirectoryPathFile = new File(cachePath);

        if (!makeDirectoryPathFile.isDirectory()) {
            makeDirectoryPathFile.mkdirs();
        }

        String filename = convertUrlToFileName(url);

        File file = new File(cachePath + File.separator + filename);

        // Logger.d(TAG, "url:" + url);
        // Logger.i(TAG, "fileName:" + filename);
        OutputStream outStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {

            file.createNewFile();

            outStream = new FileOutputStream(file);

            // 压缩比例
            int quality = 100;

            // 判断是否是Jpg,png是无损压缩,jpg不用进行质量压缩
            if (bitmap != null && isJpg) {

                byteArrayOutputStream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.JPEG, quality,
                        byteArrayOutputStream);

                // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
                while (byteArrayOutputStream.toByteArray().length / 1024 > 100) {

                    // 重置saveBaos即清空saveBaos
                    byteArrayOutputStream.reset();

                    // 每次都减少10
                    quality -= 10;

                    // 这里压缩optionsNum%，把压缩后的数据存放到saveBaos�?
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality,
                            byteArrayOutputStream);

                }

                if (quality <= 0) {

                    quality = 100;

                }

                // 输出
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream);

            } else {

                // 输出
                bitmap.compress(Bitmap.CompressFormat.PNG, quality, outStream);

            }

            result = true;

        } catch (IOException e) {

            L.e(TAG, "saveBitmapToSDCard", "IOException" + e.getMessage(), e);

            result = false;

        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                    outStream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                    byteArrayOutputStream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        // 清理缓存
        removeCache(cachePath);

        return result;
    }

    private String convertUrlToFileName(String url) {
        String fn = null;
        if (url != null && url.trim().length() > 0) {

            if (url.contains(".png")) {

                fn = String.valueOf(Math.abs(url.hashCode())) + ".png";

            } else {

                fn = String.valueOf(Math.abs(url.hashCode())) + ".jpg";

            }

        }
        return fn;
    }

    /**
     * 清理缓存
     *
     * @param cachePath 本地缓存父路径</br>PathCommonDefines.PHOTOCACHE_FOLDER 程序缓存图片路径;</br>
     *                  PathCommonDefines.MY_FAVOURITE_FOLDER 我的收藏图片路径
     */
    private void removeCache(String cachePath) {
        String dirPath = cachePath;
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        int dirSize = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().contains(WHOLESALE_CONV)) {
                dirSize += files[i].length();
            }
        }
        if (dirSize > MAX_CACHE_SIZE_NEEDED * 1024 * 1024
                || FREE_SD_SPACE_NEEDED_TO_CACHE > SystemUtil.getInstance()
                .getFreeSpaceByIndividualCache()) {
            int removeFactor = (int) ((0.4 * files.length) + 1);

            Arrays.sort(files, new FileLastModifSort());
            L.i(TAG, "Clear some expiredcache files ");

            for (int i = 0; i < removeFactor; i++) {

                if (files[i].getName().contains(WHOLESALE_CONV)) {
                    files[i].delete();
                }
            }
        }
    }

    private class FileLastModifSort implements Comparator<File> {
        @Override
        public int compare(File arg0, File arg1) {
            if (arg0.lastModified() > arg1.lastModified()) {
                return 1;
            } else if (arg0.lastModified() == arg1.lastModified()) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
