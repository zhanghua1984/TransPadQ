package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.MusicListAdapter;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.storage.StorageModule;

/**
 * Created by ctccuser on 2015/4/6.
 */
public class MusicDeleteDialog extends Dialog {
    public static final String TAG = MusicDeleteDialog.class.getSimpleName();
    Context context;
    MusicListAdapter musicListAdapter;
    ArrayList<MediaFile> musicList;
    ArrayList<MediaFile> checkedMusicList = new ArrayList<MediaFile>();

    public MusicDeleteDialog(Context context) {
        super(context);
    }

    public MusicDeleteDialog(Context context, int theme, MusicListAdapter musicListAdapter) {
        super(context, theme);
        this.context = context;
        this.musicListAdapter = musicListAdapter;
    }

    public void setMusicList(ArrayList<MediaFile> musicList, ArrayList<MediaFile> checkedMusicList) {
        this.musicList = musicList;
        this.checkedMusicList = checkedMusicList;
//        for (MediaFile mediaFile : musicList) {
//            Log.v(TAG, mediaFile + "是否选中" + mediaFile.getMediaFileIsCheckedChoise());
//            if (mediaFile.getMediaFileIsCheckedChoise()) {
//                checkedMusicList.add(mediaFile);
//            }
//        }
    }

    @InjectView(R.id.music_dialog_delete_text)
    TextView deleteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        deleteTextView.setText(String.format(context.getString(R.string.music_delete_message), checkedMusicList.size()));

    }

    @OnClick(R.id.music_delete_ok)
    public void deleteMusics() {
//        删除音乐
        if (checkedMusicList.size() != 0) {
            for (MediaFile mediaFile : checkedMusicList) {
                StorageModule.getInstance().deleteFileByFilePath(mediaFile.getMediaFilePath());
                musicList.remove(mediaFile);
            }
            musicListAdapter.notifyDataSetChanged();
        }
        dismiss();
    }

    @OnClick(R.id.music_delete_cancel)
    public void cancel() {
        dismiss();
    }

}
