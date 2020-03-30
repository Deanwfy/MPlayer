package com.dean.mplayer.view.artist;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dean.mplayer.Arts;
import com.dean.mplayer.MusicInfo;
import com.dean.mplayer.R;
import com.dean.mplayer.base.BaseActivity;
import com.dean.mplayer.util.AppConstant;
import com.dean.mplayer.util.MediaUtil;
import com.dean.mplayer.view.adapter.MusicListArtistAlbumRecyclerAdapter;
import com.dean.mplayer.view.common.ControlPanel;
import com.dean.mplayer.view.common.MToolbar;
import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_music_base)
public class ActivityMusicArtist extends BaseActivity {

    @ViewById(R.id.base_toolbar)
    MToolbar toolbar;

    @ViewById(R.id.music_control_panel)
    ControlPanel controlPanel;

    @ViewById(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    @ViewById(R.id.activity_music_base_list)
    RecyclerView musicListArtistAlbumRecyclerView;

    // 列表显示
    private LoadingDialog loadingDialog;
    private MusicListArtistAlbumRecyclerAdapter musicListArtistAlbumRecyclerAdapter;
    private List<MusicInfo> musicInfo = new ArrayList<>();
    private List<Arts> arts = new ArrayList<>();

    // 专辑子页面传值
    public static List<MusicInfo> musicArtistMusicList;

    @AfterViews
    void initViews() {

        toolbar.setTitle(R.string.activity_music_artist)
                .setHasBack(true)
                .setSearchView(getResources().getString(R.string.searchNoticeLocal), new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextChange(String s) {
                        musicListArtistAlbumRecyclerAdapter.getFilter().filter(s);
                        return true;
                    }
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        return false;
                    }
                })
                .build();

        controlPanel.build(this);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this::requestPermission);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        // 本地音乐列表
        musicListArtistAlbumRecyclerView = findViewById(R.id.activity_music_base_list);

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
            arts = MediaUtil.getArtistsLocal();
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
        musicListArtistAlbumRecyclerAdapter = new MusicListArtistAlbumRecyclerAdapter(arts){
            @Override
            public void onBindViewHolder(@NonNull MusicListArtistAlbumRecyclerAdapterHolder musicListArtistAlbumRecyclerAdapterHolder, int position) {
                super.onBindViewHolder(musicListArtistAlbumRecyclerAdapterHolder, position);
                Arts art = musicListArtistAlbumRecyclerAdapter.getMusicListArtistAlbumFilter().get(position);
                musicListArtistAlbumRecyclerAdapterHolder.musicInfoName.setText(art.getName());
            }
        };
        musicListArtistAlbumRecyclerAdapter.setOnItemClickListener(((view, position) -> {
            arts = musicListArtistAlbumRecyclerAdapter.getMusicListArtistAlbumFilter();
            musicArtistMusicList = arts.get(position).getMusicInfos();
            ActivityMusicArtistMusic_.intent(this).start();
        }));
        musicListArtistAlbumRecyclerView.setAdapter(musicListArtistAlbumRecyclerAdapter);
        musicListArtistAlbumRecyclerAdapter.notifyDataSetChanged();
    }

    // 退出时断开媒体中心连接
    @Override
    protected void onDestroy() {
        controlPanel.stopConnection();
        super.onDestroy();
    }

}

