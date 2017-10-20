package cn.transpad.transpadui.main;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.AllViewAdapter;
import cn.transpad.transpadui.cache.AsyncTask;
import cn.transpad.transpadui.entity.ApplicationList;
import cn.transpad.transpadui.util.ApplicationUtil;

/**
 * Created by user on 2015/5/19.
 */
public class AllAppView extends LinearLayout {
    private Context context;
    public AllViewAdapter allViewAdapter;
    @InjectView(R.id.all_app_recycler_view)
    RecyclerView allAppRecyclerView;

    public AllAppView(Context context) {
        super(context);
        this.context = context;
        inflate(context, R.layout.all_app_view_layout, this);
        ButterKnife.inject(this);
        allViewAdapter = new AllViewAdapter(context);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        allAppRecyclerView.setLayoutManager(linearLayoutManager);
        allAppRecyclerView.setHasFixedSize(true);
        allAppRecyclerView.setAdapter(allViewAdapter);

        notifyDataSetChange();
    }


    public void notifyDataSetChange() {
        InitAppTask initAppTask = new InitAppTask();
        initAppTask.execute();
    }

    class InitAppTask extends AsyncTask<Void, Void, ArrayList<ApplicationList>> {
        @Override
        protected ArrayList<ApplicationList> doInBackground(Void... params) {
            return ApplicationUtil.initApplicationList();
        }

        @Override
        protected void onPostExecute(ArrayList<ApplicationList> applicationLists) {
            super.onPostExecute(applicationLists);
            if (allViewAdapter != null) {
                allViewAdapter.setAllAppData(applicationLists);
                allViewAdapter.notifyDataSetChanged();
            }
        }
    }

}
