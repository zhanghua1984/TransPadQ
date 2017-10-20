package cn.transpad.transpadui.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.main.TransPadApplication;
import cn.transpad.transpadui.player.entity.AudioInfo;
import cn.transpad.transpadui.player.service.AudioPlayerService;
import cn.transpad.transpadui.storage.SharedPreferenceModule;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.PlayerUtil;

/**
 * Created by Kongxiaojun on 2015/1/19.
 * 音频播放器
 */
public class AudioPlayer {

    private static final String TAG = "AudioPlayer";

    IAudioPlayerService mService;

    private static AudioPlayer ourInstance;

    private Context mContext;

    private boolean isServiceConnetid;

    /**
     * 正在开启音频播放服务
     */
//    private boolean isStarting;

    private static final int MSG_SERVICE_CONNECT_CHECK = 1;

    private Handler mHandler;

    private boolean isOpening;

    /**
     * 正在播放文件
     */
    private MediaFile playingFile;

    /**
     * 播放列表
     */
    private List<MediaFile> playList;

    /**
     * 上一次播放的索引
     */
    private List<String> lastPlayIndexList = new ArrayList<String>();

    private int currentIndex;

    private boolean pause;

    public static AudioPlayer getInstance() {
        if (ourInstance == null) {
            ourInstance = new AudioPlayer(TransPadApplication.getTransPadApplication());
        }
        return ourInstance;
    }

    private AudioPlayer(Context context) {
        L.v(TAG, "AudioPlayer create");
        mContext = context;
        initHander();
    }

    private void initHander() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_SERVICE_CONNECT_CHECK://检查音频播放进程是否连接上
                        L.v(TAG, "MSG_SERVICE_CONNECT_CHECK");
                        open();
                        break;
                }
            }
        };
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            L.v(TAG, "onServiceConnected");
            isServiceConnetid = true;
//            isStarting = false;
            mService = IAudioPlayerService.Stub.asInterface(service);
            L.v(TAG, "onServiceConnected", "mService==" + mService);
            try {
                mService.registerCallback(playStateCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            lock.lock();
            try {
                if (playStateListenerList != null && playStateListenerList.size() > 0) {
                    for (AudioPlayStateListener listener : playStateListenerList) {
                        listener.onServiceConnected();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            L.v(TAG, "onServiceDisconnected");
            isServiceConnetid = false;
            mService = null;
        }
    };

    /**
     * 打开播放列表
     *
     * @param files 播放列表
     * @param index 播放第几个
     */
    public void open(List<MediaFile> files, int index) {
        L.v(TAG, "open Audio file = " + files);
        if (!isOpening) {
            lastPlayIndexList.clear();
            playList = files;
            currentIndex = index;
            playingFile = files.get(index);
            isOpening = true;
            open();
        }
    }

    /**
     * 下一曲，手动点击下一首时
     */
    public void next() {
        L.v(TAG, "next");
        if (playList == null || playList.size() == 0) {
            return;
        }
        stop();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        switch (getLoopType()) {
            case 0://顺序播放
            case 2://单曲循环
                if (currentIndex + 1 >= playList.size()) {
                    //最后一首
                    L.v(TAG, "已经播放到最后一首了！");
                    currentIndex = 0;
                    playingFile = playList.get(currentIndex);
                    open();
                } else {
                    lastPlayIndexList.add("" + currentIndex);
                    currentIndex++;
                    playingFile = playList.get(currentIndex);
                    open();
                }
                break;
            case 1://随机播放
                if (playList.size() == 1) {
                    //只有一首歌时的随机
                    open();
                } else {
                    //多首歌曲的随机
                    lastPlayIndexList.add("" + currentIndex);
                    Random random = new Random();
                    int randomIndex = currentIndex;
                    while (randomIndex == currentIndex) {
                        randomIndex = random.nextInt(playList.size());
                    }
                    currentIndex = randomIndex;
                    playingFile = playList.get(currentIndex);
                    open();
                }
                break;
        }
    }

    /**
     * 上一曲
     */
    public void previous() {
        if (playList == null || playList.size() == 0) {
            return;
        }
        stop();
        switch (getLoopType()) {
            case 0://顺序播放
            case 2://单曲循环
                if (currentIndex == 0) {
                    //第一首
                    L.v(TAG, "已经播放到第一首了！");
                    currentIndex = playList.size() - 1;
                    playingFile = playList.get(currentIndex);
                    open();
                } else {
                    if (lastPlayIndexList != null && lastPlayIndexList.size() > 0) {
                        lastPlayIndexList.remove(lastPlayIndexList.size() - 1);
                    }
                    currentIndex--;
                    if (currentIndex >= 0) {
                        playingFile = playList.get(currentIndex);
                        open();
                    }
                }
                break;
            case 1://随机播放
                if (playList.size() == 1) {
                    //只有一首歌时的随机
                    open();
                } else {
                    //多首歌曲的随机
                    if (lastPlayIndexList != null && lastPlayIndexList.size() > 0) {
                        currentIndex = Integer.parseInt(lastPlayIndexList.remove(lastPlayIndexList.size() - 1));
                    }
                    playingFile = playList.get(currentIndex);
                    open();
                }
                break;
        }
    }

    /**
     * 打开
     */
    private void open() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            L.v(TAG, "Environment.getExternalStorageState() = " + Environment.getExternalStorageState());
            Toast.makeText(mContext, R.string.sdcard_state_error, Toast.LENGTH_SHORT).show();
            isOpening = false;
            playingFile = null;
            playList.clear();
            currentIndex = 0;
            return;
        }
        try {
            L.v(TAG, "isServiceConnetid = " + isServiceConnetid + " mService  == null ?" + (mService == null));
            if (isServiceConnetid && mService != null) {
                isOpening = false;
                if (playingFile.getMediaFilePath().startsWith("file://")) {
                    playingFile.setMediaFilePath(playingFile.getMediaFilePath().substring(7));
                }
                mService.open(playingFile);
            } else {
                L.v(TAG, "isServiceConnetid = " + isServiceConnetid );
                if (!isServiceConnetid ) {
                    startAduioService();
                }
                mHandler.sendEmptyMessageDelayed(MSG_SERVICE_CONNECT_CHECK, 100);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放
     */
    public void play() {
        lock.lock();
        try {
            if (mService != null) {
                mService.play();
                pause = false;
                if (playStateListenerList != null && playStateListenerList.size() > 0) {
                    for (AudioPlayStateListener listener : playStateListenerList) {
                        listener.onPlayOrPause(true);
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 循环播放
     */
    public void round() {
        lock.lock();
        try {
            if (mService != null) {
                if (getCurrentMediaFile().getMediaFilePath().toLowerCase().endsWith(".mid")) {
                    stop();
                    open();
                } else {
                    mService.play();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        lock.lock();
        try {
            if (mService != null) {
                if (mService.pause()) {
                    pause = true;
                    if (playStateListenerList != null && playStateListenerList.size() > 0) {
                        for (AudioPlayStateListener listener : playStateListenerList) {
                            listener.onPlayOrPause(false);
                        }
                    }
                } else {
                    pause = false;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取当前播放位置
     *
     * @return
     */
    public int getCurrentPostion() {
        try {
            if (mService != null) {
                return mService.getCurrentPostion();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public MediaFile getCurrentMediaFile() {
        try {
            if (playList != null && playList.size() > 0) {
                return playList.get(currentIndex);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * 获取正在播放的音频信息
     *
     * @return
     */
    public AudioInfo getAudioInfo(Context context) {
        try {
            if (mService != null) {
                AudioInfo audioInfo = new AudioInfo();
                audioInfo.path = mService.getCurrentPath();
                audioInfo.mediaDuration = mService.getDuration();

                if (!TextUtils.isEmpty(audioInfo.path)) {
                    PlayerUtil.readAudioHeader(context, audioInfo);
                    L.v(TAG, "audio info = " + audioInfo);
                    return audioInfo;
                } else {
                    return null;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 是否正在播放
     *
     * @return
     */
    public boolean isPlaying() {
        L.v(TAG, "isPlaying");
        try {
            if (mService != null) {
                L.v(TAG, "mService != null && " + mService.isPlaying());
                return mService.isPlaying();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 移动位置
     *
     * @param pos
     */
    public void seekTo(int pos) {
        L.v(TAG, "seek to = " + pos);
        if (mService != null) {
            try {
                mService.seetTo(pos);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        L.v(TAG, "stop");
        try {
            if (mService != null) {
                mService.stop();
                pause = false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * 设置循环模式
     *
     * @param type 0:顺序播放；1：随机播放；2：单曲循环
     */
    public void setLoopType(int type) {
        SharedPreferenceModule.getInstance().setInt("looptype", type);
    }

    /**
     * 获取当前循环模式
     *
     * @return 0:顺序播放；1：随机播放；2：单曲循环
     */
    public int getLoopType() {
        return SharedPreferenceModule.getInstance().getInt("looptype");
    }

    /**
     * 开启音频播放服务
     */
    private synchronized void startAduioService() {
        L.v(TAG, "startAduioService");
        if (!isServiceConnetid && mService == null ) {
            L.v(TAG, "startAduioService", "bindService");
//            isStarting = true;
            Intent intent = new Intent(mContext, AudioPlayerService.class);
            mContext.bindService(intent, conn, Context.BIND_AUTO_CREATE);
            L.v(TAG, "startAduioService", "bindService end");
        }
    }

    /**
     * 关闭音频播放服务
     */
    public synchronized void stopAduioService() {
        L.v(TAG, "stopAduioService");
        lock.lock();
        try {

            isServiceConnetid = false;

            if (mService != null) {
                mContext.unbindService(conn);
                mService.unRegisterCallback(playStateCallback);
                mService.stop();
                mService = null;
            }

            //保存播放列表，以便下次打开时使用
            savePlayRecordList();

            if (playList != null) {
                playList.clear();
            }
            pause = false;
            currentIndex = 0;
//            ActivityManager am = (ActivityManager) mContext.getSystemService(Activity.ACTIVITY_SERVICE);
//            List<ActivityManager.RunningServiceInfo> list = am.getRunningServices(100);
//            for (ActivityManager.RunningServiceInfo info : list) {
//                if ((mContext.getPackageName() + ":audioplayer").equals(info.process)) {
//                    L.i(TAG, "", "found process kill process");
//                    android.os.Process.killProcess(info.pid);
//                    break;
//                }
//            }
            if (playStateListenerList != null && playStateListenerList.size() > 0) {
                for (AudioPlayStateListener listener : playStateListenerList) {
                    listener.onAudioServiceStop();
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public List<MediaFile> readPlayRecordList() {
        String audioplaylist = SharedPreferenceModule.getInstance().getString("audioplaylist");
        List<MediaFile> mediaFiles = null;
        if (!TextUtils.isEmpty(audioplaylist)) {
            Gson gson = new Gson();
            mediaFiles = gson.fromJson(audioplaylist, new TypeToken<List<MediaFile>>() {
            }.getType());
        }
        return mediaFiles;
    }

    public int readPlayRecordIndex() {
        return SharedPreferenceModule.getInstance().getInt("audioplayindex", 0);
    }


    /**
     * 保存播放列表到SharedPreference
     */
    public void savePlayRecordList() {
        L.v(TAG, "savePlayRecordList");
        if (playList != null) {
            Gson gson = new Gson();
            String result = gson.toJson(playList);
            L.v(TAG, "savePlayRecord playlist = " + result);
            SharedPreferenceModule.getInstance().setString("audioplaylist", result);
            L.v(TAG, "savePlayRecord currentIndex = " + currentIndex);
            SharedPreferenceModule.getInstance().setInt("audioplayindex", currentIndex);
        }
    }

    private IPlayStateCallback playStateCallback = new IPlayStateCallback.Stub() {
        @Override
        public void onPostionChanged(int postion) throws RemoteException {
            lock.lock();
            try {
                if (playStateListenerList != null && playStateListenerList.size() > 0) {
                    for (AudioPlayStateListener listener : playStateListenerList) {
                        listener.onPostionChanged(postion);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void onOpenFailed() throws RemoteException {
            lock.lock();
            try {
                if (playStateListenerList != null && playStateListenerList.size() > 0) {
                    for (AudioPlayStateListener listener : playStateListenerList) {
                        listener.onOpenFailed(0);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void onPrepared(int duration) throws RemoteException {
            L.v(TAG, "duration = " + duration);
            savePlayRecordList();
            lock.lock();
            try {
                if (playStateListenerList != null && playStateListenerList.size() > 0) {
                    for (AudioPlayStateListener listener : playStateListenerList) {
                        listener.onPrepared(duration);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void onCompletion() throws RemoteException {
            if (getLoopType() == 2) {
                //进行单曲循环，不跳下一首
                round();
            } else {
                next();
            }
            lock.lock();
            try {
                if (playStateListenerList != null && playStateListenerList.size() > 0) {
                    for (AudioPlayStateListener listener : playStateListenerList) {
                        listener.onCompletion();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

    };

    public interface AudioPlayStateListener {

        /**
         * 当音频服务连接上/音频服务打开成功
         */
        void onServiceConnected();

        /**
         * 播放进度改变
         */
        void onPostionChanged(int postion);

        /**
         * 打开失败
         *
         * @param type
         */
        void onOpenFailed(int type);

        /**
         * 打开成功
         */
        void onOpenSuccess();

        /**
         * 准备完成
         *
         * @param duration
         */
        void onPrepared(int duration);

        /**
         * 播放完成
         */
        void onCompletion();

        /**
         * 播放或者暂停
         *
         * @param play
         */
        void onPlayOrPause(boolean play);

        /**
         * 音频服务停止
         */
        void onAudioServiceStop();
    }

    private List<AudioPlayStateListener> playStateListenerList = new ArrayList<AudioPlayStateListener>();

    private Lock lock = new ReentrantLock();

    /**
     * 注册音频播放状态监听
     *
     * @param audioPlayStateListener
     */
    public void registerAudioPlayStateListener(AudioPlayStateListener audioPlayStateListener) {

        lock.lock();
        try {
            playStateListenerList.add(audioPlayStateListener);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 取消注册播放状态监听
     *
     * @param audioPlayStateListener
     */
    public void unRegisterAudioPlayStateListener(AudioPlayStateListener audioPlayStateListener) {
        lock.lock();
        try {
            if (playStateListenerList != null && playStateListenerList.size() > 0) {
                playStateListenerList.remove(audioPlayStateListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 是否暂停
     *
     * @return
     */
    public boolean isPause() {
        L.v(TAG, "isPause = " + pause);
        return pause;
    }
}
