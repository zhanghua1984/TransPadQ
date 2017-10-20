package cn.transpad.transpadui.player.gesture;

import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.view.MotionEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import cn.transpad.transpadui.main.TransPadApplication;

public class FoneOnGesture implements GestureOverlayView.OnGestureListener{
//    Context mContext;
    // gesture
    private float old_x, old_y, new_x, new_y, move_x, move_y;
    private boolean is_gesture_flag, is_left, is_top, is_right, is_bottom;
    private GestureLibrary gestureLib;
    private Gesture gesture;
    
    private FoneOnGestureListener mListener;
    
    public void setFoneOnGestureListener(FoneOnGestureListener listener){
        this.mListener = listener;
    }
    
    public FoneOnGesture(){
        if(mListener != null){
            mListener.FoneOnGesture();
        }
    }
    
    public void FoneOnGestureStart(){
    	loadGestureLib();
        if(mListener != null){
            mListener.FoneOnGestureStart();
        } 
    }
    
    public void FoneOnGestureEnd(boolean isEnableSeek, boolean isLeftOrRight){
        if(mListener != null){
            mListener.FoneOnGestureEnd(isEnableSeek, isLeftOrRight);
        }
    }
    
    public void FoneOnGestureMovePrevious(){
        if(mListener != null){
            mListener.FoneOnGestureMovePrevious();
        }
    }
    
    public void FoneOnGestureMoveNext(){
        if(mListener != null){
            mListener.FoneOnGestureMoveNext();
        }
    }
    
    public void FoneOnGestureMoveUPOrDown(boolean isEnableSeek, float distance,boolean two_pointer){
        if(mListener != null){
            mListener.FoneOnGestureMoveUPOrDown(isEnableSeek, distance,start_x,two_pointer);
        }
    }

    public void FoneOnGestureMoveLeftOrRight(boolean isEnableSeek, float eventY,float distance){
        if(mListener != null){
            mListener.FoneOnGestureMoveLeftOrRight(isEnableSeek,eventY, distance);
        }
    }
    
    public static interface FoneOnGestureListener{
        public void FoneOnGesture();
        public void FoneOnGestureStart();
        public void FoneOnGestureEnd(boolean isEnableSeek, boolean isLeftOrRight);
        public void FoneOnGestureMovePrevious();
        public void FoneOnGestureMoveNext();
        public void FoneOnGestureMoveUPOrDown(boolean isEnableSeek, float distance, float start_x, boolean two_pointer);
        public void FoneOnGestureMoveLeftOrRight(boolean isEnableSeek, float eventY, float distance);
        
    }
    
    public void loadGestureLib(){
        gestureLib = GestureLibraries.fromFile(getGeturePath());
        gestureLib.load();
    }
    
    @Override
    public void onGesture(GestureOverlayView overlay, MotionEvent event) {
        if (!is_top
                && !is_bottom
                && Math.abs(new_x - event.getX()) > 20
                && Math.abs(new_x - event.getX()) > Math.abs(new_y - event.getY())) {
            if (new_x > event.getX()) {
//                L.v("fullplayer","FoneOnGestureMoveLeftOrRight is is_left:",""+is_left);
                if (!is_left) {
                    is_gesture_flag = true;
                    is_left = true;
                    is_right = false;
                }
            } else {
//                L.v("fullplayer","FoneOnGestureMoveLeftOrRight is is_right:",""+is_right);
                if (!is_right) {
                    is_gesture_flag = true;
                    is_left = false;
                    is_right = true;
                }
            }
            
            if (is_gesture_flag) {
                is_gesture_flag = false;
                move_x = new_x - event.getX();
            } else {
                move_x = move_x + new_x - event.getX();
            }
//            L.v("fullplayer","FoneOnGestureMoveLeftOrRight is gesture:",""+is_gesture_flag);
            FoneOnGestureMoveLeftOrRight(true,event.getY(), move_x);
            new_x = event.getX();
            new_y = event.getY();
        } else if (!is_left
                && !is_right
                && Math.abs(new_y - event.getY()) > 20
                && Math.abs(new_x - event.getX()) < Math.abs(new_y - event.getY())) {
            if (new_y < event.getY()) {
                if (!is_top) {
                    is_gesture_flag = true;
                    is_top = true;
                    is_bottom = false;
                }
            } else {
                if (!is_bottom) {
                    is_gesture_flag = true;
                    is_top = false;
                    is_bottom = true;
                }
            }

            if (is_gesture_flag) {
                is_gesture_flag = false;
                
                move_y = event.getY() - new_y;
            } else {
                move_y = move_y + event.getY() - new_y;
            }
            FoneOnGestureMoveUPOrDown(true, move_y,event.getPointerCount()==2);
            new_x = event.getX();
            new_y = event.getY();
        }
    }

    @Override
    public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
        if (Math.abs(old_x - event.getX()) < 20 && Math.abs(old_y - event.getY()) < 20) {
            is_left = false;
            is_top = false;
            is_right = false;
            is_bottom = false;
            is_gesture_flag = false;
            FoneOnGestureEnd(false, false);
            return;
        } 

        gesture = overlay.getGesture();
        String gesture_name = findGesture(gesture);
        if (gesture_name.equals("previous")) {
            FoneOnGestureMovePrevious();
        } else if (gesture_name.equals("next")) {
            FoneOnGestureMoveNext();
        }
        
        if(is_left || is_right){
            FoneOnGestureEnd(true, true);
        }else{
            FoneOnGestureEnd(true, false);
        }

        is_left = false;
        is_top = false;
        is_right = false;
        is_bottom = false;
        is_gesture_flag = false;
        return;
    }

    float start_x;
    @Override
    public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
        start_x = new_x = old_x = event.getX();
        new_y = old_y = event.getY();
        is_gesture_flag = false;
        FoneOnGestureStart();
//        L.v("fullplayer","onGestureStarted is gesture:",""+is_gesture_flag);
    }
    
    @Override
    public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
        
    }
    
    private String findGesture(Gesture gesture) {
        List<Prediction> predictions = gestureLib.recognize(gesture);
        if (!predictions.isEmpty()) {
            Prediction prediction = predictions.get(0);
            if (prediction.score >= 1) {
                return prediction.name;
            }
        }
        return "";
    }
    
    public File getGeturePath(){
        String path = TransPadApplication.getTransPadApplication().getFilesDir() + File.separator
        + (TransPadApplication.getTransPadApplication().getPackageName()) + File.separator
        + "gesture";
        InputStream is = null;
        FileOutputStream os = null;
        File filepath = new File(path);
        File file = new File(path + File.separator + "gestures");
        
        try {
            if (!filepath.isDirectory() && filepath.mkdirs())
                ;
            if (!file.isFile()) {
                if (file.createNewFile()) {
                    if (file.canWrite()) {
                        is = TransPadApplication.getTransPadApplication().getAssets().open("gestures");
                        byte[] b = new byte[is.available()];
                        os = new FileOutputStream(file);
                        while (is.read(b) != -1) {
                            os.write(b);
                        }
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
            }
        }
        return file;
    }
}
