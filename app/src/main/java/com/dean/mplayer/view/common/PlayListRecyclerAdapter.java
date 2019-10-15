package com.dean.mplayer.view.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dean.mplayer.PlayList;
import com.dean.mplayer.R;

import java.util.List;

public class PlayListRecyclerAdapter extends RecyclerView.Adapter<PlayListRecyclerAdapter.PlayListRecyclerAdapterHolder> implements View.OnClickListener{

    class PlayListRecyclerAdapterHolder extends RecyclerView.ViewHolder{
        TextView musicInfo;
        PlayListRecyclerAdapterHolder(@NonNull View itemView) {
            super(itemView);
            musicInfo = itemView.findViewById(R.id.play_list_music_info);
        }
    }

    private List<PlayList> playList;
    private OnItemClickListener onItemClickListener = null;
    PlayListRecyclerAdapter(List<PlayList> playList){
        this.playList = playList;
    }

    @NonNull
    @Override
    public PlayListRecyclerAdapterHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.play_list_item, viewGroup, false);
        view.setOnClickListener(this);
        return new PlayListRecyclerAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListRecyclerAdapterHolder playListRecyclerAdapterHolder, int position) {
        PlayList musicInfo = playList.get(position);
        String infoText = musicInfo.title + "-" + musicInfo.artist;
        playListRecyclerAdapterHolder.musicInfo.setText(infoText);
        playListRecyclerAdapterHolder.itemView.setTag(position);   // setTag - getTag
    }

    @Override
    public int getItemCount() {
        return playList.size();
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
