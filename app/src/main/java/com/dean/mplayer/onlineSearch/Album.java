package com.dean.mplayer.onlineSearch;

public class Album {

    private long id;
    private String name;
    private long publishTime;
    private int size;

    public Album(long id, String name) {
        super();
        this.id = id;
        this.name = name;
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

}