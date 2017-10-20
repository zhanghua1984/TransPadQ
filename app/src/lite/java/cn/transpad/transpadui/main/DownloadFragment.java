package cn.transpad.transpadui.main;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.DownloadListAdapter;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.ApplicationUtil;
import cn.transpad.transpadui.util.L;
import de.greenrobot.event.EventBus;

/**
 * Created by yusiyang on 16/1/4.
 */
public class DownloadFragment extends BaseFragment {
    private static final String TAG = "DownloadFragment";

    DownloadListAdapter downloadAdapter;
    Context context;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        context = getActivity();
    }

    @InjectView(R.id.download_listView)
    ListView downloadListView;
    @InjectView(R.id.title_download_text)
    TextView title_download;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.viewpager_download_layout, container, false);
        ButterKnife.inject(this, view);
        ArrayList<OfflineCache> downloadList = ApplicationUtil.getDownloadList();
        downloadAdapter = new DownloadListAdapter(context, downloadListView);
        downloadAdapter.setOfflineCacheList(downloadList);
        downloadListView.setAdapter(downloadAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setDownloadListData();
        updateTitle();
    }

    @Override
    public void onStop() {
        super.onStop();
        dismissDialog();
    }

    @OnClick(R.id.app_back)
    void back() {
        onBack();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void updateTitle() {
//        更新下载管理title
        int size = StorageModule.getInstance().getOfflineCacheFileCount();
        L.v(TAG, "updateTitle", "下载应用updatetitle" + size);
        String titleDownload = getString(R.string.viewpager_title_download) + "(" + size + ")";
        if (size == 0) {
            title_download.setText(R.string.viewpager_title_download);
        } else {
            title_download.setText(titleDownload);
        }
    }

    public void setDownloadListData() {
        ArrayList<OfflineCache> downloadList = ApplicationUtil.getDownloadList();
        if (downloadAdapter != null) {
            downloadAdapter.setOfflineCacheList(downloadList);
            downloadAdapter.notifyDataSetChanged();
        }
    }

    public void updateDownloadProgress(OfflineCache offlineCache) {
        downloadAdapter.setOfflineCache(offlineCache);
    }

    public void dismissDialog() {
        downloadAdapter.dismissDialog();
    }

    public void onEventMainThread(Message msg) {
        OfflineCache offlineCache = null;
        switch (msg.what) {
            case StorageModule.MSG_FILE_CACHE_UPDATE_PROGRESS_SUCCESS:
                offlineCache = msg.getData().getParcelable(OfflineCache.OFFLINE_CACHE);
                //L.v(TAG, "onEventMainThread", "name=" + offlineCache.getCacheName() + " state=" + offlineCache.getCacheDownloadState());
                updateDownloadProgress(offlineCache);
            case StorageModule.MSG_DELETE_CACHE_SUCCESS:
                updateTitle();
                break;
        }
    }
}
