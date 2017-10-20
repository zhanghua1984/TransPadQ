package cn.transpad.transpadui.main;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.TPQMainPagerAdapter;
import cn.transpad.transpadui.http.SoftRst;

/**
 * Created by Kongxiaojun on 2015/6/15.
 */
public class TPQFragment extends Fragment {

    @InjectView(R.id.tpq_pager)
    ViewPager pager;

    TPQMainPagerAdapter pagerAdapter;
    private SoftRst softRst;

    private boolean showmedia;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        softRst = TransPadApplication.getTransPadApplication().getSoftRst();
        showmedia = TransPadApplication.getTransPadApplication().getShowmedia().equals("1") ? true : false;
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.tpq_main, container, false);
        ButterKnife.inject(this, contentView);
        pagerAdapter = new TPQMainPagerAdapter(getActivity(),softRst, showmedia);
        pager.setAdapter(pagerAdapter);
        return contentView;
    }
}
