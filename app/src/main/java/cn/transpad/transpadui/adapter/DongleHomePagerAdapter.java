package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.cache.GrayBitmapDisplayer;
import cn.transpad.transpadui.cache.ImageDownloadModule;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.http.SoftRst;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;
import cn.transpad.transpadui.view.DongleHomeAppView;
import cn.transpad.transpadui.view.DongleHomePage;
import cn.transpad.transpadui.view.HomePage2;
import cn.transpad.transpadui.view.MainPageDownloadDialog;

/**
 * Created by Kongxiaojun on 2015/4/14.
 * Dongle Home Pager
 */
public class DongleHomePagerAdapter extends PagerAdapter {

    private static final String TAG = "DongleHomePagerAdapter";

    private static final int HEAD_PAGE_COUNT = 2;

    private int appPageSize = 15;

    private SoftRst softRst;

    private Context mContext;

    private View mMain;

    private HomePage2 mMain2;

    private View appView;

    private DisplayImageOptions garyOptions;

    private MainPageDownloadDialog mainPageDownloadDialog;


    public DongleHomePagerAdapter(SoftRst softRst, Context mContext) {
        this.softRst = softRst;
        this.mContext = mContext;
        mMain = new DongleHomePage(mContext);
        mMain2 = new HomePage2(mContext);
        garyOptions = createGaryDisplayImageOptionsByDrawableId(R.drawable.ic_launcher);
        updateAppViews();
    }

    /**
     * 更新APP view
     */
    private void updateAppViews() {
        appView = View.inflate(mContext, R.layout.dongle_apppager, null);
//            int totalPage = (softRst.col.cnts.cntList.size() / (appPageSize + 1)) + 1;
//            L.v(TAG, "app totalPage = " + totalPage);
//            for (int i = 0; i < totalPage; i++) {
//                View view = View.inflate(mContext, R.layout.dongle_apppager, null);
//                appView.add(view);
//            }
    }

    @Override
    public int getCount() {
//        if (softRst == null || softRst.col == null || softRst.col.cnts == null || softRst.col.cnts.cntList == null) {
//            return HEAD_PAGE_COUNT;
//        }
        return HEAD_PAGE_COUNT + 1;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        L.v(TAG, "instantiateItem  position = " + position);
        if (position == 0) {
            container.addView(mMain);
            return mMain;
        } else if (position == 1) {
            container.addView(mMain2);
            return mMain2;
        } else {
            if (softRst != null) {
                final List<SoftRst.Cnt> cnts = new ArrayList<SoftRst.Cnt>();
                for (int j = 0; j < appPageSize; j++) {
                    if (softRst.cols.get(0).cnts.cntList.size() > (position - HEAD_PAGE_COUNT) * appPageSize + j) {
                        cnts.add(softRst.cols.get(0).cnts.cntList.get((position - HEAD_PAGE_COUNT) * appPageSize + j));
                    }
                }
                final DongleHomeAppView dongleHomeAppView = (DongleHomeAppView) appView.findViewById(R.id.application_rv);
                if (dongleHomeAppView.getAdapter() == null) {
                    dongleHomeAppView.setAdapter(new DongleAppAdapter(mContext, cnts, softRst.shost, softRst.host, softRst.cols.get(0).name, garyOptions));
                    dongleHomeAppView.setItemClickListener(new DongleHomeAppView.OnItemClickListener() {
                        @Override
                        public void onItemClick(int i) {
                            if (TPUtil.isNetOkWithToast()) {
                                onClickApp(softRst.cols.get(0).cnts.cntList.get(i));
                            }
                        }
                    });
                } else {
                    dongleHomeAppView.notifyDataSetChanged();
                }
            }
            container.addView(appView);
            return appView;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public void setSoftRst(SoftRst softRst) {
        this.softRst = softRst;
        updateAppViews();
        notifyDataSetChanged();
    }

    public SoftRst getSoftRst() {
        return softRst;
    }

    public void updateDownloadProgress(OfflineCache oc) {
        if (getCount() > HEAD_PAGE_COUNT) {
            int index = -1;
            for (int i = 0; i < softRst.cols.get(0).cnts.cntList.size(); i++) {
                if (Long.parseLong(softRst.cols.get(0).cnts.cntList.get(i).id) == oc.getCacheID()) {
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
//                }
            }
        }
    }

    private void onClickApp(final SoftRst.Cnt cnt) {
        PackageInfo info = TPUtil.checkApkExist(mContext, cnt.pkname);
        if (info == null) {
            OfflineCache offlineCache = StorageModule.getInstance().getOfflineCacheById(Long.parseLong(cnt.id));
            if (offlineCache != null) {
                switch (offlineCache.getCacheDownloadState()) {
                    case OfflineCache.CACHE_STATE_DOWNLOADING://下载中
                        StorageModule.getInstance().pauseCache(offlineCache);
                        break;
                    case OfflineCache.CACHE_STATE_FINISH://完成，点击安装
                        File file = new File(offlineCache.getCacheStoragePath());
                        if (file.exists()) {
                            TPUtil.installAPK(file, mContext);
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
            //未下载
            mainPageDownloadDialog = new MainPageDownloadDialog(mContext, R.style.myDialog);
            mainPageDownloadDialog.setMessage(String.format(mContext.getString(R.string.home_download_dialog_message), cnt.name), TPUtil.parseDownloadUrl(softRst.host, cnt.url));
            mainPageDownloadDialog.setClickListener(new MainPageDownloadDialog.ClickListener() {
                @Override
                public void onOk() {
                    if (TPUtil.isNetOkWithToast()) {
                        OfflineCache offlineCache = new OfflineCache();
                        offlineCache.setCacheName(cnt.name);
                        offlineCache.setCacheID(Long.parseLong(cnt.id));
                        offlineCache.setCachePackageName(cnt.pkname);
                        offlineCache.setCacheVersionCode(Integer.parseInt(cnt.ver));
                        offlineCache.setCacheDetailUrl(TPUtil.parseDownloadUrl(softRst.host, cnt.url));
                        offlineCache.setCacheImageUrl(TPUtil.parseImageUrl(softRst.shost, cnt.pic2));
                        StorageModule.getInstance().addCache(offlineCache);
                    }
                    mainPageDownloadDialog.dismiss();
                }

                @Override
                public void onCancel() {
                    mainPageDownloadDialog.dismiss();
                }
            });
            mainPageDownloadDialog.show();
        } else {
            TPUtil.startAppByPackegName(mContext, cnt.pkname);
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
        //找到这个应用在哪一页
        if (!TextUtils.isEmpty(packageName)) {
            L.v(TAG, "applicationUpdate packageName = " + packageName);
            if (softRst != null && softRst.cols.get(0).cnts.cntList != null && softRst.cols.get(0).cnts.cntList.size() > 0) {
                int index = -1;
                for (int i = 0; i < softRst.cols.get(0).cnts.cntList.size(); i++) {
                    L.v(TAG, "applicationUpdate softRst.col.cnts.cntList.get(i).pkname = " + softRst.cols.get(0).cnts.cntList.get(i).pkname);
                    if (packageName.equals(softRst.cols.get(0).cnts.cntList.get(i).pkname)) {
                        index = i;
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
                            ImageDownloadModule.getInstance().displayImage(TPUtil.getAbsoluteUrl(softRst.host, softRst.shost, softRst.cols.get(0).cnts.cntList.get(index).pic1), viewHolder.icon);
                        } else {
                            ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(softRst.host, softRst.shost, softRst.cols.get(0).cnts.cntList.get(index).pic1), viewHolder.icon, garyOptions);
                        }

                    }
//                    }
                }
            }

        }

    }

    public void updateAppItemViews() {
        if (appView != null) {
            final DongleHomeAppView dongleHomeAppView = (DongleHomeAppView) appView.findViewById(R.id.application_rv);
            dongleHomeAppView.notifyDataSetChanged();
        }
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

    public void dismessAllDialog() {
        if (mainPageDownloadDialog != null && mainPageDownloadDialog.isShowing()) {
            mainPageDownloadDialog.dismiss();
        }
        mainPageDownloadDialog = null;
    }
}
