package cn.transpad.transpadui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.util.ModifyNameComparator;
import cn.transpad.transpadui.util.ModifyTimeComparator;

/**
 * Created by user on 2015/4/17.
 */
public class MusicPopWindow extends PopupWindow {
    public static final String TAG=MusicPopWindow.class.getSimpleName();
    View view;
    Context context;
    private SortClickListener mSortClickListener = null;
    //true:升序  false:降序
    private boolean mModifyTimeFlag = false;
    private boolean mModifyNameFlag = false;

    public MusicPopWindow(Context context) {
        super(context);
        this.context = context;
//        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        view=layoutInflater.inflate(R.layout.music_sort_popwindow,null);
        view = LayoutInflater.from(context).inflate(R.layout.music_sort_popwindow, null);
        ButterKnife.inject(this, view);
        setContentView(view);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(true);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
//        update();
//        setBackgroundDrawable(new ColorDrawable());

    }

    @OnClick(R.id.music_sort_time)
    public void sortByTime() {
        //按时间排序
        if (mSortClickListener != null) {
            if (mModifyTimeFlag) {
                mSortClickListener.modifyTimeClick(ModifyTimeComparator.SORT_UP_TYPE);
            } else {
                mSortClickListener.modifyTimeClick(ModifyTimeComparator.SORT_DOWN_TYPE);
            }
            Log.v(TAG,"mModifyTimeFlag"+mModifyTimeFlag);
            mModifyTimeFlag = !mModifyTimeFlag;
        }
        dismiss();
    }

    @OnClick(R.id.music_sort_name)
    public void sortByName() {
//        按名称排序
        if (mSortClickListener!=null){
            if (mModifyNameFlag){
                mSortClickListener.nameClick(ModifyNameComparator.SORT_UP_TYPE);
            }else {
                mSortClickListener.nameClick(ModifyNameComparator.SORT_DOWN_TYPE);
            }
            mModifyNameFlag=!mModifyNameFlag;
        }
        dismiss();
    }

    public void setSortClickListener(SortClickListener sortClickListener) {
        mSortClickListener = sortClickListener;
    }

    public interface SortClickListener {
        public void modifyTimeClick(int sortType);

        public void nameClick(int sortType);
    }
}
