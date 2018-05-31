package com.baige.connect.msg;

import com.baige.common.Parm;
import com.baige.connect.BaseConnector;
import com.baige.connect.ConnectedByTCP;
import com.baige.connect.ConnectedByUDP;
import com.baige.connect.NetServerManager;
import com.baige.data.entity.Candidate;
import com.baige.data.entity.DeviceModel;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.JsonTools;
import com.baige.util.LogHelper;
import com.baige.util.Tools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MessageReceive {

    // 自己接收到消息
    public static void receive(BaseConnector connector, JSONObject json, String from, String to) {
        if (connector == null || json == null) {
            return;
        }
        try {
            LogHelper.getInstance().verbose(MessageReceive.class, "UDP1"+json);
            if (json.has(Parm.RESPONSE) ) {
                //收到回复消息
                MessageResponse.response(connector, json, from, to);
            } else if (json.has(Parm.DATA_TYPE)) {
                LogHelper.getInstance().verbose(MessageReceive.class, "UDP2"+json);
                int type = json.getInt(Parm.DATA_TYPE);
                DeviceModel deviceModel = null;
                ResponseMessage responseMessage = null;
                CacheRepository cacheRepository = CacheRepository.getInstance();
                switch (type) {
                    case Parm.TYPE_LOGIN:
                        deviceModel = null;
                        String conDeviceId = connector.getDeviceId();
                        connector.setDeviceId(from);
                        if (!Tools.isEmpty(conDeviceId) && !from.equals(conDeviceId)) {
                            deviceModel = NetServerManager.getInstance().remove(conDeviceId);
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
                            deviceModel = NetServerManager.getInstance().getDeviceModelById(from);
                        }
                        if (deviceModel == null) {
                            deviceModel = new DeviceModel();
                        }
                        deviceModel.setDeviceId(from);
                        NetServerManager.getInstance().put(from, deviceModel);

                        if (connector instanceof ConnectedByTCP) {
                            deviceModel.setConnectedByTCP((ConnectedByTCP) connector);
                        } else if (connector instanceof ConnectedByUDP) {
                            deviceModel.setConnectedByUDP((ConnectedByUDP) connector);
                        }

                        if (json.has(Parm.LOCAL_IP)) {
                            deviceModel.setLocalIp(json.getString(Parm.LOCAL_IP));
                        }
                        if (json.has(Parm.LOCAL_PORT)) {
                            deviceModel.setLocalPort(json.getInt(Parm.LOCAL_PORT));
                        }
                        if (json.has(Parm.ACCEPT_PORT)) {
                            deviceModel.setAcceptPort(json.getInt(Parm.ACCEPT_PORT));
                        }
                        if (json.has(Parm.LOCAL_UDP_PORT)) {
                            deviceModel.setLocalUdpPort(json.getInt(Parm.LOCAL_UDP_PORT));
                        }

                        deviceModel.setRemoteIp(connector.getAddress().getRemoteIP());
                        deviceModel.setRemotePort(connector.getAddress().getRemotePortIntegerValue());

                        // 回复登录信息
                        responseMessage = new ResponseMessage();
                        JSONObject msg = MessageManager.loginSuccess(cacheRepository.getDeviceId(), from,
                                deviceModel.getRemoteIp(), deviceModel.getRemotePort() + "", cacheRepository.getUdpPort());
                        responseMessage.setResponse(msg);
                        LogHelper.getInstance().debug(connector, msg.toString());
                        connector.sendString(responseMessage.toJson());
                        break;
                    case Parm.TYPE_UDP_TEST:
                        LogHelper.getInstance().verbose(MessageReceive.class, "UDP3"+json);
                        responseMessage = new ResponseMessage();
                        JSONObject dataJson = new JSONObject();
                        dataJson.put(Parm.CODE, Parm.CODE_SUCCESS);
                        if (json.has(Parm.CALLBACK)) {
                            dataJson.put(Parm.CALLBACK, json.getString(Parm.CALLBACK));
                        }
                        if (json.has(Parm.LOCAL_IP)) {
                            dataJson.put(Parm.LOCAL_IP, json.getString(Parm.LOCAL_IP));
                        }
                        if (json.has(Parm.LOCAL_UDP_PORT)) {
                            dataJson.put(Parm.LOCAL_UDP_PORT, json.getString(Parm.LOCAL_UDP_PORT));
                        }
                        if (json.has(Parm.SEND_TIME)) {
                            dataJson.put(Parm.SEND_TIME, json.getString(Parm.SEND_TIME));
                        }
                        dataJson.put(Parm.DATA_TYPE, Parm.TYPE_UDP_TEST);
                        dataJson.put(Parm.REMOTE_IP, connector.getAddress().getRemoteIP());
                        dataJson.put(Parm.REMOTE_UDP_PORT, connector.getAddress().getRemotePort());
                        dataJson.put(Parm.FROM, CacheRepository.getInstance().getDeviceId());
                        dataJson.put(Parm.TO, from);
                        responseMessage.setResponse(dataJson);
                        connector.sendString(responseMessage.toJson());
                        LogHelper.getInstance().verbose(MessageReceive.class, "UDP 回复"+responseMessage.toJson());
                        deviceModel = NetServerManager.getInstance().getDeviceModelById(from);
                        if (deviceModel != null && connector instanceof ConnectedByUDP) {

                            deviceModel.setConnectedByUDP((ConnectedByUDP) connector);
                            LogHelper.getInstance().error(MessageReceive.class, "设置UDP Connector："+connector);
                            if (json.has(Parm.LOCAL_IP)) {
                                deviceModel.setLocalIp(json.getString(Parm.LOCAL_IP));
                            }
                            if (json.has(Parm.LOCAL_UDP_PORT)) {
                                deviceModel.setLocalUdpPort(json.getInt(Parm.LOCAL_UDP_PORT));
                            }
                            deviceModel.setRemoteUdpPort(connector.getAddress().getRemotePortIntegerValue());
                            deviceModel.setRemoteIp(connector.getAddress().getRemoteIP());
                        }
                        break;
                    case Parm.TYPE_TRY_PTP:

                        if (json.has(Parm.CANDIDATES)) {
                            JSONArray jsonArray = json.getJSONArray(Parm.CANDIDATES);
                            ArrayList<Candidate> candidates = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Candidate candidate = (Candidate) JsonTools.toJavaBean(Candidate.class,
                                        jsonArray.getJSONObject(i));
                                if (candidate != null) {
                                    candidates.add(candidate);
                                }
                            }
                            // 建立P2P连接
                            if (candidates != null && candidates.size() > 0) {
                                deviceModel = NetServerManager.getInstance().getDeviceModelById(from);
                                if (deviceModel != null) {
                                    deviceModel.setCandidates(candidates);
                                }
                                NetServerManager.getInstance().tryPTPConnect(candidates, from);
                            }
                        }
                        // 回复自己的Candidate
                        responseMessage = new ResponseMessage();
                        responseMessage.setFrom(to);
                        responseMessage.setTo(from);
                        responseMessage.setResponse(MessageManager.sendCandidateTo(from));
                        connector.sendString(responseMessage.toJson());
                        break;
                    case Parm.TYPE_TRY_PTP_CONNECT:
                        from = json.getString(Parm.FROM);
                        to = json.getString(Parm.TO);
                        LogHelper.getInstance().debug(MessageManager.class, "P2P 建立成功" + connector);
                        responseMessage = new ResponseMessage();
                        responseMessage.setResponse(MessageManager.tryPTPConnect(cacheRepository.getDeviceId(), from));
                        connector.sendString(responseMessage.toJson());
                        break;
                    default:
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
