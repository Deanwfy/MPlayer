/**
  * Copyright 2019 bejson.com 
  */
package com.dean.mplayer;
import java.util.List;

/**
 * Auto-generated: 2019-03-22 1:9:24
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Songs {

    private long id;
    private String name;
    private List<Artists> artists;
    private Album album;
    private long duration;
    private long copyrightId;
    private int status;
    private List<String> alias;
    private int rtype;
    private int ftype;
    private int mvid;
    private int fee;
    private String rUrl;
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

    public void setCopyrightId(long copyrightId) {
         this.copyrightId = copyrightId;
     }
     public long getCopyrightId() {
         return copyrightId;
     }

    public void setStatus(int status) {
         this.status = status;
     }
     public int getStatus() {
         return status;
     }

    public void setAlias(List<String> alias) {
         this.alias = alias;
     }
     public List<String> getAlias() {
         return alias;
     }

    public void setRtype(int rtype) {
         this.rtype = rtype;
     }
     public int getRtype() {
         return rtype;
     }

    public void setFtype(int ftype) {
         this.ftype = ftype;
     }
     public int getFtype() {
         return ftype;
     }

    public void setMvid(int mvid) {
         this.mvid = mvid;
     }
     public int getMvid() {
         return mvid;
     }

    public void setFee(int fee) {
         this.fee = fee;
     }
     public int getFee() {
         return fee;
     }

    public void setRUrl(String rUrl) {
         this.rUrl = rUrl;
     }
     public String getRUrl() {
         return rUrl;
     }

}