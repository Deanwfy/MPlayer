package com.dean.mplayer.view.nowPlay;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.support.v4.media.session.MediaControllerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dean.mplayer.PlayService;
import com.dean.mplayer.R;
import com.dean.mplayer.base.BaseFragment;

import me.wcy.lrcview.LrcView;

public class FragmentLrc extends BaseFragment {
    
    public FragmentLrc() {
        // Required empty public constructor
    }
    
    private View fragmentLrc;
    private Activity testActivity;
    public static LrcView lrcView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentLrc = inflater.inflate(R.layout.fragment_lrc, container, false);
        return fragmentLrc;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lrcView = fragmentLrc.findViewById(R.id.lrcView);
        setLrcView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        testActivity = (Activity)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void setLrcView(){
        lrcView.loadLrc(PlayService.lrcString);
        lrcView.setOnPlayClickListener(time -> {
            MediaControllerCompat mediaControllerCompat = MediaControllerCompat.getMediaController(testActivity);
            mediaControllerCompat.getTransportControls().seekTo(time);
            return true;
        });
    }

}
