package cn.transpad.transpadui.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.http.XyzplaRst;
import cn.transpad.transpadui.main.TransPadApplication;
import cn.transpad.transpadui.util.ScreenUtil;

/**
 * 视频清晰度PopupWindow
 *
 * @author kongxiaojun
 * @since 2014-5-26
 */
public class VideoDefinitionPopupWindow {

    private PopupWindow popupWindow;
    private ListView listView;
    private ItemClickCallBack callBack;
    private View contentView;
    private List<XyzplaRst.Dfnt> dfntList;

    public VideoDefinitionPopupWindow(Context mContext, List<XyzplaRst.Dfnt> dfntList, ItemClickCallBack callBack) {
        super();
        this.callBack = callBack;
        this.dfntList = dfntList;
        popupWindow = new PopupWindow(mContext);
        contentView = LayoutInflater.from(mContext).inflate(R.layout.video_definition_popupwindow, null);
        listView = (ListView) contentView.findViewById(R.id.video_definition_list);
        listView.setAdapter(new DefinitionAdapter());
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VideoDefinitionPopupWindow.this.callBack.onItemclick(VideoDefinitionPopupWindow.this.dfntList.get(VideoDefinitionPopupWindow.this.dfntList.size() - 1 - position));
            }
        });

        popupWindow.setContentView(contentView);
        popupWindow.setWidth(ScreenUtil.dp2px(53));
        popupWindow.setHeight(this.dfntList.size() * ScreenUtil.dp2px(44));
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
    }

    /**
     * 弹出popwindow
     *
     * @param view 以这个布局的右下角为基准
     */
    public void show(View view) {
        if (popupWindow != null) {
            popupWindow.showAsDropDown(view, 0, 0);
        }
    }

    /**
     * 隐藏popwindow
     */
    public void dismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public interface ItemClickCallBack {
        void onItemclick(XyzplaRst.Dfnt dfnt);
    }

    private class DefinitionAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (dfntList == null) {
                return 0;
            }
            return dfntList.size();
        }

        @Override
        public Object getItem(int position) {
            if (dfntList == null || dfntList.size() == 0) {
                return null;
            } else {
                return dfntList.get(dfntList.size() - 1 - position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            XyzplaRst.Dfnt dnft = dfntList.get(dfntList.size() - 1 - position);
            convertView = LayoutInflater.from(TransPadApplication.getTransPadApplication()).inflate(R.layout.video_definition_item, null);
            TextView definitionText = (TextView) convertView.findViewById(R.id.definition_text);
            if (dnft.cur == 1) {
                definitionText.setTextColor(TransPadApplication.getTransPadApplication().getResources().getColor(R.color.orange));
            } else {
                definitionText.setTextColor(TransPadApplication.getTransPadApplication().getResources().getColor(R.color.white));
            }
            switch (dnft.t) {
                case 1:
                    // 标清
                    definitionText.setText(R.string.definition_normal);
                    break;
                case 2:
                    // 高清
                    definitionText.setText(R.string.definition_high);
                    break;
                case 3:
                    // 超清
                    definitionText.setText(R.string.definition_super);
                    break;
            }
            return convertView;
        }
    }

    public boolean isShowing() {
        return popupWindow.isShowing();
    }

    public void setDfntList(List<XyzplaRst.Dfnt> dfntList) {
        this.dfntList = dfntList;
    }

}
