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
public class LoginRst extends Rst implements Serializable {
	
	@Root(strict = false)
	static public class Softupdate implements Serializable {
		
		/** 升级标识: 0 推荐升级 1 强制升级. */
		@Element(required = false)
		public String updateflag;
		
		/** 升级包地址. */
		@Element(required = false)
		public String updateurl;
		
		/** 升级描述语句,纯文本,换行用\n 表示. */
		@Element(required = false)
		public String updatedesc;
		
		/** 新版本名称. */
		@Element(required = false)
		public String name;
		
		/** 提示天数. */
		@Element(required = false)
		public int hitday;
		
		/** 升级提示弹出位置：0 首页、￼1 详情页、2 全频播放页 */
		@Element(required = false)
		public int position;
		
		/** 升级框弹出时间(单位:秒). */
		@Element(required = false)
		public String t;

		/** 升级框弹按钮文字 */
		@Element(required = false)
		public Btn btn;

		@Override
		public String toString() {
			return "Softupdate [updateflag=" + updateflag + ", updateurl="
					+ updateurl + ", updatedesc=" + updatedesc + ", name="
					+ name + ", hitday=" + hitday + ", position=" + position
					+ ", t=" + t + ", btn=" + btn + "]";
		}
		
	}
	
	@Root(strict = false)
	static public class Btn implements Serializable {
		
		/** 按钮显示  0无按钮 1仅跳转按钮 2仅取消按钮 3跳转和取消按钮都有 */
		@Attribute(required = false)
		public int btns;

        /**确定升级*/
		@Attribute(required = false)
		public String btnjumplabel;

        /**取消升级*/
		@Attribute(required = false)
		public String btncancellabel;

        /**智能升级*/
        @Attribute(required = false)
        public String btnthirdlabel;

		@Override
		public String toString() {
			return "Btn [btns=" + btns + ", btncancellabel=" + btncancellabel
					+ ", btnjumplabel=" + btnjumplabel + "]";
		}
		
	}
	
	@Root(strict = false)
	static public class Mns implements Serializable {
		/** 频道数量. */
		@Attribute(required = false)
		public String count;
		
		/** 频道列表. */
		@ElementList(inline=true, entry="mn", required=false)
	    public List<Mn> mnList;
		
	}
	
	@Root(strict = false)
	static public class Mn implements Serializable {
		
		@Attribute(required = false)
		public String rank;
		
		/** 栏目模板类型 */
		@Attribute(required = false)
		public String showtype;
		
		/** 是否显示栏目 0：否  1：是. */
		@Attribute(required = false)
		public String isshow;
		
		/** 是否自定义栏目 0:否 1:是. */
		@Attribute(required = false)
		public String custom;
		
		/** 是否为新频道 0:否 1:是. */
		@Attribute(required = false)
		public String isnew;
		
		
		@Attribute(required = false)
		public String vip;
		
		
		
		/** 是否为专题 0:否 1:是. */
		@Attribute(required = false)
		public String issub;
		
		/** 频道 ID. */
		@Attribute(required = false)
		public String id;
		
		/** 频道父 ID. */
		@Attribute(required = false)
		public String parent;
		
		/** 默认是否打开 0:否 1:是. */
		@Attribute(required = false)
		public String open;
		
		/** 频道大图. */
		@Attribute(required = false)
		public String pic;
		
		/** 图标地址 (类似:hot,new 这种图标). */
		@Attribute(required = false)
		public String icon;
		
		/** 链接地址类型:
		5 频道地址,访问栏目接口
		6 文字链接地址,打开浏览器 
		11 微信地址,打开微信关注. */
		@Attribute(required = false)
		public String utp;
		
		/** 链接地址. */
		@Attribute(required = false)
		public String url;
		
		/** 当天更新数量. */
		@Attribute(required = false)
		public String num;
		
		
		/** 频道显示名称. */
		@Attribute(required = false)
		public String name;

		
	}
	
	@Root(strict = false)
	static public class Posters implements Serializable {
		
		/** 频道列表. */
		@ElementList(inline=true, entry="poster", required=false)
	    public List<Poster> posterList;
		
		@Attribute(required=false)
		public int times;

	}
	
	@Root(strict = false)
	static public class Poster implements Serializable {
		
		@Attribute(required=false)
		public String name;
		
		/** 0 下载应用,1 进入详情页. */
		@Attribute(required=false)
		public String utp;
		
		/** 链接地址. */
		@Attribute(required=false)
		public String url;
		
		/** 展示时长(单位:秒). */
		@Attribute(required=false)
		public String time;
		
		/** 类型:1 首次登录海报 0 非首次登录海报. */
		@Attribute(name="isfirst", required=false)
		public int isFirst;
		
		/** 启动页广告图片地址. */
		@Attribute(required=false)
		public String pic;

		
	}

	@Root(strict = false)
	static public class MediaPower implements Serializable {

		/** 0 不显示,1 显示. */
		@Attribute(required=false)
		public String tpa = "1";

		/** 0 不显示,1 显示. */
		@Attribute(required=false)
		public String tpd = "1";

		/** 0 不显示,1 显示. */
		@Attribute(required=false)
		public String tpq = "0";

	}

	/** 服务器地址. */
	@Element(required=false)
	public String host;
	
	@Element(required=false)
	public String shost;
	
	@Element(required=false)
	public String decurl;
	
	/** 版本升级信息. */
	@Element(name="softupdate", required=false)
	public Softupdate softupdate;
	
	/** 频道列表. */
	@Element(name="mns", required=false)
	public Mns mns;
	
	/** 软件推荐控制开关:0 关闭 1 开启. */
	@Element(required=false)
	public String rec;
	
	/** 时间戳年月日时分秒例:20120516150735. */
	@Element(required=false)
	public String time;
	
	
	@Element(required=false)
	public String sopcast;
	
	
	/** 全网搜索控制开关,0 关闭 1 开启. */
	@Element(required=false)
	public String seaflag;
	

	
	/** 应用 KEY 应用 ID 已付费用户该属性为空. */
	@Element(required=false)
	public String appkey;
	
	/** 应用 ID 应用 ID 已付费用户该属性为空. */
	@Element(required=false)
	public String appid;
	
	@Element(name="posters", required=false)
    public Posters posters;

	/** 微信包地址. */
	@Element(required = false)
	public String weixinurl;

	/**多媒体页面控制开关：0关闭1开启 */
	@Element(name="showmedie",required = false)
	public MediaPower showmedie;

	@Override
	public String toString() {
		return "LoginRst [host=" + host + ", shost=" + shost + ", decurl="
				+ decurl + ", softupdate=" + softupdate + ", mns=" + mns
				+ ", rec=" + rec + ", time=" + time + ", sopcast=" + sopcast
				+ ", seaflag=" + seaflag + ", appkey=" + appkey + ", appid="
				+ appid + ", posters=" + posters + "]";
	}



	
}
