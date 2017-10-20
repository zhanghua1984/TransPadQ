package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.MusicDialogListAdapter;
import cn.transpad.transpadui.entity.MediaFile;

/**
 * Created by ctccuser on 2015/4/6.
 */
public class MusicAddDialog extends Dialog {
    public static final String TAG = MusicAddDialog.class.getSimpleName();
    Context context;
    ArrayList<MediaFile> folderList;
    ArrayList<MediaFile> checkedMusicList;

    public MusicAddDialog(Context context) {
        super(context);
    }

    public MusicAddDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    public void setMusicList(ArrayList<MediaFile> checkedMusicList) {
        this.checkedMusicList = checkedMusicList;
    }

    @InjectView(R.id.music_dialog_listView)
    ListView folderListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        MusicDialogListAdapter musicDialogListAdapter = new MusicDialogListAdapter(context, folderListView);
//        folderList=...
        folderList = new ArrayList<MediaFile>();
        MediaFile mediaFile0 = new MediaFile();
        MediaFile mediaFile1 = new MediaFile();
        MediaFile mediaFile2 = new MediaFile();
        mediaFile0.setMediaFileName("我的最爱");
        mediaFile1.setMediaFileName("新歌速递");
        mediaFile2.setMediaFileName("港澳台");
        folderList.add(mediaFile0);
        folderList.add(mediaFile1);
        folderList.add(mediaFile2);

//        folderList=StorageModule.getInstance().getFolderList();
        musicDialogListAdapter.setMusicDialogList(folderList);
        folderListView.setAdapter(musicDialogListAdapter);
        folderListView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

    }

    @OnClick(R.id.music_add_ok)
    public void addMusics() {
//        添加音乐
//        ArrayList<MediaFile> checkedFolderList = new ArrayList<MediaFile>();
//        for(MediaFile folder:checkedFolderList){
//            if (folder.getMediaFileIsCheckedChoise()){
//                checkedFolderList.add(folder);
//            }
//        }
        MediaFile checkedFolder = null;
        for (MediaFile folder : folderList) {
            if (folder.getMediaFileIsCheckedChoise()) {
                checkedFolder = folder;
                break;
            }
        }
        if (checkedFolder != null && checkedMusicList.size() != 0) {
            for (MediaFile mediaFile : checkedMusicList) {
                mediaFile.setMediaFileParentName(checkedFolder.getMediaFileName());
            }
//            boolean success = StorageModule.getInstance().addMediaFileList(checkedMusicList);
//            L.v(TAG,"addMusics", "success " + success + "folder " + checkedFolder + "musiclist" + checkedMusicList);
        }
        dismiss();
    }

    @OnClick(R.id.music_add_cancel)
    public void cancel() {
        dismiss();
    }

}
