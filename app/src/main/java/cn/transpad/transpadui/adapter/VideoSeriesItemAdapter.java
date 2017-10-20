package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.http.DramaRst;

/**
 * Created by wangshaochun on 2015/5/26.
 */
public class VideoSeriesItemAdapter extends BaseAdapter {
    private Context mContext;
    private List<DramaRst.Cnt> cntLists;
    public VideoSeriesItemAdapter(Context context,List<DramaRst.Cnt> cntList) {
        this.mContext = context;
        cntLists = cntList;
    }
    @Override
    public int getCount() {
        return cntLists != null ? cntLists.size() : 0;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        if(convertView==null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.video_selected_content_item2, null);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        //设置holder
        holder.tv_name.setText(cntLists.get(position).name);
        return convertView;
    }
    @Override
    public Object getItem(int position) {
        return cntLists != null ? cntLists.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    class ViewHolder{
        TextView tv_name;
    }
    public void setData(List<DramaRst.Cnt> cntList2) {
        cntLists = cntList2;
        notifyDataSetChanged();
    }
}
