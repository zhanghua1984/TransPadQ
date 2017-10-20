package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.util.ApplicationUtil;

/**
 * Created by ctccuser on 2015/4/6.
 */
public class MainPageDownloadDialog extends Dialog {
    public static final String TAG = MainPageDownloadDialog.class.getSimpleName();
    String description;
    String url;

    private ClickListener listener;
    Context context;

    @InjectView(R.id.download_dialog_message)
    TextView message;

    public MainPageDownloadDialog(Context context) {
        super(context);
    }

    public MainPageDownloadDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    public void setMessage(String description, String url) {
        this.description = description;
        this.url = url;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page_download_dialog);
        ButterKnife.inject(this);
        message.setText(description);
        ApplicationUtil.nonWiFiToast();
    }

    @OnClick(R.id.download_dialog_ok)
    public void updateOk() {
        if (listener != null) {
            listener.onOk();
            Toast.makeText(context, R.string.download_detail_toast, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.download_dialog_cancel)
    public void updateCancel() {
        if (listener != null) {
            listener.onCancel();
        }
    }

    public void setClickListener(ClickListener listener) {
        this.listener = listener;
    }


    public interface ClickListener {

        void onOk();

        void onCancel();
    }
}
