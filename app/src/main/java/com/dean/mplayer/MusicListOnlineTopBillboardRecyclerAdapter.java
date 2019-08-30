package com.dean.mplayer;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dean.mplayer.onlineTopBillboard.Tracks;

import java.util.List;

public class MusicListOnlineTopBillboardRecyclerAdapter extends RecyclerView.Adapter<MusicListOnlineTopBillboardRecyclerAdapter.ListClockRecyclerAdapterHolder> implements View.OnClickListener{

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

    void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    //点击事件接口
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
