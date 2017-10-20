package cn.transpad.transpadui.storage.download;

import com.fone.player.L;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolManager {
	private static final String TAG = ThreadPoolManager.class.getSimpleName();
	/**
	 * 离线缓存可同时运行的最大任务数
	 */
	private static final int OFFLINE_CACHE_MAX_DOWNLOAD_NUM = 2;
	/**
	 * 应用可同时运行的最大任务数
	 */
	private static final int APP_FILE_MAX_DOWNLOAD_NUM = 1;
	// 离线缓存下载线程池
	private ExecutorService mOfflineCacheService;
	// apk下载线程池
	private ExecutorService mAppFileService;

	private ThreadPoolManager() {
		// Returns the number of processors available to the virtual machine.
		// int num = Runtime.getRuntime().availableProcessors();
		mOfflineCacheService = Executors
				.newFixedThreadPool(OFFLINE_CACHE_MAX_DOWNLOAD_NUM);
		mAppFileService = Executors
				.newFixedThreadPool(APP_FILE_MAX_DOWNLOAD_NUM);
	}

	private static final ThreadPoolManager manager = new ThreadPoolManager();

	public static ThreadPoolManager getInstance() {
		return manager;
	}

	public void executeOfflineCacheRunnable(Runnable runnable) {
		L.v(TAG, "executeOfflineCacheRunnable", "start");
		mOfflineCacheService.execute(runnable);
	}

	public void executeAppFileRunnable(Runnable runnable) {
		L.v(TAG, "executeAppFileTask", "start");
		mAppFileService.execute(runnable);
	}
}
