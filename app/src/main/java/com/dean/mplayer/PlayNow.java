package com.dean.mplayer;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

public class PlayNow extends AppCompatActivity {

    // 播放控制显示
    private TextView playNowTitle;
    private TextView playNowArtist;
    private ImageView playNowCover;
    private SeekBar playNowCurrent;

    // 播放控制按钮
    private Button playNowPrev;
    private Button playNowPlay;
    private Button playNowNext;

    //获取服务
    private MediaControllerCompat mediaController;
    private MediaBrowserCompat mediaBrowserCompat;

    //歌曲信息
    private List<MusicInfo> musicInfos = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_now);
        findControlBtnById();
        setControlBtnOnClickListener();
        musicInfos = MediaUtil.getMusicInfos(getApplicationContext());
        initMediaBrowser();
    }

    // 统一获取播放控制面板控件id
    private void findControlBtnById(){
        playNowTitle = findViewById(R.id.playNowTitle);
        playNowArtist = findViewById(R.id.playNowArtist);
        playNowCover = findViewById(R.id.playNowCover);
        playNowCurrent = findViewById(R.id.playNowCurrent);

        playNowPrev = findViewById(R.id.playNowPrev);
        playNowPlay = findViewById(R.id.playNowPlay);
        playNowNext = findViewById(R.id.playNowNext);
    }

    // 将监听器设置到播放控制面板控件
    private void setControlBtnOnClickListener(){
        ControlBtnOnClickListener controlBtnOnClickListener = new ControlBtnOnClickListener();
        playNowPrev.setOnClickListener(controlBtnOnClickListener);
        playNowPlay.setOnClickListener(controlBtnOnClickListener);
        playNowNext.setOnClickListener(controlBtnOnClickListener);
    }

    // 命名播放控制面板监听器类，实现监听事件
    private class ControlBtnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v){
            switch (v.getId()){
                case R.id.playNowPlay:
                    if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                        mediaController.getTransportControls().pause();
                    } else if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED){
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
                    break;
                case PlaybackStateCompat.STATE_PLAYING:
                    playNowPlay.setBackgroundResource(R.drawable.ic_now_pause);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    playNowPlay.setBackgroundResource(R.drawable.ic_now_play);
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
                playNowTitle.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
                playNowArtist.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
                playNowCover.setImageBitmap(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART));
            }
        }
    };

}
