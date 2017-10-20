package cn.transpad.transpadui.main;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fone.player.L;

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
import cn.transpad.transpadui.adapter.VideoListAdapter;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.ModifyNameComparator;
import cn.transpad.transpadui.util.ModifyTimeComparator;
import cn.transpad.transpadui.util.ScreenUtil;
import cn.transpad.transpadui.view.MediaDeleteDialog;
import cn.transpad.transpadui.view.VideoPopWindow;
import de.greenrobot.event.EventBus;

/**
 * Created by wangshaochun on 2015/4/17.
 */
public class VideoPlayFragment extends BaseFragment {
    private static final String TAG = "VideoPlayFragment";
    View view;
    VideoPopWindow videoPopWindow;
    MediaDeleteDialog mediaDeleteDialog;
    ArrayList<MediaFile> videoList = new ArrayList<>();//视频集合
    VideoListAdapter videoListAdapter;

    @InjectView(R.id.gv_menu)
    GridView gv_menu;
    @InjectView(R.id.ll_sort)
    LinearLayout ll_sort;
//    @InjectView(R.id.video_choose_button)
//    ToggleButton video_choose_button;
//    @InjectView(R.id.video_choose_left)
//    LinearLayout video_choose_left;
//    @InjectView(R.id.video_choose_right)
//    LinearLayout video_choose_right;

    @Override
    public void onResume() {
        super.onResume();
        ListIterator<MediaFile> lit = videoList.listIterator();
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
                if (videoList.contains(tl)) {
                    videoList.remove(tl);
                }
            }
        }
        videoListAdapter.setVideoList(videoList);
        videoListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        videoListAdapter = new VideoListAdapter(getActivity(), gv_menu);

        //获取到视频类文件赋值给集合对象
        videoList = StorageModule.getInstance().getMediaFileList(MediaFile.MEDIA_VIDEO_TYPE);
        L.v(TAG, "ssss" + videoList.size());
//        videoList.addAll(StorageModule.getInstance().getMediaFileList(MediaFile.MEDIA_VIDEO_100TV_TYPE));
        videoPopWindow = new VideoPopWindow(getActivity());


        videoPopWindow.setVideoSortClickListener(new VideoPopWindow.VideoSortClickListener() {
            @Override
            public void modifyTimeClick(int sortType) {//根据文件更新时间排序的方法
                Collections.sort(videoList, new ModifyTimeComparator(sortType));
                videoListAdapter.setVideoList(videoList);
                videoListAdapter.notifyDataSetChanged();
            }

            @Override
            public void nameClick(int sortType) {//根据文件名称排序的方法
                Collections.sort(videoList, new ModifyNameComparator(sortType));
                videoListAdapter.setVideoList(videoList);
                videoListAdapter.notifyDataSetChanged();
            }

            @Override
            public void playTimeClick(int sortType) {//根据视频时长排序的方法      获取不到本地视频时长，暂时无法实现此功能
//                Collections.sort(videoList, new ModifyPlayTimeComparator(sortType));
//                videoListAdapter.setVideoList(videoList);
//                videoListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.multimedia_page2, container, false);
        ButterKnife.inject(this, view);

        Collections.sort(videoList, new ModifyTimeComparator(ModifyTimeComparator.SORT_DOWN_TYPE));
        videoListAdapter.setVideoList(videoList);
        gv_menu.setAdapter(videoListAdapter);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.ll_sort)
    public void openSortPopWindow() {//显示videoPopWindow的方法

        if (videoPopWindow.isShowing()) {
            videoPopWindow.dismiss();
        } else {
            videoPopWindow.showAsDropDown(ll_sort, ScreenUtil.dp2px(-97), ScreenUtil.dp2px(0));
        }

//        else if (ViewConfiguration.get(getActivity()).hasPermanentMenuKey()) {//判断虚拟按键是否存在
//            videoPopWindow.showAtLocation(iv_sort, Gravity.NO_GRAVITY, ScreenUtil.dp2px(450), ScreenUtil.dp2px(85));//不存在
//        } else {
//            videoPopWindow.showAtLocation(iv_sort, Gravity.NO_GRAVITY, ScreenUtil.dp2px(407), ScreenUtil.dp2px(85));//存在
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (videoPopWindow != null && videoPopWindow.isShowing()) {
            videoPopWindow.dismiss();
        }
        if (mediaDeleteDialog != null && mediaDeleteDialog.isShowing()) {
            mediaDeleteDialog.dismiss();
        }
    }

    @OnClick(R.id.ll_back)
    public void backpage() {//返回上级页面的方法
        onBack();
    }


    @OnCheckedChanged(R.id.video_choose_button)
    public void chooseChecked(boolean checked) {
        //打开选择列表
        if (checked) {
//            video_choose_button.setText(R.string.music_choose);
//            video_choose_left.setVisibility(LinearLayout.INVISIBLE);
//            video_choose_right.setVisibility(LinearLayout.INVISIBLE);
            videoListAdapter.setPlayMode(false);
        } else {
//            video_choose_button.setText(R.string.music_cancel);
//            video_choose_right.setVisibility(LinearLayout.VISIBLE);
//            videoListAdapter.setPlayMode(false);
            videoListAdapter.setPlayMode(true);
            final ArrayList<MediaFile> checkedVideoList = new ArrayList<MediaFile>();
            for (MediaFile mediaFile : videoList) {
                Log.v(TAG, mediaFile + "是否选中" + mediaFile.getMediaFileIsCheckedChoise());
                if (mediaFile.getMediaFileIsCheckedChoise()) {
                    checkedVideoList.add(mediaFile);
                }
            }
            if (checkedVideoList.size() == 0) {
                Toast.makeText(getActivity(), R.string.music_dialog_delete_nothing, Toast.LENGTH_SHORT).show();
                videoListAdapter.notifyDataSetChanged();
                return;
            }

            mediaDeleteDialog = new MediaDeleteDialog(getActivity(), R.style.myDialog);
            mediaDeleteDialog.setOnDeleteListener(new MediaDeleteDialog.OnDeleteListener() {
                @Override
                public void onDelete() {
                    if (checkedVideoList.size() != 0) {
                        for (MediaFile mediaFile : checkedVideoList) {
                            StorageModule.getInstance().deleteFileByFilePath(mediaFile.getMediaFilePath());
                            videoList.remove(mediaFile);
                        }
                    }
                    videoListAdapter.notifyDataSetChanged();
                }
            });
//            mediaDeleteDialog.setVideoList(videoList, checkedVideoList);
            mediaDeleteDialog.setMessage(String.format(getActivity().getString(R.string.music_delete_message), checkedVideoList.size()));
            mediaDeleteDialog.setContentView(R.layout.music_delet_dialog);
            Window dialogWindow = mediaDeleteDialog.getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.dimAmount = 0.5f;
            lp.y = ScreenUtil.dp2px(-10);
            mediaDeleteDialog.show();
        }
        for (MediaFile mediaFile : videoList) {
            mediaFile.setMediaFileIsCheckedChoise(false);
        }
        videoListAdapter.notifyDataSetChanged();
    }

//    @OnClick(R.id.video_choose_left)
//    public void addVideoList() {
//        //增加视频到文件夹
//        VideoAddDialog videoAddDialog = new VideoAddDialog(getActivity(), R.style.myDialog);
//        videoAddDialog.setContentView(R.layout.music_add_dialog);
//
//        Window dialogWindow = videoAddDialog.getWindow();
//        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//        lp.dimAmount = 0.5f;
//        lp.y = ScreenUtil.dp2px(-10);
//        videoAddDialog.show();
//    }

//    @OnClick(R.id.video_choose_right)
//    public void deleteVideoList() {
//        //删除视频
//        ArrayList<MediaFile> checkedVideoList = new ArrayList<>();
//        for (MediaFile mediaFile : videoList) {
//            if (mediaFile.getMediaFileIsCheckedChoise()) {
//                checkedVideoList.add(mediaFile);
//            }
//        }
//        if (checkedVideoList.size() == 0) {
//            Toast.makeText(getActivity(), R.string.music_dialog_delete_nothing, Toast.LENGTH_SHORT).show();
//            return;
//        }
//        mediaDeleteDialog = new MediaDeleteDialog(getActivity(), R.style.myDialog, videoListAdapter);
//        mediaDeleteDialog.setVideoList(videoList, checkedVideoList);
//        mediaDeleteDialog.setContentView(R.layout.music_delet_dialog);
//        Window dialogWindow = mediaDeleteDialog.getWindow();
//        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//        lp.dimAmount = 0.5f;
//        lp.y = ScreenUtil.dp2px(-10);
//        mediaDeleteDialog.show();
//    }

    public void onEventMainThread(Message msg) {
        switch (msg.what) {
            case StorageModule.MSG_ACTION_SCANNER_FINISHED:
                videoList = StorageModule.getInstance().getMediaFileList(MediaFile.MEDIA_VIDEO_TYPE);
                videoListAdapter.notifyDataSetChanged();
                break;
        }
    }
}
