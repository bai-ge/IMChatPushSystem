package com.baige.connect.msg;

import com.baige.common.Parm;
import com.baige.connect.BaseConnector;
import com.baige.connect.ConnectedByTCP;
import com.baige.connect.NetServerManager;
import com.baige.connect.SocketPacket;
import com.baige.data.source.cache.CacheRepository;
import com.baige.linux.CommonProcess;
import com.baige.util.LogHelper;
import com.baige.util.Tools;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class PushHelper {

	public final static String localhost = "127.0.0.1";
	public final static int port = 12056;

	public static boolean push(String json) {
		Socket socket = new Socket();
		SocketAddress socketAddress = new InetSocketAddress(localhost, port);
		try {
			socket.connect(socketAddress, 3000);
			SocketPacket socketPacket = new SocketPacket(Tools.stringToData(json, Tools.DEFAULT_ENCODE), false);
			socketPacket.packet();
			socket.getOutputStream().write(socketPacket.getAllBuf());
			System.out.println("发送信息：" + json);
			socket.getOutputStream().flush();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean push(String from, String to, Object msg, String key) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(Parm.FROM, from);
			jsonObject.put(Parm.TO, to);
			jsonObject.put(key, msg);
			return push(jsonObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void receivePush(BaseConnector connector, SocketPacket packet) {
		if (connector == null || packet == null) {
			return;
		}
		if (packet.isHeartBeat() || packet.isDisconnected()) {
			return;
		}
		NetServerManager netServerManager = NetServerManager.getInstance();
		BaseConnector baseConnector = null;
		if (packet.getHeaderBuf() != null) { // 语音信息或文件信息
			try {
				String msg = Tools.dataToString(packet.getHeaderBuf(), Tools.DEFAULT_ENCODE);
				if (!Tools.isEmpty(msg)) {
					JSONObject json = new JSONObject(msg);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else if (packet.getContentBuf() != null) {
			try {
				String msg = Tools.dataToString(packet.getContentBuf(), Tools.DEFAULT_ENCODE);
				LogHelper.getInstance().debug(PushHelper.class, msg);
				if (!Tools.isEmpty(msg)) {
					JSONObject json = new JSONObject(msg);
					if(json.has(Parm.FROM) && json.has(Parm.TO)){
						String to = json.getString(Parm.TO);
						if(CacheRepository.getInstance().getDeviceId().equals(to)){
							return; //自己
						}
						baseConnector = netServerManager.getTCPConnectorById(json.getString(Parm.TO));
						if(baseConnector != null){
							baseConnector.sendPacket(packet);
							if(connector instanceof ConnectedByTCP){
								((ConnectedByTCP) connector).write(200);
							}
						}else{
							if(connector instanceof ConnectedByTCP){
								((ConnectedByTCP) connector).write(500);
							}
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}
}
