package cn.transpad.transpadui.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.ImageListAdapter;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.view.PhotoViewPager;


public class ImageListFragment extends BaseFragment {
    private static final String TAG = ImageListFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        L.v(TAG, "onCreateView", "start");
        View view = inflater.inflate(R.layout.activity_image_list, container, false);
        ButterKnife.inject(this, view);
        L.v(TAG, "onCreateView", "view=" + view);
        PhotoViewPager viewPager = (PhotoViewPager) view.findViewById(R.id.vpPictureList);
        List<MediaFile> mMediaFileList = StorageModule.getInstance().getMediaFileList(MediaFile.MEDIA_IMAGE_TYPE);
        ImageListAdapter myImageListAdapter = new ImageListAdapter(getActivity(), mMediaFileList);
        //viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(myImageListAdapter);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.back)
    public void backPage() {//返回上级页面的方法
        onBack();
    }
}
