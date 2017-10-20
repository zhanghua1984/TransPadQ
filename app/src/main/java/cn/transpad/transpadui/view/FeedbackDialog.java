package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.transpad.transpadui.R;

/**
 * Created by ctccuser on 2015/4/6.
 */
public class FeedbackDialog extends Dialog {
    ClickListener clickListener;

    public FeedbackDialog(Context context) {
        super(context);
    }

    public FeedbackDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback_dialog);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.feedback_dialog_known)
    public void known() {
        clickListener.ok();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void ok();
    }
}
