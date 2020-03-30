package com.dean.mplayer.base;

import android.support.v4.media.session.MediaControllerCompat;

import com.dean.mplayer.PlayList;

import java.util.ArrayList;
import java.util.List;

import me.yokeyword.fragmentation.SupportActivity;

public class BaseActivity extends SupportActivity {
    // 列表显示
    public static List<PlayList> playList = new ArrayList<>();
    public static int listPosition = 0;
    // 媒体播放服务
    public static MediaControllerCompat mediaController;
}
