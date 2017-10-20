package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.cache.ImageDownloadModule;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;
import cn.transpad.transpadui.view.DownloadItemDeleteDialog;

/**
 * Created by ctccuser on 2015/4/5.
 */
public class DownloadListAdapter extends BaseAdapter {
    private static final String TAG = DownloadListAdapter.class.getSimpleName();
    private Context context;
    private ArrayList<OfflineCache> viewList;
    private LayoutInflater layoutInflater;
    private LinkedHashMap<Long, Integer> mFileOfflineCacheMap = new LinkedHashMap<Long, Integer>();
    private ListView mListView = null;
    private DownloadItemDeleteDialog deleteDialog;

    public DownloadListAdapter(Context context, ListView listView) {
        this.context = context;
        mListView = listView;
        this.layoutInflater = LayoutInflater.from(context);
        deleteDialog = new DownloadItemDeleteDialog(context, R.style.myDialog);
    }

    public void setOfflineCacheList(ArrayList<OfflineCache> offlineCacheList) {
        viewList = offlineCacheList;
        if (viewList != null) {
            int position = 0;
            mFileOfflineCacheMap.clear();
            for (OfflineCache offlineCache : offlineCacheList) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
//        L.v(TAG, "getView" + viewList.get(position).getCacheName() +
//                "是否安装" + viewList.get(position).getCacheIsInstall());
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_downloadlist, null);
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
        ImageDownloadModule.getInstance().displayImage(viewList.get(position).getCacheImageUrl(), viewHolder.downloadImage, options);
    }

    private void setViewData(ViewHolder viewHolder, final int position) {
        final OfflineCache offlineCache = viewList.get(position);
        //L.v(TAG, "setViewData", offlineCache.getCacheName() + "是否安装" + offlineCache.getCacheIsInstall());
        if (offlineCache != null) {
//            ImageLoader.getInstance().displayImage(viewList.get(position).getCacheImageUrl(), viewHolder.downloadImage);
            viewHolder.downloadAppName.setText(viewList.get(position).getCacheName());
            viewHolder.downloadProgress.setProgress((int) viewList.get(position).getCachePercentNum());
            viewHolder.downloadSpeed.setText(viewList.get(position).getCacheSpeed() + "KB/S");
            viewHolder.downloadPercent.setText(viewList.get(position).getCachePercentNumString());

//            L.v(TAG, "setViewData", "getInstall" + offlineCache.getCacheName() +offlineCache.getCachePackageName()+ offlineCache.getCacheIsInstall());
            PackageInfo packageInfo = TPUtil.checkApkExist(context, offlineCache.getCachePackageName());
            //L.v(TAG, "setViewData", "packageInfo=" + packageInfo);

            if (packageInfo != null && packageInfo.versionCode >= offlineCache.getCacheVersionCode()) {
                //L.v(TAG, "setViewData", "packageInfoversioncode=" + packageInfo.versionCode);
                //L.v(TAG, "setViewData", "offlinecode=" + offlineCache.getCacheVersionCode());
                viewHolder.downloadInstall.setVisibility(View.INVISIBLE);
                viewHolder.downloadOpen.setVisibility(View.VISIBLE);
                viewHolder.downloadSpeed.setText("");
            } else {
                int downloadState = viewList.get(position).getCacheDownloadState();
                // L.v(TAG, "setViewData", "downloadstate1" + viewList.get(position).getCacheName() + downloadState);
                switch (downloadState) {
                    case OfflineCache.CACHE_STATE_WAITING:
                        viewHolder.mOperateButton.setImageResource(R.drawable.download_pause);
                        viewHolder.mOperateButton.setVisibility(View.VISIBLE);
                        viewHolder.downloadInstall.setVisibility(View.INVISIBLE);
                        viewHolder.downloadOpen.setVisibility(View.INVISIBLE);
                        viewHolder.downloadSpeed.setText(R.string.download_waiting);
                        break;
                    case OfflineCache.CACHE_STATE_DOWNLOADING:
                        viewHolder.mOperateButton.setImageResource(R.drawable.download_pause);
                        viewHolder.mOperateButton.setVisibility(View.VISIBLE);
                        viewHolder.downloadInstall.setVisibility(View.INVISIBLE);
                        viewHolder.downloadOpen.setVisibility(View.INVISIBLE);
                        break;
                    case OfflineCache.CACHE_STATE_PAUSE:
                    case OfflineCache.CACHE_STATE_PAUSE_USER:
                        viewHolder.mOperateButton.setImageResource(R.drawable.download_begin);
                        viewHolder.mOperateButton.setVisibility(View.VISIBLE);
                        viewHolder.downloadInstall.setVisibility(View.INVISIBLE);
                        viewHolder.downloadOpen.setVisibility(View.INVISIBLE);
                        viewHolder.downloadSpeed.setText(R.string.download_pause);
                        break;
                    case OfflineCache.CACHE_STATE_FINISH:
//                        viewHolder.downloadProgress.setProgress(100);
                        viewHolder.downloadSpeed.setText("");
                        viewHolder.mOperateButton.setVisibility(View.INVISIBLE);
                        viewHolder.downloadInstall.setVisibility(View.VISIBLE);
                        viewHolder.downloadOpen.setVisibility(View.INVISIBLE);
                        //viewHolder.mOperateButton.setEnabled(false);
                        break;
                    case OfflineCache.CACHE_STATE_ERROR:
//                        viewHolder.downloadProgress.setProgress(0);
                        viewHolder.mOperateButton.setImageResource(R.drawable.download_begin);
                        viewHolder.mOperateButton.setVisibility(View.VISIBLE);
                        viewHolder.downloadInstall.setVisibility(View.INVISIBLE);
                        viewHolder.downloadOpen.setVisibility(View.INVISIBLE);
                        viewHolder.downloadSpeed.setText(String.format(context.getString(R.string.download_error), offlineCache.getCacheErrorCode()));
                        break;
                }
            }

            viewHolder.mOperateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    OfflineCache offlineCache = viewList.get(position);
                    if (TPUtil.isNetOkWithToast()) {
                        switch (offlineCache.getCacheDownloadState()) {
                            case OfflineCache.CACHE_STATE_DOWNLOADING:
                            case OfflineCache.CACHE_STATE_WAITING:
                                L.v(TAG, "setViewData", "wating--->pause");
                                StorageModule.getInstance().pauseCache(offlineCache);
                                break;
                            case OfflineCache.CACHE_STATE_PAUSE:
                            case OfflineCache.CACHE_STATE_PAUSE_USER:
                            case OfflineCache.CACHE_STATE_ERROR:
                                StorageModule.getInstance().startCache(offlineCache);
                                break;
                        }
                    }
                }
            });
            viewHolder.downloadDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    deleteDialog = new DownloadItemDeleteDialog(context, R.style.myDialog);
                    deleteDialog.setClickListener(new DownloadItemDeleteDialog.ClickListener() {
                        @Override
                        public void onOk() {
                            OfflineCache offlineCache = viewList.remove(position);
                            StorageModule.getInstance().deleteCache(offlineCache);
//                            --------------------
//                            offlineCache.setCacheDownloadState(OfflineCache.CACHE_STATE_NOT_DOWNLOAD);
//                            offlineCache.setCacheAlreadySize(0);
//                            Message message = new Message();
//                            message.what = MSG_DELETE_OFFLINECACHE;
//                            Bundle bundle = new Bundle();
//                            bundle.putParcelable(OfflineCache.OFFLINE_CACHE,offlineCache);
//                            message.setData(bundle);
//                            EventBus.getDefault().post(message);
//                            ---------------------
                            setOfflineCacheList(viewList);
                            notifyDataSetChanged();
                            deleteDialog.dismiss();
                        }

                        @Override
                        public void onCancel() {
                            deleteDialog.dismiss();
                        }
                    });
                    deleteDialog.show();

//                gridAutoAdapter.notifyDataSetChanged();
                }
            });
            viewHolder.downloadInstall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StorageModule.getInstance().installApp(viewList.get(position).getCacheStoragePath());
                }
            });
            viewHolder.downloadOpen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TPUtil.startAppByPackegName(context, viewList.get(position).getCachePackageName());
                }
            });
        }

    }

    static class ViewHolder {
        @InjectView(R.id.downloadImage)
        ImageView downloadImage;
        @InjectView(R.id.download_progressbar)
        ProgressBar downloadProgress;
        @InjectView(R.id.btnOperate)
        ImageView mOperateButton;
        @InjectView(R.id.download_delete)
        ImageView downloadDelete;
        @InjectView(R.id.appName)
        TextView downloadAppName;
        @InjectView(R.id.speed)
        TextView downloadSpeed;
        @InjectView(R.id.download_install)
        TextView downloadInstall;
        @InjectView(R.id.download_open)
        TextView downloadOpen;
        @InjectView(R.id.percent)
        TextView downloadPercent;

        ViewHolder(View convertView) {
            ButterKnife.inject(this, convertView);
        }
    }


    public void dismissDialog() {
        if (deleteDialog != null && deleteDialog.isShowing()) {
            deleteDialog.dismiss();
        }
    }

    /**
     * 更新进度
     *
     * @param offlineCache 进度信息
     * @return void
     */
    public ArrayList<OfflineCache> setOfflineCache(OfflineCache offlineCache) {
        // 文件夹
        Integer position = mFileOfflineCacheMap.get(offlineCache.getCacheID());
        if (position != null && position < viewList.size()) {
            // 更新数据
            viewList.set(position, offlineCache);
            updateView(position);
        }
        return viewList;
    }

    /**
     * 用于更新我们想要更新的item
     *
     * @param position 想更新item的下标
     *                 *
     */

    private void updateView(int position) {
        int firstVisiblePosition = mListView.getFirstVisiblePosition();
        int childIndex = position - firstVisiblePosition;
        // 得到你需要更新item的View
        View view = mListView.getChildAt(childIndex);
        if (view != null) {
            Object objHolder = view.getTag();
            if (objHolder instanceof ViewHolder) {
                ViewHolder viewHolder = (ViewHolder) objHolder;
                // 设置数据
                setViewData(viewHolder, position);
//                setViewImageData(viewHolder, position);

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

    DisplayImageOptions options = new DisplayImageOptions.Builder()
            .cacheInMemory(true)//设置下载的图片是否缓存在内存中
            .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中
            .imageScaleType(ImageScaleType.IN_SAMPLE_INT)//设置图片以如何的编码方式显示
            .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型
            .resetViewBeforeLoading(true)
            .showImageOnLoading(R.drawable.ic_launcher)
            .displayer(new SimpleBitmapDisplayer())
            .build();//构建完成
}
