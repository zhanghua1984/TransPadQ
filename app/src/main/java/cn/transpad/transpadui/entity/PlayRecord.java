package cn.transpad.transpadui.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 播放记录
 *
 * @author wangyang
 * @since 2015年5月7日
 */
public class PlayRecord implements Parcelable {
    /**
     * 视频播放记录
     */
    public static final int RECORD_PLAYER_VIDEO = 1;
    /**
     * 音频播放记录
     */
    public static final int RECORD_PLAYER_AUDIO = 2;
    // 播放记录条目id
    private long mPlayRecordId;
    // 播放记录类型(1视频播放记录;2音频播放记录)
    private int mPlayRecordType;
    // 节目播放地址
    private String mPlayRecordPlayUrl;
    // 节目播放地址
    private long mPlayRecordCid;
    // 节目已经播放的位置
    private long mPlayRecordAlreadyPlayTime;
    // 节目时长
    private long mPlayRecordTotalTime;
    // 播放记录创建的时间
    private long mPlayRecordCreateTime;

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(mPlayRecordId);
        out.writeInt(mPlayRecordType);
        out.writeString(mPlayRecordPlayUrl);
        out.writeLong(mPlayRecordAlreadyPlayTime);
        out.writeLong(mPlayRecordTotalTime);
        out.writeLong(mPlayRecordCreateTime);
        out.writeLong(mPlayRecordCid);
    }

    public static final Creator<PlayRecord> CREATOR = new Creator<PlayRecord>() {
        public PlayRecord createFromParcel(Parcel in) {
            return new PlayRecord(in);
        }

        public PlayRecord[] newArray(int size) {
            return new PlayRecord[size];
        }
    };

    private PlayRecord(Parcel in) {
        mPlayRecordId = in.readLong();
        mPlayRecordType = in.readInt();
        mPlayRecordPlayUrl = in.readString();
        mPlayRecordAlreadyPlayTime = in.readLong();
        mPlayRecordTotalTime = in.readLong();
        mPlayRecordCreateTime = in.readLong();
        mPlayRecordCid = in.readLong();
    }

    public long getPlayRecordId() {
        return mPlayRecordId;
    }

    public void setPlayRecordId(long playRecordId) {

        mPlayRecordId = playRecordId;
    }

    /**
     * 获取播放记录类型
     *
     * @return int 播放记录类型<br>
     * PlayRecord.RECORD_PLAYER_LOCAL 播放器-本地视频<br>
     * PlayRecord.RECORD_PLAYER_NETWORK 播放器-网络视频<br>
     * PlayRecord.RECORD_WEB_NETWORK 网页-网络视频<br>
     */
    public int getPlayRecordType() {
        return mPlayRecordType;
    }

    /**
     * 设置播放记录类型
     *
     * @return int 播放记录类型<br>
     * PlayRecord.RECORD_PLAYER_LOCAL 播放器-本地视频<br>
     * PlayRecord.RECORD_PLAYER_NETWORK 播放器-网络视频<br>
     * PlayRecord.RECORD_WEB_NETWORK 网页-网络视频<br>
     */
    public int setPlayRecordType(int playRecordType) {
        return mPlayRecordType;
    }

    public String getPlayRecordPlayUrl() {
        return mPlayRecordPlayUrl == null ? "" : mPlayRecordPlayUrl;
    }

    public void setPlayRecordPlayUrl(String playRecordPlayUrl) {

        mPlayRecordPlayUrl = playRecordPlayUrl;
    }

    public long getPlayRecordAlreadyPlayTime() {
        return mPlayRecordAlreadyPlayTime;
    }

    public void setPlayRecordAlreadyPlayTime(long playRecordAlreadyPlayTime) {

        mPlayRecordAlreadyPlayTime = playRecordAlreadyPlayTime;
    }

    public long getPlayRecordTotalTime() {
        return mPlayRecordTotalTime;
    }

    public void setPlayRecordTotalTime(long playRecordTotalTime) {

        mPlayRecordTotalTime = playRecordTotalTime;
    }

    public long getPlayRecordCreateTime() {
        return mPlayRecordCreateTime;
    }

    public void setPlayRecordCreateTime(long playRecordCreateTime) {

        mPlayRecordCreateTime = playRecordCreateTime;
    }

    public long getPlayRecordCid() {
        return mPlayRecordCid;
    }

    public void setPlayRecordCid(long mPlayRecordCid) {
        this.mPlayRecordCid = mPlayRecordCid;
    }

    public PlayRecord() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "PlayRecord{" +
                "mPlayRecordId=" + mPlayRecordId +
                ", mPlayRecordType=" + mPlayRecordType +
                ", mPlayRecordPlayUrl='" + mPlayRecordPlayUrl + '\'' +
                ", mPlayRecordCid=" + mPlayRecordCid +
                ", mPlayRecordAlreadyPlayTime=" + mPlayRecordAlreadyPlayTime +
                ", mPlayRecordTotalTime=" + mPlayRecordTotalTime +
                ", mPlayRecordCreateTime=" + mPlayRecordCreateTime +
                '}';
    }
}
