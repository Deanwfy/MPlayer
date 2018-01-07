package com.dean.mplayer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import static com.dean.mplayer.MediaUtil.getMusicMaps;

public class ListActivity extends AppCompatActivity {

    // 列表显示
    private ListView mMusicList;
    private List<MusicInfo> mp3Infos = null;
    private SimpleAdapter mAdapter;

    // 播放控制显示
    private TextView PlayingTitle;
    private TextView PlayingArtist;
    private ImageView PlayingCover;

    // 播放控制按钮
    private Button PrevBtn;
    private Button PlayBtn;
    private Button NextBtn;

    // 播放标志
    private boolean isFirstTime = true;
    private boolean isPlaying;
    private boolean isPaused;
    private int listPosition = 0;

    // 防误触确认退出
    private long exitTime = 0;

    private ImageButton btnTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Toolbar toolbar =(Toolbar)findViewById(R.id.toolbar);   // 标题栏实现
        setSupportActionBar(toolbar);   // ToolBar替换ActionBar

        btnTemp = findViewById(R.id.btnTemp);//临时
        btnTemp.setOnClickListener(new TempListener());

        mMusicList = (ListView) findViewById(R.id.music_list);
        mMusicList.setOnItemClickListener(new MusicListItemClickListener());    // 将监听器设置到歌曲列表
        mp3Infos = MediaUtil.getMusicInfos(getApplicationContext());    // 获取歌曲信息
        setListAdpter(getMusicMaps(mp3Infos));  // 显示歌曲列表

        findControlBtnById(); // 获取播放控制面板控件
        setControlBtnOnClickListener(); // 为播放控制面板控件设置监听器

    }

    //临时
    class TempListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(),"我是来自计科三班的：王飞宇\n爱好电影音乐科技\n擅长前端及安卓\n请多指教",Toast.LENGTH_LONG).show();
        }
    }

    // 歌曲列表显示适配器
    public void setListAdpter(List<HashMap<String, String>> musiclist) {
        mAdapter = new SimpleAdapter(this, musiclist,
                R.layout.music_list_item_layout, new String[] { "title", "artist","duration" },
                new int[] { R.id.music_title, R.id.music_artist , R.id.music_duration });
        mMusicList.setAdapter(mAdapter);
    }

    // 歌曲列表监听器
    private class MusicListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            listPosition = position;
            playMusic(listPosition);
        }
    }

    // 通过列表播放音乐
    public void playMusic(int listPosition) {
        if (mp3Infos != null) {
            PlayBtn.setBackgroundResource(R.drawable.play);
            MusicInfo mp3Info = mp3Infos.get(listPosition);
            PlayingTitle.setText(mp3Info.getTitle());
            PlayingArtist.setText(mp3Info.getArtist());
            Bitmap bitmap = MediaUtil.getArtwork(this, mp3Info.getId(),mp3Info.getAlbumId(), true);
            PlayingCover.setImageBitmap(bitmap);
            Intent intent = new Intent(ListActivity.this, PlayService.class);
            intent.putExtra("url", mp3Info.getUrl());
            intent.putExtra("listPosition", listPosition);
            intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
            startService(intent);
        }
    }

    // 统一获取播放控制面板控件id
    private void findControlBtnById(){
        PrevBtn = (Button)findViewById(R.id.prev);
        PlayBtn = (Button)findViewById(R.id.play);
        NextBtn = (Button)findViewById(R.id.next);
        PlayingTitle = (TextView)findViewById(R.id.playing_title);
        PlayingArtist = (TextView)findViewById(R.id.playing_artist);
        PlayingCover = (ImageView)findViewById(R.id.music_cover);
    }

    // 将监听器设置到播放控制面板控件
    private void setControlBtnOnClickListener(){
        ControlBtnOnClickListener controlBtnOnClickListener = new ControlBtnOnClickListener();
        PrevBtn.setOnClickListener(controlBtnOnClickListener);
        PlayBtn.setOnClickListener(controlBtnOnClickListener);
        NextBtn.setOnClickListener(controlBtnOnClickListener);
    }

    // 命名播放控制面板监听器类，实现监听事件
    private class ControlBtnOnClickListener implements OnClickListener{
        Intent intent = new Intent(ListActivity.this, PlayService.class);

        @Override
        public void onClick(View v){
            switch (v.getId()){
                case R.id.prev:
                    PlayBtn.setBackgroundResource(R.drawable.play);
                    isFirstTime = false;
                    isPlaying = true;
                    isPaused = false;
                    prevSong();
                    break;
                case R.id.play:
                    if(isFirstTime) {
                        PlayBtn.setBackgroundResource(R.drawable.play);
                        playSong();
                        isFirstTime = false;
                        isPlaying = true;
                        isPaused = false;
                    } else {
                        if (isPlaying) {
                            PlayBtn.setBackgroundResource(R.drawable.pause);
                            intent.putExtra("MSG", AppConstant.PlayerMsg.PAUSE_MSG);
                            startService(intent);
                            isPlaying = false;
                            isPaused = true;

                        } else if (isPaused) {
                            PlayBtn.setBackgroundResource(R.drawable.play);
                            intent.putExtra("MSG", AppConstant.PlayerMsg.CONTINUE_MSG);
                            startService(intent);
                            isPaused = false;
                            isPlaying = true;
                        }
                    }
                    break;
                case R.id.next:
                    PlayBtn.setBackgroundResource(R.drawable.play);
                    isFirstTime = false;
                    isPlaying = true;
                    isPaused = false;
                    nextSong();
                    break;
            }
        }
    }

    // 播放
    public void playSong() {
        MusicInfo mp3Info = mp3Infos.get(listPosition);
        PlayingTitle.setText(mp3Info.getTitle());
        PlayingArtist.setText(mp3Info.getArtist());
        Bitmap bitmap = MediaUtil.getArtwork(this, mp3Info.getId(),mp3Info.getAlbumId(), true);
        PlayingCover.setImageBitmap(bitmap);
        Intent intent = new Intent(ListActivity.this, PlayService.class);
        intent.putExtra("listPosition", 0);
        intent.putExtra("url", mp3Info.getUrl());
        intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
        startService(intent);
    }

    // 上一曲
    public void prevSong() {
        listPosition = listPosition - 1;
        if(listPosition >= 0) {
            MusicInfo mp3Info = mp3Infos.get(listPosition);
            PlayingTitle.setText(mp3Info.getTitle());
            PlayingArtist.setText(mp3Info.getArtist());
            Bitmap bitmap = MediaUtil.getArtwork(this, mp3Info.getId(),mp3Info.getAlbumId(), true);
            PlayingCover.setImageBitmap(bitmap);
            Intent intent = new Intent(ListActivity.this, PlayService.class);
            intent.putExtra("listPosition", listPosition);
            intent.putExtra("url", mp3Info.getUrl());
            intent.putExtra("MSG", AppConstant.PlayerMsg.PRIVIOUS_MSG);
            startService(intent);
        }else {
            Toast.makeText(ListActivity.this, "已经是第一首了", Toast.LENGTH_SHORT).show();
        }
    }

    //下一曲
    public void nextSong() {
        listPosition = listPosition + 1;
        if(listPosition <= mp3Infos.size() - 1) {
            MusicInfo mp3Info = mp3Infos.get(listPosition);
            PlayingTitle.setText(mp3Info.getTitle());
            PlayingArtist.setText(mp3Info.getArtist());
            Bitmap bitmap = MediaUtil.getArtwork(this, mp3Info.getId(),mp3Info.getAlbumId(), true);
            PlayingCover.setImageBitmap(bitmap);
            Intent intent = new Intent(ListActivity.this, PlayService.class);
            intent.putExtra("listPosition", listPosition);
            intent.putExtra("url", mp3Info.getUrl());
            intent.putExtra("MSG", AppConstant.PlayerMsg.NEXT_MSG);
            startService(intent);
        } else {
            Toast.makeText(ListActivity.this, "已经是最后一首", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(ListActivity.this, PlayService.class);
        stopService(intent);
        super.onDestroy();
    }

    // 两次返回键确认退出
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

}
