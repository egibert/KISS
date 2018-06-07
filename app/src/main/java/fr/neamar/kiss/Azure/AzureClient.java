package fr.neamar.kiss.Azure;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageCallback;

import java.io.IOException;
import java.net.URISyntaxException;

public class AzureClient implements IotHubEventCallback {

    private static final String connString = "HostName=TextAndDriveHub.azure-devices.net;DeviceId=MyJavaDevice;SharedAccessKey=Awc1KvGNKTDvSuuB7yPijhlYu1eQfgWPT4hy7WyEL+A=";
    private static final String deviceID = "MyJavaDevice";
    private static final IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;
    private static DeviceClient client;

    public AzureClient() {
        // Connect to the IoT hub.
        try {
            client = new DeviceClient(connString, protocol);
            MessageCallback callback = new AppMessageCallback();
            client.setMessageCallback(callback, null);
            client.open();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void SendMessage(Message msg) {
        try {
            Object lockobj = new Object();
            client.sendEventAsync(msg, this, lockobj);

            synchronized (lockobj) {
                lockobj.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(IotHubStatusCode responseStatus, Object callbackContext) {
        System.out.println("IoT Hub responded to message with status: " + responseStatus.name());
        if (callbackContext != null) {
            synchronized (callbackContext) {
                callbackContext.notify();
            }
        }
    }
}
