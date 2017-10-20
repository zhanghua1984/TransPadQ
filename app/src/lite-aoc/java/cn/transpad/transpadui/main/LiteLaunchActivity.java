package cn.transpad.transpadui.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.dlna.DLNAPlayer;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.http.LoginRst;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.http.SoftRst;
import cn.transpad.transpadui.receiver.SuicideReceiver;
import cn.transpad.transpadui.service.TransPadService;
import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;
import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LiteLaunchActivity extends Activity {
    private static final String TAG = "LiteLaunchActivity";

    private static final int OPEN_HOME = 1;

    private Handler mHandler;

    private Animation mBottomInAnim, mAlphaAnim;

    @InjectView(R.id.main_logo)
    ImageView logo;
    @InjectView(R.id.main_bg)
    ImageView background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.v(TAG, "onCreate");
        //检查版本
        if (Build.VERSION.SDK_INT < 17) {
            showSdkVersionLowDialog();
        } else {
            Intent intent = new Intent(this.getApplicationContext(), LiteHomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            TransPadService.getInstance().setHomeIntent(intent);
            TransPadService.getInstance().onCreate(this.getApplicationContext());
            DLNAPlayer.getInstance(this.getApplication());
            boolean isFirstLaunch = SharedPreferenceModule.getInstance().getBoolean(LiteHomeActivity.FIRST_LAUNCH, true);
            if (isFirstLaunch) {
                SharedPreferenceModule.getInstance().setBoolean("is_land_screen", true);
                TransPadService.setLandScreen(true);
            }
            //应用互斥
            Intent intentReceiver = new Intent(SuicideReceiver.ACTION);
            intentReceiver.putExtra(SuicideReceiver.PACKAGE_NAME, getPackageName());
            sendBroadcast(intentReceiver);

            StorageModule.getInstance().startCacheService();
            setContentView(R.layout.activity_lite_launch);
            ButterKnife.inject(this);
            logo.setImageResource(getPackageName().equals("cn.transpad.transpadui.lite.aoc") ? R.drawable.logo_launch_aoc : R.drawable.logo_launch);
            startAnim();
            initHandler();
            EventBus.getDefault().register(this);
            requestHomeData();
        }
    }

    private void startAnim() {
        mBottomInAnim = AnimationUtils.loadAnimation(this, R.anim.push_bottom_in);
        mBottomInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mHandler.sendEmptyMessage(OPEN_HOME);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mAlphaAnim = AnimationUtils.loadAnimation(this, R.anim.main_page_alpha_anim);
        mAlphaAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                logo.setVisibility(View.VISIBLE);
                logo.startAnimation(mBottomInAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        background.setVisibility(View.VISIBLE);
        background.startAnimation(mAlphaAnim);
    }

    private void showSdkVersionLowDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_prompt)
                .setMessage(R.string.system_version_low)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case OPEN_HOME:
                        Intent intent = new Intent(LiteLaunchActivity.this, LiteHomeActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                }
            }
        };
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
                    TPUtil.saveServerData(t, "tpq_home_softrst");
                }
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
        Request.getInstance().login(0, new Callback<LoginRst>() {
            @Override
            public void success(LoginRst loginRst, Response response) {
                if (loginRst.result == 0 && loginRst.showmedie != null) {
                    TransPadApplication.getTransPadApplication().setShowmedia(loginRst.showmedie);
                }
                if (loginRst.result == 0 && loginRst.rec != null) {
                    L.v(TAG, "success rec = " + loginRst.rec);
                    TransPadApplication.getTransPadApplication().setRec(loginRst.rec);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                L.v(TAG, "failure error = " + error.getMessage());
            }
        });
    }


    public void onEventMainThread(Message message) {
        switch (message.what) {
            case TransPadService.TRANSPAD_STATE_DISCONNECTED:
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        background.setImageBitmap(null);
        logo.setImageBitmap(null);
    }

}
