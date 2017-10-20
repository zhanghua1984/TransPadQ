package cn.transpad.transpadui.http;

import android.os.Build;

import cn.transpad.transpadui.BuildConfig;

public class Configure {

    //	private static final String ENDPOINT = "http://192.168.1.53:8888/vi/player";
    private static final String ENDPOINT = BuildConfig.SERVER;

    private static String UA;

    private static final String AES_KEY = "23E5BBF9&9#02E5B";
    private static final String SALT = "xe^rp0y5q&(]";

    private static final String FEE_AES_KEY = "24E5BBF9&9#02E5B";
    private static final String FEE_SALT = "38297E^7&((1A4C";

    public static final String PHONE_NUMBER_FILE = "/vs_phone.txt";

    public static final String PREFS_NAME = "auto_update_dict";
    public static final String USER_ID_KEY = "user_id";

    public static String getEndPoint() {
        return ENDPOINT;
    }

    public static String getUa() {

        if (UA == null) {

            String manufacturerStr = Build.MANUFACTURER;

            UA = manufacturerStr + "_" + Build.MODEL;
            UA = UA.toLowerCase().replaceAll(" ", "_");
            UA = UA.replaceAll("/", "_");
            UA = UA + "_" + "android";

            return UA;

        } else {
            return UA;
        }
    }

    public static String getAesKey() {

        return AES_KEY;
    }

    public static String getFeeAesKey() {
        return FEE_AES_KEY;
    }

    public static String getSlat() {
        return SALT;
    }

    public static String getFeeSlat() {
        return FEE_SALT;
    }

}
