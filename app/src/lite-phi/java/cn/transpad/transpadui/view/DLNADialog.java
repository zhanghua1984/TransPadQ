package cn.transpad.transpadui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.transpad.dlna.DLNAPlayer;
import cn.transpad.dlna.entity.DLNADevice;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.DLNADialogListViewAdapter;
import cn.transpad.transpadui.main.TransPadApplication;
import cn.transpad.transpadui.storage.StorageModule;
import cn.transpad.transpadui.util.L;
import de.greenrobot.event.EventBus;

/**
 * Created by yusiyang on 16/1/7.
 */
public class DLNADialog extends Dialog {
    private static final String TAG = "DLNADialog";
    private static final int DEVICE_LIST_CHANGED = 700001;
    private DLNADialogListViewAdapter adapter;
    private List<DLNADevice> deviceList;

    public DLNADialog(Context context) {
        super(context);
    }

    public DLNADialog(Context context, int theme) {
        super(context, theme);
    }

    @InjectView(R.id.dlna_dialog_title)
    TextView dlnaTitle;
    @InjectView(R.id.dlna_list_view)
    ListView dlnaListView;
    @InjectView(R.id.dlna_no_device)
    TextView noDeviceTextView;
    @InjectView(R.id.dlna_dialog_cancel)
    Button cancel;

    private OnButtonClickListener onButtonClickListener;

    public interface OnButtonClickListener {
        void onButtonClick(DLNADevice device, boolean isCancel);
    }

    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dlna_dialog_layout);
        ButterKnife.inject(this);
        final DLNAPlayer player = DLNAPlayer.getInstance(TransPadApplication.getTransPadApplication());
        deviceList = player.getDeviceList();
        player.setDeviceChangeListener(new DLNAPlayer.DLNADeviceChangeListener() {
            @Override
            public synchronized void changed(String uid, boolean add) {
                deviceList = player.getDeviceList();
                Message message = new Message();
                message.what = DEVICE_LIST_CHANGED;
                EventBus.getDefault().post(message);
            }
        });

        adapter = new DLNADialogListViewAdapter(getContext());
        dlnaListView.setAdapter(adapter);

    }

    @OnClick(R.id.dlna_dialog_ok)
    synchronized void okClick() {
        if (onButtonClickListener != null) {
            int checkedItemPosition = dlnaListView.getCheckedItemPosition();
            if (dlnaListView.getVisibility() == View.VISIBLE && checkedItemPosition >= 0 && deviceList != null && !deviceList.isEmpty()) {
                onButtonClickListener.onButtonClick(deviceList.get(checkedItemPosition), false);
            } else {
                Toast.makeText(getContext(), R.string.dlna_select_nothing, Toast.LENGTH_LONG).show();
                dismiss();
            }
        }
    }

    @OnClick(R.id.dlna_dialog_cancel)
    synchronized void cancelClick() {
        dismiss();
    }

    @Override
    protected synchronized void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        updateView();
    }

    private void updateView() {
        if (deviceList == null || deviceList.size() == 0) {
            noDeviceTextView.setVisibility(View.VISIBLE);
            dlnaListView.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
            dlnaTitle.setText(R.string.dlna_device_not_found);
        } else {
            noDeviceTextView.setVisibility(View.GONE);
            dlnaListView.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
            dlnaTitle.setText(R.string.dlna_select_device);
            if (adapter != null) {
                adapter.setDeviceList(deviceList);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public synchronized void onEventMainThread(Message message) {
        switch (message.what) {
            case DEVICE_LIST_CHANGED:
            case StorageModule.MSG_WIFI_NETWORK_TYPE:
            case StorageModule.MSG_NO_NETWORK_TYPE:
                L.v(TAG, "changed", "DEVICE_LIST_CHANGED");
                updateView();
                break;
            default:
                break;
        }
    }
}
