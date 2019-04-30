package com.dean.mplayer;

import java.util.List;

public class Arts{

    private long id;
    private String name;
    private List<MusicInfo> musicInfos;
    private List<Albm> albums;

    public Arts(long id, String name, List<MusicInfo> musicInfos, List<Albm> albums) {
        super();
        this.id = id;
        this.name = name;
        this.musicInfos = musicInfos;
        this.albums = albums;
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

    public List<MusicInfo> getMusicInfos() {
        return musicInfos;
    }
    public void setMusicInfos(List<MusicInfo> musicInfos) {
        this.musicInfos = musicInfos;
    }

    public List<Albm> getAlbums() {
        return albums;
    }
    public void setAlbums(List<Albm> albums) {
        this.albums = albums;
    }
}