package com.nipungupta.helloworld;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Nipun Gupta on 4/20/2016.
 */
public class AcceptThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;
    private Handler mHandler;
    private static UUID MY_UUID = UUID.fromString("446118f0-8b1e-11e2-9e96-0800200c9a66");

    public AcceptThread(BluetoothAdapter mBtAdapter, Handler handler) {
        mHandler = handler;
        BluetoothServerSocket temp = null;
        try {
            temp = mBtAdapter.listenUsingRfcommWithServiceRecord("MyService", MY_UUID);
        } catch (IOException e) {

        }
        mmServerSocket = temp;
    }

    public void run() {
        BluetoothSocket socket = null;
        while(true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            }

            if(socket != null) {
                ConnectedThread connected = new ConnectedThread(socket, mHandler);
                connected.start();
                try {
                    mmServerSocket.close();
                } catch(IOException e) {

                }
                break;
            }
        }
    }

    public void cancel() {
        try {
            mmServerSocket.close();
        } catch(IOException e) {

        }
    }
}
