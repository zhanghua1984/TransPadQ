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
 * Created by left on 16/1/4.
 */
public class LiteAboutDialog extends Dialog {
    private static final String TAG = "AboutDialog";
    int layout;
    @InjectView(R.id.btn_close)
    Button mCloseButton;
    @InjectView(R.id.tvVersion)
    TextView mVersionTextView;
    @InjectView(R.id.tv_appname)
    TextView mAppName;

    public LiteAboutDialog(Context context) {
        super(context);
        setCanceledOnTouchOutside(false);
    }

    public LiteAboutDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_dialog);
        ButterKnife.inject(this);
        PackageInfo info = null;
        try {
            info = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
        } catch (Exception e) {

        }
        mAppName.setText(getContext().getPackageName().equals("cn.transpad.transpadui.lite.aoc") ? R.string.app_about_name_aoc : R.string.app_about_name);
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
