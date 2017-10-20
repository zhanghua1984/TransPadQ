package cn.transpad.transpadui.main;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnPageChange;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.MainViewAdapter;
import cn.transpad.transpadui.entity.InvokErp;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.http.Reporter;
import cn.transpad.transpadui.receiver.TPApplicationReceiver;
import cn.transpad.transpadui.service.TransPadService;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.BandUtil;
import cn.transpad.transpadui.util.L;
import de.greenrobot.event.EventBus;

/**
 * 首页中的滚动控件
 * Created by wangyang on 2015/11/20.
 */
public class MainFragmentLite extends BaseFragment {
    private static final String TAG = "MainFragmentLite";
    private static final String CURRENT_POSITION = "current_position";
    private static final int MSG_WHAT_ADD_ITME = 10001;
    private static final int MSG_WHAT_ADD_NOAPPVIEW = 10002;
    public static final int MSG_WHAT_HOME = 10003;
    public static final int MSG_WHAT_JUMP_PAGE = 10004;
    public static final int MSG_WHAT_DEMONSTRATION = 10005;

    private static final String TITLE_SELECT_COLOR = "#FFFFFF";
    private static final String TITLE_UN_SELECT_COLOR = "#FF8400";
    private static final String RECOMMENDED_ON = "1";

    private View mView;
    private MainViewAdapter mainViewAdapter;
    private MultimediaView multimediaView;
    private RecommendView recommendView;

    @InjectView(R.id.viewpager)
    ViewPager mainViewPager;
    @InjectView(R.id.iv_link)
    ImageView iv_link;
    @InjectView(R.id.imgLogo)
    ImageView imageLogo;

    private List<LinearLayout> viewGroupList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.v(TAG, "onCreate");
        EventBus.getDefault().register(this);
        initData();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        L.v(TAG, "onCreateView", "mView=" + mView);
        if (mView == null) {
            mView = inflater.inflate(R.layout.home_page_lite, null);
            ButterKnife.inject(this, mView);
            initView();
        }
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        L.v(TAG, "onResume", "start");
        mainViewAdapter.refresh();
        mainViewAdapter.notifyDataSetChanged();
        if (recommendView != null) {
            L.v(TAG, "onResume", "updateRedDot");
            recommendView.updateRedDot();
        }
    }

    @Override
    public void onDestroy() {
        L.v(TAG, "onDestroy");
        super.onDestroy();
        if (multimediaView != null) {
            multimediaView.onDestroy();
        }
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        L.v(TAG, "initView", "start");
        L.v(TAG, "viewGroupList size = " + viewGroupList.size());
        mainViewAdapter = new MainViewAdapter(getActivity(), viewGroupList);
        mainViewPager.setOffscreenPageLimit(3);
        mainViewPager.setAdapter(mainViewAdapter);
        if (TransPadService.isConnected()) {
            iv_link.setImageResource(R.drawable.close_screen_bg);
        } else {
            iv_link.setImageResource(R.drawable.tpq_home_page_link);
        }
        if (RECOMMENDED_ON.equals(TransPadApplication.getTransPadApplication().getRec())) {
            titleRecommendedLayout.setVisibility(View.VISIBLE);
        }
        imageLogo.setImageResource(getActivity().getPackageName().equals("cn.transpad.transpadui.lite.aoc") ? R.drawable.logo_image_aoc : R.drawable.logo_lite);
        updateTitle(0);
    }

    @InjectView(R.id.title_my_app)
    LinearLayout titleMyAppLayout;
    @InjectView(R.id.title_my_app_text)
    TextView titleMyApp;
    @InjectView(R.id.title_multimedia)
    LinearLayout titleMultiMediaLayout;
    @InjectView(R.id.title_multimedia_text)
    TextView titleMultiMedia;
    @InjectView(R.id.title_recommended)
    LinearLayout titleRecommendedLayout;
    @InjectView(R.id.title_recommended_text)
    TextView titleRecommended;


    @OnPageChange(R.id.viewpager)
    void onPageSelected(int position) {
        L.v(TAG, "onPageSelected", "position=" + position);
        updateTitle(position);
        switch (position) {
            case 0:
                Reporter.logInvokErp("", InvokErp.LITE_MY_APP);
                break;
            case 1:
                Reporter.logInvokErp("", InvokErp.LITE_MULTIMEDIA);
                break;
        }
    }

    public void updateTitle(int position) {
        switch (position) {
            case 0:
                titleMyAppLayout.setBackgroundResource(R.drawable.title_selected_background);
                titleMyApp.setTextColor(Color.parseColor(TITLE_SELECT_COLOR));
                titleMultiMediaLayout.setBackgroundResource(R.drawable.title_unselected_background);
                titleMultiMedia.setTextColor(Color.parseColor(TITLE_UN_SELECT_COLOR));
                titleRecommendedLayout.setBackgroundResource(R.drawable.title_unselected_background);
                titleRecommended.setTextColor(Color.parseColor(TITLE_UN_SELECT_COLOR));
                break;
            case 1:
                titleMyAppLayout.setBackgroundResource(R.drawable.title_unselected_background);
                titleMyApp.setTextColor(Color.parseColor(TITLE_UN_SELECT_COLOR));
                titleMultiMediaLayout.setBackgroundResource(R.drawable.title_selected_background);
                titleMultiMedia.setTextColor(Color.parseColor(TITLE_SELECT_COLOR));
                titleRecommendedLayout.setBackgroundResource(R.drawable.title_unselected_background);
                titleRecommended.setTextColor(Color.parseColor(TITLE_UN_SELECT_COLOR));
                break;
            case 2:
                titleMyAppLayout.setBackgroundResource(R.drawable.title_unselected_background);
                titleMyApp.setTextColor(Color.parseColor(TITLE_UN_SELECT_COLOR));
                titleMultiMediaLayout.setBackgroundResource(R.drawable.title_unselected_background);
                titleMultiMedia.setTextColor(Color.parseColor(TITLE_UN_SELECT_COLOR));
                titleRecommendedLayout.setBackgroundResource(R.drawable.title_selected_background);
                titleRecommended.setTextColor(Color.parseColor(TITLE_SELECT_COLOR));
            default:
                break;
        }
    }

    @OnClick({R.id.title_multimedia, R.id.title_my_app, R.id.title_recommended})
    void onTitleClick(View view) {
        switch (view.getId()) {
            case R.id.title_my_app:
                updateTitle(0);
                mainViewPager.setCurrentItem(0);
                break;
            case R.id.title_multimedia:
                updateTitle(1);
                mainViewPager.setCurrentItem(1);
                break;
            case R.id.title_recommended:
                updateTitle(2);
                mainViewPager.setCurrentItem(2);
            default:
                break;
        }
    }

    private void initData() {
        viewGroupList.add(new MyAppView(getActivity()));
        multimediaView = new MultimediaView(getActivity());
        viewGroupList.add(multimediaView);
        L.v(TAG, "initData", TransPadApplication.getTransPadApplication().getRec());
        if (RECOMMENDED_ON.equals(TransPadApplication.getTransPadApplication().getRec())) {
            recommendView = new RecommendView(getActivity());
            viewGroupList.add(recommendView);
        }
    }

    @OnClick(R.id.rl_link)
    public void link() {
        BandUtil.showConnectBeforeDialog();
    }

    @OnClick(R.id.rl_set)
    public void settings() {
        Fragment fragment = new LiteSettingsFragment();
        LiteHomeActivity.switchFragment(fragment);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (multimediaView != null) {
            multimediaView.onStop();
        }
        if (recommendView != null) {
            recommendView.dismissDialog();
        }
    }


    public void onEventMainThread(Message message) {
        OfflineCache offlineCache;
        switch (message.what) {
            case TransPadService.TRANSPAD_STATE_CONNECTED:
                com.fone.player.L.v(TAG, "onEventMainThread", "TRANSPAD_STATE_CONNECTED");
                iv_link.setImageResource(R.drawable.close_screen_bg);
                break;
            case TransPadService.TRANSPAD_STATE_DISCONNECTED:
                //tvWifiTitle.setVisibility(View.GONE);
                iv_link.setImageResource(R.drawable.tpq_home_page_link);
                break;
            case StorageModule.MSG_FILE_CACHE_UPDATE_PROGRESS_SUCCESS:
                offlineCache = message.getData().getParcelable(OfflineCache.OFFLINE_CACHE);
                //L.v(TAG, "onEventMainThread", "name=" + offlineCache.getCacheName() + " state=" + offlineCache.getCacheDownloadState());
//                if (downloadView != null) {
//                    downloadView.updateDownloadProgress(offlineCache);
//                }
                if (recommendView != null) {
                    recommendView.updateDownloadProgress(offlineCache);
                }
                break;
            case StorageModule.MSG_DELETE_CACHE_SUCCESS:
//                updateTitle();
                offlineCache = message.getData().getParcelable(OfflineCache.OFFLINE_CACHE);
                if (offlineCache != null) {
                    L.v(TAG, "onEventMainThread", "删除应用 name=" + offlineCache.getCacheName() + "state=" + offlineCache.getCacheDownloadState());
                    offlineCache.setCacheDownloadState(OfflineCache.CACHE_STATE_NOT_DOWNLOAD);
                    offlineCache.setCacheAlreadySize(0);
                    if (recommendView != null) {
                        recommendView.updateDownloadProgress(offlineCache);
                    }
                }
                break;
            case StorageModule.MSG_ADD_CACHE_FAIL:
            case StorageModule.MSG_ADD_CACHE_SUCCESS:
            case TPApplicationReceiver.MSG_WHAT_APPLICATION_INSTALL://应用安装了
            case TPApplicationReceiver.MSG_WHAT_APPLICATION_UNINSTALL://应用卸载了
                if (recommendView != null) {
                    recommendView.updateRedDot();
                }
                break;
//            case MainActivity.MSG_WHAT_ADD_APP://添加应用到桌面
//                Shortcut shortcut = (Shortcut) message.obj;
//                addApp(shortcut);
//                SharedPreferenceModule.getInstance().setInt(CURRENT_POSITION, 3);
//                break;
//            case MainActivity.MSG_WHAT_PAGE_DELETED://page删除了
////                initPoint();
//                break;
//            case MainActivity.MSG_WHAT_ADD_NOITEMVIEW://添加一个默认视图
//                Message msg1 = mHandler.obtainMessage(MSG_WHAT_ADD_NOAPPVIEW);
//                msg1.arg1 = message.arg1;
//                mHandler.sendMessageDelayed(msg1, 200);
//                break;
//            case MSG_WHAT_HOME:
//                L.v(TAG, "onEventMainThread", "MSG_WHAT_HOME");
//                boolean isLock = SharedPreferenceModule.getInstance().getBoolean(MainViewPager.IS_LOCK_SCREEN);
//                if (isLock) {
//
//                } else {
//                    //首页
//                    switchPage(2);
//                }
//                break;
//            case MSG_WHAT_DEMONSTRATION:
//                //我的演示
//                switchPage(4);
//                break;
//            case MSG_WHAT_JUMP_PAGE:
//                L.v(TAG, "onEventMainThread", "MSG_WHAT_JUMP_PAGE");
//                int currentPosition = message.arg1;
//                switchPage(currentPosition);
//                break;
            default:
                break;
        }
    }

}
