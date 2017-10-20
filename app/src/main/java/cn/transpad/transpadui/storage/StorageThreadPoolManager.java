package cn.transpad.transpadui.storage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 * 任务线程池
 * 
 * @author wangyang
 * @since 2014年5月5日
 */
public class StorageThreadPoolManager {
	// 文件下载线程池
	private ExecutorService mFileService;

	private StorageThreadPoolManager() {
		// Returns the number of processors available to the virtual machine.
		int num = Runtime.getRuntime().availableProcessors();
		mFileService = Executors.newFixedThreadPool(3 * num);
	}

	private static final StorageThreadPoolManager manager = new StorageThreadPoolManager();

	public static StorageThreadPoolManager getInstance() {
		return manager;
	}

	public void addTask(Runnable runnable) {
		mFileService.execute(runnable);
	}

}
