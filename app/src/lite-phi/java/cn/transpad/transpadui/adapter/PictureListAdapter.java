package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.cache.ImageDownloadModule;
import cn.transpad.transpadui.entity.MediaFile;

/**
 * Created by Kongxiaojun on 2016/1/12.
 */
public class PictureListAdapter extends BaseAdapter {
    private final static String TAG = PictureListAdapter.class.getSimpleName();
    private Context mContext;
    List<MediaFile> pictureList;

    public PictureListAdapter(Context context,List<MediaFile> pictureList) {
        this.mContext = context;
        this.pictureList = pictureList;
    }

    @Override
    public int getCount() {
        return pictureList != null ? pictureList.size() : 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.picturelist_gv_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }
        setViewData(viewHolder, position);
        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return pictureList != null ? pictureList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setViewData(ViewHolder viewHolder, int position) {
        MediaFile mediaFile = pictureList.get(position);
        String path = "file://"+mediaFile.getMediaFilePath();
        ImageDownloadModule.getInstance().displayImage(path, viewHolder.iv_image);
    }

    static class ViewHolder {
        RoundedImageView iv_image;

        public ViewHolder(View convertView) {
            iv_image = (RoundedImageView) convertView.findViewById(R.id.iv_image);
        }
    }
}
