package cn.transpad.transpadui.entity;

import java.io.Serializable;

/**
 * Created by Kongxiaojun on 2015/5/20.
 */
public class InvokErp implements Serializable {
    public static final int HOME_PAGE = 7;
    public static final int WEB_SITE_PAGE = 8;
    public static final int GAME_PAGE = 9;
    public static final int COMPUTER_PAGE = 10;
    public static final int DEMONSTRATION_PAGE = 11;
    public static final int NOTIFICATION_FIRMWARE_UPGRATE = 12;
    public static final int NOTIFICATION_SOFTWARE_UPGRATE = 13;
    public static final int NOTIFICATION_CONTENT_CLICK = 14;
    /**基础版我的应用*/
    public static final int LITE_MY_APP = 19;
    /**基础版多媒体*/
    public static final int LITE_MULTIMEDIA = 20;
    /**基础版放映厅*/
    public static final int LITE_VIDEOPLAY_ROOM = 21;
    /**基础版音乐厅*/
    public static final int LITE_AUDIOPLAY_ROOM = 22;
    /**基础版美图秀*/
    public static final int LITE_IMAGEPLAY_ROOM = 23;
    /**
     * 定制应用
     */
    public static final int RECOMMEND_CLICK = 15;

    private String name;

    private int times;

    private String device;

    private int state;

    public InvokErp(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
