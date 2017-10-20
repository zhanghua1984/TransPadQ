package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fone.player.L;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.storage.StorageModule;

/**
 * Created by wangshaochun on 2015/4/17.
 */
public class MultimediaAdapter extends BaseAdapter {
//    private static final int TYPE_COUNT = MediaFile.MEDIA_FOLDER_CUSTOM_TYPE + 1;
    private static final String TAG = "MultimediaAdapter";
    private Context mContext;
    ArrayList<MediaFile> folderList;

    public MultimediaAdapter(Context context,ArrayList<MediaFile> folderList) {
        this.mContext = context;
        this.folderList = folderList;
    }
    ArrayList<MediaFile> mediaFileList = new ArrayList<>();//视频集合

    @Override
    public int getItemViewType(int position) {
        return folderList.get(position).getMediaFileFolderType();
    }


    public void setFolderList(ArrayList<MediaFile> folderList) {
        this.folderList = folderList;
    }

    @Override
    public int getCount() {
        return folderList != null ? folderList.size() : 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            switch (getItemViewType(position)) {
                case MediaFile.MEDIA_FOLDER_VIDEO_TYPE://视频库
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.multimedia_gv_item1, null);
                    break;
                case MediaFile.MEDIA_FOLDER_AUDIO_TYPE://音乐厅
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.multimedia_gv_item2, null);
                    break;
                case MediaFile.MEDIA_FOLDER_CUSTOM_TYPE://美图库
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.multimedia_gv_item3, null);
                    break;
//                case MediaFile.MEDIA_FOLDER_NEW_TYPE://暂时无用---新建文件夹
//                    convertView = LayoutInflater.from(mContext).inflate(R.layout.multimedia_gv_item4, null);
//                    break;
            }

            viewHolder = new ViewHolder(convertView);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        switch (getItemViewType(position)) {
            case MediaFile.MEDIA_FOLDER_VIDEO_TYPE://视频库
                mediaFileList = StorageModule.getInstance().getMediaFileList(MediaFile.MEDIA_VIDEO_TYPE);
                cleanList();
                if(mediaFileList.size()>99) {
                    viewHolder.tv_size.setText("99+");
                }else{
                    viewHolder.tv_size.setText(String.valueOf(mediaFileList.size()));
                }
                L.v(TAG, "适配器执行了" + mediaFileList.size());
                break;
            case MediaFile.MEDIA_FOLDER_AUDIO_TYPE://音乐厅
                mediaFileList = StorageModule.getInstance().getMediaFileList(MediaFile.MEDIA_AUDIO_TYPE);
                cleanList();
                if(mediaFileList.size()>99) {
                    viewHolder.tv_size.setText("99+");
                }else{
                    viewHolder.tv_size.setText(String.valueOf(mediaFileList.size()));
                }
                break;
//            case MediaFile.MEDIA_FOLDER_CUSTOM_TYPE://美图库  暂时无用
//                mediaFileList = StorageModule.getInstance().getMediaFileList(MediaFile.MEDIA_IMAGE_TYPE);
//                viewHolder.tv_size.setText(String.valueOf(mediaFileList.size()));
//                break;
//            case MediaFile.MEDIA_FOLDER_NEW_TYPE://暂时无用---新建文件夹
//                convertView = LayoutInflater.from(mContext).inflate(R.layout.multimedia_gv_item4, null);
//                break;
        }

        setViewData(viewHolder, position);
        convertView.setTag(viewHolder);
        return convertView;
    }
    public void cleanList(){
        ListIterator<MediaFile> lit = mediaFileList.listIterator();
        List<MediaFile> tempList = new ArrayList<>();
        while (lit.hasNext()) {
            MediaFile mediaFile = lit.next();
            File file = new File(mediaFile.getMediaFilePath());
            if (!file.exists()) {
                tempList.add(mediaFile);
            }
        }
        if (tempList.size() != 0) {
            for (MediaFile tl : tempList) {
                if (mediaFileList.contains(tl)) {
                    mediaFileList.remove(tl);
                }
            }
        }
    }
    public void setViewData(ViewHolder viewHolder, int position) {
        MediaFile mediaFile = folderList.get(position);
        viewHolder.tv_image_description.setText(mediaFile.getMediaFileName());

    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    static class ViewHolder {
        @InjectView(R.id.tv_image_description)
        TextView tv_image_description;
        @InjectView(R.id.iv_image)
        ImageView iv_image;
        @InjectView(R.id.rl_layout)
        RelativeLayout rl_layout;
        @InjectView(R.id.tv_size)
        TextView tv_size;

        ViewHolder(View convertView) {
            ButterKnife.inject(this, convertView);
        }

    }
}
