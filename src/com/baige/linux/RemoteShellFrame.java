package com.baige.linux;

import com.baige.common.Parm;
import com.baige.imchat.OnShellListener;
import com.baige.imchat.ShellFrame;
import com.baige.util.NetworkTools;
import com.baige.util.StringValidation;
import com.baige.util.Tools;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class RemoteShellFrame {
    Socket socket;
    ShellFrame shellFrame;
    String header;

    public RemoteShellFrame() {
        header = "root@" + System.getProperty("user.name");
        shellFrame = new ShellFrame();
        shellFrame.registerShellListener(onShellListener);
        shellFrame.setVisible(true);
    }

    protected void connect(PipedOutputStream out, String ip, int port) throws IOException {
        SocketAddress address = new InetSocketAddress(ip, port);
        Socket socket = new Socket();
        String msg = "开始连接" + ip + ":" + port + "\r\n";
        out.write(msg.getBytes());
        socket.connect(address, 8000);
        this.socket = socket;
        msg = "连接成功" + ip + ":" + port + "\r\n";
        out.write(msg.getBytes());
        header = ip+"#";
    }

    protected boolean isConnect(){
        if(socket != null && socket.isConnected()){
            return true;
        }
        return false;
    }

    protected void close(PipedOutputStream out) throws IOException {
        if(socket != null && !socket.isClosed()){
            socket.close();
            String msg = "关闭连接\r\n";
            out.write(msg.getBytes());
            header = "root@" + System.getProperty("user.name");
        }
    }

    protected PipedInputStream process(String[] args) {
        try {
            final PipedOutputStream out = new PipedOutputStream();
            PipedInputStream ins = new PipedInputStream();
            ins.connect(out);
            ProcessThread processThread = new ProcessThread(out, args);
            processThread.run();
            return ins;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private OnShellListener onShellListener = new OnShellListener.SimpleOnShellListener() {

        @Override
        public int usage(StringBuffer result) {
            result.append("连接远程服务器\tcmd -c <ip><port>\n");
            result.append("关闭远程服务器\tclose\n");
            result.append("退出\t\texit\n");
            return 0;
        }

        @Override
        public PipedInputStream execute(String command) {
            String[] args = command.split(" ");
            return process(args);

        }

        @Override
        public int autoCompletion(StringBuffer result, String command) {
            return 0;
        }

        @Override
        public void showError(StringBuffer error) {

        }

        @Override
        public String getHeader() {
            return header;
        }
    };

    private class ProcessThread extends AjaxPrecessAbstract {

        public ProcessThread(PipedOutputStream out, String[] args) {
            super(out, args);
        }

        @Override
        protected boolean canExecute() {
            if (args == null || args.length <= 0) {
                return false;
            }
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
                if (args[0].equals("cmd") && args[1].equals("-c")) {
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
            if (args.length == 1) {
                if (args[0].equals("exit")) {
                    return true;
                }
                if (args[0].equals("close")) {
                    return true;
                }
            }
            if (args.length == 4) {
                if (args[0].equals("cmd")
                        && args[1].equals("-c")
                        && StringValidation.validateRegex(args[2], StringValidation.RegexIP)
                        && StringValidation.validateRegex(args[3], StringValidation.RegexPort)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void run() {
            try {
                boolean isExecute = false;
                if (!canExecute()) {
                    out.write("未知命令！".getBytes());
                    return;
                }
                if (args.length == 1) {
                    if (args[0].equals("exit")) {
                        isExecute = true;
                        try {
                            close(out);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (args.length == 4) {
                    if (args[0].equals("cmd")
                            && args[1].equals("-c")
                            && StringValidation.validateRegex(args[2], StringValidation.RegexIP)
                            && StringValidation.validateRegex(args[3], StringValidation.RegexPort)) {
                       isExecute = true;
                       connect(out, args[2], Integer.valueOf(args[3]));
                    }
                }
                if (args.length == 2) {
                    if (args[0].equals("cmd") && args[1].equals("-c")) {
                        isExecute = true;
                        connect(out, "120.79.203.153", 12058);
                    }
                }
                if(! isExecute && isConnect()){
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(args[0]);
                    for (int i = 1; i < args.length; i++) {
                        buffer.append("#" + args[i]);
                    }
                    JSONObject jsonObject = new JSONObject();
                    try {
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
                            out.write((lines+"\r\n").getBytes());
                            out.flush();
                            System.out.println(lines);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        RemoteShellFrame remoteShellFrame = new RemoteShellFrame();
    }
}

