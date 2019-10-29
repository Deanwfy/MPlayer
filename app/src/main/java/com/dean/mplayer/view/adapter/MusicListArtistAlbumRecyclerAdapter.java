package com.dean.mplayer.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dean.mplayer.data.model.local.localArtist.LocalArtist;
import com.dean.mplayer.R;

import java.util.ArrayList;
import java.util.List;

public class MusicListArtistAlbumRecyclerAdapter extends RecyclerView.Adapter<MusicListArtistAlbumRecyclerAdapter.MusicListArtistAlbumRecyclerAdapterHolder> implements View.OnClickListener, Filterable {

    public class MusicListArtistAlbumRecyclerAdapterHolder extends RecyclerView.ViewHolder{
        public TextView musicInfoName;

        MusicListArtistAlbumRecyclerAdapterHolder(@NonNull View itemView) {
            super(itemView);
            musicInfoName = itemView.findViewById(R.id.play_list_music_info);
        }
    }

    private List<LocalArtist> musicListArtistAlbumFilter;
    private List<LocalArtist> musicListArtistAlbum;
    private OnItemClickListener onItemClickListener = null;
    public MusicListArtistAlbumRecyclerAdapter(List<LocalArtist> musicListArtistAlbum){
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
                    List<LocalArtist> filteredList = new ArrayList<>();
                    for (LocalArtist artist: musicListArtistAlbum) {
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
                musicListArtistAlbumFilter = (List<LocalArtist>) filterResults.values;
                //刷新数据
                notifyDataSetChanged();
            }
        };
    }
    // 获取搜索结果
    public List<LocalArtist> getMusicListArtistAlbumFilter(){
        return musicListArtistAlbumFilter;
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
