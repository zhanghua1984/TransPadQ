package com.fone.player;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

/**
 * A new fone_player wrapper
 *
 * @author wcao 2014-04-11
 */
public class FonePlayer {

    private static final String TAG = FonePlayer.class.getSimpleName();

    static {
        System.loadLibrary("ff_mpeg");
        System.loadLibrary("fone_chardet");
        System.loadLibrary("fone_player");
        System.loadLibrary("new_player");
        System.loadLibrary("ff_fnsd");
    }

    private static FonePlayer mInstance;

    private int mCurrentPosition;
    private int mCachePosition;
    private String[] mCharsets = new String[] { null, "gbk", "utf-8", "utf-16" };

    public static final int PLAYER_TYPE_HARDWARE = 0;
    public static final int PLAYER_TYPE_SYSTEM = 1;
    public static final int PLAYER_TYPE_SOFTWARE = 2;

    public static final int FN_PLAYER_MESSAGE_NONE = 0;
    public static final int FN_PLAYER_MESSAGE_OPEN_SUCCESS = 1;
    public static final int FN_PLAYER_MESSAGE_OPEN_FAILED = 2;
    public static final int FN_PLAYER_MESSAGE_PAUSE_RESULT = 3;
    public static final int FN_PLAYER_MESSAGE_BUFFERING_START = 4;
    public static final int FN_PLAYER_MESSAGE_BUFFERING_PERCENT = 5;
    public static final int FN_PLAYER_MESSAGE_READY_TO_PLAY = 6;
    public static final int FN_PLAYER_MESSAGE_SEEK_THUMBNAIL = 7;
    public static final int FN_PLAYER_MESSAGE_END_OF_FILE = 8;
    public static final int FN_PLAYER_MESSAGE_MEDIA_CURRENT_POS = 9;
    public static final int FN_PLAYER_MESSAGE_MEDIA_CACHED_POS = 10;
    public static final int FN_PLAYER_MESSAGE_NOTIFICATION = 11;
    public static final int FN_PLAYER_MESSAGE_DISPLAY_FRAME = 12;
    public static final int FN_PLAYER_CONVERTER_PERCENT = 13;
    public static final int FN_PLAYER_CONVERTER_RETRUE = 14;
    public static final int FN_PLAYER_MESSAGE_HW_START_ERR = 15;
    public static final int FN_PLAYER_MESSAGE_CLOSE_SUCCESS = 16;
    public static final int FN_PLAYER_MESSAGE_AUTHORIZED = 17;
    public static final int FN_PLAYER_MESSAGE_REOPEN_VIDEO = 18;
    public static final int FN_PLAYER_MESSAGE_ENGINE_TYPE = 19;
    public static final int FN_PLAYER_MESSAGE_SHOW_SUBTITLE_TYPE = 20;
    public static final int FN_PLAYER_MESSAGE_SHOW_SUBTITLE = 21;
    public static final int FN_PLAYER_MESSAGE_SHOW_LYRIC = 22;
    public static final int FN_PLAYER_MESSAGE_OPEN_DONE = 23;
    public static final int FN_PLAYER_MESSAGE_END = 24;

    /**
     * Init the player with sdkVersion,screenWidth and screenHeight.
     *
     */
    public native boolean _init();

    /**
     * Open video with real url.
     *
     * @throws IllegalArgumentException
     *             url is null.
     */
    public boolean open(Object object) throws IllegalArgumentException {
        return _open(object);
    }

    private native boolean _open(Object url) throws IllegalArgumentException;

    /**
     * play after playback has been recv ready_to_play.
     *
     * @throws IllegalStateException
     *             if the internal player engine has not been initialized.
     */
    public boolean play() {
        m_playing = true;
        return _play();
    }

    public native boolean _play();

    /**
     * Pauses playback. Call start() to resume.
     *
     * @throws IllegalStateException
     *             if the internal player engine has not been initialized.
     */
    public boolean pause() throws IllegalStateException {
        m_playing = false;
        return _pause();
    }

    public native boolean _pause() throws IllegalStateException;

    /**
     * Stops playback after playback has been stopped.
     *
     * @throws IllegalStateException
     *             if the internal player engine has not been initialized.
     */
    public boolean stop(boolean isByUser) throws IllegalStateException {
        return _stop(isByUser ? 1 : 0);
    }

    public native boolean _stop(int isByUser) throws IllegalStateException;

    /**
     * Seeks to specified time position.
     *
     * @param msec
     *            the offset in milliseconds from the start to seek to
     * @throws IllegalStateException
     *             if the internal player engine has not been initialized
     */
    public boolean seekTo(int msec) throws IllegalStateException {
        return _seekTo(msec);
    }

    public native boolean _seekTo(int msec) throws IllegalStateException;

    /**
     * Get the TagFoneMediaDesc Object. When recv ready_to_play, you can do
     * this.
     *
     * The Object contains some media info, such as:
     * video_height,video_duration.
     */
    public Object getDesc() {
        return _getDesc();
    }

    private native Object _getDesc();

    public native boolean _isPlaying();

    /**
     * Set surface for displaying the video.
     *
     * If set a null surface will only has audio being played.
     */
    public native void _setSurface(Object surface);

    public native boolean _setPlayRate(int rate);

    public native boolean _setPlay3D(int flag);

    public native int _get_audio_channel_count();

    public native String _get_audio_channel_desc(int index);

    public native int _get_cur_audio_channel();

    public native void _get_cur_audio_channel(int index);

    public native void _set_cur_audio_channel(int index);

    public native int _get_sub_channel_count();

    public native String _get_sub_channel_desc(int index);

    public native int _get_cur_sub_channel();

    public native void _set_cur_sub_channel(int index);

    public static native Object _get_thumbnail_from_video(String path, int pos, int bpp, int width, int height);

    public native int _get_player_decoder_role();

    private EventHandler mEventHandler;

    private boolean m_playing = false;

    /**
     * Constructor method to create a FonePlayer.
     *
     * @return a FonePlayer object, or null if creation failed
     */
    public FonePlayer() {
        L.v(TAG, "new FonePlayer");
        mInstance = this;
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }
        _init();
    }

    /**
     * Checks whether the FonePlayer is playing.
     *
     * @return true if currently playing, false otherwise
     */
    public boolean isPlaying() {
        return _isPlaying();
    }

    /**
     * Releases resources associated with this FonePlayer object. It is
     * considered good practice to call this method when you're done using the
     * FonePlayer. In particular, whenever an Activity of an application is
     * paused (its onPause() method is called), or stopped (its onStop() method
     * is called), this method should be invoked to release the FonePlayer
     * object, unless the application has a special need to keep the object
     * around. In addition to unnecessary resources (such as memory and
     * instances of codecs) being held, failure to call this method immediately
     * if a FonePlayer object is no longer needed may also lead to continuous
     * battery consumption for mobile devices, and playback failure for other
     * applications if no multiple instances of the same codec are supported on
     * a device. Even if multiple instances of the same codec are supported,
     * some performance degradation may be expected when unnecessary multiple
     * instances are used at the same time.
     */
    public void release() {
        L.v(TAG, "FonePlayer Release!");
        m_playing = false;
        mOnPreparedListener = null;
        mOnBufferingUpdateListener = null;
        mOnCompletionListener = null;
        mOnErrorListener = null;
        mOnInfoListener = null;
        mInstance = null;
    }

    /**
     * Gets the current playback position.
     *
     * @return the current position in milliseconds
     */
    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public void setCurrentPosition(int mPos) {
        mCurrentPosition = mPos;
    }

    public int getCachePosition() {
        return mCachePosition;
    }

    private class EventHandler extends Handler {
        private FonePlayer mFonePlayer;

        public EventHandler(FonePlayer fp, Looper looper) {
            super(looper);
            mFonePlayer = fp;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case FonePlayer.FN_PLAYER_MESSAGE_SHOW_LYRIC:
                    break;

                case FonePlayer.FN_PLAYER_MESSAGE_SHOW_SUBTITLE_TYPE:
                    L.v("liyang", "SHOW_SUBTITLE_TYPE: ", msg.arg1);
                    break;

                case FonePlayer.FN_PLAYER_MESSAGE_SHOW_SUBTITLE:
                    break;

                case FonePlayer.FN_PLAYER_MESSAGE_ENGINE_TYPE:
                    if (mOnShowEngineTypeListener != null) {
                        mOnShowEngineTypeListener.onEngineType(mFonePlayer, msg.arg1);
                    }
                    break;
                case FonePlayer.FN_PLAYER_MESSAGE_OPEN_SUCCESS:
                    L.v("liyang", "FN_PLAYER_MESSAGE_OPEN_SUCCESS");
                    if (mOnOpenSuccessListener != null) {
                        mOnOpenSuccessListener.onOpenSuccess(mFonePlayer);
                    }
                    break;
                case FonePlayer.FN_PLAYER_MESSAGE_OPEN_FAILED:
                    L.v("liyang", "FN_PLAYER_MESSAGE_OPEN_FAILED");
                    if (mOnOpenFailedListener != null) {
                        mOnOpenFailedListener.onOpenFailed(mFonePlayer, msg.arg1);
                    }
                    break;
                case FonePlayer.FN_PLAYER_MESSAGE_CLOSE_SUCCESS:
                    if (mOnCloseSuccessListener != null) {
                        mOnCloseSuccessListener.onCloseSuccess(mFonePlayer);
                    }
                    break;
                case FonePlayer.FN_PLAYER_MESSAGE_BUFFERING_START:
                    m_playing = false;
                    break;
                case FonePlayer.FN_PLAYER_MESSAGE_BUFFERING_PERCENT:

                    if (mOnOpenPercentListener != null) {
                        L.v("liyang", "FN_PLAYER_MESSAGE_BUFFERING_PERCENT " + msg.arg1);
                        mOnOpenPercentListener.onOpenPercent(mFonePlayer, msg.arg1);
                    }

                    // MediaPlayer's listener?
				/*
				 * else if (mOnBufferingUpdateListener != null) { L.v("liyang",
				 * "FN_PLAYER_MESSAGE_BUFFERING_PERCENT "+msg.arg1);
				 * mOnBufferingUpdateListener.onBufferingUpdate(mFonePlayer,
				 * msg.arg1); }
				 */
                    break;
                case FonePlayer.FN_PLAYER_MESSAGE_READY_TO_PLAY:
                    L.v("FN_PLAYER_MESSAGE_READY_TO_PLAY", "to play");
                    m_playing = true;
                    if (mFonePlayer.mOnPreparedListener != null) {
                        mFonePlayer.mOnPreparedListener.onPrepared(mFonePlayer, _getDesc());
                    }
                    break;
                case FonePlayer.FN_PLAYER_MESSAGE_END_OF_FILE:
                    L.v("FN_PLAYER_MESSAGE_END_OF_FILE", "end_of_file msg.arg1" + msg.arg1);
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mFonePlayer, msg.arg1);
                    }
                    break;
                case FonePlayer.FN_PLAYER_MESSAGE_MEDIA_CURRENT_POS:
                    L.v("liyang", "liyangFN_PLAYER_MESSAGE_MEDIA_CURRENT_POS " + msg.arg1);
                    mCurrentPosition = msg.arg1;
                    break;
                case FonePlayer.FN_PLAYER_MESSAGE_HW_START_ERR:
                    L.v("liyang", "FN_PLAYER_MESSAGE_HW_START_ERR : " + msg.arg1);
                    if (mOnOpenFailedListener != null) {
                        mOnOpenFailedListener.onOpenFailed(mFonePlayer, msg.arg1);
                    }
                    break;
                case FonePlayer.FN_PLAYER_MESSAGE_MEDIA_CACHED_POS:
                    L.v("liyang", "FN_PLAYER_MESSAGE_MEDIA_CACHED_POS " + msg.arg1);
                    mCachePosition = msg.arg1;
                    break;
                case FonePlayer.FN_PLAYER_MESSAGE_OPEN_DONE:
                    L.v("liyang", "FN_PLAYER_MESSAGE_OPEN_DONE : " + msg.arg1);
                    if (mOnOpenDoneListener != null) {
                        mOnOpenDoneListener.onOpenDone(mFonePlayer);
                    }
                    break;

                default:
                    if (mFonePlayer.mOnMsgListener != null) {
                        mFonePlayer.mOnMsgListener.onPlayerMessage(mFonePlayer, msg.what, msg.arg1, msg.arg2);
                    }
                    break;
            }
        }
    }

    public static void onPlayerMessage(int msg, int arg1, int arg2) {

        if (mInstance != null && mInstance.mEventHandler != null) {
            Message m = mInstance.mEventHandler.obtainMessage(msg, arg1, arg2, null);
            mInstance.mEventHandler.sendMessage(m);
        }
    }

    /**
     * Interface definition for a callback to be invoked when an message is
     * availabl.
     */
    public interface OnMessageListener {
        boolean onPlayerMessage(FonePlayer player, int msg, int arg1, int arg2);
    }

    /**
     * Register a callback to be invoked when an message is available.
     *
     * @param listener
     *            the callback that will be run
     */
    public void setOnMessageListener(OnMessageListener listener) {
        mOnMsgListener = listener;
    }

    private OnMessageListener mOnMsgListener;

    public interface OnShowEngineTypeListener {
        void onEngineType(FonePlayer fp, int engineType);
    }

    public void setOnShowEngineTypeListener(OnShowEngineTypeListener listener) {
        mOnShowEngineTypeListener = listener;
    }

    private OnCloseSuccessListener mOnCloseSuccessListener;

    public interface OnCloseSuccessListener {
        void onCloseSuccess(FonePlayer fp);
    }

    public void setOnCloseSuccessListener(OnCloseSuccessListener listener) {

        mOnCloseSuccessListener = listener;
    }

    private OnShowEngineTypeListener mOnShowEngineTypeListener;

    public interface OnOpenSuccessListener {
        void onOpenSuccess(FonePlayer fp);
    }

    public void setOnOpenSuccessListener(OnOpenSuccessListener listener) {

        mOnOpenSuccessListener = listener;
    }

    private OnOpenSuccessListener mOnOpenSuccessListener;

    public interface OnOpenFailedListener {
        void onOpenFailed(FonePlayer fp, int type);
    }

    public void setOnOpenFailedListener(OnOpenFailedListener listener) {
        mOnOpenFailedListener = listener;
    }

    private OnOpenFailedListener mOnOpenFailedListener;

    private OnOpenDoneListener mOnOpenDoneListener;

    public interface OnOpenDoneListener {
        void onOpenDone(FonePlayer fp);
    }

    public void setOnOpenDoneListener(OnOpenDoneListener mOnOpenDoneListener) {

        this.mOnOpenDoneListener = mOnOpenDoneListener;
    }

    /**
     * Interface definition for a callback to be invoked when the media source
     * is ready for playback.
     */

    public interface OnOpenPercentListener {
        void onOpenPercent(FonePlayer fp, int percent);
    }

    public void setOnOpenPercentListener(OnOpenPercentListener listener) {

        mOnOpenPercentListener = listener;

    }

    private OnOpenPercentListener mOnOpenPercentListener;

    public interface OnPreparedListener {
        /**
         * Called when the media file is ready for playback.
         *
         * @param <T>
         *
         * @param fp
         *            the FonePlayer that is ready for playback
         */
        <T> void onPrepared(FonePlayer fp, T mediaInfoObj);
    }

    /**
     * Register a callback to be invoked when the media source is ready for
     * playback.
     *
     * @param listener
     *            the callback that will be run
     */
    public void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    private OnPreparedListener mOnPreparedListener;

    /**
     * Interface definition for a callback to be invoked when playback of a
     * media source has completed.
     */
    public interface OnCompletionListener {
        /**
         * Called when the end of a media source is reached during playback.
         *
         * @param fp
         *            the FonePlayer that reached the end of the file
         * @param type
         *            1:videoplater completion 0:one frag play completion
         */
        void onCompletion(FonePlayer fp, int type);
    }

    /**
     * Register a callback to be invoked when the end of a media source has been
     * reached during playback.
     *
     * @param listener
     *            the callback that will be run
     */
    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    private OnCompletionListener mOnCompletionListener;

    /**
     * Interface definition of a callback to be invoked indicating buffering
     * status of a media resource being streamed over the network.
     */
    public interface OnBufferingUpdateListener {
        /**
         * Called to update status in buffering a media stream received through
         * progressive HTTP download. The received buffering percentage
         * indicates how much of the content has been buffered or played. For
         * example a buffering update of 80 percent when half the content has
         * already been played indicates that the next 30 percent of the content
         * to play has been buffered.
         *
         * @param fp
         *            the FonePlayer the update pertains to
         * @param percent
         *            the percentage (0-100) of the content that has been
         *            buffered or played thus far
         */
        void onBufferingUpdate(FonePlayer fp, int percent);
    }

    /**
     * Register a callback to be invoked when the status of a network stream's
     * buffer has changed.
     *
     * @param listener
     *            the callback that will be run.
     */
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        mOnBufferingUpdateListener = listener;
    }

    private OnBufferingUpdateListener mOnBufferingUpdateListener;

    /**
     * Interface definition of a callback to be invoked when there has been an
     * error during an asynchronous operation (other errors will throw
     * exceptions at method call time).
     */
    public interface OnErrorListener {
        /**
         * Called to indicate an error.
         *
         * @param fp
         *            the FonePlayer the error pertains to
         * @param what
         *            the type of error that has occurred:
         * @param extra
         *            an extra code, specific to the error. Typically
         *            implementation dependent.
         * @return True if the method handled the error, false if it didn't.
         *         Returning false, or not having an OnErrorListener at all,
         *         will cause the OnCompletionListener to be called.
         */
        boolean onError(FonePlayer fp, int what, int extra);
    }

    /**
     * Register a callback to be invoked when an error has happened during an
     * asynchronous operation.
     *
     * @param listener
     *            the callback that will be run
     */
    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    private OnErrorListener mOnErrorListener;

    /**
     * Interface definition of a callback to be invoked to communicate some info
     * and/or warning about the media or its playback.
     */
    public interface OnInfoListener {
        /**
         * Called to indicate an info or a warning.
         *
         * @param fp
         *            the FonePlayer the info pertains to.
         * @param what
         *            the type of info or warning.
         * @param extra
         *            an extra code, specific to the info. Typically
         *            implementation dependent.
         * @return True if the method handled the info, false if it didn't.
         *         Returning false, or not having an OnErrorListener at all,
         *         will cause the info to be discarded.
         */
        boolean onInfo(FonePlayer fp, int what, int extra);
    }

    /**
     * Register a callback to be invoked when an info/warning is available.
     *
     * @param listener
     *            the callback that will be run
     */
    public void setOnInfoListener(OnInfoListener listener) {
        mOnInfoListener = listener;
    }

    private OnInfoListener mOnInfoListener;

    public static void Uim_Player_Message(int type, int percentage, int arg2) {
        // Message message = myHandler.obtainMessage();
        // message.what = type;
        // message.arg1 = percentage;
        // message.arg2 = arg2;
        // myHandler.sendMessage(message);
    }

    public static void fone_media_set_subtitle(String _subtitleStr) {
        L.v("liyang", "_subtitleStr" + _subtitleStr);
        if (mInstance != null) {
            if (mInstance.onNewSubtitleListener != null) {
                mInstance.onNewSubtitleListener.onNewSubtitle(_subtitleStr);
            }
        }
    }

    /**
     * Interface definition for a callback to be invoked when has new subtitle
     */
    public interface OnNewSubtitleListener {
        void onNewSubtitle(String subtitle);
    }

    private OnNewSubtitleListener onNewSubtitleListener;

    public void setOnNewSubtitleListener(OnNewSubtitleListener onNewSubtitleListener) {
        this.onNewSubtitleListener = onNewSubtitleListener;
    }

    /**
     * 获取视频截图
     *
     * @param path   视频地址
     * @param pos    位置？
     * @param bpp    质量
     * @param width  宽度
     * @param height 高度
     * @return 位图
     */
    public static Bitmap getThumbnailFromVideo(String path, int pos, int bpp, int width, int height) {
        TagFoneBitMap tfb = ((TagFoneBitMap) _get_thumbnail_from_video(path, pos, bpp, width, height));
        Bitmap bitmap = null;
        if (tfb != null) {
            bitmap = Bitmap.createBitmap((Bitmap) tfb.m_bitmap);
        }
        return bitmap;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    private static int screenHeight = 800;
    private static int screenWidth = 480;

    private static Application applicationContext;

    public static void init(Application application){
        applicationContext = application;
        DisplayMetrics metrics = application.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        if (width > height){
            screenWidth = height;
            screenHeight = width;
        }else {
            screenWidth = width;
            screenHeight = height;
        }
    }

    public static int getNetWorkType() {
        if (applicationContext != null) {
            final ConnectivityManager connectivityManager = (ConnectivityManager) applicationContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();// 获取网络的连接情况
            if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
                // 注意一：
                // NetworkInfo 为空或者不可以用的时候正常情况应该是当前没有可用网络，
                // 但是有些电信机器，仍可以正常联网，
                // 所以当成net网络处理依然尝试连接网络。
                // （然后在socket中捕捉异常，进行二次判断与用户提示）。
                L.v("getNetType", "netType=MSG_NO_NETWORK_TYPE");
                return 0;
            } else if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

                // 先判断手机网络,在判断是否是wifi,否则判断会出现错乱(考虑wifi和手机网络同时存在的情况)
                TelephonyManager mTelephonyManager = (TelephonyManager) applicationContext
                        .getSystemService(Context.TELEPHONY_SERVICE);
                int type = mTelephonyManager.getNetworkType();
                switch (type) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        L.v("getNetType", "mobile netType=MSG_2G_NETWORK_TYPE");
                        return 2;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        // 判断3g网
                        L.v("getNetType", "mobile netType=MSG_3G_NETWORK_TYPE");
                        return 3;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        // 判断4g网
                        L.v("getNetType", "mobile netType=MSG_4G_NETWORK_TYPE");
                        return 4;
                    default:
                        // 未知
                        L.v("getNetType", "mobile netType=MSG_UNKOWN_NETWORK_TYPE");
                        return 0;
                }

            } else if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                // 判断WIFI网
                L.v("getNetType", "netType=MSG_WIFI_NETWORK_TYPE");
                return 1;
            }
            L.v(TAG, "getNetType",
                    "non wifi and non mobile,netType=MSG_UNKOWN_NETWORK_TYPE");
            return 0;
        } else {
            return 0;
        }
    }

    private static int hwPlusSupport = 0;
    private static int systemDecoderSupport = 0;

    public static int getHwPlusSupport() {
        return  hwPlusSupport;
    }

    public static int getSystemDecoderSupport() {
        return systemDecoderSupport;
    }

    public static void setHwPlusSupport(int support) {
        hwPlusSupport = support;
    }

    public static void setSystemDecoderSupport(int support) {
        systemDecoderSupport = support;
    }

}
