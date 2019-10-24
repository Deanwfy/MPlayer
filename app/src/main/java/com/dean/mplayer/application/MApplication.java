package com.dean.mplayer.application;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.dean.mplayer.PlayService;
import com.dean.mplayer.base.BaseActivity;
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

        AppCompatDelegate.setDefaultNightMode(prefDataSource.isNightMode().get() ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        BaseActivity.playList = DataRepository_.getInstance_(this).getPlayList();
        BaseActivity.listPosition = prefDataSource.listPosition().get();
        PlayService.mode = prefDataSource.playMode().get();
    }
}
