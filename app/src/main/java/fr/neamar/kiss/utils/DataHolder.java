package fr.neamar.kiss.utils;

public class DataHolder {
    private boolean stopped;
    public boolean isStopped() {return stopped;}
    public void setStopped(boolean stopped) {this.stopped = stopped;}

    private boolean locked;
    public boolean isLocked() {return locked;}
    public void setLocked(Boolean locked) {this.locked = locked;}

    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() {return holder;}
}
