package cn.transpad.transpadui.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.TPUtil;
import cn.transpad.transpadui.view.FmPickerView;
import cn.transpad.transpadui.view.MainPageDownloadDialog;
import cn.yunzhisheng.basic.USCRecognizerDialog;
import cn.yunzhisheng.basic.USCRecognizerDialogListener;
import cn.yunzhisheng.common.USCError;

/**
 * Created by wangshaochun on 2015/4/15.
 */
public class MenuActivity extends Activity {
    private SharedPreferences sp;
    private String mapConfig;
    private static final String TAG = "MenuActivity";


    private String[] voiceAppPackageNames;
    private String[] voiceAppUrls;
    private String[] voiceAppIconUrls;
    private static int[] ids = new int[]{4549717,4556816};
    private MainPageDownloadDialog mainPageDownloadDialog;
    String fmValue1;
    String fmValue2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.v(TAG, "onCreate", "start");
        sp = this.getSharedPreferences("config", MODE_PRIVATE);

        int status = sp.getInt("checkLayout", 0);
        voiceAppPackageNames = getResources().getStringArray(R.array.voice_page_app_package);
        voiceAppUrls = getResources().getStringArray(R.array.voice_app_download_url);
        voiceAppIconUrls = getResources().getStringArray(R.array.voice_app_icon_download_url);
        switch (status) {
            case 1:
                initVoice();//语音
                break;
            case 2:
                initSound();//音量
                break;
            case 3:
                initFM();//收音机
                break;
            case 4:
                voiceControlApp(1, getString(R.string.carmap));//导航
                break;
            case 5:
                voiceControlApp(0, getString(R.string.kuwo));//音乐
                break;
        }

    }

    private SharedPreferences.Editor editor = null;

    // 开启语音搜索
    @SuppressLint("HandlerLeak")
    private void doVoiceSearch() {
        // 用的是云之声新申请的key
        String key = "hj5wwyc6pcawkuk6up6rvnqkuhpinsmqfvhgxnql";
        USCRecognizerDialog recognizer = new USCRecognizerDialog(this, key);
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (TPUtil.isNetOkWithToast()) {
                    String key_word = (String) msg.obj;
                    key_word = key_word.trim();
                    if (key_word != null && key_word.length() > 1) {
                        if (key_word.contains("。")) {
                            key_word = key_word.substring(0, key_word.indexOf("。"));
                        }
                    }

                    if (key_word.startsWith(getString(R.string.driving2)) || key_word.startsWith(getString(R.string.map)) || key_word.startsWith(getString(R.string.navigation))) {
                        editor = sp.edit();
                        editor.putInt("checkLayout", 4);
                        editor.commit();//提交数据.
                        Intent intent = new Intent(TransPadApplication.getTransPadApplication(), MenuActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        voiceControlApp(1, getString(R.string.carmap));
                    } else if (key_word.startsWith(getString(R.string.music))) {
                        editor = sp.edit();
                        editor.putInt("checkLayout", 5);
                        editor.commit();//提交数据.
                        Intent intent = new Intent(TransPadApplication.getTransPadApplication(), MenuActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        voiceControlApp(0, getString(R.string.kuwo));
                    } else if (key_word.startsWith(getString(R.string.search))) {
                        int firstindex = key_word.indexOf(getString(R.string.search));
                        String new_key_word = key_word.substring(firstindex + 2);
                        TPUtil.openBrowser(MenuActivity.this, "http://www.baidu.com/s?wd=" + new_key_word);
                    } else {
                        Toast.makeText(MenuActivity.this, R.string.prompt, Toast.LENGTH_SHORT).show();
                    }

                }
            }
        };
        recognizer.setListener(new USCRecognizerDialogListener() {
            @Override
            public void onEnd(USCError arg0) {
                finish();
            }

            StringBuffer sb = new StringBuffer();

            @Override
            public void onResult(String arg0, boolean arg1) {
                sb.append(arg0);
                if (arg1) {
                    Message msg = new Message();
                    msg.obj = sb.toString();
                    handler.sendMessage(msg);
                }
            }
        });
        recognizer.show();
    }

    /**
     * 初始化语音的方法
     */
    public void initVoice() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_voice);
        mapConfig = sp.getString("openMap", "null");
        doVoiceSearch();
    }

    public AudioManager audiomanage;
    private int maxVolume, currentVolume;

    /**
     * 初始化音量调节的方法
     */

    public void initSound() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sound_page);
        final SeekBar soundBar = (SeekBar) findViewById(R.id.sb_volume_seekbar);  //音量设置
        RelativeLayout rl_sound = (RelativeLayout) findViewById(R.id.rl_sound);
        rl_sound.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                finish();
                return false;
            }
        });
        audiomanage = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        maxVolume = audiomanage.getStreamMaxVolume(AudioManager.STREAM_MUSIC);  //获取系统最大音量
        soundBar.setMax(maxVolume);   //拖动条最高值与系统最大声匹配
        currentVolume = audiomanage.getStreamVolume(AudioManager.STREAM_MUSIC);  //获取当前值
        soundBar.setProgress(currentVolume);
        soundBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() //调音监听器
        {
            public void onProgressChanged(SeekBar arg0, int progress, boolean fromUser) {
                audiomanage.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                currentVolume = audiomanage.getStreamVolume(AudioManager.STREAM_MUSIC);  //获取当前值
                soundBar.setProgress(currentVolume);
                handler.removeMessages(FINISH_ACTIVITY);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        finish();
                    }
                });
            }
        });
        Message msg = new Message();
        msg.what = FINISH_ACTIVITY;
        handler.sendMessageDelayed(msg, 3000);
    }

    private static final int FINISH_ACTIVITY = 1;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == FINISH_ACTIVITY) {
                finish();
            }
            super.handleMessage(msg);
        }
    };
    private FmPickerView picker_view1;
    private FmPickerView picker_view2;
    private Button bt_ok;
    public void initFM() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fm_page);
        picker_view1 = (FmPickerView) findViewById(R.id.picker_view1);
        picker_view2 = (FmPickerView) findViewById(R.id.picker_view2);
        bt_ok = (Button)findViewById(R.id.bt_ok);
        fmValue1 = sp.getString("time1", "00");
        fmValue2 = sp.getString("time2", "00");
        List<String> data = new ArrayList<String>();
        List<String> seconds = new ArrayList<String>();
        for (int i = 0; i < 10; i++)
        {
            data.add("0" + i);
        }
        for (int i = 0; i < 60; i++)
        {
            seconds.add(i < 10 ? "0" + i : "" + i);
        }
        picker_view1.setData(data);
        picker_view1.setOnSelectListener(new FmPickerView.onSelectListener() {

            @Override
            public void onSelect(String text) {
                editor = sp.edit();
                editor.putString("time1", text);
                editor.commit();
            }
        });
        picker_view2.setData(seconds);
        picker_view2.setOnSelectListener(new FmPickerView.onSelectListener()
        {

            @Override
            public void onSelect(String text)
            {
                editor = sp.edit();
                editor.putString("time2", text);
                editor.commit();
            }
        });
        picker_view1.setSelected(fmValue1);
        picker_view2.setSelected(fmValue2);
        bt_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fmValue1 = sp.getString("time1", "00");
                fmValue2 = sp.getString("time2", "00");
                Toast.makeText(MenuActivity.this,"您选择了"+fmValue1+"和"+fmValue2,Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * 检查应用是否存在，存在就安装打开，不存在就下载的方法
     *
     * @param postion
     * @param name
     */
    private void voiceControlApp(final int postion, final String name) {
        PackageInfo info = TPUtil.checkApkExist(MenuActivity.this, voiceAppPackageNames[postion]);
        if (info == null) {
            OfflineCache offlineCache = StorageModule.getInstance().getOfflineCacheById(ids[postion]);
            if (offlineCache != null) {
                switch (offlineCache.getCacheDownloadState()){
                    case OfflineCache.CACHE_STATE_FINISH:
                        TPUtil.installAPK(new File(offlineCache.getCacheStoragePath()), MenuActivity.this);
                        break;
                    case OfflineCache.CACHE_STATE_DOWNLOADING:
                        Toast.makeText(this, R.string.app_already_downlaod, Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    case OfflineCache.CACHE_STATE_PAUSE:
                        Toast.makeText(this, R.string.download_has_been_suspended, Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                }
                return;
            }
                //未安装
                mainPageDownloadDialog = new MainPageDownloadDialog(MenuActivity.this, R.style.myDialog);
                mainPageDownloadDialog.setMessage(String.format(MenuActivity.this.getString(R.string.home_download_dialog_message), name), voiceAppUrls[postion]);
                mainPageDownloadDialog.setClickListener(new MainPageDownloadDialog.ClickListener() {
                    @Override
                    public void onOk() {

                        if (TPUtil.isNetOkWithToast()) {
                            OfflineCache offlineCache = new OfflineCache();
                            offlineCache.setCacheName(name);
                            offlineCache.setCacheID(ids[postion]);
                            offlineCache.setCachePackageName(voiceAppPackageNames[postion]);
                            offlineCache.setCacheDetailUrl(voiceAppUrls[postion]);
                            offlineCache.setCacheImageUrl(voiceAppIconUrls[postion]);
                            StorageModule.getInstance().addCache(offlineCache);

                        }
                        mainPageDownloadDialog.dismiss();
                        finish();
                    }

                    @Override
                    public void onCancel() {
                        mainPageDownloadDialog.dismiss();
                        finish();
                    }
                });
                mainPageDownloadDialog.show();
//            }
        } else {
            TPUtil.startAppByPackegName(MenuActivity.this, voiceAppPackageNames[postion]);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
