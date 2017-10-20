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

import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.receiver.SuicideReceiver;
import cn.transpad.transpadui.service.TransPadService;
import cn.transpad.transpadui.storage.StorageModule;
import de.greenrobot.event.EventBus;

/**
 * Created by Kongxiaojun on 2015/4/8.
 */
public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

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
        //检查版本
        if (Build.VERSION.SDK_INT < 17) {
            showSdkVersionLowDialog();
        } else {

            //应用互斥
            Intent intentReceiver = new Intent(SuicideReceiver.ACTION);
            intentReceiver.putExtra(SuicideReceiver.PACKAGE_NAME, getPackageName());
            sendBroadcast(intentReceiver);

            StorageModule.getInstance().startCacheService();
            setContentView(R.layout.activity_main);
            ButterKnife.inject(this);
            startAnim();
            initHandler();
            EventBus.getDefault().register(this);
            TransPadService.getInstance().onCreate(this);
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
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                }
            }
        };
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

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart(this.getClass().getSimpleName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd(this.getClass().getSimpleName());
    }
}
