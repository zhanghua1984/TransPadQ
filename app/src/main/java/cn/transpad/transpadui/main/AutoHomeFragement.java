package cn.transpad.transpadui.main;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.laplanete.mobile.pageddragdropgrid.Item;
import ca.laplanete.mobile.pageddragdropgrid.OnPageChangedListener;
import ca.laplanete.mobile.pageddragdropgrid.PagedDragDropGrid;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.TPPagedDragDropGridAdapter;
import cn.transpad.transpadui.entity.App;
import cn.transpad.transpadui.entity.NoApp;
import cn.transpad.transpadui.entity.Page;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;
import de.greenrobot.event.EventBus;

/**
 * 首页的Fragment
 * Created by Kongxiaojun on 2015/4/7.
 */
public class AutoHomeFragement extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "AutoHomeFragement";

    @InjectView(R.id.pageddragdropgrid)
    PagedDragDropGrid pagedDragDropGrid;

    @InjectView(R.id.home_point_layout)
    LinearLayout pointLayout;

    TPPagedDragDropGridAdapter dragDropGridAdapter;

    Handler mHandler;

    private static final int MSG_WHAT_ADD_ITME = 1;
    private static final int MSG_WHAT_ADD_NOAPPVIEW = 2;

    /**
     * 应用页
     */
    List<Page> appPages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.v(TAG, "onCreate");
        EventBus.getDefault().register(this);
        appPages = StorageModule.getInstance().getPageList();
        if (appPages != null && appPages.size() > 0) {
            for (Page page : appPages) {
                page.setItems(StorageModule.getInstance().getAppListByPageId(page.getId()));
            }
        }
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
    }

    @Override
    public void onDestroy() {
        L.v(TAG, "onDestroy");
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (dragDropGridAdapter != null) {
            appPages = dragDropGridAdapter.getAppPages();
            if (appPages != null && appPages.size() > 0) {
                for (Page page : appPages) {
                    if (page.getItems().size() > 0) {
                        for (Item item : page.getItems()) {
                            if (item instanceof App) {
                                App app = (App) item;
                                L.v(TAG, "app name = " + app.getName() + "   app page id = " + app.getPageId() + "  app index = " + app.getIndex());
                                StorageModule.getInstance().updateApp(app);
                            }
                        }
                    }
                }
            }
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

    private void init() {
        dragDropGridAdapter = new TPPagedDragDropGridAdapter(getActivity(), appPages);
        pagedDragDropGrid.setAdapter(dragDropGridAdapter);
        pagedDragDropGrid.setRestorePage(getActivePage());
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
    }

    private void initPoint() {
        if (pointLayout != null) {
            pointLayout.removeAllViews();
        }
        int count = dragDropGridAdapter.pageCount();
        for (int i = 0; i < count; i++) {
            ImageView imageView = new ImageView(getActivity());
            imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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
            if (selectItem == i) {
                iv.setImageResource(R.drawable.main_point_select);
            } else {
                iv.setImageResource(R.drawable.main_point_unselect);
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

    public synchronized void addApp(App app) {
        L.v(TAG, "addApp app = " + app);
        appPages = dragDropGridAdapter.getAppPages();
        //找到应该添加到的Page
        Page currentPage;
        Page lastPage;
        if (appPages == null || appPages.size() == 0) {
            lastPage = null;
        } else {
            //找到最后一页
            lastPage = appPages.get(appPages.size() - 1);
        }
        if (lastPage == null || (lastPage.getItems().size() >= (dragDropGridAdapter.columnCount() * dragDropGridAdapter.rowCount()))) {
            //新建一页
            L.v(TAG, "add new page");
            currentPage = new Page();
            int id = StorageModule.getInstance().addPage(currentPage);
            if (id > 0) {
                //插入成功
                currentPage.setId(id);
            }
        } else {
            currentPage = lastPage;
        }
        if (currentPage.getItems().size() == 1 && currentPage.getItems().get(0).getType() == 3){
            pagedDragDropGrid.removeItem(dragDropGridAdapter.pageCount()-1,0);
            currentPage.removeItem(0);
        }
        app.setPageId(currentPage.getId());
        if (currentPage.getItems().size() > 0) {
            app.setIndex(currentPage.getItems().get(currentPage.getItems().size() - 1).getIndex() + 1);
        }
        int id = StorageModule.getInstance().addItem(app);
        app.setId(id);
        currentPage.addItem(app);
        if (lastPage == null || currentPage.getId() != lastPage.getId()) {
            appPages.add(currentPage);
            dragDropGridAdapter.addPage(currentPage);
            initPoint();
        }
        pagedDragDropGrid.notifyDataSetChanged();
    }

    public void onEventMainThread(Message message) {
        switch (message.what) {
            case HomeActivity.MSG_WHAT_ADD_APP://添加应用到桌面
                App app = (App) message.obj;
                addApp(app);
                Message msg = new Message();
                msg.what = HomeActivity.MSG_WHAT_GO_BACK;
                EventBus.getDefault().post(msg);
                mHandler.sendEmptyMessageDelayed(MSG_WHAT_ADD_ITME, 500);
                break;
            case HomeActivity.MSG_WHAT_PAGE_DELETED://page删除了
                pagedDragDropGrid.notifyDataSetChanged();
                if (dragDropGridAdapter.pageCount() > message.arg1) {
                    pagedDragDropGrid.scrollToPage(message.arg1);
                } else {
                    pagedDragDropGrid.scrollToPage(dragDropGridAdapter.pageCount() - 1);
                }
                initPoint();
                break;
            case HomeActivity.MSG_WHAT_ADD_NOITEMVIEW://添加一个默认视图
                Message msg1 = mHandler.obtainMessage(MSG_WHAT_ADD_NOAPPVIEW);
                msg1.arg1 = message.arg1;
                mHandler.sendMessageDelayed(msg1,200);
                break;
        }
    }

    public void setCurrentPage(int page) {
        if (pagedDragDropGrid != null) {
            pagedDragDropGrid.scrollToPage(page);
        }
    }

    public void scrollToRestoredPage(){
        if (pagedDragDropGrid != null) {
            pagedDragDropGrid.scrollToRestoredPage();
        }
    }

    private int getActivePage(){
        int onlinePageSize = 0;
        if (TransPadApplication.getTransPadApplication().getShowmedia() != null && TransPadApplication.getTransPadApplication().getShowmedia().equals("1")){
            onlinePageSize += 1;
        }
        return onlinePageSize;
    }
}
