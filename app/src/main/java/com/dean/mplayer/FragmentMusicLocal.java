package com.dean.mplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.dean.mplayer.MediaUtil.getMusicMaps;

public class FragmentMusicLocal extends Fragment {

    public FragmentMusicLocal() {
        // Required empty public constructor
    }

    private LoadingDialog loadingDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView musicListView;
    private List<MusicInfo> musicInfo = new ArrayList<>();
    private View fragmentMusicLocal;
    private Activity activityMain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentMusicLocal = inflater.inflate(R.layout.fragment_music_local, container, false);
        return fragmentMusicLocal;
    }

    @Override
    public void onViewCreated(@NonNull View viewer, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(viewer, savedInstanceState);

        musicListView = fragmentMusicLocal.findViewById(R.id.music_list);
        musicListView.setOnItemClickListener(new MusicListItemClickListener());    // 将监听器设置到歌曲列表
        swipeRefreshLayout = fragmentMusicLocal.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this::requestPermission);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        registerForContextMenu(musicListView);
        musicListView.setOnItemLongClickListener((parent, view, position, id) -> {
            musicListView.showContextMenu();
            return true;
        });

        loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.setLoadingText("扫描中...")
                .setInterceptBack(false)
                .show();
        requestPermission();    // 权限申请

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityMain = (Activity)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    // 加载播放列表,显示本地音乐
    private void initPlayList(){
        new Thread(() -> {
            musicInfo = MediaUtil.getMusicLocal(activityMain);
            activityMain.runOnUiThread(() -> {
                if (musicInfo != null && musicInfo.size() != 0) {
                    setListAdapter(getMusicMaps(musicInfo));   // 显示歌曲列表
                    swipeRefreshLayout.setRefreshing(false);
                    for (int musicCountLocal = 0; musicCountLocal < musicInfo.size(); musicCountLocal++) {
                        ActivityMain.playList.add(new PlayList(
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
        ActivityMain.playList.clear();
        if (musicInfo != null && musicInfo.size() != 0) {
            for (int musicCountLocal = 0; musicCountLocal < musicInfo.size(); musicCountLocal++) {
                ActivityMain.playList.add(new PlayList(
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

    // 歌曲列表显示适配器
    public void setListAdapter(List<HashMap<String, String>> musicList) {
        SimpleAdapter listAdapter = new SimpleAdapter(activityMain, musicList,
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
            ActivityMain.listPosition = --position;
            ActivityMain.mediaController.getTransportControls().skipToNext();
        }
    }

    // 动态权限申请
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(activityMain, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activityMain, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activityMain, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, AppConstant.Permission.PERMISSION_READ_WRITE_EXTERNAL_STORAGE);
        } else {
            initPlayList();
        }
    }
    private void showWaringDialog() {
        new AlertDialog.Builder(activityMain)
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

    // 列表长按菜单
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,0,0,"删除");
    }
}
