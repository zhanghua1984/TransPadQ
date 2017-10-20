package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.graphics.RectF;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.List;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.cache.ImageDownloadModule;
import cn.transpad.transpadui.entity.MediaFile;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by ctccuser on 2015/4/4.
 */
public class PicturePlayAdapter extends PagerAdapter {
    private static final String TAG = PicturePlayAdapter.class.getSimpleName();
    Context context;
    List<MediaFile> mMediaFileList;
    private PhotoViewAttacher mAttacher;

    private DisplayImageOptions options = new DisplayImageOptions.Builder()
            .cacheInMemory(true)// 设置下载的图片是否缓存在内存中
            .cacheOnDisk(true)// 设置下载的图片是否缓存在SD卡中
            .considerExifParams(true) // 是否考虑JPEG图像EXIF参数（旋转，翻转）
            .imageScaleType(ImageScaleType.NONE_SAFE)// 设置图片以如何的编码方式显示
            .build();// 构建完成

    public PicturePlayAdapter(Context context, List<MediaFile> mediaFileList) {
        super();
        this.context = context;
        this.mMediaFileList = mediaFileList;
    }

    @Override
    public int getCount() {
        return mMediaFileList != null ? mMediaFileList.size() : 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_list, null);
        container.addView(view);
        PhotoView photoView = (PhotoView) view.findViewById(R.id.imgPicture);
        MediaFile mediaFile = mMediaFileList.get(position);
        String path = mediaFile.getMediaFilePath();
        if (!path.startsWith("http")) {
            path = "file://" + path;
        }
        ImageDownloadModule.getInstance().displayImage(path, photoView,options);
        // The MAGIC happens here!
        mAttacher = new PhotoViewAttacher(photoView);
        // Lets attach some listeners, not required though!
        mAttacher.setOnMatrixChangeListener(new MatrixChangeListener());
        mAttacher.setOnPhotoTapListener(new PhotoTapListener());
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View v = container.findViewById(position);
        if (v != null) {
            container.removeView(v);
        }
    }

    private class PhotoTapListener implements PhotoViewAttacher.OnPhotoTapListener {

        @Override
        public void onPhotoTap(View view, float x, float y) {
            float xPercentage = x * 100f;
            float yPercentage = y * 100f;
        }
    }

    private class MatrixChangeListener implements PhotoViewAttacher.OnMatrixChangedListener {

        @Override
        public void onMatrixChanged(RectF rect) {
        }
    }

}
