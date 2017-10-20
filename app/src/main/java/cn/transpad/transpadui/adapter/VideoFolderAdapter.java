package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.http.SpecllistRst;
import cn.transpad.transpadui.util.TPUtil;

/**
 * Created by wangshaochun on 2015/5/13.
 */
public class VideoFolderAdapter extends BaseAdapter {
    private Context mContext;
    GridView gridView;
    List<SpecllistRst.Cnt> videoList;
    private DisplayImageOptions options;
    String videoUrl;
    SpecllistRst specllistRsts;
    public VideoFolderAdapter(Context context,GridView gridView) {
        this.mContext = context;
        this.gridView = gridView;
    }
    public void setSpecllistRst(SpecllistRst specllistRst){
        specllistRsts = specllistRst;
    }
    //设置视频列表
    public void setVideoList( List<SpecllistRst.Cnt> arrayList) {
        videoList = arrayList;
    }
    //设置视频列表
//    public void setVideoUrl(String videoUrl) {
//        this.videoUrl = videoUrl;
//    }
    @Override
    public int getCount() {
        return videoList != null ? videoList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.videolist_gv_item,null);
        final ViewHolder viewHolder = new ViewHolder(convertView);
        options = TPUtil.createDisplayImageOptionsByDrawableId(R.drawable.default_220_184);

        convertView.setTag(viewHolder);
        setViewData(viewHolder, position);
        return convertView;
    }
    public void setViewData(ViewHolder viewHolder, int position) {
       final SpecllistRst.Cnt cnt = videoList.get(position);
        viewHolder.tv_image_description.setText(cnt.name);
        //设置视频图
        ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(specllistRsts.host,specllistRsts.shost,cnt.pic1),viewHolder.iv_image,options);
    }
    static class ViewHolder {
        @InjectView(R.id.iv_image)
        RoundedImageView iv_image;
        @InjectView(R.id.tv_image_description)
        TextView tv_image_description;
        @InjectView(R.id.rl_item)
        RelativeLayout rl_item;
        @InjectView(R.id.iv_choose)
        ImageView iv_choose;
        public ViewHolder(View convertView) {
            ButterKnife.inject(this, convertView);
        }
    }
}
