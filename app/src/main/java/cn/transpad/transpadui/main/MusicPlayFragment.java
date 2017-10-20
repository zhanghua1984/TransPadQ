package cn.transpad.transpadui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.MusicListAdapter;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.player.entity.AudioInfo;
import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.ModifyNameComparator;
import cn.transpad.transpadui.util.ModifyTimeComparator;
import cn.transpad.transpadui.util.PlayerUtil;
import cn.transpad.transpadui.util.ScreenUtil;
import cn.transpad.transpadui.view.LoadingDialog;
import cn.transpad.transpadui.view.MediaDeleteDialog;
import cn.transpad.transpadui.view.MusicAddDialog;
import cn.transpad.transpadui.view.MusicPopWindow;
import de.greenrobot.event.EventBus;

/**
 * Created by user on 2015/4/16.
 */
public class MusicPlayFragment extends BaseFragment {
    public static final String TAG = MusicPlayFragment.class.getSimpleName();
    private static final int MSG_INIT_MUSIC_LIST = 8001;
    Context context;
    View view;
    ArrayList<MediaFile> musicList;
    MusicPopWindow musicPopWindow;
    MusicListAdapter musicListAdapter;
    MusicAddDialog musicAddDialog;
    MediaDeleteDialog musicDeleteDialog;
    LoadingDialog loadingDialog;

    @InjectView(R.id.music_playlist)
    ListView listView;
    @InjectView(R.id.music_choose_button)
    ToggleButton musicChooseButton;
    //    @InjectView(R.id.music_choose_left)
//    LinearLayout musicAdd;
//    @InjectView(R.id.music_choose_right)
//    LinearLayout musicDelete;
//    @InjectView(R.id.music_list_sort)
//    LinearLayout musicSort;
    @InjectView(R.id.music_list_sort)
    LinearLayout musicSort;

    @Override
    public void onResume() {
        super.onResume();
        if (musicList != null) {
            ListIterator lit = musicList.listIterator();
            List<MediaFile> tempList = new ArrayList<>();
            while (lit.hasNext()) {
                MediaFile mediaFile = (MediaFile) lit.next();
                File file = new File(mediaFile.getMediaFilePath());
                if (!file.exists()) {
                    tempList.add(mediaFile);
                }
            }
            if (tempList.size() != 0) {
                for (MediaFile tl : tempList) {
                    if (musicList.contains(tl)) {
                        musicList.remove(tl);
                    }
                }
            }
            musicListAdapter.setMusicList(musicList);
            musicListAdapter.notifyDataSetChanged();
        }
    }

    boolean cancelThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
        EventBus.getDefault().register(this);
        showLoadingDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
//                musicList = new ArrayList<MediaFile>();
                cancelThread = false;
                ArrayList<MediaFile> originMusicList = StorageModule.getInstance().getMediaFileList(MediaFile.MEDIA_AUDIO_TYPE);
                String url = SharedPreferenceModule.getInstance().getString(MediaFile.CURRENT_PLAY_URL);
                if (originMusicList != null) {
                    for (MediaFile mediaFile : originMusicList) {
                        if (cancelThread) {
                            break;
                        }
                        if (mediaFile.getMediaFilePath().equals(url)) {
                            mediaFile.setMediaFileIsPlaying(true);
                        }
                        AudioInfo audioInfo = new AudioInfo();
                        audioInfo.path = mediaFile.getMediaFilePath();
                        PlayerUtil.readAudioHeader(context, audioInfo);
                        mediaFile.setMediaFileAuthor(audioInfo.artist != null ? audioInfo.artist : mediaFile.getMediaFileAuthor());
//                        L.v(TAG, "onCreate", "mediaFileAuthor" + mediaFile.getMediaFileAuthor());
                        if (mediaFile.getMediaFileAuthor() == null || mediaFile.getMediaFileAuthor().equals("<unknown>")) {
                            mediaFile.setMediaFileAuthor("V.A.");
                        }
                    }
                }
                musicList = originMusicList;
                Message message = new Message();
                message.what = MSG_INIT_MUSIC_LIST;
                EventBus.getDefault().post(message);
            }
        }).start();

    }

    private void showLoadingDialog() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            if (loadingDialog == null) {
                loadingDialog = new LoadingDialog(getActivity());
            }
            loadingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dismissLoadingDialog();
                        cancelThread = true;
                    }
                    return true;
                }
            });
            if (!loadingDialog.isShowing()) {
                loadingDialog.show();
            }
        }
    }

    private void dismissLoadingDialog() {
        if (getActivity() != null && !getActivity().isFinishing() && loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.music_list_layout, container, false);
        ButterKnife.inject(this, view);
        musicListAdapter = new MusicListAdapter(getActivity(), listView);
        musicPopWindow = new MusicPopWindow(getActivity());
        musicPopWindow.setSortClickListener(new MusicPopWindow.SortClickListener() {
            @Override
            public void modifyTimeClick(int sortType) {
                if (musicList != null) {
                    Collections.sort(musicList, new ModifyTimeComparator(sortType));
                    musicListAdapter.setMusicList(musicList);
                    musicListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void nameClick(int sortType) {
                if (musicList != null) {
                    Collections.sort(musicList, new ModifyNameComparator(sortType));
                    musicListAdapter.setMusicList(musicList);
                    musicListAdapter.notifyDataSetChanged();
                }
            }
        });
//        if (musicList != null) {
//            Collections.sort(musicList, new ModifyTimeComparator(ModifyTimeComparator.SORT_DOWN_TYPE));
//        sortMusicList(SORTBYDEFAULT);
//            musicListAdapter.setMusicList(musicList);
//        }
        listView.setAdapter(musicListAdapter);
        listView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (musicPopWindow != null && musicPopWindow.isShowing()) {
            musicPopWindow.dismiss();
        }
        if (musicAddDialog != null && musicAddDialog.isShowing()) {
            musicAddDialog.dismiss();
        }
        if (musicDeleteDialog != null && musicDeleteDialog.isShowing()) {
            musicDeleteDialog.dismiss();
        }
        dismissLoadingDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
//        if(musicPopWindow.isShowing()){
//            musicPopWindow.dismiss();
//        }
    }

    @OnClick(R.id.music_media_back)
    public void goBack() {
//        Toast.makeText(getActivity(), "dddd", Toast.LENGTH_SHORT).show();
        dismissLoadingDialog();
        onBack();
    }

    @OnClick(R.id.music_list_sort)
    public void openSortPopWindow() {
//        打开排序弹窗
        L.v(TAG, "openSortPopWindow", "" + musicPopWindow.isShowing() + musicPopWindow);

        if (musicPopWindow.isShowing()) {
            musicPopWindow.dismiss();
        } else {
            musicPopWindow.showAsDropDown(musicSort, ScreenUtil.dp2px(-97), ScreenUtil.dp2px(0));
        }
    }

    @OnCheckedChanged(R.id.music_choose_button)
    public void chooseChecked(boolean checked) {
//        Toast.makeText(getActivity(),"aaaa"+checked,Toast.LENGTH_SHORT).show();
//        打开选择列表
        if (checked) {
//            musicChooseButton.setText(R.string.music_choose);
//            musicAdd.setVisibility(LinearLayout.INVISIBLE);
//            musicDelete.setVisibility(LinearLayout.INVISIBLE);
            musicListAdapter.setPlayMode(false);
        } else {
            musicListAdapter.setPlayMode(true);
//            musicChooseButton.setText(R.string.music_cancel);
//            musicAdd.setVisibility(LinearLayout.VISIBLE);
//            musicDelete.setVisibility(LinearLayout.VISIBLE);
            final ArrayList<MediaFile> checkedMusicList = new ArrayList<MediaFile>();
            if (musicList != null) {
                for (MediaFile mediaFile : musicList) {
                    Log.v(TAG, mediaFile + "是否选中" + mediaFile.getMediaFileIsCheckedChoise());
                    if (mediaFile.getMediaFileIsCheckedChoise()) {
                        checkedMusicList.add(mediaFile);
                    }
                }
            }
            if (checkedMusicList.size() == 0) {
                Toast.makeText(getActivity(), R.string.music_dialog_delete_nothing, Toast.LENGTH_SHORT).show();
                musicListAdapter.notifyDataSetChanged();
                return;
            }
            musicDeleteDialog = new MediaDeleteDialog(getActivity(), R.style.myDialog);
            musicDeleteDialog.setOnDeleteListener(new MediaDeleteDialog.OnDeleteListener() {
                @Override
                public void onDelete() {
                    if (checkedMusicList.size() != 0) {
                        for (MediaFile mediaFile : checkedMusicList) {
                            StorageModule.getInstance().deleteFileByFilePath(mediaFile.getMediaFilePath());
                            musicList.remove(mediaFile);
                        }
                        musicListAdapter.notifyDataSetChanged();
                    }
                }
            });
            musicDeleteDialog.setMessage(String.format(context.getString(R.string.music_delete_message), checkedMusicList.size()));
            musicDeleteDialog.setContentView(R.layout.music_delet_dialog);
            Window dialogWindow = musicDeleteDialog.getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.dimAmount = 0.5f;
            lp.y = ScreenUtil.dp2px(-10);
            musicDeleteDialog.show();
        }
        if (musicList != null) {
            for (MediaFile mediaFile : musicList) {
                mediaFile.setMediaFileIsCheckedChoise(false);
            }
        }
        Log.v(TAG, "选择" + "notify");
        musicListAdapter.notifyDataSetChanged();

    }

//    本期不做添加
//    @OnClick(R.id.music_choose_left)
//    public void addMusicList() {
////        增加音乐到文件夹
//        musicAddDialog = new MusicAddDialog(getActivity(), R.style.myDialog);
//        musicAddDialog.setContentView(R.layout.music_add_dialog);
//        Window dialogWindow = musicAddDialog.getWindow();
//        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//        lp.dimAmount = 0.5f;
//        lp.y = ScreenUtil.dp2px(-10);
//        musicAddDialog.show();
//
//        ArrayList<MediaFile> checkedMusicList = new ArrayList<MediaFile>();
//        for (MediaFile mediaFile : musicList) {
//            Log.v(TAG, mediaFile + "是否选中" + mediaFile.getMediaFileIsCheckedChoise());
//            if (mediaFile.getMediaFileIsCheckedChoise()) {
//                checkedMusicList.add(mediaFile);
//            }
//        }
//        musicAddDialog.setMusicList(checkedMusicList);
//
//    }

//    @OnClick(R.id.music_choose_right)
//    public void deleteMusicList() {
////        删除音乐
////可以简化
//        ArrayList<MediaFile> checkedMusicList = new ArrayList<MediaFile>();
//        for (MediaFile mediaFile : musicList) {
//            Log.v(TAG, mediaFile + "是否选中" + mediaFile.getMediaFileIsCheckedChoise());
//            if (mediaFile.getMediaFileIsCheckedChoise()) {
//                checkedMusicList.add(mediaFile);
//            }
//        }
//        if (checkedMusicList.size() == 0) {
//            Toast.makeText(getActivity(), R.string.music_dialog_delete_nothing, Toast.LENGTH_SHORT).show();
//            return;
//        }
//        musicDeleteDialog = new MusicDeleteDialog(getActivity(), R.style.myDialog, musicListAdapter);
//        musicDeleteDialog.setMusicList(musicList, checkedMusicList);
//        musicDeleteDialog.setContentView(R.layout.music_delet_dialog);
//        Window dialogWindow = musicDeleteDialog.getWindow();
//        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//        lp.dimAmount = 0.5f;
//        lp.y = ScreenUtil.dp2px(-10);
//        musicDeleteDialog.show();
//
////        musicDeleteDialog.setMusicList(checkedMusicList);
//    }

    public void onEventMainThread(Message message) {
        switch (message.what) {
            case StorageModule.MSG_ACTION_SCANNER_AUDIO_FINISHED:
                L.v(TAG, "onEventMainThread", "yyyyyyyyyyyy");
                musicList = StorageModule.getInstance().getMediaFileList(MediaFile.MEDIA_AUDIO_TYPE);
                musicListAdapter.notifyDataSetChanged();
                break;
            case MSG_INIT_MUSIC_LIST:
                if (musicListAdapter != null) {
                    if (musicList != null) {
                        dismissLoadingDialog();
                        Collections.sort(musicList, new ModifyTimeComparator(ModifyTimeComparator.SORT_DOWN_TYPE));
                        musicListAdapter.setMusicList(musicList);
                        musicListAdapter.notifyDataSetChanged();
                    }
                }
                if (cancelThread) {
                    dismissLoadingDialog();
                    onBack();
                }
                break;
        }

    }
}
