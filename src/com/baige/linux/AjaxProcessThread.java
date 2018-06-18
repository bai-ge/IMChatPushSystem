package com.baige.linux;

import com.baige.ApplicationConfig;
import com.baige.callback.CallbackManager;
import com.baige.callback.PushCallback;
import com.baige.connect.ConnectedByUDP;
import com.baige.connect.NetServerManager;
import com.baige.connect.msg.MessageManager;
import com.baige.data.entity.Candidate;
import com.baige.data.entity.DeviceModel;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.IPUtil;
import com.baige.util.StringValidation;
import com.baige.util.Tools;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.List;

public class AjaxProcessThread extends AjaxPrecessAbstract {


    private String RegexClientCmd = "^client\\s-l$";
    private String RegexUdpCmd = "(udp\\s-t\\s(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)[:\\s](6553[0-5]|655[0-2][0-9]|65[0-4][0-9]{2}|6[0-4][0-9]{3}|[1-5][0-9]{4}|[1-9][0-9]{0,3}))|(udp\\s-t)";

    public AjaxProcessThread(PipedOutputStream out, String[] args) {
        super(out, args);
    }

    protected boolean canExecute() {
        if (args == null || args.length <= 0) {
            return false;
        }
        if(args.length == 1){
            if(args[0].equals("close")){
                return true;
            }
        }
        if (args.length == 2) {
            if (args[0].equals("client") && args[1].equals("-l")) {
                return true;
            }
            if (args[0].equals("udp") && args[1].equals("-t")) {
                return true;
            }
        }
        if (args.length == 4) {
            if (args[0].equals("udp") && args[1].equals("-t")
                    && StringValidation.validateRegex(args[2], StringValidation.RegexIP)
                    && StringValidation.validateRegex(args[3], StringValidation.RegexPort)) {
                return true;
            }
        }
        return false;
    }



    @Override
    public void run() {
        assert out == null : "AjaxProcessThread out is null";
        assert args == null : "AjaxProcessThread args is null";
        if (!canExecute()) {
            try {
                out.write("未知命令！".getBytes());
                out.close();
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(args.length == 1){
            switch (args[0]){
                case "close":
                    try {
                        out.write("关闭服务器\r\n".getBytes());
                        out.flush();
                        out.close();
                        Thread.sleep(3000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        NetServerManager.getInstance().close();
                        System.exit(0);
                    }
                    break;
            }
        }

        NetServerManager netServerManager = NetServerManager.getInstance();
        try {
            if (args[0].equals("client")) {
                for (int i = 1; i < args.length; i++) {
                    if (args[i].equals("-l")) {
                        // 所有详情
                        System.out.println("准备获取所有详情");
                        List<DeviceModel> devices = netServerManager.getAllDevices();
                        System.out.println("输出所有设备详情");

                        // deviceId = deviceId,localAddress =
                        // localIp:localPort,remoteAddress =
                        // remoteIp:remotePort,localUdpPort =;
                        // localUdpPort,remoteUdpPort = remoteUdpPort;

                        String headerTab = String.format("%-17s\t%-25s\t%-25s\t%-17s\t%-17s\n", "device Id", "local address", "remote address", "local udpPort", "remote udpPort");
                        out.write(headerTab.getBytes());

                        for (int j = 0; j < devices.size(); j++) {
                            int len = devices.get(j).getDeviceId().length();

                            String IDstr = devices.get(j).getDeviceId().substring(0, 4) + "***"
                                    + devices.get(j).getDeviceId().substring(len - 4, len - 1);
                            StringBuffer deviceMsg = new StringBuffer();
                            deviceMsg.append(String.format("%-17s\t", IDstr));
                            deviceMsg.append(String.format("%-25s\t", devices.get(j).getLocalIp() + ":" + devices.get(j).getLocalPort()));
                            if (devices.get(j).getConnectedByTCP() == null || !devices.get(j).getConnectedByTCP().isConnected()) {
                                deviceMsg.append(String.format("#%-25s\t", devices.get(j).getRemoteIp() + ":" + devices.get(j).getRemotePort()));
                            } else {
                                deviceMsg.append(String.format("%-25s\t", devices.get(j).getRemoteIp() + ":" + devices.get(j).getRemotePort()));
                            }
                            deviceMsg.append(String.format("%-17s\t", devices.get(j).getLocalUdpPort()));
                            deviceMsg.append(String.format("%-17s\t", devices.get(j).getRemoteUdpPort()) + "\n");

//											deviceMsg.append(IDstr + "\t");
//											deviceMsg.append(devices.get(j).getLocalIp() + ":" + devices.get(j).getLocalPort() + " \t");
//											deviceMsg.append(devices.get(j).getRemoteIp() + ":" + devices.get(j).getRemotePort()+ " \t");
//											deviceMsg.append(devices.get(j).getLocalUdpPort() + " \t\t\t" );
//											deviceMsg.append(devices.get(j).getRemoteUdpPort() + "\n");
                            out.write(deviceMsg.toString().getBytes());
                        }
                        out.close();
                    } else {
                        System.out.println("无法识别命令" + args[i]);
                    }
                }

            } else if (args[0].equals("udp") && args.length >= 2) {
                if (args[1].equals("-t")) {
                    String callbackId = Tools.ramdom();
                    PushCallback pushCallback = new PushCallback() {
                        @Override
                        public void loadObject(Object obj) {
                            super.loadObject(obj);
                            if (obj instanceof Candidate) {
                                Candidate candidate = (Candidate) obj;
                                StringBuffer stringBuffer = new StringBuffer();
                                stringBuffer.append("From\t" + candidate.getFrom() + "\n");

                                stringBuffer.append("Local\t" + candidate.getLocalIp() + ":" + candidate.getLocalPort() + "\n");
                                stringBuffer.append("Remote\t" + candidate.getRemoteIp() + ":" + candidate.getRemotePort() + "\n");
                                stringBuffer.append("Relay\t" + candidate.getRelayIp() + ":" + candidate.getRelayPort() + "\n");
                                stringBuffer.append("Delay\t" + candidate.getDelayTime() + " ms" + "\n\n");
                                try {
                                    out.write(stringBuffer.toString().getBytes());
                                    out.flush();
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                            this.finishOneTask();
                        }

                        public void timeout() {
                            try {
                                String text = new String("时间超时\n".getBytes(), Tools.DEFAULT_ENCODE);
                                out.write(text.getBytes());
                                out.flush();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            this.finishOneTask();
                        }

                        ;

                        public void onFinish() {
                            try {
                                out.close();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    };

                    String localIp = IPUtil.getLocalIPAddress(true);
                    String localPort = netServerManager.getDatagramSocketServer().getLocalPort() + "";
                    //out.write(("Local\t"+localIp + ":"+localPort+"\n\n").getBytes());
                    String msg = MessageManager.udpTest(CacheRepository.getInstance().getDeviceId(), callbackId, localIp, localPort);
                    if (args.length == 2) {
                        pushCallback.setTaskcount(2);
                        pushCallback.setId(callbackId);
                        pushCallback.setTimeout(8000);
                        CallbackManager.getInstance().put(pushCallback);

                        ConnectedByUDP connectedByUDP = netServerManager.getUDPConnectorByAddress(ApplicationConfig.mainServerIp, 12059);
                        connectedByUDP.sendString(msg);

                        connectedByUDP = netServerManager.getUDPConnectorByAddress(ApplicationConfig.secondaryServerIp, 12059);
                        connectedByUDP.sendString(msg);

                        //TODO 默认地址
//										serverNet.sendMessage("120.78.148.180", 12059, MessageManager.udpLogin(serverNet.getid()));
//
//										serverNet.sendMessage("39.108.74.14", 12059, MessageManager.udpLogin(serverNet.getid()));
//
//										result.append("发送udp到 120.78.148.* 成功\n");
//										result.append("发送udp到 39.108.74.* 成功\n");
                    } else if (args.length == 4) {
                        pushCallback.setTaskcount(1);
                        pushCallback.setId(callbackId);
                        pushCallback.setTimeout(8000);
                        CallbackManager.getInstance().put(pushCallback);
                        String ip = args[2];
                        int port = Integer.valueOf(args[3]);

                        ConnectedByUDP connectedByUDP = netServerManager.getUDPConnectorByAddress(ip, port);
                        connectedByUDP.sendString(msg);

//										serverNet.sendMessage(ip, port, MessageManager.udpLogin(serverNet.getid()));
//										result.append("发送udp到 "+ip+":"+port+"\n");
                        //TODO waiting
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                out.write(e.getMessage().getBytes());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }
}
