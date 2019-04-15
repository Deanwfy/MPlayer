package com.dean.mplayer;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dean.mplayer.onlineTopBillboard.Tracks;

import java.util.List;

public class MusicListOnlineTopBillboardRecyclerAdapter extends RecyclerView.Adapter<MusicListOnlineTopBillboardRecyclerAdapter.ListClockRecyclerAdapterHolder> implements View.OnClickListener, View.OnLongClickListener{

    class ListClockRecyclerAdapterHolder extends RecyclerView.ViewHolder{
        TextView musicTitle;
        TextView musicArtist;
        TextView musicDuration;
        ListClockRecyclerAdapterHolder(@NonNull View itemView) {
            super(itemView);
            musicTitle = itemView.findViewById(R.id.music_title);
            musicArtist = itemView.findViewById(R.id.music_artist);
            musicDuration = itemView.findViewById(R.id.music_duration);
        }
    }

    private List<Tracks> musicList;
    private OnItemClickListener onItemClickListener = null;
    private MusicListLocalRecyclerAdapter.OnItemLongClickListener onItemLongClickListener = null;
    MusicListOnlineTopBillboardRecyclerAdapter(List<Tracks> musicList){
        this.musicList = musicList;
    }

    @NonNull
    @Override
    public ListClockRecyclerAdapterHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.music_list_item_layout, viewGroup, false);
        view.setOnClickListener(this);
        return new ListClockRecyclerAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListClockRecyclerAdapterHolder listClockRecyclerAdapterHolder, int position) {
        Tracks musicInfo = musicList.get(position);
        listClockRecyclerAdapterHolder.musicTitle.setText(musicInfo.getName());
        listClockRecyclerAdapterHolder.musicArtist.setText(musicInfo.getAr().get(0).getName());    // artists封装在一个数组中,应该是为了处理多个歌手的情况
        listClockRecyclerAdapterHolder.musicDuration.setText(MediaUtil.formatTime(musicInfo.getDt()));
        listClockRecyclerAdapterHolder.itemView.setTag(position);   // setTag - getTag
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener != null) {
            //使用getTag方法获取position
            onItemClickListener.onItemClick(v, (int)v.getTag());
        }
    }
    @Override
    public boolean onLongClick(View v) {
        if (onItemLongClickListener != null){
            onItemLongClickListener.onItemLongClick(v, (int)v.getTag());
        }
        return true;
    }

    void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    void setOnItemLongClickListener(MusicListLocalRecyclerAdapter.OnItemLongClickListener listener){
        this.onItemLongClickListener = listener;
    }

    //点击事件接口
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
    //　长按事件接口
    public interface  OnItemLongClickListener{
        void onItemLongClick(View view, int position);
    }
}
