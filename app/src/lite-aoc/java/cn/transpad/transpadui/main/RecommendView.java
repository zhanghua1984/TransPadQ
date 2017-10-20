package cn.transpad.transpadui.main;

import android.content.Context;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.RecommendedAdapter;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.http.SoftRst;
import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.util.ApplicationUtil;
import cn.transpad.transpadui.util.L;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * 定制应用
 * Created by user on 2015/5/19.
 */
public class RecommendView extends LinearLayout {
    public static final String TAG = "RecommendView";
    public static final String APP_RECOMMEND_CACHE = "app_recommend_cache";
    Context context;
    @InjectView(R.id.gridView_recommended)
    GridView gridViewRecommended;
    RecommendedAdapter recommendedAdapter;

    public RecommendView(Context context) {
        super(context);
        initView();
        initData();
    }

    private void initView() {
        this.context = getContext();
        inflate(getContext(), R.layout.viewpager_auto_layout, this);
        ButterKnife.inject(this);
        recommendedAdapter = new RecommendedAdapter(context, gridViewRecommended);
        gridViewRecommended.setAdapter(recommendedAdapter);
    }

    private void initData() {

        //读取缓存数据
        Gson gson = new Gson();
        String json = SharedPreferenceModule.getInstance().getString(APP_RECOMMEND_CACHE);
        ArrayList<OfflineCache> offlineCacheList = gson.fromJson(json, new TypeToken<ArrayList<OfflineCache>>() {
        }.getType());
        recommendedAdapter.setOfflineCacheList(offlineCacheList);
        recommendedAdapter.notifyDataSetChanged();

        Request.getInstance().soft("1", new Callback<SoftRst>() {
            @Override
            public void success(SoftRst softRst, Response response) {
                ArrayList<OfflineCache> offlineCacheList = ApplicationUtil.getSoftFromServer(softRst);
                recommendedAdapter.setOfflineCacheList(offlineCacheList);
                recommendedAdapter.notifyDataSetChanged();
                Gson gson = new Gson();
                String json = gson.toJson(offlineCacheList);
                L.v(TAG, "success", "json=" + json);
                SharedPreferenceModule.getInstance().setString(APP_RECOMMEND_CACHE, json);
            }

            @Override
            public void failure(RetrofitError error) {
                if (error != null) {
                    L.e(TAG, "instantiateItem", "failure soft failure error=" + error.getMessage());
                }
            }
        });
    }

    @OnClick(R.id.download_button)
    void downloadFragment() {
        LiteHomeActivity.switchFragment(new DownloadFragment());
    }

    @InjectView(R.id.new_message_circle)
    ImageView redDot;

    public void updateRedDot() {
        L.v(TAG, "updateRedDot");
        redDot.setVisibility(ApplicationUtil.isTaskUninstalled() ? View.VISIBLE : View.INVISIBLE);
    }

    public void updateDownloadProgress(OfflineCache offlineCache) {
        recommendedAdapter.setOfflineCache(offlineCache);
    }

    public void dismissDialog() {
        recommendedAdapter.dismissDialog();
    }

}
