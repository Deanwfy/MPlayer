package com.dean.mplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ActivityMusicAlbum extends AppCompatActivity {

    // 列表显示
    private LoadingDialog loadingDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView musicListArtistAlbumRecyclerView;
    private MusicListAlbumRecyclerAdapter musicListAlbumRecyclerAdapter;
    private List<MusicInfo> musicInfo = new ArrayList<>();
    private List<LocalAlbm> localAlbm = new ArrayList<>();

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

    // 专辑子页面传值
    public static List<MusicInfo> musicAlbumMusicList;

    // Toolbar本地搜索
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.toolbar_search_menu);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.searchNoticeLocal));
        // 图标样式，通过Style中的colorControlNormal进行了设置
//        ImageView iconSearch = searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
//        iconSearch.setColorFilter(ContextCompat.getColor(this, R.color.drawerArrowStyle));
//        ImageView iconClose = searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
//        iconClose.setColorFilter(ContextCompat.getColor(this, R.color.drawerArrowStyle));
        // 搜索框样式
        EditText editText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        editText.setTextColor(ContextCompat.getColor(this, R.color.drawerArrowStyle));
        editText.setHintTextColor(ContextCompat.getColor(this, R.color.editNoticeText));
        // 控件间隔
        LinearLayout search_edit_frame = searchView.findViewById(R.id.search_edit_frame);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) search_edit_frame.getLayoutParams();
        params.leftMargin = 0;
        params.rightMargin = 0;
        search_edit_frame.setLayoutParams(params);
        // 搜索框监听
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String s) {
                musicListAlbumRecyclerAdapter.getFilter().filter(s);
                return true;
            }
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_music_base);

        Toolbar toolbar = findViewById(R.id.activity_music_base_toolbar);   // 标题栏实现
        toolbar.setTitle(R.string.activity_music_album); // 必须放在setSupportActionBar前面
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());    // 必须放在setSupportActionBar后面
        toolbar.setTitleTextColor(getResources().getColor(R.color.drawerArrowStyle));
        toolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()){
                case R.id.action_setting:
                    Toast.makeText(this, "开发中...", Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        });

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this::requestPermission);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        // 本地音乐列表
        musicListArtistAlbumRecyclerView = findViewById(R.id.activity_music_base_list);

        findControlBtnById(); // 获取播放控制面板控件
        setControlBtnOnClickListener(); // 为播放控制面板控件设置监听器
        initMediaBrowser();

        loadingDialog = new LoadingDialog(this);
        loadingDialog.setLoadingText("扫描中...")
                .setInterceptBack(false)
                .show();
        requestPermission();    // 权限申请

    }

    // 动态权限申请
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, AppConstant.Permission.PERMISSION_READ_WRITE_EXTERNAL_STORAGE);
        } else {
            initPlayList();
        }
    }
    private void showWaringDialog() {
        new AlertDialog.Builder(this)
                .setTitle("权限申请")
                .setMessage("请前往设置->应用->MPlayer->权限中打开相关权限，否则部分功能无法正常使用")
                .setNegativeButton("确定", (dialog, which) -> {})
                .show();
        loadingDialog.close();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case AppConstant.Permission.PERMISSION_READ_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initPlayList();
                } else {
                    showWaringDialog();
                }
                break;
            }
        }
    }

    // 加载本地音乐人
    private void initPlayList(){
        new Thread(() -> {
            musicInfo = MediaUtil.getMusicLocal(this);
            localAlbm = MediaUtil.getAlbmLocal();
            runOnUiThread(() -> {
                if (musicInfo != null && musicInfo.size() != 0) {
                    setListAdapter();   // 显示歌曲列表
                    swipeRefreshLayout.setRefreshing(false);
                }
                loadingDialog.close();
            });
        }).start();
    }
    // 歌曲列表显示适配器
    public void setListAdapter() {
        LinearLayoutManager musicListArtistAlbumRecyclerLayoutManager = new LinearLayoutManager(this);
        musicListArtistAlbumRecyclerView.setLayoutManager(musicListArtistAlbumRecyclerLayoutManager);
        musicListAlbumRecyclerAdapter = new MusicListAlbumRecyclerAdapter(localAlbm){
            @Override
            public void onBindViewHolder(@NonNull MusicListAlbumRecyclerAdapterHolder musicListAlbumRecyclerAdapterHolder, int position) {
                super.onBindViewHolder(musicListAlbumRecyclerAdapterHolder, position);
                LocalAlbm localAlbm = musicListAlbumRecyclerAdapter.getMusicListAlbumFilter().get(position);
                musicListAlbumRecyclerAdapterHolder.musicInfoName.setText(localAlbm.getName());
            }
        };
        musicListAlbumRecyclerAdapter.setOnItemClickListener(((view, position) -> {
            localAlbm = musicListAlbumRecyclerAdapter.getMusicListAlbumFilter();
            List<MusicInfo> musicInfos = localAlbm.get(position).getMusicInfos();
            Intent intentMusicAlbumMusic = new Intent(this, ActivityMusicAlbumMusic.class);
            musicAlbumMusicList = musicInfos;
            startActivity(intentMusicAlbumMusic);
        }));
        musicListArtistAlbumRecyclerView.setAdapter(musicListAlbumRecyclerAdapter);
        musicListAlbumRecyclerAdapter.notifyDataSetChanged();
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
                        ActivityMusicAlbum.this,
                        mediaBrowserCompat.getSessionToken());
                MediaControllerCompat.setMediaController(ActivityMusicAlbum.this, mediaController);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMusicAlbum.this, R.style.DialogPlayList);
                    // 自定义布局
                    @SuppressLint("InflateParams")
                    View playListView = LayoutInflater.from(ActivityMusicAlbum.this).inflate(R.layout.play_list,null);
                    // 设置AlertDialog参数，加载自定义布局
                    builder.setView(playListView);
                    // AlertDialog对象
                    AlertDialog alertDialogMusicList = builder.create();
                    // 自定义布局RecyclerLayout适配实现
                    RecyclerView playListRecycler = playListView.findViewById(R.id.play_list);
                    LinearLayoutManager playListRecyclerLayoutManager = new LinearLayoutManager(ActivityMusicAlbum.this);
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
                    windowDialog.getDecorView().setBackgroundColor(ActivityMusicAlbum.this.getResources().getColor(R.color.colorControlPanel));
                    // 设置大小
                    WindowManager.LayoutParams layoutParams = windowDialog.getAttributes();
                    layoutParams.width = displayMetrics.widthPixels;
//                    layoutParams.height = (int)(displayMetrics.heightPixels * 0.6);
                    // 设置位置为底部
                    layoutParams.gravity = Gravity.BOTTOM;
                    windowDialog.setAttributes(layoutParams);
                    break;
                case R.id.search_entry:
                    Intent intentSearchOnline = new Intent(ActivityMusicAlbum.this, ActivityMusicOnline.class);
                    startActivity(intentSearchOnline);
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
        Intent intentPlayNow = new Intent(ActivityMusicAlbum.this, ActivityNowPlay.class);
        startActivity(intentPlayNow);
        overridePendingTransition(R.anim.activity_playnow_enter, 0);
    }

    // 退出时断开媒体中心连接
    @Override
    protected void onDestroy() {
        mediaBrowserCompat.disconnect();
        super.onDestroy();
    }

}

