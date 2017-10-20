package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.cache.ImageDownloadModule;
import cn.transpad.transpadui.entity.OfflineCache;
import cn.transpad.transpadui.util.TPUtil;

/**
 * Created by ctccuser on 2015/4/5.
 */
public class InstalledListAdapter extends BaseAdapter {
    private static final String TAG = InstalledListAdapter.class.getSimpleName();
    private Context context;
    private ArrayList<OfflineCache> viewList;
    private LayoutInflater layoutInflater;

    public InstalledListAdapter(Context context) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setOfflineCacheList(ArrayList<OfflineCache> offlineCacheList) {
        viewList = offlineCacheList;
    }

    @Override
    public int getCount() {
        return viewList != null ? viewList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return viewList != null ? viewList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
//        L.v(TAG, "getView" + viewList.get(position).getCacheName() +
//                "是否安装" + viewList.get(position).getCacheIsInstall());
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.installed_app_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();

        }
        setViewData(viewHolder, position);

        return convertView;
    }

    private void setViewData(ViewHolder viewHolder, final int position) {
        final OfflineCache offlineCache = viewList.get(position);
        //L.v(TAG, "setViewData", offlineCache.getCacheName() + "是否安装" + offlineCache.getCacheIsInstall());
        if (offlineCache != null) {
            viewHolder.downloadAppName.setText(viewList.get(position).getCacheName());
//            L.v(TAG, "setViewData", "getInstall" + offlineCache.getCacheName() +offlineCache.getCachePackageName()+ offlineCache.getCacheIsInstall());
            PackageInfo packageInfo = TPUtil.checkApkExist(context, offlineCache.getCachePackageName());
            //L.v(TAG, "setViewData", "packageInfo=" + packageInfo);
            viewHolder.item_app.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TPUtil.startAppByPackegName(context, viewList.get(position).getCachePackageName());
                }
            });
            ImageDownloadModule.getInstance().displayImage(viewList.get(position).getCacheImageUrl(), viewHolder.item_appImage);
        }
    }

    public static class ViewHolder {
        @InjectView(R.id.item_appImage)
        RoundedImageView item_appImage;
        @InjectView(R.id.item_appName)
        TextView downloadAppName;
        @InjectView(R.id.item_app)
        LinearLayout item_app;

        ViewHolder(View convertView) {
            ButterKnife.inject(this, convertView);
        }
    }

}
