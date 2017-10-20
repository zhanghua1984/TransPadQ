package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.dlna.entity.DLNADevice;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.util.L;

/**
 * Created by yusiyang on 16/1/7.
 */
public class DLNADialogListViewAdapter extends BaseAdapter {
    private static final String TAG = "DLNADialogRecyclerViewAdapter";
    private Context context;
    private List<DLNADevice> deviceList;

    public DLNADialogListViewAdapter(Context context) {
        this.context = context;
    }

    public void setDeviceList(List<DLNADevice> deviceList) {
        L.v(TAG, "setDeviceList", deviceList + "");
        this.deviceList = deviceList;
    }

    @Override
    public int getCount() {
        return deviceList == null ? 0 : deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position + 1000;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.dlna_dialog_item_layout, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (deviceList != null) {
            final DLNADevice device = deviceList.get(position);
            if (device != null) {
                viewHolder.deviceName.setText(device.server_name);
            }
        }
        return convertView;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, String checkedName);
    }

    static class ViewHolder {
        @InjectView(R.id.radio_button)
        RadioButton deviceName;

        public ViewHolder(View itemView) {
            ButterKnife.inject(this, itemView);
        }
    }

}
