package cn.transpad.transpadui.main;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.DongleHomePagerAdapter;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.http.RstSerializer;
import cn.transpad.transpadui.http.SoftRst;
import cn.transpad.transpadui.receiver.TPApplicationReceiver;
import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Kongxiaojun on 2015/4/14.
 * Dongle Home
 */
public class DongleHomeFragment extends BaseFragment {


    private static final String TAG = "DongleHomeFragment";

    private View mView;

    @InjectView(R.id.dongle_home_pager)
    ViewPager viewPager;
    DongleHomePagerAdapter adapter;

    @InjectView(R.id.home_point_layout)
    LinearLayout pointLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        L.v(TAG, "onCreateView");
        if (mView == null) {
            mView = inflater.inflate(R.layout.home_dongle, null);
            ButterKnife.inject(this, mView);
            init();
        }else {
            if (adapter.getSoftRst() ==null){
                requestSoftRst();
            }else {
                adapter.updateAppItemViews();
            }
        }

        return mView;
    }

    private void init() {
        adapter = new DongleHomePagerAdapter(null, getActivity());
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updatePoint();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        requestSoftRst();
    }

    private void requestSoftRst(){

        final String soft = SharedPreferenceModule.getInstance().getString("softRst");
        if (!TextUtils.isEmpty(soft)){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RstSerializer rstSerializer = new RstSerializer();
                    SoftRst softRst = null;
                    try {
                        softRst = rstSerializer.fromString(SoftRst.class, soft);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (softRst != null) {
                        adapter.setSoftRst(softRst);
                        //初始化底部指示点
                        int count = adapter.getCount();
                        pointLayout.removeAllViews();
                        for (int i = 0; i < count; i++) {
                            ImageView imageView = new ImageView(getActivity());
                            imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            pointLayout.addView(imageView);
                        }
                        updatePoint();
                    }
                }
            });
        }

        Request.getInstance().soft("0", new Callback<SoftRst>() {
            @Override
            public void success(SoftRst softRst, Response response) {
                if (softRst.result == 0 && getActivity() != null) {
                    RstSerializer rstSerializer = new RstSerializer();
                    SharedPreferenceModule.getInstance().setString("softRst", rstSerializer.toString(softRst));
                    adapter.setSoftRst(softRst);
                    //初始化底部指示点
                    int count = adapter.getCount();
                    pointLayout.removeAllViews();
                    for (int i = 0; i < count; i++) {
                        ImageView imageView = new ImageView(getActivity());
                        imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        pointLayout.addView(imageView);
                    }
                    updatePoint();
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void onEventMainThread(Message msg) {
        switch (msg.what) {
            case StorageModule.MSG_FILE_CACHE_UPDATE_PROGRESS_SUCCESS:
                OfflineCache oc = msg.getData().getParcelable(OfflineCache.OFFLINE_CACHE);
                // L.v("onEventMainThread", "alreadySize=" + oc.getCacheAlreadySize() + " totalSize=" + oc.getCacheTotalSize() + " percentNum=" + oc.getCachePercentNum());
                if (adapter != null) {
                    adapter.updateDownloadProgress(oc);
                }
                break;
            case TPApplicationReceiver.MSG_WHAT_APPLICATION_INSTALL://应用安装了
                L.v(TAG,"MSG_WHAT_APPLICATION_INSTALL  " + msg.obj.toString());
                if (adapter != null){
                    adapter.applicationUpdate(true,msg.obj.toString());
                }
                break;
            case TPApplicationReceiver.MSG_WHAT_APPLICATION_UNINSTALL://应用卸载了
                L.v(TAG,"MSG_WHAT_APPLICATION_UNINSTALL  " + msg.obj.toString());
                if (adapter != null){
                    adapter.applicationUpdate(false,msg.obj.toString());
                }
                break;
        }
    }

    public void setCurrentPage(int page){
        L.v(TAG,"setCurrentPage");
        if (viewPager != null){
            viewPager.setCurrentItem(page);
        }
    }

    /**
     * 更新指示点
     */
    private void updatePoint() {
        int selectItem = viewPager.getCurrentItem();
        for (int i = 0; i < pointLayout.getChildCount(); i++) {
            ImageView iv = (ImageView) pointLayout.getChildAt(i);
            if (selectItem == i) {
                iv.setImageResource(R.drawable.main_point_select);
            } else {
                iv.setImageResource(R.drawable.main_point_unselect);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //隐藏所有Dialog
        if (adapter != null){
            adapter.dismessAllDialog();
        }
    }

    @Override
    public void onDestroy() {
        L.v(TAG, "onDestroy");
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
