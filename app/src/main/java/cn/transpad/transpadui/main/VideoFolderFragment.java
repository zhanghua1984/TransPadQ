package cn.transpad.transpadui.main;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.VideoFolderAdapter;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.http.SpecllistRst;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;
import cn.transpad.transpadui.view.PullToRefreshLayout;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by wangshaochun on 2015/5/12.
 */
public class VideoFolderFragment extends BaseFragment {
    public static final String TAG = "VideoFolderFragment";
    View view;
    String videoUrl;
    VideoFolderAdapter videoFolderAdapter;
    List<SpecllistRst.Cnt> mnList;
    List<SpecllistRst.Cnt> moList;
    @InjectView(R.id.content_view)
    GridView gv_menu;
    @InjectView(R.id.refresh_view)
    PullToRefreshLayout refresh_view;
    @InjectView(R.id.tv_title_name)
    TextView tv_title_name;
    static String loadMoreUrl;//加载下一页的url
    static int isExist;//是否有下一页
    String titleName;
    int toPosition;
    SpecllistRst specllistRsts;

    public VideoFolderFragment() {
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mnList != null&&videoFolderAdapter!=null) {
                    videoFolderAdapter.setVideoList(mnList);
//                        videoFolderAdapter.setVideoUrl(videoUrl);
                    gv_menu.setAdapter(videoFolderAdapter);
                    videoFolderAdapter.notifyDataSetChanged();
                    gv_menu.smoothScrollToPosition(toPosition);
            }
        }
    };
    public VideoFolderFragment(String titleName) {
        this.titleName = titleName;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.video_folder, container, false);
        ButterKnife.inject(this, view);
        L.v(TAG, "创建" + toPosition);
        refresh_view.setOnRefreshListener(new VideoFolderListener());
        tv_title_name.setText(titleName);
        videoFolderAdapter = new VideoFolderAdapter(getActivity(), gv_menu);
        videoFolderAdapter.setSpecllistRst(specllistRsts);
        videoFolderAdapter.setVideoList(mnList);
        gv_menu.setAdapter(videoFolderAdapter);

//        videoFolderAdapter.notifyDataSetChanged();

        gv_menu.setSelector(new ColorDrawable(Color.TRANSPARENT));//设置点击条目无点击效果


        gv_menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (TPUtil.isNetOkWithToast()) {
                    if (TextUtils.isEmpty(mnList.get(position).url)) {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.play_url_null), Toast.LENGTH_SHORT).show();
                    } else {
                        Fragment fragment = new VideoDetailsFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("url", mnList.get(position).url);
                        fragment.setArguments(bundle);
                        HomeActivity.switchFragment(fragment);
                    }
                    toPosition = position;
                }
            }
        });
        return view;
    }

    @OnClick(R.id.iv_back)
    public void backPage() {//返回上级页面的方法
        onBack();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoUrl = getArguments().getString("url");
        if (!TextUtils.isEmpty(videoUrl)) {
            Request.getInstance().specllist(TPUtil.handleUrl(videoUrl), new Callback<SpecllistRst>() {
                @Override
                public void success(SpecllistRst specllistRst, Response response) {
//                Toast.makeText(getActivity(),"成功", Toast.LENGTH_LONG).show();
                    if (specllistRst.cnts != null && specllistRst.cnts.cntList != null) {
                        mnList = specllistRst.cnts.cntList;
                        loadMoreUrl = specllistRst.rp.nurl;
//                        isPage = specllistRst.rp.m;
                        isExist = specllistRst.rp.m;

                        specllistRsts = specllistRst;
                        if (videoFolderAdapter != null) {
                            videoFolderAdapter.setSpecllistRst(specllistRsts);
                        }

//                        Message msg = Message.obtain();
//                        handler.sendMessage(msg);
                        handler.sendEmptyMessage(1);
                    }

                }

                @Override
                public void failure(RetrofitError error) {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.settings_version_fail_noNetwork), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public class VideoFolderListener implements PullToRefreshLayout.OnRefreshListener {

        @Override
        public void onRefresh(final PullToRefreshLayout pullToRefreshLayout) {
            if (TPUtil.isNetOkWithToast()) {
                // 下拉刷新操作
                Request.getInstance().specllist(TPUtil.handleUrl(videoUrl), new Callback<SpecllistRst>() {

                    @Override
                    public void success(SpecllistRst specllistRst, Response response) {
                        if (specllistRst.cnts!=null && specllistRst.cnts.cntList != null) {
                            mnList = specllistRst.cnts.cntList;
                            if(specllistRst.rp!=null && specllistRst.rp.nurl!=null) {

                                loadMoreUrl = specllistRst.rp.nurl;
                                isExist = specllistRst.rp.m;
                            }
//                        videoFolderAdapter = new VideoFolderAdapter(getActivity(), gv_menu);
                            if (videoFolderAdapter != null) {
                                videoFolderAdapter.setVideoList(mnList);

                                gv_menu.setAdapter(videoFolderAdapter);
                            }

                            pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);//通知刷新完毕
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.settings_version_fail_noNetwork)), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
            }
        }

        @Override
        public void onLoadMore(final PullToRefreshLayout pullToRefreshLayout) {
            if (TPUtil.isNetOkWithToast()) {
                // 加载操作
                if (isExist == 0) {//不存在
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), getString(R.string.no_more_data), Toast.LENGTH_LONG).show();
                        pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
                    }
                }
                if (isExist == 1) {//存在
                    if (!TextUtils.isEmpty(loadMoreUrl)) {
                        Request.getInstance().specllist(TPUtil.handleUrl(loadMoreUrl), new Callback<SpecllistRst>() {
                            @Override
                            public void success(SpecllistRst specllistRst, Response response) {

                                if(specllistRst.cnts!=null && specllistRst.cnts.cntList!=null) {
                                    if (moList != specllistRst.cnts.cntList) {
                                        moList = specllistRst.cnts.cntList;
                                        if(specllistRst.rp!=null && specllistRst.rp.nurl!=null) {
                                            loadMoreUrl = specllistRst.rp.nurl;
                                            isExist = specllistRst.rp.m;
                                        }
                                        if(mnList!=null) {
                                            int index = mnList.size();
                                            mnList.addAll(moList);
                                            videoFolderAdapter.setVideoList(mnList);
                                            pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
                                            videoFolderAdapter.notifyDataSetChanged();
                                            gv_menu.smoothScrollToPosition(index);
                                        }

                                    }
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.FAIL);
                            }
                        });
                    }
                }
            } else {
                pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.FAIL);
            }
        }

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();
//        Message msg = Message.obtain();
//        handler.sendMessage(msg);
        gv_menu.smoothScrollToPosition(toPosition);
    }
}
