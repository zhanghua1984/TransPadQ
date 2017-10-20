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
import cn.transpad.transpadui.main.TransPadApplication;
import cn.transpad.transpadui.util.L;
import cn.transpad.transpadui.util.ScreenUtil;

/**
 * 搜狐视频清晰度PopupWindow
 *
 * @author kongxiaojun
 * @since 2014-8-22
 */
public class SohuVideoDefinitionPopupWindow {

	private PopupWindow popupWindow;
	private ListView listView;
	private ItemClickCallBack callBack;
	private View contentView;
	private List<Integer> dfntList;
	private int currentDfnt;


	public SohuVideoDefinitionPopupWindow(Context mContext, List<Integer> dfntList, ItemClickCallBack callBack, int currentDfnt) {
		super();
		this.callBack = callBack;

		this.dfntList = dfntList;
		this.currentDfnt = currentDfnt;
		popupWindow = new PopupWindow(mContext);
		contentView = LayoutInflater.from(mContext).inflate(R.layout.video_definition_popupwindow, null);
		listView = (ListView) contentView.findViewById(R.id.video_definition_list);
		listView.setAdapter(new DefinitionAdapter());
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SohuVideoDefinitionPopupWindow.this.callBack.onItemclick(SohuVideoDefinitionPopupWindow.this.dfntList.get(SohuVideoDefinitionPopupWindow.this.dfntList.size() - 1 - position));
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
	 * @param view
	 *            以这个布局的右下角为基准
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
		void onItemclick(Integer dfnt);
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
			Integer dfnt = dfntList.get(dfntList.size() - 1 - position);
			L.v("aaaaaaaaa", " dfnt = " + dfnt);
			convertView = LayoutInflater.from(TransPadApplication.getTransPadApplication()).inflate(R.layout.video_definition_item, null);
			TextView definitionText = (TextView) convertView.findViewById(R.id.definition_text);
			if (dfnt == currentDfnt) {
				definitionText.setTextColor(TransPadApplication.getTransPadApplication().getResources().getColor(R.color.orange));
			} else {
				definitionText.setTextColor(TransPadApplication.getTransPadApplication().getResources().getColor(R.color.white));
			}
			switch (dfnt) {
				case 1:
					// 标清
					definitionText.setText(R.string.definition_normal);
					break;
				case 2:
					// 高清
					definitionText.setText(R.string.definition_high);
					break;
				case 4:
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

	public void setDfntList(List<Integer> dfntList,int currentDfnt) {
		this.dfntList = dfntList;
		this.currentDfnt = currentDfnt;
	}

}
