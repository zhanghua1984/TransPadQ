package cn.transpad.transpadui.main;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.MultimediaAdapter;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.entity.OfflineCache;
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

public class MultimediaFragment extends BaseFragment {
    private static final String TAG = MultimediaFragment.class.getSimpleName();
    @InjectView(R.id.gv_menu)
    GridView gv_menu;

    View view;
    MultimediaAdapter multimediaAdapter;
    ArrayList<MediaFile> folderList;
    private CustomDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mediaAddDialog = new MediaAddDialog(getActivity(), R.style.myDialog);
        initDate();
    }

    @Override
    public void onResume() {
        super.onResume();
//        multimediaAdapter.setFolderList(folderList);
        multimediaAdapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.multimedia_page1, null, false);
        ButterKnife.inject(this, view);
        multimediaAdapter = new MultimediaAdapter(getActivity(), folderList);
        gv_menu.setAdapter(multimediaAdapter);
        initView();
        return view;
    }

    public void initView() {
        gv_menu.setSelector(new ColorDrawable(Color.TRANSPARENT));//设置点击条目无点击效果
        gv_menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0://打开放映厅的fragment
                        Fragment fragment = new VideoPlayFragment();
                        HomeActivity.switchFragment(fragment);
                        break;
                    case 1://打开音乐厅的fragment
                        Fragment fragment1 = new MusicPlayFragment();
                        HomeActivity.switchFragment(fragment1);
                        break;
                    case 2://打开美图秀的fragment
//                        Fragment fragment2 = new ImageListFragment();
//                        HomeActivity.switchFragment(fragment2);
                        try {
                            if (Build.BRAND.toLowerCase().equals("huawei") && SystemProperties.get("ro.confg.hw_systemversion").contains("V100R001C00B127SP03")) {
                                TPUtil.startAppByPackegName(getActivity(), "com.android.gallery3d");
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            if(android.os.Build.MODEL.contains("L50u")){
                                TPUtil.startAppByPackegName(getActivity(), "com.sonyericsson.album");
                                L.v(TAG, "索尼打开图库----");
                            }else {
                                Intent photo = new Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
//                            Intent photo =  new Intent(Intent.ACTION_VIEW, Uri.parse(
//                                    "content://media/internal/images/media"));
                                startActivity(photo);
                                L.v(TAG, "正常打开图库----");
                            }
//
                        } catch (Exception e) {
                            if (TPUtil.checkApkExist(getActivity(), "com.android.gallery3d") != null) {
                                TPUtil.startAppByPackegName(getActivity(), "com.android.gallery3d");
                            } else if (TPUtil.checkApkExist(getActivity(), "com.google.android.apps.photos") != null) {
                                TPUtil.startAppByPackegName(getActivity(), "com.google.android.apps.photos");
                            }
                        }


                        break;
                    case 3://打开新建文件夹的对话框
                        AddFolderDialog addFolderDialog = new AddFolderDialog(getActivity(), R.style.myDialog, multimediaAdapter, folderList);
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
    public void initDate() {
        folderList = new ArrayList<>();//文件夹的集合
        MediaFile videoMediaFile = new MediaFile();
        videoMediaFile.setMediaFileName(this.getString(R.string.video_play_item));
        //设置该对象的模式为视频类文件夹
        videoMediaFile.setMediaFileFolderType(MediaFile.MEDIA_FOLDER_VIDEO_TYPE);
        folderList.add(videoMediaFile);

        MediaFile audioMediaFile = new MediaFile();
        audioMediaFile.setMediaFileName(this.getString(R.string.music_play_item));
        //设置该对象的模式为音频类的文件夹
        audioMediaFile.setMediaFileFolderType(MediaFile.MEDIA_FOLDER_AUDIO_TYPE);
        folderList.add(audioMediaFile);

//        List<MediaFile> mediaFilesList = StorageModule.getInstance().getMediaFileList(MediaFile.MEDIA_FOLDER_TYPE);
//        if (mediaFilesList != null && mediaFilesList.size() > 0) {
//
//            folderList.addAll(mediaFilesList);
//        }
        MediaFile mediaFilesList = new MediaFile();
        mediaFilesList.setMediaFileName(this.getString(R.string.image_browse_item));
        //设置该对象的模式为自定义类型的文件夹
        mediaFilesList.setMediaFileFolderType(MediaFile.MEDIA_FOLDER_CUSTOM_TYPE);
        folderList.add(mediaFilesList);

//        MediaFile newMediaFile = new MediaFile();
//        newMediaFile.setMediaFileName("新建文件夹");
//        //设置该对象的模式为新建文件夹的模式
//        newMediaFile.setMediaFileFolderType(MediaFile.MEDIA_FOLDER_NEW_TYPE);
//        folderList.add(newMediaFile);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @OnClick(R.id.rl_back)
    public void backPage() {//返回上级页面的方法
        onBack();
    }

    @OnClick(R.id.rl_refresh)//刷新当前界面的方法
    public void refreshView() {

        StorageModule.getInstance().scanningExternalStorage();

        showScanProgressDialog();

    }

    MediaAddDialog mediaAddDialog;

    public void onEventMainThread(Message msg) {
        switch (msg.what) {
            case StorageModule.MSG_SCANNER_FILE_LIST_SUCCESS:
                L.v(TAG, "onEventMainThread", "MSG_SCANNER_FILE_LIST_SUCCESS start");
                dismissScanProgressDialog();
                Bundle bundle = msg.getData();
                ArrayList<MediaFile> mediaFileList = bundle.getParcelableArrayList(OfflineCache.OFFLINE_CACHE_LIST);
                if (mediaFileList != null && mediaFileList.size() > 0) {
                    if (mediaAddDialog != null && !mediaAddDialog.isShowing()) {
                        mediaAddDialog.setMediaList(mediaFileList);
                        mediaAddDialog.setAdapterNotifyChanged(multimediaAdapter);
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

    @Override
    public void onStop() {
        super.onStop();
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

        mProgressDialog = new CustomDialog(getActivity());
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
