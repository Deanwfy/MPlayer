package com.dean.mplayer.view.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dean.mplayer.R;

import java.util.List;

public class MenuClockRecyclerAdapter extends RecyclerView.Adapter<MenuClockRecyclerAdapter.MenuClockRecyclerAdapterHolder> implements View.OnClickListener{

    class MenuClockRecyclerAdapterHolder extends RecyclerView.ViewHolder{
        TextView clockTime;
        MenuClockRecyclerAdapterHolder(@NonNull View itemView) {
            super(itemView);
            clockTime = itemView.findViewById(R.id.clockTime);
        }
    }

    private List<String> items;
    private OnItemClickListener onItemClickListener = null;
    public MenuClockRecyclerAdapter(List<String> items){
        this.items = items;
    }

    @NonNull
    @Override
    public MenuClockRecyclerAdapterHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.drawer_menu_clock_item, viewGroup, false);
        view.setOnClickListener(this);
        return new MenuClockRecyclerAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuClockRecyclerAdapterHolder menuClockRecyclerAdapterHolder, int position) {
        menuClockRecyclerAdapterHolder.clockTime.setText(items.get(position));
        menuClockRecyclerAdapterHolder.itemView.setTag(position);   // setTag - getTag
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
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    //点击事件接口
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
