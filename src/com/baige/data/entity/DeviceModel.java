package com.baige.data.entity;

import java.util.ArrayList;

import com.baige.connect.ConnectedByTCP;
import com.baige.connect.ConnectedByUDP;

public class DeviceModel {
	private String deviceId;
	
	private long loginTime;

	private String localIp;
	private String remoteIp;

	private int localPort;
	private int remotePort;

	private int acceptPort;
	private int localUdpPort;
	private int remoteUdpPort;
	
	private ArrayList<Candidate> candidates;

	private ConnectedByTCP connectedByTCP;
	
	private ConnectedByUDP connectedByUDP;

	
	
	public ArrayList<Candidate> getCandidates() {
		return candidates;
	}

	public void setCandidates(ArrayList<Candidate> candidates) {
		this.candidates = candidates;
	}

	public DeviceModel() {
		loginTime = System.currentTimeMillis();
		candidates = new ArrayList<>();
	}

	public DeviceModel(String deviceid, String userid) {
		this();
		this.deviceId = deviceid;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceid) {
		this.deviceId = deviceid;
	}
	
	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

	
	public int getAcceptPort() {
		return acceptPort;
	}

	public void setAcceptPort(int acceptPort) {
		this.acceptPort = acceptPort;
	}



	public String getLocalIp() {
		return localIp;
	}

	public void setLocalIp(String localIp) {
		this.localIp = localIp;
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}
	
	public String getRemoteIp() {
		return remoteIp;
	}

	public void setRemoteIp(String remoteIp) {
		this.remoteIp = remoteIp;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public int getLocalUdpPort() {
		return localUdpPort;
	}

	public void setLocalUdpPort(int localUdpPort) {
		this.localUdpPort = localUdpPort;
	}

	public int getRemoteUdpPort() {
		return remoteUdpPort;
	}

	public void setRemoteUdpPort(int remoteUdpPort) {
		this.remoteUdpPort = remoteUdpPort;
	}
	
	public ConnectedByTCP getConnectedByTCP() {
		return connectedByTCP;
	}
	
	public void setConnectedByTCP(ConnectedByTCP connector) {
		this.connectedByTCP = connector;
	}

	public ConnectedByUDP getConnectedByUDP() {
		return connectedByUDP;
	}

	public void setConnectedByUDP(ConnectedByUDP connectedByUDP) {
		this.connectedByUDP = connectedByUDP;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "{deviceId=" + deviceId + ", localAddress=" + localIp + ":"
				+ localPort + ",remoteAddress=" + remoteIp + ":" + remotePort + ",localUdpPort="
				+ localUdpPort + ",remoteUdpPort=" + remoteUdpPort + "}";
	}

}
