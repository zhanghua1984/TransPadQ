package cn.transpad.transpadui.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;


/**
 * 收藏和缓存实体类
 *
 * @author wangyang
 * @since 2014年5月13日
 */
public class OfflineCache implements Parcelable {
    // private static final String TAG = OfflineCache.class.getSimpleName();
    /**
     * 用于传递消息时,数据对应的键
     */
    public static final String OFFLINE_CACHE = "offline_cache";
    public static final String OFFLINE_CACHE_LIST = "offline_cache_list";
    public static final String OFFLINE_CACHE_FINISH_COUNT = "offline_cache_finish_count";
    public final static String OFFLINE_CACHE_CURRENT_SIZE = "offline_cache_current_size";
    public static final String OFFLINE_CACHE_IS_SHOW_LIMIT_SPACE = "offline_cache_is_show_limit_space";
    public static final String OFFLINE_CACHE_OPERATE = "offline_cache_operate";

    /**
     * 等待
     */
    public final static int CACHE_STATE_WAITING = 0;
    /**
     * 下载中
     */
    public final static int CACHE_STATE_DOWNLOADING = 1;
    /**
     * 暂停
     */
    public final static int CACHE_STATE_PAUSE = 2;
    /**
     * 暂停
     */
    public final static int CACHE_STATE_PAUSE_USER = 3;
    /**
     * 完成
     */
    public final static int CACHE_STATE_FINISH = 4;
    /**
     * 出错
     */
    public final static int CACHE_STATE_ERROR = 5;
    /**
     * 未缓存
     */
    public final static int CACHE_STATE_NOT_DOWNLOAD = 6;
    /**
     * 所有状态
     */
    public final static int CACHE_STATE_ALL = 7;
    /**
     * 操作中
     */
    public final static int CACHE_STATE_PROCESSING = 99;
    /**
     * 用户模式
     */
    public final static int CACHE_STATE_MODEL_USER = 1;
    /**
     * 自动模式
     */
    public final static int CACHE_STATE_MODEL_AUTO = 2;
    /**
     * 成功
     */
    public final static int CACHE_ERROR_CODE_SUCCESS = 0;
    /**
     * 套接字超时出错
     */
    public static final int CACHE_ERROR_CODE_SOCKET_TIMEOUT = 1;
    /**
     * 请求的分片地址未找到(404)
     */
    public static final int CACHE_ERROR_CODE_FRAGMENT_NOT_FOUNT = 2;
    /**
     * 请求的分片地址后,主机未找到
     */
    public static final int CACHE_ERROR_CODE_SOCKET_ECONNRESET = 3;
    /**
     * 请求的分片地址后,主机连接异常
     */
    public static final int CACHE_ERROR_CODE_HOST_CONNECT_REFUSED = 4;
    /**
     * 报文解析失败
     */
    public static final int CACHE_ERROR_CODE_MESSAGE_PARSE_FAIL = 5;
    /**
     * 请求的分片地址后,返回的状态码异常
     */
    public static final int CACHE_ERROR_CODE_RESPONSE_STATUS_CODE_EXCEPTION = 6;
    /**
     * io出错
     */
    public static final int CACHE_ERROR_CODE_IO = 7;
    /**
     * url出错
     */
    public static final int CACHE_ERROR_CODE_MALFORMED_URL = 8;
    /**
     * 客户端协议错误
     */
    public static final int CACHE_ERROR_CODE_CLIENT_PROTOCOL = 9;
    /**
     * 获取的文件长度异常
     */
    public static final int CACHE_ERROR_CODE_CONTENT_LENGTH_EXCEPTION = 10;
    /**
     * 文件不能写入
     */
    public static final int CACHE_ERROR_CODE_NOT_WRITE = 11;
    /**
     * 请求的分片地址成功后MIME类型错误
     */
    public static final int CACHE_ERROR_CODE_FRAGMENT_URL_RESPONSE_MIME_FAIL = 12;
    /**
     * 主机解析异常
     */
    public static final int CACHE_ERROR_CODE_UNKNOWN_HOST_EXCEPTION = 13;
    /**
     * 主机不可用(503)
     */
    public static final int CACHE_ERROR_CODE_UNAVAILABLE = 14;
    /**
     * 下载完成后,任务未成功
     */
    public static final int CACHE_ERROR_CODE_FRAGMENT_UNSUCCESS = 15;
    /**
     * 下载下一个分片
     */
    public final static int CACHE_ERROR_CODE_NEXT = 16;
    /**
     * 用户操作
     */
    public static final int CACHE_ERROR_CODE_USER_OPERATE = 17;
    /**
     * 运行任务
     */
    public static final int OPERATE_RUNNING = 1;
    /**
     * 停止任务
     */
    public static final int OPERATE_STOP = 2;
    /**
     * 批量停止任务,状态为暂停(更新状态)
     */
    public static final int OPERATE_STOP_BATCH_PAUSE_STATE = 3;
    /**
     * 批量停止任务,状态不变(不更新状态)
     */
    public static final int OPERATE_STOP_BATCH_UNCHANGE_STATE = 4;
    /**
     * 批量停止任务,状态为等待中(更新状态)
     */
    public static final int OPERATE_STOP_BATCH_WAIT_STATE = 5;
    /**
     * 批量停止任务,状态变为出错(更新状态)
     */
    public static final int OPERATE_STOP_BATCH_ERROR_STATE = 6;
    /**
     * 停止任务并删除相关文件
     */
    public static final int OPERATE_DELETE = 7;
    /**
     * 应用推荐类型
     */
    public final static int CACHE_FROM_PAGE_RECOMMEND = 1;
    /**
     * 升级类型
     */
    public final static int CACHE_FROM_PAGE_UPGRADE = 2;

    //文件ID
    private long mCacheID;
    //文件名称
    private String mCacheName;
    //文件图片路径
    private String mCacheImageUrl;
    //文件版本号
    private int mCacheVersionCode;
    //文件包名
    private String mCachePackageName;
    //关键词(语音搜索)
    private String mCacheKeyword;
    //文件当次下载大小
    private long mCacheCurrentSize;
    //文件已经缓存的大小
    private long mCacheAlreadySize;
    //文件总大小
    private long mCacheTotalSize;
    //文件下载速度
    private long mCacheSpeed;
    //文件总进度
    private float mCachePercentNum;
    //文件对应详情页地址(第一层级的文件类型点击时使用,小屏播放)
    private String mCacheDetailUrl;
    // 下载类型(1应用推荐,2升级)
    private int mCacheDownloadType = 1;
    // 缓存本地路径
    private String mCacheStoragePath;
    // 错误码
    private int mCacheErrorCode;
    // 错误信息
    private String mCacheErrorMessage;
    // 记录上报点
    private int mCacheReportPage;
    //包是否已安装
    private boolean mCacheIsInstall;
    // 当前文件重试次数
    private int mCurrentPacketRetryTime;
    //文件缓存状态
    private int mCacheDownloadState = OfflineCache.CACHE_STATE_NOT_DOWNLOAD;
    // 用户实时操作的缓存状态(用于界面处理)
    private int mCacheUserDownloadState = OfflineCache.CACHE_STATE_NOT_DOWNLOAD;
    // 下载状态模式(用户模式和自动模式)
    private int mCacheDownloadStateModel = OfflineCache.CACHE_STATE_MODEL_USER;
    private boolean mIsReplaceUserAgent;

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(mCacheID);
        out.writeString(mCacheName);
        out.writeString(mCacheImageUrl);
        out.writeInt(mCacheVersionCode);
        out.writeString(mCachePackageName);
        out.writeString(mCacheKeyword);
        out.writeLong(mCacheCurrentSize);
        out.writeLong(mCacheAlreadySize);
        out.writeLong(mCacheTotalSize);
        out.writeLong(mCacheSpeed);
        out.writeFloat(mCachePercentNum);
        out.writeString(mCacheDetailUrl);
        out.writeInt(mCacheDownloadType);
        out.writeString(mCacheStoragePath);
        out.writeInt(mCacheErrorCode);
        out.writeString(mCacheErrorMessage);
        out.writeInt(mCacheReportPage);
        out.writeInt(mCacheDownloadState);
        out.writeInt(mCacheUserDownloadState);
        out.writeInt(mCacheDownloadStateModel);
        out.writeInt(mCacheIsInstall ? 1 : 0);
    }

    public static final Creator<OfflineCache> CREATOR = new Creator<OfflineCache>() {
        public OfflineCache createFromParcel(Parcel in) {
            return new OfflineCache(in);
        }

        public OfflineCache[] newArray(int size) {
            return new OfflineCache[size];
        }
    };

    private OfflineCache(Parcel in) {
        mCacheID = in.readLong();
        mCacheName = in.readString();
        mCacheImageUrl = in.readString();
        mCacheVersionCode = in.readInt();
        mCachePackageName = in.readString();
        mCacheKeyword = in.readString();
        mCacheCurrentSize = in.readLong();
        mCacheAlreadySize = in.readLong();
        mCacheTotalSize = in.readLong();
        mCacheSpeed = in.readLong();
        mCachePercentNum = in.readFloat();
        mCacheDetailUrl = in.readString();
        mCacheDownloadType = in.readInt();
        mCacheStoragePath = in.readString();
        mCacheErrorCode = in.readInt();
        mCacheErrorMessage = in.readString();
        mCacheReportPage = in.readInt();
        mCacheDownloadState = in.readInt();
        mCacheUserDownloadState = in.readInt();
        mCacheDownloadStateModel = in.readInt();
        mCacheIsInstall = in.readInt() == 1;
    }

    public boolean getCacheIsInstall() {
        return mCacheIsInstall;
    }

    public void setCacheIsInstall(boolean cacheIsInstall) {
        mCacheIsInstall = cacheIsInstall;
    }

    public String getCachePackageName() {
        return mCachePackageName;
    }

    public void setCachePackageName(String cachePackageName) {
        mCachePackageName = cachePackageName;
    }

    public long getCacheSpeed() {
        return mCacheSpeed;
    }

    public void setCacheSpeed(long cacheSpeed) {
        mCacheSpeed = cacheSpeed;
    }

    public String getCacheKeyword() {
        return mCacheKeyword;
    }

    public void setCacheKeyword(String cacheKeyword) {
        mCacheKeyword = cacheKeyword;
    }

    public int getCacheVersionCode() {
        return mCacheVersionCode;
    }

    public void setCacheVersionCode(int cacheVersionCode) {
        mCacheVersionCode = cacheVersionCode;
    }

    public int getCacheDownloadStateModel() {
        return mCacheDownloadStateModel;
    }

    public void setCacheDownloadStateModel(int cacheDownloadStateModel) {

        mCacheDownloadStateModel = cacheDownloadStateModel;
    }

    public int getCurrentPacketRetryTime() {
        return mCurrentPacketRetryTime;
    }

    public void addCurrentPacketRetryTime() {

        mCurrentPacketRetryTime++;
    }

    public void setCurrentPacketRetryTime(int currentPacketRetryTime) {

        mCurrentPacketRetryTime = currentPacketRetryTime;
    }

    public long getCacheCurrentSize() {
        return mCacheCurrentSize;
    }

    public void setCacheCurrentSize(long cacheCurrentSize) {

        mCacheCurrentSize = cacheCurrentSize;
    }

    public int getCacheReportPage() {
        return mCacheReportPage;
    }

    public void setCacheReportPage(int cacheReportPage) {

        mCacheReportPage = cacheReportPage;
    }

    public int getCacheErrorCode() {
        return mCacheErrorCode;
    }

    public void setCacheErrorCode(int cacheErrorCode) {

        mCacheErrorCode = cacheErrorCode;
    }

    public String getCacheErrorMessage() {
        return mCacheErrorMessage == null ? "" : mCacheErrorMessage;
    }

    public void setCacheErrorMessage(String cacheErrorMessage) {

        mCacheErrorMessage = cacheErrorMessage;
    }

    public void setCachePercentNum(float percentNum) {
        mCachePercentNum = percentNum;
    }

    /**
     * 返回百分比
     *
     * @return int
     */
    public float getCachePercentNum() {
        // 计算当前进度百分比
        if (mCacheTotalSize > 0) {
            mCachePercentNum = ((float) mCacheAlreadySize / mCacheTotalSize) * 100;
        }
        return mCachePercentNum;
    }

    /**
     * 返回百分比字符串
     *
     * @return String
     */
    public String getCachePercentNumString() {
        if (mCacheTotalSize > 0) {
            mCachePercentNum = ((float) mCacheAlreadySize / mCacheTotalSize) * 100;
        }
        String percentNumStr = String.format(Locale.getDefault(), "%.2f",
                mCachePercentNum);
        return percentNumStr + "%";
    }

    public int getCacheDownloadType() {
        return mCacheDownloadType;
    }

    public void setCacheDownloadType(int cacheDownloadType) {

        mCacheDownloadType = cacheDownloadType;
    }

    public String getCacheStoragePath() {
        return mCacheStoragePath == null ? "" : mCacheStoragePath;
    }

    public void setCacheStoragePath(String cacheStoragePath) {

        mCacheStoragePath = cacheStoragePath;

    }

    public long getCacheID() {
        return mCacheID;
    }

    public void setCacheID(long cacheID) {

        mCacheID = cacheID;
    }

    public String getCacheDetailUrl() {
        return mCacheDetailUrl == null ? "" : mCacheDetailUrl;
    }

    public void setCacheDetailUrl(String cacheDetailUrl) {

        mCacheDetailUrl = cacheDetailUrl;
    }

    public String getCacheCCRId() {
        String ccrid = "";
        String[] params = mCacheDetailUrl.split("&");
        for (String param : params) {
            if (param.contains("ccrid")) {
                ccrid = param.substring(6);
            }
        }
        return ccrid;
    }

    public String getCacheName() {
        return mCacheName == null ? "" : mCacheName;
    }

    public void setCacheName(String cacheName) {

        mCacheName = cacheName;
    }

    public String getCacheImageUrl() {
        return mCacheImageUrl == null ? "" : mCacheImageUrl;
    }

    public void setCacheImageUrl(String cacheImageUrl) {

        mCacheImageUrl = cacheImageUrl;
    }

    public long getCacheAlreadySize() {
        return mCacheAlreadySize;
    }


    public void setCacheAlreadySize(long cacheAlreadySize) {

        mCacheAlreadySize = cacheAlreadySize;
    }

    public void addCacheAlreadySize(long currentLength) {

        mCacheAlreadySize += currentLength;

    }

    public long getCacheTotalSize() {
        return mCacheTotalSize;
    }

    public void setCacheTotalSize(long cacheTotalSize) {

        mCacheTotalSize = cacheTotalSize;
    }

    /**
     * 获取缓存状态
     *
     * @return int 缓存内容类型<br>
     * OfflineCache.CACHE_STATE_WAITING 等待<br>
     * OfflineCache.CACHE_STATE_PAUSE 暂停<br>
     * OfflineCache.CACHE_STATE_DOWNLOADING 下载中<br>
     * OfflineCache.CACHE_STATE_FINISH 完成<br>
     * OfflineCache.CACHE_STATE_ERROR 出错<br>
     */
    public int getCacheDownloadState() {
        return mCacheDownloadState;
    }

    /**
     * 设置缓存状态
     *
     * @param cacheState 缓存状态<br>
     *                   OfflineCache.CACHE_STATE_WAITING 等待<br>
     *                   OfflineCache.CACHE_STATE_PAUSE 暂停<br>
     *                   OfflineCache.CACHE_STATE_DOWNLOADING 下载中<br>
     *                   OfflineCache.CACHE_STATE_FINISH 完成<br>
     *                   OfflineCache.CACHE_STATE_ERROR 出错<br>
     * @return void
     */
    public void setCacheDownloadState(int cacheState) {

        mCacheDownloadState = cacheState;
        mCacheUserDownloadState = cacheState;
    }

    public int getCacheUserDownloadState() {
        return mCacheUserDownloadState;
    }

    public void setCacheUserDownloadState(int cacheUserDownloadState) {

        mCacheUserDownloadState = cacheUserDownloadState;
    }

    public boolean getIsReplaceUserAgent() {
        return mIsReplaceUserAgent;
    }

    public void setIsReplaceUserAgent(boolean isReplaceUserAgent) {

        mIsReplaceUserAgent = isReplaceUserAgent;
    }

    public OfflineCache() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

}
