package cn.transpad.transpadui.main;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnPageChange;
import cn.transpad.dlna.DLNAPlayer;
import cn.transpad.dlna.entity.DLNADevice;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.ImageListAdapter;
import cn.transpad.transpadui.entity.MediaFile;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.view.DLNADialog;
import cn.transpad.transpadui.view.PhotoViewPager;
import de.greenrobot.event.EventBus;

/**
 * Created by left on 16/1/12.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PicturePlayFragment extends BaseFragment {

    private static final String TAG = PicturePlayFragment.class.getSimpleName();

    private static final String POSITION = "position";
    private static final String MEDIAFILES = "mediaFiles";
    private static final int DLNA_OPEN_PICTURE_SUCCESS = 6001;
    private static final int DLNA_OPEN_PICTURE_FAIL = 6002;
    private static final int DLNA_OPEN_PICTURE_DEVICE_DISCONNECTED = 6003;

    private int position;
    private List<MediaFile> mediaFiles;

    public PicturePlayFragment() {
    }

    DLNAPlayer player;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        position = getArguments().getInt(POSITION);
        mediaFiles = getArguments().getParcelableArrayList(MEDIAFILES);
        player = DLNAPlayer.getInstance(TransPadApplication.getTransPadApplication());

    }

    @InjectView(R.id.vpPictureList)
    PhotoViewPager viewPager;
    @InjectView(R.id.dlna_running_background)
    LinearLayout runningBackground;
    @InjectView(R.id.dlna_state)
    TextView dlnaState;
    @InjectView(R.id.device_name)
    TextView deviceName;
    @InjectView(R.id.bt_dlna)
    LinearLayout dlnaButton;
    @InjectView(R.id.stop)
    LinearLayout dlnaStop;
    @InjectView(R.id.dlna_opening_layout)
    RelativeLayout dlnaOpeningLayout;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_image_list, container, false);
        ButterKnife.inject(this, view);
        ImageListAdapter myImageListAdapter = new ImageListAdapter(getActivity(), mediaFiles);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(myImageListAdapter);
        viewPager.setCurrentItem(position);

        return view;
    }

    @OnPageChange(R.id.vpPictureList)
    void onPageSelected(final int position) {
        L.v(TAG, "onPageSelected", "position  = " + position + " usedDevice =" + usedDevice + "");
        if (usedDevice != null) {
            showDLNALayout();
            dlnaPicture(usedDevice, mediaFiles.get(position).getMediaFilePath());
        }
    }

    public static PicturePlayFragment newInstance(ArrayList<MediaFile> mediaFiles, int position) {
        PicturePlayFragment fragment = new PicturePlayFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(MEDIAFILES, mediaFiles);
        bundle.putInt(POSITION, position);
        fragment.setArguments(bundle);
        return fragment;
    }

    private DLNADevice usedDevice;
    private DLNADialog dlnaDialog;

    @OnClick(R.id.bt_dlna)
    public void dlnaClick() {
        if (dlnaDialog == null) {
            dlnaDialog = new DLNADialog(getActivity(), R.style.myDialog);
            dlnaDialog.setOnButtonClickListener(new DLNADialog.OnButtonClickListener() {
                @Override
                public void onButtonClick(final DLNADevice device, boolean isCancel) {
                    if (!isCancel) {
                        usedDevice = device;
                        showDLNALayout();
                        dlnaPicture(device, mediaFiles.get(viewPager.getCurrentItem()).getMediaFilePath());
                    }
                    dlnaDialog.dismiss();
                }
            });
        }
        dlnaDialog.show();
    }

    private void dlnaPicture(final DLNADevice device, final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = player.openPicture(device.server_uid, path, "");
                L.v(TAG, "dlnaClick", "success  = " + success);
                if (success) {
                    sendMessageToUI(DLNA_OPEN_PICTURE_SUCCESS);
                } else {
                    sendMessageToUI(DLNA_OPEN_PICTURE_FAIL);
                }
            }
        }).start();
    }

    private void showDLNALayout() {
        dlnaState.setText(R.string.dlna_opening_message);
        viewPager.setVisibility(View.VISIBLE);
        dlnaOpeningLayout.setVisibility(View.GONE);
        runningBackground.setVisibility(View.VISIBLE);
        deviceName.setText(usedDevice.server_name);
        dlnaButton.setVisibility(View.GONE);
        dlnaStop.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.stop)
    void stop() {
        if (usedDevice != null) {
//            player.stop(usedDevice.server_uid);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    player.open(usedDevice.server_uid, "/", "", "", 0);
                    usedDevice = null;
                    thread = null;
                }
            }).start();
            hideDLNALayout();
        }
    }

    private void hideDLNALayout() {
        viewPager.setVisibility(View.VISIBLE);
        dlnaOpeningLayout.setVisibility(View.GONE);
        runningBackground.setVisibility(View.GONE);
        deviceName.setText("");
        dlnaButton.setVisibility(View.VISIBLE);
        dlnaStop.setVisibility(View.GONE);
    }

    @OnClick(R.id.dlna_button_cancel)
    void dlnaCancel() {
        //退出dlna播放，启动正常播放模式
        usedDevice = null;
        thread = null;
        hideDLNALayout();
    }

    @OnClick(R.id.dlna_button_retry)
    void dlnaRetry() {
        //重试打开DLNA
        if (usedDevice != null) {
            showDLNALayout();
            dlnaPicture(usedDevice, mediaFiles.get(viewPager.getCurrentItem()).getMediaFilePath());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
//        usedDevice = null;
//        thread = null;
        stop();
    }

    @OnClick(R.id.back)
    public void backPage() {//返回上级页面的方法
        onBack();
    }

    //    保证最后一次投放的成功与失败,忽略之前的失败
    private static long lastTime;

    private static void sendMessageToUI(int msg) {
        Message message = new Message();
        message.what = msg;
        if (msg == DLNA_OPEN_PICTURE_FAIL) {
            lastTime = System.currentTimeMillis();
            message.obj = lastTime;
        }
        EventBus.getDefault().post(message);
    }

    public void onEventMainThread(Message message) {
        switch (message.what) {
            case DLNA_OPEN_PICTURE_SUCCESS:
                dlnaState.setText(R.string.dlna_playing);
                if (thread == null) {
                    thread = new Thread(runnable);
                    thread.start();
                }
                break;
            case DLNA_OPEN_PICTURE_FAIL:
                if (message.obj instanceof Long) {
                    if (lastTime == (long) message.obj) {
                        viewPager.setVisibility(View.GONE);
                        dlnaOpeningLayout.setVisibility(View.VISIBLE);
                    }
                }
                break;
            case DLNA_OPEN_PICTURE_DEVICE_DISCONNECTED:
                dlnaCancel();
                break;
            default:
                break;
        }
    }

    private Thread thread;

    private Runnable runnable = new Runnable() {
        private int unknownTimes;

        @Override
        public void run() {
            try {
                unknownTimes = 0;
                while (usedDevice != null) {
                    int state = player.getPlayState(usedDevice.server_uid);
                    L.v(TAG, "state = " + state);
                    if (state == DLNAPlayer.PLAYSTATE_UNKNOWN) {
                        unknownTimes++;
                    }
                    if (unknownTimes > 10) {
                        sendMessageToUI(DLNA_OPEN_PICTURE_DEVICE_DISCONNECTED);
                        break;
                    }
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
