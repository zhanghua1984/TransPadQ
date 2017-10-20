package cn.transpad.transpadui.player;


import java.util.List;

import cn.transpad.transpadui.entity.MultipleVideo;

/**
 * 播放控制适配器接口
 * @author kongxiaojun
 * @since 2014-4-16
 *
 */
public interface IPlayerAdapter {

	/**缓冲超时时间*/
    public static final int PLAYER_PLAY_TIMEOUT_TIME            = 20000;
	/**播放器准备完毕*/
	public static final int FONE_PLAYER_MSG_PLAYER_PREPARED = 101;
	/**播放或暂停*/
	public static final int FONE_PLAYER_MSG_PLAYER_PLAYING_PAUSE = 102;
	/**准备中*/
	public static final int FONE_PLAYER_MSG_PLAYER_PREPARING = 103;
	/**播放完成*/
	public static final int FONE_PLAYER_MSG_PLAYER_PLAY_COMPLETION = 104;
	/** 视频获取错误*/
	public static final int FONE_PLAYER_MSG_VIDEO_GET_FAILURE = 110;
	/** 视频来源发生变化*/
	public static final int FONE_PLAYER_MSG_VIDEO_FROM_CHANGED = 111;
	/** 更新字幕*/
	public static final int FONE_PLAYER_MSG_PLAYER_UPDATE_SUBTITLE = 112;
	/** 更新SurfaceView宽高*/
	public static final int FONE_PLAYER_MSG_PLAYER_UPDATE_SURFACEVIEW = 113;
	/** 剧集请求下一集*/
	public static final int FONE_PLAYER_MSG_PLAYER_SERIES_NEXT = 114;
	/**回复播放进度成功*/
	public static final int FONE_PLAYER_MSG_RESUME_PLAY_PROGRESS = 116;
	/**不能播放了*/
	public static final int FONE_PLAYER_MSG_PLAY_TIMEOUT = 120;
	/**播放器打开失败，不能播放*/
	public static final int FONE_PLAYER_MSG_PLAYER_OPEN_FAILED = 122;
	/**缓冲开始*/
	public static final int FONE_PLAYER_MSG_BUFFERING_START = 124;
	/**视频暂时无法播放*/
	public static final int FONE_PLAYER_MSG_VIDEO_OUT_LINE_TOAST = 127;
	/**播放器打开文件成功，不能播放*/
	public static final int FONE_PLAYER_MSG_PLAYER_OPEN_SUCCESS = 128;
	/** 剧集请求上一集*/
	public static final int FONE_PLAYER_MSG_PLAYER_SERIES_PREVIOUS = 129;
	/***DLNA打开成功*/
	public static final int FONE_PLAYER_MSG_DLNA_OPEN_SUCCED = 130;
	/**DLNA打开失败*/
	public static final int FONE_PLAYER_MSG_DLNA_OPEN_FAILED = 131;
	/***DLNA开始打开*/
	public static final int FONE_PLAYER_MSG_DLNA_OPENING = 132;
	/***DLNA播放完成*/
	public static final int FONE_PLAYER_MSG_DLNA_PLAY_COMPLETION = 133;
	/***DLNA设备断开*/
	public static final int FONE_PLAYER_MSG_DLNA_DEVICE_DISCONNECT = 134;

	/**播放*/
	public void play();
	/**暂停*/
	public void pause();
	/**停止*/
	public void stop();
	/**下一集*/
	public void next();
	/**释放资源*/
	public void release();
	/**
	 * 滚动到某处播放
	 * @param progress 滚动的位置
	 */
	public void seekTo(int progress);
	/**是否正在播放*/
	public boolean isPlaying();
	/**
	 * 获取视频类型
	 * @return 视频类型
	 */
	public VideoMode getVideoMode();
	/**获取视频的持续时间*/
	public int getMediaDuration();
	/**获取当前播放时间*/
	public int getCurrentPosition();
	/**
	 * 设置url
	 * 
	 * @param index 播放urls索引
	 * @param multipleVideos 地址数组
	 * @return void
	 * @throws
	 */
	public void play(int index,List<MultipleVideo> multipleVideos);

	public String getVideoName();
}
