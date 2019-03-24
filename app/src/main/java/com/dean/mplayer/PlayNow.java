package com.dean.mplayer;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PlayNow extends AppCompatActivity {

    // 播放控制显示
    private TextView playNowTitle;
    private TextView playNowArtist;
    private TextView seekBarStart;
    private TextView seekBarEnd;
    private ImageView playNowCover;
    private SeekBar playNowCurrent;
    private ConstraintLayout playNowLayout;

    // 播放控制按钮
    private ImageButton playNowMode;
    private ImageButton playNowPrev;
    private ImageButton playNowPlay;
    private ImageButton playNowNext;

    //获取服务
    private MediaControllerCompat mediaController;
    private MediaBrowserCompat mediaBrowserCompat;

    //歌曲信息
    private List<MusicInfo> musicInfos = null;
    Bitmap cover = null;

    //SeekBar更新
    private Timer timer = new Timer();
    private Handler handler;
    private MediaMetadataCompat metadata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_now);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        findControlBtnById();
        setPlayNowMode();
        setControlBtnOnClickListener();
        musicInfos = MediaUtil.getMusicInfos(getApplicationContext());
        initMediaBrowser();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaBrowserCompat.disconnect();
    }

    // 统一获取播放控制面板控件id
    private void findControlBtnById() {
        playNowTitle = findViewById(R.id.playNowTitle);
        playNowArtist = findViewById(R.id.playNowArtist);
        seekBarStart = findViewById(R.id.seekBarStart);
        seekBarEnd = findViewById(R.id.seekBarEnd);
        playNowCover = findViewById(R.id.playNowCover);
        playNowCurrent = findViewById(R.id.playNowCurrent);
        playNowLayout = findViewById(R.id.play_now);

        playNowMode = findViewById(R.id.playNowMode);
        playNowPrev = findViewById(R.id.playNowPrev);
        playNowPlay = findViewById(R.id.playNowPlay);
        playNowNext = findViewById(R.id.playNowNext);
    }

    // 将监听器设置到播放控制面板控件
    private void setControlBtnOnClickListener() {
        ControlBtnOnClickListener controlBtnOnClickListener = new ControlBtnOnClickListener();
        ControlSeekBarOnChangeListener controlSeekBarOnChangeListener = new ControlSeekBarOnChangeListener();
        playNowMode.setOnClickListener(controlBtnOnClickListener);
        playNowPrev.setOnClickListener(controlBtnOnClickListener);
        playNowPlay.setOnClickListener(controlBtnOnClickListener);
        playNowNext.setOnClickListener(controlBtnOnClickListener);
        playNowCurrent.setOnSeekBarChangeListener(controlSeekBarOnChangeListener);
    }

    // 命名播放控制面板按键监听器类，实现点击事件监听
    private class ControlBtnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.playNowMode:
                    switch (PlayService.mode) {
                        case AppConstant.PlayMode.MODE_ORDER:
                            playNowMode.setImageResource(R.drawable.ic_now_loop);
                            if(cover != null) {
                                playNowMode.getDrawable().setTint(Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52")));
                            }
                            PlayService.mode = AppConstant.PlayMode.MODE_LOOP;
//                            Toast.makeText(getApplicationContext(),"列表循环", Toast.LENGTH_SHORT).show();
                            break;
                        case AppConstant.PlayMode.MODE_LOOP:
                            playNowMode.setImageResource(R.drawable.ic_now_single);
                            if(cover != null) {
                                playNowMode.getDrawable().setTint(Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52")));
                            }
                            PlayService.mode = AppConstant.PlayMode.MODE_SINGLE;
//                            Toast.makeText(getApplicationContext(),"单曲循环", Toast.LENGTH_SHORT).show();
                            break;
                        case AppConstant.PlayMode.MODE_SINGLE:
                            playNowMode.setImageResource(R.drawable.ic_now_random);
                            if(cover != null) {
                                playNowMode.getDrawable().setTint(Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52")));
                            }
                            PlayService.mode = AppConstant.PlayMode.MODE_RANDOM;
//                            Toast.makeText(getApplicationContext(),"随机播放", Toast.LENGTH_SHORT).show();
                            break;
                        case AppConstant.PlayMode.MODE_RANDOM:
                            playNowMode.setImageResource(R.drawable.ic_now_order);
                            if(cover != null) {
                                playNowMode.getDrawable().setTint(Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52")));
                            }
                            PlayService.mode = AppConstant.PlayMode.MODE_ORDER;
//                            Toast.makeText(getApplicationContext(),"顺序播放", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case R.id.playNowPlay:
                    if (musicInfos != null && musicInfos.size() != 0) {
                        if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                            mediaController.getTransportControls().pause();
                        } else if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED) {
                            mediaController.getTransportControls().play();
                        } else {
                            mediaController.getTransportControls().playFromUri(musicInfos.get(0).getUri(), null);
                        }
                    }
                    break;
                case R.id.playNowPrev:
                    mediaController.getTransportControls().skipToPrevious();
                    break;
                case R.id.playNowNext:
                    mediaController.getTransportControls().skipToNext();
                    break;
            }
        }
    }
    // 命名播放控制面板进度条监听器类，实现拖动事件监听
    private class ControlSeekBarOnChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            seekBarStart.setText(MediaUtil.formatTime(progress));
            // 进度变化
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // 开始拖动
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mediaController.getTransportControls().seekTo(seekBar.getProgress());
            // 拖动结束
        }
    }

    private void setPlayNowMode() {
        switch(PlayService.mode)
        {
            case AppConstant.PlayMode.MODE_ORDER:
                playNowMode.setImageResource(R.drawable.ic_now_order);
                break;
            case AppConstant.PlayMode.MODE_LOOP:
                playNowMode.setImageResource(R.drawable.ic_now_loop);
                break;
            case AppConstant.PlayMode.MODE_SINGLE:
                playNowMode.setImageResource(R.drawable.ic_now_single);
                break;
            case AppConstant.PlayMode.MODE_RANDOM:
                playNowMode.setImageResource(R.drawable.ic_now_random);
                break;
        }
    }

    private void initMediaBrowser() {
        if (mediaBrowserCompat == null) {
            // 创建MediaBrowserCompat
            mediaBrowserCompat = new MediaBrowserCompat(
                    this,
                    // 创建ComponentName 连接 MusicService
                    new ComponentName(this, PlayService.class),
                    // 创建callback
                    mediaBrowserConnectionCallback,
                    //
                    null);
            // 链接service
            mediaBrowserCompat.connect();
        }
    }

    private final MediaBrowserCompat.ConnectionCallback mediaBrowserConnectionCallback = new MediaBrowserCompat.ConnectionCallback(){
        // 连接成功
        @SuppressLint("HandlerLeak")
        @Override
        public void onConnected() {
            try {
                //SeekBar更新
                handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        switch (msg.what) {
                            case 0:
                                playNowCurrent.setMax((int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
                                seekBarEnd.setText(MediaUtil.formatTime((int)metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)));
                                break;
                            case 1:
                                playNowCurrent.setProgress(PlayService.current);
                                break;
                        }
                    }
                };
                // 获取MediaControllerCompat
                mediaController = new MediaControllerCompat(
                        PlayNow.this,
                        mediaBrowserCompat.getSessionToken());
                MediaControllerCompat.setMediaController(PlayNow.this, mediaController);
                mediaController.registerCallback(mediaControllerCompatCallback);
                //设置当前数据
                mediaControllerCompatCallback.onMetadataChanged(mediaController.getMetadata());
                mediaControllerCompatCallback.onPlaybackStateChanged(mediaController.getPlaybackState());
                String mediaId = AppConstant.MediaIdInfo.MEDIA_ID_NORMAL;
                mediaBrowserCompat.unsubscribe(mediaId);
                mediaBrowserCompat.subscribe(mediaId, mediaBrowserSubscriptionCallback);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private final MediaBrowserCompat.SubscriptionCallback mediaBrowserSubscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);

        }
    };

    private final MediaControllerCompat.Callback mediaControllerCompatCallback = new MediaControllerCompat.Callback(){
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_NONE://默认状态
                    playNowPlay.setImageResource(R.drawable.ic_now_play);
                    if (cover != null) {
                        playNowPlay.getDrawable().setTint(Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52")));
                    }
                    playNowCurrent.setProgress(0);
                    break;
                case PlaybackStateCompat.STATE_PLAYING:
                    playNowPlay.setImageResource(R.drawable.ic_now_pause);
                    if (cover != null) {
                        playNowPlay.getDrawable().setTint(Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52")));
                    }
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    playNowPlay.setImageResource(R.drawable.ic_now_play);
                    if (cover != null) {
                        playNowPlay.getDrawable().setTint(Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52")));
                    }
                    break;
                case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT://下一首
                case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS://上一首
                case PlaybackStateCompat.STATE_BUFFERING:
                case PlaybackStateCompat.STATE_CONNECTING:
                case PlaybackStateCompat.STATE_ERROR:
                case PlaybackStateCompat.STATE_FAST_FORWARDING:
                case PlaybackStateCompat.STATE_REWINDING:
                case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM:
                case PlaybackStateCompat.STATE_STOPPED:
                    break;
            }
        }
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata != null) {
                PlayNow.this.metadata = metadata;
                cover = metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);
                int tint = Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52"));
                playNowTitle.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
                playNowArtist.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
                playNowCover.setImageBitmap(cover);
                playNowLayout.setBackgroundColor(Palette.from(cover).generate().getLightVibrantColor(Color.parseColor("#ffffff")));
                playNowTitle.setTextColor(tint);
                playNowArtist.setTextColor(tint);
                seekBarStart.setTextColor(tint);
                seekBarEnd.setTextColor(tint);
                playNowPrev.getDrawable().mutate().setTint(tint);
                playNowPlay.getDrawable().mutate().setTint(tint);
                playNowNext.getDrawable().mutate().setTint(tint);
                playNowMode.getDrawable().mutate().setTint(tint);
                setSeekBarColor(playNowCurrent, tint);
                handler.sendEmptyMessage(0);
                updateSeekBar();
            }
        }

        //SeekBar变色
        private void setSeekBarColor(SeekBar seekBar, int color){
            LayerDrawable layerDrawable = (LayerDrawable)seekBar.getProgressDrawable();
            Drawable drawable=layerDrawable.getDrawable(2);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC);
            seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            seekBar.invalidate();
        }

        // SeekBar更新计时器
        private void updateSeekBar(){
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if(mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING && !playNowCurrent.isPressed()){
                        handler.sendEmptyMessage(1);
                    }
                }
            };
            timer.schedule(timerTask, 0 ,1000);
        }

    };

}
