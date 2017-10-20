package cn.transpad.transpadui.entity;

import java.io.Serializable;

/**
 * Created by Kongxiaojun on 2015/3/23.
 */
public class Aqi implements Serializable {

    public int errNum;

    public String errMsg;

    public RetData retData;

    public static class RetData implements Serializable{
        public String city; //城市
        public String time;//数据采集时间
        public String aqi;//空气质量指数
        public String level;//空气等级
        public String core;//首要污染物

        @Override
        public String toString() {
            return "RetData{" +
                    "city='" + city + '\'' +
                    ", time='" + time + '\'' +
                    ", aqi='" + aqi + '\'' +
                    ", level='" + level + '\'' +
                    ", core='" + core + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Aqi{" +
                "errNum=" + errNum +
                ", errMsg='" + errMsg + '\'' +
                ", retData=" + retData +
                '}';
    }
}
