package cn.transpad.transpadui.http;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.List;

/**
 * @author 刘昆 (liukun@100tv.com)
 * @since 2014-04-22
 */

@Root(strict = false)
public class LinkvideoRst extends Rst {

	@Root(strict = false)
	public static class Rcmds implements Serializable {

		@ElementList(inline = true, entry = "rcmd", required = false)
		public List<Rcmd> rcmdList;


	}

	@Root(strict = false)
	public static class Rcmd implements Serializable {
		/** 影片名称 */
		@Attribute(required = false)
		public String name;

		/** 影片图片 */
		@Attribute(required = false)
		public String pic;

		/** 播放访问地址 */
		@Attribute(required = false)
		public String url;

		/** 视频详情页url */
		@Attribute(required = false)
		public String vturl;

		/** 1 当前播放视频 0 不是当前播放视频 */
		@Attribute(required = false)
		public int current;
		/** 简介*/
		@Attribute(required = false)
		public String desc;

	}

	/** 服务器地址 */
	@Element(required = false)
	public String host;

	/** 资源服务器地址 */
	@Element(required = false)
	public String shost;

	/** 1 规则剧集 2 电影 3 非规则剧集 */
	@Element(required = false)
	public String drama;

	/** 推荐(类似)影片列表 */
	@Element(required = false)
	public Rcmds rcmds;

}
