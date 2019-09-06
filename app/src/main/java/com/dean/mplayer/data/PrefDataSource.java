package com.dean.mplayer.data;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(value = SharedPref.Scope.UNIQUE)
public interface PrefDataSource {

    /**
     * 初次启动
     */
    @DefaultBoolean(true)
    boolean isAppInitial();

}
