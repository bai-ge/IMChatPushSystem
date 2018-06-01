package com.baige.connect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.baige.ApplicationConfig;
import com.baige.common.Parm;
import com.baige.connect.msg.*;
import com.baige.linux.CommonProxy;
import org.json.JSONException;
import org.json.JSONObject;

import com.baige.data.entity.Candidate;
import com.baige.data.entity.DeviceModel;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.IPUtil;
import com.baige.util.LogHelper;
import com.baige.util.Tools;

public class NetServerManager {
	private final static String TAG = NetServerManager.class.getCanonicalName();

	private final NetServerManager self;

	private static NetServerManager INSTANCE = null;

	private DatagramSocketServer datagramSocketServer;

	private SocketServer socketServer;

	private Map<String, DeviceModel> devicesMap;

	private LogHelper logHelper;

	private CacheRepository cacheRepository;

	private CommonProxy commonProxy;

	private NetServerManager() {
		self = this;

		cacheRepository = CacheRepository.getInstance();

		logHelper = LogHelper.getInstance();

		List<String> ipList = IPUtil.getAllLocalIPAddress(true);
		if (ipList != null && ipList.size() > 0) {
			logHelper.debug(this, "本地IP");
			for (String ip : ipList) {
				logHelper.debug(this, ip);
			}
		}

		devicesMap = Collections.synchronizedMap(new LinkedHashMap<String, DeviceModel>());

		socketServer = new SocketServer();
		socketServer.registerSocketServerListener(mOnSocketServerListener);

		datagramSocketServer = new DatagramSocketServer();
		datagramSocketServer.registerServerListener(mOnDatagramSocketServerListener);

		socketServer.beginListen();
		if (!socketServer.isListening()) {
			logHelper.debug(self, "TCP 监听端口失败：" + socketServer.getPort());
		}
		datagramSocketServer.start();

		commonProxy = new CommonProxy();
		commonProxy.beginListen();

		cacheRepository.setTcpPort(socketServer.getPort() + "");
		cacheRepository.setUdpPort(datagramSocketServer.getLocalPort() + "");
	}

	public static NetServerManager getInstance() {
		if (INSTANCE == null) {
			synchronized (NetServerManager.class) {
				if (INSTANCE == null) {
					INSTANCE = new NetServerManager();
				}
			}
		}
		return INSTANCE;
	}

	public List<DeviceModel> getAllDevices() {
		if (devicesMap != null) {
			return new ArrayList<>(devicesMap.values());
		}
		return null;
	}

	private OnSocketServerListener mOnSocketServerListener = new OnSocketServerListener() {

		@Override
		public void onServerBeginListen(SocketServer socketServer, int port) {
			// TODO Auto-generated method stub
			logHelper.debug(self, "TCP 监听端口：" + port);
		}

		@Override
		public void onServerStopListen(SocketServer socketServer, int port) {
			// TODO Auto-generated method stub
			logHelper.debug(self, "TCP 停止监听端口：" + port);
		}

		@Override
		public void onClientConnected(SocketServer socketServer, com.baige.connect.ConnectedByTCP socketServerClient) {
			// TODO Auto-generated method stub
			logHelper.debug(self, "游客连接成功: " + socketServerClient);
			socketServerClient.registerConnectedListener(mBeOnConnectedListener);
			socketServerClient.start();//开启接收线程
			//socketServerClient.registerConnectedListener(mTempConnectedListener);
		}

		@Override
		public void onClientDisconnected(SocketServer socketServer,
				com.baige.connect.ConnectedByTCP socketServerClient) {
			// TODO Auto-generated method stub
		}

	};

	private OnDatagramSocketServerListener mOnDatagramSocketServerListener = new OnDatagramSocketServerListener() {

		@Override
		public void onServerStart(DatagramSocketServer server) {
			// TODO Auto-generated method stub
			logHelper.debug(self, "UDP 监听端口：" + server.getLocalPort());
		}

		@Override
		public void onServerReceivePacket(ConnectedByUDP connector, SocketPacket packet) {
			// TODO Auto-generated method stub
			if (!packet.isHeartBeat() && !packet.isDisconnected()) {
				logHelper.verbose(self, "UDP :" + packet.getID());
				MessageProcess.receive(connector, packet);
			}
		}

		@Override
		public void onServerClose(DatagramSocketServer server) {
			// TODO Auto-generated method stub
			logHelper.debug(self, "UDP 停止监听端口：" + server.getLocalPort());
		}

		@Override
		public void onClientDisconnected(ConnectedByUDP connector) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onClientConnected(ConnectedByUDP connector) {
			// TODO Auto-generated method stub

		}
	};

	// 主动连接的用户登录验证使用的监听器
	private OnConnectedListener mOnConnectedListener = new OnConnectedListener.SimpleOnConnectedListener() {

		public void onConnected(BaseConnector connector) {
			// TODO 发送验证信息
		};

		@Override
		public void onResponse(BaseConnector connector, SocketPacket responsePacket) {
			// TODO Auto-generated method stub
			// if(验证成功) {获取uuid, 添加进设备中}
			connector.unRegisterConnectedListener(mOnConnectedListener);
			connector.registerConnectedListener(mClientConnectedListener);
			logHelper.debug(self, "TCP接收到消息" + responsePacket.toString());
		}
	};
	// 被动连接的用户登录验证使用的监听器
	private OnConnectedListener mBeOnConnectedListener = new OnConnectedListener.SimpleOnConnectedListener() {

		@Override
		public void onResponse(BaseConnector connector, SocketPacket responsePacket) {
			// TODO Auto-generated method stub
			// if(验证成功) {获取uuid, 添加进设备中}
			if (responsePacket != null && !(responsePacket.isHeartBeat() || responsePacket.isDisconnected())
					&& responsePacket.getContentBuf() != null && responsePacket.getContentBuf().length > 0) {
				String text = Tools.dataToString(responsePacket.getContentBuf(), Tools.DEFAULT_ENCODE);
				logHelper.verbose(self, "处理验证信息：" + text);
				try {
					JSONObject jsonObject = new JSONObject(text);
					if (jsonObject.has(Parm.DATA_TYPE)) {
						int type = jsonObject.getInt(Parm.DATA_TYPE);
						if (type == Parm.TYPE_LOGIN && jsonObject.has(Parm.FROM)) {
							DeviceModel deviceModel = null;
							String from = jsonObject.getString(Parm.FROM);
							String conDeviceId = connector.getDeviceId();
							connector.setDeviceId(from);
							if (!Tools.isEmpty(conDeviceId) && !from.equals(conDeviceId)) {
								deviceModel = devicesMap.remove(conDeviceId);
								if (deviceModel != null) {
									if (deviceModel.getConnectedByTCP() != null) {
										deviceModel.getConnectedByTCP().setDeviceId(from);
									}
									if (deviceModel.getConnectedByUDP() != null) {
										deviceModel.getConnectedByUDP().setDeviceId(from);
									}
								}
							}

							if (deviceModel == null) {
								deviceModel = devicesMap.get(from);
							}
							if (deviceModel == null) {
								deviceModel = new DeviceModel();
							}
							deviceModel.setDeviceId(from);
							devicesMap.put(from, deviceModel);

							if (connector instanceof ConnectedByTCP) {
								deviceModel.setConnectedByTCP((ConnectedByTCP) connector);
							} else if (connector instanceof ConnectedByUDP) {
								deviceModel.setConnectedByUDP((ConnectedByUDP) connector);
							}

							if (jsonObject.has(Parm.LOCAL_IP)) {
								deviceModel.setLocalIp(jsonObject.getString(Parm.LOCAL_IP));
							}
							if (jsonObject.has(Parm.LOCAL_PORT)) {
								deviceModel.setLocalPort(jsonObject.getInt(Parm.LOCAL_PORT));
							}
							if (jsonObject.has(Parm.ACCEPT_PORT)) {
								deviceModel.setAcceptPort(jsonObject.getInt(Parm.ACCEPT_PORT));
							}
							if (jsonObject.has(Parm.LOCAL_UDP_PORT)) {
								deviceModel.setLocalUdpPort(jsonObject.getInt(Parm.LOCAL_UDP_PORT));
							}

							deviceModel.setRemoteIp(connector.getAddress().getRemoteIP());
							deviceModel.setRemotePort(connector.getAddress().getRemotePortIntegerValue());

							// 回复登录信息
							ResponseMessage responseMessage = new ResponseMessage();
							JSONObject msg = MessageManager.loginSuccess(cacheRepository.getDeviceId(), from,
									deviceModel.getRemoteIp(), deviceModel.getRemotePort() + "",
									datagramSocketServer.getLocalPort() + "");
							responseMessage.setResponse(msg);
							logHelper.debug(this, msg == null? "null":msg.toString());

							connector.sendString(responseMessage.toJson());
							connector.unRegisterConnectedListener(mBeOnConnectedListener);
							//connector.unRegisterConnectedListener(mTempConnectedListener);
							connector.registerConnectedListener(mClientConnectedListener);
						} else {
							logHelper.debug(self, "登录数据不符" + connector);
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	};

	private OnConnectedListener mTempConnectedListener = new OnConnectedListener.SimpleOnConnectedListener(){
		public void onResponse(BaseConnector connector, SocketPacket responsePacket) {
			
			PushHelper.receivePush(connector, responsePacket);
		}
	};
	// 登录验证之后的用户使用的监听器
	private OnConnectedListener mClientConnectedListener = new OnConnectedListener() {

		@Override
		public void onResponse(BaseConnector connector, SocketPacket responsePacket) {
			// TODO Auto-generated method stub
			logHelper.verbose(self, "TCP:" + responsePacket.toString());
			MessageProcess.receive(connector, responsePacket);
		}

		@Override
		public void onDisconnected(BaseConnector connector) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onConnected(BaseConnector connector) {
			// TODO Auto-generated method stub

		}
	};

	public DeviceModel getDeviceModelById(String deviceid) {
		if (Tools.isEmpty(deviceid)) {
			return null;
		} else {
			return devicesMap.get(deviceid);
		}
	}

	public ConnectedByTCP getTCPConnectorById(String deviceid) {
		if (Tools.isEmpty(deviceid)) {
			return null;
		} else {
			DeviceModel deviceModel = devicesMap.get(deviceid);
			if (deviceModel != null) {
				return deviceModel.getConnectedByTCP();
			}
		}
		return null;
	}

	public ConnectedByUDP getUDPConnectorById(String deviceid) {
		if (Tools.isEmpty(deviceid)) {
			return null;
		} else {
			DeviceModel deviceModel = devicesMap.get(deviceid);
			if (deviceModel != null) {
				return deviceModel.getConnectedByUDP();
			}
		}
		return null;
	}

//	public ConnectedByUDP getRunningConnectorById(String deviceid){
//		if (Tools.isEmpty(deviceid)) {
//			return null;
//		} else {
//			DeviceModel deviceModel = devicesMap.get(deviceid);
//			if (deviceModel != null) {
//				ConnectedByUDP connectedByUDP = deviceModel.getConnectedByUDP();
//				if(connectedByUDP != null){
//					return getUDPConnectorByAddress(connectedByUDP.getAddress().getRemoteIP(), connectedByUDP.getAddress().getRemotePort());
//				}
//				return null;
//			}
//		}
//		return null;
//	}

	public DatagramSocketServer getDatagramSocketServer() {
		return datagramSocketServer;
	}

	public SocketServer getSocketServer() {
		return socketServer;
	}

	public ConnectedByUDP getUDPConnectorByAddress(String ip, int port) {
		ConnectedByUDP connectedByUDP = datagramSocketServer.get(ip + ":" + port);
		if (connectedByUDP == null) {
			connectedByUDP = new ConnectedByUDP(new SocketClientAddress(ip, port));
		}
		connectedByUDP.setRunningSocket(datagramSocketServer);
		return connectedByUDP;
	}

	public ConnectedByUDP getUDPConnectorByAddress(String ip, String port) {
		ConnectedByUDP connectedByUDP = datagramSocketServer.get(ip + ":" + port);
		if (connectedByUDP == null) {
			connectedByUDP = new ConnectedByUDP(new SocketClientAddress(ip, port));
		}
		connectedByUDP.setRunningSocket(datagramSocketServer);
		return connectedByUDP;
	}

	public DeviceModel remove(String deviceId) {
		// TODO Auto-generated method stub
		if (devicesMap != null) {
			return devicesMap.remove(deviceId);
		}
		return null;
	}

	public void put(String from, DeviceModel deviceModel) {
		// TODO Auto-generated method stub
		devicesMap.put(from, deviceModel);
	}

	 public void tryUdpTest() {
	        CacheRepository cacheRepository = CacheRepository.getInstance();
	        String localIp = IPUtil.getLocalIPAddress(true);
	        String localPort = datagramSocketServer.getLocalPort() + "";
	        String msg = MessageManager.udpTest(cacheRepository.getDeviceId(), localIp, localPort);
	        ConnectedByUDP connectedByUDP;
	        
	        connectedByUDP = getUDPConnectorByAddress(ApplicationConfig.mainServerIp, 12059);
	        connectedByUDP.sendString(msg);

	        connectedByUDP = getUDPConnectorByAddress(ApplicationConfig.secondaryServerIp, 12059);
	        connectedByUDP.sendString(msg);
	    }
	    
	    public void tryUdpTest(String msg) {
	    	
	        ConnectedByUDP connectedByUDP;
	        
	        connectedByUDP = getUDPConnectorByAddress(ApplicationConfig.mainServerIp, 12059);
	        connectedByUDP.sendString(msg);

	        connectedByUDP = getUDPConnectorByAddress(ApplicationConfig.secondaryServerIp, 12059);
	        connectedByUDP.sendString(msg);
	    }

	public void tryPTPConnect(List<Candidate> candidates, String deviceId) {
		DeviceModel deviceModel = getDeviceModelById(deviceId);
		if (deviceModel != null && deviceModel.getConnectedByUDP() != null
				&& deviceModel.getConnectedByUDP().isConnected()) {
			return;
		}
		ConnectedByUDP connectedByUDP = null;
		Candidate candidate = null;
		Candidate candidate1;
		Candidate candidate2;
		int port, port1, port2;
		CacheRepository cacheRepository = CacheRepository.getInstance();
		String msg = null;
		JSONObject jsonObject = MessageManager.tryPTPConnect(cacheRepository.getDeviceId(), deviceId);
		if (jsonObject != null) {
			msg = jsonObject.toString();
		}
		// 判断自己的NAT类型
		// 1, 全锥型(Full Cone)
		//
		// 2, 受限锥型(Restricted Cone)， 或者说是IP受限锥型
		//
		// 3, 端口受限锥型(Port Restricted Cone), 或者说是IP + PORT受限锥型
		//
		// 4, 对称型(Symmetric)
		List<Candidate> candidateList = CacheRepository.getInstance().getCandidates();
		if (candidateList == null || candidateList.size() == 0) {
			tryUdpTest();
			return;
		}
		boolean isSymmetric = true;
		boolean isOpSymmetric = true;
		if (candidateList.size() == 1) {
			candidate = candidateList.get(0);
			if (Tools.isEquals(candidate.getLocalPort(), candidate.getRemotePort())) {
				isSymmetric = false;
			}

		} else if (candidateList.size() > 1) {
			candidate1 = candidateList.get(0);
			candidate2 = candidateList.get(1);
			if (Tools.isEquals(candidate1.getRemotePort(), candidate2.getRemotePort())) {
				isSymmetric = false;
			}
		}

		// 判断对方NAT类型
		if (candidates.size() == 1) {
			candidate = candidates.get(0);
			if (Tools.isEquals(candidate.getLocalPort(), candidate.getRemotePort())) {
				isOpSymmetric = false;
			}
			// TODO 当Symmetric 处理
		} else if (candidates.size() > 1) {
			candidate1 = candidates.get(0);
			candidate2 = candidates.get(1);
			if (Tools.isEquals(candidate1.getRemotePort(), candidate2.getRemotePort())) {
				isOpSymmetric = false;
			}
		}
		if (isSymmetric && isOpSymmetric) {
			// 自己和对方都是对称型，不能随便发送数据
			if (candidates.size() == 1 || candidateList.size() == 1) {
				// 假对称型，不确定
				if (candidates.size() == 1) {
					candidate = candidates.get(0);
					port = Integer.valueOf(candidate.getRelayPort());
					// 预测几条
					for (int i = 0; i < 10; i++) {
						port++;
						connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), port);
						connectedByUDP.sendString(msg);
					}
					connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), candidate.getRemotePort());
					connectedByUDP.sendString(msg);

					if (!Tools.isEquals(candidate.getLocalIp(), candidate.getRemoteIp())) {
						connectedByUDP = getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
						connectedByUDP.sendString(msg);
					}
				} else if (candidates.size() > 1) {
					candidate1 = candidates.get(0);
					candidate2 = candidates.get(1);
					port1 = Integer.valueOf(candidate1.getRemotePort());
					port2 = Integer.valueOf(candidate2.getRemotePort());

					if (Math.abs(port1 - port2) == 1) {// 相差1
						port = Math.max(port1, port2);
						// 预测几条
						for (int i = 0; i < 10; i++) {
							port++;
							connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), port);
							connectedByUDP.sendString(msg);
						}

						if (!Tools.isEquals(candidate.getLocalIp(), candidate.getRemoteIp())) {
							connectedByUDP = getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
							connectedByUDP.sendString(msg);
						}

					} else if (Math.abs(port1 - port2) < 10) {
						port = Math.max(port1, port2);
						// 预测几条
						for (int i = 0; i < 10; i++) {
							port++;
							connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), port);
							connectedByUDP.sendString(msg);
						}
						if (!Tools.isEquals(candidate.getLocalIp(), candidate.getRemoteIp())) {
							connectedByUDP = getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
							connectedByUDP.sendString(msg);
						}
					} else { // 不可能猜测
						logHelper.debug(self, "Symmetric 端口分配相差过大");
						connectedByUDP = getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
						connectedByUDP.sendString(msg);
					}
				}
			} else {// 真对称型
				candidate1 = candidates.get(0);
				candidate2 = candidates.get(1);
				port1 = Integer.valueOf(candidate1.getRemotePort());
				port2 = Integer.valueOf(candidate2.getRemotePort());

				if (Math.abs(port1 - port2) == 1) {// 相差1
					port = Math.max(port1, port2);
					// 预测几条
					for (int i = 0; i < 10; i++) {
						port++;
						connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), port);
						connectedByUDP.sendString(msg);
					}
					connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), port + 1);
					connectedByUDP.sendString(msg);

					if (!Tools.isEquals(candidate.getLocalIp(), candidate.getRemoteIp())) {
						connectedByUDP = getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
						connectedByUDP.sendString(msg);
					}

				} else if (Math.abs(port1 - port2) < 10) {
					port = Math.max(port1, port2);
					// 预测几条
					for (int i = 0; i < 10; i++) {
						port++;
						connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), port);
						connectedByUDP.sendString(msg);
					}
					if (!Tools.isEquals(candidate.getLocalIp(), candidate.getRemoteIp())) {
						connectedByUDP = getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
						connectedByUDP.sendString(msg);
					}
				} else { // 不可能猜测
					logHelper.debug(self, "Symmetric 端口分配相差过大");
					connectedByUDP = getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
					connectedByUDP.sendString(msg);
				}

			}
		} else if (isSymmetric && !isOpSymmetric) {
			// 自己对称型，而对方不是，目标唯一
			candidate = candidates.get(0);
			connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), candidate.getRemotePort());
			connectedByUDP.sendString(msg);
			if (!Tools.isEquals(candidate.getLocalIp(), candidate.getRemoteIp())) {
				connectedByUDP = getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
				connectedByUDP.sendString(msg);
			}
		} else if (!isSymmetric && isOpSymmetric) {
			// 自己非对称型，而对方是，自己可以随便发送数据
			if (candidates.size() == 1) {
				candidate = candidates.get(0);
				port = Integer.valueOf(candidate.getRelayPort());
				// 预测几条
				for (int i = 0; i < 10; i++) {
					port++;
					connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), port);
					connectedByUDP.sendString(msg);
				}
				connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), candidate.getRemotePort());
				connectedByUDP.sendString(msg);

				if (!Tools.isEquals(candidate.getLocalIp(), candidate.getRemoteIp())) {
					connectedByUDP = getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
					connectedByUDP.sendString(msg);
				}
			} else if (candidates.size() > 1) {
				candidate1 = candidates.get(0);
				candidate2 = candidates.get(1);
				port1 = Integer.valueOf(candidate1.getRemotePort());
				port2 = Integer.valueOf(candidate2.getRemotePort());

				if (Math.abs(port1 - port2) == 1) {// 相差1
					port = Math.max(port1, port2);
					// 预测几条
					for (int i = 0; i < 10; i++) {
						port++;
						connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), port);
						connectedByUDP.sendString(msg);
					}

					if (!Tools.isEquals(candidate.getLocalIp(), candidate.getRemoteIp())) {
						connectedByUDP = getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
						connectedByUDP.sendString(msg);
					}

				} else if (Math.abs(port1 - port2) < 10) {
					port = Math.max(port1, port2);
					// 预测几条
					for (int i = 0; i < 10; i++) {
						port++;
						connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), port);
						connectedByUDP.sendString(msg);
					}
					if (!Tools.isEquals(candidate.getLocalIp(), candidate.getRemoteIp())) {
						connectedByUDP = getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
						connectedByUDP.sendString(msg);
					}
				} else { // 不可能猜测
					logHelper.debug(self, "Symmetric 端口分配相差过大");
					connectedByUDP = getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
					connectedByUDP.sendString(msg);
				}
			}

		} else {
			candidate = candidates.get(0);
			connectedByUDP = getUDPConnectorByAddress(candidate.getRemoteIp(), candidate.getRemotePort());
			connectedByUDP.sendString(msg);
			if (!Tools.isEquals(candidate.getLocalIp(), candidate.getRemoteIp())) {
				connectedByUDP = getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
				connectedByUDP.sendString(msg);
			}
		}
	}

	public void close(){
		if(datagramSocketServer != null){
			datagramSocketServer.close();
		}
		if(socketServer != null){
			socketServer.close();
		}

		if(commonProxy != null){
			commonProxy.close();
		}
		try {
			finalize();
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}
}
