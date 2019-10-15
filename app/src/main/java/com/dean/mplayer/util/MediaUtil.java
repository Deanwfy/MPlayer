package com.dean.mplayer.util;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dean.mplayer.Albm;
import com.dean.mplayer.Arts;
import com.dean.mplayer.LocalAlbm;
import com.dean.mplayer.MusicInfo;
import com.dean.mplayer.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
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
		for (int i = 0; i < musicInfoLocal.size(); i++) {
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

	// 本地专辑获取
	public static List<LocalAlbm> getAlbmLocal() {
		List<LocalAlbm> albmLocal = new ArrayList<>();
		for (int i = 0; i < musicInfoLocal.size(); i++) {
			boolean repeat = false;
            List<MusicInfo> musicInfos = new ArrayList<>();
            MusicInfo musicInfo = musicInfoLocal.get(i);
            musicInfos.add(musicInfo);
			for (int j = 0; j < albmLocal.size(); j++) {
				LocalAlbm localAlbm = albmLocal.get(j);
				if (localAlbm.getName().equals(musicInfo.getTitle())) {
					localAlbm.getMusicInfos().add(musicInfo);
					repeat = true;
					break;
				}
			}
			if (!repeat){
				albmLocal.add(new LocalAlbm(
						musicInfo.getAlbumId(),
						musicInfo.getAlbum(),
						musicInfos
				));
			}
		}
		Collections.sort(albmLocal, (o1, o2) -> o1.getName().compareTo(o2.getName()));
		return albmLocal;
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
	public static Bitmap getArtwork(Context context, String picUrl) {
		Uri uri = Uri.parse(picUrl);
        final Bitmap[] bitmap = new Bitmap[1];
        Glide.with(context).asBitmap().load(uri).placeholder(R.drawable.ic_cover).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                bitmap[0] = resource;
            }
            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {}
        });
        return bitmap[0];
    }
	//获取默认专辑图片
	@SuppressWarnings("ResourceType")
	public static Bitmap getDefaultArtwork(Context context) {
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

	// 获取歌词文件
    public static String getLrc(String filePath){
        try {
            String path = filePath.substring(0, filePath.lastIndexOf(".")) + ".lrc";
            InputStreamReader inputReader = new InputStreamReader(new FileInputStream(new File(path)));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line;
            StringBuilder result= new StringBuilder();
            while((line = bufReader.readLine()) != null){
                if(line.trim().equals(""))
                    continue;
                result.append(line).append("\r\n");
            }
            return result.toString();
        } catch (Exception e) {
            return "暂无歌词";
        }
    }

	// Uri转真实路径
	public static String getRealPathFromURI(Context context, Uri contentURI) {
		String path;
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(contentURI, null, null, null, null);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (cursor == null) {
			path = contentURI.getPath();
		} else {
			cursor.moveToFirst();
			int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			path = cursor.getString(idx);
			cursor.close();
		}
		return path;
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

	// albumId转Url
	public static String albumIdToUrl(long albumId) {
		return "content://media/external/audio/albumart/" + albumId;
	}

}
