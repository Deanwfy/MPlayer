package com.dean.mplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dean.mplayer.base.BaseActivity;
import com.dean.mplayer.util.AppConstant;
import com.dean.mplayer.util.MediaUtil;
import com.dean.mplayer.view.common.ControlPanel;
import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_music_base)
public class ActivityMusicArtistMusic extends BaseActivity {

    @ViewById(R.id.music_control_panel)
    ControlPanel controlPanel;

    @ViewById(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    @ViewById(R.id.activity_music_base_list)
    RecyclerView musicListLocalRecyclerView;

    // 列表显示
    private LoadingDialog loadingDialog;
    private MusicListLocalRecyclerAdapter musicListLocalRecyclerAdapter;
    private List<MusicInfo> artistMusicInfos = new ArrayList<>();

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

    @AfterViews
    void initViews() {
        artistMusicInfos = ActivityMusicArtist.musicArtistMusicList;

        Toolbar toolbar = findViewById(R.id.activity_music_base_toolbar);   // 标题栏实现
        toolbar.setTitle(artistMusicInfos.get(0).getArtist()); // 必须放在setSupportActionBar前面
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

        controlPanel.build(this);

        swipeRefreshLayout.setOnRefreshListener(this::requestPermission);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

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
        if(artistMusicInfos != null && artistMusicInfos.size() != 0) {
            setListAdapter();   // 显示歌曲列表
            swipeRefreshLayout.setRefreshing(false);
        }
        loadingDialog.close();
    }
    // 歌曲列表显示适配器
    public void setListAdapter() {
        LinearLayoutManager musicListLocalRecyclerLayoutManager = new LinearLayoutManager(this);
        musicListLocalRecyclerView.setLayoutManager(musicListLocalRecyclerLayoutManager);
        musicListLocalRecyclerAdapter = new MusicListLocalRecyclerAdapter(artistMusicInfos);
        musicListLocalRecyclerAdapter.setOnItemClickListener(((view, position) -> {
            artistMusicInfos = musicListLocalRecyclerAdapter.getMusicListLocalFilter();
            if (playList.size() != 0) {
                PlayList playListMusicInfo;
                int playListPosition;
                for (playListPosition = 0; playListPosition < playList.size(); playListPosition++) {
                    playListMusicInfo = playList.get(playListPosition);
                    if (playListMusicInfo.getId() == artistMusicInfos.get(position).getId()) {
                        ActivityMain.listPosition = --playListPosition;
                        ActivityMain.mediaController.getTransportControls().skipToNext();
                        break;
                    }
                }
                if (playListPosition == playList.size()) {
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
        playList.clear();
        artistMusicInfos = musicListLocalRecyclerAdapter.getMusicListLocalFilter();
        if (artistMusicInfos != null && artistMusicInfos.size() != 0) {
            for (int musicCountLocal = 0; musicCountLocal < artistMusicInfos.size(); musicCountLocal++) {
                MusicInfo itemMusicInfo = artistMusicInfos.get(musicCountLocal);
                playList.add(new PlayList(
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

    // 列表长按菜单点击事件
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        artistMusicInfos = musicListLocalRecyclerAdapter.getMusicListLocalFilter();
        int contextMenuPosition = musicListLocalRecyclerAdapter.getContextMenuPosition();
        MusicInfo itemMusicInfo = artistMusicInfos.get(contextMenuPosition);
        switch (item.getItemId()){
            case 0:
                controlPanel.addToNext(itemMusicInfo);
                break;
            case 1:
                if (MediaUtil.deleteMusicFile(this, itemMusicInfo.getUri())){
                    Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                    artistMusicInfos.remove(contextMenuPosition);
                    musicListLocalRecyclerAdapter.notifyItemRemoved(contextMenuPosition);
                    musicListLocalRecyclerAdapter.notifyItemRangeChanged(contextMenuPosition,artistMusicInfos.size() - contextMenuPosition);
                }else {
                    Toast.makeText(this, "删除失败，文件不存在或权限丢失", Toast.LENGTH_SHORT).show();
                    requestPermission();
                }
                break;
        }
        return super.onContextItemSelected(item);
    }

    // 退出时断开媒体中心连接
    @Override
    protected void onDestroy() {
        controlPanel.stopConnection();
        super.onDestroy();
    }

}

