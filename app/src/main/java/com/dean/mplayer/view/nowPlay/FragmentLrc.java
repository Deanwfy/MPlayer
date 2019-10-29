package com.dean.mplayer.view.nowPlay;


import android.support.v4.media.session.MediaControllerCompat;

import com.dean.mplayer.PlayService;
import com.dean.mplayer.R;
import com.dean.mplayer.base.BaseFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import me.wcy.lrcview.LrcView;

@EFragment(R.layout.fragment_lrc)
public class FragmentLrc extends BaseFragment {
    
    @ViewById(R.id.lrcView)
    public static LrcView lrcView;
    
    @AfterViews
    void initViews() {
        setLrcView();
    }

    public static FragmentLrc newInstance() {
        return FragmentLrc_.builder().build();
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void setLrcView(){
        lrcView.loadLrc(PlayService.lrcString);
        lrcView.setOnPlayClickListener(time -> {
            MediaControllerCompat mediaControllerCompat = MediaControllerCompat.getMediaController(_mActivity);
            mediaControllerCompat.getTransportControls().seekTo(time);
            return true;
        });
    }

}
