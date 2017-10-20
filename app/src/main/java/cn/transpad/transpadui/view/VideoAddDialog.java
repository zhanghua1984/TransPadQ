package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.VideoDialogListAdapter;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.storage.StorageModule;

/**
 * Created by wangshaochun on 2015/4/15.
 */
public class VideoAddDialog extends Dialog {
    Context context;
    ArrayList<MediaFile> folderList;

    public VideoAddDialog(Context context) {
        super(context);
    }

    public VideoAddDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    @InjectView(R.id.music_dialog_listView)
    ListView folderListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        VideoDialogListAdapter videoDialogListAdapter = new VideoDialogListAdapter(context, folderListView);
        folderList=StorageModule.getInstance().getFolderList();
        videoDialogListAdapter.setMusicDialogList(folderList);
        folderListView.setAdapter(videoDialogListAdapter);
    }

    @OnClick(R.id.music_add_ok)
    public void addVideos() {
        //添加视频
//        Toast.makeText(getContext(),"添加未实现",Toast.LENGTH_SHORT).show();
        dismiss();
    }

    @OnClick(R.id.music_add_cancel)
    public void cancel() {
        dismiss();
    }

}
