package com.dean.mplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.dean.mplayer.MediaUtil.getMusicMaps;

public class ActivityMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // 列表显示
    private LoadingDialog loadingDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView musicListView;
    private List<MusicInfo> musicInfo = new ArrayList<>();
    static List<PlayList> playList = new ArrayList<>();
    public static int listPosition = 0;

    // 媒体信息
    private TextView PlayingTitle;
    private TextView PlayingArtist;
    private ImageView PlayingCover;

    // 播放控制按钮
    private ConstraintLayout musicControlPanel;
    private ImageButton PlayBtn;
    private ImageButton ListBtn;

    //睡眠定时计时器
    private Timer clockTimer;
    private boolean playFull;
    public static boolean needToStop;

    // 媒体播放服务
    private MediaControllerCompat mediaController;
    private MediaBrowserCompat mediaBrowserCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 读取配置文件
        SharedPreferences sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        int nightMode = sharedPreferences.getInt("nightMode", AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(nightMode);
        playFull = sharedPreferences.getBoolean("playFull", false);

        setContentView(R.layout.activity_main);

        // 状态栏透明
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.setStatusBarColor(Color.TRANSPARENT);

        Toolbar toolbar = findViewById(R.id.main_toolbar);   // 标题栏实现
        toolbar.inflateMenu(R.menu.toolbar_custom_menu);
//        setSupportActionBar(toolbar);   // ToolBar替换ActionBar，使用该方法自定义布局inflateMenu不生效
        findViewById(R.id.search_online_entry).setOnClickListener(new ControlBtnOnClickListener());

        // 抽屉
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawertoggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(actionBarDrawertoggle);
        actionBarDrawertoggle.syncState();
        // 抽屉菜单
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        musicListView = findViewById(R.id.music_list);
        musicListView.setOnItemClickListener(new MusicListItemClickListener());    // 将监听器设置到歌曲列表
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this::requestPermission);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        findControlBtnById(); // 获取播放控制面板控件
        setControlBtnOnClickListener(); // 为播放控制面板控件设置监听器
        registerForContextMenu(musicListView);
        musicListView.setOnItemLongClickListener((parent, view, position, id) -> {
            musicListView.showContextMenu();
            return true;
        });

        loadingDialog = new LoadingDialog(this);
        loadingDialog.setLoadingText("扫描中...")
                .setInterceptBack(false)
                .show();
        requestPermission();    // 权限申请

        initMediaBrowser();

        Intent intentPlayService = new Intent(this, PlayService.class);
        startService(intentPlayService);
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

    // 加载播放列表,显示本地音乐
    private void initPlayList(){
        new Thread(() -> {
            musicInfo = MediaUtil.getMusicLocal(this);
            runOnUiThread(() -> {
                if (musicInfo != null && musicInfo.size() != 0) {
                    setListAdapter(getMusicMaps(musicInfo));   // 显示歌曲列表
                    swipeRefreshLayout.setRefreshing(false);
                    for (int musicCountLocal = 0; musicCountLocal < musicInfo.size(); musicCountLocal++) {
                        playList.add(new PlayList(
                                        musicInfo.get(musicCountLocal).getId(),
                                        musicInfo.get(musicCountLocal).getTitle(),
                                        musicInfo.get(musicCountLocal).getAlbum(),
                                        musicInfo.get(musicCountLocal).getArtist(),
                                        musicInfo.get(musicCountLocal).getDuration(),
                                        musicInfo.get(musicCountLocal).getUri(),
                                        musicInfo.get(musicCountLocal).getAlbumBitmap()
                                )
                        );
                    }
                }
                loadingDialog.close();
            });
        }).start();
    }
    // 刷新播放列表
    private void refreshPlayList(){
        playList.clear();
        if (musicInfo != null && musicInfo.size() != 0) {
            for (int musicCountLocal = 0; musicCountLocal < musicInfo.size(); musicCountLocal++) {
                playList.add(new PlayList(
                                musicInfo.get(musicCountLocal).getId(),
                                musicInfo.get(musicCountLocal).getTitle(),
                                musicInfo.get(musicCountLocal).getAlbum(),
                                musicInfo.get(musicCountLocal).getArtist(),
                                musicInfo.get(musicCountLocal).getDuration(),
                                musicInfo.get(musicCountLocal).getUri(),
                                musicInfo.get(musicCountLocal).getAlbumBitmap()
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
                        ActivityMain.this,
                        mediaBrowserCompat.getSessionToken());
                MediaControllerCompat.setMediaController(ActivityMain.this, mediaController);
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
                    cancelClock();
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

    // 歌曲列表显示适配器
    public void setListAdapter(List<HashMap<String, String>> musicList) {
        SimpleAdapter listAdapter = new SimpleAdapter(this, musicList,
                R.layout.music_list_item_layout, new String[]{"title", "artist", "duration"},
                new int[]{R.id.music_title, R.id.music_artist, R.id.music_duration});
        musicListView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }

    // 歌曲列表监听器
    private class MusicListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            refreshPlayList();
            listPosition = --position;
            mediaController.getTransportControls().skipToNext();
        }
    }

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
    private void setControlBtnOnClickListener(){
        ControlBtnOnClickListener controlBtnOnClickListener = new ControlBtnOnClickListener();
        PlayBtn.setOnClickListener(controlBtnOnClickListener);
        ListBtn.setOnClickListener(controlBtnOnClickListener);
        musicControlPanel.setOnClickListener(controlBtnOnClickListener);
    }
    // 命名播放控制面板监听器类，实现监听事件
    private class ControlBtnOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.playing_play:
                    if (playList != null && playList.size() != 0) {
                        if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                            mediaController.getTransportControls().pause();
                        } else if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED) {
                            mediaController.getTransportControls().play();
                        } else {
                            mediaController.getTransportControls().playFromUri(playList.get(listPosition).getUri(), null);
                        }
                    }
                    break;
                case R.id.playing_list:
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMain.this);
                    // 自定义布局
                    @SuppressLint("InflateParams")
                    View playListView = LayoutInflater.from(ActivityMain.this).inflate(R.layout.play_list,null);
                    // 设置AlertDialog参数，加载自定义布局
                    builder.setView(playListView);
                    // AlertDialog对象
                    AlertDialog alertDialogMusicList = builder.create();
                    // 自定义布局RecyclerLayout适配实现
                    RecyclerView playListRecycler = playListView.findViewById(R.id.play_list);
                    LinearLayoutManager playListRecyclerLayoutManager = new LinearLayoutManager(ActivityMain.this);
                    playListRecycler.setLayoutManager(playListRecyclerLayoutManager);
                    PlayListRecyclerAdapter playListRecyclerAdapter = new PlayListRecyclerAdapter(playList);
                    playListRecyclerAdapter.setOnItemClickListener((view, position) -> {
                        listPosition = --position;
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
                    windowDialog.getDecorView().setBackgroundColor(ActivityMain.this.getResources().getColor(R.color.colorControlPanel));
                    // 设置大小
                    WindowManager.LayoutParams layoutParams = windowDialog.getAttributes();
                    layoutParams.width = displayMetrics.widthPixels;
//                    layoutParams.height = (int)(displayMetrics.heightPixels * 0.6);
                    // 设置位置为底部
                    layoutParams.gravity = Gravity.BOTTOM;
                    windowDialog.setAttributes(layoutParams);
                    break;
                case R.id.music_control_panel:
                    Intent intentPlayNow = new Intent(ActivityMain.this, PlayNow.class);
                    startActivity(intentPlayNow);
                    break;
                case R.id.search_online_entry:
                    Intent intentSearchOnline = new Intent(ActivityMain.this, ActivityMusicOnline.class);
                    startActivity(intentSearchOnline);
                    break;
            }
        }
    }
    // 列表长按菜单
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,0,0,"删除");
    }

    // 退出同时结束后台服务
    @Override
    protected void onDestroy() {
        mediaBrowserCompat.disconnect();
        Intent intentPlayService = new Intent(this, PlayService.class);
        stopService(intentPlayService);
        super.onDestroy();
    }

    // 返回退回到桌面
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
                return true;
            } else {
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                home.addCategory(Intent.CATEGORY_HOME);
                startActivity(home);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    // 抽屉菜单点击事件
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.ic_menu_clock) {
            setClock();
        } else if (id == R.id.ic_menu_theme) {
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            int nightMode = (currentNightMode == Configuration.UI_MODE_NIGHT_NO ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            AppCompatDelegate.setDefaultNightMode(nightMode);
            SharedPreferences.Editor editor = getSharedPreferences("setting", MODE_PRIVATE).edit();
            editor.putInt("nightMode",nightMode);
            editor.apply();
            recreate();
        } else if (id == R.id.ic_menu_settings) {
            Toast.makeText(this, "开发中...", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.ic_menu_exit) {
            finish();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // 睡眠定时弹窗
    @SuppressLint("InflateParams")
    private void setClock() {
        // 创建AlertDialog构建器
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // AlertDialog数据
        List<String> items = new ArrayList<>();
        items.add("不设置定时");
        items.add("15分钟");
        items.add("30分钟");
        items.add("45分钟");
        items.add("60分钟");
        items.add("自定义");
        // 自定义布局
        View menuClock = LayoutInflater.from(ActivityMain.this).inflate(R.layout.drawer_menu_clock,null);
        // 设置AlertDialog参数，加载自定义布局
        builder.setTitle("睡眠定时").setView(menuClock);
        // AlertDialog对象
        final AlertDialog alertDialog = builder.create();
        // 自定义布局RecyclerLayout适配实现
        RecyclerView menuClockRecycler = menuClock.findViewById(R.id.menuClockRecycler);
        LinearLayoutManager menuClockRecyclerLayoutManager = new LinearLayoutManager(this){
            @Override
            public boolean canScrollVertically() {
                return false;   //不允许滑动
            }
        };
        menuClockRecycler.setLayoutManager(menuClockRecyclerLayoutManager);
        MenuClockRecyclerAdapter menuClockRecyclerAdapter = new MenuClockRecyclerAdapter(items);
        menuClockRecyclerAdapter.setOnItemClickListener((view, position) -> {
            switch (position) {
                case 0:
                    if (clockTimer != null) {
                        cancelClock();
                        Toast.makeText(ActivityMain.this, "已取消定时停止播放", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 1:
                    runClock(900000);
                    break;
                case 2:
                    runClock(1800000);
                    break;
                case 3:
                    runClock(2700000);
                    break;
                case 4:
                    runClock(3600000);
                    break;
                case 5:
                    View customClock = LayoutInflater.from(ActivityMain.this).inflate(R.layout.drawer_menu_clock_custom, null);
                    final AlertDialog.Builder customBuilder = new AlertDialog.Builder(ActivityMain.this);
                    final NumberPicker customNumberPickerHour = customClock.findViewById(R.id.pickerHour);
                    final NumberPicker customNumberPickerMin = customClock.findViewById(R.id.pickerMin);
                    customNumberPickerHour.setMinValue(0);
                    customNumberPickerHour.setMaxValue(23);
                    customNumberPickerHour.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);   //不可编辑
                    customNumberPickerHour.setWrapSelectorWheel(false);    //不循环
                    customNumberPickerMin.setMinValue(0);
                    customNumberPickerMin.setMaxValue(59);
                    customNumberPickerMin.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);    //不可编辑
                    customNumberPickerMin.setWrapSelectorWheel(false);    //不循环
                    customBuilder.setTitle("自定义睡眠定时").setView(customClock)
                            .setPositiveButton("确定", (dialog, which) -> runClock(customNumberPickerHour.getValue() * 3600000 + customNumberPickerMin.getValue() * 60000))
                            .setNegativeButton("取消", (dialog, which) -> {});
                    customBuilder.create().show();
                    break;
            }
            // 自定义布局需手动调用dismiss使AlertDialog消失
            alertDialog.dismiss();
        });
        menuClockRecycler.setAdapter(menuClockRecyclerAdapter);
        // 当前歌曲结束后再停止
        CheckBox playFullCheckBox  = menuClock.findViewById(R.id.playFull);
        playFullCheckBox.setChecked(playFull);
        playFullCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            playFull = isChecked;
            SharedPreferences.Editor editor = getSharedPreferences("setting", MODE_PRIVATE).edit();
            editor.putBoolean("playFull", playFull);
            editor.apply();
        });
        // 显示AlertDialog
        alertDialog.show();
    }

    //睡眠定时计时器
    private void runClock(long clockTime){
        cancelClock();
        if (clockTime/3600000 > 0){
            long hour = clockTime / 3600000;
            long min = (clockTime % 3600000) / 60000;
            Toast.makeText(this, "已设置"+hour+"小时"+min+"分钟后停止播放", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "已设置" + clockTime / 60000 + "分钟后停止播放", Toast.LENGTH_SHORT).show();
        }
        clockTimer = new Timer();
        clockTimer.schedule(new TimerTask() {
            public void run() {
                int playBackState = mediaController.getPlaybackState().getState();
                if (!playFull) {
                    if (playBackState == PlaybackStateCompat.STATE_PLAYING || playBackState == PlaybackStateCompat.STATE_PAUSED) {
                        mediaController.getTransportControls().stop();
                    }
                }else {
                    if (playBackState == PlaybackStateCompat.STATE_PAUSED) {
                        mediaController.getTransportControls().stop();
                    } else if (playBackState == PlaybackStateCompat.STATE_PLAYING) {
                        needToStop = true;
                    }
                }
            }
        }, clockTime);
    }
    private void cancelClock(){
        if (clockTimer != null){
            clockTimer.cancel();
            needToStop = false;
        }
    }

}
