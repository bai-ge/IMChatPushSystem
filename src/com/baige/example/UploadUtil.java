package com.baige.example;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import com.baige.util.Log;
import com.baige.util.Tools;

public class UploadUtil {
	private final static String TAG = UploadUtil.class.getSimpleName();
	private static final int TIME_OUT = 10 * 1000;// 超时时间
	private static final String CHARSET = "utf-8";// 设置编码

	public static final long TIME_SIZE_SECOND = 1000;
    public static final long TIME_SIZE_MIN =  60 * 1000;
    public static final long TIME_SIZE_HOUR = 60 * 60 * 1000;
    public static final long TIME_SIZE_DAY = 24 * TIME_SIZE_HOUR;
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		File file = new File(UploadUtil.class.getResource("/res/12.png").getFile());
//		String url = "http://localhost:8080/imchat/file/upload.action";
//		uploadFile(file, url, "upload");
//		System.out.println(Tools.formatTime(1517241900000L));
//		System.out.println(Tools.formatTime(1526274592760L));
//		getMonthFirstDay();
//		getNextMonthFirstDay();
//		testTime();
		long t = System.currentTimeMillis();
		for (int i = 0; i < 10 ; i ++) {
			System.out.println(getSuitableTimeFormat(t + i * TIME_SIZE_DAY));
			System.out.println(getSuitableTimeFormat(t - i * TIME_SIZE_DAY));
		}
	}
	
    public static boolean isSameDay(long time1, long time2) {
        return isSameDay(new Date(time1), new Date(time2));
     }
     public static boolean isSameDay(Date date1, Date date2) {
         if(date1 != null && date2 != null) {
             Calendar cal1 = Calendar.getInstance();
             cal1.setTime(date1);
             Calendar cal2 = Calendar.getInstance();
             cal2.setTime(date2);
             return isSameDay(cal1, cal2);
         } else {
             throw new IllegalArgumentException("The date must not be null");
         }
     }

     public static boolean isSameDay(Calendar cal1, Calendar cal2) {
         if(cal1 != null && cal2 != null) {
             return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
         } else {
             throw new IllegalArgumentException("The date must not be null");
         }
     }

     public static long getZeroDataTime(long time){
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
         String dateString = dateFormat.format(new Date(time));
         try {
             time = dateFormat.parse(dateString).getTime();
         } catch (ParseException e) {
             e.printStackTrace();
         }
         return time;
     }

     //TODO 未完善
     public static String getSuitableTimeFormat(long time){
         SimpleDateFormat dateFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm");
         long now = System.currentTimeMillis();

         if(isSameDay(now, time)){
             dateFormat = new SimpleDateFormat("HH:mm");
         }else if(isSameDay(now - TIME_SIZE_DAY, time)){
             dateFormat = new SimpleDateFormat("昨天 HH:mm");
         }else if(isSameDay(now - TIME_SIZE_DAY * 2, time)){
             dateFormat = new SimpleDateFormat("前天 HH:mm");
         }else if(isSameDay(now + TIME_SIZE_DAY, time)){
             dateFormat = new SimpleDateFormat("明天 HH:mm");
         }else if(isSameDay(now + TIME_SIZE_DAY * 2, time)){
             dateFormat = new SimpleDateFormat("后天 HH:mm");
         }

         return dateFormat.format(new Date(time));
     }
	public static void testTime() {
		String startTimeStr = "05/30/2018";
		String endTimeStr = "06/02/2018";
		System.out.println("startTimeStr:" + startTimeStr);
        System.out.println("endTimeStr:" + endTimeStr);

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Date startDate = (Date) dateFormat.parse(startTimeStr);
            Date endDate = (Date) dateFormat.parse(endTimeStr);
            // 验证date
             System.out.println("startDate:"+startDate.toString());
             System.out.println("endDate:"+endDate.toString());
            // 验证日期
             SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
             System.out.println("startDate format:" + dateFormat2.format(startDate));
             System.out.println("endDate format:"+dateFormat2.format(endDate));
            // 毫秒数
             System.out.println("startDate:"+startDate.getTime());
             System.out.println("endDate:"+endDate.getTime());

          
        } catch (ParseException e) {
            System.out.println("date error:");
            e.printStackTrace();
        }

	}
	public static long getMonthFirstDay(){
		Calendar calendar = Calendar.getInstance();
//		calendar.setTimeInMillis();
		calendar.set(Calendar.DATE, 0);
//		calendar.setLenient(true);
		calendar.set(Calendar.HOUR, 12);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		long time = calendar.getTimeInMillis();
		System.out.println(Tools.formatTime(time));
		return time;
	}

	public static long getNextMonthFirstDay(){
		Calendar calendar = Calendar.getInstance();
//		calendar.setTimeInMillis();
		calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
		calendar.set(Calendar.DATE, 0);
//		calendar.setLenient(true);
		calendar.set(Calendar.HOUR, 12);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		long time = calendar.getTimeInMillis();
		System.out.println(Tools.formatTime(time));
		return time;
	}
	/**
	 * Android上传文件到服务端
	 *
	 * @param file
	 *            需要上传的文件
	 * @param RequestURL
	 *            请求的rul
	 * @return 返回响应的内容
	 */
	public static String uploadFile(File file, String RequestURL, String key) {
		Log.d(TAG, "文件大小：" + file.length());
		String result = null;
		String BOUNDARY = UUID.randomUUID().toString();// 边界标识
																		// 随机生成
		String PREFIX = "--";
		String LINE_END = "\r\n";
		String CONTENT_TYPE = "multipart/form-data";// 内容类型
		try {
			StringBuffer sb = new StringBuffer();
			sb.append(PREFIX);
			sb.append(BOUNDARY);
			sb.append(LINE_END);
			/**
			 * 这里重点注意： name里面的值为服务端需要key 只有这个key 才可以得到对应的文件
			 * filename是文件的名字，包含后缀名的 比如:abc.png
			 */
			sb.append("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + file.getName() + "\""
					+ LINE_END);
			sb.append("Content-Type: image/png" + LINE_END);
			sb.append(LINE_END);
			StringBuffer endData = new StringBuffer();
			endData.append(LINE_END + PREFIX + BOUNDARY + PREFIX + LINE_END);
			Log.d(TAG, sb.toString());
			long leng = file.length() + sb.length() + endData.length();
			Log.d(TAG, "数据大小：" + leng);
			URL url = new URL(RequestURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(TIME_OUT);
			conn.setConnectTimeout(TIME_OUT);
			conn.setDoInput(true);// 允许输入流
			conn.setDoOutput(true);// 允许输出流
			conn.setUseCaches(true);// 不允许使用缓存
			conn.setRequestMethod("POST");// 请求方式

			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0");
			conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
			conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
			conn.setRequestProperty("Charset", CHARSET);// 设置编码
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Referer", "http://localhost:8080/imchat/fileupload.jsp");
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + "; boundary=" + BOUNDARY);
			//conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=---------------------------4332735213595");
			conn.setRequestProperty("Content-Length", String.valueOf(leng));
			// conn.setRequestProperty("Cookie",
			// "JSESSIONID=4DBE1F5261FB58F96F44B197CE197B0C");
			conn.setRequestProperty("Upgrade-Insecure-Requests", String.valueOf(1));

			if (file != null) {
				/**
				 * 当文件不为空，把文件包装并且上传
				 */
				DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

				dos.write(sb.toString().getBytes());
				InputStream is = new FileInputStream(file);
				byte[] bytes = new byte[1024];
				int len = 0;
				while ((len = is.read(bytes)) != -1) {
					dos.write(bytes, 0, len);
				}
				is.close();
				// dos.write(LINE_END.getBytes());
				// byte[] end_data = (LINE_END + PREFIX + BOUNDARY + PREFIX +
				// LINE_END).getBytes();
				dos.write(endData.toString().getBytes());
				dos.flush();
				/**
				 * 获取响应码 200=成功 当响应成功，获取响应的流
				 */
				int res = conn.getResponseCode();
				Log.e(TAG, "response code:" + res);
				// if(res==200)
				// {
				Log.e(TAG, "request success");
				InputStream input = conn.getInputStream();
				StringBuffer sb1 = new StringBuffer();
				int ss;
				while ((ss = input.read()) != -1) {
					sb1.append((char) ss);
				}
				result = sb1.toString();
				Log.e(TAG, "result : " + result);
				// }
				// else{
				// Log.e(TAG, "request error");
				// }
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
