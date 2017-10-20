package cn.transpad.transpadui.entity;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.RemoteViews;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.util.L;

/**
 * 下载实体类
 * 
 * @author wangyang
 * 
 */
public class Download implements Parcelable {
	private static final String TAG = Download.class.getSimpleName();
	public static final String DOWNLOAD = "download";
	/**
	 * 下载中
	 */
	public final static int DOWNLOAD_STATE_DOWNLOADING = 1;
	/**
	 * 暂停
	 */
	public final static int DOWNLOAD_STATE_PAUSE = 2;
	/**
	 * 等待
	 */
	public final static int DOWNLOAD_STATE_WAITING = 3;
	/**
	 * 完成
	 */
	public final static int DOWNLOAD_STATE_FINISH = 4;
	/**
	 * 出错
	 */
	public final static int DOWNLOAD_STATE_ERROR = 5;
	/**
	 * 未缓存
	 */
	public final static int DOWNLOAD_STATE_NOT_DOWNLOAD = 6;
	/**
	 * 所有状态
	 */
	public final static int DOWNLOAD_STATE_ALL = 7;
	/**
	 * 所有未缓存状态
	 */
	public final static int DOWNLOAD_STATE_ALL_NOT_DOWNLOAD = 8;
	/**
	 * 无效url
	 */
	public final static int DOWNLOAD_MALFORMED_URL_EXCEPTION = 1;
	/**
	 * IO异常
	 */
	public final static int DOWNLOAD_IO_EXCEPTION = 2;
	/**
	 * 数据正常
	 */
	public final static int DOWNLOAD_SUCCESS = 3;
	/**
	 * 升级上报
	 */
	public final static int DOWNLOAD_REPORT_UPDATE = 1;
	/**
	 * 应用推荐上报
	 */
	public final static int DOWNLOAD_REPORT_RECOMMEND = 2;
	/**
	 * 升级类型
	 */
	public final static int DOWNLOAD_UPGRADE = 1;
	/**
	 * 应用推荐类型
	 */
	public final static int DOWNLOAD_RECOMMEND = 2;
	// 下载ID
	private int mDownloadId;
	// 已经下载大小
	private long mDownloadAlreadySize;
	// 下载总大小
	private long mDownloadTotalSize;
	// 下载名称(eg:android4.1)
	private String mDownloadFileName;
	// 下载版本名称(eg:4.0.V.10.32.7.3245.9000.9000001)
	private String mDownloadVersionName;
	// 下载版本号(1开始,自增长)
	private int mDownloadVersionCode;
	// 下载描述信息
	private String mDownloadDesc;
	// 下载URL
	private String mDownloadUrl;
	// 图片URL
	private String mDownloadImageUrl;
	// 下载文件存储路径
	private String mDownloadStoragePath;
	// 通知栏显示下载进度
	private Notification mDownloadRunningNotification;
	// 通知栏显示已完成
	private Notification mDownloadFinishNotification;
	// 下载状态
	private int mDownloadStateType;
	// 错误信息
	private int mErrorCode;
	// 通知ID
	private int mDownloadNotificationId;
	// 是否显示正在下载通知
	private boolean mDownloadIsShowRunningNotification;
	// 是否显示已完成通知
	private boolean mDownloadIsShowFinishNotification;
	// 是否安装
	private boolean mDownloadIsInstall;
	// 错误时是否提示信息
	private boolean mDownloadIsErrorToast;
	// 上报类型
	private int mDownloadReportType;
	// 下载类型
	private int mDownloadType;
	// 是否限速
	private boolean mDownloadIsLimitSpeed;
	// 错误信息
	private String mDownloadErrorMessage;
	// 是否需要升级
	private boolean mDownloadIsUpgrade;
	// 是否支持切网(true:wifi时正常下载,非wifi停止下载;false:不做限制)
	private boolean mDownloadIsSupportSwitchNetwork;

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(mDownloadId);
		out.writeLong(mDownloadAlreadySize);
		out.writeLong(mDownloadTotalSize);
		out.writeString(mDownloadFileName);
		out.writeString(mDownloadUrl);
		out.writeString(mDownloadImageUrl);
		out.writeString(mDownloadStoragePath);
		out.writeParcelable(mDownloadRunningNotification, flags);
		out.writeParcelable(mDownloadFinishNotification, flags);
		out.writeInt(mDownloadStateType);
		out.writeInt(mErrorCode);
		out.writeInt(mDownloadIsShowRunningNotification ? 1 : 0);
		out.writeInt(mDownloadIsShowFinishNotification ? 1 : 0);
		out.writeInt(mDownloadIsInstall ? 1 : 0);
		out.writeInt(mDownloadIsErrorToast ? 1 : 0);
		out.writeInt(mDownloadReportType);
		out.writeInt(mDownloadType);
		out.writeInt(mDownloadIsLimitSpeed ? 1 : 0);
		out.writeString(mDownloadErrorMessage);
		out.writeInt(mDownloadIsUpgrade ? 1 : 0);
		out.writeInt(mDownloadIsSupportSwitchNetwork ? 1 : 0);
	}

	public static final Creator<Download> CREATOR = new Creator<Download>() {
		public Download createFromParcel(Parcel in) {
			return new Download(in);
		}

		public Download[] newArray(int size) {
			return new Download[size];
		}
	};

	private Download(Parcel in) {
		mDownloadId = in.readInt();
		mDownloadAlreadySize = in.readLong();
		mDownloadTotalSize = in.readLong();
		mDownloadFileName = in.readString();
		mDownloadUrl = in.readString();
		mDownloadImageUrl = in.readString();
		mDownloadStoragePath = in.readString();
		mDownloadRunningNotification = in.readParcelable(Notification.class
				.getClassLoader());
		mDownloadFinishNotification = in.readParcelable(Notification.class
				.getClassLoader());
		mDownloadStateType = in.readInt();
		mErrorCode = in.readInt();
		mDownloadIsShowRunningNotification = in.readInt() == 1;
		mDownloadIsShowFinishNotification = in.readInt() == 1;
		mDownloadIsInstall = in.readInt() == 1;
		mDownloadIsErrorToast = in.readInt() == 1;
		mDownloadReportType = in.readInt();
		mDownloadType = in.readInt();
		mDownloadIsLimitSpeed = in.readInt() == 1;
		mDownloadErrorMessage = in.readString();
		mDownloadIsUpgrade = in.readInt() == 1;
		mDownloadIsSupportSwitchNetwork = in.readInt() == 1;
	}

	public Download() {

	}

	@Override
	public int describeContents() {
		return 0;
	}

	public boolean getDownloadIsSupportSwitchNetwork() {
		return mDownloadIsSupportSwitchNetwork;
	}

	/**
	 * 
	 * 是否支持切换网络
	 * 
	 * @param downloadIsSupportSwitchNetwork
	 *            是否支持
	 * @return void
	 */
	public void setDownloadIsSupportSwitchNetwork(
			boolean downloadIsSupportSwitchNetwork) {

		mDownloadIsSupportSwitchNetwork = downloadIsSupportSwitchNetwork;
	}

	public boolean getDownloadIsShowRunningNotification() {
		return mDownloadIsShowRunningNotification;
	}

	public void setDownloadIsShowRunningNotification(
			boolean downloadIsShowRunningNotification) {

		mDownloadIsShowRunningNotification = downloadIsShowRunningNotification;
	}

	public boolean getDownloadIsShowFinishNotification() {
		return mDownloadIsShowFinishNotification;
	}

	public void setDownloadIsShowFinishNotification(
			boolean downloadIsShowFinishNotification) {

		mDownloadIsShowFinishNotification = downloadIsShowFinishNotification;
	}

	public String getDownloadDesc() {
		return mDownloadDesc;
	}

	public void setDownloadDesc(String downloadDesc) {

		mDownloadDesc = downloadDesc;
	}

	public void setDownloadImageUrl(String downloadImageUrl) {

		mDownloadImageUrl = downloadImageUrl;
	}

	public String getDownloadImageUrl() {

		return mDownloadImageUrl;
	}

	public String getDownloadVersionName() {
		return mDownloadVersionName;
	}

	public void setDownloadVersionName(String downloadVersionName) {

		mDownloadVersionName = downloadVersionName;
	}

	public int getDownloadVersionCode() {
		return mDownloadVersionCode;
	}

	public void setDownloadVersionCode(int downloadVersionCode) {

		mDownloadVersionCode = downloadVersionCode;
	}

	public boolean getDownloadIsUpgrade() {
		return mDownloadIsUpgrade;
	}

	public void setDownloadIsUpgrade(boolean downloadIsUpgrade) {

		mDownloadIsUpgrade = downloadIsUpgrade;
	}

	public String getDownloadErrorMessage() {
		return mDownloadErrorMessage;
	}

	public void setDownloadErrorMessage(String downloadErrorMessage) {

		mDownloadErrorMessage = downloadErrorMessage;
	}

	public boolean getDownloadIsLimitSpeed() {
		return mDownloadIsLimitSpeed;
	}

	public void setDownloadIsLimitSpeed(boolean downloadLimitSpeed) {

		mDownloadIsLimitSpeed = downloadLimitSpeed;
	}

	public int getDownloadType() {
		return mDownloadType;
	}

	public void setDownloadType(int downloadType) {

		mDownloadType = downloadType;
	}

	public int getDownloadReportType() {
		return mDownloadReportType;
	}

	public void setDownloadReportType(int downloadReportType) {

		mDownloadReportType = downloadReportType;
	}

	public boolean getDownloadIsErrorToast() {
		return mDownloadIsErrorToast;
	}

	public void setDownloadIsErrorToast(boolean downloadIsErrorToast) {

		mDownloadIsErrorToast = downloadIsErrorToast;
	}

	public boolean getDownloadIsInstall() {
		return mDownloadIsInstall;
	}

	public void setDownloadIsInstall(boolean downloadIsInstall) {

		mDownloadIsInstall = downloadIsInstall;
	}

	public int getDownloadNotificationId() {
		if (mDownloadUrl != null) {
			mDownloadNotificationId = Math.abs(mDownloadUrl.hashCode());
		} else {
			mDownloadNotificationId = 0;
		}
		return mDownloadNotificationId;
	}

	public void setDownloadNotificationId(int notificationId) {

		mDownloadNotificationId = notificationId;
	}

	public int getErrorCode() {
		return mErrorCode;
	}

	public void setErrorCode(int errorCode) {

		mErrorCode = errorCode;
	}

	public String getDownloadUrl() {
		return mDownloadUrl == null ? "" : mDownloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {

		mDownloadUrl = downloadUrl;
	}

	public int getDownloadId() {
		return mDownloadId;
	}

	public void setDownloadId(int downloadId) {

		mDownloadId = downloadId;
	}

	public long getDownloadAlreadySize() {
		return mDownloadAlreadySize;
	}

	/**
	 * 
	 * 返回百分比
	 * 
	 * @return int
	 */
	public int getDownloadPercentNum() {
		int percentNum = 0;
		if (mDownloadTotalSize > 0) {
			percentNum = (int) (((double) mDownloadAlreadySize / mDownloadTotalSize) * 100);
		}
		return percentNum;
	}

	public void setDownloadAlreadySize(long downloadAlreadySize) {

		mDownloadAlreadySize = downloadAlreadySize;
	}

	public long getDownloadTotalSize() {
		return mDownloadTotalSize;
	}

	public void setDownloadTotalSize(long downloadTotalSize) {

		mDownloadTotalSize = downloadTotalSize;
	}

	public String getDownloadFileName() {
		return mDownloadFileName == null ? Math
				.abs(getDownloadUrl().hashCode()) + "" : mDownloadFileName;
	}

	public void setDownloadFileName(String downloadFileName) {

		mDownloadFileName = downloadFileName;
	}

	public String getDownloadStoragePath() {
		return mDownloadStoragePath == null ? "" : mDownloadStoragePath;
	}

	public void setDownloadStoragePath(String downloadStoragePath) {

		mDownloadStoragePath = downloadStoragePath;
	}

	public Notification getDownloadRunningNotification() {
		// L.v("download", "getDownloadNotification", "mDownloadNotification="
		// + mDownloadNotification);
		return mDownloadRunningNotification;
	}

	public Notification getDownloadFinishNotification() {
		// L.v("download", "getDownloadNotification", "mDownloadNotification="
		// + mDownloadNotification);
		return mDownloadFinishNotification;
	}

	public void setDownloadNotification(Context context) {
		if (mDownloadRunningNotification == null) {
			mDownloadRunningNotification = getRunningNotification(context);

		}
		if (mDownloadFinishNotification == null) {
			mDownloadFinishNotification = getFinishNotification(context);

		}
		if (mDownloadImageUrl != null && !mDownloadImageUrl.equals("")) {
			// 测试
			getBitmapByUrl(mDownloadImageUrl);
		}
		// L.v("download", "setDownloadNotification", "mDownloadNotification="
		// + mDownloadNotification);
	}

	public int getDownloadStateType() {
		return mDownloadStateType;
	}

	public void setDownloadStateType(int downloadStateType) {

		mDownloadStateType = downloadStateType;
	}

	/**
	 * 创建运行通知
	 */
	private Notification getRunningNotification(Context context) {

		Notification notification = new Notification();
		notification.icon = android.R.drawable.stat_sys_download;
		notification.when = System.currentTimeMillis();
		notification.defaults = Notification.DEFAULT_LIGHTS;
		notification.flags = Notification.FLAG_NO_CLEAR;
		Intent intent = new Intent(context, context.getClass());
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.contentIntent = contentIntent;
		notification.tickerText = mDownloadFileName;
		RemoteViews removeViews = new RemoteViews(context.getPackageName(),
				R.layout.update);
		notification.contentView = removeViews;
		return notification;
	}

	/**
	 * 创建已完成通知
	 */
	private Notification getFinishNotification(Context context) {

		Notification notification = new Notification();
		notification.icon = android.R.drawable.stat_sys_download;
		notification.when = System.currentTimeMillis();
		notification.defaults = Notification.DEFAULT_LIGHTS;
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		Intent intent = new Intent(context, context.getClass());
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.contentIntent = contentIntent;
		notification.tickerText = mDownloadFileName;
		RemoteViews removeViews = new RemoteViews(context.getPackageName(),
				R.layout.outside_notification_view);
		removeViews.setTextViewText(R.id.notification_title_tv,
				mDownloadFileName);
		removeViews
				.setTextViewText(R.id.notification_content_tv, mDownloadDesc);
		notification.contentView = removeViews;
		return notification;
	}

	/**
	 * 
	 * 根据url获取bitmap
	 * 
	 * @param imageUrl
	 *            图片url
	 * @return void
	 */
	private void getBitmapByUrl(final String imageUrl) {

		new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					URL url = new URL(imageUrl);

					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setDoInput(true);
					conn.connect();
					InputStream inputStream = conn.getInputStream();
					final Bitmap bitmap = BitmapFactory
							.decodeStream(inputStream);
					L.v(TAG, "getBitmapByUrl", "bitmap=" + bitmap
							+ " imageUrl=" + imageUrl);
					if (bitmap != null) {
						mDownloadRunningNotification.contentView
								.setImageViewBitmap(R.id.ivLogo, bitmap);
					}

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				}
			}
		}).start();

	}

	/**
	 * 
	 * 根据url获取apk版本名称
	 * 
	 * @param url
	 *            下载地址
	 * @return String 版本名称
	 */
	public String getDownloadVersionNameByUrl(String url, String defaultName) {
		String versionName = defaultName;
		try {
			URL httpUrl = new URL(url);
			HttpURLConnection urlConnection = (HttpURLConnection) httpUrl
					.openConnection();
			urlConnection.getResponseCode();
			URL absUrl = urlConnection.getURL();
			if (absUrl != null && !absUrl.equals("")) {
				String filePath = absUrl.getPath();
				if (filePath.contains(".apk")) {
					versionName = filePath.substring(
							filePath.lastIndexOf("/") + 1,
							filePath.lastIndexOf(".apk"));
				} else {
					L.w(TAG, "getDownloadVersionNameByUrl",
							"no contains absUrl=" + absUrl);
				}
			} else {
				L.w(TAG, "getDownloadUrlByUrl", "absUrl=" + absUrl + " url="
						+ url);
			}
			urlConnection.disconnect();
		} catch (MalformedURLException e) {
			//java.net.MalformedURLException: Protocol not found:
			if (e != null) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			if (e != null) {
				e.printStackTrace();
			}

		}
		return versionName;
	}

	/**
	 * 
	 * 根据url获取真实URL
	 * 
	 * @param url
	 *            下载地址
	 * @return String 真是URL
	 */
	public String getDownloadUrlByUrl(String url) {
		String downloadUrl = url;
		try {
			URL httpUrl = new URL(url);
			HttpURLConnection urlConnection = (HttpURLConnection) httpUrl
					.openConnection();
			urlConnection.getResponseCode();
			URL absUrl = urlConnection.getURL();
			if (absUrl != null && !absUrl.equals("")) {
				downloadUrl = absUrl.toString();
			} else {
				L.w(TAG, "getDownloadUrlByUrl", "absUrl=" + absUrl + " url="
						+ url);
			}
			urlConnection.disconnect();
		} catch (MalformedURLException e) {
			if (e != null) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			if (e != null) {
				e.printStackTrace();
			}

		}
		return downloadUrl;
	}
}
