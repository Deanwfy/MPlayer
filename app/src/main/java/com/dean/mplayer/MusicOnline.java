package com.dean.mplayer;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MusicOnline extends AppCompatActivity {

    EditText searchOnline;
    Button playMusic;

    String searchUrl;
    String songUrl;
    String playUrl;

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
        searchOnline = findViewById(R.id.music_online_search);
        SearchOnlineListener searchOnlineListener = new SearchOnlineListener();
        searchOnline.setOnKeyListener(searchOnlineListener);
        playMusic = findViewById(R.id.playMusic);
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
                String userInput = searchOnline.getText().toString();
                if(TextUtils.isEmpty(userInput)){
                    return true;
                }
                userInput = userInput.trim();
                searchUrl = "http://39.108.4.217:8888/search?keywords= " + userInput;
                Toast.makeText(MusicOnline.this, "已搜索，点击播放", Toast.LENGTH_SHORT).show();
                sendRequestWithOkHttp(searchUrl, "searchUrl");
                return true;
            }
            return false;
        }
    }

    private void sendRequestWithOkHttp(String url, String type){
        new Thread(() -> {
            try {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = okHttpClient.newCall(request).execute();
                assert response.body() != null;
                String responseData = response.body().string();
                switch (type){
                    case "searchUrl":
                        songUrl = "http://39.108.4.217:8888/song/url?id=" + parseJSON(responseData, type);
                        sendRequestWithOkHttp(songUrl, "songUrl");
                        break;
                    case "songUrl":
                        songUrl = parseJSON(responseData, type);
                        showUrl(songUrl);
                        break;
                }
//                showResponse(responseData);
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    private String parseJSON(String response, String type){
        JSONObject jsonObject = JSON.parseObject(response);
        switch (type){
            case "searchUrl":
                return jsonObject.getJSONObject("result").getJSONArray("songs").getJSONObject(0).getString("id");
            case "songUrl":
                return jsonObject.getJSONArray("data").getJSONObject(0).getString("url");
            default:
                return "找不到结果";
        }
    }

    private void showUrl(final String songUrl){
        runOnUiThread(() ->
                playMusic.setOnClickListener(v -> mediaController.getTransportControls().playFromUri(Uri.parse(songUrl), null))
        );
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
                // 获取MediaControllerCompat
                mediaController = new MediaControllerCompat(
                        MusicOnline.this,
                        mediaBrowserCompat.getSessionToken());
                MediaControllerCompat.setMediaController(MusicOnline.this, mediaController);
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
                MusicOnline.this.metadata = metadata;
            }
        }
    };
}
