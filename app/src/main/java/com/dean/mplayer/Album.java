/**
  * Copyright 2019 bejson.com 
  */
package com.dean.mplayer;

/**
 * Auto-generated: 2019-03-22 1:9:24
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Album {

    private long id;
    private String name;
    private Artist artist;
    private long publishTime;
    private int size;
    private long copyrightId;
    private int status;
    private long picId;
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

    public void setArtist(Artist artist) {
         this.artist = artist;
     }
     public Artist getArtist() {
         return artist;
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

    public void setPicId(long picId) {
         this.picId = picId;
     }
     public long getPicId() {
         return picId;
     }

}