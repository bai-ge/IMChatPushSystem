package com.baige.connect.msg;

import com.baige.common.Parm;
import com.baige.connect.BaseConnector;
import com.baige.connect.NetServerManager;
import com.baige.callback.BaseCallback;
import com.baige.callback.CallbackManager;
import com.baige.data.entity.Candidate;
import com.baige.data.entity.DeviceModel;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.JsonTools;
import com.baige.util.LogHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MessageResponse {
    // 收到回复
    public static void response(BaseConnector connector, JSONObject json, String from, String to) {
        if (connector == null || json == null) {
            return;
        }

        try {
            if (json.has(Parm.RESPONSE)) {
                JSONObject dataJson = json.getJSONObject(Parm.RESPONSE);
                if (dataJson.has(Parm.DATA_TYPE)) {
                    int type = dataJson.getInt(Parm.DATA_TYPE);
                    Candidate candidate;
                    DeviceModel deviceModel;
                    if(dataJson.has(Parm.FROM)){
                        from = dataJson.getString(Parm.FROM);
                    }
                    switch (type) {
                        case Parm.TYPE_LOGIN:
                            break;
                        case Parm.TYPE_LOGOUT:
                            break;
                        case Parm.TYPE_UDP_TEST:
                            LogHelper.getInstance().debug(MessageResponse.class, json.toString());
                            candidate = new Candidate();
                            candidate.setTime(System.currentTimeMillis());
                            if (dataJson.has(Parm.FROM)) {
                                candidate.setFrom(dataJson.getString(Parm.FROM));
                            }
                            if (dataJson.has(Parm.REMOTE_IP)) {
                                candidate.setRemoteIp(dataJson.getString(Parm.REMOTE_IP));
                            }
                            if (dataJson.has(Parm.REMOTE_UDP_PORT)) {
                                candidate.setRemotePort(dataJson.getString(Parm.REMOTE_UDP_PORT));
                            }
                            if (dataJson.has(Parm.LOCAL_IP)) {
                                candidate.setLocalIp(dataJson.getString(Parm.LOCAL_IP));
                            }
                            if (dataJson.has(Parm.LOCAL_UDP_PORT)) {
                                candidate.setLocalPort(dataJson.getString(Parm.LOCAL_UDP_PORT));
                            }
                            if (dataJson.has(Parm.SEND_TIME)) {
                                long time = dataJson.getLong(Parm.SEND_TIME);
                                candidate.setDelayTime(System.currentTimeMillis() - time);
                            }
                            candidate.setRelayIp(connector.getAddress().getRemoteIP());
                            candidate.setRelayPort(connector.getAddress().getRemotePort());
                            if (dataJson.has(Parm.CALLBACK)) {
                                String callback = dataJson.getString(Parm.CALLBACK);
                                BaseCallback baseCallBack = CallbackManager.getInstance().get(callback);
                                if (baseCallBack != null) {
                                    baseCallBack.loadObject(candidate);
                                }
                            }
                            CacheRepository.getInstance().add(candidate);

                            LogHelper logHelper = LogHelper.getInstance();
                            logHelper.debug(connector, dataJson.toString());
                            connector.disconnect();
                            break;
                        case Parm.TYPE_TRY_PTP:
                            if (dataJson.has(Parm.CANDIDATES)) {
                                JSONArray jsonArray = dataJson.getJSONArray(Parm.CANDIDATES);
                                ArrayList<Candidate> candidates = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    candidate = (Candidate) JsonTools.toJavaBean(Candidate.class,
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
                            break;
                        case Parm.TYPE_TRY_PTP_CONNECT:
                            LogHelper.getInstance().debug(MessageManager.class, "P2P连接成功："+connector);
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
