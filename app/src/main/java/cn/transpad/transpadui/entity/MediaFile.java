package cn.transpad.transpadui.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * 媒体文件
 *
 * @author wangyang
 * @since 2014年4月30日
 */
public class MediaFile implements Parcelable {
    public static final String CURRENT_PLAY_URL = "current_play_URL";
    /**
     * 视频文件集合
     */
    public static final String MEDIA_VIDEO_FILE_LIST = "media_video_file_list";
    /**
     * 音频文件集合
     */
    public static final String MEDIA_AUDIO_FILE_LIST = "media_audio_file_list";
    /**
     * 图片文件集合
     */
    public static final String MEDIA_IMAGE_FILE_LIST = "media_image_file_list";
    /**
     * 文件夹类型
     */
    public static final int MEDIA_FOLDER_TYPE = 1;
    /**
     * 文件类型
     */
    public static final int MEDIA_FILE_TYPE = 2;
    /**
     * 文件夹视频类型
     */
    public static final int MEDIA_FOLDER_VIDEO_TYPE = 1;
    /**
     * 文件夹音频类型
     */
    public static final int MEDIA_FOLDER_AUDIO_TYPE = 2;
    /**
     * 文件夹自定义类型
     */
    public static final int MEDIA_FOLDER_CUSTOM_TYPE = 3;
    /**
     * 文件夹新建类型
     */
    public static final int MEDIA_FOLDER_NEW_TYPE = 4;

    /**
     * 视频类型
     */
    public static final int MEDIA_VIDEO_TYPE = 1;
    /**
     * 音频类型
     */
    public static final int MEDIA_AUDIO_TYPE = 2;
    /**
     * 图片类型
     */
    public static final int MEDIA_IMAGE_TYPE = 3;
    /**
     * 100tv视频类型
     */
    public static final int MEDIA_VIDEO_100TV_TYPE = 4;
    // 文件Id
    private long mMediaFileId;
    // 文件名称(无后缀)
    private String mMediaFileName;
    // 文件作者
    private String mMediaFileAuthor;
    //目录类型(1文件夹;2文件)
    private int mMediaFileDirectoryType;
    //文件夹类型
    private int mMediaFileFolderType;
    //文件类型(1视频;2音频;3图片;100tv视频)
    private int mMediaFileType;
    // 父路径
    private String mMediaFileParentName;
    // 文件绝对路径
    private String mMediaFilePath;
    // 文件原始路径(解除加密后恢复文件时使用)
    private String mMediaFileOriginalPath;
    // 文件更改时间
    private long mMediaFileDateModified;
    // 文件时长
    private long mMediaFileDuration;
    // 文件大小
    private long mMediaFileSize;
    // 是否加密
    private boolean mMediaFileIsEncrypt;
    // 是否显示选择框
    private boolean mMediaFileIsVisibleChoise;
    // 是否选中选择框
    private boolean mMediaFileIsCheckedChoise;
    // 是否正在播放
    private boolean mMediaFileIsPlaying;
    // 文件加密时的时间
    private long mMediaFileEncryptTime;
    /**
     * 播放位置
     */
    private int mMediaFileSeekPos;

    public List<MediaFileFragment> getMediaFileFragmentList() {
        return mMediaFileFragmentList;
    }

    public void addMediaFileFragment(MediaFileFragment mediaFileFragment) {
        mMediaFileFragmentList.add(mediaFileFragment);
    }
    public void setMediaFileFragment(List<MediaFileFragment> mediaFileFragmentList) {
        mMediaFileFragmentList.addAll(mediaFileFragmentList);
    }
    private List<MediaFileFragment> mMediaFileFragmentList = new ArrayList<>();

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mMediaFileId);
        dest.writeString(this.mMediaFileName);
        dest.writeInt(this.mMediaFileDirectoryType);
        dest.writeInt(this.mMediaFileFolderType);
        dest.writeInt(this.mMediaFileType);
        dest.writeString(this.mMediaFileParentName);
        dest.writeString(this.mMediaFilePath);
        dest.writeString(this.mMediaFileOriginalPath);
        dest.writeLong(this.mMediaFileDateModified);
        dest.writeLong(this.mMediaFileDuration);
        dest.writeLong(this.mMediaFileSize);
        dest.writeByte(mMediaFileIsEncrypt ? (byte) 1 : (byte) 0);
        dest.writeByte(mMediaFileIsVisibleChoise ? (byte) 1 : (byte) 0);
        dest.writeByte(mMediaFileIsCheckedChoise ? (byte) 1 : (byte) 0);
        dest.writeByte(mMediaFileIsPlaying ? (byte) 1 : (byte) 0);
        dest.writeLong(this.mMediaFileEncryptTime);
        dest.writeInt(this.mMediaFileSeekPos);
        int mediaFileCount = 0;
        if (mMediaFileFragmentList != null && mMediaFileFragmentList.size() > 0) {
            mediaFileCount = mMediaFileFragmentList.size();
            dest.writeInt(mediaFileCount);
            for (int i = 0; i < mediaFileCount; i++) {
                dest.writeParcelable(mMediaFileFragmentList.get(i), flags);
            }
        } else {
            dest.writeInt(mediaFileCount);
        }
    }

    private MediaFile(Parcel in) {
        this.mMediaFileId = in.readLong();
        this.mMediaFileName = in.readString();
        mMediaFileDirectoryType = in.readInt();
        mMediaFileFolderType = in.readInt();
        this.mMediaFileType = in.readInt();
        this.mMediaFileParentName = in.readString();
        this.mMediaFilePath = in.readString();
        mMediaFileOriginalPath = in.readString();
        this.mMediaFileDateModified = in.readLong();
        mMediaFileDuration = in.readLong();
        this.mMediaFileSize = in.readLong();
        this.mMediaFileIsEncrypt = in.readByte() != 0;
        this.mMediaFileIsVisibleChoise = in.readByte() != 0;
        this.mMediaFileIsCheckedChoise = in.readByte() != 0;
        this.mMediaFileIsPlaying = in.readByte() != 0;
        this.mMediaFileEncryptTime = in.readLong();
        this.mMediaFileSeekPos = in.readInt();
        int meidaFileCount = in.readInt();
        if (meidaFileCount > 0) {
            mMediaFileFragmentList = new ArrayList<MediaFileFragment>();
            MediaFileFragment mediaFile = null;
            for (int i = 0; i < meidaFileCount; i++) {
                mediaFile = in.readParcelable(MediaFileFragment.class.getClassLoader());
                mMediaFileFragmentList.add(mediaFile);
            }
        } else {
            mMediaFileFragmentList = null;
        }
    }

    public int getMediaFileFolderType() {
        return mMediaFileFolderType;
    }

    public void setMediaFileFolderType(int mediaFileFolderType) {
        mMediaFileFolderType = mediaFileFolderType;
    }

    public long getMediaFileDuration() {
        return mMediaFileDuration;
    }

    public void setMediaFileDuration(long mediaFileDuration) {
        mMediaFileDuration = mediaFileDuration;
    }

    public String getMediaFileOriginalPath() {
        return mMediaFileOriginalPath;
    }

    public void setMediaFileOriginalPath(String mediaFileOriginalPath) {
        mMediaFileOriginalPath = mediaFileOriginalPath;
    }

    public int getMediaFileDirectoryType() {
        return mMediaFileDirectoryType;
    }

    public void setMediaFileDirectoryType(int mediaFileDirectoryType) {
        this.mMediaFileDirectoryType = mediaFileDirectoryType;
    }

    public int getMediaFileType() {
        return mMediaFileType;
    }

    public void setMediaFileType(int mediaFileType) {
        mMediaFileType = mediaFileType;
    }

    public String getMediaFileAuthor() {
        return mMediaFileAuthor;
    }

    public void setMediaFileAuthor(String mediaFileAuthor) {
        mMediaFileAuthor = mediaFileAuthor;
    }

    /**
     * 获取文件加密时的时间
     *
     * @return long 文件加密时的时间
     */
    public long getMediaFileEncryptTime() {
        return mMediaFileEncryptTime;
    }

    /**
     * 设置文件加密时的时间
     *
     * @param mediaFileEncryptTime 文件加密时的时间
     * @return void
     */
    public void setMediaFileEncryptTime(long mediaFileEncryptTime) {

        mMediaFileEncryptTime = mediaFileEncryptTime;
    }

    /**
     * 获取加密状态
     *
     * @return boolean 加密状态<br>
     * true 加密<br>
     * false 未加密
     */
    public boolean getMediaFileIsEncrypt() {
        return mMediaFileIsEncrypt;
    }

    /**
     * 设置加密状态
     *
     * @param isEncrypt 加密状态.true-加密, false-未加密.
     * @return void
     */
    public void setMediaFileIsEncrypt(boolean isEncrypt) {

        mMediaFileIsEncrypt = isEncrypt;
    }

    /**
     * 获取文件大小
     *
     * @return long 文件大小,单位是字节
     */
    public long getMediaFileSize() {
        return mMediaFileSize;
    }

    /**
     * 设置文件大小
     *
     * @param mediaFileSize 文件大小
     * @return void
     */
    public void setMediaFileSize(long mediaFileSize) {

        mMediaFileSize = mediaFileSize;
    }

    /**
     * 获取文件ID
     *
     * @return long 媒体库文件ID
     */
    public long getMediaFileId() {
        return mMediaFileId;
    }

    /**
     * 设置文件ID
     *
     * @param mediaFileId 媒体库文件ID
     * @return void
     */
    public void setMediaFileId(long mediaFileId) {

        mMediaFileId = mediaFileId;
    }

    /**
     * 获取文件名称(不带后缀名)
     *
     * @return String 文件名
     */
    public String getMediaFileName() {
        return mMediaFileName == null ? "" : mMediaFileName;
    }

    /**
     * 设置文件名称(不带后缀名)
     *
     * @param mediaFileName 文件名
     * @return void
     */
    public void setMediaFileName(String mediaFileName) {

        mMediaFileName = mediaFileName;
    }

    /**
     * 获取文件父文件夹名称
     *
     * @return String 父文件夹名称
     */
    public String getMediaFileParentName() {
        return mMediaFileParentName == null ? "" : mMediaFileParentName;
    }

    /**
     * 设置文件父文件夹名称
     *
     * @param mediaFileParentName 父文件夹名称
     * @return void
     */
    public void setMediaFileParentName(String mediaFileParentName) {

        mMediaFileParentName = mediaFileParentName;
    }

    /**
     * 获取文件路径
     *
     * @return String 文件路径
     */
    public String getMediaFilePath() {
        return mMediaFilePath == null ? "" : mMediaFilePath;
    }

    /**
     * 设置文件路径
     *
     * @param mediaFilePath 文件路径
     * @return void
     */
    public void setMediaFilePath(String mediaFilePath) {

        mMediaFilePath = mediaFilePath;
    }

    /**
     * 获取文件最后更改时间
     *
     * @return long 最后的更改时间
     */
    public long getMediaFileDateModified() {
        return mMediaFileDateModified;
    }

    /**
     * 设置文件最后更改时间
     *
     * @param mediaFileDateModified 最后更改时间
     * @return void
     */
    public void setMediaFileDateModified(long mediaFileDateModified) {

        mMediaFileDateModified = mediaFileDateModified;
    }

    public int getMediaFileSeekPos() {
        return mMediaFileSeekPos;
    }

    public void setMediaFileSeekPos(int mMediaFileSeekPos) {
        this.mMediaFileSeekPos = mMediaFileSeekPos;
    }

    /**
     * 获取url数组
     *
     * @return String[]
     */
    public String[] getMediaFileFragmentUrlArray() {
        String[] urlArray = new String[mMediaFileFragmentList.size()];
        int i = 0;
        for (MediaFileFragment mediaFileFragment : mMediaFileFragmentList) {
            String path = mediaFileFragment.getMediaFileFragmentStoragePath();
            urlArray[i] = path;
            i++;
        }
        return urlArray;
    }

    /**
     * 获取时长数组
     *
     * @return String[]
     */
    public int[] getMediaFileFragmentDurationArray() {
        int[] durationArray = new int[mMediaFileFragmentList.size()];
        int i = 0;
        for (MediaFileFragment mediaFileFragment : mMediaFileFragmentList) {
            int duration = (int) mediaFileFragment.getMediaFileDuration();
            durationArray[i] = duration;
            i++;
        }
        return durationArray;
    }

    public MediaFile() {

    }

    public boolean getMediaFileIsCheckedChoise() {
        return mMediaFileIsCheckedChoise;
    }

    public void setMediaFileIsCheckedChoise(boolean mediaFileIsCheckedChoise) {
        mMediaFileIsCheckedChoise = mediaFileIsCheckedChoise;
    }

    public boolean getMediaFileIsPlaying() {
        return mMediaFileIsPlaying;
    }

    public void setMediaFileIsPlaying(boolean mMediaFileIsPlaying) {
        this.mMediaFileIsPlaying = mMediaFileIsPlaying;
    }

    public boolean getMediaFileIsVisibleChoise() {
        return mMediaFileIsVisibleChoise;
    }

    public void setMediaFileIsVisibleChoise(boolean mediaFileIsVisibleChoise) {
        mMediaFileIsVisibleChoise = mediaFileIsVisibleChoise;
    }

    /**
     * 根据MediaFileList返回MultipleVideoList
     *
     * @param mediaFileList 媒体文件列表
     * @return MultipleVideoList
     */
    public static ArrayList<MultipleVideo> parseMultipleVideoList(List<MediaFile> mediaFileList) {
        ArrayList<MultipleVideo> multipleVideoList = new ArrayList<>();
        if (mediaFileList != null) {
            for (MediaFile mediaFile : mediaFileList) {
                MultipleVideo multipleVideo = new MultipleVideo();
                switch (mediaFile.getMediaFileType()) {
                    case MediaFile.MEDIA_VIDEO_TYPE:
                        multipleVideo.setName(mediaFile.getMediaFileName());
                        int[] durationArray = new int[1];
                        durationArray[0] = (int) mediaFile.getMediaFileDuration();
                        multipleVideo.setDurations(durationArray);
                        String[] urlArray = new String[1];
                        urlArray[0] = mediaFile.getMediaFilePath();
                        multipleVideo.setUrls(urlArray);
                        multipleVideoList.add(multipleVideo);
                        break;
                    case MediaFile.MEDIA_VIDEO_100TV_TYPE:
                        multipleVideo.setName(mediaFile.getMediaFileName());
                        multipleVideo.setDurations(mediaFile.getMediaFileFragmentDurationArray());
                        multipleVideo.setUrls(mediaFile.getMediaFileFragmentUrlArray());
                        multipleVideoList.add(multipleVideo);
                        break;
                }
            }
        }

        return multipleVideoList;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public static final Creator<MediaFile> CREATOR = new Creator<MediaFile>() {
        public MediaFile createFromParcel(Parcel source) {
            return new MediaFile(source);
        }

        public MediaFile[] newArray(int size) {
            return new MediaFile[size];
        }
    };
}
