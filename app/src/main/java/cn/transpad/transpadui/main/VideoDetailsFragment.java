package cn.transpad.transpadui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.VideoDetailsAdapter;
import cn.transpad.transpadui.http.LinkvideoRst;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.http.VgdetailRst;
import cn.transpad.transpadui.player.activity.WebViewPlayerActivity;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.PlayerUtil;
import cn.transpad.transpadui.util.TPUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by wangshaochun on 2015/5/6.
 */
public class VideoDetailsFragment extends BaseFragment {
    View view;
    @InjectView(R.id.id_recyclerview_horizontal)
    RecyclerView mRecyclerView;
    @InjectView(R.id.tv_video_name)
    TextView tv_video_name;
    @InjectView(R.id.tv_actor)
    TextView tv_actor;
    @InjectView(R.id.tv_director)
    TextView tv_director;
    @InjectView(R.id.tv_description)
    TextView tv_description;
    @InjectView(R.id.iv_image1)
    RoundedImageView iv_image1;
    private VideoDetailsAdapter videoDetailsAdapter;
    String videoDescUrl;//��Ƶ���ŵ�url
    String linkUrl;//�Ƽ��б��url
    private DisplayImageOptions options;
    private boolean hadIntercept;
    private static final String TAG = "VideoDetailsFragment";

    private VgdetailRst vgdetailRsts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    List<LinkvideoRst.Rcmd> mLists;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.video_details, container, false);
        ButterKnife.inject(this, view);
        options = TPUtil.createDisplayImageOptionsByDrawableId(R.drawable.default_342_456);
        sp = getActivity().getSharedPreferences("config", 0);
        videoDescUrl = getArguments().getString("url");
        if (TextUtils.isEmpty(videoDescUrl)) {
            Toast.makeText(getActivity(),R.string.sorry_no_the_video_data, Toast.LENGTH_SHORT).show();
        } else {
            initData(videoDescUrl);
        }

        //���ò��ֹ�����
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        return view;
    }

    @InjectView(R.id.tv_play)
    TextView tv_play;
    @InjectView(R.id.rl_play)
    RelativeLayout rl_play;
    private SharedPreferences.Editor editor = null;
    private SharedPreferences sp;

    public void initData(String url) {
        Request.getInstance().vgdetail(TPUtil.handleUrl(url), new Callback<VgdetailRst>() {
            @Override
            public void success(final VgdetailRst vgdetailRst, Response response) {

                if (vgdetailRst.result == 0) {
                    vgdetailRsts = vgdetailRst;
                    switch (vgdetailRst.drama) {
                        case "1":
                            tv_play.setText(R.string.selection_play);
                            editor = sp.edit();
                            editor.putInt("checkFragmentLayout", 1);
                            editor.commit();
                            rl_play.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (TPUtil.isNetOkWithToast()) {
                                        if (vgdetailRst.froms.fromList.get(0) != null) {
                                            VideoSeriesFragment videoSeriesFragment = new VideoSeriesFragment();
                                            Bundle bundle = new Bundle();
                                            bundle.putString("dramaurl", vgdetailRst.froms.fromList.get(0).dramaurl);
                                            bundle.putString("issdkplay", vgdetailRst.froms.fromList.get(0).issdkplay);
                                            videoSeriesFragment.setArguments(bundle);
                                            HomeActivity.switchFragment(videoSeriesFragment);
//                                    getFragmentManager().beginTransaction().replace(R.id.rl_fpg,videoSeriesFragment);
                                        }
                                    }
                                }
                            });
                            break;
                        case "3":
                            tv_play.setText(R.string.selection_play);
                            Log.v(TAG, "----------" + vgdetailRst.froms.fromList.size());
                            editor = sp.edit();
                            editor.putInt("checkFragmentLayout", 3);
                            editor.commit();
                            rl_play.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (TPUtil.isNetOkWithToast()) {
                                        if (vgdetailRst.froms.fromList.get(0) != null) {
                                            VideoSeriesFragment videoSeriesFragment = new VideoSeriesFragment();
                                            Bundle bundle = new Bundle();
                                            bundle.putString("dramaurl", vgdetailRst.froms.fromList.get(0).dramaurl);
                                            bundle.putString("issdkplay", vgdetailRst.froms.fromList.get(0).issdkplay);
                                            videoSeriesFragment.setArguments(bundle);
                                            HomeActivity.switchFragment(videoSeriesFragment);
                                        }
                                    }
                                }
                            });
                            break;
                        case "2":
                            tv_play.setText(R.string.play_immediately);
                            rl_play.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (TPUtil.isNetOkWithToast()) {
                                        try {
                                            VgdetailRst.From from = vgdetailRst.froms.fromList.get(0);
                                            if (from.toply == 0) {
                                                PlayerUtil.openVideoPlayer(getActivity(), from.defaulturl);
                                            } else {
                                                go3rdPlayer();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Toast.makeText(getActivity(), R.string.play_url_null, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                            break;

                    }

                    linkUrl = vgdetailRst.linkurl;
                    if(!TextUtils.isEmpty(vgdetailRst.desc)) {
                        String oldDesc = vgdetailRst.desc;
                        String newDesc = oldDesc.replaceAll("\\s*", "");
                        tv_description.setText(newDesc);
                        L.v(TAG, newDesc);
                    }
                    if(!TextUtils.isEmpty(vgdetailRst.name)) {
                        tv_video_name.setText(vgdetailRst.name);
                    }

                    //����ͼƬ
                    ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(vgdetailRst.host, vgdetailRst.shost, vgdetailRst.pic), iv_image1, options);
                    if (vgdetailRst.infos != null && vgdetailRst.infos.infoList!=null) {
                        List<VgdetailRst.Info> infoLists = vgdetailRst.infos.infoList;
                        //���õ�������
                        if(infoLists.size()>0&&infoLists.get(0).vList!=null) {
                            List<VgdetailRst.V> directors = infoLists.get(0).vList;
                            StringBuilder sb1 = new StringBuilder();
                            if (directors.size() != 0) {
                                for (int i = 0; i < directors.size(); i++) {
                                    String name = directors.get(i).name;
                                    if (i == directors.size() - 1) {
                                        sb1.append(name);
                                    } else {
                                        sb1.append(name);
                                        sb1.append("/");
                                    }
                                }
                                tv_director.setText(sb1);
                            }
                            //������������
                            List<VgdetailRst.V> actors = infoLists.get(1).vList;//�������ֵļ���
                            StringBuilder sb2 = new StringBuilder();
                            if (directors.size() != 0) {
                                for (int x = 0; x < actors.size(); x++) {
                                    String name = actors.get(x).name;
                                    if (x == actors.size() - 1) {
                                        sb2.append(name);
                                    } else {
                                        sb2.append(name);
                                        sb2.append("/");
                                    }
                                }
                                tv_actor.setText(sb2);
                            }
                            if (!TextUtils.isEmpty(linkUrl)) {
                                initDataList(linkUrl);
                            }
                        }

                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.sorry_no_the_video_data), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void initDataList(String linkUrl) {
        //����������ҳ��ɹ�ʱ�����������Ƽ���Ƶ�б�
        Request.getInstance().linkvideo(TPUtil.handleUrl(linkUrl), new Callback<LinkvideoRst>() {
            @Override
            public void success(LinkvideoRst linkvideoRst, Response response) {
                    if (linkvideoRst.rcmds != null&&linkvideoRst.rcmds.rcmdList != null) {
                        mLists = linkvideoRst.rcmds.rcmdList;
                        if (mLists != null && mLists.size() > 0) {
                            if (getActivity() != null) {
                                videoDetailsAdapter = new VideoDetailsAdapter(getActivity(), mLists, linkvideoRst);
                                videoDetailsAdapter.setOnItemClickLitener(new VideoDetailsAdapter.OnItemClickLitener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
//                                                    PlayerUtil.openVideoPlayer(getActivity(), mLists.get(position).url);
                                        if (TPUtil.isNetOkWithToast()) {
                                            if (!TextUtils.isEmpty(mLists.get(position).vturl)) {
                                                initData(mLists.get(position).vturl);
                                            } else {
                                                Toast.makeText(getActivity(), R.string.sorry_no_the_video_data, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                                mRecyclerView.setAdapter(videoDetailsAdapter);
                            }
                        }
                    }else {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.no_recommendation_list_data), Toast.LENGTH_SHORT).show();
                        }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.no_related_series), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 去第三方播放
     *
     * @return void
     * @throws
     */
    private void go3rdPlayer() {
        if (TPUtil.isNetOkWithToast()) {

            try {
                String ourl = vgdetailRsts.froms.fromList.get(0).ourl;
                String isSdkPlay = vgdetailRsts.froms.fromList.get(0).issdkplay;

                if (!TextUtils.isEmpty(isSdkPlay) && isSdkPlay.equals("1")) {
                    // 处理sohu视频
                    if (ourl.contains("sohu.com")) {// sohu
                        PlayerUtil.openSohuPlayer(getActivity(), "111", "100tv", ourl,
                                false, vgdetailRsts.froms.fromList.get(0).defaulturl);
                        return;
                    } else if (ourl.contains("letv.com")) {// 乐视
                        if (PlayerUtil.openLetvSdkPlayer(getActivity(),ourl)) {
                            return;
                        }
                    }
                }

                Intent intent = new Intent(getActivity(),
                        WebViewPlayerActivity.class);
                intent.putExtra("xyzplay", vgdetailRsts.froms.fromList.get(0).defaulturl);
                intent.putExtra("ourl", ourl);
                intent.putExtra("btnply", vgdetailRsts.froms.fromList.get(0).btnply);
                intent.putExtra("name", vgdetailRsts.name);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), R.string.play_url_null, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
