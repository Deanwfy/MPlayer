package com.dean.mplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class PlayNow extends AppCompatActivity {

    // 播放控制显示
    private TextView playNowTitle;
    private TextView playNowArtist;
    private ImageView playNowCover;
    private SeekBar playNowCurrent;
    private ConstraintLayout playNowLayout;

    // 播放控制按钮
    private Button playNowMode;
    private Button playNowPrev;
    private Button playNowPlay;
    private Button playNowNext;

    //获取服务
    private MediaControllerCompat mediaController;
    private MediaBrowserCompat mediaBrowserCompat;

    //歌曲信息
    private List<MusicInfo> musicInfos = null;
    Bitmap cover = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_now);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        findControlBtnById();
        setPlayNowMode();
        setControlBtnOnClickListener();
        musicInfos = MediaUtil.getMusicInfos(getApplicationContext());
        initMediaBrowser();
    }

    // 统一获取播放控制面板控件id
    private void findControlBtnById() {
        playNowTitle = findViewById(R.id.playNowTitle);
        playNowArtist = findViewById(R.id.playNowArtist);
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
        playNowMode.setOnClickListener(controlBtnOnClickListener);
        playNowPrev.setOnClickListener(controlBtnOnClickListener);
        playNowPlay.setOnClickListener(controlBtnOnClickListener);
        playNowNext.setOnClickListener(controlBtnOnClickListener);
    }

    // 命名播放控制面板监听器类，实现监听事件
    private class ControlBtnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.playNowMode:
                    switch (PlayService.mode) {
                        case AppConstant.PlayMode.MODE_ORDER:
                            playNowMode.setBackgroundResource(R.drawable.ic_now_loop);
                            if(cover != null) {
                                playNowMode.getBackground().setTint(Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52")));
                            }
                            PlayService.mode = AppConstant.PlayMode.MODE_LOOP;
//                            Toast.makeText(getApplicationContext(),"列表循环", Toast.LENGTH_SHORT).show();
                            break;
                        case AppConstant.PlayMode.MODE_LOOP:
                            playNowMode.setBackgroundResource(R.drawable.ic_now_single);
                            if(cover != null) {
                                playNowMode.getBackground().setTint(Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52")));
                            }
                            PlayService.mode = AppConstant.PlayMode.MODE_SINGLE;
//                            Toast.makeText(getApplicationContext(),"单曲循环", Toast.LENGTH_SHORT).show();
                            break;
                        case AppConstant.PlayMode.MODE_SINGLE:
                            playNowMode.setBackgroundResource(R.drawable.ic_now_random);
                            if(cover != null) {
                                playNowMode.getBackground().setTint(Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52")));
                            }
                            PlayService.mode = AppConstant.PlayMode.MODE_RANDOM;
//                            Toast.makeText(getApplicationContext(),"随机播放", Toast.LENGTH_SHORT).show();
                            break;
                        case AppConstant.PlayMode.MODE_RANDOM:
                            playNowMode.setBackgroundResource(R.drawable.ic_now_order);
                            if(cover != null) {
                                playNowMode.getBackground().setTint(Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52")));
                            }
                            PlayService.mode = AppConstant.PlayMode.MODE_ORDER;
//                            Toast.makeText(getApplicationContext(),"顺序播放", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case R.id.playNowPlay:
                    if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                        mediaController.getTransportControls().pause();
                    } else if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED) {
                        mediaController.getTransportControls().play();
                    } else {
                        mediaController.getTransportControls().playFromUri(musicInfos.get(0).getUri(), null);
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

    private void setPlayNowMode() {
        switch(PlayService.mode)
        {
            case AppConstant.PlayMode.MODE_ORDER:
                playNowMode.setBackgroundResource(R.drawable.ic_now_order);
                break;
            case AppConstant.PlayMode.MODE_LOOP:
                playNowMode.setBackgroundResource(R.drawable.ic_now_loop);
                break;
            case AppConstant.PlayMode.MODE_SINGLE:
                playNowMode.setBackgroundResource(R.drawable.ic_now_single);
                break;
            case AppConstant.PlayMode.MODE_RANDOM:
                playNowMode.setBackgroundResource(R.drawable.ic_now_random);
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
        @Override
        public void onConnected() {
            try {
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
                    playNowPlay.setBackgroundResource(R.drawable.ic_now_play);
                    if(cover != null) {
                        playNowPlay.getBackground().setTint(Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52")));
                    }
                    break;
                case PlaybackStateCompat.STATE_PLAYING:
                    playNowPlay.setBackgroundResource(R.drawable.ic_now_pause);
                    if(cover != null) {
                        playNowPlay.getBackground().setTint(Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52")));
                    }
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    playNowPlay.setBackgroundResource(R.drawable.ic_now_play);
                    if(cover != null) {
                        playNowPlay.getBackground().setTint(Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52")));
                    }
                    break;
                case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT://下一首
                    break;
                case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS://上一首
                    break;
            }
        }
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata != null) {
                cover = metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);
                playNowTitle.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
                playNowArtist.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
                playNowCover.setImageBitmap(cover);
                playNowLayout.setBackgroundColor(Palette.from(cover).generate().getLightVibrantColor(Color.parseColor("#ffffff")));
                playNowPrev.getBackground().setTint(Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52")));
                playNowPlay.getBackground().setTint(Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52")));
                playNowNext.getBackground().setTint(Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52")));
                playNowMode.getBackground().setTint(Palette.from(cover).generate().getVibrantColor(Color.parseColor("#005b52")));
            }
        }
    };

}
