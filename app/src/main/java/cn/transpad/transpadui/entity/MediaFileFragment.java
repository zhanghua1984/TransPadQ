package cn.transpad.transpadui.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 缓存碎片实体类，仅用于离线缓存下载使用，属于OfflineCache子集
 *
 * @author wangyang
 * @since 2014年5月13日
 */
public class MediaFileFragment implements Parcelable {
    public static final String OFFLINE_CACHE_FRAGMENT = "offline_cache_fragment";
    public static final String OFFLINE_CACHE_FRAGMENT_LIST = "offline_cache_fragment_list";

    // 内容ID(第一层级)
    private long mMediaFileID;
    // 总时长
    private long mMediaFileDuration;
    // 缓存分片视频路径
    private String mMediaFileFragmentStoragePath;

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(mMediaFileID);
        out.writeLong(mMediaFileDuration);
        out.writeString(mMediaFileFragmentStoragePath);
    }

    public static final Creator<MediaFileFragment> CREATOR = new Creator<MediaFileFragment>() {
        public MediaFileFragment createFromParcel(Parcel in) {
            return new MediaFileFragment(in);
        }

        public MediaFileFragment[] newArray(int size) {
            return new MediaFileFragment[size];
        }
    };

    private MediaFileFragment(Parcel in) {
        mMediaFileID = in.readLong();
        mMediaFileDuration = in.readLong();
        mMediaFileFragmentStoragePath = in.readString();
    }

    public MediaFileFragment() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getMediaFileFragmentStoragePath() {
        return mMediaFileFragmentStoragePath;
    }

    public void setMediaFileFragmentStoragePath(String mediaFileFragmentStoragePath) {
        mMediaFileFragmentStoragePath = mediaFileFragmentStoragePath;
    }

    public long getMediaFileDuration() {
        return mMediaFileDuration;
    }

    public void setMediaFileDuration(long mediaFileDuration) {
        mMediaFileDuration = mediaFileDuration;
    }

    public long getMediaFileID() {
        return mMediaFileID;
    }

    public void setMediaFileID(long mediaFileID) {
        mMediaFileID = mediaFileID;
    }

}
