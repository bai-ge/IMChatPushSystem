package com.baige.linux;

import com.baige.common.Parm;
import com.baige.connect.NetServerManager;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Scanner;

public class Daemon {
    public final static int DEFALUT_PORT = 12056;

    public NetServerManager netServerManager;

    private CacheRepository cacheRepository;

    public Daemon() {
        LogHelper logHelper = LogHelper.getInstance();
        logHelper.setShow(true);
        logHelper.setLevel(LogLevel.DEBUG);
        logHelper.setLogListener(logListener);
        netServerManager = NetServerManager.getInstance();
        cacheRepository = CacheRepository.getInstance();
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        boolean vailable = NetworkTools.isPortAvailable(DEFALUT_PORT);
        if (vailable) {
            Daemon daemon = new Daemon();
        } else {
            help();
//            clientAgent();
            testInput();
        }
    }


    public static int usage(StringBuffer result) {
//        result.append(copyright + "\n");
        result.append("系统命令\n");
        result.append("清屏\t\tcls\n");
        result.append("停止运行\t\tCTRL+Q\n");
        result.append("输出时间\t\ttime\n");
        result.append("显示错误\t\terr\n");
        result.append("命令填充\t\tTABLE\n");
        result.append("查看历史命令\tUP/DOWM\n");
        result.append("使用规则\t\t-usage\n");
        return 0;
    }

    private static void help() {
        StringBuffer stringBuffer = new StringBuffer();
        usage(stringBuffer);
        System.out.println(stringBuffer.toString());
    }

    private static void clientAgent() {

        try {
            SocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), 12058);
            Socket socket = new Socket();
            socket.connect(address, 8000);
            System.out.println("connect success");
            Scanner sc = new Scanner(System.in);
            System.out.print(">");
            String input = sc.nextLine();
            System.out.println(input);
            while (!input.equals("exit")) {
                if (Tools.isEmpty(input)) {
                    System.out.print(">");
                    continue;
                }
                String[] args = input.split(" ");
                StringBuffer buffer = new StringBuffer();
                buffer.append(args[0]);
                for (int i = 1; i < args.length; i++) {
                    buffer.append("#" + args[i]);
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Parm.CMD, buffer.toString());
                String send = jsonObject.toString() + "\r\n";
                socket.getOutputStream().write(send.getBytes());
                socket.getOutputStream().flush();
                System.out.println("发送数据" + send);
                //读取相应
                BufferedReader reader = null;
                StringBuffer response = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String lines;
                while ((lines = reader.readLine()) != null) {
                    lines = new String(lines.getBytes(), "utf-8");
                    if (lines.equals("end")) {
                        break;
                    }
                    response.append(lines);
                    System.out.println(lines);
                }
                System.out.println(response.toString());
                System.out.print(">");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("close ");
    }

    protected static void testInput() {
        try {
            SocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), 12058);
            Socket socket = new Socket();
            socket.connect(address, 8000);
            System.out.println("connect success");
            Scanner scanner = new Scanner(System.in);
            System.out.print(">");
            String input = "";
            while (scanner.hasNext()) {
                input = scanner.nextLine();
                if (Tools.isEmpty(input)) {
                    System.out.print(">");
                    continue;
                }
                if (input.equals("exit")) {
                    break;
                }
                String[] args = input.split(" ");
                StringBuffer buffer = new StringBuffer();
                buffer.append(args[0]);
                for (int i = 1; i < args.length; i++) {
                    buffer.append("#" + args[i]);
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Parm.CMD, buffer.toString());
                String send = jsonObject.toString() + "\r\n";

                //发送数据
                socket.getOutputStream().write(send.getBytes());
                socket.getOutputStream().flush();
                System.out.println(send);

                //读取相应
                BufferedReader reader = null;
                StringBuffer response = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String lines;
                while ((lines = reader.readLine()) != null) {
                    lines = new String(lines.getBytes(), "utf-8");
                    if (lines.equals("end")) {
                        break;
                    }
//                    response.append(lines);
                    System.out.println(lines);
                }
//                System.out.println(response.toString());


                System.out.print(">");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("close ");
    }

    private LogHelper.LogListener logListener = new LogHelper.LogListener() {
        @Override
        public void showLog(LogBean log) {
            System.out.println(log.toString());
        }
    };
}
