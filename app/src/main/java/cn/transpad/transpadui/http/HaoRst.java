package cn.transpad.transpadui.http;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * Created by Kongxiaojun on 2015/5/15.
 * 限号信息
 */
@Root(strict = false)
public class HaoRst extends Rst implements Serializable {

    @Element(required = false)
    public String city;

    @Element(required = false)
    public Hao hao;

    @Root(strict = false)
    public static class Hao implements Serializable {

        @Attribute(required = false)
        public String local;

        @Attribute(required = false)
        public String nonlocal;
    }

}
