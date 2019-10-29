package com.dean.mplayer.view.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.dean.mplayer.data.model.local.MusicInfo;
import com.dean.mplayer.R;
import com.dean.mplayer.util.MediaUtil;

import java.util.ArrayList;
import java.util.List;

public class MusicListLocalRecyclerAdapter extends RecyclerView.Adapter<MusicListLocalRecyclerAdapter.MusicListLocalRecyclerAdapterHolder> implements View.OnClickListener, Filterable {

    class MusicListLocalRecyclerAdapterHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener{
        TextView musicTitle;
        TextView musicArtist;
        TextView musicDuration;

        MusicListLocalRecyclerAdapterHolder(@NonNull View itemView) {
            super(itemView);
            musicTitle = itemView.findViewById(R.id.music_title);
            musicArtist = itemView.findViewById(R.id.music_artist);
            musicDuration = itemView.findViewById(R.id.music_duration);
            itemView.setOnCreateContextMenuListener(this);  // 弹出菜单长按监听
        }

        // 弹出菜单
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(0, 0, 0, "下一首播放");
            menu.add(0, 1, 0, "删除");
        }
    }

    // 取消弹出菜单长按监听
    @Override
    public void onViewRecycled(@NonNull MusicListLocalRecyclerAdapterHolder holder) {
        holder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }

    private List<MusicInfo> musicListLocalFilter;
    private List<MusicInfo> musicListLocal;
    private OnItemClickListener onItemClickListener = null;
    public MusicListLocalRecyclerAdapter(List<MusicInfo> musicListLocal){
        this.musicListLocal = musicListLocal;
        this.musicListLocalFilter = this.musicListLocal;    // 初始化时默认填充全部歌曲
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
        MusicInfo musicInfo = musicListLocalFilter.get(position);
        musicListLocalRecyclerAdapterHolder.musicTitle.setText(musicInfo.getTitle());
        musicListLocalRecyclerAdapterHolder.musicArtist.setText(musicInfo.getArtist());
        musicListLocalRecyclerAdapterHolder.musicDuration.setText(MediaUtil.formatTime(musicInfo.getDuration()));
        musicListLocalRecyclerAdapterHolder.itemView.setTag(position);   // setTag - getTag
        musicListLocalRecyclerAdapterHolder.itemView.setOnLongClickListener(v -> {
            setContextMenuPosition(musicListLocalRecyclerAdapterHolder.getLayoutPosition());
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return musicListLocalFilter.size();
    }

    // 过滤器
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchKey = charSequence.toString();
                if (searchKey.isEmpty()) {
                    musicListLocalFilter = musicListLocal;
                } else {
                    List<MusicInfo> filteredList = new ArrayList<>();
                    for (MusicInfo musicInfo: musicListLocal) {
                        // 匹配规则
                        if (musicInfo.contains(searchKey)) {
                            filteredList.add(musicInfo);
                        }
                    }
                    musicListLocalFilter = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = musicListLocalFilter;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                musicListLocalFilter = (List<MusicInfo>) filterResults.values;
                //刷新数据
                notifyDataSetChanged();
            }
        };
    }
    // 获取搜索结果
    public List<MusicInfo> getMusicListLocalFilter(){
        return musicListLocalFilter;
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

    // 弹出菜单位置获取
    private int position;
    public void setContextMenuPosition(int position) { this.position = position; }
    public int getContextMenuPosition() { return position; }
}
