package com.dean.mplayer;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class MusicListArtistAlbumRecyclerAdapter extends RecyclerView.Adapter<MusicListArtistAlbumRecyclerAdapter.MusicListArtistAlbumRecyclerAdapterHolder> implements View.OnClickListener, Filterable {

    class MusicListArtistAlbumRecyclerAdapterHolder extends RecyclerView.ViewHolder{
        TextView musicInfoName;

        MusicListArtistAlbumRecyclerAdapterHolder(@NonNull View itemView) {
            super(itemView);
            musicInfoName = itemView.findViewById(R.id.play_list_music_info);
        }
    }

    private List<Arts> musicListArtistAlbumFilter;
    private List<Arts> musicListArtistAlbum;
    private OnItemClickListener onItemClickListener = null;
    MusicListArtistAlbumRecyclerAdapter(List<Arts> musicListArtistAlbum){
        this.musicListArtistAlbum = musicListArtistAlbum;
        this.musicListArtistAlbumFilter = this.musicListArtistAlbum;    // 初始化时默认填充全部歌曲
    }

    @NonNull
    @Override
    public MusicListArtistAlbumRecyclerAdapterHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.play_list_item, viewGroup, false);
        view.setOnClickListener(this);
        return new MusicListArtistAlbumRecyclerAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicListArtistAlbumRecyclerAdapterHolder musicListArtistAlbumRecyclerAdapterHolder, int position) {
        //Override
        musicListArtistAlbumRecyclerAdapterHolder.itemView.setTag(position);   // setTag - getTag
    }

    @Override
    public int getItemCount() {
        return musicListArtistAlbumFilter.size();
    }

    // 过滤器
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchKey = charSequence.toString();
                if (searchKey.isEmpty()) {
                    musicListArtistAlbumFilter = musicListArtistAlbum;
                } else {
                    List<Arts> filteredList = new ArrayList<>();
                    for (Arts artist: musicListArtistAlbum) {
                        // 匹配规则
                        if (artist.contains(searchKey)) {
                            filteredList.add(artist);
                        }
                    }
                    musicListArtistAlbumFilter = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = musicListArtistAlbumFilter;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                musicListArtistAlbumFilter = (List<Arts>) filterResults.values;
                //刷新数据
                notifyDataSetChanged();
            }
        };
    }
    // 获取搜索结果
    List<Arts> getMusicListArtistAlbumFilter(){
        return musicListArtistAlbumFilter;
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

    // 点击事件接口
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

}
