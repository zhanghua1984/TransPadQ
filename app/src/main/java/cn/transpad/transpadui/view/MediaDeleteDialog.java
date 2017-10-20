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
 * Created by wangshaochun on 2015/4/15.
 */
public class MediaDeleteDialog extends Dialog {

    public MediaDeleteDialog(Context context) {
        super(context);
    }

    public MediaDeleteDialog(Context context, int theme) {
        super(context, theme);
    }

    @InjectView(R.id.music_dialog_delete_text)
    TextView deleteTextView;

    String message;

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        deleteTextView.setText(message);
    }

    @OnClick(R.id.music_delete_ok)
    public void deleteItem() {
        if (onDeleteListener != null) {
            onDeleteListener.onDelete();
        }
        dismiss();
    }

    @OnClick(R.id.music_delete_cancel)
    public void cancelDialog() {
        dismiss();
    }

    private OnDeleteListener onDeleteListener;

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    public interface OnDeleteListener {
        void onDelete();
    }

}
