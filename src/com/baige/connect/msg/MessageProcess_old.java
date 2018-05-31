package com.baige.connect.msg;

import com.baige.common.Parm;
import com.baige.callback.BaseCallback;

import com.baige.callback.CallbackManager;

public class MessageProcess_old {

	/**
	 * 处理转发
	 * 
	 * @param connector
	 * @param packet
	 */

	public static int[] filterNotTranspond = new int[] { Parm.TYPE_UDP_TEST, Parm.TYPE_TRY_PTP_CONNECT };

//	public static void receive(BaseConnector connector, SocketPacket packet) {
//		if (connector == null || packet == null) {
//			return;
//		}
//		if (packet.isHeartBeat() || packet.isDisconnected()) {
//			return;
//		}
//		NetServerManager netServerManager = NetServerManager.getInstance();
//		BaseConnector baseConnector = null;
//		if (packet.getHeaderBuf() != null) { // 语音信息或文件信息
//			try {
//				String msg = Tools.dataToString(packet.getHeaderBuf(), Tools.DEFAULT_ENCODE);
//				if (!Tools.isEmpty(msg)) {
//					JSONObject json = new JSONObject(msg);
//					if (json.has(Parm.DATA_TYPE)) {
//						int type = json.getInt(Parm.DATA_TYPE);
//						String from = null;
//						String to = null;
//						switch (type) {
//						case Parm.TYPE_FILE:
//						case Parm.TYPE_VOICE:
//							from = json.getString(Parm.FROM);
//							to = json.getString(Parm.TO);
//							if (Tools.isEmpty(to) || CacheRepository.getInstance().getDeviceId().equals(to)) {
//								// 自己收到自己的消息
//								return;
//							}
//							netServerManager = NetServerManager.getInstance();
//							baseConnector = netServerManager.getUDPConnectorById(to);
//							if (baseConnector != null) {
//								baseConnector.sendPacket(packet);
//							}
//							break;
//
//						// TODO 转发将要废除
//						case Parm.TYPE_TRANSPOND: // 转发
//							from = json.getString(Parm.FROM);
//							to = json.getString(Parm.TO);
//							netServerManager = NetServerManager.getInstance();
//							baseConnector = netServerManager.getUDPConnectorById(to);
//							if (baseConnector != null) {
//								baseConnector.sendPacket(packet);
//							} else {
//								baseConnector = netServerManager.getTCPConnectorById(to);
//								if (baseConnector != null && ((ConnectedByTCP) baseConnector).isConnected()) {
//									baseConnector.sendPacket(packet);
//								} else {
//									// TODO 返回无法发送错误
//									ResponseMessage responseMessage = new ResponseMessage();
//									responseMessage.setCode(Parm.CODE_UNKNOWN);
//									responseMessage.setData(json);
//									connector.sendString(responseMessage.toJson());
//								}
//							}
//							break;
//						default:
//							break;
//						}
//					}
//				}
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		} else if (packet.getContentBuf() != null) {
//			try {
//				String msg = Tools.dataToString(packet.getContentBuf(), Tools.DEFAULT_ENCODE);
//				if (!Tools.isEmpty(msg)) {
//					JSONObject json = new JSONObject(msg);
//					if (json.has(Parm.TO)) {
//						String to = json.getString(Parm.TO);
//						if (CacheRepository.getInstance().getDeviceId().equals(to)) {
//							// 自己收的消息
//							receive(connector, json);
//						} else {
//							// 过滤掉不转发的类型 Parm.TYPE_UDP_TEST
//							if (json.has(Parm.DATA_TYPE)) {
//								int type = json.getInt(Parm.DATA_TYPE);
//								for (int i = 0; i < filterNotTranspond.length; i++) {
//									if (type == filterNotTranspond[i]) {
//										return;
//									}
//								}
//
//							}
//							transpond(connector, packet, json, to);
//						}
//					} else {// if (json.has(Parm.TO))
//						receive(connector, json);
//					}
//
//				}
//
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
//	public static void transpond(BaseConnector connector, SocketPacket packet, JSONObject json, String to) {
//		NetServerManager netServerManager = NetServerManager.getInstance();
//		BaseConnector baseConnector = null;
//		if (connector instanceof ConnectedByTCP) {
//			baseConnector = netServerManager.getTCPConnectorById(to);
//			if (baseConnector != null && baseConnector.isConnected()) {
//				baseConnector.sendPacket(packet);
//			} else {
//				baseConnector = netServerManager.getUDPConnectorById(to);
//				if (baseConnector != null) {
//					baseConnector.sendPacket(packet);
//				} else {
//					// TODO 返回无法发送错误
//					ResponseMessage responseMessage = new ResponseMessage();
//					responseMessage.setCode(Parm.CODE_UNKNOWN);
//					responseMessage.setData(json);
//					connector.sendString(responseMessage.toJson());
//				}
//			}
//		} else if (connector instanceof ConnectedByUDP) {
//			baseConnector = netServerManager.getUDPConnectorById(to);
//			if (baseConnector != null) {
//				baseConnector.sendPacket(packet);
//			} else {
//				baseConnector = netServerManager.getTCPConnectorById(to);
//				if (baseConnector != null && baseConnector.isConnected()) {
//					baseConnector.sendPacket(packet);
//				} else {
//					// TODO 返回无法发送错误
//					ResponseMessage responseMessage = new ResponseMessage();
//					responseMessage.setCode(Parm.CODE_NOT_FIND);
//					responseMessage.setData(json);
//					connector.sendString(responseMessage.toJson());
//				}
//			}
//		}
//	}
//
//	// 自己接收到消息
//	public static void receive(BaseConnector connector, JSONObject json) {
//		if (connector == null || json == null) {
//			return;
//		}
//		try {
//			if (json.has(Parm.CODE) && json.has(Parm.DATA)) {
//				response(connector, json);
//			} else if (json.has(Parm.DATA_TYPE)) {
//				int type = json.getInt(Parm.DATA_TYPE);
//				String from = null;
//				String to = null;
//				DeviceModel deviceModel = null;
//				ResponseMessage responseMessage = null;
//				CacheRepository cacheRepository = CacheRepository.getInstance();
//				switch (type) {
//				case Parm.TYPE_LOGIN:
//
//					deviceModel = null;
//					from = json.getString(Parm.FROM);
//					String conDeviceId = connector.getDeviceId();
//					connector.setDeviceId(from);
//					if (!Tools.isEmpty(conDeviceId) && !from.equals(conDeviceId)) {
//						deviceModel = NetServerManager.getInstance().remove(conDeviceId);
//						if (deviceModel != null) {
//							if (deviceModel.getConnectedByTCP() != null) {
//								deviceModel.getConnectedByTCP().setDeviceId(from);
//							}
//							if (deviceModel.getConnectedByUDP() != null) {
//								deviceModel.getConnectedByUDP().setDeviceId(from);
//							}
//						}
//					}
//
//					if (deviceModel == null) {
//						deviceModel = NetServerManager.getInstance().getDeviceModelById(from);
//					}
//					if (deviceModel == null) {
//						deviceModel = new DeviceModel();
//					}
//					deviceModel.setDeviceId(from);
//					NetServerManager.getInstance().put(from, deviceModel);
//
//					if (connector instanceof ConnectedByTCP) {
//						deviceModel.setConnectedByTCP((ConnectedByTCP) connector);
//					} else if (connector instanceof ConnectedByUDP) {
//						deviceModel.setConnectedByUDP((ConnectedByUDP) connector);
//					}
//
//					if (json.has(Parm.LOCAL_IP)) {
//						deviceModel.setLocalIp(json.getString(Parm.LOCAL_IP));
//					}
//					if (json.has(Parm.LOCAL_PORT)) {
//						deviceModel.setLocalPort(json.getInt(Parm.LOCAL_PORT));
//					}
//					if (json.has(Parm.ACCEPT_PORT)) {
//						deviceModel.setAcceptPort(json.getInt(Parm.ACCEPT_PORT));
//					}
//					if (json.has(Parm.LOCAL_UDP_PORT)) {
//						deviceModel.setLocalUdpPort(json.getInt(Parm.LOCAL_UDP_PORT));
//					}
//
//					deviceModel.setRemoteIp(connector.getAddress().getRemoteIP());
//					deviceModel.setRemotePort(connector.getAddress().getRemotePortIntegerValue());
//
//					// 回复登录信息
//					responseMessage = new ResponseMessage();
//					responseMessage.setCode(Parm.CODE_SUCCESS);
//					JSONObject msg = MessageManager.loginSuccess(cacheRepository.getDeviceId(), from,
//							deviceModel.getRemoteIp(), deviceModel.getRemotePort() + "", cacheRepository.getUdpPort());
//					responseMessage.setData(msg);
//					LogHelper.getInstance().debug(connector, msg.toString());
//					connector.sendString(responseMessage.toJson());
//					break;
//				case Parm.TYPE_UDP_TEST:
//					from = json.getString(Parm.FROM);
//					responseMessage = new ResponseMessage();
//					responseMessage.setCode(Parm.CODE_SUCCESS);
//					JSONObject dataJson = new JSONObject();
//					if (json.has(Parm.CALLBACK)) {
//						dataJson.put(Parm.CALLBACK, json.getString(Parm.CALLBACK));
//					}
//					if (json.has(Parm.LOCAL_IP)) {
//						dataJson.put(Parm.LOCAL_IP, json.getString(Parm.LOCAL_IP));
//					}
//					if (json.has(Parm.LOCAL_UDP_PORT)) {
//						dataJson.put(Parm.LOCAL_UDP_PORT, json.getString(Parm.LOCAL_UDP_PORT));
//					}
//					if (json.has(Parm.SEND_TIME)) {
//						dataJson.put(Parm.SEND_TIME, json.getString(Parm.SEND_TIME));
//					}
//					dataJson.put(Parm.DATA_TYPE, Parm.TYPE_UDP_TEST);
//					dataJson.put(Parm.REMOTE_IP, connector.getAddress().getRemoteIP());
//					dataJson.put(Parm.REMOTE_UDP_PORT, connector.getAddress().getRemotePort());
//					dataJson.put(Parm.FROM, CacheRepository.getInstance().getDeviceId());
//					dataJson.put(Parm.TO, from);
//					responseMessage.setData(dataJson);
//					connector.sendString(responseMessage.toJson());
//					deviceModel = NetServerManager.getInstance().getDeviceModelById(from);
//					if (deviceModel != null && connector instanceof ConnectedByUDP) {
//
//						deviceModel.setConnectedByUDP((ConnectedByUDP) connector);
//						if (json.has(Parm.LOCAL_IP)) {
//							deviceModel.setLocalIp(json.getString(Parm.LOCAL_IP));
//						}
//						if (json.has(Parm.LOCAL_UDP_PORT)) {
//							deviceModel.setLocalUdpPort(json.getInt(Parm.LOCAL_UDP_PORT));
//						}
//						deviceModel.setRemoteUdpPort(connector.getAddress().getRemotePortIntegerValue());
//						deviceModel.setRemoteIp(connector.getAddress().getRemoteIP());
//					}
//					break;
//				case Parm.TYPE_TRY_PTP:
//					from = json.getString(Parm.FROM);
//					to = json.getString(Parm.TO);
//
//					if (json.has(Parm.CANDIDATES)) {
//						JSONArray jsonArray = json.getJSONArray(Parm.CANDIDATES);
//						ArrayList<Candidate> candidates = new ArrayList<>();
//						for (int i = 0; i < jsonArray.length(); i++) {
//							Candidate candidate = (Candidate) JsonTools.toJavaBean(Candidate.class,
//									jsonArray.getJSONObject(i));
//							if (candidate != null) {
//								candidates.add(candidate);
//							}
//						}
//						// 建立P2P连接
//						if (candidates != null && candidates.size() > 0) {
//							deviceModel = NetServerManager.getInstance().getDeviceModelById(from);
//							if (deviceModel != null) {
//								deviceModel.setCandidates(candidates);
//							}
//							NetServerManager.getInstance().tryPTPConnect(candidates, from);
//						}
//					}
//					// 回复自己的Candidate
//					responseMessage = new ResponseMessage();
//					responseMessage.setFrom(to);
//					responseMessage.setTo(from);
//					responseMessage.setCode(Parm.CODE_SUCCESS);
//					responseMessage.setData(MessageManager.sendCandidateTo(from));
//					connector.sendString(responseMessage.toJson());
//					break;
//				case Parm.TYPE_TRY_PTP_CONNECT:
//					from = json.getString(Parm.FROM);
//					to = json.getString(Parm.TO);
//					LogHelper.getInstance().debug(MessageManager.class, "P2P 建立成功" + connector);
//					responseMessage = new ResponseMessage();
//					responseMessage.setCode(Parm.CODE_SUCCESS);
//					responseMessage.setData(MessageManager.tryPTPConnect(cacheRepository.getDeviceId(), from));
//					connector.sendString(responseMessage.toJson());
//					break;
//				default:
//					break;
//				}
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//	}
//
//	// 收到回复
//	public static void response(BaseConnector connector, JSONObject json) {
//		if (connector == null || json == null) {
//			return;
//		}
//		try {
//			if (json.has(Parm.DATA)) {
//				JSONObject dataJson = json.getJSONObject(Parm.DATA);
//				if (dataJson.has(Parm.DATA_TYPE)) {
//					int type = dataJson.getInt(Parm.DATA_TYPE);
//					String from = null;
//					Candidate candidate;
//					DeviceModel deviceModel;
//					if(json.has(Parm.FROM)){
//						from = json.getString(Parm.FROM);
//					}
//					if(dataJson.has(Parm.FROM)){
//						from = dataJson.getString(Parm.FROM);
//					}
//					switch (type) {
//					case Parm.TYPE_LOGIN:
//						break;
//					case Parm.TYPE_LOGOUT:
//						break;
//					case Parm.TYPE_UDP_TEST:
//						candidate = new Candidate();
//						candidate.setTime(System.currentTimeMillis());
//						if (dataJson.has(Parm.FROM)) {
//							candidate.setFrom(dataJson.getString(Parm.FROM));
//						}
//						if (dataJson.has(Parm.REMOTE_IP)) {
//							candidate.setRemoteIp(dataJson.getString(Parm.REMOTE_IP));
//						}
//						if (dataJson.has(Parm.REMOTE_UDP_PORT)) {
//							candidate.setRemotePort(dataJson.getString(Parm.REMOTE_UDP_PORT));
//						}
//						if (dataJson.has(Parm.LOCAL_IP)) {
//							candidate.setLocalIp(dataJson.getString(Parm.LOCAL_IP));
//						}
//						if (dataJson.has(Parm.LOCAL_UDP_PORT)) {
//							candidate.setLocalPort(dataJson.getString(Parm.LOCAL_UDP_PORT));
//						}
//						if (dataJson.has(Parm.SEND_TIME)) {
//							long time = dataJson.getLong(Parm.SEND_TIME);
//							candidate.setDelayTime(System.currentTimeMillis() - time);
//						}
//						candidate.setRelayIp(connector.getAddress().getRemoteIP());
//						candidate.setRelayPort(connector.getAddress().getRemotePort());
//						if (dataJson.has(Parm.CALLBACK)) {
//						String callback = dataJson.getString(Parm.CALLBACK);
//						BaseCallback baseCallBack = CallbackManager.getInstance().get(callback);
//						if (baseCallBack != null) {
//							baseCallBack.loadObject(candidate);
//						}
//					}
//						CacheRepository.getInstance().add(candidate);
//
//						LogHelper logHelper = LogHelper.getInstance();
//						logHelper.debug(connector, dataJson.toString());
//						connector.disconnect();
//						break;
//					case Parm.TYPE_TRY_PTP:
//						if (dataJson.has(Parm.CANDIDATES)) {
//							JSONArray jsonArray = dataJson.getJSONArray(Parm.CANDIDATES);
//							ArrayList<Candidate> candidates = new ArrayList<>();
//							for (int i = 0; i < jsonArray.length(); i++) {
//								candidate = (Candidate) JsonTools.toJavaBean(Candidate.class,
//										jsonArray.getJSONObject(i));
//								if (candidate != null) {
//									candidates.add(candidate);
//								}
//							}
//							// 建立P2P连接
//							if (candidates != null && candidates.size() > 0) {
//								deviceModel = NetServerManager.getInstance().getDeviceModelById(from);
//								if (deviceModel != null) {
//									deviceModel.setCandidates(candidates);
//								}
//								NetServerManager.getInstance().tryPTPConnect(candidates, from);
//							}
//						}
//						break;
//					case Parm.TYPE_TRY_PTP_CONNECT:
//						LogHelper.getInstance().debug(MessageManager.class, "P2P连接成功："+connector);
//						break;
//					default:
//						break;
//					}
//				}
//			}
//		} catch (
//
//		JSONException e) {
//			e.printStackTrace();
//		}
//	}

}
