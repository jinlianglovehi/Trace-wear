package com.hub;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class HubCli {

    private static final String TAG = HubCli.class.getName();
    private static final String name = "sensorhubd";
    private LocalSocket Client = null;
    private static final String CMD_START = "CTRL-REQ-CAPTURE-START";
    private static final String CMD_AUTO = "CTRL-REQ-CAPTURE-AUTO";
    private static final String CMD_STOP = "CTRL-REQ-CAPTURE-STOP";
    private OutputStream out = null;
    private InputStream in = null;
    private int timeout = 30000;

    private void connect() {
        try {
            Client = new LocalSocket();
            Client.connect(new LocalSocketAddress(name, LocalSocketAddress.Namespace.RESERVED));
            Client.setSoTimeout(timeout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void close() {
        try {
            out.close();
            in.close();
            Client.close();
            Client = null;
            in = null;
            out = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int sendCommand(String cmd) {
        byte data[] = cmd.getBytes();
        try {
            out = Client.getOutputStream();
            out.write(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;

    }

    private String recvAck() {
        byte ack[] = new byte[1024];
        int n = 0;
        String res = null;
        try {
            in = Client.getInputStream();
            n = in.read(ack);
            res = new String(ack, 0, n);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    public int startCapture() {
        String ack;
        connect();
        sendCommand(CMD_START);
        ack = recvAck();
        if ((ack == null) || !ack.equals("OK")) {
            close();
            return -1;
        }
        close();
        return 0;
    }

    public int autoCapture() {
        String ack;
        connect();
        sendCommand(CMD_AUTO);
        ack = recvAck();
        if ((ack == null) || !ack.equals("OK")) {
            close();
            return -1;
        }
        close();
        return 0;
    }

    public String stopCapture() {

        String ack;
        connect();
        sendCommand(CMD_STOP);
        ack = recvAck();
        if (ack == null) {

            close();
            return null;
        }
        String result[] = ack.split(" ");
        if (result.length != 2) {
            close();
            return null;
        }
        if (!result[0].equals("OK")) {
            close();
            return null;
        }
        close();
        return result[1];
    }


}
