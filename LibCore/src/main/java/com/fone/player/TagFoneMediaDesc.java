package com.fone.player;

public class TagFoneMediaDesc {
	public int m_IsAudioVideo;

	/** 文件格式 */
	public String m_strMediaMuxerFmt;

	/** 媒体文件播长度 */
	public int m_nMediaDuration;

	/** 视频编码格式 */
	public String m_strVideoSurfaceFmt;
	
	/** 原始视频资源高 */
	public int m_nHeight;
	
	/** 原始视频资源宽 */
	public int m_nWidth;
	
	/** 视频帧率 */
	public int m_nFramerate;
		
	/** 音频压缩格式 */
	public String m_strAudioSampleFmt;
	
	/** 声道数 */
	public int m_nAudioChannel;
	
	/** 音频采样率 */
	public int m_nAudioSampleRate;

	/** 歌曲名 */
	public String m_strSongName;

	/** 专辑名 */
	public String m_strSpecialName;
	
	/** 歌手名 */
	public String m_strSingerName;
	
	/** 码流 */
	public int m_nuiKbps;
	
	public Object value;


    @Override
    public String toString() {
        return "TagFoneMediaDesc{" +
                "m_IsAudioVideo=" + m_IsAudioVideo +
                ", m_strMediaMuxerFmt='" + m_strMediaMuxerFmt + '\'' +
                ", m_nMediaDuration=" + m_nMediaDuration +
                ", m_strVideoSurfaceFmt='" + m_strVideoSurfaceFmt + '\'' +
                ", m_nHeight=" + m_nHeight +
                ", m_nWidth=" + m_nWidth +
                ", m_nFramerate=" + m_nFramerate +
                ", m_strAudioSampleFmt='" + m_strAudioSampleFmt + '\'' +
                ", m_nAudioChannel=" + m_nAudioChannel +
                ", m_nAudioSampleRate=" + m_nAudioSampleRate +
                ", m_strSongName='" + m_strSongName + '\'' +
                ", m_strSpecialName='" + m_strSpecialName + '\'' +
                ", m_strSingerName='" + m_strSingerName + '\'' +
                ", m_nuiKbps=" + m_nuiKbps +
                ", value=" + value +
                '}';
    }
}