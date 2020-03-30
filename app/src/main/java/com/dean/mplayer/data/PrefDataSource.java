package com.dean.mplayer.data;

import com.dean.mplayer.util.AppConstant;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.DefaultString;
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

    @DefaultBoolean(false)
    boolean isNightMode();

    @DefaultBoolean(false)
    boolean isPlayFull();

    @DefaultString(AppConstant.PlayMode.MODE_ORDER)
    String playMode();


}
