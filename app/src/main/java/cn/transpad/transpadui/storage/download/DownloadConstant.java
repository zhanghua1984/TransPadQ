package cn.transpad.transpadui.storage.download;

public class DownloadConstant {

	/**
	 * 下载的重试次数
	 */
	public static final int CACHE_DOWNLOAD_RETRY_NUM = 3;

	public static boolean isDownloading = false;

	/**
	 * Max count of the download task.
	 */
	public static final int MAX_DOWNLOAD_NUM = 100;

	public static final int DOWNLOAD_STATUS_ADD_ONLINE = -2; // 边播边下
	// public static final int DOWNLOAD_STATUS_NOT_START = -1;
	public static final int DOWNLOAD_STATUS_USER_PAUSED = -1;
	public static final int DOWNLOAD_STATUS_RUNNING = 1;
	public static final int DOWNLOAD_STATUS_WAIT = 0;
	public static final int DOWNLOAD_STATUS_FINISHED = 2;
	public static final int DOWNLOAD_STATUS_PAUSED = 3;
	public static final int DOWNLOAD_STATUS_FAILED = 4;
	public static final int DOWNLOAD_STATUS_QWAIT = 6;

	/**
	 * 重试刷url
	 */
	public static final int CACHE_DOWLOAD_REFRESH_URL_NUM = 3;

}
