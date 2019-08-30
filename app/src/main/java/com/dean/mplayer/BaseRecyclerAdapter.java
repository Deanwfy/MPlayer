package com.dean.mplayer;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class BaseRecyclerAdapter extends RecyclerView.Adapter<BaseRecyclerAdapter.BaseRecyclerAdapterHolder> implements View.OnClickListener{

    class BaseRecyclerAdapterHolder extends RecyclerView.ViewHolder{
        TextView category;
        BaseRecyclerAdapterHolder(@NonNull View itemView) {
            super(itemView);
            category = itemView.findViewById(R.id.clockTime);
        }
    }

    private List<String> items;
    private OnItemClickListener onItemClickListener = null;
    BaseRecyclerAdapter(List<String> items){
        this.items = items;
    }

    @NonNull
    @Override
    public BaseRecyclerAdapterHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.drawer_menu_clock_item, viewGroup, false);
        view.setOnClickListener(this);
        return new BaseRecyclerAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseRecyclerAdapterHolder baseRecyclerAdapterHolder, int position) {
        baseRecyclerAdapterHolder.category.setText(items.get(position));
        baseRecyclerAdapterHolder.itemView.setTag(position);   // setTag - getTag
//        ViewCompat.setTransitionName(baseRecyclerAdapterHolder.category, "CategoryName");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener != null) {
            //使用getTag方法获取position
            onItemClickListener.onItemClick(v, (int)v.getTag());
        }
    }
    void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    //点击事件接口
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
