package cn.transpad.transpadui.http;

/**
 * @author 刘昆  (liukun@100tv.com)
 * @since  2014-04-22
 */

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.List;


@Root(strict = false)
public class SpecllistRst extends Rst implements Serializable{
	
	@Root(strict = false)
	static public class Mns implements Serializable{
		
		/** 频道数量. */
		@Attribute(required=false)
		public String count;

		/** 模块列表. */
		@ElementList(inline=true, entry="mn", required=false)
	    public List<Mn> mnList;
	}

	@Root(strict = false)
	static public class Mn implements Serializable{
		@Attribute(required=false)
		public String vip;

		/** 频道显示名称. */
		@Attribute(required=false)
		public String name;

		/** 当天更新数量. */
		@Attribute(required=false)
		public String num;

		/** 链接地址. */
		@Attribute(required=false)
		public String url;

		/**
		 * 链接地址类型：
		 * 5频道地址，访问栏目接口
		 * 6文字链接地址，打开浏览器
		 * 11微信地址，打开微信关注.
		 */
		@Attribute(required=false)
		public String utp;

		/** 图标地址 . */
		@Attribute(required=false)
		public String icon;

		/** 频道左侧的图标. */
		@Attribute(required=false)
		public String iconm;

		/** 频道大图. */
		@Attribute(required=false)
		public String pic;

		/** 默认是否打开   0:否  1:是. */
		@Attribute(required=false)
		public String open;

		/** 频道父ID. */
		@Attribute(required=false)
		public String parent;

		/** 频道ID. */
		@Attribute(required=false)
		public String id;

		/** 是否为专题    0：否  1：是. */
		@Attribute(required=false)
		public String issub;

		/** 该频道影片的总数量. */
		@Attribute(required=false)
		public String count;

		/** 是否为新频道  0：否  1：是. */
		@Attribute(required=false)
		public String isnew;

		/** 是否自定义栏目 0：否  1：是. */
		@Attribute(required=false)
		public String custom;

		/** 是否显示栏目0：否  1：是. */
		@Attribute(required=false)
		public String isshow;

		/** 频道展示形式. */
		@Attribute(required=false)
		public String showtype;

	}

	@Root(strict = false)
	static public class Poster implements Serializable{

		@Element(required=false)
		public Cnts cnts;

	}

	@Root(strict = false)
	static public class Cnts implements Serializable{
		/** 海报列表. */
		@ElementList(inline=true, entry="cnt", required=false)
	    public List<Cnt> cntList;

	}

	@Root(strict = false)
	static public class Cnt implements Serializable{

		@Attribute(required=false)
		public String collectionurl;

		@Attribute(required=false)
		public String time;

		@Attribute(required=false)
		public String timelen;

		@Attribute(required=false)
		public String dzdfeeid;

		/** 海报显示名称. */
		@Attribute(required=false)
		public String name;

		/** 主演. */
		@Attribute(required=false)
		public String actor;

		/** 导演. */
		@Attribute(required=false)
		public String dir;

		/** 描述. */
		@Attribute(required=false)
		public String desc;

		/** 链接地址,访问播放接口. */
		@Attribute(required=false)
		public String url;

		/** 计费或活动 url. */
		@Attribute(required=false)
		public String furl;

		/** furl 的类型 0:不走计费或活动界面 1 计费 2 活动 3 直接计费. */
		@Attribute(required=false)
		public String ftype;

		/** 按钮名称. */
		@Attribute(required=false)
		public String fbtn;

		/** 微博分享视频地址. */
		@Attribute(required=false)
		public String weibourl;

		/** 收藏类型,中文,例:电影、电视剧„. */
		@Attribute(required=false)
		public String favtyp;

		/** 链接地址限定 1播放 2 请求栏目3详情页 4打开 web 页. */
		@Attribute(required=false)
		public int utp;

		/** 海报页图片地址,带时间戳. */
		@Attribute(required=false)
		public String pic1;

		/** 九宫格内的海报图片地址,带时间戳. */
		@Attribute(required=false)
		public String pic2;

		/** 海报下载上报地址. */
		@Attribute(required=false)
		public String deurl;

		/** 是否高清:0 高清 1 非高清. */
		@Attribute(required=false)
		public String hd;

		/** 内容品质:例如 5 表示五星. */
		@Attribute(required=false)
		public String quality;

		/** 原网页地址. */
		@Attribute(required=false)
		public String ourl;

		/** 0:直接播放 1:去原网页. */
		@Attribute(required=false)
		public int toply;

		/** 显示播放按钮 0:不显示 1:显示(默认)  */
		@Attribute(required=false)
		public String btnply;

		/** 显示下载按钮 0:不显示 1:显示(默认). */
		@Attribute(required=false)
		public String btndown;

		/** 清晰度类型,逗号分隔,例如: 1,2,3 表示同时存在标清、高清、超清. */
		@Attribute(required=false)
		public String dfnt;

		/** 来源(例如:土豆). */
		@Attribute(required=false)
		public String from;

		/** 是否显示工具条(含播放,分享等按钮)
		0: 表示无 (仅显示背景图片)
		1: 表示 bar+介绍框
		2: 表示无 bar 有介绍框(含来源,标题,主演,内容)
		3: 表示无 bar 仅有简介. */
		@Attribute(required=false)
		public String bar;

		/** 当前视频是否收费0否1是. */
		@Attribute(required=false)
		public String ftv;

		/** 更新到多少集(如果是电视剧). */
		@Attribute(required=false)
		public String updatenum;

		/** 更新信息(类型匹配的完整字符串). */
		@Attribute(required=false)
		public String updatedetail;

		/** 图片标签 1最新 2最热 3推荐 4独家. */
		@Attribute(required=false)
		public String labelimg;

		/** 备注名称. */
		@Attribute(required=false)
		public String memo;

		@Element(required=false)
		public Exts exts;

		/** 点击播放的次数. */
		@Attribute(required=false)
		public String showcount;

		/** 上线日期 */
		@Attribute(required=false)
		public String showtime;

		/** 片库id */
		@Attribute(required=false)
		public String videoid;

		@Override
		public String toString() {
			return "Cnt [collectionurl=" + collectionurl + ", time=" + time
					+ ", timelen=" + timelen + ", dzdfeeid=" + dzdfeeid
					+ ", name=" + name + ", actor=" + actor + ", dir=" + dir
					+ ", desc=" + desc + ", url=" + url + ", furl=" + furl
					+ ", ftype=" + ftype + ", fbtn=" + fbtn + ", weibourl="
					+ weibourl + ", favtyp=" + favtyp + ", utp=" + utp
					+ ", pic1=" + pic1 + ", pic2=" + pic2 + ", deurl=" + deurl
					+ ", hd=" + hd + ", quality=" + quality + ", ourl=" + ourl
					+ ", toply=" + toply + ", btnply=" + btnply + ", btndown="
					+ btndown + ", dfnt=" + dfnt + ", from=" + from + ", bar="
					+ bar + ", ftv=" + ftv + ", updatenum=" + updatenum
					+ ", updatedetail=" + updatedetail + ", labelimg="
					+ labelimg + ", memo=" + memo + ", exts=" + exts
					+ ", showcount=" + showcount + ", showtime=" + showtime
					+ ", videoid=" + videoid + "]";
		}

	}

	@Root(strict = false)
	static public class Exts implements Serializable{
		@Element(required=false)
		public Ext ext;
	}

	@Root(strict = false)
	static public class Ext implements Serializable{

		@Attribute(required=false)
		public String name;

		@ElementList(inline=true, entry="cnt", required=false)
	    public List<Cnt> cntList;
	}

	@Root(strict = false)
	static public class Label implements Serializable{
		/** 是否显示分类检索0：否  1：是. */
		@Attribute(required=false)
		public String isshow;

		@Attribute(required=false)
		public String clid;

		/** 列表. */
		@ElementList(inline=true, entry="lbs", required=false)
	    public List<Lbs> lbs;

	}

	@Root(strict = false)
	static public class Lbs implements Serializable{
		@Attribute(required=false)
		public String type;

		/** 列表. */
		@ElementList(inline=true, entry="lb", required=false)
	    public List<Lb> lb;
	}

	@Root(strict = false)
	static public class Lb implements Serializable{
		/** 名称. */
		@Attribute(required=false)
		public String name;

		/** 访问url. */
		@Attribute(required=false)
		public String url;
	}

	/** 此栏目下的子栏目. */
	@Root(strict = false)
	static public class Cols implements Serializable{
		/** 列表. */
		@ElementList(inline=true, entry="col", required=false)
	    public List<Col> colList;
	}

	@Root(strict = false)
	static public class Col implements Serializable{
		/** 栏目名称. */
		@Attribute(required=false)
		public String name;

		/** 链接地址，访问栏目接口. */
		@Attribute(required=false)
		public String url;

		/** 1普通栏目  2webview栏目. */
		@Attribute(required=false)
		public int type;

		/** webview栏目高度. */
		@Attribute(required=false)
		public int h;

		/** 栏目导语. */
		@Attribute(required=false)
		public String memo;

		/** 内容数量. */
		@Attribute(required=false)
		public String count;

		/** 栏目图片. */
		@Attribute(required=false)
		public String pic;

		/** 4.1  电视报 日期. */
		@Attribute(required=false)
		public String date;

		/** 4.1  电视报 节目. */
		@Element(required=false)
		public Cnts cnts;

		@Override
		public String toString() {
			return "Col [name=" + name + ", url=" + url + ", type=" + type
					+ ", h=" + h + ", memo=" + memo + ", count=" + count
					+ ", pic=" + pic + ", date=" + date + ", cnts=" + cnts
					+ "]";
		}



	}

	@Root(strict = false)
	static public class Rp implements Serializable{
		/** 客户端传来的页码 cp. */
		@Attribute(required=false)
		public int p;

		/** 是否存在下一页 0 不存在 1 存在. */
		@Attribute(required=false)
		public int m;

		/** 下一页 url. */
		@Attribute(required=false)
		public String nurl;

	}

	/** 服务器地址. */
	@Element(required=false)
	public String host;

	/** 资源服务器地址. */
	@Element(required=false)
	public String shost;

	/** 二级栏目页简介. */
	@Element(required=false)
	public String desc;

	/** 栏目图片地址. */
	@Element(required=false)
	public String pic;

	/** 栏目导语. */
	@Element(required=false)
	public String memo;

	/** 是否默认展开海报.1:展开,0:不展开. */
	@Element(required=false)
	public String showposter;

	@Element(required=false)
	public Mns mns;

	/** 海报. */
	@Element(required=false)
	public Poster poster;

	@Element(required=false)
	public String tip;

	@Element(required=false)
	public Label label;

	@Element(required=false)
	public Cols cols;

	/**视频内容*/
	@Element(required=false)
	public Cnts cnts;

	/** 错误代码 0 表示成功. */
	@Element(required=false)
	public Rp rp;
	
}
