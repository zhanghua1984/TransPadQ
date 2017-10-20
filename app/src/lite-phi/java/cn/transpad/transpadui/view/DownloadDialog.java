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
 * Created by user on 2015/4/27.
 */
public class DownloadDialog extends Dialog {
    public OnDialogClickListener onDialogClickListener;
    private String name;

    public DownloadDialog(Context context) {
        super(context);
    }

    public DownloadDialog(Context context, int theme) {
        super(context, theme);

    }

    @InjectView(R.id.download_text)
    TextView downloadText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_dialog);
        ButterKnife.inject(this);
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected void onStart() {
        super.onStart();
        downloadText.setText(String.format(getContext().getString(R.string.download_immediately), name));
    }

    @OnClick(R.id.download_ok)
    public void startDownload() {
        if (onDialogClickListener != null) {
            onDialogClickListener.onClick();
        }
        dismiss();
    }

    @OnClick(R.id.download_cancel)
    public void cancel() {
        dismiss();
    }

    public void setOnDialogClickListener(OnDialogClickListener onDialogClickListener) {
        this.onDialogClickListener = onDialogClickListener;
    }

    public interface OnDialogClickListener {
        void onClick();
    }
}
