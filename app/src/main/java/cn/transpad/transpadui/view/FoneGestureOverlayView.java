package cn.transpad.transpadui.view;

import android.content.Context;
import android.gesture.GestureOverlayView;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

import cn.transpad.transpadui.player.gesture.TouchPointersUtil;


/**
 * 
 * 3.4上移植过来的代码，全屏播放时显示手势的View
 * 
 * @author kongxiaojun
 * @since 2014-4-21
 *
 */
public class FoneGestureOverlayView extends GestureOverlayView {


    private TouchPointersUtil mTouchPointersUtil;
    
	public FoneGestureOverlayView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        TouchPointersInit();
	}

	public FoneGestureOverlayView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FoneGestureOverlayView(Context context) {
		this(context, null);
	}
    

    public float getAngle(float x0, float y0, float x1, float y1){
    	return (float) Math.atan((y1 - y0) / (x1 - x0));
    }
    
    @Override
	public void draw(Canvas canvas) {
    }
    
    public void setGestureEvent(MotionEvent event){
        int action = event.getAction();
        switch(action){
        case MotionEvent.ACTION_DOWN:
            break;
        case MotionEvent.ACTION_UP:
            break;
        case MotionEvent.ACTION_MOVE:
            break;
        default:
            break;
        }
    }
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getPointerCount() == 1) {
		    setGestureEvent(event);
		    
		}else if(event.getPointerCount() == 2){
		    setTouchPointersEvent(event);
		}
		return true;
	}
	
	
	public void TouchPointersInit(){
	    mTouchPointersUtil = new TouchPointersUtil();
	}
	
	public void TouchPointersRelease(){
	    mTouchPointersUtil = null;
	}
	
	public void setTouchPointersEvent(MotionEvent event){
        if(mTouchPointersUtil != null){
            mTouchPointersUtil.setMoveEvent(event);
        }
    
        if(mTouchPointersUtil != null){
            mTouchPointersUtil.setZoomEvent(event);
        }
    }
	
	public void setMoveCallback(TouchPointersUtil.TouchPointersMoveCallback callback){
	    if(mTouchPointersUtil != null){
	        mTouchPointersUtil.setUtilMoveCallback(callback);
	    }
	}
	
    public void setZoomCallback(TouchPointersUtil.TouchPointersZoomCallback callback){
        if(mTouchPointersUtil != null){
            mTouchPointersUtil.setUtilZoomCallback(callback);
        }
    }
    
}
