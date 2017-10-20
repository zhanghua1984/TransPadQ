package cn.transpad.transpadui.player.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;

import com.fone.player.L;

import java.io.File;

import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.player.IAudioPlayerService;
import cn.transpad.transpadui.player.IPlayStateCallback;


/**
 * Created by Kongxiaojun on 2015/1/19.
 * 音频播放器服务
 */
public class AudioPlayerService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private static final String TAG = "AudioPlayerService";

    private String currentPlayUrl;

    private RemoteCallbackList<IPlayStateCallback> callbackList = new RemoteCallbackList<>();

    MediaPlayer mMediaPlayer = null;

    @Override
    public IBinder onBind(Intent intent) {

        IBinder mBinder = new IAudioPlayerService.Stub() {
            @Override
            public synchronized boolean open(MediaFile file) throws RemoteException {
                return AudioPlayerService.this.open(file);
            }

            @Override
            public synchronized boolean play() throws RemoteException {
                return AudioPlayerService.this.play();
            }

            @Override
            public synchronized boolean pause() throws RemoteException {
                return AudioPlayerService.this.pause();
            }

            @Override
            public synchronized void stop() throws RemoteException {
                AudioPlayerService.this.stop();
                AudioPlayerService.this.release();
            }

            @Override
            public synchronized boolean seetTo(int pos) throws RemoteException {
                return AudioPlayerService.this.seetkTo(pos);
            }

            @Override
            public synchronized String getCurrentPath() throws RemoteException {
                return AudioPlayerService.this.getCurrentPath();
            }

            @Override
            public synchronized int getCurrentPostion() throws RemoteException {
                return AudioPlayerService.this.getCurrentPostion();
            }

            @Override
            public synchronized int getDuration() throws RemoteException {
                return AudioPlayerService.this.getDuration();
            }

            @Override
            public synchronized boolean isPlaying() throws RemoteException {
                return AudioPlayerService.this.isPlaying();
            }

            @Override
            public synchronized void registerCallback(IPlayStateCallback cb) throws RemoteException {
                L.v(TAG, "registerCallback callbackList == null ? " + (callbackList == null));
                callbackList.register(cb);
            }

            @Override
            public synchronized void unRegisterCallback(IPlayStateCallback cb) throws RemoteException {
                callbackList.unregister(cb);
            }

        };

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        L.v(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        L.v(TAG, "onDestroy");
        super.onDestroy();
        stop();
        release();
        System.exit(0);
    }

    private synchronized boolean open(MediaFile file) {
        L.v(TAG, "open AudioFile = " + file);
        if (file != null && !TextUtils.isEmpty(file.getMediaFilePath())) {
            try {
                currentPlayUrl = file.getMediaFilePath();
                File playFile = new File(currentPlayUrl);
                if (playFile.exists()) {
                    MediaPlayer player = new MediaPlayer();
                    player.setOnCompletionListener(this);
                    player.setOnErrorListener(this);
                    player.setOnPreparedListener(this);
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.setDataSource(getApplicationContext(), Uri.fromFile(playFile));
                    player.prepareAsync();
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                onError(mMediaPlayer,1,1);
            }
        }
        return false;
    }

    private synchronized void stop() {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized boolean seetkTo(int pos) {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo(pos);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            onError(mMediaPlayer,1,1);
        }
        return false;
    }

    private synchronized boolean play() {
        L.v(TAG, "play");
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            onError(mMediaPlayer,1,1);
        }
        return false;
    }

    private synchronized boolean pause() {
        L.v(TAG, "pause");
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.pause();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private synchronized String getCurrentPath() {
        try {
            if (mMediaPlayer != null) {
                return currentPlayUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private synchronized int getCurrentPostion() {
        try {
            if (mMediaPlayer != null) {
                return mMediaPlayer.getCurrentPosition();
            }
        } catch (Exception e) {
            e.printStackTrace();
            onError(mMediaPlayer,1,1);
        }
        return 0;
    }

    private synchronized int getDuration() {
        try {
            if (mMediaPlayer != null) {
                return mMediaPlayer.getDuration();
            }
        } catch (Exception e) {
            e.printStackTrace();
            onError(mMediaPlayer,1,1);
        }
        return 0;
    }

    private synchronized boolean isPlaying() {
        try {
            if (mMediaPlayer != null) {
                return mMediaPlayer.isPlaying();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private synchronized void release() {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放完成
     *
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        try {
            currentPlayUrl = null;
            final int n = callbackList.beginBroadcast();
            for (int i = 0; i < n; i++) {
                callbackList.getBroadcastItem(i).onCompletion();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            try {
                callbackList.finishBroadcast();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        L.v(TAG, "onPrepared");
        try {
            mMediaPlayer = mp;
            mMediaPlayer.start();
            final int n = callbackList.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    callbackList.getBroadcastItem(i).onPrepared(mMediaPlayer.getDuration());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            onError(mp, 1, 1);
        } finally {
            try {
                callbackList.finishBroadcast();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        try {
            stop();
            release();
            final int n = callbackList.beginBroadcast();
            for (int i = 0; i < n; i++) {
                callbackList.getBroadcastItem(i).onOpenFailed();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            try {
                callbackList.finishBroadcast();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }
}
