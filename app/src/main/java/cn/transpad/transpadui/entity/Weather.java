package cn.transpad.transpadui.entity;

import java.io.Serializable;

/**
 * Created by Kongxiaojun on 2015/3/23.
 */
public class Weather implements Serializable{

    public int errNum;
    public String errMsg;

    public RetData retData;

    public static class RetData implements Serializable{

        public String city; //城市
        public String pinyin;//城市拼音
        public String citycode;//城市编码
        public String date;//日期
        public String time;//发布时间
        public String postCode;//邮编
        public String longitude;//经度
        public String latitude;//维度
        public String altitude;//海拔
        public String weather;//天气情况
        public String temp;//气温
        public String l_tmp;//最低气温
        public String h_tmp; //最高气温
        public String WD; //风向
        public String WS;//风力
        public String sunrise;//日出时间
        public String sunset;//日落时间

        @Override
        public String toString() {
            return "retData{" +
                    "city='" + city + '\'' +
                    ", pinyin='" + pinyin + '\'' +
                    ", citycode='" + citycode + '\'' +
                    ", date='" + date + '\'' +
                    ", time='" + time + '\'' +
                    ", postCode='" + postCode + '\'' +
                    ", longitude='" + longitude + '\'' +
                    ", latitude='" + latitude + '\'' +
                    ", altitude='" + altitude + '\'' +
                    ", weather='" + weather + '\'' +
                    ", temp='" + temp + '\'' +
                    ", l_tmp='" + l_tmp + '\'' +
                    ", h_tmp='" + h_tmp + '\'' +
                    ", WD='" + WD + '\'' +
                    ", WS='" + WS + '\'' +
                    ", sunrise='" + sunrise + '\'' +
                    ", sunset='" + sunset + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Weather{" +
                "errNum=" + errNum +
                ", errMsg='" + errMsg + '\'' +
                ", retData=" + retData +
                '}';
    }
}
