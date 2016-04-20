package com.nipungupta.helloworld;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Nipun Gupta on 4/20/2016.
 */
public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tempIn = null;
        OutputStream tempOut = null;

        try {
            tempIn = socket.getInputStream();
            tempOut = socket.getOutputStream();
        } catch (IOException e) {

        }

        mmInStream = tempIn;
        mmOutStream = tempOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        while(true) {
            try {
                bytes = mmInStream.read(buffer);
                String jsonString = new String(buffer,0,bytes);
            } catch (IOException e) {
                break;
            }
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {

        }
    }
}
