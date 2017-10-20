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

@Root(strict=false)
public class DramaRst extends Rst {

	@Root(strict = false)
	public static class Cnts implements Serializable{

		/** 剧集列表显示类型 0 规则显示 1 不规则显示 */
		@Attribute(required = false)
		public int showtyp;

		/** 剧集数量名称 */
		@Attribute(required = false)
		public String name;

		/** 请求剧集地址 */
		@Attribute(required = false)
		public String url;



		/** 剧集 */
		@ElementList(inline = true, entry = "cnt", required = false)
		public List<Cnt> cntList;

		/** 是否选中，在UI中使用 */
		public boolean isChecked;

		@Override
		public String toString() {
			return "Cnts [showtyp=" + showtyp + ", name=" + name + ", url=" + url + ", cntList=" + cntList + "]";
		}

	}

	@Root(strict = false)
	public static class Cnt implements Serializable{

		/** 显示名称 */
		@Attribute(required = false)
		public String name;

		/** 视频地址，访问播放接口 */
		@Attribute(required = false)
		public String url;

		/** 分享地址 */
		@Attribute(required = false)
		public String weibourl;


		/** 排序序号 */
		@Attribute(required = false)
		public int idx;

		/** 原网页地址 */
		@Attribute(required = false)
		public String ourl;

		/** 0:直接播放 1：去原网页 */
		@Attribute(required = false)
		public int toply;

		/** 显示播放按钮 0：不显示 1：显示(默认) */
		@Attribute(required = false)
		public int btnply;

		/** 显示下载按钮 0：不显示 1：显示(默认) */
		@Attribute(required = false)
		public int btndown;

		/** 横屏显示0否1是 */
		@Attribute(required = false)
		public int horizontal;

		/** 图片地址 */
		@Attribute(required = false)
		public String pic;

		/**是否选择，可以多选，仅供详情页缓存使用*/
		public boolean isChecked;
		/**缓存状态，仅供详情页缓存使用*/
		public int cacheState = -1;

		@Override
		public String toString() {
			return "Cnt [name=" + name + ", url=" + url + ", weibourl="
					+ weibourl + ", idx=" + idx + ", ourl=" + ourl + ", toply="
					+ toply + ", btnply=" + btnply + ", btndown=" + btndown
					+ ", horizontal=" + horizontal + ", pic=" + pic
					+ ", isChecked=" + isChecked + "]";
		}



	}

	@Root(strict = false)
	public static class Rp implements Serializable{
		/** 当前页码 */
		@Attribute(required = false)
		public int p;

		/** 总页数 */
		@Attribute(required = false)
		public int m;

		/** 下一页URL */
		@Attribute(required = false)
		public String nurl;

		@Override
		public String toString() {
			return "Rp [p=" + p + ", m=" + m + ", nurl=" + nurl + "]";
		}

	}

	/** 服务器地址 */
	@Element(required = false)
	public String host;

	/** 资源服务器地址 */
	@Element(required = false)
	public String shost;

	/** */
	@ElementList(inline = true, entry = "cnts", required = false)
	public List<Cnts> cntsList;

	/** */
	@Element(required = false)
	public Rp rp;

	/** 剧集名称 */
	@Element(required = false)
	public String name;

	/** 剧集类型 */
	@Element(required = false)
	public int favtyp;
	
	

	@Override
	public String toString() {
		return "DramaRst [host=" + host + ", shost=" + shost + ", cntsList="
				+ cntsList + ", rp=" + rp + ", name=" + name + ", favtyp="
				+ favtyp + "]";
	}

}
