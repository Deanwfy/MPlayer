package com.dean.mplayer.data.model.local.localAlbum;

import com.dean.mplayer.data.model.local.MusicInfo;

import java.util.List;

public class LocalAlbum {

    private long id;
    private String name;
    private long publishTime;
    private int size;
    private List<MusicInfo> musicInfos;

    public LocalAlbum(long id, String name, List<MusicInfo> musicInfos) {
        super();
        this.id = id;
        this.name = name;
        this.musicInfos = musicInfos;
    }

    public boolean contains(CharSequence charSequence){
        String searchKey = charSequence.toString();
        return name.contains(searchKey);
    }

    public void setId(long id) {
        this.id = id;
    }
    public long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }
    public long getPublishTime() {
        return publishTime;
    }

    public void setSize(int size) {
        this.size = size;
    }
    public int getSize() {
        return size;
    }

    public List<MusicInfo> getMusicInfos() {
        return musicInfos;
    }
    public void setMusicInfos(List<MusicInfo> musicInfos) {
        this.musicInfos = musicInfos;
    }

}