package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.graphics.Color;
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
 * Created by user on 2015/4/16.
 */
public class MusicDialogListAdapter extends BaseAdapter {
    Context context;
    ListView listView;
    ArrayList<MediaFile> folderList;
    int preCheckedPosition = -1;

    public MusicDialogListAdapter(Context context, ListView listView) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.music_dialog_list_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        setViewData(viewHolder, position);

        return convertView;
    }

    public void setViewData(ViewHolder viewHolder, final int position) {

        if (folderList.get(position).getMediaFileIsCheckedChoise()) {
            viewHolder.musicDialogListItemName.setTextColor(Color.parseColor("#ff8400"));
            viewHolder.musicDialogListItemChoose.setVisibility(ImageView.VISIBLE);
        } else {
            viewHolder.musicDialogListItemName.setTextColor(Color.parseColor("#ffffff"));
            viewHolder.musicDialogListItemChoose.setVisibility(ImageView.INVISIBLE);
        }

        final MediaFile mediaFile = folderList.get(position);
        viewHolder.musicDialogListItemName.setText(mediaFile.getMediaFileName());

        viewHolder.musicDialogListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                选中
                if (position != preCheckedPosition && preCheckedPosition != -1) {
                    folderList.get(preCheckedPosition).setMediaFileIsCheckedChoise(false);
                }
                mediaFile.setMediaFileIsCheckedChoise(true);
                preCheckedPosition = position;
                notifyDataSetChanged();

            }
        });
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
