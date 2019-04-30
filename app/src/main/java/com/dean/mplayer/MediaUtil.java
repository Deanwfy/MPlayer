package com.dean.mplayer;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;

import com.dean.mplayer.onlineSearch.Album;
import com.dean.mplayer.onlineSearch.Artists;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MediaUtil {

	//版本判断
	public static boolean isOreo() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
	}
	public static boolean isMarshmallow() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
	}
	public static boolean isLollipop() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	}

	//本地歌曲信息获取
	private static List<MusicInfo> musicInfoLocal;
	public static List<MusicInfo> getMusicLocal(Context context) {
		Cursor cursor = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		List<MusicInfo> musicInfos = new ArrayList<>();
		if(cursor != null) {
			while (cursor.moveToNext()) {
				MusicInfo musicInfo = new MusicInfo();
				long id = cursor.getLong(cursor
						.getColumnIndex(MediaStore.Audio.Media._ID));
				String title = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.TITLE));
				String artist = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.ARTIST));
				long artistId = cursor.getLong(cursor
						.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
				String album = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.ALBUM));
				String displayName = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
				long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
				Bitmap albumBitmap = getArtwork(context, albumId);
				long duration = cursor.getLong(cursor
						.getColumnIndex(MediaStore.Audio.Media.DURATION));
				long size = cursor.getLong(cursor
						.getColumnIndex(MediaStore.Audio.Media.SIZE));
				String url = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.DATA));
				Uri uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
				int isMusic = cursor.getInt(cursor
						.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
				if (isMusic != 0 && duration > 15000) {    //过滤15s内的音频文件
					musicInfo.setId(id);
					musicInfo.setTitle(title);
					musicInfo.setArtist(artist);
					musicInfo.setArtistId(artistId);
					musicInfo.setAlbum(album);
					musicInfo.setDisplayName(displayName);
					musicInfo.setAlbumId(albumId);
					musicInfo.setAlbumBitmap(albumBitmap);
					musicInfo.setDuration(duration);
					musicInfo.setSize(size);
					musicInfo.setUrl(url);
					musicInfo.setUri(uri);
					musicInfos.add(musicInfo);
				}
			}
			cursor.close();
			musicInfoLocal = musicInfos;
			return musicInfos;
		}else {
			return null;
		}
	}

	// 本地音乐人获取
	public static List<Arts> getArtistsLocal() {
		List<Arts> artistsLocal = new ArrayList<>();
		for (int i = 0; i < musicInfoLocal.size() - 1; i++) {
			boolean repeat = false;
			List<MusicInfo> musicInfos = new ArrayList<>();
			MusicInfo musicInfo = musicInfoLocal.get(i);
			musicInfos.add(musicInfo);
			List<Albm> artistAlbms = new ArrayList<>();
			Albm artistAlbm = new Albm(musicInfo.getAlbumId(), musicInfo.getAlbum());
			artistAlbms.add(artistAlbm);
			for (int j = 0; j < artistsLocal.size(); j++) {
				Arts arts = artistsLocal.get(j);
				if (arts.getId() == musicInfo.getArtistId()) {
					arts.getMusicInfos().add(musicInfo);
					arts.getAlbums().add(artistAlbm);
					repeat = true;
					break;
				}
			}
			if (!repeat){
				artistsLocal.add(new Arts(
						musicInfo.getArtistId(),
						musicInfo.getArtist(),
						musicInfos,
						artistAlbms
				));
			}
		}
		Collections.sort(artistsLocal, (o1, o2) -> o1.getName().compareTo(o2.getName()));
		return artistsLocal;
	}

	//时间显示格式
	public static String formatTime(long time) {
		String min = time / (1000 * 60) + "";
		String sec = time % (1000 * 60) + "";
		if (min.length() < 2) {
			min = "0" + time / (1000 * 60) + "";
		} else {
			min = time / (1000 * 60) + "";
		}
		if (sec.length() == 4) {
			sec = "0" + (time % (1000 * 60)) + "";
		} else if (sec.length() == 3) {
			sec = "00" + (time % (1000 * 60)) + "";
		} else if (sec.length() == 2) {
			sec = "000" + (time % (1000 * 60)) + "";
		} else if (sec.length() == 1) {
			sec = "0000" + (time % (1000 * 60)) + "";
		}
		return min + ":" + sec.trim().substring(0, 2);
	}

	//获取专辑封面位图对象
	public static Bitmap getArtwork(Context context, long album_id) {
		Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), album_id);
		try {
			return Picasso.get().load(uri).error(R.drawable.ic_cover).get();
		} catch (IOException e) {
			return getDefaultArtwork(context);
		}
	}
	//获取默认专辑图片
	@SuppressWarnings("ResourceType")
	private static Bitmap getDefaultArtwork(Context context) {
		Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_cover);
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		} else if (drawable instanceof VectorDrawable || drawable instanceof VectorDrawableCompat) {
			Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			drawable.draw(canvas);
			return bitmap;
		} else {
			throw new IllegalArgumentException("unsupported drawable type");
		}
	}

	// Uri转真实路径
	public static String getRealPathFromURI(Context context, Uri contentURI) {
		String result;
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(contentURI, null, null, null, null);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (cursor == null) {
			result = contentURI.getPath();
		} else {
			cursor.moveToFirst();
			int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			result = cursor.getString(idx);
			cursor.close();
		}
		return result;
	}

	// 删除
	public static boolean deleteMusicFile(Context context,Uri uri){
		String filePath = getRealPathFromURI(context, uri);
		File file = new File(filePath);
		if (file.exists()){
			if (file.isFile()){
				if (file.delete()){
					scanFileAsync(context, filePath);
					return true;
				}else return false;
			}else return false;
		}else return false;
	}

	// 更新媒体库
	public static void scanFileAsync(Context context, String filePath) {
		Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		scanIntent.setData(Uri.fromFile(new File(filePath)));
		context.sendBroadcast(scanIntent);
	}

}
