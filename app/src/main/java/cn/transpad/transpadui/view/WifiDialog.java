package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.MediaFile;

/**
 * Created by wangshaochun on 2015/4/15.
 */
public class WifiDialog extends Dialog {
    ArrayList<MediaFile> videoList;
    ArrayList<MediaFile> checkedVideoList = new ArrayList<>();
    Context mContext;

    public WifiDialog(Context context) {
        super(context, R.style.comm_alertdialog);
        this.mContext = context;
    }

    public WifiDialog(Context context, int theme) {
        super(context, R.style.NoTitle);
        this.mContext = context;
    }

    @InjectView(R.id.btnSwitch)
    TextView btnSwitch;
    @InjectView(R.id.tvWifiName)
    TextView tvWifiName;
    @InjectView(R.id.tvWifiLevel)
    TextView tvWifiLevel;
    @InjectView(R.id.lvWifiList)
    ListView lvWifiList;
    private boolean is5G = false;
    private WifiAdapter wifiAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_wifi_list);
        ButterKnife.inject(this);
        initData();
    }

    private void initData() {
        wifiAdapter = new WifiAdapter();
        lvWifiList.setAdapter(wifiAdapter);
        // 使用定时器,每隔5秒获得一次信号强度值
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                WifiModel wifiModel = new WifiModel();

                WifiManager wifiManager = (WifiManager) mContext
                        .getSystemService(Context.WIFI_SERVICE);
                wifiModel.mWifiInfo = wifiManager.getConnectionInfo();
                wifiModel.wifiInfolevel = getLevel(wifiModel.mWifiInfo.getRssi());
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = wifiModel;
                mHandler.sendMessage(msg);


                List<WifiModel> scanResultList = new ArrayList<>();
                wifiManager.startScan();
                for (ScanResult sr : wifiManager.getScanResults()) {
                    wifiModel = new WifiModel();
                    if (is5G) {
                        if ((sr.frequency + "").startsWith("5")) {
                            wifiModel.mScanResult = sr;
                            wifiModel.scanResultlevel = getLevel(sr.level);
                            scanResultList.add(wifiModel);
                        }
                    } else {
                        if ((sr.frequency + "").startsWith("2")) {
                            wifiModel.mScanResult = sr;
                            wifiModel.scanResultlevel = getLevel(sr.level);
                            scanResultList.add(wifiModel);
                        }
                    }
                }
                msg = mHandler.obtainMessage();
                msg.what = 2;
                msg.obj = scanResultList;
                mHandler.sendMessage(msg);

            }

        }, 1000, 1000);
    }

    private int getLevel(int level) {
        int lev = 0;
        //根据获得的信号强度发送信息
        if (level <= 0 && level >= -50) {
            lev = 1;
        } else if (level < -50 && level >= -70) {
            lev = 2;
        } else if (level < -70) {
            lev = 3;
        }
        return lev;
    }

    @OnClick(R.id.btnSwitch)
    public void switchState() {
        //切换2.4G和5G
        if (is5G) {
            btnSwitch.setText("2.4G");
        } else {
            btnSwitch.setText("5G");
        }
        is5G = !is5G;
        initData();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    WifiModel wifiModel = (WifiModel) msg.obj;
                    tvWifiName.setText(wifiModel.mWifiInfo.getSSID());
                    tvWifiLevel.setText(wifiModel.mWifiInfo.getRssi() + "");
                    switch (wifiModel.wifiInfolevel) {
                        case 1:
                            tvWifiLevel.setTextColor(Color.GREEN);
                            break;
                        case 2:
                            tvWifiLevel.setTextColor(Color.YELLOW);
                            break;
                        case 3:
                            tvWifiLevel.setTextColor(Color.RED);
                            break;
                    }
                    break;
                case 2:
                    List<WifiModel> wifiModelList = (List<WifiModel>) msg.obj;
                    wifiAdapter.setScanResultList(wifiModelList);
                    wifiAdapter.notifyDataSetChanged();
                    break;
            }

        }
    };

    private class WifiModel implements Serializable {
        public WifiInfo mWifiInfo;
        public ScanResult mScanResult;
        public int scanResultlevel;
        public int wifiInfolevel;
        public int type;
    }

    private class WifiAdapter extends BaseAdapter {
        private List<WifiModel> mScanResultList = null;

        public void setScanResultList(List<WifiModel> scanResultList) {
            mScanResultList = scanResultList;
        }

        @Override
        public int getCount() {
            return mScanResultList != null ? mScanResultList.size() : 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = LayoutInflater.from(mContext).inflate(R.layout.dialog_wifi_list_item, null);
            TextView wifiNameTextView = (TextView) view.findViewById(R.id.tvWifiName);
            TextView wifiLevelTextView = (TextView) view.findViewById(R.id.tvWifiLevel);
            if (mScanResultList != null && mScanResultList.size() > 0) {
                WifiModel wifiModel = mScanResultList.get(i);
                wifiNameTextView.setText(wifiModel.mScanResult.SSID);
                // int level = WifiManager.calculateSignalLevel(wifiModel.mScanResult.level, 5); //设定为4级
                int level = wifiModel.mScanResult.level;
                wifiLevelTextView.setText(level + "  " + wifiModel.mScanResult.frequency + "hz");
                switch (wifiModel.scanResultlevel) {
                    case 1:
                        wifiLevelTextView.setTextColor(Color.GREEN);
                        break;
                    case 2:
                        wifiLevelTextView.setTextColor(Color.YELLOW);
                        break;
                    case 3:
                        wifiLevelTextView.setTextColor(Color.RED);
                        break;
                }

            }
            return view;
        }
    }
}
