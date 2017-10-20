package cn.transpad.transpadui.player.adapter;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.fone.player.AndroidPlayer;
import com.fone.player.FonePlayer;
import com.fone.player.FonePlayer.OnCloseSuccessListener;
import com.fone.player.FonePlayer.OnCompletionListener;
import com.fone.player.FonePlayer.OnNewSubtitleListener;
import com.fone.player.FonePlayer.OnOpenDoneListener;
import com.fone.player.FonePlayer.OnOpenFailedListener;
import com.fone.player.FonePlayer.OnOpenPercentListener;
import com.fone.player.FonePlayer.OnOpenSuccessListener;
import com.fone.player.FonePlayer.OnPreparedListener;
import com.fone.player.FonePlayer.OnShowEngineTypeListener;
import com.fone.player.TagFoneMediaDesc;
import com.fone.player.TagFoneMediaInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.MultipleVideo;
import cn.transpad.transpadui.entity.PlayRecord;
import cn.transpad.transpadui.http.Reporter;
import cn.transpad.transpadui.http.XyzplaRst;
import cn.transpad.transpadui.main.TransPadApplication;
import cn.transpad.transpadui.player.IPlayerAdapter;
import cn.transpad.transpadui.player.VideoMode;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.ScreenUtil;
import cn.transpad.transpadui.util.TPUtil;

/**
 * 播放器适配器,控制播放
 *
 * @author kongxiaojun
 * @since 2014-4-18
 */
public abstract class FonePlayerAdapter implements IPlayerAdapter {
    /**
     * vip视频最大免费时长5分钟，当后台配置时长超过此时长时，后台配置无效
     */
    private static final int MAX_VIP_FREE_TIME = 300000;
    /**
     * log Tag
     */
    private static final String TAG = "FonePlayerAdapter";
    /**
     * 视频播放类
     */
    private FonePlayer player;
    /**
     * 显示视频SurfaceView
     */
    private SurfaceView videoSurface;
    /**
     * 视频模版
     */
    private VideoMode mode;
    /**
     * 当前播放进度,单位毫秒
     */
    private int currentPostion;
    /**
     * 视频持续时间，单位毫秒
     */
    private int videoDuration;
    /**
     * 开一个线程池
     */
    private ExecutorService threadPool;
    /**
     * 线程池大小
     */
    private int poolSize = 5;
    /**
     * 是否正在播放
     */
    private boolean isPlaying;
    /**
     * 当前播放器播放数据
     */
    private TagFoneMediaInfo currentMedia;
    /**
     * 播放视频地址列表
     */
    private List<MultipleVideo> multipleVideos;
    /**
     * 当前播放地址索引
     */
    private int playIndex;
    /**
     * 播放数据
     */
    private XyzplaRst plaRst;
    /**
     * 视频宽
     */
    private int mediaWidth;
    /**
     * 视频高
     */
    private int mediaHeight;
    /**
     * 当前播放的xyzPlayUrl，或者本地视频文件的绝对地址
     */
    private String xyzPlayUrl;
    /**
     * 是否已经恢复了播放记录，每一个视频的第一次准备完成时进行恢复播放记录
     */
    private boolean hasResumedPlayRecord = false;
    /**
     * 资源服务器地址
     */
    private String shost;
    /**
     * 多片播放时播放的url索引
     */
    protected int airIndex;
    /**
     * 当前倍速索引 0-->0.8倍速,1-->正常速度,2-->1.5倍速,3-->1.8倍速,4-->2倍速
     */
    private int currentRate = 1;
    /**
     * 正在缓冲标志
     */
    private boolean buffering = false;
    /**
     * 是否是缓存视频
     */
    private boolean isCacheVideo = false;
    /**
     * 是否缓存完成
     */
    private boolean cacheFinish = false;
    /**
     * 投放设备集合锁
     */
    private Lock deviceMapLock = new ReentrantLock();
    /**
     * 是否来自原网页视频
     */
    private boolean fromWebView;
    /**
     * 播放器是否关闭完成
     */
    public static boolean isCloseSuccess = true;
    private Handler mHandler;
    /**
     * 是否打开成功
     */
    private boolean openDone = true;
    /**
     * 系统播放器打开是否
     */
    private boolean systemPlayerOpenFailed = false;
    /**
     * 播放器是否销毁
     */
    private boolean playerRelease = false;
    /**
     * 播放器是否停止
     */
    private boolean playerStop = false;
    /**
     * 是否正在seek
     */
    private boolean isSeeking;
    /**
     * 正在打开
     */
    private boolean opening;
    /**
     * 0:hw 1:sys 2:sw
     */
    private int engineType = -1;

    public FonePlayerAdapter(SurfaceView videoSurface) {
        super();
        this.videoSurface = videoSurface;
        mHandler = new Handler();
    }

    /**
     * 打开url并播放
     */
    private void open() {
        if (playerRelease) {
            return;
        }
        L.v(TAG, "wwb_message open");
        if (currentMedia == null || currentMedia.fragUrl == null || currentMedia.fragUrl.length == 0) {
            sendMessage2UI(FONE_PLAYER_MSG_VIDEO_OUT_LINE_TOAST);
            return;
        }
        try {
            L.v(TAG, "open url :" + currentMedia.fragUrl[0]);
            if (mode == VideoMode.LOCAL) {
                if (currentMedia.fragUrl[0].startsWith("/")) {
                    File f = new File(currentMedia.fragUrl[0]);
                    if (!f.exists()) {
                        Toast.makeText(TransPadApplication.getTransPadApplication(), R.string.player_full_file_not_exist, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
            playerStop = false;
            prepareOpen();
            player = new FonePlayer();
            player.open(currentMedia);
            player._setSurface(videoSurface.getHolder().getSurface());
            player.setOnPreparedListener(new PlayerOnPreparedListener()); // 当准备好的时候再播放
            player.setOnOpenSuccessListener(new FoneOnOpenSuccessListener());// 打开成功
            player.setOnOpenFailedListener(new FoneOnOpenFailedListener());// 打开失败
            player.setOnOpenPercentListener(new FoneOnOpenPercentListener());// 打开百分比
            player.setOnShowEngineTypeListener(new FoneOnShowEngineTypeLister());
            player.setOnNewSubtitleListener(new FoneOnNewSubtitleLister());
            player.setOnCloseSuccessListener(new FoneOnCloseSuccessListener());
            player.setOnOpenDoneListener(new FoneOnOpenDoneListener());
            sendMessage2UI(FONE_PLAYER_MSG_PLAYER_PREPARING);
            isPlaying = true;
            opening = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareOpen() {
        if (mode != VideoMode.LOCAL && !isCacheVideo) {
            setSystem_decoder(0);// 在线视频不支持硬解
            setHwPlusSupport(0);
            openDone = false;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 5秒超时后默认打开
                    openDone = true;
                    opening = false;
                }
            }, 5000);
        } else {
            setSystem_decoder(1);
            setHwPlusSupport(0);
        }
    }

    /**
     * 发送消息给UI
     *
     * @param msg
     */
    protected abstract void sendMessage2UI(Message msg);

    /**
     * 发送消息给UI
     */
    private void sendMessage2UI(int what) {
        L.v(TAG, "sendMessage2UI Messgae what:" + what);
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        sendMessage2UI(msg);
    }

    /**
     * 准备完成监听
     *
     * @author kongxiaojun
     * @since 2014-4-16
     */
    private class PlayerOnPreparedListener implements OnPreparedListener {
        @Override
        public <T> void onPrepared(FonePlayer fp, T mediaInfoObj) {
            L.v(TAG, "player onPrepared!");
            buffering = false;
            isSeeking = false;
            videoSurface.setVisibility(View.VISIBLE);
            if (player != null && !playerStop) {
                L.v(TAG, "player onPrepared! videoSurface setVisibility VISIBLE");
                player.play();
                TagFoneMediaDesc mediaInfo = (TagFoneMediaDesc) mediaInfoObj;
                mediaWidth = mediaInfo.m_nWidth;
                mediaHeight = mediaInfo.m_nHeight;
                L.v(TAG, "onPrepared Width = " + mediaWidth + " Height = " + mediaHeight);
                videoDuration = mediaInfo.m_nMediaDuration;
                videoSurface.getHolder().setFixedSize(mediaWidth, mediaHeight);
                L.v(TAG, "onPrep ared get videoDuration == " + videoDuration);
                player.setOnCompletionListener(new FonePlayerOnCompletionListener()); // 监听播放完成事件
                setSurfaceScale(0);
                sendMessage2UI(FONE_PLAYER_MSG_PLAYER_PREPARED);
            }
        }
    }

    /**
     * 播放完成监听器
     *
     * @author kongxiaojun
     * @since 2014-4-16
     */
    private class FonePlayerOnCompletionListener implements OnCompletionListener {
        @Override
        public void onCompletion(FonePlayer fp, int type) {
            L.v(TAG, "player onCompletion!");
            if (type == 1) {// 整片播放完成
                // 直接seek到结束已经不会有openSuccess消息，导致不能返回，所以在这里把openDone置为true
                openDone = true;
                L.v(TAG, "wwb_message new player onCompletion stop start");
                if (engineType != 1) {
                    player.stop(true); // 停止播放
                }
                L.v(TAG, "wwb_message player onCompletion stop end");
                if (currentPostion + 5000 < videoDuration && mode != VideoMode.LOCAL) {
                    // 网络可用，但是网络超时了再显示此提示
                    if (TPUtil.isNetOk()) {
                        Toast.makeText(TransPadApplication.getTransPadApplication(), R.string.fullplayer_media_buffer_timeout, Toast.LENGTH_SHORT).show();
                    }
                    if (engineType != 1) {
                        player.release(); // 释放资源
                        player = null; // 垃圾回收
                    }
                    L.v(TAG,"onCompletion() 1 savePlayRecord");
                    savePlayRecord();
                    updatePlayRecord2MediaInfo();
                } else {
                    currentPostion = videoDuration;
                    if (engineType != 1) {
                        player.release(); // 释放资源
                        player = null; // 垃圾回收
                    }
                    L.v(TAG,"onCompletion() 2 savePlayRecord");
                    savePlayRecord();
                }
                Reporter.logPlayerClose();
                sendMessage2UI(FONE_PLAYER_MSG_PLAYER_PLAY_COMPLETION);
                videoDuration = 0;
            } else {// 单片播放完成
                player.stop(false); // 停止播放
                if (player != null) {
                    player._setSurface(videoSurface.getHolder().getSurface());
                }
                sendMessage2UI(FONE_PLAYER_MSG_BUFFERING_START);
            }
        }
    }

    private class FoneOnOpenSuccessListener implements OnOpenSuccessListener {
        @Override
        public void onOpenSuccess(FonePlayer fp) {
            L.v(TAG, "onOpenSuccess");
            if (mode != VideoMode.LOCAL) {
                Reporter.logPlayerOpen(xyzPlayUrl, (byte) 4);
            }
            if (player != null) {
                openDone = true;
                opening = false;
                L.v(TAG, "onOpenSuccess videoSurface setVisibility VISIBLE");
                videoSurface.setVisibility(View.VISIBLE);
                TagFoneMediaDesc mediaInfo = (TagFoneMediaDesc) player.getDesc();
                mediaWidth = mediaInfo.m_nWidth;
                mediaHeight = mediaInfo.m_nHeight;
                L.v(TAG, "onOpenSuccess Width = " + mediaWidth + " Height = " + mediaHeight);
                videoDuration = mediaInfo.m_nMediaDuration;
                videoSurface.getHolder().setFixedSize(mediaWidth, mediaHeight);
                sendMessage2UI(FONE_PLAYER_MSG_PLAYER_OPEN_SUCCESS);
            }
        }
    }

    private class FoneOnOpenFailedListener implements OnOpenFailedListener {
        @Override
        // when sw player open failed, recv this.
        public void onOpenFailed(FonePlayer fp, int type) {
            openDone = true;
            opening = false;
            L.v(TAG, "onOpenFailed");
            if (type == 1) {
                systemPlayerOpenFailed = true;
                // 硬解码打开失败，会自动切换到软解
                videoSurface.setVisibility(View.INVISIBLE);
                if (player != null) {
                    L.v(TAG, "system player OpenFailed !");
                    player.stop(false);
                }
                return;
            }
            if (player != null) {
                playerStop = true;
                L.v(TAG,"onOpenFailed() savePlayRecord");
                savePlayRecord();
                hasResumedPlayRecord = false;
                isCloseSuccess = false;
                videoDuration = 0;
                L.v(TAG, "wwb_message player stopping");
                player.stop(true);
                player.release();
                player = null;
            }
            // }
            Message msg = new Message();
            msg.what = FONE_PLAYER_MSG_PLAYER_OPEN_FAILED;
            msg.arg1 = type;
            sendMessage2UI(msg);
        }
    }

    private class FoneOnOpenPercentListener implements OnOpenPercentListener {
        @Override
        public void onOpenPercent(FonePlayer fp, int percent) {
            if (mode != VideoMode.LOCAL) {
                // 显示打开的百分比
                if (!buffering) {
                    buffering = true;
                    sendMessage2UI(FONE_PLAYER_MSG_BUFFERING_START);
                }
                L.v(TAG, "onOpenPercent " + percent);
            }
        }
    }

    private class FoneOnShowEngineTypeLister implements OnShowEngineTypeListener {
        public void onEngineType(FonePlayer fp, int engineType) {
            // msg.arg1 0:hw 1:sys 2:sw
            FonePlayerAdapter.this.engineType = engineType;
            L.v("liyang", "onEngineType = " + engineType);
            if (player != null) {
                if (engineType == 1) {
                    videoSurface.setVisibility(View.VISIBLE);
                } else {
                    player._setSurface(videoSurface.getHolder().getSurface());
                }
            }
        }
    }

    private class FoneOnNewSubtitleLister implements OnNewSubtitleListener {

        @Override
        public void onNewSubtitle(String arg0) {
            // 新字幕
            Message msg = new Message();
            msg.what = FONE_PLAYER_MSG_PLAYER_UPDATE_SUBTITLE;
            msg.obj = arg0;
            sendMessage2UI(msg);
        }

    }

    private class FoneOnCloseSuccessListener implements OnCloseSuccessListener {
        @Override
        public void onCloseSuccess(FonePlayer fp) {
            L.v(TAG, "onCloseSuccess");
            L.v(TAG, "wwb_message new onCloseSuccess");
            openDone = false;

            // 关闭完成后才可以进行open()
            isCloseSuccess = true;
            // 开始所有暂停的下载任务(先注释,后期需要再解开)
            // StorageModule.getInstance().startAllCache(
            // StorageModule.MSG_ACTION_OPERATE_PROGRAM_TYPE);
        }

    }

    private class FoneOnOpenDoneListener implements OnOpenDoneListener {
        @Override
        public void onOpenDone(FonePlayer arg0) {
            L.v(TAG, "wwb_message onOpenDone");
            L.v(TAG, "wwb_message set false in open done");
            isCloseSuccess = false;
        }
    }

    @Override
    public void pause() {
        L.v(TAG, "pause");

        Reporter.logPlayerPause();

        if (player != null && player.isPlaying()) {
            player.pause();
            sendMessage2UI(FONE_PLAYER_MSG_PLAYER_PLAYING_PAUSE);
        }
    }

    @Override
    public synchronized void stop() {
        L.v(TAG, "stop");
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (player != null) {
            playerStop = true;
            savePlayRecord();
            hasResumedPlayRecord = false;
            isCloseSuccess = false;
            videoDuration = 0;
            player.stop(true);
            if (player != null) {
                player.release();
                player = null;
            }
        }
        opening = false;
        Reporter.logPlayerClose();
    }

    @Override
    public synchronized void release() {
        if (threadPool != null) {
            threadPool.shutdown();
            threadPool = null;
        }
        openDone = true;
        currentRate = 1;
        // 关闭省电加速
        setHwPlusSupport(0);
        setSystem_decoder(0);
    }

    @Override
    public void seekTo(int progress) {
        L.v(TAG, "seekTo");
        L.v(TAG, "wwb_message start seekTo = " + progress);
        if (mode != VideoMode.LIVE && player != null) {
            isSeeking = true;
            if (FonePlayer.getHwPlusSupport() == 0 && currentMedia.fragUrl.length > 1 && getPlayIndexByPostion(currentPostion) != getPlayIndexByPostion(progress)) {
                L.v(TAG, "FonePlayerAdapter seekto frag videoSurface INVISIBLE");
                openDone = false;
                // 不在同一片
                videoSurface.setVisibility(View.INVISIBLE);
            }
            currentPostion = progress;
            player.seekTo(progress);
            sendMessage2UI(FONE_PLAYER_MSG_BUFFERING_START);
        }
    }

    @Override
    public boolean isPlaying() {
        L.v(TAG, "isPlaying");
        if (player != null) {
            isPlaying = player.isPlaying();
            L.v(TAG, "isPlaying == " + isPlaying);
            return isPlaying;
        }
        return false;
    }

    public boolean isPause() {
        L.v(TAG, "isPause");
        if (player != null) {
            isPlaying = player.isPlaying();
            return !isPlaying;
        }
        return false;
    }

    @Override
    public VideoMode getVideoMode() {
        return mode;
    }

    @Override
    public int getMediaDuration() {
        if (player != null) {
            return videoDuration;
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (player != null && player.isPlaying()) {
            int pos = player.getCurrentPosition();
            if (pos > 0) {
                currentPostion = pos;
            }
        }
        L.v(TAG, "currentPostion == " + currentPostion);
        return currentPostion;
    }

    @Override
    public synchronized void play() {
        L.v(TAG, "play");
        if (player == null) {
            // 发送播放消息
            // 要先进行判断是否已经关闭完成，关闭完成了再进行下一次播放
            if (isCloseSuccess) {
                L.v(TAG, "wwb_message  at once open 11");
                openSystemDecoder();
                open();
            } else {
                startTime = System.currentTimeMillis();
                mHandler.postDelayed(openPlayerRunnable, 1);
            }
        } else {
            player.play(); // 继续播放
            sendMessage2UI(FONE_PLAYER_MSG_PLAYER_PLAYING_PAUSE);
        }
    }

    private void openSystemDecoder() {
        if (mode == VideoMode.LOCAL && !isCacheVideo) {
            L.v(TAG, "open System_decoder");
            setSystem_decoder(1);// 本地视频支持硬解
            videoSurface.setVisibility(View.VISIBLE);
            AndroidPlayer.Uim_Set_Player_Surface(videoSurface);
        } else {
            L.v(TAG, "close System_decoder");
            setSystem_decoder(0);
        }
    }

    private long startTime;

    private Runnable openPlayerRunnable = new Runnable() {
        @Override
        public void run() {
            L.v(TAG, "wwb_message while" + this.getClass());
            if (!playerRelease) {
                if (isCloseSuccess || System.currentTimeMillis() - startTime > 3000) {
                    L.v(TAG, "wwb_message  delay open" + this.getClass());
                    openSystemDecoder();
                    open();
                } else {
                    mHandler.postDelayed(this, 1);
                }
            }
        }
    };

    /**
     * 根据位置获取该位置属于第几片,从0开始
     *
     * @param postion
     * @return int 第几片 从0开始
     * @throws
     */
    private int getPlayIndexByPostion(int postion) {
        if (currentMedia != null) {
            if (currentMedia.fragDuration == null || currentMedia.fragDuration.length == 1 || postion == 0) {
                return 0;
            } else {
                int tempPostion = 0;
                for (int i = 0; i < currentMedia.fragDuration.length; i++) {
                    if (postion > tempPostion && postion < (tempPostion + currentMedia.fragDuration[i])) {
                        return i;
                    }
                    tempPostion += currentMedia.fragDuration[i];
                }
            }
        }
        return 0;
    }

    public void play(int index, List<MultipleVideo> multipleVideos) {
        this.multipleVideos = multipleVideos;
        mode = VideoMode.LOCAL;
        xyzPlayUrl = multipleVideos.get(index).getUrls()[0];
        this.playIndex = index;
//        updateCurrentMedia(multipleVideos.get(index).getUrls(), new int[]{0}, 0, 0);
        updateCurrentMedia(multipleVideos.get(index).getUrls(), multipleVideos.get(index).getDurations() == null ? new int[multipleVideos.size()] : multipleVideos.get(index).getDurations(), 0, 0);
        play();
    }

    private void updateCurrentMedia(String[] urls, int[] duiations, int isInstanceVideo, int isCachePlay) {
        currentMedia = new TagFoneMediaInfo();
        currentMedia.fragUrl = urls;
        currentMedia.fragDuration = duiations;
        currentMedia.isCachePlay = isCachePlay;
        currentMedia.isInstanceVideo = isInstanceVideo;
        updatePlayRecord2MediaInfo();
    }

    public void updatePlayRecord2MediaInfo() {
        if (currentMedia != null) {
            PlayRecord record = getPlayRecord(xyzPlayUrl);
            L.v(TAG, "updatePlayRecord2MediaInfo recoed = " + record);
            if (record != null && record.getPlayRecordTotalTime() > 0 && record.getPlayRecordAlreadyPlayTime() > 4000 && record.getPlayRecordAlreadyPlayTime() + 5000 < record.getPlayRecordTotalTime()) {
                currentMedia.seekPos = (int) record.getPlayRecordAlreadyPlayTime();
                // 恢复播放记录位置
                Message msg = new Message();
                msg.what = FONE_PLAYER_MSG_RESUME_PLAY_PROGRESS;
                L.v(TAG, "updatePlayRecord2MediaInfo currentMedia.seekPos = " + currentMedia.seekPos);
                currentPostion = currentMedia.seekPos;
                msg.arg1 = (int) record.getPlayRecordAlreadyPlayTime();
                msg.arg2 = (int) record.getPlayRecordTotalTime();
                sendMessage2UI(msg);
            } else {
                currentMedia.seekPos = 0;
            }
        }

    }

    /**
     * @return boolean
     * @throws
     */
    public boolean hasNext() {
        if (mode == VideoMode.LOCAL || mode == VideoMode.NETWORK) {
            L.v(TAG, "hasnext multipleVideos.size = " + multipleVideos.size() + "  playIndex = " + playIndex);
            // 本地或者网络视频
            if (multipleVideos.size() == 1) {
                // 只有一个视频
                // 没有下一部
                return false;
            } else {
                if (multipleVideos.size() <= playIndex + 1) {
                    return false;
                } else {
                    return true;
                }
            }
        } else {
            // 剧集下一部
            if (plaRst != null && !TextUtils.isEmpty(plaRst.nexturl)) {
                // 剧集切到下一集
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void next() {
        L.v(TAG, "next");
        hasResumedPlayRecord = false;
        if (mode == VideoMode.LOCAL) {
            if (multipleVideos.size() == 1) {// 本地或者网络视频
                // 只有一个视频
                // 没有下一部
                return;
            } else {
                if (multipleVideos.size() <= playIndex + 1) {
                    return;
                } else {
                    stop();
                    release();
                    playIndex++;
                }
                currentPostion = 0;
                xyzPlayUrl = multipleVideos.get(playIndex).getUrls()[0];
                updateCurrentMedia(multipleVideos.get(playIndex).getUrls(), multipleVideos.get(playIndex).getDurations() == null ? new int[multipleVideos.size()] : multipleVideos.get(playIndex).getDurations(), 0, 0);
                play();
            }
            return;
        } else {
            // 剧集下一部
            if (plaRst != null && !TextUtils.isEmpty(plaRst.nexturl)) {
                if (isPlaying) {
                    stop();
                    release();
                }
                currentPostion = 0;
                // 剧集切到下一集
                sendMessage2UI(FONE_PLAYER_MSG_PLAYER_SERIES_NEXT);
                return;
            } else {
                return;
            }
        }
    }

    /**
     * 是否有上一级
     */
    public boolean hasPrevious() {
        if (mode == VideoMode.LOCAL || mode == VideoMode.NETWORK) {
            // 本地或者网络视频
            if (multipleVideos.size() == 1) {
                // 只有一个视频
                // 没有上一部
                return false;
            } else {
                if (playIndex == 0) {
                    return false;
                } else {
                    return true;
                }
            }
        } else {
            // 剧集上一部
            if (plaRst != null && !TextUtils.isEmpty(plaRst.provurl)) {
                // 剧集是否有上一集
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 上一集
     */
    public void previous() {
        L.v(TAG, "next");
        hasResumedPlayRecord = false;
        if (mode == VideoMode.LOCAL) {
            if (multipleVideos.size() == 1) {// 本地或者网络视频
                // 只有一个视频
                // 没有下一部
                return;
            } else {
                if (playIndex <= 0) {
                    return;
                } else {
                    stop();
                    release();
                    playIndex--;
                }
                currentPostion = 0;
                xyzPlayUrl = multipleVideos.get(playIndex).getUrls()[0];
                updateCurrentMedia(multipleVideos.get(playIndex).getUrls(), multipleVideos.get(playIndex).getDurations() == null ? new int[multipleVideos.size()] : multipleVideos.get(playIndex).getDurations(), 0, 0);
                play();
            }
            return;
        } else {
            // 剧集下一部
            if (plaRst != null && !TextUtils.isEmpty(plaRst.provurl)) {
                if (isPlaying) {
                    stop();
                    release();
                }
                currentPostion = 0;
                // 剧集切到上一集
                sendMessage2UI(FONE_PLAYER_MSG_PLAYER_SERIES_PREVIOUS);
                return;
            } else {
                return;
            }
        }
    }

    public void setPlayerRst(XyzplaRst t, String xyzPlayUrl) {
        this.plaRst = t;
        this.xyzPlayUrl = xyzPlayUrl;
        if (t == null) {
            currentMedia = null;
            isCacheVideo = false;
            return;
        }
        String[] fragUrl = new String[t.cnt.fraglist.fragList.size()];
        int[] fragDuration = new int[t.cnt.fraglist.fragList.size()];
        for (int i = 0; i < t.cnt.fraglist.fragList.size(); i++) {
            fragUrl[i] = t.cnt.fraglist.fragList.get(i).url;
            fragDuration[i] = t.cnt.fraglist.fragList.get(i).t;
        }
        if (TextUtils.isEmpty(plaRst.dramaurl)) {
            mode = VideoMode.SINGLE;
        } else {
            mode = VideoMode.SERIES;
        }
        updateCurrentMedia(fragUrl, fragDuration, 0, 0);
        play();
        isCacheVideo = false;
    }

    /**
     * 设置3d
     *
     * @param state
     * @return boolean
     * @throws
     */
    public boolean set3DState(int state) {
        if (player != null) {
            return player._setPlay3D(state);
        }
        return false;
    }

    /**
     * 设置倍速
     *
     * @param rate
     * @return boolean
     * @throws
     */
    public void setPlayRate(int rate) {
        if (rate == 3) {// 底层3代表1.8倍速，而这里3就代表2.0倍速
            rate = 4;
        }
        if (player != null) {
            boolean play = false;
            if (player.isPlaying()) {
                play = true;
                player.pause();
            }
            if (player._setPlayRate(rate)) {
                currentRate = rate == 4 ? 3 : rate;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (play) {
                player.play();
            }
        }
    }

    /**
     * 获取所有音轨
     *
     * @return List<String>
     * @throws
     */
    public List<String> getAudioChannels() {
        if (player != null) {
            if (getAudioChannelCount() == 0) {
                return null;
            }
            List<String> channels = new ArrayList<String>();
            for (int i = 0; i < getAudioChannelCount(); i++) {
                channels.add(player._get_audio_channel_desc(i));
            }
            return channels;
        }
        return null;
    }

    /**
     * 获取音轨数量
     *
     * @return List<String>
     * @throws
     */
    public int getAudioChannelCount() {
        int count = 0;
        if (player != null) {
            return player._get_audio_channel_count();
        }
        return count;
    }

    /**
     * 获取所有字幕
     *
     * @return List<String>
     * @throws
     */
    public List<String> getSubChannels() {
        if (player != null) {
            List<String> channels = new ArrayList<String>();
            for (int i = 0; i < player._get_sub_channel_count(); i++) {
                channels.add(player._get_sub_channel_desc(i));
            }
            return channels;
        }
        return null;
    }

    /**
     * 获取字幕数量
     *
     * @return List<String>
     * @throws
     */
    public int getSubCount() {
        int count = 0;
        if (player != null) {
            return player._get_sub_channel_count();
        }
        return count;
    }

    /**
     * 获取当前字幕
     *
     * @return int
     * @throws
     */
    public int getCurrentSubChannel() {
        if (player != null) {
            return player._get_cur_sub_channel();
        }
        return 0;
    }

    /**
     * 设置当前字幕index
     *
     * @param sub
     * @return boolean
     * @throws
     */
    public boolean setCurrentSubChannel(int sub) {
        if (player != null) {
            player._set_cur_sub_channel(sub);
            return true;
        }
        return false;
    }

    /**
     * 获取当前音轨
     *
     * @return int
     * @throws
     */
    public int getCurrentAudioChannel() {
        if (player != null) {
            return player._get_cur_audio_channel();
        }
        return 0;
    }

    /**
     * 设置当前音轨
     *
     * @param channel
     * @return boolean
     * @throws
     */
    public boolean setCurrentAudioChannel(int channel) {
        if (player != null) {
            player._set_cur_audio_channel(channel);
            return true;
        }
        return false;
    }

    /**
     * 获取解码模式
     *
     * @return int 0.省电加速;1.硬解码;2.软解;-1.不能获取解码模式
     * @throws
     */
    public int getPlayerDecoderRole() {
        if (player != null) {
            return player._get_player_decoder_role();
        }
        return -1;
    }

    /**
     * 屏幕比例 0.等比例全屏;1.全屏拉伸;2.原始比例
     */
    private int frameScale = 0;

    public int getFrameScale() {
        return frameScale;
    }

    public static int getHwPlusSupport() {
        return FonePlayer.getHwPlusSupport();
    }

    public static void setHwPlusSupport(int state) {
        FonePlayer.setHwPlusSupport(state);
    }

    public static int getSystem_decoder() {
        return FonePlayer.getSystemDecoderSupport();
    }

    public static void setSystem_decoder(int system_decoder) {
        FonePlayer.setSystemDecoderSupport(system_decoder);
    }

    public XyzplaRst getPlaRst() {
        return plaRst;
    }

    /**
     * 获取缓存的位置
     *
     * @return int
     * @throws
     */
    public int getCachePosition() {
        if (player != null) {
            return player.getCachePosition();
        }
        return 0;
    }

    public String getShost() {
        return shost;
    }

    public void setShost(String shost) {

        this.shost = shost;
    }

    public void setCurrentPostion(int currentProgress) {
        this.currentPostion = currentProgress;
    }

    /**
     * 获取当前倍速索引 0-->0.8倍速,1-->正常速度,2-->1.5倍速,3-->1.8倍速,4-->2倍速
     *
     * @return int
     * @throws
     */
    public int getCurrentRate() {
        return currentRate;
    }

    public boolean isCacheVideo() {
        return isCacheVideo;
    }

    public void setCacheVideo(boolean isCacheVideo) {
        this.isCacheVideo = isCacheVideo;
    }

    public boolean isFromWebView() {
        return fromWebView;
    }

    public void setFromWebView(boolean fromWebView) {
        this.fromWebView = fromWebView;
    }

    /**
     * 提交线程到线程池
     *
     * @param runnable
     */
    public void submitRunable(Runnable runnable) {
        if (threadPool == null) {
            threadPool = Executors.newFixedThreadPool(poolSize);
        }
        threadPool.submit(runnable);
    }

    /**
     * 设置SurfaceView
     *
     * @param holder
     * @return void
     * @throws
     */
    public void setSurfaceView(SurfaceHolder holder) {
        if (player != null) {
            player._setSurface(holder.getSurface());
        }
    }

    public void setPlayerRelease(boolean playerRelease) {
        this.playerRelease = playerRelease;
    }

    public boolean isOpenSuccess() {
        return openDone;
    }

    public boolean isSeeking() {
        return isSeeking;
    }

    public boolean isSystemPlayerOpenFailed() {
        return systemPlayerOpenFailed;
    }

    public void setSystemPlayerOpenFailed(boolean systemPlayerOpenFailed) {
        this.systemPlayerOpenFailed = systemPlayerOpenFailed;
    }

    /**
     * 是否正在打开
     *
     * @return boolean
     * @throws
     */
    public boolean isOpening() {
        return opening;
    }

    /**
     * 保存视频播放记录
     */
    public void savePlayRecord() {
        L.v(TAG, "savePlayRecord");
        L.v(TAG, "saveVideoPlayRecord url = " + xyzPlayUrl + "   currentPostion = " + currentPostion);
        if (!TextUtils.isEmpty(xyzPlayUrl)) {
            PlayRecord playRecord = new PlayRecord();
            if (videoDuration - 5000 > currentPostion) {
                playRecord.setPlayRecordAlreadyPlayTime(currentPostion);
            } else {
                playRecord.setPlayRecordAlreadyPlayTime(videoDuration);
            }
            L.v(TAG, "saveVideoPlayRecord progress = " + playRecord.getPlayRecordAlreadyPlayTime());
            String cid = TPUtil.getCIdByUrl(xyzPlayUrl);
            if (!TextUtils.isEmpty(cid) && !cid.equals("0")) {
                playRecord.setPlayRecordCid(Long.parseLong(cid));
            }
            playRecord.setPlayRecordTotalTime(videoDuration);
            playRecord.setPlayRecordPlayUrl(xyzPlayUrl);
            StorageModule.getInstance().addPlayRecord(playRecord);
        }
    }

    /**
     * 获取播放记录
     *
     * @return
     */
    private PlayRecord getPlayRecord(String url) {
        L.v(TAG, "getVideoAlreadyPlayTime xyzPlayUrl = " + url);
        if (!TextUtils.isEmpty(url)) {
            String cid = TPUtil.getCIdByUrl(url);
            PlayRecord playRecord;
            if (TextUtils.isEmpty(cid) || cid.equals("0")) {
                playRecord = StorageModule.getInstance().getPlayRecordByPlayUrl(url);
            } else {
                playRecord = StorageModule.getInstance().getPlayRecordByCid(Long.parseLong(cid));
            }
            if (playRecord != null) {
                return playRecord;
            }
        }
        return null;
    }

    @Override
    public String getVideoName() {
        try {
            if (mode == VideoMode.LOCAL) {
                MultipleVideo video = multipleVideos.get(playIndex);
                if (!TextUtils.isEmpty(video.getName())) {
                    return video.getName();
                } else {
                    File file = new File(video.getUrls()[0]);
                    return file.getName();
                }
            } else {
                if (plaRst != null) {
                    return plaRst.cnt.name;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置屏幕显示比例，全屏模式时可用
     *
     * @param mode
     * @return void
     * @throws
     */
    public void setSurfaceScale(int mode) {
        int nScreenWidth = ScreenUtil.getScreenWidthPix(TransPadApplication.getTransPadApplication());
        int nScreenHeight = ScreenUtil.getScreenHeightPix(TransPadApplication.getTransPadApplication());
        if (nScreenWidth < nScreenHeight) {
            nScreenWidth = nScreenHeight;
            nScreenHeight = ScreenUtil.getScreenWidthPix(TransPadApplication.getTransPadApplication());
        }

        int width = 0;
        int height = 0;

        if (nScreenWidth < nScreenHeight) {
            int temp = nScreenWidth;
            nScreenWidth = nScreenHeight;
            nScreenHeight = temp;
        }
        switch (mode) {
            case 2:// 切换至原始比例
                if (nScreenWidth < mediaWidth || nScreenHeight < mediaHeight) {
                    float previewScale = (float) nScreenWidth / nScreenHeight;
                    float mediaScale = (float) mediaWidth / mediaHeight;
                    if (previewScale > mediaScale) {
                        width = (int) (nScreenHeight * mediaScale);
                        height = nScreenHeight;
                    } else {
                        width = nScreenWidth;
                        height = (int) (nScreenWidth / mediaScale);
                    }
                } else {
                    width = mediaWidth;
                    height = mediaHeight;
                }
                break;

            case 0: // 等比例全屏
                float previewScale = (float) nScreenWidth / nScreenHeight;
                float mediaScale = (float) mediaWidth / mediaHeight;
                if (previewScale > mediaScale) {
                    width = (int) (nScreenHeight * mediaScale);
                    height = nScreenHeight;
                } else {
                    width = nScreenWidth;
                    height = (int) (nScreenWidth / mediaScale);
                }
                break;

            case 1:// 全屏拉伸
                width = nScreenWidth;
                height = nScreenHeight;
                break;
        }
        Message msg = new Message();
        msg.what = FONE_PLAYER_MSG_PLAYER_UPDATE_SURFACEVIEW;
        msg.arg1 = width;
        msg.arg2 = height;
        sendMessage2UI(msg);
    }
}
