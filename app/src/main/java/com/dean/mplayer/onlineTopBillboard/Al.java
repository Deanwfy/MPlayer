package com.dean.mplayer.onlineTopBillboard;
import java.util.List;

public class Al {

    private long id;
    private String name;
    private String picUrl;
    private List<String> tns;
    private String pic_str;
    private long pic;
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

    public void setPicUrl(String picUrl) {
         this.picUrl = picUrl;
     }
     public String getPicUrl() {
         return picUrl;
     }

    public void setTns(List<String> tns) {
         this.tns = tns;
     }
     public List<String> getTns() {
         return tns;
     }

    public void setPic_str(String pic_str) {
         this.pic_str = pic_str;
     }
     public String getPic_str() {
         return pic_str;
     }

    public void setPic(long pic) {
         this.pic = pic;
     }
     public long getPic() {
         return pic;
     }

}