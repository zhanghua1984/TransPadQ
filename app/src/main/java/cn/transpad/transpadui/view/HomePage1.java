package cn.transpad.transpadui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.entity.Weather;
import cn.transpad.transpadui.http.HaoRst;
import cn.transpad.transpadui.http.HotspotRst;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.http.WeatherApi;
import cn.transpad.transpadui.main.ApplicationFragment;
import cn.transpad.transpadui.main.HomeActivity;
import cn.transpad.transpadui.main.SettingsFragment;
import cn.transpad.transpadui.main.TransPadApplication;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.CalendarUtils;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Created by Kongxiaojun on 2015/4/7.
 */
public class HomePage1 extends LinearLayout implements View.OnClickListener{

    private static final String TAG = "HomePage1";

    private static final int MSG_WHAT_UPDATE_DATE = 963852848;
    private static final int MSG_WHAT_REQUEST_SUCESS = 852741737;

    @InjectView(R.id.tv_city)
    TextView tv_city;
    @InjectView(R.id.tv_date)
    TextView tv_date;
    @InjectView(R.id.tv_temperature)
    TextView tv_temperature;
    @InjectView(R.id.tv_time)
    TextView tv_time;
    @InjectView(R.id.tv_wind)
    TextView tv_wind;
    @InjectView(R.id.tv_Limit_of_no)
    TextView tv_Limit_of_no;
    @InjectView(R.id.iv_weather)
    ImageView iv_weather;
    private Weather weathers;
    private HaoRst haoRsts;
    private Handler mHandler;

    public HomePage1(Context context) {
        super(context);
        init();
    }

    public HomePage1(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public HomePage1(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    private void init() {
        inflate(getContext(), R.layout.home_page1, this);
        ButterKnife.inject(this);
        initHandler();
        weathers = TPUtil.readCachedServerData(Weather.class,getContext(),"weather");
        if(null!=weathers){
            initData(weathers);
        }
        haoRsts = TPUtil.readCachedServerData(HaoRst.class,getContext(),"haoRst");
        if(null!=haoRsts){
            initLimit(haoRsts);
        }
        initData();
        updateDate();
        //创建的时候就已经传入了Context的对象
        mLocationClient = new LocationClient(TransPadApplication.getTransPadApplication());     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        option.setCoorType("gcj02");
        option.setIsNeedAddress(true);
        option.setScanSpan(1000 * 60);
        options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).build();
        mLocationClient.setLocOption(option);
        mLocationClient.start();
        mLocationClient.requestLocation();

    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_WHAT_UPDATE_DATE:
                        updateDate();
                        break;
                    case MSG_WHAT_UPDATE_HOTSPOT:
                        updateHomeHotspot();
                        break;
                    case MSG_WHAT_REQUEST_SUCESS:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    TPUtil.saveServerData(weathers, "weather");
                                    TPUtil.saveServerData(haoRsts, "haoRst");
                                }
                            }).start();
                        break;
                }
            }
        };
    }

    private static String mWay;

    private void updateDate() {
        if (getResources().getConfiguration().locale.getCountry().equals("CN")) {
            //设置农历
            Calendar cFirstStartTime = Calendar.getInstance();
            CalendarUtils nongli = new CalendarUtils(cFirstStartTime);
            //设置时间
            String time = getSysDate();
            tv_time.setText(time);
            //设置日期
            String date = nongli.StringData();
            tv_date.setText(date);
        } else {
            Calendar c = Calendar.getInstance();
            mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
            if ("1".equals(mWay)) {
                mWay = "Sunday";
            } else if ("2".equals(mWay)) {
                mWay = "Monday";
            } else if ("3".equals(mWay)) {
                mWay = "Tuesday";
            } else if ("4".equals(mWay)) {
                mWay = "Wednesday";
            } else if ("5".equals(mWay)) {
                mWay = "Thursday";
            } else if ("6".equals(mWay)) {
                mWay = "Friday";
            } else if ("7".equals(mWay)) {
                mWay = "Saturday";
            }
            String time = getSysDate();
            tv_time.setText(time);
            SimpleDateFormat myFmt = new SimpleDateFormat("dd/MM/yyyy ");
            long date = System.currentTimeMillis();
            String systemDate = myFmt.format(date);
            tv_date.setText(systemDate + mWay);
        }

        mHandler.sendEmptyMessage(MSG_WHAT_UPDATE_DATE);
//        mHandler.sendEmptyMessageDelayed(MSG_WHAT_UPDATE_DATE,1000);
    }

    class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.v(TAG, "onReceiveLocation");
            if (location == null)
                return;
//            tv_city.setText("");
            String city = location.getCity();
            if (TextUtils.isEmpty(city)) {
                return;
            }
            if (city.endsWith("市") || city.endsWith("县") || city.endsWith("区")) {
                city = city.substring(0, city.length() - 1);
            }
            WeatherApi.getInstance().getWeatherByCityName(city, new Callback<Weather>() {
                @Override
                public void success(final Weather weather, Response response) {
                    Log.v(TAG, weather.toString());
                    weathers = weather;
                    mHandler.sendEmptyMessage(MSG_WHAT_REQUEST_SUCESS);
                    initData(weather);
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
//            WeatherApi.getInstance().getAqiByCityName(city,new Callback<Aqi>() {
//                @Override
//                public void success(Aqi aqi, Response response) {
//                    Log.v(TAG, aqi.toString());
//                }
//
//                @Override
//                public void failure(RetrofitError error) {
//
//                }
//            });
        }
    }
    private void initData(Weather weather){
        //城市的显示
        tv_city.setText(weather.retData.city.toString());
        //温度的显示
        tv_temperature.setText(weather.retData.temp.toString());
        //风向的显示
        tv_wind.setText(weather.retData.WD.toString());
        //天气的图片显示
        if (weather.retData.weather.toString().contains("云")) {
            iv_weather.setImageResource(R.drawable.index_weather_cloud);
        } else if (weather.retData.weather.toString().contains("雨")) {
            iv_weather.setImageResource(R.drawable.index_weather_rain);
        } else if (weather.retData.weather.toString().contains("晴")) {
            iv_weather.setImageResource(R.drawable.index_weather_sunny);
        } else if (weather.retData.weather.toString().contains("雾") || weather.retData.weather.toString().contains("霾")) {
            iv_weather.setImageResource(R.drawable.index_weather_fog);
        } else if (weather.retData.weather.toString().contains("雪")) {
            iv_weather.setImageResource(R.drawable.index_weather_snow);
        } else if (weather.retData.weather.toString().contains("雷")) {
            iv_weather.setImageResource(R.drawable.index_weather_thunder);
        }
        //限号信息的显示
        Request.getInstance().hao(weather.retData.city.toString(), new Callback<HaoRst>() {
            @Override
            public void success(HaoRst haoRst, Response response) {
                haoRsts = haoRst;
                initLimit(haoRst);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }
    private void initLimit(HaoRst haoRst){
        if(TextUtils.isEmpty(haoRst.hao.local)){
            tv_Limit_of_no.setText(String.format(getContext().getString(R.string.limit)));
        }else {
            if(!haoRst.hao.local.toUpperCase().equals("NULL")) {
                tv_Limit_of_no.setText(String.format(getContext().getString(R.string.limit)) + haoRst.hao.local);
            }
        }
    }
    @OnClick(R.id.iv_link)
    void disConnect() {
        TPUtil.connectTransPad();
    }

    @OnClick(R.id.iv_set)
    void goSetting() {
        HomeActivity.switchFragment(new SettingsFragment());
//        Fragment fragment = new MusicPlayFragment();
//        HomeActivity.switchFragment(fragment);
//        HomeActivity.switchFragment(new MusicPlayFragment());
//        StorageModule.getInstance().copyDataBaseToSdCard();
    }

    @OnClick(R.id.ll_other)
    void goApplicationPage() {
        HomeActivity.switchFragment(new ApplicationFragment());
    }
    @OnClick(R.id.rl_ie)
    void network() {
        TPUtil.openBrowser(getContext(), null);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    //当页面被移出时调用
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //停止服务
        if (mLocationClient != null) {
            mLocationClient.stop();
        }
    }

    /*获取当前系统时间*/
    public static String getSysDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(date);
    }






    @InjectView(R.id.rl_drive)
    RelativeLayout relativeLayout1;
    @InjectView(R.id.rl_ie)
    RelativeLayout relativeLayout2;
    @InjectView(R.id.rl_music)
    RelativeLayout relativeLayout3;
    @InjectView(R.id.tv_drive_name)
    TextView appName1;
    @InjectView(R.id.tv_ie_name)
    TextView appName2;
    @InjectView(R.id.tv_music_name)
    TextView appName3;
    @InjectView(R.id.iv_drive_pic)
    ImageView appImage1;
    @InjectView(R.id.iv_ie_pic)
    ImageView appImage2;
    @InjectView(R.id.iv_music_pic)
    ImageView appImage3;
    private MainPageDownloadDialog mainPageDownloadDialog;
    DisplayImageOptions options;
    private HotspotRst hotspotRst;
    private static final int MSG_WHAT_UPDATE_HOTSPOT = 1;
    /**
     * 初始化数据
     */
    private void initData() {
        L.v(TAG, "initData");
        new Thread(new Runnable() {
            @Override
            public void run() {
                HotspotRst hotspotRst = TPUtil.readCachedServerData(HotspotRst.class, getContext(), "auto_home_hotspot");
                if (hotspotRst != null){
                    HomePage1.this.hotspotRst = hotspotRst;
                    mHandler.sendEmptyMessage(MSG_WHAT_UPDATE_HOTSPOT);
                }
            }
        }).start();
        Request.getInstance().hotspot(2, new Callback<HotspotRst>() {
            @Override
            public void success(HotspotRst hotspotRst, Response response) {
                if (hotspotRst != null && hotspotRst.result == 0) {
                    HomePage1.this.hotspotRst = hotspotRst;
                    updateHomeHotspot();
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
        relativeLayout1.setOnClickListener(this);
        relativeLayout2.setOnClickListener(this);
        relativeLayout3.setOnClickListener(this);
    }
    /**
     * 更新hotspot
     */
    private void updateHomeHotspot(){
        if (hotspotRst != null){
            if (hotspotRst.posters != null){
                if (hotspotRst.posters.posterList.size() > 0){
                    ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(hotspotRst.host,hotspotRst.shost,hotspotRst.posters.posterList.get(0).pic),appImage1,options);
                    appName1.setText(hotspotRst.posters.posterList.get(0).name);
                }
                if (hotspotRst.posters.posterList.size() > 1){
                    ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(hotspotRst.host,hotspotRst.shost,hotspotRst.posters.posterList.get(1).pic),appImage2,options);
                    appName2.setText(hotspotRst.posters.posterList.get(1).name);
                }
                if (hotspotRst.posters.posterList.size() > 2){
                    ImageLoader.getInstance().displayImage(TPUtil.getAbsoluteUrl(hotspotRst.host,hotspotRst.shost,hotspotRst.posters.posterList.get(2).pic),appImage3,options);
                    appName3.setText(hotspotRst.posters.posterList.get(2).name);
                }
            }
        }
    }
     HotspotRst.Poster poster;
    private void onClickApp(final int postion) {
        if (hotspotRst == null){
            Toast.makeText(getContext(), R.string.no_network_toast,
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (!TPUtil.isNetOkWithToast()){
            return;
        }
        if(hotspotRst.posters!=null) {
            if (hotspotRst.posters.posterList.size() <= postion) {
                return;
            }
        }

        if(hotspotRst.posters!=null) {
             poster = hotspotRst.posters.posterList.get(postion);

        if (poster.utp.equals("3")){
            //打开浏览器
            TPUtil.openBrowser(getContext(),poster.url);
            return;
        }
        PackageInfo info = TPUtil.checkApkExist(getContext(), poster.pkname);
        if (info == null) {
            OfflineCache offlineCache = StorageModule.getInstance().getOfflineCacheById(poster.id);
            if (offlineCache != null) {
                switch (offlineCache.getCacheDownloadState()){
                    case OfflineCache.CACHE_STATE_FINISH:
                        File file = new File(offlineCache.getCacheStoragePath());
                        if(file.exists()) {
                            TPUtil.installAPK(file, getContext());
                        }else{
                            StorageModule.getInstance().deleteCache(offlineCache);
                            showDialog(poster);
                        }
                    break;
                    case OfflineCache.CACHE_STATE_DOWNLOADING:
                        Toast.makeText(getContext(), R.string.app_already_downlaod, Toast.LENGTH_SHORT).show();
                        break;
                    case OfflineCache.CACHE_STATE_PAUSE:
                        Toast.makeText(getContext(), R.string.download_has_been_suspended, Toast.LENGTH_SHORT).show();
                        break;
                }
            }else{
                showDialog(poster);
            }
        } else {
            TPUtil.startAppByPackegName(getContext(), poster.pkname);
        }
        }
    }
    public  void showDialog(final HotspotRst.Poster poster){
        //未安装
        mainPageDownloadDialog = new MainPageDownloadDialog(getContext(), R.style.myDialog);
        mainPageDownloadDialog.setMessage(String.format(getContext().getString(R.string.home_download_dialog_message), poster.name), poster.url);
        mainPageDownloadDialog.setClickListener(new MainPageDownloadDialog.ClickListener() {
            @Override
            public void onOk() {
                if (TPUtil.isNetOkWithToast()) {
                    OfflineCache offlineCache = new OfflineCache();
                    offlineCache.setCacheName(poster.name);
                    offlineCache.setCacheID(poster.id);
                    offlineCache.setCachePackageName(poster.pkname);
                    offlineCache.setCacheDetailUrl(poster.url);
                    offlineCache.setCacheImageUrl(TPUtil.getAbsoluteUrl(hotspotRst.host,hotspotRst.shost,poster.pic));
                    StorageModule.getInstance().addCache(offlineCache);
                }
                mainPageDownloadDialog.dismiss();
            }

            @Override
            public void onCancel() {
                mainPageDownloadDialog.dismiss();
            }
        });
        mainPageDownloadDialog.show();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_drive:
                onClickApp(0);
                break;
            case R.id.rl_ie:
                onClickApp(1);
                break;
            case R.id.rl_music:
                onClickApp(2);
                break;

        }
    }
}




