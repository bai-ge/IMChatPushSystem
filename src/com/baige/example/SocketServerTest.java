package com.baige.example;

import com.baige.connect.ConnectedByTCP;
import com.baige.connect.ConnectedByUDP;
import com.baige.connect.DatagramSocketServer;
import com.baige.connect.OnDatagramSocketServerListener;
import com.baige.connect.OnSocketServerListener;
import com.baige.connect.SocketPacket;
import com.baige.connect.SocketServer;
import com.baige.util.Log;

public class SocketServerTest {
private final static String TAG = SocketServerTest.class.getCanonicalName();
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SocketServer server = new SocketServer();
		server.registerSocketServerListener(new OnSocketServerListener(){

			@Override
			public void onServerBeginListen(SocketServer socketServer, int port) {
				// TODO Auto-generated method stub
				Log.d(TAG, "监听成功"+port);
			}

			@Override
			public void onServerStopListen(SocketServer socketServer, int port) {
				// TODO Auto-generated method stub
				Log.d(TAG, "停止监听"+port);
				
			}

			@Override
			public void onClientConnected(SocketServer socketServer, ConnectedByTCP socketServerClient) {
				// TODO Auto-generated method stub
				Log.d(TAG, "客户连接成功"+socketServerClient);
			}

			@Override
			public void onClientDisconnected(SocketServer socketServer, ConnectedByTCP socketServerClient) {
				// TODO Auto-generated method stub
				Log.d(TAG, "客户掉线"+socketServerClient);
			}
			
		});
		Log.d(TAG, ""+server.getPort());
		server.beginListen();
		DatagramSocketServer datagramSocketServer = new DatagramSocketServer();
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
	}

}
