package com.dean.mplayer;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class MusicInfo implements Parcelable {

	private long id;
	private String title;
	private String album;
	private long albumId;
	private Bitmap albumBitmap;
	private String displayName;
	private String artist;
	private long artistId;
	private long duration;
	private long size;
	private String url;
	private Uri uri;
	private String lrcTitle;
	private String lrcSize;

	public MusicInfo() {
		super();
	}

	public MusicInfo(long id, String title, String album, long albumId, Bitmap albumBitmap,
					 String displayName, String artist,long artistId, long duration, long size,
					 String url, Uri uri, String lrcTitle, String lrcSize) {
		this.id = id;
		this.title = title;
		this.album = album;
		this.albumId = albumId;
		this.albumBitmap = albumBitmap;
		this.displayName = displayName;
		this.artist = artist;
		this.artistId = artistId;
		this.duration = duration;
		this.size = size;
		this.url = url;
		this.uri = uri;
		this.lrcTitle = lrcTitle;
		this.lrcSize = lrcSize;
	}

	protected MusicInfo(Parcel in) {
		id = in.readLong();
		title = in.readString();
		album = in.readString();
		albumId = in.readLong();
		albumBitmap = in.readParcelable(Bitmap.class.getClassLoader());
		displayName = in.readString();
		artist = in.readString();
		artistId = in.readLong();
		duration = in.readLong();
		size = in.readLong();
		url = in.readString();
		uri = in.readParcelable(Uri.class.getClassLoader());
		lrcTitle = in.readString();
		lrcSize = in.readString();
	}

	@Override
	public String toString() {
		return "MusicInfo [id=" + id + ", title=" + title + ", album=" + album
				+ ", albumId=" + albumId + ", displayName=" + displayName
				+ ", artist=" + artist + ", artistId=" + artistId + ", duration=" + duration + ", size="
				+ size + ", url=" + url + ", uri=" + uri + ",lrcTitle=" + lrcTitle
				+ ", lrcSize=" + lrcSize + "]";
	}

	public boolean contains(CharSequence charSequence){
		String searchKey = charSequence.toString();
		if (title.contains(searchKey)){
			return true;
		}
		else if (artist.contains(searchKey)){
			return true;
		}
		else return album.contains(searchKey);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public long getAlbumId() {
		return albumId;
	}

	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}

	public Bitmap getAlbumBitmap() {
		return albumBitmap;
	}

	public void setAlbumBitmap(Bitmap albumBitmap) {
		this.albumBitmap = albumBitmap;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public long getArtistId() {
		return artistId;
	}

	public void setArtistId(long artistId) {
		this.artistId = artistId;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getUrl() { return url; }

	public void setUrl(String url) { this.url = url; }

	public Uri getUri() { return uri; }

	public void setUri(Uri uri) { this.uri = uri; }


	public String getLrcTitle() {
		return lrcTitle;
	}

	public void setLrcTitle(String lrcTitle) {
		this.lrcTitle = lrcTitle;
	}

	public String getLrcSize() {
		return lrcSize;
	}

	public void setLrcSize(String lrcSize) {
		this.lrcSize = lrcSize;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	// Parcelable
	public static final Creator<MusicInfo> CREATOR = new Creator<MusicInfo>() {
		@Override
		public MusicInfo createFromParcel(Parcel in) {
			return new MusicInfo(in);
		}

		@Override
		public MusicInfo[] newArray(int size) {
			return new MusicInfo[size];
		}
	};
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(title);
		dest.writeString(album);
		dest.writeLong(albumId);
		dest.writeParcelable(albumBitmap, flags);
		dest.writeString(displayName);
		dest.writeString(artist);
		dest.writeLong(artistId);
		dest.writeLong(duration);
		dest.writeLong(size);
		dest.writeString(url);
		dest.writeParcelable(uri, flags);
		dest.writeString(lrcTitle);
		dest.writeString(lrcSize);
	}
}