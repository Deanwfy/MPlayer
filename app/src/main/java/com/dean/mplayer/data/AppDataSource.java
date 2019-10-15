package com.dean.mplayer.data;

import com.dean.mplayer.PlayList;

import java.util.List;

public interface AppDataSource {

    /**
     * 初次启动
     */
    boolean isAppInitial();

    /**
     * 播放列表
     */
    List<PlayList> getPlayList();
    void updatePlayList(List<PlayList> playList);

}
