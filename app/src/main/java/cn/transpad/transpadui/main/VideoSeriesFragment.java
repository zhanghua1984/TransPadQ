package cn.transpad.transpadui.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.VideoSeriesAdapter;
import cn.transpad.transpadui.adapter.VideoSeriesItemAdapter;
import cn.transpad.transpadui.adapter.VideoSeriesItemAdapter2;
import cn.transpad.transpadui.http.DramaRst;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.player.activity.WebViewPlayerActivity;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.PlayerUtil;
import cn.transpad.transpadui.util.TPUtil;
import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by wangshaochun on 2015/5/26.
 */
public class VideoSeriesFragment extends BaseFragment {
    private static final String TAG = "VideoSeriesFragment";
    private View contentView;
    private String dramaurl;
    private SharedPreferences sp;
    @InjectView(R.id.rv_hor_title)
    RecyclerView rv_hor_title;
    @InjectView(R.id.gv_list)
    GridView gv_list;
    @InjectView(R.id.gv_list2)
    GridView gv_list2;
    VideoSeriesAdapter videoSeriesAdapter;
    VideoSeriesItemAdapter videoSeriesItemAdapter;
    VideoSeriesItemAdapter2 videoSeriesItemAdapter2;
    private DramaRst dramaRst;
    private String issdkplay;

    public VideoSeriesFragment() {
        super();
//        EventBus.getDefault().register(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sp = getActivity().getSharedPreferences("config", 0);
//        int status = sp.getInt("checkFragmentLayout", 0);
        dramaurl = getArguments().getString("dramaurl");
        issdkplay = getArguments().getString("issdkplay");

        contentView = inflater.inflate(R.layout.video_series, null);

        ButterKnife.inject(this, contentView);
        if (TPUtil.isNetOk()) {
            L.v(TAG, "---------1---开始请求");
            requestSeries(dramaurl);
        }
        //设置布局管理器
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv_hor_title.setLayoutManager(linearLayoutManager);
        gv_list2.setSelector(new ColorDrawable(Color.TRANSPARENT));
        gv_list.setSelector(new ColorDrawable(Color.TRANSPARENT));
        //设置选集适配器
        //选择详细视频集数的界面显示
//        switch (status){
//            case 0:
//                Toast.makeText(getActivity(),"暂无集数",Toast.LENGTH_SHORT).show();
//                break;
//            case 1:
//                gv_list.setVisibility(View.GONE);
//                gv_list2.setVisibility(View.VISIBLE);
//                //设置选集对应条目适配器
//                gv_list2.setSelector(new ColorDrawable(Color.TRANSPARENT));//设置点击条目无点击效果
//                break;
//            case 3:
//                gv_list.setVisibility(View.VISIBLE);
//                gv_list2.setVisibility(View.GONE);
//                //设置选集对应条目适配器
//                gv_list.setSelector(new ColorDrawable(Color.TRANSPARENT));//设置点击条目无点击效果
//                break;
//        }
        return contentView;
    }

    private void requestSeries(String url) {
        if (TextUtils.isEmpty(url)) {
            L.v(TAG, "---------2---为空返回");
            return;
        }
        Request.getInstance().drama(TPUtil.handleUrl(url), new Callback<DramaRst>() {
            @Override
            public void success(DramaRst t, Response response) {
                if (destory) {
                    L.v(TAG, "---------3---销毁返回");
                    return;
                }
                Activity activity = getActivity();
                if (activity == null) {
                    L.v(TAG, "---------4---activity为空");
                    return;
                }
                if (t == null) {
                    return;
                }
                if (t.result == 0) {
                    if (dramaRst == null) {
                        dramaRst = t;
                        if (dramaRst.cntsList != null && dramaRst.cntsList.size() > 0) {
                            L.v(TAG, "---------5---更新数据");
                            updateData();
                        }
                    } else {

                        if (dramaRst.cntsList == null || t.rp == null) {
                            return;
                        }

                        // 数据返回了
                        if (dramaRst.cntsList.get(t.rp.p - 1).cntList == null) {
                            dramaRst.cntsList.get(t.rp.p - 1).cntList = new ArrayList<DramaRst.Cnt>();
                        }
                        dramaRst.cntsList.get(t.rp.p - 1).cntList.clear();
                        dramaRst.cntsList.get(t.rp.p - 1).cntList.addAll(t.cntsList.get(t.rp.p - 1).cntList);
                        // 显示规则剧集
                        if (dramaRst.cntsList.get(0).showtyp == 0) {
                            showRuleSeries(dramaRst.cntsList.get(t.rp.p - 1));
                        } else {
//                            showUnRuleSeries(dramaRst.cntsList.get(t.rp.p - 1));
                        }
                    }
                    if (dramaRst.cntsList != null && dramaRst.cntsList.size() > t.rp.p) {
                        if (dramaRst.cntsList.get(0).showtyp != 0) {
                            // 不规则剧集继续请求
                            requestSeries(dramaRst.cntsList.get(t.rp.p).url);
                        }
                    }
                } else {
                    // 出错了
                    if (t.error != null && !TextUtils.isEmpty(t.error.errormsg)) {
                        Toast.makeText(getActivity(), t.error.errormsg, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    /**
     * 更新数据
     *
     * @return void
     * @throws
     */
    private void updateData() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (dramaRst.cntsList != null && dramaRst.cntsList.size() > 0) {


            if (videoSeriesAdapter != null) {
                videoSeriesAdapter.setData(dramaRst.cntsList);
            } else {
                if (getActivity() != null) {
                    videoSeriesAdapter = new VideoSeriesAdapter(dramaRst.cntsList, getActivity());
                    rv_hor_title.setAdapter(videoSeriesAdapter);
                }
            }
            videoSeriesAdapter.setOnItemClickLitener(new VideoSeriesAdapter.OnItemClickLitener() {
                @Override
                public void onItemClick(View view, int position) {

                    if (dramaRst == null || dramaRst.cntsList == null) {
                        return;
                    }
                    for (DramaRst.Cnts cnts : dramaRst.cntsList) {
                        cnts.isChecked = false;
                    }
                    DramaRst.Cnts cnts = dramaRst.cntsList.get(position);
                    cnts.isChecked = true;
                    videoSeriesAdapter.notifyDataSetChanged();
                    // 显示当前tab下的所有剧集
                    if (cnts != null && cnts.cntList != null && cnts.cntList.size() > 0) {
                        L.v(TAG, "---------8---显示剧集");
                        if(dramaRst.cntsList.get(0).showtyp == 0){
                            showRuleSeries(cnts);
                        }else{
                            showUnRuleSeries(cnts);
                        }

//                        show();
                    }
                    else {
                        // 显示请求对话框
                        gv_list2.setVisibility(View.GONE);
                        gv_list.setVisibility(View.GONE);
                        requestSeries(cnts.url);
                    }

                }
            });
            show();
        }
    }
    public void show(){
        if (dramaRst.cntsList.get(0).showtyp == 0) {// 规则显示剧集
            L.v(TAG, "---------6---有规则的" + dramaRst.cntsList.get(0).name);
            gv_list.setVisibility(View.GONE);
            gv_list2 .setVisibility(View.VISIBLE);
            dramaRst.cntsList.get(0).isChecked = true;// 默认选中第一tab

            refreshTabView();

            // 显示第一个tab下的所有剧集
            showRuleSeries(dramaRst.cntsList.get(0));
        }
        else {
            gv_list .setVisibility(View.VISIBLE);
            gv_list2.setVisibility(View.GONE);
            dramaRst.cntsList.get(0).isChecked = true;// 默认选中第一tab

            refreshTabView();
            // 不规则显示
            showUnRuleSeries(dramaRst.cntsList.get(0));
        }
    }
    /**
     * 更新分页选择滑动条
     *
     * @return void
     * @throws
     */
    public void refreshTabView() {
        L.v(TAG, "---------7---更新分页选择滑动条");
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        videoSeriesAdapter.notifyDataSetChanged();
//        rv_hor_title.setNumColumns(dramaRst.cntsList.size());
        // width是否也需要取余
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (ScreenUtil.dp2px(78) * dramaRst.cntsList.size()), ScreenUtil.dp2px(50));
//
//        rv_hor_title.setLayoutParams(params);
    }


    /**
     * 显示当前Cnts下的剧集(规则剧集)
     *
     * @param cnts
     * @return void
     * @throws
     */
    private void showRuleSeries(final DramaRst.Cnts cnts) {
        L.v(TAG, "---------9---显示剧集的方法执行了");
        if (cnts.cntList != null && cnts.cntList.size() > 0) {
            gv_list2.setVisibility(View.VISIBLE);
            gv_list.setVisibility(View.GONE);
            if (videoSeriesItemAdapter != null) {
                videoSeriesItemAdapter.setData(cnts.cntList);
            } else {
                if (getActivity() != null) {
                    videoSeriesItemAdapter = new VideoSeriesItemAdapter(getActivity(),cnts.cntList);
                    gv_list2.setAdapter(videoSeriesItemAdapter);
                }
            }
            gv_list2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    //播放视频

                    if (TextUtils.isEmpty(cnts.cntList.get(arg2).url)) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), R.string.play_url_null, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (getActivity() != null) {
                            if (cnts.cntList.get(arg2).toply == 0) {
                                PlayerUtil.openVideoPlayer(getActivity(), cnts.cntList.get(arg2).url);
                            }else {
                                go3rdPlayer(cnts.cntList.get(arg2));
                            }
                        }
                    }
                }
            });
        } else {
            requestSeries(cnts.url);
        }
    }
    List<DramaRst.Cnt> dramas;
    /**
     * 显示非规则剧集
     *
     * @param cnts
     * @return void
     * @throws
     */
    private synchronized void showUnRuleSeries(final DramaRst.Cnts cnts) {
        L.v(TAG, "---------10--- 显示非规则剧集");
        if (cnts.cntList != null && cnts.cntList.size() > 0) {
            gv_list.setVisibility(View.VISIBLE);
            gv_list2.setVisibility(View.GONE);
            if (videoSeriesItemAdapter2 != null) {
                videoSeriesItemAdapter2.setData(cnts.cntList);
            } else {
                if (getActivity() != null) {
                    videoSeriesItemAdapter2 = new VideoSeriesItemAdapter2(getActivity(),cnts.cntList);
                    gv_list.setAdapter(videoSeriesItemAdapter2);
                }
            }
            gv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    //播放视频

                    if (TextUtils.isEmpty(cnts.cntList.get(arg2).url)) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), R.string.play_url_null, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (getActivity() != null) {
                            if (cnts.cntList.get(arg2).toply == 0) {
                                PlayerUtil.openVideoPlayer(getActivity(), cnts.cntList.get(arg2).url);
                            } else {
                                go3rdPlayer(cnts.cntList.get(arg2));
                            }
                        }
                    }
                }
            });
        } else {
            requestSeries(cnts.url);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        contentView = null;
        if (rv_hor_title != null) {
            rv_hor_title.removeAllViewsInLayout();
            rv_hor_title = null;
        }
        if (gv_list != null) {
            gv_list.removeAllViewsInLayout();
            gv_list = null;
        }
        if (gv_list2 != null) {
            gv_list2.removeAllViewsInLayout();
            gv_list2 = null;
        }
        videoSeriesAdapter = null;
        videoSeriesItemAdapter = null;
        videoSeriesItemAdapter2 = null;
        dramaurl = null;
        gv_list = null;
        gv_list2 = null;
        dramaRst = null;
    }

    /**
     * 去第三方播放
     *
     * @return void
     * @throws
     */
    private void go3rdPlayer(DramaRst.Cnt cnt) {
        if (TPUtil.isNetOkWithToast()) {

            try {
                String ourl = cnt.ourl;

                if (!TextUtils.isEmpty(issdkplay) && issdkplay.equals("1")) {
                    // 处理sohu视频
                    if (ourl.contains("sohu.com")) {// sohu
                        PlayerUtil.openSohuPlayer(getActivity(), "111", "100tv", ourl,
                                false, cnt.url);
                        return;
                    } else if (ourl.contains("letv.com")) {// 乐视
                        if (PlayerUtil.openLetvSdkPlayer(getActivity(),ourl)) {
                            return;
                        }
                    }
                }

                Intent intent = new Intent(getActivity(),
                        WebViewPlayerActivity.class);
                intent.putExtra("xyzplay", cnt.url);
                intent.putExtra("ourl", ourl);
                intent.putExtra("btnply", cnt.btnply);
                intent.putExtra("name", cnt.name);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), R.string.play_url_null, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
