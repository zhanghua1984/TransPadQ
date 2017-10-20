package cn.transpad.transpadui.player.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.player.AudioPlayer;
import cn.transpad.transpadui.player.entity.AudioInfo;
import cn.transpad.transpadui.service.TransPadService;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.PlayerUtil;
import cn.transpad.transpadui.util.TPUtil;
import cn.transpad.transpadui.view.AudioAlbumListDialog;
import cn.transpad.transpadui.view.PlayerDialog;
import de.greenrobot.event.EventBus;

/**
 * Created by Kongxiaojun on 2015/1/19.
 * 音频播放activity
 */
public class AudioPlayActivity extends Activity implements AudioPlayer.AudioPlayStateListener {

    private static final String TAG = "AudioPlayActivity";

    private static int activeInstances;

    private boolean audioStoped;

    @InjectView(R.id.musicSeekBar)
    SeekBar mSeekBar;
    @InjectView(R.id.playPause)
    ImageButton playPause;

    @InjectView(R.id.audio_image)
    RoundedImageView audioImage;
    @InjectView(R.id.audio_name_songer)
    TextView audioName;
    @InjectView(R.id.progress_text)
    TextView playProgress;
    @InjectView(R.id.progress_maxt_text)
    TextView playMaxProgress;
    @InjectView(R.id.play_loop)
    ImageView playLoop;

    private AudioInfo audioInfo;

    private Handler mHandler;

    private static final int UPDATE_AUDIO_INFO = 1;
    private static final int UPDATE_PROGRESS = 2;
    private static final int AUDIO_SERVICE_STOPED = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        L.v(TAG, "onCreate");
        activeInstances++;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_play);
        ButterKnife.inject(this);
        AudioPlayer.getInstance().registerAudioPlayStateListener(this);
        initHandler();
        handelIntent(getIntent());
        TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        manager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
        mSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        updateLoopImage();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        L.v(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handelIntent(intent);
    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case UPDATE_AUDIO_INFO://更新UI
                        L.v(TAG, "UPDATE_AUDIO_INFO");
                        if (AudioPlayer.getInstance().isPlaying()) {
                            playPause.setBackgroundResource(R.drawable.player_pause);
                            startImageAnim();
                        } else {
                            playPause.setBackgroundResource(R.drawable.player_play);
                            stopImageAnim();
                        }
                        if (audioInfo != null) {
                            audioName.setText("　" + (TextUtils.isEmpty(audioInfo.title) ? new File(audioInfo.path).getName() : audioInfo.title) + "　" + ((TextUtils.isEmpty(audioInfo.artist) || audioInfo.artist.toLowerCase().equals("unknown")) ? "" : "  " + audioInfo.artist + "　"));
                            playProgress.setText(PlayerUtil.second2MinuteStr(Math.round((float) AudioPlayer.getInstance().getCurrentPostion() / 1000f)));
                            playMaxProgress.setText("/" + PlayerUtil.second2MinuteStr(Math.round((float) audioInfo.mediaDuration / 1000f)));
                            mSeekBar.setMax(audioInfo.mediaDuration);
                            if (audioInfo.image != null) {
                                audioImage.setImageBitmap(audioInfo.image);
                            } else {
                                audioImage.setImageResource(R.drawable.audio_image_default);
                            }
                        }
                        break;
                    case UPDATE_PROGRESS:
                        if (AudioPlayer.getInstance().isPlaying() && audioInfo != null) {
                            int currentPostion = AudioPlayer.getInstance().getCurrentPostion();
                            if (currentPostion > audioInfo.mediaDuration) {
                                currentPostion = audioInfo.mediaDuration;
                            }
                            mSeekBar.setProgress(currentPostion);
                            playProgress.setText(PlayerUtil.second2MinuteStr(Math.round((float) currentPostion / 1000f)));
                            playMaxProgress.setText("/" + PlayerUtil.second2MinuteStr(Math.round((float) audioInfo.mediaDuration / 1000f)));
                        }
                        mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 200);
                        break;
                    case AUDIO_SERVICE_STOPED:
                        stopImageAnim();
                        playPause.setBackgroundResource(R.drawable.player_play);
                        break;
                }
            }
        };
    }


    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (AudioPlayer.getInstance().isPlaying() || AudioPlayer.getInstance().isPause()) {
                AudioPlayer.getInstance().seekTo(seekBar.getProgress());
            }
        }
    };

    @Override
    public void onServiceConnected() {
        //显示正在播放的文件信息
        audioInfo = AudioPlayer.getInstance().getAudioInfo(this);
        mHandler.sendEmptyMessage(UPDATE_AUDIO_INFO);
    }

    @Override
    public void onPostionChanged(int postion) {
    }

    @Override
    public void onOpenFailed(int type) {
        //打开失败，提示不支持此视频格式
        Toast.makeText(AudioPlayActivity.this, R.string.audio_format_not_support, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onOpenSuccess() {

    }

    @Override
    public void onPrepared(int duration) {
        L.v(TAG, "onPrepared duration = " + duration);
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        mSeekBar.setMax(duration);
        audioInfo = AudioPlayer.getInstance().getAudioInfo(this);
        mHandler.sendEmptyMessage(UPDATE_AUDIO_INFO);
        mHandler.removeMessages(UPDATE_PROGRESS);
        mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 200);
    }

    @Override
    public void onCompletion() {
    }

    @Override
    public void onPlayOrPause(boolean play) {
        mHandler.sendEmptyMessage(UPDATE_AUDIO_INFO);
    }

    @Override
    public void onAudioServiceStop() {
        mHandler.sendEmptyMessage(AUDIO_SERVICE_STOPED);
    }

    private void handelIntent(Intent intent) {
        List<MediaFile> audioFiles = intent.getParcelableArrayListExtra("playlist");
        int playindex = intent.getIntExtra("playindex", 0);
        if (audioFiles == null || audioFiles.size() == 0) {//获取第三方文件管理器打开的音频
            //首先获取播放列表
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
                    if (!TextUtils.isEmpty(url)) {
                        MediaFile file = new MediaFile();
                        file.setMediaFilePath(url);
                        audioFiles = new ArrayList<>();
                        audioFiles.add(file);
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (audioFiles == null || audioFiles.size() == 0) {
            //没有获取到文件，显示正在播放的文件信息
            audioInfo = AudioPlayer.getInstance().getAudioInfo(this);
            mHandler.sendEmptyMessage(UPDATE_AUDIO_INFO);
        } else {
            AudioPlayer.getInstance().stop();
            AudioPlayer.getInstance().open(audioFiles, playindex);
        }

    }

    @Override
    protected void onDestroy() {
        L.v(TAG, "onDestroy");
        activeInstances--;
        mHandler.removeMessages(UPDATE_PROGRESS);
        AudioPlayer.getInstance().unRegisterAudioPlayStateListener(this);
        EventBus.getDefault().unregister(this);
        if (AudioPlayer.getInstance().isPlaying() && activeInstances <= 0 && !audioStoped) {
            AudioPlayer.getInstance().stop();
        }
        super.onDestroy();
    }

    @OnClick(R.id.playPause)
    void playPause() {
        if (AudioPlayer.getInstance().isPlaying()) {
            AudioPlayer.getInstance().pause();
            playPause.setBackgroundResource(R.drawable.player_play);
            stopImageAnim();
        } else if (AudioPlayer.getInstance().isPause()) {
            AudioPlayer.getInstance().play();
            playPause.setBackgroundResource(R.drawable.player_pause);
            startImageAnim();
        } else {
            //音频服务停止了
            List<MediaFile> mediaFiles = AudioPlayer.getInstance().readPlayRecordList();
            if (mediaFiles != null && mediaFiles.size() > 0) {
                AudioPlayer.getInstance().open(mediaFiles, AudioPlayer.getInstance().readPlayRecordIndex());
            }
        }
    }

    @OnClick(R.id.playNext)
    void next() {
        if (TPUtil.isFastClick()) {
            L.v(TAG, "next", "fastClick");
            return;
        }
        AudioPlayer.getInstance().next();
    }

    @OnClick(R.id.playPrevious)
    void previous() {
        if (TPUtil.isFastClick()) {
            return;
        }
        AudioPlayer.getInstance().previous();
    }

    @OnClick(R.id.back)
    void back() {
        onBackPressed();
    }

    private PlayerDialog dialog;

    @OnClick(R.id.audio_info)
    void showAudioInfo() {
        if (audioInfo != null) {
            L.v(TAG, "audioinfo = " + audioInfo);
            View content = LayoutInflater.from(AudioPlayActivity.this).inflate(R.layout.audio_info_dialog, null);
            TextView textView = (TextView) content.findViewById(R.id.audio_info_text);
            Button okButton = (Button) content.findViewById(R.id.audio_info_ok);
            File file = new File(audioInfo.path);
            String info = String.format(getString(R.string.music_info_text), TextUtils.isEmpty(audioInfo.title) ? new File(audioInfo.path).getName() : audioInfo.title, (TextUtils.isEmpty(audioInfo.artist) || audioInfo.artist.toLowerCase().equals("unknown")) ? getString(R.string.unknow) : audioInfo.artist, TextUtils.isEmpty(audioInfo.album) ? getString(R.string.unknow) : audioInfo.album, TextUtils.isEmpty(audioInfo.format) ? TPUtil.getExtension(audioInfo.path) : audioInfo.format, PlayerUtil.formatFileLength(file.length()), file.getParent());
            textView.setText(info);
            dialog = new PlayerDialog(this);
            dialog.setTitle(R.string.music_info_title);
            dialog.setContentView(content);
            dialog.show();
            dialog.setCanceledOnTouchOutside(true);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
    }

    /**
     * 添加音频文件到指定播放列表
     */
    @OnClick(R.id.add_to_list)
    public void onAddTextViewClick() {

        final MediaFile mediaFile = AudioPlayer.getInstance().getCurrentMediaFile();
        if (mediaFile == null) {
            return;
        }
        L.v(TAG, "onAddTextViewClick MediaFile = " + mediaFile);

        AudioAlbumListDialog audioAlbumListDialog = new AudioAlbumListDialog(this);
        audioAlbumListDialog.show();
        audioAlbumListDialog.setOnAlbumDialogClickListener(new AudioAlbumListDialog.OnAlbumDialogClickListener() {
            @Override
            public void onOkClick(final List<MediaFile> folderList) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        L.v(TAG, "onOkClick", "folderList.size=" + folderList.size());
                        for (MediaFile folder : folderList) {
                            ArrayList<MediaFile> mediaFileSelectedList = new ArrayList<MediaFile>();
                            mediaFile.setMediaFileParentName(folder.getMediaFileName());
                            mediaFile.setMediaFileIsEncrypt(folder.getMediaFileIsEncrypt());
                            if (TextUtils.isEmpty(mediaFile.getMediaFileName())) {
                                //第三方文件浏览器过来的，需要填充信息
                                L.v(TAG, "from 3rd party file exploer");
                                mediaFile.setMediaFileDirectoryType(MediaFile.MEDIA_FILE_TYPE);
                                mediaFile.setMediaFileType(MediaFile.MEDIA_AUDIO_TYPE);
                                mediaFile.setMediaFileOriginalPath(mediaFile.getMediaFilePath());
                            }
                            mediaFileSelectedList.add(mediaFile);
                            StorageModule.getInstance().updateMediaFileList(mediaFileSelectedList);
                        }
                        Message message = new Message();
                        message.what = StorageModule.MSG_ADD_FILE_LIST_SUCCESS;
                        EventBus.getDefault().post(message);
                    }
                }).start();
            }
        });
    }


    @OnClick(R.id.play_loop)
    void changeLoop() {
        AudioPlayer.getInstance().setLoopType(AudioPlayer.getInstance().getLoopType() + 1 > 2 ? 0 : AudioPlayer.getInstance().getLoopType() + 1);
        updateLoopImage();
    }

    /**
     * 更新循环模式图片
     */
    private void updateLoopImage() {
        switch (AudioPlayer.getInstance().getLoopType()) {
            case 0://顺序播放
                playLoop.setImageResource(R.drawable.player_list_circle);
                break;
            case 1://随机播放
                playLoop.setImageResource(R.drawable.player_random);
                break;
            case 2://单曲循环
                playLoop.setImageResource(R.drawable.player_one);
                break;
        }
    }

    private Animation audioImageAnim;

    private boolean animPlaying;

    private void startImageAnim() {

        if (audioImageAnim != null && animPlaying) {
            audioImageAnim.setRepeatCount(Animation.INFINITE);
            return;
        }

        if (audioImageAnim == null) {
            audioImageAnim = AnimationUtils.loadAnimation(this, R.anim.audio_image_anim);
            LinearInterpolator lir = new LinearInterpolator();
            audioImageAnim.setInterpolator(lir);
            audioImageAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    animPlaying = true;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    animPlaying = false;
                    L.v(TAG, "onAnimationEnd");
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
        audioImageAnim.setRepeatCount(Animation.INFINITE);
        audioImage.startAnimation(audioImageAnim);
    }

    private void stopImageAnim() {
        if (audioImageAnim != null) {
            audioImageAnim.setRepeatCount(0);
        }
    }

    public void onEventMainThread(Message message) {
        switch (message.what) {
            case StorageModule.MSG_ADD_FILE_LIST_SUCCESS:
                Toast.makeText(this, R.string.audio_add_success, Toast.LENGTH_SHORT).show();
                break;
            case TransPadService.TRANSPAD_STATE_DISCONNECTED:
                onBackPressed();
                break;
        }
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        private boolean isPause;

        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    if (AudioPlayer.getInstance().isPlaying()) {
                        AudioPlayer.getInstance().pause();
                        isPause = true;
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (isPause && AudioPlayer.getInstance().isPause()) {
                        AudioPlayer.getInstance().play();
                        isPause = false;
                    }
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        AudioPlayer.getInstance().unRegisterAudioPlayStateListener(this);
        if (AudioPlayer.getInstance().isPlaying() && !audioStoped) {
            AudioPlayer.getInstance().stop();
            audioStoped = true;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart(this.getClass().getSimpleName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd(this.getClass().getSimpleName());
    }

}
