package cn.transpad.transpadui.adapter;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import cn.transpad.transpadui.util.ScreenUtil;

/**
 * Created by user on 2015/7/31.
 */
public class AppItemDecoration extends RecyclerView.ItemDecoration {
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(ScreenUtil.px2dp(22), ScreenUtil.px2dp(12), ScreenUtil.px2dp(22), ScreenUtil.px2dp(12));
    }
}
