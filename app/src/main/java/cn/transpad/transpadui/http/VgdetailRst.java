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
public class VgdetailRst extends Rst implements Serializable{

	/***/
	private static final long serialVersionUID = 1L;

	@Root(strict = false)
	public static class Gameinfo implements Serializable{

		/** 名称 */
		@Attribute(required = false)
		public String name;

		/** 游戏分享地址 */
		@Attribute(required = false)
		public String gsurl;

		/** 游戏海报页 */
		@Attribute(required = false)
		public String gamepic;

		/** 游戏版本 */
		@Attribute(required = false)
		public String version;

		/** 安装包 id */
		@Attribute(required = false)
		public String pkid;

		/** Apk versioncode 版本 */
		@Attribute(required = false)
		public String vcode;

		/** 安装包名称 */
		@Attribute(required = false)
		public String pkname;

		/** 文件大小 */
		@Attribute(required = false)
		public String filesize;

		/** 下载游戏地址 */
		@Attribute(required = false)
		public String durl;

		/** 载/安装成功上报地址,访问软件 下载及上报接口 */
		@Attribute(required = false)
		public String dlrpt;

		/** 游戏简介 */
		@Attribute(required = false)
		public String intro;
	}

	@Root(strict = false)
	public static class Froms implements Serializable{
		@ElementList(inline = true, entry = "from", required = false)
		public List<From> fromList;
	}

	@Root(strict = false)
	public static class From implements Serializable{

		/**集数*/
		@Attribute(required = false)
		public int updatenum;

		/**收藏类型*/
		@Attribute(required = false)
		public int favtyp;

		/**来源id*/
		@Attribute(required = false)
		public long fromid;

		/**更新信息*/
		@Attribute(required = false)
		public String updateDesc;

		/** 来源,例如:优酷、土豆 */
		@Attribute(required = false)
		public String from;

		/** 来源图片地址,带时间戳 */
		@Attribute(required = false)
		public String logo;

		/** 连接速度 1:快 2:慢 3:差 */
		@Attribute(required = false)
		public String speed;

		/** 清晰度类型,逗号分隔,例如: 1,2,3 表示同时存在标清、高清、超 清 */
		@Attribute(required = false)
		public String dfnt;

		/** 原始页地址 */
		@Attribute(required = false)
		public String ourl;

		/** 0:直接播放 1:去原网页 */
		@Attribute(required = false)
		public int toply;

		/** 显示播放按钮 0:不显示 1:显 示(默认) */
		@Attribute(required = false)
		public int btnply;

		/** 显示下载按钮 0:不显示 1:显 示(默认) */
		@Attribute(required = false)
		public int btndown;

		/** 分享地址 */
		@Attribute(required = false)
		public String weibourl;

		/** 下载地址(下载地址/剧集地址)如 果是剧集则是剧集接口地址;如果 是电影则是电影下载地址和 defaulturl 相同 */
		@Attribute(required = false)
		public String durl;

		/** ?默认播放地址 */
		@Attribute(required = false)
		public String defaulturl;

		/**
		 * 追播状态 0 不能追播 1 已取消,显示追播按钮 2 已追播,显示取消按钮
		 */
		@Attribute(required = false)
		public String state;

		/** 追播地址或取消地址,访问追剧接 口,客户端根据实际状态填写 flag 值 */
		@Attribute(required = false)
		public String churl;

		/** 当前视频是否收费:0:否,1:是 */
		@Attribute(required = false)
		public int ftv;

		/** 如果是剧集,则显示剧集接口 */
		@Attribute(required = false)
		public String dramaurl;

		/** 默认是否直接播放 1 直接播放 0 不 播放 */
		@Attribute(required = false)
		public int defaultplay;

		/** 横屏显示0否1是 */
		@Attribute(required = false)
		public String horizontal;

        /** 是否使用sdk（1 是 0或空 否） */
        @Attribute(required = false)
        public String issdkplay;

	}

	@Root(strict = false)
	public static class Infos implements Serializable{
		@ElementList(inline = true, entry = "info", required = false)
		public List<Info> infoList;
	}

	@Root(strict = false)
	public static class Info implements Serializable{
		/** 键:视频信息:导演、主演、类型 */
		@Attribute(required = false)
		public String k;

		/** 值: */
		@ElementList(inline = true, required = false, entry = "v")
		public List<V> vList;

	}

	@Root(strict = false)
	public static class V implements Serializable{

		/** 视频信息例如:伊利亚-伍德.... (单个人名或类型) */
		@Attribute(required = false)
		public String name;

		/** 链接(如人名链接或类型链接) */
		@Attribute(required = false)
		public String url;

	}

	@Root(strict = false)
	public static class Likeurl implements Serializable{

		/** 喜欢接口地址 */
		@Attribute(required = false)
		public String url;

		/** 喜欢的数量 */
		@Attribute(required = false)
		public String count;

	}

	@Root(strict = false)
	public static class Zurl implements Serializable{

		/** 赞接口地址/取消赞地址 */
		@Attribute(required = false)
		public String url;

		/**
		 * 1 添加赞地址(未赞过) 0 取消赞地址(已赞过)
		 */
		@Attribute(required = false)
		public String state;

	}

	@Root(strict = false)
	public static class Tips implements Serializable{
		@ElementList(inline = true, entry = "tip", required = false)
		public List<Tip> tipList;
	}

	@Root(strict = false)
	public static class Tip implements Serializable{

		/** 提示标题 */
		@Attribute(required = false)
		public String title;

		/** 提示内容 */
		@Attribute(required = false)
		public String cnt;

		/** 了解详情按钮点击后访问的网页地 址,注意需传递 CIPHER 参数 (为空 时,不显示了解详情按钮) */
		@Attribute(required = false)
		public String durl;

		/** 弹出时间点,单位秒 */
		@Attribute(required = false)
		public String t;

		/**
		 * 0:付费提示 1:播放结束提示 2: 普通提示 3:计费栏目提示有效期 4:详情页活动信息 5:确认下载 6:搜索法律申明
		 * 7:详情页法律申明 8:热点接口法律申明 9:剧集法律申明 10:栏目法律申明 11: 播放前提示(收费的视频,未计
		 * 费用户,刚开始播的时候提示)
		 */
		@Attribute(required = false)
		public String type;

		/** 0 无按钮 1 仅跳转按钮 2 仅取消按钮 3 跳转和取消按钮都有 */
		@Attribute(required = false)
		public String btns;

		/** 关闭对话框按钮的标题 */
		@Attribute(required = false)
		public String btncancellabel;

		/** 跳转按钮的标题 */
		@Attribute(required = false)
		public String btnjumplabel;

	}

	@Root(strict = false)
	public static class Exts implements Serializable{
		@ElementList(inline = true, entry = "ext", required = false)
		public List<Ext> extList;
	}

	@Root(strict = false)
	public static class Coll implements Serializable{

		/** 收藏来源id */
		@Attribute(required = false)
		public long fromid;
		/** 收藏url */
		@Element(required = false)
		public String url;
	}

	@Root(strict = false)
	public static class Ext implements Serializable{

		/** 节点显示名称,例如:节目单 */
		@Attribute(required = false)
		public String name;

		/** 节目 */
		@ElementList(inline = true, required = false, name = "cnt")
		public List<Cnt> cntList;

		/** 活动 */
		@Element(required = false)
		public Event event;

	}

	@Root(strict = false)
	public static class Cnt implements Serializable{

		/** 节目名称 */
		@Attribute(required = false)
		public String name;

		/** 节目时间 */
		@Attribute(required = false)
		public String time;

		/** 节目描述 */
		@Attribute(required = false)
		public String desc;

	}

	@Root(strict = false)
	public static class Event implements Serializable{

		/** 活动描述信息 */
		@Attribute(required = false)
		public String des;

		/** 活动参与地址 */
		@Attribute(required = false)
		public String url;

		/** 活动显示名称 */
		@Attribute(required = false)
		public String title;

		/** 跳转按钮的标题 */
		@Attribute(required = false)
		public String btnjumplabel;

	}

	/** 视频名称 */
	@Element(required = false)
	public String name;

	/** 图片 */
	@Element(required = false)
	public String pic;

	/** 服务器地址 */
	@Element(required = false)
	public String host;

	/** 资源服务器地址 */
	@Element(required = false)
	public String shost;

	/**
	 * 详情页类型 ? * 0:剧集模式(剧集海报版) ? * 1:直播模式(直播小屏播放版) ? * 2:单片模式(非剧集海报版) ? *
	 * 3:短视频模式(非直播小屏播放
	 */
	@Element(required = false)
	public int type;

	/**
	 * 1 隐藏视频简介 2 隐藏游戏简介 3 隐藏剧集 4 隐藏评论 5 隐藏活动 6 隐藏下载 7 隐藏直播节目单
	 */
	@Element(required = false)
	public String hiddentag;

	/**
	 * 1 当前显示视频简介 2 当前显示游戏简介 3 当前显示剧集 4 当前显示评论 5 当前显示活动 6 当前显示下载 7 当前显示直播节目单
	 */
	@Element(required = false)
	public String selecttag;

	/**
	 * 1 规则剧集 2 电影 3 非规则剧集
	 */
	@Element(required = false)
	public String drama;

	/** 视频简介 */
	@Element(required = false)
	public String desc;

	/** 年代 */
	@Element(required = false)
	public String year;

	/** 评分 */
	@Element(required = false)
	public String score;

	/** 游戏信息 */
	@Element(required = false)
	public Gameinfo gameinfo;

	/** 来源列表 */
	@Element(required = false)
	public Froms froms;

	/** 视频详请页信息键值列表 */
	@Element(required = false)
	public Infos infos;

	/** 添加评论接口地址 */
	@Element(required = false)
	public String addcommenturl;

	/** 评论列表接口地址 */
	@Element(required = false)
	public String commenturl;

	/** 喜欢接口地址 */
	@Element(required = false)
	public Likeurl likeurl;

	/** 关联视频地址 */
	@Element(required = false)
	public String linkurl;

	/** 赞接口地址 */
	@Element(required = false)
	public Zurl zurl;

	/** 收藏 */
	@Element(required = false, name = "furl")
	public Coll furl;

	/** 收藏状态 1已收藏 0未收藏 */
	@Element(required = false)
	public int fstate;

	/** 提示列表 */
	@Element(required = false)
	public Tips tips;

	/** 扩展节点列表 */
	@Element(required = false)
	public Exts exts;

	/** 活动信息 */
	@Element(required = false)
	public Event event;

	/** 时间戳年月日时分秒 */
	@Element(required = false)
	public String time;

	/** vgurl，视频详情地址 */
	@Element(required = false)
	public String vgurl;

}
