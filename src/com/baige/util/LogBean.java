package com.baige.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogBean {
	private long time;
	private LogLevel level;
	private String from;
	private String text;
	
	public final static SimpleDateFormat dateFromat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public LogBean(Object from, String text, LogLevel level) {
		this.time = System.currentTimeMillis();
		this.level = level;
		this.from = from.getClass().getName();
		this.text = text;
	}
	public LogBean(Object from, String text) {
		this(from, text, LogLevel.DEBUG);
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public LogLevel getLevel() {
		return level;
	}
	public void setLevel(LogLevel level) {
		this.level = level;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return dateFromat.format(new Date(time))+"\t["+level+"]:"+from+"~$"+text;
	}
	

}
