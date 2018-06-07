package fr.neamar.kiss.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.androidhiddencamera.CameraCallbacks;
import com.androidhiddencamera.CameraConfig;
import com.androidhiddencamera.CameraError;
import com.androidhiddencamera.CameraPreview;
import com.androidhiddencamera.HiddenCameraUtils;
import com.androidhiddencamera.config.CameraFacing;
import com.androidhiddencamera.config.CameraImageFormat;
import com.androidhiddencamera.config.CameraResolution;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import fr.neamar.kiss.Classifier;
import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.R;
import fr.neamar.kiss.TensorFlowImageClassifier;
import fr.neamar.kiss.utils.DataHolder;

public class SpeedTracker extends Service implements CameraCallbacks {

    private Notification.Builder mBuilder;
    private static int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "channel_01";
    final Handler mHandler = new Handler();
    private final static int INTERVAL = 1000 * 20; //5 Minute

    private CameraPreview mCameraPreview;
    private WindowManager mWindowManager;

    private Classifier classifier;

    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "final_result";

    private static final String MODEL_FILE = "file:///android_asset/graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/labels.txt";


    @Override
    public void onCreate() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

            if (HiddenCameraUtils.canOverDrawOtherApps(this)) {

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {

                    CameraConfig cameraConfig = new CameraConfig()
                            .getBuilder(SpeedTracker.this)
                            .setCameraFacing(CameraFacing.REAR_FACING_CAMERA)
                            .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
                            .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                            .build();

                    startCamera(cameraConfig);
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    CharSequence name = getString(R.string.app_name);
                    // Create the channel for the notification
                    NotificationChannel mChannel =
                            new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

                    // Set the Notification Channel for the Notification Manager.
                    mNotificationManager.createNotificationChannel(mChannel);
                    startForeground(NOTIFICATION_ID, getNotification());
                    startRepeatingTask();

                } else {
                    CameraConfig cameraConfig = new CameraConfig()
                            .getBuilder(SpeedTracker.this)
                            .setCameraFacing(CameraFacing.REAR_FACING_CAMERA)
                            .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
                            .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                            .build();

                    startCamera(cameraConfig);
                    startForeground(1, getNotification());
                    startRepeatingTask();
                }


            } else {

                //Open settings to grant permission for "Draw other apps".
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
            }
        } else {
            //TODO Ask your parent activity for providing runtime permission
            Toast.makeText(this, "Camera permission not available", Toast.LENGTH_SHORT).show();
        }

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

        classifier = TensorFlowImageClassifier.create(
                this.getAssets(),
                MODEL_FILE,
                LABEL_FILE,
                INPUT_SIZE,
                IMAGE_MEAN,
                IMAGE_STD,
                INPUT_NAME,
                OUTPUT_NAME);

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
    @Override
    public void onImageCapture(@NonNull File imageFile) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        //Do something with the bitmap
        //   saveImage(DemoCamService.this, bitmap, imageFile.getName(), "jpeg");

        Bitmap resizedbitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        final List<Classifier.Recognition> results = classifier.recognizeImage(resizedbitmap);
        Toast.makeText(this, results.get(0).toString(), Toast.LENGTH_SHORT).show();


        Log.d("Image capture", imageFile.getAbsolutePath() + "" + imageFile.getName());
        //     stopSelf();
        try {
            File f = savebitmap(bitmap);
            Log.d("Image capture", f.getAbsolutePath() + "" + f.getName());
            //       stopCamera();
//           onCreate();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCameraError(@CameraError.CameraErrorCodes int errorCode) {
        switch (errorCode) {
            case CameraError.ERROR_CAMERA_OPEN_FAILED:
                //Camera open failed. Probably because another application
                //is using the camera
                Toast.makeText(this, "Cannot open camera.", Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_IMAGE_WRITE_FAILED:
                //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
                Toast.makeText(this, "Cannot write image captured by camera.", Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE:
                //camera permission is not available
                //Ask for the camera permission before initializing it.
                Toast.makeText(this, "Camera permission not available.", Toast.LENGTH_LONG).show();
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION:
                //Display information dialog to the user with steps to grant "Draw over other app"
                //permission for the app.
                HiddenCameraUtils.openDrawOverPermissionSetting(this);
                break;
            case CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA:
                Toast.makeText(this, "Your device does not have front camera.", Toast.LENGTH_LONG).show();
                break;
        }

        //      stopSelf();
    }

    private Notification getNotification() {
        CharSequence text = "app is running";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(SpeedTracker.this, CHANNEL_ID)
                .setContentText(text)
                .setContentTitle("Kiss app")
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }


    Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            //     new SendPostRequest1().execute();
            CameraConfig cameraConfig = new CameraConfig()
                    .getBuilder(SpeedTracker.this)
                    .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                    .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
                    .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                    .build();

            if (ActivityCompat.checkSelfPermission(SpeedTracker.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startCamera(cameraConfig);
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    takePicture();
                }
            }, 2000);

            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };

    void startRepeatingTask()
    {
        mHandlerTask.run();

    }
    @RequiresPermission(allOf = {Manifest.permission.CAMERA, Manifest.permission.SYSTEM_ALERT_WINDOW})
    public void startCamera(CameraConfig cameraConfig) {

        if (!HiddenCameraUtils.canOverDrawOtherApps(this)) {    //Check if the draw over other app permission is available.

            onCameraError(CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION);
        } else if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) { //check if the camera permission is available

            //Throw error if the camera permission not available
            onCameraError(CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE);
        } else if (cameraConfig.getFacing() == CameraFacing.FRONT_FACING_CAMERA
                && !HiddenCameraUtils.isFrontCameraAvailable(this)) {   //Check if for the front camera

            onCameraError(CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA);
        } else {

            //Add the camera preview surface to the root of the activity view.
            if (mCameraPreview == null) {

                mCameraPreview = addPreView();

            }

            else{

                mCameraPreview.stopPreviewAndFreeCamera();
                mCameraPreview = addPreView();

            }
            mCameraPreview.startCameraInternal(cameraConfig);
        }
    }

    /**
     * Call this method to capture the image using the camera you initialized. Don't forget to
     * initialize the camera using {@link #startCamera(CameraConfig)} before using this function.
     */
    public void takePicture() {
        if (mCameraPreview != null) {
            if (mCameraPreview.isSafeToTakePictureInternal()) {
                mCameraPreview.takePictureInternal();
            }
        } else {
            throw new RuntimeException("Background camera not initialized. Call startCamera() to initialize the camera.");
        }
    }

    /**
     * Stop and release the camera forcefully.
     */
    public void stopCamera() {
        if (mCameraPreview != null) {
            mWindowManager.removeView(mCameraPreview);
            mCameraPreview.stopPreviewAndFreeCamera();
        }
    }

    /**
     * Add camera preview to the root of the activity layout.
     *
     * @return {@link CameraPreview} that was added to the view.
     */
    private CameraPreview addPreView() {
        //create fake camera view
        CameraPreview cameraSourceCameraPreview = new CameraPreview(this, this);
        cameraSourceCameraPreview.setLayoutParams(new ViewGroup
                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1,
                Build.VERSION.SDK_INT < Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY :
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        mWindowManager.addView(cameraSourceCameraPreview, params);
        return cameraSourceCameraPreview;
    }


    public void saveImage(Context context, Bitmap bitmap, String name, String extension){
        name = name + "." + extension;
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static File savebitmap(Bitmap bmp) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c);
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, bytes);
        File f = new File(Environment.getExternalStorageDirectory()
                + File.separator +"kiss"+c+".jpeg");
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();
        return f;
    }
}
