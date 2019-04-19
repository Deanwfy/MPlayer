package com.dean.mplayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;

import com.squareup.picasso.Picasso;

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
			return musicInfos;
		}else {
			return null;
		}
	}

	public static List<HashMap<String, String>> getMusicMaps(
			List<MusicInfo> musicInfos) {
		List<HashMap<String, String>> musiclist = new ArrayList<>();
		for (MusicInfo musicInfo : musicInfos) {
			HashMap<String, String> map = new HashMap<>();
			map.put("title", musicInfo.getTitle());
			map.put("artist", musicInfo.getArtist());
			map.put("album", musicInfo.getAlbum());
			map.put("displayName", musicInfo.getDisplayName());
			map.put("albumId", String.valueOf(musicInfo.getAlbumId()));
			map.put("duration", formatTime(musicInfo.getDuration()));
			map.put("size", String.valueOf(musicInfo.getSize()));
			map.put("url", musicInfo.getUrl());
			map.put("uri", String.valueOf(musicInfo.getUri()));
			musiclist.add(map);
		}
		return musiclist;
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
