package cn.transpad.transpadui.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.App;
import cn.transpad.transpadui.entity.AppDetail;
import cn.transpad.transpadui.entity.ApplicationList;
import cn.transpad.transpadui.entity.ApplicationTab;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.entity.Shortcut;
import cn.transpad.transpadui.http.Reporter;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.http.SoftRst;
import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.storage.StorageModule;
import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by user on 2015/5/20.
 */
public class ApplicationUtil {
    private static Context sContext = null;
    private static final String TAG = ApplicationUtil.class.getSimpleName();
    public static final int GET_APP_LIST_FROM_SERVER_SUCCESS = 1;
    public static final int GET_APP_LIST_FROM_SERVER_FAIL = 2;
    private static final EventBus mEventBus = new EventBus();

    public static EventBus getEventBus() {
        return mEventBus;
    }

    public static EventBus getDefaultEventBus() {
        return EventBus.getDefault();
    }

    public static void init(Context context) {
        sContext = context;
    }

    public static List<ApplicationTab> getApplicationTabList() {
        return initQLiteList();
    }

    public static List<ApplicationTab> initQList() {
        List<ApplicationTab> applicationTabList = new ArrayList<>();
        ApplicationTab applicationTab;
        applicationTab = new ApplicationTab();
        applicationTab.setApplicationTabType(ApplicationTab.TYPE_DOWNLOAD_APP_LIST);
        applicationTab.setDownloadOfflineCacheList(getDownloadList());
        applicationTabList.add(applicationTab);
        return applicationTabList;
    }

    public static List<ApplicationTab> initQLiteList() {
        List<ApplicationTab> applicationTabList = new ArrayList<>();
        ApplicationTab applicationTab;
        applicationTab = new ApplicationTab();
        applicationTab.setApplicationTabType(ApplicationTab.TYPE_LOCAL_APP_LIST);
        applicationTab.setShortcutList(getAppList());
        applicationTabList.add(applicationTab);
        return applicationTabList;
    }

    public static List<ApplicationTab> initAllList() {
        List<ApplicationTab> applicationTabList = new ArrayList<>();
        ApplicationTab applicationTab;
        applicationTab = new ApplicationTab();
        applicationTab.setApplicationTabType(ApplicationTab.TYPE_LOCAL_APP_LIST);
        applicationTab.setShortcutList(getAppList());
        applicationTabList.add(applicationTab);
        applicationTab = new ApplicationTab();
        applicationTab.setApplicationTabType(ApplicationTab.TYPE_AUTO_APP_LIST);
        applicationTabList.add(applicationTab);
        applicationTab = new ApplicationTab();
        applicationTab.setApplicationTabType(ApplicationTab.TYPE_DOWNLOAD_APP_LIST);
        applicationTab.setDownloadOfflineCacheList(getDownloadList());
        applicationTabList.add(applicationTab);
        return applicationTabList;
    }

    public static List<ApplicationTab> initNonMTKList() {
        List<ApplicationTab> applicationTabList = new ArrayList<>();
        ApplicationTab applicationTab;
        applicationTab = new ApplicationTab();
        applicationTab.setApplicationTabType(ApplicationTab.TYPE_AUTO_APP_LIST);
        applicationTabList.add(applicationTab);
        applicationTab = new ApplicationTab();
        applicationTab.setApplicationTabType(ApplicationTab.TYPE_DOWNLOAD_APP_LIST);
        applicationTab.setDownloadOfflineCacheList(getDownloadList());
        applicationTabList.add(applicationTab);
        return applicationTabList;
    }

    public static List<Shortcut> getAppList() {
        return TPUtil.getAppInfoListLite(sContext);
    }

    public static ArrayList<OfflineCache> autoAppList = new ArrayList<>();

    public static ArrayList<OfflineCache> getOnlineAppList() {
        return autoAppList;
    }

    public static LinkedHashMap<Long, AppDetail> serverAppDetailMap = new LinkedHashMap<>();

    public static void getListFromServer() {
        Request.getInstance().soft("1", new Callback<SoftRst>() {
            @Override
            public void success(SoftRst softRst, Response response) {
                L.v(TAG, "success", "softRst = " + softRst);
                TPUtil.saveServerData(softRst, "recommend_apps");
                ArrayList<OfflineCache> serverAppList = getSoftFromServer(softRst);
                autoAppList = reArrangeList(sContext, serverAppList);
                Message message = new Message();
                message.what = GET_APP_LIST_FROM_SERVER_SUCCESS;
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(OfflineCache.OFFLINE_CACHE_LIST, autoAppList);
                message.setData(bundle);
                getDefaultEventBus().post(message);
            }

            @Override
            public void failure(RetrofitError error) {
                if (error != null) {

                    L.v(TAG, "instantiateItem", "soft failure error=" + error.getBody());
                }
//                SoftRst rst = TPUtil.readCachedServerData(SoftRst.class, sContext, "recommend_apps");
//                ArrayList<OfflineCache> serverAppList = getSoftFromServer(rst);
//                autoAppList = reArrangeList(sContext, serverAppList);
                Message message = new Message();
                message.what = GET_APP_LIST_FROM_SERVER_FAIL;
//                message.obj = autoAppList;
                getDefaultEventBus().post(message);
            }
        });
    }

    public static ArrayList<OfflineCache> getSoftFromServer(SoftRst softRst) {

        ArrayList<OfflineCache> serverAppList = new ArrayList<OfflineCache>();
        if (softRst != null && softRst.cols != null && softRst.cols.get(0).cnts != null) {
            L.v(TAG, "getSoftFromServer", "softRst" + softRst.host + softRst.cols.get(0).url);
            List<SoftRst.Cnt> cntList = softRst.cols.get(0).cnts.cntList;
            if (cntList != null && cntList.size() > 0) {
                L.v(TAG, "getSoftFromServer", "cnt.size" + cntList.size() + "cnt" + cntList);
                for (SoftRst.Cnt cnt : cntList) {
                    OfflineCache offlineCache = new OfflineCache();
                    offlineCache.setCacheID(TPUtil.parseStringToLong(cnt.id));
                    offlineCache.setCacheVersionCode(TPUtil.parseStringToInt(cnt.ver));
                    offlineCache.setCacheKeyword(cnt.kwd);
                    offlineCache.setCacheName(cnt.name);
                    offlineCache.setCachePackageName(cnt.pkname);
                    offlineCache.setCacheImageUrl(TPUtil.parseImageUrl(softRst.shost, cnt.pic2));
                    offlineCache.setCacheDetailUrl(TPUtil.parseDownloadUrl(softRst.host, cnt.url));
                    OfflineCache localOfflineCache = StorageModule.getInstance().getOfflineCacheById(offlineCache.getCacheID());
                    if (localOfflineCache != null) {
                        offlineCache.setCacheAlreadySize(localOfflineCache.getCacheAlreadySize());
                        offlineCache.setCacheTotalSize(localOfflineCache.getCacheTotalSize());
                        offlineCache.setCacheDownloadState(localOfflineCache.getCacheDownloadState());
                    }
                    serverAppList.add(offlineCache);
                }
            } else {
                //TODO 可能会有问题
                L.v(TAG, "getSoftFromServer", "cntList==null");
            }
        }
        return serverAppList;
    }

    public static ArrayList<OfflineCache> reArrangeList(Context context, ArrayList<OfflineCache> serverAppList) {
        ArrayList<OfflineCache> gridAutoViewList;
        ArrayList<OfflineCache> installedList = new ArrayList<OfflineCache>();
        ArrayList<OfflineCache> unInstalledList = new ArrayList<OfflineCache>();
        for (OfflineCache offlineCache : serverAppList) {
            PackageInfo packageInfo = TPUtil.checkApkExist(context, offlineCache.getCachePackageName());
//            L.v(TAG, "reArrangeList", "pacageinfo" + offlineCache.getCacheName() + packageInfo);
            if (packageInfo != null && packageInfo.versionCode >= offlineCache.getCacheVersionCode()) {
                installedList.add(offlineCache);
            } else {
                unInstalledList.add(offlineCache);
            }
        }
//                        gridAutoViewList.clear();
//                        gridAutoViewList.addAll(installedList);
        gridAutoViewList = installedList;
//        L.v(TAG, "reArrangeList", "gridlist" + "installedList" + installedList.size() + "gridAutoViewList" + gridAutoViewList.size());
        gridAutoViewList.addAll(unInstalledList);
//        L.v(TAG, "reArrangeList", "gridlist" + "installedList" + installedList + "unInstalledList" + unInstalledList + "gridAutoViewList" + gridAutoViewList);
        return gridAutoViewList;
    }

    public static ArrayList<OfflineCache> getDownloadList() {
        return StorageModule.getInstance().getOfflineCacheList();
    }

    public static ArrayList<OfflineCache> getLocalList() {
        SoftRst rst = TPUtil.readCachedServerData(SoftRst.class, sContext, "recommend_apps");
        ArrayList<OfflineCache> serverAppList = getSoftFromServer(rst);
        ArrayList<OfflineCache> gridAutoViewList = reArrangeList(sContext, serverAppList);
        return gridAutoViewList;
    }

    /**
     * 根据本地缓存得到hasMap
     *
     * @return
     */
    public static LinkedHashMap<Long, AppDetail> getLocalMap() {
        SoftRst softRst = TPUtil.readCachedServerData(SoftRst.class, sContext, "recommend_apps");
        return getSoftDetailFromServer(softRst);
    }

    /**
     * 根据softRst做id与AppDetail的hashMap
     *
     * @param softRst
     * @return
     */
    public static LinkedHashMap<Long, AppDetail> getSoftDetailFromServer(SoftRst softRst) {

        LinkedHashMap<Long, AppDetail> serverAppDetailMap = new LinkedHashMap<>();
        if (softRst != null && softRst.cols != null && softRst.cols.get(0).cnts != null) {
            L.v(TAG, "getSoftDetailFromServer", "softRst" + softRst.host + softRst.cols.get(0).url);
            List<SoftRst.Cnt> cntList = softRst.cols.get(0).cnts.cntList;
            if (cntList != null && cntList.size() > 0) {
                L.v(TAG, "getSoftDetailFromServer", "cnt.size" + cntList.size() + "cnt" + cntList);
                for (SoftRst.Cnt cnt : cntList) {
                    AppDetail appDetail = new AppDetail();
                    appDetail.setId(TPUtil.parseStringToLong(cnt.id));
                    appDetail.setImageUrl(TPUtil.parseImageUrl(softRst.shost, cnt.pic2));
                    appDetail.setDownloadAmount(cnt.clicknum);
                    appDetail.setName(cnt.name);
                    appDetail.setRecommend(cnt.recmond);
                    appDetail.setSize(cnt.mb);
                    appDetail.setType(cnt.type);
                    appDetail.setVersion(cnt.vername);
                    appDetail.setDescription(cnt.desc);

                    if (cnt.imgs != null) {
                        List<SoftRst.Img> imgList = cnt.imgs.imgList;
                        if (imgList != null && imgList.size() > 0) {
                            ArrayList<String> imageUrlList = new ArrayList<>();
                            for (SoftRst.Img img : imgList) {
                                //拼接图片url不知是哪个host
                                imageUrlList.add(TPUtil.parseImageUrl(softRst.shost, img.url));
                            }
                            appDetail.setImageList(imageUrlList);
                        }
                    } else {
                        L.v(TAG, "getSoftDetailFromServer" + "imgs==null");
                    }
//                    offlineCache.setCacheImageUrl(TPUtil.parseImageUrl(softRst.shost, cnt.pic2));
//                    offlineCache.setCacheDetailUrl(TPUtil.parseDownloadUrl(softRst.host, cnt.url));
//                    serverAppDetailList.add(appDetail);
                    serverAppDetailMap.put(appDetail.getId(), appDetail);
                }
            } else {
                //TODO 可能会有问题
                L.v(TAG, "getSoftDetailFromServer", "cntList==null");
            }
        }
        return serverAppDetailMap;
    }

    public static AppDetail searchAppDetail(long id) {
        return serverAppDetailMap.get(id);
    }

    /**
     * 利用cnt拼接出AppDetail
     *
     * @param cnt
     * @param shost
     * @return
     */
    public static AppDetail getAppDetail(SoftRst.Cnt cnt, String shost) {
        AppDetail appDetail = new AppDetail();
        appDetail.setId(TPUtil.parseStringToLong(cnt.id));
        appDetail.setImageUrl(TPUtil.parseImageUrl(shost, cnt.pic2));
        appDetail.setDownloadAmount(cnt.clicknum);
        appDetail.setName(cnt.name);
        appDetail.setRecommend(cnt.recmond);
        appDetail.setSize(cnt.mb);
        appDetail.setType(cnt.type);
        appDetail.setVersion(cnt.vername);
        appDetail.setDescription(cnt.desc);

        if (cnt.imgs != null) {
            List<SoftRst.Img> imgList = cnt.imgs.imgList;
            if (imgList != null && imgList.size() > 0) {
                ArrayList<String> imageUrlList = new ArrayList<>();
                for (SoftRst.Img img : imgList) {
                    //拼接图片url不知是哪个host
                    imageUrlList.add(TPUtil.parseImageUrl(shost, img.url));
                }
                appDetail.setImageList(imageUrlList);
            }
        } else {
            L.v(TAG, "getAppDetail" + "imgs==null");
        }
        return appDetail;
    }

    /**
     * @param id
     * @return
     */
    public static boolean isDownloadFinish(long id) {
        OfflineCache offlineCache = StorageModule.getInstance().getOfflineCacheById(id);
        if (offlineCache != null && offlineCache.getCacheDownloadState() == OfflineCache.CACHE_STATE_FINISH) {
            return true;
        }
        return false;
    }

    /**
     * 是否有未安装的下载任务
     *
     * @return true:有未安装的包 false:全已安装
     */
    public static boolean isTaskUninstalled() {
        L.v(TAG, "isTaskUninstalled");
        ArrayList<OfflineCache> offlineCacheList = StorageModule.getInstance().getOfflineCacheList();
        for (OfflineCache offlineCache : offlineCacheList) {
            PackageInfo packageInfo = TPUtil.checkApkExist(sContext, offlineCache.getCachePackageName());
            if (packageInfo == null || packageInfo.versionCode < offlineCache.getCacheVersionCode()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 非WiFi网络提示
     */
    public static void nonWiFiToast() {
        int type = TPUtil.getCurrentNetType(sContext);
        if (type != 0 && type != 1) {
            Toast.makeText(sContext, R.string.download_detail_not_wifi, Toast.LENGTH_LONG).show();
        }
    }

    public static void saveRecentList(ArrayList<App> recentAppList) {
        Gson gson = new Gson();
        String recentList = gson.toJson(recentAppList);
        SharedPreferenceModule.getInstance().setString("recent_app_list", recentList);
    }

    public static ArrayList<App> getRecentList() {
        String recentList = SharedPreferenceModule.getInstance().getString("recent_app_list");
        Gson gson = new Gson();
        ArrayList<App> recentAppList = gson.fromJson(recentList, new TypeToken<ArrayList<App>>() {
        }.getType());

        if (recentAppList == null) {
            recentAppList = new ArrayList<>();
        }

        for (int i = 0; i < recentAppList.size(); i++) {
            PackageInfo packageInfo = TPUtil.checkApkExist(sContext, recentAppList.get(i).getPackageName());
            if (packageInfo == null) {
                recentAppList.remove(i);
            }
        }
        saveRecentList(recentAppList);
        return recentAppList;
    }

    public static void checkRecentList() {
        ArrayList<App> recentList = getRecentList();
        for (int i = 0; i < recentList.size(); i++) {
            PackageInfo packageInfo = TPUtil.checkApkExist(sContext, recentList.get(i).getPackageName());
            if (packageInfo == null) {
                recentList.remove(i);
            }
        }
        saveRecentList(recentList);
    }

    public static void addRecentApp(App app) {
        L.v(TAG, "addRecentApp", "app = " + app);
        ArrayList<App> recentAppList = getRecentList();
        if (app != null) {
            int position = getRecentAppPosition(app, recentAppList);
            if (position == -1) {
                recentAppList.add(0, app);
                while (recentAppList.size() > 6) {
                    recentAppList.remove(recentAppList.size() - 1);
                }
            } else {
                recentAppList.remove(position);
                recentAppList.add(0, app);
            }
        }
        L.v(TAG, "addRecentApp", "saveRecentList: " + recentAppList);
        saveRecentList(recentAppList);
    }

    private static int getRecentAppPosition(App app, ArrayList<App> recentAppList) {
        int length = recentAppList.size();
        try {
            for (int i = 0; i < length; i++) {
                if (recentAppList.get(i).getName().equals(app.getName())
                        && recentAppList.get(i).getPackageName().equals(app.getPackageName())) {
                    return i;
                }
            }
        } catch (Exception e) {
            L.e(TAG, "getRecentAppPosition", "e = " + e);
        }
        return -1;
    }

    public static final int N = 6;

    public static ArrayList<ApplicationList> getAllList() {
        ArrayList<ApplicationList> contentAppList = new ArrayList<>();
        List<App> allAppList = TPUtil.getAppInfoList(sContext);
        int group = allAppList.size() / N;
        int residue = allAppList.size() % N;
        L.v(TAG, "getAllList", "total==" + allAppList.size() + "group==" + group + "residue==" + residue);

        for (int i = 0; i < group; i++) {
            ApplicationList applicationList = new ApplicationList();
            ArrayList<App> contentList = new ArrayList<>();
            for (int j = i * N; j < (i + 1) * N; j++) {
                contentList.add(allAppList.get(j));
            }
            applicationList.setAllList(contentList);
            applicationList.setApplicationListType(ApplicationList.APPLICATION_LIST_ALL);
            contentAppList.add(applicationList);
        }

        if (residue != 0) {
            ApplicationList applicationResideList = new ApplicationList();
            ArrayList<App> groupResideList = new ArrayList<>();
            for (int i = allAppList.size() - residue; i < allAppList.size(); i++) {
                groupResideList.add(allAppList.get(i));
            }
            applicationResideList.setAllList(groupResideList);
            applicationResideList.setApplicationListType(ApplicationList.APPLICATION_LIST_ALL);
            contentAppList.add(applicationResideList);
        }
        L.v(TAG, "getAllList", "contentAppList: " + contentAppList);
        return contentAppList;
    }

    public static ArrayList<ApplicationList> initApplicationList() {
        L.v(TAG, "initApplicationList");
        ArrayList<ApplicationList> allList = new ArrayList<>();
        ApplicationList applicationList;
        applicationList = new ApplicationList();
        ArrayList<App> recentList = getRecentList();
        if (!(recentList.size() == 0)) {
            applicationList.setApplicationListType(ApplicationList.APPLICATION_LIST_RECENT);
//        checkRecentList();
            applicationList.setRecentList(getRecentList());
            allList.add(applicationList);
        }

        allList.addAll(getAllList());
        return allList;
    }

    public static void saveAppRecentList(Context context, String packageName, String activityName) {
        L.v(TAG, "saveAppRecentList");
        App app = new App();
        String name = TPUtil.getAppNameByPackageName(context, packageName);
        app.setName(name);
        app.setActivityName(activityName);
        app.setPackageName(packageName);
        ApplicationUtil.addRecentApp(app);
    }

    /**
     * 根据包名启动应用
     *
     * @param context
     * @param packageName
     */
    public static void startAppByPackageName(Context context, String packageName) {
        if (context != null) {
            try {
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                Reporter.logInvokErp(TPUtil.getAppNameByPackageName(context, packageName), 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            ApplicationUtil.saveAppRecentList(context, packageName, null);
        }
    }

    /**
     * 根据包名启动应用
     *
     * @param context
     * @param packageName
     */
    public static void startAppByActivityNamePackageName(Context context, String packageName, String activityName) {
        if (context != null) {
            try {
                Intent launchIntent = new Intent();
                launchIntent.setComponent(new ComponentName(packageName,
                        activityName));
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
                Reporter.logInvokErp(TPUtil.getAppNameByPackageName(context, packageName), 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            ApplicationUtil.saveAppRecentList(context, packageName, activityName);
        }
    }
}
