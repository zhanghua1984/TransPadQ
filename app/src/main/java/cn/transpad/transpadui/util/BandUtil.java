package cn.transpad.transpadui.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.reflect.Method;
import java.util.List;

import cn.transpad.transpadui.service.TransPadService;
import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.view.ConnectBeforeDialog;

public class BandUtil {
    /**
     * Auto settings in the driver. The driver could choose to operate on both
     * 2.4 GHz and 5 GHz or make a dynamic decision on selecting the band.
     */
    private static final int WIFI_FREQUENCY_BAND_AUTO = 0;

    /**
     * Operation on 5 GHz alone
     */
    private static final int WIFI_FREQUENCY_BAND_5GHZ = 1;

    /**
     * Operation on 2.4 GHz alone
     */
    private static final int WIFI_FREQUENCY_BAND_2GHZ = 2;
    private static Context mContext;
    private static ConnectBeforeDialog sConnectBeforeDialog = null;

    public static void init(Context context) {
        mContext = context;
    }

    /**
     * Get the operational frequency band.
     *
     * @return One of
     * {@link #WIFI_FREQUENCY_BAND_AUTO},
     * {@link #WIFI_FREQUENCY_BAND_5GHZ},
     * {@link #WIFI_FREQUENCY_BAND_2GHZ} or
     * {@code -1} on failure.
     */
    private static int getFrequencyBand() {
        WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = WifiManager.class.getMethod("getFrequencyBand");
            Object result = method.invoke(mWifiManager);
            if (result instanceof Integer) {
                return (Integer) result;
            }
        } catch (Exception e) {
        }
        return -1;
    }

    /**
     * Check if the chipset supports dual frequency band (2.4 GHz and 5 GHz)
     *
     * @return {@code true} if supported, {@code false} otherwise.
     */
    private static boolean isDualBandSupported() {
        WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = WifiManager.class.getMethod("isDualBandSupported");
            Object result = method.invoke(mWifiManager);
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
        } catch (Exception e) {
        }
        return false;
    }

    private static boolean is24GHz(int freq) {
        return freq > 2400 && freq < 2500;
    }

    public static boolean is5GHz(int freq) {
        return freq > 4900 && freq < 5900;
    }

    public static boolean is2GHz(int freq) {
        return freq == 2;
    }

    public static boolean is5GHzWifiP2p() {
        boolean isDualBand = isDualBandSupported();
        int mFrequencyBandSupport = getFrequencyBand();
        switch (mFrequencyBandSupport) {
            case WIFI_FREQUENCY_BAND_AUTO:
                WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                boolean has5GHz = false;
                List<android.net.wifi.ScanResult> scanResults = mWifiManager.getScanResults();
                for (android.net.wifi.ScanResult scanResult : scanResults) {
                    if (is5GHz(scanResult.frequency)) {
                        has5GHz = true;
                    }
                    if (wifiInfo != null && wifiInfo.getSSID().equals(scanResult.SSID)) {
                        if (is5GHz(scanResult.frequency)) {
                            return true;
                        }
                    }
                }
                if (isDualBand) {
                    return true;
                }
                break;
            case WIFI_FREQUENCY_BAND_5GHZ:
                return true;
            case WIFI_FREQUENCY_BAND_2GHZ:
                return false;
        }
        return false;
    }

    private static boolean is5GHzWifiNet() {
        WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (Build.VERSION.SDK_INT >= 21) {
            if (is5GHz(wifiInfo.getFrequency())) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public static boolean is24GHzWifiNet() {
        if (Build.VERSION.SDK_INT >= 21) {
            WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            if (is24GHz(wifiInfo.getFrequency())) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public static String getFrequency(int freq) {
        if (is2GHz(freq)) {
            return "5";
        } else {
            return "2.4";
        }
    }

    public static void showConnectBeforeDialog() {

        if (TransPadService.isConnected()) {

            TPUtil.connectTransPad();

        } else {
            boolean isShow = SharedPreferenceModule.getInstance().getBoolean("is_show_before_dialog", true);
            //判断是否可以显示&&支持双通道

            if (TPUtil.isNetOk() && isShow) {
                if (Build.VERSION.SDK_INT >= 21) {
                    if (is24GHzWifiNet()) {
                        isShow = true;
                    } else {
                        isShow = false;
                    }
                } else {
                    isShow = true;
                }
            } else {
                isShow = false;
            }

            if (isShow) {
                sConnectBeforeDialog = new ConnectBeforeDialog(mContext);
                if (!sConnectBeforeDialog.isShowing()) {
                    sConnectBeforeDialog.show();
                }
            } else {
                TPUtil.connectTransPad();
            }
        }

    }

    public static void showConnectAfterDialog() {
        boolean isShow = SharedPreferenceModule.getInstance().getBoolean("is_show_after_dialog", true);
        //判断是否可以显示&&不是5G网络&&双通道
        if (isShow && !is5GHzWifiNet() && isDualBandSupported()) {
            if (sConnectBeforeDialog == null) {
                sConnectBeforeDialog = new ConnectBeforeDialog(mContext);
            }
            if (!sConnectBeforeDialog.isShowing()) {
                L.v("BandUtil", "showConnectAfterDialog", "sConnectAfterDialog show");
                sConnectBeforeDialog.show();
            }
        } else {
            L.v("BandUtil", "showConnectAfterDialog", "isShow=" + isShow + " is5GHzWifiNet=" + is5GHzWifiNet() + " isDualBandSupported=" + isDualBandSupported());
        }
    }

    private static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
}
