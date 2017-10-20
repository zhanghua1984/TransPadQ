package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.http.LinkvideoRst;
import cn.transpad.transpadui.util.TPUtil;

/**
 * Created by wangshaochun on 2015/5/6.
 */
public class VideoDetailsAdapter extends
        RecyclerView.Adapter<VideoDetailsAdapter.ViewHolder> {
    private OnItemClickLitener mOnItemClickLitener;
    private LayoutInflater mInflater;
    private List<LinkvideoRst.Rcmd> mLists;
    private LinkvideoRst linkvideoRst;
    private DisplayImageOptions options;
//    private DramaRst dramaRst;
//    private List<DramaRst.Cnts> sss;
    public VideoDetailsAdapter(Context context, List<LinkvideoRst.Rcmd> mlists, LinkvideoRst linkvideoRst) {
        mInflater = LayoutInflater.from(context);
        this.mLists = mlists;
        this.linkvideoRst = linkvideoRst;
    }
    //这是连续剧剧集需要的构造方法，暂时无用
//    public VideoDetailsAdapter(Context context,List<DramaRst.Cnts> sss,DramaRst dramaRst) {
//        mInflater = LayoutInflater.from(context);
//        this.sss = sss;
//        this.dramaRst = dramaRst;
//    }
    /**
     * ItemClick的回调接口
     *
     */
    public interface OnItemClickLitener
    {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View arg0) {
            super(arg0);
        }

        ImageView iv_image;
        TextView tv_image_description;
    }

    @Override
    public int getItemCount() {
        return mLists != null ? mLists.size() : 0;
    }

    /**
     * ViewHolder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.video_details_item,
                viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.iv_image = (ImageView) view
                .findViewById(R.id.iv_image);

        viewHolder.tv_image_description = (TextView) view.findViewById(R.id.tv_image_description);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        options = TPUtil.createDisplayImageOptionsByDrawableId(R.drawable.default_220_184);
        LinkvideoRst.Rcmd video = mLists.get(i);
        if(video!=null) {
            viewHolder.tv_image_description.setText(video.name);
            ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(linkvideoRst.host, linkvideoRst.shost, video.pic), viewHolder.iv_image, options);
        }
        //如果设置了回调，则设置点击事件
        if (mOnItemClickLitener != null)
        {
            viewHolder.iv_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickLitener.onItemClick(viewHolder.iv_image, i);
                }
            });

        }
    }
}
