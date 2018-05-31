package com.baige.example;

import com.baige.connect.BaseConnector;
import com.baige.connect.ConnectedByTCP;
import com.baige.connect.ConnectedByUDP;
import com.baige.connect.DatagramSocketServer;
import com.baige.connect.OnConnectedListener;
import com.baige.connect.OnDatagramSocketServerListener;
import com.baige.connect.OnSocketReceivingListener;
import com.baige.connect.OnSocketSendingListener;
import com.baige.connect.SocketClientAddress;
import com.baige.connect.SocketPacket;
import com.baige.connect.SocketServer;
import com.baige.util.IPUtil;
import com.baige.util.Log;
import com.baige.util.StringValidation;

public class SocketClientTest {
	private final static String TAG = SocketClientTest.class.getCanonicalName();
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Log.d(TAG, IPUtil.getLocalIPAddress(true));
		SocketClientAddress address = new SocketClientAddress(IPUtil.getLocalIPAddress(true), SocketServer.DEFAULT_PORT);
		ConnectedByTCP connectedByTCP = new ConnectedByTCP(address);
		connectedByTCP.registerConnectedListener(new OnConnectedListener(){

			@Override
			public void onConnected(BaseConnector connector) {
				// TODO Auto-generated method stub
				Log.d(TAG, "连接成功"+connector.toString());
				//connector.sendString("Hello world!"+connector.toString());
				connector.sendData(connector.toString().getBytes(), new String("Hello world!").getBytes());
			}

			@Override
			public void onDisconnected(BaseConnector connector) {
				// TODO Auto-generated method stub
				Log.d(TAG, "连接失败"+connector.toString());
			}

			@Override
			public void onResponse(BaseConnector connector, SocketPacket responsePacket) {
				// TODO Auto-generated method stub
				Log.d(TAG, connector+"收到信息"+responsePacket.toString());
			}
			
		});
		connectedByTCP.registerSendingListener(new OnSocketSendingListener(){

			@Override
			public void onSendPacketBegin(BaseConnector connector, SocketPacket packet) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSendPacketEnd(BaseConnector connector, SocketPacket packet) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSendPacketCancel(BaseConnector connector, SocketPacket packet) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSendingPacketInProgress(BaseConnector connector, SocketPacket packet, float progress,
					int sendedLength) {
				// TODO Auto-generated method stub
				
			}
			
		});
		connectedByTCP.registerReceivingListener(new OnSocketReceivingListener(){

			@Override
			public void onReceivePacketBegin(BaseConnector connector, SocketPacket packet) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onReceivePacketEnd(BaseConnector connector, SocketPacket packet) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onReceivePacketCancel(BaseConnector connector, SocketPacket packet) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onReceivingPacketInProgress(BaseConnector connector, SocketPacket packet, float progress,
					int receivedLength) {
				// TODO Auto-generated method stub
				
			}
			
		});
		connectedByTCP.connect();
		DatagramSocketServer datagramSocketServer = new DatagramSocketServer(-1);
		datagramSocketServer.registerServerListener(new OnDatagramSocketServerListener(){

			@Override
			public void onServerStart(DatagramSocketServer server) {
				// TODO Auto-generated method stub
				Log.d(TAG, "UDP 服务开始监听："+server.getLocalPort());
			}

			@Override
			public void onServerClose(DatagramSocketServer server) {
				// TODO Auto-generated method stub
				Log.d(TAG, "UDP 服务停止监听："+server.getLocalPort());
			}

			@Override
			public void onServerReceivePacket(ConnectedByUDP connector, SocketPacket packet) {
				// TODO Auto-generated method stub
				Log.d(TAG, connector+"UDP 接收到数据"+ packet);
			}

			@Override
			public void onClientConnected(ConnectedByUDP connector) {
				// TODO Auto-generated method stub
				Log.d(TAG, "UDP 连接成功"+connector);
				connector.sendData(connector.toString().getBytes(), new String("UDP 连接").getBytes());
			}

			@Override
			public void onClientDisconnected(ConnectedByUDP connector) {
				// TODO Auto-generated method stub
				Log.d(TAG, "UDP 断开连接"+connector);
			}
			
		});
		datagramSocketServer.start();
		SocketClientAddress udpaddress = new SocketClientAddress(IPUtil.getLocalIPAddress(true), DatagramSocketServer.DEFAULT_LOCAL_PORT);
		ConnectedByUDP connectedByUDP = new ConnectedByUDP(udpaddress);
		connectedByUDP.setRunningSocket(datagramSocketServer);
		connectedByUDP.connect();
	}

}
