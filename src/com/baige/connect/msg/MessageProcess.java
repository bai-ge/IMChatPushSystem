package com.baige.connect.msg;

import com.baige.common.Parm;
import com.baige.connect.BaseConnector;
import com.baige.connect.SocketPacket;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.LogHelper;
import com.baige.util.Tools;
import org.json.JSONException;
import org.json.JSONObject;

/*
* 消息分为几种类型
* 1.自己的
*   1)别人发给自己的消息
*   2)别人回复自己的消息
* 2.转发的
*
*
* */

public class MessageProcess {

    /**
     * 处理转发
     *
     * @param connector
     * @param packet
     */
    //不转发的类型
    public static int[] filterNotTranspond = new int[] { Parm.TYPE_UDP_TEST, Parm.TYPE_TRY_PTP_CONNECT };

    public static void receive(BaseConnector connector, SocketPacket packet) {
        if (connector == null || packet == null) {
            return;
        }
        if (packet.isHeartBeat() || packet.isDisconnected()) {
            return;
        }
        String from = null;
        String to = null;
        if (packet.getHeaderBuf() != null) { // 语音信息或文件信息
            try {
                String msg = Tools.dataToString(packet.getHeaderBuf(), Tools.DEFAULT_ENCODE);
                if (!Tools.isEmpty(msg)) {
                    JSONObject json = new JSONObject(msg);

                    if(json.has(Parm.FROM)){
                        from = json.getString(Parm.FROM);
                    }
                    if(json.has(Parm.TO)){
                        to = json.getString(Parm.TO);
                    }
                    if (Tools.isEmpty(to) || CacheRepository.getInstance().getDeviceId().equals(to)) {
                        // 自己收到自己的消息
                        return;
                    }else{
                        MessageTranspond.transpond(connector, packet, json, to);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (packet.getContentBuf() != null) {
            try {
                String msg = Tools.dataToString(packet.getContentBuf(), Tools.DEFAULT_ENCODE);
                LogHelper.getInstance().verbose(MessageProcess.class, msg);
                if (!Tools.isEmpty(msg)) {
                    JSONObject json = new JSONObject(msg);
                    if(json.has(Parm.FROM)){
                        from = json.getString(Parm.FROM);
                    }
                    if(json.has(Parm.TO)){
                        to = json.getString(Parm.TO);
                    }
                    if (Tools.isEmpty(to) || CacheRepository.getInstance().getDeviceId().equals(to)) {
                        // 自己收到自己的消息
                        MessageReceive.receive(connector, json, from, to);
                        return;
                    }else{
                        //过滤不转发的类型
                        if (json.has(Parm.DATA_TYPE)) {
                            int type = json.getInt(Parm.DATA_TYPE);
                            for (int i = 0; i < filterNotTranspond.length; i++) {
                                if (type == filterNotTranspond[i]) {
                                    return;
                                }
                            }

                        }
                        MessageTranspond.transpond(connector, packet, json, to);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
