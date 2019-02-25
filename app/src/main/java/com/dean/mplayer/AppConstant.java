package com.dean.mplayer;

public class AppConstant {
	public class PlayAction {
		public static final String ACTION_PLAY = "ic_play";	// 播放
		public static final String ACTION_PAUSE = "pause";	// 暂停
		public static final String ACTION_STOP = "stop";	// 停止
		public static final String ACTION_CONTINUE = "continue";	// 继续
		public static final String ACTION_PRIVIOUS = "previous";	// 上一首
		public static final String ACTION_NEXT = "next";	// 下一首
		public static final String PROGRESS_CHANGE = "progress_change";	// 进度改变
		public static final String ISPLAYING = "isplaying";	// 正在播放
	}

	public class MediaIdInfo {
		public static final String MEDIA_ID_EMPTY_ROOT = "__EMPTY_ROOT__";
		public static final String MEDIA_ID_ROOT = "__ROOT__";//初次连接
		public static final String MEDIA_ID_NORMAL = "__LOCAL_NORMAL__";//所有的本地音乐
		public static final String MEDIA_ID_ALBUM = "__LOCAL_ALBUM__";//音乐专辑
		public static final String MEDIA_ID_ALBUM_DETAIL = "__LOCAL_ALBUM_DETAIL__";//具体某个专辑的歌曲
		public static final String MEDIA_ID_ARTIST = "__LOCAL_ARTIST__";
		public static final String MEDIA_ID_ARTIST_DETAIL = "__LOCAL_ARTIST_DETAIL__";
	}
}
