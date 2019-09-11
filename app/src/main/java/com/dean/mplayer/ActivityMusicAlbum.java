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
public class ActivityMusicAlbum extends BaseActivity {

    @ViewById(R.id.music_control_panel)
    ControlPanel controlPanel;

    @ViewById(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;

    @ViewById(R.id.activity_music_base_list)
    RecyclerView musicListArtistAlbumRecyclerView;

    // 列表显示
    private LoadingDialog loadingDialog;
    private MusicListAlbumRecyclerAdapter musicListAlbumRecyclerAdapter;
    private List<MusicInfo> musicInfo = new ArrayList<>();
    private List<LocalAlbm> localAlbm = new ArrayList<>();

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

    @AfterViews
    void initViews(){
        Toolbar toolbar = findViewById(R.id.activity_music_base_toolbar);   // 标题栏实现
        toolbar.setTitle(R.string.activity_music_album); // 必须放在setSupportActionBar前面
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());    // 必须放在setSupportActionBar后面
        toolbar.setTitleTextColor(getResources().getColor(R.color.drawerArrowStyle));
        toolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
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
                .setNegativeButton("确定", (dialog, which) -> {
                })
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
    private void initPlayList() {
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
        musicListAlbumRecyclerAdapter = new MusicListAlbumRecyclerAdapter(localAlbm) {
            @Override
            public void onBindViewHolder(@NonNull MusicListAlbumRecyclerAdapterHolder musicListAlbumRecyclerAdapterHolder, int position) {
                super.onBindViewHolder(musicListAlbumRecyclerAdapterHolder, position);
                LocalAlbm localAlbm = musicListAlbumRecyclerAdapter.getMusicListAlbumFilter().get(position);
                musicListAlbumRecyclerAdapterHolder.musicInfoName.setText(localAlbm.getName());
            }
        };
        musicListAlbumRecyclerAdapter.setOnItemClickListener(((view, position) -> {
            localAlbm = musicListAlbumRecyclerAdapter.getMusicListAlbumFilter();
            musicAlbumMusicList = localAlbm.get(position).getMusicInfos();
            ActivityMusicAlbumMusic_.intent(this).start();
        }));
        musicListArtistAlbumRecyclerView.setAdapter(musicListAlbumRecyclerAdapter);
        musicListAlbumRecyclerAdapter.notifyDataSetChanged();
    }

    // 退出时断开媒体中心连接
    @Override
    protected void onDestroy() {
        controlPanel.stopConnection();
        super.onDestroy();
    }

}

