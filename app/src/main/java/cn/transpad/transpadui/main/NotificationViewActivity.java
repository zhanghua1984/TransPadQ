package cn.transpad.transpadui.main;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.LinearLayout;

import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;

/**
 * Created by Kongxiaojun on 2015/4/10.
 */
public class NotificationViewActivity extends Activity {

    @InjectView(R.id.nf_content_layout)
    LinearLayout contentLayout;

    Handler mHandler;

    private static final int MSG_WHAT_FINISH_ACTIVITY = 1;

    private Notification notification;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_notification_view);
        initHandler();
        ButterKnife.inject(this);
        updateNotficationView(getIntent());
    }

    void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_WHAT_FINISH_ACTIVITY:
                        finish();
                        break;
                }
            }
        };
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateNotficationView(intent);
    }

    void updateNotficationView(Intent intent){
        mHandler.removeMessages(MSG_WHAT_FINISH_ACTIVITY);
        contentLayout.removeAllViews();
        notification = intent.getParcelableExtra("notification");
        if (notification.contentView != null){
            contentLayout.addView(notification.contentView.apply(this, null));
            mHandler.sendEmptyMessageDelayed(MSG_WHAT_FINISH_ACTIVITY, 5000);
        }else {
            finish();
        }
    }

    @OnClick(R.id.nf_all_layout)
    void openNotification(){
        if (notification != null){
            //通知被点击了
            try {
                notification.contentIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
        finish();
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
