package com.dean.mplayer;

import java.io.IOException;
import java.util.List;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;

public class PlayService extends MediaBrowserServiceCompat implements OnPreparedListener {
	private MediaPlayer mediaPlayer; // 媒体播放器对象
	private String action;	//媒体控制动作
	private int listPosition = 0;        // 当前正在播放的音乐
	private List<MusicInfo> musicInfos;   //存放MusicInfo对象的集合
	public static String mode = AppConstant.PlayMode.MODE_ORDER;         //播放状态，默认为顺序播放
	private int currentTime;        //当前播放进度

	String channelId = "MPlayer_channel_1";	//通知渠道Id
	NotificationManager notificationManager;	//通知管理器

	private MediaSessionCompat mediaSessionCompat;	//mediaSession
	private PlaybackStateCompat playbackStateCompat;	//播放状态
	private MediaControllerCompat mediaControllerCompat;	//播放控制

	//extends MediaBrowserServiceCompat
	@Nullable
	@Override
	public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
		return new BrowserRoot(AppConstant.MediaIdInfo.MEDIA_ID_ROOT, null);
	}
	@Override
	public void onLoadChildren(@NonNull String parentMediaId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
		result.sendResult(null);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mediaPlayer = new MediaPlayer();
		musicInfos = MediaUtil.getMusicInfos(this);
		setUpMediaSessionCompat();

		//设置音乐准备完成时的监听器
		mediaPlayer.setOnPreparedListener(this);
		//设置音乐播放完成时的监听器
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				switch (mode){
					case AppConstant.PlayMode.MODE_ORDER:	//顺序播放
						if (listPosition < musicInfos.size() - 1) {
							mediaControllerCompat.getTransportControls().skipToNext();
						}else {
							mediaControllerCompat.getTransportControls().stop();
						}
						break;
					case AppConstant.PlayMode.MODE_LOOP:	//列表循环
						mediaControllerCompat.getTransportControls().skipToNext();
						break;
					case AppConstant.PlayMode.MODE_SINGLE:	//单曲循环
						mediaControllerCompat.getTransportControls().playFromUri(musicInfos.get(listPosition).getUri(), null);
						break;
					case AppConstant.PlayMode.MODE_RANDOM:	//随机播放
						listPosition = getRandomIndex(musicInfos.size() - 1);
						mediaControllerCompat.getTransportControls().playFromUri(musicInfos.get(listPosition).getUri(), null);
						break;
				}
			}
		});

	}

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
		action = intent.getAction();
		//播放信息
		if (action != null) {
			switch (action) {
				case AppConstant.PlayAction.ACTION_PLAY:	//直接播放
					mediaControllerCompat.getTransportControls().playFromUri(musicInfos.get(listPosition).getUri(), null);
					break;
				case AppConstant.PlayAction.ACTION_PAUSE:	//暂停
					mediaControllerCompat.getTransportControls().pause();
					break;
				case AppConstant.PlayAction.ACTION_STOP:	//停止
					mediaControllerCompat.getTransportControls().stop();
					break;
				case AppConstant.PlayAction.ACTION_CONTINUE:	//继续
					mediaControllerCompat.getTransportControls().play();
					break;
				case AppConstant.PlayAction.ACTION_PREVIOUS:	//上一曲
					mediaControllerCompat.getTransportControls().skipToPrevious();
					break;
				case AppConstant.PlayAction.ACTION_NEXT:	//下一曲
					mediaControllerCompat.getTransportControls().skipToNext();
					break;
			}
		}
        return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		if (mediaPlayer != null) {
			notificationManager.cancelAll();
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	//获取随机位置
	protected int getRandomIndex(int end) {
		return (int)(Math.random() * end);
	}

	//通知按键点击事件
	private NotificationCompat.Action createAction(int iconResId, String title, String action) {
		Intent intent = new Intent(this, PlayService.class);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, 0);
		return new NotificationCompat.Action(iconResId, title, pendingIntent);
	}

	//发送／更新通知
	private void sendNotification(){
		//获取歌曲信息
		MusicInfo mp3Info = musicInfos.get(listPosition);
		String musicTitle = mp3Info.getTitle();
		String musicArtist = mp3Info.getArtist();
		Bitmap musicCover = MediaUtil.getArtwork(this, mp3Info.getId(),mp3Info.getAlbumId(), true);
		//通知内容
		NotificationCompat.Action playPauseAction = playbackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING ?
				createAction(R.drawable.ic_notification_play, "Pause", AppConstant.PlayAction.ACTION_PAUSE) :
				createAction(R.drawable.ic_notification_pause, "Play", AppConstant.PlayAction.ACTION_CONTINUE) ;
		NotificationCompat.Builder mNotificationCompat = new NotificationCompat.Builder(this, channelId)
				.setContentTitle(musicTitle)
				.setContentText(musicArtist)
				.setSmallIcon(R.drawable.ic_notification)
				.setLargeIcon(musicCover)
				.setShowWhen(false)
				.addAction(createAction(R.drawable.ic_notification_prev, "Prev", AppConstant.PlayAction.ACTION_PREVIOUS))
				.addAction(playPauseAction)
				.addAction(createAction(R.drawable.ic_notification_next, "next", AppConstant.PlayAction.ACTION_NEXT));
		//版本兼容
		if (MediaUtil.isLollipop()) {
			mNotificationCompat.setVisibility(Notification.VISIBILITY_PUBLIC);	//锁屏显示
			android.support.v4.media.app.NotificationCompat.MediaStyle mediaStyle = new android.support.v4.media.app.NotificationCompat.MediaStyle()    //通知类型为"多媒体"
					.setMediaSession(mediaSessionCompat.getSessionToken())
					.setShowActionsInCompactView(0, 1, 2);    //通知栏折叠状态下保持按键显示
			mNotificationCompat.setStyle(mediaStyle);
			if (musicCover != null) {
				mNotificationCompat.setColor(Palette.from(musicCover).generate().getVibrantColor(Color.parseColor("#005b52")));
			}else {
				mNotificationCompat.setColor(0x005b52);
			}
		}
		if (MediaUtil.isOreo()){
			mNotificationCompat.setOngoing(true);	//通知常驻
			mNotificationCompat.setColorized(true);	//通知变色
		}
		//通知点击事件
		Intent resultIntent = new Intent(this, PlayNow.class);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);
		mNotificationCompat.setContentIntent(resultPendingIntent);
		//发布通知
		if (MediaUtil.isOreo()) {
			//建立通知渠道
			CharSequence channelName = "MPlayer";
			int importance = NotificationManager.IMPORTANCE_LOW;
			NotificationChannel mNotificationChannel = new NotificationChannel(channelId, channelName, importance);
			//创建通知
			notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			//将通知绑定至通知渠道
			notificationManager.createNotificationChannel(mNotificationChannel);
			//推送
			notificationManager.notify(1, mNotificationCompat.build());
		}
	}

	private void setUpMediaSessionCompat() {
		//播放状态初始化
		playbackStateCompat = new PlaybackStateCompat.Builder()
				.setState(PlaybackStateCompat.STATE_NONE, 0, 0.0f)
				.build();
		//MediaSession初始化
		mediaSessionCompat = new MediaSessionCompat(this, "MPlayer");
		setSessionToken(mediaSessionCompat.getSessionToken());
		try {
			mediaControllerCompat = new MediaControllerCompat(this,mediaSessionCompat.getSessionToken());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		//多媒体交互
		mediaSessionCompat.setActive(true);
		mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
				| MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
		//设置播放状态
		mediaSessionCompat.setPlaybackState(playbackStateCompat);
//		sendNotification();

		//回调播放控制
		mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
			//播放
			@Override
			public void onPlayFromUri(Uri uri, Bundle position) {
				if (position != null) {
					listPosition = position.getInt("listPosition", listPosition);
				}
				MusicInfo musicInfo = musicInfos.get(listPosition);
				MediaMetadataCompat.Builder mediaMetaDataCompat = new MediaMetadataCompat.Builder()
						.putString(MediaMetadataCompat.METADATA_KEY_TITLE, musicInfo.getTitle())
						.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, musicInfo.getArtist())
						.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, MediaUtil.getArtwork(PlayService.this, musicInfo.getId(), musicInfo.getAlbumId(), true));
				mediaSessionCompat.setMetadata(mediaMetaDataCompat.build());
					try {
						switch (playbackStateCompat.getState()) {
							case PlaybackStateCompat.STATE_PLAYING:
							case PlaybackStateCompat.STATE_PAUSED:
							case PlaybackStateCompat.STATE_NONE:
								mediaPlayer.reset();
								//设置播放地址
								mediaPlayer.setDataSource(PlayService.this, uri);
								//异步进行播放
								mediaPlayer.prepareAsync();
								break;
							case PlaybackStateCompat.STATE_BUFFERING:
							case PlaybackStateCompat.STATE_CONNECTING:
							case PlaybackStateCompat.STATE_ERROR:
							case PlaybackStateCompat.STATE_FAST_FORWARDING:
							case PlaybackStateCompat.STATE_REWINDING:
							case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
							case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
							case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM:
							case PlaybackStateCompat.STATE_STOPPED:
								break;
						}
					} catch (IOException e) {
						onStop();
				}
			}
			//停止
			@Override
			public void onStop(){
				mediaPlayer.stop();
				playbackStateCompat = new PlaybackStateCompat.Builder()
						.setState(PlaybackStateCompat.STATE_NONE, 0, 0.0f)
						.build();
				mediaSessionCompat.setPlaybackState(playbackStateCompat);
				sendNotification();
			}
			//暂停
			@Override
			public void onPause() {
				mediaPlayer.pause();
				playbackStateCompat = new PlaybackStateCompat.Builder()
						.setState(PlaybackStateCompat.STATE_PAUSED, 0, 0.0f)
						.build();
				mediaSessionCompat.setPlaybackState(playbackStateCompat);
				//更新通知栏
				sendNotification();
			}
			//继续
			@Override
			public void onPlay() {
				mediaPlayer.start();
				playbackStateCompat = new PlaybackStateCompat.Builder()
						.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
						.build();
				mediaSessionCompat.setPlaybackState(playbackStateCompat);
				//开启／更新通知栏
				sendNotification();
			}
			//上一曲
			@Override
			public void onSkipToPrevious() {
				if (listPosition > 0){
					listPosition--;
				}else {
					listPosition = musicInfos.size() - 1;
				}
				onPlayFromUri(musicInfos.get(listPosition).getUri(),null);
			}
			//下一曲
			@Override
			public void onSkipToNext() {
				if (listPosition < musicInfos.size() - 1){
					listPosition++;
				}else {
					listPosition = 0;
				}
				onPlayFromUri(musicInfos.get(listPosition).getUri(),null);
			}

		});
	}

	//当音乐准备好的时候开始播放
	@Override
	public void onPrepared(MediaPlayer mp) {
		mediaPlayer.start(); // 开始播放
		playbackStateCompat = new PlaybackStateCompat.Builder()
				.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
				.build();
		mediaSessionCompat.setPlaybackState(playbackStateCompat);
		sendNotification();
		if (currentTime > 0) { // 如果音乐不是从头播放
			mediaPlayer.seekTo(currentTime);
		}
	}

}