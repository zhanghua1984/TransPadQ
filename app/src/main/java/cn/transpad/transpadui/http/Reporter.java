package cn.transpad.transpadui.http;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.transpad.transpadui.entity.InvokErp;
import cn.transpad.transpadui.main.TransPadApplication;
import cn.transpad.transpadui.service.TransPadService;
import cn.transpad.transpadui.util.Base64;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.StringUtil;
import cn.transpad.transpadui.util.TPUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class Reporter {
    private static final String TAG = "Reporter";
    private static final boolean DEBUG = false;
    static private StringBuffer replocalInf = new StringBuffer();
    //static private StringBuffer replocaInfTmp  = new StringBuffer();
    //static private StringBuffer playrepInf = new StringBuffer();
    //static private StringBuffer playrepInfTmp = new StringBuffer();

    static private Class_d h5_evnt;

    static private Class_play player_event;

    static private Class_link pad_link;


//    public static class ErrorType {
//
//        public static final int CACHE_ERROR = 1;         /* 缓存失败 */
//        public static final int CACHE_PLAY_ERROR = 2;    /* 缓存播放失败 */
//        public static final int ONLLINE_PLAY_ERROR = 3;  /* 在线播放失败 */
//        public static final int SYNC_COLLECT_ERROR = 4;  /* 收藏至云端失败 */
//        public static final int LOGIN_ERROR = 5;  		 /* 登陆空间失败 */
//        public static final int LAUNCH_PLAYER_ERROR = 6; /* 打开播放器失败 */
//        public static final int VGDETAIL_ERROR = 7; /* 详情页接口请求失败 */
//        public static final int XYZPLAY_ERROR = 8; /* 播放接口请求失败 */
//
//    }

    public static class EventId {

        /*通知上报*/
        public static final int NOTIC_INNER_CLICK = 0x9000000b; /*内通知点击次数*/
        public static final int NOTIC_OUTER_CLICK = 0x9000000d; /*外通知点击次数*/


        @Deprecated
        public static final int UPGRADE_RECEIVE = 0x90000003; /*收到升级提示框的次数，与罗周确认过，这个已经废除*/
        /*升级上报 */
        public static final int UPGRADE_CLICK_OK = 0x90000005; /*点击“确定”升级次数*/
        public static final int UPGRADE_CLICK_CANCEL = 0x90000007; /*点击“取消”升级次数*/
        public static final int UPGRADE_DOWNLOAD = 0x90000009; /*成功下载升级包的次数*/

        public static final int UPGRADE_POPUP = 0x90000017; /* 收到弹窗用户数*/
        public static final int UPGRADE_SELF_CHECK = 0x90000019; /* 自检升级成功*/
        public static final int UPGRADE_SMART = 0x9000001b; /* 智能升级*/

		/*用户按钮点击次数上报*/

        public static final int CLICK_TOTAL_BTN = 0xa0000011; /*‘全部’按钮*/
        public static final int QUICK_PLAY_ENTRY = 0xa0000013; /*快速播放入口*/
        public static final int CLICK_MY_VEDIO_BTN = 0xa0000015; /*我的视频入口次数*/
        public static final int CLICK_NOTIC_BTN = 0xa0000017; /*“通知”入口*/
        public static final int CLICK_MSG_BTN = 0xa0000019; /*“消息”入口*/
        public static final int CLICK_RECHARGE_BTN = 0xa000001B; /*“马上充值”按钮*/
        public static final int CLICK_VIP_BTN = 0xa000001D; /*来自“我的”点击“成为 VIP”*/
        public static final int CLICK_VIP_CHANNEL_BTN = 0xa000001F; /*来自 VIP 频道的“成为 VIP”*/
        public static final int CLICK_MY_COLLECT_BTN = 0xa0000021; /*“我的收藏”入口*/
        public static final int CLICK_MY_CACHE_BTN = 0xa0000023; /*“我的缓存”入口*/
        public static final int CLICK_LOCAL_BTN = 0xa0000025; /*本地入口*/
        public static final int CLICK_SEARCH_BTN = 0xa0000027; /*搜索入口*/
        public static final int CLICK_DETAIL_SHARE_BTN = 0xa0000029; /*详情页的分享按钮*/
        public static final int CLICK_DETAIL_BTN = 0xa000002B; /*详情页的评论按钮*/
        public static final int CLICK_DETAIL_BOOK_BTN = 0xa000002D; /*详情页的预约按钮*/
        public static final int CLICK_H5_PLAY_BTN = 0xa000002F; /*页面的 100TV 播放按钮*/
        public static final int CLICK_CLASS_BTN = 0xa0000031; /*分类检索按钮*/
        public static final int POPUP_VIP = 0xa0000033; /*是否成为VIP弹框次数*/
        public static final int DNLD_MORE_BTN = 0xa0000035; /*确认按钮点击次数*/
        public static final int POPUP_VIP_CANCEL = 0xa0000037; /*取消按钮点击次数*/
        public static final int CLICK_VIDEO_DETAIL_COLLECT_BTN = 0xa0000039; /*详情页收藏按钮点击次数*/
        public static final int CLICK_QUICK_PLAY_BTN = 0xa000003A; /*快速播放按钮点击次数*/
        /*下载失败上报*/
        public static final int OFFLINE_CACHE_FAIL = 0xb0000001; /* 离线缓存下载失败*/

        /*编辑频道上报*/
        public static final int EDIT_CHANNEL = 0xb0000000;

        /*操作失败上报接口*/
        public static final int OPERATION_FAIL = 0xc0000000;

        /*H5页面播放日志上报*/
        public static final int H5_PLAY = 0xd0000000;

        /*访问分享日志上报*/
        public static final int SHARE_LOG = 0xe0000000;

        /*点击权利页面“加入vip”上报数据*/
        public static final int JOIN_VIP = 0xf0000000;
    }

    static private class Class_d {
        public String entry_time; /*停留开始时间*/
        public String exit_time;  /*停留结束时间*/
        public String cid;        /*内容id*/
        public String ccid;       /*栏目id*/
        public int comeFrom;      /*来源*/

        public String toReportString() {

            StringBuffer str = new StringBuffer();
            str.append("d");

            str.append(entry_time);
            str.append(exit_time);

            int len = 9 - cid.length();
            while (len > 0) {
                str.append(' ');
                len--;
            }
            str.append(cid);

            len = 9 - ccid.length();
            while (len > 0) {
                str.append(' ');
                len--;
            }
            str.append(ccid);

            str.append(String.format("%02d", comeFrom));

            len = 60 - str.length();

            while (len-- > 0) {
                str.append(' ');
            }

            return str.toString();
        }

    }

    static private class Class_a {

        static public byte click_total_btn = 0;
        static public byte quick_play_entry = 0;
        static public byte click_my_vedio_btn = 0;
        static public byte click_notic_btn = 0;
        static public byte click_msg_btn = 0;
        static public byte click_recharge_btn = 0;
        static public byte click_vip_btn = 0;
        static public byte click_vip_channel_btn = 0;
        static public byte click_my_collect_btn = 0;
        static public byte click_my_cache_btn = 0;
        static public byte click_local_btn = 0;
        static public byte click_search_btn = 0;
        static public byte click_detail_share_btn = 0;
        static public byte click_detail_btn = 0;
        static public byte click_detail_book_btn = 0;
        static public byte click_h5_play_btn = 0;
        static public byte click_class_btn = 0;
        static public byte popup_vip = 0;
        static public byte dl_more_btn = 0;
        static public byte popup_vip_cancel = 0;
        static public byte click_video_detail_cillect_btn = 0;
        static public byte click_quick_play_btn = 0;


        static public String toReportString() {
            StringBuffer str = new StringBuffer();
            str.append("a");

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date curDate = new Date(System.currentTimeMillis());

            str.append(formatter.format(curDate));

            str.append(String.format("%02d", click_total_btn));
            str.append(String.format("%02d", quick_play_entry));
            str.append(String.format("%02d", click_my_vedio_btn));
            str.append(String.format("%02d", click_notic_btn));
            str.append(String.format("%02d", click_msg_btn));
            str.append(String.format("%02d", click_recharge_btn));
            str.append(String.format("%02d", click_vip_btn));
            str.append(String.format("%02d", click_vip_channel_btn));
            str.append(String.format("%02d", click_my_collect_btn));
            str.append(String.format("%02d", click_my_cache_btn));
            str.append(String.format("%02d", click_local_btn));
            str.append(String.format("%02d", click_search_btn));
            str.append(String.format("%02d", click_detail_share_btn));
            str.append(String.format("%02d", click_detail_btn));
            str.append(String.format("%02d", click_detail_book_btn));
            str.append(String.format("%02d", click_h5_play_btn));
            str.append(String.format("%02d", click_class_btn));
            str.append(String.format("%02d", popup_vip));
            str.append(String.format("%02d", dl_more_btn));
            str.append(String.format("%02d", popup_vip_cancel));
            str.append(String.format("%02d", click_video_detail_cillect_btn));
            str.append(String.format("%02d", click_quick_play_btn));

            int len = 60 - str.length();

            while (len-- > 0) {
                str.append(' ');
            }

            return str.toString();
        }

        public static void save() {
            setPrefString("Class_a", toReportString());
        }
    }

    static private class Class_9 {

        static public byte upgrade_receive = 0;
        static public byte upgrade_click_ok = 0;
        static public byte upgrade_click_cancel = 0;
        static public byte upgrade_download = 0;
        static public byte notice_inner_click = 0;
        static public byte notice_outer_click = 0;

        static public byte field_15 = 0;
        static public byte field_17 = 0;
        static public byte field_19 = 0;
        static public byte field_21 = 0;

        static public byte upgrad_popup = 0;
        static public byte upgrad_self_check = 0;
        static public byte upgrad_smart = 0;


        static public String toReportString() {
            StringBuffer str = new StringBuffer();
            str.append("9");
            //第2位-3位为  收到升级提示框的次数(与罗周确认过这个次数已经废除,只做占位)
            str.append(String.format("%02d", upgrade_receive));
            //第4位-5位为  点击“确定”升级次数
            str.append(String.format("%02d", upgrade_click_ok));
            //第6位-7位为  点击“取消”升级次数
            str.append(String.format("%02d", upgrade_click_cancel));
            //第8位-9位为  升级成功
            str.append(String.format("%02d", upgrade_download));
            //第10位-11位为  内通知点击次数(废除,只做占位)
            str.append(String.format("%02d", notice_inner_click));
            //第12位-13位为  外通知点击次数(废除,只做占位)
            str.append(String.format("%02d", notice_outer_click));
            //第14位-15位下载失败数据
            str.append(String.format("%02d", field_15));
            //第16位-17位为来自全屏播放页的缓存入口(废除,只做占位)
            str.append(String.format("%02d", field_17));
            //第18位-19位为来自详情页的缓存入口(废除,只做占位)
            str.append(String.format("%02d", field_19));
            //第20位-21位为来自魔法盒子左侧菜单入口(废除,只做占位)
            str.append(String.format("%02d", field_21));
            //第22位-23 收到弹窗用户数;(与罗周确认过这个没有废除)
            str.append(String.format("%02d", upgrad_popup));
            //第24位-25 自检升级成功;(废除,只做占位)
            str.append(String.format("%02d", upgrad_self_check));
            //第26位收藏开关(废除,只做占位)
            str.append(String.format("%01d", 0));
            //第27-28位智能升级按钮点击次数(废除,只做占位)
            str.append(String.format("%02d", upgrad_smart));

            int len = 60 - str.length();

            while (len-- > 0) {
                str.append(' ');
            }
            v("Class_9 toReportString", "str=" + str.toString());
            return str.toString();
        }

        public static void save() {
            setPrefString("Class_9", toReportString());
        }
    }

    static private class Class_play {
        public String start_time;
        public String end_time;
        public byte comefrom;
        public String net_type;
        public byte server_comefrom;
        public byte pause_times;

        public String cid;
        public String clid;
        public String suffix;
        public byte localSC;
        public String querydata;

        public String toReportString() {
            StringBuffer str = new StringBuffer();
            str.append('0');
            str.append(start_time);
            str.append(end_time);
            str.append(String.format("%02d", comefrom));

            int len = 4;
            if (!StringUtil.isEmpty(suffix)) {
                len -= suffix.length();

            }
            while (len > 0) {
                str.append(' ');
                len--;
            }
            if (suffix != null)
                str.append(suffix);


            str.append(net_type);
            str.append(String.format("%02d", server_comefrom));
            str.append(String.format("%02d", pause_times));

            len = 10;
            if (!StringUtil.isEmpty(cid)) {
                len -= cid.length();

            }
            while (len > 0) {
                str.append(' ');
                len--;
            }
            if (cid != null)
                str.append(cid);

            len = 9;
            if (!StringUtil.isEmpty(clid)) {
                len -= clid.length();

            }
            while (len > 0) {
                str.append(' ');
                len--;
            }
            if (clid != null)
                str.append(clid);

            str.append(String.format("%01d", localSC));

            if (querydata != null) {
                str.append(querydata);
            }

            v("Class_play toReportString", "str=" + str.toString());

            return str.toString();

        }

    }

    static private class Class_link {
        /***
         * 5 ： 为 连接成功    6 为 ：  连接失败
         */
        public int state;

        /**
         * 网络类型 （1为wifi ，2为3g ，3为gprs  4 为 lte）
         */
        public String networkType = " ";

        /**
         * 开始时间 （单位 毫秒）
         */
        public long startTime;

        /**
         * 结束时间 （单位 毫秒）
         */
        public long endTime;

        /**
         * 设备号（指 pad 的 设备号）
         */
        public String deviceName;

        /**
         * 固件版本号
         */
        public String firmversion;

        public String toReportString() {
            StringBuffer str = new StringBuffer();
            str.append('h');
            str.append(state);
            str.append(networkType);
            long time = endTime - startTime;
            str.append(String.format("%012d", time));
            if (!TextUtils.isEmpty(deviceName)) {
                str.append(String.format("%-20s", deviceName));
            }
            if (!TextUtils.isEmpty(firmversion)) {
                str.append(String.format("%-25s", firmversion));
            }
            v("Class_link toReportString", "str=" + str.toString());
            return str.toString();
        }
    }


    static private class Class_g {
        public String clid = "";    //栏目id   9位
        public String videoid = "";//videoid 10位
        public int comefrom;// 10 电视预约   9电视报push

        public String toReportString() {
            StringBuffer str = new StringBuffer();
            str.append('g');

            int len = 9 - clid.length();
            while (len > 0) {
                str.append(' ');
                len--;
            }
            str.append(clid);

            len = 10 - videoid.length();
            while (len > 0) {
                str.append(' ');
                len--;
            }
            str.append(videoid);

            str.append(String.format("%02d", comefrom));

            len = 60 - str.length();
            while (len > 0) {
                str.append(' ');
                len--;
            }

            return str.toString();
        }

        public void save() {
            if (comefrom == 10) {
                String inf = getPrefString("Class_g");
                setPrefString("Class_g", inf + toReportString());
            } else {
                String inf = getPrefString("Class_g_9");
                setPrefString("Class_g_9", inf + toReportString());
            }
        }

    }


    public static void logEvent(int eventId) {

        v("logEvent", "eventId=" + Integer.toHexString(eventId));

        switch (eventId) {
            case EventId.NOTIC_INNER_CLICK:
                Class_9.notice_inner_click++;
                Class_9.save();
                break;

            case EventId.NOTIC_OUTER_CLICK:
                Class_9.notice_outer_click++;
                Class_9.save();
                break;

            case EventId.UPGRADE_RECEIVE:
                Class_9.upgrade_receive++;
                Class_9.save();
                break;

            case EventId.UPGRADE_CLICK_OK:
                Class_9.upgrade_click_ok++;
                Class_9.save();
                break;

            case EventId.UPGRADE_CLICK_CANCEL:
                Class_9.upgrade_click_cancel++;
                Class_9.save();
                break;

            case EventId.OFFLINE_CACHE_FAIL:
                Class_9.field_15++;
                Class_9.save();
                break;
            case EventId.UPGRADE_DOWNLOAD:
                Class_9.upgrade_download = 1;
                Class_9.save();
                break;

            case EventId.UPGRADE_POPUP:
                Class_9.upgrad_popup = 1;
                Class_9.save();
                break;

            case EventId.UPGRADE_SELF_CHECK:
                Class_9.upgrad_self_check = 1;
                Class_9.save();
                break;
            case EventId.UPGRADE_SMART:
                Class_9.upgrad_smart++;
                L.v(TAG, "logEvent", "upgrad_smart=" + Class_9.upgrad_smart);
                Class_9.save();
                break;

            case EventId.CLICK_TOTAL_BTN:
                Class_a.click_total_btn++;
                Class_a.save();
                break;

            case EventId.QUICK_PLAY_ENTRY:
                Class_a.quick_play_entry++;
                Class_a.save();
                break;

            case EventId.CLICK_MY_VEDIO_BTN:
                Class_a.click_my_vedio_btn++;
                Class_a.save();
                break;

            case EventId.CLICK_NOTIC_BTN:
                Class_a.click_notic_btn++;
                Class_a.save();
                break;

            case EventId.CLICK_MSG_BTN:
                Class_a.click_msg_btn++;
                Class_a.save();
                break;

            case EventId.CLICK_RECHARGE_BTN:
                Class_a.click_recharge_btn++;
                Class_a.save();
                break;

            case EventId.CLICK_VIP_BTN:
                Class_a.click_vip_btn++;
                Class_a.save();
                break;

            case EventId.CLICK_VIP_CHANNEL_BTN:
                Class_a.click_vip_channel_btn++;
                Class_a.save();
                break;

            case EventId.CLICK_MY_COLLECT_BTN:
                Class_a.click_my_collect_btn++;
                Class_a.save();
                break;

            case EventId.CLICK_MY_CACHE_BTN:
                Class_a.click_my_cache_btn++;
                Class_a.save();
                break;

            case EventId.CLICK_LOCAL_BTN:
                Class_a.click_local_btn++;
                Class_a.save();
                break;

            case EventId.CLICK_SEARCH_BTN:
                Class_a.click_search_btn++;
                Class_a.save();
                break;

            case EventId.CLICK_DETAIL_SHARE_BTN:
                Class_a.click_detail_share_btn++;
                Class_a.save();
                break;

            case EventId.CLICK_DETAIL_BTN:
                Class_a.click_detail_btn++;
                Class_a.save();
                break;

            case EventId.CLICK_DETAIL_BOOK_BTN:
                Class_a.click_detail_book_btn++;
                Class_a.save();
                break;

            case EventId.CLICK_H5_PLAY_BTN:
                Class_a.click_h5_play_btn++;
                Class_a.save();
                break;

            case EventId.CLICK_CLASS_BTN:
                Class_a.click_class_btn++;
                Class_a.save();
                break;

            case EventId.POPUP_VIP:
                Class_a.popup_vip++;
                Class_a.save();
                break;

            case EventId.DNLD_MORE_BTN:
                Class_a.dl_more_btn++;
                Class_a.save();
                break;

            case EventId.POPUP_VIP_CANCEL:
                Class_a.popup_vip_cancel++;
                Class_a.save();
                break;

            case EventId.CLICK_VIDEO_DETAIL_COLLECT_BTN:
                Class_a.click_video_detail_cillect_btn++;
                Class_a.save();
                break;

            case EventId.CLICK_QUICK_PLAY_BTN:
                Class_a.click_quick_play_btn++;
                Class_a.save();
                break;

        }

        return;

    }

    public static void logLocalPlay(int from) {

        v("logLocalPlay", "from=" + from);

        StringBuffer inf = new StringBuffer();
        inf.append('1');

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());
        inf.append(formatter.format(curDate));
        inf.append(getNetworkType());
        inf.append(from);

        int len = 60 - inf.length();

        while (len-- > 0) {
            inf.append(' ');
        }

        saveReplocal(inf.toString());

    }

    private static String getNetworkType() {

        ConnectivityManager connectionManager = (ConnectivityManager) TransPadApplication.getTransPadApplication()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();

        if (networkInfo == null) {
            return "0";
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

            TelephonyManager telephonyManager = (TelephonyManager) TransPadApplication.getTransPadApplication()
                    .getSystemService(Context.TELEPHONY_SERVICE);

            if (telephonyManager.getNetworkType() < 4) {
                return "3";
            } else if (telephonyManager.getNetworkType() == 13) {
                return "4";
            } else {
                return "2";
            }

        } else {
            return "1";
        }

    }


    public static void logPlayerOpen(String url, byte comefrom) {

        if (player_event != null && player_event.cid.equals(TPUtil.getCIdByUrl(url))) {
            return;
        }

        v("logPlayerOpen", " url=" + url + " comefrom=" + comefrom);

        player_event = new Class_play();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());

        player_event.start_time = formatter.format(curDate);
        player_event.comefrom = processFrom(comefrom);
        player_event.server_comefrom = TransPadService.isConnected() ? (byte) 98 : (byte) 99;
        player_event.localSC = 2;

        if (!StringUtil.isEmpty(url)) {
            player_event.cid = TPUtil.getCIdByUrl(url);
            player_event.clid = TPUtil.getCLIdByUrl(url);
            player_event.querydata = TPUtil.getUerydataByUrl(url);
        }

        player_event.net_type = getNetworkType();

    }

    public static void logTransPadConnectFaild(String deviceName) {
        pad_link = new Class_link();
        pad_link.deviceName = deviceName;
        pad_link.state = 6;
        pad_link.startTime = 0;
        pad_link.endTime = 0;
        pad_link.networkType = getNetworkType();
    }

    public static void logTransPadConnected(String deviceId, String firmwareVersion) {
        pad_link = new Class_link();
        pad_link.state = 5;
        pad_link.startTime = System.currentTimeMillis();
        pad_link.endTime = System.currentTimeMillis();
        pad_link.networkType = getNetworkType();
        pad_link.deviceName = deviceId;
        pad_link.firmversion = firmwareVersion;
        v("logTransPadConnected", "pad_link=" + pad_link.toReportString());
        saveTransPadLinkRep(pad_link.toReportString());
        linkStart();
    }

    /**
     * TransPad断开数据上报
     */
    public static void logTransPadDisConnected(String deviceId, String firmwareVersion) {
        v("logTransPadDisConnected", "start deviceId=" + deviceId + " firmwareVersion=" + firmwareVersion);
        if (pad_link != null) {
            pad_link.state = 4;
            pad_link.endTime = System.currentTimeMillis();
            pad_link.deviceName = deviceId;
            pad_link.firmversion = firmwareVersion;
            v("logTransPadDisConnected", "pad_link=" + pad_link.toString());
            saveTransPadLinkRep(pad_link.toReportString());
            pad_link = null;
            linkStart();
        }
    }

    public static void logTransPadDevicesName(String name) {
        v("logTransPadDevicesName", "name=" + name);
        if (pad_link != null) {
            pad_link.deviceName = name;
        }
    }

    public static void logTransPadDeviceFirmversion(String firmversion) {
        v("logTransPadDeviceFirmversion", "firmversion=" + firmversion);
        if (pad_link != null) {
            pad_link.firmversion = firmversion;
        }
    }

    /**
     * 是否已经有设备名称了
     */
    public static boolean hasDevicesName() {
        if (pad_link != null && pad_link.deviceName != null) {
            return true;
        }
        return false;
    }

    /**
     * 是否已经有物理地址了
     */
    public static boolean hasFirmversion() {
        if (pad_link != null && pad_link.firmversion != null) {
            return true;
        }
        return false;
    }

    /**
     * 记录点击次数
     *
     * @param appName
     */
    public static void logInvokErp(String appName, int state) {
        InvokErp invokErp = new InvokErp(appName);
        invokErp.setState(state);
        logInvokErp(invokErp);
    }

    public static void logInvokErp(InvokErp invokErp) {
        if (invokErp != null) {
            String invokErpStr = getPrefString("transpad_invok_erp");
            Gson gson = new Gson();
            List<InvokErp> invokErps = null;
            if (!TextUtils.isEmpty(invokErpStr)) {
                invokErps = gson.fromJson(invokErpStr, new TypeToken<List<InvokErp>>() {
                }.getType());
                boolean hasSameNameItem = false;
                for (InvokErp erp : invokErps) {
                    if (erp.getState() == invokErp.getState()) {
                        if (erp.getName().equals(invokErp.getName())) {
                            hasSameNameItem = true;
                            erp.setTimes(erp.getTimes() + 1);
                            break;
                        }
                    }
                }
                if (!hasSameNameItem) {
                    invokErp.setTimes(1);
                    invokErps.add(invokErp);
                }
            } else {
                invokErps = new ArrayList<>();
                invokErp.setTimes(1);
                invokErps.add(invokErp);
            }
            invokErpStr = gson.toJson(invokErps);
            v("logInvokErp", "invokErpStr=" + invokErpStr);
            setPrefString("transpad_invok_erp", invokErpStr);
        }
    }

    private static void saveTransPadLinkRep(String inf) {
        if (!StringUtil.isEmpty(inf)) {

            String linkRep = getPrefString("transpad_link_inf");

            if (linkRep.length() > 0) {
                setPrefString("transpad_link_inf", linkRep + inf);
            } else {
                setPrefString("transpad_link_inf", inf);
            }

        }
    }


    private static byte processFrom(byte from) {
        byte ret = 4;
        switch (from) {
            case 0:
                ret = 2;
                break;
            case 1:
                ret = 1;
                break;
            case 2:
                ret = 23;
                break;
            case 3:
                ret = 4;
                break;
            case 4:
                ret = 4;
                break;
            case 5:
                ret = 24;
                break;
            case 6:
                ret = 4;
                break;
            case 7:
                ret = 4;
                break;
            case 8:
                ret = 4;
                break;
            case 9:
                ret = 27;
                break;
            case 10:
                ret = 27;
                break;
            case 11:
                ret = 4;
                break;
            case 12:
                ret = 18;
                break;
            case 13:
                ret = 4;
                break;
            case 40:
                ret = 18;
                break;
            case 46:
                ret = 27;
                break;
            case 47:
                ret = 28;
                break;
            case 48:
                ret = 29;
                break;
            case 49:
                ret = 26;
                break;
            case 50:
                ret = 26;
                break;
            case 51:
                ret = 25;
                break;
            case 52:
                ret = 26;
                break;
            case 53:
                ret = 31;
                break;
            case 54:
                ret = 32;
                break;
            case 55:
                ret = 23;
                break;
            case 56:
                ret = 12;
                break;
            case 57:
                ret = 30;
                break;
            case 58:
                ret = 33;
                break;
            case 59:
                ret = 34;
                break;
            case 60:
                ret = 35;
                break;

        }

        return ret;
    }


    public static void logPlayerClose() {

        v("logPlayerClose", "start");

        if (player_event != null) {

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date curDate = new Date(System.currentTimeMillis());

            player_event.end_time = formatter.format(curDate);

            savePlayrep(player_event.toReportString());

            player_event = null;
        }

    }

    public static void logPlayerPause() {

        v("logPlayerPause", "start");

        if (player_event != null) {
            player_event.pause_times++;
        }

    }

    private static void savePlayrep(String inf) {

        if (!StringUtil.isEmpty(inf)) {

            String playrep = getPrefString("playrep_inf");

            if (playrep.length() > 0) {
                setPrefString("playrep_inf", playrep + "$#" + inf);
            } else {
                setPrefString("playrep_inf", inf);
            }

        }

    }


    private static void saveReplocal(String inf) {

        if (!StringUtil.isEmpty(inf)) {

            String replocal = getPrefString("replocal_inf");

            v("saveReplocal", "replocal=" + replocal + " inf=" + inf);

            setPrefString("replocal_inf", replocal + inf);

        }

    }

    private static void cleanInvokErp() {
        setPrefString("transpad_invok_erp", "");
    }

    public static void ordinaryStart() {

        v("ordinaryStart", "start");

        //本地上报
        String inf = getPrefString("replocal_inf");
        if (!StringUtil.isEmpty(inf)) {

            v("start", "replocal_inf inf=" + inf);
            localReport(inf);
            setPrefString("replocal_inf", "");
        }

        inf = getPrefString("Class_9");
        if (!StringUtil.isEmpty(inf)) {
            v("start", "Class_9 inf=" + inf);
            localReport(inf);
            setPrefString("Class_9", "");
        }

        inf = getPrefString("Class_a");
        if (!StringUtil.isEmpty(inf)) {
            v("start", "Class_a inf=" + inf);
            localReport(inf);
            setPrefString("Class_a", "");
        }

        inf = getPrefString("Class_g_9");
        if (!StringUtil.isEmpty(inf)) {
            v("start", "Class_g_9 inf=" + inf);
            localReport(inf);
            setPrefString("Class_g_9", "");
        }

        inf = getPrefString("Class_g");
        if (!StringUtil.isEmpty(inf)) {
            v("start", "Class_g inf=" + inf);
            localReport(inf);
            setPrefString("Class_g", "");
        }

        //播放上报
        playReport();

        //应用启动上报
        invokReport();
    }

    public static void linkStart() {
        v("linkStart", "start");
        String inf = getPrefString("transpad_link_inf");
        if (!StringUtil.isEmpty(inf)) {
            v("linkStart", "transpad_link_inf inf=" + inf);
            localReport(inf);
            setPrefString("transpad_link_inf", "");
        }
    }

    private static void invokReport() {
        v("invokReport", "start");
        String invokErpStr = getPrefString("transpad_invok_erp");
        try {
            String data = URLEncoder.encode(new String(Base64.encode(invokErpStr.getBytes("UTF-8"), Base64.URL_SAFE | Base64.NO_WRAP), "UTF-8"), "UTF-8");
            if (!TextUtils.isEmpty(data)) {
                v("report", "invokErp=" + invokErpStr + " data=" + data);
                Request.getInstance().invokerp(data, new Callback<Rst>() {
                    @Override
                    public void success(Rst rst, Response response) {
                        if (rst.result == 0) {
                            //上报成功
                            cleanInvokErp();
                        }
                        v("report", "success invokerp result=" + rst.result);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        v("report", "failure invokerp error=" + error.getMessage());
                    }
                });

            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            e("report", "UnsupportedEncodingException", e);
        }
    }

    private static void localReport(String replocalInf) {
        v("localReport", "start");
        try {
            String inf;
            inf = new String(Base64.encode(
                    replocalInf.getBytes("UTF-8"), Base64.URL_SAFE
                            | Base64.NO_WRAP), "UTF-8");

            Request.getInstance().replocal(inf, new Callback<Rst>() {
                @Override
                public void success(Rst rst, Response response) {
                    v("ordinaryReport", "success replocal result=" + rst.result);
                }

                @Override
                public void failure(RetrofitError error) {
                    v("ordinaryReport", "failure replocal error=" + error.getMessage());
                }
            });

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }
    }

    private static void playReport() {
        v("playReport", "start");
        String playrep_inf = getPrefString("playrep_inf");
        if (!StringUtil.isEmpty(playrep_inf)) {

            v("playReport", "playrep=" + playrep_inf);

            try {
                String inf;
                inf = new String(Base64.encode(
                        playrep_inf.getBytes("UTF-8"), Base64.URL_SAFE
                                | Base64.NO_WRAP), "UTF-8");

                Request.getInstance().playrep(inf, "1", new Callback<Rst>() {

                    @Override
                    public void success(Rst rst, Response response) {
                        if (rst.result == 0) {
                            setPrefString("playrep_inf", "");
                        }
                        v("ordinaryReport", "success playrep result=" + rst.result);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        v("ordinaryReport", "failure playrep error=" + error.getMessage());
                    }
                });
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 下载成功上报
     */
    public static void reportDownloadSuccess(String ccrid) {
        v("reportDownloadSuccess", "ccrid=" + ccrid);
        Request.getInstance().repdownload(ccrid, new Callback<Rst>() {
            @Override
            public void success(Rst rst, Response response) {
                if (rst != null) {
                    v("reportDownloadSuccess", "success result=" + rst.result);
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                if (retrofitError != null) {
                    v("reportDownloadSuccess", "failure error=" + retrofitError.getLocalizedMessage());
                }
            }
        });
    }

    private static SharedPreferences mSharedPreferences;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void setPrefString(String key, String value) {

        if (mSharedPreferences == null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mSharedPreferences = TransPadApplication.getTransPadApplication().getApplicationContext().getSharedPreferences(
                        "Reporter_Info", Context.MODE_MULTI_PROCESS);

            } else {
                mSharedPreferences = TransPadApplication.getTransPadApplication().getApplicationContext().getSharedPreferences(
                        "Reporter_Info", Context.MODE_PRIVATE);
            }
        }

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static String getPrefString(String key) {

        if (mSharedPreferences == null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mSharedPreferences = TransPadApplication.getTransPadApplication().getApplicationContext().getSharedPreferences(
                        "Reporter_Info", Context.MODE_MULTI_PROCESS);

            } else {
                mSharedPreferences = TransPadApplication.getTransPadApplication().getApplicationContext().getSharedPreferences(
                        "Reporter_Info", Context.MODE_PRIVATE);
            }
        }

        return mSharedPreferences.getString(key, "");

    }

    static void v(String type, String msg) {
        L.v(TAG, type, msg);
    }

    static void e(String type, String msg, Exception e) {
        L.e(TAG, type, msg, e);
    }
}
