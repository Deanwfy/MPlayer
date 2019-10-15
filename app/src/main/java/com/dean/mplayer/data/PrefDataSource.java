package com.dean.mplayer.data;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(value = SharedPref.Scope.UNIQUE)
public interface PrefDataSource {

    /**
     * 初次启动
     */
    @DefaultBoolean(true)
    boolean isAppInitial();

    @DefaultInt(0)
    int listPosition();

}
