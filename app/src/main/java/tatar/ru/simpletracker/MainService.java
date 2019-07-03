package tatar.ru.simpletracker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("FieldCanBeLocal")
public class MainService extends Service implements LocationListener {
    private static final String TAG = "MainService";

    private static String CHANNEL_ID = "CHANNEL_ID";
    private static int NOTIFICATION_ID = 22222;

    private LocationManager mLocationManager;
    private ScheduledExecutorService pingScheduler;

    public MainService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        startTracking();
        startPing();
    }

    // @Override
    // public void onDestroy() {
    //     super.onDestroy();
    //
    //     stopTracking();
    //     stopPing();
    // }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        makeForeground();
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void makeForeground() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH
        );

        assert notificationManager != null;
        notificationManager.createNotificationChannel(notificationChannel);
        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID);

        startForeground(NOTIFICATION_ID, buildNotification(builder));
    }

    private Notification buildNotification(Notification.Builder builder) {
        Intent i = new Intent(this, MainActivity.class);
        i.setAction(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        return builder
                .setContentTitle("Test title")
                .setContentText("Test description")
                .setContentIntent(intent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .build();
    }

    private void startPing() {
        pingScheduler = Executors.newSingleThreadScheduledExecutor();
        pingScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                MainService.this.ping();
            }
        }, Constants.PING_DELAY, Constants.PING_DELAY, TimeUnit.SECONDS);
    }

    @SuppressLint("MissingPermission")
    private void startTracking() {
        long minTime = 5 * 1000;

        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, 0, this);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, 0, this);
    }

    private void stopTracking() {
        mLocationManager.removeUpdates(this);
    }

    private void ping() {
        String message = "SERVICE PING: " + Utils.formateDate(new Date());
        sendMessage(message);
    }

    @Override
    public void onLocationChanged(Location location) {
        String message = "SERVICE LOCATION: " + Utils.locationToString(location);
        sendMessage(message);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled");
    }

    private void sendMessage(String message) {
        Log.d(TAG, message);

        Intent intent = new Intent(Constants.ACTION_MESSAGE_BROADCAST);
        intent.putExtra(Constants.EXTRA_MESSAGE, message);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
