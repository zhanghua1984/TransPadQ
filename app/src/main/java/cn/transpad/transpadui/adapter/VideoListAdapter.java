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

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.cache.ImageDownloadModule;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.entity.MultipleVideo;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.PlayerUtil;

/**
 * Created by wangshaochun on 2015/4/17.
 */
public class VideoListAdapter extends BaseAdapter {
    private final static String TAG = VideoListAdapter.class.getSimpleName();
    private Context mContext;
    ArrayList<MediaFile> videoList;
    GridView gridView;
    MultipleVideo multipleVideo = null;
    int preChoosePosition = -1;
    boolean isPlayMode = true;

    public VideoListAdapter(Context context, GridView gridView) {
        this.mContext = context;
        this.gridView = gridView;
    }

    public void setPlayMode(boolean isPlayMode) {
        this.isPlayMode = isPlayMode;
    }

    //设置视频列表
    public void setVideoList(ArrayList<MediaFile> arrayList) {
        videoList = arrayList;
    }

    @Override
    public int getCount() {
        return videoList != null ? videoList.size() : 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.videolist_gv_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }
        setViewData(viewHolder, position);
        final MediaFile mediaFile = videoList.get(position);
        //设置条目的点击事件
        viewHolder.rl_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isPlayMode) {//根据播放模式进行不同点击效果的判断

                    //播放
                    ArrayList<MultipleVideo> multipleVideoList = MediaFile.parseMultipleVideoList(videoList);
                    PlayerUtil.openMultipleVideoPlayer(mContext, multipleVideoList, position);

                    if (preChoosePosition != position && preChoosePosition != -1) {
                        videoList.get(preChoosePosition).setMediaFileIsPlaying(false);
                    }
                    preChoosePosition = position;
                    VideoListAdapter.this.notifyDataSetChanged();
                } else {
                    if (mediaFile.getMediaFileIsCheckedChoise() == false) {
                        mediaFile.setMediaFileIsCheckedChoise(true);
                    } else {
                        mediaFile.setMediaFileIsCheckedChoise(false);
                    }
                    VideoListAdapter.this.notifyDataSetChanged();
                }
            }
        });


        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return videoList != null ? videoList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setViewData(ViewHolder viewHolder, int position) {
        MediaFile mediaFile = videoList.get(position);

        String VideoFilePath = null;
        //设置视频的缩略图
//        switch (mediaFile.getMediaFileType()) {
//            case MediaFile.MEDIA_VIDEO_100TV_TYPE:
//                VideoFilePath = mediaFile.getMediaFilePath();
        viewHolder.tv_image_description.setText(mediaFile.getMediaFileName());
//                break;
//            case MediaFile.MEDIA_VIDEO_TYPE:
//                if(multipleVideo!=null) {
//                    if(multipleVideo.getUrls().length>0) {
//                        VideoFilePath = multipleVideo.getUrls()[0];
//                    }
//                    viewHolder.tv_image_description.setText(multipleVideo.getName());
//                }
//                break;
//        }
        String path = "";
        switch (mediaFile.getMediaFileType()) {
            case MediaFile.MEDIA_VIDEO_100TV_TYPE:
                String[] url = mediaFile.getMediaFileFragmentUrlArray();
                if (url != null && url.length > 0) {
                    path = url[0];
                }
                break;
            case MediaFile.MEDIA_VIDEO_TYPE:
                path = mediaFile.getMediaFilePath();
                break;
        }
        L.v(TAG, "setViewData", "path=" + path);
        ImageDownloadModule.getInstance().displayVideoImage(path, R.drawable.default_220_184, 109, 92, viewHolder.iv_image);
        if (isPlayMode) {//根据模式判断是否显示图片
            viewHolder.iv_choose.setVisibility(ImageView.INVISIBLE);
        } else {
            viewHolder.iv_choose.setVisibility(ImageView.VISIBLE);
            if (mediaFile.getMediaFileIsCheckedChoise()) {
                viewHolder.iv_choose.setImageResource(R.drawable.media_choose);
            } else {
                viewHolder.iv_choose.setImageResource(R.drawable.media_unchoose);
            }
        }
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
