package com.dean.mplayer;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MusicListLocalRecyclerAdapter extends RecyclerView.Adapter<MusicListLocalRecyclerAdapter.MusicListLocalRecyclerAdapterHolder> implements View.OnClickListener, View.OnLongClickListener {

    class MusicListLocalRecyclerAdapterHolder extends RecyclerView.ViewHolder{
        TextView musicTitle;
        TextView musicArtist;
        TextView musicDuration;
        MusicListLocalRecyclerAdapterHolder(@NonNull View itemView) {
            super(itemView);
            musicTitle = itemView.findViewById(R.id.music_title);
            musicArtist = itemView.findViewById(R.id.music_artist);
            musicDuration = itemView.findViewById(R.id.music_duration);
        }
    }

    private List<MusicInfo> musicListLocal;
    private OnItemClickListener onItemClickListener = null;
    private OnItemLongClickListener onItemLongClickListener = null;
    MusicListLocalRecyclerAdapter(List<MusicInfo> musicListLocal){
        this.musicListLocal = musicListLocal;
    }

    @NonNull
    @Override
    public MusicListLocalRecyclerAdapterHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.music_list_item_layout, viewGroup, false);
        view.setOnClickListener(this);
        return new MusicListLocalRecyclerAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicListLocalRecyclerAdapterHolder musicListLocalRecyclerAdapterHolder, int position) {
        MusicInfo musicInfo = musicListLocal.get(position);
        musicListLocalRecyclerAdapterHolder.musicTitle.setText(musicInfo.getTitle());
        musicListLocalRecyclerAdapterHolder.musicArtist.setText(musicInfo.getArtist());
        musicListLocalRecyclerAdapterHolder.musicDuration.setText(MediaUtil.formatTime(musicInfo.getDuration()));
        musicListLocalRecyclerAdapterHolder.itemView.setTag(position);   // setTag - getTag
    }

    @Override
    public int getItemCount() {
        return musicListLocal.size();
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
    void setOnItemLongClickListener(OnItemLongClickListener listener){
        this.onItemLongClickListener = listener;
    }

    //点击事件接口
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
    public interface  OnItemLongClickListener{
        void onItemLongClick(View view, int position);
    }
}
