package cn.transpad.transpadui.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kongxiaojun on 2015/5/6.
 * 多片视频
 */
public class MultipleVideo implements Parcelable {
    String name;
    String[] urls;
    int[] durations;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getUrls() {
        return urls;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    public int[] getDurations() {
        return durations;
    }

    public void setDurations(int[] durations) {
        this.durations = durations;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeStringArray(this.urls);
        dest.writeIntArray(this.durations);
    }

    public MultipleVideo() {
    }

    private MultipleVideo(Parcel in) {
        this.name = in.readString();
        this.urls = in.createStringArray();
        this.durations = in.createIntArray();
    }

    public static final Parcelable.Creator<MultipleVideo> CREATOR = new Parcelable.Creator<MultipleVideo>() {
        public MultipleVideo createFromParcel(Parcel source) {
            return new MultipleVideo(source);
        }

        public MultipleVideo[] newArray(int size) {
            return new MultipleVideo[size];
        }
    };
}
