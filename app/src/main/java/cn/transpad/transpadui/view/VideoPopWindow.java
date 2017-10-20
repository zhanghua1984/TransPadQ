package cn.transpad.transpadui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.util.ModifyNameComparator;
import cn.transpad.transpadui.util.ModifyTimeComparator;

/**
 * Created by wangshaochun on 2015/4/17.
 */
public class VideoPopWindow extends PopupWindow {
    View view;
    private VideoSortClickListener mVideoSortClickListener = null;
    private boolean byUpdateTimeFlag = false;
    private boolean byNameFlag = false;
    private boolean byPlayTimeFlag = false;
    public VideoPopWindow(Context context) {
        super(context);
        view = LayoutInflater.from(context).inflate(R.layout.videolist_sort_pop, null);
        ButterKnife.inject(this, view);
        setContentView(view);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(true);
        setFocusable(true);
        //设置透明背景
        setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
    }
    @OnClick(R.id.ll_update_time)
    public void byUpdateTime(){//按更新时间排序
        if(mVideoSortClickListener != null){
            if(byUpdateTimeFlag){
                mVideoSortClickListener.modifyTimeClick(ModifyTimeComparator.SORT_UP_TYPE);
            }else{
                mVideoSortClickListener.modifyTimeClick(ModifyTimeComparator.SORT_DOWN_TYPE);
            }
            byUpdateTimeFlag = !byUpdateTimeFlag;
        }
        dismiss();
    }
    @OnClick(R.id.ll_file_name)
    public void byFileName(){//按文件名称排序
        if(mVideoSortClickListener != null){
            if(byNameFlag){
                mVideoSortClickListener.nameClick(ModifyNameComparator.SORT_UP_TYPE);
            }else{
                mVideoSortClickListener.nameClick(ModifyNameComparator.SORT_DOWN_TYPE);
            }
            byNameFlag = !byNameFlag;
        }
        dismiss();
    }
//    @OnClick(R.id.ll_play_time)
//    public void ll_play_time(){//按视频时长排序   获取不到本地视频，暂时不做此功能
//        if(mVideoSortClickListener != null){
//            if(byPlayTimeFlag){
//                mVideoSortClickListener.playTimeClick(ModifyPlayTimeComparator.SORT_UP_TYPE);
//            }else{
//                mVideoSortClickListener.playTimeClick(ModifyPlayTimeComparator.SORT_DOWN_TYPE);
//            }
//            byPlayTimeFlag = !byPlayTimeFlag;
//        }
//        dismiss();
//    }
    public void setVideoSortClickListener(VideoSortClickListener videoSortClickListener){
        mVideoSortClickListener = videoSortClickListener;
    }
    public interface VideoSortClickListener{

        public void modifyTimeClick(int sortType);

        public void nameClick(int sortType);

        public void playTimeClick(int sortType);
    }
}
