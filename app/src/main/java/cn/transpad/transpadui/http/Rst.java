package cn.transpad.transpadui.http;

/**
 * @author 刘昆  (liukun@100tv.com)
 * @since 2014-04-22
 */

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

@Root(strict = false)
public class Rst implements Serializable {

    @Root(strict = false)
    public static class Error implements Serializable {

        @Element(required = false)
        public String errorcode;

        @Element(required = false)
        public String errormsg;

        @Override
        public String toString() {
            return "Error [errorcode=" + errorcode + ", errormsg=" + errormsg
                    + "]";
        }

    }

    @Element(required = false)
    public int result;

    @Element(required = false)
    public Error error;

    @Override
    public String toString() {
        return "Rst [result=" + result + ", error=" + error + "]";
    }

}
