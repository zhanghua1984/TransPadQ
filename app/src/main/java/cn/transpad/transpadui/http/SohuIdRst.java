package cn.transpad.transpadui.http;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * 搜狐视频相关id
 *
 * @author kongxiaojun
 * @since 2014-8-20
 */
@Root(strict = false)
public class SohuIdRst extends Rst implements Serializable{

	@Element(required = false)
	public Ids idlist;

	@Root(strict = false)
	public static class Ids implements Serializable{

		/** 搜狐视频vid */
		@Element(required = false)
		public int cid;

		/** 搜狐视频vid */
		@Element(required = false)
		public long vid;

		/** 搜狐视频sid */
		@Element(required = false)
		public long sid;

		/** 搜狐直播视频tvid */
		@Element(required = false)
		public int tvid;

		/** 搜狐视频site */
		@Element(required = false)
		public int site;
	}

}
