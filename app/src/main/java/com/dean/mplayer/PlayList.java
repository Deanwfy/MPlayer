package com.dean.mplayer;
import android.graphics.Bitmap;
import android.net.Uri;

public class PlayList {

    private long id;
    private String title;
    private String artist;
    private String album;
    private long duration;
    private Uri uri;
    private Bitmap albumBitmap;

    public PlayList() {
        super();
    }

    public PlayList(long id, String title, String album, String artist, long duration, Uri uri, Bitmap albumBitmap) {
        super();
        this.id = id;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.duration = duration;
        this.uri = uri;
        this.albumBitmap = albumBitmap;
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

    public Bitmap getAlbumBitmap() {
        return albumBitmap;
    }
     public void setAlbumBitmap(Bitmap albumBitmap) {
        this.albumBitmap = albumBitmap;
    }

    @Override
    public String toString() {
        return "MusicInfo [id=" + id + ", title=" + title + ", album=" + album
                + ", artist=" + artist + ", duration=" + duration + ", uri=" + uri + "]";
    }
}