package cn.transpad.transpadui.view;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.http.RstSerializer;
import cn.transpad.transpadui.http.SpecllistRst;
import cn.transpad.transpadui.main.HomeActivity;
import cn.transpad.transpadui.main.MultimediaFragment;
import cn.transpad.transpadui.main.VideoDetailsFragment;
import cn.transpad.transpadui.main.VideoFolderFragment;
import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.PlayerUtil;
import cn.transpad.transpadui.util.TPUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by wangshaochun on 2015/4/7.
 */
public class HomePage2 extends LinearLayout implements View.OnClickListener {

    private SpecllistRst spRst;
    private static final String TAG = "HomePage2";

    @InjectView(R.id.iv_image)
    RoundedImageView iv_image1;
    @InjectView(R.id.iv_image1)
    RoundedImageView iv_image2;
    @InjectView(R.id.iv_image2)
    RoundedImageView iv_image3;
    @InjectView(R.id.iv_image3)
    RoundedImageView iv_image4;
    @InjectView(R.id.iv_image4)
    RoundedImageView iv_image5;
    @InjectView(R.id.iv_image5)
    RoundedImageView iv_image6;
    @InjectView(R.id.tv_image_description1)
    TextView tv_description1;
    @InjectView(R.id.tv_image_description2)
    TextView tv_description2;
    @InjectView(R.id.tv_image_description3)
    TextView tv_description3;
    @InjectView(R.id.tv_image_description4)
    TextView tv_description4;
    @InjectView(R.id.tv_image_description5)
    TextView tv_description5;
    @InjectView(R.id.tv_image_description6)
    TextView tv_description6;

    private DisplayImageOptions options1;
    private DisplayImageOptions options2;

    private Handler mHandler;

    public HomePage2(Context context) {
        super(context);
        init();
    }

    public HomePage2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HomePage2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.home_page2, this);
        ButterKnife.inject(this);
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 1:
                        updateUI((SpecllistRst) msg.obj);
                        break;
                }
            }
        };
        iv_image1.setOnClickListener(this);
        iv_image2.setOnClickListener(this);
        iv_image3.setOnClickListener(this);
        iv_image4.setOnClickListener(this);
        iv_image5.setOnClickListener(this);
        iv_image6.setOnClickListener(this);
        options1 = TPUtil.createDisplayImageOptionsByDrawableId(R.drawable.default_220_292);
        options2 = TPUtil.createDisplayImageOptionsByDrawableId(R.drawable.default_720_340);
        requestSpecl();
    }

    @OnClick(R.id.rl_file)
    void goMultimedia() {
        Fragment fragment = new MultimediaFragment();
        HomeActivity.switchFragment(fragment);
    }

    public void requestSpecl() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String specllist = SharedPreferenceModule.getInstance().getString("specllist");
                if (!TextUtils.isEmpty(specllist)){
                    RstSerializer rst = new RstSerializer();
                    try {
                        final SpecllistRst specllistRst = rst.fromString(SpecllistRst.class, specllist);
                        if (specllistRst != null){
                            Message message = new Message();
                            message.what = 1;
                            message.obj = specllistRst;
                            mHandler.sendMessage(message);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        Request.getInstance().specllist(new Callback<SpecllistRst>() {
            @Override
            public void success(SpecllistRst specllistRst, Response response) {
                L.v(TAG, "specllistRst = " + specllistRst);
                if (specllistRst.result == 0) {
                    RstSerializer rst = new RstSerializer();
                    SharedPreferenceModule.getInstance().setString("specllist", rst.toString(specllistRst));
                    updateUI(specllistRst);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void updateUI(SpecllistRst specllistRst){
        spRst = specllistRst;
        try {
            List<SpecllistRst.Cnt> cntList = specllistRst.poster.cnts.cntList;//海报页集合

            if (cntList.size() > 0) {
                SpecllistRst.Cnt cnt = cntList.get(0);
                tv_description1.setText(cnt.name);
                ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(specllistRst.host, specllistRst.shost, cnt.pic1), iv_image1, options2);
            }
            if (cntList.size() > 1) {
                SpecllistRst.Cnt cnt = cntList.get(1);
                tv_description2.setText(cnt.name);
                ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(specllistRst.host, specllistRst.shost, cnt.pic2), iv_image2, options1);
            }

            if (cntList.size() > 2) {
                SpecllistRst.Cnt cnt = cntList.get(2);
                tv_description3.setText(cnt.name);
                ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(specllistRst.host, specllistRst.shost, cnt.pic2), iv_image3, options1);
            }
            if (cntList.size() > 3) {
                SpecllistRst.Cnt cnt = cntList.get(3);
                tv_description4.setText(cnt.name);
                ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(specllistRst.host, specllistRst.shost, cnt.pic2), iv_image4, options1);
            }
            if (cntList.size() > 4) {
                SpecllistRst.Cnt cnt = cntList.get(4);
                tv_description5.setText(cnt.name);
                ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(specllistRst.host, specllistRst.shost, cnt.pic2), iv_image5, options1);
            }
            if (cntList.size() > 5) {
                SpecllistRst.Cnt cnt = cntList.get(5);
                tv_description6.setText(cnt.name);
                ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(specllistRst.host, specllistRst.shost, cnt.pic2), iv_image6, options1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {//打开此页面的多个点击事件
            case R.id.iv_image:
                //打开澳门风云
                if (TPUtil.isNetOkWithToast()) {
                    if (spRst != null) {
                        if (spRst.poster.cnts.cntList.size() >= 1) {
                            SpecllistRst.Cnt cnt = spRst.poster.cnts.cntList.get(0);

                            if (cnt.url.contains("xyzplay")) {
                                PlayerUtil.openVideoPlayer(getContext(), cnt.url);
                            } else {
                                Fragment fragment = new VideoDetailsFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("url", cnt.url);
                                fragment.setArguments(bundle);
                                HomeActivity.switchFragment(fragment);
                            }
                        }
                    }
                }
                break;
            case R.id.iv_image1:
                if (TPUtil.isNetOkWithToast()) {
                    if (spRst != null) {
                        if (spRst.poster.cnts.cntList.size() >= 2) {

                            SpecllistRst.Cnt cnt = spRst.poster.cnts.cntList.get(1);
                            String titleName = cnt.name;
                            Fragment fragment = new VideoFolderFragment(titleName);
                            Bundle bundle = new Bundle();
                            bundle.putString("url",cnt.url);
                            fragment.setArguments(bundle);
                            HomeActivity.switchFragment(fragment);
                        }
                    }
                }
                break;
            case R.id.iv_image2:
                if (TPUtil.isNetOkWithToast()) {
                    if (spRst != null) {
                        if (spRst.poster.cnts.cntList.size() >= 3) {
                            SpecllistRst.Cnt cnt = spRst.poster.cnts.cntList.get(2);
                            String titleName = cnt.name;
                            Fragment fragment = new VideoFolderFragment(titleName);
                            Bundle bundle = new Bundle();
                            bundle.putString("url",cnt.url);
                            fragment.setArguments(bundle);
                            HomeActivity.switchFragment(fragment);
                        }
                    }
                }
                break;
            case R.id.iv_image3:
                if (TPUtil.isNetOkWithToast()) {
                    if (spRst != null) {
                        if (spRst.poster.cnts.cntList.size() >= 4) {
                            SpecllistRst.Cnt cnt = spRst.poster.cnts.cntList.get(3);
                            String titleName = cnt.name;
                            Fragment fragment = new VideoFolderFragment(titleName);
                            Bundle bundle = new Bundle();
                            bundle.putString("url",cnt.url);
                            fragment.setArguments(bundle);
                            HomeActivity.switchFragment(fragment);
                        }
                    }
                }
                break;
            case R.id.iv_image4:
                if (TPUtil.isNetOkWithToast()) {
                    if (spRst != null) {
                        if (spRst.poster.cnts.cntList.size() >= 5) {
                            SpecllistRst.Cnt cnt = spRst.poster.cnts.cntList.get(4);
                            String titleName = cnt.name;
                            Fragment fragment = new VideoFolderFragment(titleName);
                            Bundle bundle = new Bundle();
                            bundle.putString("url",cnt.url);
                            fragment.setArguments(bundle);
                            HomeActivity.switchFragment(fragment);
                        }
                    }
                }
                break;
            case R.id.iv_image5:
                if (TPUtil.isNetOkWithToast()) {
                    if (spRst != null) {
                        if (spRst.poster.cnts.cntList.size() >= 6) {
                            SpecllistRst.Cnt cnt = spRst.poster.cnts.cntList.get(5);
                            String titleName = cnt.name;
                            Fragment fragment = new VideoFolderFragment(titleName);
                            Bundle bundle = new Bundle();
                            bundle.putString("url",cnt.url);
                            fragment.setArguments(bundle);
                            HomeActivity.switchFragment(fragment);
                        }
                    }
                }
                break;
        }
    }
}
