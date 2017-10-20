package cn.transpad.transpadui.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2015/5/20.
 */
public class ApplicationTab {
    /**
     * 本地应用列表
     */
    public static final int TYPE_LOCAL_APP_LIST = 1;
    public static final int TYPE_AUTO_APP_LIST = 2;
    public static final int TYPE_DOWNLOAD_APP_LIST = 3;


    //我的应用标签页类型
    private int mApplicationTabType;
    //本地应用列表
    private List<Shortcut> mShortcutList;
    //推荐应用列表
    private ArrayList<OfflineCache> mAutoOfflineCacheList;
    //下载管理列表
    private ArrayList<OfflineCache> mDownloadOfflineCacheList;

    public ArrayList<OfflineCache>  getDownloadOfflineCacheList() {
        return mDownloadOfflineCacheList;
    }

    public void setDownloadOfflineCacheList(ArrayList<OfflineCache>  downloadOfflineCacheList) {
        mDownloadOfflineCacheList = downloadOfflineCacheList;
    }

    public ArrayList<OfflineCache>  getAutoOfflineCacheList() {
        return mAutoOfflineCacheList;
    }

    public void setAutoOfflineCacheList(ArrayList<OfflineCache>  autoOfflineCacheList) {
        mAutoOfflineCacheList = autoOfflineCacheList;
    }

    public List<Shortcut> getShortcutList() {
        return mShortcutList;
    }

    public void setShortcutList(List<Shortcut> shortcutList) {
        mShortcutList = shortcutList;
    }

    public int getApplicationTabType() {
        return mApplicationTabType;
    }

    public void setApplicationTabType(int applicationTabType) {
        mApplicationTabType = applicationTabType;
    }
}
