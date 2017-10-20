package cn.transpad.transpadui.util;

import android.util.Log;

import java.util.Comparator;

import cn.transpad.transpadui.entity.MediaFile;

/**
 * Created by user on 2015/4/17.
 */
public class ModifyTimeComparator implements Comparator {
    public static final String TAG=ModifyTimeComparator.class.getSimpleName();
    public static final int SORT_UP_TYPE = 1;
    public static final int SORT_DOWN_TYPE = 2;
    private int mSortType;

    public ModifyTimeComparator(int sortType) {
        mSortType = sortType;
    }

    @Override
    public int compare(Object object1, Object object2) {
        MediaFile mediaFile1 = (MediaFile) object1;
        MediaFile mediaFile2 = (MediaFile) object2;
        int sort = 0;
        switch (mSortType) {
            case SORT_UP_TYPE:
                sort = mediaFile1.getMediaFileDateModified() >
                        mediaFile2.getMediaFileDateModified() ? 1
                        : (mediaFile1.getMediaFileDateModified() ==
                        mediaFile2.getMediaFileDateModified() ? 0 : -1);
                Log.v(TAG,"1:"+mediaFile1.getMediaFileDateModified()+" 2:"+mediaFile2.getMediaFileDateModified());
                break;
            case SORT_DOWN_TYPE:
                sort = mediaFile1.getMediaFileDateModified() >
                        mediaFile2.getMediaFileDateModified() ? -1
                        : (mediaFile1.getMediaFileDateModified() ==
                        mediaFile2.getMediaFileDateModified() ? 0 : 1);
                break;
        }

        return sort;
    }
}
