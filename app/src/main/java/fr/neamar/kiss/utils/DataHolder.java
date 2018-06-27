package fr.neamar.kiss.utils;

import fr.neamar.kiss.Azure.AzureClient;

public class DataHolder {
    private float speed;
    public float getSpeed() {return speed;}
    public void setSpeed(float speed) {this.speed = speed;}

    private boolean locked;
    public boolean isLocked() {return locked;}
    public void setLocked(Boolean locked) {this.locked = locked;}

    private int fingers;
    public int getFingers() {return fingers;}
    public void setFingers(int fingers) {this.fingers = fingers;}

    private AzureClient client;
    public AzureClient getClient() {return client;}
    public void setClient(AzureClient client) {this.client = client;}

    private boolean firstTime;
    public boolean getFirstTime() {return firstTime;}
    public void setFirstTime(boolean firstTime) {this.firstTime = firstTime;}

    private static final DataHolder holder = new DataHolder();

    public DataHolder() {
        speed = 0;
        locked = true;
        usb = false;
        firstTime = true;
    }
    public static DataHolder getInstance() {return holder;}

    private static final String connString = "HostName=TextAndDriveHub.azure-devices.net;DeviceId=MyJavaDevice;SharedAccessKey=Awc1KvGNKTDvSuuB7yPijhlYu1eQfgWPT4hy7WyEL+A=";
    private static final String deviceID = "MyJavaDevice";
    public String getConnString() {return connString;}
    public String getDeviceID() {return  deviceID;}

    public enum Locations {STEERING_WHEEL(2), CAR_RADIO(3), CAR_STICK_SHIFT(4), DASHBOARD(5), FLOOR_MAT(6), HEADREST(7), SEATS(8), NULL(9);
        public final int value;
        private Locations(int value) {
            this.value = value;
        }
    }
    private Locations CarLocation = Locations.NULL;
    public Locations getCarLocation() { return CarLocation; }
    public void setCarLocation(Locations l) { CarLocation = l;}

    private boolean usb;
    public boolean getUsb() {return usb;}
    public void setUsb(boolean usb) {this.usb = usb;}
}
