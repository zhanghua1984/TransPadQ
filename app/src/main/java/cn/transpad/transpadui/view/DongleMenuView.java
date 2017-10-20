package cn.transpad.transpadui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;

/**
 * Created by Kongxiaojun on 2015/4/17.
 */
public class DongleMenuView extends LinearLayout {

    @InjectView(R.id.iv1)
    ImageView iv1;
    @InjectView(R.id.iv2)
    ImageView iv2;
    @InjectView(R.id.iv3)
    ImageView iv3;
    @InjectView(R.id.iv4)
    ImageView iv4;

    public DongleMenuView(Context context) {
        super(context);
        init();
    }

    public DongleMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DongleMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.tv_tpq_menu_page1, this);
        ButterKnife.inject(this);
    }

}
