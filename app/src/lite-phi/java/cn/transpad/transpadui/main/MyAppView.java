package cn.transpad.transpadui.main;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import cn.transpad.transpadui.R;
import cn.transpad.transpadui.adapter.MyAppAdapter;
import cn.transpad.transpadui.entity.Shortcut;
import cn.transpad.transpadui.util.LiteUtil;
import cn.transpad.transpadui.util.TPUtil;

/**
 * Created by user on 2015/5/19.
 */
public class MyAppView extends LinearLayout {
    private List<Shortcut> shortcutList;
    private MyAppAdapter myAppAdapter;

    public MyAppView(Context context) {
        super(context);
        show();
    }

    public void show() {
        inflate(getContext(), R.layout.viewpager_all_layout, this);
        ButterKnife.inject(this);
        shortcutList = LiteUtil.getMyAppList();
        if (shortcutList == null) {
            shortcutList = new ArrayList<>();
        }
        guide.setVisibility(shortcutList.isEmpty() ? VISIBLE : GONE);
        myAppAdapter = new MyAppAdapter(getContext());
        myAppAdapter.setShortcutList(shortcutList);
        gridViewAll.setAdapter(myAppAdapter);
    }

    @InjectView(R.id.gridView_all)
    GridView gridViewAll;

    @InjectView(R.id.add_app_guide)
    ImageView guide;

    @OnItemClick(R.id.gridView_all)
    void itemClick(int position) {
        if (shortcutList == null || position == shortcutList.size()) {
            LiteHomeActivity.switchFragment(new AllFragment());
        } else {
            Shortcut shortcut = shortcutList.get(position);
//        TPUtil.startAppByPackegName(context, app.getPackageName());
            if (shortcut != null && shortcut.getShortcutPath() != null) {
                if (shortcut.getActivityName() != null) {
                    TPUtil.startAppByActvityNamePackageName(getContext(), shortcut.getShortcutPath(), shortcut.getActivityName());
                } else {
                    TPUtil.startAppByPackegName(getContext(), shortcut.getShortcutPath());
                }
            }
        }
    }

    @OnItemLongClick(R.id.gridView_all)
    boolean itemLongClick(final int position) {
        if (shortcutList != null && position != shortcutList.size()) {
            Shortcut shortcut = shortcutList.get(position);
            final Dialog dialog = new Dialog(getContext(), R.style.myDialog);
            dialog.setCancelable(true);
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.remove_myapp_dialog, null);
            TextView message = (TextView) dialogView.findViewById(R.id.message);
            Button ok = (Button) dialogView.findViewById(R.id.button_ok);
            Button cancel = (Button) dialogView.findViewById(R.id.button_cancel);
            ok.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    shortcutList.remove(position);
                    LiteUtil.saveMyAppList(shortcutList);
                    myAppAdapter.notifyDataSetChanged();
                    guide.setVisibility(shortcutList.isEmpty() ? VISIBLE : GONE);
                    dialog.dismiss();
                }
            });
            cancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            message.setText(String.format(getResources().getString(R.string.delete_my_app_dialog), shortcut.getName()));
            dialog.setContentView(dialogView);
            dialog.show();
            return true;
        }
        return false;
    }

    public void refreshMyApp() {
        shortcutList.clear();
        shortcutList = LiteUtil.getMyAppList();
        if (shortcutList == null) {
            shortcutList = new ArrayList<Shortcut>();
        }
        guide.setVisibility(shortcutList.isEmpty() ? VISIBLE : GONE);
        myAppAdapter.setShortcutList(shortcutList);
        myAppAdapter.notifyDataSetChanged();
    }
}
