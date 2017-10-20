package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.cache.ImageDownloadModule;
import cn.transpad.transpadui.entity.App;
import cn.transpad.transpadui.util.ApplicationUtil;

/**
 * Created by user on 2015/7/30.
 */
public class AllAppAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = AllAppAdapter.class.getSimpleName();
    private Context context;
    private ArrayList<App> allAppList;

    public AllAppAdapter(Context context) {
        this.context = context;
    }

    public void setAllAppData(ArrayList<App> list) {
        this.allAppList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.all_app_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        final App app = allAppList.get(position);
        viewHolder.appName.setText(app.getName());
        String packageName = app.getPackageName();
        ImageDownloadModule.getInstance().displayAppIconImage(packageName, app.getActivityName(), 0, viewHolder.appImage);
        viewHolder.app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (app.getPackageName() != null) {
                    if (app.getActivityName() != null) {
                        ApplicationUtil.startAppByActivityNamePackageName(context, app.getPackageName(), app.getActivityName());
                    } else {
                        ApplicationUtil.startAppByPackageName(context, app.getPackageName());
                    }
                }
                ApplicationUtil.addRecentApp(app);
            }
        });
    }

    @Override
    public int getItemCount() {
        return allAppList != null ? allAppList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.item_app)
        LinearLayout app;
        @InjectView(R.id.item_appName)
        TextView appName;
        @InjectView(R.id.item_appImage)
        ImageView appImage;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
