package com.baige.connect.msg;

import com.baige.common.Parm;
import com.baige.connect.*;
import com.baige.util.Log;
import com.baige.util.LogHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageTranspond {


    public static void transpond(BaseConnector connector, SocketPacket packet, JSONObject json, String to) {
        NetServerManager netServerManager = NetServerManager.getInstance();
        BaseConnector baseConnector = null;
        if (connector instanceof ConnectedByTCP) {
            baseConnector = netServerManager.getTCPConnectorById(to);
            if (baseConnector != null && baseConnector.isConnected()) {
                baseConnector.sendPacket(packet);
            } else {
                baseConnector = netServerManager.getUDPConnectorById(to);
                if (baseConnector != null) {
                    baseConnector.sendPacket(packet);
                } else {
                    // TODO 返回无法发送错误
                    ResponseMessage responseMessage = new ResponseMessage();
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(Parm.CODE, Parm.CODE_UNKNOWN);
                        jsonObject.put(Parm.DATA, json);
                        responseMessage.setResponse(jsonObject);
                        connector.sendString(responseMessage.toJson());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (connector instanceof ConnectedByUDP) {
            baseConnector = netServerManager.getUDPConnectorById(to);
            if (baseConnector != null) {
                baseConnector.sendPacket(packet);
//                LogHelper.getInstance().verbose(MessageTranspond.class, "转发成功：from:, UDP Connnector:"+connector);
//                LogHelper.getInstance().verbose(MessageTranspond.class, "转发成功："+to + ", UDP Connnector:"+baseConnector);
            } else {
                baseConnector = netServerManager.getTCPConnectorById(to);
                if (baseConnector != null && baseConnector.isConnected()) {
                    baseConnector.sendPacket(packet);
                } else {
                    // TODO 返回无法发送错误
                    ResponseMessage responseMessage = new ResponseMessage();
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(Parm.CODE, Parm.CODE_UNKNOWN);
                        jsonObject.put(Parm.DATA, json);
                        responseMessage.setResponse(jsonObject);
                        connector.sendString(responseMessage.toJson());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
