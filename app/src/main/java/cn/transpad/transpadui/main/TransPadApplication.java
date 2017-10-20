package cn.transpad.transpadui.main;

import android.support.multidex.MultiDexApplication;

import com.fone.player.FonePlayer;
import com.sohuvideo.sdk.SohuVideoPlayer;
import com.umeng.analytics.MobclickAgent;

import cn.trans.core.api.TransManager;
import cn.transpad.transpadui.cache.ImageDownloadModule;
import cn.transpad.transpadui.http.LoginRst;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.http.SoftRst;
import cn.transpad.transpadui.service.TransPadService;
import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.ApplicationUtil;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;
import cn.transpad.transpadui.util.UpdateUtil;

/**
 * Created by Kongxiaojun on 2015/4/2.
 */
public class TransPadApplication extends MultiDexApplication {

    private static final String TAG = "TransPadApplication";

    private static TransPadApplication application;
    private static TransManager mTranspadManager;

    @Override
    public void onCreate() {
        super.onCreate();

        application = this;
        //sp模块初始化
        SharedPreferenceModule.init(this);

        //工具模块初始化
        TPUtil.init(this);

        //存储模块初始化
        StorageModule.init(this);

        //图片模块初始化
        ImageDownloadModule.init(this);

        // 初始化网络请求
        Request.initializeInstance(getApplicationContext());

//        //初始化UncaughtException处理类
//        CrashHandler.getInstance().init();

        ApplicationUtil.init(this);

        UpdateUtil.init(this);

        //扫描媒体库
        StorageModule.getInstance().scanningAllStorage();

        SohuVideoPlayer.init(getApplicationContext());

        readHomeData();

        FonePlayer.init(this);

        TransPadService.getInstance().init();

        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
        MobclickAgent.openActivityDurationTrack(false);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mTranspadManager.onDestroy();
    }

    public static TransPadApplication getTransPadApplication() {
        return application;
    }

    private SoftRst tpqSoft;

    private LoginRst.MediaPower showmedia = new LoginRst.MediaPower();


    private void readHomeData() {
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                SoftRst rst = TPUtil.readCachedServerData(SoftRst.class, application, "tpq_home_softrst");
                if (rst != null) {
                    tpqSoft = rst;
                }
            }
        });
        thread2.start();
    }

    /**
     * Home软件推荐
     *
     * @return
     */
    public SoftRst getSoftRst() {
        return tpqSoft;
    }

    public String getShowmedia() {
        return showmedia.tpq;
    }

    public void setTpqSoft(SoftRst tpqSoft) {
        this.tpqSoft = tpqSoft;
    }

    public void setShowmedia(LoginRst.MediaPower showmedia) {
        this.showmedia = showmedia;
    }

    /**
     * 软件推荐控制开关:0 关闭 1 开启.
     */
    private String rec;

    public String getRec() {
        return rec;
    }

    public void setRec(String rec) {
        this.rec = rec;
    }

    public TransManager getTranspadManager() {
        L.v(TAG, "getTranspadManager", "start");
        if (mTranspadManager == null) {
            mTranspadManager = new TransManager(this);
        }
        return mTranspadManager;
    }

}
