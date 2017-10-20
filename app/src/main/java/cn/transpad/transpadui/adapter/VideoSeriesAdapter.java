package cn.transpad.transpadui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import cn.transpad.transpadui.R;
import cn.transpad.transpadui.http.DramaRst;

/**
 * Created by wangshaochun on 2015/5/26.
 */
public class VideoSeriesAdapter extends RecyclerView.Adapter<VideoSeriesAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private List<DramaRst.Cnts> cntsList;


    /**
     * ItemClick的回调接口
     *
     */
    public interface OnItemClickLitener
    {
        void onItemClick(View view, int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public VideoSeriesAdapter(List<DramaRst.Cnts> cntsList,Context context){
        super();
        this.cntsList = cntsList;
        mInflater = LayoutInflater.from(context);
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.video_selected_title_item,
                parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.tv_name = (TextView) view.findViewById(R.id.tv_name);
        viewHolder.rl_click = (RelativeLayout)view.findViewById(R.id.rl_click);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        viewHolder.tv_name.setText(cntsList.get(position).name);
        if (cntsList.get(position).isChecked) {
            viewHolder.rl_click.setBackgroundResource(R.color.orange2);
            viewHolder.tv_name.setTextColor(Color.parseColor("#ffffff"));
        } else {
            viewHolder.rl_click.setBackgroundResource(R.color.transparent);
            viewHolder.tv_name.setTextColor(Color.parseColor("#ff8400"));
        }
        //如果设置了回调，则设置点击事件
        if (mOnItemClickLitener != null)
        {
            viewHolder.rl_click.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                            mOnItemClickLitener.onItemClick(viewHolder.rl_click, position);
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return cntsList != null ? cntsList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View arg0) {
            super(arg0);
        }
        RelativeLayout rl_click;
        TextView tv_name;
    }
    public void setData(List<DramaRst.Cnts> cntsList){
        this.cntsList = cntsList;
        notifyDataSetChanged();
    }
}
