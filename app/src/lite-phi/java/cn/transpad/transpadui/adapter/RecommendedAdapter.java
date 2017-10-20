package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.cache.GrayBitmapDisplayer;
import cn.transpad.transpadui.cache.ImageDownloadModule;
import cn.transpad.transpadui.entity.InvokErp;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.http.Reporter;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;
import cn.transpad.transpadui.view.DownloadDialog;
import cn.transpad.transpadui.view.NumberCircleProgressBar;


/**
 * Created by ctccuser on 2015/4/5.
 */
public class RecommendedAdapter extends BaseAdapter {
    private static final String TAG = RecommendedAdapter.class.getSimpleName();
    private Context context;
    private GridView mGridView = null;
    private ArrayList<OfflineCache> viewList;
    private LinkedHashMap<Long, Integer> mFileOfflineCacheMap = new LinkedHashMap<Long, Integer>();
    DownloadDialog downloadDialog;
//    DownloadDetailDialog downloadDialog;

    public RecommendedAdapter(Context context, GridView gridView) {
        this.context = context;
        mGridView = gridView;
        createOptions();
    }

    public void setOfflineCacheList(ArrayList<OfflineCache> offlineCacheList) {
        if (offlineCacheList != null) {
            viewList = offlineCacheList;
            int position = 0;
            for (OfflineCache offlineCache : viewList) {
                mFileOfflineCacheMap.put(offlineCache.getCacheID(), position);
                position++;
            }
        }
    }

    @Override
    public int getCount() {
        return viewList != null ? viewList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return viewList != null ? viewList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_download_app, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        setViewImageData(viewHolder, position);
        setViewData(viewHolder, position);

        return convertView;
    }

    private void setViewImageData(ViewHolder viewHolder, int position) {
        final OfflineCache offlineCache = viewList.get(position);
        if (offlineCache != null) {
            viewHolder.appTextView.setText(offlineCache.getCacheName());
            final PackageInfo packageInfo = TPUtil.checkApkExist(context, offlineCache.getCachePackageName());
            if (packageInfo != null && packageInfo.versionCode >= offlineCache.getCacheVersionCode()) {
                viewHolder.numberCircleProgressBar.setVisibility(NumberCircleProgressBar.INVISIBLE);
                viewHolder.frame.setVisibility(ImageView.INVISIBLE);
                viewHolder.downloadArrow.setVisibility(ImageView.INVISIBLE);
                ImageDownloadModule.getInstance().displayImage(offlineCache.getCacheImageUrl(), viewHolder.appImageView);
            } else {
                viewHolder.downloadArrow.setVisibility(ImageView.VISIBLE);
                int state = offlineCache.getCacheDownloadState();
//                L.v(TAG, "setViewImageData", "name" + offlineCache.getCacheName() + "state" + state);
                switch (state) {
                    case OfflineCache.CACHE_STATE_NOT_DOWNLOAD:
                        viewHolder.numberCircleProgressBar.setVisibility(NumberCircleProgressBar.INVISIBLE);
                        viewHolder.frame.setVisibility(ImageView.INVISIBLE);
                        ImageLoader.getInstance().displayImage(offlineCache.getCacheImageUrl(), viewHolder.appImageView, garyOptions);
                        break;
                    case OfflineCache.CACHE_STATE_WAITING:
                    case OfflineCache.CACHE_STATE_PAUSE:
                    case OfflineCache.CACHE_STATE_PAUSE_USER:
                        viewHolder.numberCircleProgressBar.setVisibility(NumberCircleProgressBar.VISIBLE);
                        viewHolder.frame.setVisibility(ImageView.VISIBLE);
                        viewHolder.numberCircleProgressBar.setProgress((int) offlineCache.getCachePercentNum());
                        viewHolder.downloadArrow.setVisibility(ImageView.INVISIBLE);
                        ImageDownloadModule.getInstance().displayImage(offlineCache.getCacheImageUrl(), viewHolder.appImageView);
                        break;
                    case OfflineCache.CACHE_STATE_DOWNLOADING:
                        ImageDownloadModule.getInstance().displayImage(offlineCache.getCacheImageUrl(), viewHolder.appImageView);
                        break;
                    case OfflineCache.CACHE_STATE_FINISH:
                        viewHolder.numberCircleProgressBar.setVisibility(NumberCircleProgressBar.INVISIBLE);
                        viewHolder.frame.setVisibility(ImageView.INVISIBLE);
                        viewHolder.downloadArrow.setVisibility(ImageView.INVISIBLE);
                        ImageLoader.getInstance().displayImage(offlineCache.getCacheImageUrl(), viewHolder.appImageView, garyOptions);
                        break;
                    case OfflineCache.CACHE_STATE_ERROR:
                        ImageLoader.getInstance().displayImage(offlineCache.getCacheImageUrl(), viewHolder.appImageView, garyOptions);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void setViewData(final ViewHolder viewHolder, final int position) {
        final OfflineCache offlineCache = viewList.get(position);
        if (offlineCache != null) {
            final PackageInfo packageInfo = TPUtil.checkApkExist(context, offlineCache.getCachePackageName());
            if (packageInfo != null && packageInfo.versionCode >= offlineCache.getCacheVersionCode()) {
            } else {
                int state = offlineCache.getCacheDownloadState();
//                L.v(TAG, "setViewData", "name" + offlineCache.getCacheName() + "state" + state);
                switch (state) {
                    case OfflineCache.CACHE_STATE_DOWNLOADING:
                        viewHolder.numberCircleProgressBar.setVisibility(NumberCircleProgressBar.VISIBLE);
                        viewHolder.frame.setVisibility(ImageView.VISIBLE);
                        viewHolder.numberCircleProgressBar.setProgress((int) offlineCache.getCachePercentNum());
                        viewHolder.downloadArrow.setVisibility(ImageView.INVISIBLE);
                        break;
                    case OfflineCache.CACHE_STATE_NOT_DOWNLOAD:
                        viewHolder.numberCircleProgressBar.setVisibility(NumberCircleProgressBar.INVISIBLE);
                        viewHolder.frame.setVisibility(ImageView.INVISIBLE);
                        ImageLoader.getInstance().displayImage(offlineCache.getCacheImageUrl(), viewHolder.appImageView, garyOptions);
                        viewHolder.downloadArrow.setVisibility(ImageView.VISIBLE);
                        break;
                    default:
                        break;
                }
            }

            //添加离线缓存
            viewHolder.mItemRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    L.v(TAG, "setViewData", "onClick name=" + offlineCache.getCacheName());
//                    viewHolder.mItemRelativeLayout.setClickable(false);
                    Reporter.logInvokErp(offlineCache.getCacheName(), InvokErp.RECOMMEND_CLICK);
                    if (packageInfo != null && packageInfo.versionCode >= offlineCache.getCacheVersionCode()) {
//                        L.v(TAG,"setViewData",offlineCache.getCachePackageName());
                        TPUtil.startAppByPackegName(context, offlineCache.getCachePackageName());
                    } else {

                        if (!TPUtil.isNetOkWithToast()) {
                            return;
                        }

                        OfflineCache offlineCacheDownload = StorageModule.getInstance().getOfflineCacheById(offlineCache.getCacheID());
                        if (offlineCacheDownload != null && offlineCacheDownload.getCacheDownloadState() == OfflineCache.CACHE_STATE_FINISH) {
                            //安装应用
//                            downloadDialog = new DownloadDetailDialog(context, R.style.myDialog);
                            downloadDialog = new DownloadDialog(context, R.style.myDialog);
//                            AppDetail appDetail = offlineCache.getAppDetail();
//                            downloadDialog.setAppDetail(appDetail);
                            downloadDialog.setName(offlineCache.getCacheName());
                            downloadDialog.show();
                        } else {
                            if (StorageModule.getInstance().isOfflineCacheById(offlineCache.getCacheID())) {
                                TPUtil.showToast(R.string.app_already_downlaod);
                            } else {
//                                downloadDialog = new DownloadDetailDialog(context, R.style.myDialog);
                                downloadDialog = new DownloadDialog(context, R.style.myDialog);
//                                AppDetail appDetail = offlineCache.getAppDetail();
//                                downloadDialog.setAppDetail(appDetail);
                                downloadDialog.setName(offlineCache.getCacheName());
                                downloadDialog.setOnDialogClickListener(new DownloadDialog.OnDialogClickListener() {
                                    @Override
                                    public void onClick() {

                                        if (TPUtil.isNetOkWithToast()) {
                                            StorageModule.getInstance().addCache(offlineCache);
                                            ImageDownloadModule.getInstance().displayImage(offlineCache.getCacheImageUrl(), viewHolder.appImageView);
//                                    ImageLoader.getInstance().displayImage(offlineCache.getCacheImageUrl(), viewHolder.appImageView, options);
                                            viewHolder.numberCircleProgressBar.setVisibility(NumberCircleProgressBar.VISIBLE);
                                            viewHolder.frame.setVisibility(ImageView.VISIBLE);
                                            viewHolder.numberCircleProgressBar.setProgress((int) offlineCache.getCachePercentNum());
                                            viewHolder.downloadArrow.setVisibility(ImageView.INVISIBLE);
                                        }
                                    }
                                });
                                downloadDialog.show();
                            }
                        }
                    }
                }
            });
        }

//        viewHolder.mItemRelativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
//                                                                  @Override
//                                                                  public boolean onLongClick(View v) {
//                                                                      OfflineCache oc = viewList.get(position);
//                                                                      PackageInfo packageInfo = TPUtil.checkApkExist(context, oc.getCachePackageName());
//                                                                      if (packageInfo != null) {
//                                                                          Shortcut shortcut = new Shortcut();
//                                                                          shortcut.setName(oc.getCacheName());
//                                                                          shortcut.setShortcutPath(oc.getCachePackageName());
//                                                                          shortcut.setIsInstall(true);
//                                                                          shortcut.setShortcutType(Shortcut.APP_SHORTCUT_PAGE_TYPE);
//                                                                          Message message = new Message();
//                                                                          message.what = MainActivity.MSG_WHAT_ADD_APP;
//                                                                          message.obj = shortcut;
//                                                                          EventBus.getDefault().post(message);
//                                                                      }
//                                                                      return false;
//                                                                  }
//                                                              }
//
//        );

    }

    public void dismissDialog() {
        if (downloadDialog != null && downloadDialog.isShowing()) {
            downloadDialog.dismiss();
        }
    }


    static class ViewHolder {

        @InjectView(R.id.appItem)
        RelativeLayout mItemRelativeLayout;
        @InjectView(R.id.item_appImage)
        ImageView appImageView;
        @InjectView(R.id.item_appName)
        TextView appTextView;
        @InjectView(R.id.item_circle_progress)
        NumberCircleProgressBar numberCircleProgressBar;
        @InjectView(R.id.item_frame)
        ImageView frame;
        @InjectView(R.id.item_download_arrow)
        ImageView downloadArrow;

        ViewHolder(View convertView) {
            ButterKnife.inject(this, convertView);
        }

    }

    /**
     * 鏇存柊杩涘害
     *
     * @param offlineCache 杩涘害淇℃伅
     * @return void
     */
    public ArrayList<OfflineCache> setOfflineCache(OfflineCache offlineCache) {
        // L.v(TAG, "setOfflineCache", "name=" + offlineCache.getCacheName());
        Integer position = mFileOfflineCacheMap.get(offlineCache.getCacheID());
        if (position != null) {
            // 鏇存柊鏁版嵁
            viewList.set(position, offlineCache);
            updateView(position);
        }
        return viewList;
    }

    /**
     * 鐢ㄤ簬鏇存柊鎴戜滑鎯宠鏇存柊鐨刬tem
     *
     * @param position 鎯虫洿鏂癷tem鐨勪笅鏍?
     */
    private void updateView(int position) {
        //L.v(TAG, "updateView", "updateView");
        int firstVisiblePosition = mGridView.getFirstVisiblePosition();
        int childIndex = position - firstVisiblePosition;
        // 寰楀埌浣犻渶瑕佹洿鏂癷tem鐨刅iew
        View view = mGridView.getChildAt(childIndex);
        //L.v(TAG, "updateView", "updateView" + position + firstVisiblePosition + childIndex + view);
        if (view != null) {
            Object objHolder = view.getTag();
            if (objHolder instanceof ViewHolder) {
                ViewHolder viewHolder = (ViewHolder) objHolder;
                // L.v(TAG, "updateView", "name=" +
                // viewHolder.itemName.getText()
                // + viewHolder.seasonInfo.getText() + " position="
                // + position + " firstVisiblePosition="
                // + firstVisiblePosition + " childIndex=" + childIndex);
                // 璁剧疆鏁版嵁
                setViewData(viewHolder, position);
            } else {
                L.e(TAG, "updateView", "position=" + position
                        + " firstVisiblePosition=" + firstVisiblePosition
                        + " childIndex=" + childIndex + " objHolder="
                        + objHolder);
            }
        } else {
            // L.e(TAG, "updateView", "position=" + position
            // + " firstVisiblePosition=" + firstVisiblePosition
            // + " childIndex=" + childIndex + " view=null");
        }
    }

    DisplayImageOptions options;
    DisplayImageOptions garyOptions;

    /**
     * 设置常用的设置项
     *
     * @return
     */
    private void createOptions() {
        garyOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                .imageScaleType(ImageScaleType.EXACTLY)//解决内存溢出问题
                .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)//设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型
                .displayer(new GrayBitmapDisplayer())//设置灰度图
                .showImageOnLoading(R.drawable.ic_launcher)
                .resetViewBeforeLoading(true)
                .build();//构建完成
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)//设置下载的图片是否缓存在内存中
                .imageScaleType(ImageScaleType.EXACTLY)//解决内存溢出问题
                .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)//设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型
                .resetViewBeforeLoading(true)
                .displayer(new SimpleBitmapDisplayer())
                .build();//构建完成
    }
}
