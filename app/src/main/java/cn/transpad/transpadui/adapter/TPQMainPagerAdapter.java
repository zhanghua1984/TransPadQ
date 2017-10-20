package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import cn.transpad.transpadui.http.SoftRst;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.view.DongleHomePage;
import cn.transpad.transpadui.view.HomePage2;
import cn.transpad.transpadui.view.TpqRecoAppView;

/**
 * Created by Kongxiaojun on 2015/6/15.
 */
public class TPQMainPagerAdapter extends PagerAdapter {

    private Context mContext;
    private SoftRst softRst;
    private boolean showmedia;

    public TPQMainPagerAdapter(Context context,SoftRst softRst, boolean showmedia) {
        this.mContext = context;
        this.softRst = softRst;
        this.showmedia = showmedia;
    }

    @Override
    public int getCount() {
        int colcount = 0;
        if (softRst != null && softRst.cols != null){
            colcount = softRst.cols.size();
        }
        return showmedia ? colcount + 2 : colcount + 1;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (softRst != null && softRst.cols != null && softRst.cols.size() > 0 && position < softRst.cols.size()) {
            L.v("aaaaaaaaaaa", "instantiateItem col position = " + position);
            TpqRecoAppView recoAppView = new TpqRecoAppView(mContext,softRst.cols.get(position),softRst.host,softRst.shost);
            container.addView(recoAppView);
            return recoAppView;
        }else {
            if (showmedia && position == getCount()-2){
                L.v("aaaaaaaaaaa", "instantiateItem col HomePage2");
                HomePage2 page2 = new HomePage2(mContext);
                container.addView(page2);
                return page2;
            }else {
                L.v("aaaaaaaaaaa","instantiateItem col DongleHomePage");
                DongleHomePage page = new DongleHomePage(mContext);
                container.addView(page);
                return page;
            }
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

}
