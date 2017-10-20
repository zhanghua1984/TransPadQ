package cn.transpad.transpadui.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;

import cn.transpad.transpadui.util.L;

/**
 * 图片处理工具
 *
 * @author wangyang
 * @since 2014年4月21日
 */
class BitmapUtil {

    private final static String TAG = BitmapUtil.class.getSimpleName();

    /**
     * 根据图像URL创建Bitmap
     *
     * @param url URL地址
     * @return bitmap 图片位图对象
     */
    public Bitmap createImage(String url, int screenWidth, int screenHeight) {
        // Logger.d("ImageDownloader",
        // "开始调用CreateImage():" + System.currentTimeMillis());
        Bitmap bitmap = null;
        if (url == null || url.equals("")) {
            return null;
        }
        try {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inTempStorage = new byte[100 * 1024];
            options.inPurgeable = true;
            options.inInputShareable = true;
            // 只加载图片的边界
            options.inJustDecodeBounds = true;

            // 获取Bitmap信息
            BitmapFactory.decodeFile(url, options);

            // 屏幕最大像素个数
            int maxNumOfPixels = screenWidth * screenHeight;

            // 计算采样率
            int sampleSize = computeSampleSize(options, -1, maxNumOfPixels);

            options.inSampleSize = sampleSize;

            options.inJustDecodeBounds = false;

            // 重新读入图片,此时为缩放后的图片
            bitmap = BitmapFactory.decodeFile(url, options);

        } catch (OutOfMemoryError e) {
            L.e(TAG, "OutOfMemoryError", e.getMessage());
            System.gc();
        }
        // Logger.d("ImageDownloader",
        // "结束调用CreateImage():" + System.currentTimeMillis());
        return bitmap;
    }

    /**
     * 图片缩放处理,并保存到SDCard
     *
     * @param byteArrayOutputStream 图片字节流
     * @param screenWidth           屏幕宽
     * @param screenHeight          屏幕高
     * @param url                   图片网络路径
     * @param cachePath             本地缓存父路径</br>PathCommonDefines.PHOTOCACHE_FOLDER 程序缓存图片路径;</br>
     *                              PathCommonDefines.MY_FAVOURITE_FOLDER 我的收藏图片路径
     * @param isJpg                 是否是Jpg
     * @return 缩放后的图片bitmap
     */
    public static Bitmap saveZoomBitmapToSDCard(
            ByteArrayOutputStream byteArrayOutputStream, int screenWidth,
            int screenHeight, String url, String cachePath, boolean isJpg) {

        SoftReference<Bitmap> bitmapSoftReference = null;
        try {

            byte[] byteArray = byteArrayOutputStream.toByteArray();

            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inTempStorage = new byte[16 * 1024];

            // 只加载图片的边界
            options.inJustDecodeBounds = true;

            // 获取Bitmap信息
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length,
                    options);

            // 屏幕最大像素个数
            int maxNumOfPixels = screenWidth * screenHeight;

            // 计算采样率
            int sampleSize = computeSampleSize(options, -1, maxNumOfPixels);

            options.inSampleSize = sampleSize;

            options.inJustDecodeBounds = false;

            // 重新读入图片,此时为缩放后的图片
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0,
                    byteArray.length, options);

            bitmapSoftReference = new SoftReference<Bitmap>(bitmap);

            // 保存到SDCard
            ImageSDCache.getInstance().saveBitmapToSDCard(bitmap, url,
                    cachePath, isJpg);

        } catch (Exception e) {
            Log.e("saveZoomBitmapToSDCard", "" + e);
        } finally {
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                    byteArrayOutputStream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return bitmapSoftReference.get();
    }

    /**
     * 计算采样率
     *
     * @param options
     * @param minSideLength
     * @param maxNumOfPixels
     * @return
     */
    private static int computeSampleSize(BitmapFactory.Options options,
                                         int minSideLength, int maxNumOfPixels) {

        int initialSize = computeInitialSampleSize(options, minSideLength,

                maxNumOfPixels);

        int roundedSize;

        if (initialSize <= 8) {

            roundedSize = 1;

            while (roundedSize < initialSize) {

                roundedSize <<= 1;

            }

        } else {

            roundedSize = (initialSize + 7) / 8 * 8;

        }

        return roundedSize;

    }

    /**
     * 计算初始采样率
     *
     * @param options
     * @param minSideLength
     * @param maxNumOfPixels
     * @return
     */
    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {

        double w = options.outWidth;

        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 :

                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));

        int upperBound = (minSideLength == -1) ? 128 :

                (int) Math.min(Math.floor(w / minSideLength),

                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {

            // return the larger one when there is no overlapping zone.

            return lowerBound;

        }

        if ((maxNumOfPixels == -1) &&

                (minSideLength == -1)) {

            return 1;

        } else if (minSideLength == -1) {

            return lowerBound;

        } else {

            return upperBound;

        }

    }
}
