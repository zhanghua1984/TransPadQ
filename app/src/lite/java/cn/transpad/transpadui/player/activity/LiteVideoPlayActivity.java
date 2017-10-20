package cn.transpad.transpadui.player.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.dlna.entity.DLNADevice;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.MultipleVideo;
import cn.transpad.transpadui.http.Reporter;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.http.XyzplaRst;
import cn.transpad.transpadui.player.AudioPlayer;
import cn.transpad.transpadui.player.IPlayerAdapter;
import cn.transpad.transpadui.player.VideoMode;
import cn.transpad.transpadui.player.adapter.FonePlayerAdapter;
import cn.transpad.transpadui.player.adapter.LiteFonePlayerAdapter;
import cn.transpad.transpadui.player.gesture.FoneOnGesture;
import cn.transpad.transpadui.service.TransPadService;
import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.PlayerUtil;
import cn.transpad.transpadui.util.ScreenUtil;
import cn.transpad.transpadui.util.TPUtil;
import cn.transpad.transpadui.view.ColorBallProgressView;
import cn.transpad.transpadui.view.DLNADialog;
import cn.transpad.transpadui.view.FoneGestureOverlayView;
import cn.transpad.transpadui.view.VideoDefinitionPopupWindow;
import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LiteVideoPlayActivity extends Activity {

    @InjectView(R.id.video_player_seekbar)
    SeekBar mSeekBar;
    @InjectView(R.id.videoplayer_PlayPause)
    ImageButton playPause;
    @InjectView(R.id.videoplayer_next)
    ImageButton nextButton;
    @InjectView(R.id.videoplayer_sv)
    SurfaceView playerView;
    private Context mContext = null;
    private static final String TAG = "LiteVideoPlayActivity";
    @InjectView(R.id.videoplayer_title_bar)
    LinearLayout titleBarLayout;
    @InjectView(R.id.videoplayer_cotroll_layout)
    RelativeLayout controlLayout;
    @InjectView(R.id.videoplayer_title)
    TextView videoTitle;
    @InjectView(R.id.videoplayer_progress_text)
    TextView videoProgress;
    @InjectView(R.id.videoplayer_duration_text)
    TextView videoDuration;
    @InjectView(R.id.loading_view)
    ColorBallProgressView loadingView;
    /**
     * 清晰度
     */
    @InjectView(R.id.videoplayer_definition_text)
    TextView videoDefinition;
    @InjectView(R.id.videoplayer_definition_layout)
    LinearLayout videoDefinitionLayout;
    private Handler mHandler;

    private static final int HIDE_CONTROL_LAYOUT = 1;
    private static final int UPDATE_PROGRESS = 2;
    /**
     * 清晰度保存的key
     */
    private static final String DEFINITION_KEY = "definition";
    private String xyzplay;
    /**
     * 播放进度
     */
    private int progress;
    /**
     * 清晰度选择框
     */
    private VideoDefinitionPopupWindow definitionPopupWindow;

    /**
     * 捕获手势View
     */
    @InjectView(R.id.full_surface_gesture)
    FoneGestureOverlayView mPlayerGestureView;
    /**
     * 是否正在手势更改音量/亮度
     */
    private boolean gestureChangeVolOrBright;
    /**
     * 是否正在手势调节进度
     */
    private boolean gestureChangeProgress;
    /**
     * 音量百分比
     */
    private int volPercentage;
    /**
     * 最大音量，当前音量
     */
    private int maxVolume, currentVolume;
    /**
     * 显示音量调节/亮度/进度 layout
     */
    @InjectView(R.id.full_player_gensture_layout)
    View genstureLayout;
    /**
     * 音量/亮度layout
     */
    @InjectView(R.id.video_full_vol_layout)
    View soundBrightLayout;
    /**
     * 音量/亮度icon
     */
    @InjectView(R.id.video_full_vol_icon)
    ImageView ivSoundBright;
    /**
     * 音量/亮度值
     */
    @InjectView(R.id.video_full_vol_text)
    TextView tvSoundBright;
    /**
     * 进度值
     */
    @InjectView(R.id.video_full_postion_text)
    TextView tvGestureProgress;
    /**
     * 当前屏幕亮度
     */
    private float currentBrightness;
    /**
     * 音量管理器
     */
    private AudioManager audiomanage;
    /**
     * 视频时长，毫秒
     */
    private int duration;

    /**
     * activity 是否停止
     */
    private boolean stop = false;

    /**
     * 播放控制
     */
    private LiteFonePlayerAdapter playerAdapter;

    private long lastTrackingTime;
    private long backClickTiem;
    private boolean buffering;
    private long lastClickPlayBtTime;
    private boolean hasNewIntent;
    private boolean surfaceCreated;
    @InjectView(R.id.bt_dlna)
    LinearLayout dlnaButton;
    @InjectView(R.id.dlna_layout)
    RelativeLayout dlnaLayout;
    @InjectView(R.id.dlna_opening_layout)
    RelativeLayout dlnaOpeningLayout;
    @InjectView(R.id.dlna_open_control_layout)
    LinearLayout dlnaOpeningControlLayout;
    @InjectView(R.id.dlna_play_device)
    TextView dlnaDeviceName;
    @InjectView(R.id.dlna_open_message)
    TextView dlnaOpenMessage;
    @InjectView(R.id.dlna_open_state)
    ImageView dlnaOpenStateIv;

    protected void onCreate(Bundle savedInstanceState) {
        L.v(TAG, "onCreate");
        mContext = this;
        super.onCreate(savedInstanceState);
        AudioPlayer.getInstance().stopAduioService();
//        hideNavigation();
        setContentView(R.layout.video_play);
        ButterKnife.inject(this);
        playerView.getHolder().addCallback(new SurfaceViewCallback()); // 监听SurfaceView的摧毁和重构事件
        initPlayerAdapter();
        EventBus.getDefault().register(this);
        initHandler();
        handleIntent(getIntent());
        keepScreenOn();
        initAudioAndBrightness();
        initPlayerGestureListener();
        mHandler.sendEmptyMessageDelayed(HIDE_CONTROL_LAYOUT, 5000);
        initTelephoneListener();
        fullScreen();
        showControlView();
    }

    private void fullScreen() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(lp);
    }

    /**
     * 初始化电话状态监听
     */
    private void initTelephoneListener() {
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        manager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * 初始化播放控制
     */
    private void initPlayerAdapter() {
        playerAdapter = new LiteFonePlayerAdapter(playerView) {
            @Override
            protected void sendMessage2UI(Message msg) {
                if (mHandler != null) {
                    L.v(TAG, "receiver message what = " + msg.what);
                    mHandler.sendMessage(msg);
                }
            }
        };
    }

    /**
     * 初始化音量和亮度
     *
     * @return void
     * @throws
     */
    private void initAudioAndBrightness() {
        audiomanage = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audiomanage.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = audiomanage.getStreamVolume(AudioManager.STREAM_MUSIC);
        volPercentage = (int) (((double) currentVolume / (double) maxVolume) * 100);
        try {
//            if (TransPadService.isConnected() && TransPadService.getConnectDeviceMaxBrightness() > 0 && TransPadService.getConnectDeviceType() == TransPadService.TRANSPAD_DEVICE_TYPE_AUTO) {
//                currentBrightness = (float) TransPadService.getConnectDeviceBrightness() / (float) TransPadService.getConnectDeviceMaxBrightness();
//            } else {
            currentBrightness = (float) Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS) / 255f;
//            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化Handler
     */
    @SuppressLint("HandlerLeak")
    private void initHandler() {
        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case HIDE_CONTROL_LAYOUT://隐藏控制栏
                        if (titleBarLayout.getVisibility() == View.VISIBLE) {
                            toggle();
                        }
                        break;
                    case UPDATE_PROGRESS://更新seekbar进度
                        updateProgress();
                        sendEmptyMessageDelayed(UPDATE_PROGRESS, 500);
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_PLAYER_PREPARED:// 播放器准备就绪
                        mHandler.removeCallbacks(hwPlusRunable);
                        if (stop) {
                            playerAdapter.stop();
                            break;
                        }
                        hideLoadingView();
                        updateMediaDuring();
                        // 启动新线程, 控制进度条
                        mHandler.removeMessages(UPDATE_PROGRESS);
                        mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                        mSeekBar.setOnSeekBarChangeListener(new FoneOnSeekBarChangeListener()); // 监听进度条拖动事件
                        playPause.setBackgroundResource(R.drawable.player_pause);
                        sendEmptyMessageDelayed(HIDE_CONTROL_LAYOUT, 5000);
                        if (playerAdapter.getVideoMode() == VideoMode.LOCAL) {
                            videoTitle.setText(playerAdapter.getVideoName());
                        }
                        // 取消那些网络提示
                        if (mHandler
                                .hasMessages(IPlayerAdapter.FONE_PLAYER_MSG_PLAY_TIMEOUT)) {
                            mHandler.removeMessages(IPlayerAdapter.FONE_PLAYER_MSG_PLAY_TIMEOUT);
                        }
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_PLAYER_PLAYING_PAUSE:// 暂停或者播放
                        updatePlayButton();
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_PLAYER_PREPARING:// 准备中
                        showLoadingView();
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_PLAYER_PLAY_COMPLETION:// 播放完成
                        L.v(TAG, "player play completion!");
                        // 隐藏SurfaceView
                        if (playerView.getVisibility() == View.VISIBLE) {
                            playerView.setVisibility(View.INVISIBLE);
                        }
                        if (definitionPopupWindow != null && definitionPopupWindow.isShowing()) {
                            definitionPopupWindow.dismiss();
                        }
                        FonePlayerAdapter.setHwPlusSupport(0);
                        // 隐藏手势框
                        genstureLayout.setVisibility(View.GONE);
                        if (playerAdapter == null) {
                            break;
                        }
                        playPause.setBackgroundResource(R.drawable.player_play);
                        mHandler.removeMessages(HIDE_CONTROL_LAYOUT);
                        mHandler.sendEmptyMessageDelayed(HIDE_CONTROL_LAYOUT, 5000);
                        if (playerAdapter.getVideoMode() != VideoMode.LOCAL) {
                            if (!TPUtil.isNetOk()) {
                                hideLoadingView();
                                return;
                            }
                        }
                        boolean hasNext = playerAdapter.hasNext();
                        L.v(TAG, "player has next == " + hasNext);
                        if (!hasNext) {
                            if (playerAdapter.getVideoMode() == VideoMode.LOCAL) {
                                finish();
                            } else {
                                hideLoadingView();
                                onBackPressed();
                            }
                        } else {
                            playerAdapter.next();
                        }
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_VIDEO_GET_FAILURE:// 视频获取错误
                        hideLoadingView();
                        finish();
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_PLAYER_UPDATE_SURFACEVIEW:// 设置SurfaceView宽高
                        setSurfaceWidthHeight(msg.arg1, msg.arg2);
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_PLAYER_SERIES_NEXT:// 剧集下一步
                        XyzplaRst plaRst = playerAdapter.getPlaRst();
                        requestXyzplay(plaRst.nexturl);
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_RESUME_PLAY_PROGRESS:// 恢复播放进度成功
                        L.v(TAG, "FONE_PLAYER_MSG_RESUME_PLAY_PROGRESS progress = "
                                + msg.arg1);
                        progress = msg.arg1;
                        mSeekBar.setMax(msg.arg2);
                        mSeekBar.setProgress(msg.arg1);
                        videoProgress.setText(PlayerUtil
                                .second2HourStr(msg.arg1 / 1000));
                        videoDuration.setText("/" + PlayerUtil
                                .second2HourStr(msg.arg2 / 1000));
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_PLAY_TIMEOUT:// 超时，请返回重试
                        Toast.makeText(LiteVideoPlayActivity.this,
                                R.string.fullplayer_media_buffer_timeout,
                                Toast.LENGTH_LONG).show();
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_PLAYER_OPEN_FAILED:// 底层播放器打开失败
                        L.v(TAG, "player open failed!");
                        // 如果已经Activity
                        // 已经关闭了就不处理这个消息了，底层现在有一个bug，当省电加速播放状态退出播放时会发这个消息过来，所以暂时这样处理
                        if (stop) {
                            return;
                        }
                        playerView.setVisibility(View.INVISIBLE);
                        if (msg.arg1 == 0) {
                            L.v(TAG, "hardware plus open faild !");
                            // 省电加速打开失败
                            if (playerAdapter != null) {
                                Toast.makeText(LiteVideoPlayActivity.this,
                                        R.string.play_url_null,
                                        Toast.LENGTH_SHORT).show();
                                playerAdapter.release();
                                playerAdapter.play();
                            }
                            break;
                        }
                        hideLoadingView();
                        playerAdapter.stop();
                        playerAdapter.release();
                        playerAdapter.updatePlayRecord2MediaInfo();
                        if (playerAdapter != null
                                && playerAdapter.getVideoMode() != VideoMode.LOCAL
                                && !TPUtil
                                .isNetOkWithToast()) {
                            // 没有网络
                            mSeekBar.setProgress(0);
                            break;
                        }
                        Toast.makeText(LiteVideoPlayActivity.this,
                                R.string.player_open_failed, Toast.LENGTH_SHORT)
                                .show();
                        finish();
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_BUFFERING_START://开始缓存
                        showLoadingView();
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_VIDEO_OUT_LINE_TOAST:// 视频暂时无法播放
                        Toast.makeText(LiteVideoPlayActivity.this,
                                R.string.play_url_null, Toast.LENGTH_SHORT)
                                .show();
                        hideLoadingView();
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_PLAYER_OPEN_SUCCESS:// 播放器打开成功
                        updateMediaDuring();
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_PLAYER_SERIES_PREVIOUS://打开在线视频上一集视频
                        requestXyzplay(playerAdapter.getPlaRst().provurl);
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_DLNA_OPEN_SUCCED://DLNA打开成功
                        dlnaOpeningLayout.setVisibility(View.GONE);
                        dlnaLayout.setVisibility(View.VISIBLE);
                        dlnaStopButton.setVisibility(View.VISIBLE);
                        dlnaDeviceName.setText(playerAdapter.getDlnaDeviceName());
                        showControlView();
                        mHandler.removeMessages(HIDE_CONTROL_LAYOUT);
                        updateMediaDuring();
                        // 启动新线程, 控制进度条
                        mHandler.removeMessages(UPDATE_PROGRESS);
                        mHandler.sendEmptyMessage(UPDATE_PROGRESS);
                        mSeekBar.setOnSeekBarChangeListener(new FoneOnSeekBarChangeListener()); // 监听进度条拖动事件
                        playPause.setBackgroundResource(R.drawable.player_pause);
                        if (playerAdapter.getVideoMode() == VideoMode.LOCAL) {
                            videoTitle.setText(playerAdapter.getVideoName());
                        }
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_DLNA_OPEN_FAILED://DLNA打开失败
                        dlnaOpeningControlLayout.setVisibility(View.VISIBLE);
                        dlnaOpenMessage.setText(R.string.dlna_opening_failed);
                        dlnaOpenStateIv.setImageResource(R.drawable.dlna_open_faild);
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_DLNA_OPENING://DLNA正在打开
                        dlnaOpeningControlLayout.setVisibility(View.GONE);
                        playerView.setVisibility(View.INVISIBLE);
                        hideControlView();
                        hideLoadingView();
                        titleBarLayout.setVisibility(View.VISIBLE);
                        dlnaOpeningLayout.setVisibility(View.VISIBLE);
                        dlnaOpenMessage.setText(R.string.dlna_opening_message);
                        dlnaOpenStateIv.setImageResource(R.drawable.dlna_opening);
                        if (playerAdapter.getVideoMode() == VideoMode.LOCAL) {
                            videoTitle.setText(playerAdapter.getVideoName());
                        }
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_DLNA_PLAY_COMPLETION://DLNA播放完成
                        hasNext = playerAdapter.hasNext();
                        L.v(TAG, "player has next == " + hasNext);
                        if (!hasNext) {
                            if (playerAdapter.getVideoMode() == VideoMode.LOCAL) {
                                finish();
                            }
                        } else {
                            playerAdapter.next();
                        }
                        break;
                    case IPlayerAdapter.FONE_PLAYER_MSG_DLNA_DEVICE_DISCONNECT://DLNA设备断开
                        dlnaCancel();
                        break;
                    default:
                        break;
                }
            }

        };
    }

    private Runnable hwPlusRunable = new Runnable() {

        @Override
        public void run() {
            // 停止播放
            playerAdapter.stop();
            playerAdapter.release();
            playerView.setVisibility(View.INVISIBLE);
            playerAdapter.play();
        }
    };

    /**
     * 进度条更新线程
     *
     * @author kongxiaojun
     * @since 2014-4-16
     */
    private void updateProgress() {
        try {
            if (playerAdapter != null) {
                L.v(TAG, "updateProgress");
                // 解决有声音无画面的bug
                if (playerView != null && playerView.getVisibility() != View.VISIBLE) {
                    playerView.setVisibility(View.VISIBLE);
                }
                if (playerAdapter.isPlaying()
                        && !gestureChangeProgress) {
                    progress = playerAdapter.getCurrentPosition();
                    if (progress > playerAdapter.getMediaDuration()) {
                        progress = playerAdapter.getMediaDuration();
                    }
                    if (progress > 0) {
                        L.v(TAG, "updateProgress update postion = "
                                + progress);
                        mSeekBar.setProgress(progress); // 设置进度条的进度为当前播放进度
                        if (!mSeekBar.isEnabled()) {
                            mSeekBar.setEnabled(true);
                        }
                        videoProgress.setText(PlayerUtil.second2HourStr(Math
                                .round((float) progress / 1000f)));

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleIntent(Intent intent) {
        L.v(TAG, "handleIntent");
        List<MultipleVideo> multipleVideos = intent.getParcelableArrayListExtra("playlist");
        if (multipleVideos != null && multipleVideos.size() > 0) {
            L.v(TAG, "multipleVideos != null");
            Reporter.logLocalPlay(5);
            Reporter.ordinaryStart();
            int position = intent.getIntExtra("playindex", 0);
            playerAdapter.play(position, multipleVideos);
        } else {
            L.v(TAG, "multipleVideos == null");
            //检查是不是100TV的播放地址
            String playurl = intent.getStringExtra("xyzplay");
            if (!TextUtils.isEmpty(playurl)) {
                requestXyzplay(playurl, SharedPreferenceModule.getInstance().getString(DEFINITION_KEY, "1"));
                videoDefinitionLayout.setVisibility(View.VISIBLE);
            } else {
                Uri uriPath = intent.getData();
                try {
                    if (null != uriPath) {
                        String scheme = uriPath.getScheme();
                        String url = null;
                        if (null != scheme) {
                            url = URLDecoder.decode(uriPath.toString(), "utf-8");
                        } else {
                            url = URLDecoder.decode(uriPath.getPath(), "utf-8");
                        }
                        if (TextUtils.isEmpty(url)) {
                            Toast.makeText(LiteVideoPlayActivity.this, R.string.play_url_null, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {

                            if (url.startsWith("content://")) {//处理uri
                                url = PlayerUtil.getFilePathByMediaUri(url);
                            }

                            MultipleVideo multipleVideo = new MultipleVideo();
                            multipleVideo.setUrls(new String[]{url});
                            File file = new File(url);
                            multipleVideo.setName(file.getName());
                            multipleVideos = new ArrayList<>();
                            multipleVideos.add(multipleVideo);
                            playerAdapter.play(0, multipleVideos);
                        }
                    } else {
                        Toast.makeText(LiteVideoPlayActivity.this, R.string.play_url_null, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void requestXyzplay(String absoluteUrl, String dfnt) {
        xyzplay = absoluteUrl;
        if (!TextUtils.isEmpty(dfnt)) {
            absoluteUrl = absoluteUrl + "&dfnt=" + dfnt;
        }
        requestXyzplay(absoluteUrl);
    }

    private void requestXyzplay(final String absoluteUrl) {
        L.v(TAG, "requestXyzplay");
        showLoadingView();
        xyzplay = absoluteUrl;
        Request.getInstance().xyzplay(TPUtil.handleUrl(absoluteUrl), "1", "62", new Callback<XyzplaRst>() {
            @Override
            public void success(XyzplaRst xyzplaRst, Response response) {

                L.v(TAG, "xyz success = " + xyzplaRst);
                if (xyzplaRst.result == 0 && xyzplaRst.cnt.fraglist.fragList != null && xyzplaRst.cnt.fraglist.fragList.size() > 0) {
                    videoTitle.setText(xyzplaRst.cnt.name);
                    if (xyzplaRst.cnt.toply == 0 || xyzplaRst.cnt.btnply == 1) {
                        int[] fragDurationList = new int[xyzplaRst.cnt.fraglist.fragList.size()];
                        String[] fragUrlList = new String[xyzplaRst.cnt.fraglist.fragList.size()];

                        for (int i = 0; i < xyzplaRst.cnt.fraglist.fragList.size(); i++) {
                            XyzplaRst.Frag frag = xyzplaRst.cnt.fraglist.fragList.get(i);
                            fragDurationList[i] = frag.t;
                            fragUrlList[i] = frag.url;
                        }
                        if (fragUrlList != null) {
                            playerAdapter.setPlayerRst(xyzplaRst, absoluteUrl);
                            if (xyzplaRst.cnt.dfnts != null
                                    && xyzplaRst.cnt.dfnts.dfntList != null) {
                                // 显示清晰度
                                for (XyzplaRst.Dfnt dfnt : xyzplaRst.cnt.dfnts.dfntList) {
                                    if (dfnt.cur == 1) {// 表示是当前播放的视频清晰度
                                        updateDefinition(dfnt.t);
                                    }
                                }
                            } else {
                                videoDefinitionLayout.setVisibility(View.INVISIBLE);
                            }
                            return;
                        }
                    }
                }
                Toast.makeText(LiteVideoPlayActivity.this, R.string.play_url_null, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(RetrofitError error) {
                L.v(TAG, "xyz failure = " + error);
                Toast.makeText(LiteVideoPlayActivity.this, R.string.play_url_null, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        L.v(TAG, "onRestart");
        if (!hasNewIntent) {
            if (playerAdapter != null && !playerAdapter.isDlnaMode()) {
                L.v(TAG, "onRestart hasNewIntent == false");
                if (playerAdapter.getVideoMode() != VideoMode.LOCAL
                        && playerAdapter.getPlaRst() == null) {
                    if (!TextUtils.isEmpty(xyzplay)) {
                        requestXyzplay(xyzplay);
                        return;
                    }
                }
                if (!playerAdapter.isOpenSuccess() && playerAdapter.isCloseSuccess) {
                    playerAdapter.updatePlayRecord2MediaInfo();
                    playerAdapter.play();
                }
            }
        } else {
            L.v(TAG, "onRestart hasNewIntent == true");
            hasNewIntent = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        L.v(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        hasNewIntent = false;
        stop = false;
        if (playerAdapter != null && !playerAdapter.isDlnaMode()) {
            if (titleBarLayout.getVisibility() != View.VISIBLE) {
                toggle();
            } else {
                mHandler.sendEmptyMessageDelayed(HIDE_CONTROL_LAYOUT, 5000);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        L.v(TAG, "onNewIntent");
        hasNewIntent = true;
        playerAdapter.stop();
        playerAdapter.release();
        playerView.setVisibility(View.INVISIBLE);
        handleIntent(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        L.v(TAG, "onConfigurationChanged newConfig = " + newConfig);
//        if (playerView != null && newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {//此app仅支持横屏播放所以当强制切到竖屏是关闭播放器
//            playerView.setVisibility(View.INVISIBLE);
//        } else if (playerView != null && newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {//切回横屏时显示playerView会自动恢复播放
//            playerView.setVisibility(View.VISIBLE);
//        }
    }

    /**
     * 保持屏幕常亮
     *
     * @return void
     * @throws
     */
    private void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void toggle() {
        if (controlLayout.getVisibility() != View.VISIBLE) {
            showControlView();
        } else {
            hideControlView();
        }
    }

    public void hideControlView() {
        mHandler.removeMessages(HIDE_CONTROL_LAYOUT);
        titleBarLayout.setVisibility(View.INVISIBLE);
        controlLayout.setVisibility(View.INVISIBLE);
        dlnaButton.setVisibility(View.INVISIBLE);
        if (definitionPopupWindow != null && definitionPopupWindow.isShowing()) {
            definitionPopupWindow.dismiss();
        }
    }

    public void showControlView() {
        mHandler.removeMessages(HIDE_CONTROL_LAYOUT);
        mHandler.sendEmptyMessageDelayed(HIDE_CONTROL_LAYOUT, 5000);
        titleBarLayout.setVisibility(View.VISIBLE);
        controlLayout.setVisibility(View.VISIBLE);
        dlnaButton.setVisibility(View.VISIBLE);
    }

    private DLNADialog dlnaDialog;

    @OnClick(R.id.bt_dlna)
    public void dlnaButtonClick() {
        if (dlnaDialog == null) {
            dlnaDialog = new DLNADialog(this, R.style.myDialog);
            dlnaDialog.setOnButtonClickListener(new DLNADialog.OnButtonClickListener() {
                @Override
                public void onButtonClick(DLNADevice device, boolean isCancel) {
                    if (!isCancel) {
                        playerAdapter.dlnaOpen(device);
                    }
                    dlnaDialog.dismiss();
                }
            });
        }
        dlnaDialog.show();
    }

    @OnClick(R.id.videoplayer_PlayPause)
    public void playClick() {
        L.v(TAG, "play");
        if (playerAdapter != null && !playerAdapter.isDlnaMode()) {
            mHandler.removeMessages(HIDE_CONTROL_LAYOUT);
            mHandler.sendEmptyMessageDelayed(HIDE_CONTROL_LAYOUT, 5000);
        }
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            //SD卡弹出了
            Toast.makeText(this, R.string.sdcard_state_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (playerAdapter.getVideoMode() != VideoMode.LOCAL && !TPUtil.isNetOkWithToast()) {
            return;
        }
        playPause.setBackgroundResource(playerAdapter.isPlaying() ? R.drawable.player_pause : R.drawable.player_play);

        if (buffering || isOpening()) {
            // 正在缓冲中直接返回不进行处理
            return;
        }
        if (System.currentTimeMillis() - lastClickPlayBtTime < 1000) {
            // 1000毫秒内多次点击播放暂停无效
            return;
        }
        lastClickPlayBtTime = System.currentTimeMillis();
        play();
    }

    /**
     * 播放
     */
    public void play() {
        L.v(TAG, "play");
        if (playerAdapter == null) {
            initPlayerAdapter();
        }
        if (playerAdapter.isPlaying()) {
            playerAdapter.pause();
        } else {
            playerAdapter.play();
        }
    }

    @OnClick(R.id.videoplayer_previous)
    public void previous() {
        L.v(TAG, "previous");
        if (playerAdapter != null && playerAdapter.isDlnaMode()) {
            mHandler.removeMessages(HIDE_CONTROL_LAYOUT);
            mHandler.sendEmptyMessageDelayed(HIDE_CONTROL_LAYOUT, 5000);
        }
        if (playerAdapter.hasPrevious()) {
            playerAdapter.stop();
            playerAdapter.release();
            playerView.setVisibility(View.INVISIBLE);
            playerAdapter.previous();
        } else {
            Toast.makeText(this, R.string.has_play_to_first_video, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.videoplayer_next)
    public void next() {
        L.v(TAG, "next");
        if (playerAdapter != null && playerAdapter.isDlnaMode()) {
            mHandler.removeMessages(HIDE_CONTROL_LAYOUT);
            mHandler.sendEmptyMessageDelayed(HIDE_CONTROL_LAYOUT, 5000);
        }
        if (playerAdapter.hasNext()) {
            playerAdapter.stop();
            playerAdapter.release();
            playerView.setVisibility(View.INVISIBLE);
            playerAdapter.next();
            mSeekBar.setProgress(0);
        } else {
            Toast.makeText(this, R.string.has_play_to_last_video, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.videoplayer_back)
    public void back() {
        onBackPressed();
    }

    @OnClick(R.id.videoplayer_definition_layout)
    public void showDefinition() {
        if (playerAdapter != null && playerAdapter.getPlaRst() != null && playerAdapter.getPlaRst().cnt.dfnts != null) {
            if (definitionPopupWindow == null) {
                //todo:0代表流畅，看以后需不需要加
                List<XyzplaRst.Dfnt> dfntList = playerAdapter.getPlaRst().cnt.dfnts.dfntList;
                for (int i = 0; i < dfntList.size(); i++) {
                    switch (dfntList.get(i).t) {
                        case 1:
                        case 2:
                        case 3:
                            break;
                        default:
                            dfntList.remove(i);
                            break;
                    }
                }
                definitionPopupWindow = new VideoDefinitionPopupWindow(mContext,
                        dfntList,
                        new VideoDefinitionPopupWindow.ItemClickCallBack() {
                            @Override
                            public void onItemclick(XyzplaRst.Dfnt dfnt) {
                                // 切换清晰度
                                if (playerAdapter.isPlaying() && dfnt.cur == 0) {
                                    playerAdapter.savePlayRecord();
                                    playerAdapter.stop();
                                    playerAdapter.release();
                                    playerView
                                            .setVisibility(View.INVISIBLE);
                                    requestXyzplay(dfnt.url);
                                    // 保存当前清晰度
                                    SharedPreferenceModule.getInstance()
                                            .setString(DEFINITION_KEY,
                                                    dfnt.t + "");
                                    // 更新清晰度背景
                                    updateDefinition(dfnt.t);
                                    if (definitionPopupWindow != null) {
                                        definitionPopupWindow.dismiss();
                                    }
                                }
                            }

                        });
            } else {
                definitionPopupWindow
                        .setDfntList(playerAdapter.getPlaRst().cnt.dfnts.dfntList);
            }
            if (definitionPopupWindow.isShowing()) {
                definitionPopupWindow.dismiss();
            } else {
                definitionPopupWindow.show(videoDefinitionLayout);
                mHandler.removeMessages(HIDE_CONTROL_LAYOUT);
                mHandler.sendEmptyMessageDelayed(HIDE_CONTROL_LAYOUT, 5000);
            }
        }
    }

    private void updateDefinition(int t) {
        switch (t) {
            case 1:
                // 标清
                videoDefinition.setText(R.string.definition_normal);
                break;
            case 2:
                // 高清
                videoDefinition.setText(R.string.definition_high);
                break;
            case 3:
                // 超清
                videoDefinition.setText(R.string.definition_super);
                break;
        }
    }

    /**
     * SurfaceView状态改变的回调类
     *
     * @author kongxiaojun
     * @since 2014-4-18
     */
    private class SurfaceViewCallback implements SurfaceHolder.Callback {
        public void surfaceDestroyed(SurfaceHolder holder) { // 被切换到后台时, 自动摧毁
            L.v(TAG, "surfaceDestroyed");
            surfaceCreated = false;
            if (playerAdapter != null && !playerAdapter.isSeeking()
                    && !playerAdapter.isSystemPlayerOpenFailed()
                    && playerAdapter.isOpenSuccess()) {
                L.v(TAG, "wwb_message  call stop and release");
                playerAdapter.stop();
                playerAdapter.release();
            } else if (playerAdapter != null
                    && playerAdapter.isSystemPlayerOpenFailed()) {
                playerAdapter.setSystemPlayerOpenFailed(false);
            }
            playPause.setBackgroundResource(R.drawable.player_play);
        }

        public void surfaceCreated(SurfaceHolder holder) { // 从后台切换到前台时, 重新创建
            L.v(TAG, "surfaceCreated");
            surfaceCreated = true;
            if (playerAdapter != null) {
                playerAdapter.setSurfaceView(holder);
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }
    }

    @Override
    protected void onStop() {
        L.v(TAG, "onStop");
        stop = true;
        if (playerAdapter != null && !playerAdapter.isDlnaMode() && titleBarLayout.getVisibility() == View.VISIBLE) {
            toggle();
        }
        if (definitionPopupWindow != null && definitionPopupWindow.isShowing()) {
            definitionPopupWindow.dismiss();
        }
        hideLoadingView();
        if (playerAdapter != null && !playerAdapter.isDlnaMode()){
            if (playerAdapter.isOpenSuccess()) {
                playerAdapter.stop();
                playerAdapter.release();
                playerAdapter.updatePlayRecord2MediaInfo();
                playerView.setVisibility(View.INVISIBLE);
                mHandler.removeMessages(HIDE_CONTROL_LAYOUT);
                playPause.setBackgroundResource(R.drawable.player_play);
            }
            mHandler.removeMessages(UPDATE_PROGRESS);
        }
        super.onStop();
    }

    public void onEventMainThread(Message message) {
        switch (message.what) {
            case TransPadService.TRANSPAD_STATE_DISCONNECTED:
                finish();
                break;
            case StorageModule.MSG_NO_NETWORK_TYPE:
                TPUtil.isNetOkWithToast();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        L.v(TAG, "onDestroy");
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (playerAdapter != null) {
            playerAdapter.exitDlnaMode();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void showLoadingView() {
        if (playerAdapter != null && playerAdapter.getVideoMode() != VideoMode.LOCAL) {
            loadingView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示缓冲圆形进度条
     *
     * @param isAirone 是否airone进度条
     */
    private void showLoadingView(boolean isAirone, int percent) {
        if (!isAirone && playerAdapter != null
                && playerAdapter.getVideoMode() == VideoMode.LOCAL) {
            return;
        }
        if (genstureLayout.getVisibility() == View.VISIBLE) {
            genstureLayout.setVisibility(View.GONE);
        }
        buffering = true;
        if (loadingView.getVisibility() != View.VISIBLE) {
            loadingView.setVisibility(View.VISIBLE);
        }
        // setSeekBarEnable(!buffering);
    }

    private void hideLoadingView() {
        loadingView.setVisibility(View.GONE);
    }

    /**
     * 初始化手势监听
     */
    private void initPlayerGestureListener() {

        L.v(TAG, "initPlayerGestureListener", "start");
        FoneOnGesture foneOnGesture = new FoneOnGesture();
        foneOnGesture.setFoneOnGestureListener(new FoneOnGesture.FoneOnGestureListener() {

            @Override
            public void FoneOnGestureStart() {
                L.v(TAG, "initPlayerGestureListener", "FoneOnGestureStart");
                mHandler.removeCallbacks(hideGenstureLayoutRunnable);
            }

            int width;

            @Override
            public void FoneOnGestureMoveUPOrDown(boolean isEnableSeek, float distance, float start_x, boolean two_pointer) {
                L.v(TAG, "initPlayerGestureListener", "FoneOnGestureMoveUPOrDown");
                L.v(TAG, "initPlayerGestureListener", "isEnableSeek=" + isEnableSeek + "  distance=" + distance + "  start_x=" + start_x + "  two_pointer=" + two_pointer);
                if (width == 0) {
                    width = ScreenUtil.getScreenWidthPix(LiteVideoPlayActivity.this);
                }
                gestureChangeVolOrBright = true;
                // 修改音量/亮度
                int status = (width / 2 < start_x) ? 2 : 1;
                if (status == 1) {
                    //音量是否被其它途径修改了，如果修改了就更新音量数据
                    currentVolume = audiomanage.getStreamVolume(AudioManager.STREAM_MUSIC);
                    int oldCurrentVolume = (int) (((double) volPercentage / 100) * maxVolume);
                    if (oldCurrentVolume != currentVolume) {
                        volPercentage = (int) (((double) currentVolume / (double) maxVolume) * 100);
                    }
                    // 修改音量
                    int addVol = (int) (distance / (ScreenUtil.getScreenHeightPix(LiteVideoPlayActivity.this) / 50));
                    volPercentage -= addVol;
                    currentVolume = (int) (((double) volPercentage / 100) * maxVolume);
                    if (volPercentage >= 100) {
                        volPercentage = 100;
                        currentVolume = maxVolume;
                    }
                    if (volPercentage <= 0) {
                        volPercentage = 0;
                        currentVolume = 0;
                    }
                    updateVolume();
                } else {
                    // 修改亮度
                    float addBright = distance / (float) (ScreenUtil.getScreenHeightPix(LiteVideoPlayActivity.this) * 10);
                    currentBrightness -= addBright;
                    if (currentBrightness >= 1) {
                        currentBrightness = 1;
                    }
                    if (currentBrightness <= 0) {
                        currentBrightness = 0;
                    }
                    updateBright();
                }
            }

            @Override
            public void FoneOnGestureMovePrevious() {
                L.v(TAG, "initPlayerGestureListener", "FoneOnGestureMovePrevious");
                // onPlayerPreviousAction();
            }

            @Override
            public void FoneOnGestureMoveNext() {
                L.v(TAG, "initPlayerGestureListener", "FoneOnGestureMoveNext");
                // onPlayerNextAction();
            }

            int currentProgress;

            @Override
            public void FoneOnGestureMoveLeftOrRight(boolean isEnableSeek, float eventY, float distance) {
                try {
                    if (playerAdapter != null && playerAdapter.getMediaDuration() > 0) {
                        int videoDruation = playerAdapter.getMediaDuration();
                        if (videoDruation > 0) {
                            // 调节进度
                            gestureChangeProgress = true;
                            currentProgress = playerAdapter.getCurrentPosition();
                            L.v(TAG, "FoneOnGestureMoveLeftOrRight currentProgress start : " + currentProgress);
                            // 每滑动10像素加1秒
                            currentProgress -= distance / 10 * 1000;
                            L.v(TAG, "FoneOnGestureMoveLeftOrRight currentProgress computed : " + currentProgress);
                            if (currentProgress <= 0) {
                                currentProgress = 0;
                            }
                            if (currentProgress >= videoDruation) {
                                currentProgress = videoDruation;
                            }
                            L.v(TAG, "FoneOnGestureMoveLeftOrRight currentProgress compute end : " + currentProgress);
                            L.v(TAG, "FoneOnGestureMoveLeftOrRight videoDruation : " + videoDruation);
                            gestureUpdateProgress(currentProgress);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void FoneOnGestureEnd(boolean isEnableSeek, boolean isLeftOrRight) {
                L.v(TAG, "initPlayerGestureListener", "FoneOnGestureEnd isEnableSeek: " + isEnableSeek + " ,isLeftOrRight:" + isLeftOrRight);
                if (!gestureChangeVolOrBright && !gestureChangeProgress) {
                    // 单击
                    if (playerAdapter != null && !playerAdapter.isDlnaMode()) {
                        toggle();
                    }
                    mHandler.postDelayed(hideGenstureLayoutRunnable, 2000);
                } else {
                    mHandler.post(gestureEndRun);
                }
            }

            /**
             * 单击时运行
             */
            Runnable gestureEndRun = new Runnable() {
                @Override
                public void run() {
                    if (gestureChangeVolOrBright || gestureChangeProgress) {
                        if (gestureChangeProgress) {
                            playerAdapter.seekTo(currentProgress);
                            showLoadingView();
                        }
                        gestureChangeVolOrBright = false;
                        gestureChangeProgress = false;
                    }
                    mHandler.postDelayed(hideGenstureLayoutRunnable, 2000);
                }
            };

            @Override
            public void FoneOnGesture() {
            }
        });

        mPlayerGestureView.addOnGestureListener(foneOnGesture);

    }

    /**
     * 更新音量
     *
     * @return void
     * @throws
     */
    private void updateVolume() {
        mHandler.removeCallbacks(hideGenstureLayoutRunnable);
        genstureLayout.setVisibility(View.VISIBLE);
        soundBrightLayout.setVisibility(View.VISIBLE);
        ivSoundBright.setVisibility(View.VISIBLE);
        tvSoundBright.setVisibility(View.VISIBLE);
        tvGestureProgress.setVisibility(View.GONE);

        audiomanage.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
        if (volPercentage == 0) {
            ivSoundBright.setImageResource(R.drawable.full_icon_gesture_sound_un);
        } else {
            ivSoundBright.setImageResource(R.drawable.full_icon_gesture_sound);
        }
        tvSoundBright.setText(volPercentage + "%");
    }

    /**
     * 隐藏 音量/亮度/进度显示区
     */
    private Runnable hideGenstureLayoutRunnable = new Runnable() {

        @Override
        public void run() {
            genstureLayout.setVisibility(View.GONE);
        }
    };

    /**
     * 更新亮度
     *
     * @return void
     * @throws
     */
    private void updateBright() {
        mHandler.removeCallbacks(hideGenstureLayoutRunnable);
        genstureLayout.setVisibility(View.VISIBLE);
        soundBrightLayout.setVisibility(View.VISIBLE);
        ivSoundBright.setVisibility(View.VISIBLE);
        tvSoundBright.setVisibility(View.VISIBLE);
        tvGestureProgress.setVisibility(View.GONE);
        ivSoundBright.setImageResource(R.drawable.full_icon_gesture_bright);

//        if (TransPadService.isConnected() && TransPadService.getConnectDeviceMaxBrightness() > 0 && TransPadService.getConnectDeviceType() == TransPadService.TRANSPAD_DEVICE_TYPE_AUTO) {
//            Message msg = mHandler.obtainMessage(TransPadService.SET_DEVICE_BRIGHTNESS);
//            msg.arg1 = (int) (currentBrightness * TransPadService.getConnectDeviceMaxBrightness());
//            EventBus.getDefault().post(msg);
//        } else {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = currentBrightness;
        getWindow().setAttributes(lp);
//        }


        tvSoundBright.setText((int) (currentBrightness * 100) + "%");
    }

    /**
     * 更新进度
     *
     * @param currentProgress
     * @return void
     * @throws
     */
    protected void gestureUpdateProgress(int currentProgress) {
        L.v(TAG, "gestureUpdateProgress");
        mHandler.removeCallbacks(hideGenstureLayoutRunnable);
        tvGestureProgress.setText("");
        genstureLayout.setVisibility(View.VISIBLE);
        soundBrightLayout.setVisibility(View.GONE);
        ivSoundBright.setVisibility(View.GONE);
        tvGestureProgress.setVisibility(View.VISIBLE);
        String curStr = PlayerUtil.second2HourStr(currentProgress / 1000);
        String durStr = PlayerUtil.second2HourStr(playerAdapter.getMediaDuration() % 1000 > 0 ? playerAdapter.getMediaDuration() / 1000 + 1 : playerAdapter.getMediaDuration() / 1000);
        SpannableString ss = new SpannableString(curStr);
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.video_info_tab_line)), 0, curStr.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        tvGestureProgress.append(ss);
        ss = new SpannableString("/" + durStr);
        ss.setSpan(new ForegroundColorSpan(Color.WHITE), 0, durStr.length() + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        tvGestureProgress.append(ss);
        mSeekBar.setProgress(currentProgress);
        videoProgress.setText(PlayerUtil.second2HourStr(currentProgress / 1000));
    }

    @Override
    public void onBackPressed() {

        L.v(TAG, "onBackPressed");

        if (isOpening()) {
            return;
        }


        if (System.currentTimeMillis() - backClickTiem < 1000) {
            backClickTiem = System.currentTimeMillis();
            // 双击
            return;
        } else {
            backClickTiem = System.currentTimeMillis();
            if (playerAdapter != null) {
                playerAdapter.savePlayRecord();
            }
            finish();
        }
    }

    /**
     * 是否正在打开
     *
     * @return boolean
     * @throws
     */
    public boolean isOpening() {
        if (playerAdapter != null && playerAdapter.isOpening()) {
            return true;
        }
        return false;
    }

    /**
     * 更新视频长度
     *
     * @return void
     * @throws
     */
    private void updateMediaDuring() {
        L.v(TAG, "updateMediaDuring");
        int duration = playerAdapter.getMediaDuration();
        if (duration > 0 && mSeekBar.getMax() != duration) {
            mSeekBar.setMax(duration); // 获取媒体文件的持续时间,
            L.v(TAG, "updateMediaDuring currentPostion = " + progress);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    videoDuration.setText("/" + PlayerUtil.second2HourStr(playerAdapter.getMediaDuration() / 1000));
                }
            });
        }
    }

    /**
     * 进度条改变监听
     *
     * @author kongxiaojun
     * @since 2014-4-16
     */
    private class FoneOnSeekBarChangeListener implements
            OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            /** 限制不能频繁seek，防止播放器死锁，导致手机死机 */
            if (System.currentTimeMillis() - 1000 > lastTrackingTime
                    && playerAdapter != null) {
                if (!playerAdapter.isDlnaMode()) {
                    mHandler.removeMessages(HIDE_CONTROL_LAYOUT);
                    mHandler.sendEmptyMessageDelayed(HIDE_CONTROL_LAYOUT, 5000);
                }
                if (playerAdapter.isDlnaMode() || (playerAdapter.isOpenSuccess()
                        && playerAdapter.getMediaDuration() > 0)) {
                    videoProgress
                            .setText(PlayerUtil.second2HourStr(Math
                                    .round((float) mSeekBar.getProgress() / 1000f)));
                    playerAdapter.seekTo(seekBar.getProgress()); // 把播放器调整到进度条的位置
                    if (!playerAdapter.isDlnaMode()) {
                        showLoadingView();
                    }
                }
                lastTrackingTime = System.currentTimeMillis();
            }
        }
    }

    /**
     * 更新播放按钮
     *
     * @return void
     * @throws
     */
    protected void updatePlayButton() {
        L.v(TAG, "updatePlayButton isplaying = " + playerAdapter.isPlaying());
        playPause.setBackgroundResource(playerAdapter.isPlaying() ? R.drawable.player_pause
                : R.drawable.player_play);
    }

    /**
     * 电话状态监听
     *
     * @author kongxiaojun
     * @since 2014-4-16
     */
    private class MyPhoneStateListener extends PhoneStateListener {

        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    L.v(TAG, "call ringing");
                    if (playerAdapter != null && playerAdapter.isPlaying()) {
                        playerAdapter.pause();
                        playPause.setBackgroundResource(R.drawable.player_play);
                        showControlView();
                    }
                    break;
                /**
                 * bug443 电话挂断后不继续播放，应该暂停
                 * */
                case TelephonyManager.CALL_STATE_IDLE:
                    if (playerAdapter != null) {
                        if (!playerAdapter.isDlnaMode()) {
                            if (controlLayout.getVisibility() != View.VISIBLE) {
                                showControlView();
                            }
                            mHandler.removeMessages(HIDE_CONTROL_LAYOUT);
                        }else {
                            playerAdapter.play();
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 设置SurfaceView的宽高
     *
     * @param width
     * @param height
     * @return void
     * @throws
     */
    private void setSurfaceWidthHeight(int width, int height) {
        // 设置SurfaceView的宽高
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) playerView.getLayoutParams();
        int leftRight = (ScreenUtil.getScreenWidthPix(LiteVideoPlayActivity.this) - width) / 2;
        int topBottom = 0;
        topBottom = (ScreenUtil.getScreenHeightPix(LiteVideoPlayActivity.this) - height) / 2;
        if (topBottom <= 0) {
            topBottom = 1;
        }
        params.setMargins(leftRight, topBottom, leftRight, topBottom);
        playerView.setLayoutParams(params);
        playerView.invalidate();
    }

    @InjectView(R.id.dlna_stop)
    LinearLayout dlnaStopButton;

    @OnClick(R.id.dlna_stop)
    void dlnaStop() {
        dlnaCancel();
    }

    @OnClick(R.id.dlna_button_cancel)
    void dlnaCancel(){
        //退出dlna播放，启动正常播放模式
        dlnaLayout.setVisibility(View.GONE);
        dlnaOpeningLayout.setVisibility(View.GONE);
        dlnaStopButton.setVisibility(View.GONE);
        playerView.setVisibility(View.VISIBLE);
        showControlView();
        if (playerAdapter != null) {
            playerAdapter.exitDlnaMode();
            playerAdapter.play();
        }
    }

    @OnClick(R.id.dlna_button_retry)
    void dlnaRetry(){
        //重试打开DLNA
        if (playerAdapter != null) {
            playerAdapter.dlnaRetry();
        }
    }
}
