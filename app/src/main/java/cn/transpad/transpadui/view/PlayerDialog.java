package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;

/**
 * Created by Kongxiaojun on 2015/1/23.
 * 自定义带标题和按钮的dialog
 */
public class PlayerDialog extends Dialog {

    private View content;

    @InjectView(R.id.dialog_title)
    TextView title;
    @InjectView(R.id.dialog_content)
    LinearLayout contentLayout;
    @InjectView(R.id.button_layout)
    LinearLayout buttonLayout;
    @InjectView(R.id.dialog_button_left)
    Button leftBt;
    @InjectView(R.id.dialog_button_right)
    Button rightBt;

    public PlayerDialog(Context context) {
        super(context, R.style.dialog_base);
        init();
    }

    public PlayerDialog(Context context, int theme) {
        super(context, theme);
        init();
    }

    protected PlayerDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    private void init() {
        content = LayoutInflater.from(getContext()).inflate(R.layout.dialog_base, null);
        super.setContentView(content);
        ButterKnife.inject(this);
    }

    /**
     * 设置标题
     *
     * @param title1
     */
    public void setTitle(String title1) {
        title.setText(title1);
    }

    /**
     * 设置标题
     *
     * @param resId
     */
    public void setTitle(int resId) {
        title.setText(resId);
    }

    /**
     * 设置左侧按钮 文字 和点击事件
     *
     * @param text
     * @param listener
     */
    public void setLeftButton(String text, View.OnClickListener listener) {
        buttonLayout.setVisibility(View.VISIBLE);
        leftBt.setVisibility(View.VISIBLE);
        leftBt.setText(text);
        leftBt.setOnClickListener(listener);
    }

    /**
     * 设置右侧按钮 文字 和点击事件
     *
     * @param text
     * @param listener
     */
    public void setRightButton(String text, View.OnClickListener listener) {
        buttonLayout.setVisibility(View.VISIBLE);
        rightBt.setVisibility(View.VISIBLE);
        rightBt.setText(text);
        rightBt.setOnClickListener(listener);
    }

    /**
     * 设置内容view
     *
     * @param layoutResID
     */
    @Override
    public void setContentView(int layoutResID) {
        View view = LayoutInflater.from(getContext()).inflate(layoutResID, null);
        setContentView(view);

    }

    /**
     * 设置内容view
     *
     * @param view
     */
    @Override
    public void setContentView(View view) {
        contentLayout.addView(view);
    }

    /**
     * 设置内容view和参数
     *
     * @param view
     * @param params
     */
    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        contentLayout.addView(view, params);
    }

}
