package cn.transpad.transpadui.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Message;

import cn.trans.core.api.TransManager;
import cn.trans.core.entity.Task;
import cn.trans.core.entity.TransCoreMessage;
import cn.trans.core.protocol.ITransDevice;
import cn.transpad.dlna.entity.DLNADevice;
import cn.transpad.transpadui.http.Reporter;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.main.HomeActivity;
import cn.transpad.transpadui.main.TransPadApplication;
import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.util.L;
import de.greenrobot.event.EventBus;


/**
 * 连接TransPad的service
 * Created by Kongxiaojun on 2015/2/13.
 */
public class TransPadService {

    private static final String TAG = "TransPadService";
    private static final TransPadService sTransPadService = new TransPadService();

    public static TransPadService getInstance() {
        return sTransPadService;
    }

    public static final String LANDSCAPE_MODE_NAME = "landscape_mode_name";
    /**
     * 切换横屏状态
     */
    public static final int TRANSPAD_CHANG_LANDSCAPE = 901;

    /**
     * 设置亮度
     */
    public static final int SET_DEVICE_BRIGHTNESS = 907;

    /**
     * 设置音量
     */
    public static final int SET_DEVICE_VOLUME = 908;

    /**
     * 设备连接上
     */
    public static final int TRANSPAD_STATE_CONNECTED = 1004;
    /**
     * 设备断开
     */
    public static final int TRANSPAD_STATE_DISCONNECTED = 1001;
    /**
     * 固件升级
     */
    public static final int EVENT_MSG_WHAT_UPGRADE_HARD_WARE = 9000000;
    /**
     * 设备音量改变
     */
    public static final int TRANSPAD_DEVICE_VOLUME_CHANGED = 90000001;
    public static final int UPDATE_STATUS_ALREADY_LATEST_VERSION = 1;
    public static final int UPDATE_STATUS_HAS_NEW_VERSION = 2;
    public static final int UPDATE_STATUS_NO_NETWORK = 3;
    public static final int UPDATE_STATUS_CONNECT_ERROR = 4;
    private static boolean connect;
    private static TransManager mTranspadManager;
    /**
     * 是否在主页
     */
    private static ITransDevice activeDevice;
    private static Context sContext;
    private String version = "";
    private static String mDeviceId = "";
    private static DLNADevice mDLNADevice;
    private Intent homeIntent;

    public void init() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mTranspadManager = TransPadApplication.getTransPadApplication().getTranspadManager();
    }

    public void onCreate(Context context) {
        L.v(TAG, "onCreate", "start");
        sContext = context;
        mTranspadManager.setNotificationHomeIntent(getHomeIntent());
        mTranspadManager.setSideBarHomeIntent(getHomeIntent());
        mTranspadManager.setSideBarMyAppIntent(getAppIntent());
        mTranspadManager.setSideBarVisibility(TransManager.SETTING_FLOATWIN_NONE);
        mTranspadManager.setTaskBarVisibility(TransManager.SETTING_TASKBAR_NONE);
        mTranspadManager.setTransOrientation(TransManager.SETTING_TRANSROT_FREE);
        mTranspadManager.setTaskBarMaxCount(10);
        L.v(TAG, "onCreate", "registerDeviceEventListener");
        mTranspadManager.registerDeviceEventListener();
        mTranspadManager.startService();
    }

    public void onResume() {
        L.v(TAG, "onResume", "start");
        activeDevice = mTranspadManager.getActiveDevice();
        if (activeDevice != null && activeDevice.isControllable()) {
            mTranspadManager.setSideBarVisibility(TransManager.SETTING_FLOATWIN_PROJECTION_SHOW);
            mTranspadManager.setTaskBarVisibility(TransManager.SETTING_TASKBAR_PROJECTION_SHOW);
        }

        connect = mTranspadManager.isMiracastConnected();
        L.v(TAG, "onResume", "connect=" + connect);
        if (connect) {
            mTranspadManager.onResume();
            Message message = new Message();
            message.what = TRANSPAD_STATE_CONNECTED;
            EventBus.getDefault().post(message);
        }

    }

    public void onPause() {
        L.v(TAG, "onPause", "start");
    }

    public void smartScreenProjection() {
        if (mTranspadManager.isMiracastConnected()) {
            //显示退出dialog
            mTranspadManager.requestDisconnect();
        } else {
            mTranspadManager.requestConnect();
        }

    }

    public void onEventMainThread(Message message) {
        switch (message.what) {
            case TRANSPAD_CHANG_LANDSCAPE://切换横竖屏状态
                L.v(TAG, "onEventMainThread TRANSPAD_CHANG_LANDSCAPE");
                if (message.arg1 == 0) {
                    //打开横屏
                    L.v(TAG, "onEventMainThread TRANSPAD_CHANG_LANDSCAPE message.arg1 == 0");
                    mTranspadManager.setTransOrientation(TransManager.SETTING_TRANSROT_LANDSCAPE);
                } else {
                    //关闭横屏
                    L.v(TAG, "onEventMainThread TRANSPAD_CHANG_LANDSCAPE message.arg1 == 1");
                    mTranspadManager.setTransOrientation(TransManager.SETTING_TRANSROT_FREE);
                }
                break;
            case SET_DEVICE_BRIGHTNESS://设置亮度
                L.v(TAG, "onEventMainThread", "message" + message.arg1);
                if (mTranspadManager != null) {
                    mTranspadManager.setDeviceBrightness(message.arg1);
                }
                break;
            case SET_DEVICE_VOLUME://TODO 设置音量 没找到这方面接口
                break;
        }
    }

    public void onEventMainThread(TransCoreMessage transCoreMessage) {
        Message message = new Message();
        switch (transCoreMessage.what) {
            case TransCoreMessage.DEVICE_CONNECTED:
                connect = true;
                activeDevice = mTranspadManager.getActiveDevice();
                if (activeDevice == null) {
                    L.v(TAG, "onEventMainThread", "DEVICE_CONNECTED activeDevice=null");
                } else {
                    L.v(TAG, "onEventMainThread", "DEVICE_CONNECTED id=" + activeDevice.getId() + " name=" + activeDevice.getName() + " version=" + activeDevice.getVersion() + " freq=" + getFreq());
                    version = activeDevice.getVersion();
                    //将固件版本号更新,更新cipher
                    if (Request.getInstance() != null) {
                        Request.getInstance().initCipher();
                    }
                    Reporter.logTransPadConnected(getConnectDeviceId(), version);
                    L.v(TAG, "onEventMainThread", "DEVICE_CONNECTED frequency=" + activeDevice.getFreq());

                    switch (transCoreMessage.arg1) {
                        case TransCoreMessage.TRANS_LINK_DLNA_TYPE:
                            //dlna连接成功
                            L.v(TAG, "onEventMainThread", "DEVICE_CONNECTED TRANS_LINK_DLNA_TYPE");
                            break;
                        case TransCoreMessage.TRANS_LINK_DISPLAY_TYPE:
                            L.v(TAG, "onEventMainThread", "DEVICE_CONNECTED TRANS_LINK_DISPLAY_TYPE");
                            Intent intent = getHomeIntent();
                            L.v(TAG, "onEventMainThread", "DEVICE_CONNECTED isActiveConnected=false isControllable=false");
                            mTranspadManager.setTransOrientation(TransManager.SETTING_TRANSROT_LANDSCAPE);
                            sContext.startActivity(intent);
                            break;
                    }

                }
                message.what = TRANSPAD_STATE_CONNECTED;
                EventBus.getDefault().post(message);
                break;
            case TransCoreMessage.DEVICE_DIS_CONNECTED://设置亮度
                L.v(TAG, "onEventMainThread", "DEVICE_DIS_CONNECTED start");
                //mTranspadManager.setTransOrientation(TransManager.SETTING_TRANSROT_FREE);
                //将固件版本号更新,更新cipher
                Request.getInstance().initCipher();
                //设备断开
                connect = false;
                Reporter.logTransPadDisConnected(mDeviceId, version);
                activeDevice = null;
                message.what = TransPadService.TRANSPAD_STATE_DISCONNECTED;
                EventBus.getDefault().post(message);

                switch (transCoreMessage.arg1) {
                    case TransCoreMessage.TRANS_LINK_DLNA_TYPE:
                        L.v(TAG, "onEventMainThread", "DEVICE_DIS_CONNECTED TRANS_LINK_DLNA_TYPE");
                        break;
                    case TransCoreMessage.TRANS_LINK_DISPLAY_TYPE:
                        L.v(TAG, "onEventMainThread", "DEVICE_DIS_CONNECTED TRANS_LINK_DISPLAY_TYPE");
                        Intent intent = new Intent(sContext, HomeActivity.class);
                        sContext.startActivity(intent);
                        break;
                }
                break;
            case SET_DEVICE_VOLUME://TODO 设置音量 没找到这方面接口
                break;
        }
    }

    public static boolean isConnected() {
        if (mTranspadManager != null) {
            connect = mTranspadManager.isMiracastConnected();
            return connect;
        }
        return false;
    }

    public static boolean isTransConnected() {
        if (mTranspadManager != null) {
            return mTranspadManager.isTransConnected();
        }
        return false;
    }

    public void setDLNADevice(DLNADevice device) {
        mDLNADevice = device;
        if (device != null) {
            SharedPreferenceModule.getInstance().setString("lastdlnadevicename", device.server_name);
        }
    }

    public DLNADevice getDLNADevice() {
        return mDLNADevice;
    }

    public String getLastDlnaDeviceName() {
        return SharedPreferenceModule.getInstance().getString("lastdlnadevicename",null);
    }

    public void activeDevice(boolean isActive) {
        L.v(TAG, "activeDevice", "isActive=" + isActive);
        mTranspadManager.activeDevice(isActive);
    }

    public void switchMode(int model) {
        L.v(TAG, "switchMode", "model=" + model);
        mTranspadManager.switchMode(model);
    }

    public void connectWifi(String deviceAddress, String deviceName) {
        L.v(TAG, "activeDevice", "deviceAddress=" + deviceAddress + " deviceName=" + deviceName);
        mTranspadManager.connectWifi(deviceAddress, deviceName);
    }

    public void disConnectP2p() {
        L.v(TAG, "disConnectP2p");
        mTranspadManager.disconnectP2p();
    }

    public void connectRCtrl() {
        L.v(TAG, "connectRCtrl", "start");
        mTranspadManager.connectRCtrl();
    }

    public static void requestDisconnect() {
        if (mTranspadManager != null) {
            mTranspadManager.requestDisconnect();
        }
    }

    /**
     * 获取连接设备当前亮度
     *
     * @return
     */
    public static int getConnectDeviceBrightness() {
        if (activeDevice != null) {
            return activeDevice.getBrightness();
        }
        return -1;
    }

    /**
     * 获取连接设备最大亮度
     *
     * @return
     */
    public static int getConnectDeviceMaxBrightness() {
        if (activeDevice != null) {
            return activeDevice.getMaxBrightness();
        }
        return -1;
    }

    /**
     * 获取连接设备频率
     *
     * @return
     */
    public int getFreq() {
        if (activeDevice != null) {
            return activeDevice.getFreq();
        }
        return -1;
    }

    public void onDestroy() {
        L.v(TAG, "onDestroy");
        EventBus.getDefault().unregister(this);
        mTranspadManager.onDestroy();
    }

    public Intent getHomeIntent() {
        if (homeIntent == null) {
            Intent intent = new Intent(sContext, HomeActivity.class);
            intent.putExtra(HomeActivity.OPEN_ACTION, HomeActivity.OPEN_HOME_INTENT_NAME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        }else {
            return homeIntent;
        }
    }

    public void setHomeIntent(Intent intent) {
        homeIntent = intent;
    }

    public Intent getAppIntent() {
        Intent intent = new Intent(sContext, HomeActivity.class);
        intent.putExtra(HomeActivity.OPEN_ACTION, HomeActivity.OPEN_MY_APP_INTENT_NAME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static void setLandScreen(boolean isLandScreen) {
        L.v(TAG, "setLandScreen", "isLandScreen=" + isLandScreen);
        if (isLandScreen) {
            mTranspadManager.setTransOrientation(TransManager.SETTING_TRANSROT_LANDSCAPE);
        } else {
            mTranspadManager.setTransOrientation(TransManager.SETTING_TRANSROT_FREE);
        }
    }

    public void setMouseSpeed(float speed) {
        L.v(TAG, "setLandScreen", "speed=" + speed);
        mTranspadManager.setMouseSpeedSetting(speed);
    }

    public float getMouseSpeed() {
        float speed = mTranspadManager.getMouseSpeedSetting();
        L.v(TAG, "setLandScreen", "speed=" + speed);
        return speed;
    }

    /**
     * 获取连接设备名称
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static String getConnectDeviceName() {
        String deviceName = null;
        if (connect && activeDevice == null) {
            L.v(TAG, "getConnectDeviceName", "start activeDevice=null");
            activeDevice = mTranspadManager.getActiveDevice();
            L.v(TAG, "getConnectDeviceName", "end activeDevice=" + activeDevice);
        }

        if (activeDevice != null) {
            if (activeDevice.getName() == null || activeDevice.getName().equals("")) {
                deviceName = activeDevice.getModel();
                L.v(TAG, "getConnectDeviceName", "name=null model=" + deviceName);
            } else {
                deviceName = activeDevice.getName();
                L.v(TAG, "getConnectDeviceName", "name=" + deviceName);
            }
        } else {
            L.v(TAG, "getConnectDeviceName", "activeDevice=null");
        }
        return deviceName;
    }

    /**
     * 获取连接设备ID
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static String getConnectDeviceId() {
        if (activeDevice == null) {
            activeDevice = mTranspadManager.getActiveDevice();
        }

        if (connect && activeDevice != null) {
            mDeviceId = activeDevice.getId();
            return mDeviceId;
        }
        return "";
    }

    /**
     * 获取固件版本号
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static String getDeviceFirmVersion() {
        L.v(TAG, "getDeviceFirmVersion", "start");
        if (activeDevice == null && mTranspadManager != null) {
            activeDevice = mTranspadManager.getActiveDevice();
            L.v(TAG, "getDeviceFirmVersion", "get activeDevice");
        }
        L.v(TAG, "getDeviceFirmVersion", "connect=" + connect + " activeDevice=" + activeDevice);
        if (connect && activeDevice != null) {
            L.v(TAG, "getDeviceFirmVersion", "version=" + activeDevice.getVersion());
            return activeDevice.getVersion();
        }
        return "";
    }

    public void addTask(Task task) {
        L.v(TAG, "addTask", "taskPackageName=" + task.taskPackageName + " taskActivityName=" + task.taskActivityName);
        if (mTranspadManager != null) {
            mTranspadManager.addTask(task);
        }
    }
}
