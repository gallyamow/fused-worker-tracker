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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("FieldCanBeLocal")
public class MainService extends Service implements LocationListener {
    private static final String TAG = MainService.class.getSimpleName();

    private static String CHANNEL_ID = "CHANNEL_ID";
    private static int NOTIFICATION_ID = 22222;

    private LocationManager mLocationManager;
    private ScheduledExecutorService mPingScheduler;

    public MainService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        startTracking();
        startPing();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        int res = super.onStartCommand(intent, flags, startId);

        makeForeground();
        return res;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onLocationChanged(Location location) {
        sendLocation(location);
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

    @SuppressLint("MissingPermission")
    private void startTracking() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        mLocationManager.requestLocationUpdates(
                Constants.LOCATION_UPDATE_INTERVAL_MS,
                0,
                criteria,
                this,
                Looper.getMainLooper()
        );
    }

    private void sendPing() {
        String message = "SERVICE PING: " + Utils.formatDate(new Date());
        Utils.sendMessage(getApplicationContext(), message);
    }

    private void sendLocation(@NonNull Location location) {
        String message = "SERVICE LOCATION: " + Utils.locationToString(location);
        Utils.sendMessage(getApplicationContext(), message);
    }

    private void makeForeground() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH
            );
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        startForeground(NOTIFICATION_ID, buildNotification(builder));
    }

    private Notification buildNotification(NotificationCompat.Builder builder) {
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
        if (mPingScheduler != null) {
            mPingScheduler.shutdown();
            mPingScheduler = null;
        }

        mPingScheduler = Executors.newSingleThreadScheduledExecutor();
        mPingScheduler.scheduleAtFixedRate(() -> MainService.this.sendPing(), Constants.PING_DELAY_MS, Constants.PING_DELAY_MS, TimeUnit.MILLISECONDS);
    }

}
