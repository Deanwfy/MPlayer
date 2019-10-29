package com.dean.mplayer.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.dean.mplayer.data.model.PlayList;

import java.util.List;

@Dao
public interface PlayListDao {

    @Query("SELECT * FROM play_list ORDER BY uid")
    List<PlayList> getAll();

    @Query("SELECT COUNT(*) FROM play_list WHERE id == :id")
    int isExist(String id);

    @Insert
    void insertAll(List<PlayList> playList);

    @Query("DELETE FROM play_list")
    void deleteAll();

}
