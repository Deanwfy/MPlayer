package com.dean.mplayer.application;

import android.app.Application;

import com.dean.mplayer.data.PrefDataSource_;

import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EApplication
public class MApplication extends Application {

    @Pref
    PrefDataSource_ prefDataSource;
}
