package com.dean.mplayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import static com.dean.mplayer.MediaUtil.getMusicMaps;

public class ListActivity extends AppCompatActivity {

    // 列表显示
    private ListView mMusicList;
    private List<MusicInfo> musicInfos = null;

    // 播放控制显示
    private TextView PlayingTitle;
    private TextView PlayingArtist;
    private ImageView PlayingCover;

    // 播放控制按钮
    private Button PrevBtn;
    private Button PlayBtn;
    private Button NextBtn;

    private int listPosition = 0;

    // 防误触确认退出
    private long exitTime = 0;

    //获取服务
    private MediaControllerCompat mediaController;
    private MediaBrowserCompat mediaBrowserCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Toolbar toolbar = findViewById(R.id.toolbar);   // 标题栏实现
        setSupportActionBar(toolbar);   // ToolBar替换ActionBar

        mMusicList = findViewById(R.id.music_list);
        mMusicList.setOnItemClickListener(new MusicListItemClickListener());    // 将监听器设置到歌曲列表
        musicInfos = MediaUtil.getMusicInfos(getApplicationContext());    // 获取歌曲信息
        setListAdpter(getMusicMaps(musicInfos));  // 显示歌曲列表

        findControlBtnById(); // 获取播放控制面板控件
        setControlBtnOnClickListener(); // 为播放控制面板控件设置监听器

        initMediaBrowser();

        Intent intent = new Intent(this, PlayService.class);
        startService(intent);
//        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
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
                        ListActivity.this,
                        mediaBrowserCompat.getSessionToken());
                MediaControllerCompat.setMediaController(ListActivity.this, mediaController);
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

    private final MediaControllerCompat.Callback mediaControllerCompatCallback =
            new MediaControllerCompat.Callback(){
                @Override
                public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
                    switch (state.getState()) {
                        case PlaybackStateCompat.STATE_NONE://默认状态
                            PlayBtn.setBackgroundResource(R.drawable.pause);
                            break;
                        case PlaybackStateCompat.STATE_PLAYING:
                            PlayBtn.setBackgroundResource(R.drawable.play);
                            break;
                        case PlaybackStateCompat.STATE_PAUSED:
                            PlayBtn.setBackgroundResource(R.drawable.pause);
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
                        PlayingTitle.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
                        PlayingArtist.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
                        PlayingCover.setImageBitmap(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART));
                    }
                }
            };

        //菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // 歌曲列表显示适配器
    public void setListAdpter(List<HashMap<String, String>> musiclist) {
        SimpleAdapter mAdapter;
        mAdapter = new SimpleAdapter(this, musiclist,
                R.layout.music_list_item_layout, new String[] { "title", "artist","duration" },
                new int[] { R.id.music_title, R.id.music_artist , R.id.music_duration });
        mMusicList.setAdapter(mAdapter);
    }

    // 歌曲列表监听器
    private class MusicListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle listPosition = new Bundle();
            listPosition.putInt("listPosition", position);
            mediaController.getTransportControls().playFromUri(musicInfos.get(position).getUri(), listPosition);
        }
    }

    // 统一获取播放控制面板控件id
    private void findControlBtnById(){
        PrevBtn = findViewById(R.id.prev);
        PlayBtn = findViewById(R.id.play);
        NextBtn = findViewById(R.id.next);
        PlayingTitle = findViewById(R.id.playing_title);
        PlayingArtist = findViewById(R.id.playing_artist);
        PlayingCover = findViewById(R.id.music_cover);
    }

    // 将监听器设置到播放控制面板控件
    private void setControlBtnOnClickListener(){
        ControlBtnOnClickListener controlBtnOnClickListener = new ControlBtnOnClickListener();
        PrevBtn.setOnClickListener(controlBtnOnClickListener);
        PlayBtn.setOnClickListener(controlBtnOnClickListener);
        NextBtn.setOnClickListener(controlBtnOnClickListener);
    }

    // 命名播放控制面板监听器类，实现监听事件
    private class ControlBtnOnClickListener implements OnClickListener{

        @Override
        public void onClick(View v){
            switch (v.getId()){
                case R.id.prev:
                    mediaController.getTransportControls().skipToPrevious();
                    break;
                case R.id.play:
                    if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                        mediaController.getTransportControls().pause();
                    } else if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED){
                        mediaController.getTransportControls().play();
                    } else {
                        mediaController.getTransportControls().playFromUri(musicInfos.get(0).getUri(), null);
                    }
                    break;
                case R.id.next:
                    mediaController.getTransportControls().skipToNext();
                    break;
            }
        }
    }

    //退出同时结束后台服务
    @Override
    protected void onDestroy() {
        Intent intent = new Intent(ListActivity.this, PlayService.class);
        stopService(intent);
        super.onDestroy();
    }

    // 两次返回键确认退出
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }


}
