package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

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
import cn.transpad.transpadui.view.NumberCircleProgressBar;

/**
 * Created by kongxiaojun on 2015/2/14.
 * 应用列表适配器
 */
public class DongleAppAdapter implements DongleHomeAppView.DongeHomeAppAdapter {

    private static final String TAG = "DongleAppAdapter";
    private List<SoftRst.Cnt> cnts;
    private Context mContext;
    private DisplayImageOptions garyOptions;
    private String shost;
    private String host;
    private String title;

    public DongleAppAdapter(Context context, List<SoftRst.Cnt> cnts, String shost, String host, String title, DisplayImageOptions garyOptions) {
        this.mContext = context;
        this.cnts = cnts;
        this.shost = shost;
        this.host = host;
        this.title = title;
        this.garyOptions = garyOptions;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getCount() {
        L.v(TAG, "getItemCount");
        if (cnts != null) {
            L.v(TAG, "getItemCount = " + cnts.size());
            return cnts.size();
        } else {
            L.v(TAG, "getItemCount = 0");
            return 0;
        }
    }

    @Override
    public int getRowCount() {
        return 3;
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public int getItemId(int position) {
        return Integer.parseInt(cnts.get(position).id);
    }

    @Override
    public Object getItem(int postion) {
        if (cnts != null) {
            L.v(TAG, "getItemCount = " + cnts.size());
            return cnts.get(postion);
        } else {
            L.v(TAG, "getItemCount = 0");
            return null;
        }
    }

    @Override
    public View getView(final int i, View parentView) {

        View view = View.inflate(parentView.getContext(), R.layout.dongle_app_item, null);
        final ViewHolder viewHolder = new ViewHolder(view);
        //设置显示内容
        viewHolder.name.setText(cnts.get(i).name.toString().trim().replaceAll("\\xa0", "").replaceAll("\\x200B", ""));
        if (TPUtil.checkApkExist(mContext, cnts.get(i).pkname) == null) {//未安装
            ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(host, shost, cnts.get(i).pic1), viewHolder.icon, garyOptions);
            //未安装，点击下载
            SoftRst.Cnt cnt = cnts.get(i);
            OfflineCache offlineCache = StorageModule.getInstance().getOfflineCacheById(Integer.parseInt(cnt.id));
            if (offlineCache != null) {
                L.v(TAG, "offlineCache.getCacheAlreadySize() = " + offlineCache.getCacheAlreadySize());
                L.v(TAG, "offlineCache.getCacheDownloadState() = " + offlineCache.getCacheDownloadState());
                switch (offlineCache.getCacheDownloadState()) {
                    case OfflineCache.CACHE_STATE_DOWNLOADING://下载中
                    case OfflineCache.CACHE_STATE_PAUSE://暂停
                    case OfflineCache.CACHE_STATE_PAUSE_USER://暂停
                    case OfflineCache.CACHE_STATE_WAITING://等待
                        viewHolder.progressBar.setVisibility(View.VISIBLE);
                        viewHolder.frame.setVisibility(View.VISIBLE);
                        viewHolder.downlaodIcon.setVisibility(View.GONE);
                        viewHolder.progressBar.setMax(100);
                        viewHolder.progressBar.setProgress((int) offlineCache.getCachePercentNum());
                        break;
                    case OfflineCache.CACHE_STATE_FINISH://下载完成
                        viewHolder.progressBar.setVisibility(View.GONE);
                        viewHolder.frame.setVisibility(View.GONE);
                        viewHolder.downlaodIcon.setVisibility(View.GONE);
                        break;
                    default:
                        viewHolder.progressBar.setVisibility(View.GONE);
                        viewHolder.frame.setVisibility(View.GONE);
                        viewHolder.downlaodIcon.setVisibility(View.VISIBLE);
                        break;
                }
            }

        } else {//已安装
            viewHolder.downlaodIcon.setVisibility(View.GONE);
            //viewHolder.icon.setImageDrawable(TPUtil.getDrawableByPackageName(mContext, cnts.get(i).pkname));
            // String packageName = cnts.get(i).pkname;
            ImageDownloadModule.getInstance().displayImage(TPUtil.getAbsoluteUrl(host, shost, cnts.get(i).pic1), viewHolder.icon);
        }
//        viewHolder.icon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//        });
        return view;
    }

    @Override
    public boolean canDrag(int postion) {
        String pkName = cnts.get(postion).pkname;
        if (TPUtil.checkApkExist(mContext, pkName) == null) {
            return false;
        }
        return true;
    }

    @Override
    public void onDrageStart() {
    }

    @Override
    public void onDrageEnd(int startPos, int tartgerPos) {
    }

    public static class ViewHolder {
        public RoundedImageView icon;
        public TextView name;
        public ImageView frame;
        public NumberCircleProgressBar progressBar;
        public ImageView downlaodIcon;

        public ViewHolder(View itemView) {
            icon = (RoundedImageView) itemView.findViewById(R.id.application_image);
            name = (TextView) itemView.findViewById(R.id.application_name);
            frame = (ImageView) itemView.findViewById(R.id.application_frame);
            progressBar = (NumberCircleProgressBar) itemView.findViewById(R.id.application_progress);
            downlaodIcon = (ImageView) itemView.findViewById(R.id.app_download_icon);
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
}
