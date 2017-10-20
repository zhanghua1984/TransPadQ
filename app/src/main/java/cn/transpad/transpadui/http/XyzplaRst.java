package cn.transpad.transpadui.http;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.List;


/**
 * @author 刘昆  (liukun@100tv.com)
 * @since 2014-04-22
 */

@Root(strict = false)
public class XyzplaRst extends Rst implements Serializable{

    @Root(strict = false)
    public static class Tips implements Serializable{
        @ElementList(inline = true, entry = "tip", required = false)
        public List<Tip> tipList;

    }

    @Root(strict = false)
    public static class Tip implements Serializable{

        /** */
        @Attribute(required = false)
        public String title;

        /** */
        @Attribute(required = false)
        public String cnt;

        /**
         * 了解详情按钮点击后访问的网页地址,注意需传递CIPHER参数
         */
        @Attribute(required = false)
        public String durl;

        /**
         * 弹出时间点，单位秒
         */
        @Attribute(required = false)
        public int t;

        /**
         * 0:付费提示
         * 1:播放结束提示
         * 2:普通提示
         * 3:计费栏目提示有效期
         * 4:详情页活动信息
         * 5:确认下载
         * 6:搜索法律申明
         * 7:详情页法律申明
         * 8:热点接口法律申明
         * 9:剧集法律申明
         */
        @Attribute(required = false)
        public String type;

        /**
         * 0 无按钮  1 仅跳转按钮 2 仅取消按钮  3  跳转和取消按钮都有
         */
        @Attribute(required = false)
        public String btns;

        /**
         * 关闭对话框按钮的标题
         */
        @Attribute(required = false)
        public String btncancellabel;

        /**
         * 跳转按钮的标题
         */
        @Attribute(required = false)
        public String btnjumplabel;
    }

    @Root(strict = false)
    public static class Cnt  implements Serializable{

        /**
         * 内容显示名称
         */
        @Attribute(required = false)
        public String name;

        /**
         * 视频内容图片地址，带时间戳
         */
        @Attribute(required = false)
        public String pic;

        /**
         * 收藏类型，中文表示例：电影、电视剧…..
         */
        @Attribute(required = false)
        public String favtyp;

        /**
         * 内容品质：例如5表示五星
         */
        @Attribute(required = false)
        public String quality;

        /**
         * 是否高清：0高清1非高清
         */
        @Attribute(required = false)
        public String hd;

        /**
         * 详情页地址，内容为剧集有值，非剧集为空
         */
        @Attribute(required = false)
        public String durl;

        /**
         * 清晰度类型，逗号分隔，例如： 1,2,3 表示同时存在标清、高清、超清
         */
        @Attribute(required = false)
        public String dfnt;

        /**
         * 免费时长
         */
        @Attribute(required = false)
        public int ft;

        /**
         * 当前用户是否为已缴费用户 0:否，1：是
         */
        @Attribute(required = false)
        public String puser;

        /**
         * 当前视频是否收费：0:否，1：是
         */
        @Attribute(required = false)
        public int ftv;

        /**
         * 允许播放的清晰度，逗号分隔，例如： 1,2,3 表示同时存在标清、高清、超清 如果为空，则表示所有都允许
         */
        @Attribute(required = false)
        public String alp;

        /**
         * 允许下载的清晰度,逗号分隔，例如： 1,2,3 表示同时存在标清、高清、超清 如果为空，则表示所有都允许
         */
        @Attribute(required = false)
        public String ald;

        /**
         * 0:直接播放 1：去原网页
         */
        @Attribute(required = false)
        public int toply;

        /**
         * 显示播放按钮 0：不显示 1：显示(默认)
         */
        @Attribute(required = false)
        public int btnply;

        /**
         * 显示下载按钮 0：不显示 1：显示(默认)
         */
        @Attribute(required = false)
        public int btndown;

        /**
         * 原网页地址
         */
        @Attribute(required = false)
        public String ourl;

        @Element(required = false)
        public Dfnts dfnts;

        /**
         * 分片视频地址列表
         */
        @Element(required = false)
        public Fraglist fraglist;

        /**
         * 最后播放时间
         */
        @Element(required = false)
        public String lastplaytime;
    }

    @Root(strict = false)
    public static class Dfnts implements Serializable {
        @ElementList(inline = true, entry = "dfnt", required = false)
        public List<Dfnt> dfntList;

    }

    @Root(strict = false)
    public static class Dfnt implements Serializable {

        /**
         * 清晰度类型1标清、2高清、3超清
         */
        @Attribute(required = false)
        public int t;

        /**
         * 该清晰度对应的地址
         */
        @Attribute(required = false)
        public String url;

        /**
         * 0:非当前  1:当前播放的视频清晰度
         */
        @Attribute(required = false)
        public int cur;

    }

    @Root(strict = false)
    public static class Fraglist implements Serializable {

        /**
         * 总时长表
         */
        @Attribute(required = false)
        public int t;

        /**
         * 分片
         */
        @ElementList(inline = true, entry = "frag", required = false)
        public List<Frag> fragList;

    }

    @Root(strict = false)
    public static class Frag implements Serializable {

        /**
         * 分片时长
         */
        @Attribute(required = false)
        public int t;

        /**
         * 视频地址
         */
        @Attribute(required = false)
        public String url;
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
     * 直播标识： 0 非直播 1 视频直播 2 音频直播 没有ds属性为非直播
     */
    @Element(required = false)
    public String ds;

    /**
     * 多屏代码
     */
    @Element(required = false)
    public String aircode;

    /**
     * 收藏地址
     */
    @Element(required = false)
    public String furl;

    /**
     * 提示
     */
    @Element(required = false)
    public Tips tips;

    /**
     * 内容信息
     */
    @Element(required = false)
    public Cnt cnt;

    /**
     * 关联内容列表节点
     */
    @Element(required = false)
    public String lkurl;

    /**
     * 微博分享视频地址
     */
    @Element(required = false)
    public String weibourl;

    /**
     * 下一条内容
     */
    @Element(required = false)
    public String nexturl;

    /**
     * 上一条内容
     */
    @Element(required = false)
    public String provurl;

    /**
     * 时间戳
     */
    @Element(required = false)
    public String time;

    /**
     * 广告
     */
    @Element(required = false)
    public String ads;

    /**
     * 名称？
     */
    @Element(required = false)
    public String lkname;

    /**
     * 剧集url
     */
    @Element(required = false)
    public String dramaurl;

    /**
     * 是否使用sdk 播放 1 是 0 或空 否
     */
    @Element(required = false)
    public String issdkplay;

    /**
     * 收费视频是否已购买
     */
    @Element(required = false)
    public int ispayed;

    /**
     * 收费视频购买地址
     */
    @Element(required = false)
    public String payurl;

    /**
     * 视频价格（单位V币）
     */
    @Element(required = false)
    public int price;

    /**
     * 付费视频有效期（单位小时）
     */
    @Element(required = false)
    public String timelength;

}
