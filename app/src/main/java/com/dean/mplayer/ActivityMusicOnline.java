package com.dean.mplayer;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.squareup.picasso.Picasso;
import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ActivityMusicOnline extends AppCompatActivity {

    MusicListRecyclerAdapter musicListRecyclerAdapter;
    RecyclerView musicListOnlineRecycler;
    EditText searchOnline;
    LoadingDialog loadingDialog;

    List<Songs> musicInfo = new ArrayList<>();  //设置空数据以完成RecyclerAdapter初始化，否则报错（虽然会自动忽略）

    //获取服务
    private MediaControllerCompat mediaController;
    private MediaBrowserCompat mediaBrowserCompat;
    MediaMetadataCompat metadata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_online);

        Toolbar toolbar = findViewById(R.id.music_online_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        musicListOnlineRecycler = findViewById(R.id.music_list_online);
        LinearLayoutManager musicListOnlineRecyclerLayoutManager = new LinearLayoutManager(this);
        musicListOnlineRecycler.setLayoutManager(musicListOnlineRecyclerLayoutManager);
        musicListOnlineRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        musicListRecyclerAdapter = new MusicListRecyclerAdapter(musicInfo);
        musicListOnlineRecycler.setAdapter(musicListRecyclerAdapter);

        searchOnline = findViewById(R.id.music_search_online);
        SearchOnlineListener searchOnlineListener = new SearchOnlineListener();
        searchOnline.setOnKeyListener(searchOnlineListener);
        initMediaBrowser();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaBrowserCompat.disconnect();
    }

    private class SearchOnlineListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP){
                loadingAnimation("搜索中...");
                String userInput = searchOnline.getText().toString();
                if(TextUtils.isEmpty(userInput)){
                    return true;
                }
                getSearchUrl(userInput.trim());
                return true;
            }
            return false;
        }
    }

    // 搜索结果
    private void getSearchUrl(String userInput){
        String searchUrl = "http://39.108.4.217:8888/search?keywords= " + userInput;
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
        musicInfo = JSONArray.parseArray((jsonObject.getJSONObject("result").getJSONArray("songs").toJSONString()), Songs.class);
    }
    private void uiUpdate(){
        runOnUiThread(() -> {
                    musicListRecyclerAdapter = new MusicListRecyclerAdapter(musicInfo);
                    musicListRecyclerAdapter.setOnItemClickListener((view, position) -> getMusicCheck(position));
                    musicListOnlineRecycler.setAdapter(musicListRecyclerAdapter);
                    loadingDialog.close();
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
        String album = musicInfo.get(position).getAlbum().getName();
        String artist = musicInfo.get(position).getArtists().get(0).getName();
        long duration = musicInfo.get(position).getDuration();
        Uri uri = Uri.parse(jsonObject.getJSONArray("data").getJSONObject(0).getString("url"));
        long albumId = musicInfo.get(position).getAlbum().getId();
        getAlbumCover(id, title, album, artist, duration, uri, albumId);
    }
    private void getAlbumCover(long id, String title, String album, String artist, long duration, Uri uri, long albumId){
        String url = "http://39.108.4.217:8888/album?id=" + albumId;
        new Thread(() -> {
            try {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = okHttpClient.newCall(request).execute();
                assert response.body() != null;
                String responseData = response.body().string();
                JSONObject jsonObject = JSON.parseObject(responseData);
                Bitmap albumBitmap = Picasso.get().load(jsonObject.getJSONArray("songs").getJSONObject(0).getJSONObject("al").getString("picUrl")).get();
                playOnlineMusic(id, title, album, artist, duration, uri, albumBitmap);
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }
    private void playOnlineMusic(long id, String title, String album, String artist, long duration, Uri uri, Bitmap albumBitmap){
        if (ActivityMain.playList.size() != 0) {
            ActivityMain.playList.add(ActivityMain.listPosition + 1, new PlayList(id, title, album, artist, duration, uri, albumBitmap));
            mediaController.getTransportControls().skipToNext();
        }else {
            //　无本地音乐的情况（直接播放网络音乐）
            ActivityMain.playList.add(0, new PlayList(id, title, album, artist, duration, uri, albumBitmap));
            mediaController.getTransportControls().playFromUri(uri, null);
        }
        musicCheckResult(true, false);
        Intent playNowIntent = new Intent(this, PlayNow.class);
        startActivity(playNowIntent);
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
        @SuppressLint("HandlerLeak")
        @Override
        public void onConnected() {
            try {
                // 获取MediaControllerCompat
                mediaController = new MediaControllerCompat(
                        ActivityMusicOnline.this,
                        mediaBrowserCompat.getSessionToken());
                MediaControllerCompat.setMediaController(ActivityMusicOnline.this, mediaController);
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

    private final MediaControllerCompat.Callback mediaControllerCompatCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_NONE://默认状态
                    break;
                case PlaybackStateCompat.STATE_PLAYING:
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
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
                ActivityMusicOnline.this.metadata = metadata;
            }
        }
    };
}
