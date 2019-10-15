package com.dean.mplayer.view.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dean.mplayer.ActivityNowPlay_;
import com.dean.mplayer.MusicInfo;
import com.dean.mplayer.PlayList;
import com.dean.mplayer.PlayService;
import com.dean.mplayer.R;
import com.dean.mplayer.base.BaseActivity;
import com.dean.mplayer.data.DataRepository_;
import com.dean.mplayer.data.PrefDataSource_;
import com.dean.mplayer.util.AppConstant;
import com.dean.mplayer.util.MediaUtil;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.List;

import static com.dean.mplayer.base.BaseActivity.listPosition;
import static com.dean.mplayer.base.BaseActivity.playList;

@EViewGroup(R.layout.control_panel)
public class ControlPanel extends ConstraintLayout {

    @Pref
    PrefDataSource_ prefDataSource;

    // 信息
    @ViewById(R.id.playing_title)
    TextView PlayingTitle;

    @ViewById(R.id.playing_artist)
    TextView PlayingArtist;

    @ViewById(R.id.music_cover)
    ImageView PlayingCover;

    // 控制
    @ViewById(R.id.music_control_panel)
    ConstraintLayout musicControlPanel;

    @ViewById(R.id.playing_play)
    ImageButton PlayBtn;

    @ViewById(R.id.playing_list)
    ImageButton ListBtn;

    private Context context;

    private Activity activity;
    private PlayListRecyclerAdapter playListRecyclerAdapter;

    private MediaControllerCompat mediaController;
    private MediaBrowserCompat mediaBrowserCompat;

    public ControlPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void stopConnection() {
        mediaBrowserCompat.disconnect();
    }

    public void build(Activity activity) {
        this.activity = activity;
        playListRecyclerAdapter = new PlayListRecyclerAdapter(playList);
        initMediaBrowser();
    }

    private void initMediaBrowser() {
        if (mediaBrowserCompat == null) {
            // 创建MediaBrowserCompat
            mediaBrowserCompat = new MediaBrowserCompat(
                    context,
                    // 创建ComponentName 连接 MusicService
                    new ComponentName(context, PlayService.class),
                    // 创建callback
                    mediaBrowserConnectionCallback,
                    //
                    null);
            // 链接service
            mediaBrowserCompat.connect();
        }
    }

    private final MediaBrowserCompat.ConnectionCallback mediaBrowserConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {
        // 连接成功
        @Override
        public void onConnected() {
            try {
                // 获取MediaControllerCompat
                mediaController = new MediaControllerCompat(
                        context,
                        mediaBrowserCompat.getSessionToken());
                MediaControllerCompat.setMediaController(activity, mediaController);
                mediaController.registerCallback(mediaControllerCompatCallback);
                //设置当前数据
                mediaControllerCompatCallback.onMetadataChanged(mediaController.getMetadata());
                mediaControllerCompatCallback.onPlaybackStateChanged(mediaController.getPlaybackState());
                String mediaId = AppConstant.MediaIdInfo.MEDIA_ID_NORMAL;
                mediaBrowserCompat.unsubscribe(mediaId);
                mediaBrowserCompat.subscribe(mediaId, mediaBrowserSubscriptionCallback);

                BaseActivity.mediaController = mediaController;
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private final MediaBrowserCompat.SubscriptionCallback mediaBrowserSubscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);

        }
    };

    private final MediaControllerCompat.Callback mediaControllerCompatCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_NONE: // 默认状态
                case PlaybackStateCompat.STATE_PAUSED:
                    PlayBtn.setImageResource(R.drawable.ic_play);
                    break;
                case PlaybackStateCompat.STATE_PLAYING:
                    PlayBtn.setImageResource(R.drawable.ic_pause);
                    break;
                case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
                case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
                case PlaybackStateCompat.STATE_BUFFERING:
                case PlaybackStateCompat.STATE_CONNECTING:
                case PlaybackStateCompat.STATE_ERROR:
                case PlaybackStateCompat.STATE_FAST_FORWARDING:
                case PlaybackStateCompat.STATE_REWINDING:
                case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM:
                case PlaybackStateCompat.STATE_STOPPED:
                    break;
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata != null) {
                prefDataSource.listPosition().put(listPosition);
                PlayingTitle.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
                PlayingArtist.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
                PlayingCover.setImageBitmap(metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART));
            }
        }
    };

    @Click(R.id.music_control_panel)
    void clickPlayPanel() {
        ActivityNowPlay_.intent(context).start();
    }

    @Click(R.id.playing_play)
    void clickPlayButton() {
        if (playList != null && playList.size() != 0) {
            if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                mediaController.getTransportControls().pause();
            } else if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED) {
                mediaController.getTransportControls().play();
            } else {
                mediaController.getTransportControls().playFromUri(playList.get(listPosition).getUri(), null);
            }
        }
    }

    @Click(R.id.playing_list)
    void clickPlayList() {
        playListRecyclerAdapter.notifyDataSetChanged();
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogPlayList);
        // 自定义布局
        @SuppressLint("InflateParams")
        View playListView = LayoutInflater.from(context).inflate(R.layout.play_list, null);
        // 设置AlertDialog参数，加载自定义布局
        builder.setView(playListView);
        // AlertDialog对象
        AlertDialog alertDialogMusicList = builder.create();
        // 自定义布局RecyclerLayout适配实现
        RecyclerView playListRecycler = playListView.findViewById(R.id.play_list);
        LinearLayoutManager playListRecyclerLayoutManager = new LinearLayoutManager(context);
        playListRecycler.setLayoutManager(playListRecyclerLayoutManager);
        playListRecyclerAdapter.setOnItemClickListener((view, position) -> {
            listPosition = --position;
            mediaController.getTransportControls().skipToNext();
        });
        playListRecycler.setAdapter(playListRecyclerAdapter);
        // 关闭按钮
        Button buttonClose = playListView.findViewById(R.id.play_list_close);
        buttonClose.setOnClickListener((view) -> alertDialogMusicList.dismiss());
        // 显示
        alertDialogMusicList.show();
        // 获取屏幕
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // 获取列表dialog
        Window windowDialog = alertDialogMusicList.getWindow();
        assert windowDialog != null;
        //去掉dialog默认的padding
        windowDialog.getDecorView().setPadding(0, 0, 0, 0);
        windowDialog.getDecorView().setBackgroundColor(context.getResources().getColor(R.color.colorControlPanel));
        // 设置大小
        WindowManager.LayoutParams layoutParams = windowDialog.getAttributes();
        layoutParams.width = displayMetrics.widthPixels;
//        layoutParams.height = (int)(displayMetrics.heightPixels * 0.6);
        // 设置位置为底部
        layoutParams.gravity = Gravity.BOTTOM;
        windowDialog.setAttributes(layoutParams);
    }

    // 添加下一首播放
    public void addToNext(MusicInfo itemMusicInfo) {
        PlayList playListMusicInfo;
        long id = itemMusicInfo.getId();
        String title = itemMusicInfo.getTitle();
        String album = itemMusicInfo.getAlbum();
        String artist = itemMusicInfo.getArtist();
        long duration = itemMusicInfo.getDuration();
        Uri uri = itemMusicInfo.getUri();
        long albumId = itemMusicInfo.getAlbumId();
        if (playList.size() != 0) {
            int playListPosition;
            for (playListPosition = 0; playListPosition < playList.size(); playListPosition++) {
                playListMusicInfo = playList.get(playListPosition);
                if (playListMusicInfo.id == id) {
                    playList.add(listPosition + 1, new PlayList(id, title, album, artist, duration, uri, "Local", MediaUtil.albumIdToUrl(albumId)));
                    playListRecyclerAdapter.notifyItemInserted(listPosition + 1);
                    playList.remove(playListPosition);
                    playListRecyclerAdapter.notifyItemRemoved(playListPosition);
                    if (playListPosition < listPosition) {
                        playListRecyclerAdapter.notifyItemRangeChanged(playListPosition, playList.size() - playListPosition);
                        --listPosition;
                    } else {
                        playListRecyclerAdapter.notifyItemRangeChanged(listPosition, playList.size() - listPosition);
                    }
                    break;
                }
            }
            if (playListPosition == playList.size()) {
                playList.add(listPosition + 1, new PlayList(id, title, album, artist, duration, uri, "Local", MediaUtil.albumIdToUrl(albumId)));
                playListRecyclerAdapter.notifyItemInserted(listPosition + 1);
                playListRecyclerAdapter.notifyItemRangeChanged(listPosition + 1, playList.size() - listPosition + 1);
            }
        } else {
            //　播放列表为空的情况（直接播放）
            playList.add(0, new PlayList(id, title, album, artist, duration, uri, "Local", MediaUtil.albumIdToUrl(albumId)));
        }
        DataRepository_.getInstance_(context).updatePlayList(playList);
    }
}
