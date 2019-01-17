package com.dean.mplayer;

import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;

public class PlayService extends Service {
	private MediaPlayer mediaPlayer; // 媒体播放器对象
	private String path;            // 音乐文件路径
	private int msg;
	private boolean isPause;        // 暂停状态
	private int current = 0;        // 记录当前正在播放的音乐
	private List<MusicInfo> mp3Infos;   //存放Mp3Info对象的集合
	private int status = 3;         //播放状态，默认为顺序播放
	private int currentTime;        //当前播放进度

	@Override
	public void onCreate() {
		super.onCreate();
		mediaPlayer = new MediaPlayer();
		mp3Infos = MediaUtil.getMusicInfos(PlayService.this);

		// 设置音乐播放完成时的监听器
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if (status == 1) {		// 单曲循环
					mediaPlayer.start();
				} else if (status == 2) { // 全部循环
					current++;
					if(current > mp3Infos.size() - 1) {		// 变为第一首的位置继续播放
						current = 0;
					}
					path = mp3Infos.get(current).getUrl();
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
						mediaPlayer.seekTo(0);
						Intent musicUpdate = new Intent("musicUpdate");	//播放完毕，将服务自动进行的切歌操作回传到前台
						musicUpdate.putExtra("current", current);
						sendBroadcast(musicUpdate);
						current--;
					}
				} else if(status == 4) {    //随机播放
					current = getRandomIndex(mp3Infos.size() - 1);
					System.out.println("currentIndex ->" + current);
					path = mp3Infos.get(current).getUrl();
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
		int index = (int) (Math.random() * end);
		return index;
	}

	 //播放
	private void play(int currentTime) {
		try {
			mediaPlayer.reset();// 把各项参数恢复到初始状态
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare(); // 进行缓冲
			mediaPlayer.setOnPreparedListener(new PreparedListener(currentTime));// 注册一个监听器
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//TODO
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
			try {
				mediaPlayer.prepare(); // stop后再次start前需要先prepare
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	 //当音乐准备好的时候开始播放
	private final class PreparedListener implements OnPreparedListener {
		private int currentTime;
		public PreparedListener(int currentTime) {
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

}