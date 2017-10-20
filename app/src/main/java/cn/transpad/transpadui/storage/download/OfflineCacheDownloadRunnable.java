package cn.transpad.transpadui.storage.download;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.http.Reporter;
import cn.transpad.transpadui.storage.OfflineCacheDataBaseAdapter;
import cn.transpad.transpadui.storage.OfflineCacheModule;
import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;

/**
 * 离线缓存下载
 *
 * @author wangyang
 * @since 2014-8-12
 */
public class OfflineCacheDownloadRunnable implements Runnable {
    private static final String TAG = "OfflineCacheDownloadRunnable";
    /**
     * 碎片重试次数
     */
    private static final int FRAGMENT_MAX_TIME = 5;
    /**
     * 视频重试次数
     */
    private static final int FILE_MAX_TIME = 5;
    /**
     * 任务运行中
     */
    public static final int STATE_RUNNING = 1;
    /**
     * 任务已停止且发送消息
     */
    public static final int STATE_STOP_SEND = 2;
    /**
     * 任务已停止但不发送消息
     */
    public static final int STATE_STOP_UNSEND = 3;
    /**
     * 任务完成
     */
    public static final int STATE_FINISH = 4;

    // 任务状态
    private int mRunnableState = STATE_STOP_SEND;
    // 操作类型
    private int mOperateType = OfflineCache.OPERATE_STOP;
    // 当前分片逻辑重试次数
    private int mCurrentFragmentLogicRetryTime = 0;
    // 当前分片文件长度重试次数
    private int mCurrentFragmentContentLengthRetryTime = 0;

    private OfflineCache mOfflineCache = null;
    private Handler mHandler = null;
    private long mStartTime = System.currentTimeMillis();
    private HttpURLConnection mHttpURLConnection;
    private int mResponseCode = 0;
    private int mContentLength = 0;

    /**
     * 设置任务执行方式
     *
     * @param operateType 执行类型
     */
    public void setOperateType(int operateType) {
        L.v(TAG, "setOperateType", "start operateType=" + operateType);
        mOperateType = operateType;

        Message msg = new Message();
        Bundle bundle = new Bundle();
        switch (operateType) {
            case OfflineCache.OPERATE_STOP:
                mRunnableState = STATE_STOP_SEND;
                mOfflineCache.setCacheDownloadState(OfflineCache.CACHE_STATE_PAUSE);
                bundle.putParcelable(OfflineCache.OFFLINE_CACHE, mOfflineCache);
                msg.setData(bundle);
                msg.what = StorageModule.MSG_DOWNLOAD_SUCCESS;
                mHandler.sendMessage(msg);
                break;
            case OfflineCache.OPERATE_STOP_BATCH_PAUSE_STATE:
                mRunnableState = STATE_STOP_SEND;
                mOfflineCache.setCacheDownloadState(OfflineCache.CACHE_STATE_PAUSE);
                mOfflineCache
                        .setCacheDownloadStateModel(OfflineCache.CACHE_STATE_MODEL_AUTO);
                bundle.putParcelable(OfflineCache.OFFLINE_CACHE, mOfflineCache);
                msg.setData(bundle);
                msg.what = StorageModule.MSG_DOWNLOAD_SUCCESS;
                mHandler.sendMessage(msg);

                break;
            case OfflineCache.OPERATE_STOP_BATCH_WAIT_STATE:
                mRunnableState = STATE_STOP_SEND;
                mOfflineCache
                        .setCacheDownloadState(OfflineCache.CACHE_STATE_WAITING);
                bundle.putParcelable(OfflineCache.OFFLINE_CACHE, mOfflineCache);
                msg.setData(bundle);
                msg.what = StorageModule.MSG_DOWNLOAD_SUCCESS;
                mHandler.sendMessage(msg);

                break;
            case OfflineCache.OPERATE_STOP_BATCH_UNCHANGE_STATE:
                mRunnableState = STATE_STOP_UNSEND;
                break;
            case OfflineCache.OPERATE_STOP_BATCH_ERROR_STATE:
                mRunnableState = STATE_STOP_SEND;
                break;
            case OfflineCache.OPERATE_DELETE:
                mRunnableState = STATE_STOP_UNSEND;
                break;
            case OfflineCache.OPERATE_RUNNING:
                mRunnableState = STATE_RUNNING;
                mOfflineCache
                        .setCacheDownloadState(OfflineCache.CACHE_STATE_DOWNLOADING);
                mOfflineCache.setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_SUCCESS);
                bundle.putParcelable(OfflineCache.OFFLINE_CACHE, mOfflineCache);
                msg.setData(bundle);
                msg.what = StorageModule.MSG_DOWNLOAD_SUCCESS;
                mHandler.sendMessage(msg);
                L.v(TAG, "setOperateType", "CACHE_STATE_DOWNLOADING percentNum="
                        + mOfflineCache.getCachePercentNumString());
                break;
            default:
                break;
        }

    }

    /**
     * 获取任务状态
     */
    public int getRunnableState() {

        return mRunnableState;
    }

    public void setOfflineCache(OfflineCache offlineCache) {
        mOfflineCache = offlineCache;
    }

    public OfflineCache getOfflineCache() {
        return mOfflineCache;
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void run() {

        // 任务运行
        mRunnableState = STATE_RUNNING;
        L.v(TAG,
                "run",
                "mRunnableState=STATE_RUNNING name="
                        + mOfflineCache.getCacheName() + " errorCode="
                        + mOfflineCache.getCacheErrorCode() + " downloadState="
                        + mOfflineCache.getCacheDownloadState());

        // 下载视频
        limitSpeedDownloadFile();

        // 任务停止
        mRunnableState = STATE_STOP_UNSEND;

        L.v(TAG,
                "run",
                "mRunnableState=STATE_STOP_UNSEND name="
                        + mOfflineCache.getCacheName()
                        + " errorCode="
                        + mOfflineCache.getCacheErrorCode() + " downloadState="
                        + mOfflineCache.getCacheDownloadState());

    }

    /**
     * 限速下载文件
     */
    public void limitSpeedDownloadFile() {

        L.v(TAG, "limitSpeedDownloadFile", "start");

        mCurrentFragmentLogicRetryTime = 0;
        mCurrentFragmentContentLengthRetryTime = 0;

        mOfflineCache
                .setCacheDownloadState(OfflineCache.CACHE_STATE_DOWNLOADING);
        // 更新数据库
        OfflineCacheDataBaseAdapter.getInstance().updateOfflineCacheFile(
                mOfflineCache);

        L.v(TAG,
                "limitSpeedDownloadFile",
                "send downloadState=CACHE_STATE_DOWNLOADING name="
                        + mOfflineCache.getCacheName()
        );
        // 发送通知
        Message msg = new Message();
        Bundle bundle = new Bundle();
//        bundle.putParcelable(OfflineCache.OFFLINE_CACHE, mOfflineCache);
//        msg.setData(bundle);
//        msg.what = StorageModule.MSG_DOWNLOAD_PROGRESS;
        OfflineCacheModule.getInstance().mergeOfflineCache(mOfflineCache);

        switch (mOfflineCache.getCacheDownloadState()) {
            case OfflineCache.CACHE_STATE_ERROR:
                // 解析出错
                L.e(TAG, "limitSpeedDownloadFile", "CACHE_STATE_ERROR errorCode="
                        + mOfflineCache.getCacheErrorCode());
                mOfflineCache.setCacheSpeed(0);

                // 更新数据库
                OfflineCacheDataBaseAdapter.getInstance()
                        .updateOfflineCacheFile(mOfflineCache);

                // 更新UI
                msg = new Message();
                bundle = new Bundle();
                bundle.putParcelable(OfflineCache.OFFLINE_CACHE, mOfflineCache);
                msg.setData(bundle);
                msg.what = StorageModule.MSG_DOWNLOAD_ERROR;
                mHandler.sendMessage(msg);

                break;
            default:

                L.v(TAG, "limitSpeedDownloadFile",
                        "name="
                                + mOfflineCache.getCacheName());

                mStartTime = System.currentTimeMillis();

                // 下载分片
                limitSpeedDownloadFragmentList(0);

                long currentTime = System.currentTimeMillis() - mStartTime;

                long lastTime = SharedPreferenceModule.getInstance().getLong(mOfflineCache.getCacheID() + "");

                SharedPreferenceModule.getInstance().setLong(mOfflineCache.getCacheID() + "", lastTime + currentTime);

                // 判断任务状态
                switch (mRunnableState) {
                    case STATE_FINISH:

                        // 计算真实的总大小
                        long actFileTotalSize = mOfflineCache.getCacheAlreadySize();

                        mOfflineCache.setCacheTotalSize(actFileTotalSize);
                        mOfflineCache.setCachePercentNum(100);
                        mOfflineCache
                                .setCacheDownloadState(OfflineCache.CACHE_STATE_FINISH);
                        mOfflineCache
                                .setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_SUCCESS);
                        L.v(TAG,
                                "limitSpeedDownloadFile",
                                "send name=" + mOfflineCache.getCacheName()
                                        + " mRunnableState=STATE_FINISH downloadState="
                                        + mOfflineCache.getCacheDownloadState()
                                        + " totalPercentNum=" + mOfflineCache.getCachePercentNum()
                        );

                        L.writeDownloadTimeLog(mOfflineCache);

                        msg = new Message();
                        bundle = new Bundle();
                        bundle.putParcelable(OfflineCache.OFFLINE_CACHE, mOfflineCache);
                        msg.setData(bundle);
                        msg.what = StorageModule.MSG_DOWNLOAD_SUCCESS;
                        mHandler.sendMessage(msg);
                        break;
                    case STATE_STOP_SEND:

                        L.v(TAG,
                                "limitSpeedDownloadFile",
                                "send name="
                                        + mOfflineCache.getCacheName()
                                        + " mRunnableState=STATE_STOP_SEND downloadState="
                                        + mOfflineCache.getCacheDownloadState());
                        mOfflineCache.setCacheSpeed(0);
                        msg = new Message();
                        bundle = new Bundle();
                        bundle.putParcelable(OfflineCache.OFFLINE_CACHE, mOfflineCache);
                        msg.setData(bundle);
                        msg.what = StorageModule.MSG_DOWNLOAD_SUCCESS;
                        mHandler.sendMessage(msg);
                        break;
                    case STATE_STOP_UNSEND:
                        L.v(TAG,
                                "limitSpeedDownloadFile",
                                "unsend name="
                                        + mOfflineCache.getCacheName()
                                        + " mRunnableState=STATE_STOP_UNSEND downloadState="
                                        + mOfflineCache.getCacheDownloadState());
                        break;
                    default:

                        break;
                }

                // 下载结束(可能失败,可能成功),单个更新数据库,批量更新放在外面做
                OfflineCacheDataBaseAdapter.getInstance()
                        .updateOfflineCacheFile(mOfflineCache);

                mHttpURLConnection = null;

                break;
        }

    }

    /**
     * 限速下载分片集合
     *
     * @param speed 每秒允许下载的字节数(0代表不限速)
     */
    public void limitSpeedDownloadFragmentList(long speed) {

        if (mOfflineCache == null) {
            L.e(TAG, "limitSpeedDownloadFragmentList",
                    "mOfflineCache=null");
            return;
        }
        L.v(TAG, "limitSpeedDownloadFragmentList", "start storagePath="
                + mOfflineCache.getCacheStoragePath());

        int errorCode;
        switch (mOfflineCache.getCacheDownloadState()) {
            case OfflineCache.CACHE_STATE_FINISH:
                L.v(TAG,
                        "limitSpeedDownloadFragmentList",
                        "CACHE_STATE_FINISH storagePath="
                                + mOfflineCache.getCacheStoragePath() + " url="
                                + mOfflineCache.getCacheDetailUrl());

                errorCode = OfflineCache.CACHE_ERROR_CODE_NEXT;
                break;
            default:
                // 读取流信息
                errorCode = limitSpeedDownloadFragment(speed);
                break;
        }

        L.v(TAG, "limitSpeedDownloadFragmentList", "errorCode=" + errorCode
                + " url=" + mOfflineCache.getCacheDetailUrl());

        // 设置状态码
        mOfflineCache.setCacheErrorCode(errorCode);

        // 判断下载状态
        judgeResult(errorCode);
    }

    /**
     * 限速下载分片文件
     *
     * @param speed 每秒允许下载的字节数(0代表不限速)
     */
    private int limitSpeedDownloadFragment(long speed) {
        L.v(TAG,
                "limitSpeedDownloadFragment",
                "start name=" + mOfflineCache.getCacheName()
                        + " url="
                        + mOfflineCache.getCacheDetailUrl());
        int errorCode = OfflineCache.CACHE_ERROR_CODE_SUCCESS;
        Message msg;
        Bundle bundle;
        //判断是否有无效的文件
        OfflineCache tempOfflineCache = OfflineCacheDataBaseAdapter.getInstance().getOfflineCacheById(mOfflineCache.getCacheID());

        if (tempOfflineCache == null) {
            L.v(TAG, "limitSpeedDownloadFragment", "tempOfflineCache=null");
            File file = new File(
                    mOfflineCache.getCacheStoragePath());
            if (file.exists()) {
                file.delete();
                L.v(TAG, "limitSpeedDownloadFragment", "delete old file path="
                        + mOfflineCache.getCacheStoragePath());
            }
        } else {
            L.v(TAG, "limitSpeedDownloadFragment", "id=" + mOfflineCache.getCacheID() + " alreadySize=" + tempOfflineCache.getCacheAlreadySize() + " totalSize=" + tempOfflineCache.getCacheTotalSize());
            if (mOfflineCache.getCacheTotalSize() == 0) {
                mOfflineCache.setCacheTotalSize(tempOfflineCache.getCacheTotalSize());
            }
        }


        // 请求真实文件
        File file = new File(
                mOfflineCache.getCacheStoragePath());

        if (!file.exists()) {

            L.v(TAG, "limitSpeedDownloadFragment", "create new file path="
                    + mOfflineCache.getCacheStoragePath());

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                L.e(TAG, "limitSpeedDownloadFragment", "e=" + e, e);
            }

            // 文件被删除,更新进度为0
            mOfflineCache.setCacheAlreadySize(0);
            SharedPreferenceModule.getInstance().setLong(mOfflineCache.getCacheID() + "", 0);

        } else {

            switch (mOfflineCache.getCacheDownloadState()) {
                case OfflineCache.CACHE_STATE_FINISH:

                    // 标记分片已下载完成,进行下一个下载
                    L.v(TAG, "limitSpeedDownloadFragment",
                            "downloadState=CACHE_STATE_FINISH errorCode=CACHE_ERROR_CODE_NEXT");

                    errorCode = OfflineCache.CACHE_ERROR_CODE_NEXT;

                    break;
                default:

                    L.v(TAG, "limitSpeedDownloadFragment", "file.length="
                            + file.length());

                    // 获得开始位置,继续下载
                    mOfflineCache.setCacheAlreadySize(file.length());
                    break;
            }

        }

        RandomAccessFile randomAccessFile = null;
        InputStream inputStream = null;

        try {

            if (errorCode == OfflineCache.CACHE_ERROR_CODE_NEXT) {

                L.v(TAG, "limitSpeedDownloadFragment",
                        "errorCode=CACHE_ERROR_CODE_NEXT");

            } else {

                mResponseCode = 0;

                //获取响应流
                getHttpResponse();

                //数据校验
                boolean isValidate = isValidateHttpReponse();
                if (!isValidate) {

                    errorCode = mOfflineCache
                            .getCacheErrorCode();

                    // 数据无效
                    L.e(TAG,
                            "limitSpeedDownloadFragment",
                            "isValidate="
                                    + isValidate
                                    + " errorCode="
                                    + mOfflineCache
                                    .getCacheErrorCode()
                                    + " fragmentUrl="
                                    + mOfflineCache
                                    .getCacheDetailUrl());

                } else {

                    errorCode = mOfflineCache
                            .getCacheErrorCode();

                    L.v(TAG, "limitSpeedDownloadFragment", "isValidate="
                            + isValidate + " errorCode=" + errorCode
                            + " response HttpEntity");

                    // 请求文件大小
                    long contentLength = mHttpURLConnection.getContentLength();
                    if (contentLength > 208) {
                        L.v(TAG, "limitSpeedDownloadFragment", "file.length=" + file.length() + " contentLength=" + contentLength);
                        //防止超过100%
                        if (file.length() >= mOfflineCache.getCacheTotalSize()) {
                            if (file.exists()) {
                                L.v(TAG, "limitSpeedDownloadFragment", "deleteFile path=" + file.getAbsolutePath());
                                file.delete();
                                SharedPreferenceModule.getInstance().setLong(mOfflineCache.getCacheID() + "", 0);
                            }
                            L.v(TAG, "limitSpeedDownloadFragment", "newFile path=" + file.getAbsolutePath());
                            file = new File(
                                    mOfflineCache.getCacheStoragePath());
                            file.createNewFile();
                            mOfflineCache.setCacheAlreadySize(0);
                        }
                        // 第一次下载记录总大小
                        if (file.length() == 0 && mOfflineCache.getCacheTotalSize() == 0) {

                            mOfflineCache
                                    .setCacheTotalSize(contentLength);

                            //保存总进度
                            OfflineCacheDataBaseAdapter.getInstance().updateOfflineCacheFile(mOfflineCache);

                            L.v(TAG, "limitSpeedDownloadFragment", "totalSize=" + mOfflineCache.getCacheTotalSize());
                        }

                    } else {

                        errorCode = OfflineCache.CACHE_ERROR_CODE_CONTENT_LENGTH_EXCEPTION;
                        L.e(TAG,
                                "limitSpeedDownloadFragment",
                                "name="
                                        + mOfflineCache.getCacheName()
                                        + " errorCode="
                                        + errorCode
                                        + " fragmentLength="
                                        + contentLength
                                        + " content="
                                        + mHttpURLConnection.getContent().toString()
                                        + " url="
                                        + mOfflineCache
                                        .getCacheDetailUrl());
                        return errorCode;

                    }

                    L.v(TAG,
                            "limitSpeedDownloadFragment",
                            "name="
                                    + mOfflineCache.getCacheName()
                                    + " fragmentLength="
                                    + contentLength
                                    + " storagePath="
                                    + mOfflineCache
                                    .getCacheStoragePath()
                                    + " url="
                                    + mOfflineCache
                                    .getCacheDetailUrl());

                    randomAccessFile = new RandomAccessFile(file, "rws");

                    randomAccessFile.seek(mOfflineCache
                            .getCacheAlreadySize());
                    // 开始读写数据
                    inputStream =
                            mHttpURLConnection.getInputStream();
                    int pack = 10240; // 10K bytes
                    speed = speed * 1024;// 将速度换算成字节
                    // int sleep = 200; //每秒5次 即5*10K bytes每秒
                    int sleep = 0;
                    if (speed != 0) {
                        sleep = (int) Math.floor(1000 * pack / speed) + 1;
                        L.v(TAG, "limitSpeedDownloadFragment", "speed=" + speed
                                + " sleep=" + sleep + " fragmentUrl="
                                + mOfflineCache.getCacheDetailUrl());
                    }
                    // int maxCount = (int) Math
                    // .floor((length - offlineCacheFragment
                    // .getCacheAlreadySize()) / pack) + 1;
                    // L.v(TAG,
                    // "limitSpeedDownloadFragment",
                    // "maxCount="
                    // + maxCount
                    // + " fragmentUrl="
                    // + offlineCacheFragment
                    // .getCacheFragmentUrl());
                    byte[] buf = new byte[1024 * 80];
                    int currentLength;
                    long startTime = System.currentTimeMillis(); // 开始下载时获取开始时间
                    //每秒发送的消息数量
                    int totalMessageCount = 20;
                    int currentMessageCount = 0;
                    int speedLength = 0;
                    L.v(TAG, "limitSpeedDownloadFragment", "bufferedInputStream read");
                    while ((currentLength = inputStream.read(buf)) != -1) {
                        long currentTime = System.currentTimeMillis();
                        // 判断操作
                        switch (mOperateType) {
                            case OfflineCache.OPERATE_STOP:
                            case OfflineCache.OPERATE_STOP_BATCH_PAUSE_STATE:
                            case OfflineCache.OPERATE_STOP_BATCH_UNCHANGE_STATE:
                            case OfflineCache.OPERATE_STOP_BATCH_WAIT_STATE:
                            case OfflineCache.OPERATE_DELETE:
                                // 手动停止任务
                                if (mHttpURLConnection != null) {
                                    mHttpURLConnection.disconnect();
                                }

                                errorCode = OfflineCache.CACHE_ERROR_CODE_USER_OPERATE;
                                L.v(TAG,
                                        "limitSpeedDownloadFragment",
                                        "name="
                                                + mOfflineCache.getCacheName()
                                                + " aborted errorCode=CACHE_ERROR_CODE_USER_OPERATE");
                                break;
                            default:
                                // 正常下载
                                if (file.exists() && file.canWrite()) {

                                    randomAccessFile.write(buf, 0, currentLength);
                                    mOfflineCache
                                            .setCacheDownloadState(OfflineCache.CACHE_STATE_DOWNLOADING);
                                    // 计算当前进度百分比并累加进度
                                    mOfflineCache.setCacheCurrentSize(currentLength);
                                    mOfflineCache.addCacheAlreadySize(currentLength);

                                    if (currentTime >= (startTime + 1000)) {
                                        // 下载速度
                                        int downloadSpeed = speedLength / 1024;
                                        mOfflineCache.setCacheSpeed(downloadSpeed);
                                        startTime = System.currentTimeMillis();
                                        speedLength = 0;
                                        currentMessageCount = 0;
                                    } else {
                                        speedLength += currentLength;
                                        if (currentMessageCount < totalMessageCount) {
                                            sendDownloadingMessage();
                                            currentMessageCount++;
                                        }
                                    }


                                    if (sleep != 0) {
                                        try {

                                            Thread.sleep(sleep);
                                            L.v(TAG,
                                                    "limitSpeedDownloadFragment",
                                                    "sleep="
                                                            + sleep
                                                            + " fragmentUrl="
                                                            + mOfflineCache
                                                            .getCacheDetailUrl());

                                        } catch (InterruptedException e) {
                                            if (e != null) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                } else {

                                    L.e(TAG,
                                            "limitSpeedDownloadFragment",
                                            "name="
                                                    + mOfflineCache.getCacheName()
                                                    + " CACHE_ERROR_CODE_NOT_WRITE fragmentUrl="
                                                    + mOfflineCache
                                                    .getCacheDetailUrl());
                                    mOfflineCache
                                            .setCacheDownloadState(OfflineCache.CACHE_STATE_ERROR);

                                    // 不能写数据,io异常(usb存储模式)
                                    errorCode = OfflineCache.CACHE_ERROR_CODE_NOT_WRITE;
                                    mRunnableState = STATE_STOP_SEND;
                                }
                        }
                        // 判断下载期间是否有外部操作干预
                        if (errorCode != OfflineCache.CACHE_ERROR_CODE_SUCCESS) {
                            L.e(TAG,
                                    "limitSpeedDownloadFragment",
                                    "name="
                                            + mOfflineCache.getCacheName()
                                            + " stop");
                            break;
                        }

                    }

                    // 判断操作
                    switch (mOperateType) {
                        case OfflineCache.OPERATE_STOP:
                        case OfflineCache.OPERATE_STOP_BATCH_PAUSE_STATE:
                        case OfflineCache.OPERATE_STOP_BATCH_UNCHANGE_STATE:
                        case OfflineCache.OPERATE_STOP_BATCH_WAIT_STATE:
                        case OfflineCache.OPERATE_DELETE:
                            // 手动停止任务
                            errorCode = OfflineCache.CACHE_ERROR_CODE_USER_OPERATE;
                            mRunnableState = STATE_STOP_SEND;
                            L.v(TAG,
                                    "limitSpeedDownloadFragment",
                                    "name="
                                            + mOfflineCache.getCacheName()
                                            + " errorCode=CACHE_ERROR_CODE_USER_OPERATE");
                            break;
                        default:

                            switch (errorCode) {
                                case OfflineCache.CACHE_ERROR_CODE_SUCCESS:
                                    // 判断已下载的大小是否与总大小相同
                                    if (mOfflineCache.getCacheAlreadySize() != mOfflineCache
                                            .getCacheTotalSize()) {
                                        L.w(TAG,
                                                "limitSpeedDownloadFragment",
                                                "errorCode="
                                                        + errorCode
                                                        + " alreadySize no equal totalSize,alreadySize="
                                                        + mOfflineCache
                                                        .getCacheAlreadySize()
                                                        + " totalSize="
                                                        + mOfflineCache
                                                        .getCacheTotalSize()
                                                        + " name="
                                                        + mOfflineCache.getCacheName()
                                                        + " storagePath="
                                                        + mOfflineCache
                                                        .getCacheStoragePath()
                                                        + " url="
                                                        + mOfflineCache
                                                        .getCacheDetailUrl());
                                        mOfflineCache
                                                .setCacheAlreadySize(mOfflineCache
                                                        .getCacheTotalSize());
                                    } else {

                                        // 下载成功,不做处理
                                        L.v(TAG, "limitSpeedDownloadFragment", "alreadySize="
                                                + mOfflineCache
                                                .getCacheAlreadySize()
                                                + " totalSize="
                                                + mOfflineCache
                                                .getCacheTotalSize());
                                        errorCode = OfflineCache.CACHE_ERROR_CODE_SUCCESS;
                                    }
                                    break;
                                case OfflineCache.CACHE_ERROR_CODE_NOT_WRITE:
                                    //忽略
                                    break;
                            }
                            break;
                    }

                }
            }

        } catch (SocketException e) {

            switch (mOperateType) {
                case OfflineCache.OPERATE_RUNNING:
                    // 正常下载io异常
                    L.e(TAG,
                            "limitSpeedDownloadFragment",
                            "name="
                                    + mOfflineCache.getCacheName()
                                    + " SocketException errorCode=CACHE_ERROR_CODE_IO mOperateType=OPERATE_RUNNING"
                                    + " fragmentUrl="
                                    + mOfflineCache
                                    .getCacheDetailUrl(), e);

                    errorCode = OfflineCache.CACHE_ERROR_CODE_IO;
                    break;
                case OfflineCache.OPERATE_STOP:
                case OfflineCache.OPERATE_STOP_BATCH_PAUSE_STATE:
                case OfflineCache.OPERATE_STOP_BATCH_UNCHANGE_STATE:
                case OfflineCache.OPERATE_STOP_BATCH_WAIT_STATE:
                case OfflineCache.OPERATE_DELETE:
                    L.w(TAG,
                            "limitSpeedDownloadFragment",
                            "name="
                                    + mOfflineCache.getCacheName()
                                    + " SocketException errorCode=CACHE_ERROR_CODE_USER_OPERATE mOperateType="
                                    + mOperateType + " fragmentUrl="
                                    + mOfflineCache
                                    .getCacheDetailUrl());

                    // 手动停止任务
                    if (mHttpURLConnection != null) {
                        mHttpURLConnection.disconnect();
                    }

                    errorCode = OfflineCache.CACHE_ERROR_CODE_USER_OPERATE;
                    break;
                default:
                    // 其他情况
                    L.e(TAG,
                            "limitSpeedDownloadFragment",
                            "name=" + mOfflineCache.getCacheName()
                                    + " SocketException default errorCode="
                                    + errorCode + " mOperateType=" + mOperateType
                                    + " fragmentUrl="
                                    + mOfflineCache
                                    .getCacheDetailUrl(), e);
                    break;
            }

        } catch (SocketTimeoutException e) {

            // 超过规定次数就发送出错信息
            errorCode = OfflineCache.CACHE_ERROR_CODE_SOCKET_TIMEOUT;
            L.e(TAG,
                    "limitSpeedDownloadFragment",
                    "name=" + mOfflineCache.getCacheName()
                            + " SocketTimeoutException retry errorCode="
                            + errorCode + " alreadySize="
                            + mOfflineCache.getCacheAlreadySize()
                            + " totalSize=" + mOfflineCache.getCacheTotalSize()
                            + " fragmentUrl="
                            + mOfflineCache
                            .getCacheDetailUrl(), e);

        } catch (MalformedURLException e) {
            if (e != null) {
                e.printStackTrace();
                mOfflineCache.setCacheErrorMessage(mOfflineCache.getCacheName()
                        + e.getMessage() + " fragmentUrl="
                        + mOfflineCache
                        .getCacheDetailUrl());
            }
            L.e(TAG,
                    "limitSpeedDownloadFragment",
                    "name=" + mOfflineCache.getCacheName()
                            + " MalformedURLException alreadySize="
                            + mOfflineCache.getCacheAlreadySize()
                            + " totalSize=" + mOfflineCache.getCacheTotalSize()
                            + " fragmentUrl="
                            + mOfflineCache
                            .getCacheDetailUrl(), e);
            errorCode = OfflineCache.CACHE_ERROR_CODE_MALFORMED_URL;
            mOfflineCache.setCacheDownloadState(OfflineCache.CACHE_STATE_ERROR);
            msg = new Message();
            bundle = new Bundle();
            bundle.putParcelable(OfflineCache.OFFLINE_CACHE, mOfflineCache);
            msg.setData(bundle);
            msg.what = StorageModule.MSG_DOWNLOAD_ERROR;
            mHandler.sendMessage(msg);

        } catch (ProtocolException e) {
            if (e != null) {
                e.printStackTrace();
                mOfflineCache.setCacheErrorMessage(mOfflineCache.getCacheName()
                        + e.getMessage() + " fragmentUrl="
                        + mOfflineCache
                        .getCacheDetailUrl());
            }
            L.e(TAG,
                    "limitSpeedDownloadFragment",
                    "name=" + mOfflineCache.getCacheName()
                            + " ProtocolException alreadySize="
                            + mOfflineCache.getCacheAlreadySize()
                            + " totalSize=" + mOfflineCache.getCacheTotalSize()
                            + " fragmentUrl="
                            + mOfflineCache
                            .getCacheDetailUrl(), e);
            errorCode = OfflineCache.CACHE_ERROR_CODE_CLIENT_PROTOCOL;
            mOfflineCache.setCacheDownloadState(OfflineCache.CACHE_STATE_ERROR);
            msg = new Message();
            bundle = new Bundle();
            bundle.putParcelable(OfflineCache.OFFLINE_CACHE, mOfflineCache);
            msg.setData(bundle);
            msg.what = StorageModule.MSG_DOWNLOAD_ERROR;
            mHandler.sendMessage(msg);

        } catch (IOException e) {
            if (e != null) {
                e.printStackTrace();
                mOfflineCache.setCacheErrorMessage(mOfflineCache.getCacheName()
                        + e.getMessage() + " fragmentUrl="
                        + mOfflineCache
                        .getCacheDetailUrl());
            }
            L.e(TAG,
                    "limitSpeedDownloadFragment",
                    "exception=" + e + " name=" + mOfflineCache.getCacheName()
                            + " alreadySize="
                            + mOfflineCache.getCacheAlreadySize()
                            + " totalSize=" + mOfflineCache.getCacheTotalSize()
                            + " fragmentUrl="
                            + mOfflineCache
                            .getCacheDetailUrl(), e);

            if (e.getMessage().equals("unexpected end of stream")) {

                errorCode = OfflineCache.CACHE_ERROR_CODE_CLIENT_PROTOCOL;

            } else {

                errorCode = OfflineCache.CACHE_ERROR_CODE_IO;
                mOfflineCache.setCacheDownloadState(OfflineCache.CACHE_STATE_ERROR);
                msg = new Message();
                bundle = new Bundle();
                bundle.putParcelable(OfflineCache.OFFLINE_CACHE, mOfflineCache);
                msg.setData(bundle);
                msg.what = StorageModule.MSG_DOWNLOAD_ERROR;
                mHandler.sendMessage(msg);

            }

        } finally {

            // 释放资源
            release(inputStream, randomAccessFile);
        }

        L.v(TAG,
                "limitSpeedDownloadFragment",
                "end name=" + mOfflineCache.getCacheName()
                        + " errorCode="
                        + errorCode + " storagePath="
                        + mOfflineCache
                        .getCacheStoragePath()
                        + " url=" + mOfflineCache
                        .getCacheDetailUrl());

        return errorCode;
    }

    /**
     * 判断下载状态
     *
     * @param errorCode 状态码
     * @return void
     */

    private void judgeResult(int errorCode) {

        L.v(TAG,
                "judgeResult",
                "start errorCode=" + errorCode + " name="
                        + mOfflineCache.getCacheName()
                        + " storagePath="
                        + mOfflineCache.getCacheStoragePath()
                        + " url=" + mOfflineCache.getCacheDetailUrl());

        // 判断结果
        switch (errorCode) {
            case OfflineCache.CACHE_ERROR_CODE_SUCCESS:
                long alreadySize = mOfflineCache.getCacheAlreadySize();
                long totalSize = mOfflineCache.getCacheTotalSize();
                if (alreadySize != 0 && totalSize != 0 && alreadySize == totalSize) {

                    L.v(TAG,
                            "judgeResult",
                            "CACHE_ERROR_CODE_SUCCESS DOWNLOAD_STATE_FINISH name="
                                    + mOfflineCache.getCacheName()
                                    + " alreadySize=" + alreadySize + " totalSize="
                                    + totalSize + " fragmentUrl="
                                    + mOfflineCache.getCacheDetailUrl());
                    mOfflineCache.setCacheAlreadySize(mOfflineCache
                            .getCacheTotalSize());

                    // 分片列表最后一个分片下载完毕
                    L.v(TAG, "judgeResult",
                            "already download finished last frag,offlineCacheFragmentLinkedList.size=0");
                    mRunnableState = STATE_FINISH;

                    mOfflineCache
                            .setCacheDownloadState(OfflineCache.CACHE_STATE_FINISH);
                    String ccrid = mOfflineCache.getCacheCCRId();
                    L.v(TAG, "judgeResult", "ccrid=" + ccrid + " path=" + mOfflineCache.getCacheDetailUrl());
                    Reporter.reportDownloadSuccess(ccrid);
                } else {

                    L.e(TAG,
                            "judgeResult",
                            "CACHE_ERROR_CODE_SUCCESS CACHE_STATE_ERROR name="
                                    + mOfflineCache.getCacheName()
                                    + " alreadySize=" + alreadySize + " totalSize="
                                    + totalSize + " fragmentUrl="
                                    + mOfflineCache
                                    .getCacheDetailUrl());
                    mOfflineCache
                            .setCacheDownloadState(OfflineCache.CACHE_STATE_ERROR);
                    mOfflineCache
                            .setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_FRAGMENT_UNSUCCESS);

                    mRunnableState = STATE_STOP_SEND;

                }

                break;
            case OfflineCache.CACHE_ERROR_CODE_NOT_WRITE:

                L.e(TAG,
                        "judgeResult",
                        "CACHE_ERROR_CODE_NOT_WRITE name="
                                + mOfflineCache.getCacheName()
                                + " alreadySize="
                                + mOfflineCache.getCacheAlreadySize()
                                + " totalSize="
                                + mOfflineCache.getCacheTotalSize()
                                + " storagePath="
                                + mOfflineCache
                                .getCacheStoragePath()
                                + " fragmentUrl="
                                + mOfflineCache.getCacheDetailUrl());
                switch (mOperateType) {
                    case OfflineCache.OPERATE_STOP:
                    case OfflineCache.OPERATE_STOP_BATCH_PAUSE_STATE:
                    case OfflineCache.OPERATE_STOP_BATCH_UNCHANGE_STATE:
                    case OfflineCache.OPERATE_STOP_BATCH_WAIT_STATE:
                    case OfflineCache.OPERATE_DELETE:
                        // 手动停止任务
                        errorCode = OfflineCache.CACHE_ERROR_CODE_USER_OPERATE;
                        mRunnableState = STATE_STOP_SEND;
                        break;
                    default:
                        switch (mOfflineCache.getCacheDownloadType()) {
                            case OfflineCache.CACHE_FROM_PAGE_RECOMMEND:
                                currentFragmentLogicRetry(errorCode);
                                break;
                        }

                }

                // 存储设备未挂载
//                mOfflineCache
//                        .setCacheDownloadState(OfflineCache.CACHE_STATE_WAITING);

                break;
            case OfflineCache.CACHE_ERROR_CODE_USER_OPERATE:

                switch (mOperateType) {
                    case OfflineCache.OPERATE_STOP:
                        L.v(TAG, "judgeResult",
                                "CACHE_STATE_PAUSE mOperateType=OPERATE_STOP fragmentUrl="
                                        + mOfflineCache.getCacheDetailUrl());
                        mOfflineCache
                                .setCacheDownloadState(OfflineCache.CACHE_STATE_PAUSE);
                        mOfflineCache
                                .setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_USER_OPERATE);
                        mRunnableState = STATE_STOP_SEND;
                        break;
                    case OfflineCache.OPERATE_STOP_BATCH_PAUSE_STATE:
                        L.v(TAG, "judgeResult",
                                "CACHE_STATE_PAUSE mOperateType=OPERATE_STOP_BATCH_PAUSE_STATE fragmentUrl="
                                        + mOfflineCache.getCacheDetailUrl());
                        // mOfflineCache.addOfflineCacheFragment(offlineCacheFragment);
                        mOfflineCache
                                .setCacheDownloadState(OfflineCache.CACHE_STATE_PAUSE);
                        mOfflineCache
                                .setCacheDownloadStateModel(OfflineCache.CACHE_STATE_MODEL_AUTO);
                        mOfflineCache
                                .setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_USER_OPERATE);
                        mRunnableState = STATE_STOP_SEND;
                        break;
                    case OfflineCache.OPERATE_STOP_BATCH_WAIT_STATE:
                        L.v(TAG, "judgeResult",
                                "CACHE_STATE_PAUSE OPERATE_STOP_BATCH_WAIT_STATE fragmentUrl="
                                        + mOfflineCache.getCacheDetailUrl());
                        // mOfflineCache.addOfflineCacheFragment(offlineCacheFragment);
                        mOfflineCache
                                .setCacheDownloadState(OfflineCache.CACHE_STATE_WAITING);
                        mOfflineCache
                                .setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_USER_OPERATE);
                        mRunnableState = STATE_STOP_SEND;
                        break;
                    case OfflineCache.OPERATE_STOP_BATCH_UNCHANGE_STATE:
                        L.v(TAG, "judgeResult",
                                "CACHE_STATE_PAUSE OPERATE_STOP_BATCH_UNCHANGE_STATE fragmentUrl="
                                        + mOfflineCache.getCacheDetailUrl());
                        mOfflineCache
                                .setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_USER_OPERATE);
                        mRunnableState = STATE_STOP_SEND;
                        break;
                    case OfflineCache.OPERATE_DELETE:

                        L.v(TAG, "judgeResult",
                                "CACHE_STATE_PAUSE OPERATE_DELETE fragmentUrl="
                                        + mOfflineCache.getCacheDetailUrl());
                        // 删除文件
                        OfflineCacheModule.getInstance().deleteOfflineCacheFile(
                                mOfflineCache);
                        mRunnableState = STATE_STOP_UNSEND;
                        break;
                    default:
                        break;
                }
                break;
            case OfflineCache.CACHE_ERROR_CODE_CLIENT_PROTOCOL:
            case OfflineCache.CACHE_ERROR_CODE_SOCKET_ECONNRESET:
                switch (mOperateType) {
                    case OfflineCache.OPERATE_STOP:
                    case OfflineCache.OPERATE_STOP_BATCH_PAUSE_STATE:
                    case OfflineCache.OPERATE_STOP_BATCH_UNCHANGE_STATE:
                    case OfflineCache.OPERATE_STOP_BATCH_WAIT_STATE:
                    case OfflineCache.OPERATE_DELETE:
                        // 手动停止任务
                        errorCode = OfflineCache.CACHE_ERROR_CODE_USER_OPERATE;
                        mRunnableState = STATE_STOP_SEND;
                        break;
                    default:
                        currentFragmentProtocal(errorCode);
                        break;
                }
                break;
            case OfflineCache.CACHE_ERROR_CODE_SOCKET_TIMEOUT:
            case OfflineCache.CACHE_ERROR_CODE_HOST_CONNECT_REFUSED:
            case OfflineCache.CACHE_ERROR_CODE_IO:
            case OfflineCache.CACHE_ERROR_CODE_RESPONSE_STATUS_CODE_EXCEPTION:
            case OfflineCache.CACHE_ERROR_CODE_FRAGMENT_URL_RESPONSE_MIME_FAIL:
            case OfflineCache.CACHE_ERROR_CODE_UNKNOWN_HOST_EXCEPTION:
            case OfflineCache.CACHE_ERROR_CODE_UNAVAILABLE:
                /*** 分片文件,下载过程中真对具体问题,进行重试 ***/

                L.w(TAG,
                        "judgeResult",
                        "errorCode="
                                + errorCode
                                + " name="
                                + mOfflineCache.getCacheName()
                                + " storagePath="
                                + mOfflineCache
                                .getCacheStoragePath()
                                + " fragmentUrl="
                                + mOfflineCache.getCacheDetailUrl());

                switch (mOperateType) {
                    case OfflineCache.OPERATE_STOP:
                    case OfflineCache.OPERATE_STOP_BATCH_PAUSE_STATE:
                    case OfflineCache.OPERATE_STOP_BATCH_UNCHANGE_STATE:
                    case OfflineCache.OPERATE_STOP_BATCH_WAIT_STATE:
                    case OfflineCache.OPERATE_DELETE:
                        // 手动停止任务
                        errorCode = OfflineCache.CACHE_ERROR_CODE_USER_OPERATE;
                        mRunnableState = STATE_STOP_SEND;
                        break;
                    default:

                        currentFragmentLogicRetry(errorCode);
                }

                break;
            case OfflineCache.CACHE_ERROR_CODE_CONTENT_LENGTH_EXCEPTION:
            case OfflineCache.CACHE_ERROR_CODE_FRAGMENT_NOT_FOUNT:
                /** 分片文件,下载文件的长度异常,进行重试 **/
                L.w(TAG,
                        "judgeResult",
                        "errorCode="
                                + errorCode
                                + " name="
                                + mOfflineCache.getCacheName()
                                + " storagePath="
                                + mOfflineCache
                                .getCacheStoragePath()
                                + " fragmentUrl="
                                + mOfflineCache.getCacheDetailUrl());

                switch (mOperateType) {
                    case OfflineCache.OPERATE_STOP:
                    case OfflineCache.OPERATE_STOP_BATCH_PAUSE_STATE:
                    case OfflineCache.OPERATE_STOP_BATCH_UNCHANGE_STATE:
                    case OfflineCache.OPERATE_STOP_BATCH_WAIT_STATE:
                    case OfflineCache.OPERATE_DELETE:
                        // 手动停止任务
                        errorCode = OfflineCache.CACHE_ERROR_CODE_USER_OPERATE;
                        mRunnableState = STATE_STOP_SEND;
                        break;
                    default:
                        //重试
                        currentFragmentLogicRetry(errorCode);

//                        File file = new File(mOfflineCache.getCacheStoragePath());
//                        if (file.exists()) {
//                            file.delete();
//                        }
//                        mOfflineCache.setCacheAlreadySize(0);
//                        mOfflineCache.setCacheTotalSize(0);
//                        mOfflineCache.setCacheDownloadState(OfflineCache.CACHE_STATE_DOWNLOADING);
//                        mOfflineCache.setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_SUCCESS);
//                        run();
                        break;

                }
                break;
            case OfflineCache.CACHE_ERROR_CODE_NEXT:
                L.v(TAG, "judgeResult",
                        "CACHE_ERROR_CODE_NEXT DOWNLOAD_STATE_FINISH fragmentUrl="
                                + mOfflineCache.getCacheDetailUrl());
                // 下载下一个分片
                limitSpeedDownloadFragmentList(0);

                break;
            case OfflineCache.CACHE_ERROR_CODE_MALFORMED_URL:
            case OfflineCache.CACHE_ERROR_CODE_MESSAGE_PARSE_FAIL:
                mRunnableState = STATE_STOP_SEND;
                L.e(TAG,
                        "judgeResult",
                        "name="
                                + mOfflineCache.getCacheName()
                                + " errorCode="
                                + errorCode
                                + "storagePath="
                                + mOfflineCache
                                .getCacheStoragePath()
                                + " fragmentUrl="
                                + mOfflineCache.getCacheDetailUrl());
                break;
            default:
                break;
        }
    }

    /**
     * 发送下载中的消息
     */
    private void sendDownloadingMessage() {
        // 发送消息
        //L.v(TAG, "sendDownloadingMessage", "name=" + mOfflineCache.getCacheName() + " alreadySize=" + mOfflineCache.getCacheAlreadySize() + " totalSize=" + mOfflineCache.getCacheTotalSize());
        OfflineCacheModule.getInstance().mergeOfflineCache(mOfflineCache);

    }

    private void release(
            InputStream inputStream,
            RandomAccessFile randomAccessFile) {

        L.v(TAG, "release", "start name=" + mOfflineCache.getCacheName()
        );
        // 关闭文件流
        if (randomAccessFile != null) {
            try {
                L.v(TAG, "release", "name=" + mOfflineCache.getCacheName()
                        + " randomAccessFile close");
                randomAccessFile.close();

            } catch (IOException e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        } else {
            L.e(TAG, "release", "randomAccessFile=null");
        }

        // 关闭缓冲流
        if (inputStream != null) {
            try {

                inputStream.close();

                L.v(TAG, "release", "name=" + mOfflineCache.getCacheName()
                        + " inputStream close");

            } catch (IOException e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        } else {
            L.e(TAG, "release", "inputStream=null");
        }
        //关闭连接
        if (mHttpURLConnection != null) {
            mHttpURLConnection.disconnect();
        }
    }

    /**
     * 验证请求是否有效
     *
     * @return boolean 是否有效
     */
    private boolean isValidateHttpReponse() {
        boolean isValidate;
        L.v(TAG, "isValidateHttpReponse", "start");
        // rf2616协议
        switch (mResponseCode) {
            case HttpURLConnection.HTTP_OK:
            case HttpURLConnection.HTTP_PARTIAL:
                // 请求成功
                isValidate = true;

                // 判断内容是否合法
                String contentType = mHttpURLConnection.getContentType();
                if (contentType != null) {
                    if (!contentType.contains("text/html") && !contentType.contains("text/xml")) {
                        String lastModified = mHttpURLConnection.getHeaderField("Last-Modified");
                        updateDownloadConfig(lastModified);
                        L.v(TAG, "isValidateHttpReponse", "contentLength=" + mContentLength + " lastModified=" + lastModified);
                        return isValidate;
                    }

                    //输出错误信息
                    StringBuilder content = new StringBuilder();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(mHttpURLConnection.getInputStream()));
                        String temp;
                        while ((temp = reader.readLine()) != null) {
                            content.append(temp);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        L.e(TAG, "isValidateHttpReponse", "contentType=" + contentType + " contentLength=" + mContentLength, e);
                    }
                    L.e(TAG, "isValidateHttpReponse", "contentType=" + contentType + " contentLength=" + mContentLength + " content=" + content.toString());
                    mOfflineCache
                            .setCacheDownloadState(OfflineCache.CACHE_STATE_ERROR);
                    mOfflineCache
                            .setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_FRAGMENT_URL_RESPONSE_MIME_FAIL);
                    isValidate = false;
                } else {
                    L.e(TAG, "isValidateHttpReponse", "contentType=null contentLength=" + mContentLength);
                }
                break;
            case HttpURLConnection.HTTP_NOT_FOUND:
                // 地址未找到
                L.e(TAG,
                        "isValidateHttpReponse",
                        "responseCode=" + mResponseCode + " name="
                                + mOfflineCache.getCacheName()
                                + " fragmentUrl="
                                + mOfflineCache.getCacheDetailUrl());
                isValidate = false;
                mOfflineCache
                        .setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_FRAGMENT_NOT_FOUNT);
                break;
            case HttpURLConnection.HTTP_UNAVAILABLE:
                // 服务器不可用
                L.e(TAG,
                        "isValidateHttpReponse",
                        "responseCode=" + mResponseCode + " name="
                                + mOfflineCache.getCacheName()
                                + " fragmentUrl="
                                + mOfflineCache.getCacheDetailUrl());
                isValidate = false;
                mOfflineCache
                        .setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_UNAVAILABLE);
                break;
            case HttpURLConnection.HTTP_LENGTH_REQUIRED:
                L.e(TAG,
                        "isValidateHttpReponse",
                        "responseCode=" + mResponseCode + " name="
                                + mOfflineCache.getCacheName()
                                + " fragmentUrl="
                                + mOfflineCache.getCacheDetailUrl());
                isValidate = false;
                mOfflineCache
                        .setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_CONTENT_LENGTH_EXCEPTION);
                break;
            default:
                // 请求失败
                L.e(TAG,
                        "isValidateHttpReponse",
                        "responseCode=" + mResponseCode + " name="
                                + mOfflineCache.getCacheName()
                                + " fragmentUrl="
                                + mOfflineCache.getCacheDetailUrl());
                isValidate = false;
                mOfflineCache
                        .setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_RESPONSE_STATUS_CODE_EXCEPTION);
                break;
        }
        // 发送消息
        if (!isValidate) {

            //  mOfflineCache.addOfflineCacheFragment(offlineCacheFragment);
            mOfflineCache.setCacheDownloadState(OfflineCache.CACHE_STATE_ERROR);

        }
        return isValidate;
    }

    /**
     * 根据分片地址获取http响应
     */
    private void getHttpResponse() {

        String fragmentUrl = mOfflineCache.getCacheDetailUrl();
        //fragmentUrl=fragmentUrl.replace("v4.100tv.com","192.168.0.222:8080");
        // fragmentUrl =
        // "http://112.65.44.26/0/2bd065483a0d764687e6ddd38c9684bb.mp4?fpp.ver=1.3.0.10&key=a95d91d98cc50d89e7a515bce5030bc0&k=0129893215c13243f51927bb39d5498e-e5b1-1416040803&type=web.fpp";
        long alreadySize = mOfflineCache.getCacheAlreadySize();
        long totalSize = mOfflineCache.getCacheTotalSize();

        L.v(TAG,
                "getHttpResponse",
                "start name=" + mOfflineCache.getCacheName()
                        + " alreadySize="
                        + alreadySize + " totalSize=" + totalSize
                        + " fragmentUrl=" + fragmentUrl);
        try {

            URL url = new URL(fragmentUrl);
            mHttpURLConnection = (HttpURLConnection) url.openConnection();
            if (totalSize > 0) {
                // 获取指定位置的数据，Range范围如果超出服务器上数据范围, 会以服务器数据末尾为准
                // 总大小减1,防止responseCode=416错误
                mHttpURLConnection.setRequestProperty("Range", "bytes=" + alreadySize + "-"
                        + (totalSize - 1));
                // 防止ConnectionClosedException
                String lastModified = getDownloadConfig();
                //mHttpURLConnection.setRequestProperty("If-Range", lastModified);
                L.v(TAG, "getHttpResponse", "Range bytes=" + alreadySize + "-"
                        + totalSize);
            } else {
                mHttpURLConnection.setRequestProperty("Range", "bytes=0-");
                L.v(TAG, "getHttpResponse", "Range bytes=" + alreadySize + "-");

            }
            // 防止length=0
            mHttpURLConnection.setRequestProperty("Accept-Encoding", "identity;q=1, *;q=0");
            //httpGet.addHeader("User-Agent", "Lavf54.29.104");
            mHttpURLConnection.setRequestProperty("Accept", "*/*");
            mHttpURLConnection.setRequestProperty("Accept-Language", "zh-CN");
            mHttpURLConnection.setRequestProperty("Charset", "UTF-8");
            // httpGet.addHeader("Accept-Language", "zh-CN");
            //httpGet.addHeader("Connection", "close");
            // httpGet.addHeader("Referer", fragmentUrl);
            // CloseableHttpResponse closeableHttpResponse = null;
            L.v(TAG, "getHttpResponse",
                    "headers=" + mHttpURLConnection.getRequestProperties().toString());
            mHttpURLConnection.setRequestMethod("GET");
            mHttpURLConnection.setConnectTimeout(1000 * 1000);
            mHttpURLConnection.setReadTimeout(1000 * 1000);
            mHttpURLConnection.setUseCaches(false);
            mHttpURLConnection.setDoInput(true);
            mHttpURLConnection.setInstanceFollowRedirects(true);
            mHttpURLConnection.connect();

            //获取状态码
            mResponseCode = mHttpURLConnection.getResponseCode();

            //获取内容长度
            mContentLength = mHttpURLConnection.getContentLength();

        } catch (SocketException e) {

            if (e != null) {
                e.printStackTrace();
            }

            L.e(TAG,
                    "getHttpResponse",
                    "SocketException name="
                            + mOfflineCache.getCacheName()
                            + " alreadySize=" + alreadySize + " totalSize="
                            + totalSize + " fragmentUrl=" + fragmentUrl, e);

            if (e.getMessage().equals("recvfrom failed: ECONNRESET (Connection reset by peer)")) {
                mOfflineCache
                        .setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_SOCKET_ECONNRESET);
            } else {
                mOfflineCache
                        .setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_IO);
            }


        } catch (IllegalStateException e) {

            if (e != null) {
                e.printStackTrace();
            }

            L.e(TAG,
                    "getHttpResponse",
                    "IllegalStateException name="
                            + mOfflineCache.getCacheName()
                            + " alreadySize=" + alreadySize + " totalSize="
                            + totalSize + " fragmentUrl=" + fragmentUrl, e);
            mOfflineCache
                    .setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_HOST_CONNECT_REFUSED);

        } catch (SocketTimeoutException e) {

            if (e != null) {
                e.printStackTrace();
            }

            L.e(TAG,
                    "getHttpResponse",
                    "SocketTimeoutException name="
                            + mOfflineCache.getCacheName()
                            + " alreadySize=" + alreadySize + " totalSize="
                            + totalSize + " fragmentUrl=" + fragmentUrl, e);

            mOfflineCache
                    .setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_SOCKET_TIMEOUT);

        } catch (UnknownHostException e) {

            if (e != null) {
                e.printStackTrace();
            }

            L.e(TAG,
                    "getHttpResponse",
                    "UnknownHostException name="
                            + mOfflineCache.getCacheName()
                            + " alreadySize=" + alreadySize + " totalSize="
                            + totalSize + " fragmentUrl=" + fragmentUrl, e);

            mOfflineCache
                    .setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_SOCKET_TIMEOUT);

        } catch (IOException e) {
            if (e != null) {
                e.printStackTrace();
            }

            L.e(TAG, "getHttpResponse",
                    "IOException name=" + mOfflineCache.getCacheName()
                            + " alreadySize=" + alreadySize + " totalSize="
                            + totalSize + " fragmentUrl=" + fragmentUrl, e);
            mOfflineCache
                    .setCacheErrorCode(OfflineCache.CACHE_ERROR_CODE_IO);
        }
    }

    /**
     * 分片文件逻辑重试
     *
     * @param errorCode 状态码
     * @return void
     */
    private void currentFragmentLogicRetry(int errorCode) {

        // 判断网络
        if (DownloadUtil.getInstance().isNetwork()) {

            // 其他状态错误
            mCurrentFragmentLogicRetryTime++;

            // 超过规定次数就发送出错信息
            if (mCurrentFragmentLogicRetryTime <= FRAGMENT_MAX_TIME) {

                mRunnableState = STATE_RUNNING;

                L.w(TAG,
                        "currentFragmentLogicRetry",
                        "retry mCurrentFragmentLogicRetryTime="
                                + mCurrentFragmentLogicRetryTime
                                + " errorCode=" + errorCode + " name="
                                + mOfflineCache.getCacheName()
                                + " fragmentUrl="
                                + mOfflineCache.getCacheDetailUrl());

                errorCode = limitSpeedDownloadFragment(0);

                // 设置状态码
                mOfflineCache.setCacheErrorCode(errorCode);

                // 判断下载状态
                judgeResult(errorCode);

            } else {

                L.e(TAG, "currentFragmentLogicRetry",
                        "already retry FRAGMENT_MAX_TIME errorCode="
                                + errorCode + " send MSG_DOWNLOAD_ERROR");
                // 重试结束,判断下载状态
                mOfflineCache
                        .setCacheDownloadState(OfflineCache.CACHE_STATE_ERROR);

                mCurrentFragmentLogicRetryTime = 0;

                mRunnableState = STATE_STOP_SEND;

            }

        } else {

            L.e(TAG,
                    "currentFragmentLogicRetry",
                    "name="
                            + mOfflineCache.getCacheName()
                            + " isNetwork=false downloadState=CACHE_STATE_WAITING mRunnableState=STATE_STOP_SEND");

            mRunnableState = STATE_STOP_SEND;

            // 本地无网,url下载出错,标记成等待
            //mOfflineCache.addOfflineCacheFragment(offlineCacheFragment);
            mOfflineCache
                    .setCacheDownloadState(OfflineCache.CACHE_STATE_WAITING);
            mCurrentFragmentLogicRetryTime = 0;

        }

    }

    /**
     * 分片下载文件的长度重试
     *
     * @param errorCode 状态码
     * @return void
     */
    private void currentFragmentProtocal(int errorCode) {

        // 判断网络
        if (DownloadUtil.getInstance().isNetwork()) {

            // 其他状态错误
            mRunnableState = STATE_RUNNING;

            L.w(TAG,
                    "currentFragmentProtocal",
                    "retry errorCode=" + errorCode + " name="
                            + mOfflineCache.getCacheName()
                            + " fragmentUrl="
                            + mOfflineCache.getCacheDetailUrl());

            // 设置状态码
            mOfflineCache.setCacheErrorCode(0);

            errorCode = limitSpeedDownloadFragment(0);

            // 判断下载状态
            judgeResult(errorCode);

        } else {

            L.e(TAG,
                    "currentFragmentContentLengthRetry",
                    "name="
                            + mOfflineCache.getCacheName()
                            + " isNetwork=false downloadState=CACHE_STATE_WAITING mRunnableState=STATE_STOP_SEND");

            mRunnableState = STATE_STOP_SEND;

            // 本地无网,url下载出错,标记成等待
            // mOfflineCache.addOfflineCacheFragment(offlineCacheFragment);
            mOfflineCache
                    .setCacheDownloadState(OfflineCache.CACHE_STATE_WAITING);

        }

    }

    /**
     * 视频报文重试
     *
     * @return void
     */
    private void currentFilePacketRetry() {

        // 判断网络
        if (DownloadUtil.getInstance().isNetwork()) {

            // 报文重试,分片重试次数清0
            mCurrentFragmentContentLengthRetryTime = 0;
            mCurrentFragmentLogicRetryTime = 0;

            // 累计重试次数
            mOfflineCache.addCurrentPacketRetryTime();

            if (mOfflineCache.getCurrentPacketRetryTime() <= FILE_MAX_TIME) {

                L.w(TAG,
                        "currentFilePacketRetry",
                        "retry mCurrentFileRetryTime="
                                + mOfflineCache.getCurrentPacketRetryTime()
                                + " name=" + mOfflineCache.getCacheName()
                );

                // 强制刷新服务器缓存报文
//                OfflineCachePacketDownloadManager.getInstance()
//                        .getPacketByOfflineCache(this, true);

                mRunnableState = STATE_STOP_UNSEND;

            } else {

                L.e(TAG,
                        "currentFilePacketRetry",
                        "already retry FILE_MAX_TIME=" + FILE_MAX_TIME
                                + " errorCode="
                                + mOfflineCache.getCacheErrorCode()
                                + " send MSG_DOWNLOAD_ERROR");

                mRunnableState = STATE_STOP_SEND;

            }

        } else {

            L.e(TAG,
                    "currentFilePacketRetry",
                    "name="
                            + mOfflineCache.getCacheName()
                            + " isNetwork=false downloadState=CACHE_STATE_WAITING mRunnableState=STATE_STOP_SEND");

            mRunnableState = STATE_STOP_SEND;

            // 本地无网,url下载出错,标记成等待
            mOfflineCache
                    .setCacheDownloadState(OfflineCache.CACHE_STATE_WAITING);

        }
    }

    private void updateDownloadConfig(String lastModified) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("lastModified", lastModified);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SharedPreferenceModule.getInstance().setString(mOfflineCache.getCacheID() + "_dl_conf", jsonObject.toString());
    }

    private String getDownloadConfig() {
        String lastModified = "";
        try {
            String jsonObj = SharedPreferenceModule.getInstance().getString(mOfflineCache.getCacheID() + "_dl_conf");
            JSONObject jsonObject = new JSONObject(jsonObj);
            lastModified = jsonObject.optString("lastModified");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lastModified;
    }
}
