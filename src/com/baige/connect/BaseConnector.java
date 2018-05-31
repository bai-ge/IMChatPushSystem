package com.baige.connect;

/**
 * Created by baige on 2018/3/21.
 */

public abstract class BaseConnector {
	private String deviceId;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    public abstract void connect();
    public abstract void disconnect();
    public abstract void start();
    
    public abstract boolean isConnected();

    public abstract boolean isDisconnected();

    public abstract boolean isConnecting();

    public abstract boolean isDisconnecting();
    protected abstract void sendHeartBeat();
    public abstract SocketClientAddress getAddress();
    public abstract BaseConnector registerConnectedListener(OnConnectedListener listener);
    public abstract BaseConnector unRegisterConnectedListener(OnConnectedListener listener);
    public abstract BaseConnector registerSendingListener(OnSocketSendingListener listener);
    public abstract BaseConnector unRegiserSendingListener(OnSocketSendingListener listener);
    public abstract BaseConnector registerReceivingListener(OnSocketReceivingListener listener);
    public abstract BaseConnector unRegiserReceivingListener(OnSocketReceivingListener listener);
    public abstract SocketPacket sendPacket(SocketPacket packet);
    public abstract SocketPacket sendData(byte[] content);
    public abstract SocketPacket sendData(byte[] heart, byte[] content);
    public abstract SocketPacket sendString(String message);
}
