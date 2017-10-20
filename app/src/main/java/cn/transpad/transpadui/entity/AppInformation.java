package cn.transpad.transpadui.entity;

import android.graphics.drawable.Drawable;

/**
 * Created by ctccuser on 2015/4/4.
 */
public class AppInformation {
    String appName;
    Drawable appIcon;
    boolean downloadFlag=true;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public boolean isDownloadFlag() {
        return downloadFlag;
    }

    public void setDownloadFlag(boolean downloadFlag) {
        this.downloadFlag = downloadFlag;
    }
}
