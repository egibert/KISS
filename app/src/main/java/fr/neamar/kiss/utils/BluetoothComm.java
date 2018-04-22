package fr.neamar.kiss.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothComm implements Runnable {

    private final String UUID_DEVICE = "00001101-0000-1000-8000-00805F9B34FB";
    private final String MAC = "00:18:E4:00:27:B3";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket = null;

    @Override
    public void run() {
        init();
        connectToDevice(MAC);


        InputStream in = null;
        OutputStream out = null;

        try {
            in = bluetoothSocket.getInputStream();
            out = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        byte[] buffer = new byte[1024];
        int bytes;

        while (true) {
            try {
                bytes = in.read(buffer);
                String strReceived = new String(buffer, 0, bytes);
                final String msgReceived = String.valueOf(bytes) +
                        " bytes received:\n"
                        + strReceived;

                try {
                    Integer fingers = Integer.parseInt(strReceived);
                    DataHolder.getInstance().setLocked(fingers < 4);
                    Log.d("BLUE", strReceived);
                }
                catch (NumberFormatException nfe) {

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

                final String msgConnectionLost = "Connection lost:\n"
                        + e.getMessage();
            }
        }
    }

    // Start and setup bluetooth
    // Returns true if bluetooth is correctly setup otherwise returns false
    private boolean init() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return false;
        }

        //Turn ON BlueTooth if it is OFF
        while (!bluetoothAdapter.isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().enable();
        }

        return true;
    }

    private boolean connectToDevice(String address) {

        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);


        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(UUID_DEVICE));
        } catch (IOException e) {
            Log.e("BT", "exception", e);
            return false;
        }

        try {
            bluetoothSocket.connect();
        } catch (IOException e) {
            Log.e("BT", "exception CONNECT:", e);
            try {
                bluetoothSocket.close();
            } catch (IOException e1) {
                Log.e("BT", "exception CLOSE AFTER CONNECT:", e);
            }
            return false;
        }

        return true;
    }
}





