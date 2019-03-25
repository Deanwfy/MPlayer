package com.dean.mplayer;
import android.net.Uri;

public class PlayList {

    private long id;
    private String title;
    private String artist;
    private String album;
    private long duration;
    private Uri uri;
    private long albumId;

    public PlayList() {
        super();
    }

    public PlayList(long id, String title, String album, String artist, long duration, Uri uri, long albumId) {
        super();
        this.id = id;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.duration = duration;
        this.uri = uri;
        this.albumId = albumId;
    }

    public void setId(long id) {
         this.id = id;
     }
     public long getId() {
         return id;
     }

    public void setTitle(String name) {
         this.title = name;
     }
     public String getTitle() {
         return title;
     }

    public void setArtist(String artist) {
         this.artist = artist;
     }
     public String getArtist() {
         return artist;
     }

    public void setAlbum(String album) {
         this.album = album;
     }
     public String getAlbum() {
         return album;
     }

    public void setDuration(long duration) {
         this.duration = duration;
     }
     public long getDuration() {
         return duration;
     }

    public void setUri(Uri uri){
        this.uri = uri;
     }
     public Uri getUri(){
        return uri;
     }

    public long getAlbumId() {
        return albumId;
    }
     public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    @Override
    public String toString() {
        return "MusicInfo [id=" + id + ", title=" + title + ", album=" + album
                + ", artist=" + artist + ", duration=" + duration + ", uri=" + uri + "]";
    }
}