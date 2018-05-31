package com.baige.util;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;



public class LogHelper {

	private static LogHelper INSTANCE;
	private boolean isShow;
	private LogLevel level;
	private LogListener listener;
	private int MaxLogLength = 1000;
	
	private List<LogBean> logs = new LinkedList<>();
	public final static SimpleDateFormat dateFromat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private LogHelper() {
		init();
	}

	public void init() {
		isShow = true;
		level = LogLevel.DEBUG;
	}
	
	
	public boolean isShow() {
		return isShow;
	}

	public void setShow(boolean isShow) {
		this.isShow = isShow;
	}

	public LogLevel getLevel() {
		return level;
	}

	public void setLevel(LogLevel level) {
		this.level = level;
	}

	public void setLogListener(LogListener listener){
		this.listener = listener;
	}

	public static LogHelper getInstance() {
		if (INSTANCE == null) {
			synchronized (LogHelper.class) {
				if (INSTANCE == null) {
					INSTANCE = new LogHelper();
				}
			}
		}
		return INSTANCE;
	}

	public void verbose(String msg) {
		if (level.compareTo(LogLevel.VERBOSE) <= 0) {
			if (isShow) {
				System.out.println(msg);
			}
		}
	}

	public void debug(String msg) {
		if (level.compareTo(LogLevel.DEBUG) <= 0) {
			if (isShow) {
				System.out.println(msg);
			}
		}
	}

	public void info(String msg) {
		if (level.compareTo(LogLevel.INFO) <= 0) {
			if (isShow) {
				System.out.println(msg);
			}
		}
	}

	public void warn(String msg) {
		if (level.compareTo(LogLevel.WARN) <= 0) {
			if (isShow) {
				System.out.println(msg);
			}
		}
	}

	public void error(String msg) {
		if (level.compareTo(LogLevel.ERROR) <= 0) {
			if (isShow) {
				System.err.println(msg);
			}
		}
	}
	
	//************************************升级版*****************************
	public void verbose(Object from, String msg) {
		LogBean log = new LogBean(from, msg, LogLevel.VERBOSE);
		logs.add(log);
		if(logs.size() > MaxLogLength){
			logs.remove(0);
		}
		if(listener != null){
			listener.showLog(log);
		}
	}

	public void debug(Object from, String msg) {
		LogBean log = new LogBean(from, msg, LogLevel.DEBUG);
		logs.add(log);
		if(logs.size() > MaxLogLength){
			logs.remove(0);
		}
		if(listener != null){
			listener.showLog(log);
		}
	}

	public void info(Object from, String msg) {
		LogBean log = new LogBean(from, msg, LogLevel.INFO);
		logs.add(log);
		if(logs.size() > MaxLogLength){
			logs.remove(0);
		}
		if(listener != null){
			listener.showLog(log);
		}
	}

	public void warn(Object from, String msg) {
		LogBean log = new LogBean(from, msg, LogLevel.WARN);
		logs.add(log);
		if(logs.size() > MaxLogLength){
			logs.remove(0);
		}
		if(listener != null){
			listener.showLog(log);
		}
	}

	public void error(Object from, String msg) {
		LogBean log = new LogBean(from, msg, LogLevel.ERROR);
		logs.add(log);
		if(logs.size() > MaxLogLength){
			logs.remove(0);
		}
		if(listener != null){
			listener.showLog(log);
		}
	}

	public List<LogBean> getLogs() {
		return logs;
	}

	public void setLogs(LinkedList<LogBean> logs) {
		this.logs = logs;
	}

	@Override
	public String toString() {
		return "{isShow="+isShow+", level="+level+",logsSize="+logs.size()+"}";
	}
	public interface LogListener{
		void showLog(LogBean log);
	}
}
