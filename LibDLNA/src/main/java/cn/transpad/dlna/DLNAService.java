package cn.transpad.dlna;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.device.DeviceChangeListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.transpad.dlna.entity.DLNADevice;
import fi.iki.elonen.HTTPServer;


/**
 * Created by Kongxiaojun on 16/1/6.
 */
public class DLNAService extends Service implements DeviceChangeListener {

    private static final String TAG = "DLNAService";

    public static int PORT = 19999;

    private RemoteCallbackList<IDeviceChangedCallback> callbackList = new RemoteCallbackList<>();

    private DLNAControlPoint dlnaControlPoint;

    private HashMap<String, Device> deviceHashMap = new HashMap<>();

    private boolean start = false;

    private HTTPServer httpServer;

    @Override
    public void onCreate() {
        super.onCreate();
        startHttpServer();
        dlnaControlPoint = new DLNAControlPoint();
        dlnaControlPoint.setDeviceChangeListener(DLNAService.this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        IDLNAService.Stub mBinder = new IDLNAService.Stub() {

            @Override
            public void start() throws RemoteException {
                Log.v(TAG, "start");
                start = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dlnaControlPoint.start();
                    }
                }).start();
                startRefreshDeviceThread();
            }

            @Override
            public void stop() throws RemoteException {
                start = false;
            }

            @Override
            public boolean isStarted() throws RemoteException {
                return start;
            }

            @Override
            public void reSearchDevs() throws RemoteException {

            }

            @Override
            public boolean dlna_open(String uuid, String file, String title, String icon, int pos) throws RemoteException {
                boolean open = false;
                if (dlnaControlPoint != null) {
                    open = dlnaControlPoint.play(file, uuid, title);
                    if (pos > 0) {
                        dlna_seek(uuid, pos);
                    }
                }
                return open;
            }

            @Override
            public boolean dlna_openPicture(String playurl, String devuuid, String playTitle) throws RemoteException {
                if (dlnaControlPoint != null) {
                    return dlnaControlPoint.playPicure(playurl, devuuid, playTitle);
                }
                return false;
            }

            @Override
            public boolean dlna_openAudio(String playurl, String devuuid, String playTitle,String icon,int pos) throws RemoteException {
                boolean open = false;
                if (dlnaControlPoint != null) {
                    open = dlnaControlPoint.playAudio(playurl, devuuid, playTitle);
                    if (pos > 0) {
                        dlna_seek(devuuid, pos);
                    }
                }
                return open;
            }

            @Override
            public boolean dlna_pause(String uuid) throws RemoteException {
                if (dlnaControlPoint != null){
                    return dlnaControlPoint.playPause(uuid,false);
                }
                return false;
            }

            @Override
            public boolean dlna_resume(String uuid) throws RemoteException {
                if (dlnaControlPoint != null){
                    return dlnaControlPoint.playPause(uuid,true);
                }
                return false;
            }

            @Override
            public boolean dlna_seek(String uuid, long seconds) throws RemoteException {
                if (seconds < 0) {
                    seconds = 0;
                }
                long hours = seconds / 3600;
                long minutes = (seconds / 60) % 60;
                long second = seconds % 60;
                String formatTime = String.format("%1$02d:%2$02d:%3$02d", hours, minutes, second);
                if (dlnaControlPoint != null){
                    return dlnaControlPoint.seek(uuid,formatTime);
                }
                return false;
            }

            @Override
            public boolean dlna_stop(String uuid) throws RemoteException {
                if (dlnaControlPoint != null){
                    return dlnaControlPoint.stop(uuid);
                }
                return false;
            }

            @Override
            public boolean dlna_set_volume(String uuid, int volume) throws RemoteException {
                if (dlnaControlPoint != null){
                    return dlnaControlPoint.setVolume(uuid,volume);
                }
                return false;
            }

            @Override
            public int dlna_get_play_state(String uuid) throws RemoteException {
                if (dlnaControlPoint != null){
                    return dlnaControlPoint.getPlayState(uuid);
                }
                return -1;
            }

            @Override
            public int dlna_get_play_volume(String uuid) throws RemoteException {
                if (dlnaControlPoint != null){
                    return dlnaControlPoint.getVolume(uuid);
                }
                return -1;
            }

            @Override
            public long dlna_get_duration(String uuid) throws RemoteException {
                if (dlnaControlPoint != null){
                    return get60Unit(dlnaControlPoint.getDuration(uuid));
                }
                return -1;
            }

            @Override
            public long dlna_get_play_curtime(String uuid) throws RemoteException {
                if (dlnaControlPoint != null){
                    return get60Unit(dlnaControlPoint.getCurrentTime(uuid));
                }
                return -1;
            }

            @Override
            public List<DLNADevice> getDevices() throws RemoteException {
                return DLNAService.this.getDevices();
            }

            @Override
            public DLNADevice getDevice(String uuid) throws RemoteException {
                return Util.transformDevice(dlnaControlPoint.getDevice(uuid));
            }

            @Override
            public String getDeviceManufactur(String devuuid) throws RemoteException {
                return null;
            }

            @Override
            public void registerCallback(IDeviceChangedCallback cb) throws RemoteException {
                callbackList.register(cb);
            }

            @Override
            public void unRegisterCallback(IDeviceChangedCallback cb) throws RemoteException {
                callbackList.unregister(cb);
            }
        };
        return mBinder;
    }

    private void startHttpServer() {
        httpServer = new HTTPServer(PORT);
        try {
            httpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopHttpServer() {
        httpServer.stop();
        httpServer = null;
    }

    private synchronized List<DLNADevice> getDevices() {
        if (deviceHashMap != null && !deviceHashMap.isEmpty()) {
            List<DLNADevice> devices = new ArrayList<>();
            for (Map.Entry<String, Device> entry : deviceHashMap.entrySet()) {
                devices.add(Util.transformDevice(entry.getValue()));
            }
            return devices;
        }
        return null;
    }

    @Override
    public synchronized void deviceAdded(Device dev) {
        if (dev.isDeviceType(DLNADevice.DEVICE_TYPE)) {
            if (!deviceHashMap.containsKey(dev.getUDN())) {
                deviceHashMap.put(dev.getUDN(), dev);
                try {
                    final int n = callbackList.beginBroadcast();
                    for (int i = 0; i < n; i++) {
                        callbackList.getBroadcastItem(i).onDeviceChanged(dev.getUDN(), true);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        callbackList.finishBroadcast();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public synchronized void deviceRemoved(Device dev) {
        Log.v(TAG, "deviceRemoved name = " + dev.getFriendlyName());
        if (dev.isDeviceType(DLNADevice.DEVICE_TYPE)) {
            if (!TextUtils.isEmpty(dev.getUDN()) && deviceHashMap.containsKey(dev.getUDN())) {
                deviceHashMap.remove(dev.getUDN());
                try {
                    final int n = callbackList.beginBroadcast();
                    for (int i = 0; i < n; i++) {
                        callbackList.getBroadcastItem(i).onDeviceChanged(dev.getUDN(), false);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        callbackList.finishBroadcast();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void startRefreshDeviceThread() {
        if (refreshDeviceThread == null){
            refreshDeviceThread = new Thread(refreshDeviceRunnable);
            refreshDeviceThread.setDaemon(true);
        }
        if (!refreshDeviceThread.isAlive()) {
            refreshDeviceThread.start();
        }
    }

    Thread refreshDeviceThread;

    private Runnable refreshDeviceRunnable = new Runnable() {
        @Override
        public void run() {
            while (start) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (dlnaControlPoint == null){
                    continue;
                }
                HashMap<String, Device> deviceMap = dlnaControlPoint.getAvailableDevicesMap();
                List<Device> tempList = new ArrayList<>();
                if (deviceMap != null && !deviceMap.isEmpty()) {
                    for (Map.Entry<String, Device> entry : deviceMap.entrySet()) {
                        Device device = entry.getValue();
                        if (!deviceHashMap.containsKey(device.getUDN())) {
                            tempList.add(device);
                        }
                    }
                    if (!tempList.isEmpty()) {
                        for (Device device : tempList) {
                            deviceAdded(device);
                        }
                    }
                    tempList.clear();
                    if (!deviceHashMap.isEmpty()) {
                        synchronized (DLNAService.this) {
                            for (Map.Entry<String, Device> entry : deviceHashMap.entrySet()) {
                                String uid = entry.getKey();
                                if (!deviceMap.containsKey(uid)) {
                                    tempList.add(entry.getValue());
                                }
                            }
                        }
                    }
                    if (!tempList.isEmpty()) {
                        for (Device device : tempList) {
                            deviceRemoved(device);
                        }
                    }
                }
                try {
                    dlnaControlPoint.restart();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopHttpServer();
        start = false;
        dlnaControlPoint.stop();
        dlnaControlPoint = null;
        try {
            if (refreshDeviceThread != null){
                refreshDeviceThread.interrupt();
                refreshDeviceThread = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static int get60Unit(String duration) {
        try {
            int t = 1;
            int seconds = 0;
            String[] times = duration.split(":");
            for (int i = times.length - 1; i > -1; i--) {
                seconds += Integer.parseInt(times[i]) * t;
                t *= 60;
            }
            return seconds;
        } catch (Exception e) {
            return -1;
        }

    }
}
