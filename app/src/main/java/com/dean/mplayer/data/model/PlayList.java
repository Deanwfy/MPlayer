package com.dean.mplayer.data.model;

import android.net.Uri;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.dean.mplayer.util.MediaUtil;

import java.io.Serializable;

@Entity(tableName = "play_list")
public class PlayList implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public long uid;
    public long id;
    public String title;
    public String artist;
    public String album;
    public long duration;
    public String uri;
    public String source;
    public String picUrl;

    public PlayList() {}

    public PlayList(long id, String title, String album, String artist, long duration, Uri uri, String source, String picUrl) {
        super();
        this.id = id;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.duration = duration;
        this.uri = uri.toString();
        this.source = source;
        this.picUrl = picUrl;
    }

    public void setUri(Uri uri) {
        this.uri = uri.toString();
    }

    public Uri getUri() {
        return Uri.parse(uri);
    }

    @Override
    public String toString() {
        return "MusicInfo [id=" + id + ", title=" + title + ", album=" + album + ", artist=" + artist
                + ", duration=" + duration + ", uri=" + uri + ", picUrl=" + picUrl + "]";
    }
}