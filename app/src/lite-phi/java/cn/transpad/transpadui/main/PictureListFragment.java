package cn.transpad.transpadui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.PictureListAdapter;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;


/**
 * 图片列表
 */
public class PictureListFragment extends BaseFragment {

    private View contentView;

    @InjectView(R.id.gv_picture)
    GridView pictureGrid;

    private ArrayList<MediaFile> pictures;

    public PictureListFragment() {
        // Required empty public constructor
    }

    public static PictureListFragment newInstance() {
        PictureListFragment fragment = new PictureListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pictures = StorageModule.getInstance().getMediaFileList(MediaFile.MEDIA_IMAGE_TYPE);
        L.v("aaaaaaa",pictures.size()+"");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_picture_list, container, false);
        ButterKnife.inject(this,contentView);
        pictureGrid.setAdapter(new PictureListAdapter(TransPadApplication.getTransPadApplication(), pictures));
        return contentView;
    }

    @OnClick(R.id.ll_back)
    public void back(){
        onBack();
    }

    @OnItemClick(R.id.gv_picture)
    public void pictureItemClick(int position){
        PicturePlayFragment fragment = PicturePlayFragment.newInstance(pictures,position);
        LiteHomeActivity.switchFragment(fragment);
    }

}
