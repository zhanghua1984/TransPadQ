package cn.transpad.transpadui.main;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.game.UMGameAgent;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.DownloadListAdapter;
import cn.transpad.transpadui.adapter.InstalledListAdapter;
import cn.transpad.transpadui.entity.ApplicationTab;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.ApplicationUtil;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;
import de.greenrobot.event.EventBus;


public class ApplicationFragment extends BaseFragment {
    private static final String TAG = ApplicationFragment.class.getSimpleName();
    private Context context;
    private DownloadListAdapter downloadAdapter;
    private InstalledListAdapter mInstalledListAdapter;
    private List<ApplicationTab> mApplicationTabList = null;
    @InjectView(R.id.title_download)
    TextView title_download;
    @InjectView(R.id.title_installed)
    TextView title_installed;
    @InjectView(R.id.download_listView)
    ListView downloadListView;
    @InjectView(R.id.gvInstalled)
    GridView installedGridView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mApplicationTabList = ApplicationUtil.getApplicationTabList();
    }

    @Override
    public void onResume() {
        super.onResume();
        L.v(TAG, "onResume");
        initData();
    }

    private void initData() {
        updateTitle();
        setDownloadListData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.viewpager_layout, container, false);
        ButterKnife.inject(this, view);
        context = getActivity();
        downloadAdapter = new DownloadListAdapter(context, downloadListView);

        ApplicationTab applicationTab = mApplicationTabList.get(0);
        ArrayList<OfflineCache> downloadList = applicationTab.getDownloadOfflineCacheList();
        downloadAdapter.setOfflineCacheList(downloadList);
        downloadListView.setAdapter(downloadAdapter);
        mInstalledListAdapter = new InstalledListAdapter(context);
        installedGridView.setAdapter(mInstalledListAdapter);
        title_download.setTextColor(getResources().getColor(R.color.white));
        title_installed.setTextColor(getResources().getColor(R.color.orange));
        title_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                installedGridView.setVisibility(View.GONE);
                downloadListView.setVisibility(View.VISIBLE);
                title_download.setTextColor(R.color.orange);
                title_download.setTextColor(getResources().getColor(R.color.orange));
                title_installed.setTextColor(getResources().getColor(R.color.white));
            }
        });
        title_installed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                installedGridView.setVisibility(View.VISIBLE);
                downloadListView.setVisibility(View.GONE);
                title_download.setTextColor(getResources().getColor(R.color.white));
                title_installed.setTextColor(getResources().getColor(R.color.orange));
            }
        });
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        dismissDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    public void onEventMainThread(Message msg) {
        OfflineCache offlineCache;
        switch (msg.what) {
            case StorageModule.MSG_FILE_CACHE_UPDATE_PROGRESS_SUCCESS:
                offlineCache = msg.getData().getParcelable(OfflineCache.OFFLINE_CACHE);
                //L.v(TAG, "onEventMainThread", "name=" + offlineCache.getCacheName() + " state=" + offlineCache.getCacheDownloadState());
                updateDownloadProgress(offlineCache);
                break;
            case StorageModule.MSG_ADD_CACHE_SUCCESS:
                updateTitle();
                setDownloadListData();
                break;
            case StorageModule.MSG_DELETE_CACHE_SUCCESS:
                updateTitle();
                break;
            default:
                break;
        }
    }

    public void updateTitle() {
//        更新下载管理title
        int size = StorageModule.getInstance().getOfflineCacheFileCount();
        L.v(TAG, "updateTitle", "下载应用updatetitle" + size);
        String titleDownload = getString(R.string.viewpager_title_download) + "（" + size + "）";
        if (size == 0) {
            title_download.setText(R.string.viewpager_title_download);
        } else {
            title_download.setText(titleDownload);
        }
    }

    @OnClick(R.id.app_back)
    void backClick() {
        getFragmentManager().popBackStack();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    public void setDownloadListData() {
        ArrayList<OfflineCache> downloadList = ApplicationUtil.getDownloadList();
        if (downloadAdapter != null) {
            downloadAdapter.setOfflineCacheList(downloadList);
            downloadAdapter.notifyDataSetChanged();
        }
        ArrayList<OfflineCache> installList = StorageModule.getInstance().getOfflineCacheList();
        for (int i = installList.size() - 1; i >= 0; i--) {
            if (!TPUtil.isApkInstalled(context, installList.get(i).getCachePackageName())) {
                installList.remove(i);
            }
        }
        mInstalledListAdapter.setOfflineCacheList(installList);
        mInstalledListAdapter.notifyDataSetChanged();
    }

    public void updateDownloadProgress(OfflineCache offlineCache) {
        downloadAdapter.setOfflineCache(offlineCache);
    }

    public void dismissDialog() {
        downloadAdapter.dismissDialog();
    }
}
