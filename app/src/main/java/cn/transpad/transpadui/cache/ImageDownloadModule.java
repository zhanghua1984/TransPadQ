package cn.transpad.transpadui.cache;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.widget.ImageView;

import com.fone.player.FonePlayer;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import org.apache.http.HttpStatus;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.datatype.Artwork;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.RejectedExecutionException;
import java.util.regex.Pattern;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.ScreenUtil;
import cn.transpad.transpadui.util.StringUtil;
import cn.transpad.transpadui.util.SystemUtil;

/**
 * 图片下载模块. 从网络下载图片,同时绑定到指定的ImageView控件上.<br>
 * 需要android.permission.INTERNET权限添加到manifest文件中.<br>
 * 其中使用了缓存技术, 提高了性能. <br>
 * Example:<br>
 * <p/>
 * <pre>
 * ImageDownloadModule.getInstance().download(url, imageView);
 * </pre>
 *
 * @author wangyang
 * @since 2014年4月29日
 */
public class ImageDownloadModule {
    private static final String TAG = ImageDownloadModule.class.getSimpleName();
    private static final String DEFAULT_BITMAP_CACHE = "default_bitmap_cache";
    private static final int FADE_IN_TIME = 200;// 图片显示到屏幕上的动画时间
    /**
     * 视频图片
     */
    private static final int LOADING_VIDEO_TYPE = 0;
    /**
     * 音频图片
     */
    private static final int LOADING_AUDIO_TYPE = 1;
    /**
     * 灰图图片
     */
    private static final int LOADING_GRAY_TYPE = 2;
    /**
     * 应用图标图片
     */
    private static final int LOADING_ICON_TYPE = 3;
    // 开辟16M硬缓存空间
    private final int mHardCachedSize = 16 * 1024 * 1024;
    private static Context sContext;
    private static int mScreenHeight = 0;
    private static int mScreenWidth = 0;
    protected boolean mPauseWork = false;
    private static final int LOADING_TYPE = 0;
    private Object mPauseWorkLock = new Object();
    private static final ImageDownloadModule sImageDownloadModule = new ImageDownloadModule();
    private DisplayImageOptions mNormalOptions = new DisplayImageOptions.Builder()
            // .showImageOnLoading(R.drawable.default_image_bg) // 设置图片在下载期间显示的图片
            .showImageForEmptyUri(R.drawable.ic_launcher)// 设置图片Uri为空或是错误的时候显示的图片
            .showImageOnFail(R.drawable.ic_launcher) // 设置图片加载/解码过程中错误时候显示的图片
            .cacheInMemory(true)// 设置下载的图片是否缓存在内存中
            .cacheOnDisk(true)// 设置下载的图片是否缓存在SD卡中
            .considerExifParams(true) // 是否考虑JPEG图像EXIF参数（旋转，翻转）
            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型//
                    // ..decodingOptions(android.graphics.BitmapFactory.Options
                    // decodingOptions)// 设置图片的解码配置
                    // .delayBeforeLoading(int delayInMillis)//int
                    // delayInMillis为你设置的下载前的延迟时间
                    // 设置图片加入缓存前，对bitmap进行设置
                    // .preProcessor(BitmapProcessor preProcessor)
                    // .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
                    // .displayer(new RoundedBitmapDisplayer(20))// 是否设置为圆角，弧度为多少
                    // .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();// 构建完成
    private DisplayImageOptions mDefaultOptions = new DisplayImageOptions.Builder()
            // .showImageOnLoading(R.drawable.default_image_bg) // 设置图片在下载期间显示的图片
            // .showImageForEmptyUri(R.drawable.default_image_bg)//
            // 设置图片Uri为空或是错误的时候显示的图片
            // .showImageOnFail(R.drawable.default_image_bg) //
            // 设置图片加载/解码过程中错误时候显示的图片
            .
                    cacheInMemory(true)// 设置下载的图片是否缓存在内存中
            .
                    cacheOnDisk(true)// 设置下载的图片是否缓存在SD卡中

            .
                    considerExifParams(true) // 是否考虑JPEG图像EXIF参数（旋转，翻转）
            .
                    imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .
                    bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型//
                            // ..decodingOptions(android.graphics.BitmapFactory.Options
                            // decodingOptions)// 设置图片的解码配置
                            // .delayBeforeLoading(int delayInMillis)//int
                            // delayInMillis为你设置的下载前的延迟时间
                            // 设置图片加入缓存前，对bitmap进行设置
                            // .preProcessor(BitmapProcessor preProcessor)
                            // .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
                            // .displayer(new RoundedBitmapDisplayer(20))// 是否设置为圆角，弧度为多少
                            // .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .

                    build();// 构建完成

    /**
     * 获取{@link ImageDownloadModule}对象<br>
     * 单例方法<br>
     * 同步
     *
     * @return {@link ImageDownloadModule}对象
     */
    public static ImageDownloadModule getInstance() {
        return sImageDownloadModule;
    }

    /**
     * 初始化{@link ImageDownloadModule}
     *
     * @param context 当前上下文
     */
    public static void init(Context context) {

        if (context == null) {
            throw new NullPointerException("Context is null");
        }

        sContext = context;

        mScreenHeight = ScreenUtil.getScreenHeightPix(context);
        mScreenWidth = ScreenUtil.getScreenWidthPix(context);
        SystemUtil.init(context);
        // This configuration tuning is custom. You can tune every option, you
        // may tune some of them,
        // or you can create default configuration by
        // ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                        // 50M
                .diskCacheSize(50 * 1024 * 1024)
                        // 缓存的文件数量
                .diskCacheFileCount(200)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                        // .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
        com.nostra13.universalimageloader.utils.L.writeLogs(false);
    }

    /**
     * 图片下载模块构造方法
     */
    private ImageDownloadModule() {

    }

    // Hard cache, with a fixed maximum capacity and a life duration
    final LruCache<String, Bitmap> mHardBitmapCache = new LruCache<String, Bitmap>(
            mHardCachedSize) {
        @Override
        public int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }

        @Override
        protected void entryRemoved(boolean evicted, String key,
                                    Bitmap oldValue, Bitmap newValue) {
            v("entryRemoved", "hard cache is full , push to soft cache");
            if (evicted) {
                oldValue.recycle();
            }
        }

    };

    /**
     * 根据媒体文件路径获取App图片
     *
     * @param packageName 媒体库文件绝对路径
     * @param imageView   显示图片的控件
     * @return void
     * @throws NullPointerException     imageView为null
     * @throws IllegalArgumentException url为空字符串
     */
    public void displayAppIconImage(String packageName, String activityClassPath, int defaultResId, ImageView imageView) {
        v("display", "start url=" + packageName);
        if (imageView == null) {
            e("displayAppIconImage",
                    "imageView is null");
            return;
        }

        if (StringUtil.isEmpty(packageName)) {
            e("displayAppIconImage",
                    "url is empty");
            return;
        }

//        if (StringUtil.isEmpty(activityClassPath)) {
//            e("displayAppIconImage",
//                    "activityClassPath is empty");
//            return;
//        }

        AsyncTaskParam asyncTaskParam = new AsyncTaskParam();
        asyncTaskParam.mLoadingType = LOADING_ICON_TYPE;
        asyncTaskParam.mUrl = packageName;
        asyncTaskParam.mActivityClassPath = activityClassPath;
        asyncTaskParam.mResId = defaultResId;

        download(imageView, asyncTaskParam);
    }

    /**
     * 根据媒体文件路径获取灰色图片
     *
     * @param url       媒体库文件绝对路径
     * @param imageView 显示图片的控件
     * @return void
     * @throws NullPointerException     imageView为null
     * @throws IllegalArgumentException url为空字符串
     */
    public void displayGrayImage(String url, int defaultResId, ImageView imageView) {
        v("display", "start url=" + url);
        if (imageView == null) {
            e("displayGrayImage",
                    "imageView is null");
            return;
        }
        if (url == null) {
            e("displayGrayImage",
                    "url is null");
            return;
        }
        if ("".equals(url)) {
            e("displayGrayImage",
                    "url is empty");
            return;
        }
        AsyncTaskParam asyncTaskParam = new AsyncTaskParam();
        asyncTaskParam.mLoadingType = LOADING_GRAY_TYPE;
        asyncTaskParam.mUrl = url;
        asyncTaskParam.mResId = defaultResId;

        download(imageView, asyncTaskParam);
    }

    /**
     * 根据媒体文件路径获取图片
     *
     * @param url       媒体库文件绝对路径
     * @param imageView 显示图片的控件
     * @return void
     * @throws NullPointerException     imageView为null
     * @throws IllegalArgumentException url为空字符串
     */
    public void displayAudioImage(String url, int defaultResId, ImageView imageView) {
        v("display", "start url=" + url);
        if (imageView == null) {
            e("displayAudioImage",
                    "imageView is null");
            return;
        }
        if (url == null) {
            e("displayAudioImage",
                    "url is null");
            return;
        }
        if ("".equals(url)) {
            e("displayAudioImage",
                    "url is empty");
            return;
        }

        if (!url.startsWith("http://") && !url.startsWith("file://") && !url.startsWith("audio://")) {
            url = "audio://" + url;
        }
        if (url.startsWith("file://")) {
            url = url.replace("file://", "audio://");
        }
        AsyncTaskParam asyncTaskParam = new AsyncTaskParam();
        asyncTaskParam.mLoadingType = LOADING_AUDIO_TYPE;
        asyncTaskParam.mUrl = url;
        asyncTaskParam.mResId = defaultResId;

        download(imageView, asyncTaskParam);
    }

    /**
     * 根据媒体文件路径获取图片
     *
     * @param url       媒体库文件绝对路径
     * @param width     指定图片宽(像素)
     * @param height    指定图片高(像素)
     * @param imageView 显示图片的控件
     * @return void
     * @throws NullPointerException     imageView为null
     * @throws IllegalArgumentException url为空字符串
     */
    public void displayVideoImage(String url, int defaultResId, int width, int height, ImageView imageView) {
        v("displayVideoImage", "start url=" + url);
        if (imageView == null) {
            e("displayVideoImage",
                    "imageView is null");
            return;
        }
        if (url == null) {
            e("displayVideoImage",
                    "url is null");
            return;
        }
        if ("".equals(url)) {
            e("ImageDownloadModule.display(String url, int width, int height, ImageView imageView)",
                    "url is empty");
            return;
        }
        if (width == 0) {
            e("ImageDownloadModule.display(String url, int width, int height, ImageView imageView)",
                    "width is 0");
            return;
        }
        if (height == 0) {
            e("ImageDownloadModule.display(String url, int width, int height, ImageView imageView)",
                    "height is 0");
            return;
        }

        AsyncTaskParam asyncTaskParam = new AsyncTaskParam();
        asyncTaskParam.mLoadingType = LOADING_VIDEO_TYPE;
        asyncTaskParam.mUrl = url;
        asyncTaskParam.mResId = defaultResId;
        asyncTaskParam.mWidth = width;
        asyncTaskParam.mHeight = height;

        download(imageView, asyncTaskParam);
    }

    /**
     * 根据网络URL下载图片.
     *
     * @param url       图片网络URL
     * @param imageView 显示图片的控件
     * @return void
     * @throws NullPointerException     <br>
     *                                  url为null<br>
     *                                  imageView为null
     * @throws IllegalArgumentException url为空字符串
     */
    public void displayImage(String url, ImageView imageView) {

        if (url == null) {
            e("displayImage",
                    "url is null");
            return;
        }
        if (imageView == null) {
            e("displayImage",
                    "imageView is null");
            return;
        }
        if ("".equals(url)) {
            e("displayImage", "url is empty");
            return;
        }
        ImageLoader.getInstance().displayImage(url, imageView, mDefaultOptions);

    }

    /**
     * 根据URL下载图片.
     *
     * @param url       图片URL
     * @param imageView 显示图片的控件
     * @return void
     * @throws NullPointerException     <br>
     *                                  url为null<br>
     *                                  imageView为null
     * @throws IllegalArgumentException url为空字符串
     */
    public void displayImage(String url, ImageView imageView,
                             DisplayImageOptions options) {

        if (url == null) {
            e("displayImage DisplayImageOptions",
                    "url is null");
            return;
        }
        if (imageView == null) {
            e("displayImage DisplayImageOptions",
                    "imageView is null");
            return;
        }
        if ("".equals(url)) {
            e("displayImage DisplayImageOptions",
                    "url is empty");
            return;
        }
        ImageLoader.getInstance().displayImage(url, imageView, options);
    }

    private void download(ImageView imageView, AsyncTaskParam asyncTaskParam) {

        v("download", "start time=" + System.currentTimeMillis() + " url="
                + asyncTaskParam.mUrl);
        imageView.setImageBitmap(null);
        v("RAM", "star url=" + asyncTaskParam.mUrl);
        Bitmap bitmap = TextUtils.isEmpty(asyncTaskParam.mActivityClassPath)? getBitmapFromCache(asyncTaskParam.mUrl) : getBitmapFromCache(asyncTaskParam.mActivityClassPath);
        v("RAM", "end bitmap=" + bitmap + " url=" + asyncTaskParam.mUrl);
        if (bitmap == null) {

            // sd卡和网络
            forceDownload(imageView, asyncTaskParam);

        } else {

            if (imageView != null) {
                cancelPotentialDownload(TextUtils.isEmpty(asyncTaskParam.mActivityClassPath)? asyncTaskParam.mUrl : asyncTaskParam.mActivityClassPath, imageView);
                imageView.setImageBitmap(bitmap);// 设置
                v("download", "setImageBitmap");
            }

        }

    }

    /**
     * get bitmap by local sdcard image
     *
     * @param url
     * @return
     */
    private Bitmap loadFromSDCardPath(String url) {

        // 从普通图片缓存取bitmap
        Bitmap bitmap = ImageSDCache.getInstance().getBitmapBySDCardPath(url,
                mScreenWidth, mScreenHeight);

        if (bitmap != null) {

            // Add to RAM cache
            synchronized (mHardBitmapCache) {
                mHardBitmapCache.put(url, bitmap);
                v("", "");
            }
        }
        return bitmap;
    }

    /**
     * get bitmap by local cache image
     *
     * @param url
     * @return
     */
    private Bitmap loadFromAppCachePath(String url) {

        // 从普通图片缓存取bitmap
        Bitmap bitmap = ImageSDCache.getInstance().getBitmapByCachePath(
                url,
                SystemUtil.getInstance().getIndividualCacheDirectory()
                        .getAbsolutePath(), mScreenWidth, mScreenHeight);

        if (bitmap != null) {

            // Add to RAM cache
            synchronized (mHardBitmapCache) {
                mHardBitmapCache.put(url, bitmap);
                v("", "");
            }
        }
        return bitmap;
    }

    /**
     * Returns true if the current download has been canceled or if there was no
     * download in progress on this image view. Returns false if the download in
     * progress deals with the same url. The download is not stopped in that
     * case.
     */
    private static boolean cancelPotentialDownload(String url,
                                                   ImageView imageView) {

        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            return bitmapDownloaderTask.isFinished();
        }
        return true;
    }

	/*
     * Same as download but the image is always downloaded and the cache is not
	 * used. Kept private at the moment as its interest is not clear. private
	 * void forceDownload(String url, ImageView view) { forceDownload(url, view,
	 * null); }
	 */

    /**
     * Same as download but the image is always downloaded and the cache is not
     * used. Kept private at the moment as its interest is not clear.
     */
    private void forceDownload(ImageView imageView, AsyncTaskParam asyncTaskParam) {

        v("forceDownload", "start");
        try {

            // State sanity: url is guaranteed to never be null in
            if (asyncTaskParam.mUrl == null && imageView != null) {
                // 设置默认图片
                // imageView.setImageBitmap(getDefaultBitmap(mContext));
                e("forceDownload", "url=" + asyncTaskParam.mUrl);
                imageView.setImageDrawable(null);
                return;
            }

            if (cancelPotentialDownload(TextUtils.isEmpty(asyncTaskParam.mActivityClassPath)?asyncTaskParam.mUrl : asyncTaskParam.mActivityClassPath, imageView)) {

                // mBitmapDownloaderTaskCache.remove(url);

                BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);

                DownloadedDrawable downloadedDrawable = new DownloadedDrawable(
                        task, sContext);
                imageView.setImageResource(asyncTaskParam.mResId);

                imageView.setTag(R.id.drawable_task, downloadedDrawable);

                imageView.setTag(R.id.default_image_url, asyncTaskParam.mResId);


                task.execute(asyncTaskParam);

                // 每创建一个线程就加入到缓存中
                // mBitmapDownloaderTaskCache.put(url, task);
            }

        } catch (RejectedExecutionException e) {
            String message = e != null ? e.getMessage() : "";
            v("forceDownload(String url, ImageView imageView, int resId,String... screen)",
                    "RejectedExecutionException=" + message);

        }
    }

    /**
     * The actual AsyncTask that will asynchronously download the image.
     */
    class BitmapDownloaderTask extends AsyncTask<AsyncTaskParam, Void, AsyncTaskParam> {
        private static final int IO_BUFFER_SIZE = 4 * 1024;
        private AsyncTaskParam mAsyncTaskParam;
        private final WeakReference<ImageView> imageViewReference;

        public BitmapDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Actual download method.
         */
        @Override
        protected AsyncTaskParam doInBackground(AsyncTaskParam... params) {

            v("doInBackground", "start");

            // 获得URL
            mAsyncTaskParam = params[0];

            Bitmap bitmap = null;

            // Wait here if work is paused and the task is not cancelled
//            synchronized (mPauseWorkLock) {
//                while (mPauseWork && !isCancelled()) {
//                    try {
//                        mPauseWorkLock.wait();
//                    } catch (InterruptedException e) {
//                    }
//                }
//
//            }

            switch (mAsyncTaskParam.mLoadingType) {
                case LOADING_AUDIO_TYPE:
                    // 去底层截图
                    bitmap = createAlbumThumbnail(mAsyncTaskParam.mUrl);
                    v("doInBackground", "audio url=" + mAsyncTaskParam.mUrl + " bitmap=" + bitmap);
                    mAsyncTaskParam.mBitmap = bitmap;
                    return mAsyncTaskParam;
                case LOADING_VIDEO_TYPE:
                    // 去底层截图
                    int width = mAsyncTaskParam.mWidth;
                    int height = mAsyncTaskParam.mHeight;
                    bitmap = FonePlayer
                            .getThumbnailFromVideo(mAsyncTaskParam.mUrl, -1, 16, width, height);
                    if (bitmap != null) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                                byteArrayOutputStream);
                        BitmapUtil.saveZoomBitmapToSDCard(
                                byteArrayOutputStream, mScreenWidth,
                                mScreenHeight, mAsyncTaskParam.mUrl, SystemUtil.getInstance()
                                        .getIndividualCacheDirectory()
                                        .getAbsolutePath(), true);
                        v("doInBackground", "video url=" + mAsyncTaskParam.mUrl + " bitmap=" + bitmap);
                    } else {

                        e("doInBackground", "video bitmap=null url=" + mAsyncTaskParam.mUrl);

                    }
                    mAsyncTaskParam.mBitmap = bitmap;
                    return mAsyncTaskParam;
                case LOADING_ICON_TYPE:
                    //app图标
                    Drawable drawable = null;

                    try {

                        if (TextUtils.isEmpty(mAsyncTaskParam.mActivityClassPath)){
                            drawable = sContext.getPackageManager().getApplicationIcon(mAsyncTaskParam.mUrl);
                        }else {
                            ComponentName componentName = new ComponentName(mAsyncTaskParam.mUrl, mAsyncTaskParam.mActivityClassPath);
                            drawable = sContext.getPackageManager().getActivityIcon(componentName);
                        }

                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    bitmap = drawableToBitamp(drawable);
                    mAsyncTaskParam.mBitmap = bitmap;
//                    mAsyncTaskParam.mDrawable = drawable;
                    return mAsyncTaskParam;
            }

            // 去SDCard目录取图片
            if (!isCancelled() && getAttachedImageView() != null) {

                bitmap = loadFromSDCardPath(mAsyncTaskParam.mUrl);// SD卡

                if (bitmap != null) {

                    mAsyncTaskParam.mBitmap = bitmap;
                    return mAsyncTaskParam;

                }

            }

            // 去网络端取图片
            try {
                v("Web", "star time=" + System.currentTimeMillis() + "url="
                        + mAsyncTaskParam.mUrl);
                HttpURLConnection httpURLConnection = null;

                URL httpUrl = new URL(mAsyncTaskParam.mUrl);

                httpURLConnection = (HttpURLConnection) httpUrl
                        .openConnection();

                httpURLConnection.setDoInput(true);

                httpURLConnection.connect();

                int statusCode = httpURLConnection.getResponseCode();
                if (statusCode != HttpStatus.SC_OK) {
                    // 连接失败
                    e("doInBackground", "statusCode=" + statusCode
                            + " while retrieving bitmap from " + mAsyncTaskParam.mUrl);
                    return null;
                }

                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = httpURLConnection.getInputStream();
                    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    outputStream = new BufferedOutputStream(
                            byteArrayOutputStream, IO_BUFFER_SIZE);

                    copy(inputStream, outputStream);

                    outputStream.flush();

                    boolean isJpg = mAsyncTaskParam.mUrl.contains("jpg");

                    v("Web", "end time=" + System.currentTimeMillis() + "url="
                            + mAsyncTaskParam.mUrl);

                    bitmap = BitmapUtil.saveZoomBitmapToSDCard(
                            byteArrayOutputStream, mScreenWidth, mScreenHeight,
                            mAsyncTaskParam.mUrl, SystemUtil.getInstance()
                                    .getIndividualCacheDirectory()
                                    .getAbsolutePath(), isJpg);

                    Bitmap grayImg = null;

                    switch (mAsyncTaskParam.mLoadingType) {
                        case LOADING_GRAY_TYPE:
                            //灰图
                            int width = bitmap.getWidth();
                            int height = bitmap.getHeight();
                            try {

                                grayImg = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                                Canvas canvas = new Canvas(grayImg);

                                Paint paint = new Paint();

                                ColorMatrix colorMatrix = new ColorMatrix();

                                colorMatrix.setSaturation(0);

                                ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(colorMatrix);

                                paint.setColorFilter(colorMatrixFilter);

                                canvas.drawBitmap(bitmap, 0, 0, paint);

                                bitmap.recycle();

                            } catch (Exception e) {

                                e.printStackTrace();

                            }
                            break;
                    }

                    mAsyncTaskParam.mBitmap = grayImg != null ? grayImg : bitmap;

                    return mAsyncTaskParam;

                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            } catch (IOException e) {
                e("IOException", "I/O error while retrieving bitmap from "
                        + mAsyncTaskParam.mUrl);
            } catch (IllegalStateException e) {
                e("IllegalStateException", "Incorrect URL: " + mAsyncTaskParam.mUrl);
            } catch (Exception e) {
                e("Exception", "Error while retrieving bitmap from " + mAsyncTaskParam.mUrl);
            } catch (OutOfMemoryError e) {
                e("OutOfMemoryError", "OOM bitmap from " + mAsyncTaskParam.mUrl);
            } finally {

            }

            return null;

        }

        /**
         * Once the image is downloaded, associates it to the imageView
         */
        @Override
        protected void onPostExecute(AsyncTaskParam asyncTaskParam) {
            if (isCancelled()) {
                asyncTaskParam = null;
            }

            // Logger.d("ImageDownloader", "开始保存下载的图片");
            // Add bitmap to cache
            if (asyncTaskParam != null) {
                synchronized (mHardBitmapCache) {
                    if (asyncTaskParam.mBitmap != null) {
                        mHardBitmapCache.put(TextUtils.isEmpty(mAsyncTaskParam.mActivityClassPath) ? mAsyncTaskParam.mUrl : mAsyncTaskParam.mActivityClassPath, asyncTaskParam.mBitmap);
                    }
                }
            }
            // Logger.d("ImageDownloader", "结束保存下载的图片");
            v("onPostExecute", "bitmap=" + asyncTaskParam.mBitmap + " url=" + mAsyncTaskParam.mUrl);
            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                // Change bitmap only if this process is still associated with
                // it
                if (this == bitmapDownloaderTask) {
                    if (asyncTaskParam.mBitmap != null) {
                        // Transition drawable with a transparent drawable and
                        // the
                        // final drawable

//                        final TransitionDrawable td = new TransitionDrawable(
//                                new Drawable[]{
//                                        new ColorDrawable(
//                                                android.R.color.transparent),
//                                        new BitmapDrawable(sContext
//                                                .getResources(),
//                                                bitmap)});
//                        imageView.setImageDrawable(td);
//                        td.startTransition(FADE_IN_TIME);
                        if (asyncTaskParam.mBitmap != null) {
                            imageView.setImageBitmap(asyncTaskParam.mBitmap);
                        }

                    } else {
                        e("onPostExecute", "bitmap=null url=" + mAsyncTaskParam.mUrl);
                    }

                } else {

                    e("onPostExecute", "bitmapDownloaderTask=null url=" + mAsyncTaskParam.mUrl);

                }
            }

            v("download", "end time=" + System.currentTimeMillis()

                    + " url="
                    + mAsyncTaskParam.mUrl);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }

        public void copy(InputStream in, OutputStream out) throws IOException {
            byte[] b = new byte[IO_BUFFER_SIZE];
            int read;
            while ((read = in.read(b)) != -1) {
                out.write(b, 0, read);
            }
        }

        public boolean isFinished() {
            switch (getStatus()) {
                case RUNNING:
                    cancel(true);
                    return true;
                case FINISHED:
                    return true;
            }
            return false;
        }
        /**
         * 判断字符串是否是数字
         */
        public boolean isNumeric(String str) {
            Pattern pattern = Pattern.compile("[0-9]*");
            return pattern.matcher(str).matches();
        }

        /**
         * Returns the ImageView associated with this task as long as the
         * ImageView's task still points to this task as well. Returns null
         * otherwise.
         */
        private ImageView getAttachedImageView() {
            final ImageView imageView = imageViewReference.get();
            final BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

            if (this == bitmapDownloaderTask) {
                return imageView;
            }

            return null;
        }

        private Bitmap createAlbumThumbnail(String filePath) {
            Bitmap bitmap = null;
            try {
                AudioFile audioFile = AudioFileIO.read(new File(filePath));
                Tag tag = audioFile.getTag();
                if (tag != null) {
                    Artwork artwork = tag.getFirstArtwork();
                    if (artwork != null) {
                        byte[] imageData = artwork.getBinaryData();
                        bitmap = BitmapFactory.decodeByteArray(imageData, 0,
                                imageData.length);
                        L.v(TAG, "createAlbumThumbnail", "filePath=" + filePath + " bitmap=" + bitmap);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        private Bitmap drawableToBitamp(Drawable drawable) {
            int w = drawable.getIntrinsicWidth();
            int h = drawable.getIntrinsicHeight();
            Bitmap.Config config =
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                            : Bitmap.Config.RGB_565;
            Bitmap bitmap = Bitmap.createBitmap(w, h, config);
            //注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, w, h);
            drawable.draw(canvas);
            return bitmap;
        }
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active download task (if any) associated
     * with this imageView. null if there is no such task.
     */
    private static BitmapDownloaderTask getBitmapDownloaderTask(
            ImageView imageView) {
        if (imageView != null) {
            Object obj = imageView.getTag(R.id.drawable_task);
            if (obj instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable) obj;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    /**
     * A fake Drawable that will be attached to the imageView while the download
     * is in progress.
     * <p/>
     * <p>
     * Contains a reference to the actual download task, so that a download task
     * can be stopped if a new binding is required, and makes sure that only the
     * last started download process can bind its result, independently of the
     * download finish order.
     * </p>
     */
    class DownloadedDrawable extends ColorDrawable {
        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

        public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask,
                                  Context context) {
            super(Color.TRANSPARENT);
            //super(context.getResources(), getDefaultBitmap(context));//
            // 此位置可以改变图片的背景颜色----王阳
            bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(
                    bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }

    }

    /**
     * @param url The URL of the image that will be retrieved from the cache.
     * @return The cached bitmap or null if it was not found.
     */
    Bitmap getBitmapFromCache(String url) {
        if (url == null || url.length() == 0)
            return null;
        // First try the hard reference cache
        synchronized (mHardBitmapCache) {
            final Bitmap bitmap = mHardBitmapCache.get(url);
            if (bitmap != null) {

                // 把数据更新
                mHardBitmapCache.remove(url);
                mHardBitmapCache.put(url, bitmap);
                v("getBitmapFromCache", "bitmap=" + bitmap + " url=" + url);
                return bitmap;
            } else {
                e("getBitmapFromCache", "bitmap=" + bitmap + " url=" + url);
            }
        }

        return null;
    }

    /**
     * 返回默认的加载图片
     *
     * @param context
     * @return
     */
    Bitmap getDefaultBitmap(Context context) {

        // 创建图片的Bitmap
//        Bitmap defaultBitmap = getBitmapByResId(context, R.drawable.audio_default_bg,
//                DEFAULT_BITMAP_CACHE);
        // Bitmap defaultBitmap = null;

        // 返回默认图片的Bitmap
        // return defaultBitmap;
        return null;
    }

    /**
     * 根据图片的资源ID创建Bitmap
     *
     * @param context
     * @param resId
     * @return
     */
    Bitmap getBitmapByResId(Context context, int resId, String cacheKey) {

        // 从缓存中取默认图片
        Bitmap resBitmap = mHardBitmapCache.get(cacheKey);
        try {
            if (resBitmap == null) {

                // 创建图片的Bitmap
                resBitmap = BitmapFactory.decodeResource(
                        context.getResources(), resId);

                // 再放到缓存中
                mHardBitmapCache.put(cacheKey, resBitmap);

            }

        } catch (OutOfMemoryError e) {
            e("OutOfMemoryError", "getBitmapByResId()");
            System.gc();
        }

        return resBitmap;
    }

    /**
     * 当使用ListView或GridView时,使用
     * {@link android.widget.AbsListView.OnScrollListener}去保持滚动平滑.<br>
     * 例如:SCROLL_STATE_FLING时可以设置setPauseWork (true),暂停下载,<br>
     * SCROLL_STATE_IDLE和SCROLL_STATE_TOUCH_SCROLL时设置setPauseWork
     * (false)恢复下载,提高性能
     */
    public void setPauseWork(boolean isPauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = isPauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    /**
     * 图片下载参数类
     */
    private static class AsyncTaskParam {
        public int mLoadingType;
        public String mUrl;
        public String mActivityClassPath;
        public int mWidth = ScreenUtil.dp2px(140.0f);
        public int mHeight = ScreenUtil.dp2px(80.0f);
        public int mResId;
        public Bitmap mBitmap;
        public Drawable mDrawable;
    }

    public Drawable bitmapParseDrawable(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        return new BitmapDrawable(bitmap);
    }

    /**
     * log
     *
     * @param type
     * @param msg
     * @return void
     */
    private void v(String type, String msg) {
        if (false) {
            L.v(TAG, type, msg);
        }
    }

    /**
     * log
     *
     * @param type
     * @param msg
     * @return void
     */
    private void e(String type, String msg) {
        if (false) {
            L.e(TAG, type, msg);
        }
    }
}
