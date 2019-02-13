package com.dean.mplayer;

import java.util.List;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.graphics.Palette;

public class PlayService extends Service {
	private MediaPlayer mediaPlayer; // 媒体播放器对象
	private String path;            // 音乐文件路径
	private int msg;
	private boolean isPause;        // 暂停状态
	private int current = 0;        // 记录当前正在播放的音乐
	private List<MusicInfo> mp3Infos;   //存放Mp3Info对象的集合
	private int status = 3;         //播放状态，默认为顺序播放
	private int currentTime;        //当前播放进度


	String channelId = "MPlayer_channel_1";	//通知渠道Id
	private MediaSessionCompat mediaSessionCompat;

	@Override
	public void onCreate() {
		super.onCreate();
		mediaPlayer = new MediaPlayer();
		mp3Infos = MediaUtil.getMusicInfos(PlayService.this);

		if (MediaUtil.isLollipop())
			setUpMediaSessionCompat();

		// 设置音乐播放完成时的监听器
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if (status == 1) {		// 单曲循环
					play(0);
				} else if (status == 2) { // 全部循环
					current++;
					if(current > mp3Infos.size() - 1) {		// 变为第一首的位置继续播放
						current = 0;
					}
					path = mp3Infos.get(current).getUrl();
					Intent musicUpdate = new Intent("musicUpdate");	//播放完毕，将服务自动进行的切歌操作回传到前台
					musicUpdate.putExtra("current", current);
					sendBroadcast(musicUpdate);
					play(0);
				} else if (status == 3) {		// 顺序播放
					current++;		// 下一首位置
					if (current <= mp3Infos.size() - 1) {
						path = mp3Infos.get(current).getUrl();
						Intent musicUpdate = new Intent("musicUpdate");	//播放完毕，将服务自动进行的切歌操作回传到前台
						musicUpdate.putExtra("current", current);
						sendBroadcast(musicUpdate);
						play(0);
					}else {
						stop();
						Intent musicUpdate = new Intent("musicUpdate");	//最后一首播放完毕，将服务自动进行的停止操作回传到前台
						musicUpdate.putExtra("current", current);
						sendBroadcast(musicUpdate);
						current--;
					}
				} else if(status == 4) {    //随机播放
					current = getRandomIndex(mp3Infos.size() - 1);
					path = mp3Infos.get(current).getUrl();
					Intent musicUpdate = new Intent("musicUpdate");	//播放完毕，将服务自动进行的切歌操作回传到前台
					musicUpdate.putExtra("current", current);
					sendBroadcast(musicUpdate);
					play(0);
				}
			}
		});

	}

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
		path = intent.getStringExtra("url");        //歌曲路径
		current = intent.getIntExtra("listPosition", -1);   //当前歌曲的位置
		msg = intent.getIntExtra("MSG", 0);         //播放信息
		if (msg == AppConstant.PlayerMsg.PLAY_MSG) {    //直接播放
			play(0);
		} else if (msg == AppConstant.PlayerMsg.PAUSE_MSG) {    //暂停
			pause();
		} else if (msg == AppConstant.PlayerMsg.STOP_MSG) {     //停止
			stop();
		} else if (msg == AppConstant.PlayerMsg.CONTINUE_MSG) { //继续播放
			resume();
		} else if (msg == AppConstant.PlayerMsg.PRIVIOUS_MSG) { //上一首
			previous();
		} else if (msg == AppConstant.PlayerMsg.NEXT_MSG) {     //下一首
			next();
		} else if (msg == AppConstant.PlayerMsg.PROGRESS_CHANGE) {  //进度更新
			currentTime = intent.getIntExtra("progress", -1);
			play(currentTime);
		}
        return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	//获取随机位置
	protected int getRandomIndex(int end) {
		return (int)(Math.random() * end);
	}

	 //播放
	private void play(int currentTime) {
		try {
			mediaSessionCompat.setActive(true);
			mediaPlayer.reset();// 把各项参数恢复到初始状态
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare(); // 进行缓冲
			mediaPlayer.setOnPreparedListener(new PreparedListener(currentTime));// 注册一个监听器
			//启动音乐播放后开启通知栏
			sendNotification();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	 //暂停
	private void pause() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			isPause = true;
		}
	}

	private void resume() {
		if (isPause) {
			mediaPlayer.start();
			isPause = false;
		}
	}

	//上一曲
	private void previous() {
		play(0);
	}


	//下一曲
	private void next() {
		play(0);
	}


	//停止
	private void stop() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
	}

	 //当音乐准备好的时候开始播放
	private final class PreparedListener implements OnPreparedListener {
		private int currentTime;
		private PreparedListener(int currentTime) {
			this.currentTime = currentTime;
		}

		@Override
		public void onPrepared(MediaPlayer mp) {
			mediaPlayer.start(); // 开始播放
			if (currentTime > 0) { // 如果音乐不是从头播放
				mediaPlayer.seekTo(currentTime);
			}
		}
	}

	private void sendNotification(){
		//通知点击事件
		Intent testIntent = new Intent(this, PlayDetail.class);
		PendingIntent testPendingIntent = PendingIntent.getActivity(this, 0, testIntent, 0);
		//获取歌曲信息
		MusicInfo mp3Info = mp3Infos.get(current);
		String musicTitle = mp3Info.getTitle();
		String musicArtist = mp3Info.getArtist();
		Bitmap musicCover = MediaUtil.getArtwork(this, mp3Info.getId(),mp3Info.getAlbumId(), true);
		//通知内容
		NotificationCompat.Builder mNotificationCompat = new NotificationCompat.Builder(this, channelId)
				.setContentTitle(musicTitle)
				.setContentText(musicArtist)
				.setSmallIcon(R.drawable.music_cover)
				.setLargeIcon(musicCover)
				.setShowWhen(false)
				.addAction(R.drawable.ic_notification_prev, "Prev", testPendingIntent)
				.addAction(R.drawable.ic_notification_play, "play", testPendingIntent)
				.addAction(R.drawable.ic_notification_next, "next", testPendingIntent);
		//版本兼容
		if (MediaUtil.isLollipop()) {
			mNotificationCompat.setVisibility(Notification.VISIBILITY_PUBLIC);	//锁屏显示
			android.support.v4.media.app.NotificationCompat.MediaStyle mediaStyle = new android.support.v4.media.app.NotificationCompat.MediaStyle()    //通知类型为"多媒体"
					.setMediaSession(mediaSessionCompat.getSessionToken())
					.setShowActionsInCompactView(0, 1, 2);    //通知栏折叠状态下保持按键显示
			mNotificationCompat.setStyle(mediaStyle);
			mNotificationCompat.setColor(Palette.from(musicCover).generate().getVibrantColor(Color.parseColor("#005b52")));
		}
		if (MediaUtil.isOreo()){
			mNotificationCompat.setOngoing(true);	//通知常驻
			mNotificationCompat.setColorized(true);	//通知变色
		}
		//通知点击事件
		Intent resultIntent = new Intent(this, PlayDetail.class);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);
		mNotificationCompat.setContentIntent(resultPendingIntent);
		//发布通知
		if (MediaUtil.isOreo()) {
			//建立通知渠道
			CharSequence channelName = "MPlayer";
			int importance = NotificationManager.IMPORTANCE_LOW;
			NotificationChannel mNotificationChannel = new NotificationChannel(channelId, channelName, importance);
			//创建通知
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			//将通知绑定至通知渠道
			mNotificationManager.createNotificationChannel(mNotificationChannel);
			//推送
			mNotificationManager.notify(1, mNotificationCompat.build());
		}
	}

	private void setUpMediaSessionCompat() {
		mediaSessionCompat = new MediaSessionCompat(this, "MPlayer");
		mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
			@Override
			public void onPause() { pause(); }

			@Override
			public void onPlay() {
				play(0);
			}

			@Override
			public void onSkipToNext() {
				current++;
				next();
			}

			@Override
			public void onSkipToPrevious() {
				current--;
				previous();
			}

		});
		mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
				| MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
	}

}