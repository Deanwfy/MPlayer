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

public class MusicListAlbumRecyclerAdapter extends RecyclerView.Adapter<MusicListAlbumRecyclerAdapter.MusicListAlbumRecyclerAdapterHolder> implements View.OnClickListener, Filterable {

    class MusicListAlbumRecyclerAdapterHolder extends RecyclerView.ViewHolder{
        TextView musicInfoName;

        MusicListAlbumRecyclerAdapterHolder(@NonNull View itemView) {
            super(itemView);
            musicInfoName = itemView.findViewById(R.id.play_list_music_info);
        }
    }

    private List<LocalAlbm> musicListAlbumFilter;
    private List<LocalAlbm> musicListAlbum;
    private OnItemClickListener onItemClickListener = null;
    MusicListAlbumRecyclerAdapter(List<LocalAlbm> musicListAlbum){
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
                    List<LocalAlbm> filteredList = new ArrayList<>();
                    for (LocalAlbm localAlbm: musicListAlbum) {
                        // 匹配规则
                        if (localAlbm.contains(searchKey)) {
                            filteredList.add(localAlbm);
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
                musicListAlbumFilter = (List<LocalAlbm>) filterResults.values;
                //刷新数据
                notifyDataSetChanged();
            }
        };
    }
    // 获取搜索结果
    List<LocalAlbm> getMusicListAlbumFilter(){
        return musicListAlbumFilter;
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
