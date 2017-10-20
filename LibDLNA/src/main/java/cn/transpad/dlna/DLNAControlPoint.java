package cn.transpad.dlna;

import android.util.Log;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.DeviceList;
import org.cybergarage.upnp.Service;
import org.cybergarage.upnp.device.DeviceChangeListener;
import org.cybergarage.upnp.std.av.renderer.AVTransport;
import org.cybergarage.upnp.std.av.renderer.RenderingControl;
import org.cybergarage.upnp.std.av.server.object.DIDLLite;
import org.cybergarage.upnp.std.av.server.object.item.ItemNode;

import java.util.ArrayList;
import java.util.HashMap;

import cn.transpad.dlna.entity.DLNADevice;

/**
 * Created by Kongxiaojun on 16/1/6.
 */
public class DLNAControlPoint {

    private static int PLAYSTATE_PLAYING = 1;
    private static int PLAYSTATE_TRANSITIONING = 2;
    private static int PLAYSTATE_PAUSED_PLAYBACK = 3;
    private static int PLAYSTATE_STOPPED = 4;
    private static int PLAYSTATE_UNKNOWN = -1;

    private long mInstantId = 0L;

    private ControlPoint controlPoint;

    private DeviceChangeListener deviceChangeListener;
    private boolean mUseRelTime = true;
    private static String MACRO_RELTIME = "REL_TIME";
    private static String MACRO_ABSTIME = "ABS_TIME";

    public boolean start() {
        try {
            controlPoint = new ControlPoint();
            controlPoint.setNMPRMode(true);
            if (deviceChangeListener != null) {
                controlPoint.addDeviceChangeListener(deviceChangeListener);
            }
            return controlPoint.start();
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean stop() {
        if (controlPoint != null) {
            return controlPoint.stop();
        }
        return false;
    }

    public void restart() {
        if (controlPoint != null) {
            controlPoint.stop();
            controlPoint = null;
        }
        start();
    }

    public void setDeviceChangeListener(DeviceChangeListener deviceChangeListener) {
        if (controlPoint != null) {
            controlPoint.addDeviceChangeListener(deviceChangeListener);
        }
        this.deviceChangeListener = deviceChangeListener;
    }


    public Device getDevice(String name) {
        if (controlPoint != null) {
            return controlPoint.getDevice(name);
        }
        return null;
    }

    private DeviceList getDeviceList(String deviceType) {
        if (controlPoint != null) {
            DeviceList devList = new DeviceList();
            DeviceList allDevList = controlPoint.getDeviceList();
            int allDevCnt = allDevList.size();

            for (int n = 0; n < allDevCnt; ++n) {
                Device dev = allDevList.getDevice(n);
                if (dev.isDeviceType(deviceType)) {
                    devList.add(dev);
                }
            }

            return devList;
        }
        return null;
    }

    private DeviceList getRendererDeviceList() {
        return this.getDeviceList(DLNADevice.DEVICE_TYPE);
    }

    public ArrayList getAvailableDevices() {
        DeviceList dvlist = this.getRendererDeviceList();
        if (dvlist != null && dvlist.size() > 0) {
            ArrayList outdev = new ArrayList();

            for (int i = 0; i < dvlist.size(); ++i) {
                Device dv = dvlist.getDevice(i);
                if (dv != null) {
                    outdev.add(dv.getUDN());
                }
            }

            return outdev;
        } else {
            return null;
        }
    }

    public HashMap<String, Device> getAvailableDevicesMap() {
        DeviceList dvlist = this.getRendererDeviceList();
        if (dvlist != null && dvlist.size() > 0) {
            HashMap<String, Device> outdev = new HashMap<>();

            for (int i = 0; i < dvlist.size(); ++i) {
                Device dv = dvlist.getDevice(i);
                if (dv != null) {
                    outdev.put(dv.getLocation(), dv);
                }
            }

            return outdev;
        } else {
            return null;
        }
    }

    public boolean play(String playurl, String devuuid, String playtitle) {
        try {
            if (devuuid != null) {
                Device dv = this.getDevice(devuuid);
                if (dv != null) {
                    this.stop(dv);
                    if (this.setUriVideo(dv, playurl.replace("&amp;", "&"), playtitle)) {
                        return this.sendPlayAction(dv);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean playPicure(String playurl, String devuuid, String playTitle) {
        try {
            if (devuuid != null) {
                Device dv = this.getDevice(devuuid);
                if (dv != null) {
                    this.stop(dv);
                    if (this.setUrlPciture(dv, playurl.replace("&amp;", "&"), playTitle)) {
                        return this.sendPlayAction(dv);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean playAudio(String playurl, String devuuid, String playTitle) {
        try {
            if (devuuid != null) {
                Device dv = this.getDevice(devuuid);
                if (dv != null) {
                    this.stop(dv);
                    if (this.setUrlAudio(dv, playurl.replace("&amp;", "&"), playTitle)) {
                        return this.sendPlayAction(dv);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean seek(String devuuid, String seekstring) {
        try {
            if (devuuid != null && seekstring != null && seekstring.length() > 0) {
                Device dv = this.getDevice(devuuid);
                if (dv != null) {
                    return this.seek(dv, seekstring);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean seek(Device paramDevice, String paramString) {
        boolean bool = false;
        try {
            Service localService = paramDevice.getService(AVTransport.SERVICE_TYPE);
            if (localService != null) {
                Action localAction = localService.getAction(AVTransport.SEEK);
                if (localAction != null) {
                    localAction.setArgumentValue(AVTransport.INSTANCEID, "" + this.mInstantId);
                    if (this.mUseRelTime) {
                        localAction.setArgumentValue(AVTransport.UNIT, MACRO_RELTIME);
                    } else {
                        localAction.setArgumentValue(AVTransport.UNIT, MACRO_ABSTIME);
                    }
                    localAction.setArgumentValue(AVTransport.TARGET, paramString);
                    bool = localAction.postControlAction();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bool;
    }

    public boolean playPause(String uuid, boolean play) {
        Device device = getDevice(uuid);
        if (device != null) {
            if (play) {
                return play(device);
            } else {
                return pause(device);
            }
        }

        return false;
    }

    private boolean pause(Device paramDevice) {
        boolean bool = false;
        try {
            if (paramDevice == null) {
                return bool;
            } else {
                Service localService = paramDevice.getService(AVTransport.SERVICE_TYPE);
                if (localService != null) {
                    Action localAction = localService.getAction(AVTransport.PAUSE);
                    if (localAction != null) {
                        localAction.setArgumentValue(AVTransport.INSTANCEID, "" + this.mInstantId);
                        bool = localAction.postControlAction();
                    }
                }
                return bool;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bool;
    }

    private boolean play(Device dev) {
        try {
            if (dev == null) {
                return false;
            } else {
                Service avTransService = dev.getService(AVTransport.SERVICE_TYPE);
                if (avTransService == null) {
                    return false;
                } else {
                    Action action = avTransService.getAction(AVTransport.PLAY);
                    if (action == null) {
                        return false;
                    } else {
                        action.setArgumentValue(AVTransport.INSTANCEID, "" + this.mInstantId);
                        action.setArgumentValue(AVTransport.SPEED, "1");
                        action.setArgumentValue("MediaTypeStarts", "old");
                        return action.postControlAction();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getVolume(String devuuid) {
        try {
            if (devuuid != null) {
                Device dv = this.getDevice(devuuid);
                if (dv != null) {
                    return this.getVolume(dv);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getVolume(Device paramDevice) {
        int i = -1;
        try {
            Service localService = paramDevice.getService(RenderingControl.SERVICE_TYPE);
            if (localService != null) {
                Action localAction = localService.getAction(RenderingControl.GETVOLUME);
                if (localAction != null) {
                    localAction.setArgumentValue(AVTransport.INSTANCEID, "" + this.mInstantId);
                    localAction.setArgumentValue(RenderingControl.CHANNEL, RenderingControl.MASTER);
                    if (localAction.postControlAction()) {
                        i = localAction.getArgumentIntegerValue(RenderingControl.CURRENTVOLUME);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return i;
    }

    public boolean setVolume(String devuuid, int volumeValue) {
        try {
            if (devuuid != null) {
                if (volumeValue > 100) {
                    volumeValue = 100;
                }
                if (volumeValue < 0) {
                    volumeValue = 0;
                }
                Device dv = this.getDevice(devuuid);
                if (dv != null) {
                    return this.setVolume(dv, volumeValue);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean setVolume(Device paramDevice, int paramInt) {
        boolean bool = false;
        try {
            Service localService = paramDevice.getService(RenderingControl.SERVICE_TYPE);
            if (localService == null) {
                return bool;
            } else {
                Action localAction = localService.getAction(RenderingControl.SETVOLUME);
                if (localAction != null) {
                    localAction.setArgumentValue(AVTransport.INSTANCEID, "" + this.mInstantId);
                    localAction.setArgumentValue(RenderingControl.CHANNEL, RenderingControl.MASTER);
                    localAction.setArgumentValue(RenderingControl.DESIREDVOLUME, paramInt);
                    bool = localAction.postControlAction();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bool;
    }

    public String getCurrentTime(String devuuid) {
        try {
            if (devuuid != null) {
                Device dv = this.getDevice(devuuid);
                if (dv != null) {
                    return this.getCurrentTime(dv);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getCurrentTime(Device paramDevice) {
        String str = null;
        try {
            Service localService = paramDevice.getService(AVTransport.SERVICE_TYPE);
            if (localService != null) {
                Action localAction = localService.getAction(AVTransport.GETPOSITIONINFO);
                if (localAction != null) {
                    localAction.setArgumentValue(AVTransport.INSTANCEID, "" + this.mInstantId);
                    if (localAction.postControlAction()) {
                        str = localAction.getArgumentValue(AVTransport.RELTIME);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public String getDuration(String devuuid) {
        try {
            if (devuuid != null) {
                Device dv = this.getDevice(devuuid);
                if (dv != null) {
                    return this.getDuration(dv);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getDuration(Device paramDevice) {
        String str = null;
        try {
            Service localService = paramDevice.getService(AVTransport.SERVICE_TYPE);
            if (localService != null) {
                Action localAction = localService.getAction(AVTransport.GETPOSITIONINFO);
                if (localAction != null) {
                    localAction.setArgumentValue(AVTransport.INSTANCEID, "" + this.mInstantId);
                    if (localAction.postControlAction()) {
                        str = localAction.getArgumentValue(AVTransport.TRACKDURATION);
                        String str2 = localAction.getArgumentValue(AVTransport.RELTIME);
                        if ("NOT_IMPLEMENTED".equals(str2)) {
                            this.mUseRelTime = false;
                            Log.v("CDLNAPlayer", "NOT_IMPLEMENTED:RelTime!");
                        } else {
                            this.mUseRelTime = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return str;
    }

    public int getPlayState(String devuuid) {
        if (devuuid != null) {
            Device dv = this.getDevice(devuuid);
            if (dv != null) {
                String str = this.getTransport(dv);
                if (str == null) {
                    Log.e("DLNAControlPoint", "(:)getPlayState:null");
                    return PLAYSTATE_UNKNOWN;
                }

                Log.i("DLNAControlPoint", "(:)getPlayState:" + str);
                if (str.equals("STOPPED")) {
                    return PLAYSTATE_STOPPED;
                }

                if (str.equals("PLAYING")) {
                    return PLAYSTATE_PLAYING;
                }

                if (str.equals("TRANSITIONING")) {
                    return PLAYSTATE_TRANSITIONING;
                }

                if (str.equals("PAUSED_PLAYBACK")) {
                    return PLAYSTATE_PAUSED_PLAYBACK;
                }

                if (str.equals("NO_MEDIA_PRESENT")) {
                    return PLAYSTATE_UNKNOWN;
                }
            }
        }

        return PLAYSTATE_UNKNOWN;
    }

    private String getTransport(Device paramDevice) {
        String str = null;
        try {
            Service localService = paramDevice.getService(AVTransport.SERVICE_TYPE);
            if (localService == null) {
                return str;
            } else {
                Action localAction = localService.getAction(AVTransport.GETTRANSPORTINFO);
                if (localAction != null) {
                    localAction.setArgumentValue(AVTransport.INSTANCEID, "" + this.mInstantId);
                    if (localAction.postControlAction()) {
                        str = localAction.getArgumentValue(AVTransport.CURRENTTRANSPORTSTATE);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    private boolean sendPlayAction(Device paramDevice) {
        boolean bool = false;
        if (paramDevice != null) {
            Service localService;
            Action localAction;
            localService = paramDevice.getService(AVTransport.SERVICE_TYPE);
            if (localService != null) {
                localAction = localService.getAction(AVTransport.PLAY);
                localAction.setArgumentValue(AVTransport.INSTANCEID, "" + this.mInstantId);
                localAction.setArgumentValue(AVTransport.SPEED, "1");
                bool = localAction.postControlAction();
            }
        }

        return bool;
    }


    private boolean setUriVideo(Device paramDevice, String uri, String title) {
        boolean bool = false;
        if (!uri.toLowerCase().startsWith("http") && !uri.toLowerCase().startsWith("rtsp")) {
            return bool;
        } else {
            bool = this.setAVTransportURI(paramDevice, this.episodeToItemVideoNode(uri, title, "video/mp4"));
            if (!bool) {
                bool = this.setAVTransportURI(paramDevice, this.episodeToItemVideoNode(uri, title, "video/*"));
            }

            if (!bool) {
                bool = this.setAVTransportURI(paramDevice, uri);
            }

            return bool;
        }
    }

    private boolean setUrlPciture(Device paramDevice, String uri, String title) {
        boolean bool = false;
        if (!uri.toLowerCase().startsWith("http")) {
            return bool;
        } else {
            bool = this.setAVTransportURI(paramDevice, this.episodeToItemPicNode(uri, title, "image/jpeg"));
            if (!bool) {
                bool = this.setAVTransportURI(paramDevice, this.episodeToItemPicNode(uri, title, "image/*"));
            }

            return bool;
        }
    }

    private boolean setUrlAudio(Device paramDevice, String uri, String title) {
        boolean bool = false;
        if (!uri.toLowerCase().startsWith("http")) {
            return bool;
        } else {
            bool = this.setAVTransportURI(paramDevice, this.episodeToItemAudNode(uri, title, "audio/mp3"));
            if (!bool) {
                bool = this.setAVTransportURI(paramDevice, this.episodeToItemAudNode(uri, title, "audio/*"));
            }

            return bool;
        }
    }

    private ItemNode episodeToItemVideoNode(String CurrentUrl, String videotitle, String mimetype) {
        ItemNode localUrlItemNode = new ItemNode();
        String str1 = "http-get:*:" + mimetype + ":*";
        String str2 = CurrentUrl;
        if (CurrentUrl != null) {
            str2 = CurrentUrl.replaceAll("&", "&amp;");
        }

        localUrlItemNode.setUrl(str2);
        localUrlItemNode.setResource(str2, str1);
        if (videotitle != null && videotitle.length() > 0) {
            localUrlItemNode.setTitle(videotitle);
        } else {
            localUrlItemNode.setTitle("no title");
        }

        if (CurrentUrl.contains("&locid=")) {
            String locid = null;
            String start = CurrentUrl.substring(CurrentUrl.indexOf("&locid=") + 7);
            if (start.contains("&")) {
                locid = start.substring(0, start.indexOf("&"));
            } else {
                locid = start;
            }

            localUrlItemNode.setUPnPClass("object.item.videoItem.movie");
            if (locid != null) {
                localUrlItemNode.setID(locid);
            }
        }

        return localUrlItemNode;
    }

    private ItemNode episodeToItemPicNode(String CurrentUrl, String title, String mimetype) {
        ItemNode localUrlItemNode = new ItemNode();
        String str1 = "http-get:*:" + mimetype + ":*";
        String str2 = CurrentUrl;
        if (CurrentUrl != null) {
            str2 = CurrentUrl.replaceAll("&", "&amp;");
        }

        localUrlItemNode.setUrl(str2);
        localUrlItemNode.setResource(str2, str1);
        if (title != null && title.length() > 0) {
            localUrlItemNode.setTitle(title);
        } else {
            localUrlItemNode.setTitle("no title");
        }

        return localUrlItemNode;
    }

    private ItemNode episodeToItemAudNode(String CurrentUrl, String title, String mimetype) {
        ItemNode localUrlItemNode = new ItemNode();
        String str1 = "http-get:*:" + mimetype + ":*";
        String str2 = CurrentUrl;
        if (CurrentUrl != null) {
            str2 = CurrentUrl.replaceAll("&", "&amp;");
        }

        localUrlItemNode.setUrl(str2);
        localUrlItemNode.setResource(str2, str1);
        if (title != null && title.length() > 0) {
            localUrlItemNode.setTitle(title);
        } else {
            localUrlItemNode.setTitle("no title");
        }

        return localUrlItemNode;
    }

    private boolean setAVTransportURI(Device paramDevice, String paramString) {
        boolean bool = false;
        if (paramString != null && paramString.length() > 0) {
            Service localService = paramDevice.getService("urn:schemas-upnp-org:service:AVTransport:1");
            if (localService != null) {
                Action localAction = localService.getAction("SetAVTransportURI");
                if (localAction != null) {
                    localAction.setArgumentValue(AVTransport.INSTANCEID, "" + this.mInstantId);
                    localAction.setArgumentValue(AVTransport.CURRENTURI, paramString);
                    localAction.setArgumentValue(AVTransport.CURRENTURIMETADATA, "");
                    bool = localAction.postControlAction();
                }
            }
        }

        return bool;
    }

    private boolean setAVTransportURI(Device dev, ItemNode itemNode) {
        if (dev == null) {
            return false;
        } else {
            String resURL = itemNode.getUrl();
            if (resURL != null && resURL.length() > 0) {
                Service avTransService = dev.getService(AVTransport.SERVICE_TYPE);
                if (avTransService == null) {
                    return false;
                } else {
                    Action action = avTransService.getAction(AVTransport.SETAVTRANSPORTURI);
                    if (action == null) {
                        return false;
                    } else {
                        action.setArgumentValue(AVTransport.INSTANCEID, "" + this.mInstantId);
                        action.setArgumentValue(AVTransport.CURRENTURI, resURL);
                        DIDLLite localDIDLLite = new DIDLLite();
                        localDIDLLite.setContentNode(itemNode);
                        action.setArgumentValue(AVTransport.CURRENTURIMETADATA, localDIDLLite.toxmlString());
                        return action.postControlAction();
                    }
                }
            } else {
                return false;
            }
        }
    }


    public boolean stop(String devuuid) {
        try {
            if (devuuid != null) {
                Device dv = this.getDevice(devuuid);
                if (dv != null) {
                    return this.stop(dv);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean stop(Device dev) {
        if (dev == null)
            return false;
        try {
            Service avTransService = dev.getService(AVTransport.SERVICE_TYPE);
            if (avTransService == null)
                return false;

            Action action = avTransService.getAction(AVTransport.STOP);
            if (action == null)
                return false;

            action.setArgumentValue(AVTransport.INSTANCEID, "0");

            return action.postControlAction();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
