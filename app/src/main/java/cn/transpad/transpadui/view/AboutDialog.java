package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.util.L;

/**
 * Created by wangyang on 2015/9/23.
 */
public class AboutDialog extends Dialog {
    private static final String TAG = "AboutDialog";
    int layout;
    Context context;
    @InjectView(R.id.btn_close)
    Button mCloseButton;
    @InjectView(R.id.tvVersion)
    TextView mVersionTextView;

    public AboutDialog(Context context) {
        super(context);
        setCanceledOnTouchOutside(false);
        this.context = context;
    }

    public AboutDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_dialog);
        ButterKnife.inject(this);
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (Exception e) {

        }
        mVersionTextView.setText(info.versionName);
    }

    @OnClick(R.id.btn_close)
    public void close() {
        dismiss();
    }

    @Override
    protected void onStop() {
        super.onStop();
        L.v(TAG, "onStop");
    }
}
