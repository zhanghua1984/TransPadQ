package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.cache.ImageDownloadModule;
import cn.transpad.transpadui.entity.Shortcut;

/**
 * Created by ctccuser on 2015/4/5.
 */
public class MyAppAdapter extends BaseAdapter {
    public static final String TAG = MyAppAdapter.class.getSimpleName();
    Context context;
    List<Shortcut> mShortcutList = new ArrayList<>();

    public MyAppAdapter(Context context) {
        this.context = context;
    }

    public void setShortcutList(List<Shortcut> shortcutList) {
        this.mShortcutList = shortcutList;
    }

    @Override
    public int getCount() {
        return mShortcutList != null ? mShortcutList.size() + 1 : 1;
    }

    @Override
    public Object getItem(int position) {
        if (position == mShortcutList.size()) {
            return null;
        }
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_demonstration_app, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        setViewDate(viewHolder, position);

        return convertView;
    }

    public void setViewDate(ViewHolder viewHolder, int position) {
        if (position == mShortcutList.size()) {
            viewHolder.appImageView.setImageResource(R.drawable.add_icon);
            viewHolder.appTextView.setText(R.string.add_app);
        } else {
            String packageName = mShortcutList.get(position).getShortcutPath();
            String activityName = mShortcutList.get(position).getActivityName();
            ImageDownloadModule.getInstance().displayAppIconImage(packageName, activityName, R.drawable.ic_launcher, viewHolder.appImageView);
            viewHolder.appTextView.setText(mShortcutList.get(position).getName());
        }
    }

    static class ViewHolder {

        @InjectView(R.id.item_appImage)
        ImageView appImageView;
        @InjectView(R.id.item_appName)
        TextView appTextView;

        ViewHolder(View convertView) {
            ButterKnife.inject(this, convertView);
        }
    }

}
