package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.entity.ApplicationList;
import cn.transpad.transpadui.util.L;

/**
 * Created by user on 2015/7/30.
 */
public class AllViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = AllViewAdapter.class.getSimpleName();
    private Context context;
    private ArrayList<ApplicationList> allAppList;

    public AllViewAdapter(Context context) {
        this.context = context;
    }

    public void setAllAppData(ArrayList<ApplicationList> list) {
        this.allAppList = list;
    }

    @Override
    public int getItemViewType(int position) {
        return allAppList.get(position).getApplicationListType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case ApplicationList.APPLICATION_LIST_RECENT:
                View viewRecent = LayoutInflater.from(context).inflate(R.layout.all_app_recent_list, parent, false);
                viewHolder = new ViewHolderRecent(viewRecent);
                break;
            case ApplicationList.APPLICATION_LIST_ALL:
                View viewAll = LayoutInflater.from(context).inflate(R.layout.all_app_all_list, parent, false);
                viewHolder = new ViewHolderAll(viewAll);
                break;
            default:
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        ApplicationList applicationList = allAppList.get(position);
        switch (getItemViewType(position)) {
            case ApplicationList.APPLICATION_LIST_ALL:
                ViewHolderAll viewHolderAll = (ViewHolderAll) holder;
                AllAppAdapter allAppAdapter = new AllAppAdapter(context);
                L.v(TAG, "onBindViewHolder", "all=======" + applicationList.getAllList());
                allAppAdapter.setAllAppData(applicationList.getAllList());
                viewHolderAll.recyclerView.setLayoutManager(linearLayoutManager);
                viewHolderAll.recyclerView.setHasFixedSize(true);
                viewHolderAll.recyclerView.setAdapter(allAppAdapter);
                break;
            case ApplicationList.APPLICATION_LIST_RECENT:
                //recent layout
                ViewHolderRecent viewHolderRecent = (ViewHolderRecent) holder;
                AllAppAdapter recentAdapter = new AllAppAdapter(context);
                L.v(TAG, "onBindViewHolder", "recent=====" + applicationList.getRecentList());
                recentAdapter.setAllAppData(applicationList.getRecentList());
                viewHolderRecent.recyclerView.setLayoutManager(linearLayoutManager);
                viewHolderRecent.recyclerView.setHasFixedSize(true);
                viewHolderRecent.recyclerView.setAdapter(recentAdapter);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return allAppList != null ? allAppList.size() : 0;
    }

    public static class ViewHolderRecent extends RecyclerView.ViewHolder {
        @InjectView(R.id.all_app_recycler_view_recent)
        RecyclerView recyclerView;

        public ViewHolderRecent(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    public static class ViewHolderAll extends RecyclerView.ViewHolder {
        @InjectView(R.id.all_app_recycler_view_all)
        RecyclerView recyclerView;

        public ViewHolderAll(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
