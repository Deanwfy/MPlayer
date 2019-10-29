package com.dean.mplayer.data.model.online.onlineSearch;
import java.util.List;

public class Songs {

    private long id;
    private String name;
    private List<Artists> artists;
    private Album album;
    private long duration;

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

    public void setArtists(List<Artists> artists) {
         this.artists = artists;
     }
     public List<Artists> getArtists() {
         return artists;
     }

    public void setAlbum(Album album) {
         this.album = album;
     }
     public Album getAlbum() {
         return album;
     }

    public void setDuration(long duration) {
         this.duration = duration;
     }
     public long getDuration() {
         return duration;
     }

}