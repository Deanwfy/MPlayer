package com.dean.mplayer.data;

import android.content.Context;

import com.dean.mplayer.PlayList;
import com.dean.mplayer.util.LogUtils;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.List;

@EBean(scope = EBean.Scope.Singleton)
public class DataRepository implements AppDataSource {

    @RootContext
    Context mContext;

    @Pref
    PrefDataSource_ mPrefDataSource;

    private AppDatabase mDatabase;

    @AfterInject
    void initData() {
        mDatabase = AppDatabase.getInstance(mContext);
    }

    @Override
    public boolean isAppInitial() {
        return mPrefDataSource.isAppInitial().get();
    }

    @Override
    public List<PlayList> getPlayList() {
        List<PlayList> list = mDatabase.playListDao().getAll();
        for (PlayList item: list) {
            LogUtils.v(item.title);
        }
        return list;
    }

    @Override
    public void updatePlayList(List<PlayList> playList) {
        mDatabase.playListDao().deleteAll();
        mDatabase.playListDao().insertAll(playList);
        for (PlayList item: playList) {
            LogUtils.v(item.title);
        }
    }
}
