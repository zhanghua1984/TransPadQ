package cn.transpad.transpadui.entity;

import java.util.ArrayList;

/**
 * Created by user on 2015/7/31.
 */
public class ApplicationList {
    public static final int APPLICATION_LIST_RECENT = 1;
    public static final int APPLICATION_LIST_ALL = 2;
    public int ApplicationListType;
    ArrayList<App> recentList = new ArrayList<>();
    ArrayList<App> allList = new ArrayList<>();

    public ArrayList<App> getRecentList() {
        return recentList;
    }

    public void setRecentList(ArrayList<App> recentList) {
        this.recentList = recentList;
    }

    public ArrayList<App> getAllList() {
        return allList;
    }

    public void setAllList(ArrayList<App> allList) {
        this.allList = allList;
    }

    public int getApplicationListType() {
        return ApplicationListType;
    }

    public void setApplicationListType(int applicationListType) {
        ApplicationListType = applicationListType;
    }
}
