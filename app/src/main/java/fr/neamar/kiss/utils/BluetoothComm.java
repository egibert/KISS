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

public class BluetoothComm {

    private final String UUID_DEVICE = "00001101-0000-1000-8000-00805F9B34FB";
    private final String MAC = "20:FA:BB:02:1B:DE";

    private Context context;

    private static BluetoothComm single_instance;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket = null;
    private boolean locked;

    public static BluetoothComm getInstance(Context c)
    {
        if (single_instance == null)
            single_instance = new BluetoothComm(c);

        return single_instance;
    }

    public BluetoothComm(Context c) {
        locked = false;
        context = c;
        init();
        connectToDevice(MAC);
    }

    public boolean isLocked() {
        return locked;
    }

    // Start and setup bluetooth
    // Returns true if bluetooth is correctly setup otherwise returns false
    private boolean init() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return false;
        }

        //Turn ON BlueTooth if it is OFF
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) context).startActivityForResult(enableIntent, 1);
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

        ThreadConnected myThreadConnected = new ThreadConnected(bluetoothSocket);
        myThreadConnected.start();
        return true;
     }

    /*
    ThreadConnected:
    Background Thread to handle Bluetooth data communication
    after connected
    */
    private class ThreadConnected extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;

        public ThreadConnected(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    String strReceived = new String(buffer, 0, bytes);
                    final String msgReceived = String.valueOf(bytes) +
                            " bytes received:\n"
                            + strReceived;
                    locked = false;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost:\n"
                            + e.getMessage();
                }
            }
        }
    }
}





