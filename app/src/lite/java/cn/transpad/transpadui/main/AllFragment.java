package cn.transpad.transpadui.main;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.AllAppAdapter;
import cn.transpad.transpadui.adapter.AllAppAdapterLite;
import cn.transpad.transpadui.entity.ApplicationTab;
import cn.transpad.transpadui.entity.Shortcut;
import cn.transpad.transpadui.util.ApplicationUtil;
import cn.transpad.transpadui.adapter.AllAppAdapterLite.ViewHolder;
import cn.transpad.transpadui.util.LiteUtil;

/**
 * Created by user on 2015/5/19.
 */
public class AllFragment extends BaseFragment {
    Context context;
    private List<Shortcut> shortcutList;
    private AllAppAdapterLite allAppAdapterLite;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        View view = inflater.inflate(R.layout.all_app_layout, container, false);
        ButterKnife.inject(this, view);
        List<ApplicationTab> applicationTabList = ApplicationUtil.getApplicationTabList();
        for (ApplicationTab applicationTab : applicationTabList) {
            switch (applicationTab.getApplicationTabType()) {
                case ApplicationTab.TYPE_LOCAL_APP_LIST:
                    shortcutList = applicationTab.getShortcutList();
                    break;
                default:
                    break;
            }
        }
        allAppAdapterLite = new AllAppAdapterLite(context);
        allAppAdapterLite.setShortcutList(shortcutList);
        gridViewAll.setAdapter(allAppAdapterLite);
        return view;
    }

    @InjectView(R.id.gridView_all)
    GridView gridViewAll;

    @OnClick(R.id.add)
    void addTo() {
        ArrayList<Shortcut> addList = new ArrayList<>();
        for (int i = 0; i < shortcutList.size(); i++) {
            Shortcut shortcut = shortcutList.get(i);
            if (shortcut != null) {
                if (shortcut.isSelect()) {
                    addList.add(shortcut);
                }
            }
        }
        LiteUtil.addMyApp(addList);
        onBack();
    }

    @OnItemClick(R.id.gridView_all)
    void itemClick(int position) {
        Shortcut shortcut = shortcutList.get(position);
        if (shortcut != null) {
            shortcut.setSelect(!shortcut.isSelect());
//            if (allAppAdapterLite != null) {
//                allAppAdapterLite.notifyDataSetChanged();
//            }
            updateView(position);
        }
    }

    public void updateView(int position) {
        int firstVisiblePosition = gridViewAll.getFirstVisiblePosition();
        int selectPosition = position - firstVisiblePosition;
        View selectView = gridViewAll.getChildAt(selectPosition);
        Object object = selectView.getTag();
        if (object instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder) object;
            if (allAppAdapterLite != null) {
                allAppAdapterLite.setViewDate(viewHolder, position);
            }
        }
    }

    @OnClick(R.id.back)
    void goBack() {
        onBack();
    }

}
