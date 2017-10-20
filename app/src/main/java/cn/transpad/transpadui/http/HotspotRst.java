package cn.transpad.transpadui.http;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Kongxiaojun on 2015/4/8.
 */
@Root(strict = false)
public class HotspotRst extends Rst implements Serializable {

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

    @Element(required = false)
    public Modules modules;

    @Element(required = false)
    public Posters posters;

    @Root(strict = false)
    public static class Modules implements Serializable {
        @ElementList(inline = true, entry = "module", required = false)
        public List<Module> moduleList;

        @Override
        public String toString() {
            return "Modules{" +
                    "moduleList=" + moduleList +
                    '}';
        }
    }

    @Root(strict = false)
    public static class Module implements Serializable {

        @ElementList(required = false)
        public List<Cnt> cnts;

        @Override
        public String toString() {
            return "Module{" +
                    "cnts=" + cnts +
                    '}';
        }
    }

    @Root(strict = false)
    public static class Cnt implements Serializable {


        /**
         * 链接地址限定
         * 0 播放地址 访问播放接口
         * 1 详情页地址 访问详情页接口
         * 2 搜索地址 访问搜索接口
         * 5 菜单地址，访问栏目接口
         * 6 文字链接地址，打开浏览器
         * 7 剧集列表也地址，访问剧集接口
         * 8 打开原始网页
         * 9 计费详情页
         * 10 打开h5
         */
        @Attribute(required = false)
        public String utp;

        /**
         * 链接地址
         */
        @Attribute(required = false)
        public String url;

        /**
         * 海报页图片地址，带时间戳
         */
        @Attribute(required = false)
        public String pic1;

        @Override
        public String toString() {
            return "Cnt{" +
                    "utp='" + utp + '\'' +
                    ", url='" + url + '\'' +
                    ", pic1='" + pic1 + '\'' +
                    '}';
        }
    }

    @Root(strict = false)
    public static class Posters implements Serializable {
        @ElementList(inline = true, entry = "poster", required = false)
        public List<Poster> posterList;

        @Override
        public String toString() {
            return "Posters{" +
                    "posterList=" + posterList +
                    '}';
        }
    }

    @Root(strict = false)
    public static class Poster implements Serializable {

        /**
         * id
         */
        @Attribute(required = false)
        public long id;

        /**
         * 包名
         */
        @Attribute(required = false)
        public String pkname;

        /**
         * 版本号
         */
        @Attribute(required = false)
        public String version;

        /**
         * 名称
         */
        @Attribute(required = false)
        public String name;
        /**
         * 首页提示语
         */
        @Attribute(required = false)
        public String msg;

        /**
         * 动作 0下载应用 3打开网页
         */
        @Attribute(required = false)
        public String utp;

        /**
         * 地址，utp=0就是下载地址，utp=3就是打开地址
         */
        @Attribute(required = false)
        public String url;

        /**
         * 时间
         */
        @Attribute(required = false)
        public String time;

        /**
         * 图片地址
         */
        @Attribute(required = false)
        public String pic;

        @Override
        public String toString() {
            return "Poster{" +
                    "pkname='" + pkname + '\'' +
                    ", version='" + version + '\'' +
                    ", name='" + name + '\'' +
                    ", utp='" + utp + '\'' +
                    ", url='" + url + '\'' +
                    ", time='" + time + '\'' +
                    ", pic='" + pic + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "HotspotRst{" +
                "host='" + host + '\'' +
                ", shost='" + shost + '\'' +
                ", modules=" + modules +
                ", posters=" + posters +
                '}';
    }
}
