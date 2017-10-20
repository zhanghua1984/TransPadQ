package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.cache.ImageDownloadModule;
import cn.transpad.transpadui.entity.Shortcut;
import cn.transpad.transpadui.util.L;

/**
 * Created by ctccuser on 2015/4/5.
 */
public class AllAppAdapterLite extends BaseAdapter {
    public static final String TAG = MyAppAdapter.class.getSimpleName();
    Context context;
    List<Shortcut> mShortcutList;

    public AllAppAdapterLite(Context context) {
        this.context = context;
    }

    public void setShortcutList(List<Shortcut> shortcutList) {

        this.mShortcutList = shortcutList;
    }

    @Override
    public int getCount() {
        return mShortcutList != null ? mShortcutList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mShortcutList != null ? mShortcutList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_demonstration_app_lite, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        setViewDate(viewHolder, position);

        return convertView;
    }

    public void setViewDate(ViewHolder viewHolder, int position) {
        L.v(TAG, "setViewDate", "start position=" + position);
        if (mShortcutList != null) {
            Shortcut shortcut = mShortcutList.get(position);
            if (shortcut != null) {
                String packageName = shortcut.getShortcutPath();
                String activityName = shortcut.getActivityName();
                ImageDownloadModule.getInstance().displayAppIconImage(packageName, activityName, R.drawable.ic_launcher, viewHolder.appImageView);
                viewHolder.appTextView.setText(shortcut.getName());
                if (shortcut.isSelect()) {
                    viewHolder.selectState.setImageResource(R.drawable.media_choose);
                } else {
                    viewHolder.selectState.setImageResource(R.drawable.un_select);
                }

            }
        }
    }

    public static class ViewHolder {

        @InjectView(R.id.item_appImage)
        ImageView appImageView;
        @InjectView(R.id.item_appName)
        TextView appTextView;
        @InjectView(R.id.item_select)
        ImageView selectState;

        ViewHolder(View convertView) {
            ButterKnife.inject(this, convertView);
        }
    }

}
