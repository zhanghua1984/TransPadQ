package cn.transpad.transpadui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import cn.transpad.transpadui.main.TransPadApplication;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;
import cn.transpad.transpadui.view.NumberCircleProgressBar;

/**
 * Created by Kongxiaojun on 2015/6/15.
 */
public class TpqRecoAppAdapter extends RecyclerView.Adapter<TpqRecoAppAdapter.ViewHolder> {


    private static final String TAG = "TpqRecoAppAdapter";

    private List<SoftRst.Cnt> cnts;

    private String host;

    private String shost;

    private DisplayImageOptions garyOptions;

    public TpqRecoAppAdapter(List<SoftRst.Cnt> cnts, String host, String shost) {
        this.cnts = cnts;
        this.host = host;
        this.shost = shost;
        garyOptions = createGaryDisplayImageOptionsByDrawableId(R.drawable.ic_launcher);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tpq_recom_app_item,null);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //设置显示内容
        holder.name.setText(cnts.get(position).name.toString().trim().replaceAll("\\xa0", "").replaceAll("\\x200B", ""));
        if (TPUtil.checkApkExist(TransPadApplication.getTransPadApplication(), cnts.get(position).pkname) == null) {//未安装
            ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(host, shost, cnts.get(position).pic1), holder.icon, garyOptions);
            //未安装，点击下载
            SoftRst.Cnt cnt = cnts.get(position);
            OfflineCache offlineCache = StorageModule.getInstance().getOfflineCacheById(Integer.parseInt(cnt.id));
            if (offlineCache != null) {
                L.v(TAG, "offlineCache.getCacheAlreadySize() = " + offlineCache.getCacheAlreadySize());
                L.v(TAG, "offlineCache.getCacheDownloadState() = " + offlineCache.getCacheDownloadState());
                switch (offlineCache.getCacheDownloadState()) {
                    case OfflineCache.CACHE_STATE_DOWNLOADING://下载中
                    case OfflineCache.CACHE_STATE_PAUSE://暂停
                    case OfflineCache.CACHE_STATE_PAUSE_USER://暂停
                    case OfflineCache.CACHE_STATE_WAITING://等待
                        holder.progressBar.setVisibility(View.VISIBLE);
                        holder.frame.setVisibility(View.VISIBLE);
                        holder.downlaodIcon.setVisibility(View.GONE);
                        holder.progressBar.setMax(100);
                        holder.progressBar.setProgress((int) offlineCache.getCachePercentNum());
                        break;
                    case OfflineCache.CACHE_STATE_FINISH://下载完成
                        holder.progressBar.setVisibility(View.GONE);
                        holder.frame.setVisibility(View.GONE);
                        holder.downlaodIcon.setVisibility(View.GONE);
                        break;
                    default:
                        holder.progressBar.setVisibility(View.GONE);
                        holder.frame.setVisibility(View.GONE);
                        holder.downlaodIcon.setVisibility(View.VISIBLE);
                        break;
                }
            }

        } else {//已安装
            holder.downlaodIcon.setVisibility(View.GONE);
            //holder.icon.setImageDrawable(TPUtil.getDrawableByPackageName(TransPadApplication.getTransPadApplication(), cnts.get(position).pkname));
            ImageDownloadModule.getInstance().displayImage(TPUtil.getAbsoluteUrl(host, shost, cnts.get(position).pic1),holder.icon);
        }
    }

    @Override
    public int getItemCount() {
        return cnts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public RoundedImageView icon;
        public TextView name;
        public ImageView frame;
        public NumberCircleProgressBar progressBar;
        public ImageView downlaodIcon;

        public ViewHolder(View itemView) {
            super(itemView);
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
