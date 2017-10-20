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
public class SoftRst extends Rst implements Serializable {

	@Root(strict = false)
	public static class Col implements Serializable {

		/**
		 * 栏目名称，如100tv推荐，热门游戏，装机必备
		 */
		@Element(required = false)
		public String name;

		/**
		 * 栏目url
		 */
		@Element(required = false)
		public String url;

		@Element(required = false)
		public Cnts cnts;

		@Element(required = false)
		public Rp rp;

		@Override
		public String toString() {
			return "Col{" +
					"name='" + name + '\'' +
					", url='" + url + '\'' +
					", cnts=" + cnts +
					", rp=" + rp +
					'}';
		}
	}

	@Root(strict = false)
	public static class Cnts implements Serializable {

		@ElementList(inline = true, entry = "cnt", required = false)
		public List<Cnt> cntList;

		@Override
		public String toString() {
			return "Cnts{" +
					"cntList=" + cntList +
					'}';
		}
	}

	@Root(strict = false)
	public static class Cnt implements Serializable {
		/**
		 * 描述
		 */
		@Attribute(required = false)
		public String desc;

		/**
		 * 小图地址，带时间戳
		 */
		@Attribute(required = false)
		public String pic2;

		/**
		 * 大图地址，带时间戳
		 */
		@Attribute(required = false)
		public String pic1;

		/**
		 * 下载地址
		 */
		@Attribute(required = false)
		public String url;

		/**
		 * 栏目内容关联id
		 */
		@Attribute(required = false)
		public String id;

		/**
		 * 关键词
		 */
		@Attribute(required = false)
		public String kwd;

		/**
		 * 内容显示名称
		 */
		@Attribute(required = false)
		public String name;

		/**
		 * 版本号
		 */
		@Attribute(required = false)
		public String ver;

		/**
		 * 包名
		 */
		@Attribute(required = false)
		public String pkname;

		/**
		 * 点击次数
		 */
		@Attribute(required = false)
		public String clicknum;

		/**
		 * 分类
		 */
		@Attribute(required = false)
		public String type;

		/**
		 * 大小
		 */
		@Attribute(required = false)
		public String mb;

		/**
		 * 推荐指数
		 */
		@Attribute(required = false)
		public String recmond;

		/**
		 * 版本名称
		 */
		@Attribute(required = false)
		public String vername;

		/**
		 * 详情图片
		 */
		@Element(required = false)
		public Imgs imgs;

		@Override
		public String toString() {
			return "Cnt{" +
					"desc='" + desc + '\'' +
					", pic2='" + pic2 + '\'' +
					", pic1='" + pic1 + '\'' +
					", url='" + url + '\'' +
					", id='" + id + '\'' +
					", kwd='" + kwd + '\'' +
					", name='" + name + '\'' +
					", ver='" + ver + '\'' +
					", pkname='" + pkname + '\'' +
					", clicknum='" + clicknum + '\'' +
					", type='" + type + '\'' +
					", mb='" + mb + '\'' +
					", recmond='" + recmond + '\'' +
					", vername='" + vername + '\'' +
					", imgs=" + imgs +
					'}';
		}
	}

	@Root(strict = false)
	public static class Imgs implements Serializable {
		/**
		 * 推荐指数
		 */
		@ElementList(inline = true, entry = "img", required = false)
		public List<Img> imgList;

		@Override
		public String toString() {
			return "Imgs{" +
					"imgList=" + imgList +
					'}';
		}
	}

	@Root(strict = false)
	public static class Img implements Serializable {
		/**
		 * 推荐指数
		 */
		@Attribute(required = false)
		public String url;

		@Override
		public String toString() {
			return "Img{" +
					"url='" + url + '\'' +
					'}';
		}
	}

	@Root(strict = false)
	public static class Rp implements Serializable {
		/**
		 * 下一页url
		 */
		@Attribute(required = false)
		public String nurl;

		/**
		 * 总页数
		 */
		@Attribute(required = false)
		public int m;

		/**
		 * 当前页码
		 */
		@Attribute(required = false)
		public int p;

		@Override
		public String toString() {
			return "Rp{" +
					"nurl='" + nurl + '\'' +
					", m=" + m +
					", p=" + p +
					'}';
		}
	}

	/**
	 * 服务器地址
	 */
	@Element(required = false)
	public String host;

	/**
	 * 资源服务器地址
	 */
	@Element(required = false)
	public String shost;

	/**
	 * 栏目
	 */
	@ElementList(inline = true, required = false, entry = "col")
	public List<Col> cols;

	@Override
	public String toString() {
		return "SoftRst{" +
				"host='" + host + '\'' +
				", shost='" + shost + '\'' +
				", cols=" + cols +
				'}';
	}
}
