package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;

/**
 * Created by ctccuser on 2015/4/6.
 */
public class DownloadItemDeleteDialog extends Dialog {
    public static final String TAG = DownloadItemDeleteDialog.class.getSimpleName();
    String updateDescription;
    String updateUrl;

    private ClickListener listener;

    @InjectView(R.id.download_delete_message)
    TextView message;

    public DownloadItemDeleteDialog(Context context) {
        super(context);
    }

    public DownloadItemDeleteDialog(Context context, int theme) {
        super(context, theme);
    }

    public void setUpdateMessage(String updateDescription, String updateUrl) {
        this.updateDescription = updateDescription;
        this.updateUrl = updateUrl;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_delete_dialog);
        ButterKnife.inject(this);
//        message.setText(description);
    }

    @OnClick(R.id.download_delete_dialog_ok)
    public void updateOk() {
        if (listener != null) {
            listener.onOk();
        }
    }

    @OnClick(R.id.download_delete_dialog_cancel)
    public void cancel() {
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
