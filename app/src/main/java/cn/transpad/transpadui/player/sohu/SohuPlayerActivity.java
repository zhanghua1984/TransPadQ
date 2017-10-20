package cn.transpad.transpadui.player.sohu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.sohuvideo.base.widget.SohuScreenView;
import com.sohuvideo.sdk.LoadFailure;
import com.sohuvideo.sdk.PlayerError;
import com.sohuvideo.sdk.PlayerMonitor;
import com.sohuvideo.sdk.SohuLibLoadListener;
import com.sohuvideo.sdk.SohuVideoPlayer;
import com.sohuvideo.sdk.entity.SohuPlayitemBuilder;
import com.umeng.analytics.MobclickAgent;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.http.Request;
import cn.transpad.transpadui.http.SohuIdRst;
import cn.transpad.transpadui.player.gesture.FoneOnGesture;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.PlayerUtil;
import cn.transpad.transpadui.util.ScreenUtil;
import cn.transpad.transpadui.view.FoneGestureOverlayView;
import cn.transpad.transpadui.view.SohuVideoDefinitionPopupWindow;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class SohuPlayerActivity extends Activity {
    private static final String TAG = "SohuPlayerActivity";

    /**
     * 搜狐视频播放View
     */
    private SohuScreenView mSohuScreenView;

    private View mControllerView, mTitleBarView;
    private ImageView mStartPlayBtn;
    private ImageButton mPlayOrPauseBtn;
    private ImageButton mNextBtn;
    private LinearLayout mDefinitionBtn;
    private TextView mDefinitionText;
    private ImageButton mBackBtn;
    //	private Button mLockBtn;
//	private Button mDownBtn;
//    private boolean isFromNotify;
    /**
     * 捕获手势View
     */
    private FoneGestureOverlayView mPlayerGestureView;
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
    private View genstureLayout;
    /**
     * 音量/亮度layout
     */
    private View soundBrightLayout;
    /**
     * 音量/亮度icon
     */
    private ImageView ivSoundBright;
    /**
     * 音量/亮度值
     */
    private TextView tvSoundBright;
    /**
     * 进度值
     */
    private TextView tvGestureProgress;
    /**
     * 搜狐来源
     */
    private TextView tvSohuSource;

    /**
     * 当前屏幕亮度
     */
    private float currentBrightness;
    /**
     * 音量管理器
     */
    private AudioManager audiomanage;

    private SohuVideoPlayer mSohuVideoPlayer;
    private ProgressBar mProgressBar;

    private SeekBar mSeekBar;
    private TextView postionTime, durationTime, titleView;
    private boolean mTrackingTouch = false;

//	private boolean lock = false;

    private SohuVideoDefinitionPopupWindow definitionPopupWindow;

    private final static int MSG_HIDE_CONTROLLER = 0;

    private String id;

    private String keyword;

    private SohuPlayitemBuilder currentPlayItem;

//	/**
//	 * 电池电量接收者
//	 */
//	private BatteryReceiver batteryReceiver;
//	/**
//	 * 电池状态图标
//	 */
//	private ImageView batteryStatus;
//	/**
//	 * 全屏播放时间
//	 */
//	private TextView fullSystemTime;
    /**
     * 栏目id
     */
    private int cid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SohuVideoPlayer.initSohuPlayer(sohuLibLoadListener, getApplicationContext());
        setContentView(R.layout.sohu_player);
        setupViews();
        initPlayerGestureListener();
        initAudioAndBrightness();
        resolveIntent();
        if (android.os.Build.VERSION.SDK_INT >= 9) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE); // ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        fullScreen();
        showControlView();
    }

    private void fullScreen() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(lp);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HIDE_CONTROLLER: {
                    if (mTitleBarView.getVisibility() == View.VISIBLE) {
                        toggle();
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

    private SohuLibLoadListener sohuLibLoadListener = new SohuLibLoadListener() {
        @Override
        public void onProgressUpdate(long downloaded, long total) {
        }

        @Override
        public void onLoadResult(boolean result) {
        }

        @Override
        public void onFailed() {
        }

        @Override
        public void onDownloadComplete() {
//			// 下载完成,重新播
//			Toast.makeText(getApplicationContext(), R.string.update_player_success, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAskForDownload() {
//			Toast.makeText(getApplicationContext(), R.string.update_player_lib_toast, Toast.LENGTH_SHORT).show();
            SohuVideoPlayer.onDownloadLibComfirm(true);
        }

        @Override
        public void onDownloadCancel() {
        }
    };

    private void setupViews() {
        mStartPlayBtn = (ImageView) findViewById(R.id.btn_start_play);
        mStartPlayBtn.setOnClickListener(mOnClickListener);
        mPlayOrPauseBtn = (ImageButton) findViewById(R.id.videoplayer_PlayPause);
        mPlayOrPauseBtn.setOnClickListener(mOnClickListener);

        mNextBtn = (ImageButton) findViewById(R.id.videoplayer_next);
        mNextBtn.setOnClickListener(mOnClickListener);

        mBackBtn = (ImageButton) findViewById(R.id.videoplayer_back);
        mBackBtn.setOnClickListener(mOnClickListener);

        mDefinitionBtn = (LinearLayout) findViewById(R.id.videoplayer_definition_layout);
        mDefinitionBtn.setOnClickListener(mOnClickListener);

        mDefinitionText = (TextView) findViewById(R.id.videoplayer_definition_text);

//		mLockBtn = (Button) findViewById(R.id.player_full_lock);
//		mLockBtn.setOnClickListener(mOnClickListener);

        mSohuScreenView = (SohuScreenView) findViewById(R.id.sohu_screen);
        mSohuScreenView.setOnClickListener(mOnClickListener);
        mControllerView = findViewById(R.id.videoplayer_cotroll_layout);
        mTitleBarView = findViewById(R.id.videoplayer_title_bar);
//		mLockView = findViewById(R.id.full_player_left);

        mSeekBar = (SeekBar) findViewById(R.id.video_player_seekbar);
        mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

        postionTime = (TextView) findViewById(R.id.videoplayer_progress_text);
        durationTime = (TextView) findViewById(R.id.videoplayer_duration_text);

        titleView = (TextView) findViewById(R.id.videoplayer_title);
        tvSohuSource = (TextView) findViewById(R.id.full_player_source);
        tvSohuSource.setOnClickListener(mOnClickListener);

        // mFastForwardBtn = (ImageView) findViewById(R.id.btn_fast_forward);
        // mFastForwardBtn.setOnClickListener(mOnClickListener);
        // mFastBackwardBtn = (ImageView) findViewById(R.id.btn_fast_backward);
        // mFastBackwardBtn.setOnClickListener(mOnClickListener);

        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mProgressBar.setVisibility(View.GONE);

        mPlayerGestureView = (FoneGestureOverlayView) findViewById(R.id.full_surface_gesture);
        genstureLayout = findViewById(R.id.full_player_gensture_layout);
        soundBrightLayout = findViewById(R.id.video_full_vol_layout);
        ivSoundBright = (ImageView) findViewById(R.id.video_full_vol_icon);
        tvSoundBright = (TextView) findViewById(R.id.video_full_vol_text);
        tvGestureProgress = (TextView) findViewById(R.id.video_full_postion_text);
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
//            if (TransPadService.isConnected() && TransPadService.getConnectDeviceMaxBrightness() > 0 && TransPadService.getConnectDeviceType() == TransPadService.TRANSPAD_DEVICE_TYPE_AUTO){
//                currentBrightness = (float) TransPadService.getConnectDeviceBrightness() / (float) TransPadService.getConnectDeviceMaxBrightness();
//            }else {
            currentBrightness = (float) Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS) / 255f;
//            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

//	/**
//	 * 初始化电池电量接收
//	 *
//	 * @return void
//	 * @throws
//	 */
//	private void initBatteryReceiver() {
//		batteryReceiver = new BatteryReceiver();
//		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
//		registerReceiver(batteryReceiver, filter);
//	}

//	/**
//	 * 电池电量广播接收者
//	 *
//	 * @author kongxiaojun
//	 * @since 2014-9-2
//	 */
//	private class BatteryReceiver extends BroadcastReceiver {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			// 判断它是否是为电量变化的Broadcast Action
//			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
//				// 获取当前电量
//				int level = intent.getIntExtra("level", 0);
//				// 电量的总刻度
//				int scale = intent.getIntExtra("scale", 100);
//				// 电池状态
//				int status = intent.getIntExtra("status", -1);
//				// 把它转成百分比
//				// tv.setText("电池电量为"+((level*100)/scale)+"%");
//				switch (status) {
//				case BatteryManager.BATTERY_STATUS_CHARGING: {// 充电中
//					batteryStatus.setImageResource(R.drawable.player_batter_charging);
//					break;
//				}
//				default: {
//					// 计算电池电量百分比显示对应的状态图
//					int percentage = (level * 100) / scale;
//					if (percentage > 75) {
//						// 显示满格
//						batteryStatus.setImageResource(R.drawable.player_battery_full);
//					} else if (percentage > 35) {
//						// 显示中格
//						batteryStatus.setImageResource(R.drawable.player_battery_middle);
//					} else {
//						// 显示低电量
//						batteryStatus.setImageResource(R.drawable.player_battery_low);
//					}
//					break;
//				}
//				}
//			}
//		}
//
//	}

    private OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mTrackingTouch = false;
            int whereto = seekBar.getProgress();
            if (whereto > 0 && (currentPlayItem == null || currentPlayItem.getTvId() <= 0)) {
                mSohuVideoPlayer.seekTo(whereto);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mTrackingTouch = true;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            long duration = mSohuVideoPlayer.getDuration();
            long temp = (duration * progress) / 1000;
            String timeStr = PlayerUtil.second2HourStr((int) (temp / seekBar.getMax()));
            postionTime.setText(timeStr);
        }
    };

    /**
     * 初始化播放器
     *
     * @return void
     * @throws
     */
    private void initPlayer() {
        mSohuVideoPlayer = new SohuVideoPlayer(this);
        mSohuVideoPlayer.setSohuScreenView(mSohuScreenView);
        mSohuVideoPlayer.setPlayerMonitor(mPlayerMonitor);
    }

    /**
     * Override methods if needed.
     */
    private PlayerMonitor mPlayerMonitor = new PlayerMonitor() {

        @Override
        public void onPreparing() {
            super.onPreparing();
            mStartPlayBtn.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPrepared() {
            super.onPrepared();
            mPlayOrPauseBtn.setBackgroundResource(R.drawable.player_pause);
            mProgressBar.setVisibility(View.GONE);
            durationTime.setText("/" + PlayerUtil.second2HourStr(mSohuVideoPlayer.getDuration() / 1000));
            updateDefinition(mSohuVideoPlayer.getCurrentDefinition());
            mHandler.sendEmptyMessageDelayed(MSG_HIDE_CONTROLLER, 3000);
            mSeekBar.setMax(mSohuVideoPlayer.getDuration());
        }

        @Override
        public void onPlay() {
            super.onPlay();
            mPlayOrPauseBtn.setBackgroundResource(R.drawable.player_pause);
            mHandler.sendEmptyMessageDelayed(MSG_HIDE_CONTROLLER, 3000);
        }

        @Override
        public void onPause() {
            super.onPause();
            mPlayOrPauseBtn.setBackgroundResource(R.drawable.player_play);
        }

        @Override
        public void onStop() {
            super.onStop();
            mPlayOrPauseBtn.setBackgroundResource(R.drawable.player_play);
        }

        @Override
        public void onError(PlayerError error) {
            L.v(TAG, error.toString());
        }

        @Override
        public void onStartLoading() {
            super.onStartLoading();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoadSuccess() {
            super.onLoadSuccess();
        }

        @Override
        public void onLoadFail(LoadFailure failure) {

        }

        @Override
        public void onBuffering(int progress) {
            super.onBuffering(progress);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onComplete() {
            super.onComplete();
            SohuPlayerActivity.this.finish();
        }

        @Override
        public void onPausedAdvertShown() {
            super.onPausedAdvertShown();
            if (mTitleBarView.getVisibility() == View.VISIBLE) {
                toggle();
            }
            mHandler.removeMessages(MSG_HIDE_CONTROLLER);
        }

        @Override
        public void onProgressUpdated(int currentPostion, int duration) {
            mProgressBar.setVisibility(View.GONE);
            if (mTrackingTouch || gestureChangeProgress) {
                return;
            }
            if (currentPostion >= 0) {
                mSeekBar.setProgress(currentPostion);
            }
            postionTime.setText(PlayerUtil.second2HourStr(currentPostion / 1000));
        }

        @Override
        public void onVideoClick() {
        }

        @Override
        public void onDecodeChanged(boolean arg0, int arg1, int arg2) {
            super.onDecodeChanged(arg0, arg1, arg2);
        }

        @Override
        public void onDefinitionChanged() {
            super.onDefinitionChanged();
            updateDefinition(mSohuVideoPlayer.getCurrentDefinition());
        }

        @Override
        public void onPlayItemChanged(SohuPlayitemBuilder arg0, int arg1) {
            super.onPlayItemChanged(arg0, arg1);
            currentPlayItem = arg0;
            titleView.setText(arg0.getTitle());
        }

        @Override
        public void onPlayOver(SohuPlayitemBuilder arg0) {
            super.onPlayOver(arg0);
        }

        @Override
        public void onPreviousNextStateChange(boolean arg0, boolean arg1) {
            super.onPreviousNextStateChange(arg0, arg1);
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart(this.getClass().getSimpleName());
        if (mSohuVideoPlayer != null) {
            mSohuVideoPlayer.play();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd(this.getClass().getSimpleName());
        if (mSohuVideoPlayer != null) {
            mSohuVideoPlayer.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mSohuVideoPlayer != null) {
            mSohuVideoPlayer.stop(true);
        }
    }

    @Override
    protected void onDestroy() {
        if (mSohuVideoPlayer != null) {
            mSohuVideoPlayer.stop(false);
            mSohuVideoPlayer.release();
        }
        super.onDestroy();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_start_play: { // 点击播放
                    if (mSohuVideoPlayer != null) {
                        mSohuVideoPlayer.play();
                        sendHideControllerMessge();
                    }
                    break;
                }
                case R.id.videoplayer_PlayPause: { // 点击播放/暂停
                    if (currentPlayItem != null && currentPlayItem.getTvId() > 0) {
                        break;
                    }
                    if (mSohuVideoPlayer != null) {
                        mSohuVideoPlayer.playOrPause();
                        sendHideControllerMessge();
                    }
                    break;
                }
                case R.id.videoplayer_next: { // 点击下一集
                    if (currentPlayItem != null && currentPlayItem.getTvId() <= 0 && mSohuVideoPlayer != null) {
                        mSohuVideoPlayer.next();
                        sendHideControllerMessge();
                    }
                    break;
                }

                case R.id.videoplayer_back: { // 点击返回
                    SohuPlayerActivity.this.finish();
                    break;
                }

//			case R.id.player_full_lock: { // 点击锁定
//				lock = !lock;
//				mLockBtn.setBackgroundResource(lock ? R.drawable.video_fullscreen_lock_selector : R.drawable.video_fullscreen_unlock_selector);
//				if (lock) {
//					mControllerView.setVisibility(View.GONE);
//					mTitleBarView.setVisibility(View.GONE);
//					mDownBtn.setVisibility(View.GONE);
//				} else {
//					showController();
//				}
//				sendHideControllerMessge();
//				break;
//			}
                case R.id.videoplayer_definition_layout: { // 点击清晰度
                    if (mSohuVideoPlayer != null && mSohuVideoPlayer.getSupportDefinitions() != null && mSohuVideoPlayer.getSupportDefinitions().size() > 0) {
                        showDefinitionWindow(v);
                        sendHideControllerMessge();
                    }
                    break;
                }
//			case R.id.full_player_source: { // 点击下载搜狐apk
//
//				// 是否已经安装了搜狐app
//				if (TPUtil.checkApkExist(SohuPlayerActivity.this, AppConst.SOHU_APP_PACKAGENAME) != null) {
//					TPUtil.start3rdApp(SohuPlayerActivity.this, AppConst.SOHU_APP_PACKAGENAME);
//					return;
//				}
//
//				startDownloadSohuApp();
//
//				break;
//			}
//			case R.id.sohu_player_download: {// 缓存
//				// 检测是否安装搜狐视频，已经安装了就打开搜狐视频详情页，未安装就提示安装
//				if (TPUtil.checkApkExist(SohuPlayerActivity.this, AppConst.SOHU_APP_PACKAGENAME) != null) {
//					if (currentPlayItem == null) {
//						return;
//					}
//					// 已安装搜狐视频
//					StringBuffer sb = new StringBuffer();
//					sb.append("sva:// action.cmd?action=1.1");
//					sb.append("&cid=").append(cid);
//					if (currentPlayItem.getSid() != 0) {
//						sb.append("&sid=");
//						sb.append(currentPlayItem.getSid());
//					}
//					if (currentPlayItem.getVid() != 0) {
//						sb.append("&vid=");
//						sb.append(currentPlayItem.getVid());
//					}
//					if (currentPlayItem.getSite() != 0) {
//						sb.append("&site=");
//						sb.append(currentPlayItem.getSite());
//					}
//					sb.append("&channelid=1000120021&paused=1&backpage=1");
//					// if (!TextUtils.isEmpty(currentPlayItem.getTitle())) {
//					// sb.append("&ex1=");
//					// sb.append(currentPlayItem.getTitle());
//					// }
//					String data = sb.toString();
//					Intent intent = new Intent(Intent.ACTION_DEFAULT);
//					intent.setData(Uri.parse(data));
//					startService(intent);
//				} else {
//					// 未安装搜狐视频
//					new CustomAlertDialog.Builder(SohuPlayerActivity.this, getString(R.string.dialog_title), getString(R.string.sohu_video_cache_install_sohuvideo), getString(R.string.dialog_cancel), getString(R.string.dialog_ok), new CustomDialogOnClickListener() {
//						@Override
//						public void onRight() {
//							startDownloadSohuApp();
//						}
//
//						@Override
//						public void onLeft() {
//						}
//					}).create().show();
//				}
//				break;
//			}
            /*
             * case R.id.btn_prev: { break; } case R.id.btn_next: { break; }
			 */
                default:
                    break;
            }
        }
    };

    /**
     * 显示清晰度选择框
     *
     * @param v
     * @return void
     * @throws
     */
    private void showDefinitionWindow(View v) {
        if (definitionPopupWindow == null) {
            definitionPopupWindow = new SohuVideoDefinitionPopupWindow(SohuPlayerActivity.this, mSohuVideoPlayer.getSupportDefinitions(), new SohuVideoDefinitionPopupWindow.ItemClickCallBack() {
                @Override
                public void onItemclick(Integer dfnt) {
                    // 切换清晰度
                    if (dfnt != mSohuVideoPlayer.getCurrentDefinition()) {
                        mSohuVideoPlayer.changeDefinition(dfnt);
                        // 更新清晰度背景
                        updateDefinition(dfnt);
                    }
                    if (definitionPopupWindow != null) {
                        definitionPopupWindow.dismiss();
                    }

                }
            }, mSohuVideoPlayer.getCurrentDefinition());
        } else {
            definitionPopupWindow.setDfntList(mSohuVideoPlayer.getSupportDefinitions(), mSohuVideoPlayer.getCurrentDefinition());
        }
        if (definitionPopupWindow.isShowing()) {
            definitionPopupWindow.dismiss();
        } else {
            definitionPopupWindow.show(v);
        }
    }

//	/**
//	 * 开始下载搜狐视频客户端
//	 *
//	 * @return void
//	 * @throws
//	 */
//	protected void startDownloadSohuApp() {
//		Download download = new Download();
//		download.setDownloadIsShowRunningNotification(true);
//		download.setDownloadUrl(AppConst.SOHU_APK_URL);
//		download.setDownloadFileName("搜狐视频");
//		download.setDownloadNotification(SohuPlayerActivity.this);
//		download.setDownloadIsInstall(true);
//		download.setDownloadIsErrorToast(true);
//		download.setDownloadIsLimitSpeed(false);
//		download.setDownloadType(Download.DOWNLOAD_RECOMMEND);
//		int result = StorageModule.getInstance().addFileDownload(download);
//		switch (result) {
//		case 1:
//			TPUtil.showToast(SohuPlayerActivity.this, "已经开始下载搜狐视频");
//			break;
//		case -1:
//			TPUtil.showToast(SohuPlayerActivity.this, "该任务已下载");
//			break;
//		default:
//			break;
//		}
//	}

    /**
     * 更新清晰度背景
     *
     * @param dfnt
     * @return void
     * @throws
     */
    private void updateDefinition(int dfnt) {
        mDefinitionBtn.setVisibility(View.VISIBLE);
        switch (dfnt) {
            case 1:// 标清
                mDefinitionText.setText(R.string.definition_normal);
                break;
            case 2:// 高清
                mDefinitionText.setText(R.string.definition_high);
                break;
            case 4:// 超清
                mDefinitionText.setText(R.string.definition_super);
                break;
            default:
                break;
        }
    }

    public void toggle() {
        if (mControllerView.getVisibility() != View.VISIBLE) {
            showControlView();
        } else {
            hideControlView();
        }
    }

    public void hideControlView() {
        mHandler.removeMessages(MSG_HIDE_CONTROLLER);
        hideSystemUI();
        mTitleBarView.setVisibility(View.INVISIBLE);
        mControllerView.setVisibility(View.INVISIBLE);
        if (definitionPopupWindow != null && definitionPopupWindow.isShowing()) {
            definitionPopupWindow.dismiss();
        }
    }

    public void showControlView() {
        mHandler.removeMessages(MSG_HIDE_CONTROLLER);
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_CONTROLLER, 5000);
        showSystemUI();
        mTitleBarView.setVisibility(View.VISIBLE);
        mControllerView.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏系统UI，包括导航栏状态栏
     */
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    /**
     * 显示系统UI
     */
    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


    /**
     * 发送隐藏控制区layout消息
     *
     * @return void
     * @throws
     */
    private void sendHideControllerMessge() {
        mHandler.removeMessages(MSG_HIDE_CONTROLLER);
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_CONTROLLER, 3000);
    }

    private void resolveIntent() {
        // Intent intent = new Intent();
        Intent intent = getIntent();
//        isFromNotify = intent.getBooleanExtra(AppConst.INTENT_FROM_NOTIFY, false);
        id = intent.getStringExtra(AppConst.INTENT_KEY_ID);
        if (TextUtils.isEmpty(id)) {
            id = "111";
        }
        keyword = intent.getStringExtra(AppConst.INTENT_KEY_KEYWORD);
        if (TextUtils.isEmpty(keyword)) {
            keyword = "test";
        }
        String ourl = intent.getStringExtra(AppConst.INTENT_KEY_OURL);
        if (TextUtils.isEmpty(ourl)) {
            L.v(TAG, "原网页地址是空的");
            finish();
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        Request.getInstance().getSohuIds(ourl, new Callback<SohuIdRst>() {
            @Override
            public void success(SohuIdRst sohuIdRst, Response response) {
                L.v(TAG, "sohuIdRst = " + sohuIdRst);
                if (sohuIdRst.result == 0) {
                    initPlayer();
                    cid = sohuIdRst.idlist.cid;
                    // 成功
                    int tvid = sohuIdRst.idlist.tvid;
                    if (tvid > 0) {
                        mSohuVideoPlayer.setDataSource(new SohuPlayitemBuilder(id, tvid), keyword);
                        mSohuVideoPlayer.play();
                        return;
                    }
                    long sid = sohuIdRst.idlist.sid;
                    long vid = sohuIdRst.idlist.vid;
                    int site = sohuIdRst.idlist.site;
                    if (sid > 0 || vid > 0) {
                        if (site > 0) {
                            mSohuVideoPlayer.setDataSource(new SohuPlayitemBuilder(id, sid, vid).setSite(site), keyword);
                        } else {
                            mSohuVideoPlayer.setDataSource(new SohuPlayitemBuilder(id, sid, vid), keyword);
                        }
                        SohuVideoPlayer.setArgs(AppConst.FONE_APPKEY, AppConst.TEST_PARTNER, getApplicationContext());
                        mSohuVideoPlayer.play();
                    }
                } else {
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), R.string.play_url_null, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:// 返回键
                finish();
                break;

            default:
                break;
        }

        return super.onKeyDown(keyCode, event);

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
                    width = ScreenUtil.getScreenWidthPix(SohuPlayerActivity.this);
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
                    int addVol = (int) (distance / (ScreenUtil.getScreenHeightPix(SohuPlayerActivity.this) / 50));
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
                    float addBright = distance / (float) (ScreenUtil.getScreenHeightPix(SohuPlayerActivity.this) * 10);
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
                if (currentPlayItem != null && currentPlayItem.getTvId() <= 0) {
                    L.v(TAG, "initPlayerGestureListener", "FoneOnGestureMoveLeftOrRight");
                    try {
                        if (mSohuVideoPlayer != null && mSohuVideoPlayer.getDuration() > 0) {
                            int videoDruation = mSohuVideoPlayer.getDuration();
                            if (videoDruation > 0) {
                                // 调节进度
                                gestureChangeProgress = true;
                                currentProgress = mSohuVideoPlayer.getCurrentPosition();
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
            }

            @Override
            public void FoneOnGestureEnd(boolean isEnableSeek, boolean isLeftOrRight) {
                L.v(TAG, "initPlayerGestureListener", "FoneOnGestureEnd isEnableSeek: " + isEnableSeek + " ,isLeftOrRight:" + isLeftOrRight);
                if (!gestureChangeVolOrBright && !gestureChangeProgress) {
                    // 单击
                    toggle();
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
                            mSohuVideoPlayer.seekTo(currentProgress);
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
//        }else {
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
        String durStr = PlayerUtil.second2HourStr(mSohuVideoPlayer.getDuration() % 1000 > 0 ? mSohuVideoPlayer.getDuration() / 1000 + 1 : mSohuVideoPlayer.getDuration() / 1000);
        SpannableString ss = new SpannableString(curStr);
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.video_info_tab_line)), 0, curStr.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        tvGestureProgress.append(ss);
        ss = new SpannableString("/" + durStr);
        ss.setSpan(new ForegroundColorSpan(Color.WHITE), 0, durStr.length() + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        tvGestureProgress.append(ss);
        mSeekBar.setProgress(currentProgress);
        postionTime.setText(PlayerUtil.second2HourStr(currentProgress / 1000));
    }

}
