package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.util.L;

/**
 * Created by ctccuser on 2015/4/6.
 */
public class LightDialog extends Dialog {
    private static final String TAG = LightDialog.class.getSimpleName();
    int layout;
    Context context;
    int brightness;

    public LightDialog(Context context) {
        super(context);
    }

    public LightDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
//        this.layout= layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lightadjust_layout);
        SeekBar lightSeekBar = (SeekBar) findViewById(R.id.lightSeekBar);
//        滑动杆调节手机亮度
//        lightSeekBar.setProgress(getBrightness());
//        滑动杆调节Pad亮度
//        获取当前亮度设置到seekBar上
//        if (TransPadService.getConnectDeviceBrightness() == -1) {
//            lightSeekBar.setProgress(0);
//        } else {
//            lightSeekBar.setProgress(256 * TransPadService.getConnectDeviceBrightness() / TransPadService.getConnectDeviceMaxBrightness());
//        }
//        lightSeekBar.setOnSeekBarChangeListener(new LightChangeListener());
//        L.v(TAG, "onCreate", "当前亮度" + TransPadService.getConnectDeviceBrightness() +
//                "最大亮度" + TransPadService.getConnectDeviceMaxBrightness());

    }

    @Override
    protected void onStop() {
        super.onStop();
        L.v(TAG, "onStop");
    }

    public int getBrightness() {
        try {
            brightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return brightness;
    }

    public void setBrightness(int values) {
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();

        layoutParams.screenBrightness = values / 255f;
        window.setAttributes(layoutParams);

    }

    /**
     * 获得当前屏幕亮度的模式
     * SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 为自动调节屏幕亮度
     * SCREEN_BRIGHTNESS_MODE_MANUAL=0  为手动调节屏幕亮度
     */
    private int getScreenMode() {
        int screenMode = 0;
        try {
            screenMode = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return screenMode;
    }


    /**
     * 设置当前屏幕亮度的模式
     * SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 为自动调节屏幕亮度
     * SCREEN_BRIGHTNESS_MODE_MANUAL=0  为手动调节屏幕亮度
     */
    private void setScreenMode(int paramInt) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, paramInt);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    class LightChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//            进度改变时触发
//            调节亮度
//            setBrightness(i);
//            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, i);
            L.v(TAG, "brightness:" + brightness + "i:" + i);
//            int j = -1;
//            if (TransPadService.getConnectDeviceMaxBrightness() != -1) {
//                j = (int) Math.round(i / 256.0 * TransPadService.getConnectDeviceMaxBrightness());
//            }
//            L.v(TAG, "onProgressChanged", "int j=" + j);
//            if (j != -1) {
//                Message message = new Message();
//                message.what = TransPadService.SET_DEVICE_BRIGHTNESS;
//                message.arg1 = j;
//                EventBus.getDefault().post(message);
//            }
        }


        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
// 开始拖动时触发，与onProgressChanged区别在于onStartTrackingTouch在停止拖动前只触发一次
            //而onProgressChanged只要在拖动，就会重复触发
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //结束拖动时触发
        }
    }
}
