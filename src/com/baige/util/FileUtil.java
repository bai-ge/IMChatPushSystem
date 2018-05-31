package com.baige.util;

public class FileUtil {

	public final static String JSON_DIR = "\\res\\json\\";
	public final static String IMG_DIR = "\\res\\images\\";
	public final static String LOG_DIR = "\\res\\log\\";
	public final static String INI_DIR = "\\res\\";

	public static String getJsonDir() {
		return System.getProperty("user.dir")+JSON_DIR;
	}
	public static String getImgDir(){
		return System.getProperty("user.dir")+IMG_DIR;
	}
	public static String getLogDir(){
		return System.getProperty("user.dir")+LOG_DIR;
	}
	public static String getIniDir(){
		return System.getProperty("user.home")+INI_DIR;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(getJsonDir());
		System.out.println(getImgDir());
		System.out.println(getLogDir());
	}

}
