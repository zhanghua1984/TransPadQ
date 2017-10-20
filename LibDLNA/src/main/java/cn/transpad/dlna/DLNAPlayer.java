package cn.transpad.dlna;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import cn.transpad.dlna.entity.DLNADevice;

/**
 * Created by Kongxiaojun on 16/1/6.
 */
public class DLNAPlayer {

    public static int PLAYSTATE_PLAYING = 1;
    public static int PLAYSTATE_TRANSITIONING = 2;
    public static int PLAYSTATE_PAUSED_PLAYBACK = 3;
    public static int PLAYSTATE_STOPPED = 4;
    public static int PLAYSTATE_UNKNOWN = -1;

    private static final String TAG = "DLNAPlayer";

    private static DLNAPlayer instance;

    private IDLNAService mService;

    private Application application;

    private DLNADeviceChangeListener deviceChangeListener;

    private DLNAPlayer(Application application) {
        this.application = application;
        connectService();
    }

    public synchronized static DLNAPlayer getInstance(Application application) {
        if (instance == null) {
            instance = new DLNAPlayer(application);
        }
        return instance;
    }

    private void connectService() {
        ServiceConnection conn = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = IDLNAService.Stub.asInterface(service);
                try {
                    mService.start();
                    mService.registerCallback(changedCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }
        };
        Intent intent = new Intent(application, DLNAService.class);
        application.bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    /**
     * play url
     *
     * @param uuid  device's server_uid
     * @param file  to play url
     * @param title display title
     * @param icon  display icon url
     * @param pos   play postion unit second
     * @return boolean result
     */
    public boolean open(final String uuid, final String file, final String title, final String icon, final int pos) {
        try {
            String url = handleUrl(file);
            Log.i(TAG, " dlna to open source:" + file);
            Log.i(TAG, " dlna to open url:" + url);
            if (mService != null) {
                return mService.dlna_open(uuid, url, title, icon, pos);
            }
        } catch (RemoteException e) {
            Log.i(TAG, "dlna to open error:" + e);
        }
        return false;
    }

    public boolean openAudio(final String uuid, final String file, final String title, final String icon, final int pos) {
        try {
            String url = handleUrl(file);
            Log.i(TAG, " dlna to open source:" + file);
            Log.i(TAG, " dlna to open url:" + url);
            if (mService != null) {
                return mService.dlna_openAudio(url, uuid, title, icon, pos);
            }
        } catch (RemoteException e) {
            Log.i(TAG, "dlna to open error:" + e);
        }
        return false;
    }

    public boolean openPicture(final String uuid, final String file, final String title) {
        try {
            String url = handleUrl(file);
            Log.i(TAG, " dlna to open source:" + file);
            Log.i(TAG, " dlna to open url:" + url);
            if (mService != null) {
                return mService.dlna_openPicture(url, uuid, title);
            }
        } catch (RemoteException e) {
            Log.i(TAG, "dlna to open error:" + e);
        }
        return false;
    }

    /**
     * stop play dlna video
     *
     * @param uuid device server_uid
     * @return int result
     * @throws
     */
    public boolean stop(final String uuid) {
        boolean result = false;
        try {
            if (mService != null) {
                result = mService.dlna_stop(uuid);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * seek video audio
     *
     * @param uuid
     * @param seconds
     * @return
     */
    public boolean seek(final String uuid, final long seconds) {
        boolean result = false;
        try {
            if (mService != null) {
                result = mService.dlna_seek(uuid, seconds);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * get volume
     *
     * @param uuid
     * @return
     */
    public int getVolume(final String uuid) {
        try {
            if (mService != null) {
                return mService.dlna_get_play_volume(uuid);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * set volume
     *
     * @param uuid
     * @param volume
     * @return
     */
    public boolean setVolume(final String uuid, final int volume) {
        try {
            if (mService != null) {
                return mService.dlna_set_volume(uuid, volume);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * resume play
     *
     * @param uuid
     * @return
     */
    public boolean resume(String uuid) {
        try {
            if (mService != null) {
                return mService.dlna_resume(uuid);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * pause
     *
     * @param uuid
     * @return
     */
    public boolean pause(String uuid) {
        try {
            if (mService != null) {
                return mService.dlna_pause(uuid);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * get play state
     *
     * @param uuid
     * @return
     */
    public int getPlayState(String uuid) {
        try {
            if (mService != null) {
                return mService.dlna_get_play_state(uuid);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return DLNAPlayer.PLAYSTATE_UNKNOWN;
    }

    /**
     * get duration
     *
     * @param uuid
     * @return duration unit second
     */
    public long getDuration(final String uuid) {
        try {
            if (mService != null) {
                return mService.dlna_get_duration(uuid);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * get play current time
     *
     * @param uuid
     * @return current time unit second
     */
    public long getCurrentTime(final String uuid) {
        try {
            if (mService != null) {
                return mService.dlna_get_play_curtime(uuid);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * convert domain name to ip ex:www.baidu.com->192.191.11.1
     *
     * @param file url
     * @return String
     * @throws
     */
    private static String convertDnsToIpUrl(String file) {
        String url = null;
        String ip = null;
        try {
            int i_s = file.indexOf("://");
            if (i_s > -1) {
                int i_e = file.indexOf('/', i_s + 3);
                if (i_e > -1) {
                    String dns = file.substring(i_s + 3, i_e);
                    if (dns.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*")) {
                        url = file;
                    } else {
                        ip = InetAddress.getByName(dns).getHostAddress();
                        url = file.replace(dns, ip);
                    }
                } else
                    url = file;
            } else
                url = file;
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
            url = file;
        }
        return url;

    }

    public List<DLNADevice> getDeviceList() {
        if (mService != null) {
            try {
                return mService.getDevices();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public DLNADevice getDevice(String server_uid) {
        if (mService != null) {
            try {
                return mService.getDevice(server_uid);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setDeviceChangeListener(DLNADeviceChangeListener deviceChangeListener) {
        this.deviceChangeListener = deviceChangeListener;
    }

    public interface DLNADeviceChangeListener {
        void changed(String server_uid, boolean add);
    }

    public IDeviceChangedCallback changedCallback = new IDeviceChangedCallback.Stub() {
        @Override
        public void onDeviceChanged(String server_uid, boolean add) throws RemoteException {
            if (deviceChangeListener != null) {
                deviceChangeListener.changed(server_uid, add);
            }
        }
    };

    public String getLocalIpAndPort() {
        return Util.getLocalIP(application) + ":" + DLNAService.PORT;
    }

    private String handleUrl(String file) {
        String url;
        if (file.startsWith("/")) {
            url = "http://" + getLocalIpAndPort() + file;
        } else {
            url = convertDnsToIpUrl(file);
        }
        url = url.replaceAll(" ", "%20");
        return url;
    }

}
