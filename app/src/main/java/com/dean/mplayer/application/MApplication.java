package com.dean.mplayer.application;

import android.app.Application;

import com.dean.mplayer.base.BaseActivity;
import com.dean.mplayer.data.AppDataSource;
import com.dean.mplayer.data.DataRepository_;
import com.dean.mplayer.data.PrefDataSource_;

import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EApplication
public class MApplication extends Application {

    @Pref
    PrefDataSource_ prefDataSource;

    @Override
    public void onCreate() {
        super.onCreate();
        BaseActivity.playList = DataRepository_.getInstance_(this).getPlayList();
        BaseActivity.listPosition = prefDataSource.listPosition().get();
    }
}
