package cn.transpad.transpadui.http;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import cn.transpad.transpadui.BuildConfig;
import cn.transpad.transpadui.service.TransPadService;
import cn.transpad.transpadui.util.SystemUtil;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Kongxiaojun on 2015/4/2.
 */
public class Request {

    private static final String TAG = "Request";

    private static Request ourInstance;

    private String cipher;

    public static Request getInstance() {
        return ourInstance;
    }

    private RestAdapter restAdapter;

    private RequestApi api;

    private final Context context;

    private String version;
    private String uid;

    public String getCipher() {
        return cipher;
    }

    private Request(Context context) {
        final OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(15, TimeUnit.SECONDS);
        okHttpClient.setConnectTimeout(15, TimeUnit.SECONDS);
        this.context = context;
        restAdapter = new RestAdapter.Builder()
                .setEndpoint(Configure.getEndPoint())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(new AndroidLog(TAG))
                .setConverter(new SimpleXMLConverter())
                .setClient(new OkClient(okHttpClient))
                .build();
        api = restAdapter.create(RequestApi.class);
        initCipher();
    }

    public static synchronized void initializeInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new Request(context);
        }
    }

    public void initCipher() {
        // cipher =
        // "pVkybguX6Mw8FSXoSLNfnok1AtFpU8fju1Gl7yoSoJNp3QxhCo37HIu50JD14F3QsOwXBo7wz5hduCIYW5n68Uy0C-cyKYBubtj_-24zltjDj8NU_P002h52xgFKy0xQ_FDBjV1RPi3vWRCZbiDLSVAByq-CrPaD_6oDOd3ZLJMj0l_MP17qgir-PFJWzCUAy7OlUpicjo2W_BRUYvRO3xDWMilcZP0P05Kjh5ALegs%3D";

        // loadCipherFromPreference();

        // 获取新的seqid
        String seqid = loadSeqidFromPreference();

        updateCipher(seqid);

        Log.i(TAG, "initCip                                                                                                                                                                                                           her() load seqid form Preference. seqid=" + seqid);

    }


    interface RequestApi {
        /**
         * 登录、升级接口: login?cipher=&ua=&nt=&uflag=
         */
        @GET("/androidliteaoclogin1.xml")
        void login(@Query(value = "cipher", encodeValue = false) String cipher,
                   @Query("nt") String nt,
                   @Query("uflag") int uflag, Callback<LoginRst> callback);

        /**
         * 海报接口
         */
        @GET("/hotspot")
        void hotspot(@Query(value = "cipher", encodeValue = false) String cipher, @Query(value = "type") String type,
                     @Query("nt") String nt, Callback<HotspotRst> callback);

        /**
         * 栏目接口
         */
        @GET("/specllist")
        void specllist(@Query(value = "cipher", encodeValue = false) String cipher,
                       @Query("nt") String nt, Callback<SpecllistRst> callback);

        /**
         * 栏目接口 specllist?clid=&cipher=&nt=&page=
         */
        @GET("/{path}")
        void specllist(@Path(value = "path", encode = false) String path,
                       Callback<SpecllistRst> callback);

        @FormUrlEncoded
        @POST("/playrep")
        void playrep(@Query(value = "cipher", encodeValue = false) String cipher,
                     @Query("index") String index, @Field("inf") String inf,
                     retrofit.Callback<Rst> callback);

        /**
         * 本地上报接口 replocal?cipher=&inf=
         */
        @FormUrlEncoded
        @POST("/replocal")
        void replocal(@Query(value = "cipher", encodeValue = false) String cipher,
                      @Query(value = "fmt", encodeValue = false) String fmt,
                      @Field(value = "inf", encodeValue = false) String inf, retrofit.Callback<Rst> callback);

        @GET("/dl")
        void repdownload(@Query(value = "cipher", encodeValue = false) String cipher, @Query(value = "flag", encodeValue = false) String flag, @Query(value = "ccrid", encodeValue = false) String ccrid,
                         Callback<Rst> callback);

        /**
         * 软件推荐接口 soft?cipher=&flag=&mf=
         */
        @GET("/androidlitesoft1.xml")
        void soft(@Query(value = "cipher", encodeValue = false) String cipher,
                  @Query("flag") String flag, Callback<SoftRst> callback);

        /**
         * 视频详情页 vgdetail?cipher=&nt=&videoid=&cid=&ccid=
         */
        @GET("/{path}")
        void vgdetail(@Path(value = "path", encode = false) String path,
                      Callback<VgdetailRst> callback);

        /**
         * 播放、离线缓存接口 xyzplay?cid=&loop=&cipher=&nt=&ifp=&pt=&lk=&kw=&dfnt=
         */
        @GET("/{path}")
        void xyzplay(@Path(value = "path", encode = false) String path,
                     Callback<XyzplaRst> callback);

        @GET("/{path}")
        void linkvideo(@Path(value = "path", encode = false) String path, Callback<LinkvideoRst> callback);

        @FormUrlEncoded
        @POST("/fb.xml")
        void fb(@Query(value = "cipher", encodeValue = false) String cipher, @Query("nt") String networkType, @Field("cnt") String cnt, @Field("contact") String contact, @Field("ctyp") String ctyp, @Field("fbtype") String fbtype, Callback<Rst> callback);

        @GET("/hao")
        void hao(@Query(value = "cipher", encodeValue = false) String cipher, @Query(value = "city", encodeValue = false) String city, Callback<HaoRst> callback);

        @GET("/{path}")
        void drama(@Path(value = "path", encode = false) String path, Callback<DramaRst> callback);

        @FormUrlEncoded
        @POST("/invokerp")
        void invokerp(@Query(value = "cipher", encodeValue = false) String cipher, @Field(value = "data", encodeValue = false) String data, Callback<Rst> callback);

        /**
         * 获取搜狐视频id接口
         */
        @GET("/getvideoid")
        void sohuIds(@Query("ourl") String ourl,
                     @Query(value = "cipher", encodeValue = false) String cipher,
                     Callback<SohuIdRst> callback);
    }

    /**
     * 登录、升级接口
     *
     * @param uflag    初始化类型: 0全部 1升级 2频道
     * @param callback
     */
    public void login(int uflag, Callback<LoginRst> callback) {
        Log.i(TAG, "login cipher = " + cipher);

        api.login(cipher, getNetworkType(), uflag,
                callback);

    }

    /**
     * 海报接口
     *
     * @param type     0: (取海报) ,1:  tpd(首页),2：tpa首页
     * @param callback
     */
    public void hotspot(int type, Callback<HotspotRst> callback) {
        api.hotspot(cipher, type + "", getNetworkType(), callback);
    }

    /**
     * 栏目接口
     *
     * @param callback
     */
    public void specllist(Callback<SpecllistRst> callback) {
        api.specllist(cipher, getNetworkType(), callback);
    }

    /**
     * 栏目接口
     *
     * @param callback
     */
    public void specllist(String url, Callback<SpecllistRst> callback) {

        url = url + "&cipher=" + cipher + "&nt=" + getNetworkType();
        api.specllist(url, callback);

    }

    public void playrep(String inf, String index, Callback<Rst> callback) {

        Log.v("Reporter", "playrep inf: " + inf + " index: " + index);
        api.playrep(cipher, index, inf, callback);
    }

    public void replocal(String inf, Callback<Rst> callback) {

        Log.v("Reporter", "replocal inf: " + inf);

        api.replocal(cipher, "xml", inf, callback);
    }

    public void repdownload(String ccrid, Callback<Rst> callback) {

        Log.v("Request", "repdownload ccrid=" + ccrid);
        api.repdownload(cipher, "3", ccrid, callback);
    }

    /**
     * 软件推荐接口
     *
     * @param flag     标识符  0:TPQ软件推荐 1:只取软件 2:首屏软件推荐 3:TPD软件推荐
     * @param callback 回调函数
     */
    public void soft(String flag, Callback<SoftRst> callback) {

        Log.v("Reporter", "soft flag=" + flag);

        api.soft(cipher, flag, callback);
    }

    /**
     * 视频详情页
     *
     * @param url      地址
     * @param callback
     * @return void
     * @throws
     */
    public void vgdetail(String url, Callback<VgdetailRst> callback) {

        url = url + "&cipher=" + cipher + "&nt=" + getNetworkType();
        api.vgdetail(url, callback);

    }

    /**
     * 关联视频列表接口（详情页面）
     *
     * @param callback
     */
    public void linkvideo(String path, Callback<LinkvideoRst> callback) {

        path = path + "&cipher=" + cipher + "&nt=" + getNetworkType();
        api.linkvideo(path, callback);

    }

    /**
     * 播放、离线缓存接口
     *
     * @param path     请求地址
     * @param pt       播放类型信息 pt=1—播放 pt=2—下载 pt=3—下载成功上报
     * @param comefrom 来源参考PlayerFrom类常量
     * @param callback 请求回调
     * @return void
     * @throws
     */
    public void xyzplay(String path, String pt, String comefrom,
                        Callback<XyzplaRst> callback) {

        path = path + "&cipher=" + cipher + "&nt=" + getNetworkType()
                + "&fmt=xml" + "&pt=" + pt + "&comefrom=" + comefrom;

        api.xyzplay(path, callback);
    }

    /**
     * 剧集接口
     *
     * @param callback
     */
    public void drama(String url, Callback<DramaRst> callback) {

        url = url + "&cipher=" + cipher + "&nt=" + getNetworkType();
        api.drama(url, callback);

    }

    /**
     * 调用应用上报
     *
     * @param data     数据信息先Base64编码 再URLENCODE), Json格式：[{“name”:”100tv”,“times”:”10”},{“name”:”aiqiy”,“times”:”2”},{“name”:”高德地图”,“times”:”10”}……]
     * @param callback
     */
    public void invokerp(String data, Callback<Rst> callback) {
        api.invokerp(cipher, data, callback);
    }

    /**
     * 获取用户评论接口
     *
     * @param cnt      反馈内容
     * @param contact  联系方式
     * @param ctyp     联系方式类型  email phone qq
     * @param fbtype   问题类型 5 ： pad问题 6 :  软件问题 7： 内容问题 8 ：  其他
     * @param callback
     */
    public void fb(String cnt, String contact, String ctyp, int fbtype,
                   Callback<Rst> callback) {
        api.fb(cipher, getNetworkType(), cnt, contact, ctyp, fbtype + "", callback);
    }

    /**
     * 限号信息
     *
     * @param city     城市名
     * @param callback
     */
    public void hao(String city, Callback<HaoRst> callback) {
        api.hao(cipher, Base64.encodeToString(city.getBytes(), Base64.URL_SAFE), callback);
    }

    /**
     * 获取搜狐视频相关id
     *
     * @param ourl     原网页地址需要先Base64 再url
     * @param callback
     * @return void
     * @throws
     */
    public void getSohuIds(String ourl, Callback<SohuIdRst> callback) {
        api.sohuIds(ourl, cipher, callback);
    }

    public String getNetworkType() {

        ConnectivityManager connectionManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();

        if (networkInfo == null) {
            return "";
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            if (telephonyManager.getNetworkType() < 4) {
                return "gprs";

            } else if (telephonyManager.getNetworkType() == 13) {

                return "4g";

            } else {

                return "3g";
            }

        } else {
            return "wifi";
        }

    }

    private String loadSeqidFromPreference() {

        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        return settings.getString("seqid", "");

    }

    public void updateCipher(String seqid) {

        StringBuffer plaintext = new StringBuffer();

        if (null != seqid) {
            plaintext.append("seqid=" + seqid);
            saveSeqidToPreference(seqid);

        } else {
            plaintext.append("seqid=");
        }

        plaintext.append("&cv=" + getVersion());
        plaintext.append("&imei=" + getIMEI());
        plaintext.append("&imsi=" + getIMSI());
        plaintext.append("&uid=" + getUID());
        plaintext.append("&p=" + getPhoneNumber());
        plaintext.append("&pfv=android_" + getSDK());
        plaintext.append("&macadd=" + getMac());
        plaintext.append("&ua=" + Configure.getUa());
        plaintext.append("&firmversion=" + getDeviceFirmVersion());

        StringBuffer temp = new StringBuffer(plaintext);
        temp.append(Configure.getSlat());

        String md5 = toMD5(temp.toString());
        plaintext.append("&key=" + md5);

        Log.i(TAG, "updateCipher() before encrypt,  plaintext:" + plaintext);

        cipher = encrypt(Configure.getAesKey(), plaintext.toString());

        Log.i(TAG, "updateCipher() aftr encrypt, cipher:" + cipher);

        saveCipherToPreference();

    }

    private void saveSeqidToPreference(String seqid) {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("seqid", seqid);
        editor.commit();

    }

    private void saveCipherToPreference() {
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("cipher", cipher);
        editor.commit();

    }

    private String getPhoneNumber() {

        String phoneNumber = "";

        // TelephonyManager tm = (TelephonyManager)
        // context.getSystemService(Context.TELEPHONY_SERVICE);
        // if (tm != null && tm.getLine1Number() != null) {
        // phoneNumber = tm.getLine1Number();
        // }

        if (phoneNumber == null || phoneNumber.length() == 0) {
            phoneNumber = loadPhoneNumberFromFile();
        }

        return phoneNumber;

    }

    private String loadPhoneNumberFromFile() {
        String number = "";

        String path = context.getFilesDir().getAbsolutePath()
                + Configure.PHONE_NUMBER_FILE;

        File file = new File(path);

        if (file.exists() && file.isFile()) {
            try {

                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                fis.close();
                number = new String(buffer);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Log.i(TAG, "loadPhoneNumberFormFile() the file " + path
                    + "doesn't exist.");
        }

        Log.i(TAG, "loadPhoneNumberFormFile() number:" + number);

        return number;
    }

    private String getDeviceFirmVersion() {
        return TransPadService.getDeviceFirmVersion();
    }

    /**
     * 此渠道号在variants.gradle中定义
     *
     * @return channelCode
     */
    private String getVersion() {
        return BuildConfig.CHANNEL_CODE;
    }

    private String getSDK() {
        return Build.VERSION.RELEASE;
    }

    private String getMac() {
        String macSerial = null;
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();
                    break;
                }
            }
        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return macSerial;
    }

    private String getIMEI() {

        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        String imei = tm.getDeviceId();
        if (imei == null)
            imei = "";

        return imei;
    }

    private String getIMSI() {

        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = tm.getSubscriberId();
        if (imsi == null)
            imsi = "";

        return imsi;
    }

    public String getUID() {

        if (uid != null) {
            return uid;
        }

        uid = laodUserIdFromPreference();// 从sp读取UID

        if (uid == null || uid.length() == 0) {

            uid = getUIDFromFile();// 从文件读取UID

            if (!TextUtils.isEmpty(uid)) {

                saveUserIdToPreference(uid);// 写入sp
            }

        }

        if (uid == null || uid.length() == 0) {

            int flag = (int) (Math.random() * 10);
            Time time = new Time();
            time.setToNow();

            long ms = System.currentTimeMillis();
            ms = ms % 1000;
            uid = String.format(
                    "%1$02d%2$02d%3$02d%4$02d%5$02d%6$02d%7$03d%8$01d0",
                    time.year % 100, time.month + 1, time.monthDay, time.hour,
                    time.minute, time.second, ms, flag % 10);

            saveUserIdToPreference(uid);// 写入sp

        }

        writeUID2File(uid);// 写入文件

        return uid;
    }

    private void saveUserIdToPreference(String id) {

        SharedPreferences settings = context.getSharedPreferences(
                Configure.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Configure.USER_ID_KEY, id);
        editor.commit();

    }

    private String laodUserIdFromPreference() {

        SharedPreferences settings = context.getSharedPreferences(
                Configure.PREFS_NAME, 0);
        return settings.getString(Configure.USER_ID_KEY, "");

    }

    /**
     * 从文件获取UID
     *
     * @return
     */
    private String getUIDFromFile() {

        String idPath = SystemUtil.getInstance().getTPRootPath() + File.separator + "id_num.txt";
        File f = new File(idPath);
        if (f.isFile()) {
            if (f.canRead()) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(f);
                    byte[] bytes = new byte[100];
                    int len = fis.read(bytes);
                    fis.close();

                    if (len == 17) {
                        String g_id_string = new String(bytes, 0, len);
                        return g_id_string;
                    } else {

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return "";
    }

    /**
     * 将UID写入文件
     *
     * @param UID
     */
    private void writeUID2File(String UID) {

        if (UID != null && UID.length() > 0) {
            OutputStream fos = null;

            try {
                String idPath = SystemUtil.getInstance().getTPRootPath() + File.separator + "id_num.txt";
                File outputFile = new File(idPath);
                if (!outputFile.exists()) {
                    fos = new FileOutputStream(outputFile);
                    byte[] bt = UID.getBytes();
                    fos.write(bt);
                    fos.flush();
                }
            } catch (Exception e) {

            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public String toMD5(String str) {

        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] byteArray = messageDigest.digest();
        StringBuffer md5StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                md5StrBuff.append("0").append(
                        Integer.toHexString(0xFF & byteArray[i]));
            } else {
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
        }

        return md5StrBuff.toString();
    }

    private String encrypt(String seed, String cleartext) {
        try {
            byte[] result = encrypt(seed.getBytes(), cleartext.getBytes());

            byte[] data = Base64.encode(result, Base64.URL_SAFE
                    | Base64.NO_WRAP);
            String str = new String(data);
            return URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private byte[] encrypt(byte[] raw, byte[] clear) throws Exception {

        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher ci = Cipher.getInstance("AES");
        ci.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = ci.doFinal(clear);
        return encrypted;
    }


}
