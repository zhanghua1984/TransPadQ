package cn.transpad.transpadui.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import cn.transpad.transpadui.util.L;

/**
 * Created by user on 2015/4/23.
 */
public class PhotoViewPager extends ViewPager {
    private static final String TAG = "PhotoViewPager";

    public PhotoViewPager(Context context) {
        super(context);
    }

    public PhotoViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            L.e(TAG, "onInterceptTouchEvent", "e = " + e);
            return false;
        }
    }
}
