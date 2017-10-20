package cn.transpad.transpadui.storage.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.Download;
import cn.transpad.transpadui.http.Reporter;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;

public class NotificationHandle {
	private static final String TAG = NotificationHandle.class.getSimpleName();
	private static final NotificationHandle mInstance = new NotificationHandle();;
	private static Context sContext = null;
	private static NotificationManager mNotificationManager = null;

	private NotificationHandle() {
	}

	public static void init(Context context) {
		sContext = context;
		if (mNotificationManager == null) {
			mNotificationManager = (NotificationManager) sContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
		}
	}

	public static NotificationHandle getInstance() {

		return mInstance;
	}

	/**
	 * 
	 * 更新进度
	 * 
	 * @param download
	 *            下载对象
	 * @return void
	 */
	public void showNotification(Download download) {

		if (download == null) {
			return;
		}

		Notification runningNotification = download
				.getDownloadRunningNotification();

		if (runningNotification != null
				&& runningNotification.contentView != null) {
			// 更新进度
			runningNotification.contentView.setTextViewText(R.id.down_title,
					download.getDownloadFileName());
			runningNotification.contentView.setTextViewText(R.id.tvProcess,
					"已下载" + download.getDownloadPercentNum() + "%");
			runningNotification.contentView.setProgressBar(R.id.pbDownload,
					100, download.getDownloadPercentNum(), false);
		}

		switch (download.getErrorCode()) {
		case Download.DOWNLOAD_SUCCESS:
			switch (download.getDownloadStateType()) {
			case Download.DOWNLOAD_STATE_FINISH:
				L.v("NotificationHandle", "showNotification",
						"DOWNLOAD_STATE_FINISH");
				// 上报
				switch (download.getDownloadReportType()) {
				case Download.DOWNLOAD_REPORT_UPDATE:
					// 成功下载升级包的次数
					Reporter.logEvent(Reporter.EventId.UPGRADE_DOWNLOAD);
					break;
				default:
					break;
				}

				// 是否安装
				if (download.getDownloadIsInstall()) {

					L.v(TAG, "showNotification", "already upgrade");
					installApp(download.getDownloadStoragePath());

				}

				// 是否显示notification
				if (download.getDownloadIsShowFinishNotification()
						&& runningNotification != null) {
					Notification finishNotification = download
							.getDownloadFinishNotification();
					runningNotification.flags = Notification.FLAG_AUTO_CANCEL;
					// 更新进度
					mNotificationManager.notify(
							download.getDownloadNotificationId(),
							runningNotification);
					// 替换通知
					mNotificationManager.notify(
							download.getDownloadNotificationId(),
							finishNotification);
				} else {

					// 取消通知
					mNotificationManager.cancel(download
							.getDownloadNotificationId());
				}

				break;
			default:

				L.v(TAG, "showNotification",
						"state=" + download.getDownloadStateType()
								+ " notification=" + runningNotification);
				if (download.getDownloadIsShowRunningNotification()
						&& runningNotification != null) {
					L.v(TAG, "DownloadStateType",
							"mNotificationManager.notify NotificationId="
									+ download.getDownloadNotificationId());
					mNotificationManager.notify(
							download.getDownloadNotificationId(),
							runningNotification);
				}

				break;
			}
			break;
		default:
			L.v(TAG, "showNotification",
					"name=" + download.getDownloadFileName() + " isErrorToast="
							+ download.getDownloadIsErrorToast()
							+ " errorCode=" + download.getErrorCode());
			// io或者url非法
			if (download.getDownloadIsErrorToast()) {
				TPUtil.showToast(download.getDownloadErrorMessage());
			} else {
				L.w(TAG, "showNotification",
						"errorMessage=" + download.getDownloadErrorMessage());
			}

			// 是否显示notification
			if (download.getDownloadIsShowRunningNotification()) {
				// 取消通知
				mNotificationManager.cancel(download
						.getDownloadNotificationId());
			}

			break;
		}

	}

	/**
	 * 安装下载后的apk文件
	 * 
	 * @param filePath
	 *            文件本地路径
	 * @return void
	 */
	public void installApp(String filePath) {
		File file = new File(filePath);

		if (file.exists()) {

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file),
					"application/vnd.android.package-archive");
			sContext.startActivity(intent);

		} else {

			TPUtil.showToast("安装文件不存在,请重新下载");

		}

	}

}
