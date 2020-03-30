package com.dean.mplayer.util;

import android.util.Log;

public class LogUtils {

	private LogUtils() {
	}

	private static final String TAG = "MPlayer";

	public static void v(String msg) {
		outputLog(msg);
	}

	public static void e(String msg, Exception e) {
		outputLog(Log.ERROR, msg, e);
	}

	public static void w(String msg) {
		outputLog(Log.WARN, msg);
	}

	public static <T> void trace(T instance) {
		v(instance.getClass().getSimpleName() + ": " + Thread.currentThread().getStackTrace()[3].getMethodName());
	}

	private static void outputLog(String msg) {
		outputLog(Log.DEBUG, msg, null);
	}

	private static void outputLog(int type, String msg) {
		outputLog(type, msg, null);
	}

	private static void outputLog(int type, String msg, Exception e) {
		switch(type) {
			case Log.ERROR:
				Log.e(TAG, msg, e);
				break;
			case Log.WARN:
				Log.w(TAG, msg);
				break;
			case Log.DEBUG:
				Log.d(TAG, msg);
				break;
			case Log.INFO:
				Log.i(TAG, msg);
				break;
			case Log.VERBOSE:
				Log.v(TAG, msg);
				break;
			case Log.ASSERT:
			default:
				break;
		}
	}
}
