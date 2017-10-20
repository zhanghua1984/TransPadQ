package cn.transpad.transpadui.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import org.apache.http.impl.cookie.DateUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.App;
import cn.transpad.transpadui.entity.Shortcut;
import cn.transpad.transpadui.http.Reporter;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.http.RstSerializer;
import cn.transpad.transpadui.main.TPBrowserActivity;
import cn.transpad.transpadui.main.TransPadApplication;
import cn.transpad.transpadui.service.NotificationFetcherService;
import cn.transpad.transpadui.service.TransPadService;
import cn.transpad.transpadui.storage.SharedPreferenceModule;

/**
 * 一般工具类
 *
 * @author kongxiaojun
 * @since 2014-5-4
 */
public class TPUtil {

    private static String TAG = "FoneUtil";
    private static Context sContext = null;

    public static void init(Context context) {
        sContext = context;
    }

    /**
     * 把秒钟转换成分钟"mm:ss"显示
     *
     * @return void
     * @throws
     */
    public static String second2MinuteStr(int second) {
        int min = second / 60;
        int sec = second % 60;
        String mins;
        String secs;
        if (min < 10) {
            mins = "0" + min;
        } else {
            mins = "" + min;
        }
        if (sec < 10) {
            secs = "0" + sec;
        } else {
            secs = "" + sec;
        }
        return mins + ":" + secs;
    }

    /**
     * 判断网络是否连接
     *
     * @param context
     * @return
     */
    public static boolean isNetOkWithToast() {

        if (sContext == null) {
            return false;
        }

        if (getCurrentNetType(sContext) == 0) {
            L.v(TAG, "isNetOkWithToast", "no_network_toast");
            Toast.makeText(sContext, R.string.no_network_toast,
                    Toast.LENGTH_LONG).show();
        }
        return getCurrentNetType(sContext) != 0;
    }

    /**
     * 判断网络是否连接
     *
     * @return
     */
    public static boolean isNetOk() {
        return getCurrentNetType(sContext) != 0;
    }

    /**
     * @param context
     * @return 1 : wifi 2:3g 3:gprs 0: no net
     */
    public static int getCurrentNetType(Context context) {
        if (null == context) {
            return 0;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);// 获取系统的连接服务
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();// 获取网络的连接情况
        if (null != activeNetInfo) {
            L.v(TAG, "getNetType : ", activeNetInfo.toString());
            if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                // 判断WIFI网
                return 1;
            } else if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                TelephonyManager mTelephonyManager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                int type = mTelephonyManager.getNetworkType();
                if (type == TelephonyManager.NETWORK_TYPE_UNKNOWN
                        || type == TelephonyManager.NETWORK_TYPE_GPRS
                        || type == TelephonyManager.NETWORK_TYPE_EDGE) {
                    // 判断gprs网
                    return 3;
                } else {
                    // 判断3g网
                    return 2;
                }
            }
        }
        return 0;
    }

    /**
     * 数据加密
     *
     * @param plainText
     * @return
     */
    public static String Md5(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }

            return buf.toString();// 32位的加密

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Base64String
     *
     * @param data
     * @return
     */
    public static String Base64Encode(String data) {
        String encodeData = "";
        try {
            encodeData = new String(Base64.encode(data.getBytes("UTF-8"),
                    Base64.URL_SAFE | Base64.NO_WRAP), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodeData;
    }

    /**
     * Base64String
     *
     * @param data
     * @return
     */
    public static String Base64Encode(byte[] data) {
        String encodeData = "";
        try {
            encodeData = new String(Base64.encode(data, Base64.URL_SAFE
                    | Base64.NO_WRAP), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodeData;
    }

    /**
     * Toast 提示
     *
     * @param text
     */
    public static void showToast(String text) {
        if (!TextUtils.isEmpty(text)) {
            Toast.makeText(sContext, text, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showToastLong(String text) {
        if (!TextUtils.isEmpty(text)) {
            Toast.makeText(sContext, text, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Toast 提示
     *
     * @param resId
     */
    public static void showToast(int resId) {
        Toast.makeText(sContext, resId, Toast.LENGTH_SHORT).show();
    }

    /**
     * 处理url，去掉前面的"[host]"和"/player/"，该方法是临时的，以后随着url的变化可能就不需要该方法了
     *
     * @param url
     * @return String
     * @throws
     */
    public static String handleUrl(String url) {
        url = url.trim();
        if (url.startsWith("[host]")) {
            url = url.replace("[host]", "");
        }
        if (url.startsWith("/player")) {
            url = url.replace("/player", "");
        }
        return url;
    }

    public static String onlineUrl(String url) {
        url = url.trim();
        if (url.startsWith("[host]/player/")) {
            url = url.replace("[host]/player/", "");
        }
        return url;
    }

    /**
     * 拼接 url 绝对路径
     *
     * @param host
     * @param shost
     * @param url
     * @return
     */
    public static String getAbsoluteUrl(String host, String shost, String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        } else if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        } else if (url.startsWith("[host]") && !TextUtils.isEmpty(host)) {
            return host + url.substring(url.indexOf("]") + 1, url.length());
        } else if (url.startsWith("[shost]") && !TextUtils.isEmpty(shost)) {
            return shost + url.substring(url.indexOf("]") + 1, url.length());
        } else {
            return url;
        }
    }

    public static String getAbsoluteUrl(String host, String shost, String url,
                                        String cipher) {
        if (TextUtils.isEmpty(url)) {
            return "";
        } else if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        } else if (url.startsWith("[host]") && !TextUtils.isEmpty(host)) {
            StringBuffer nurl = new StringBuffer();
            nurl.append(host).append(
                    url.substring(url.indexOf("]") + 1, url.length()));
            if (url.contains("?")) {
                nurl.append("&cipher=").append(cipher);
            } else {
                nurl.append("?cipher=").append(cipher);
            }
            return nurl.toString();
        } else if (url.startsWith("[shost]") && !TextUtils.isEmpty(shost)) {
            return shost + url.substring(url.indexOf("]") + 1, url.length());
        } else {
            return null;
        }
    }

    /**
     * String 类型 转换为 Integer 类型
     *
     * @param str 要转换的string
     * @return int
     * @throws
     */
    public static int String2Integer(String str) {
        int num = 0;
        try {
            num = Integer.valueOf(str);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return num;
    }

    /**
     * String 类型 转换为 Long 类型
     *
     * @param str 要转换的string
     * @return Long
     * @throws
     */
    public static Long String2Long(String str) {
        Long num = (long) 0;
        try {
            num = Long.valueOf(str);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return num;
    }

//	/**
//	 * 登陆成功后广播
//	 *
//	 * @param context
//	 * @param from
//	 * @param type
//	 */
//	public static void sendLoginSuccess(Context context, String from, int type,
//			boolean issend, String clid) {
//		Intent intent = new Intent();
//		intent.setAction(FoneConstant.BROADCAST_LOGIN_SUCCESS);
//		intent.putExtra("payfrom", from);
//		intent.putExtra("type", type);
//		intent.putExtra("clid", clid);
//		intent.putExtra("issend", issend);
//		context.sendOrderedBroadcast(intent, null);
//	}

//	/**
//	 * 计费跳转至登陆页
//	 *
//	 * @param activity
//	 * @param comefrom
//	 *            1 点击头像注册 2 点击立即充值 3 点击成为VIP 4 付费影片计费跳转/点击权利页按钮跳转到登陆 5点击vip频道
//	 *            加入vip按钮,7 其他
//	 * @param feeFrom
//	 *            计费使用
//	 * @param feeType
//	 *            计费使用
//	 * @param userlist
//	 *            计费使用
//	 * @param clid
//	 *            栏目id
//	 */
//	public static void feeToLoginPage(Context context, String comefrom,
//			String feeFrom, int feeType, ArrayList<String> userlist, String clid) {
//		Intent intent = new Intent(context, LoginActivity.class);
//		if (comefrom != null) {
//			intent.putExtra("loginfrom", comefrom);
//		}
//		intent.putExtra("payfrom", feeFrom);
//		intent.putExtra("clid", clid);
//		intent.putExtra("type", feeType);
//		if (userlist != null && userlist.size() > 0) {
//			intent.putExtra("userlist", userlist);
//		}
//		context.startActivity(intent);
//	}

    /**
     * 拼接地址(去掉 [host] [shost])
     *
     * @param url
     * @return String
     * @throws
     */
    public static String formatUrl(String url) {
        if (null == url) {
            L.e("FoneUtil", "nullPointer", "formatUrl------->url=null!");
            return null;
        }
        if (url.startsWith("[host]") || url.startsWith("[shost]")) {
            return url.substring(url.indexOf("]") + 1);
        } else {
            return url;
        }
    }

    /**
     * 格式化日期
     *
     * @param date   日期
     * @param patten 模版例如："yyyy-MM-dd"
     * @return String
     * @throws
     */
    @SuppressLint("SimpleDateFormat")
    public static String formatDate(Date date, String patten) {
        if (date == null || TextUtils.isEmpty(patten)) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(patten);
        return sdf.format(date);
    }

    /**
     * 解析日期
     *
     * @param source 源字符串
     * @param patten 模版例如："yyyy-mm-dd"
     * @return Date
     * @throws
     */
    @SuppressLint("SimpleDateFormat")
    public static Date parserDate(String source, String patten) {
        if (TextUtils.isEmpty(source) || TextUtils.isEmpty(patten)) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(patten);
        try {
            return sdf.parse(source);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加一个一次性闹钟
     *
     * @param context
     * @param type      闹钟类型 AlarmManager.ELAPSED_REALTIME、
     *                  AlarmManager.ELAPSED_REALTIME_WAKEUP、AlarmManager.RTC、
     *                  AlarmManager.RTC_WAKEUP、AlarmManager.POWER_OFF_WAKEUP
     * @param startTime 响铃时间 毫秒的时间戳
     * @param pi        PendingIntent
     * @return void
     * @throws
     */
    public static void addOnceAlarm(Context context, int type, long startTime,
                                    PendingIntent pi) {
        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        am.set(type, startTime, pi);
    }

    /**
     * 取消一个闹钟
     *
     * @param context
     * @param pi
     * @return void
     * @throws
     */
    public static void cancelAlarm(Context context, PendingIntent pi) {
        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    /**
     * 取cid
     *
     * @param url
     * @return String
     * @throws
     */
    public static String getCIdByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            L.v(TAG, "url是空的");
            return null;
        }
        if (url.contains("&cid=")) {
            int index = url.indexOf("&cid=");
            int endIndex = url.indexOf("&", index + 5);
            if (endIndex == -1) {
                return url.substring(index + 5);
            } else {
                return url.substring(index + 5, endIndex);
            }
        } else if (url.contains("cid=")) {
            int index = url.indexOf("cid=");
            int endIndex = url.indexOf("&", index + 4);
            if (endIndex == -1) {
                return url.substring(index + 4);
            } else {
                return url.substring(index + 4, endIndex);
            }
        } else {
            L.v(TAG, "url里没有cid");
            L.v(TAG, "url = " + url);
            return "0";
        }
    }

    /**
     * 取ccid
     *
     * @param url
     * @return String
     * @throws
     */
    public static String getCCIdByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            L.v(TAG, "url是空的");
            return null;
        }
        if (url.contains("ccid=")) {
            int index = url.indexOf("ccid=");
            int endIndex = url.indexOf("&", index + 5);
            if (endIndex == -1) {
                return url.substring(index + 5);
            } else {
                return url.substring(index + 5, endIndex);
            }
        } else {
            L.v(TAG, "url里没有ccid");
            return null;
        }
    }

    /**
     * 取clid
     *
     * @param url
     * @return String
     * @throws
     */
    public static String getCLIdByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            L.v(TAG, "url是空的");
            return null;
        }
        if (url.contains("clid=")) {
            int index = url.indexOf("clid=");
            int endIndex = url.indexOf("&", index + 5);
            if (endIndex == -1) {
                return url.substring(index + 5);
            } else {
                return url.substring(index + 5, endIndex);
            }
        } else {
            L.v(TAG, "url里没有clid");
            return null;
        }
    }

    public static String getUerydataByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            L.v(TAG, "url是空的");
            return null;
        }
        if (url.contains("querydata=")) {
            int index = url.indexOf("querydata=");
            int endIndex = url.indexOf("&", index + 10);
            if (endIndex == -1) {
                return url.substring(index + 10);
            } else {
                return url.substring(index + 10, endIndex);
            }
        } else {
            L.v(TAG, "url里没有querydata");
            return null;
        }
    }

    /**
     * 取videoid
     *
     * @param url
     * @return String
     * @throws
     */
    public static String getVideoIdByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            L.v(TAG, "url是空的");
            return "";
        }
        if (url.contains("videoid=")) {
            int index = url.indexOf("videoid=");
            int endIndex = url.indexOf("&", index + 8);
            if (endIndex == -1) {
                return url.substring(index + 8);
            } else {
                return url.substring(index + 8, endIndex);
            }
        } else {
            L.v(TAG, "url里没有videoid");
            return "";
        }
    }

    /**
     * 隐藏输入法
     *
     * @param editText
     * @param context
     */
    public static void hideInputMethod(EditText editText, Context context) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    /**
     * 隐藏输入法
     *
     * @param activity
     */
    public static void hideInputMethod(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(activity.getCurrentFocus()
                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 格式化时间 转为 TimeMillis
     *
     * @param currentTime
     * @param inFormat
     * @return
     */
    public static String FormatedTime2String(String currentTime, String inFormat) {

        if (TextUtils.isEmpty(currentTime) || TextUtils.isEmpty(inFormat)) {
            L.v(TAG, "FormatedTime2String", "currentTime : " + currentTime
                    + " inFormat : " + inFormat);
            return currentTime;
        }
        Date data = null;
        try {
            data = DateUtils.parseDate(currentTime, new String[]{inFormat});
        } catch (Exception e) {
            L.e(TAG, "FormatedTime2String", e.getMessage());
            return currentTime;
        }
        return String.valueOf(data.getTime());
    }

    /**
     * 格式化数字
     *
     * @param d       数字
     * @param pattern 模型 "0.00"
     * @return String
     * @throws
     */
    public static String formatNumber(double d, String pattern) {
        String s = "";
        try {
            if (pattern == null)
                pattern = "0.00";
            DecimalFormat df = new DecimalFormat(pattern);
            s = df.format(d);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getCurrentTime(long currentTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(currentTime);
        String currentDate = formatter.format(curDate);
        return currentDate;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTime() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(currentTime);
    }

    /**
     * 判断是否在前台
     *
     * @param context
     * @return
     */
    public static boolean isAppOnForeground(Context context) {
        // Returns a list of application processes that are running on the
        // device
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从后台唤醒应用
     *
     * @param clazz
     */
    public static void wakeUpfromBackground(Class<? extends Activity> clazz,
                                            Intent mIntent) {

        if (mIntent == null) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClass(TransPadApplication.getTransPadApplication(), clazz);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            TransPadApplication.getTransPadApplication().startActivity(intent);
        } else {
            mIntent.setClass(TransPadApplication.getTransPadApplication(), clazz);
            mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            TransPadApplication.getTransPadApplication().startActivity(mIntent);
        }

    }

    /**
     * 创建一个图片显示DisplayImageOptions
     *
     * @param id
     * @return DisplayImageOptions
     * @throws
     */
    public static DisplayImageOptions createDisplayImageOptionsByDrawableId(
            int id) {
        return new DisplayImageOptions.Builder().showImageOnLoading(id)
                .showImageForEmptyUri(id).showImageOnFail(id)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
    }

    /**
     * 根据包名判断此应用是否安装,如果安装了就返回应用信息，如果未安装返回空
     *
     * @param packageName
     * @return
     */
    public static PackageInfo checkApkExist(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName))
            return null;
        try {
            return context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    /**
     * 获取系统是否允许安装非市场应用
     *
     * @param context
     * @return
     */
    public static boolean canInstall(Context context) {
        int result = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
        if (result == 0) {
            return false;
        }
        return true;
    }

    /**
     * 卸载应用
     *
     * @param context
     * @param packageName
     */
    public static void uninstallApp(Context context, String packageName) {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        context.startActivity(uninstallIntent);
    }

    /**
     * 根据包名启动第三方应用
     *
     * @param context
     * @param packageName
     * @return void
     * @throws
     */
    public static void start3rdApp(Context context, String packageName) {
        if (checkApkExist(context, packageName) != null) {
            // 启动
            Intent intent = context.getPackageManager()
                    .getLaunchIntentForPackage(packageName);
            context.startActivity(intent);
        }
    }

    /**
     * 检测安装包是否完整
     *
     * @param context
     * @param filePath
     * @return
     */
    public static boolean checkApk(Context context, String filePath) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageArchiveInfo(filePath,
                    PackageManager.GET_ACTIVITIES);
            if (info != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 安装应用
     *
     * @param file
     */
    public static void installAPK(File file, Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (context instanceof Service) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 判断指定时间是否过时
     *
     * @param specifiedTime 指定时间(毫秒数)
     * @return 是否过时
     */
    public static boolean isBeforeDay(long specifiedTime) {
        boolean isBeforeDay = false;
        Date beforeDate = new Date(specifiedTime);
        Date todayDate = new Date();
        if (todayDate.before(beforeDate)) {
            isBeforeDay = true;
        } else {
            isBeforeDay = false;
        }
        return isBeforeDay;
    }

    /**
     * 获取已安装应用列表
     *
     * @param context
     * @return
     */
    public static List<ApplicationInfo> getInstalledAppList(Context context) {
        List<ApplicationInfo> applicationInfos = context.getPackageManager()
                .getInstalledApplications(
                        PackageManager.GET_UNINSTALLED_PACKAGES);
        return applicationInfos;
    }

    /**
     * 获取已安装应用列表
     *
     * @param context
     * @return
     */
    public static boolean isApkInstalled(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(packageName,
                            PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }


    /**
     * 获得所有启动Activity的信息
     *
     * @param context
     */
    public static List<App> getAppInfoList(Context context) {
        // 获得PackageManager对象
        PackageManager pm = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // 通过查询，获得所有ResolveInfo对象.
        List<ResolveInfo> resolveInfos = pm
                .queryIntentActivities(mainIntent, 0);
        // 调用系统排序,根据name排序
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pm));
        List<App> appInfos = new ArrayList<App>();
        for (ResolveInfo reInfo : resolveInfos) {
            String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
            if (pkgName.equals(context.getPackageName())) {
                continue;
            }
            String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
            // 创建一个AppInfo对象，并赋值
            App appInfo = new App();
            appInfo.setName(appLabel);
            appInfo.setPackageName(pkgName);
            appInfo.setIsInstall(true);
            appInfo.setActivityName(reInfo.activityInfo.name);
            appInfos.add(appInfo); // 添加至列表中
            L.v(TAG, "appInfo = " + appInfo);
        }
        return appInfos;
    }
    /**
     * 获得所有启动Activity的信息
     *
     * @param context
     */
    public static List<Shortcut> getAppInfoListLite(Context context) {
        // 获得PackageManager对象
        PackageManager pm = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // 通过查询，获得所有ResolveInfo对象.
        List<ResolveInfo> resolveInfos = pm
                .queryIntentActivities(mainIntent, 0);
        // 调用系统排序,根据name排序
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pm));
        List<Shortcut> shortcutInfos = new ArrayList<Shortcut>();
        for (ResolveInfo reInfo : resolveInfos) {
            String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
            if (pkgName.equals(context.getPackageName())) {
                continue;
            }
            String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
            // 创建一个AppInfo对象，并赋值
            Shortcut shortcutInfo = new Shortcut();
            shortcutInfo.setName(appLabel);
            shortcutInfo.setShortcutPath(pkgName);
            shortcutInfo.setIsInstall(true);
            shortcutInfo.setActivityName(reInfo.activityInfo.name);
            shortcutInfo.setShortcutType(Shortcut.APP_SHORTCUT_PAGE_TYPE);
            shortcutInfos.add(shortcutInfo); // 添加至列表中
            L.v(TAG, "appInfo = " + shortcutInfo);
        }
        return shortcutInfos;
    }
    /**
     * 根据包名启动应用
     *
     * @param context
     * @param packageName
     */
    public static void startAppByPackegName(Context context, String packageName) {
        if (context != null) {
            try {
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                Reporter.logInvokErp(getAppNameByPackageName(context, packageName), 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ApplicationUtil.saveAppRecentList(context, packageName, null);
        }
    }

    /**
     * 根据包名启动应用
     *
     * @param context
     * @param packageName
     */
    public static void startAppByActvityNamePackageName(Context context, String packageName, String activityName) {
        if (context != null) {
            try {
                Intent launchIntent = new Intent();
                launchIntent.setComponent(new ComponentName(packageName,
                        activityName));
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
                Reporter.logInvokErp(getAppNameByPackageName(context, packageName), 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ApplicationUtil.saveAppRecentList(context, packageName, activityName);
        }
    }


    /**
     * 根据包名启动应用
     *
     * @param context
     * @param packageName
     */
    public static void startAppByPackegNameNewTask(Context context, String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        Reporter.logInvokErp(getAppNameByPackageName(context, packageName), 1);
    }

    /**
     * 根据包名获取图标
     *
     * @param context
     * @param packageName
     * @return
     */
    public static Drawable getDrawableByPackageName(Context context, String packageName) {
        try {
            return context.getPackageManager().getApplicationIcon(packageName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据包名获取应用名称
     *
     * @param context
     * @param packageName
     * @return
     */
    public static String getAppNameByPackageName(Context context, String packageName) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return info.applicationInfo.loadLabel(context.getPackageManager()).toString();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将String类型的数字转换成长整型
     *
     * @param id 字符串
     * @return 长整型
     */
    public static long parseStringToLong(String id) {
        long longId = 0;
        if (id != null && !id.equals("")) {
            longId = Long.parseLong(id);
        }
        return longId;
    }

    /**
     * 将String类型的数字转换成整型
     *
     * @param id 字符串
     * @return 长整型
     */
    public static int parseStringToInt(String id) {
        int longId = 0;
        if (id != null && !id.equals("")) {
            longId = Integer.parseInt(id);
        }
        return longId;
    }

    public static String parseImageUrl(String shost, String imageUrl) {
        if (shost != null && imageUrl != null) {
            imageUrl = imageUrl.replace("[shost]", shost);
        } else {
            imageUrl = "";
        }
        return imageUrl;
    }

    public static String parseDownloadUrl(String host, String downloadUrl) {
        if (host != null && downloadUrl != null) {
            downloadUrl = downloadUrl.replace("[host]", host) + "&cipher=" + Request.getInstance().getCipher();
        } else {
            downloadUrl = "";
        }
        return downloadUrl;
    }

    /**
     * 打开浏览器
     *
     * @param context
     * @param url     要打开的链接
     */
    public static void openBrowser(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            url = TPBrowserActivity.DEFAULT_URL;
        }
        Intent intent = new Intent(context, TPBrowserActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TPBrowserActivity.URL, url);
        context.startActivity(intent);
    }

    /**
     * 打开浏览器
     *
     * @param context
     * @param url     要打开的链接
     */
    public static void openBrowser(Context context, String url, boolean landscape) {
        if (TextUtils.isEmpty(url)) {
            url = TPBrowserActivity.DEFAULT_URL;
        }
        Intent intent = new Intent(context, TPBrowserActivity.class);
        intent.putExtra(TPBrowserActivity.URL, url);
        intent.putExtra(TPBrowserActivity.LANDSCAPE, landscape);
        context.startActivity(intent);
    }

    public static String getUA() {
//        String ua = "TransPadUI/1.0 Mozilla/5.0 (Linux; Android %1$s; Nexus 7 Build/JRO03D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Safari/535.19";
//        return String.format(ua, Build.VERSION.RELEASE);
        return "Mozilla/5.0 (Linux; Android 4.3; Nexus 10 Build/JSS15Q) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.72 Safari/537.36";
    }

    public static void connectTransPad() {
        L.v(TAG, "connectTransPad", "connectTransPad");
        try {
            //投屏
            TransPadService.getInstance().smartScreenProjection();
        } catch (Exception e) {
            e.printStackTrace();
            L.e(TAG, "connectTransPad", e.getMessage(), e);
            TPUtil.showToast(R.string.open_wifidisplay_faild);
        }

    }

    /**
     * 是否是MIUI系统
     *
     * @return
     */
    public static boolean isMIUISystem() {
        boolean miui = false;
        try {
            String miuiName = SystemProperties.get("ro.miui.ui.version.name");
            if (!TextUtils.isEmpty(miuiName)) {
                miui = true;
            }
        } catch (Exception e) {
        }
        return miui;
    }

    /**
     * 是否是酷派手机系统
     *
     * @return
     */
    public static boolean isCoolUISystem() {
        boolean cool = false;
        try {
            String coolName = SystemProperties.get("ro.yulong.version.software");
            if (!TextUtils.isEmpty(coolName)) {
                cool = true;
            }
        } catch (Exception e) {
        }
        return cool;
    }

    /**
     * 文件拷贝
     *
     * @param sourceFile
     * @param targetFile
     * @throws IOException
     */
    public static void copyFile(File sourceFile, File targetFile)
            throws IOException {
        copyStream(new FileInputStream(sourceFile), new FileOutputStream(
                targetFile));
    }

    /**
     * 文件拷贝
     *
     * @param source
     * @param target
     * @throws IOException
     */
    public static void copyStream(InputStream source, OutputStream target)
            throws IOException {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // 新建文件输入流并对它进行缓冲
            inBuff = new BufferedInputStream(source);
            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(target);
            // 缓冲数组
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();
        } finally {
            // 关闭流
            if (inBuff != null)
                inBuff.close();
            if (outBuff != null)
                outBuff.close();
        }
    }

    /**
     * 将原始图片压缩到指定宽高内
     *
     * @param srcPath 原始图片位置
     * @param width   宽度
     * @param height  高度
     * @return
     */
    public static Bitmap compressImageFromFile(String srcPath, int width, int height) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;//只读边,不读内容
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        int be = 1;
        if (w > h && w > width) {
            be = newOpts.outWidth / width;
        } else if (w < h && h > height) {
            be = newOpts.outHeight / height;
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置采样率
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return bitmap;
    }

    /**
     * 打开辅助功能设置页，为通知助手功能开启条件
     *
     * @param context
     */
    public static void openAccessibility(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 通知助手功能是否打开
     *
     * @return true打开，false 关闭
     */
    public static boolean getNotifuactionAccessibilityState() {
        return SharedPreferenceModule.getInstance().getBoolean(NotificationFetcherService.NOTIFICATION_ACCESSIBILITY_SHAREREFECES, false);
    }

    /**
     * 读取已缓存的报文，首先读取缓存在SharedReference中的报文，如果没有再读在assets/data_server_xml目录下的报文数据，如果还没有就返回空
     *
     * @param T       需要读取的对象
     * @param context 上下文
     * @param name    文件名 例如：dongle_home_hotspot
     * @param <T>     返回对应的对象，如果没有返回null
     * @return
     */
    public static <T> T readCachedServerData(Class<T> T, Context context, String name) {
        try {
            String dataStr = SharedPreferenceModule.getInstance().getString(name);
            RstSerializer serializer = new RstSerializer();
            if (!TextUtils.isEmpty(dataStr)) {
                T t = serializer.fromString(T, dataStr);
                if (t != null) {
                    return t;
                }
            }
            InputStream inputStream = context.getAssets().open("data_server_xml" + File.separator + name);
            if (inputStream != null) {
                return serializer.fromInputStream(T, inputStream);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * 保存服务器报文，以便下次使用
     *
     * @param obj  报文对象
     * @param name 保存的名称
     */
    public static void saveServerData(Object obj, String name) {
        try {
            if (obj != null) {
                RstSerializer serializer = new RstSerializer();
                String data = serializer.toString(obj);
                if (!TextUtils.isEmpty(data)) {
                    SharedPreferenceModule.getInstance().setString(name, data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 设备是否有导航栏，虚拟按键
     *
     * @param activity
     * @return
     */
    public static boolean checkDeviceHasNavigationBar(Context activity) {

        //通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
        boolean hasMenuKey = ViewConfiguration.get(activity)
                .hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap
                .deviceHasKey(KeyEvent.KEYCODE_BACK);

        if (!hasMenuKey && !hasBackKey) {
            // 做任何你需要做的,这个设备有一个导航栏
            return true;
        }
        return false;
    }

    /**
     * 是否是MTK手机
     *
     * @return
     */
    public static boolean isMTKPhone() {
        if (Build.HARDWARE.toLowerCase().startsWith("mt")) {
            return true;
        }
        return false;
    }

    /**
     * 获取文件后缀名
     *
     * @param path
     * @return
     */
    public static String getExtension(String path) {
        if (!TextUtils.isEmpty(path)) {
            int lastPointIndex = path.lastIndexOf(".");
            if (lastPointIndex >= 0) {
                return path.substring(lastPointIndex + 1);
            }
        }
        return null;
    }

    private static long lastClickTime;

    /**
     * 防止连续点击
     *
     * @return true 连续点击
     */
    public static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 800) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    public static boolean isEmpty(String value) {
        if (value == null || value.equals("") || value.equals("null")) {
            return true;
        }
        return false;
    }
}
