package com.baige.linux;

import com.baige.connect.BaseConnector;
import com.baige.connect.ConnectedByTCP;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;

public class CommonProcess {

    public static void process(BaseConnector connector, String[] args) {
        try {
            if(connector instanceof ConnectedByTCP){
                ConnectedByTCP connectedByTCP = (ConnectedByTCP) connector;
                process(connectedByTCP.getRunningSocket().getOutputStream(), args);
            }
        } catch (Exception e) {
            connector.sendString(e.getMessage());
        } finally {
            connector.disconnect();
        }
    }

    public static void process(OutputStream outputStream, String[] args) {
        try {
            PipedInputStream ins = process(args);
            if (ins != null) {
                byte[] buf = new byte[1024];
                int size = 0;
                try {
                    while (!Thread.interrupted() && (size = ins.read(buf)) != -1) {
                        outputStream.write(Arrays.copyOfRange(buf, 0, size));
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    if (ins != null) {
                        try {
                            ins.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    outputStream.write("\r\nend\r\n".getBytes());
                }
            } else {
               outputStream.write("未知命令".getBytes());
                outputStream.write("\r\nend\r\n".getBytes());
            }
        } catch (Exception e) {
            try {
                outputStream.write(e.getMessage().getBytes());
                outputStream.write("\r\nend\r\n".getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static PipedInputStream process(String[] args) {
        try {
            final PipedOutputStream out = new PipedOutputStream();
            PipedInputStream ins = new PipedInputStream();
            ins.connect(out);
            AjaxProcessThread ajaxProcessThread = new AjaxProcessThread(out, args);
            ajaxProcessThread.run();
            return ins;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
