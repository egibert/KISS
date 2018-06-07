package fr.neamar.kiss.utils;

import fr.neamar.kiss.Azure.AzureClient;

public class DataHolder {
    private float speed;
    public float getSpeed() {return speed;}
    public void setSpeed(float speed) {this.speed = speed;}

    private boolean locked;
    public boolean isLocked() {return locked;}
    public void setLocked(Boolean locked) {this.locked = locked;}

    private AzureClient client;
    public AzureClient getClient() {return client;}
    public void setClient(AzureClient client) {this.client = client;}

    private static final DataHolder holder = new DataHolder();

    public DataHolder() {
        speed = 0;
        locked = true;
    }
    public static DataHolder getInstance() {return holder;}
}
