package cn.transpad.transpadui.main;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.MultimediaAdapter;
import cn.transpad.transpadui.entity.InvokErp;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.http.Reporter;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.ScreenUtil;
import cn.transpad.transpadui.util.TPUtil;
import cn.transpad.transpadui.view.AddFolderDialog;
import cn.transpad.transpadui.view.CustomDialog;
import cn.transpad.transpadui.view.MediaAddDialog;
import de.greenrobot.event.EventBus;

/**
 * Created by wangshaochun on 2015/4/16.
 */

public class MultimediaView extends LinearLayout {
    private static final String TAG = MultimediaView.class.getSimpleName();
    @InjectView(R.id.gv_menu)
    GridView gv_menu;

    MultimediaAdapter multimediaAdapter;
    ArrayList<MediaFile> folderList;
    private CustomDialog mProgressDialog;


    public MultimediaView(Context context) {
        super(context);
        createView();
    }

    public MultimediaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MultimediaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        createView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MultimediaView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        createView();
    }

    private void createView() {
        EventBus.getDefault().register(this);
        inflate(getContext(), R.layout.multimedia_page_lite, this);
        ButterKnife.inject(this);
        initDate();
        multimediaAdapter = new MultimediaAdapter(getContext(), folderList);
        gv_menu.setAdapter(multimediaAdapter);
        mediaAddDialog = new MediaAddDialog(getContext(), R.style.myDialog);

        initView();
    }


    public void resume() {
        multimediaAdapter.notifyDataSetChanged();
    }

    public void initView() {
        L.v(TAG, "initView");
        gv_menu.setSelector(new ColorDrawable(Color.TRANSPARENT));//设置点击条目无点击效果
        gv_menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0://打开放映厅的fragment
                        Reporter.logInvokErp("", InvokErp.LITE_VIDEOPLAY_ROOM);
                        Fragment fragment = new LiteVideoPlayFragment();
                        LiteHomeActivity.switchFragment(fragment);
                        break;
                    case 1://打开音乐厅的fragment
                        Reporter.logInvokErp("", InvokErp.LITE_AUDIOPLAY_ROOM);
                        Fragment fragment1 = new MusicPlayFragment();
                        LiteHomeActivity.switchFragment(fragment1);
                        break;
                    case 2://打开美图秀的fragment
                        Fragment fragment2 = PictureListFragment.newInstance();
                        LiteHomeActivity.switchFragment(fragment2);
                        break;
                    case 3://打开新建文件夹的对话框
                        AddFolderDialog addFolderDialog = new AddFolderDialog(getContext(), R.style.myDialog, multimediaAdapter, folderList);
                        addFolderDialog.setContentView(R.layout.new_folder);
                        Window dialoWindow = addFolderDialog.getWindow();
                        WindowManager.LayoutParams lp = dialoWindow.getAttributes();
                        lp.dimAmount = 0.5f;
                        lp.y = ScreenUtil.dp2px(-10);
                        addFolderDialog.show();
                        break;
                }
            }
        });

    }

    //初始化数据的方法
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void initDate() {
        folderList = new ArrayList<>();//文件夹的集合
        MediaFile videoMediaFile = new MediaFile();
        videoMediaFile.setMediaFileName(getContext().getString(R.string.video_play_item));
        //设置该对象的模式为视频类文件夹
        videoMediaFile.setMediaFileFolderType(MediaFile.MEDIA_FOLDER_VIDEO_TYPE);
        folderList.add(videoMediaFile);

        MediaFile audioMediaFile = new MediaFile();
        audioMediaFile.setMediaFileName(getContext().getString(R.string.music_play_item));
        //设置该对象的模式为音频类的文件夹
        audioMediaFile.setMediaFileFolderType(MediaFile.MEDIA_FOLDER_AUDIO_TYPE);
        folderList.add(audioMediaFile);

//        List<MediaFile> mediaFilesList = StorageModule.getInstance().getMediaFileList(MediaFile.MEDIA_FOLDER_TYPE);
//        if (mediaFilesList != null && mediaFilesList.size() > 0) {
//
//            folderList.addAll(mediaFilesList);
//        }
        MediaFile mediaFilesList = new MediaFile();
        mediaFilesList.setMediaFileName(getContext().getString(R.string.image_browse_item));
        //设置该对象的模式为自定义类型的文件夹
        mediaFilesList.setMediaFileFolderType(MediaFile.MEDIA_FOLDER_CUSTOM_TYPE);
        folderList.add(mediaFilesList);

//        MediaFile newMediaFile = new MediaFile();
//        newMediaFile.setMediaFileName("新建文件夹");
//        //设置该对象的模式为新建文件夹的模式
//        newMediaFile.setMediaFileFolderType(MediaFile.MEDIA_FOLDER_NEW_TYPE);
//        folderList.add(newMediaFile);
    }

    @OnClick(R.id.rl_refresh)//刷新当前界面的方法
    public void refreshView() {

        StorageModule.getInstance().scanningExternalStorage();

        showScanProgressDialog();

    }

    MediaAddDialog mediaAddDialog;


    public void onStop() {
        if (mediaAddDialog != null && mediaAddDialog.isShowing()) {
            mediaAddDialog.dismiss();
        }
        dismissScanProgressDialog();
    }

    // 展示全盘扫描时的对话框
    public void showScanProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

        mProgressDialog = new CustomDialog(getContext());
        mProgressDialog.show();

    }

    // 取消扫描时的对话框
    public void dismissScanProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        StorageModule.getInstance().cancelScanning();
    }

    public void onDestroy() {
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(Message message) {
        switch (message.what) {
            case StorageModule.MSG_SCANNER_FILE_LIST_SUCCESS:
                L.v(TAG, "onEventMainThread", "MSG_SCANNER_FILE_LIST_SUCCESS start");
                dismissScanProgressDialog();
                Bundle bundle = message.getData();
                ArrayList<MediaFile> mediaFileList = bundle.getParcelableArrayList(OfflineCache.OFFLINE_CACHE_LIST);
                if (mediaFileList != null && mediaFileList.size() > 0) {
                    if (mediaAddDialog != null && !mediaAddDialog.isShowing()) {
                        mediaAddDialog.setMediaList(mediaFileList);
                        mediaAddDialog.setAdapterNotifyChanged(multimediaAdapter);
//                        multimediaAdapter.notifyDataSetChanged();
//                    mediaAddDialog.setContentView(R.layout.media_add_dialog);
                        Window dialogWindow2 = mediaAddDialog.getWindow();
                        WindowManager.LayoutParams lp2 = dialogWindow2.getAttributes();
                        lp2.dimAmount = 0.5f;
                        lp2.y = ScreenUtil.dp2px(-10);
                        mediaAddDialog.show();
                    }
                } else {
                    TPUtil.showToast(R.string.no_scanning_file_message);
                }
                break;
        }
    }
}
