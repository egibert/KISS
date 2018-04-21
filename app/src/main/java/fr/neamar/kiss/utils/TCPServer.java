package fr.neamar.kiss.utils;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer extends IntentService {
    private ServerSocket welcomeSocket;
    private Socket connectionSocket;
    private String msg;

    public TCPServer() {
        super("TCP SERVER");
    }

    public TCPServer(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        Integer port = workIntent.getIntExtra("port", 8080);
        try {
            welcomeSocket = new ServerSocket(port);
            Log.d("TCP", "SERVER STARTED");
        } catch (IOException e) {
            Log.e("TCP", e.getMessage());
        }

        while(true) {
            try {
                connectionSocket = welcomeSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader inFromClient = null;
            try {
                inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String message = null;
            try {
                message = inFromClient.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("TCP","Received: " + message);
        }
    }
}
