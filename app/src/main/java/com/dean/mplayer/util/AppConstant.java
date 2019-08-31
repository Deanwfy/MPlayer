package com.dean.mplayer.util;

public class AppConstant {
	public class PlayAction {
		public static final String ACTION_PLAY = "PLAY";	// 播放
		public static final String ACTION_PAUSE = "PAUSE";	// 暂停
		public static final String ACTION_STOP = "STOP";	// 停止
		public static final String ACTION_CONTINUE = "CONTINUE";	// 继续
		public static final String ACTION_PREVIOUS = "PREVIOUS";	// 上一首
		public static final String ACTION_NEXT = "NEXT";	// 下一首
		public static final String PROGRESS_CHANGE = "PROGRESS_CHANGE";	// 进度改变
		public static final String IS_PLAYING = "IS_PLAYING";	// 正在播放
	}

	public class PlayMode {
		public static final String MODE_ORDER = "ORDER";	//顺序播放
		public static final String MODE_LOOP = "LOOP";	//列表循环
		public static final String MODE_SINGLE = "SINGLE";	//单曲循环
		public static final String MODE_RANDOM = "RANDOM";	//随机播放
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

	public class Permission {
		public static final int PERMISSION_READ_WRITE_EXTERNAL_STORAGE = 1;
	}
}
