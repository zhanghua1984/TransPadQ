package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import cn.transpad.transpadui.R;

/**
 * Created by Kongxiaojun on 2015/7/1.
 */
public class LoadingDialog extends Dialog {

    private ImageView imageView;
    private View contentView;
    private Animation animation;

    public LoadingDialog(Context context) {
        super(context, R.style.dialog_base);
        init();
    }

    public LoadingDialog(Context context, int theme) {
        super(context, theme);
        init();
    }

    private void init() {
        contentView = LayoutInflater.from(getContext()).inflate(R.layout.loading_dialog,null);
        setContentView(contentView);
        imageView = (ImageView) contentView.findViewById(R.id.loading_dialog_view);
        animation = AnimationUtils.loadAnimation(getContext(),R.anim.loading_dialog_rotating);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
    }

    @Override
    public void show() {
        super.show();
        imageView.startAnimation(animation);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        imageView.clearAnimation();
    }

    @Override
    public void hide() {
        super.hide();
        imageView.clearAnimation();
    }
}
