package com.dean.mplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
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

import com.dean.mplayer.base.BaseActivity;
import com.dean.mplayer.search.ActivityMusicOnline;
import com.dean.mplayer.util.AppConstant;
import com.dean.mplayer.util.MediaUtil;
import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import java.util.ArrayList;
import java.util.List;

public class ActivityMusicLocal extends BaseActivity {

    // 列表显示
    private LoadingDialog loadingDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView musicListLocalRecyclerView;
    private MusicListLocalRecyclerAdapter musicListLocalRecyclerAdapter;
    private List<MusicInfo> musicInfo = new ArrayList<>();
    private PlayListRecyclerAdapter playListRecyclerAdapter;

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
        EditText editText = searchView.findViewById(R.id.search_src_text);
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
                musicListLocalRecyclerAdapter.getFilter().filter(s);
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
        toolbar.setTitle(R.string.activity_music_local); // 必须放在setSupportActionBar前面
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
        musicListLocalRecyclerView = findViewById(R.id.activity_music_base_list);

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

    // 加载本地音乐
    private void initPlayList(){
        new Thread(() -> {
            musicInfo = MediaUtil.getMusicLocal(this);
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
        LinearLayoutManager musicListLocalRecyclerLayoutManager = new LinearLayoutManager(this);
        musicListLocalRecyclerView.setLayoutManager(musicListLocalRecyclerLayoutManager);
        musicListLocalRecyclerAdapter = new MusicListLocalRecyclerAdapter(musicInfo);
        musicListLocalRecyclerAdapter.setOnItemClickListener(((view, position) -> {
            musicInfo = musicListLocalRecyclerAdapter.getMusicListLocalFilter();
            if (ActivityMain.playList.size() != 0) {
                PlayList playListMusicInfo;
                int playListPosition;
                for (playListPosition = 0; playListPosition < ActivityMain.playList.size(); playListPosition++) {
                    playListMusicInfo = ActivityMain.playList.get(playListPosition);
                    if (playListMusicInfo.getId() == musicInfo.get(position).getId()) {
                        ActivityMain.listPosition = --playListPosition;
                        ActivityMain.mediaController.getTransportControls().skipToNext();
                        break;
                    }
                }
                if (playListPosition == ActivityMain.playList.size()) {
                    refreshPlayList();
                    ActivityMain.listPosition = --position;
                    ActivityMain.mediaController.getTransportControls().skipToNext();
                }
            }else {
                refreshPlayList();
                ActivityMain.listPosition = --position;
                ActivityMain.mediaController.getTransportControls().skipToNext();
            }
        }));
        musicListLocalRecyclerView.setAdapter(musicListLocalRecyclerAdapter);
        musicListLocalRecyclerAdapter.notifyDataSetChanged();
    }
    // 刷新播放列表
    private void refreshPlayList(){
        ActivityMain.playList.clear();
        musicInfo = musicListLocalRecyclerAdapter.getMusicListLocalFilter();
        if (musicInfo != null && musicInfo.size() != 0) {
            for (int musicCountLocal = 0; musicCountLocal < musicInfo.size(); musicCountLocal++) {
                MusicInfo itemMusicInfo = musicInfo.get(musicCountLocal);
                ActivityMain.playList.add(new PlayList(
                        itemMusicInfo.getId(),
                        itemMusicInfo.getTitle(),
                        itemMusicInfo.getAlbum(),
                        itemMusicInfo.getArtist(),
                        itemMusicInfo.getDuration(),
                        itemMusicInfo.getUri(),
                        itemMusicInfo.getAlbumBitmap(),
                        "Local"
                        )
                );
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
                        ActivityMusicLocal.this,
                        mediaBrowserCompat.getSessionToken());
                MediaControllerCompat.setMediaController(ActivityMusicLocal.this, mediaController);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMusicLocal.this, R.style.DialogPlayList);
                    // 自定义布局
                    @SuppressLint("InflateParams")
                    View playListView = LayoutInflater.from(ActivityMusicLocal.this).inflate(R.layout.play_list,null);
                    // 设置AlertDialog参数，加载自定义布局
                    builder.setView(playListView);
                    // AlertDialog对象
                    AlertDialog alertDialogMusicList = builder.create();
                    // 自定义布局RecyclerLayout适配实现
                    RecyclerView playListRecycler = playListView.findViewById(R.id.play_list);
                    LinearLayoutManager playListRecyclerLayoutManager = new LinearLayoutManager(ActivityMusicLocal.this);
                    playListRecycler.setLayoutManager(playListRecyclerLayoutManager);
                    playListRecyclerAdapter = new PlayListRecyclerAdapter(ActivityMain.playList);
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
                    windowDialog.getDecorView().setBackgroundColor(ActivityMusicLocal.this.getResources().getColor(R.color.colorControlPanel));
                    // 设置大小
                    WindowManager.LayoutParams layoutParams = windowDialog.getAttributes();
                    layoutParams.width = displayMetrics.widthPixels;
//                    layoutParams.height = (int)(displayMetrics.heightPixels * 0.6);
                    // 设置位置为底部
                    layoutParams.gravity = Gravity.BOTTOM;
                    windowDialog.setAttributes(layoutParams);
                    break;
                case R.id.search_entry:
                    Intent intentSearchOnline = new Intent(ActivityMusicLocal.this, ActivityMusicOnline.class);
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
        Intent intentPlayNow = new Intent(ActivityMusicLocal.this, ActivityNowPlay.class);
        startActivity(intentPlayNow);
    }

    // 列表长按菜单点击事件
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        musicInfo = musicListLocalRecyclerAdapter.getMusicListLocalFilter();
        int contextMenuPosition = musicListLocalRecyclerAdapter.getContextMenuPosition();
        MusicInfo itemMusicInfo = musicInfo.get(contextMenuPosition);
        switch (item.getItemId()){
            case 0:
                addToNext(itemMusicInfo);
                break;
            case 1:
                if (MediaUtil.deleteMusicFile(this, itemMusicInfo.getUri())){
                    Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                    musicInfo.remove(contextMenuPosition);
                    musicListLocalRecyclerAdapter.notifyItemRemoved(contextMenuPosition);
                    musicListLocalRecyclerAdapter.notifyItemRangeChanged(contextMenuPosition,musicInfo.size() - contextMenuPosition);
                }else {
                    Toast.makeText(this, "删除失败，文件不存在或权限丢失", Toast.LENGTH_SHORT).show();
                    requestPermission();
                }
                break;
        }
        return super.onContextItemSelected(item);
    }

    // 添加下一首播放
    private void addToNext(MusicInfo itemMusicInfo){
        PlayList playListMusicInfo;
        long id = itemMusicInfo.getId();
        String title = itemMusicInfo.getTitle();
        String album = itemMusicInfo.getAlbum();
        String artist = itemMusicInfo.getArtist();
        long duration = itemMusicInfo.getDuration();
        Uri uri = itemMusicInfo.getUri();
        Bitmap albumCover = itemMusicInfo.getAlbumBitmap();
        if (ActivityMain.playList.size() != 0){
            int playListPosition;
            for (playListPosition = 0; playListPosition < ActivityMain.playList.size(); playListPosition++) {
                playListMusicInfo = ActivityMain.playList.get(playListPosition);
                if (playListMusicInfo.getId() == id) {
                    ActivityMain.playList.add(ActivityMain.listPosition + 1, new PlayList(id, title, album, artist, duration, uri, albumCover, "Local"));
                    playListRecyclerAdapter.notifyItemInserted(ActivityMain.listPosition + 1);
                    ActivityMain.playList.remove(playListPosition);
                    playListRecyclerAdapter.notifyItemRemoved(playListPosition);
                    if (playListPosition < ActivityMain.listPosition) {
                        playListRecyclerAdapter.notifyItemRangeChanged(playListPosition, ActivityMain.playList.size() - playListPosition);
                        --ActivityMain.listPosition;
                    } else {
                        playListRecyclerAdapter.notifyItemRangeChanged(ActivityMain.listPosition, ActivityMain.playList.size() - ActivityMain.listPosition);
                    }
                    break;
                }
            }
            if (playListPosition == ActivityMain.playList.size()) {
                ActivityMain.playList.add(ActivityMain.listPosition + 1, new PlayList(id, title, album, artist, duration, uri, albumCover, "Local"));
                playListRecyclerAdapter.notifyItemInserted(ActivityMain.listPosition + 1);
                playListRecyclerAdapter.notifyItemRangeChanged(ActivityMain.listPosition + 1, ActivityMain.playList.size() - ActivityMain.listPosition + 1);
            }
        }else {
            //　播放列表为空的情况（直接播放）
            ActivityMain.playList.add(0, new PlayList(id, title, album, artist, duration, uri, albumCover, "Local"));
        }
    }

    // 退出时断开媒体中心连接
    @Override
    protected void onDestroy() {
        mediaBrowserCompat.disconnect();
        super.onDestroy();
    }

}

