package com.dean.mplayer.data.model.local.localArtist;

import com.dean.mplayer.data.model.local.MusicInfo;

import java.util.List;

public class LocalArtist {

    private long id;
    private String name;
    private List<MusicInfo> musicInfos;
    private List<LocalArtistAlbum> albums;

    public LocalArtist(long id, String name, List<MusicInfo> musicInfos, List<LocalArtistAlbum> albums) {
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

    public List<LocalArtistAlbum> getAlbums() {
        return albums;
    }
    public void setAlbums(List<LocalArtistAlbum> albums) {
        this.albums = albums;
    }
}