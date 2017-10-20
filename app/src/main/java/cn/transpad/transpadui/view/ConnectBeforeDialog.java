package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.util.BandUtil;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;

/**
 * 连接设备之后的弹框
 */
public class ConnectBeforeDialog extends Dialog {
    Context mContext;
    @InjectView(R.id.download_message)
    TextView mMessageTextView;

    public ConnectBeforeDialog(Context context) {
        super(context, R.style.myDialog);
        mContext = context;
        setCanceledOnTouchOutside(false);
    }

    public ConnectBeforeDialog(Context context, int theme) {
        super(context, R.style.myDialog);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_wifiband_dialog);
        ButterKnife.inject(this);
        if (Build.VERSION.SDK_INT >= 21) {
            if (BandUtil.is24GHzWifiNet()) {
                mMessageTextView.setText(R.string.msg_5_0);
            }
        } else {
            mMessageTextView.setText(R.string.msg_not_5_0);
        }
    }

    @OnClick(R.id.btn_learn_more)
    public void ok() {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        mContext.startActivity(intent);
        dismiss();
    }

    @OnClick(R.id.btn_no_care)
    public void cancle() {
        TPUtil.connectTransPad();
        dismiss();
    }

    @OnCheckedChanged(R.id.ckb_show_again)
    public void again(CompoundButton buttonView, boolean isChecked) {
        L.v("ConnectAfterDialog", "again", "isChecked=" + isChecked);
        SharedPreferenceModule.getInstance().setBoolean("is_show_before_dialog", !isChecked);
    }
}
