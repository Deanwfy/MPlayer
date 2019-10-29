package com.dean.mplayer.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dean.mplayer.data.model.local.localAlbum.LocalAlbum;
import com.dean.mplayer.R;

import java.util.ArrayList;
import java.util.List;

public class MusicListAlbumRecyclerAdapter extends RecyclerView.Adapter<MusicListAlbumRecyclerAdapter.MusicListAlbumRecyclerAdapterHolder> implements View.OnClickListener, Filterable {

    public class MusicListAlbumRecyclerAdapterHolder extends RecyclerView.ViewHolder{
        public TextView musicInfoName;

        MusicListAlbumRecyclerAdapterHolder(@NonNull View itemView) {
            super(itemView);
            musicInfoName = itemView.findViewById(R.id.play_list_music_info);
        }
    }

    private List<LocalAlbum> musicListAlbumFilter;
    private List<LocalAlbum> musicListAlbum;
    private OnItemClickListener onItemClickListener = null;
    public MusicListAlbumRecyclerAdapter(List<LocalAlbum> musicListAlbum){
        this.musicListAlbum = musicListAlbum;
        this.musicListAlbumFilter = this.musicListAlbum;    // 初始化时默认填充全部歌曲
    }

    @NonNull
    @Override
    public MusicListAlbumRecyclerAdapterHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.play_list_item, viewGroup, false);
        view.setOnClickListener(this);
        return new MusicListAlbumRecyclerAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicListAlbumRecyclerAdapterHolder musicListAlbumRecyclerAdapterHolder, int position) {
        //Override
        musicListAlbumRecyclerAdapterHolder.itemView.setTag(position);   // setTag - getTag
    }

    @Override
    public int getItemCount() {
        return musicListAlbumFilter.size();
    }

    // 过滤器
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchKey = charSequence.toString();
                if (searchKey.isEmpty()) {
                    musicListAlbumFilter = musicListAlbum;
                } else {
                    List<LocalAlbum> filteredList = new ArrayList<>();
                    for (LocalAlbum localAlbum : musicListAlbum) {
                        // 匹配规则
                        if (localAlbum.contains(searchKey)) {
                            filteredList.add(localAlbum);
                        }
                    }
                    musicListAlbumFilter = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = musicListAlbumFilter;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                musicListAlbumFilter = (List<LocalAlbum>) filterResults.values;
                //刷新数据
                notifyDataSetChanged();
            }
        };
    }
    // 获取搜索结果
    public List<LocalAlbum> getMusicListAlbumFilter(){
        return musicListAlbumFilter;
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
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

    // 点击事件接口
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

}
