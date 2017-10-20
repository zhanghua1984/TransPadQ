package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import cn.transpad.transpadui.main.MyAppView;
import cn.transpad.transpadui.util.L;

/**
 * Created by user on 2015/12/25.
 */
public class MainViewAdapter extends PagerAdapter {
    public static final String TAG = MainViewAdapter.class.getSimpleName();
    Context context;
    List<LinearLayout> viewGroupList;

    public MainViewAdapter(final Context context,List<LinearLayout> viewGroupList) {
        this.context = context;
        this.viewGroupList = viewGroupList;
    }

    public void setViewGroups(List<LinearLayout> viewGroupList) {
        this.viewGroupList = viewGroupList;
        notifyDataSetChanged();
    }


//    @Override
//    public int getItemPosition(Object object) {
//        viewpager每次加载
//        View view = (View) object;
//        L.v(TAG, "getItemPosition", "" + object);
//
//        if (view.getTag() instanceof Integer) {
//            if ((Integer) view.getTag() == 1 || (Integer) view.getTag() == 2) {
//                L.v(TAG, "getItemPosition", "" + view.getTag());
//                return POSITION_NONE;
//            } else {
//                return POSITION_UNCHANGED;
//            }
//        }
//        return POSITION_NONE;
//    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        L.v(TAG,"instantiateItem position = " + position);
        container.addView(viewGroupList.get(position));
        return viewGroupList.get(position);
    }

    public void refresh(){
        MyAppView appView = (MyAppView) viewGroupList.get(0);
        appView.refreshMyApp();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(viewGroupList.get(position));
    }

    @Override
    public int getCount() {
        return viewGroupList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
