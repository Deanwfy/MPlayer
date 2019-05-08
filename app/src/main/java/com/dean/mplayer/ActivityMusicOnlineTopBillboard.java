package com.dean.mplayer;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dean.mplayer.onlineTopBillboard.Tracks;
import com.squareup.picasso.Picasso;
import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ActivityMusicOnlineTopBillboard extends AppCompatActivity {

    // 列表显示
    private LoadingDialog loadingDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView musicListOnlineTopBillboardRecycler;
    private MusicListOnlineTopBillboardRecyclerAdapter musicListOnlineTopBillboardRecyclerAdapter;
    List<Tracks> musicInfo = new ArrayList<>();

    // 媒体信息
    private TextView PlayingTitle;
    private TextView PlayingArtist;
    private ImageView PlayingCover;

    // 播放控制按钮
    private ConstraintLayout musicControlPanel;
    private ImageButton PlayBtn;
    private ImageButton ListBtn;

    // 媒体播放服务
    private MediaControllerCompat mediaController;
    private MediaBrowserCompat mediaBrowserCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_music_base);

        Toolbar toolbar = findViewById(R.id.activity_music_base_toolbar);   // 标题栏实现
        toolbar.setTitle(R.string.activity_music_online_top_billboard); // 必须放在setSupportActionBar前面
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());    // 必须放在setSupportActionBar后面
        toolbar.setTitleTextColor(getResources().getColor(R.color.drawerArrowStyle));

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this::getSearchUrl);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        findControlBtnById(); // 获取播放控制面板控件
        setControlBtnOnClickListener(); // 为播放控制面板控件设置监听器
        initMediaBrowser();

        musicListOnlineTopBillboardRecycler = findViewById(R.id.activity_music_base_list);
        LinearLayoutManager musicListOnlineRecyclerLayoutManager = new LinearLayoutManager(this);
        musicListOnlineTopBillboardRecycler.setLayoutManager(musicListOnlineRecyclerLayoutManager);
        musicListOnlineTopBillboardRecyclerAdapter = new MusicListOnlineTopBillboardRecyclerAdapter(musicInfo);
        musicListOnlineTopBillboardRecycler.setAdapter(musicListOnlineTopBillboardRecyclerAdapter);
        
        loadingAnimation("获取中...");
        getSearchUrl();
    }

    private void getSearchUrl(){
        String searchUrl = "http://39.108.4.217:8888/top/list?idx=6";
        new Thread(() -> {
            try {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(searchUrl)
                        .build();
                Response response = okHttpClient.newCall(request).execute();
                assert response.body() != null;
                String responseData = response.body().string();
                parseJSON(responseData);
                uiUpdate();
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }
    private void parseJSON(String response){
        JSONObject jsonObject = JSON.parseObject(response);
        musicInfo = JSON.parseArray((jsonObject.getJSONObject("playlist").getJSONArray("tracks").toJSONString()), Tracks.class);
    }
    private void uiUpdate(){
        runOnUiThread(() -> {
                    musicListOnlineTopBillboardRecyclerAdapter = new MusicListOnlineTopBillboardRecyclerAdapter(musicInfo);
                    musicListOnlineTopBillboardRecyclerAdapter.setOnItemClickListener((view, position) -> getMusicCheck(position));
                    musicListOnlineTopBillboardRecycler.setAdapter(musicListOnlineTopBillboardRecyclerAdapter);
                    loadingDialog.close();
                    swipeRefreshLayout.setRefreshing(false);
                }
        );
    }
    // 检查所选音乐是否可用
    private void getMusicCheck(int position){
        String musicCheckUrl = "http://39.108.4.217:8888/check/music?id=" + String.valueOf(musicInfo.get(position).getId());
        new Thread(() -> {
            try {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(musicCheckUrl)
                        .build();
                Response response = okHttpClient.newCall(request).execute();
                assert response.body() != null;
                String responseData = response.body().string();
                String state = JSON.parseObject(responseData).getString("success");
                if (state.equals("true")){
                    musicCheckResult(true, true);
                    getMusicInfoUrl(position);
                }else {
                    musicCheckResult(false, false);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }
    // 从搜索结果中播放
    private void getMusicInfoUrl(int position){
        String musicInfoUrl = "http://39.108.4.217:8888/song/url?id=" + String.valueOf(musicInfo.get(position).getId());
        new Thread(() -> {
            try {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(musicInfoUrl)
                        .build();
                Response response = okHttpClient.newCall(request).execute();
                assert response.body() != null;
                String responseData = response.body().string();
                setOnlineMusicInfo(responseData, position);
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }
    private void setOnlineMusicInfo(String response, int position){
        JSONObject jsonObject = JSON.parseObject(response);
        long id = musicInfo.get(position).getId();
        String title = musicInfo.get(position).getName();
        String album = musicInfo.get(position).getAl().getName();
        String artist = musicInfo.get(position).getAr().get(0).getName();
        long duration = musicInfo.get(position).getDt();
        Uri uri = Uri.parse(jsonObject.getJSONArray("data").getJSONObject(0).getString("url"));
        try {
            Bitmap albumBitmap = Picasso.get().load(musicInfo.get(position).getAl().getPicUrl()).get();
            playOnlineMusic(id, title, album, artist, duration, uri, albumBitmap);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void playOnlineMusic(long id, String title, String album, String artist, long duration, Uri uri, Bitmap albumBitmap){
        if (ActivityMain.playList.size() != 0) {
            ActivityMain.playList.add(ActivityMain.listPosition + 1, new PlayList(id, title, album, artist, duration, uri, albumBitmap, "Netease"));
            mediaController.getTransportControls().skipToNext();
        }else {
            //　播放列表为空的情况（直接播放网络音乐）
            ActivityMain.playList.add(0, new PlayList(id, title, album, artist, duration, uri, albumBitmap, "Netease"));
            mediaController.getTransportControls().playFromUri(uri, null);
        }
        musicCheckResult(true, false);
    }

    // 加载动画
    private void loadingAnimation(String dialogText){
        loadingDialog = new LoadingDialog(this);
        loadingDialog.setLoadingText(dialogText)
                .setInterceptBack(false)
                .show();
    }

    // 版权提示
    private void musicCheckResult(boolean result, boolean show){
        if (result) {
            if (show) {
                runOnUiThread(() -> loadingAnimation("加载中..."));
            }else {
                runOnUiThread(() -> loadingDialog.close());
            }
        }else {
            runOnUiThread(() -> Toast.makeText(this, "抱歉，暂无版权", Toast.LENGTH_SHORT).show());
        }
    }

    //——————————————————————————————————MediaBrowser————————————————————————————————————————————

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
                        ActivityMusicOnlineTopBillboard.this,
                        mediaBrowserCompat.getSessionToken());
                MediaControllerCompat.setMediaController(ActivityMusicOnlineTopBillboard.this, mediaController);
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
                    PlayBtn.setImageResource(R.drawable.ic_play);
                    break;
                case PlaybackStateCompat.STATE_PLAYING:
                    PlayBtn.setImageResource(R.drawable.ic_pause);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    PlayBtn.setImageResource(R.drawable.ic_play);
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
                PlayingTitle.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
                PlayingArtist.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
                PlayingCover.setImageBitmap(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART));
            }
        }
    };

    // 统一获取播放控制面板控件id
    private void findControlBtnById(){
        musicControlPanel = findViewById(R.id.music_control_panel);
        PlayBtn = findViewById(R.id.playing_play);
        ListBtn = findViewById(R.id.playing_list);
        PlayingTitle = findViewById(R.id.playing_title);
        PlayingArtist = findViewById(R.id.playing_artist);
        PlayingCover = findViewById(R.id.music_cover);
    }

    // 将监听器设置到播放控制面板控件
    @SuppressLint("ClickableViewAccessibility")
    private void setControlBtnOnClickListener(){
        ControlBtnOnClickListener controlBtnOnClickListener = new ControlBtnOnClickListener();
        PlayBtn.setOnClickListener(controlBtnOnClickListener);
        ListBtn.setOnClickListener(controlBtnOnClickListener);
        GestureDetector gestureDetector = new GestureDetector(this, new ControlPanelOnGestureListener());
        musicControlPanel.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }
    // 命名播放控制面板监听器类，实现监听事件
    private class ControlBtnOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.playing_play:
                    if (ActivityMain.playList != null && ActivityMain.playList.size() != 0) {
                        if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                            mediaController.getTransportControls().pause();
                        } else if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED) {
                            mediaController.getTransportControls().play();
                        } else {
                            mediaController.getTransportControls().playFromUri(ActivityMain.playList.get(ActivityMain.listPosition).getUri(), null);
                        }
                    }
                    break;
                case R.id.playing_list:
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMusicOnlineTopBillboard.this, R.style.DialogPlayList);
                    // 自定义布局
                    @SuppressLint("InflateParams")
                    View playListView = LayoutInflater.from(ActivityMusicOnlineTopBillboard.this).inflate(R.layout.play_list,null);
                    // 设置AlertDialog参数，加载自定义布局
                    builder.setView(playListView);
                    // AlertDialog对象
                    AlertDialog alertDialogMusicList = builder.create();
                    // 自定义布局RecyclerLayout适配实现
                    RecyclerView playListRecycler = playListView.findViewById(R.id.play_list);
                    LinearLayoutManager playListRecyclerLayoutManager = new LinearLayoutManager(ActivityMusicOnlineTopBillboard.this);
                    playListRecycler.setLayoutManager(playListRecyclerLayoutManager);
                    PlayListRecyclerAdapter playListRecyclerAdapter = new PlayListRecyclerAdapter(ActivityMain.playList);
                    playListRecyclerAdapter.setOnItemClickListener((view, position) -> {
                        ActivityMain.listPosition = --position;
                        mediaController.getTransportControls().skipToNext();
                    });
                    playListRecycler.setAdapter(playListRecyclerAdapter);
                    // 关闭按钮
                    Button buttonClose = playListView.findViewById(R.id.play_list_close);
                    buttonClose.setOnClickListener((view) -> alertDialogMusicList.dismiss());
                    // 显示
                    alertDialogMusicList.show();
                    // 获取屏幕
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    // 获取列表dialog
                    Window windowDialog = alertDialogMusicList.getWindow();
                    assert windowDialog != null;
                    //去掉dialog默认的padding
                    windowDialog.getDecorView().setPadding(0, 0, 0, 0);
                    windowDialog.getDecorView().setBackgroundColor(ActivityMusicOnlineTopBillboard.this.getResources().getColor(R.color.colorControlPanel));
                    // 设置大小
                    WindowManager.LayoutParams layoutParams = windowDialog.getAttributes();
                    layoutParams.width = displayMetrics.widthPixels;
//                    layoutParams.height = (int)(displayMetrics.heightPixels * 0.6);
                    // 设置位置为底部
                    layoutParams.gravity = Gravity.BOTTOM;
                    windowDialog.setAttributes(layoutParams);
                    break;
            }
        }
    }
    private class ControlPanelOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e1.getRawY() - e2.getRawY() > 50) {
                startActivityPlayNow();
            }
            return true;
        }
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            startActivityPlayNow();
            return false;
        }
    }
    private void startActivityPlayNow(){
        Intent intentPlayNow = new Intent(ActivityMusicOnlineTopBillboard.this, ActivityNowPlay.class);
        startActivity(intentPlayNow);
        overridePendingTransition(R.anim.activity_playnow_enter, 0);
    }

    // 列表长按菜单
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,0,0,"下一首播放");
    }

    // 退出时断开媒体中心连接
    @Override
    protected void onDestroy() {
        mediaBrowserCompat.disconnect();
        super.onDestroy();
    }

}

