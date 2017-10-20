package cn.transpad.transpadui.view;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.DongleAppAdapter;
import cn.transpad.transpadui.cache.GrayBitmapDisplayer;
import cn.transpad.transpadui.entity.AppDetail;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.http.SoftRst;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.ApplicationUtil;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;

/**
 * Created by Kongxiaojun on 2015/5/13.
 * Dongle的游戏推荐页
 */
public class DongleHomeAppPage extends LinearLayout {


    private static final String TAG = "DongleHomeAppPage";

    private Handler mHandler;

    DongleHomeAppView appView;

    private SoftRst.Col colume;

    private String host;
    private String shost;

    DongleAppAdapter appAdapter;

    private int appPageSize = 15;

    private static final int MSG_WHAT_UPDATE_DATA = 1;

    private DisplayImageOptions garyOptions;

    private DownloadDetailDialog downloadDetailDialog = new DownloadDetailDialog(getContext(), R.style.myDialog);

    public DongleHomeAppPage(Context context, SoftRst.Col col, String host, String shost) {
        super(context);
        this.colume = col;
        this.shost = shost;
        this.host = host;
        init();
    }

    private DongleHomeAppPage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private DongleHomeAppPage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.dongle_apppager, this);
        appView = (DongleHomeAppView) findViewById(R.id.application_rv);
        garyOptions = createGaryDisplayImageOptionsByDrawableId(R.drawable.ic_launcher);
        initHandler();
        initData();
    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                }
            }
        };
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    private void initData() {
        if (colume != null) {
            appAdapter = new DongleAppAdapter(getContext(), colume.cnts.cntList, shost, host, colume.name, garyOptions);
            appView.setAdapter(appAdapter);
            appView.setItemClickListener(new DongleHomeAppView.OnItemClickListener() {
                @Override
                public void onItemClick(int i) {
                    if (i >= 0) {
                        onClickApp(i, colume.cnts.cntList.get(i));
                    }
                }
            });
        }
    }

    private void onClickApp(final int position, final SoftRst.Cnt cnt) {
        PackageInfo info = TPUtil.checkApkExist(getContext(), cnt.pkname);
        if (info == null) {
            if (!TPUtil.isNetOkWithToast()) {
                return;
            }
            OfflineCache offlineCache = StorageModule.getInstance().getOfflineCacheById(Long.parseLong(cnt.id));
            if (offlineCache != null) {
                switch (offlineCache.getCacheDownloadState()) {
                    case OfflineCache.CACHE_STATE_DOWNLOADING://下载中
                        TPUtil.showToast(R.string.app_already_downlaod);
                        break;
                    case OfflineCache.CACHE_STATE_WAITING://等待中
                        TPUtil.showToast(R.string.app_already_downlaod);
                        break;
                    case OfflineCache.CACHE_STATE_FINISH://完成，点击安装
                        File file = new File(offlineCache.getCacheStoragePath());
                        if (file.exists()) {
                            AppDetail appDetail = ApplicationUtil.getAppDetail(cnt, shost);
                            downloadDetailDialog.setAppDetail(appDetail);
                            downloadDetailDialog.show();
//                            TPUtil.installAPK(file, getContext());
                        } else {
                            //文件不存在重新下载
                            StorageModule.getInstance().deleteCache(offlineCache);
                            StorageModule.getInstance().addCache(offlineCache);
                        }
                        break;
                    case OfflineCache.CACHE_STATE_ERROR://出错
                        StorageModule.getInstance().deleteCache(offlineCache);
                        StorageModule.getInstance().addCache(offlineCache);
                        break;
                    default:
                        StorageModule.getInstance().startCache(offlineCache);
                        break;
                }
                return;
            }

            //未安装
            AppDetail appDetail = ApplicationUtil.getAppDetail(cnt, shost);
            downloadDetailDialog.setAppDetail(appDetail);
            downloadDetailDialog.setOnDialogClickListener(new DownloadDetailDialog.OnDialogClickListener() {
                @Override
                public void onClick() {
                    if (TPUtil.isNetOkWithToast()) {
                        OfflineCache offlineCache = new OfflineCache();
                        offlineCache.setCacheName(cnt.name);
                        offlineCache.setCacheID(Long.parseLong(cnt.id));
                        offlineCache.setCachePackageName(cnt.pkname);
                        offlineCache.setCacheVersionCode(Integer.parseInt(cnt.ver));
                        offlineCache.setCacheDetailUrl(TPUtil.parseDownloadUrl(host, cnt.url));
                        offlineCache.setCacheImageUrl(TPUtil.parseImageUrl(shost, cnt.pic2));
                        StorageModule.getInstance().addCache(offlineCache);
                        DongleHomeAppView dongleHomeAppView = (DongleHomeAppView) appView.findViewById(R.id.application_rv);
                        DongleAppAdapter.ViewHolder viewHolder = new DongleAppAdapter.ViewHolder(dongleHomeAppView.getChildView(position));
                        if (viewHolder != null) {
                            viewHolder.progressBar.setVisibility(View.VISIBLE);
                            viewHolder.frame.setVisibility(View.VISIBLE);
                            viewHolder.downlaodIcon.setVisibility(View.GONE);
                        }
                    }
                }
            });
            downloadDetailDialog.show();
        } else {
            TPUtil.startAppByPackegName(getContext(), cnt.pkname);
        }
    }

    /**
     * 应用安装/卸载
     *
     * @param isInstall   是否是安装
     * @param packageName 包名
     */
    public void applicationUpdate(boolean isInstall, String packageName) {
        L.v(TAG, "applicationUpdate");
        if (!TextUtils.isEmpty(packageName)) {
            L.v(TAG, "applicationUpdate packageName = " + packageName);
            if (colume != null && colume.cnts.cntList != null && colume.cnts.cntList.size() > 0) {
                int index = -1;
                String id = null;
                for (int i = 0; i < colume.cnts.cntList.size(); i++) {
                    L.v(TAG, "applicationUpdate softRst.col.cnts.cntList.get(i).pkname = " + colume.cnts.cntList.get(i).pkname);
                    if (packageName.equals(colume.cnts.cntList.get(i).pkname)) {
                        index = i;
                        id = colume.cnts.cntList.get(i).id;
                        break;
                    }
                }
                if (index > -1 && index < appPageSize) {
                    L.v(TAG, "applicationUpdate index > -1");
                    //找到属于哪一页
//                    int page = index / appPageSize;
//                    if (page >= 0) {
                    L.v(TAG, "applicationUpdate page >= 0");
//                        View view = appView.get(page);
                    DongleHomeAppView dongleHomeAppView = (DongleHomeAppView) appView.findViewById(R.id.application_rv);
                    int position = index % appPageSize;
                    DongleAppAdapter.ViewHolder viewHolder = new DongleAppAdapter.ViewHolder(dongleHomeAppView.getChildView(position));
                    if (viewHolder != null) {
                        L.v(TAG, "applicationUpdateviewHolder != null");
                        if (isInstall) {
                            viewHolder.icon.setImageDrawable(TPUtil.getDrawableByPackageName(getContext(), packageName));
                            viewHolder.downlaodIcon.setVisibility(INVISIBLE);
                        } else {
                            ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(host, shost, colume.cnts.cntList.get(index).pic1), viewHolder.icon, garyOptions);
                            try {
                                OfflineCache offlineCache = StorageModule.getInstance().getOfflineCacheById(Long.parseLong(id));
                                if (offlineCache == null){
                                    viewHolder.downlaodIcon.setVisibility(VISIBLE);
                                }else {
                                    viewHolder.downlaodIcon.setVisibility(INVISIBLE);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }

                    }
//                    }
                }
            }

        }

    }

    public void dismessDialog() {
        if (downloadDetailDialog != null && downloadDetailDialog.isShowing()){
            downloadDetailDialog.dismiss();
        }
    }

    /**
     * 更新下载进度
     *
     * @param oc
     */
    public void updateDownloadProgress(OfflineCache oc) {
        if (oc != null) {
            int index = -1;
            for (int i = 0; i < colume.cnts.cntList.size(); i++) {
                if (Long.parseLong(colume.cnts.cntList.get(i).id) == oc.getCacheID()) {
                    index = i;
                    break;
                }
            }
            if (index > -1 && index < appPageSize) {
                //找到属于哪一页
//                int page = (index) / appPageSize + HEAD_PAGE_COUNT;
//                if (page > 0) {
//                    View view = appView.get(page - HEAD_PAGE_COUNT);
                DongleHomeAppView dongleHomeAppView = (DongleHomeAppView) appView.findViewById(R.id.application_rv);
                int position = index % appPageSize;
                DongleAppAdapter.ViewHolder viewHolder = new DongleAppAdapter.ViewHolder(dongleHomeAppView.getChildView(position));
                if (viewHolder != null) {
                    viewHolder.progressBar.setVisibility(View.VISIBLE);
                    viewHolder.frame.setVisibility(View.VISIBLE);
                    viewHolder.downlaodIcon.setVisibility(View.GONE);
                    viewHolder.progressBar.setProgress((int) oc.getCachePercentNum());
                    if (oc.getCacheDownloadState() == OfflineCache.CACHE_STATE_FINISH) {
                        viewHolder.progressBar.setVisibility(View.GONE);
                        viewHolder.frame.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    /**
     * 有下载被删除了
     */
    public void cacheStateChanged() {
        appView.notifyDataSetChanged();
    }

    /**
     * 创建一个图片显示DisplayImageOptions
     *
     * @param id
     * @return DisplayImageOptions
     * @throws
     */
    private DisplayImageOptions createGaryDisplayImageOptionsByDrawableId(
            int id) {
        return new DisplayImageOptions.Builder().showImageOnLoading(id)
                .showImageForEmptyUri(id).showImageOnFail(id)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .displayer(new GrayBitmapDisplayer())
                .build();
    }

}
