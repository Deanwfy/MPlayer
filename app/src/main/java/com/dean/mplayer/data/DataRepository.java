package com.dean.mplayer.data;

import android.content.Context;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EBean(scope = EBean.Scope.Singleton)
public class DataRepository implements AppDataSource {

    @RootContext
    Context mContext;

    @Pref
    PrefDataSource_ mPrefDataSource;

//    private AppDatabase mDatabase;

    @AfterInject
    void initData() {
//        mDatabase = AppDatabase.getInstance(mContext);
    }

    @Override
    public boolean isAppInitial() {
        return mPrefDataSource.isAppInitial().get();
    }

}
