package com.dean.mplayer;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dean.mplayer.base.BaseActivity;
import com.dean.mplayer.data.DataRepository_;
import com.dean.mplayer.onlineTopBillboard.Tracks;
import com.dean.mplayer.view.common.ControlPanel;
import com.dean.mplayer.view.common.MToolbar;
import com.squareup.picasso.Picasso;
import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@EActivity(R.layout.activity_music_base)
public class ActivityMusicOnlineTopBillboard extends BaseActivity {

    @ViewById(R.id.base_toolbar)
    MToolbar toolbar;

    @ViewById(R.id.music_control_panel)
    ControlPanel controlPanel;

    @ViewById(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    @ViewById(R.id.activity_music_base_list)
    RecyclerView musicListOnlineTopBillboardRecycler;

    // 列表显示
    private LoadingDialog loadingDialog;
    private MusicListOnlineTopBillboardRecyclerAdapter musicListOnlineTopBillboardRecyclerAdapter;
    List<Tracks> musicInfo = new ArrayList<>();

    @AfterViews
    void initViews() {

        toolbar.setTitle(R.string.activity_music_online_top_billboard)
                .setHasBack(true)
                .build();

        controlPanel.build(this);

        swipeRefreshLayout.setOnRefreshListener(this::getSearchUrl);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

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
        String musicCheckUrl = "http://39.108.4.217:8888/check/music?id=" + musicInfo.get(position).getId();
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
        String musicInfoUrl = "http://39.108.4.217:8888/song/url?id=" + musicInfo.get(position).getId();
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
        Uri uri = Uri.parse(jsonObject.getJSONArray("data").getJSONObject(0).getString("url"));
        Tracks itemMusicInfo = musicInfo.get(position);
        long id = itemMusicInfo.getId();
        String title = itemMusicInfo.getName();
        String album = itemMusicInfo.getAl().getName();
        String artist = itemMusicInfo.getAr().get(0).getName();
        long duration = itemMusicInfo.getDt();
        String picUrl = itemMusicInfo.getAl().getPicUrl();
        try {
            playOnlineMusic(id, title, album, artist, duration, uri, picUrl);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void playOnlineMusic(long id, String title, String album, String artist, long duration, Uri uri, String picUrl){
        if (playList.size() != 0) {
            playList.add(ActivityMain.listPosition + 1, new PlayList(id, title, album, artist, duration, uri, "Netease", picUrl));
            mediaController.getTransportControls().skipToNext();
        }else {
            //　播放列表为空的情况（直接播放网络音乐）
            playList.add(0, new PlayList(id, title, album, artist, duration, uri, "Netease", picUrl));
            mediaController.getTransportControls().playFromUri(uri, null);
        }
        DataRepository_.getInstance_(this).updatePlayList(playList);
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

    // 列表长按菜单
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,0,0,"下一首播放");
    }

    // 退出时断开媒体中心连接
    @Override
    protected void onDestroy() {
        controlPanel.stopConnection();
        super.onDestroy();
    }

}

