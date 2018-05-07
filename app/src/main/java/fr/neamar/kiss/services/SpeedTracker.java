package fr.neamar.kiss.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.Log;

import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.R;
import fr.neamar.kiss.utils.DataHolder;

public class SpeedTracker extends Service {

    private Notification.Builder mBuilder;
    private static int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {

        initializeNotificationBuilder();

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                float speed = location.getSpeed() * 3600 / 1000;
                updateNotificationSpeed(speed);
                DataHolder.getInstance().setSpeed(speed);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }


        startForeground(NOTIFICATION_ID, getNotificationBuilder().build());
    }

    private void initializeNotificationBuilder() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        mBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_car)
                .setContentTitle("APP locking enabled")
                .setContentText("Tracking speed...")
                .setContentIntent(pendingIntent)
                .setOngoing(true);
    }

    private Notification.Builder getNotificationBuilder() {
        return this.mBuilder;
    }

    public void updateNotificationSpeed(float speed) {
        Notification.Builder b = getNotificationBuilder();

        b.setContentText("SPEED: " + Float.toString(speed));
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, b.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
