package cn.transpad.transpadui.util;

import android.util.Log;

import java.util.Comparator;

import cn.transpad.transpadui.entity.MediaFile;

/**
 * Created by wangshaochun on 2015/4/21.
 */
public class ModifyPlayTimeComparator implements Comparator {
    public static final String TAG=ModifyPlayTimeComparator.class.getSimpleName();
    public static final int SORT_UP_TYPE = 1;
    public static final int SORT_DOWN_TYPE = 2;
    private int mSortType;

    public ModifyPlayTimeComparator(int sortType) {
        mSortType = sortType;
    }

    @Override
    public int compare(Object object1, Object object2) {
        MediaFile mediaFile1 = (MediaFile) object1;
        MediaFile mediaFile2 = (MediaFile) object2;
        int sort = 0;
        switch (mSortType) {
            case SORT_UP_TYPE:
                sort = mediaFile1.getMediaFileDuration() >
                        mediaFile2.getMediaFileDuration() ? 1
                        : (mediaFile1.getMediaFileDuration() ==
                        mediaFile2.getMediaFileDuration() ? 0 : -1);
                Log.v(TAG,"1:"+mediaFile1.getMediaFileDuration()+" 2:"+mediaFile2.getMediaFileDuration());
                break;
            case SORT_DOWN_TYPE:
                sort = mediaFile1.getMediaFileDuration() >
                        mediaFile2.getMediaFileDuration() ? -1
                        : (mediaFile1.getMediaFileDuration() ==
                        mediaFile2.getMediaFileDuration() ? 0 : 1);
                Log.v(TAG,"2:"+mediaFile1.getMediaFileDuration()+" 2:"+mediaFile2.getMediaFileDuration());
                break;
        }

        return sort;
    }
}
