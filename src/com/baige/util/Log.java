package com.baige.util;

public final class Log {

	public static void v(String tag, String msg) {
		System.out.println(Tools.formatTime(System.currentTimeMillis())+ " "+tag + "  " + msg);
	}
	public static void d(String tag, String msg) {
		System.out.println(Tools.formatTime(System.currentTimeMillis())+ " "+tag + "  " + msg);
	}
	public static void i(String tag, String msg) {
		System.out.println(Tools.formatTime(System.currentTimeMillis())+ " "+tag + "  " + msg);
	}
	public static void w(String tag, String msg) {
		System.err.println(Tools.formatTime(System.currentTimeMillis())+ " "+tag + "  " + msg);
	}
	public static void e(String tag, String msg) {
		System.err.println(Tools.formatTime(System.currentTimeMillis())+ " "+tag + "  " + msg);
	}
}
