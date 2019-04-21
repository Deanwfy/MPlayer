package com.dean.mplayer.onlineSearch;

import com.dean.mplayer.MusicInfo;
import java.util.List;

public class Artists {

    private long id;
    private String name;
    private List<MusicInfo> musicInfos;
    private List<Album> albums;

    public Artists(long id, String name, List<MusicInfo> musicInfos, List<Album> albums) {
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

    public List<Album> getAlbums() {
        return albums;
    }
    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }
}