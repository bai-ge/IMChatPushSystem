package com.baige.linux;

import com.baige.common.Parm;
import com.baige.util.LogHelper;
import com.baige.util.StringValidation;
import com.baige.util.Tools;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommonProxy {

    private static final String TAG = CommonProxy.class.getSimpleName();

    private int port = 12058;

    private boolean listening;

    private ServerSocket runningServerSocket;

    private CommonProxy.ListenThread listenThread;

    private static ExecutorService fixedThreadPool = null;

    public CommonProxy() {
        fixedThreadPool = Executors.newFixedThreadPool(5);//创建最多能并发运行5个线程的线程池
    }

    public boolean beginListen() {
        if (isListening()) {
            return false;
        }

        if (getRunningServerSocket() == null) {
            return false;
        }
        setListening(true);
        __i__onSocketServerBeginListen();
        LogHelper.getInstance().debug(this, "远控端口："+getPort());
        return true;
    }

    public boolean beginListen(int port) {
        if (isListening()) {
            return false;
        }

        setPort(port);

        if (getRunningServerSocket() == null) {
            return false;
        }
        setListening(true);
        __i__onSocketServerBeginListen();
        return true;
    }

    protected CommonProxy setListening(boolean listening) {
        this.listening = listening;
        return this;
    }

    protected CommonProxy setListenThread(ListenThread listenThread) {
        this.listenThread = listenThread;
        return this;
    }

    protected CommonProxy setRunningServerSocket(ServerSocket runningServerSocket) {
        this.runningServerSocket = runningServerSocket;
        return this;
    }


    protected ServerSocket getRunningServerSocket() {
        if (this.runningServerSocket == null) {
            try {
                this.runningServerSocket = new ServerSocket(getPort());
//                this.runningServerSocket.setSoTimeout(5000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.runningServerSocket;
    }

    public int getPort() {
        return this.port;
    }


    protected CommonProxy setPort(int port) {
        if (!StringValidation.validateRegex("" + port, StringValidation.RegexPort)) {
            throw new IllegalArgumentException("we need a correct remote port to listen");
        }

        if (isListening()) {
            return this;
        }

        this.port = port;
        return this;
    }

    public boolean isListening() {
        return this.listening;
    }

    protected ListenThread getListenThread() {
        if (this.listenThread == null) {
            this.listenThread = new ListenThread();
        }
        return this.listenThread;
    }

    private boolean __i__checkServerSocketAvailable() {
        return getRunningServerSocket() != null && !getRunningServerSocket().isClosed();
    }

    private void __i__onSocketServerBeginListen() {
        getListenThread().start();
    }

    public void close(){
        if(__i__checkServerSocketAvailable()){
            try {
                runningServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private class ListenThread extends Thread {
        private boolean running;

        protected ListenThread setRunning(boolean running) {
            this.running = running;
            return this;
        }

        protected boolean isRunning() {
            return this.running;
        }

        @Override
        public void run() {
            super.run();
            setRunning(true);
            while (!Thread.interrupted()
                    && __i__checkServerSocketAvailable()) {
                Socket socket = null;
                try {
                    socket = getRunningServerSocket().accept();
                    LogHelper.getInstance().verbose(this, "客户连接成功");
                    ClientThread clientThread = new ClientThread(socket);
//                    fixedThreadPool.submit(clientThread);
                    clientThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            setRunning(false);
            setListening(false);
            setListenThread(null);
            setRunningServerSocket(null);
        }
    }

    private class ClientThread extends Thread {
        Socket socket = null;
        BufferedReader reader = null;

        protected ClientThread(Socket socket) {
            LogHelper.getInstance().verbose(this, "新建客户端"+socket.getInetAddress());
            this.socket = socket;
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            LogHelper.getInstance().verbose(this, "开始解析命令1");
            try {
                while (reader != null
                        && socket != null
                        && !socket.isClosed()
                        && !this.isInterrupted()) {
                    LogHelper.getInstance().verbose(this,"开始解析命令2");
                    String msg = reader.readLine();
                    LogHelper.getInstance().verbose(this, "收到命令："+msg);
                    if (!Tools.isEmpty(msg)) {
                        try {
                            JSONObject json = new JSONObject(msg);
                            if (json.has(Parm.CMD)) {
                                String cmd = json.getString(Parm.CMD);
                                String[] args = cmd.split("#");
                                CommonProcess.process(socket.getOutputStream(), args);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
