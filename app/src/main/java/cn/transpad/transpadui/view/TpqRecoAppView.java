package cn.transpad.transpadui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.TpqRecoAppAdapter;
import cn.transpad.transpadui.http.SoftRst;

/**
 * Created by Kongxiaojun on 2015/6/15.
 */
public class TpqRecoAppView extends RelativeLayout {

    private SoftRst.Col col;

    private String host;

    private String shost;

    @InjectView(R.id.app_apge_title)
    TextView title;
    @InjectView(R.id.tpq_reco_app_rv)
    RecyclerView recyclerView;
    private TpqRecoAppAdapter adapter;

    public TpqRecoAppView(Context context, SoftRst.Col col, String host, String shost) {
        super(context);
        this.col = col;
        this.host = host;
        this.shost = shost;
        init();
    }

    private TpqRecoAppView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private TpqRecoAppView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.tpq_reco_app_layout, this);
        ButterKnife.inject(this);
        title.setText(col.name);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 5);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDraw(c, parent, state);
            }

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, 10, 0, 10);
            }
        });
        adapter = new TpqRecoAppAdapter(col.cnts.cntList, host, shost);
        recyclerView.setAdapter(adapter);
    }

}
