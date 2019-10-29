package com.dean.mplayer.data.model.online.onlineRankList;
import java.util.List;

public class Tracks {

    private String name;
    private long id;
    private List<Ar> ar;
    private Al al;
    private long dt;

    public void setName(String name) {
         this.name = name;
     }
     public String getName() {
         return name;
     }

    public void setId(long id) {
         this.id = id;
     }
     public long getId() {
         return id;
     }

    public void setAr(List<Ar> ar) {
         this.ar = ar;
     }
     public List<Ar> getAr() {
         return ar;
     }

    public void setAl(Al al) {
         this.al = al;
     }
     public Al getAl() {
         return al;
     }

    public void setDt(long dt) {
         this.dt = dt;
     }
     public long getDt() {
         return dt;
     }



}