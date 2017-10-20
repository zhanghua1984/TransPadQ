package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.graphics.RectF;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cn.transpad.transpadui.cache.ImageDownloadModule;
import cn.transpad.transpadui.entity.MediaFile;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by ctccuser on 2015/4/4.
 */
public class ImageListAdapter extends PagerAdapter {
    private static final String TAG = "ImageListAdapter";
    private Context context;
    private List<MediaFile> mMediaFileList;

    public ImageListAdapter(Context context, List<MediaFile> mediaFileList) {
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
        PhotoView photoView = new PhotoView(container.getContext());
        MediaFile mediaFile = mMediaFileList.get(position);
        String path = mediaFile.getMediaFilePath();
        if (!path.startsWith("http")) {
            path = "file://" + path;
        }
        ImageDownloadModule.getInstance().displayImage(path, photoView);
        // The MAGIC happens here!
        PhotoViewAttacher mAttacher = new PhotoViewAttacher(photoView);
        // Lets attach some listeners, not required though!
        mAttacher.setOnMatrixChangeListener(new MatrixChangeListener());
        mAttacher.setOnPhotoTapListener(new PhotoTapListener());
        container.addView(photoView);
        return photoView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
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
