package cn.transpad.transpadui.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.ScreenUtil;

/**
 * Created by Kongxiaojun on 2015/4/17.
 */
public class DongleHomeAppView extends ViewGroup{

    private static final int MSG_WHAT_LONG_CLICK = 1;

    /**
     * 判断长按事件的事件偏差
     */
    private static final int LONG_CLICK_TIME_OFFSET = 1000;

    /**
     * 判断点击事件的距离偏差
     */
    private static final int CLICK_DISTANCE_OFFSET = 100;

    /**
     * 判断点击事件的时间偏差(毫秒)
     */
    private static final int CLICK_TIME_OFFSET = 200;

    private static final String TAG = "DongleHomeAppView";

    /**
     * 行数
     */
    private int rowCount = 3;

    /**
     * 列数
     */
    private int columnCount = 5;

    /**
     * 应用item 宽度
     */
    private int itemWidth = 100;

    /**
     * 应用item 高度
     */
    private int itemHeight = 100;

    private int dongleMenuViewWidth = ScreenUtil.dp2px(80);

    private int paddingleft = ScreenUtil.dp2px(10);
    private int paddingRight = ScreenUtil.dp2px(40);

    private int paddingTop = ScreenUtil.dp2px(55);
    private int paddingBottom = ScreenUtil.dp2px(30);

    DongleMenuView dongleMenuView;

    DongeHomeAppAdapter adapter;

    private List<View> itemViews = new ArrayList<View>();

    private long touchDownTime;

    private int initialX;
    private int initialY;
    private int lastTouchX;
    private int lastTouchY;

    private ImageView dragView;

    private int dragPosition = -1;

    private boolean dragMode;

    private Handler mHandler;

    private OnItemClickListener itemClickListener;

    private TextView tv_title;
    private View titleBackground;

    private static final int TITLE_TEXT_SIZE_DP = 20;

    public DongleHomeAppView(Context context) {
        super(context);
        init();
    }

    public DongleHomeAppView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DongleHomeAppView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        L.v(TAG, "init");
        createDongHomeWindowAppView();
        initHandler();
        createDragView();
    }

    private void initHandler() {
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case MSG_WHAT_LONG_CLICK://长按
                        onLongClick();
                        break;
                }
            }
        };
    }

    private void createDongHomeWindowAppView() {
        L.v(TAG, "createDongHomeWindowAppView");
        dongleMenuView = new DongleMenuView(getContext());
        addView(dongleMenuView);
        dongleMenuView.setVisibility(INVISIBLE);
    }

    private void createDragView() {
        L.v(TAG, "createDragView");
        if (dragView != null){
            removeView(dragView);
        }
        dragView = new ImageView(getContext());
        addView(dragView);
    }

    private void createTitleView(){
        if (tv_title != null){
            removeView(tv_title);
        }
        tv_title = new TextView(getContext());
        if (adapter != null){
            tv_title.setText(adapter.getTitle());
        }else {
            tv_title.setText(R.string.dongle_home_app_title);
        }
        tv_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, ScreenUtil.dp2px(TITLE_TEXT_SIZE_DP));
        tv_title.setGravity(Gravity.CENTER);
        tv_title.setTextColor(Color.WHITE);
        tv_title.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        titleBackground = LayoutInflater.from(getContext()).inflate(R.layout.dongle_home_title_bg,null);
        addView(titleBackground);
        addView(tv_title);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        L.v(TAG, "onMeasure");
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

//        dongleMenuView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        itemWidth = (widthSize - dongleMenuViewWidth - paddingleft - paddingRight) / columnCount;
        itemHeight = (heightSize - paddingTop - paddingBottom) / rowCount;
        measureChildren(widthSize, heightSize);
        measureChild(dongleMenuView, MeasureSpec.makeMeasureSpec(dongleMenuViewWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
        setMeasuredDimension(widthSize, heightSize);
    }

    /**
     * 刷新app子View
     */
    private void reflushChildrenView() {
        L.v(TAG, "addChildrenView");
        if (itemViews.size() > 0){
            for (View v : itemViews){
                removeView(v);
            }
            itemViews.clear();
        }
        if (adapter != null && adapter.getCount() > 0) {
            for (int i = 0; i < adapter.getCount(); i++) {
                View view = adapter.getView(i, this);
                addView(view);
                if (view != dongleMenuView) {
                    view.setDrawingCacheEnabled(true);
                    itemViews.add(view);
                }
            }
        }
        createDragView();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
       // L.v(TAG, "onLayout l = " + l + "   t = " + t + "  r = " + r + "  b = " + b);
        layoutChildren(l, t, r, b);
    }

    private void layoutChildren(int l, int t, int r, int b) {
       // L.v(TAG, "layoutChildren dongleMenuView width = " + (getPaddingLeft() - 10) + "   height = " + getMeasuredHeight());
        if (itemViews.size() > 0) {
            for (int i = 0; i < itemViews.size(); i++) {
                int row = i / columnCount;
                int column = i % columnCount;
                View view = itemViews.get(i);
                int top = row * itemHeight + paddingTop;
                int left = dongleMenuViewWidth + paddingleft + column * itemWidth;
                view.layout(left, top, left + itemWidth, top + itemHeight);
            }
        }
        dongleMenuView.layout(l, t, dongleMenuViewWidth, b);
        dragView.layout(-800, 0, -800 + itemWidth, itemHeight);
        titleBackground.layout(l,t,r,titleBackground.getMeasuredHeight());
        tv_title.layout((ScreenUtil.getScreenWidthPix(getContext()) - tv_title.getMeasuredWidth()) / 2, ScreenUtil.dp2px(6), (ScreenUtil.getScreenWidthPix(getContext()) - tv_title.getMeasuredWidth()) / 2 + tv_title.getMeasuredWidth(), 140);
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        L.v(TAG, "onInterceptTouchEvent");
        return isDragMode();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        L.v(TAG,"onTouchEvent");
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                touchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(event);
                break;
            case MotionEvent.ACTION_UP:
                touchUp(event);
                break;
            case MotionEvent.ACTION_CANCEL:
                L.v(TAG,"onTouchEvent ACTION_CANCEL");
                mHandler.removeMessages(MSG_WHAT_LONG_CLICK);
                break;
        }
        return true;
    }

    private void touchDown(MotionEvent event) {
        touchDownTime = System.currentTimeMillis();
        initialX = (int) event.getRawX();
        initialY = (int) event.getRawY();

        lastTouchX = (int) event.getRawX();
        lastTouchY = (int) event.getRawY();
        mHandler.sendEmptyMessageDelayed(MSG_WHAT_LONG_CLICK, LONG_CLICK_TIME_OFFSET);
    }


    private void touchMove(MotionEvent event) {
        L.v(TAG,"touchMove");
        lastTouchX = (int) event.getRawX();
        lastTouchY = (int) event.getRawY();
        int distance = getDistance(initialX, initialY, lastTouchX, lastTouchY);
        if (distance > CLICK_DISTANCE_OFFSET){
            //取消长按
            mHandler.removeMessages(MSG_WHAT_LONG_CLICK);
        }
        if (isDragMode()){
//            dragView.scrollTo(lastTouchX,lastTouchY);
            dragView.layout(lastTouchX - (int)(itemWidth*1.3) / 2, lastTouchY - (int)(itemHeight*1.3) / 2, lastTouchX + (int)(itemWidth*1.3) / 2, lastTouchY + (int)(itemHeight*1.3) / 2);
        }
    }

    private int getDistance(int startX, int startY, int endX, int endY) {
        return (int)Math.hypot(endX - startX,endY - startY);
    }

    private void touchUp(MotionEvent event) {
        L.v(TAG,"touchUp");
        getParent().requestDisallowInterceptTouchEvent(false);
        //非长按事件
        mHandler.removeMessages(MSG_WHAT_LONG_CLICK);
        lastTouchX = (int) event.getRawX();
        lastTouchY = (int) event.getRawY();

        if (dragMode){
            //拖拽结束
            dragEnd();
        }else {
            if (System.currentTimeMillis() - touchDownTime < CLICK_TIME_OFFSET && getDistance(initialX, initialY, lastTouchX, lastTouchY) < CLICK_DISTANCE_OFFSET){
                if (itemClickListener != null){
                    int pos = positionForView();
                    if (pos >= 0) {
                        itemClickListener.onItemClick(pos);
                    }
                }
            }
        }
    }

    /**
     * 拖拽结束
     */
    private void dragEnd(){
        dragMode = false;
        dragView.setVisibility(INVISIBLE);
        //检查结束位置是否在侧边栏中
        if (lastTouchX < dongleMenuViewWidth){
            //在侧边栏
            if (lastTouchY > getResources().getDimensionPixelOffset(R.dimen.dongle_window_app_layout_margin_top)){
                //在三个app区域内
                if (lastTouchY < (getResources().getDimensionPixelOffset(R.dimen.dongle_window_first_app_margin_top) + getResources().getDimensionPixelOffset(R.dimen.dongle_window_app_item_height))){
                    //在第一个icon区域内
                    adapter.onDrageEnd(dragPosition,0);
                }else if (lastTouchY < (getResources().getDimensionPixelOffset(R.dimen.dongle_window_first_app_margin_top) + getResources().getDimensionPixelOffset(R.dimen.dongle_window_app_item_height)*2)){
                    //在第二个icon区域内
                    adapter.onDrageEnd(dragPosition,1);
                }else if ((lastTouchY > (getResources().getDimensionPixelOffset(R.dimen.dongle_window_first_app_margin_top) + getResources().getDimensionPixelOffset(R.dimen.dongle_window_app_item_height)*2 + getResources().getDimensionPixelOffset(R.dimen.dongle_window_back_height))) && (lastTouchY < (getResources().getDimensionPixelOffset(R.dimen.dongle_window_first_app_margin_top) + getResources().getDimensionPixelOffset(R.dimen.dongle_window_app_item_height)*3 + getResources().getDimensionPixelOffset(R.dimen.dongle_window_back_height)))){
                    //在第三个icon区域内
                    adapter.onDrageEnd(dragPosition,2);
                }else if ((lastTouchY > (getResources().getDimensionPixelOffset(R.dimen.dongle_window_first_app_margin_top) + getResources().getDimensionPixelOffset(R.dimen.dongle_window_app_item_height)*3 + getResources().getDimensionPixelOffset(R.dimen.dongle_window_back_height))) && (lastTouchY < (getResources().getDimensionPixelOffset(R.dimen.dongle_window_first_app_margin_top) + getResources().getDimensionPixelOffset(R.dimen.dongle_window_app_item_height)*4 + getResources().getDimensionPixelOffset(R.dimen.dongle_window_back_height)))){
                    //在第四个icon区域内
                    adapter.onDrageEnd(dragPosition,3);
                }
            }
        }
        adapter.onDrageEnd(-1, -1);
        dongleMenuView.setVisibility(INVISIBLE);
        dragPosition = -1;
    }

    private boolean isDragMode() {
        return dragMode;
    }

    public void onLongClick() {
        L.v(TAG,"onLongClick");
        int pos = positionForView();
        itemClickListener.onItemClick(pos);
//        if (TransPadService.getConnectDeviceType() == TransPadService.TRANSPAD_DEVICE_TYPE_Q && adapter != null && pos != -1 && adapter.canDrag(pos)) {
//            dongleMenuView.setVisibility(VISIBLE);
//            adapter.onDrageStart();
//            L.v(TAG, "onLongClick positionForView(v) != -1");
//            getParent().requestDisallowInterceptTouchEvent(true);
//            dragPosition = pos;
//            View v = getChildView(dragPosition);
//            dragMode = true;
//
//            dragView.setImageBitmap(v.getDrawingCache());
//            dragView.layout(lastTouchX - itemWidth / 2, lastTouchY - itemHeight / 2, lastTouchX + itemWidth / 2, lastTouchY + itemHeight / 2);
//            dragView.setVisibility(VISIBLE);
//            //添加一个滑动View
////            dragView.scrollTo(lastTouchX, lastTouchY);
//        }else if (adapter != null && pos != -1){
//            itemClickListener.onItemClick(pos);
//        }
    }

    private int positionForView() {
        for (int index = 0; index < getItemViewCount(); index++) {
            View child = getChildView(index);
            if (isPointInsideView(initialX, initialY, child)) {
                return index;
            }
        }
        return -1;
    }

    private boolean isPointInsideView(float x, float y, View view) {
        int location[] = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        if (pointIsInsideViewBounds(x, y, view, viewX, viewY)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean pointIsInsideViewBounds(float x, float y, View view, int viewX, int viewY) {
        return (x > viewX && x < (viewX + view.getWidth())) && (y > viewY && y < (viewY + view.getHeight()));
    }

    private int getItemViewCount() {
        return itemViews.size();
    }

    public View getChildView(int index) {
        return itemViews.get(index);
    }

    public void setAdapter(DongeHomeAppAdapter adapter) {
        L.v(TAG, "setAdapter");
        this.adapter = adapter;
        columnCount = adapter.getColumnCount();
        rowCount = adapter.getRowCount();
        createTitleView();
        reflushChildrenView();
    }

    public DongeHomeAppAdapter getAdapter() {
        return adapter;
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void notifyDataSetChanged() {
        L.v(TAG,"notifyDataSetChanged");
        reflushChildrenView();
        requestLayout();
    }

    public interface OnItemClickListener {
        void onItemClick(int postion);
    }

    public interface DongeHomeAppAdapter {

        /**
         * 获取标题
         * @return
         */
        String getTitle();

        /**
         * 获取item数量
         */
        int getCount();

        /**
         * 获取行数
         */
        int getRowCount();

        /**
         * 获取列数
         */
        int getColumnCount();

        /**
         * 获取item id
         */
        int getItemId(int postion);

        /**
         * 获取item
         */
        Object getItem(int postion);

        /**
         * 获取item view
         */
        View getView(int postion, View parentView);

        boolean canDrag(int postion);

        void onDrageStart();

        /***
         * 当拖拽结束后，并且结束位置在三个icon区域内
         * @param startPos 拖拽的是那个应用的位置
         * @param tartgerPos 目标在三个icon区域内的位置,从0开始
         */
        void onDrageEnd(int startPos,int tartgerPos);
    }
}
