package cn.transpad.transpadui.main;


import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.laplanete.mobile.pageddragdropgrid.Item;
import ca.laplanete.mobile.pageddragdropgrid.OnPageChangedListener;
import ca.laplanete.mobile.pageddragdropgrid.PagedDragDropGrid;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.TpqPagedDragDropGridAdapter;
import cn.transpad.transpadui.entity.App;
import cn.transpad.transpadui.entity.NoApp;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.entity.Page;
import cn.transpad.transpadui.http.LoginRst;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.http.SoftRst;
import cn.transpad.transpadui.receiver.TPApplicationReceiver;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;
import cn.transpad.transpadui.view.LoadingDialog;
import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Tpq首页的Fragment
 * Created by Kongxiaojun on 2015/4/7.
 */
public class TpqHomeFragement extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "AutoHomeFragement";

    @InjectView(R.id.pageddragdropgrid)
    PagedDragDropGrid pagedDragDropGrid;

    @InjectView(R.id.home_point_layout)
    LinearLayout pointLayout;

    TpqPagedDragDropGridAdapter dragDropGridAdapter;

    Handler mHandler;

    private static final int MSG_WHAT_ADD_ITME = 1;
    private static final int MSG_WHAT_ADD_NOAPPVIEW = 2;

    SoftRst softRst;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.v(TAG, "onCreate");
        EventBus.getDefault().register(this);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_WHAT_ADD_ITME:
                        pagedDragDropGrid.scrollToPage(dragDropGridAdapter.pageCount() - 1);
                        break;
                    case MSG_WHAT_ADD_NOAPPVIEW:
                        Page page = dragDropGridAdapter.getPage(msg.arg1);
                        if (page != null) {
                            Item item = new NoApp();
                            page.addItem(item);
                        }
                        pagedDragDropGrid.notifyDataSetChanged();
                        break;
                }
            }
        };
        if (TransPadApplication.getTransPadApplication().getSoftRst() == null) {
            requestHomeData();
        }
    }

    @Override
    public void onDestroy() {
        L.v(TAG, "onDestroy");
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (dragDropGridAdapter != null) {
            dragDropGridAdapter.dismessDialog();
        }
        if (refrushDialog != null) {
            refrushDialog.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        L.v(TAG, "onCreateView");
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_home, null);
            ButterKnife.inject(this, mView);
            init();
        }
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        dismessLoadingDialog();
    }

    private void init() {

        dragDropGridAdapter = new TpqPagedDragDropGridAdapter(getActivity());
        pagedDragDropGrid.setRestorePage(1);
        pagedDragDropGrid.setAdapter(dragDropGridAdapter);
        pagedDragDropGrid.notifyDataSetChanged();
        pagedDragDropGrid.setClickListener(this);
        pagedDragDropGrid.setOnPageChangedListener(new OnPageChangedListener() {
            @Override
            public void onPageChanged(PagedDragDropGrid sender, int newPageNumber) {
//                Toast.makeText(getActivity(), "Page changed to page " + newPageNumber, Toast.LENGTH_SHORT).show();
                updatePoint();
            }
        });

//初始化底部指示点
        initPoint();
        softRst = TransPadApplication.getTransPadApplication().getSoftRst();
        if (softRst == null || softRst.cols == null) {
            showRefrushDialog();
        }
    }

    private void initPoint() {
        if (pointLayout != null) {
            pointLayout.removeAllViews();
        }
        int count = dragDropGridAdapter.pageCount();
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(getActivity());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i == 0) {
                layoutParams.setMargins(0, 0, 6, 0);
            } else if (i == 1) {
                layoutParams.setMargins(0, 0, 3, 0);
            } else {
                layoutParams.setMargins(0, 0, 0, 0);
            }
            imageView.setLayoutParams(layoutParams);
            pointLayout.addView(imageView);
        }
        updatePoint();
    }

    /**
     * 更新指示点
     */
    private void updatePoint() {
        int selectItem = pagedDragDropGrid.currentPage();
        for (int i = 0; i < pointLayout.getChildCount(); i++) {

            ImageView iv = (ImageView) pointLayout.getChildAt(i);
            if (i == 0) {
                if (selectItem == i) {
                    iv.setImageResource(R.drawable.dot_allapp);
                } else {
                    iv.setImageResource(R.drawable.dot_allapp_unselect);
                }
            } else if (i == 1) {
                if (selectItem == i) {
                    iv.setImageResource(R.drawable.dot_home);
                } else {
                    iv.setImageResource(R.drawable.dot_home_unselect);
                }
            } else {
                if (selectItem == i) {
                    iv.setImageResource(R.drawable.main_point_select);
                } else {
                    iv.setImageResource(R.drawable.main_point_unselect);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        Item item = (Item) v.getTag();
        if (item != null) {
            if (item instanceof App) {
                App app = (App) item;
                if (app.isInstall()) {
                    if (!TextUtils.isEmpty(app.getActivityName())) {
                        TPUtil.startAppByActvityNamePackageName(getActivity(), app.getPackageName(), app.getActivityName());
                    } else {
                        TPUtil.startAppByPackegName(getActivity(), app.getPackageName());
                    }
                } else {
                    //TODO 下载安装
                }
            }
        }
    }

    public void onEventMainThread(Message message) {
        switch (message.what) {
            case HomeActivity.MSG_WHAT_PAGE_DELETED://page删除了
                pagedDragDropGrid.notifyDataSetChanged();
                if (dragDropGridAdapter.pageCount() > message.arg1) {
                    pagedDragDropGrid.scrollToPage(message.arg1);
                } else {
                    pagedDragDropGrid.scrollToPage(dragDropGridAdapter.pageCount() - 1);
                }
                initPoint();
                break;
            case HomeActivity.MSG_WHAT_SHOW_LOADING_DIALOG://显示加载对话框
                showLoadingDialog();
                break;
            case HomeActivity.MSG_WHAT_DISMESS_LOADING_DIALOG://隐藏加载对话框
                dismessLoadingDialog();
                break;
            case StorageModule.MSG_FILE_CACHE_UPDATE_PROGRESS_SUCCESS:
                OfflineCache oc = message.getData().getParcelable(OfflineCache.OFFLINE_CACHE);
                // L.v("onEventMainThread", "alreadySize=" + oc.getCacheAlreadySize() + " totalSize=" + oc.getCacheTotalSize() + " percentNum=" + oc.getCachePercentNum());
                if (dragDropGridAdapter != null) {
                    dragDropGridAdapter.updateDownloadProgress(oc);
                }
                break;
            case StorageModule.MSG_DELETE_CACHE_SUCCESS:
            case StorageModule.MSG_ADD_CACHE_FAIL:
                if (dragDropGridAdapter != null) {
                    dragDropGridAdapter.cacheStateChanged();
                    dragDropGridAdapter.updateHomeRedDot();
                }
                break;
            case StorageModule.MSG_ADD_CACHE_SUCCESS:
                List<OfflineCache> offlineCacheList = message.getData().getParcelableArrayList(OfflineCache.OFFLINE_CACHE_LIST);
                // L.v("onEventMainThread", "alreadySize=" + oc.getCacheAlreadySize() + " totalSize=" + oc.getCacheTotalSize() + " percentNum=" + oc.getCachePercentNum());
                if (dragDropGridAdapter != null) {
                    dragDropGridAdapter.updateDownloadProgress(offlineCacheList);
                    dragDropGridAdapter.updateHomeRedDot();
                }
                break;
            case TPApplicationReceiver.MSG_WHAT_APPLICATION_INSTALL://应用安装了
                L.v(TAG, "MSG_WHAT_APPLICATION_INSTALL  " + message.obj.toString());
                if (dragDropGridAdapter != null) {
                    dragDropGridAdapter.applicationUpdate(true, message.obj.toString());
                    dragDropGridAdapter.updateHomeRedDot();
                }
                break;
            case TPApplicationReceiver.MSG_WHAT_APPLICATION_UNINSTALL://应用卸载了
                L.v(TAG, "MSG_WHAT_APPLICATION_UNINSTALL  " + message.obj.toString());
                if (dragDropGridAdapter != null) {
                    dragDropGridAdapter.applicationUpdate(false, message.obj.toString());
                    dragDropGridAdapter.updateHomeRedDot();
                }
                break;
            case HomeActivity.MSG_WHAT_ADD_NOITEMVIEW://添加一个默认视图
                Message msg1 = mHandler.obtainMessage(MSG_WHAT_ADD_NOAPPVIEW);
                msg1.arg1 = message.arg1;
                mHandler.sendMessageDelayed(msg1, 200);
                break;
            case StorageModule.MSG_WIFI_NETWORK_TYPE:
            case StorageModule.MSG_2G_NETWORK_TYPE:
            case StorageModule.MSG_3G_NETWORK_TYPE:
            case StorageModule.MSG_4G_NETWORK_TYPE:
                if (dragDropGridAdapter != null) {
                    SoftRst softRst = TransPadApplication.getTransPadApplication().getSoftRst();
                    if (softRst == null) {
                        requestHomeData();
                    }
                    dragDropGridAdapter.updateHomeData();
                }
                if (refrushDialog != null) {
                    refrushDialog.dismiss();
                }
                break;
            case HomeActivity.MSG_WHAT_HOME_DATA_REQUEST_ERROR://首页数据请求异常
                showRefrushDialog();
                break;
        }
    }

    LoadingDialog loadingDialog;

    private void showLoadingDialog() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            if (loadingDialog == null) {
                loadingDialog = new LoadingDialog(getActivity());
            }
            if (!loadingDialog.isShowing()) {
                loadingDialog.show();
            }
        }
    }

    private void dismessLoadingDialog() {
        if (getActivity() != null && !getActivity().isFinishing() && loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private Dialog refrushDialog;

    private void showRefrushDialog() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            return;
        }
        if (refrushDialog == null) {
            refrushDialog = new Dialog(getActivity(), R.style.dialog_base);
            View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.refrush_home_data_dialog, null);
            Button refresh = (Button) contentView.findViewById(R.id.refresh_ok);
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dragDropGridAdapter != null) {
                        if (softRst == null || softRst.cols == null) {
                            requestHomeData();
                        } else {
                            dragDropGridAdapter.updateHomeData();
                        }
                    }
                    refrushDialog.dismiss();
                }
            });
            Button cancel = (Button) contentView.findViewById(R.id.refresh_cancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refrushDialog.dismiss();
                }
            });
            refrushDialog.setContentView(contentView);
            refrushDialog.setCancelable(true);
            refrushDialog.setCanceledOnTouchOutside(true);
        }
        if (!refrushDialog.isShowing()) {
            refrushDialog.show();
        }
    }

    /**
     * 请求首页显示数据
     */

    private void requestHomeData() {
        Request.getInstance().soft("0", new Callback<SoftRst>() {
            @Override
            public void success(SoftRst t, Response response) {
                if (t.result == 0) {
                    TransPadApplication.getTransPadApplication().setTpqSoft(t);
                    pagedDragDropGrid.reGrid();
                    dragDropGridAdapter = new TpqPagedDragDropGridAdapter(getActivity());
                    pagedDragDropGrid.setAdapter(dragDropGridAdapter);
                    TPUtil.saveServerData(t, "tpq_home_softrst");
                    initPoint();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                showRefrushDialog();
            }
        });
        Request.getInstance().login(0, new Callback<LoginRst>() {
            @Override
            public void success(LoginRst loginRst, Response response) {
                if (loginRst.result == 0 && loginRst.showmedie != null) {
                    TransPadApplication.getTransPadApplication().setShowmedia(loginRst.showmedie);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                L.v(TAG, "failure error = " + error.getMessage());
            }
        });
    }

    public void setCurrentPage(int page) {
        if (pagedDragDropGrid != null) {
            pagedDragDropGrid.scrollToPage(page);
        }
    }

    public void scrollToRestoredPage() {
        if (pagedDragDropGrid != null) {
            pagedDragDropGrid.scrollToRestoredPage();
        }
        if (dragDropGridAdapter != null) {
            dragDropGridAdapter.dismessDialog();
        }
    }

    public int getCurrentPage() {
        if (pagedDragDropGrid != null) {
            return pagedDragDropGrid.currentPage();
        }
        return 0;
    }

    public int getRestorePage() {
        if (pagedDragDropGrid != null) {
            return pagedDragDropGrid.getRestorePage();
        }
        return 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dragDropGridAdapter != null) {
            dragDropGridAdapter.onFragmentResume();
            dragDropGridAdapter.updateHomeRedDot();
        }
    }

}
