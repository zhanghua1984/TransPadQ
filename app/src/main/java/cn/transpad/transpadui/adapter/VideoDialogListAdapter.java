package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.MediaFile;

/**
 * Created by wangshaochun on 2015/4/16.
 */
public class VideoDialogListAdapter extends BaseAdapter {
    Context context;
    ListView listView;
    ArrayList<MediaFile> folderList;

    public VideoDialogListAdapter(Context context, ListView listView) {
        this.context = context;
        this.listView = listView;
    }

    public void setMusicDialogList(ArrayList<MediaFile> arrayList) {
        folderList = arrayList;
    }

    @Override
    public int getCount() {
        return folderList != null ? folderList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return folderList != null ? folderList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.music_dialog_list_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        setViewData(viewHolder, position);

        final MediaFile mediaFile = folderList.get(position);
        viewHolder.musicDialogListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                选中

                viewHolder.musicDialogListItemChoose.setVisibility(ImageView.VISIBLE);
            }
        });


        return convertView;

    }

    public void setViewData(ViewHolder viewHolder, int position) {
        MediaFile mediaFile = folderList.get(position);
        viewHolder.musicDialogListItemName.setText(mediaFile.getMediaFileName());
        viewHolder.musicDialogListItemChoose.setImageResource(R.drawable.videolist_add_choose);
    }

    static class ViewHolder {
        @InjectView(R.id.music_dialog_list_item)
        RelativeLayout musicDialogListItem;
        @InjectView(R.id.music_dialog_list_item_name)
        TextView musicDialogListItemName;
        @InjectView(R.id.music_dialog_list_item_choose)
        ImageView musicDialogListItemChoose;

        ViewHolder(View convertView) {
            ButterKnife.inject(this, convertView);
        }
    }
}
