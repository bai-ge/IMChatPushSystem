package com.baige.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

/**
 * Created by baige on 2017/12/27.
 */

public class Tools {

	public final static String DEFAULT_ENCODE = "UTF-8";

	public static String MD5(String s) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] bytes = md.digest(s.getBytes("utf-8"));
			return toHex(bytes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	

	public static String ramdom() {
		int number = (int) (Math.random() * 900 + 100);
		return System.currentTimeMillis() + "_" + number;
	}

	public static String toHex(byte[] bytes) {

		final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
		StringBuilder ret = new StringBuilder(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++) {
			ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
			ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
		}
		return ret.toString();
	}

	public static byte[] toByte(long data) {
		byte[] buf = new byte[Long.BYTES];
		for (int i = 0; i < buf.length; i++) {
			buf[i] = (byte) ((data >> (i * 8)) & 0xff);
		}
		return buf;
	}

	public static byte[] toByte(int data) {
		byte[] buf = new byte[Integer.BYTES];
		for (int i = 0; i < buf.length; i++) {
			buf[i] = (byte) ((data >> (i * 8)) & 0xff);
		}
		return buf;
	}

	public static long toLong(byte buf[]) {
		long data = 0x00;
		for (int i = buf.length - 1; i >= 0; i--) {
			data <<= 8;
			data |= (buf[i] & 0xff);
		}
		return data;
	}

	public static boolean isEmpty(String s) {
		if (null == s)
			return true;
		if (s.length() == 0)
			return true;
		if (s.trim().length() == 0)
			return true;
		return false;
	}
	
	public static boolean isEquals(Object a, Object b) {
		if (a == null || b == null) {
			return false; // 注意 都为null时还是不相等
		}
		return a.equals(b);
	}

	public static String formatTime(long time) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(new Date(time));
	}

	/* Public Methods */
	public static byte[] stringToData(String string, String charsetName) {
		if (string != null) {
			try {
				return string.getBytes(charsetName);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String dataToString(byte[] data, String charsetName) {
		if (data != null) {
			try {
				return new String(data, charsetName);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String getServerDeviceId() {
		byte[] buf = toByte(System.currentTimeMillis());
		String timeString = Base64.getEncoder().encodeToString(buf);
		return "0" + timeString.substring(0, timeString.length() - 1)
				+ String.format("%03d", Integer.valueOf((int) (Math.random() * 1000)));
	}

	public static String getMobileDeviceId() {
		byte[] buf = toByte(System.currentTimeMillis());
		String timeString = Base64.getEncoder().encodeToString(buf);
		return "1" + timeString.substring(0, timeString.length() - 1)
				+ String.format("%03d", Integer.valueOf((int) (Math.random() * 1000)));
	}

	public static void main(String[] args) {
		String mobile = getMobileDeviceId();
		if (StringValidation.validateRegex(mobile, StringValidation.RegexMobileId)) {
			System.out.println(mobile + "匹配成功");
		}
		String serverId = getServerDeviceId();
		if (StringValidation.validateRegex(serverId, StringValidation.RegexServerId)) {
			System.out.println(serverId + "匹配成功");
		}
		for (int i = 0; i < 10000; i++) {
			mobile = getMobileDeviceId();
			if (!StringValidation.validateRegex(mobile, StringValidation.RegexMobileId)) {
				System.out.println(mobile + "匹配失败");
			}
			serverId = getServerDeviceId();
			if (!StringValidation.validateRegex(serverId, StringValidation.RegexServerId)) {
				System.out.println(serverId + "匹配失败");
			}
		}
		System.out.println(mobile);
		System.out.println(serverId);

		String RegexClientCmd = "^client\b-l$";
		String[] command = new String[] { "udp -t", "udp  -", "udp", "udp -t 192.168.12.4 654",
				"udp -t 192.168.12.4:654" };
		String RegexUdpCmd = "(udp\\s-t\\s(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)[:\\s](6553[0-5]|655[0-2][0-9]|65[0-4][0-9]{2}|6[0-4][0-9]{3}|[1-5][0-9]{4}|[1-9][0-9]{0,3}))|(udp\\s-t)";
		for (int i = 0; i < command.length; i++) {
			if (command[i].matches(RegexUdpCmd)) {
				System.out.println(command[i]);
			}
		}
	}
}
