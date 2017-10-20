package com.fone.player;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileInputStream;
import java.util.Timer;
import java.util.TimerTask;


public class AndroidPlayer implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnVideoSizeChangedListener {

	private static final String TAG = "AndroidPlayer";

	private MediaPlayer mMediaPlayer = null;
	private static SurfaceView mSurfaceView;
	private static boolean mbMediainfo_ok;
	private static boolean mbMediaAvailable = false;
	private static boolean mbAtomicSeek = true;
	private static boolean mUIReady = false;

	private final static int mEventPrepared = tagFone_player_ui_message.FN_PLAYER_MESSAGE_READY_TO_PLAY.ordinal();// 1;
	private final static int mEventPlayBackComplete = tagFone_player_ui_message.FN_PLAYER_MESSAGE_END_OF_FILE.ordinal();// 2;
	private final static int mEventBufferingUpdate = tagFone_player_ui_message.FN_PLAYER_MESSAGE_BUFFERING_PERCENT.ordinal();// 3;
	private final static int mEventSeekComplete = tagFone_player_ui_message.FN_PLAYER_MESSAGE_SEEK_THUMBNAIL.ordinal();// 7;
	private final static int mEventBufferingStart = tagFone_player_ui_message.FN_PLAYER_MESSAGE_BUFFERING_START.ordinal();// 6;
	private final static int mEventProgressInd = tagFone_player_ui_message.FN_PLAYER_MESSAGE_MEDIA_CURRENT_POS.ordinal();// 9;
	private final static int mInfoVideoSizeChanged = 1;
	private final static int mEventError = tagFone_player_ui_message.FN_PLAYER_MESSAGE_OPEN_FAILED.ordinal();// 100;
	private final static int mOpenSuccess = tagFone_player_ui_message.FN_PLAYER_MESSAGE_OPEN_SUCCESS.ordinal();// 100;
	private final static int mCloseSuccess = tagFone_player_ui_message.FN_PLAYER_MESSAGE_CLOSE_SUCCESS.ordinal();// 100;
	private final static int mEventInfo = 200;
	private Timer mTimer = null;
	private MyTimerTask mTimerTask = null;

	static enum tagFone_player_ui_message {
		FN_PLAYER_MESSAGE_NONE, FN_PLAYER_MESSAGE_OPEN_SUCCESS, FN_PLAYER_MESSAGE_OPEN_FAILED, FN_PLAYER_MESSAGE_PAUSE_RESULT, FN_PLAYER_MESSAGE_BUFFERING_START, FN_PLAYER_MESSAGE_BUFFERING_PERCENT, FN_PLAYER_MESSAGE_READY_TO_PLAY, FN_PLAYER_MESSAGE_SEEK_THUMBNAIL, FN_PLAYER_MESSAGE_END_OF_FILE, FN_PLAYER_MESSAGE_MEDIA_CURRENT_POS, FN_PLAYER_MESSAGE_MEDIA_CACHED_POS, FN_PLAYER_MESSAGE_NOTIFICATION, FN_PLAYER_MESSAGE_DISPLAY_FRAME, FN_PLAYER_CONVERTER_PERCENT, FN_PLAYER_CONVERTER_RETRUE, FN_PLAYER_MESSAGE_HW_START_ERR, FN_PLAYER_MESSAGE_CLOSE_SUCCESS, FN_PLAYER_MESSAGE_AUTHORIZED, FN_PLAYER_MESSAGE_END
	};

	static AndroidPlayer instance;

	protected AndroidPlayer() {
		mbMediainfo_ok = false;
		instance = null;
		instance = this;
	}

	/**
	 * set Surface
	 */
	public static void Uim_Set_Player_Surface(SurfaceView surfaceview) {
		L.i(TAG, "set Surface", " Uim_Set_Player_Surface set surface");
		mUIReady = false;
		mSurfaceView = null;
		mSurfaceView = surfaceview;
	}

	public boolean media_close() {
		L.i(TAG, "media_close", "");
		if (perper_task != null) {
			perper_task.cancel();
			perper_task = null;
		}
		try {
			if (mTimerTask != null) {
				mTimerTask.cancel();
				mTimerTask = null;
			}
			if (mTimer != null) {
				mTimer.purge();
			}
			if (mMediaPlayer != null) {
				mbMediaAvailable = false;
				mMediaPlayer.reset();
				mMediaPlayer.setDisplay(null);
				mMediaPlayer.release();
				// modifi for 31736 special device surfaceview was not free
				// if( mSurfaceView !=null ) {
				// int viewStat=mSurfaceView.getVisibility();
				// mSurfaceView.setVisibility(View.INVISIBLE);
				// mSurfaceView.setVisibility(viewStat);
				// }
				mMediaPlayer = null;
			}
			return true;
		} catch (Exception e) {
			// err(LOGTAG, e.getMessage());
			return false;
		}
	}

	public int media_get_audio_volume_value() {
		// TODO media_get_audio_volume_value
		return 0;
	}

	public int media_get_current_playpos() {
		int ret = -1;
		try {
			ret = mMediaPlayer.getCurrentPosition();
		} catch (Exception e) {
			// err(LOGTAG, "getCurrentPosition()");
		}
		return ret;
	}

	public int getVideoHeight() {

		int ret = -1;
		try {
			ret = mMediaPlayer.getVideoHeight();
			if(ret <= 0){
				throw new Exception("getVideoHeight == 0");
			}
		} catch (Exception e) {
			fone_sys_player_notify_func_java(mEventError, 0, 0);
			L.e(TAG, "Exception", " getVideoHeight()");
		}
		L.i(TAG, "getVideoHeight", " media_height:" + ret);
		return ret;
	}

	public int getVideoWidth() {
		int ret = -1;
		try {
			ret = mMediaPlayer.getVideoWidth();
			if(ret <= 0){
				throw new Exception("getVideoHeight == 0");
			}
		} catch (Exception e) {
			fone_sys_player_notify_func_java(mEventError, 0, 0);
			L.e(TAG, "Exception", " getVideoWidth()");
		}
		L.i(TAG, "getVideoWidth", " media_width:" + ret);
		return ret;
	}

	public int media_get_total_playpos() {

		int ret = -1;
		try {
			ret = mMediaPlayer.getDuration();
		} catch (Exception e) {
			L.e(TAG, "Exception", e.toString());
			fone_sys_player_notify_func_java(mEventError, 0, 0);
		}
		L.i(TAG, "total playpos", " media_get_total_playpos duration: " + ret);
		return ret;
	}

	public boolean media_init() {
		L.i(TAG, "media_init", "");
		errored = false;
		// initSurface();
		media_uninit();
		mMediaPlayer = new MediaPlayer();
		if (null != mMediaPlayer) {
			mMediaPlayer.setOnBufferingUpdateListener(this);
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnErrorListener(this);
			mMediaPlayer.setOnInfoListener(this);
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setOnSeekCompleteListener(this);
			mMediaPlayer.setOnVideoSizeChangedListener(this);
			mMediaPlayer.reset();
		}

		// if (mTimerTask == null)
		// {
		// mTimerTask = new MyTimerTask();
		// }

		return true;
	}

	public boolean media_isPlaying() {
		try {
			return mMediaPlayer.isPlaying();
		} catch (Exception e) {
			// err(LOGTAG, e.getMessage());
			return false;
		}
	}

	String mPath;

	public boolean media_open(final String path) {

		new Thread() {
			public void run() {
				L.i(TAG, "media_open ", "path " + path);
				if (null == mMediaPlayer) {
					mMediaPlayer = new MediaPlayer();
					mMediaPlayer.setOnBufferingUpdateListener(AndroidPlayer.this);
					mMediaPlayer.setOnCompletionListener(AndroidPlayer.this);
					mMediaPlayer.setOnErrorListener(AndroidPlayer.this);
					mMediaPlayer.setOnInfoListener(AndroidPlayer.this);
					mMediaPlayer.setOnPreparedListener(AndroidPlayer.this);
					mMediaPlayer.setOnSeekCompleteListener(AndroidPlayer.this);
					mMediaPlayer.setOnVideoSizeChangedListener(AndroidPlayer.this);
					mMediaPlayer.reset();
				}
				mbMediainfo_ok = false;

				try {
					TimerTask timeout = new TimerTask() {

						@Override
						public void run() {
							L.i(TAG, "media_prepare", "onError");
							onError(null, 0, 0);
						}

					};
					Timer timer = new Timer();
					timer.schedule(timeout, 3000);
					mPath = path;
					if (path.startsWith("/"))
						mMediaPlayer.setDataSource((new FileInputStream(new File(path))).getFD());
					else
						mMediaPlayer.setDataSource(path);
					timer.cancel();
					timer = null;

					mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					mbMediaAvailable = false;

					media_prepare();

				} catch (Exception e) {
					e.printStackTrace();
					media_uninit();
					L.e(TAG, "Exception", " open fiail " + e.getMessage());
					fone_sys_player_notify_func_java(mEventError, 0, 0);
				}
			}
		}.start();

		return true;
	}

	public boolean media_pause() {
		L.i(TAG, "media_pause", "");
		try {
			if (mMediaPlayer.isPlaying())
				mMediaPlayer.pause();
			return true;
		} catch (Exception e) {
			fone_sys_player_notify_func_java(mEventError, 0, 0);
			return false;
		}
	}

	public synchronized boolean media_play() {
		L.i(TAG, "media_play", "");
		try {
			if (mSurfaceView != null && mSurfaceView.getVisibility() == View.VISIBLE) {
				L.v(TAG, "mMediaPlayer.setDisplay != null");
				mMediaPlayer.setDisplay(mSurfaceView.getHolder());
			}
			mMediaPlayer.start();
			if (mTimer != null) {
				L.i(TAG, "media_play", "mTimer is not null");
				mTimer.purge();
			} else {
				L.i(TAG, "media_play", "mTimer is null");
				mTimer = new Timer();
			}
			if (null != mTimerTask) {
				mTimerTask.cancel();
			}
			mTimerTask = new MyTimerTask();
			mTimer.schedule(mTimerTask, 0, 250);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
//			media_uninit();
			fone_sys_player_notify_func_java(mEventError, 0, 0);
			return false;
		}
	}

	int mSeek;

	public int media_seek_to(int ms) {
		mSeek = ms;
		L.i(TAG, "media_seek_to", "");
		mbAtomicSeek = false;
		if (mbMediaAvailable) {
			try {
				mMediaPlayer.seekTo(ms);
				fone_sys_player_notify_func(mEventPrepared, 0, 0);
			} catch (Exception e) {
				L.e(TAG, "Exception ", " seek fiail " + e.getMessage());
				mbMediaAvailable = false;
			}
		} else {
			try {
				if (ms > 0)
					mMediaPlayer.seekTo(ms);
			} catch (Exception e) {
				fone_sys_player_notify_func_java(mEventError, 0, 0);
				L.e(TAG, "Exception ", " androidplayer seek error " + e.getMessage());
			}
		}
		return 0;
	}

	public int media_set_audio_volume_value(int value) {
		/* not used */
		return 0;
	}

	public void media_set_video_display(int left, int top, int right, int bottom, boolean full) {
		L.i(TAG, "video display", " media_set_video_display left right:" + left + " " + right);
		if (null != mSurfaceView) {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
			lp.leftMargin = left;
			lp.topMargin = top;
			lp.width = right - left;
			lp.height = bottom - top;
			mSurfaceView.setLayoutParams(lp);
		}
		// mHolder.setFixedSize(right-left, bottom-top);
	}

	static Timer prepare_timer = new Timer();
	static TimerTask perper_task;

	public boolean media_prepare() {
		try {
			L.i(TAG, "AndroidPlayermedia_prepare", "");
			perper_task = new TimerTask() {
				@Override
				public void run() {
					L.i(TAG, "media_prepare", "onError");
					onError(null, 0, 0);
				}
			};
			prepare_timer.schedule(perper_task, 5000);
			mMediaPlayer.prepare();
		} catch (Exception e) {
			L.e(TAG, "Exception", " media_prepare error " + e.toString());
			fone_sys_player_notify_func_java(mEventError, 0, 0);
		}
		return true;
	}

	public boolean media_stop() {
		L.i(TAG, "media_stop", "");
		try {
			if (mTimerTask != null) {
				mTimerTask.cancel();
				mTimerTask = null;
			}
			if (mTimer != null) {
				mTimer.purge();
				mTimer = null;
			}
			if (mMediaPlayer != null)
				mMediaPlayer.stop();

			L.e(TAG, "Exception", "sunhuazhu: media_close success send");
			fone_sys_player_notify_func(mCloseSuccess, 0, 0);
			return true;
		} catch (Exception e) {
			// err(LOGTAG, e.getMessage());
			return false;
		}
	}

	public boolean media_uninit() {
		// mSurfaceView = null; //多片释放mSurfaceView 会导致画面不刷新
		L.i(TAG, "media_uninit", "");
		mSeek = 0;
		if (perper_task != null) {
			perper_task.cancel();
			perper_task = null;
		}
		try {
			if (null != mMediaPlayer) {
				media_close();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		fone_sys_player_notify_func(mEventBufferingUpdate, 0, 0);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		L.i(TAG, "onError", " jni_java onCompletion");
		fone_sys_player_notify_func(mEventPlayBackComplete, 0, 0);
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		L.i(TAG, "onError", " jni_java onError what:" + what);
		if (mMediaPlayer != null) {
			media_uninit();
			fone_sys_player_notify_func_java(mEventError, 0, 1);
		}
		return false;
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		// fone_sys_player_notify_func_java(mEventInfo, what, extra);
		L.i(TAG, "onInfo", " jni_java onInfo what:" + what + " extra:" + extra);
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mbMediainfo_ok = true;
		mbMediaAvailable = true;
		if (perper_task != null) {
			perper_task.cancel();
			perper_task = null;
		}
		fone_sys_player_notify_func(mOpenSuccess, 0, 0);
		fone_sys_player_notify_func(mEventPrepared, 0, 0);
		L.i(TAG, "onPrepared", " send to jni ready to play");
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		/* not used */

		if (!mbAtomicSeek) {
			fone_sys_player_notify_func(mEventSeekComplete, 0, 0);
			mbAtomicSeek = true;
		}
		mSeek = 0;
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

		// boolean isAudio = false;
		// if (!isAudio)
		// {
		// fone_sys_player_notify_func_java(mEventInfo, mInfoVideoSizeChanged,
		// 0);
		// }
	}

	int last;

	class MyTimerTask extends TimerTask {

		int count;

		@Override
		public void run() {
			if (mMediaPlayer == null /* || mListener == null */)
				return;
			try {
				if (mMediaPlayer.isPlaying()) {
					int pos = mMediaPlayer.getCurrentPosition();
					if (pos == last && last != 0) {
						count++;
						if (count > 5) {
							count = 0;
							media_uninit();
							fone_sys_player_notify_func_java(mEventError, 0, 1);
						}
					} else {
						if (mSeek != 0) {
							return;
						}
						count = 0;
						last = pos;
						fone_sys_player_notify_func(mEventProgressInd, 0, pos);
					}
				}
			} catch (Exception e) {
				L.e(TAG, "Exception", " timer error:" + e.getMessage());
				// e.printStackTrace();
			}
		}
	}

	static boolean errored = false;

	public static void fone_sys_player_notify_func_java(int msg, int arg1, int arg2) {
		if (!errored) {
			errored = true;
			fone_sys_player_notify_func(msg, arg1, arg2);
		}
	}

	public native static void fone_sys_player_notify_func(int msg, int arg1, int arg2);
}
