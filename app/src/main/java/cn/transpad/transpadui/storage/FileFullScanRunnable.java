package cn.transpad.transpadui.storage;

import android.content.Context;
import android.os.Message;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.util.ExtensionUtil;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.SystemUtil;
import de.greenrobot.event.EventBus;

/**
 * 完整扫描
 *
 * @author wangyang
 * @since 2014年4月23日
 */
public class FileFullScanRunnable implements Runnable {
    private static final String TAG = "FileFullScanRunnable";
    // 文件集合
    private ArrayList<MediaFile> mMediaFileList = new ArrayList<MediaFile>();
    private ArrayList<MediaFile> mImageMediaFileList = new ArrayList<MediaFile>();
    private Context mContext = null;
    private boolean mIsStop = false;
    private long mTotalFileNum = 0;
    private long mMediaFileNum = 0;
    private boolean mIsFirstStart;

    FileFullScanRunnable(Context context) {
        mContext = context;
    }

    @Override
    public void run() {
        try {
            mIsFirstStart = SharedPreferenceModule.getInstance().getBoolean("is_first_start", true);
            // 扫描开始
            Message message = new Message();
            message.what = StorageModule.MSG_ACTION_SCANNER_STARTED;
            EventBus.getDefault().post(message);

            // 获取挂载的路径
            ArrayList<String> rootDrive = SystemUtil.getInstance()
                    .getAllStoragePath();

            if (rootDrive != null) {
                v("run", "rootDrive.toArray()=" + rootDrive);

                // append '/' at the tail
                for (int i = 0; i < rootDrive.size(); i++) {
                    String str = rootDrive.get(i);
                    if (!str.endsWith("/"))
                        rootDrive.set(i, str + "/");
                }

                // check whether one path is a subpath of another
                for (int i = 0; i < rootDrive.size() - 1; i++) {
                    String str1 = rootDrive.get(i);
                    for (int k = i + 1; k < rootDrive.size(); k++) {
                        String str2 = rootDrive.get(k);

                        // 检测目录是否相同
                        if (str1.equals(str2)) {
                            rootDrive.remove(k);
                            k--; // retry the position
                            continue;
                        }

                        // 检测目录是否互相包含
//                        if (str1.startsWith(str2)) {
//                            // [i] is subpath of [k], remove [i]
//                            rootDrive.remove(i);
//                            i--; // retry the postion
//                            break; // break from the inner loop
//                        } else if (str2.startsWith(str1)) {
//                            // [k] is subpath of [i], remove [k]
//                            rootDrive.remove(k);
//                            k--; // retry the position
//                            continue;
//                        }
                    }
                }

                // scan these folders
                for (int i = 0; i < rootDrive.size(); i++) {
                    // fix bug for MOTO XT910
                    String homeStr = rootDrive.get(i);
                    String[] homeArry = homeStr.split(":");
                    File home = new File(homeArry[0]);
                    if (home != null) {
                        v("run", "home:" + home);
                        searchMdeiaFile(home);
                    }
                }
            }

            //100tv视频
            ArrayList<MediaFile> mediaFileList = MediaFileConfigManager.getInstance().get100TVViewFileList();
            mMediaFileList.addAll(mediaFileList);
            v("run", "mMediaFileList.size=" + mMediaFileList.size() + " mediaFileList.size=" + mediaFileList.size());
            if (!mIsStop) {

                // 入库
                FileDataBaseAdapter.getInstance().addFullMediaFileList(
                        mMediaFileList);
                FileDataBaseAdapter.getInstance().addFastMediaFileList(
                        mImageMediaFileList);
                // 扫描完成
                message.what = StorageModule.MSG_ACTION_SCANNER_FINISHED;
                EventBus.getDefault().post(message);
            }

            mIsStop = false;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        mIsStop = true;
    }

    /**
     * 搜索文件
     *
     * @param rootFile 根目录
     * @return void
     */
    private void searchMdeiaFile(File rootFile) {
        try {

            if (rootFile == null
                    || (rootFile != null && rootFile.listFiles() == null)) {
                L.w(TAG, "searchMdeiaFile", "rootFile=null");
                return;
            }
            File[] fileArray = rootFile.listFiles();
            for (File file : fileArray) {
                if (mIsStop) {
                    // 停止扫描
                    e("searchMdeiaFile", "stop", null);
                    Message message = new Message();
                    message.what = StorageModule.MSG_ACTION_SCANNER_STOPPED;
                    EventBus.getDefault().post(message);
                    return;
                }

                // 判断文件是否存在
                if (file == null || !file.exists()) {
                    continue;
                }

                if (file.isDirectory()) {


                    //判断是否存在非法文件夹
                    if (!ExtensionUtil.getInstance().isFolderExtension(file.getName())) {
                        searchMdeiaFile(file);
                    }

                } else {

                    mTotalFileNum++;

                    // 读取文件信息
                    MediaFile mediaFile = getMediaFileByFile(file);
                    v("searchMdeiaFile", "mediaFile=" + mediaFile + " path=" + file.getAbsolutePath());
                    // 判断是否是媒体文件
                    if (mediaFile == null) {
                        continue;
                    }

                    switch (mediaFile.getMediaFileType()) {
                        case MediaFile.MEDIA_IMAGE_TYPE:
                            mImageMediaFileList.add(mediaFile);
                            break;
                        default:
                            mMediaFileNum++;
                            mMediaFileList.add(mediaFile);
                            break;
                    }


                    // 发送进度
                    sendProcessingMessage(mMediaFileNum, mTotalFileNum);
                }
            }
        } catch (Exception e) {
            e("searchMdeiaFile", "Exception", e);
            return;
        }
    }

    /**
     * 根据文件返回媒体文件对象
     *
     * @param file 文件对象
     * @return 媒体文件
     */
    public MediaFile getMediaFileByFile(File file) {

        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            return null;
        }
        String extension = fileName.substring(dotIndex + 1).toLowerCase(
                Locale.getDefault());
        String filePath = file.getAbsolutePath();
        MediaFile mediaFile = new MediaFile();
        mediaFile.setMediaFilePath(filePath);
        mediaFile.setMediaFileName(fileName.substring(0, dotIndex));

        if (ExtensionUtil.getInstance().isAudioExtension(extension)) {

            //判断连接中是否包含非法文件夹
            if (ExtensionUtil.getInstance().isFolderPath(filePath)) {
                v("getMediaFileByFile", "isFolderPath filePath=" + filePath);
                return null;
            }

            // 过滤文件大小,800k以下的视频忽略
            if ("ogg".equals(extension) && file.length() < StorageConfig.AUDIO_FILE_SIZE_FILTER_CONDITION) {
                v("getMediaFileByFile", "ogg length=" + file.length());
                return null;
            }

            // 匹配音频
            mediaFile.setMediaFileSize(file.length());
            mediaFile.setMediaFileDateModified(file.lastModified());
            mediaFile.setMediaFileDirectoryType(MediaFile.MEDIA_FILE_TYPE);
            mediaFile.setMediaFileType(MediaFile.MEDIA_AUDIO_TYPE);
        } else if (ExtensionUtil.getInstance().isVideoExtension(extension)) {
            v("getMediaFileByFile", "extension=" + extension);
            // 匹配视频
            // 过滤文件大小,1M以下的视频忽略
            if (file.length() < StorageConfig.VIDEO_FILE_SIZE_FILTER_CONDITION) {
                v("getMediaFileByFile", "VIDEO_FILE_SIZE_FILTER_CONDITION filePath=" + filePath);
                return null;
            }

            //判断连接中是否包含非法文件夹
            if (ExtensionUtil.getInstance().isFolderPath(filePath)) {
                v("getMediaFileByFile", "isFolderPath filePath=" + filePath);
                return null;
            }
            mediaFile.setMediaFileSize(file.length());
            mediaFile.setMediaFileDateModified(file.lastModified());
            mediaFile.setMediaFileDirectoryType(MediaFile.MEDIA_FILE_TYPE);
            mediaFile.setMediaFileType(MediaFile.MEDIA_VIDEO_TYPE);
        }
//        else if (ExtensionUtil.getInstance().isImageExtension(extension)) {
//            if (!filePath.toLowerCase().contains("dcim/camera")) {
//                return null;
//            }
//            mediaFile.setMediaFileDirectoryType(MediaFile.MEDIA_FILE_TYPE);
//            mediaFile.setMediaFileType(MediaFile.MEDIA_IMAGE_TYPE);
//        }
        else {
            return null;
        }
        return mediaFile;
    }

    /**
     * 判断路径是否在当前集合中重复
     *
     * @param mediaFolderList 路径集合
     * @param path            比对的路径
     * @return int 重复路径的索引,否则返回-1
     */
//    public int isContainsPath(ArrayList<MediaFolder> mediaFolderList,
//                              String path) {
//
//        if (mediaFolderList == null) {
//            L.e(TAG, "isPathInArray", "mediaFolderList is null");
//            return -1;
//        }
//
//        if (mediaFolderList.size() == 0) {
//            L.e(TAG, "isPathInArray", "mediaFolderList.size() == 1");
//            return -1;
//        }
//        int index = 0;
//        for (MediaFolder mediaFolder : mediaFolderList) {
//            if (mediaFolder.getMediaFolderPath().equals(path)) {
//                return index;
//            }
//            index++;
//        }
//
//        return -1;
//    }

    /**
     * 发送进度信息
     *
     * @param mediaFileNum 符合要求的文件数
     * @param totalFileNum 已搜索到的文件数
     * @return void
     */
    private void sendProcessingMessage(long mediaFileNum, long totalFileNum) {
        if (!mIsFirstStart) {
            Message message = new Message();
            message.what = StorageModule.MSG_ACTION_SCANNER_PROCESSING;
            String media = String.format(mContext.getResources()
                    .getString(R.string.search_mdeia_msg), mediaFileNum, totalFileNum);
            message.obj = media;
            EventBus.getDefault().post(message);
        }
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
    private void e(String type, String msg, Exception e) {
        if (false) {
            if (e != null) {
                L.e(TAG, type, msg, e);
            } else {
                L.e(TAG, type, msg);
            }
        }
    }
}
