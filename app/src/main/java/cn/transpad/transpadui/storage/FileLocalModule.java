package cn.transpad.transpadui.storage;

import android.content.Context;

/**
 * 本地文件模块
 *
 * @author wangyang
 */
final class FileLocalModule {
    private static Context sContext = null;
    private FileFullScanRunnable mFileFullScanRunnable = null;
    private FileFastScanRunnable mFileFastScanRunnable = null;
    private static final FileLocalModule mInstance = new FileLocalModule();

    static FileLocalModule getInstance() {
        return mInstance;
    }

    /**
     * 初始化{@link StorageModule}
     *
     * @param context 当前上下文
     */
    static void init(Context context) {

        if (context == null) {
            throw new NullPointerException("activity is null");
        }
        sContext = context;
        FileDataBaseAdapter.init(context);
    }

    /**
     * 扫描外部存储<br>
     * 异步方法
     *
     * @return void
     */

    void scanningExternalStorage() {
        // 开启任务
        mFileFullScanRunnable = new FileFullScanRunnable(sContext);
        StorageThreadPoolManager.getInstance().addTask(mFileFullScanRunnable);
    }


    /**
     * 扫描媒体库<br>
     * 异步方法
     *
     * @return void
     */
    void scanningMediaStore() {

        // 开启任务
        mFileFastScanRunnable = new FileFastScanRunnable(sContext);
        StorageThreadPoolManager.getInstance().addTask(mFileFastScanRunnable);

    }

    /**
     * 取消扫描(只支持全盘扫描,不包括系统自动扫描)<br>
     * 异步方法
     */
    void cancelScanning() {

        if (mFileFullScanRunnable != null) {
            mFileFullScanRunnable.cancel();
        }
        if (mFileFastScanRunnable != null) {
            mFileFastScanRunnable.cancel();
        }
    }

    /**
     * 根据文件路径删除文件 <br>
     * 同步方法
     *
     * @param filePath 文件路径
     * @return int 操作结果<br>
     * 1 成功<br>
     * -1 删除媒体库记录失败<br>
     * -2 删除异常<br>
     */
    int deleteFileByFilePath(String filePath) {
        return FileDataBaseAdapter.getInstance().deleteFileByFilePath(filePath);
    }

}
