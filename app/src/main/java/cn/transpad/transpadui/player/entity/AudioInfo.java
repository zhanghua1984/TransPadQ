package cn.transpad.transpadui.player.entity;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kongxiaojun on 2015/1/20.
 * 音频信息
 */
public class AudioInfo implements Parcelable {

    public String path;

    /**
     * 媒体文件播长度
     */
    public int mediaDuration;

    /**
     * 比特率
     */
    public String bitRate;

    /**
     * 格式
     */
    public String format;

    /**
     * 声道
     */
    public String channels;

    /**
     * 采样率
     */
    public String sampleRate;

    /**
     * 编码类型
     */
    public String encodingType;

    /**
     * 歌手
     */
    public String artist;

    /**
     * 专辑
     */
    public String album;

    /**
     * 歌名
     */
    public String title;

    /**
     * 年份
     */
    public String year;

    /**
     * 图片
     */
    public Bitmap image;



    @Override
    public String toString() {
        return "AudioInfo{" +
                "path='" + path + '\'' +
                ", mediaDuration=" + mediaDuration +
                ", bitRate='" + bitRate + '\'' +
                ", format='" + format + '\'' +
                ", channels='" + channels + '\'' +
                ", sampleRate='" + sampleRate + '\'' +
                ", encodingType='" + encodingType + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", title='" + title + '\'' +
                ", year='" + year + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
        dest.writeInt(this.mediaDuration);
        dest.writeString(this.bitRate);
        dest.writeString(this.format);
        dest.writeString(this.channels);
        dest.writeString(this.sampleRate);
        dest.writeString(this.encodingType);
        dest.writeString(this.artist);
        dest.writeString(this.album);
        dest.writeString(this.title);
        dest.writeString(this.year);
    }

    public AudioInfo() {
    }

    private AudioInfo(Parcel in) {
        this.path = in.readString();
        this.mediaDuration = in.readInt();
        this.bitRate = in.readString();
        this.format = in.readString();
        this.channels = in.readString();
        this.sampleRate = in.readString();
        this.encodingType = in.readString();
        this.artist = in.readString();
        this.album = in.readString();
        this.title = in.readString();
        this.year = in.readString();
    }

    public static final Creator<AudioInfo> CREATOR = new Creator<AudioInfo>() {
        public AudioInfo createFromParcel(Parcel source) {
            return new AudioInfo(source);
        }

        public AudioInfo[] newArray(int size) {
            return new AudioInfo[size];
        }
    };
}
