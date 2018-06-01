package com.baige.data.source.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;


import com.baige.ApplicationConfig;
import com.baige.common.Parm;

import com.baige.data.entity.Candidate;
import com.baige.util.FileUtil;
import com.baige.util.LogHelper;
import com.baige.util.Tools;

public class CacheRepository {

	private final static String TAG = CacheRepository.class.getCanonicalName();

	private static CacheRepository INSTANCE = null;

	private String deviceId;

	private String tcpPort;

	private String udpPort;

	private Map<String, Candidate> candidateMap;

	// type cmd 、id、 ip、 port
	private Map<String, List<String>> keyworkMap;

	private CacheRepository() {
		readConfig();
		candidateMap = Collections.synchronizedMap(new LinkedHashMap<String, Candidate>());
		put("cmd", "cls");
		put("cmd", "time");
		put("cmd", "err");
		put("cmd", "-usage");
		put("cmd", "client");
		put("cmd", "udp");
		put("cmd", "ptp");
		put("ip", ApplicationConfig.mainServerIp);
		put("ip", ApplicationConfig.secondaryServerIp);
		put("port", "12056");
		put("port", "12059");
	}

	public static CacheRepository getInstance() {
		if (INSTANCE == null) {
			synchronized (CacheRepository.class) {
				if (INSTANCE == null) {
					INSTANCE = new CacheRepository();
				}
			}
		}
		return INSTANCE;
	}

	public ArrayList<Candidate> getCandidates() {
		if (candidateMap != null && candidateMap.size() > 0) {
			return new ArrayList<>(candidateMap.values());
		}
		return null;
	}

	public void put(String type, String key) {
		if (Tools.isEmpty(type) || Tools.isEmpty(key)) {
			return;
		}
		if (keyworkMap == null) {
			keyworkMap = new LinkedHashMap<>();
		}
		ArrayList<String> list = (ArrayList<String>) keyworkMap.get(type);
		if (list == null) {
			list = new ArrayList<>();
			keyworkMap.put(type, list);
		}
		if (!list.contains(key)) {
			list.add(key);
		}
	}

	public List<String> startWith(String type, String text) {
		ArrayList<String> list = new ArrayList<>();
		if(keyworkMap == null){
			return list;
		}
		if (Tools.isEmpty(type) || Tools.isEmpty(text)) {
			return list;
		}
		ArrayList<String> keys = (ArrayList<String>) keyworkMap.get(type);
		if (keys != null) {
			for (String key : keys) {
				if (key.startsWith(text)) {
					list.add(key);
				}
			}
		}
		return list;
	}

	public List<String> startWith(String text) {
		ArrayList<String> list = new ArrayList<>();
		if(keyworkMap == null){
			return list;
		}
		if (Tools.isEmpty(text)) {
			return list;
		}
		Iterator<Map.Entry<String, List<String>>> iterator = keyworkMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, List<String>> entity = iterator.next();
			ArrayList<String> keys = (ArrayList<String>) entity.getValue();
			for (String key : keys) {
				if (key.startsWith(text)) {
					list.add(key);
				}
			}
		}
		return list;
	}

	public Candidate add(Candidate candidate) {
		if (candidate != null) {
			candidateMap.put(candidate.getFrom(), candidate);
		}
		return candidate;
	}

	public String getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(String tcpPort) {
		this.tcpPort = tcpPort;
	}

	public String getUdpPort() {
		return udpPort;
	}

	public void setUdpPort(String udpPort) {
		this.udpPort = udpPort;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		saveConfig();
	}

	public void readConfig() {
		Properties prop = new Properties();
		File file = new File(FileUtil.getIniDir());
		if (!file.exists()) {
			file.mkdirs();
		}
		File propFile = new File(file.getAbsoluteFile() + "//prop.ini");
		if (!propFile.exists()) {
			try {
				propFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setDeviceId(UUID.randomUUID().toString());
		} else {
			BufferedInputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(propFile));
				prop.load(in);
				setDeviceId(prop.getProperty(Parm.DEVICE_ID));
				LogHelper logHelper = LogHelper.getInstance();
				logHelper.debug(this, "读取device_id =" + getDeviceId());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		if (Tools.isEmpty(getDeviceId())) {
			setDeviceId(Tools.getServerDeviceId());
			saveConfig();
		}
	}

	public void saveConfig() {
		Properties prop = new Properties();
		File file = new File(FileUtil.getIniDir());
		if (!file.exists()) {
			file.mkdirs();
		}
		File propFile = new File(file.getAbsoluteFile() + "//prop.ini");
		if (!propFile.exists()) {
			try {
				propFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		BufferedOutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(propFile));
			prop.put(Parm.DEVICE_ID, getDeviceId());
			prop.store(out, "modify");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
