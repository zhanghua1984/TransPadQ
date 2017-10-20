package cn.transpad.dlna.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by left on 16/1/6.
 */
public class DLNADevice implements Parcelable {
    public final static String DEVICE_TYPE = "urn:schemas-upnp-org:device:MediaRenderer:1";
    /**
     * 0为airplay,非0为dlna
     */
    public int proto;
    public String server_name;
    public String server_ip_addr;
    public int server_port;
    public String server_uid;

    public int media_index = -1;
    public int player_state = -1;

    public int seek_pos;

    public boolean checked;

    public boolean logined;
    /**请求状态连续错误次数*/
    public int error_times = 0;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.proto);
        dest.writeString(this.server_name);
        dest.writeString(this.server_ip_addr);
        dest.writeInt(this.server_port);
        dest.writeString(this.server_uid);
        dest.writeInt(this.media_index);
        dest.writeInt(this.player_state);
        dest.writeInt(this.seek_pos);
        dest.writeByte(checked ? (byte) 1 : (byte) 0);
        dest.writeByte(logined ? (byte) 1 : (byte) 0);
        dest.writeInt(this.error_times);
    }

    public DLNADevice() {
    }

    protected DLNADevice(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in){
        this.proto = in.readInt();
        this.server_name = in.readString();
        this.server_ip_addr = in.readString();
        this.server_port = in.readInt();
        this.server_uid = in.readString();
        this.media_index = in.readInt();
        this.player_state = in.readInt();
        this.seek_pos = in.readInt();
        this.checked = in.readByte() != 0;
        this.logined = in.readByte() != 0;
        this.error_times = in.readInt();
    }

    public static final Parcelable.Creator<DLNADevice> CREATOR = new Parcelable.Creator<DLNADevice>() {
        public DLNADevice createFromParcel(Parcel source) {
            return new DLNADevice(source);
        }

        public DLNADevice[] newArray(int size) {
            return new DLNADevice[size];
        }
    };

    @Override
    public String toString() {
        return "DLNADevice{" +
                "proto=" + proto +
                ", server_name='" + server_name + '\'' +
                ", server_ip_addr='" + server_ip_addr + '\'' +
                ", server_port=" + server_port +
                ", server_uid='" + server_uid + '\'' +
                ", media_index=" + media_index +
                ", player_state=" + player_state +
                ", seek_pos=" + seek_pos +
                ", checked=" + checked +
                ", logined=" + logined +
                ", error_times=" + error_times +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DLNADevice device = (DLNADevice) o;

        if (server_ip_addr.equals(device.server_ip_addr)) return false;
        return server_port == device.server_port;

    }

}
