package cn.transpad.transpadui.storage;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Message;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.util.ExtensionUtil;
import cn.transpad.transpadui.util.L;
import de.greenrobot.event.EventBus;

/**
 * 文件夹扫描,读取媒体库中的文件,把文件归类到相应文件夹
 *
 * @author wangyang
 * @since 2014年4月23日
 */
public class FileFastScanRunnable implements Runnable {
    public static final String TAG = FileFastScanRunnable.class.getSimpleName();
    private Context mContext = null;
    // 文件集合
    private ArrayList<MediaFile> mMediaFileList = new ArrayList<MediaFile>();
    private boolean mIsStop = false;

    FileFastScanRunnable(Context context) {
        mContext = context;
    }

    @Override
    public void run() {
        try {
            // 扫描开始
            Message message = new Message();
            message.what = StorageModule.MSG_ACTION_SCANNER_STARTED;
            EventBus.getDefault().post(message);

            getFileListByMediaStore(MediaFile.MEDIA_AUDIO_TYPE);

            getFileListByMediaStore(MediaFile.MEDIA_VIDEO_TYPE);

            getFileListByMediaStore(MediaFile.MEDIA_IMAGE_TYPE);

            // 扫描完成
            if (mMediaFileList.size() > 0) {

                // 入库
                FileDataBaseAdapter.getInstance().addFastMediaFileList(
                        mMediaFileList);
                // 通知
                //  message.what = StorageModule.MSG_ACTION_SCANNER_FINISHED;
                // EventBus.getDefault().post(message);

            } else {
                L.e(TAG, "run", "not find any media file in sdcard!");
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        mIsStop = true;
    }

    /**
     * 将视频归类到文件夹
     *
     * @param mediaFileType 文件类型
     * @return ArrayList<MediaFolder> 视频文件夹集合
     */
    public void getFileListByMediaStore(int mediaFileType) {
        // MediaStore.Video.Media.DATA：视频文件路径；
        // MediaStore.Video.Media.DISPLAY_NAME : 视频文件名，如 testVideo.mp4
        // MediaStore.Video.Media.TITLE: 视频标题 : testVideo
        Uri uri = null;
        switch (mediaFileType) {
            case MediaFile.MEDIA_AUDIO_TYPE:
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                break;
            case MediaFile.MEDIA_VIDEO_TYPE:
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;
            case MediaFile.MEDIA_IMAGE_TYPE:
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
        }

        Cursor cursor = mContext.getContentResolver().query(
                uri, null, null, null,
                null);

        if (cursor == null) {
            L.e(TAG, "getFileListByMediaStore", "cursor == null");
            return;
        }

//        L.v(TAG, "getFileListByMediaStore",
//                "cursor.count:" + cursor.getCount() + " StorageState: "
//                        + Environment.getExternalStorageState());

        while (cursor.moveToNext()) {

            if (mIsStop) {
                // 停止
                Message message = new Message();
                message.what = StorageModule.MSG_ACTION_SCANNER_STOPPED;
                EventBus.getDefault().post(message);
                return;
            }

            String url = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            // L.v(TAG, "getFileListByMediaStore", "url=" + url + " type=" + mediaFileType);
            File file = new File(url);
            if (!file.exists()) {
                L.e(TAG, "getFileListByMediaStore", url + " no exists!");
                continue;
            }
            // 读取文件信息
            MediaFile mediaFile = getMediaFileByCursor(cursor, mediaFileType);
            if (mediaFile == null) {
                L.e(TAG, "getFileListByMediaStore", "mediaFile=null");
                continue;
            }
            // L.v(TAG, "getFileListByMediaStore", "name=" + mediaFile.getMediaFileName() + " type=" + mediaFileType);
            switch (mediaFileType) {
                case MediaFile.MEDIA_AUDIO_TYPE:
                    mediaFile.setMediaFileDirectoryType(MediaFile.MEDIA_FILE_TYPE);
                    mediaFile.setMediaFileType(MediaFile.MEDIA_AUDIO_TYPE);
                    break;
                case MediaFile.MEDIA_VIDEO_TYPE:
                    mediaFile.setMediaFileDirectoryType(MediaFile.MEDIA_FILE_TYPE);
                    mediaFile.setMediaFileType(MediaFile.MEDIA_VIDEO_TYPE);
                    break;
                case MediaFile.MEDIA_IMAGE_TYPE:
                    mediaFile.setMediaFileDirectoryType(MediaFile.MEDIA_FILE_TYPE);
                    mediaFile.setMediaFileType(MediaFile.MEDIA_IMAGE_TYPE);
                    break;
            }

            mMediaFileList.add(mediaFile);
            // 发送进度
            //sendProcessingMessage(mMediaFileNum, mTotalFileNum);
        }

        if (cursor != null) {
            cursor.close();
        }

    }

    /**
     * 发送进度信息
     *
     * @param mediaFileNum 符合要求的文件数
     * @param totalFileNum 已搜索到的文件数
     * @return void
     */
    private void sendProcessingMessage(long mediaFileNum, long totalFileNum) {
        Message message = new Message();
        message.what = StorageModule.MSG_ACTION_SCANNER_PROCESSING;
        StringBuffer stringBuffer = new StringBuffer(mContext.getResources()
                .getString(R.string.search_mdeia_msg))
                .append(mediaFileNum)
                .append(File.separator)
                .append(totalFileNum)
                .append(mContext.getResources().getString(
                        R.string.search_cancel_msg));
        L.v(TAG, stringBuffer.toString());
        message.obj = stringBuffer.toString();
        EventBus.getDefault().post(message);
    }

    /**
     * 根据游标读取媒体文件对象
     *
     * @param cursor 游标
     * @return MediaFile 媒体文件对象
     */
    private MediaFile getMediaFileByCursor(Cursor cursor, int mediaFileType) {
        String filePath = cursor.getString(cursor
                .getColumnIndex(MediaStore.MediaColumns.DATA));
        //L.v(TAG, "getMediaFileByCursor", "filePath=" + filePath);
        File file = new File(filePath);
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            L.v(TAG, "getMediaFileByCursor", "dotIndex=-1 filePath=" + filePath);
            return null;
        }
        String extension = fileName.substring(dotIndex + 1).toLowerCase(
                Locale.getDefault());

        switch (mediaFileType) {
            case MediaFile.MEDIA_AUDIO_TYPE:
                // 匹配视频
                if (!ExtensionUtil.getInstance().isAudioExtension(extension)) {
                    return null;
                }

                //判断连接中是否包含非法文件夹
                if (ExtensionUtil.getInstance().isFolderPath(filePath)) {
                    L.v(TAG, "getMediaFileByCursor", "isFolderPath filePath=" + filePath);
                    return null;
                }

                // 过滤文件大小,800k以下的视频忽略
                if ("ogg".equals(extension) && file.length() < StorageConfig.AUDIO_FILE_SIZE_FILTER_CONDITION) {
                    L.v(TAG, "getMediaFileByCursor", "ogg length=" + file.length());
                    return null;
                }
                break;
            case MediaFile.MEDIA_VIDEO_TYPE:
                if (!ExtensionUtil.getInstance().isVideoExtension(extension)) {
                    L.v("getMediaFileByCursor", "isVideoExtension filePath=" + filePath);
                    return null;
                }
                // 非自拍视频进行文件大小过滤,自拍视频忽略大小比对
                // 过滤文件大小,1M以下的视频忽略
                if (file.length() < StorageConfig.VIDEO_FILE_SIZE_FILTER_CONDITION) {
                    L.v(TAG, "getMediaFileByCursor", "VIDEO_FILE_SIZE_FILTER_CONDITION filePath=" + filePath);
                    return null;
                }
                //判断连接中是否包含非法文件夹
                if (ExtensionUtil.getInstance().isFolderPath(filePath)) {
                    L.v(TAG, "getMediaFileByCursor", "isFolderPath filePath=" + filePath);
                    return null;
                }
                break;
            case MediaFile.MEDIA_IMAGE_TYPE:
                if (!filePath.toLowerCase().contains("dcim")) {
                    return null;
                }
                break;
        }
        MediaFile mediaFile = new MediaFile();
        mediaFile.setMediaFilePath(filePath);
        String title = cursor.getString(cursor
                .getColumnIndex(MediaStore.Video.Media.TITLE));
        mediaFile.setMediaFileName(title);
        switch (mediaFileType) {
            case MediaFile.MEDIA_AUDIO_TYPE:
                long size = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Audio.Media.SIZE));
                mediaFile.setMediaFileSize(size);
                mediaFile.setMediaFileDateModified(file.lastModified());
                long duration = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Video.Media.DURATION));
                mediaFile.setMediaFileDuration(duration);
//                String artist = cursor.getString(cursor
//                        .getColumnIndex(MediaStore.Audio.Media.ARTIST));
//                mediaFile.setMediaFileAuthor(artist);
                // L.v(TAG, "getMediaFileByCursor", "artist=" + artist);
//                if (artist == null || artist.equals("") || artist.equals("null")) {
//                    AudioInfo audioInfo = new AudioInfo();
//                    audioInfo.path = mediaFile.getMediaFilePath();
//                    PlayerUtil.readAudioHeader(mContext, audioInfo);
//                    mediaFile.setMediaFileAuthor(audioInfo.artist);
//                    //L.v(TAG, "getMediaFileByCursor", "audioInfo artist=" + artist);
//                }
                break;
            case MediaFile.MEDIA_VIDEO_TYPE:
                size = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Video.Media.SIZE));
                mediaFile.setMediaFileSize(size);
//                artist = cursor.getString(cursor
//                        .getColumnIndex(MediaStore.Video.Media.ARTIST));
//                mediaFile.setMediaFileAuthor(artist);
                mediaFile.setMediaFileDateModified(file.lastModified());
                duration = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Video.Media.DURATION));
                mediaFile.setMediaFileDuration(duration);
                //L.v(TAG, "getMediaFileByCursor", "duration=" + duration);
                break;

        }
        return mediaFile;
    }

}
