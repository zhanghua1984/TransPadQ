package cn.transpad.dlna;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import org.cybergarage.upnp.Device;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import cn.transpad.dlna.entity.DLNADevice;

/**
 * Created by left on 16/1/6.
 */
public class Util {

    public static DLNADevice transformDevice(Device device) {
        if (device != null) {
            DLNADevice dlnaDevice = new DLNADevice();
            dlnaDevice.server_name = device.getFriendlyName();
            dlnaDevice.server_uid = device.getUDN();
            dlnaDevice.proto = 1;
            dlnaDevice.server_ip_addr = getHost(device.getLocation());
            return dlnaDevice;
        }
        return null;
    }


    private static String getHost(String url) {
        if (!TextUtils.isEmpty(url)) {
            String host;
            for (host = url.replaceAll("^[^\\/]*\\/", ""); host.startsWith("/"); host = host.substring(1)) {
                ;
            }

            return host.replaceAll("\\/.*$", "").replaceAll("\\?.*$", "").replaceAll(":.*$", "");
        }
        return null;
    }


    /**
     *
     * get local ip address
     *
     * @return ip address ex:192.168.1.11
     * @throws
     */
    public static String getLocalIP(Context context) {

        String ip_ = wifiAddress(context);
        if (ip_ != null)
            return ip_;

        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    InetAddress local = ips.nextElement();

                    if (!local.isLoopbackAddress() && !local.isLinkLocalAddress()) {
                        String ip = local.toString();
                        if (ip.startsWith("/"))
                            return ip.substring(1);
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return intToIp(wifiManager.getConnectionInfo().getIpAddress());
    }

    /**
     *
     * get wifi address if wifi is connection
     *
     * @param localContext
     * @return wifi ip address ex:192.168.1.11
     * @throws
     */
    public static String wifiAddress(Context localContext) {
        Object[] arrayOfObject;
        String str = null;
        if (localContext != null) {
            WifiManager ocheck = ((WifiManager) localContext.getSystemService(Context.WIFI_SERVICE));
            if (ocheck != null) {
                WifiInfo info = ocheck.getConnectionInfo();
                if (info != null) {
                    int i = info.getIpAddress();
                    if (i != 0) {
                        arrayOfObject = new Object[4];
                        arrayOfObject[0] = Integer.valueOf(i & 0xFF);
                        arrayOfObject[1] = Integer.valueOf(0xFF & i >> 8);
                        arrayOfObject[2] = Integer.valueOf(0xFF & i >> 16);
                        arrayOfObject[3] = Integer.valueOf(0xFF & i >> 24);
                        str = String.format("%d.%d.%d.%d", arrayOfObject);
                    }
                }
            }
        }
        if (str == null) {
            String iptest = null;
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            iptest = inetAddress.getHostAddress().toString();
                            if (!iptest.contains("%") && !iptest.contains("wlan")) {
                                str = iptest;
                                break;
                            }
                        }
                    }
                }
            } catch (SocketException ex) {
                str = "0.0.0.0";
            }
        }
        return str;
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }
}
